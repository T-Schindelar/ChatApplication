package ChatApplication.Library;

import javafx.application.Platform;
import javafx.scene.control.ListView;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Server {
    private final int port;
    private final ServerSocket server;
    private final List<User> clients;
    private List<Account> accounts;
    private final AccountDbService service;
    private final HashMap<String, ArrayList<User>> rooms;
    private String selectedRoom;

    public Server(int port) throws Exception {
        this.port = port;
        this.server = new ServerSocket(port);
        this.clients = new ArrayList<User>();
        this.service = new AccountDbService();
        this.accounts = service.getAllAccounts();
        this.rooms = new HashMap<String, ArrayList<User>>();
        this.rooms.put("Lobby", new ArrayList<User>());
        this.selectedRoom = "";
    }

    public User listenForNewClients() throws IOException {
        // accepts a new client
        Socket clientSocket = server.accept();
        ObjectInputStream oin = new ObjectInputStream(clientSocket.getInputStream());

        // create a new initial client
        return new User(clientSocket, "", oin);
    }


    // ------------------------- ACCOUNT MANAGEMENT  -------------------------
    // handles login and registration process
    public void accountAndEntryManagement(User client) {
        try {
            while (true) {
                Mode mode = (Mode) client.getOin().readObject();
                Account loginAccount = (Account) client.getOin().readObject();
                // registration
                boolean registrationSuccessful = true;
                switch (mode) {
                    case REGISTRATION:
                        for (Account account : accounts) {
                            if (account.getName().equalsIgnoreCase(loginAccount.getName())) {
                                sendMessage(client, new Message("Server", Mode.ERROR,
                                        "Bitte wählen Sie einen anderen Namen."));
                                registrationSuccessful = false;
                                break;
                            }
                        }
                        if (registrationSuccessful) {
                            client.setName(loginAccount.getName());
                            addUser(client);
                            addAccount(loginAccount);
                            sendMessage(client, new Message("Server", Mode.MESSAGE, ""));
                            return;
                        }
                    case LOGIN:
                        if (service.get(loginAccount.getName()).isBanned()) {
                            sendMessage(client, new Message("Server", Mode.ERROR,
                                    "Dieses Benutzerkonto wurde gesperrt."));
                        } else if (accounts.contains(loginAccount) && !isLoggedIn(loginAccount)) {
                            client.setName(loginAccount.getName());
                            addUser(client);
                            sendMessage(client, new Message("Server", Mode.MESSAGE, ""));
                            return;
                        } else {
                            sendMessage(client, new Message("Server", Mode.ERROR,
                                    "Fehler! Bitte versuchen Sie es erneut."));
                        }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // add a account to the list and to the database
    public void addAccount(Account account) {
        accounts.add(account);
        service.set(account);
    }

    // removes a account from the account list
    public void removeAccount(Account account) {
        accounts.remove(account);
    }

    // changes client name in the server and the account in the database
    public void changeAccountName(String oldName, String newName) {
        User u = getClientFromClientsByName(oldName);
        u.setName(newName);
        service.updateName(oldName, newName);
        accounts = service.getAllAccounts();
    }

    // changes account password in the database
    public void changeAccountPassword(String name, String newPassword) {
        service.updatePassword(name, newPassword);
        accounts = service.getAllAccounts();
    }

    // deletes a account and removes the client
    public void deleteAccount(String name) {
        removeAccount(getAccountFromAccountsByName(name));
        service.delete(name);
        User client = getClientFromClientsByName(name);
        String room = getRoomNameForUser(client);
        removeUser(client);
        broadcastToRoom(new Message("Server", Mode.MESSAGE, name + " hat den Chat verlassen.", room));
        broadcastRoomUsers(room);
        sendMessage(client, new Message("Server", Mode.DISCONNECT,
                "Ihr Benutzerkonto wurde erfolgreich gelöscht.\nDie Anwendung schließt sich in 20 Sekunden!\n"));
    }

    // returns a account from the accounts list by name
    public Account getAccountFromAccountsByName(String name) {
        for (Account account : accounts) {
            if (account.getName().equals(name))
                return account;
        }
        return null;
    }

    // checks if client is already logged in
    public boolean isLoggedIn(Account account) {
        for (User client : clients) {
            if (client.getName().equalsIgnoreCase(account.getName())) {
                return true;
            }
        }
        return false;
    }


    // ------------------------- SEND MESSAGES  -------------------------
    // send message to a specific client
    public void sendMessage(User client, Message message) {
        try {
            client.getOout().writeObject(message);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // sends message to all clients
    public void broadcastMessages(Message message) {
        try {
            for (User client : clients) {
                client.getOout().writeObject(message);
                client.getOout().flush();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // sends message to all room clients
    public void broadcastToRoom(Message message) {
        try {
            ArrayList<User> users = rooms.get(message.getRoom());
            for (User client : users) {
                client.getOout().writeObject(message);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // send list of clients to all clients
    public void broadcastAllUsers() {
        try {
            for (User client : clients) {
                client.getOout().writeObject(new Message("Server", Mode.USER_TRANSMIT, clients.toString()));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // send list of clients to all room clients
    public void broadcastRoomUsers(String roomName) {
        try {
            for (User client : rooms.get(roomName)) {
                client.getOout().writeObject(new Message("Server", Mode.USER_TRANSMIT,
                        rooms.get(roomName).toString(), roomName));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // send list of rooms to all clients
    public void broadcastRooms() {
        try {
            for (User client : clients) {
                client.getOout().writeObject(new Message("Server", Mode.ROOM_TRANSMIT,
                        rooms.keySet().toString()));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    // ------------------------- ROOM MANAGEMENT  -------------------------
    // adds a new room to rooms
    public void addRoom(String name) {
        if (!rooms.containsKey(name)) {
            rooms.put(name, new ArrayList<User>());
            broadcastRooms();
        }
    }

    public void changeRoomName(String room, String newName, ListView list){
        System.out.println(this.selectedRoom);
        if(!this.selectedRoom.isEmpty()){
            rooms.put( newName, rooms.remove( room ));
            System.out.println(rooms);
            broadcastRooms();
            this.selectedRoom = "";
            populateList(rooms.keySet().toArray(new String[0]), list);
        }
    }

    public void deleteRoom(String name){
        if(rooms.keySet().contains(name)){
            for(User u : rooms.get(name)){
                System.out.println(u);
                rooms.get("Lobby").add(u);
                System.out.println(u);
            }
            rooms.remove(name);
            System.out.println(rooms);
            broadcastRooms();
            broadcastRoomUsers("Lobby");
        }
    }

    // adds User client to the entered room and removes it from the old room
    public void addToRoom(Message message) {
        User client = getClientFromClientsByName(message.getClient());
        rooms.get(message.getRoom()).add(client);       // adds client to new room
        rooms.get(message.getText()).remove(client);    // remove client from old room
        broadcastRoomUsers(message.getRoom());  //broadcast to new room
        broadcastRoomUsers(message.getText()); //broadcast to old room
    }

    // returns all room names as a String array
    public String[] getRoomNames() {
        String roomNames = rooms.keySet().toString();
        return roomNames.substring(1, roomNames.length() - 1).split(",");
    }

    // returns room name in which the client currently is
    public String getRoomNameForUser(User client) {
        for (String key : rooms.keySet()) {
            if (rooms.get(key).contains(client)) {
                return key;
            }
        }
        return null;
    }

    // removes a client from a room
    public void removeFromRoom(String roomName, User client) {
        rooms.get(roomName).remove(client);
    }


    // ------------------------- USER/CLIENT MANAGEMENT  -------------------------
    // adds a client to the clients list
    public void addUser(User client) {
        clients.add(client);
        rooms.get("Lobby").add(client);
    }

    // removes a client from the clients list
    public void removeUser(User client) {
        clients.remove(client);
        removeFromRoom(getRoomNameForUser(client), client);
    }

    // returns a client from the clients list by name
    public User getClientFromClientsByName(String name) {
        for (User client : clients) {
            if (client.getName().equals(name))
                return client;
        }
        return null;
    }

    // returns all client names as a String array
    public String[] getClientNames() {
        String clientNames = clients.toString();
        return clientNames.substring(1, clientNames.length() - 1).split(",");
    }

    // disconnects a client from the clients list
    public void warnUser(String name) {
        sendMessage(getClientFromClientsByName(name), new Message("Server", Mode.MESSAGE,
                "Verwarnung vom Server!"));
    }

    // disconnects a client from the clients list
    public void disconnectUser(String name) {
        User client = getClientFromClientsByName(name);
        String room = getRoomNameForUser(client);
        sendMessage(client, new Message("Server", Mode.DISCONNECT, "Sie wurden vom Server ausgschlossen!\n" +
                "Die Anwendung schließt sich in 20 Sekunden!\n"));
        removeUser(client);
        broadcastToRoom(new Message("Server", Mode.MESSAGE, name + " hat den Chat verlassen.", room));
        broadcastRoomUsers(room);
    }

    // bans a account and removes the client
    public void banAccount(String name) {
        User client = getClientFromClientsByName(name);
        String room = getRoomNameForUser(client);
        removeUser(client);
        removeAccount(getAccountFromAccountsByName(name));
        service.updateBanned(name, true);
        sendMessage(client, new Message("Server", Mode.DISCONNECT, "Ihr Benutzerkonto wurde gesperrt!\n" +
                "Die Anwendung schließt sich in 20 Sekunden!\n"));
        broadcastToRoom(new Message("Server", Mode.MESSAGE, name + " hat den Chat verlassen.", room));
        broadcastRoomUsers(room);
    }

    // ------------------------- GETTER & SETTER -------------------------
    public int getPort() {
        return port;
    }
    public String getSelectedRoom() { return this.selectedRoom; }
    public void setSelectedRoom(String selectedRoom) {
        this.selectedRoom = selectedRoom;
        System.out.println(this.selectedRoom);} //todo
    // setter


    public void populateList(String[] list, ListView object) {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                object.getItems().clear();
                for (String item : list) {
                    object.getItems().add(item.strip());
                }
            }
        });

    }
}

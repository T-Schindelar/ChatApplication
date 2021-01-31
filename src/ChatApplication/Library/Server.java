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
import java.util.Set;

public class Server {
    private final int port;
    private final ServerSocket server;
    private final List<User> clients;
    private List<Account> accounts;
    private final AccountDbService service;
    private final HashMap<String, ArrayList<User>> rooms;
    private final HashMap<String, ArrayList<User>> privateRooms;
    private String selectedRoom;

    public Server(int port) throws Exception {
        this.port = port;
        this.server = new ServerSocket(port);
        this.clients = new ArrayList<User>();
        this.service = new AccountDbService();
        this.accounts = service.getAllAccounts();
        this.rooms = new HashMap<String, ArrayList<User>>();
        this.privateRooms = new HashMap<String, ArrayList<User>>();
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
                        Account account = service.get(loginAccount.getName());
                        if (account == null) {
                            sendMessage(client, new Message("Server", Mode.ERROR,
                                    "Unbekannter Nutzer."));
                        } else if (account.isBanned()){
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
        User client = getClientFromClientsByName(oldName);
        client.setName(newName);
        client.setName(newName);
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
        broadcastToRoom(new Message("Server", Mode.MESSAGE, room, name + " hat den Chat verlassen."));
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
            client.getOout().flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // sends message to all clients
    public void broadcastMessages(Message message) {
        for (User client : clients) {
            sendMessage(client, message);
        }
    }

    // sends message to all room clients
    public void broadcastToRoom(Message message) {
        ArrayList<User> users = rooms.get(message.getRoom());
        for (User client : users) {
            sendMessage(client, message);
        }
    }

    // todo weitermachen, nachrichten nur an private fenster
    // sends message to all room clients
    public void broadcastToPrivateRoom(Message message) {
        System.out.println(message);    // todo
        ArrayList<User> users = privateRooms.get(message.getRoom());
        System.out.println("users: "+ users);      // todo
        for (User client : users) {
            System.out.println(client);      // todo
            sendMessage(client, message);
        }
    }

    // send list of clients to all clients
    public void broadcastAllUsers() {
        for (User client : clients) {
            sendMessage(client, new Message("Server", Mode.USER_TRANSMIT, clients.toString()));
        }
    }

    // send list of clients to all room clients
    public void broadcastRoomUsers(String roomName) {
        for (User client : rooms.get(roomName)) {
            sendMessage(client, new Message("Server", Mode.USER_TRANSMIT, roomName,
                    rooms.get(roomName).toString()));
        }
    }

    // send list of rooms to all clients
    public void broadcastRooms() {
        for (User client : clients) {
            sendMessage(client, new Message("Server", Mode.ROOM_TRANSMIT,
                    rooms.keySet().toString()));
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

    // adds a new private room to rooms
    public void addPrivateRoom(String name){
        if (!privateRooms.containsKey(name)) {
            privateRooms.put(name, new ArrayList<User>());
        }
    }

    // todo nutzer des raums verwalten
    // changes room name
    public void changeRoomName(String room, String newName, ListView list){
        if(!this.selectedRoom.isEmpty()){
            rooms.put(newName, rooms.remove(room));
            broadcastRooms();
            this.selectedRoom = "";
            populateList(rooms.keySet().toArray(new String[0]), list);
        }
    }

    // deletes a room
    public void deleteRoom(String name){
        try{
            if(rooms.keySet().contains(name)){
                for(User client : rooms.get(name)){
                    rooms.get("Lobby").add(client);
                    sendMessage(client, new Message("Server", Mode.UPDATE_ROOM, "Lobby"));
                }
                rooms.remove(name);
                broadcastRooms();
                broadcastRoomUsers("Lobby");
            }
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

//    todo löschen wenn nicht benötigt
//    // sends a message
//    public void updateRoom(String newRoom){
//            for(User client : rooms.get(newRoom)){
//                sendMessage(client, new Message("Server", Mode.UPDATE_ROOM, newRoom));
//            }
//    }

    // adds client to the entered room and removes it from the old room
    public void addToRoom(Message message) {
        User client = getClientFromClientsByName(message.getClient());
        String oldRoom = message.getRoom();
        String newRoom = message.getText();
        rooms.get(oldRoom).remove(client);
        rooms.get(newRoom).add(client);
        broadcastRoomUsers(newRoom);
        broadcastRoomUsers(oldRoom);
        sendMessage(client, new Message("Server", Mode.UPDATE_ROOM, newRoom));
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
        broadcastToRoom(new Message("Server", Mode.MESSAGE, room, name + " hat den Chat verlassen."));
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
        broadcastToRoom(new Message("Server", Mode.MESSAGE, room, name + " hat den Chat verlassen."));
        broadcastRoomUsers(room);
    }

    // ------------------------- GETTER & SETTER -------------------------
    //getter
    public int getPort() {
        return port;
    }
    public String getSelectedRoom() { return this.selectedRoom; }
    public Set<String> getRoomsKeySet() { return rooms.keySet(); }
    public Set<String> getPrivateRoomsKeySet() { return rooms.keySet(); }

    // setter
    public void setSelectedRoom(String selectedRoom) { this.selectedRoom = selectedRoom;
        System.out.println(selectedRoom);}

    // populates the given ListView with the list items
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

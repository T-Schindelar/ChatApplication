package ChatApplication.Library;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.lang.reflect.Array;
import java.net.ServerSocket;
import java.net.Socket;
import java.rmi.server.ExportException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Server {
    private final int port;
    private final ServerSocket server;
    private final List<User> clients;
    private List<Account> accounts;
    private final AccountDbService service;
    private HashMap<String, ArrayList<User>> rooms;

    public Server(int port) throws Exception {
        this.port = port;
        this.server = new ServerSocket(port);
        this.clients = new ArrayList<User>();
        this.service = new AccountDbService();
        this.accounts = service.getAllAccounts();
        this.rooms = new HashMap<String, ArrayList<User>>();
        System.out.println("Port " + port + " ist geöffnet.");
    }

    // todo löschen wenn nicht benötigt

    //    public void run() throws IOException {
    //        ServerSocket server = new ServerSocket(port);
    //        System.out.println("Port " + port + " is now open.");
    //        txtFieldStateServer.setText("Port " + port + " is now open.");
    //
    //        while (true) {
    //            // accepts a new client
    //            Socket clientSocket = server.accept();
    //            ObjectInputStream oin = new ObjectInputStream(clientSocket.getInputStream());
    //
    //            // create a new initial client
    //            User client = new User(clientSocket, "", oin);
    //
    //            // create a new thread for logging in and incoming messages handling
    //            new Thread(new UserHandler( client)).start();
    //        }
    //    }
    //public void run(){
        //leer, da hier nichts gemacht werden muss
    //}

    public User listenForNewClients() throws IOException {
        // accepts a new client
        Socket clientSocket = server.accept();
        ObjectInputStream oin = new ObjectInputStream(clientSocket.getInputStream());

        // create a new initial client
        return new User(clientSocket, "", oin);
    }

    // handles login process and account management
    public void accountAndEntryManagement(User client) {
        try {
            while (true) {
                Mode mode = (Mode) client.getOin().readObject();
                Account loginAccount = (Account) client.getOin().readObject();
                // registration
                boolean registrationSuccessful = true;
                if (mode == Mode.REGISTRATION) {
                    for (Account account : accounts) {
                        if (account.getName().equalsIgnoreCase(loginAccount.getName())) {
                            sendMessage(client, new Message("Server", Mode.ERROR, "Bitte wählen Sie " +
                                    "einen anderen Namen.", ""));
                            registrationSuccessful = false;
                            break;
                        }
                    }
                    if (registrationSuccessful) {
                        client.setName(loginAccount.getName());
                        addUser(client);
                        addAccount(loginAccount);
                        sendMessage(client, new Message("Server", Mode.MESSAGE, "\n------ Willkommen ------", "default"));
                        return;
                    }
                }
                // login
                else {
                    if (accounts.contains(loginAccount) && !isLoggedIn(loginAccount)) {
                        client.setName(loginAccount.getName());
                        addUser(client);
                        sendMessage(client, new Message("Server", Mode.MESSAGE,"\n----- Willkommen zurück " +
                                "-----", "default"));
                        return;
                    } else {
                        sendMessage(client, new Message("Server",Mode.ERROR, "Fehler! Bitte versuchen Sie " +
                                "es erneut.", ""));
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
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

    // send message to a specific client
    public void sendMessage(User client, Message message) throws IOException {
        client.getOout().writeObject(message);
    }

    // send incoming message to all Users
    public void broadcastMessages(Message message) throws IOException {
        for (User client : clients) {
            client.getOout().writeObject(message);
            client.getOout().flush();
        }
    }

    public void broadcastToRoom(Message message) throws IOException{
        ArrayList<User> users = rooms.get(message.getRoom());
        for (User client : users){
            client.getOout().writeObject(new Message("Server", Mode.MESSAGE, clients.toString(), message.getRoom()));
        }
    }

    // send list of clients to all Users
    public void broadcastAllUsers() throws IOException {
        for (User client : clients) {
            client.getOout().writeObject(new Message("Server", Mode.USER_TRANSMIT, clients.toString(), ""));
        }
    }

    public void broadcastRooms() throws IOException {
        for (User client : clients) {
            client.getOout().writeObject(new Message("Server", Mode.ROOM_TRANSMIT, rooms.keySet().toString(), "default"));
        }
    }

    // add a account to the list and to the database
    public void addAccount(Account account) {
        accounts.add(account);
        service.set(account);
    }

    private User getUserByName(String name) throws IOException {
        User u;
        for(User user : clients){
            if(user.getName().equals(name)){
                u = user;
                return u;
            }
        }
        return new User(new Socket(), "", new ObjectInputStream(new FileInputStream(""))); //wird nicht ausgeführt, ist nur da damit der Compiler nicht nervt
    }

    public void addRoom(String client, String name) throws IOException {
        try{
            if(!rooms.containsKey(name)) {
                ArrayList a = new ArrayList<User>();
                a.add(getUserByName(client));
                rooms.put(name, a);
                broadcastRooms();
            }
            else{
                System.out.println("Raum vorhanden");   //todo: richtige Rückmeldung an Nutzer
            }
            System.out.println(rooms);
        }
        catch (Exception e){
            System.out.println("CREATE_ROOM EXCEPTION");
            e.printStackTrace();
        }

    }

    public void addToRoom(Message message) throws IOException {
        ArrayList a = rooms.get(message.getRoom());
        a.add(getUserByName(message.getClient()));
        rooms.put(message.getRoom(), a);
        rooms.remove(message.getText(), message.getClient());
    }

    public void changeAccountName(String oldName, String newName) {
        for (User client : clients) {
            if (client.getName().equals(oldName))
                client.setName(newName);
        }
        service.updateName(oldName, newName);
        accounts = service.getAllAccounts();
    }

    public void changeAccountPassword(String name, String newPassword) {
        service.updatePassword(name, newPassword);
    }

    public void deleteAccount(String name) {
        service.delete(name);
    }

    // add a user to the list
    public void addUser(User user) {
        clients.add(user);
    }

    // delete a user from the list
    public void removeUser(User user) {
        clients.remove(user);
    }

    // getter
    public int getPort() {
        return port;
    }

    // setter

    // todo löschen wenn nicht benötigt
    //    public static void main(String[] args) {
    //        try {
    //            new Server(5000).run();
    //        } catch (Exception e) {
    //            e.printStackTrace();
    //        }
    //    }
}

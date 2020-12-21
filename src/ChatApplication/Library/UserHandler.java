package ChatApplication.Library;

import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class UserHandler implements Runnable {
    private final Server server;
    private final User user;
    private final TextArea txtAreaServerlog;
    private final TextField txtFieldStateServer;
    private final ListView listUser;
    private final ListView listRooms;


    public UserHandler(Server server, User user, TextArea txtAreaServerlog, TextField txtFieldStateServer,
                       ListView listUser, ListView listRooms) {
        this.server = server;
        this.user = user;
        this.txtAreaServerlog = txtAreaServerlog;
        this.txtFieldStateServer = txtFieldStateServer;
        this.listUser = listUser;
        this.listRooms = listRooms;
    }

    public void run() {
        try {
            this.server.accountAndEntryManagement(this.user);
            this.server.broadcastMessages(new Message("Server", user + " joined the server."));
            this.server.broadcastAllUsers();
        } catch (Exception e) {
            e.printStackTrace();
        }
        while (true) {
            try {
                // gets client messages
                Message message = (Message) user.getOin().readObject();
//                System.out.println(message);            // todo löschen
                addMessageToTxtAreaServerlog(message);

                // checks end of thread
                if (message.getText().equals("close")) {
                    server.removeUser(user);
                    server.broadcastMessages(new Message("Server", user + " has left the server."));
                    server.broadcastAllUsers();
                } else {
                    server.broadcastMessages(message);
                }
            } catch (Exception e) {
                System.out.println(user + " left.");
                addMessageToTxtAreaServerlog(new Message(user.toString(), "left"));
                server.removeUser(user);
                try {
                    server.broadcastMessages(new Message("Server", user + " has left the server."));
                    server.broadcastAllUsers();
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }
                return;
            }
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                System.out.println(user + " left.");
                addMessageToTxtAreaServerlog(new Message(user.toString(), "left"));
                server.removeUser(user);
                try {
                    server.broadcastMessages(new Message("Server", user + " has left the server."));
                    server.broadcastAllUsers();
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }
                return;
            }
        }
    }
    public void addMessageToTxtAreaServerlog(Message message) {
        txtAreaServerlog.setText(txtAreaServerlog.getText() + message + "\n");
    }

    public static class Server {
        private final int port;
        private final List<User> clients;
        private final List<Account> accounts;
        private final ServerSocket server;

        public Server(int port) throws Exception {
            this.port = port;
            this.clients = new ArrayList<User>();
            this.accounts = new ArrayList<Account>();
            this.server = new ServerSocket(this.port);
            System.out.println("Port " + port + " is now open.");
        }

        // todo löschen wenn nicht benötigt
    //    public void run() throws IOException {
    //        ServerSocket server = new ServerSocket(this.port);
    //        System.out.println("Port " + port + " is now open.");
    ////        txtFieldStateServer.setText("Port " + port + " is now open.");
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
    //            new Thread(new UserHandler(this, client)).start();
    //        }
    //    }

        public User listenForNewClients() throws IOException {
            // accepts a new client
            Socket clientSocket = server.accept();
            ObjectInputStream oin = new ObjectInputStream(clientSocket.getInputStream());

            // create a new initial client
            return new User(clientSocket, "", oin);
        }

        // todo löschen wenn nicht benötigt
    //    public Thread createThreadUserHandler(User client, TextArea txtAreaServerlog, TextArea txtAreaInfo,
    //                                          TextField txtFieldStateServer, ListView listUser, ListView listRooms) {
    //        return new Thread(new UserHandler(this, client));
    //    }

        // handles login process and account management
        public void accountAndEntryManagement(User client) {
            try {
                while (true) {
                    // message contains command about registration (r) or login (l)
                    Message message = (Message) client.getOin().readObject();
                    Account loginAccount = (Account) client.getOin().readObject();
                    // registration
                    boolean registrationSuccessful = true;
                    if (message.getText().equals("r")) {
                        for (Account account : this.accounts) {
                            if (account.getName().equalsIgnoreCase(loginAccount.getName())) {
                                sendMessage(client, new Message("Server", "Error: User already exists. " +
                                        "Try another name."));
                                registrationSuccessful = false;
                                break;
                            }
                        }
                        if (registrationSuccessful) {
                            client.setName(loginAccount.getName());
                            addUser(client);
                            addAccount(loginAccount);
                            sendMessage(client, new Message("Server", "\n------ Welcome ------"));
                            return;
                        }
                    }
                    // login
                    else {
                        if (this.accounts.contains(loginAccount) && !isLoggedIn(loginAccount)) {
                            client.setName(loginAccount.getName());
                            addUser(client);
                            sendMessage(client, new Message("Server", "\n----- Welcome back -----"));
                            return;
                        } else {
                            sendMessage(client, new Message("Server", "Error: Login failed. Please try again"));
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
            for (User client : this.clients) {
                client.getOout().writeObject(message);
                client.getOout().flush();
            }
        }

        // send list of clients to all Users
        public void broadcastAllUsers() throws IOException {
            for (User client : this.clients) {
                client.getOout().writeObject(new Message("Server", this.clients.toString()));
            }
        }

        // add a account to the list
        public void addAccount(Account account) {
            this.accounts.add(account);
        }

        // add a user to the list
        public void addUser(User user) {
            this.clients.add(user);
        }

        // delete a user from the list
        public void removeUser(User user) {
            this.clients.remove(user);
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
}
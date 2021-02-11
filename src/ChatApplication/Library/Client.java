package ChatApplication.Library;

import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class Client {
    private final String host;
    private final int port;
    private String name;
    private final Socket socket;
    private final ObjectOutputStream oout;
    private final ObjectInputStream oin;
    private String activeRoom;
    private String selectedUser;
    private Thread thread = null;

    // client with specific host
    public Client(String host, int port) throws Exception {
        this.host = host;
        this.port = port;
        this.activeRoom = "Lobby";
        // connect client to server
        this.socket = new Socket(host, port);
        // create object input/output streams
        this.oout = new ObjectOutputStream(socket.getOutputStream());
        this.oin = new ObjectInputStream(socket.getInputStream());
        this.selectedUser = "";
    }

    // client with default host
    public Client(int port) throws Exception {
        this.host = "localhost";
        this.port = port;
        this.activeRoom = "Lobby";
        // connect client to server
        this.socket = new Socket(host, port);
        // create object input/output streams
        this.oout = new ObjectOutputStream(socket.getOutputStream());
        this.oin = new ObjectInputStream(socket.getInputStream());
    }

    // handles login process
    public Message login(Mode mode, String name, String password) {
        if (name.isEmpty() || password.isEmpty()) {
            return new Message("Server", Mode.ERROR, "", "Bitte Namen und Passwort eingeben!");
        }
        try {
            oout.writeObject(mode);
            oout.flush();
            oout.writeObject(new Account(name.strip(), password));
            oout.flush();

            Message message = (Message) oin.readObject();
            return message;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // returns an new thread for the ReceivedMessagesHandler
    public Thread createThreadReceivedMessagesHandler(TextArea txtAreaChat, TextField txtFieldState,
                                                      ListView listRooms, ListView listUser) {
        this.thread = new Thread(new ReceivedMessagesHandler(oin, txtAreaChat, txtFieldState, listRooms,
                listUser, this));
        return this.thread;
    }

    public Thread createThreadReceivedMessagesHandler(TextArea txtAreaChat) {
        this.thread = new Thread(new ReceivedMessagesHandler(oin, txtAreaChat, this));
        return this.thread;
    }

    // disconnects client
    public void disconnect() {
        try {
            oout.close();
            oin.close();
            socket.close();
            Thread.sleep(20000);    // sleeps 20 seconds
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // sends object via output stream
    public void sendObject(Object object) {
        try {
            oout.writeObject(object);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // getter
    public String getName() {
        return name;
    }

    public int getPort() {
        return port;
    }

    public String getHost() {
        return host;
    }

    public String getActiveRoom() {
        return activeRoom;
    }

    public String getSelectedUser() {
        return selectedUser;
    }

    // setter
    public void setName(String name) {
        this.name = name;
    }

    public void setActiveRoom(String name) {
        this.activeRoom = name;
    }

    public void setSelectedUser(String name) {
        this.selectedUser = name;
    }
}

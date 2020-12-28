package ChatApplication.Library;

import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ConnectException;
import java.net.Socket;

public class Client {
    private final String host;
    private final int port;
    private String name;
    private Socket socket;
    private ObjectOutputStream oout;
    private ObjectInputStream oin;

    // client with specific host
    public Client(String host, int port) throws Exception {
        this.host = host;
        this.port = port;
        // connect client to server
        this.socket = new Socket(host, port);
        System.out.println("Client successfully connected to server!");
        // create object input/output streams
        this.oout = new ObjectOutputStream(socket.getOutputStream());
        this.oin = new ObjectInputStream(socket.getInputStream());
    }

    // client with default host
    public Client(int port) throws Exception {
        this.host = "localhost";
        this.port = port;
        // connect client to server
        this.socket = new Socket(host, port);
        // create object input/output streams
        this.oout = new ObjectOutputStream(socket.getOutputStream());
        this.oin = new ObjectInputStream(socket.getInputStream());
        System.out.println("Client successfully connected to server!");
    }

    // todo: dekonstruktor schreiben


    // todo löschen
    public void run() throws IOException, ConnectException {
//        // connect client to server
//        Socket socket = new Socket(host, port);
//        System.out.println("Client successfully connected to server!");
//
//        // create object input/output streams
//        ObjectOutputStream oout = new ObjectOutputStream(socket.getOutputStream());
//        ObjectInputStream oin = new ObjectInputStream(socket.getInputStream());

//        Scanner scanner = new Scanner(System.in);
//        // logging in client
//        try {
//            login(scanner, oin, oout);
//        } catch (Exception ignore) {
//        }
//
//        // create a new thread for received messages handling and start it
//        Thread thread = new Thread(new ReceivedMessagesHandler(oin, this.name));
//        thread.start();
//
//        // send chat messages to server
//        String message;
//        while (!(message = scanner.nextLine()).toLowerCase().equals("close")) {
//            oout.writeObject(new Message(name, message));
//            oout.flush();
//        }
//
//        // send closing message to server and closes all connections
//        oout.writeObject(new Message(name, message));
//        oout.flush();
//        oin.close();
//        oout.close();
//        thread.interrupt();
//        scanner.close();
//        socket.close();
    }

    // handles login process
    public Message login(Mode mode, String name, String password) throws IOException, ClassNotFoundException {
        if (name.isEmpty() || password.isEmpty())
            return new Message("", Mode.ERROR, "Bitte Namen und Passwort eingeben!");
        oout.writeObject(mode);
        oout.flush();
        oout.writeObject(new Account(name.strip(), password));
        oout.flush();

        Message message = (Message) oin.readObject();
        return message;
    }

    // returns an new thread for the ReceivedMessagesHandler
    public Thread createThreadReceivedMessagesHandler(TextArea txtAreaChat, TextArea txtAreaState,
                                                      ListView listRooms, ListView listUser) {
        return new Thread(new ReceivedMessagesHandler(oin, name, txtAreaChat, txtAreaState, listRooms, listUser));
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
    public int getPort() { return port; }
    public String getHost() { return host; }

    // setter
    public void setName(String name) {
        this.name = name;
    }

    // todo: evtl. wieder löschen
    @Override
    public String toString() {
        return "Client{" +
                "host='" + host + '\'' +
                ", port=" + port +
                ", name='" + name + '\'' +
                '}';
    }
}

//package sample;
//
//import java.io.IOException;
//import java.io.ObjectInputStream;
//import java.io.ObjectOutputStream;
//import java.net.ConnectException;
//import java.net.Socket;
//import java.util.Scanner;
//
//public class Client2 {
//    private final String host;
//    private final int port;
//    private String name;
//
//    public Client2(String host, int port) {
//        this.host = host;
//        this.port = port;
//    }
//
//    public Client2(int port) {
//        this.host = "localhost";
//        this.port = port;
//    }
//
//    public void run() throws IOException, ConnectException {
//        // connect client to server
//        Socket socket = new Socket(host, port);
//        System.out.println("Client2 successfully connected to server!");
//
//        // create object input/output streams
//        ObjectOutputStream oout = new ObjectOutputStream(socket.getOutputStream());
//        ObjectInputStream oin = new ObjectInputStream(socket.getInputStream());
//
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
//    }
//
//    // gets client inputs create account and sends it to server
//    public void getInputSendAccount(Scanner scanner, ObjectOutputStream oout) throws IOException {
//        // get client name and password
//        System.out.print("Enter your Name: ");
//        this.name = scanner.nextLine().strip();
//        System.out.print("Enter your Password: ");
//        String password = scanner.nextLine();
//
//        // send account to server
//        oout.writeObject(new Account(name, password));
//        oout.flush();
//    }
//
//    // handles login process
//    public void login(Scanner scanner, ObjectInputStream oin, ObjectOutputStream oout) throws IOException, ClassNotFoundException {
//        while (true) {
//            // check for login or registration and send it to server
//            System.out.print("Would you like to log in or create a new account?\n" +
//                    "Enter l for login or r for registration: ");
//            while (true) {
//                String input = scanner.nextLine();
//                if (input.toLowerCase().equals("l") || input.toLowerCase().equals("r")) {
//                    oout.writeObject(new Message(name, input));
//                    oout.flush();
//                    break;
//                }
//                System.out.println("Unknown command. Try again.");
//            }
//            getInputSendAccount(scanner, oout);
//
//            // check for a valid login, E=Error
//            Message message = (Message) oin.readObject();
//            if (message.getText().charAt(0) == 'E') {
//                System.out.println(message.getText());
//            } else {
//                System.out.println(message.getText());
//                break;
//            }
//        }
//    }
//
//    public static void main(String[] args) {
//        try {
//            new Client2(5000).run();
//        } catch (ConnectException e) {
//            System.out.println("Connection failed!");
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
//}

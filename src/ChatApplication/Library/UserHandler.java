package ChatApplication.Library;

import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

import java.io.IOException;

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

}
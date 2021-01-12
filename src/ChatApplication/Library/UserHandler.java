package ChatApplication.Library;

import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

import java.io.IOException;

public class UserHandler implements Runnable {
    private final Server server;
    private User user;
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
            this.server.accountAndEntryManagement(user);
            this.server.broadcastMessages(new Message("Server", Mode.MESSAGE, user +
                    " ist dem Chat beigetreten.", "default"));
            addMessageToTxtAreaServerlog(new Message("Server", Mode.MESSAGE, user + " ist dem Chat beigetreten.", ""));
            this.server.broadcastAllUsers();
            this.server.broadcastRooms();
        } catch (Exception e) {
            e.printStackTrace();
        }
        while (true) {
            try {
                // gets client messages
                Message message = (Message) user.getOin().readObject();

                // checks message mode
                switch (message.getMode()) {
                    case MESSAGE:
                        //if (!message.getRoom().equals("default")){
                        server.broadcastToRoom(message);
                        addMessageToTxtAreaServerlog(new Message(message.getClient(), message.getMode(), "{" + message.getRoom() + "} " + message.getText(), ""));
                        //}
                        /*else{
                            server.broadcastMessages(message);
                            addMessageToTxtAreaServerlog(message);
                        }*/
                        break;
                    case LOGOUT:
                        server.removeUser(user);
                        server.broadcastMessages(new Message("Server", Mode.MESSAGE, user +
                                " hat den Chat verlassen.", "default"));
                        server.broadcastAllUsers();
                        break;
                    case CHANGE_NAME:
                        String oldName = user.getName();
                        String newName = message.getText();
                        user.setName(newName);
                        server.changeAccountName(oldName, newName);
                        server.broadcastMessages(new Message("Server", Mode.MESSAGE, oldName +
                                " hat den Namen zu " + newName + " geändert.", "default"));
                        server.broadcastAllUsers();
                        break;
                    case CHANGE_PASSWORD:
                        server.changeAccountPassword(user.getName(), message.getText());
                        server.sendMessage(user, new Message("server", Mode.MESSAGE,"Änderung erfolgreich.", "default"));
                        break;
                    case ROOM_CREATE:
                        System.out.println("ROOM_CREATE");
                        //server.addRoom(message.getClient(), message.getText());
                    case ROOM_JOIN:
                        server.addToRoom(message);
                        break;
                    default:
                        break;
                }
            } catch (Exception e) {
                System.out.println(user + " hat den Chat verlassen.");
                addMessageToTxtAreaServerlog(new Message(user.getName(), Mode.LOGOUT, " hat den Chat verlassen.", ""));
                server.removeUser(user);
                try {
                    server.broadcastMessages(new Message("Server", Mode.LOGOUT,user +
                            " hat den Chat verlassen.", "default"));
                    server.broadcastAllUsers();
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }
                e.printStackTrace();
                return;
            }
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                System.out.println(user + " hat den Chat verlassen..");
                addMessageToTxtAreaServerlog(new Message(user.getName(), Mode.LOGOUT, " hat den Chat verlassen.", ""));
                server.removeUser(user);
                try {
                    server.broadcastMessages(new Message("Server", Mode.LOGOUT,user +
                            " hat den Chat verlassen.", "default"));
                    server.broadcastAllUsers();
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }
                return;
            }
        }
    }

    // add message to server log
    public void addMessageToTxtAreaServerlog(Message message) {
        txtAreaServerlog.setText(txtAreaServerlog.getText() + message + "\n");
    }

}
package ChatApplication.Library;

import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

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
//            this.server.broadcastMessages(new Message("Server", Mode.MESSAGE, user +
//                    " ist dem Chat beigetreten."));
//            addMessageToTxtAreaServerlog(new Message("Server", Mode.MESSAGE,
//                    user + " ist dem Chat beigetreten."));
//            this.server.broadcastAllUsers();
//            this.server.broadcastRooms();
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
                        server.broadcastToRoom(message);
                        addMessageToTxtAreaServerlog(new Message(message.getClient(), message.getMode(),
                                "{" + message.getRoom() + "} " + message.getText(), ""));
                        break;
                    case INFORMATION_REQUEST:
                        System.out.println(message);
                        populateList(server.getClientNames(), listUser);
                        populateList(server.getRoomNames(), listRooms);

                        server.broadcastMessages(new Message("Server", Mode.MESSAGE, user +
                                " ist dem Chat beigetreten."));
                        addMessageToTxtAreaServerlog(new Message("Server", Mode.MESSAGE,
                                user + " ist dem Chat beigetreten."));
                        server.broadcastAllUsers();
                        server.broadcastRooms();
                        break;
                    case LOGOUT:
                        server.removeUser(user);
                        populateList(server.getClientNames(), listUser);
                        server.broadcastMessages(new Message("Server", Mode.MESSAGE, user +
                                " hat den Chat verlassen."));
                        server.broadcastAllUsers();
                        break;
                    case CHANGE_NAME:
                        String oldName = user.getName();
                        String newName = message.getText();
                        user.setName(newName);
                        server.changeAccountName(oldName, newName);
                        populateList(server.getClientNames(), listUser);
                        server.broadcastToRoom(new Message("Server", Mode.MESSAGE, oldName +
                                " hat den Namen zu " + newName + " geändert.", message.getRoom()));
                        server.broadcastAllUsers();
                        break;
                    case CHANGE_PASSWORD:
                        server.changeAccountPassword(user.getName(), message.getText());
                        server.sendMessage(user, new Message("server", Mode.MESSAGE,"Änderung erfolgreich.",
                                message.getRoom()));
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
                System.out.println(user + " hat den Chat verlassen.");      // todo löschen
                addMessageToTxtAreaServerlog(new Message(user.getName(), Mode.LOGOUT, " hat den Chat verlassen.", ""));
                server.removeUser(user);
                populateList(server.getClientNames(), listUser);
                server.broadcastMessages(new Message("Server", Mode.LOGOUT,user +
                        " hat den Chat verlassen."));
                server.broadcastAllUsers();
                e.printStackTrace();
                return;
            }
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                System.out.println(user + " hat den Chat verlassen..");
                addMessageToTxtAreaServerlog(new Message(user.getName(), Mode.LOGOUT, " hat den Chat verlassen.", ""));
                server.removeUser(user);
                populateList(server.getClientNames(), listUser);
                server.broadcastMessages(new Message("Server", Mode.LOGOUT,user +
                        " hat den Chat verlassen."));
                server.broadcastAllUsers();
                return;
            }
        }
    }

    // add message to server log
    public void addMessageToTxtAreaServerlog(Message message) {
        txtAreaServerlog.setText(txtAreaServerlog.getText() + message + "\n");
    }

    public void populateList(String[] list, ListView object) {
        object.getItems().clear();
        for (String item : list) {
            object.getItems().add(item.strip());
        }
    }

}
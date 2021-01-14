package ChatApplication.Library;

import javafx.application.Platform;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

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
            this.server.accountAndEntryManagement(user);
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
                        populateList(server.getClientNames(), listUser);
                        populateList(server.getRoomNames(), listRooms);
                        server.broadcastToRoom(new Message("Server", Mode.MESSAGE, user +
                                " ist dem Chat beigetreten."));
                        addMessageToTxtAreaServerlog(new Message("Server", Mode.MESSAGE,
                                user + " ist dem Chat beigetreten."));
                        server.broadcastRoomUsers("Lobby");
                        server.broadcastRooms();
                        break;
                    // todo
//                    case LOGOUT:
//                        server.removeUser(user);
//                        populateList(server.getClientNames(), listUser);
//                        server.broadcastMessages(new Message("Server", Mode.MESSAGE, user +
//                                " hat den Chat verlassen."));
//                        server.broadcastAllUsers();
//                        break;
                    case CHANGE_NAME:
                        String oldName = user.getName();
                        String newName = message.getText();
                        user.setName(newName);
                        server.changeAccountName(oldName, newName);
                        populateList(server.getClientNames(), listUser);
                        server.broadcastToRoom(new Message("Server", Mode.MESSAGE, oldName +
                                " hat den Namen zu " + newName + " geändert.", message.getRoom()));
                        server.broadcastRoomUsers(message.getRoom());
                        break;
                    case CHANGE_PASSWORD:
                        server.changeAccountPassword(user.getName(), message.getText());
                        server.sendMessage(user, new Message("Server", Mode.MESSAGE, "Änderung erfolgreich.",
                                message.getRoom()));
                        break;
                    case DELETE_ACCOUNT:
                        server.deleteAccount(user.getName());
                        populateList(server.getClientNames(), listUser);
                        break;
                    case ROOM_CREATE:
                        //server.addRoom(message.getClient(), message.getText());
                    case ROOM_JOIN:
                        server.addToRoom(message);
                        break;
                    default:
                        break;
                }
            } catch (Exception e) {
                addMessageToTxtAreaServerlog(new Message(user.getName(), Mode.MESSAGE, "hat den Chat verlassen."));
                String room = server.getRoomNameForUser(user);
                server.removeUser(user);
                populateList(server.getClientNames(), listUser);
                server.broadcastToRoom(new Message("Server", Mode.MESSAGE, user +
                        " hat den Chat verlassen.", room));
                server.broadcastRoomUsers(room);
                return;
            }
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                addMessageToTxtAreaServerlog(new Message(user.getName(), Mode.MESSAGE, "hat den Chat verlassen."));
                String room = server.getRoomNameForUser(user);
                server.removeUser(user);
                populateList(server.getClientNames(), listUser);
                server.broadcastToRoom(new Message("Server", Mode.MESSAGE, user +
                        " hat den Chat verlassen.", room));
                server.broadcastRoomUsers(room);
                return;
            }
        }
    }

    // add message to server log
    public void addMessageToTxtAreaServerlog(Message message) {
        txtAreaServerlog.setText(txtAreaServerlog.getText() + message + "\n");
    }

    // todo liste sortiert ausgeben
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
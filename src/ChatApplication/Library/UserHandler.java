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
                                "{" + message.getRoom() + "} " + message.getText()));
                        break;
                    case MESSAGE_PRIVATE:
                        server.broadcastToPrivateRoom(message);
                        break;
                    case INFORMATION_REQUEST:
                        server.populateList(server.getClientNames(), listUser); // geändert
                        server.populateList(server.getRoomNames(), listRooms);  // geändert
                        server.broadcastToRoom(new Message("Server", Mode.MESSAGE, user +
                                " ist dem Chat beigetreten."));
                        addMessageToTxtAreaServerlog(new Message("Server", Mode.MESSAGE,
                                user + " ist dem Chat beigetreten."));
                        server.broadcastRoomUsers("Lobby");
                        server.broadcastRooms();
                        break;
                    case LOGOUT:
                        disconnectRoutine();
                        return;
                    case CHANGE_NAME:
                        String oldName = user.getName();
                        String newName = message.getText();
                        if(!server.nameInUse(newName)){
                            server.changeAccountName(oldName, newName);
                            server.populateList(server.getClientNames(), listUser); //geändert
                            Message m = new Message("Server", Mode.MESSAGE, message.getRoom(),
                                    oldName + " hat den Namen zu " + newName + " geändert.");
                            server.broadcastToRoom(m);
                            server.broadcastRoomUsers(message.getRoom());
                            addMessageToTxtAreaServerlog(m);
                        }
                        else{
                            server.sendMessage(user, new Message("Server", Mode.CHANGE_NAME, message.getRoom(),
                                    oldName));
                        }
                        break;
                    case CHANGE_PASSWORD:
                        server.changeAccountPassword(user.getName(), message.getText());
                        server.sendMessage(user, new Message("Server", Mode.MESSAGE, message.getRoom(),
                                "Änderung erfolgreich."
                        ));
                        break;
                    case DELETE_ACCOUNT:
                        server.deleteAccount(user.getName());
                        server.populateList(server.getClientNames(), listUser); //geändert
                        break;
                    case ROOM_CREATE:
                        String newRoom = message.getText();
                        if(!server.getRoomsKeySet().contains(newRoom)){
                            server.addRoom(newRoom);
                            server.addToRoom(message);
                            Platform.runLater(new Runnable() {
                                @Override
                                public void run() {
                                    listRooms.getItems().add(newRoom);
                                }
                            });
                            addMessageToTxtAreaServerlog(new Message(message.getClient(), Mode.MESSAGE,
                                    String.format("%s hat Raum %s erstellt", message.getClient(), newRoom)));
                        }
                        break;
                    case ROOM_CREATE_PRIVATE:
                        server.addPrivateRoom(message);
                        server.addClientToPrivateRoom(message);
                        break;
                    case ROOM_JOIN:
                        server.addToRoom(message);
                        break;
                    default:
                        break;
                }
            } catch (Exception e) {
                disconnectRoutine();
                e.printStackTrace();
                return;
            }
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                disconnectRoutine();
                e.printStackTrace();
                return;
            }
        }
    }

    // add message to server log
    public void addMessageToTxtAreaServerlog(Message message) {
        txtAreaServerlog.setText(txtAreaServerlog.getText() + message + "\n");
    }

    public void disconnectRoutine() {
        if (server.isPrivateClient(user)) {
            server.removeUser(user);
        } else {
            addMessageToTxtAreaServerlog(new Message(user.getName(), Mode.MESSAGE, "hat den Chat verlassen."));
            String room = server.getRoomNameForUser(user);
            server.removeUser(user);
            server.populateList(server.getClientNames(), listUser);
            server.broadcastToRoom(new Message("Server", Mode.MESSAGE, room, user +
                    " hat den Chat verlassen."));
            server.broadcastRoomUsers(room);
        }
    }
}
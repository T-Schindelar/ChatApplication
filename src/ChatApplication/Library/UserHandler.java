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
                System.out.println(message + " " + message.getRoom());

                // checks message mode
                switch (message.getMode()) {
                    case MESSAGE:
                        server.broadcastToRoom(message);
                        addMessageToTxtAreaServerlog(new Message(message.getClient(), message.getMode(),
                                "{" + message.getRoom() + "} " + message.getText(), ""));
                        break;
                    case INFORMATION_REQUEST:
                        server.populateList(server.getClientNames(), listUser); //geändert
                        server.populateList(server.getRoomNames(), listRooms);  //geändert
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
                        server.changeAccountName(oldName, newName);
                        server.populateList(server.getClientNames(), listUser); //geändert
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
                        server.populateList(server.getClientNames(), listUser); //geändert
                        break;
                    case ROOM_CREATE:
                        Message m = new Message(message.getClient(), Mode.ROOM_JOIN, message.getRoom(), message.getText());
                        if(!server.getRoomsKeySet().contains(m.getRoom())){
                            server.addRoom(message.getText());
                            server.addToRoom(m);
                            server.updateRoom(m);
                            listRooms.getItems().add(m.getRoom());
                            addMessageToTxtAreaServerlog(new Message(m.getClient(), Mode.MESSAGE, String.format("%s hat Raum %s erstellt", m.getClient(), m.getRoom())));
                        }
                        break;
                    case ROOM_CREATE_PRIVATE:       //todo weitermachen
                        Message m1 = new Message(message.getClient(), Mode.ROOM_JOIN, message.getRoom(), message.getText());
                        if(!server.getPrivateRoomsKeySet().contains(m1.getRoom())){
                            server.addPrivateRoom(message.getText());
                            server.addToRoom(m1);
                            server.updateRoom(m1);
                            listRooms.getItems().add(m1.getRoom());
                            addMessageToTxtAreaServerlog(new Message(m1.getClient(), Mode.MESSAGE, String.format("%s hat Raum %s erstellt", m1.getClient(), m1.getRoom())));
                        }
                        break;
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
                server.populateList(server.getClientNames(), listUser); //geändert
                server.broadcastToRoom(new Message("Server", Mode.MESSAGE, user +
                        " hat den Chat verlassen.", room));
                server.broadcastRoomUsers(room);
                e.printStackTrace();
                return;
            }
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                addMessageToTxtAreaServerlog(new Message(user.getName(), Mode.MESSAGE, "hat den Chat verlassen."));
                String room = server.getRoomNameForUser(user);
                server.removeUser(user);
                server.populateList(server.getClientNames(), listUser); //geändert
                server.broadcastToRoom(new Message("Server", Mode.MESSAGE, user +
                        " hat den Chat verlassen.", room));
                server.broadcastRoomUsers(room);
                e.printStackTrace();
                return;
            }
        }
    }

    // add message to server log
    public void addMessageToTxtAreaServerlog(Message message) {
        txtAreaServerlog.setText(txtAreaServerlog.getText() + message + "\n");
    }

    // todo liste sortiert ausgeben
//    public void populateList(String[] list, ListView object) {
//        Platform.runLater(new Runnable() {
//            @Override
//            public void run() {
//                object.getItems().clear();
//                for (String item : list) {
//                    object.getItems().add(item.strip());
//                }
//            }
//        });
//
//    }

}
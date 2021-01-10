package ChatApplication.Library;

import javafx.application.Platform;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;

import java.io.ObjectInputStream;
import java.util.Arrays;

class ReceivedMessagesHandler implements Runnable {
    private final ObjectInputStream oin;
    private final String name;      // todo: evtl. löschen

    // controller properties
    private final TextArea txtAreaChat;
    private final TextArea txtAreaState;
    private final ListView listRooms;
    private final ListView listUser;
    private Client client;

    public ReceivedMessagesHandler(ObjectInputStream oin, String name, TextArea txtAreaChat,
                                   TextArea txtAreaState, ListView listRooms, ListView listUser, Client client) {
        this.oin = oin;
        this.name = name;
        this.txtAreaChat = txtAreaChat;
        this.txtAreaState = txtAreaState;
        this.listRooms = listRooms;
        this.listUser = listUser;
        this.client = client;
    }

    public void run() {
        while (true) {
            try {
                Message message = (Message) oin.readObject();
                //System.out.println(message);            // todo
                // server messages
                if (message.getClient().equals("Server")) {
                    // show all other Users
                    if (message.getMode() == Mode.USER_TRANSMIT) {
                        populateUserList(message.getText().substring(1, message.getText().length() - 1).split(","));
                    }
                    else if (message.getMode() == Mode.ROOM_TRANSMIT) {
                        System.out.println(message.getText());
                        populateRoomsList(message.getText().substring(1, message.getText().length() - 1).split(","));
                    }
                    // server info messages
                    else {
                        if(message.getRoom().equals(client.getActiveRoom()) && !message.getText().contains(client.getName())){
                            addMessageToTxtAreaChat(message);
                        }
                        else{
                            System.out.println("message " + message.getText() + " dismissed; " + message.getRoom() + " " + client.getActiveRoom());
                        }
                    }
                }
                // other client messages
                else {
                    addMessageToTxtAreaChat(message);
                }
            } catch (Exception e) {
                System.out.println("Connection closed.");
                System.out.println("RMH : 1");  //todo
                e.printStackTrace();
                System.exit(1);
            }
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                System.out.println("Connection closed.");
                System.out.println("RMH : 2");  //todo
                e.printStackTrace();
                System.exit(1);
            }
        }
    }

    public void populateUserList(String[] list) {
        listUser.getItems().clear();
        for (String item : list) {
            listUser.getItems().add(item.strip());
//            if (!listUser.getItems().contains(item)) {
//            }
        }
    }

    public void populateRoomsList(String[] list) {
        listRooms.getItems().clear();
        listRooms.getItems().add("default");
        for (String item : list) {
            listRooms.getItems().add(item.strip());
        }
    }

    public void addMessageToTxtAreaChat(Message message) {
        txtAreaChat.setText(txtAreaChat.getText() + message + "\n");
    }
}
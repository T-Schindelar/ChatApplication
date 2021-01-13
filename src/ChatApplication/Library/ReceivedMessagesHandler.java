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
                System.out.println("RMH: " + message);            // todo löschen
                // server messages
                if (message.getClient().equals("Server")) {
                    // show all other Users
                    switch (message.getMode()) {
                        // server info messages
                        case MESSAGE:
                            addMessageToTxtAreaChat(message);
                            break;
                        case USER_TRANSMIT:
                            System.out.println(message.getText()); //todo löschen
                            populateList(message.getText().substring(1, message.getText().length() - 1).split(","),
                                    listUser);
                            break;
                        case ROOM_TRANSMIT:
                            System.out.println(message.getText()); //todo löschen
                            populateList(message.getText().substring(1, message.getText().length() - 1).split(","),
                                    listRooms);
                            break;
                        case DISCONNECT:
                            addMessageToTxtAreaChat(message);
                            client.disconnect();
                            break;
                        default:
                            break;
                    }
                }
                // other client messages
                else {
                    addMessageToTxtAreaChat(message);
                }
            } catch (Exception e) {
                System.out.println("Connection closed.");
                System.out.println("RMH : 1");  //todo löschen
                e.printStackTrace();
                System.exit(1);
            }
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                System.out.println("Connection closed.");
                System.out.println("RMH : 2");  //todo löschen
                e.printStackTrace();
                System.exit(1);
            }
        }
    }

    public void populateList(String[] list, ListView object) {
        System.out.println(Arrays.toString(list));  //todo löschen
        //Arrays.sort(list);
        System.out.println(Arrays.toString(list));  //todo löschen
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                object.getItems().clear();
                for (String item : list) {
                    System.out.println(item); //todo löschen
                    object.getItems().add(item.strip());
                }
            }
        });
    }

    public void addMessageToTxtAreaChat(Message message) {
        txtAreaChat.setText(txtAreaChat.getText() + message + "\n");
    }
}
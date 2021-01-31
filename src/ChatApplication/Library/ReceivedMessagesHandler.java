package ChatApplication.Library;

import javafx.application.Platform;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

import java.io.ObjectInputStream;

class ReceivedMessagesHandler implements Runnable {
    private final ObjectInputStream oin;

    // controller properties
    private final TextArea txtAreaChat;
    private final TextField txtFieldState;
    private final ListView listRooms;
    private final ListView listUser;
    private final Client client;

    public ReceivedMessagesHandler(ObjectInputStream oin, TextArea txtAreaChat,
                                   TextField txtFieldState, ListView listRooms, ListView listUser, Client client) {
        this.oin = oin;
        this.txtAreaChat = txtAreaChat;
        this.txtFieldState = txtFieldState;
        this.listRooms = listRooms;
        this.listUser = listUser;
        this.client = client;
    }

    public ReceivedMessagesHandler(ObjectInputStream oin, TextArea txtAreaChat, Client client) {
        this.oin = oin;
        this.txtAreaChat = txtAreaChat;
        this.client = client;
        this.txtFieldState = null;
        this.listRooms = null;
        this.listUser = null;
    }

    public void run() {
        while (true) {
            try {
                Message message = (Message) oin.readObject();
                // server messages
                if (message.getClient().equals("Server")) {
                    // show all other Users
                    switch (message.getMode()) {
                        // server info messages
                        case MESSAGE:
                        case MESSAGE_PRIVATE:
                            addMessageToTxtAreaChat(message);
                            break;
                        case USER_TRANSMIT:
                            populateList(message.getText().substring(1, message.getText().length() - 1).split(","),
                                    listUser);
                            break;
                        case ROOM_TRANSMIT:
                            populateList(message.getText().substring(1, message.getText().length() - 1).split(","),
                                    listRooms);
                            break;
                        case DISCONNECT:
                            txtFieldState.clear();
                            txtFieldState.setText("Keine Verbindung zum Server.");
                            addMessageToTxtAreaChat(message);
                            client.disconnect();
                            break;
                        case UPDATE_ROOM:
                            this.client.setActiveRoom(message.getText());
                            txtFieldState.setText(String.format("Verbunden mit %s:%d als %s in Raum %s", client.getHost(), client.getPort(),
                                    client.getName(), client.getActiveRoom()));
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
                e.printStackTrace();
                System.exit(1);
            }
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
                System.exit(1);
            }
        }
    }

    // todo liste sortiert ausgeben
    public void populateList(String[] list, ListView object) {
        if (object == null) {
            return;
        }
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

    public void addMessageToTxtAreaChat(Message message) {
        txtAreaChat.setText(txtAreaChat.getText() + message + "\n");
    }
}
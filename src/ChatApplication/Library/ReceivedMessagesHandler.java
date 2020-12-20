package ChatApplication.Library;

import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;

import java.io.ObjectInputStream;

class ReceivedMessagesHandler implements Runnable {
    private final ObjectInputStream oin;
    private final String name;      // todo: evtl. löschen

    // controller properties
    private final TextArea txtAreaChat;
    private final TextArea txtAreaState;
    private final ListView listRooms;
    private final ListView listUser;

    public ReceivedMessagesHandler(ObjectInputStream oin, String name, TextArea txtAreaChat,
                                   TextArea txtAreaState, ListView listRooms, ListView listUser) {
        this.oin = oin;
        this.name = name;
        this.txtAreaChat = txtAreaChat;
        this.txtAreaState = txtAreaState;
        this.listRooms = listRooms;
        this.listUser = listUser;
    }

    public void run() {
        while (true) {
            try {
                Message message = (Message) oin.readObject();
                System.out.println(message);            // todo
                // server messages
                if (message.getClient().equals("Server")) {
                    // show all other Users
                    if (message.getText().charAt(0) == '[') {
                        populateUserList(message.getText().substring(1, message.getText().length() - 1).split(","));
                    }
                    // server info messages
                    else {
                        addMessageToTxtAreaChat(message);
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
        this.listUser.getItems().clear();
        for (String item : list) {
            this.listUser.getItems().add(item.strip());
//            if (!this.listUser.getItems().contains(item)) {
//            }
        }
    }

    public void populateRoomsList(String[] list) {
        for (String item : list) {
            if (!this.listRooms.getItems().contains(item)) {
                this.listRooms.getItems().add(item.strip());
            }
        }
    }

    public void addMessageToTxtAreaChat(Message message) {
        txtAreaChat.setText(txtAreaChat.getText() + message + "\n");
    }
}
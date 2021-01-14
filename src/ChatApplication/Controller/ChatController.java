package ChatApplication.Controller;

import ChatApplication.Library.Client;
import ChatApplication.Library.Message;
import ChatApplication.Library.Mode;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;

import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;

public class ChatController implements Initializable {
    public TextArea txtAreaChat;
    public TextField txtFieldMessage;
    public TextField txtFieldState;
    public ListView listRooms;
    public ListView listUser;


    private final Client client;

    public ChatController(Client client) {
        this.client = client;
    }

    public void initialize(URL location, ResourceBundle resource) {
        client.createThreadReceivedMessagesHandler(txtAreaChat, txtFieldState, listRooms, listUser).start();
        // gets information about other clients and available rooms
        client.sendObject(new Message(client.getName(), Mode.INFORMATION_REQUEST, "", client.getActiveRoom()));
        txtFieldState.setText(String.format("Verbunden mit %s:%d als %s in Raum %s", client.getHost(), client.getPort(),
                client.getName(), client.getActiveRoom()));
        txtFieldMessage.setOnKeyPressed(keyEvent -> {
            if (keyEvent.getCode() == KeyCode.ENTER)
                sendMessage();
        });
    }

    public void btnSendMessage(ActionEvent actionEvent) {
        sendMessage();
    }

    public void menuItemChangeName() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Name ändern");
        dialog.setHeaderText("");
        dialog.setContentText("Bitte neuen Namen eingeben");
        Optional<String> result = dialog.showAndWait();
        if (result.isPresent()) {
            client.sendObject(new Message(client.getName(), Mode.CHANGE_NAME, result.get(), client.getActiveRoom()));
            client.setName(result.get());
            txtFieldState.setText(String.format("Verbunden mit %s:%d als %s in Raum %s", client.getHost(), client.getPort(),
                    client.getName(), client.getActiveRoom()));
        }
    }

    public void menuItemDeleteAccount() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Konto löschen");
        alert.setHeaderText("");
        alert.setContentText("Sind Sie sich sicher?");
        Optional<ButtonType> result = alert.showAndWait();
        if (result.get() == ButtonType.OK) {
            client.sendObject(new Message(client.getName(), Mode.DELETE_ACCOUNT, "", client.getActiveRoom()));
        }
    }

    public void menuItemChangePassword() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Passwort ändern");
        dialog.setHeaderText("");
        dialog.setContentText("Bitte neuen Passwort eingeben");
        Optional<String> result = dialog.showAndWait();
        if (result.isPresent()) {
            client.sendObject(new Message(client.getName(), Mode.CHANGE_PASSWORD, result.get()));
        }
    }

    // todo
//    public void menuItemCreateRoom() {
//        TextInputDialog dialog = new TextInputDialog();
//        dialog.setTitle("Raum erstellen");
//        dialog.setHeaderText("");
//        dialog.setContentText("Bitte Raumnamen eingeben");
//        Optional<String> result = dialog.showAndWait();
//        if (result.isPresent()){
//            createRoom(result.get());
//        }
//    }

    // switches rooms
    public void listRoomsClicked() {
        String previousRoom = client.getActiveRoom();
        Object item = listRooms.getSelectionModel().getSelectedItem();
        if (listRooms.getSelectionModel().getSelectedItem() != null &&
                listRooms.getSelectionModel().getSelectedItem() != client.getActiveRoom()) {
            client.setActiveRoom(item.toString());
            client.sendObject(new Message(client.getName(), Mode.ROOM_JOIN, previousRoom, item.toString()));
        }
        txtFieldState.setText(String.format("Verbunden mit %s:%d als %s in Raum %s",
                client.getHost(), client.getPort(), client.getName(), client.getActiveRoom()));
    }

    // sends a message to the server
    public void sendMessage() {
        client.sendObject(new Message(client.getName(), Mode.MESSAGE, txtFieldMessage.getText(),
                client.getActiveRoom()));
        txtFieldMessage.clear();
    }

    // todo
//    public void createRoom(String name){
//        client.sendObject(new Message(client.getName(), Mode.ROOM_CREATE, name, ""));
//    }
}

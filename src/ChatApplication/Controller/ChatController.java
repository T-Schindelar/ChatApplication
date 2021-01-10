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
    public TextField txtFieldNickname;
    public TextArea txtAreaChat;
    public TextField txtFieldMessage;
    public Button btnSendMessage;
    public Button btnSendNickname;
    public TextArea txtAreaState;
    public ListView listRooms;
    public ListView listUser;


    private final Client client;

    public ChatController(Client client) {
        this.client = client;
    }

    public void initialize(URL location, ResourceBundle resource) {
        client.createThreadReceivedMessagesHandler(txtAreaChat, txtAreaState, listRooms, listUser).start();
        txtAreaState.setText(String.format("Verbunden mit %s:%d als %s", client.getHost(), client.getPort(), client.getName()));
        txtFieldMessage.setOnKeyPressed(keyEvent -> {
            if(keyEvent.getCode() == KeyCode.ENTER)
                sendMessage(); } );
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
            client.sendObject(new Message(client.getName(), Mode.CHANGE_NAME,result.get(), "default"));
            client.setName(result.get());
        }
    }

    public void menuItemChangePassword() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Passwort ändern");
        dialog.setHeaderText("");
        dialog.setContentText("Bitte neuen Passwort eingeben");
        Optional<String> result = dialog.showAndWait();
        if (result.isPresent()) {
            client.sendObject(new Message(client.getName(), Mode.CHANGE_PASSWORD, result.get(), "default"));
        }
    }

    public void menuItemCreateRoom() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Raum erstellen");
        dialog.setHeaderText("");
        dialog.setContentText("Bitte Raumnamen eingeben");
        Optional<String> result = dialog.showAndWait();
        if (result.isPresent()){
            createRoom(result.get());
        }
    }

    public void listRoomsClicked(){
        String previousRoom = client.getActiveRoom();
        Object item = listRooms.getSelectionModel().getSelectedItem();
        if(listRooms.getSelectionModel().getSelectedItem() != null){
            client.setActiveRoom(item.toString());
            client.sendObject(new Message(client.getName(), Mode.ROOM_JOIN, previousRoom, item.toString()));
        }
        System.out.println(client.getActiveRoom());
    }

    public void sendMessage() {
        client.sendObject(new Message(client.getName(), Mode.MESSAGE, txtFieldMessage.getText(), client.getActiveRoom()));
        txtFieldMessage.clear();
    }

    public void createRoom(String name){
        client.sendObject(new Message(client.getName(), Mode.ROOM_CREATE, name, ""));
    }
}

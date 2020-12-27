package ChatApplication.Controller;

import ChatApplication.Library.Client;
import ChatApplication.Library.Message;
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
        txtFieldNickname.setOnKeyPressed(keyEvent -> {
            if(keyEvent.getCode() == KeyCode.ENTER)
                sendNickname(); } );
        txtFieldMessage.setOnKeyPressed(keyEvent -> {
            if(keyEvent.getCode() == KeyCode.ENTER)
                sendMessage(); } );
    }

    public void btnSendMessage(ActionEvent actionEvent) {
        sendMessage();
    }

    public void btnSendNickname(ActionEvent actionEvent) {
        sendNickname();
    }

    public void menuItemChangeName() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Name ändern");
        dialog.setHeaderText("");
        dialog.setContentText("Bitte neuen Namen eingeben");
        Optional<String> result = dialog.showAndWait();
        if (result.isPresent()) {
            System.out.println(result.get());
            client.sendObject(new Message(client.getName(), "change_name_to_" + result.get()));
        }
    }

    public void menuItemChangePassword() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Passwort ändern");
        dialog.setHeaderText("");
        dialog.setContentText("Bitte neuen Passwort eingeben");
        Optional<String> result = dialog.showAndWait();
        if (result.isPresent()) {
            System.out.println(result.get());
            client.sendObject(new Message(client.getName(), "change_password_to_" + result.get()));
        }
    }

    public void sendMessage() {
        this.client.sendObject(new Message(client.getName(), txtFieldMessage.getText()));
        txtFieldMessage.clear();
    }

    public void sendNickname() {
        this.client.sendObject(new Message(client.getName(), "change nickname to" + txtFieldNickname.getText()));
        txtFieldMessage.clear();
    }
}

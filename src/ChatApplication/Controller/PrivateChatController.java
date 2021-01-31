package ChatApplication.Controller;

import ChatApplication.Library.Client;
import ChatApplication.Library.Message;
import ChatApplication.Library.Mode;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;

import java.net.URL;
import java.util.ResourceBundle;

public class PrivateChatController implements Initializable {
    public TextArea txtAreaChat;
    public TextField txtFieldMessage;
    public Button btnSendMessage;

    private final Client client;
    private final String otherClient;

    public PrivateChatController(Client client, String otherClient) {
        this.client = client;
        this.otherClient = otherClient;
    }

    public void initialize(URL location, ResourceBundle resource) {
        client.createThreadReceivedMessagesHandler(txtAreaChat).start();
        // gets information about other clients and available rooms
        txtFieldMessage.setOnKeyPressed(keyEvent -> {
            if (keyEvent.getCode() == KeyCode.ENTER)
                sendMessage();
        });
    }

    public void btnSendMessage(ActionEvent actionEvent) {
        sendMessage();
    }

    // sends a message to the server
    public void sendMessage() {
        client.sendObject(new Message(client.getName(), Mode.MESSAGE_PRIVATE, otherClient, txtFieldMessage.getText()));
        txtFieldMessage.clear();
    }
}

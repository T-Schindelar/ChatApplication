package ChatApplication.Controller;

import ChatApplication.Library.Account;
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

    private Client client;
    private String otherClient;

    public PrivateChatController(String clientName, String otherClient) {
        try {
            this.otherClient = otherClient;
            this.client = new Client(5000);
            client.setName(clientName);
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }

    public void initialize(URL location, ResourceBundle resource) {
        try {
            // passing the account and and management
            client.sendObject(Mode.ROOM_CREATE_PRIVATE);
            client.sendObject(new Account(client.getName(), ""));

            client.createThreadReceivedMessagesHandler(txtAreaChat).start();
            client.sendObject(new Message(client.getName(), Mode.ROOM_CREATE_PRIVATE, otherClient, ""));
            txtFieldMessage.setOnKeyPressed(keyEvent -> {
                if (keyEvent.getCode() == KeyCode.ENTER)
                    sendMessage();
            });
        }
        catch (Exception e){
            e.printStackTrace();
        }
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

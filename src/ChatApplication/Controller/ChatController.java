package ChatApplication.Controller;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import ChatApplication.Library.Client;
import ChatApplication.Library.Message;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.Scene;

import java.io.IOException;
import java.net.URL;
import java.security.Key;
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
        txtAreaState.setText(String.format("Connected to %s:%d as %s", client.getHost(), client.getPort(), client.getName()));
    }

    public void btnSendMessage(ActionEvent actionEvent) throws IOException {
        sendMessage();
    }

    public void btnSendNickname(ActionEvent actionEvent) throws IOException {
        sendNickname();
    }

    public void sendMessage() throws IOException{
        this.client.sendObject(new Message(client.getName(), txtFieldMessage.getText()));
        txtFieldMessage.clear();
    }

    public void sendNickname() throws IOException{
        //this.client.sendObject(new Message(client.getName(), txtFieldMessage.getText()));
        //txtFieldMessage.clear();
    }
}

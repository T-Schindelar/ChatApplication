package ChatApplication.Controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;
import ChatApplication.Library.Client;

import java.net.URL;
import java.security.Key;
import java.util.ResourceBundle;

public class LoginController implements Initializable {
    public TextField txtFieldName;
    public PasswordField pswFieldPassword;
    public TextField txtFieldError;


    private Client client;

    public void initialize(URL location, ResourceBundle resource) {
        try {
            this.client = new Client(5000);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void btnLogin(ActionEvent actionEvent) throws Exception {
        if (this.client.login("l", txtFieldName.getText(), pswFieldPassword.getText())) {
            this.client.setName(txtFieldName.getText());
            // get current window stage information
            Stage currentWindow = (Stage) (((Node) actionEvent.getSource()).getScene().getWindow());

            // setup new window
            FXMLLoader loader = new FXMLLoader(getClass().getResource("../Resource/chatWindow.fxml"));
            loader.setController(new ChatController(client));

            // show new window
            currentWindow.setTitle("ChatApp");
            currentWindow.setScene(new Scene(loader.load()));
            currentWindow.show();
        }
        else {
            txtFieldError.setText("Fehlgeschlagen, versuchen Sie es erneut.");
        }
    }

    public void btnRegistration(ActionEvent actionEvent) throws Exception {
        if (this.client.login("r", txtFieldName.getText(), pswFieldPassword.getText())) {
            this.client.setName(txtFieldName.getText());
            // get current window stage information
            Stage currentWindow = (Stage) (((Node) actionEvent.getSource()).getScene().getWindow());

            // setup new window
            FXMLLoader loader = new FXMLLoader(getClass().getResource("../Resource/chatWindow.fxml"));
            ChatController c = new ChatController(client);
            loader.setController(c);

            // show new window
            currentWindow.setTitle("Chatroom");
            Scene s = new Scene(loader.load());
            //damit können KeyEvents abgefangen werden um unser eigenes Verhalten festzulegen
            s.addEventFilter(KeyEvent.KEY_PRESSED, (KeyEvent event) -> {
                try {
                    if (event.getCode() == KeyCode.ENTER && c.txtFieldMessage.isFocused()) {
                        c.sendMessage();
                    }
                    if (event.getCode() == KeyCode.ENTER && c.txtFieldNickname.isFocused()) {
                        c.sendNickname();
                    }
                    //todo: tab cycle event handling
                    //if (event.getCode() == KeyCode.TAB){
                    //
                    //}
                    event.consume();
                }
                catch(Exception e){
                    e.printStackTrace();
                }
            });
            currentWindow.setScene(s);
            currentWindow.show();
        }
        else {
            txtFieldError.setText("Fehlgeschlagen, versuchen Sie es erneut.");
        }
    }

    // getter
    public Client getClient() {
        return client;
    }
}

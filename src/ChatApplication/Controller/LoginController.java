package ChatApplication.Controller;

import ChatApplication.Library.Client;
import ChatApplication.Library.Message;
import ChatApplication.Library.Mode;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.net.URL;
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
        // login() returns true for a valid login/registration
        Message message = client.login(Mode.LOGIN, txtFieldName.getText(), pswFieldPassword.getText());
        if (message.getMode() == Mode.ERROR) {
            txtFieldError.setText(message.getText());
        }
        else {
            changeWindow(actionEvent);
        }
    }

    public void btnRegistration(ActionEvent actionEvent) throws Exception {
        Message message = client.login(Mode.REGISTRATION, txtFieldName.getText(), pswFieldPassword.getText());
        if (message.getMode() == Mode.ERROR) {
            txtFieldError.setText(message.getText());
        }
        else {
            changeWindow(actionEvent);
        }
    }

    // changes the window from login to chat
    private void changeWindow(ActionEvent actionEvent) throws Exception {
        client.setName(txtFieldName.getText());
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

    // getter
    public Client getClient() {
        return client;
    }
}

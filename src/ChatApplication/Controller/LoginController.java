package ChatApplication.Controller;

import ChatApplication.Library.Client;
import ChatApplication.Library.Message;
import ChatApplication.Library.Mode;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;

import java.io.IOException;
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

    public void btnLogin(ActionEvent actionEvent) {
        login(actionEvent);
    }

    public void enter(KeyEvent keyEvent) {
        if (keyEvent.getCode() == KeyCode.ENTER)
            login(keyEvent);
    }

    private void login(Event event) {
        // login() returns true for a valid login/registration
        Message message = client.login(Mode.LOGIN, txtFieldName.getText(), pswFieldPassword.getText());
        if (message.getMode() == Mode.ERROR) {
            txtFieldError.setText(message.getText());
        } else {
            changeWindow(event);
        }
    }

    public void btnRegistration(ActionEvent actionEvent) {
        // login() returns true for a valid login/registration
        Message message = client.login(Mode.REGISTRATION, txtFieldName.getText(), pswFieldPassword.getText());
        if (message.getMode() == Mode.ERROR) {
            txtFieldError.setText(message.getText());
        } else {
            changeWindow(actionEvent);
        }
    }

    // changes the window from login to chat
    private void changeWindow(Event event) {
        try {
            client.setName(txtFieldName.getText());
            // get current window stage information
            Stage currentWindow = (Stage) (((Node) event.getSource()).getScene().getWindow());

            // setup new window
            FXMLLoader loader = new FXMLLoader(getClass().getResource("../Resource/chatWindow.fxml"));
            ChatController controller = new ChatController(client);
            loader.setController(controller);

            // show new window
            currentWindow.setOnCloseRequest(e -> {
                System.out.println("normaler chat geschlossen");    //todo
                controller.logoutRequest();
                currentWindow.close();
            });
            currentWindow.setTitle("ChatApp");
            currentWindow.setScene(new Scene(loader.load()));
            currentWindow.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // getter
    public Client getClient() {
        return client;
    }
}

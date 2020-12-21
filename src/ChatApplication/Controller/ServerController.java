package ChatApplication.Controller;

import ChatApplication.Library.User;
import ChatApplication.Library.UserHandler;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.control.*;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class ServerController implements Initializable {
    public TextArea txtAreaServerlog;
    public TextArea txtAreaInfo;
    public TextField txtFieldStateServer;
    public TextField txtFieldTask;
    public TextField txtFieldStateTask;
    public ListView listUser;
    public ListView listRooms;
    public ChoiceBox chBoxTasks;
    public Button btnTask;

    private UserHandler.Server server;

    public void initialize(URL location, ResourceBundle resource) {
        try {
            this.server = new UserHandler.Server(5000);
            txtFieldStateServer.setText("Port " + this.server.getPort() + " ist offen.");

            while (true) {
                User client = this.server.listenForNewClients();
                new Thread(new UserHandler(this.server, this.server.listenForNewClients(), txtAreaServerlog,
                        txtFieldStateServer, listUser, listRooms)).start();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void btnTask(ActionEvent actionEvent) {

    }
}

package ChatApplication.Controller;

import ChatApplication.Library.Client;
import ChatApplication.Library.Message;
import ChatApplication.Library.Mode;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Optional;
import java.util.ResourceBundle;

public class ChatController implements Initializable {
    public TextArea txtAreaChat;
    public TextField txtFieldMessage;
    public TextField txtFieldState;
    public ListView listRooms;
    public ListView listUser;


    private final Client client;
    private final ArrayList<Stage> activeStages = new ArrayList<Stage>();

    public ChatController(Client client) {
        this.client = client;
    }

    public void initialize(URL location, ResourceBundle resource) {
        client.createThreadReceivedMessagesHandler(txtAreaChat, txtFieldState, listRooms, listUser).start();
        // gets information about other clients and available rooms
        client.sendObject(new Message(client.getName(), Mode.INFORMATION_REQUEST, client.getActiveRoom(), ""));
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
        if (result.isPresent() && !result.get().isEmpty()) {
            client.sendObject(new Message(client.getName(), Mode.CHANGE_NAME, client.getActiveRoom(), result.get()));
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
            client.sendObject(new Message(client.getName(), Mode.DELETE_ACCOUNT, client.getActiveRoom(), ""));
        }
    }

    public void menuItemChangePassword() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Passwort ändern");
        dialog.setHeaderText("");
        dialog.setContentText("Bitte neues Passwort eingeben");
        Optional<String> result = dialog.showAndWait();
        if (result.isPresent() && !result.get().isEmpty()) {
            client.sendObject(new Message(client.getName(), Mode.CHANGE_PASSWORD, result.get()));
        }
    }

    public void menuItemCreateRoom() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Raum erstellen");
        dialog.setHeaderText("");
        dialog.setContentText("Bitte Raumnamen eingeben");
        Optional<String> result = dialog.showAndWait();
        if (result.isPresent() && !result.get().isEmpty()) {
            createRoom(result.get());
        }
    }

    public void menuItemCreatePrivateRoom() {
        ChoiceDialog dialog = new ChoiceDialog();
        dialog.setTitle("Privaten Raum erstellen");
        dialog.setHeaderText("");
        for (Object item : listUser.getItems()) {
            if (!item.equals(client.getName())) {
                dialog.getItems().add(item);
            }
        }
        dialog.setContentText("Bitte Namen auswählen");
        Optional<String> result = dialog.showAndWait();
        if (result.isPresent()) {
            if (!isActiveStage(result.get())) {
                createPrivateRoom(result.get());
            }
        }
    }

    // switches rooms
    public void listRoomsClicked() {
        Object item = listRooms.getSelectionModel().getSelectedItem();
        if (listRooms.getSelectionModel().getSelectedItem() != null &&
                listRooms.getSelectionModel().getSelectedItem() != client.getActiveRoom()) {
            String newRoom = item.toString();
            client.sendObject(new Message(client.getName(), Mode.ROOM_JOIN, client.getActiveRoom(), newRoom));
        }
    }

    public void listUsersClicked() {
        Object item = listUser.getSelectionModel().getSelectedItem();
        if (item != null) {
            client.setSelectedUser(item.toString());
        } else {
            client.setSelectedUser("");
        }
    }

    // sends a message to the server
    public void sendMessage() {
        if (!txtFieldMessage.getText().equals("")) {
            client.sendObject(new Message(client.getName(), Mode.MESSAGE, client.getActiveRoom(), txtFieldMessage.getText()
            ));
            txtFieldMessage.clear();
        }
    }

    // sends a logout request to the server
    public void logoutRequest() {
        client.sendObject(new Message(client.getName(), Mode.LOGOUT, client.getActiveRoom(),
                client.getName() + "hat den Chat verlassen."));
    }

    // creates a new community room
    public void createRoom(String roomName) {
        client.sendObject(new Message(client.getName(), Mode.ROOM_CREATE, client.getActiveRoom(), roomName));
    }

    // opens a new private chat in an new window
    public void createPrivateRoom(String otherClient) {
        newWindow(otherClient);
    }

    // creates a new window for a private chat
    private void newWindow(String otherClient) {
        try {
            PrivateChatController controller = new PrivateChatController(client.getName(), otherClient);
            Stage stage = new Stage();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("../Resource/privateChat.fxml"));
            loader.setController(controller);
            stage.setOnCloseRequest(e -> {
                controller.logoutRequest();
                stage.close();
                activeStages.remove(stage);
            });
            stage.setTitle("Privater Chat mit " + otherClient);
            stage.setScene(new Scene(loader.load()));
            activeStages.add(stage);

            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // checks if the name is the active stage
    private boolean isActiveStage(String name) {
        for (Stage s : activeStages) {
            if (s.getTitle().equals("Privater Chat mit " + name)) {
                return true;
            }
        }
        return false;
    }
}

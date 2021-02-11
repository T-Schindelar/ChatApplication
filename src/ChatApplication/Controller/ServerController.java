package ChatApplication.Controller;

import ChatApplication.Library.*;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;

import java.io.IOException;
import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;

public class ServerController implements Initializable, Runnable {
    public TextArea txtAreaServerlog;
    public TextArea txtAreaInfo;
    public TextField txtFieldStateServer;
    public ListView listUser;
    public ListView listRooms;

    private Server server;

    public void initialize(URL location, ResourceBundle resource) {
        try {
            this.server = new Server(5000);
            txtFieldStateServer.setText("Port " + server.getPort() + " ist offen.");
            listRooms.getItems().add("Lobby");
            new Thread(this).start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void run() {
        try {
            while (true) {
                User client = server.listenForNewClients();
                new Thread(new UserHandler(server, client, txtAreaServerlog,
                        txtFieldStateServer, listUser, listRooms)).start();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void menuItemWarnUser(ActionEvent actionEvent) {
        ChoiceDialog dialog = new ChoiceDialog();
        dialog.setTitle("Nutzer verwarnen");
        dialog.setHeaderText("");
        for (Object item : listUser.getItems()) {
            dialog.getItems().add(item);
        }
        dialog.setContentText("Bitte Nutzer auswählen");
        Optional<String> result = dialog.showAndWait();
        if (result.isPresent()) {
            server.warnUser(result.get());
            txtAreaServerlog.setText(txtAreaServerlog.getText() + new Message("Server", Mode.MESSAGE,
                    "Nutzer: " + result.get() + " wurde verwarnt.") + "\n");
        }
    }

    public void menuItemTimeOutUser(ActionEvent actionEvent) {
        ChoiceDialog dialog = new ChoiceDialog();
        dialog.setTitle("Nutzer temporär ausschließen");
        dialog.setHeaderText("");
        for (Object item : listUser.getItems()) {
            dialog.getItems().add(item);
        }
        dialog.setContentText("Bitte Nutzer auswählen");
        Optional<String> result = dialog.showAndWait();
        if (result.isPresent()) {
            server.disconnectUser(result.get());
            server.populateList(server.getClientNames(), listUser);
            txtAreaServerlog.setText(txtAreaServerlog.getText() + new Message("Server", Mode.MESSAGE,
                    "Nutzer: " + result.get() + " wurde entfernt.") + "\n");
        }
    }

    public void menuItemBanUser(ActionEvent actionEvent) {
        ChoiceDialog dialog = new ChoiceDialog();
        dialog.setTitle("Nutzer permanent sperren");
        dialog.setHeaderText("");
        for (Object item : listUser.getItems()) {
            dialog.getItems().add(item);
        }
        dialog.setContentText("Bitte Nutzer auswählen");
        Optional<String> result = dialog.showAndWait();
        if (result.isPresent()) {
            server.banAccount(result.get());
            txtAreaServerlog.setText(txtAreaServerlog.getText() + new Message("Server", Mode.MESSAGE,
                    "Nutzer: " + result.get() + " wurde geperrt.") + "\n");
        }
    }

    public void menuItemCreateRoom(ActionEvent actionEvent) throws IOException {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Raum erstellen");
        dialog.setHeaderText("");
        dialog.setContentText("Bitte Raumnamen eingeben");
        Optional<String> result = dialog.showAndWait();
        if (result.isPresent()) {
            server.addRoom(result.get());
            txtAreaServerlog.setText(txtAreaServerlog.getText() + new Message("Server", Mode.MESSAGE,
                    "neuer Raum: " + result.get() + " wurde angelegt.") + "\n");
            listRooms.getItems().add(result.get());
        }
    }

    public void menuItemEditRoom(ActionEvent actionEvent) {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Raum ändern");
        dialog.setHeaderText("");
        dialog.setContentText("Bitte neuen Namen eingeben");
        Optional<String> result = dialog.showAndWait();
        if (result.isPresent()) {
            server.changeRoomName(server.getSelectedRoom(), result.get(), listRooms);
        }
    }

    public void menuItemDeleteRoom(ActionEvent actionEvent) {
        ChoiceDialog dialog = new ChoiceDialog();
        dialog.setTitle("Raum löschen");
        dialog.setHeaderText("");
        for (Object item : listRooms.getItems()) {
            if (!item.equals("Lobby")) {
                dialog.getItems().add(item);
            }
        }
        dialog.setContentText("Bitte Raumnamen auswählen");
        Optional<String> result = dialog.showAndWait();
        if (result.isPresent()) {
            server.deleteRoom(result.get());
            listRooms.getItems().clear();
            server.populateList(server.getRoomNames(), listRooms);
        }
    }

    // writes information about the client in the info box
    public void listUserClicked(MouseEvent mouseEvent) {
        Object item = listUser.getSelectionModel().getSelectedItem();   //String item = listUser.getSelectionModel().getSelectedItem().toString();
        if (item != null) {
            txtAreaInfo.clear();
            txtAreaInfo.setText(String.format("%s befindet sich in Raum %s", item.toString(),
                    server.getRoomNameForUser(server.getClientFromClientsByName(item.toString()))));
        }
    }

    // todo rauminfos anzeigen
    public void listRoomsClicked() {
        Object item = listRooms.getSelectionModel().getSelectedItem();
        if (item != null) {
            server.setSelectedRoom(item.toString());
        } else {
            server.setSelectedRoom("");
        }
    }

}

package ChatApplication;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class ServerApp extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception{
        // get root
        Parent serverWindowRoot = FXMLLoader.load(getClass().getResource("Resource/serverWindow.fxml"));

        // show and start controller
        primaryStage.setTitle("Observer");
        primaryStage.setScene(new Scene(serverWindowRoot));
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}

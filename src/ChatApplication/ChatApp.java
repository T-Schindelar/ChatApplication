package ChatApplication;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class ChatApp extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception{
        // get root
        Parent loginFormRoot = FXMLLoader.load(getClass().getResource("Resource/loginForm.fxml"));

        // show and start controller
        primaryStage.setTitle("ChatApp");

        primaryStage.setScene(new Scene(loginFormRoot));
        primaryStage.show();
    }

    @Override
    public void stop() throws Exception {
        super.stop();
        System.exit(0);
    }

    public static void main(String[] args) {
        launch(args);
    }
}

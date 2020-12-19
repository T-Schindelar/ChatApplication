package ChatApplication;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception{
        // get roots
        Parent loginFormRoot = FXMLLoader.load(getClass().getResource("Resource/loginForm.fxml"));

        // show
        primaryStage.setTitle("Chatroom");
        primaryStage.setScene(new Scene(loginFormRoot));
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}

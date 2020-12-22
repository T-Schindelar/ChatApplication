package ChatApplication;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;

public class ChatApp extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception{
        // get root
        Parent loginFormRoot = FXMLLoader.load(getClass().getResource("Resource/loginForm.fxml"));
        System.out.println("test");

        // show and start controller
        primaryStage.setTitle("ChatApp");

        primaryStage.setScene(new Scene(loginFormRoot));
        primaryStage.show();
    }

    @Override
    public void stop() throws Exception {
        try {
            super.stop();
        }
        catch (Exception e){
            e.printStackTrace();
            System.out.println("stop"); // todo
        }



        //System.exit(0);
    }

    public static void main(String[] args) {
        launch(args);
    }
}

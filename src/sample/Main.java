package sample;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception{
        setPrimaryStage(primaryStage); // **Set the Stage**
        Parent root = FXMLLoader.load(getClass().getResource("sample.fxml"));
        primaryStage.setTitle("Tiny PNG Recursive compressor");
        primaryStage.setScene(new Scene(root, 300, 275));
        primaryStage.show();
    }

    private static Stage primaryStage; // **Declare static Stage**

    private void setPrimaryStage(Stage stage) {
        Main.primaryStage = stage;
    }

    static public Stage getPrimaryStage() {
        return Main.primaryStage;
    }

    public static void main(String[] args) {
        launch(args);
    }
}

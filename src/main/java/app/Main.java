package app;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class Main extends Application {

    @Override
    public void start(Stage stage) {
        Button play = new Button("▶");
        Button next = new Button("⏭");
        Button like = new Button("♡");

        HBox root = new HBox(10, play, next, like);
        root.setStyle("-fx-background-color: rgba(10,10,20,0.8); -fx-padding: 10; -fx-background-radius: 12;");

        Scene scene = new Scene(root);

        stage.initStyle(StageStyle.UNDECORATED);
        stage.setAlwaysOnTop(true);
        stage.setScene(scene);
        stage.setWidth(220);
        stage.setHeight(80);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}

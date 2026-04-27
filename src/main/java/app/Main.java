package app;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class Main extends Application {
    private double dragOffsetX;
    private double dragOffsetY;

    @Override
    public void start(Stage stage) {
        WebView hiddenPlayer = new WebView();
        hiddenPlayer.setPrefSize(1, 1);
        hiddenPlayer.setMaxSize(1, 1);
        hiddenPlayer.setOpacity(0.01);
        WebEngine engine = hiddenPlayer.getEngine();
        YandexMusicController controller = new YandexMusicController(engine);
        controller.openMusic();

        Label source = new Label("МОЯ ВОЛНА");
        source.getStyleClass().add("source-label");

        Label title = new Label("Ya Music Vidjet");
        title.getStyleClass().add("track-title");
        Label artist = new Label("скрытый WebView + neon HUD");
        artist.getStyleClass().add("track-artist");

        Button wave = neonButton("WAVE", () -> controller.openMyWave());
        Button prev = neonButton("PREV", () -> controller.previous());
        Button play = neonButton("PLAY", () -> controller.playPause());
        Button next = neonButton("NEXT", () -> controller.next());
        Button like = neonButton("LIKE", () -> controller.like());
        Button dislike = neonButton("NOPE", () -> controller.dislike());
        Button close = neonButton("×", stage::close);
        close.getStyleClass().add("close-button");

        Slider volume = new Slider(0, 100, 65);
        volume.getStyleClass().add("neon-slider");
        volume.valueProperty().addListener((obs, oldValue, newValue) -> controller.setVolume(newValue.doubleValue() / 100.0));

        HBox top = new HBox(12, source, spacer(), wave, close);
        top.getStyleClass().add("top-row");

        HBox controls = new HBox(12, prev, play, next, like, dislike);
        controls.getStyleClass().add("controls-row");

        VBox info = new VBox(3, title, artist);
        HBox volumeRow = new HBox(10, new Label("VOL"), volume);
        HBox.setHgrow(volume, Priority.ALWAYS);
        volumeRow.getStyleClass().add("volume-row");

        VBox panel = new VBox(14, top, info, controls, volumeRow);
        panel.getStyleClass().add("neon-panel");
        panel.setPadding(new Insets(16));

        StackPane root = new StackPane(panel, hiddenPlayer);
        root.getStyleClass().add("root");
        StackPane.setMargin(hiddenPlayer, new Insets(0));

        Scene scene = new Scene(root, 520, 210);
        scene.setFill(null);
        scene.getStylesheets().add(getClass().getResource("/app/neon.css").toExternalForm());

        enableDragAndSnap(stage, panel);
        stage.initStyle(StageStyle.TRANSPARENT);
        stage.setAlwaysOnTop(true);
        stage.setScene(scene);
        stage.show();
    }

    private Button neonButton(String text, Runnable action) {
        Button button = new Button(text);
        button.getStyleClass().add("neon-button");
        button.setOnAction(event -> action.run());
        return button;
    }

    private Region spacer() {
        Region region = new Region();
        HBox.setHgrow(region, Priority.ALWAYS);
        return region;
    }

    private void enableDragAndSnap(Stage stage, Region dragSurface) {
        dragSurface.addEventFilter(MouseEvent.MOUSE_PRESSED, event -> {
            dragOffsetX = event.getSceneX();
            dragOffsetY = event.getSceneY();
        });

        dragSurface.addEventFilter(MouseEvent.MOUSE_DRAGGED, event -> {
            stage.setX(event.getScreenX() - dragOffsetX);
            stage.setY(event.getScreenY() - dragOffsetY);
        });

        dragSurface.addEventFilter(MouseEvent.MOUSE_RELEASED, event -> snapToScreenEdges(stage));
    }

    private void snapToScreenEdges(Stage stage) {
        Rectangle2D bounds = Screen.getPrimary().getVisualBounds();
        double snap = 28;
        double x = stage.getX();
        double y = stage.getY();

        if (Math.abs(x - bounds.getMinX()) < snap) x = bounds.getMinX();
        if (Math.abs((x + stage.getWidth()) - bounds.getMaxX()) < snap) x = bounds.getMaxX() - stage.getWidth();
        if (Math.abs(y - bounds.getMinY()) < snap) y = bounds.getMinY();
        if (Math.abs((y + stage.getHeight()) - bounds.getMaxY()) < snap) y = bounds.getMaxY() - stage.getHeight();

        stage.setX(x);
        stage.setY(y);
    }

    public static void main(String[] args) {
        launch();
    }
}

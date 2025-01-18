package svkreml.shimeji;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.geometry.Bounds;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;

import java.io.FileInputStream;
import java.io.FileNotFoundException;

public class JavaFXShimejiApp extends Application {

    private ImageView shimejiImageView;

    @Override
    public void start(Stage primaryStage) throws FileNotFoundException {

        Group root = new Group();
        Image shimejiImage = new Image(new FileInputStream("shimeji.png"));
        shimejiImageView = new ImageView(shimejiImage);
        shimejiImageView.setFitWidth(128);
        shimejiImageView.setFitHeight(128);

        root.getChildren().add(shimejiImageView);
        Scene scene = new Scene(root, 128, 128);
        scene.setFill(null);

        primaryStage.setScene(scene);
        primaryStage.initStyle(StageStyle.TRANSPARENT);
        primaryStage.setAlwaysOnTop(true);
        primaryStage.setTitle("JavaFX Shimeji App");
        primaryStage.show();




    }

    private boolean isCharacterClicked(MouseEvent event) {
        Bounds characterBounds = shimejiImageView.localToScene(shimejiImageView.getBoundsInLocal());
        return characterBounds.contains(event.getSceneX(), event.getSceneY());
    }

    public static void main(String[] args) {
        launch(args);
    }
}
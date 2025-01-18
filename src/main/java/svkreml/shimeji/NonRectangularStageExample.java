package svkreml.shimeji;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.robot.Robot;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Shape;
import javafx.stage.Stage;
import javafx.stage.StageStyle;


import java.io.FileInputStream;
import java.io.IOException;

public class NonRectangularStageExample extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws IOException {
        primaryStage.initStyle(StageStyle.TRANSPARENT);
        primaryStage.setAlwaysOnTop(true);
        StackPane stackPane = new StackPane();

        // Create a non-rectangular shape (e.g., a circle)
        Circle circle = new Circle(100, 100, 50);
        //circle.setFill(Color.TRANSPARENT);
        circle.setStroke(Color.BLACK);


        stackPane.setShape(createWindowShape(new Image(new FileInputStream("shimeji.png"))));
        stackPane.getChildren().add(circle);
        stackPane.setMouseTransparent(true);
        Scene scene = new Scene(stackPane, 200, 200);
        scene.setFill(Color.TRANSPARENT);

        primaryStage.setScene(scene);

        //stackPane.setPickOnBounds(true);

        primaryStage.setAlwaysOnTop(true);
        primaryStage.show();

/*        Robot robot = new Robot();
        scene.addEventFilter(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                double mouseX = event.getX();
                double mouseY = event.getY();

                if (!circle.contains(mouseX, mouseY)) {
                    event.consume();
                    Platform.runLater(() -> {
                        stackPane.setVisible(false);
                        Platform.runLater(() -> {
                            robot.mouseClick(event.getButton());
                            event.consume();
                            stackPane.setVisible(true);
                        });
                    });
                }
            }
        });*/
/*        scene.addEventFilter(MouseEvent.MOUSE_PRESSED, new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                double mouseX = event.getX();
                double mouseY = event.getY();

                if (!circle.contains(mouseX, mouseY)) {

                    event.consume(); // Consume the event to prevent handling within the Stage
                    robot.mousePress(event.getButton());
                    stackPane.setVisible(true);
                }
            }
        });
        scene.addEventFilter(MouseEvent.MOUSE_RELEASED, new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                double mouseX = event.getX();
                double mouseY = event.getY();

                if (!circle.contains(mouseX, mouseY)) {
                    stackPane.setVisible(false);
                    event.consume(); // Consume the event to prevent handling within the Stage
                    robot.mouseRelease(event.getButton());
                    stackPane.setVisible(true);
                }
            }
        });*/
    }


    private static Shape createWindowShape(Image mask) {
        int width = (int) mask.getWidth();
        int height = (int) mask.getHeight();
        Polygon polygon = new Polygon();

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int alpha = (mask.getPixelReader().getArgb(x, y) >> 24) & 0xFF;
                if (alpha > 130) {
                    polygon.getPoints().add((double) x);
                    polygon.getPoints().add((double) y);
                }
            }
        }
        return polygon;
    }


}
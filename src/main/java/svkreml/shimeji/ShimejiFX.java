package svkreml.shimeji;

import com.group_finity.mascot.environment.Area;
import com.group_finity.mascot.x11.X;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.application.ConditionalFeature;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.geometry.Point2D;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;

import java.awt.*;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;

public class ShimejiFX extends Application {

    private final Random random = new Random();
    private final int number = 8;
    private final int dy = 30;
    private final List<ImageView> walkImagesLeft = new ArrayList<>();
    private final List<ImageView> walkImagesRight = new ArrayList<>();
    private final List<ImageView> standImages = new ArrayList<>();
    private final List<ImageView> dragImages = new ArrayList<>();
    private final List<ImageView> fallImages = new ArrayList<>();
    private final X.Display display = new X.Display();
    private final ArrayList<Number> badStateList = new ArrayList<>();
    private final ArrayList<Number> badTypeList = new ArrayList<>();
    public Area activeIE = new Area();
    public String activeIETitle = null;
    Group group = new Group();
    ImageView currentImageView = new ImageView();
    int maxX;
    int maxY;
    X.Window activeWindow = null;
    // Environment environment = NativeFactoryImpl.getInstance().getEnvironment();
    private int x = 4000;
    int oldX = x;
    private int y = 0;
    int oldY = y;
    private int speedX = 0;
    private int speedY = 0;
    private int dx = 0;
    private List<ImageView> currentImages;
    private Point2D initialClick;
    private int currentFrameIndex = 0;
    private int tick = 0;
    private Stage primaryStage;
    private boolean dragged = false;
    private int minimizedValue;
    private int dockValue;

    public static void main(String[] args) {
        launch(args);
    }

    private static Image flip(final Image src) {
        final WritableImage copy = new WritableImage((int) src.getWidth(), (int) src.getHeight());
        for (int y = 0; y < src.getHeight(); ++y) {
            for (int x = 0; x < src.getWidth(); ++x) {
                copy.getPixelWriter().setColor((int) (copy.getWidth() - x - 1), y, src.getPixelReader().getColor(x, y));
            }
        }
        return copy;
    }

    @Override
    public void start(Stage primaryStage) throws FileNotFoundException {
        badStateList.add(Integer.decode(display.getAtom("_NET_WM_STATE_MODAL").toString()));
        badStateList.add(Integer.decode(display.getAtom("_NET_WM_STATE_HIDDEN").toString()));
        minimizedValue = Integer.decode(display.getAtom("_NET_WM_STATE_HIDDEN").toString());
        badStateList.add(Integer.decode(display.getAtom("_NET_WM_STATE_ABOVE").toString()));
        badTypeList.add(Integer.decode(display.getAtom("_NET_WM_WINDOW_TYPE_DOCK").toString()));
        dockValue = Integer.decode(display.getAtom("_NET_WM_WINDOW_TYPE_DOCK").toString());
        badTypeList.add(Integer.decode(display.getAtom("_NET_WM_WINDOW_TYPE_MENU").toString()));
        badTypeList.add(Integer.decode(display.getAtom("_NET_WM_WINDOW_TYPE_SPLASH").toString()));
        badTypeList.add(Integer.decode(display.getAtom("_NET_WM_WINDOW_TYPE_DIALOG").toString()));
        badTypeList.add(Integer.decode(display.getAtom("_NET_WM_WINDOW_TYPE_DESKTOP").toString()));


        this.primaryStage = primaryStage;
        screenSize();


        System.out.println("ConditionalFeature.SCENE3D:" + Platform.isSupported(ConditionalFeature.SCENE3D));
        System.out.println("Loading images...");
        loadImages();
        System.out.println("Creating transparent window...");
        createTransparentWindow();
        System.out.println("Starting animation...");
        startAnimation();
        addDrag();
    }

    private void createTransparentWindow() throws FileNotFoundException {


        Image shimejiImage = new Image(new FileInputStream("shimeji.png"));
        ImageView shimejiImageView = new ImageView(shimejiImage);
        shimejiImageView.setFitWidth(128);
        shimejiImageView.setFitHeight(128);


        primaryStage.initStyle(StageStyle.TRANSPARENT);
        primaryStage.setAlwaysOnTop(true);
        primaryStage.setTitle("JavaFX Shimeji App");
        primaryStage.show();


        primaryStage.setX(x);
        primaryStage.setY(y);


        currentImageView = new ImageView(currentImages.get(currentFrameIndex).getImage());
        group.getChildren().add(currentImageView);


        Scene scene = new Scene(group);
        scene.setFill(null);
        primaryStage.setScene(scene);

        // javafx.stage.Window


    }

    private void addDrag() {
        currentImageView.setOnMousePressed(event -> {
            // initialClick = new Point2D(event.getSceneX(), event.getSceneY());
            initialClick = new Point2D(70, 10);
            dragged = true;
            tick = 0;
            currentFrameIndex = 0;
        });

        AtomicLong last = new AtomicLong(System.currentTimeMillis());

        currentImageView.setOnMouseDragged(event -> {
            if (System.currentTimeMillis() - last.get() > 40) {
                double deltaX = event.getSceneX() - initialClick.getX();
                double deltaY = event.getSceneY() - initialClick.getY();
                x = (int) primaryStage.getX();
                y = (int) primaryStage.getY();
                primaryStage.setX(x + deltaX);
                primaryStage.setY(y + deltaY);
                // setSpeed();
                last.set(System.currentTimeMillis());
            }
        });


        currentImageView.setOnMouseReleased(event -> {
            dragged = false;
            tick = 0;
            currentFrameIndex = 0;
            // setSpeed();
        });
    }

    private void setSpeed() {

        speedX = x - oldX;
        speedY = y - oldY;
        oldX = x;
        oldY = y;
    }

    private void startAnimation() {
        Timeline timeline = new Timeline(new KeyFrame(Duration.millis(40), e -> {


            if (!dragged) {
                if (isAboveIE() || (y + primaryStage.getHeight() >= maxY)) {
                    move();
                } else {
                    fall();
                }
            } else {
                setSpeed();
                currentImages = dragImages;
            }
            updateFrameIndex();
            ImageView newImageView = currentImages.get(currentFrameIndex);
            currentImageView.setImage(newImageView.getImage());

            newImageView.setTranslateX(x);
            newImageView.setTranslateY(y);


        }));
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
    }

    private boolean isAboveIE() {
        boolean overWindow = getActiveIE().getLeft() <= (x - primaryStage.getWidth() / 2)
                && getActiveIE().getRight() >= (x + primaryStage.getWidth() / 2)
                && getActiveIE().getTop() == y;
        //  System.out.println(overWindow);
        return overWindow;
    }

    private void screenSize() {
        Timeline timeline = new Timeline(new KeyFrame(Duration.millis(160), e -> {
            ObservableList<Screen> screensForRectangle = Screen.getScreensForRectangle(primaryStage.getX(), primaryStage.getY(), primaryStage.getWidth(), primaryStage.getHeight());
            Screen screen = screensForRectangle.isEmpty() ?
                    Screen.getPrimary() : screensForRectangle.get(0);
            maxX = (int) screen.getVisualBounds().getMaxX();
            maxY = (int) screen.getVisualBounds().getMaxY();
            updateActiveIE();
            //System.out.println("maxX: " + maxX + ", maxY: " + maxY);
        }));
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
    }

    private void fall() {
        checkBounds();
        currentImages = fallImages;


    }

    private void checkBounds() {
        if (x < 0) x = 0;
        if (y < 0) y = 0;
        if (x > maxX - primaryStage.getWidth()) {
            x = (int) (maxX - primaryStage.getWidth());
        }

        if (getActiveIE().getLeft() <= (x - primaryStage.getWidth() / 2) && getActiveIE().getRight() >= (x + primaryStage.getWidth() / 2)
                && (
                        y >= getActiveIE().getTop() - speedY
                                && y <= getActiveIE().getTop()
        )
        ) {
            speedY = 0;
            y = getActiveIE().getTop();
            if (y < 0) y = 0;

        } else if (y >= maxY - primaryStage.getHeight()) {
            y = (int) (maxY - primaryStage.getHeight());

        } else {
            speedX();
            speedY();
            x = x + speedX;
            y = y + speedY;
        }
        primaryStage.setX(x);
        primaryStage.setY(y);
    }

    private void speedX() {
        int airSpeedDecrease = 0;
        speedX = Math.min(speedX, 150);
        speedX = Math.max(speedX, -150);

        if (x < 10 && speedX < 0) {
            speedX = -speedX / 2;
        }

        if (x > maxX - 10 - 128 && speedX > 0) {
            speedX = -speedX / 2;
        }


        if (speedX > airSpeedDecrease) {
            speedX -= airSpeedDecrease;
        } else if (speedX < -airSpeedDecrease) {
            speedX += airSpeedDecrease;
        } else {
            speedX = 0;
        }

        speedX *= 0.97;
    }

    private void speedY() {

        if (y < 20 && speedY < 0) {
            speedY = -speedY / 2;
        }

        speedY = Math.max(speedY, -100);
        speedY = Math.min(speedY, 150);
        if (speedY < 0) {
            speedY *= 0.98;
        }
        speedY += 4;
    }

    private void move() {
        if (random.nextInt(100) < 5) {
            dx = 5 * (random.nextInt(3) - 1); // -1, 0, or 1
        }


        if (dx != 0) {
            if (dx < 0) {
                currentImages = walkImagesLeft;
            } else {
                currentImages = walkImagesRight;
            }
        } else {
            currentImages = standImages;
        }

        x += dx;

        checkBounds();
    }

    private void updateFrameIndex() {
        tick = (tick + 1) % (currentImages.size() * number);

        currentFrameIndex = tick / number;
    }

    private void loadImages() {

        loadImage();

        loadImage(standImages, 4, "/home/svkreml/.config/JetBrains/IntelliJIdea2024.2/scratches/stand");
        loadImage(dragImages, 5, "/home/svkreml/.config/JetBrains/IntelliJIdea2024.2/scratches/drag/drag");
        loadImage(fallImages, 2, "/home/svkreml/.config/JetBrains/IntelliJIdea2024.2/scratches/fall/fall");
        // Set initial images to standing
        currentImages = standImages;
    }

    private void loadImage(List<ImageView> standImages, int x, String x1) {
        for (int i = 1; i <= x; i++) {
            try {
                Image img = new Image(new FileInputStream(x1 + i + ".png"));
                ImageView imageView = new ImageView(img);
                imageView.setFitWidth(128);
                imageView.setFitHeight(128);
                standImages.add(imageView);
            } catch (IOException e) {
                System.err.println("Failed to load image: stand" + i + ".png");
            }
        }
    }

    private void loadImage() {
        for (int i = 1; i <= 4; i++) {
            try {
                Image img = new Image(new FileInputStream("/home/svkreml/.config/JetBrains/IntelliJIdea2024.2/scratches/walk" + i + ".png"));
                ImageView imageViewLeft = new ImageView(img);
                walkImagesLeft.add(imageViewLeft);

                ImageView imageViewRight = new ImageView(flip(img));
                walkImagesRight.add(imageViewRight);
            } catch (IOException e) {
                System.err.println("Failed to load image: walk" + i + ".png");
            }
        }
    }

    private void updateActiveIE() {

        try {
            final X.Window window = display.getActiveWindow();
            int desktop = window.getDesktop();
            int curDesktop = display.getActiveDesktopNumber();
            boolean badDesktop = ((desktop != curDesktop) && (desktop != -1));
            boolean badState = checkState(window.getState());
            boolean badType = checkType(window.getType());
            final String title = window.getTitle();

            if (title.startsWith("win")) {
                return;
            }

            if (title.equals(primaryStage.getTitle())) {
                return;
            }

            if (badDesktop || badType || badState) {
                // System.out.println(title);
                return;
            }


            final Rectangle windowBounds = window.getBounds();
            activeIETitle = title;
            Rectangle r = new Rectangle(
                    windowBounds.x,
                    windowBounds.y,
                    window.getGeometry().width,
                    window.getGeometry().height
            );
            Area a = new Area();
            a.set(r);
            a.setVisible(true);
            activeIE = a;
            activeWindow = window;

            final Area ie = getActiveIE();
            ie.set(r);
            ie.setVisible(true);


            activeIE.setVisible(activeIE.isVisible());
            activeIE.set(activeIE.toRectangle());
            activeIE.setTop(activeIE.getTop() - (int)primaryStage.getHeight());
            //    System.out.println(activeIETitle);

        } catch (X.X11Exception ignored) {
        }
    }

    private boolean checkState(int state) {
        return state == minimizedValue;
    }

    private boolean checkType(int type) {
        return badTypeList.contains(type);
    }

    public Area getActiveIE() {
        return activeIE;
    }

    public String getActiveIETitle() {
        return activeIETitle;
    }

}

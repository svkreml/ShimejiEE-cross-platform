package svkreml.shimeji;

import com.group_finity.mascot.image.ImagePairLoader;
import com.sun.jna.platform.WindowUtils;
import hqx.Hqx_2x;
import hqx.Hqx_3x;
import hqx.Hqx_4x;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;


public class Shimeji {


    private final Random random = new Random();
    private final int dy = 0;
    private int x = 1000;
    private int y = 1000;
    private int dx = 0;
    private List<ImageHolder> walkImagesLeft;
    private List<ImageHolder> walkImagesRight;
    private List<ImageHolder> standImages;
    private List<ImageHolder> currentImages;
    private int currentFrameIndex = 0;
    private boolean isMoving = false;
    private JFrame frame;
    private Point initialClick;
    public Shimeji() {
        loadImages();
        createTransparentWindow();
        startAnimation();
    }

    public static void main(String[] args) {
        System.setProperty("sun.java2d.noddraw", "true");
        System.setProperty("sun.java2d.opengl", "true");
        SwingUtilities.invokeLater(Shimeji::new);
    }

    private static BufferedImage flip(final BufferedImage src) {
        final BufferedImage copy = new BufferedImage(src.getWidth(), src.getHeight(),
                src.getType() == BufferedImage.TYPE_CUSTOM ? BufferedImage.TYPE_INT_ARGB : src.getType());

        for (int y = 0; y < src.getHeight(); ++y) {
            for (int x = 0; x < src.getWidth(); ++x) {
                copy.setRGB(copy.getWidth() - x - 1, y, src.getRGB(x, y));
            }
        }
        return copy;
    }

    private static BufferedImage premultiply(final BufferedImage source, final double opacity) {
        final BufferedImage returnImage = new BufferedImage(source.getWidth(), source.getHeight(),
                source.getType() == BufferedImage.TYPE_CUSTOM ? BufferedImage.TYPE_INT_ARGB_PRE : source.getType());
        Color colour;
        float[] components;

        for (int y = 0; y < returnImage.getHeight(); ++y) {
            for (int x = 0; x < returnImage.getWidth(); ++x) {
                colour = new Color(source.getRGB(x, y), true);
                components = colour.getComponents(null);
                components[3] *= opacity;
                components[0] = components[3] * components[0];
                components[1] = components[3] * components[1];
                components[2] = components[3] * components[2];
                colour = new Color(components[0], components[1], components[2], components[3]);
                returnImage.setRGB(x, y, colour.getRGB());
            }
        }

        return returnImage;
    }

    private static BufferedImage scale(final BufferedImage source, final double scaling, ImagePairLoader.Filter filter) {
        int width = source.getWidth();
        int height = source.getHeight();
        BufferedImage workingImage = null;

        double effectiveScaling = scaling;
        if (filter == ImagePairLoader.Filter.HQX && scaling > 1) {
            int[] buffer;
            int[] rbgValues = source.getRGB(0, 0, width, height, null, 0, width);

            if (scaling == 4 || scaling == 8) {
                width *= 4;
                height *= 4;
                buffer = new int[width * height];
                Hqx_4x.hq4x_32_rb(rbgValues, buffer, width / 4, height / 4);
                rbgValues = buffer;
                effectiveScaling = scaling > 4 ? 2 : 1;
            } else if (scaling == 3 || scaling == 6) {
                width *= 3;
                height *= 3;
                buffer = new int[width * height];
                Hqx_3x.hq3x_32_rb(rbgValues, buffer, width / 3, height / 3);
                rbgValues = buffer;
                effectiveScaling = scaling > 4 ? 2 : 1;
            } else if (scaling == 2) {
                width *= 2;
                height *= 2;
                buffer = new int[width * height];
                Hqx_2x.hq2x_32_rb(rbgValues, buffer, width / 2, height / 2);
                rbgValues = buffer;
                effectiveScaling = 1;
            } else
                filter = ImagePairLoader.Filter.NEAREST_NEIGHBOUR;

            if (filter == ImagePairLoader.Filter.HQX) {
                workingImage = new BufferedImage((int) Math.round(width * effectiveScaling), (int) Math.round(height * effectiveScaling), BufferedImage.TYPE_INT_ARGB_PRE);
                int srcColIndex = 0;
                int srcRowIndex = 0;

                for (int y = 0; y < workingImage.getHeight(); ++y) {
                    for (int x = 0; x < workingImage.getWidth(); ++x) {
                        workingImage.setRGB(x, y, rbgValues[srcColIndex / (int) effectiveScaling]);
                        ++srcColIndex;
                    }

                    ++srcRowIndex;
                    if (srcRowIndex != effectiveScaling)
                        srcColIndex -= workingImage.getWidth();
                    else
                        srcRowIndex = 0;
                }
            }
        }

        width = (int) Math.round(width * effectiveScaling);
        height = (int) Math.round(height * effectiveScaling);

        final BufferedImage copy = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB_PRE);

        Graphics2D g2d = copy.createGraphics();
        Object renderHint = filter == ImagePairLoader.Filter.BICUBIC
                ? RenderingHints.VALUE_INTERPOLATION_BICUBIC
                : RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR;

        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, renderHint);
        g2d.drawImage(workingImage != null ? workingImage : source, 0, 0, width, height, null);

        g2d.dispose();

        return copy;
    }

    private void loadImages() {
        walkImagesLeft = new ArrayList<>();
        walkImagesRight = new ArrayList<>();
        standImages = new ArrayList<>();


        for (int i = 1; i <= 4; i++) {
            try {
                BufferedImage img = scale(premultiply(ImageIO.read(
                        new FileInputStream("/home/svkreml/.config/JetBrains/IntelliJIdea2024.2/scratches/walk" + i + ".png")), 1.0f), 1.0f, ImagePairLoader.Filter.BICUBIC);
                walkImagesLeft.add(new ImageHolder(img));
                walkImagesRight.add(new ImageHolder(flip(img)));
            } catch (IOException e) {
                System.err.println("Failed to load image: walk" + i + ".png");
            }
        }


        for (int i = 1; i <= 4; i++) {
            try {
                BufferedImage img = scale(premultiply(ImageIO.read(
                        new FileInputStream("/home/svkreml/.config/JetBrains/IntelliJIdea2024.2/scratches/stand" + i + ".png")), 1.0f), 1.0f, ImagePairLoader.Filter.BICUBIC);
                standImages.add(new ImageHolder(img));
            } catch (IOException e) {
                System.err.println("Failed to load image: stand" + i + ".png");
            }
        }

        // Set initial images to standing
        currentImages = standImages;
    }

    private void createTransparentWindow() {
        frame = new JFrame(WindowUtils.getAlphaCompatibleGraphicsConfiguration());

        frame.setUndecorated(true);
        frame.setBackground(new Color(0, 0, 0, 0));
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel panel = getPanel();
        panel.setOpaque(true);
        panel.setDoubleBuffered(true);
        frame.add(panel);
        frame.pack();
        frame.setLocation(
                random.nextInt(GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDisplayMode().getWidth()),
                random.nextInt(GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDisplayMode().getHeight()));
        frame.setAlwaysOnTop(true); // Optional: Keep the shimeji always on top
        frame.setVisible(true);
        frame.setBounds(new Rectangle(3000, 1000, 128, 128));
        try {
            frame.setOpacity(1f);
        } catch (UnsupportedOperationException e) {
            System.err.println("Window opacity is not supported.");
        }
        WindowUtils.setWindowTransparent(frame, true);
        WindowUtils.setWindowAlpha(frame, 0.0f);


    }

    private JPanel getPanel() {
        JPanel panel = getjPanel();

        panel.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                initialClick = e.getPoint();
            }
        });

        panel.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                int thisX = frame.getLocation().x;
                int thisY = frame.getLocation().y;

                int xMoved = e.getX() - initialClick.x;
                int yMoved = e.getY() - initialClick.y;

                x = thisX + xMoved;
                y= thisY + yMoved;
                frame.setLocation(x, y);
            }
        });
        return panel;
    }

    private JPanel getjPanel() {
        //super.paintComponent(g);
        /*            @Override
            public Dimension getPreferredSize() {
                return new Dimension(128, 128); // Set the size of the panel to match the image size
            }*/

        // panel.setOpaque(false); // Make the panel transparent
        return new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (!currentImages.isEmpty()) {
                    ImageHolder currentImage = currentImages.get(currentFrameIndex);

                    g.drawImage(currentImage.image, 0, 0, null);
                    WindowUtils.setWindowMask(frame, currentImage.icon);
                }
            }
        };
    }

    private void startAnimation() {
        Timer timer = new Timer(160, e -> {
            move();
            updateFrameIndex();
            frame.repaint(100);
            Toolkit.getDefaultToolkit().sync(); // Sync the drawing operations
        });
        timer.start();
    }

    private void move() {
        if (random.nextInt(100) < 5) {
            dx = 10 * (random.nextInt(3) - 1); // -1, 0, or 1
            //  dy = 5 * (random.nextInt(3) - 1); // -1, 0, or 1
        }

        if (dx != 0 || dy != 0) {
            isMoving = true;
            if (dx < 0) {
                currentImages = walkImagesLeft;
            } else {
                currentImages = walkImagesRight;
            }
        } else {
            isMoving = false;
            currentImages = standImages;
        }

        x += dx;
        //y += dy;

        GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices()[1];
        DisplayMode dm = gd.getDisplayMode();

        if (x < 0) x = 500;
        if (y < 0) y = 500;
/*        if (x > dm.getWidth() - currentImages.get(currentFrameIndex).image.getWidth(null)) {
            x = dm.getWidth() - currentImages.get(currentFrameIndex).image.getWidth(null);
        }*/
 /*       if (y > dm.getHeight() - currentImages.get(currentFrameIndex).getHeight(null)) {
            y = dm.getHeight() - currentImages.get(currentFrameIndex).getHeight(null);
        }*/

        frame.setLocation(x, y);
      //  frame.move(x, y);
       // frame.setBounds(new Rectangle(x, y, 128, 128));
    }

    private void updateFrameIndex() {
        currentFrameIndex = (currentFrameIndex + 1) % currentImages.size();
    }

    public static class ImageHolder {
        public Image image;
        public Icon icon;
        public Shape shape;
        public ImageHolder(BufferedImage image) {
            this.image = Toolkit.getDefaultToolkit().createImage(image.getSource());
            this.icon = new ImageIcon(image);
            this.shape = createWindowShape(image);
        }
    }


    private static Shape createWindowShape(BufferedImage mask) {
        int width = mask.getWidth();
        int height = mask.getHeight();
        Path2D path = new Path2D.Double();

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int alpha = (mask.getRGB(x, y) >> 24) & 0xFF;
                if (alpha > 130) {
                    path.append(new Rectangle2D.Double(x, y, 1, 1), false);
                }
            }
        }

        return path;
    }
}
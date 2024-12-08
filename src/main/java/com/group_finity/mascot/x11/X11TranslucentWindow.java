package com.group_finity.mascot.x11;

import com.group_finity.mascot.image.NativeImage;
import com.group_finity.mascot.image.TranslucentWindow;
import com.sun.jna.platform.WindowUtils;

import javax.swing.*;
import java.awt.*;

public class X11TranslucentWindow extends JWindow implements TranslucentWindow {


    /**
     *
     */
    private static final long serialVersionUID = 1L;
    private NativeImage prevImage = null;

    /**
     * To view images.
     */
    private X11NativeImage image;

    private JPanel panel;
    private float alpha = 1.0f;

    public X11TranslucentWindow() {
        super(WindowUtils.getAlphaCompatibleGraphicsConfiguration());
        this.init();

        this.panel = new JPanel() {
            /**
             *
             */
            private static final long serialVersionUID = 1L;

            @Override
            protected void paintComponent(final Graphics g) {
                g.drawImage(getImage().getManagedImage(), 0, 0, null);
            }
        };
        this.setContentPane(this.panel);
    }

    private void init() {
        System.setProperty("sun.java2d.noddraw", "true");
        System.setProperty("sun.java2d.opengl", "true");
    }

    @Override
    public void setVisible(final boolean b) {
        super.setVisible(b);
        if (b) {
            WindowUtils.setWindowTransparent(this, true);
        }
    }

    @Override
    protected void addImpl(final Component comp, final Object constraints, final int index) {
        super.addImpl(comp, constraints, index);
        if (comp instanceof JComponent) {
            final JComponent jcomp = (JComponent) comp;
            jcomp.setOpaque(false);
        }
    }

    public void setAlpha(final float alpha) {
        WindowUtils.setWindowAlpha(this, alpha);
    }

    public float getAlpha() {
        return this.alpha;
    }

    @Override
    public Component asComponent() {
        return this;
    }

    @Override
    public String toString() {
        return "LayeredWindow[hashCode="+hashCode()+",bounds="+getBounds()+"]";
    }

    public X11NativeImage getImage() {
        return this.image;
    }

    public void setImage(final NativeImage image) {
        this.image = (X11NativeImage)image;
    }

    public void updateImage() {
        if (getImage() != prevImage) {
            WindowUtils.setWindowMask(this, getImage().getIcon());
            validate();
            this.repaint();
            prevImage = getImage();
        }
    }
}

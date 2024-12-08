package com.group_finity.mascot.x11;

import com.group_finity.mascot.image.NativeImage;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
import java.awt.image.ImageProducer;

public class X11NativeImage implements NativeImage{

    /**
     * Java Image object.
     */
    private final BufferedImage managedImage;

    private final Icon icon;

    public X11NativeImage(final BufferedImage image) {
        this.managedImage = image;
        this.icon = new ImageIcon(image);
    }

    public void flush() {
        this.getManagedImage().flush();
    }

    public Graphics getGraphics() {
        return this.getManagedImage().createGraphics();
    }

    public int getHeight() {
        return this.getManagedImage().getHeight();
    }

    public int getWidth() {
        return this.getManagedImage().getWidth();
    }

    public int getHeight(final ImageObserver observer) {
        return this.getManagedImage().getHeight(observer);
    }

    public Object getProperty(final String name, final ImageObserver observer) {
        return this.getManagedImage().getProperty(name, observer);
    }

    public ImageProducer getSource() {
        return this.getManagedImage().getSource();
    }

    public int getWidth(final ImageObserver observer) {
        return this.getManagedImage().getWidth(observer);
    }

    BufferedImage getManagedImage() {
        return this.managedImage;
    }

    public Icon getIcon() {
        return this.icon;
    }

}

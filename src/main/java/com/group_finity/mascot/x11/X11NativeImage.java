package com.group_finity.mascot.x11;

import com.group_finity.mascot.image.NativeImage;

import javax.swing.*;
import java.awt.image.BufferedImage;

public class X11NativeImage implements NativeImage {

    private final BufferedImage managedImage;

    private final Icon icon;

    public X11NativeImage(final BufferedImage image) {
        this.managedImage = image;
        this.icon = new ImageIcon(image);
    }

    BufferedImage getManagedImage() {
        return this.managedImage;
    }

    Icon getIcon() {
        return this.icon;
    }

}

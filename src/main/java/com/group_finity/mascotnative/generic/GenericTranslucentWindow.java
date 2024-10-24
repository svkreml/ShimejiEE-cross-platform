package com.group_finity.mascotnative.generic;

import com.group_finity.mascot.image.NativeImage;
import com.group_finity.mascotnative.shared.BaseTranslucentSwingWindow;
import com.sun.jna.platform.WindowUtils;

import javax.swing.*;
import java.awt.*;

class GenericTranslucentWindow extends BaseTranslucentSwingWindow<GenericNativeImage> {

    private static final Color CLEAR = new Color(0, 0, 0, 0);
    private NativeImage prevImage = null;

    @Override
    protected void setUp() {

        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(final Graphics g) {
                g.drawImage(getImage().getManagedImage(), 0, 0, null);
            }
        };

        panel.setBackground(CLEAR);
        setBackground(CLEAR);

        this.setContentPane(panel);

        setAlwaysOnTop(true);
    }

    @Override
    protected void addImpl(final Component comp, final Object constraints, final int index) {
        super.addImpl(comp, constraints, index);
        if (comp instanceof final JComponent jcomp) {
            jcomp.setOpaque(false);
        }
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

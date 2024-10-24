package com.group_finity.mascotnative.x11;

import com.group_finity.mascot.environment.Area;
import com.group_finity.mascot.environment.BaseNativeEnvironment;

import java.awt.*;
import java.util.ArrayList;
import java.util.logging.Logger;

public class X11Environment extends BaseNativeEnvironment {

    public static final Area workArea = new Area();
    private static final Logger log = Logger.getLogger(X11Environment.class.getName());
    private final X.Display display = new X.Display();

    private final ArrayList<Number> badStateList = new ArrayList<>();
    private final ArrayList<Number> badTypeList = new ArrayList<>();
    private final int minimizedValue;
    private final int dockValue;

    public Area activeIE = new Area();
    public String activeIETitle = null;
    X.Window activeWindow = null;

    X11Environment() {
        workArea.set(getWorkAreaRect());
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
    }


    public String getActiveIETitle() {
        return activeIETitle;
    }

    @Override
    public void moveActiveIE(final Point point) {
       // System.out.println("moving active IE" + point);

        activeWindow.move(activeWindow, point);
    }

    @Override
    public void restoreIE() {

    }


    @Override
    protected void updateIe(Area ieToUpdate) {
        update();
        ieToUpdate.setVisible(activeIE.isVisible());
        ieToUpdate.set(activeIE.toRectangle());
    }


    private void update() {

        try {
            final X.Window window = display.getActiveWindow();
            int desktop = window.getDesktop();
            int curDesktop = display.getActiveDesktopNumber();
            boolean badDesktop = ((desktop != curDesktop) && (desktop != -1));
            boolean badState = checkState(window.getState());
            boolean badType = checkType(window.getType());
            final String title = window.getTitle();
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

        } catch (X.X11Exception ignored) {
        }
    }

    private boolean checkState(int state) {
        return state == minimizedValue;
    }

    private Rectangle getWorkAreaRect() {
        return getScreen().toRectangle();
    }

    private boolean checkType(int type) {
        return badTypeList.contains(type);
    }
}

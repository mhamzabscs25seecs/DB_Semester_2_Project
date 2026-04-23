package ui;

import javax.swing.*;
import java.awt.*;

public class WindowState {
    private static Rectangle appBounds;
    private static int appExtendedState = JFrame.NORMAL;

    private WindowState() {
    }

    public static void apply(JFrame frame, Dimension defaultSize, Dimension minimumSize) {
        frame.setMinimumSize(minimumSize);
        if (appBounds == null) {
            frame.setSize(defaultSize);
            frame.setLocationRelativeTo(null);
        } else {
            frame.setBounds(appBounds);
        }

        frame.setExtendedState(appExtendedState);
    }

    public static void remember(JFrame frame) {
        int state = frame.getExtendedState();
        if ((state & JFrame.MAXIMIZED_BOTH) == 0 || appBounds == null) {
            appBounds = frame.getBounds();
        }
        appExtendedState = state;
    }
}

/*
 * Decompiled with CFR 0.152.
 */
package org.saig.jump.widgets.util;

import com.vividsolutions.jump.workbench.ui.GUIUtil;
import java.awt.Component;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import org.saig.jump.widgets.util.WrappingOptionPane;

public class DialogFactory {
    public static final int YES_OPTION = 0;
    public static final int NO_OPTION = 1;
    public static final int CANCEL_OPTION = 2;
    public static final int OK_OPTION = 0;
    public static final int CLOSED_OPTION = -1;
    public static final Object UNINITIALIZED_VALUE = JOptionPane.UNINITIALIZED_VALUE;

    public static void showInformationDialog(Component parent, String message, String title) {
        WrappingOptionPane optionPane = new WrappingOptionPane();
        optionPane.setMessage(message);
        optionPane.setMessageType(1);
        JDialog dialog = optionPane.createDialog(parent, title);
        dialog.setVisible(true);
    }

    public static void showErrorDialog(Component parent, String message, String title) {
        WrappingOptionPane optionPane = new WrappingOptionPane();
        optionPane.setMessage(message);
        optionPane.setMessageType(0);
        JDialog dialog = optionPane.createDialog(parent, title);
        dialog.setVisible(true);
    }

    public static void showWarningDialog(Component parent, String message, String title) {
        WrappingOptionPane optionPane = new WrappingOptionPane();
        optionPane.setMessage(message);
        optionPane.setMessageType(2);
        JDialog dialog = optionPane.createDialog(parent, title);
        dialog.setVisible(true);
    }

    public static int showYesNoDialog(Component parent, String message, String title) {
        return DialogFactory.showDialog(parent, message, title, 1, 0, null, null);
    }

    public static int showYesNoCancelWarningDialog(Component parent, String message, String title) {
        return DialogFactory.showDialog(parent, message, title, 2, 1, null, null);
    }

    public static int showYesNoWarningDialog(Component parent, String message, String title) {
        return DialogFactory.showDialog(parent, message, title, 2, 0, null, null);
    }

    public static Object showInputDialog(Component parent, String message, String title, String initialValue) {
        return DialogFactory.showInputDialog(parent, message, title, -1, -1, null, initialValue);
    }

    public static int showYesNoCancelDialog(Component parent, String message, String title) {
        return DialogFactory.showDialog(parent, message, title, 1, 1, null, null);
    }

    public static Object showSelectionDialog(Component parent, String message, String title, Object[] choices, Object defaultValue) {
        return JOptionPane.showInputDialog(parent, message, title, 3, null, choices, defaultValue);
    }

    private static int showDialog(Component parent, String message, String title, int messageType, int optionType, Object[] selectionValues, Object initialSelectionValue) {
        WrappingOptionPane optionPane = new WrappingOptionPane();
        optionPane.setMessage(message);
        optionPane.setMessageType(messageType);
        optionPane.setOptionType(optionType);
        if (selectionValues != null) {
            optionPane.setOptions(selectionValues);
            optionPane.setInitialSelectionValue(initialSelectionValue);
        }
        JDialog dialog = optionPane.createDialog(parent, title);
        if (parent == null) {
            GUIUtil.centreOnWindow(dialog);
        }
        dialog.setVisible(true);
        Object selectedValue = optionPane.getValue();
        Object[] options = optionPane.getOptions();
        if (selectedValue == null) {
            return -1;
        }
        if (options == null) {
            if (selectedValue instanceof Integer) {
                return (Integer)selectedValue;
            }
            return -1;
        }
        int counter = 0;
        int maxCounter = options.length;
        while (counter < maxCounter) {
            if (options[counter].equals(selectedValue)) {
                return counter;
            }
            ++counter;
        }
        return -1;
    }

    private static Object showInputDialog(Component parent, String message, String title, int messageType, int optionType, Object[] selectionValues, Object initialSelectionValue) {
        WrappingOptionPane optionPane = new WrappingOptionPane();
        optionPane.setMessage(message);
        optionPane.setMessageType(messageType);
        optionPane.setOptionType(optionType);
        optionPane.setWantsInput(true);
        optionPane.setOptions(selectionValues);
        optionPane.setInitialSelectionValue(initialSelectionValue);
        JDialog dialog = optionPane.createDialog(parent, title);
        dialog.setVisible(true);
        Object value = optionPane.getInputValue();
        if (value == UNINITIALIZED_VALUE) {
            return null;
        }
        return value;
    }

    public static Object showOptionDialog(Component parent, String message, String title, Object[] options, Object initialValue) {
        return WrappingOptionPane.showOptionDialog(parent, message, title, 1, 3, null, options, initialValue);
    }

    public static Object showWarningOptionDialog(Component parent, String message, String title, Object[] options, Object initialValue) {
        return WrappingOptionPane.showOptionDialog(parent, message, title, 1, 2, null, options, initialValue);
    }
}


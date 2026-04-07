package ar.com.catgis;

import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JRootPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.KeyStroke;
import java.awt.Component;
import java.awt.KeyboardFocusManager;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

public final class DialogKeyboardSupport {

    private static final String ACCEPT_ACTION_KEY = "catgis.dialog.accept";
    private static final String CANCEL_ACTION_KEY = "catgis.dialog.cancel";

    private DialogKeyboardSupport() {
    }

    public static void install(JDialog dialog, JButton defaultButton, Runnable cancelAction) {
        if (dialog == null) {
            return;
        }

        JRootPane rootPane = dialog.getRootPane();
        if (defaultButton != null) {
            rootPane.setDefaultButton(defaultButton);
        }

        InputMap inputMap = rootPane.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        ActionMap actionMap = rootPane.getActionMap();

        if (defaultButton != null) {
            inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), ACCEPT_ACTION_KEY);
            actionMap.put(ACCEPT_ACTION_KEY, new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    Component focusOwner = KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner();
                    if (focusOwner instanceof JButton && ((JButton) focusOwner).isEnabled()) {
                        ((JButton) focusOwner).doClick();
                        return;
                    }
                    if (shouldTriggerDefaultButton(focusOwner, defaultButton)) {
                        defaultButton.doClick();
                    }
                }
            });
        }

        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), CANCEL_ACTION_KEY);
        actionMap.put(CANCEL_ACTION_KEY, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (cancelAction != null) {
                    cancelAction.run();
                } else {
                    dialog.dispose();
                }
            }
        });
    }

    private static boolean shouldTriggerDefaultButton(Component focusOwner, JButton defaultButton) {
        if (focusOwner instanceof JTextArea || focusOwner instanceof JTable) {
            return false;
        }
        JComboBox<?> comboBox = findAncestorComboBox(focusOwner);
        return comboBox == null || !comboBox.isPopupVisible();
    }

    private static JComboBox<?> findAncestorComboBox(Component component) {
        Component current = component;
        while (current != null) {
            if (current instanceof JComboBox<?>) {
                return (JComboBox<?>) current;
            }
            current = current.getParent();
        }
        return null;
    }
}

/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.commons.lang.StringUtils
 *  org.apache.log4j.Logger
 *  org.jdesktop.swingx.auth.DefaultUserNameStore
 *  org.jdesktop.swingx.auth.LoginEvent
 *  org.jdesktop.swingx.auth.LoginListener
 *  org.jdesktop.swingx.auth.LoginService
 *  org.jdesktop.swingx.auth.PasswordStore
 *  org.jdesktop.swingx.auth.UserNameStore
 */
package es.kosmo.desktop.gui.dialogs;

import es.kosmo.desktop.gui.dialogs.LoginDialog;
import java.awt.CardLayout;
import java.awt.Cursor;
import java.awt.EventQueue;
import java.awt.KeyEventDispatcher;
import java.awt.KeyboardFocusManager;
import java.awt.Robot;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowFocusListener;
import java.awt.event.WindowListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.jdesktop.swingx.auth.DefaultUserNameStore;
import org.jdesktop.swingx.auth.LoginEvent;
import org.jdesktop.swingx.auth.LoginListener;
import org.jdesktop.swingx.auth.LoginService;
import org.jdesktop.swingx.auth.PasswordStore;
import org.jdesktop.swingx.auth.UserNameStore;
import org.saig.jump.lang.I18N;

public class LoginDialogController
implements ActionListener,
PropertyChangeListener,
LoginListener {
    private static final Logger LOGGER = Logger.getLogger(LoginDialogController.class);
    private Status status = Status.NOT_STARTED;
    private LoginService loginService;
    private PasswordStore passwordStore;
    private UserNameStore userNameStore;
    private SaveMode saveMode;
    private final CapsOnTest capsOnTest;
    private boolean caps;
    private boolean isTestingCaps;
    private final KeyEventDispatcher capsOnListener;
    private final CapsOnWinListener capsOnWinListener;
    private boolean capsLockSupport = true;
    protected LoginDialog dialog;

    public LoginDialogController(LoginDialog loginDialog, LoginService service, PasswordStore pStore, UserNameStore userStore) {
        this.dialog = loginDialog;
        this.capsOnTest = new CapsOnTest();
        this.capsOnListener = new KeyEventDispatcher(){

            @Override
            public boolean dispatchKeyEvent(KeyEvent e) {
                if (e.getID() != 401) {
                    return false;
                }
                if (e.getKeyCode() == 20) {
                    LoginDialogController.this.setCapsLock(!LoginDialogController.this.isCapsLockOn());
                }
                return false;
            }
        };
        this.capsOnWinListener = new CapsOnWinListener(this.capsOnTest);
        this.setLoginService(service);
        this.passwordStore = pStore == null ? new NullPasswordStore() : pStore;
        Object object = this.userNameStore = userStore == null ? new DefaultUserNameStore() : userStore;
        this.saveMode = pStore != null && userStore != null ? SaveMode.BOTH : (pStore != null ? SaveMode.PASSWORD : (userStore != null ? SaveMode.USER_NAME : SaveMode.NONE));
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource().equals(this.dialog.getOkCancelPanel())) {
            if (this.dialog.getOkCancelPanel().wasOKPressed()) {
                this.autenticateUser();
            } else {
                this.dialog.setVisible(false);
                this.dialog.dispose();
            }
        } else if ("Cancel".equals(e.getActionCommand())) {
            this.dialog.getWarningLabel().setText("<HTML><P><B>" + I18N.getString("es.kosmo.desktop.gui.dialogs.LoginDialogController.Authentication-process-cancelled-by-the-user") + "</B></P></HTML>");
            this.dialog.getWarningPanel().setCollapsed(false);
        } else {
            this.dialog.getMainPanel().repaint();
        }
    }

    protected boolean autenticateUser() {
        Cursor oldCursor = this.dialog.getCursor();
        boolean authenticated = false;
        try {
            try {
                this.dialog.setCursor(Cursor.getPredefinedCursor(3));
                String userName = this.getUserName();
                char[] password = this.getPassword();
                this.loginService.startAuthentication(userName, password, null);
            }
            catch (Exception ex) {
                LOGGER.warn((Object)I18N.getString("es.kosmo.desktop.gui.dialogs.LoginDialogController.Authentication-exception-while-logging-in"), (Throwable)ex);
                this.dialog.setCursor(oldCursor);
            }
        }
        finally {
            this.dialog.setCursor(oldCursor);
        }
        return authenticated;
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (this.dialog.getWarningPanel().equals(evt.getSource())) {
            this.dialog.pack();
        }
    }

    public SaveMode getSaveMode() {
        return this.saveMode;
    }

    public boolean isRememberPassword() {
        return this.dialog.getSaveCB().isVisible() && this.dialog.getSaveCB().isSelected();
    }

    public Status getStatus() {
        return this.status;
    }

    protected void setStatus(Status newStatus) {
        this.status = newStatus;
    }

    public void setLoginService(LoginService service) {
        LoginService newService;
        LoginService oldService = this.getLoginService();
        LoginService loginService = newService = service == null ? new NullLoginService() : service;
        if (!newService.equals(oldService)) {
            if (oldService != null) {
                oldService.removeLoginListener((LoginListener)this);
            }
            this.loginService = newService;
            this.loginService.addLoginListener((LoginListener)this);
        }
    }

    public void loginFailed(LoginEvent source) {
        assert (EventQueue.isDispatchThread());
        ((CardLayout)this.dialog.getCenterPanel().getLayout()).first(this.dialog.getCenterPanel());
        this.dialog.getWarningLabel().setText(String.valueOf(I18N.getString("es.kosmo.desktop.gui.dialogs.LoginDialogController.Invalid-authentication-parameters")) + "\n\n- " + I18N.getString("es.kosmo.desktop.gui.dialogs.LoginDialogController.Check-your-user-name-and-password") + "\n\n- " + I18N.getString("es.kosmo.desktop.gui.dialogs.LoginDialogController.Check-that-the-caps-block-is-not-pressed"));
        this.dialog.getWarningPanel().setVisible(true);
        ((JPanel)this.dialog.getContentPane()).revalidate();
        this.dialog.repaint();
        this.dialog.pack();
        this.setStatus(Status.FAILED);
    }

    public void loginStarted(LoginEvent source) {
        assert (EventQueue.isDispatchThread());
        ((CardLayout)this.dialog.getCenterPanel().getLayout()).last(this.dialog.getCenterPanel());
        this.dialog.getWarningPanel().setVisible(false);
        ((JPanel)this.dialog.getContentPane()).revalidate();
        this.dialog.repaint();
        this.setStatus(Status.IN_PROGRESS);
    }

    public void loginCanceled(LoginEvent source) {
        assert (EventQueue.isDispatchThread());
        ((CardLayout)this.dialog.getCenterPanel().getLayout()).first(this.dialog.getCenterPanel());
        this.dialog.getWarningPanel().setVisible(false);
        ((JPanel)this.dialog.getContentPane()).revalidate();
        this.dialog.repaint();
        this.setStatus(Status.CANCELLED);
    }

    public void loginSucceeded(LoginEvent source) {
        String userName = this.getUserName();
        if (!(this.getSaveMode() != SaveMode.USER_NAME && this.getSaveMode() != SaveMode.BOTH || userName == null || userName.trim().equals(""))) {
            this.userNameStore.addUserName(userName);
            this.userNameStore.saveUserNames();
        }
        if (this.dialog.getSaveCB().isSelected()) {
            this.savePassword();
        } else if (this.passwordStore != null) {
            this.passwordStore.removeUserPassword(userName);
        }
        ((CardLayout)this.dialog.getCenterPanel().getLayout()).first(this.dialog.getCenterPanel());
        this.dialog.getWarningPanel().setVisible(false);
        ((JPanel)this.dialog.getContentPane()).revalidate();
        this.dialog.repaint();
        this.dialog.pack();
        this.setStatus(Status.SUCCEEDED);
        this.dialog.setVisible(false);
    }

    protected String getUserName() {
        return this.dialog.getUserTextField().getUserName();
    }

    protected char[] getPassword() {
        return this.dialog.getPasswordPasswordField().getPassword();
    }

    protected void savePassword() {
        if (this.dialog.getSaveCB().isSelected() && (this.saveMode == SaveMode.BOTH || this.saveMode == SaveMode.PASSWORD) && this.passwordStore != null) {
            this.passwordStore.set(this.getUserName(), this.getLoginService().getServer(), this.getPassword());
        }
    }

    public LoginService getLoginService() {
        return this.loginService;
    }

    public PasswordStore getPasswordStore() {
        return this.passwordStore;
    }

    public UserNameStore getUserNameStore() {
        return this.userNameStore;
    }

    private void setCapsLock(boolean b) {
        this.caps = b;
        this.dialog.getCapsOn().setText(this.caps ? I18N.getString("es.kosmo.desktop.gui.dialogs.LoginDialogController.Caps-block-active") : " ");
    }

    public boolean isCapsLockOn() {
        return this.caps;
    }

    public boolean isCapsLockDetectionSupported() {
        return this.capsLockSupport;
    }

    public void removeNotify() {
        if (this.capsLockSupport) {
            KeyboardFocusManager.getCurrentKeyboardFocusManager().removeKeyEventDispatcher(this.capsOnListener);
        }
        this.dialog.removeWindowFocusListener(this.capsOnWinListener);
        this.dialog.removeWindowListener(this.capsOnWinListener);
    }

    public void addNotify() {
        KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(this.capsOnListener);
        this.dialog.addWindowFocusListener(this.capsOnWinListener);
        this.dialog.addWindowListener(this.capsOnWinListener);
    }

    public void updatePassword(String username) {
        String password = "";
        if (StringUtils.isNotEmpty((String)username)) {
            char[] pw = this.passwordStore.get(username, null);
            password = pw == null ? "" : new String(pw);
            this.dialog.getSaveCB().setSelected(this.userNameStore.containsUserName(username));
        }
        this.dialog.getPasswordPasswordField().setText(password);
    }

    private final class CapsOnTest {
        RemovableKeyEventDispatcher ked;

        private CapsOnTest() {
        }

        public void runTest() {
            boolean success = false;
            if (!success) {
                try {
                    KeyboardFocusManager kfm = KeyboardFocusManager.getCurrentKeyboardFocusManager();
                    if (this.ked != null) {
                        kfm.removeKeyEventDispatcher(this.ked);
                    }
                    this.ked = new RemovableKeyEventDispatcher(this);
                    kfm.addKeyEventDispatcher(this.ked);
                    Robot r = new Robot();
                    LoginDialogController.this.isTestingCaps = true;
                    r.keyPress(65);
                    r.keyRelease(65);
                    r.keyPress(8);
                    r.keyRelease(8);
                }
                catch (Exception e1) {
                    this.ked.uninstall();
                }
            }
        }

        public void clean() {
            if (this.ked != null) {
                this.ked.cleanOnBogusFocus();
            }
        }
    }

    private final class CapsOnWinListener
    extends WindowAdapter
    implements WindowFocusListener,
    WindowListener {
        private CapsOnTest cot;
        private long stamp;

        public CapsOnWinListener(CapsOnTest cot) {
            this.cot = cot;
        }

        @Override
        public void windowActivated(WindowEvent e) {
            this.cot.runTest();
            this.stamp = System.currentTimeMillis();
        }

        @Override
        public void windowGainedFocus(WindowEvent e) {
            if (this.stamp + 20L < System.currentTimeMillis()) {
                this.cot.runTest();
            }
        }

        @Override
        public void windowOpened(WindowEvent arg0) {
            this.cot.clean();
        }
    }

    private static final class NullLoginService
    extends LoginService {
        private NullLoginService() {
        }

        public boolean authenticate(String name, char[] password, String server) throws Exception {
            return true;
        }

        public boolean equals(Object obj) {
            return obj instanceof NullLoginService;
        }

        public int hashCode() {
            return 7;
        }
    }

    private static final class NullPasswordStore
    extends PasswordStore {
        private NullPasswordStore() {
        }

        public boolean set(String username, String server, char[] password) {
            return false;
        }

        public char[] get(String username, String server) {
            return new char[0];
        }

        public void removeUserPassword(String username) {
        }

        public boolean equals(Object obj) {
            return obj instanceof NullPasswordStore;
        }

        public int hashCode() {
            return 7;
        }
    }

    private class RemovableKeyEventDispatcher
    implements KeyEventDispatcher {
        private CapsOnTest cot;
        private boolean tested = false;
        private int retry = 0;

        public RemovableKeyEventDispatcher(CapsOnTest capsOnTest) {
            this.cot = capsOnTest;
        }

        @Override
        public boolean dispatchKeyEvent(KeyEvent e) {
            this.tested = true;
            if (e.getID() != 401) {
                return true;
            }
            if (LoginDialogController.this.isTestingCaps && e.getKeyCode() > 64 && e.getKeyCode() < 91) {
                LoginDialogController.this.setCapsLock(!e.isShiftDown() && Character.isUpperCase(e.getKeyChar()));
            }
            if (LoginDialogController.this.isTestingCaps && e.getKeyCode() == 8) {
                this.uninstall();
                this.retry = 0;
            }
            return true;
        }

        void uninstall() {
            LoginDialogController.this.isTestingCaps = false;
            KeyboardFocusManager.getCurrentKeyboardFocusManager().removeKeyEventDispatcher(this);
            if (this.cot.ked == this) {
                this.cot.ked = null;
            }
        }

        void cleanOnBogusFocus() {
            SwingUtilities.invokeLater(new Runnable(){

                @Override
                public void run() {
                    if (!RemovableKeyEventDispatcher.this.tested) {
                        RemovableKeyEventDispatcher.this.uninstall();
                        if (RemovableKeyEventDispatcher.this.retry < 3) {
                            ((RemovableKeyEventDispatcher)RemovableKeyEventDispatcher.this).LoginDialogController.this.dialog.toFront();
                            RemovableKeyEventDispatcher.this.cot.runTest();
                            RemovableKeyEventDispatcher removableKeyEventDispatcher = RemovableKeyEventDispatcher.this;
                            removableKeyEventDispatcher.retry = removableKeyEventDispatcher.retry + 1;
                        }
                    }
                }
            });
        }
    }

    public static enum SaveMode {
        NONE,
        USER_NAME,
        PASSWORD,
        BOTH;

    }

    public static enum Status {
        NOT_STARTED,
        IN_PROGRESS,
        FAILED,
        CANCELLED,
        SUCCEEDED;

    }
}


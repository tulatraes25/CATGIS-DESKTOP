/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.log4j.Logger
 *  org.divxdede.swing.busy.DefaultBusyModel
 *  org.divxdede.swing.busy.JBusyComponent
 *  org.divxdede.swing.busy.ui.BasicBusyLayerUI
 *  org.divxdede.swing.busy.ui.BusyLayerUI
 *  org.jdesktop.swingx.JXCollapsiblePane
 *  org.jdesktop.swingx.JXCollapsiblePane$Direction
 *  org.jdesktop.swingx.JXLabel
 *  org.jdesktop.swingx.auth.LoginService
 *  org.jdesktop.swingx.auth.PasswordStore
 *  org.jdesktop.swingx.auth.UserNameStore
 *  org.jdesktop.swingx.autocomplete.AutoCompleteDecorator
 *  org.jdesktop.swingx.painter.MattePainter
 *  org.jdesktop.swingx.painter.Painter
 *  org.jdesktop.swingx.painter.TextPainter
 *  org.jdesktop.swingx.painter.effects.AreaEffect
 *  org.jdesktop.swingx.painter.effects.ShadowPathEffect
 */
package es.kosmo.desktop.gui.dialogs;

import com.vividsolutions.jump.workbench.ui.OKCancelPanel;
import es.kosmo.desktop.gui.components.CurvesPanel;
import es.kosmo.desktop.gui.dialogs.LoginDialogController;
import es.kosmo.desktop.images.DesktopIconLoader;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridBagLayout;
import java.awt.LayoutManager;
import java.awt.Paint;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.beans.PropertyChangeListener;
import javax.swing.AbstractListModel;
import javax.swing.BorderFactory;
import javax.swing.ComboBoxModel;
import javax.swing.Icon;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.Timer;
import javax.swing.border.Border;
import javax.swing.border.LineBorder;
import org.apache.log4j.Logger;
import org.divxdede.swing.busy.DefaultBusyModel;
import org.divxdede.swing.busy.JBusyComponent;
import org.divxdede.swing.busy.ui.BasicBusyLayerUI;
import org.divxdede.swing.busy.ui.BusyLayerUI;
import org.jdesktop.swingx.JXCollapsiblePane;
import org.jdesktop.swingx.JXLabel;
import org.jdesktop.swingx.auth.LoginService;
import org.jdesktop.swingx.auth.PasswordStore;
import org.jdesktop.swingx.auth.UserNameStore;
import org.jdesktop.swingx.autocomplete.AutoCompleteDecorator;
import org.jdesktop.swingx.painter.MattePainter;
import org.jdesktop.swingx.painter.Painter;
import org.jdesktop.swingx.painter.TextPainter;
import org.jdesktop.swingx.painter.effects.AreaEffect;
import org.jdesktop.swingx.painter.effects.ShadowPathEffect;
import org.saig.core.gui.swing.sldeditor.util.FormUtils;
import org.saig.jump.lang.I18N;

public class LoginDialog
extends JDialog {
    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = Logger.getLogger(LoginDialog.class);
    private String titleText;
    private JLabel messageLabel;
    private JCheckBox saveCB;
    private JLabel capsOn;
    protected JPanel centerPanel;
    protected JXLabel titleLabel;
    protected CurvesPanel mainPanel;
    protected JXLabel userLabel;
    protected NameComponent userNamePanel;
    protected JXLabel passwordLabel;
    protected JPasswordField passwordPasswordField;
    protected JBusyComponent<JPanel> busyCenterPanel;
    protected JXCollapsiblePane warningPanel;
    protected JXLabel warningLabel;
    protected OKCancelPanel okCancelPanel;
    protected Timer animation;
    protected LoginDialogController controller;

    public LoginDialog(JFrame owner, boolean modal, String bannerText, LoginService service, PasswordStore passwordStore, UserNameStore userStore) {
        super((Frame)owner, modal);
        this.titleText = bannerText;
        this.setUndecorated(true);
        this.controller = new LoginDialogController(this, service, passwordStore, userStore);
        this.initialize();
        this.pack();
        this.setLocationRelativeTo(null);
        this.animation = new Timer(75, this.controller);
        this.animation.start();
    }

    private void initialize() {
        this.mainPanel = new CurvesPanel();
        int innerGap = 30;
        this.mainPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(Color.BLACK, 1), BorderFactory.createEmptyBorder(0, innerGap + 20, innerGap + 25, innerGap + 25)));
        this.mainPanel.setLayout(new BorderLayout());
        this.mainPanel.setOpaque(false);
        this.getContentPane().add("Center", (Component)((Object)this.mainPanel));
        this.mainPanel.add("North", (Component)this.getTitleLabel());
        this.mainPanel.add("Center", this.getCenterPanel());
        this.mainPanel.add("South", (Component)this.getWarningPanel());
        this.mainPanel.add("West", this.getIconLabel());
    }

    private JLabel getIconLabel() {
        JLabel iconLabel = new JLabel(DesktopIconLoader.icon("password.png"));
        iconLabel.setVerticalAlignment(1);
        return iconLabel;
    }

    public JXLabel getTitleLabel() {
        if (this.titleLabel == null) {
            this.titleLabel = new JXLabel(" ");
            ShadowPathEffect effect = new ShadowPathEffect();
            effect.setBrushColor(new Color(0, 0, 0, 100));
            TextPainter text = new TextPainter();
            text.setFont(new Font("Tahoma", 1, 20));
            text.setText(this.titleText);
            text.setAntialiasing(true);
            text.setFillPaint((Paint)Color.WHITE);
            text.setAreaEffects(new AreaEffect[]{effect});
            this.titleLabel.setBackgroundPainter((Painter)text);
            this.titleLabel.setBorder(BorderFactory.createEmptyBorder(40, 10, 20, 10));
        }
        return this.titleLabel;
    }

    public JPanel getCenterPanel() {
        if (this.centerPanel == null) {
            this.centerPanel = new JPanel(new CardLayout());
            this.centerPanel.setOpaque(false);
            this.centerPanel.add((Component)this.getUserPasswordPanel(), "0");
            this.centerPanel.add((Component)this.getProgressPanel(), "1");
        }
        return this.centerPanel;
    }

    public JPanel getUserPasswordPanel() {
        JPanel userPasswordPanel = new JPanel(new GridBagLayout());
        this.passwordLabel = new JXLabel(String.valueOf(I18N.getString("es.kosmo.desktop.gui.dialogs.LoginDialog.Password")) + ":");
        this.passwordPasswordField = new JPasswordField(15);
        this.userLabel = new JXLabel(String.valueOf(I18N.getString("es.kosmo.desktop.gui.dialogs.LoginDialog.User")) + ":");
        this.userNamePanel = this.controller.getSaveMode() == LoginDialogController.SaveMode.NONE ? new SimpleNamePanel() : new ComboNamePanel();
        this.saveCB = new JCheckBox(I18N.getString("es.kosmo.desktop.gui.dialogs.LoginDialog.Remember-password"));
        this.saveCB.setIconTextGap(10);
        this.saveCB.setSelected(false);
        this.saveCB.setVisible(this.controller.getSaveMode() == LoginDialogController.SaveMode.PASSWORD || this.controller.getSaveMode() == LoginDialogController.SaveMode.BOTH);
        this.saveCB.setOpaque(false);
        this.capsOn = new JLabel(" ");
        this.capsOn.setFont(this.capsOn.getFont().deriveFont(2));
        this.messageLabel = new JLabel(" ");
        this.messageLabel.setOpaque(false);
        this.messageLabel.setFont(this.messageLabel.getFont().deriveFont(1));
        FormUtils.addRowInGBL((JComponent)userPasswordPanel, 0, 0, (JLabel)this.userLabel, (JComponent)((Object)this.userNamePanel));
        FormUtils.addRowInGBL((JComponent)userPasswordPanel, 1, 0, (JLabel)this.passwordLabel, (JComponent)this.passwordPasswordField);
        FormUtils.addRowInGBL((JComponent)userPasswordPanel, 2, 0, (JComponent)this.saveCB, true, true);
        FormUtils.addRowInGBL((JComponent)userPasswordPanel, 3, 0, (JComponent)this.capsOn, true, true);
        FormUtils.addRowInGBL(userPasswordPanel, 4, 0, this.getOkCancelPanel());
        userPasswordPanel.setOpaque(false);
        return userPasswordPanel;
    }

    public JBusyComponent<JPanel> getProgressPanel() {
        JPanel progressPanel = new JPanel();
        this.busyCenterPanel = new JBusyComponent((JComponent)progressPanel);
        this.busyCenterPanel.getBusyModel().setCancellable(true);
        BasicBusyLayerUI ui = new BasicBusyLayerUI(400, 0.0f, Color.WHITE);
        this.busyCenterPanel.setBusyLayerUI((BusyLayerUI)ui);
        ((DefaultBusyModel)this.busyCenterPanel.getBusyModel()).setDescription(I18N.getString("es.kosmo.desktop.gui.dialogs.LoginDialog.Connecting-to-the-database"));
        this.busyCenterPanel.getBusyModel().addActionListener((ActionListener)this.controller);
        progressPanel.setOpaque(false);
        this.busyCenterPanel.setOpaque(false);
        return this.busyCenterPanel;
    }

    public OKCancelPanel getOkCancelPanel() {
        if (this.okCancelPanel == null) {
            this.okCancelPanel = new OKCancelPanel();
            this.okCancelPanel.setOpaque(false);
            this.okCancelPanel.setAcceptButtonText(I18N.getString("es.kosmo.desktop.gui.dialogs.LoginDialog.Connect"));
            this.okCancelPanel.addActionListener(this.controller);
        }
        return this.okCancelPanel;
    }

    public CurvesPanel getMainPanel() {
        return this.mainPanel;
    }

    @Override
    public void dispose() {
        super.dispose();
        this.animation.stop();
        this.animation = null;
        this.controller = null;
    }

    public JXCollapsiblePane getWarningPanel() {
        if (this.warningPanel == null) {
            this.warningPanel = new JXCollapsiblePane(JXCollapsiblePane.Direction.DOWN, (LayoutManager)new BorderLayout());
            Color errorColor = new Color(255, 215, 215, 127);
            this.warningLabel = new JXLabel();
            this.warningLabel.setOpaque(false);
            this.warningLabel.setIcon((Icon)DesktopIconLoader.icon("status_unknown.png"));
            this.warningLabel.setVerticalTextPosition(1);
            this.warningLabel.setLineWrap(true);
            this.warningLabel.setPaintBorderInsets(false);
            this.warningLabel.setBackgroundPainter((Painter)new MattePainter((Paint)errorColor, true));
            this.warningLabel.setMaxLineSpan(225);
            this.warningLabel.setBorder((Border)BorderFactory.createCompoundBorder(new LineBorder(Color.BLACK, 1, true), BorderFactory.createMatteBorder(5, 5, 5, 5, errorColor)));
            this.warningPanel.add((Component)this.warningLabel, (Object)"Center");
            this.warningPanel.setAnimated(false);
            this.warningPanel.setCollapsed(false);
            this.warningPanel.setVisible(false);
            this.warningPanel.addPropertyChangeListener("collapsed", (PropertyChangeListener)this.controller);
            this.warningPanel.setOpaque(false);
        }
        return this.warningPanel;
    }

    public JBusyComponent<JPanel> getBusyCenterPanel() {
        return this.busyCenterPanel;
    }

    public NameComponent getUserTextField() {
        return this.userNamePanel;
    }

    public JPasswordField getPasswordPasswordField() {
        return this.passwordPasswordField;
    }

    public JXLabel getWarningLabel() {
        return this.warningLabel;
    }

    public JCheckBox getSaveCB() {
        return this.saveCB;
    }

    public void setMessage(String message) {
        String old = this.messageLabel.getText();
        this.messageLabel.setText(message);
        this.firePropertyChange("message", old, this.messageLabel.getText());
    }

    public String getErrorMessage() {
        return this.warningLabel.getText();
    }

    public void setErrorMessage(String errorMessage) {
        String old = this.warningLabel.getText();
        this.warningLabel.setText(errorMessage);
        this.firePropertyChange("errorMessage", old, this.warningLabel.getText());
    }

    public JLabel getCapsOn() {
        return this.capsOn;
    }

    @Override
    public void removeNotify() {
        try {
            this.controller.removeNotify();
        }
        catch (Exception e) {
            LOGGER.error((Object)"", (Throwable)e);
        }
        super.removeNotify();
    }

    @Override
    public void addNotify() {
        try {
            this.controller.addNotify();
        }
        catch (Exception e) {
            LOGGER.error((Object)"", (Throwable)e);
        }
        super.addNotify();
    }

    public LoginDialogController.Status getStatus() {
        return this.controller != null ? this.controller.getStatus() : LoginDialogController.Status.CANCELLED;
    }

    private final class ComboNamePanel
    extends JComboBox
    implements NameComponent {
        private static final long serialVersionUID = 2511649075486103959L;

        public ComboNamePanel() {
            this.setModel(new NameComboBoxModel());
            this.setEditable(true);
            AutoCompleteDecorator.decorate((JComboBox)this);
            if (LoginDialog.this.controller.getPasswordStore() != null && LoginDialog.this.getPasswordPasswordField() != null) {
                final JTextField textfield = (JTextField)this.getEditor().getEditorComponent();
                textfield.addKeyListener(new KeyAdapter(){

                    @Override
                    public void keyReleased(KeyEvent e) {
                        ((ComboNamePanel)ComboNamePanel.this).LoginDialog.this.controller.updatePassword(textfield.getText());
                    }
                });
                super.addItemListener(new ItemListener(){

                    @Override
                    public void itemStateChanged(ItemEvent e) {
                        ((ComboNamePanel)ComboNamePanel.this).LoginDialog.this.controller.updatePassword((String)ComboNamePanel.this.getSelectedItem());
                    }
                });
            }
        }

        @Override
        public String getUserName() {
            Object item = this.getModel().getSelectedItem();
            return item == null ? null : item.toString();
        }

        @Override
        public void setUserName(String userName) {
            this.getModel().setSelectedItem(userName);
        }

        @Override
        public JComponent getComponent() {
            return this;
        }

        private final class NameComboBoxModel
        extends AbstractListModel
        implements ComboBoxModel {
            private static final long serialVersionUID = 7097674687536018633L;
            private Object selectedItem;

            private NameComboBoxModel() {
            }

            @Override
            public void setSelectedItem(Object anItem) {
                this.selectedItem = anItem;
                this.fireContentsChanged(this, -1, -1);
            }

            @Override
            public Object getSelectedItem() {
                return this.selectedItem;
            }

            @Override
            public Object getElementAt(int index) {
                return ((ComboNamePanel)ComboNamePanel.this).LoginDialog.this.controller.getUserNameStore().getUserNames()[index];
            }

            @Override
            public int getSize() {
                return ((ComboNamePanel)ComboNamePanel.this).LoginDialog.this.controller.getUserNameStore().getUserNames().length;
            }
        }
    }

    public static interface NameComponent {
        public String getUserName();

        public boolean isEnabled();

        public boolean isEditable();

        public void setEditable(boolean var1);

        public void setEnabled(boolean var1);

        public void setUserName(String var1);

        public JComponent getComponent();
    }

    private final class SimpleNamePanel
    extends JTextField
    implements NameComponent {
        private static final long serialVersionUID = 6513437813612641002L;

        public SimpleNamePanel() {
            super("", 15);
            if (LoginDialog.this.controller.getPasswordStore() != null && LoginDialog.this.getPasswordPasswordField() != null) {
                this.addKeyListener(new KeyAdapter(){

                    @Override
                    public void keyReleased(KeyEvent e) {
                        ((SimpleNamePanel)SimpleNamePanel.this).LoginDialog.this.controller.updatePassword(SimpleNamePanel.this.getText());
                    }
                });
            }
        }

        @Override
        public String getUserName() {
            return this.getText();
        }

        @Override
        public void setUserName(String userName) {
            this.setText(userName);
        }

        @Override
        public JComponent getComponent() {
            return this;
        }
    }
}


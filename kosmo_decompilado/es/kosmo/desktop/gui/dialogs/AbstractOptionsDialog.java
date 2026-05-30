/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.commons.lang.StringUtils
 *  org.jdesktop.swingx.JXLabel
 *  org.jdesktop.swingx.JXLabel$TextAlignment
 */
package es.kosmo.desktop.gui.dialogs;

import com.vividsolutions.jump.workbench.ui.OKCancelPanel;
import com.vividsolutions.jump.workbench.ui.images.IconLoader;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Frame;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import org.apache.commons.lang.StringUtils;
import org.jdesktop.swingx.JXLabel;
import org.saig.core.gui.swing.sldeditor.util.FormUtils;
import org.saig.jump.lang.I18N;
import org.saig.jump.widgets.util.DialogFactory;

public abstract class AbstractOptionsDialog
extends JDialog
implements ActionListener {
    private static final long serialVersionUID = 1L;
    protected JXLabel descriptionLabel;
    protected OKCancelPanel okCancelPanel;
    protected String descriptionText;
    protected String imagePath;
    public static final String APPLY_TO_LABEL = I18N.getString("es.kosmo.desktop.gui.dialogs.AbstractOptionsDialog.Apply-to");
    public static final String WHOLE_LAYER_LABEL = I18N.getString("es.kosmo.desktop.gui.dialogs.AbstractOptionsDialog.The-whole-layer");
    public static final String BASE_SELECTION_LABEL = I18N.getString("es.kosmo.desktop.gui.dialogs.AbstractOptionsDialog.Use-only-the-selected-features");

    protected AbstractOptionsDialog(JFrame parent, boolean modal) {
        super((Frame)parent, modal);
    }

    protected AbstractOptionsDialog(JDialog parent, boolean modal) {
        super((Dialog)parent, modal);
    }

    public AbstractOptionsDialog(JFrame parent, boolean modal, String toolName, String toolDescription, String toolImagePath) {
        super((Frame)parent, modal);
        this.descriptionText = toolDescription;
        this.imagePath = toolImagePath;
        this.setDialogTitle(toolName);
        this.initialize();
        this.pack();
        if (this.getTitle() != null) {
            FontMetrics fm = this.getFontMetrics(this.getFont());
            int width = fm.stringWidth(this.getTitle()) + 75;
            width = Math.max(width, this.getPreferredSize().width);
            this.setSize(new Dimension(width, this.getPreferredSize().height));
        }
    }

    public AbstractOptionsDialog(JDialog parent, boolean modal, String toolName, String toolDescription, String toolImagePath) {
        super((Dialog)parent, modal);
        this.descriptionText = toolDescription;
        this.imagePath = toolImagePath;
        this.setDialogTitle(toolName);
        this.initialize();
        this.pack();
        if (this.getTitle() != null) {
            FontMetrics fm = this.getFontMetrics(this.getFont());
            int width = fm.stringWidth(this.getTitle()) + 75;
            width = Math.max(width, this.getPreferredSize().width);
            this.setSize(new Dimension(width, this.getPreferredSize().height));
        }
    }

    protected void setDialogTitle(String toolName) {
        this.setTitle(I18N.getMessage("org.saig.jump.widgets.utils.AbstractBasicOptionsDialog.{0}-options", new Object[]{toolName}));
    }

    public void setDescriptionText(String description) {
        if (this.descriptionLabel != null) {
            this.descriptionLabel.setText(description);
        }
    }

    protected void initialize() {
        JPanel mainPanel = this.getMainPanel();
        this.setContentPane(mainPanel);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 5, 15));
        if (StringUtils.isNotEmpty((String)this.descriptionText)) {
            mainPanel.add((Component)this.getDescriptionPanel(), "North");
        }
        if (StringUtils.isNotEmpty((String)this.imagePath)) {
            mainPanel.add((Component)this.getImageLabel(), "West");
        }
        mainPanel.add((Component)this.getCenterPanel(), "Center");
        mainPanel.add((Component)this.getOkCancelPanel(), "South");
    }

    protected JPanel getMainPanel() {
        return new JPanel(new BorderLayout());
    }

    protected JPanel getDescriptionPanel() {
        JPanel descriptionPanel = new JPanel(new GridBagLayout());
        descriptionPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        this.descriptionLabel = new JXLabel(this.descriptionText);
        this.descriptionLabel.setLineWrap(true);
        this.descriptionLabel.setMaxLineSpan(500);
        this.descriptionLabel.setTextAlignment(JXLabel.TextAlignment.JUSTIFY);
        FormUtils.addRowInGBL((JComponent)descriptionPanel, 0, 0, (JComponent)this.descriptionLabel, true, true);
        return descriptionPanel;
    }

    protected JLabel getImageLabel() {
        ImageIcon icon = IconLoader.icon(this.imagePath);
        JLabel photoLabel = new JLabel(icon);
        Dimension dim = new Dimension(icon.getIconWidth(), icon.getIconHeight());
        photoLabel.setMinimumSize(dim);
        photoLabel.setPreferredSize(dim);
        return photoLabel;
    }

    protected abstract JPanel getCenterPanel();

    public OKCancelPanel getOkCancelPanel() {
        if (this.okCancelPanel == null) {
            this.okCancelPanel = new OKCancelPanel();
            this.okCancelPanel.setOpaque(false);
            this.okCancelPanel.addActionListener(this);
        }
        return this.okCancelPanel;
    }

    public boolean isInputValid() {
        return true;
    }

    public boolean wasOkPressed() {
        return this.okCancelPanel.wasOKPressed();
    }

    @Override
    public void setVisible(boolean visible) {
        if (visible) {
            this.okCancelPanel.setOKPressed(false);
        }
        super.setVisible(visible);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource().equals(this.okCancelPanel)) {
            if (this.okCancelPanel.wasOKPressed()) {
                if (this.isInputValid()) {
                    this.setVisible(false);
                } else {
                    this.okCancelPanel.setOKPressed(false);
                }
            } else {
                this.okCancelPanel.setOKPressed(false);
                this.setVisible(false);
            }
        }
    }

    public void warnUser(String message) {
        DialogFactory.showWarningDialog(this, message, this.getTitle());
    }
}


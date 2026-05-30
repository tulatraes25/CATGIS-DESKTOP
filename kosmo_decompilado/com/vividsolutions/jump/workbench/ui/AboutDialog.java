/*
 * Decompiled with CFR 0.152.
 */
package com.vividsolutions.jump.workbench.ui;

import com.vividsolutions.jump.workbench.JUMPWorkbench;
import com.vividsolutions.jump.workbench.WorkbenchContext;
import com.vividsolutions.jump.workbench.ui.ExtensionsAboutPanel;
import com.vividsolutions.jump.workbench.ui.GUIUtil;
import com.vividsolutions.jump.workbench.ui.SplashPanel;
import com.vividsolutions.jump.workbench.ui.WorkbenchFrame;
import com.vividsolutions.jump.workbench.ui.images.IconLoader;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.DecimalFormat;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import org.saig.core.gui.swing.sldeditor.util.FormUtils;
import org.saig.jump.lang.I18N;

public class AboutDialog
extends JDialog {
    private static final long serialVersionUID = 1L;
    BorderLayout borderLayout2 = new BorderLayout();
    JPanel buttonPanel = new JPanel();
    JButton okButton = new JButton();
    private JTabbedPane jTabbedPane1 = new JTabbedPane();
    private JPanel logoPanel = new JPanel();
    private JPanel infoPanel = new JPanel();
    private JPanel developmentInfoPanel = new JPanel();
    private JPanel systemInfoPanel = new JPanel();
    private JLabel javaVersionLabel;
    private JLabel javaVersionNumberLabel;
    private JLabel osLabel;
    private JLabel osVersionLabel;
    private JLabel totalMemoryLabel;
    private JLabel totalMemoryNumberLabel;
    private JLabel committedMemoryLabel;
    private JLabel committedMemoryNumberLabel;
    private JLabel freeMemoryLabel;
    private JLabel freeMemoryNumberLabel;
    private JButton btnGC = new JButton();
    private SplashPanel splashPanel;
    private JPanel projectsInformationPanel;
    private JLabel projectsLabel;
    private JPanel projectContributionsPanel;
    private ExtensionsAboutPanel extensionsAboutPanel = new ExtensionsAboutPanel();

    public static AboutDialog instance(WorkbenchContext context) {
        String INSTANCE_KEY = String.valueOf(AboutDialog.class.getName()) + " - INSTANCE";
        if (JUMPWorkbench.getBlackboard().get(INSTANCE_KEY) == null) {
            AboutDialog aboutDialog = new AboutDialog(context.getWorkbench().getFrame());
            JUMPWorkbench.getBlackboard().put(INSTANCE_KEY, aboutDialog);
            GUIUtil.centreOnWindow(aboutDialog);
        }
        return (AboutDialog)JUMPWorkbench.getBlackboard().get(INSTANCE_KEY);
    }

    private AboutDialog(WorkbenchFrame frame) {
        super(frame, I18N.getString("workbench.ui.AboutDialog.title"), true);
        this.extensionsAboutPanel.setPlugInManager(frame.getContext().getWorkbench().getPlugInManager());
        this.splashPanel = new SplashPanel(new ImageIcon(JUMPWorkbench.ABOUT_IMAGE), "3.0 RC1 (20130528)");
        try {
            this.jbInit();
            this.pack();
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    void jbInit() throws Exception {
        this.getContentPane().setLayout(this.borderLayout2);
        this.setResizable(false);
        this.okButton.setText(I18N.getString("workbench.ui.AboutDialog.ok"));
        this.okButton.addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent e) {
                AboutDialog.this.okButton_actionPerformed(e);
            }
        });
        this.infoPanel.setLayout(new GridBagLayout());
        this.developmentInfoPanel.setLayout(new GridBagLayout());
        this.developmentInfoPanel.setBorder(BorderFactory.createTitledBorder(I18N.getString("workbench.ui.AboutDialog.application-info")));
        this.systemInfoPanel.setLayout(new GridBagLayout());
        this.systemInfoPanel.setBorder(BorderFactory.createTitledBorder(I18N.getString("workbench.ui.AboutDialog.system-info")));
        Font boldFont = new JLabel().getFont().deriveFont(1);
        JLabel appLabel = new JLabel(String.valueOf(I18N.getString("workbench.ui.AboutDialog.application")) + " :");
        appLabel.setFont(boldFont);
        JLabel appNameLabel = new JLabel(I18N.getString("JUMPWorkbench.about-app-name"));
        appNameLabel.setForeground(Color.blue);
        JLabel appVersionLabel = new JLabel(String.valueOf(I18N.getString("workbench.ui.AboutDialog.version")) + " :");
        appVersionLabel.setFont(boldFont);
        JLabel appVersionNumberLabel = new JLabel("3.0 RC1 (20130528)");
        appVersionNumberLabel.setForeground(Color.blue);
        JLabel developmentTeamLabel = new JLabel(String.valueOf(I18N.getString("workbench.ui.AboutDialog.development-team")) + " :");
        developmentTeamLabel.setFont(boldFont);
        JLabel saigSLLabel = new JLabel("SAIG S.L. - http://www.saig.es");
        saigSLLabel.setForeground(Color.blue);
        this.javaVersionLabel = new JLabel(String.valueOf(I18N.getString("workbench.ui.AboutDialog.java-version")) + " :");
        this.javaVersionLabel.setFont(boldFont);
        this.javaVersionNumberLabel = new JLabel("x");
        this.osLabel = new JLabel(String.valueOf(I18N.getString("workbench.ui.AboutDialog.os")) + " :");
        this.osLabel.setFont(boldFont);
        this.osVersionLabel = new JLabel("x");
        this.totalMemoryLabel = new JLabel(String.valueOf(I18N.getString("workbench.ui.AboutDialog.total-memory")) + " :");
        this.totalMemoryLabel.setFont(boldFont);
        this.totalMemoryNumberLabel = new JLabel("x");
        this.committedMemoryLabel = new JLabel(String.valueOf(I18N.getString("workbench.ui.AboutDialog.committed-memory")) + " :");
        this.committedMemoryLabel.setFont(boldFont);
        this.committedMemoryNumberLabel = new JLabel("x");
        this.freeMemoryLabel = new JLabel(String.valueOf(I18N.getString("workbench.ui.AboutDialog.free-memory")) + " :");
        this.freeMemoryLabel.setFont(boldFont);
        this.freeMemoryNumberLabel = new JLabel("x");
        JPanel buttonGCPanel = new JPanel();
        buttonGCPanel.setLayout(new FlowLayout());
        this.btnGC.setText(I18N.getString("workbench.ui.AboutDialog.garbage-collect"));
        this.btnGC.addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent e) {
                AboutDialog.this.btnGC_actionPerformed(e);
            }
        });
        buttonGCPanel.add(this.btnGC);
        this.projectsInformationPanel = new JPanel(new GridBagLayout());
        this.projectsInformationPanel.setBorder(BorderFactory.createTitledBorder(I18N.getString("com.vividsolutions.jump.workbench.ui.AboutDialog.Projects-used-in-Kosmo")));
        this.projectsLabel = new JLabel("<HTML><P align=\"justify\">JTS, Geotools, JUMP, OpenJUMP, DeeJUMP, Pirol, Sigle, gvSIG, Log4J, Castor, JCalendar, Jeks, ErMapper, L2fprod, JUNG, JScience<BR>Apache Commons</P></HTML>");
        FormUtils.addRowInGBL(this.projectsInformationPanel, 0, 0, this.projectsLabel);
        FormUtils.addRowInGBL((JComponent)this.developmentInfoPanel, 0, 0, appLabel, (JComponent)appNameLabel);
        FormUtils.addRowInGBL((JComponent)this.developmentInfoPanel, 2, 0, appVersionLabel, (JComponent)appVersionNumberLabel);
        FormUtils.addRowInGBL((JComponent)this.developmentInfoPanel, 4, 0, developmentTeamLabel, (JComponent)saigSLLabel);
        FormUtils.addRowInGBL((JComponent)this.systemInfoPanel, 0, 0, this.javaVersionLabel, (JComponent)this.javaVersionNumberLabel);
        FormUtils.addRowInGBL((JComponent)this.systemInfoPanel, 2, 0, this.osLabel, (JComponent)this.osVersionLabel);
        FormUtils.addRowInGBL((JComponent)this.systemInfoPanel, 4, 0, this.totalMemoryLabel, (JComponent)this.totalMemoryNumberLabel);
        FormUtils.addRowInGBL((JComponent)this.systemInfoPanel, 6, 0, this.committedMemoryLabel, (JComponent)this.committedMemoryNumberLabel);
        FormUtils.addRowInGBL((JComponent)this.systemInfoPanel, 8, 0, this.freeMemoryLabel, (JComponent)this.freeMemoryNumberLabel);
        FormUtils.addRowInGBL(this.systemInfoPanel, 10, 0, buttonGCPanel);
        this.jTabbedPane1.add((Component)this.logoPanel, I18N.getString("workbench.ui.AboutDialog.about"));
        this.logoPanel.add((Component)this.splashPanel, "Center");
        this.getContentPane().add((Component)this.buttonPanel, "South");
        this.buttonPanel.add((Component)this.okButton, null);
        this.jTabbedPane1.setBounds(0, 0, 0, 0);
        this.jTabbedPane1.addTab(I18N.getString("workbench.ui.AboutDialog.info"), this.infoPanel);
        this.jTabbedPane1.addTab(I18N.getString("com.vividsolutions.jump.workbench.ui.AboutDialog.contributions"), this.getProjectContributionsPanel());
        FormUtils.addRowInGBL(this.infoPanel, 0, 0, this.developmentInfoPanel);
        FormUtils.addRowInGBL(this.infoPanel, 1, 0, this.systemInfoPanel);
        FormUtils.addRowInGBL(this.infoPanel, 2, 0, this.projectsInformationPanel);
        FormUtils.addFiller(this.infoPanel, 3, 0);
        this.getContentPane().add((Component)this.jTabbedPane1, "North");
    }

    private JPanel getProjectContributionsPanel() {
        if (this.projectContributionsPanel == null) {
            this.projectContributionsPanel = new JPanel(new GridBagLayout());
            JPanel acknowledgementsPanel = new JPanel(new GridBagLayout());
            acknowledgementsPanel.setBorder(BorderFactory.createTitledBorder(I18N.getString("com.vividsolutions.jump.workbench.ui.AboutDialog.acknowledgements")));
            JLabel contributionsLabel = new JLabel(String.valueOf(I18N.getString("com.vividsolutions.jump.workbench.ui.AboutDialog.we-would-like-to-spetially-thank-to-the-next-people-and-organizations-their-contributions-to-the-application-development")) + ":");
            ImageIcon euskeraIcon = IconLoader.icon("flags/es_eu.png");
            ImageIcon portugueseBrasilianIcon = IconLoader.icon("flags/br.png");
            ImageIcon usersIcon = IconLoader.icon("flags/users_flag.png");
            ImageIcon russianIcon = IconLoader.icon("flags/ru.png");
            ImageIcon germanIcon = IconLoader.icon("flags/de.png");
            ImageIcon italianIcon = IconLoader.icon("flags/it.png");
            ImageIcon czechIcon = IconLoader.icon("flags/cz.png");
            ImageIcon catalanIcon = IconLoader.icon("flags/cat.png");
            ImageIcon slovakIcon = IconLoader.icon("flags/sk.png");
            ImageIcon croatianIcon = IconLoader.icon("flags/hr.png");
            ImageIcon finnishIcon = IconLoader.icon("flags/fi.png");
            JLabel userLabel = new JLabel(I18N.getString("com.vividsolutions.jump.workbench.ui.AboutDialog.Kosmo-users"));
            JLabel dfaLabel = new JLabel(I18N.getString("com.vividsolutions.jump.workbench.ui.AboutDialog.Alava-County-Council"));
            JLabel territorioLivreLabel = new JLabel("<html><head></head><body>TerritorioLivre.net (<a href=\"http://www.territoriolivre.net\">http://www.territoriolivre.net</a>)</body></html>");
            JLabel russianLabel = new JLabel("<html><head></head><body>Sergey Smirnov, YugNIRO (<a href=\"http://yugniro.crimea.com\">http://yugniro.crimea.com</a>)</body></html>");
            JLabel germanLabel = new JLabel("Johannes Sommer");
            JLabel italianLabel = new JLabel("<html><head></head><body>Giuseppe Aruta (<a href=\"http://www.openjump.org\">http://www.openjump.org</a>)</body></html>");
            JLabel czechLabel = new JLabel("Jan Helebrant");
            JLabel catalanLabel = new JLabel("<html><head></head><body>Isimat Geogr\u00e1fica (<a href=\"http://www.isimatgeo.es\">http://www.isimatgeo.es</a>)</body></html>");
            JLabel catalanLabel2 = new JLabel("Servei de Sistemes d'Informaci\u00f3 Geogr\u00e0fica i Teledetecci\u00f3 de la Univ. de Girona (SIGTE)");
            JLabel catalanLabel3 = new JLabel("<html><head></head><body>(<a href=\"http://www.sigte.udg.edu\">http://www.sigte.udg.edu</a>)</body></html>");
            JLabel slovakLabel = new JLabel("<html><head></head><body>Ivan Mincik, Gista s.r.o. (<a href=\"http://gista.sk\">http://gista.sk</a>)</body></html>");
            JLabel croatianLabel = new JLabel("<html><body>IGEA d.o.o. (<a href=\"http://www.igea.hr\">http://www.igea.hr</a>)</body></html>");
            JLabel finnishLabel = new JLabel("Jukka Rakhonen");
            JLabel userAcknowledgementsLabel = new JLabel(I18N.getString("com.vividsolutions.jump.workbench.ui.AboutDialog.suggestions-error-reports-and-support"), usersIcon, 10);
            JLabel euskeraLanguage = new JLabel(I18N.getString("com.vividsolutions.jump.workbench.ui.AboutDialog.Euskera-language"), euskeraIcon, 10);
            JLabel portugueseBrasilianLanguage = new JLabel(I18N.getString("com.vividsolutions.jump.workbench.ui.AboutDialog.Brasilian-portuguese-language"), portugueseBrasilianIcon, 10);
            JLabel russianLanguage = new JLabel(I18N.getString("com.vividsolutions.jump.workbench.ui.AboutDialog.Russian-language"), russianIcon, 10);
            JLabel germanLanguage = new JLabel(I18N.getString("com.vividsolutions.jump.workbench.ui.AboutDialog.German-language"), germanIcon, 10);
            JLabel italianLanguage = new JLabel(I18N.getString("com.vividsolutions.jump.workbench.ui.AboutDialog.Italian-language"), italianIcon, 10);
            JLabel czechLanguage = new JLabel(I18N.getString("com.vividsolutions.jump.workbench.ui.AboutDialog.Czech-language"), czechIcon, 10);
            JLabel catalanLanguage = new JLabel(I18N.getString("com.vividsolutions.jump.workbench.ui.AboutDialog.Catalan-language"), catalanIcon, 10);
            JLabel slovakLanguage = new JLabel(I18N.getString("com.vividsolutions.jump.workbench.ui.AboutDialog.slovak-language"), slovakIcon, 10);
            JLabel croatianLanguage = new JLabel(I18N.getString("com.vividsolutions.jump.workbench.ui.AboutDialog.Croatian-language"), croatianIcon, 10);
            JLabel finnishLanguage = new JLabel(I18N.getString("com.vividsolutions.jump.workbench.ui.AboutDialog.Finnish-language"), finnishIcon, 10);
            int currentRow = 0;
            FormUtils.addRowInGBL((JComponent)acknowledgementsPanel, ++currentRow, 0, (JComponent)contributionsLabel, true, true);
            FormUtils.addRowInGBL(acknowledgementsPanel, currentRow, 0, new JLabel());
            FormUtils.addRowInGBL((JComponent)acknowledgementsPanel, ++currentRow, 0, (JComponent)userLabel, false, true);
            FormUtils.addRowInGBL((JComponent)acknowledgementsPanel, currentRow, 60, (JComponent)new JLabel(" - "), false, true);
            FormUtils.addRowInGBL((JComponent)acknowledgementsPanel, currentRow, 90, (JComponent)userAcknowledgementsLabel, true, true);
            FormUtils.addRowInGBL((JComponent)acknowledgementsPanel, ++currentRow, 0, (JComponent)dfaLabel, false, true);
            FormUtils.addRowInGBL((JComponent)acknowledgementsPanel, currentRow, 60, (JComponent)new JLabel(" - "), false, true);
            FormUtils.addRowInGBL((JComponent)acknowledgementsPanel, currentRow, 90, (JComponent)euskeraLanguage, true, true);
            FormUtils.addRowInGBL((JComponent)acknowledgementsPanel, ++currentRow, 0, (JComponent)territorioLivreLabel, false, true);
            FormUtils.addRowInGBL((JComponent)acknowledgementsPanel, currentRow, 60, (JComponent)new JLabel(" - "), false, true);
            FormUtils.addRowInGBL((JComponent)acknowledgementsPanel, currentRow, 90, (JComponent)portugueseBrasilianLanguage, true, true);
            FormUtils.addRowInGBL((JComponent)acknowledgementsPanel, ++currentRow, 0, (JComponent)russianLabel, false, true);
            FormUtils.addRowInGBL((JComponent)acknowledgementsPanel, currentRow, 60, (JComponent)new JLabel(" - "), false, true);
            FormUtils.addRowInGBL((JComponent)acknowledgementsPanel, currentRow, 90, (JComponent)russianLanguage, true, true);
            FormUtils.addRowInGBL((JComponent)acknowledgementsPanel, ++currentRow, 0, (JComponent)germanLabel, false, true);
            FormUtils.addRowInGBL((JComponent)acknowledgementsPanel, currentRow, 60, (JComponent)new JLabel(" - "), false, true);
            FormUtils.addRowInGBL((JComponent)acknowledgementsPanel, currentRow, 90, (JComponent)germanLanguage, true, true);
            FormUtils.addRowInGBL((JComponent)acknowledgementsPanel, ++currentRow, 0, (JComponent)italianLabel, false, true);
            FormUtils.addRowInGBL((JComponent)acknowledgementsPanel, currentRow, 60, (JComponent)new JLabel(" - "), false, true);
            FormUtils.addRowInGBL((JComponent)acknowledgementsPanel, currentRow, 90, (JComponent)italianLanguage, true, true);
            FormUtils.addRowInGBL((JComponent)acknowledgementsPanel, ++currentRow, 0, (JComponent)czechLabel, false, true);
            FormUtils.addRowInGBL((JComponent)acknowledgementsPanel, currentRow, 60, (JComponent)new JLabel(" - "), false, true);
            FormUtils.addRowInGBL((JComponent)acknowledgementsPanel, currentRow, 90, (JComponent)czechLanguage, true, true);
            FormUtils.addRowInGBL((JComponent)acknowledgementsPanel, ++currentRow, 0, (JComponent)catalanLabel, false, true);
            FormUtils.addRowInGBL((JComponent)acknowledgementsPanel, currentRow, 60, (JComponent)new JLabel(" - "), false, true);
            FormUtils.addRowInGBL((JComponent)acknowledgementsPanel, currentRow, 90, (JComponent)catalanLanguage, true, true);
            FormUtils.addRowInGBL((JComponent)acknowledgementsPanel, ++currentRow, 0, (JComponent)catalanLabel2, false, true);
            FormUtils.addRowInGBL((JComponent)acknowledgementsPanel, ++currentRow, 0, (JComponent)catalanLabel3, false, true);
            FormUtils.addRowInGBL((JComponent)acknowledgementsPanel, ++currentRow, 0, (JComponent)slovakLabel, false, true);
            FormUtils.addRowInGBL((JComponent)acknowledgementsPanel, currentRow, 60, (JComponent)new JLabel(" - "), false, true);
            FormUtils.addRowInGBL((JComponent)acknowledgementsPanel, currentRow, 90, (JComponent)slovakLanguage, true, true);
            FormUtils.addRowInGBL((JComponent)acknowledgementsPanel, ++currentRow, 0, (JComponent)croatianLabel, false, true);
            FormUtils.addRowInGBL((JComponent)acknowledgementsPanel, currentRow, 60, (JComponent)new JLabel(" - "), false, true);
            FormUtils.addRowInGBL((JComponent)acknowledgementsPanel, currentRow, 90, (JComponent)croatianLanguage, true, true);
            FormUtils.addRowInGBL((JComponent)acknowledgementsPanel, ++currentRow, 0, (JComponent)finnishLabel, false, true);
            FormUtils.addRowInGBL((JComponent)acknowledgementsPanel, currentRow, 60, (JComponent)new JLabel(" - "), false, true);
            FormUtils.addRowInGBL((JComponent)acknowledgementsPanel, currentRow, 90, (JComponent)finnishLanguage, true, true);
            FormUtils.addRowInGBL(this.projectContributionsPanel, 0, 0, acknowledgementsPanel);
            FormUtils.addFiller(this.projectContributionsPanel, 1, 0);
        }
        return this.projectContributionsPanel;
    }

    @Override
    public void setVisible(boolean b) {
        if (b) {
            DecimalFormat format = new DecimalFormat("###,###");
            this.javaVersionNumberLabel.setText(System.getProperty("java.version"));
            this.osVersionLabel.setText(String.valueOf(System.getProperty("os.name")) + " (" + System.getProperty("os.version") + ")");
            long totalMem = Runtime.getRuntime().totalMemory();
            long freeMem = Runtime.getRuntime().freeMemory();
            this.totalMemoryNumberLabel.setText(String.valueOf(format.format(totalMem)) + " bytes");
            this.committedMemoryNumberLabel.setText(String.valueOf(format.format(totalMem - freeMem)) + " bytes");
            this.freeMemoryNumberLabel.setText(String.valueOf(format.format(freeMem)) + " bytes");
        }
        super.setVisible(b);
    }

    void okButton_actionPerformed(ActionEvent e) {
        this.setVisible(false);
    }

    void btnGC_actionPerformed(ActionEvent e) {
        Runtime.getRuntime().gc();
        this.setVisible(true);
    }
}


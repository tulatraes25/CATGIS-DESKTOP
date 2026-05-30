/*
 * Decompiled with CFR 0.152.
 */
package org.saig.jump.widgets.print.util;

import com.vividsolutions.jump.workbench.ui.OKCancelPanel;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.print.PageFormat;
import java.awt.print.Paper;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.text.DecimalFormat;
import java.text.ParsePosition;
import javax.print.DocFlavor;
import javax.print.PrintService;
import javax.print.PrintServiceLookup;
import javax.print.attribute.standard.Media;
import javax.print.attribute.standard.MediaSize;
import javax.print.attribute.standard.MediaSizeName;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.ListCellRenderer;
import org.saig.core.gui.swing.sldeditor.util.FormUtils;
import org.saig.jump.lang.I18N;
import org.saig.jump.widgets.print.Conversion;
import org.saig.jump.widgets.print.PrintLayoutFrame;
import org.saig.jump.widgets.util.validating.GreaterOrEqualThanTextFieldValidator;

public class PageSetupDialog
extends JDialog {
    private static final long serialVersionUID = 1L;
    private static final String TITLE = I18N.getString("org.saig.jump.widgets.print.util.PageSetupDialog.page-configuration");
    private DecimalFormat formateador = new DecimalFormat("##0.##");
    private PageFormat pageFormat;
    private PageFormat newPageFormat;
    private PrinterJob job;
    private PrintService jobPrintService;
    private JPanel jContentPane;
    private JPanel printerPanel;
    private JPanel paperPanel;
    private JPanel orientationPanel;
    private JPanel marginPanel;
    private JPanel noPrinterPanel;
    private JComboBox printerComboBox;
    private JComboBox paperComboBox;
    private ButtonGroup orientationGroup = new ButtonGroup();
    private JRadioButton verticalOrientation;
    private JRadioButton horizontalOrientation;
    private JTextField leftMarginTextField;
    private JTextField rightMarginTextField;
    private JTextField upperMarginTextField;
    private JTextField bottomMarginTextField;
    private OKCancelPanel okCancelPanel;

    public PageSetupDialog(JFrame owner, boolean modal, PrinterJob printerJob) {
        super((Frame)owner, modal);
        this.job = printerJob;
        this.setTitle(TITLE);
        this.initialize();
    }

    private void initialize() {
        this.setContentPane(this.getJContentPane());
        this.initComponents();
        this.pack();
    }

    public void loadPageFormat(PageFormat pf) {
        this.pageFormat = pf;
        if (this.getPrinters().length > 0) {
            this.loadPageFormat();
        }
    }

    private void loadPageFormat() {
        int orientation = this.pageFormat.getOrientation();
        if (orientation == 1) {
            this.verticalOrientation.setSelected(true);
        } else {
            this.horizontalOrientation.setSelected(true);
        }
        double leftMargin = Conversion.seventyTwoInch_To_Cm(this.pageFormat.getPaper().getImageableX());
        double rightMargin = Conversion.seventyTwoInch_To_Cm(Math.max(0.0, this.pageFormat.getPaper().getWidth() - this.pageFormat.getPaper().getImageableWidth() - this.pageFormat.getPaper().getImageableX()));
        double topMargin = Conversion.seventyTwoInch_To_Cm(this.pageFormat.getPaper().getImageableY());
        double bottomMargin = Conversion.seventyTwoInch_To_Cm(Math.max(0.0, this.pageFormat.getPaper().getHeight() - this.pageFormat.getPaper().getImageableHeight() - this.pageFormat.getPaper().getImageableY()));
        PrintService[] printServices = this.getPrinters();
        this.jobPrintService = this.job.getPrintService();
        PrintService defaultPrintService = this.getDefaultPrinter();
        this.printerComboBox.removeAllItems();
        int i = 0;
        while (i < printServices.length) {
            this.printerComboBox.addItem(printServices[i]);
            ++i;
        }
        if (this.jobPrintService != null) {
            this.printerComboBox.setSelectedItem(this.jobPrintService);
        } else if (defaultPrintService != null) {
            this.printerComboBox.setSelectedItem(defaultPrintService);
        }
        float x = (float)this.pageFormat.getPaper().getWidth();
        float y = (float)this.pageFormat.getPaper().getHeight();
        MediaSizeName msn = MediaSize.findMedia(x / 72.0f, y / 72.0f, 25400);
        this.paperComboBox.setSelectedItem(msn);
        double[] minimumMargins = this.getPrinterMinimumMargins(this.jobPrintService);
        this.setMinimumMargins(minimumMargins[0], minimumMargins[1], minimumMargins[2], minimumMargins[3]);
        this.leftMarginTextField.setText(this.formateador.format(Math.max(leftMargin, minimumMargins[0])));
        this.rightMarginTextField.setText(this.formateador.format(Math.max(rightMargin, minimumMargins[1])));
        this.upperMarginTextField.setText(this.formateador.format(Math.max(topMargin, minimumMargins[2])));
        this.bottomMarginTextField.setText(this.formateador.format(Math.max(bottomMargin, minimumMargins[3])));
    }

    private void setMinimumMargins(double left, double right, double top, double bottom) {
        ((GreaterOrEqualThanTextFieldValidator)this.getLeftMarginTextField().getInputVerifier()).setMinimumValue(left);
        ((GreaterOrEqualThanTextFieldValidator)this.getRightMarginTextField().getInputVerifier()).setMinimumValue(right);
        ((GreaterOrEqualThanTextFieldValidator)this.getUpperMarginTextField().getInputVerifier()).setMinimumValue(top);
        ((GreaterOrEqualThanTextFieldValidator)this.getBottomMarginTextField().getInputVerifier()).setMinimumValue(bottom);
    }

    private double[] getPrinterMinimumMargins(PrintService service) {
        double[] margins = new double[4];
        PrinterJob job = PrinterJob.getPrinterJob();
        try {
            job.setPrintService(service);
        }
        catch (PrinterException printerException) {
            // empty catch block
        }
        PageFormat pf = job.defaultPage();
        Paper paper = pf.getPaper();
        paper.setImageableArea(0.0, 0.0, paper.getWidth(), paper.getHeight());
        pf.setPaper(paper);
        pf = job.validatePage(pf);
        margins[0] = Conversion.seventyTwoInch_To_Cm(pf.getPaper().getImageableX());
        margins[1] = Conversion.seventyTwoInch_To_Cm(Math.max(0.0, pf.getPaper().getWidth() - pf.getPaper().getImageableWidth() - pf.getPaper().getImageableX()));
        margins[2] = Conversion.seventyTwoInch_To_Cm(pf.getPaper().getImageableY());
        margins[3] = Conversion.seventyTwoInch_To_Cm(Math.max(0.0, pf.getPaper().getHeight() - pf.getPaper().getImageableHeight() - pf.getPaper().getImageableY()));
        return margins;
    }

    private void initComponents() {
        if (this.getPrinters().length == 0) {
            FormUtils.addRowInGBL((JComponent)this.getJContentPane(), 1, 0, (JComponent)this.getNoPrinterPanel(), true, false);
            FormUtils.addRowInGBL((JComponent)this.getJContentPane(), 2, 0, (JComponent)this.getOkCancelPanel(), true, false);
        } else {
            FormUtils.addRowInGBL((JComponent)this.getJContentPane(), 1, 0, (JComponent)this.getPrinterPanel(), true, false);
            FormUtils.addRowInGBL((JComponent)this.getJContentPane(), 2, 0, (JComponent)this.getPaperPanel(), true, false);
            FormUtils.addRowInGBL((JComponent)this.getJContentPane(), 3, 0, (JComponent)this.getOrientationPanel(), false, false);
            FormUtils.addRowInGBL((JComponent)this.getJContentPane(), 3, 2, (JComponent)this.getMarginPanel(), true, false);
            FormUtils.addRowInGBL((JComponent)this.getJContentPane(), 5, 0, (JComponent)this.getOkCancelPanel(), true, false);
        }
    }

    private JPanel getJContentPane() {
        if (this.jContentPane == null) {
            this.jContentPane = new JPanel();
            this.jContentPane.setLayout(new GridBagLayout());
        }
        return this.jContentPane;
    }

    private void okCancelPanel_actionPerformed(ActionEvent e) {
        if (this.okCancelPanel.wasOKPressed()) {
            if (!this.isInputValid()) {
                return;
            }
            if (this.getPrinters().length == 0) {
                this.newPageFormat = this.pageFormat;
            } else {
                int orientation = 1;
                if (this.horizontalOrientation.isSelected()) {
                    orientation = 0;
                }
                double leftMargin = 0.0;
                double rightMargin = 0.0;
                double topMargin = 0.0;
                double bottomMargin = 0.0;
                try {
                    PrintLayoutFrame.LEFT_MARGIN = this.getMarginValue(this.leftMarginTextField.getText());
                    PrintLayoutFrame.RIGHT_MARGIN = this.getMarginValue(this.rightMarginTextField.getText());
                    PrintLayoutFrame.TOP_MARGIN = this.getMarginValue(this.upperMarginTextField.getText());
                    PrintLayoutFrame.BOTTOM_MARGIN = this.getMarginValue(this.bottomMarginTextField.getText());
                    leftMargin = Conversion.Cm_To_seventyTwoInch(this.getMarginValue(this.leftMarginTextField.getText()));
                    rightMargin = Conversion.Cm_To_seventyTwoInch(this.getMarginValue(this.rightMarginTextField.getText()));
                    topMargin = Conversion.Cm_To_seventyTwoInch(this.getMarginValue(this.upperMarginTextField.getText()));
                    bottomMargin = Conversion.Cm_To_seventyTwoInch(this.getMarginValue(this.bottomMarginTextField.getText()));
                }
                catch (NumberFormatException nfe) {
                    System.out.println("NUMBER FORMAT EXCEPTION");
                }
                MediaSizeName msn = (MediaSizeName)this.paperComboBox.getSelectedItem();
                MediaSize ms = MediaSize.getMediaSizeForName(msn);
                this.newPageFormat = new PageFormat();
                Paper paper = new Paper();
                float[] f = ms.getSize(25400);
                double x = f[0] * 72.0f;
                double y = f[1] * 72.0f;
                paper.setSize(x, y);
                paper.setImageableArea(leftMargin, topMargin, x - leftMargin - rightMargin, y - topMargin - bottomMargin);
                this.newPageFormat.setOrientation(orientation);
                this.newPageFormat.setPaper(paper);
                PrintService ps = (PrintService)this.printerComboBox.getSelectedItem();
                if (ps != this.jobPrintService) {
                    try {
                        this.job.setPrintService(ps);
                    }
                    catch (PrinterException pe) {
                        pe.printStackTrace();
                    }
                }
            }
        }
        this.setVisible(false);
    }

    private boolean isInputValid() {
        boolean isValid = true;
        if (this.getPrinters().length > 0) {
            isValid = isValid && this.leftMarginTextField.getInputVerifier().verify(this.leftMarginTextField) && this.rightMarginTextField.getInputVerifier().verify(this.rightMarginTextField) && this.upperMarginTextField.getInputVerifier().verify(this.upperMarginTextField) && this.bottomMarginTextField.getInputVerifier().verify(this.bottomMarginTextField);
        }
        return isValid;
    }

    public JPanel getPaperPanel() {
        if (this.paperPanel == null) {
            this.paperPanel = new JPanel();
            this.paperPanel.setLayout(new GridBagLayout());
            this.paperPanel.setBorder(BorderFactory.createTitledBorder(I18N.getString("org.saig.jump.widgets.print.util.PageSetupDialog.paper-format")));
            this.paperPanel.setMinimumSize(new Dimension(400, 55));
            this.paperPanel.setPreferredSize(new Dimension(400, 55));
            this.paperComboBox = new JComboBox();
            this.paperComboBox.setMinimumSize(new Dimension(300, 20));
            this.paperComboBox.setPreferredSize(new Dimension(300, 20));
            this.paperComboBox.addActionListener(new ActionListener(){

                @Override
                public void actionPerformed(ActionEvent arg0) {
                }
            });
            FormUtils.addRowInGBL(this.paperPanel, 1, 0, this.paperComboBox);
        }
        return this.paperPanel;
    }

    public JPanel getMarginPanel() {
        if (this.marginPanel == null) {
            this.marginPanel = new JPanel();
            this.marginPanel.setLayout(new GridBagLayout());
            this.marginPanel.setBorder(BorderFactory.createTitledBorder(I18N.getString("org.saig.jump.widgets.print.util.PageSetupDialog.borders-cm")));
            this.marginPanel.setMinimumSize(new Dimension(200, 100));
            this.marginPanel.setPreferredSize(new Dimension(200, 100));
            JLabel leftLabel = new JLabel(I18N.getString("org.saig.jump.widgets.print.util.PageSetupDialog.left"));
            JLabel rightLabel = new JLabel(I18N.getString("org.saig.jump.widgets.print.util.PageSetupDialog.right"));
            JLabel upperLabel = new JLabel(I18N.getString("org.saig.jump.widgets.print.util.PageSetupDialog.top"));
            JLabel bottomLabel = new JLabel(I18N.getString("org.saig.jump.widgets.print.util.PageSetupDialog.bottom"));
            FormUtils.addRowInGBL((JComponent)this.marginPanel, 1, 0, leftLabel, (JComponent)this.getLeftMarginTextField());
            FormUtils.addRowInGBL((JComponent)this.marginPanel, 1, 2, rightLabel, (JComponent)this.getRightMarginTextField());
            FormUtils.addRowInGBL((JComponent)this.marginPanel, 2, 0, upperLabel, (JComponent)this.getUpperMarginTextField());
            FormUtils.addRowInGBL((JComponent)this.marginPanel, 2, 2, bottomLabel, (JComponent)this.getBottomMarginTextField());
        }
        return this.marginPanel;
    }

    public OKCancelPanel getOkCancelPanel() {
        if (this.okCancelPanel == null) {
            this.okCancelPanel = new OKCancelPanel();
            this.okCancelPanel.addActionListener(new ActionListener(){

                @Override
                public void actionPerformed(ActionEvent e) {
                    if (PageSetupDialog.this.isInputValid()) {
                        PageSetupDialog.this.okCancelPanel_actionPerformed(e);
                    }
                }
            });
        }
        return this.okCancelPanel;
    }

    public JPanel getOrientationPanel() {
        if (this.orientationPanel == null) {
            this.orientationPanel = new JPanel();
            this.orientationPanel.setLayout(new GridBagLayout());
            this.orientationPanel.setBorder(BorderFactory.createTitledBorder(I18N.getString("org.saig.jump.widgets.print.util.PageSetupDialog.orientation")));
            this.orientationPanel.setMinimumSize(new Dimension(100, 100));
            this.orientationPanel.setPreferredSize(new Dimension(100, 100));
            this.verticalOrientation = new JRadioButton(I18N.getString("org.saig.jump.widgets.print.util.PageSetupDialog.vertical"));
            this.horizontalOrientation = new JRadioButton(I18N.getString("org.saig.jump.widgets.print.util.PageSetupDialog.horizontal"));
            this.horizontalOrientation.setSelected(true);
            this.orientationGroup.add(this.verticalOrientation);
            this.orientationGroup.add(this.horizontalOrientation);
            FormUtils.addRowInGBL(this.orientationPanel, 1, 0, this.verticalOrientation);
            FormUtils.addRowInGBL(this.orientationPanel, 2, 0, this.horizontalOrientation);
        }
        return this.orientationPanel;
    }

    public JPanel getPrinterPanel() {
        if (this.printerPanel == null) {
            this.printerPanel = new JPanel();
            this.printerPanel.setLayout(new GridBagLayout());
            this.printerPanel.setBorder(BorderFactory.createTitledBorder(I18N.getString("org.saig.jump.widgets.print.util.PageSetupDialog.printer")));
            this.printerPanel.setMinimumSize(new Dimension(400, 55));
            this.printerPanel.setPreferredSize(new Dimension(400, 55));
            this.printerComboBox = new JComboBox();
            this.printerComboBox.setRenderer(new PrinterComboboxCellRenderer());
            this.printerComboBox.setMinimumSize(new Dimension(300, 20));
            this.printerComboBox.setPreferredSize(new Dimension(300, 20));
            this.printerComboBox.addActionListener(new ActionListener(){

                @Override
                public void actionPerformed(ActionEvent e) {
                    JComboBox cb = (JComboBox)e.getSource();
                    PrintService printService = (PrintService)cb.getSelectedItem();
                    if (printService == null) {
                        return;
                    }
                    Media[] med = (Media[])printService.getSupportedAttributeValues(Media.class, null, null);
                    Object selectedMedia = PageSetupDialog.this.paperComboBox.getSelectedItem();
                    PageSetupDialog.this.paperComboBox.removeAllItems();
                    if (med != null) {
                        int i = 0;
                        while (i < med.length) {
                            if (med[i] instanceof MediaSizeName) {
                                MediaSizeName msn = (MediaSizeName)med[i];
                                PageSetupDialog.this.paperComboBox.addItem(msn);
                            }
                            ++i;
                        }
                    }
                    if (selectedMedia != null) {
                        PageSetupDialog.this.paperComboBox.setSelectedItem(selectedMedia);
                    }
                    double[] minimumMargins = PageSetupDialog.this.getPrinterMinimumMargins(printService);
                    PageSetupDialog.this.setMinimumMargins(minimumMargins[0], minimumMargins[1], minimumMargins[2], minimumMargins[3]);
                    double leftMargin = PageSetupDialog.this.getMarginValue(PageSetupDialog.this.leftMarginTextField.getText());
                    double rightMargin = PageSetupDialog.this.getMarginValue(PageSetupDialog.this.rightMarginTextField.getText());
                    double topMargin = PageSetupDialog.this.getMarginValue(PageSetupDialog.this.upperMarginTextField.getText());
                    double bottomMargin = PageSetupDialog.this.getMarginValue(PageSetupDialog.this.bottomMarginTextField.getText());
                    PageSetupDialog.this.leftMarginTextField.setText(PageSetupDialog.this.formateador.format(Math.max(leftMargin, minimumMargins[0])));
                    PageSetupDialog.this.rightMarginTextField.setText(PageSetupDialog.this.formateador.format(Math.max(rightMargin, minimumMargins[1])));
                    PageSetupDialog.this.upperMarginTextField.setText(PageSetupDialog.this.formateador.format(Math.max(topMargin, minimumMargins[2])));
                    PageSetupDialog.this.bottomMarginTextField.setText(PageSetupDialog.this.formateador.format(Math.max(bottomMargin, minimumMargins[3])));
                }
            });
            FormUtils.addRowInGBL(this.printerPanel, 1, 0, this.printerComboBox);
        }
        return this.printerPanel;
    }

    private double getMarginValue(String value) {
        double solucion = 0.0;
        Number number = null;
        try {
            number = this.formateador.parse(value.trim().replace('.', ','), new ParsePosition(0));
        }
        catch (Exception exception) {
            // empty catch block
        }
        if (number != null) {
            solucion = number.doubleValue();
        }
        return solucion;
    }

    private JTextField getLeftMarginTextField() {
        if (this.leftMarginTextField == null) {
            this.leftMarginTextField = new JTextField();
            this.leftMarginTextField.setInputVerifier(new GreaterOrEqualThanTextFieldValidator((JDialog)this, (JComponent)this.leftMarginTextField, 0.0));
        }
        return this.leftMarginTextField;
    }

    private JTextField getRightMarginTextField() {
        if (this.rightMarginTextField == null) {
            this.rightMarginTextField = new JTextField();
            this.rightMarginTextField.setInputVerifier(new GreaterOrEqualThanTextFieldValidator((JDialog)this, (JComponent)this.leftMarginTextField, 0.0));
        }
        return this.rightMarginTextField;
    }

    private JTextField getUpperMarginTextField() {
        if (this.upperMarginTextField == null) {
            this.upperMarginTextField = new JTextField();
            this.upperMarginTextField.setInputVerifier(new GreaterOrEqualThanTextFieldValidator((JDialog)this, (JComponent)this.leftMarginTextField, 0.0));
        }
        return this.upperMarginTextField;
    }

    private JTextField getBottomMarginTextField() {
        if (this.bottomMarginTextField == null) {
            this.bottomMarginTextField = new JTextField();
            this.bottomMarginTextField.setInputVerifier(new GreaterOrEqualThanTextFieldValidator((JDialog)this, (JComponent)this.leftMarginTextField, 0.0));
        }
        return this.bottomMarginTextField;
    }

    private PrintService[] getPrinters() {
        DocFlavor.SERVICE_FORMATTED flavor = DocFlavor.SERVICE_FORMATTED.PRINTABLE;
        return PrintServiceLookup.lookupPrintServices(flavor, null);
    }

    private PrintService getDefaultPrinter() {
        return PrintServiceLookup.lookupDefaultPrintService();
    }

    public PageFormat getNewPageFormat() {
        return this.newPageFormat;
    }

    public boolean wasOKPressed() {
        return this.okCancelPanel.wasOKPressed();
    }

    public JPanel getNoPrinterPanel() {
        if (this.noPrinterPanel == null) {
            this.noPrinterPanel = new JPanel(new GridBagLayout());
            this.noPrinterPanel.setBorder(BorderFactory.createTitledBorder(I18N.getString("org.saig.jump.widgets.print.util.PageSetupDialog.There-is-no-printer-installed")));
            JLabel noPrinterLabel = new JLabel(I18N.getString("org.saig.jump.widgets.print.util.PageSetupDialog.There-is-no-default-printer-stablished-It-is-not-posible-to-setup-the-page-properly"));
            FormUtils.addRowInGBL(this.noPrinterPanel, 0, 0, noPrinterLabel);
            FormUtils.addFiller(this.noPrinterPanel, 1, 0);
        }
        return this.noPrinterPanel;
    }

    private class PrinterComboboxCellRenderer
    extends JLabel
    implements ListCellRenderer {
        private static final long serialVersionUID = 1L;

        private PrinterComboboxCellRenderer() {
        }

        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            PrintService printer = (PrintService)value;
            this.setText(printer.getName());
            if (isSelected) {
                this.setBackground(list.getSelectionBackground());
                this.setForeground(list.getSelectionForeground());
            } else {
                this.setBackground(list.getBackground());
                this.setForeground(list.getForeground());
            }
            this.setEnabled(list.isEnabled());
            this.setFont(list.getFont());
            this.setOpaque(true);
            return this;
        }
    }
}


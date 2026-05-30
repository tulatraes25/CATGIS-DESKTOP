/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.log4j.Logger
 */
package org.saig.jump.widgets.print;

import java.awt.Dimension;
import java.awt.Insets;
import java.net.URL;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import org.apache.log4j.Logger;
import org.saig.jump.lang.I18N;
import org.saig.jump.widgets.print.PrintLayoutFrame;
import org.saig.jump.widgets.print.actions.AddGeometry;
import org.saig.jump.widgets.print.actions.AddImage;
import org.saig.jump.widgets.print.actions.AddLegend;
import org.saig.jump.widgets.print.actions.AddMap;
import org.saig.jump.widgets.print.actions.AddNorth;
import org.saig.jump.widgets.print.actions.AddScale;
import org.saig.jump.widgets.print.actions.AddText;
import org.saig.jump.widgets.print.actions.BatchPrintingAction;
import org.saig.jump.widgets.print.actions.Delete;
import org.saig.jump.widgets.print.actions.FullPage;
import org.saig.jump.widgets.print.actions.OpenLayout;
import org.saig.jump.widgets.print.actions.PageSetup;
import org.saig.jump.widgets.print.actions.Print;
import org.saig.jump.widgets.print.actions.PrintAction;
import org.saig.jump.widgets.print.actions.PrintOptions;
import org.saig.jump.widgets.print.actions.SaveAsLayout;
import org.saig.jump.widgets.print.actions.SaveLayout;
import org.saig.jump.widgets.print.actions.ShowElementsDialog;
import org.saig.jump.widgets.print.actions.WidthPage;
import org.saig.jump.widgets.print.actions.Zoom;
import org.saig.jump.widgets.print.images.PrintIconLoader;

public class PrintLayoutToolBar
extends JToolBar {
    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = Logger.getLogger((String)"org.saig.jump.widgets.print.PrintLayoutToolBar");
    private ToolBarButton deleteButton;
    private JComboBox jComboBoxZoom = new JComboBox<String>(new String[]{"50%", "75%", "100%", "125%", "150%", "175%", "200%", "300%", "400%"});
    private ButtonGroup addButtonGroup = new ButtonGroup();
    private PrintAction openLayout;
    private PrintAction saveLayout;
    private PrintAction saveAsLayout;
    private PrintAction pageSetup;
    private PrintAction printOptions;
    private PrintAction print;
    private PrintAction batchPrint;
    private PrintAction fullPage;
    private PrintAction widthPage;
    private PrintAction zoom;
    private PrintAction addMap;
    private PrintAction addLegend;
    private PrintAction addtext;
    private PrintAction addNorth;
    private PrintAction addImage;
    private PrintAction addScale;
    private PrintAction addGeometry;
    private PrintAction delete;
    private PrintAction showElementsDialog;

    public PrintLayoutToolBar(PrintLayoutFrame parent) {
        ToolBarButton button = null;
        ToolBarToggleButton toggleButton = null;
        button = new ToolBarButton("open.gif", I18N.getString("org.saig.jump.widgets.print.PrintLayoutToolBar.open"));
        this.openLayout = new OpenLayout(parent);
        button.addActionListener(this.openLayout);
        this.add(button);
        button = new ToolBarButton("save.gif", I18N.getString("org.saig.jump.widgets.print.PrintLayoutToolBar.save"));
        this.saveLayout = new SaveLayout(parent);
        button.addActionListener(this.saveLayout);
        this.add(button);
        button = new ToolBarButton("saveas.gif", I18N.getString("org.saig.jump.widgets.print.PrintLayoutToolBar.save-as"));
        this.saveAsLayout = new SaveAsLayout(parent);
        button.addActionListener(this.saveAsLayout);
        this.add(button);
        this.addSeparator();
        button = new ToolBarButton("PageSetup16.gif", I18N.getString("org.saig.jump.widgets.print.PrintLayoutToolBar.page-setup"));
        this.pageSetup = new PageSetup(parent);
        button.addActionListener(this.pageSetup);
        this.add(button);
        button = new ToolBarButton("printOptions.gif", I18N.getString("org.saig.jump.widgets.print.PrintLayoutToolBar.print-options"));
        this.printOptions = new PrintOptions(parent);
        button.addActionListener(this.printOptions);
        this.add(button);
        button = new ToolBarButton("print.gif", I18N.getString("org.saig.jump.widgets.print.PrintLayoutToolBar.print"));
        this.print = new Print(parent);
        button.addActionListener(this.print);
        this.add(button);
        ToolBarButton batchPrintingJButton = new ToolBarButton("printbatch.gif", I18N.getString("org.saig.jump.widgets.print.PrintLayoutToolBar.Batch-printing"));
        this.batchPrint = new BatchPrintingAction(parent);
        batchPrintingJButton.addActionListener(this.batchPrint);
        this.add(batchPrintingJButton);
        this.addSeparator();
        button = new ToolBarButton("heightAdj.gif", I18N.getString("org.saig.jump.widgets.print.PrintLayoutToolBar.full-page"));
        this.fullPage = new FullPage(parent);
        button.addActionListener(this.fullPage);
        this.add(button);
        button = new ToolBarButton("widthAdj.gif", I18N.getString("org.saig.jump.widgets.print.PrintLayoutToolBar.page-width"));
        this.widthPage = new WidthPage(parent);
        button.addActionListener(this.widthPage);
        this.add(button);
        this.jComboBoxZoom.setMaximumSize(new Dimension(70, 25));
        this.zoom = new Zoom(parent);
        this.jComboBoxZoom.addActionListener(this.zoom);
        this.add(this.jComboBoxZoom);
        this.addSeparator();
        toggleButton = new ToolBarToggleButton("addView.gif", I18N.getString("org.saig.jump.widgets.print.PrintLayoutToolBar.add-view"));
        this.addMap = new AddMap(parent);
        toggleButton.addActionListener(this.addMap);
        this.addButtonGroup.add(toggleButton);
        this.add(toggleButton);
        toggleButton = new ToolBarToggleButton("addLegend.gif", I18N.getString("org.saig.jump.widgets.print.PrintLayoutToolBar.add-legend"));
        this.addLegend = new AddLegend(parent);
        toggleButton.addActionListener(this.addLegend);
        this.addButtonGroup.add(toggleButton);
        this.add(toggleButton);
        toggleButton = new ToolBarToggleButton("addText.gif", I18N.getString("org.saig.jump.widgets.print.PrintLayoutToolBar.add-text"));
        this.addtext = new AddText(parent);
        toggleButton.addActionListener(this.addtext);
        this.addButtonGroup.add(toggleButton);
        this.add(toggleButton);
        toggleButton = new ToolBarToggleButton("addNorth.gif", I18N.getString("org.saig.jump.widgets.print.PrintLayoutToolBar.add-north"));
        this.addNorth = new AddNorth(parent);
        toggleButton.addActionListener(this.addNorth);
        this.addButtonGroup.add(toggleButton);
        this.add(toggleButton);
        toggleButton = new ToolBarToggleButton("addImage.gif", I18N.getString("org.saig.jump.widgets.print.PrintLayoutToolBar.add-image"));
        this.addImage = new AddImage(parent);
        toggleButton.addActionListener(this.addImage);
        this.addButtonGroup.add(toggleButton);
        this.add(toggleButton);
        toggleButton = new ToolBarToggleButton("addScale.gif", I18N.getString("org.saig.jump.widgets.print.PrintLayoutToolBar.add-scale"));
        this.addScale = new AddScale(parent);
        toggleButton.addActionListener(this.addScale);
        this.addButtonGroup.add(toggleButton);
        this.add(toggleButton);
        toggleButton = new ToolBarToggleButton("addGeometry.gif", I18N.getString("org.saig.jump.widgets.print.PrintLayoutToolBar.add-geometry"));
        this.addGeometry = new AddGeometry(parent);
        toggleButton.addActionListener(this.addGeometry);
        this.addButtonGroup.add(toggleButton);
        this.add(toggleButton);
        this.addSeparator();
        this.deleteButton = new ToolBarButton("delete.gif", I18N.getString("org.saig.jump.widgets.print.PrintLayoutToolBar.delete"));
        this.deleteButton.setEnabled(false);
        this.delete = new Delete(parent);
        this.deleteButton.addActionListener(this.delete);
        this.add(this.deleteButton);
        this.addSeparator();
        ToolBarButton elementsDialogJButton = new ToolBarButton("sort.gif", I18N.getString("org.saig.jump.widgets.print.PrintLayoutToolBar.sort-elements"));
        this.showElementsDialog = new ShowElementsDialog(parent);
        elementsDialogJButton.addActionListener(this.showElementsDialog);
        this.add(elementsDialogJButton);
    }

    public void setZoom(String zoom) {
        this.jComboBoxZoom.removeItemAt(this.jComboBoxZoom.getMaximumRowCount());
        this.jComboBoxZoom.addItem(zoom);
        this.jComboBoxZoom.setSelectedIndex(this.jComboBoxZoom.getMaximumRowCount());
    }

    public ToolBarButton getDeleteButton() {
        return this.deleteButton;
    }

    public void dispose() {
        this.openLayout.dispose();
        this.openLayout = null;
        this.saveLayout.dispose();
        this.saveLayout = null;
        this.saveAsLayout.dispose();
        this.saveAsLayout = null;
        this.pageSetup.dispose();
        this.pageSetup = null;
        this.printOptions.dispose();
        this.printOptions = null;
        this.print.dispose();
        this.print = null;
        this.batchPrint.dispose();
        this.batchPrint = null;
        this.fullPage.dispose();
        this.fullPage = null;
        this.widthPage.dispose();
        this.widthPage = null;
        this.zoom.dispose();
        this.zoom = null;
        this.addMap.dispose();
        this.addMap = null;
        this.addLegend.dispose();
        this.addLegend = null;
        this.addtext.dispose();
        this.addtext = null;
        this.addNorth.dispose();
        this.addNorth = null;
        this.addImage.dispose();
        this.addImage = null;
        this.addScale.dispose();
        this.addScale = null;
        this.addGeometry.dispose();
        this.addGeometry = null;
        this.delete.dispose();
        this.delete = null;
        this.showElementsDialog.dispose();
        this.showElementsDialog = null;
    }

    public class ToolBarButton
    extends JButton {
        private static final long serialVersionUID = 1L;
        private final Insets margins = new Insets(0, 0, 0, 0);

        public ToolBarButton(String imageFile, String text) {
            URL imageURL = PrintIconLoader.class.getResource(imageFile);
            this.setActionCommand(text);
            this.setToolTipText(text);
            if (imageURL != null) {
                this.setIcon(new ImageIcon(imageURL, text));
            } else {
                this.setText(text);
                LOGGER.error((Object)(String.valueOf(I18N.getString("org.saig.jump.widgets.print.PrintLayoutToolBar.resource-not-found")) + ":" + imageFile));
            }
            this.setMargin(this.margins);
            this.setVerticalTextPosition(3);
            this.setHorizontalTextPosition(0);
        }
    }

    public class ToolBarToggleButton
    extends JToggleButton {
        private static final long serialVersionUID = 1L;
        private final Insets margins = new Insets(0, 0, 0, 0);

        public ToolBarToggleButton(String imageFile, String text) {
            URL imageURL = PrintIconLoader.class.getResource(imageFile);
            this.setActionCommand(text);
            this.setToolTipText(text);
            if (imageURL != null) {
                this.setIcon(new ImageIcon(imageURL, text));
            } else {
                this.setText(text);
                LOGGER.error((Object)(String.valueOf(I18N.getString("org.saig.jump.widgets.print.PrintLayoutToolBar.resource-not-found")) + " " + imageFile));
            }
            this.setMargin(this.margins);
            this.setVerticalTextPosition(3);
            this.setHorizontalTextPosition(0);
        }
    }
}


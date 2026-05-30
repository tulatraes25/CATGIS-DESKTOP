/*
 * Decompiled with CFR 0.152.
 */
package org.saig.jump.widgets.print;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;
import org.saig.jump.lang.I18N;
import org.saig.jump.widgets.print.PrintLayoutFrame;
import org.saig.jump.widgets.print.actions.AddImage;
import org.saig.jump.widgets.print.actions.AddLegend;
import org.saig.jump.widgets.print.actions.AddMap;
import org.saig.jump.widgets.print.actions.AddNorth;
import org.saig.jump.widgets.print.actions.AddScale;
import org.saig.jump.widgets.print.actions.AddText;
import org.saig.jump.widgets.print.actions.FullPage;
import org.saig.jump.widgets.print.actions.OpenLayout;
import org.saig.jump.widgets.print.actions.PageSetup;
import org.saig.jump.widgets.print.actions.Print;
import org.saig.jump.widgets.print.actions.PrintAction;
import org.saig.jump.widgets.print.actions.PrintOptions;
import org.saig.jump.widgets.print.actions.Quit;
import org.saig.jump.widgets.print.actions.SaveAsLayout;
import org.saig.jump.widgets.print.actions.SaveLayout;
import org.saig.jump.widgets.print.actions.WidthPage;
import org.saig.jump.widgets.print.elements.GraphicElements;
import org.saig.jump.widgets.print.images.PrintIconLoader;

public class PrintLayoutMenuBar
extends JMenuBar {
    private static final long serialVersionUID = 1L;
    private JMenu file = new JMenu(I18N.getString("org.saig.jump.widgets.print.PrintLayoutMenuBar.file"));
    private JMenu add = new JMenu(I18N.getString("org.saig.jump.widgets.print.PrintLayoutMenuBar.add"));
    private JMenu show = new JMenu(I18N.getString("org.saig.jump.widgets.print.PrintLayoutMenuBar.show"));
    private JMenu elements = new JMenu(I18N.getString("org.saig.jump.widgets.print.PrintLayoutMenuBar.elements"));
    private JMenuItem open;
    private JMenuItem save;
    private JMenuItem saveAs;
    private JMenuItem pageSetup;
    private JMenuItem print;
    private JMenuItem options;
    private JMenuItem quit;
    private JMenuItem map;
    private JMenuItem scaleBar;
    private JMenuItem northSymbol;
    private JMenuItem text;
    private JMenuItem image;
    private JMenuItem legend;
    private JMenuItem fullPage;
    private JMenuItem widthPage;
    private PrintLayoutFrame parent;
    private PrintAction openLayout;
    private PrintAction saveLayout;
    private PrintAction saveAsLayout;
    private PrintAction printAction;
    private PrintAction pageSetupAction;
    private PrintAction printOptions;
    private PrintAction quitAction;
    private PrintAction addMapAction;
    private PrintAction addLegendAction;
    private PrintAction addTextAction;
    private PrintAction addNorthAction;
    private PrintAction addImageAction;
    private PrintAction addScale;
    private PrintAction fullPageAction;
    private PrintAction widthPageAction;

    public PrintLayoutMenuBar(PrintLayoutFrame parent) {
        this.parent = parent;
        this.open = new JMenuItem(I18N.getString("org.saig.jump.widgets.print.PrintLayoutMenuBar.open"), PrintIconLoader.icon("open.gif"));
        this.openLayout = new OpenLayout(parent);
        this.open.addActionListener(this.openLayout);
        this.file.add(this.open);
        this.file.addSeparator();
        this.save = new JMenuItem(I18N.getString("org.saig.jump.widgets.print.PrintLayoutMenuBar.save"), PrintIconLoader.icon("save.gif"));
        this.saveLayout = new SaveLayout(parent);
        this.save.addActionListener(this.saveLayout);
        this.file.add(this.save);
        this.saveAs = new JMenuItem(I18N.getString("org.saig.jump.widgets.print.PrintLayoutMenuBar.save-as"), PrintIconLoader.icon("saveas.gif"));
        this.saveAsLayout = new SaveAsLayout(parent);
        this.saveAs.addActionListener(this.saveAsLayout);
        this.file.add(this.saveAs);
        this.file.addSeparator();
        this.pageSetup = new JMenuItem(I18N.getString("org.saig.jump.widgets.print.PrintLayoutMenuBar.page-setup"), PrintIconLoader.icon("PageSetup16.gif"));
        this.pageSetupAction = new PageSetup(parent);
        this.pageSetup.addActionListener(this.pageSetupAction);
        this.file.add(this.pageSetup);
        this.print = new JMenuItem(I18N.getString("org.saig.jump.widgets.print.PrintLayoutMenuBar.print"), PrintIconLoader.icon("print.gif"));
        this.printAction = new Print(parent);
        this.print.addActionListener(this.printAction);
        this.file.add(this.print);
        this.file.addSeparator();
        this.options = new JMenuItem(I18N.getString("org.saig.jump.widgets.print.PrintLayoutMenuBar.print-options"), PrintIconLoader.icon("printOptions.gif"));
        this.printOptions = new PrintOptions(parent);
        this.options.addActionListener(this.printOptions);
        this.file.add(this.options);
        this.file.addSeparator();
        this.quit = new JMenuItem(I18N.getString("org.saig.jump.widgets.print.PrintLayoutMenuBar.quit"));
        this.quitAction = new Quit(parent);
        this.quit.addActionListener(this.quitAction);
        this.file.add(this.quit);
        this.add(this.file);
        this.map = new JMenuItem(I18N.getString("org.saig.jump.widgets.print.PrintLayoutMenuBar.view"), PrintIconLoader.icon("addView.gif"));
        this.addMapAction = new AddMap(parent);
        this.map.addActionListener(this.addMapAction);
        this.add.add(this.map);
        this.legend = new JMenuItem(I18N.getString("org.saig.jump.widgets.print.PrintLayoutMenuBar.legend"), PrintIconLoader.icon("addLegend.gif"));
        this.addLegendAction = new AddLegend(parent);
        this.legend.addActionListener(this.addLegendAction);
        this.add.add(this.legend);
        this.text = new JMenuItem(I18N.getString("org.saig.jump.widgets.print.PrintLayoutMenuBar.text"), PrintIconLoader.icon("addText.gif"));
        this.addTextAction = new AddText(parent);
        this.text.addActionListener(this.addTextAction);
        this.add.add(this.text);
        this.northSymbol = new JMenuItem(I18N.getString("org.saig.jump.widgets.print.PrintLayoutMenuBar.north-symbol"), PrintIconLoader.icon("addNorth.gif"));
        this.addNorthAction = new AddNorth(parent);
        this.northSymbol.addActionListener(this.addNorthAction);
        this.add.add(this.northSymbol);
        this.image = new JMenuItem(I18N.getString("org.saig.jump.widgets.print.PrintLayoutMenuBar.image"), PrintIconLoader.icon("addImage.gif"));
        this.addImageAction = new AddImage(parent);
        this.image.addActionListener(this.addImageAction);
        this.add.add(this.image);
        this.scaleBar = new JMenuItem(I18N.getString("org.saig.jump.widgets.print.PrintLayoutMenuBar.scale-bar"), PrintIconLoader.icon("addScale.gif"));
        this.addScale = new AddScale(parent);
        this.scaleBar.addActionListener(this.addScale);
        this.add.add(this.scaleBar);
        this.add(this.add);
        this.fullPage = new JMenuItem(I18N.getString("org.saig.jump.widgets.print.PrintLayoutMenuBar.full-page"), PrintIconLoader.icon("heightAdj.gif"));
        this.fullPageAction = new FullPage(parent);
        this.fullPage.addActionListener(this.fullPageAction);
        this.show.add(this.fullPage);
        this.widthPage = new JMenuItem(I18N.getString("org.saig.jump.widgets.print.PrintLayoutMenuBar.page-width"), PrintIconLoader.icon("widthAdj.gif"));
        this.widthPageAction = new WidthPage(parent);
        this.widthPage.addActionListener(this.widthPageAction);
        this.show.add(this.widthPage);
        this.add(this.show);
        this.elements.addMenuListener(new MenuListener(){

            @Override
            public void menuCanceled(MenuEvent e) {
            }

            @Override
            public void menuDeselected(MenuEvent e) {
            }

            @Override
            public void menuSelected(MenuEvent e) {
                PrintLayoutMenuBar.this.elementsMenu_menuSelected(e);
            }
        });
        this.add(this.elements);
    }

    void elementsMenu_menuSelected(MenuEvent e) {
        this.elements.removeAll();
        if (this.parent != null) {
            List<GraphicElements> graphicElements = this.parent.getGraphicElements();
            int i = 0;
            while (i < graphicElements.size()) {
                JCheckBoxMenuItem menuItem = new JCheckBoxMenuItem();
                GraphicElements graphic = graphicElements.get(i);
                if (graphic.isSelected()) {
                    menuItem.setSelected(true);
                }
                menuItem.setText(graphic.getName());
                menuItem.setIcon(graphic.getIcon());
                this.associate(menuItem, graphic);
                this.elements.add(menuItem);
                ++i;
            }
        }
    }

    private void associate(JCheckBoxMenuItem menuItem, final GraphicElements graphic) {
        menuItem.addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    if (PrintLayoutMenuBar.this.parent.getSelectedComponent() != null && !PrintLayoutMenuBar.this.parent.getSelectedComponent().getName().equals(graphic.getName())) {
                        PrintLayoutMenuBar.this.parent.getSelectedComponent().setSelected(false);
                    }
                    graphic.setSelected(true);
                    PrintLayoutMenuBar.this.parent.setSelectedComponent(graphic);
                }
                catch (Throwable throwable) {
                    // empty catch block
                }
            }
        });
    }

    public void dispose() {
        this.openLayout.dispose();
        this.openLayout = null;
        this.saveLayout.dispose();
        this.saveLayout = null;
        this.saveAsLayout.dispose();
        this.saveAsLayout = null;
        this.printAction.dispose();
        this.printAction = null;
        this.pageSetupAction.dispose();
        this.pageSetupAction = null;
        this.printOptions.dispose();
        this.printOptions = null;
        this.quitAction.dispose();
        this.quitAction = null;
        this.addMapAction.dispose();
        this.addMapAction = null;
        this.addLegendAction.dispose();
        this.addLegendAction = null;
        this.addTextAction.dispose();
        this.addTextAction = null;
        this.addNorthAction.dispose();
        this.addNorthAction = null;
        this.addImageAction.dispose();
        this.addImageAction = null;
        this.addScale.dispose();
        this.addScale = null;
        this.fullPageAction.dispose();
        this.fullPageAction = null;
        this.widthPageAction.dispose();
        this.widthPageAction = null;
        int i = this.getMenuCount() - 1;
        while (i >= 0) {
            JMenu menu = this.getMenu(i);
            this.remove(menu);
            --i;
        }
        this.file = null;
        this.add = null;
        this.show = null;
        this.elements = null;
        this.open = null;
        this.save = null;
        this.saveAs = null;
        this.pageSetup = null;
        this.print = null;
        this.options = null;
        this.quit = null;
        this.map = null;
        this.scaleBar = null;
        this.northSymbol = null;
        this.text = null;
        this.image = null;
        this.legend = null;
        this.fullPage = null;
        this.widthPage = null;
        this.parent = null;
    }
}


/*
 * Decompiled with CFR 0.152.
 */
package org.saig.jump.widgets.print;

import com.vividsolutions.jump.workbench.ui.images.IconLoader;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import org.saig.jump.lang.I18N;
import org.saig.jump.widgets.print.DragableJList;
import org.saig.jump.widgets.print.ElementTransferHandler;
import org.saig.jump.widgets.print.ElementsListModel;
import org.saig.jump.widgets.print.PrintLayoutFrame;
import org.saig.jump.widgets.print.elements.GraphicElements;

public class ElementsViewerDialog
extends JDialog {
    private static final long serialVersionUID = 1L;
    private DragableJList elementsList;
    private ElementsListModel<GraphicElements> elementsListModel;
    private JButton jButtonUp;
    private JButton jButtonDown;
    private JPanel jPanelBottom;

    public ElementsViewerDialog(PrintLayoutFrame parent) {
        super((Frame)parent, I18N.getString("org.saig.jump.widgets.print.ElementsViewerDialog.elements"));
        this.elementsList = new DragableJList(parent);
        this.getContentPane().setLayout(new BorderLayout());
        this.getContentPane().add((Component)new JScrollPane(this.elementsList), "Center");
        this.setSize(200, 300);
        this.setLocation(200, 200);
        this.elementsListModel = new ElementsListModel<GraphicElements>(parent.getGraphicElements());
        this.elementsList.setModel(this.elementsListModel);
        this.elementsList.setDragEnabled(true);
        this.elementsList.setTransferHandler(new ElementTransferHandler());
        this.jButtonUp = new JButton(IconLoader.icon("Up2.gif"));
        this.jButtonDown = new JButton(IconLoader.icon("Down2.gif"));
        this.jPanelBottom = new JPanel();
        this.jPanelBottom.add(this.jButtonUp);
        this.jPanelBottom.add(this.jButtonDown);
        this.getContentPane().add((Component)this.jPanelBottom, "South");
        this.jButtonUp.addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent arg0) {
                ElementsViewerDialog.this.moveup();
            }
        });
        this.jButtonDown.addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent arg0) {
                ElementsViewerDialog.this.movedown();
            }
        });
        this.setVisible(true);
    }

    public void addElement(GraphicElements e) {
        this.elementsListModel.addElementAt(this.elementsListModel.getSize() - 1, e);
    }

    public void orderChanged() {
        this.elementsList.orderChanged();
        this.elementsListModel.contentsChanged();
    }

    public void contentsChanged() {
        this.elementsListModel.contentsChanged();
    }

    public void moveup() {
        int i = this.elementsList.getSelectedIndex();
        if (i != -1 && i != 0) {
            GraphicElements o = this.elementsListModel.getElementAt(i);
            this.elementsListModel.removeElement(i);
            this.elementsListModel.addElementAt(i - 1, o);
            this.elementsList.setSelectedIndex(i - 1);
            this.orderChanged();
        }
    }

    public void movedown() {
        int i = this.elementsList.getSelectedIndex();
        if (i != -1 && i <= this.elementsListModel.getSize() - 3) {
            GraphicElements o = this.elementsListModel.getElementAt(i);
            this.elementsListModel.removeElement(i);
            this.elementsListModel.addElementAt(i + 1, o);
            this.elementsList.setSelectedIndex(i + 1);
            this.orderChanged();
        }
    }
}


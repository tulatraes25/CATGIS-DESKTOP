/*
 * Decompiled with CFR 0.152.
 */
package org.saig.core.model.data.widgets.tables.management;

import com.vividsolutions.jump.workbench.ui.images.IconLoader;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JSpinner;
import javax.swing.JToolBar;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.saig.core.model.data.widgets.tables.management.InfoPanel;
import org.saig.core.model.data.widgets.tables.management.PagesNavigationListener;
import org.saig.jump.lang.I18N;

public class PagesNavigationPanel
extends JToolBar {
    private int totalNumRecords;
    private int numRecordsPerPage;
    private int firstRecordShown;
    private int lastRecordShown;
    private int actualRecord;
    private JButton nextRecordButton;
    private JButton nextPageButton;
    private JButton previousRecordButton;
    private JButton previousPageButton;
    private JButton firstRecordButton;
    private JButton lastRecordButton;
    private JButton firstPageButton;
    private JButton lastPageButton;
    private JSpinner numRecordsPerPageSpinner;
    private InfoPanel infoPanel;
    private List<PagesNavigationListener> listenerList;

    public PagesNavigationPanel() {
        this.setFloatable(false);
        this.listenerList = new ArrayList<PagesNavigationListener>();
        this.infoPanel = new InfoPanel();
        this.initializeIndexes();
        this.initializeButtons();
        this.initializeSpinner();
        this.updateButtonsState();
        this.add(this.firstRecordButton);
        this.add(this.previousRecordButton);
        this.add(this.nextRecordButton);
        this.add(this.lastRecordButton);
        this.addSeparator();
        this.add(this.firstPageButton);
        this.add(this.previousPageButton);
        this.add(this.numRecordsPerPageSpinner);
        this.add(this.nextPageButton);
        this.add(this.lastPageButton);
    }

    private void initializeIndexes() {
        this.firstRecordShown = 1;
        this.lastRecordShown = 1;
        this.actualRecord = 1;
        this.totalNumRecords = 0;
        this.numRecordsPerPage = 100;
    }

    private void initializeButtons() {
        this.firstPageButton = new JButton(IconLoader.icon("navegacion_primera_pag.PNG"));
        this.firstPageButton.setPreferredSize(new Dimension(28, 28));
        this.firstPageButton.setToolTipText(I18N.getString(this.getClass(), "first-page"));
        this.firstPageButton.addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent e) {
                PagesNavigationPanel.this.firstRecordShown = 1;
                PagesNavigationPanel.this.lastRecordShown = PagesNavigationPanel.this.numRecordsPerPage;
                PagesNavigationPanel.this.actualRecord = 1;
                PagesNavigationPanel.this.updateButtonsState();
                PagesNavigationPanel.this.firePageNavigationEvent(6);
            }
        });
        this.previousPageButton = new JButton(IconLoader.icon("navegacion_anterior_pag.PNG"));
        this.previousPageButton.setPreferredSize(new Dimension(28, 28));
        this.previousPageButton.setToolTipText(I18N.getString(this.getClass(), "previous-page"));
        this.previousPageButton.addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent e) {
                PagesNavigationPanel pagesNavigationPanel = PagesNavigationPanel.this;
                pagesNavigationPanel.firstRecordShown = pagesNavigationPanel.firstRecordShown - PagesNavigationPanel.this.numRecordsPerPage;
                if (PagesNavigationPanel.this.firstRecordShown < 1) {
                    PagesNavigationPanel.this.firstRecordShown = 1;
                }
                PagesNavigationPanel.this.lastRecordShown = PagesNavigationPanel.this.firstRecordShown + PagesNavigationPanel.this.numRecordsPerPage - 1;
                PagesNavigationPanel.this.actualRecord = 1;
                PagesNavigationPanel.this.updateButtonsState();
                PagesNavigationPanel.this.firePageNavigationEvent(2);
            }
        });
        this.firstRecordButton = new JButton(IconLoader.icon("navegacion_primero.PNG"));
        this.firstRecordButton.setPreferredSize(new Dimension(28, 28));
        this.firstRecordButton.setToolTipText(I18N.getString(this.getClass(), "first-record-on-page"));
        this.firstRecordButton.addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent e) {
                PagesNavigationPanel.this.actualRecord = 1;
                PagesNavigationPanel.this.updateButtonsState();
                PagesNavigationPanel.this.firePageNavigationEvent(4);
            }
        });
        this.previousRecordButton = new JButton(IconLoader.icon("navegacion_anterior.PNG"));
        this.previousRecordButton.setPreferredSize(new Dimension(28, 28));
        this.previousRecordButton.setToolTipText(I18N.getString(this.getClass(), "previous-record"));
        this.previousRecordButton.addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent e) {
                if (PagesNavigationPanel.this.actualRecord > 1) {
                    PagesNavigationPanel pagesNavigationPanel = PagesNavigationPanel.this;
                    pagesNavigationPanel.actualRecord = pagesNavigationPanel.actualRecord - 1;
                }
                PagesNavigationPanel.this.updateButtonsState();
                PagesNavigationPanel.this.firePageNavigationEvent(1);
            }
        });
        this.nextRecordButton = new JButton(IconLoader.icon("navegacion_siguiente.PNG"));
        this.nextRecordButton.setPreferredSize(new Dimension(28, 28));
        this.nextRecordButton.setToolTipText(I18N.getString(this.getClass(), "next-record"));
        this.nextRecordButton.addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent e) {
                if (PagesNavigationPanel.this.actualRecord < PagesNavigationPanel.this.numRecordsPerPage && PagesNavigationPanel.this.firstRecordShown + PagesNavigationPanel.this.actualRecord - 1 < PagesNavigationPanel.this.totalNumRecords) {
                    PagesNavigationPanel pagesNavigationPanel = PagesNavigationPanel.this;
                    pagesNavigationPanel.actualRecord = pagesNavigationPanel.actualRecord + 1;
                }
                PagesNavigationPanel.this.updateButtonsState();
                PagesNavigationPanel.this.firePageNavigationEvent(0);
            }
        });
        this.lastRecordButton = new JButton(IconLoader.icon("navegacion_ultimo.PNG"));
        this.lastRecordButton.setPreferredSize(new Dimension(28, 28));
        this.lastRecordButton.setToolTipText(I18N.getString(this.getClass(), "last-record-on-page"));
        this.lastRecordButton.addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent e) {
                if (PagesNavigationPanel.this.totalNumRecords < PagesNavigationPanel.this.numRecordsPerPage) {
                    PagesNavigationPanel.this.actualRecord = PagesNavigationPanel.this.totalNumRecords;
                } else if (PagesNavigationPanel.this.isInLastPage()) {
                    PagesNavigationPanel.this.actualRecord = PagesNavigationPanel.this.totalNumRecords - (PagesNavigationPanel.this.getNumPages() - 1) * PagesNavigationPanel.this.numRecordsPerPage;
                } else {
                    PagesNavigationPanel.this.actualRecord = PagesNavigationPanel.this.numRecordsPerPage;
                }
                PagesNavigationPanel.this.updateButtonsState();
                PagesNavigationPanel.this.firePageNavigationEvent(5);
            }
        });
        this.nextPageButton = new JButton(IconLoader.icon("navegacion_siguiente_pag.PNG"));
        this.nextPageButton.setPreferredSize(new Dimension(28, 28));
        this.nextPageButton.setToolTipText(I18N.getString(this.getClass(), "next-page"));
        this.nextPageButton.addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent e) {
                double numPages = PagesNavigationPanel.this.getNumPages();
                PagesNavigationPanel pagesNavigationPanel = PagesNavigationPanel.this;
                pagesNavigationPanel.firstRecordShown = pagesNavigationPanel.firstRecordShown + PagesNavigationPanel.this.numRecordsPerPage;
                if ((double)PagesNavigationPanel.this.firstRecordShown > (numPages - 1.0) * (double)PagesNavigationPanel.this.numRecordsPerPage + 1.0) {
                    PagesNavigationPanel.this.firstRecordShown = (int)((numPages - 1.0) * (double)PagesNavigationPanel.this.numRecordsPerPage) + 1;
                }
                PagesNavigationPanel pagesNavigationPanel2 = PagesNavigationPanel.this;
                pagesNavigationPanel2.lastRecordShown = pagesNavigationPanel2.lastRecordShown + PagesNavigationPanel.this.numRecordsPerPage;
                if (PagesNavigationPanel.this.lastRecordShown > PagesNavigationPanel.this.totalNumRecords) {
                    PagesNavigationPanel.this.lastRecordShown = PagesNavigationPanel.this.totalNumRecords;
                }
                PagesNavigationPanel.this.actualRecord = 1;
                PagesNavigationPanel.this.updateButtonsState();
                PagesNavigationPanel.this.firePageNavigationEvent(2);
            }
        });
        this.lastPageButton = new JButton(IconLoader.icon("navegacion_ultima_pag.PNG"));
        this.lastPageButton.setPreferredSize(new Dimension(28, 28));
        this.lastPageButton.setToolTipText(I18N.getString(this.getClass(), "last-page"));
        this.lastPageButton.addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent e) {
                double numPages = PagesNavigationPanel.this.getNumPages();
                PagesNavigationPanel.this.firstRecordShown = (int)((numPages - 1.0) * (double)PagesNavigationPanel.this.numRecordsPerPage) + 1;
                PagesNavigationPanel.this.lastRecordShown = PagesNavigationPanel.this.firstRecordShown + PagesNavigationPanel.this.numRecordsPerPage - 1;
                if (PagesNavigationPanel.this.lastRecordShown > PagesNavigationPanel.this.totalNumRecords) {
                    PagesNavigationPanel.this.lastRecordShown = PagesNavigationPanel.this.totalNumRecords;
                }
                PagesNavigationPanel.this.actualRecord = 1;
                PagesNavigationPanel.this.updateButtonsState();
                PagesNavigationPanel.this.firePageNavigationEvent(2);
            }
        });
    }

    private void initializeSpinner() {
        SpinnerNumberModel model = new SpinnerNumberModel(this.numRecordsPerPage, 0, 1000, 100);
        this.numRecordsPerPageSpinner = new JSpinner(model);
        Dimension dim = new Dimension(60, 20);
        this.numRecordsPerPageSpinner.setMinimumSize(dim);
        this.numRecordsPerPageSpinner.setPreferredSize(dim);
        this.numRecordsPerPageSpinner.setEnabled(true);
        this.numRecordsPerPageSpinner.setToolTipText(I18N.getString(this.getClass(), "number-of-records-shown-in-each-page"));
        this.numRecordsPerPageSpinner.addChangeListener(new ChangeListener(){

            @Override
            public void stateChanged(ChangeEvent e) {
                PagesNavigationPanel.this.numRecordsPerPage = ((Number)PagesNavigationPanel.this.numRecordsPerPageSpinner.getValue()).intValue();
                PagesNavigationPanel.this.firstRecordShown = 1;
                PagesNavigationPanel.this.lastRecordShown = PagesNavigationPanel.this.numRecordsPerPage;
                if (PagesNavigationPanel.this.numRecordsPerPage > PagesNavigationPanel.this.totalNumRecords) {
                    PagesNavigationPanel.this.lastRecordShown = PagesNavigationPanel.this.totalNumRecords;
                }
                PagesNavigationPanel.this.actualRecord = 1;
                PagesNavigationPanel.this.updateButtonsState();
                PagesNavigationPanel.this.firePageNavigationEvent(8);
            }
        });
    }

    private void updateButtonsState() {
        if (this.totalNumRecords == 0) {
            this.previousRecordButton.setEnabled(false);
            this.firstRecordButton.setEnabled(false);
            this.previousPageButton.setEnabled(false);
            this.firstPageButton.setEnabled(false);
            this.nextPageButton.setEnabled(false);
            this.lastPageButton.setEnabled(false);
            this.nextRecordButton.setEnabled(false);
            this.lastRecordButton.setEnabled(false);
        } else {
            boolean isInFirstRecordOfPage = this.actualRecord == 1;
            this.previousRecordButton.setEnabled(!isInFirstRecordOfPage);
            this.firstRecordButton.setEnabled(!isInFirstRecordOfPage);
            boolean isInFirstPage = this.firstRecordShown == 1;
            this.previousPageButton.setEnabled(!isInFirstPage);
            this.firstPageButton.setEnabled(!isInFirstPage);
            if (this.numRecordsPerPage != 0) {
                boolean isInLastPage = this.isInLastPage();
                this.nextPageButton.setEnabled(!isInLastPage);
                this.lastPageButton.setEnabled(!isInLastPage);
                boolean isInLastRecordOfPage = this.actualRecord == this.numRecordsPerPage || isInLastPage && this.firstRecordShown + this.actualRecord - 1 == this.totalNumRecords;
                this.nextRecordButton.setEnabled(!isInLastRecordOfPage);
                this.lastRecordButton.setEnabled(!isInLastRecordOfPage);
            }
        }
        this.infoPanel.updateInfoLabel(this.firstRecordShown, this.actualRecord);
        this.infoPanel.updateRanges(this.totalNumRecords, this.firstRecordShown, this.lastRecordShown);
    }

    public void addPageNavigationListener(PagesNavigationListener listener) {
        this.listenerList.add(listener);
    }

    public void removeNavigationListener(PagesNavigationListener listener) {
        this.listenerList.remove(listener);
    }

    public void clearListeners() {
        this.listenerList.clear();
    }

    private void firePageNavigationEvent(int eventType) {
        Iterator<PagesNavigationListener> it = this.listenerList.iterator();
        while (it.hasNext()) {
            it.next().pagesNavigationEventFired(eventType);
        }
    }

    public int getTotalNumRecords() {
        return this.totalNumRecords;
    }

    public void setTotalNumRecords(int totalNumRecords) {
        this.totalNumRecords = totalNumRecords;
        this.infoPanel.updateRanges(totalNumRecords, this.firstRecordShown, this.lastRecordShown);
        this.lastRecordShown = totalNumRecords >= this.numRecordsPerPage ? this.numRecordsPerPage : totalNumRecords;
        this.firstRecordShown = 1;
        this.actualRecord = 1;
        this.updateButtonsState();
    }

    public int getNumRecordsPerPage() {
        return this.numRecordsPerPage;
    }

    public int getFirstRecordShown() {
        return this.firstRecordShown;
    }

    public int getLastRecordShown() {
        return this.lastRecordShown;
    }

    public int getActualRecord() {
        return this.actualRecord;
    }

    public void setActualRecord(int actualRecord) {
        this.actualRecord = actualRecord;
        this.updateButtonsState();
        this.infoPanel.updateInfoLabel(this.firstRecordShown, actualRecord);
    }

    private int getNumPages() {
        return new Double(Math.ceil((double)this.totalNumRecords / (double)this.numRecordsPerPage)).intValue();
    }

    private boolean isInLastPage() {
        return this.firstRecordShown == (this.getNumPages() - 1) * this.numRecordsPerPage + 1;
    }

    public InfoPanel getInfoPanel() {
        return this.infoPanel;
    }
}


/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vividsolutions.jts.geom.Envelope
 *  com.vividsolutions.jts.geom.Geometry
 *  com.vividsolutions.jts.geom.GeometryCollection
 *  com.vividsolutions.jts.geom.GeometryFactory
 *  com.vividsolutions.jts.io.ParseException
 *  com.vividsolutions.jts.io.WKTReader
 *  org.apache.log4j.Logger
 */
package org.saig.jump.widgets.utils.bookmarks;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;
import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.geom.EnvelopeUtil;
import com.vividsolutions.jump.workbench.JUMPWorkbench;
import com.vividsolutions.jump.workbench.ui.LayerViewPanel;
import com.vividsolutions.jump.workbench.ui.OKCancelPanel;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import org.apache.log4j.Logger;
import org.saig.core.gui.swing.sldeditor.util.FormUtils;
import org.saig.core.model.bookmark.IBookmark;
import org.saig.core.model.bookmark.TemporalBookmark;
import org.saig.jump.lang.I18N;
import org.saig.jump.widgets.util.TimestampSelectionPanel;
import org.saig.jump.widgets.util.validating.NullTextFieldValidator;

public class EditBookmarkDialog
extends JDialog {
    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = Logger.getLogger(EditBookmarkDialog.class);
    private static final String TITLE = I18N.getString("org.saig.jump.widgets.utils.bookmarks.EditBookmarkDialog.edit-bookmark");
    private JPanel bookmarkDetailsPanel;
    private JTextField bookmarkNameTextField;
    private JTextArea bookmarkDescriptionTextArea;
    private JTextArea localizationTextArea;
    private JLabel timestampLabel;
    private TimestampSelectionPanel timestampPanel;
    private JRadioButton customDateOption;
    private JRadioButton currentDateOption;
    private ButtonGroup dateButtonGroup;
    private JRadioButton currentViewEnvelopeOption;
    private JRadioButton wktTextOption;
    private JRadioButton selectedFeaturesOption;
    private ButtonGroup localizationButtonGroup;
    private OKCancelPanel okCancelPanel;
    private IBookmark bookmark;
    private WKTReader wktReader = new WKTReader();
    private Envelope currentViewEnvelope;
    private Geometry selectedGeometries;

    public EditBookmarkDialog(JFrame owner, boolean modal, IBookmark bm) {
        super((Frame)owner, modal);
        this.bookmark = bm;
        this.setTitle(TITLE);
        this.initialize();
        this.loadCurrentViewParameters();
        this.loadBookmark(this.bookmark);
    }

    protected void initialize() {
        JPanel mainPanel = new JPanel(new GridBagLayout());
        this.setContentPane(mainPanel);
        FormUtils.addRowInGBL(mainPanel, 0, 0, this.getBookmarkDetailsPanel());
        FormUtils.addRowInGBL(mainPanel, 1, 0, this.getOkCancelPanel());
    }

    private JPanel getBookmarkDetailsPanel() {
        if (this.bookmarkDetailsPanel == null) {
            this.bookmarkDetailsPanel = new JPanel(new GridBagLayout());
            this.bookmarkDetailsPanel.setBorder(BorderFactory.createTitledBorder(I18N.getString("org.saig.jump.widgets.utils.bookmarks.EditBookmarkDialog.bookmark-details")));
            JLabel bookmarkNameLabel = new JLabel(String.valueOf(I18N.getString("org.saig.jump.widgets.utils.bookmarks.EditBookmarkDialog.name")) + ":");
            this.bookmarkNameTextField = new JTextField();
            this.bookmarkNameTextField.setInputVerifier(new NullTextFieldValidator(this, this.bookmarkNameTextField));
            JLabel bookmarkDescriptionLabel = new JLabel(String.valueOf(I18N.getString("org.saig.jump.widgets.utils.bookmarks.EditBookmarkDialog.description")) + ":");
            this.bookmarkDescriptionTextArea = new JTextArea(80, 4);
            this.bookmarkDescriptionTextArea.setLineWrap(true);
            this.bookmarkDescriptionTextArea.setFont(this.bookmarkNameTextField.getFont());
            JScrollPane bookmarkTextAreaScrollPane = new JScrollPane(this.bookmarkDescriptionTextArea, 22, 31);
            bookmarkTextAreaScrollPane.setMinimumSize(new Dimension(400, 80));
            bookmarkTextAreaScrollPane.setPreferredSize(new Dimension(400, 80));
            JLabel localizationLabel = new JLabel(String.valueOf(I18N.getString("org.saig.jump.widgets.utils.bookmarks.EditBookmarkDialog.localization")) + ":");
            this.localizationTextArea = new JTextArea(80, 4);
            this.localizationTextArea.setLineWrap(true);
            this.localizationTextArea.setFont(this.bookmarkNameTextField.getFont());
            this.localizationTextArea.setEditable(false);
            JScrollPane localizationTextAreaScrollPane = new JScrollPane(this.localizationTextArea, 22, 31);
            localizationTextAreaScrollPane.setMinimumSize(new Dimension(400, 80));
            localizationTextAreaScrollPane.setPreferredSize(new Dimension(400, 80));
            this.currentViewEnvelopeOption = new JRadioButton(I18N.getString("org.saig.jump.widgets.utils.bookmarks.EditBookmarkDialog.current-view-envelope"));
            this.currentViewEnvelopeOption.addActionListener(new ActionListener(){

                @Override
                public void actionPerformed(ActionEvent e) {
                    if (EditBookmarkDialog.this.currentViewEnvelope != null) {
                        EditBookmarkDialog.this.localizationTextArea.setText(EnvelopeUtil.toGeometry(EditBookmarkDialog.this.currentViewEnvelope).toText());
                        EditBookmarkDialog.this.localizationTextArea.setCaretPosition(0);
                    }
                    EditBookmarkDialog.this.updateComponentsState();
                }
            });
            this.wktTextOption = new JRadioButton(I18N.getString("org.saig.jump.widgets.utils.bookmarks.EditBookmarkDialog.wkt-text"));
            this.wktTextOption.addActionListener(new ActionListener(){

                @Override
                public void actionPerformed(ActionEvent e) {
                    EditBookmarkDialog.this.updateComponentsState();
                }
            });
            this.selectedFeaturesOption = new JRadioButton(I18N.getString("org.saig.jump.widgets.utils.bookmarks.EditBookmarkDialog.active-view-selected-features"));
            this.selectedFeaturesOption.addActionListener(new ActionListener(){

                @Override
                public void actionPerformed(ActionEvent e) {
                    if (EditBookmarkDialog.this.selectedGeometries != null) {
                        EditBookmarkDialog.this.localizationTextArea.setText(EditBookmarkDialog.this.selectedGeometries.toText());
                        EditBookmarkDialog.this.localizationTextArea.setCaretPosition(0);
                    }
                    EditBookmarkDialog.this.updateComponentsState();
                }
            });
            this.localizationButtonGroup = new ButtonGroup();
            this.localizationButtonGroup.add(this.currentViewEnvelopeOption);
            this.localizationButtonGroup.add(this.wktTextOption);
            this.localizationButtonGroup.add(this.selectedFeaturesOption);
            this.timestampLabel = new JLabel(String.valueOf(I18N.getString("org.saig.jump.widgets.utils.bookmarks.EditBookmarkDialog.Date")) + ":");
            this.timestampPanel = new TimestampSelectionPanel();
            this.customDateOption = new JRadioButton(I18N.getString("org.saig.jump.widgets.utils.bookmarks.EditBookmarkDialog.Custom-date"));
            this.customDateOption.addActionListener(new ActionListener(){

                @Override
                public void actionPerformed(ActionEvent e) {
                    EditBookmarkDialog.this.timestampPanel.setEnabled(EditBookmarkDialog.this.customDateOption.isSelected());
                }
            });
            this.currentDateOption = new JRadioButton(I18N.getString("org.saig.jump.widgets.utils.bookmarks.EditBookmarkDialog.Current-date"));
            this.currentDateOption.addActionListener(new ActionListener(){

                @Override
                public void actionPerformed(ActionEvent e) {
                    EditBookmarkDialog.this.timestampPanel.setEnabled(EditBookmarkDialog.this.customDateOption.isSelected());
                }
            });
            this.dateButtonGroup = new ButtonGroup();
            this.dateButtonGroup.add(this.customDateOption);
            this.dateButtonGroup.add(this.currentDateOption);
            FormUtils.addRowInGBL((JComponent)this.bookmarkDetailsPanel, 0, 0, bookmarkNameLabel, (JComponent)this.bookmarkNameTextField, true);
            FormUtils.addRowInGBL((JComponent)this.bookmarkDetailsPanel, 1, 0, (JComponent)bookmarkDescriptionLabel, true, true);
            FormUtils.addRowInGBL(this.bookmarkDetailsPanel, 2, 0, bookmarkTextAreaScrollPane);
            FormUtils.addRowInGBL((JComponent)this.bookmarkDetailsPanel, 5, 0, (JComponent)this.timestampLabel, true, true);
            FormUtils.addRowInGBL((JComponent)this.bookmarkDetailsPanel, 6, 0, (JComponent)this.customDateOption, true, true);
            FormUtils.addRowInGBL((JComponent)this.bookmarkDetailsPanel, 7, 0, (JComponent)this.timestampPanel, true, true);
            FormUtils.addRowInGBL((JComponent)this.bookmarkDetailsPanel, 8, 0, (JComponent)this.currentDateOption, true, true);
            FormUtils.addRowInGBL((JComponent)this.bookmarkDetailsPanel, 10, 0, (JComponent)localizationLabel, false, true);
            FormUtils.addRowInGBL(this.bookmarkDetailsPanel, 11, 0, localizationTextAreaScrollPane);
            FormUtils.addRowInGBL((JComponent)this.bookmarkDetailsPanel, 12, 0, (JComponent)this.currentViewEnvelopeOption, true, true);
            FormUtils.addRowInGBL((JComponent)this.bookmarkDetailsPanel, 13, 0, (JComponent)this.wktTextOption, true, true);
            FormUtils.addRowInGBL((JComponent)this.bookmarkDetailsPanel, 14, 0, (JComponent)this.selectedFeaturesOption, true, true);
        }
        return this.bookmarkDetailsPanel;
    }

    protected void loadBookmark(IBookmark bookmark) {
        this.bookmarkNameTextField.setEditable(bookmark != null);
        this.bookmarkDescriptionTextArea.setEditable(bookmark != null);
        if (bookmark != null) {
            this.bookmarkNameTextField.setText(bookmark.getName());
            this.bookmarkDescriptionTextArea.setText(bookmark.getDescription());
            if (bookmark.getLocalization() != null) {
                boolean isGC = bookmark.getLocalization() instanceof GeometryCollection;
                this.localizationTextArea.setText(bookmark.getLocalization().toText());
                this.localizationTextArea.setCaretPosition(0);
                if (this.currentViewEnvelope != null && !isGC) {
                    Geometry currentEnvGeom = EnvelopeUtil.toGeometry(this.currentViewEnvelope);
                    if (!(currentEnvGeom instanceof GeometryCollection) && bookmark.getLocalization().equals(currentEnvGeom)) {
                        this.currentViewEnvelopeOption.setSelected(true);
                    }
                } else if (this.selectedGeometries != null && !isGC && !(this.selectedGeometries instanceof GeometryCollection) && bookmark.getLocalization().equals(this.selectedGeometries)) {
                    this.selectedFeaturesOption.setSelected(true);
                }
            }
            if (bookmark instanceof TemporalBookmark) {
                Timestamp bookmarkTimemark = ((TemporalBookmark)bookmark).getTimemark();
                if (bookmarkTimemark != null) {
                    this.timestampPanel.setDate(bookmarkTimemark);
                    this.timestampLabel.setVisible(true);
                    this.timestampPanel.setVisible(true);
                    this.customDateOption.setVisible(true);
                    this.currentDateOption.setVisible(true);
                    this.customDateOption.doClick();
                } else {
                    this.currentDateOption.doClick();
                }
            } else {
                this.customDateOption.setVisible(false);
                this.currentDateOption.setVisible(false);
                this.timestampLabel.setVisible(false);
                this.timestampPanel.setVisible(false);
            }
        } else {
            this.bookmarkNameTextField.setText("");
            this.bookmarkDescriptionTextArea.setText("");
            this.localizationTextArea.setText("");
        }
        this.updateComponentsState();
    }

    public IBookmark getBookmark() {
        return this.bookmark;
    }

    public OKCancelPanel getOkCancelPanel() {
        if (this.okCancelPanel == null) {
            this.okCancelPanel = new OKCancelPanel();
            this.okCancelPanel.addActionListener(new ActionListener(){

                @Override
                public void actionPerformed(ActionEvent e) {
                    if (EditBookmarkDialog.this.okCancelPanel.wasOKPressed()) {
                        if (EditBookmarkDialog.this.isInputValid()) {
                            EditBookmarkDialog.this.updateBookmark();
                            EditBookmarkDialog.this.setVisible(false);
                        }
                    } else {
                        EditBookmarkDialog.this.setVisible(false);
                    }
                }
            });
        }
        return this.okCancelPanel;
    }

    protected void updateBookmark() {
        String bookmarkName = this.bookmarkNameTextField.getText().trim();
        String bookmarkDescription = this.bookmarkDescriptionTextArea.getText().trim();
        this.bookmark.setName(bookmarkName, false);
        this.bookmark.setDescription(bookmarkDescription);
        try {
            this.bookmark.setLocalization(this.wktReader.read(this.localizationTextArea.getText().trim()));
        }
        catch (ParseException e) {
            LOGGER.error((Object)"", (Throwable)e);
            this.bookmark.setLocalization(null);
        }
        if (this.bookmark instanceof TemporalBookmark) {
            if (this.customDateOption.isSelected()) {
                ((TemporalBookmark)this.bookmark).setTimemark(this.timestampPanel.getTimestamp());
            } else {
                ((TemporalBookmark)this.bookmark).setTimemark(null);
            }
        }
    }

    protected boolean isInputValid() {
        Geometry geom = null;
        try {
            geom = this.wktReader.read(this.localizationTextArea.getText().trim());
        }
        catch (ParseException parseException) {
            // empty catch block
        }
        return this.bookmarkNameTextField.getInputVerifier().verify(this.bookmarkNameTextField) && geom != null;
    }

    @Override
    public void setVisible(boolean visible) {
        if (visible) {
            this.okCancelPanel.setOKPressed(false);
        }
        super.setVisible(visible);
    }

    public boolean wasOkPressed() {
        return this.okCancelPanel.wasOKPressed();
    }

    private void loadCurrentViewParameters() {
        LayerViewPanel panel = JUMPWorkbench.getFrameInstance().getContext().getLayerViewPanel();
        if (panel != null) {
            this.currentViewEnvelope = panel.getViewport().getEnvelopeInModelCoordinates();
            Collection<Feature> col = panel.getSelectionManager().getFeaturesWithSelectedItems();
            if (col.size() > 0) {
                ArrayList<Geometry> geometries = new ArrayList<Geometry>();
                for (Feature currentFeature : col) {
                    geometries.add(currentFeature.getGeometry());
                }
                GeometryFactory gf = new GeometryFactory();
                Geometry[] geomBuffers = new Geometry[geometries.size()];
                geometries.toArray(geomBuffers);
                this.selectedGeometries = gf.createGeometryCollection(geomBuffers);
            }
        }
    }

    private void updateComponentsState() {
        this.currentViewEnvelopeOption.setEnabled(this.currentViewEnvelope != null);
        this.selectedFeaturesOption.setEnabled(this.selectedGeometries != null);
        this.localizationTextArea.setEditable(this.wktTextOption.isSelected());
    }
}


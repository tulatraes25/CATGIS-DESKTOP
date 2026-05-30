/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.log4j.Logger
 */
package org.saig.core.gui.swing.sldeditor.property.std;

import com.vividsolutions.jump.workbench.JUMPWorkbench;
import com.vividsolutions.jump.workbench.ui.plugin.PersistentBlackboardPlugIn;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.net.MalformedURLException;
import java.util.Iterator;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JTextField;
import org.apache.log4j.Logger;
import org.saig.core.gui.swing.sldeditor.property.ExternalGraphicEditor;
import org.saig.core.gui.swing.sldeditor.util.FormUtils;
import org.saig.core.gui.swing.sldeditor.util.ImageFilter;
import org.saig.core.styling.ExternalGraphic;
import org.saig.core.styling.ExternalGraphicImpl;
import org.saig.jump.lang.I18N;

public class DefaultExternalGraphicEditor
extends ExternalGraphicEditor {
    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = Logger.getLogger(DefaultExternalGraphicEditor.class);
    public static final String FILE_CHOOSER_DIRECTORY_KEY = String.valueOf(DefaultExternalGraphicEditor.class.getName()) + " - FILE_EXTERNAL_GRAPHIC_CHOOSER_DIRECTORY_KEY";
    private ExternalGraphic externalGraphic;
    private JLabel lblUrl;
    private JLabel lblMimeType;
    private JTextField txtUrl;
    private JComboBox cmbMimeType;
    private JButton btnOpen;

    public DefaultExternalGraphicEditor() {
        this(null);
    }

    public DefaultExternalGraphicEditor(ExternalGraphic externalGraphic) {
        this.setLayout(new GridBagLayout());
        this.lblUrl = new JLabel(I18N.getString("org.saig.core.gui.swing.sldeditor.property.std.DefaultExternalGraphicEditor.image-location"));
        this.txtUrl = new JTextField("12345678901234567890");
        this.txtUrl.setPreferredSize(this.txtUrl.getPreferredSize());
        this.txtUrl.setText("");
        this.btnOpen = new JButton("...");
        this.btnOpen.setToolTipText(I18N.getString("org.saig.core.gui.swing.sldeditor.property.std.DefaultExternalGraphicEditor.open"));
        this.btnOpen.setPreferredSize(FormUtils.getButtonDimension());
        this.btnOpen.addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent event) {
                DefaultExternalGraphicEditor.this.openFileDialog();
            }
        });
        this.lblMimeType = new JLabel(I18N.getString("org.saig.core.gui.swing.sldeditor.property.std.DefaultExternalGraphicEditor.image-type"));
        this.cmbMimeType = new JComboBox<String>(this.getMimeTypes());
        FormUtils.addRowInGBL((JComponent)this, 0, 0, this.lblUrl, (JComponent)this.txtUrl);
        FormUtils.addRowInGBL((JComponent)this, 0, 2, (JComponent)this.btnOpen, false, true);
        FormUtils.addRowInGBL((JComponent)this, 1, 0, this.lblMimeType, (JComponent)this.cmbMimeType);
        FormUtils.addFiller(this, 2, 0);
        this.setExternalGraphic(externalGraphic);
    }

    @Override
    public void setExternalGraphic(ExternalGraphic externalGraphic) {
        if (externalGraphic == null) {
            this.txtUrl.setText("");
            this.cmbMimeType.setSelectedIndex(0);
            return;
        }
        try {
            this.txtUrl.setText(externalGraphic.getLocation().toString());
            this.cmbMimeType.setSelectedItem(externalGraphic.getFormat());
            this.externalGraphic = externalGraphic;
        }
        catch (MalformedURLException e) {
            LOGGER.error((Object)I18N.getString("org.saig.core.gui.swing.sldeditor.property.std.DefaultExternalGraphicEditor.illegal-url-in-external-graphic"), (Throwable)e);
        }
    }

    @Override
    public ExternalGraphic getExternalGraphic() {
        this.externalGraphic = new ExternalGraphicImpl();
        this.externalGraphic.setURI(this.txtUrl.getText());
        if (this.cmbMimeType.getSelectedIndex() != -1) {
            this.externalGraphic.setFormat((String)this.cmbMimeType.getSelectedItem());
        }
        return this.externalGraphic;
    }

    private void openFileDialog() {
        int retval;
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileFilter(new ImageFilter());
        if (PersistentBlackboardPlugIn.get(JUMPWorkbench.getFrameInstance().getContext()).get(FILE_CHOOSER_DIRECTORY_KEY) != null) {
            fileChooser.setCurrentDirectory(new File((String)PersistentBlackboardPlugIn.get(JUMPWorkbench.getFrameInstance().getContext()).get(FILE_CHOOSER_DIRECTORY_KEY)));
        }
        if ((retval = fileChooser.showOpenDialog(this)) == 0) {
            PersistentBlackboardPlugIn.get(JUMPWorkbench.getFrameInstance().getContext()).put(FILE_CHOOSER_DIRECTORY_KEY, fileChooser.getCurrentDirectory().toString());
            File f = fileChooser.getSelectedFile();
            this.txtUrl.setText(f.toURI().toString());
            String extension = ImageFilter.getExtension(f);
            String mime = null;
            Iterator<ImageReader> itReaders = ImageIO.getImageReadersBySuffix(extension);
            if (itReaders.hasNext()) {
                ImageReader reader = itReaders.next();
                mime = reader.getOriginatingProvider().getMIMETypes()[0];
            }
            if (mime != null) {
                this.cmbMimeType.setSelectedItem(mime);
            } else {
                this.cmbMimeType.setSelectedIndex(0);
            }
        }
    }

    private String[] getMimeTypes() {
        String[] mimes = ImageIO.getReaderMIMETypes();
        String[] types = new String[mimes.length + 1];
        types[0] = "<" + I18N.getString("org.saig.core.gui.swing.sldeditor.property.std.DefaultExternalGraphicEditor.unknow-type") + ">";
        System.arraycopy(mimes, 0, types, 1, mimes.length);
        return types;
    }
}


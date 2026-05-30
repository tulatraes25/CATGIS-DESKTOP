/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.media.jai.JAI
 *  javax.media.jai.RenderedOp
 *  org.apache.log4j.Logger
 */
package org.saig.jump.plugin.view;

import com.vividsolutions.jump.task.TaskMonitor;
import com.vividsolutions.jump.util.FileUtil;
import com.vividsolutions.jump.workbench.JUMPWorkbench;
import com.vividsolutions.jump.workbench.WorkbenchContext;
import com.vividsolutions.jump.workbench.plugin.AbstractPlugIn;
import com.vividsolutions.jump.workbench.plugin.EnableCheck;
import com.vividsolutions.jump.workbench.plugin.EnableCheckFactory;
import com.vividsolutions.jump.workbench.plugin.MultiEnableCheck;
import com.vividsolutions.jump.workbench.plugin.PlugInContext;
import com.vividsolutions.jump.workbench.plugin.ThreadedPlugIn;
import com.vividsolutions.jump.workbench.ui.GUIUtil;
import com.vividsolutions.jump.workbench.ui.LayerViewPanel;
import com.vividsolutions.jump.workbench.ui.images.IconLoader;
import es.kosmo.core.dao.coverage.WorldFileHandler;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.image.renderable.ParameterBlock;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import javax.media.jai.JAI;
import javax.media.jai.RenderedOp;
import javax.swing.Icon;
import javax.swing.JFileChooser;
import org.apache.log4j.Logger;
import org.saig.core.util.ImageUtils;
import org.saig.jump.lang.I18N;
import org.saig.jump.widgets.util.DialogFactory;

public class ViewPlugIn
extends AbstractPlugIn
implements ThreadedPlugIn {
    private static final Logger LOGGER = Logger.getLogger(ViewPlugIn.class);
    public static final String NAME = I18N.getString("org.saig.jump.widgets.view.ViewPlugIn.save-view-to-file");
    public static final Icon ICON = IconLoader.icon("Camera.gif");
    protected File file;
    protected Map<String, String> extensions = new HashMap<String, String>();
    public static final String JPEG_EXTENSION = "JPEG";
    public static final String BMP_EXTENSION = "BMP";
    public static final String TIF_EXTENSION = "TIFF";
    public static final String PNG_EXTENSION = "PNG";

    public ViewPlugIn() {
        this.extensions.put("JPG", JPEG_EXTENSION);
        this.extensions.put(JPEG_EXTENSION, JPEG_EXTENSION);
        this.extensions.put(BMP_EXTENSION, BMP_EXTENSION);
        this.extensions.put("TIF", TIF_EXTENSION);
        this.extensions.put(TIF_EXTENSION, TIF_EXTENSION);
        this.extensions.put(PNG_EXTENSION, PNG_EXTENSION);
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public Icon getIcon() {
        return ICON;
    }

    @Override
    public boolean execute(PlugInContext context) throws Exception {
        this.reportNothingToUndoYet(context);
        JFileChooser fileChooser = GUIUtil.createJFileChooserWithOverwritePrompting();
        fileChooser.setFileFilter(GUIUtil.createFileFilter(I18N.getString("org.saig.jump.widgets.view.ViewPlugIn.image-files"), new String[]{"jpg", "tif", "jpeg", "tiff", "png", "bmp"}));
        fileChooser.setDialogTitle(I18N.getString("org.saig.jump.widgets.view.ViewPlugIn.save-view"));
        int option = fileChooser.showSaveDialog(JUMPWorkbench.getFrameInstance());
        if (option == 0) {
            this.file = fileChooser.getSelectedFile();
            return true;
        }
        return false;
    }

    @Override
    public void run(TaskMonitor monitor, PlugInContext context) throws Exception {
        this.file = FileUtil.addExtensionIfNone(this.file, "jpg");
        String extension = FileUtil.getExtension(this.file);
        LayerViewPanel viewPanel = context.getLayerViewPanel();
        try {
            OutputStream out;
            String trueExtension = this.extensions.get(extension.toUpperCase());
            BufferedImage image = null;
            image = trueExtension.equals(PNG_EXTENSION) || trueExtension.equals(TIF_EXTENSION) ? new BufferedImage(viewPanel.getWidth(), viewPanel.getHeight(), 2) : new BufferedImage(viewPanel.getWidth(), viewPanel.getHeight(), 1);
            Graphics2D graphics = (Graphics2D)image.getGraphics();
            graphics.setBackground(Color.white);
            graphics.fillRect(0, 0, viewPanel.getWidth(), viewPanel.getHeight());
            viewPanel.getRenderingManager().copyTo(graphics);
            if (trueExtension.equalsIgnoreCase(PNG_EXTENSION)) {
                out = null;
                try {
                    long time = System.currentTimeMillis();
                    int compressionLevel = -1;
                    out = new FileOutputStream(this.file);
                    ImageUtils.writeBufferedImageAsPNG(out, image, true, compressionLevel);
                    LOGGER.debug((Object)I18N.getMessage("org.saig.jump.plugin.view.ViewPlugin.elapsed-time-{0}-ms-while-creating-the-image-{1}-{2}-x-{3}-with-compression-level-{4}-{5}-kb", new Object[]{System.currentTimeMillis() - time, this.file.getAbsolutePath(), image.getWidth(), image.getHeight(), compressionLevel, this.file.length() / 1024L}));
                }
                finally {
                    if (out != null) {
                        out.close();
                    }
                }
            } else if (trueExtension.equalsIgnoreCase(JPEG_EXTENSION)) {
                out = null;
                try {
                    out = new FileOutputStream(this.file);
                    ImageUtils.writeBufferedImageAsJPEG(out, 1.0f, image);
                }
                finally {
                    if (out != null) {
                        out.close();
                    }
                }
            } else {
                ParameterBlock pb = new ParameterBlock();
                pb.addSource(image);
                pb.add(this.file.getAbsolutePath());
                pb.add(trueExtension);
                RenderedOp op = JAI.create((String)"filestore", (ParameterBlock)pb);
                op.dispose();
            }
            WorldFileHandler handler = new WorldFileHandler(this.file.getAbsolutePath(), true);
            handler.writeWorldFile(viewPanel.getViewport().getEnvelopeInModelCoordinates(), image.getWidth(), image.getHeight());
        }
        catch (Exception e) {
            DialogFactory.showErrorDialog(JUMPWorkbench.getFrameInstance(), String.valueOf(I18N.getString("org.saig.jump.widgets.view.ViewPlugIn.an-error-has-been-produced-while-saving-the-file")) + ".\n" + I18N.getMessage("org.saig.jump.widgets.view.ViewPlugIn.the-error-is-{0}", new Object[]{e.getMessage()}), I18N.getString("org.saig.jump.widgets.view.ViewPlugIn.error"));
            return;
        }
        DialogFactory.showInformationDialog(JUMPWorkbench.getFrameInstance(), I18N.getMessage("org.saig.jump.widgets.view.ViewPlugIn.the-file-{0}-has-been-created-successfully", new Object[]{this.file.getAbsolutePath()}), I18N.getString("org.saig.jump.widgets.view.ViewPlugIn.image-file-created"));
    }

    public static MultiEnableCheck createEnableCheck(WorkbenchContext workbenchContext) {
        EnableCheckFactory checkFactory = new EnableCheckFactory(workbenchContext);
        return new MultiEnableCheck().add(checkFactory.createTaskWindowMustBeActiveCheck());
    }

    @Override
    public EnableCheck getCheck() {
        return ViewPlugIn.createEnableCheck(JUMPWorkbench.getFrameInstance().getContext());
    }
}


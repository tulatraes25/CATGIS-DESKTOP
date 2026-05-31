package ar.com.catgis.layout;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetAdapter;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.List;
import javax.imageio.ImageIO;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

public class CanvasDropTarget {

    public interface DropHandler {
        void onImageDropped(BufferedImage image, double mmX, double mmY);
        void onFileDropped(File file, double mmX, double mmY);
    }

    public static void install(JComponent canvas, DropHandler handler,
                                double dpi, double scale,
                                java.util.function.Supplier<Integer> pageXSupplier,
                                java.util.function.Supplier<Integer> pageYSupplier) {
        DropTarget dt = new DropTarget(canvas, new DropTargetAdapter() {
            @Override
            public void drop(DropTargetDropEvent e) {
                try {
                    e.acceptDrop(DnDConstants.ACTION_COPY);
                    Transferable tr = e.getTransferable();

                    double mmPerPx = 25.4 / dpi / scale;
                    int pageX = pageXSupplier.get();
                    int pageY = pageYSupplier.get();
                    double mmX = (e.getLocation().x - pageX) * mmPerPx;
                    double mmY = (e.getLocation().y - pageY) * mmPerPx;

                    // Try image flavor first
                    if (tr.isDataFlavorSupported(DataFlavor.imageFlavor)) {
                        BufferedImage img = (BufferedImage) tr.getTransferData(DataFlavor.imageFlavor);
                        if (img != null) {
                            handler.onImageDropped(img, mmX, mmY);
                            e.dropComplete(true);
                            return;
                        }
                    }

                    // Try file list flavor
                    if (tr.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
                        @SuppressWarnings("unchecked")
                        List<File> files = (List<File>) tr.getTransferData(DataFlavor.javaFileListFlavor);
                        if (files != null && !files.isEmpty()) {
                            for (File file : files) {
                                String name = file.getName().toLowerCase();
                                if (name.endsWith(".png") || name.endsWith(".jpg") || name.endsWith(".jpeg")
                                        || name.endsWith(".gif") || name.endsWith(".bmp") || name.endsWith(".tif")
                                        || name.endsWith(".tiff")) {
                                    try {
                                        BufferedImage img = ImageIO.read(file);
                                        if (img != null) {
                                            handler.onImageDropped(img, mmX, mmY);
                                            continue;
                                        }
                                    } catch (Exception ex) { ex.printStackTrace(); }
                                }
                                handler.onFileDropped(file, mmX, mmY);
                            }
                            e.dropComplete(true);
                            return;
                        }
                    }

                    e.dropComplete(false);
                } catch (Exception ex) {
                    ex.printStackTrace();
                    e.dropComplete(false);
                }
            }
        });
        canvas.setDropTarget(dt);
    }
}

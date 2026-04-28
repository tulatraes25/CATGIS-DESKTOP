package ar.com.catgis;

import javax.swing.JComponent;
import javax.swing.TransferHandler;
import java.awt.datatransfer.DataFlavor;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public final class FileDropSupport {

    private FileDropSupport() {
    }

    public static void install(JComponent... components) {
        if (components == null) {
            return;
        }
        TransferHandler handler = new TransferHandler() {
            @Override
            public boolean canImport(TransferSupport support) {
                return support != null && support.isDataFlavorSupported(DataFlavor.javaFileListFlavor);
            }

            @Override
            public boolean importData(TransferSupport support) {
                if (!canImport(support)) {
                    return false;
                }
                try {
                    Object data = support.getTransferable().getTransferData(DataFlavor.javaFileListFlavor);
                    if (!(data instanceof List<?> list)) {
                        return false;
                    }
                    List<File> files = new ArrayList<>();
                    for (Object item : list) {
                        if (item instanceof File file) {
                            files.add(file);
                        }
                    }
                    if (files.isEmpty()) {
                        return false;
                    }
                    return OpenFileAction.openDroppedFiles(files.toArray(new File[0]), CatgisDesktopApp.getMainFrameSafe());
                } catch (Exception ex) {
                    AppErrorSupport.logFailure("No se pudieron importar archivos por arrastre", ex);
                    return false;
                }
            }
        };

        for (JComponent component : components) {
            if (component != null) {
                component.setTransferHandler(handler);
            }
        }
    }
}

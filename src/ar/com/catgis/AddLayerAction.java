package ar.com.catgis;

import javax.swing.AbstractAction;
import java.awt.event.ActionEvent;

public class AddLayerAction extends AbstractAction {

    @Override
    public void actionPerformed(ActionEvent e) {
        openLayer();
    }

    public static void openLayer() {
        OpenFileAction.openFile();
    }
}

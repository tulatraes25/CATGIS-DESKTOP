package ar.com.catgis;

import javax.swing.AbstractAction;
import java.awt.event.ActionEvent;

public class ProOpenDatasetAction extends AbstractAction {

    @Override
    public void actionPerformed(ActionEvent e) {
        openDialog();
    }

    public static void openDialog() {
        ProOpenDatasetDialog.open(CatgisDesktopApp.getMainFrameSafe());
    }
}

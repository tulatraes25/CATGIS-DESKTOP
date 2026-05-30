/*
 * Decompiled with CFR 0.152.
 */
package org.saig.jump.plugin.sdi.wms;

import com.vividsolutions.jump.workbench.JUMPWorkbench;
import com.vividsolutions.jump.workbench.WorkbenchContext;
import com.vividsolutions.jump.workbench.plugin.AbstractPlugIn;
import com.vividsolutions.jump.workbench.plugin.MultiEnableCheck;
import com.vividsolutions.jump.workbench.plugin.PlugInContext;
import com.vividsolutions.jump.workbench.ui.images.IconLoader;
import es.kosmo.desktop.widgets.sdi.wms.WMSConfigDialog;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import javax.swing.Icon;
import org.saig.jump.lang.I18N;
import org.saig.jump.widgets.util.DialogFactory;

public class UploadProjectToWMSServerPlugIn
extends AbstractPlugIn {
    public static final String NAME = I18N.getString("org.saig.jump.plugin.wms.UploadProjectToWMSServerPlugIn.Configure-WMS-server");
    public static final Icon ICON = IconLoader.icon("Palette.gif");

    @Override
    public boolean execute(PlugInContext context) {
        WMSConfigDialog dialog = new WMSConfigDialog(JUMPWorkbench.getFrameInstance(), true);
        if (dialog.isExitOk()) {
            this.sendRequest(dialog.getURLPath(), dialog.getFilePath());
        }
        return true;
    }

    private void sendRequest(String urlServer, String filePath) {
        block7: {
            HttpURLConnection conn = null;
            DataOutputStream dos = null;
            DataInputStream inStream = null;
            String lineEnd = "\r\n";
            String twoHyphens = "--";
            String boundary = "*****";
            int maxBufferSize = 0x100000;
            try {
                FileInputStream fileInputStream = new FileInputStream(new File(filePath));
                URL url = new URL(urlServer);
                conn = (HttpURLConnection)url.openConnection();
                conn.setDoInput(true);
                conn.setDoOutput(true);
                conn.setUseCaches(false);
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Connection", "Keep-Alive");
                conn.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
                dos = new DataOutputStream(conn.getOutputStream());
                dos.writeBytes(String.valueOf(twoHyphens) + boundary + lineEnd);
                dos.writeBytes("Content-Disposition: form-data; name=\"upload\"; filename=\"" + filePath + "\"" + lineEnd);
                dos.writeBytes(lineEnd);
                int bytesAvailable = fileInputStream.available();
                int bufferSize = Math.min(bytesAvailable, maxBufferSize);
                byte[] buffer = new byte[bufferSize];
                int bytesRead = fileInputStream.read(buffer, 0, bufferSize);
                while (bytesRead > 0) {
                    dos.write(buffer, 0, bufferSize);
                    bytesAvailable = fileInputStream.available();
                    bufferSize = Math.min(bytesAvailable, maxBufferSize);
                    bytesRead = fileInputStream.read(buffer, 0, bufferSize);
                }
                dos.writeBytes(lineEnd);
                dos.writeBytes(String.valueOf(twoHyphens) + boundary + twoHyphens + lineEnd);
                fileInputStream.close();
                dos.flush();
                dos.close();
            }
            catch (MalformedURLException ex) {
                DialogFactory.showErrorDialog(JUMPWorkbench.getFrameInstance(), I18N.getMessage("org.saig.jump.plugin.wms.UploadProjectToWMSServerPlugIn.The-address-{0}-is-not-well-formed", new Object[]{urlServer}), I18N.getString("org.saig.jump.plugin.wms.UploadProjectToWMSServerPlugIn.Error"));
                return;
            }
            catch (IOException ioe) {
                DialogFactory.showErrorDialog(JUMPWorkbench.getFrameInstance(), I18N.getMessage("org.saig.jump.plugin.wms.UploadProjectToWMSServerPlugIn.An-IO-error-was-produced-Error-description-is-{0}-{1}", new Object[]{":\n", ioe.getMessage()}), I18N.getString("org.saig.jump.plugin.wms.UploadProjectToWMSServerPlugIn.Error"));
                return;
            }
            try {
                String str;
                inStream = new DataInputStream(conn.getInputStream());
                BufferedReader d = new BufferedReader(new InputStreamReader(inStream));
                while ((str = d.readLine()) != null) {
                    System.out.println("Server response is: " + str);
                    System.out.println("");
                }
                inStream.close();
            }
            catch (IOException ioex) {
                String msg = ioex.getMessage();
                if (msg.indexOf("500") == -1) break block7;
                DialogFactory.showErrorDialog(JUMPWorkbench.getFrameInstance(), I18N.getString("org.saig.jump.plugin.wms.UploadProjectToWMSServerPlugIn.Load-failed"), I18N.getString("org.saig.jump.plugin.wms.UploadProjectToWMSServerPlugIn.Error"));
                return;
            }
        }
        DialogFactory.showInformationDialog(JUMPWorkbench.getFrameInstance(), I18N.getString("org.saig.jump.plugin.wms.UploadProjectToWMSServerPlugIn.Load-successfully-finished"), I18N.getString("org.saig.jump.plugin.wms.UploadProjectToWMSServerPlugIn.Success"));
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public Icon getIcon() {
        return ICON;
    }

    public static MultiEnableCheck createEnableCheck(WorkbenchContext workbenchContext) {
        MultiEnableCheck solucion = new MultiEnableCheck();
        return solucion;
    }
}


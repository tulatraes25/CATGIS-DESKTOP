/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.commons.lang.StringUtils
 *  org.apache.log4j.Logger
 */
package com.vividsolutions.jump.util;

import com.vividsolutions.jump.workbench.JUMPWorkbench;
import com.vividsolutions.jump.workbench.ui.plugin.PersistentBlackboardPlugIn;
import com.vividsolutions.jump.workbench.ui.task.TaskMonitorDialog;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import javax.swing.JComponent;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.saig.jump.lang.I18N;
import org.saig.jump.widgets.config.ConfigPathPanel;
import org.saig.jump.widgets.util.DialogFactory;

public class FileUtil {
    private static final Logger LOGGER = Logger.getLogger(FileUtil.class);
    private static final int NUMBER_OF_RETRIES = 10;

    public static List<String> getContents(String textFileName) throws FileNotFoundException, IOException {
        ArrayList<String> contents = new ArrayList<String>();
        FileReader fileReader = new FileReader(textFileName);
        BufferedReader bufferedReader = null;
        try {
            bufferedReader = new BufferedReader(fileReader);
            String line = bufferedReader.readLine();
            while (line != null) {
                contents.add(line);
                line = bufferedReader.readLine();
            }
        }
        finally {
            if (bufferedReader != null) {
                bufferedReader.close();
            }
        }
        return contents;
    }

    public static void setContents(String textFileName, String contents) throws IOException {
        FileWriter fileWriter = new FileWriter(textFileName, false);
        BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
        bufferedWriter.write(contents);
        bufferedWriter.flush();
        bufferedWriter.close();
        fileWriter.close();
    }

    public static List<String> getContents(InputStream inputStream) throws IOException {
        ArrayList<String> contents = new ArrayList<String>();
        InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
        try {
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            try {
                String line = bufferedReader.readLine();
                while (line != null) {
                    contents.add(line);
                    line = bufferedReader.readLine();
                }
            }
            finally {
                bufferedReader.close();
            }
        }
        finally {
            inputStreamReader.close();
        }
        return contents;
    }

    public static void setContents(String textFileName, List<String> lines) throws IOException {
        String contents = "";
        for (String line : lines) {
            contents = String.valueOf(contents) + line + System.getProperty("line.separator");
        }
        FileUtil.setContents(textFileName, contents);
    }

    public static void zip(Collection<File> files, File zipFile) throws IOException {
        FileOutputStream fos = new FileOutputStream(zipFile);
        try {
            BufferedOutputStream bos = new BufferedOutputStream(fos);
            try {
                ZipOutputStream zos = new ZipOutputStream(bos);
                try {
                    block15: for (File file : files) {
                        zos.putNextEntry(new ZipEntry(file.getName()));
                        FileInputStream fis = new FileInputStream(file);
                        try {
                            BufferedInputStream bis = new BufferedInputStream(fis);
                            try {
                                while (true) {
                                    int j;
                                    if ((j = bis.read()) == -1) {
                                        continue block15;
                                    }
                                    zos.write(j);
                                }
                            }
                            finally {
                                bis.close();
                            }
                        }
                        finally {
                            fis.close();
                            zos.closeEntry();
                        }
                    }
                }
                finally {
                    zos.close();
                }
            }
            finally {
                bos.close();
            }
        }
        finally {
            fos.close();
        }
    }

    public static String getExtension(File f) {
        String ext = "";
        String s = f.getName();
        int i = s.lastIndexOf(46);
        if (i > 0 && i < s.length() - 1) {
            ext = s.substring(i + 1).toLowerCase();
        }
        return ext;
    }

    public static String getExtension(String filename) {
        String ext = "";
        int i = filename.lastIndexOf(46);
        if (i > 0 && i < filename.length() - 1) {
            ext = filename.substring(i + 1).toLowerCase();
        }
        return ext;
    }

    public static String addExtensionIfNone(String fileName, String extension) {
        File tempFile = new File(fileName);
        tempFile = FileUtil.addExtensionIfNone(tempFile, extension);
        return tempFile.getAbsolutePath();
    }

    public static File addExtensionIfNone(File file, String extension) {
        if (FileUtil.getExtension(file).length() > 0) {
            return file;
        }
        String path = file.getAbsolutePath();
        if (!path.endsWith(".")) {
            path = String.valueOf(path) + ".";
        }
        path = String.valueOf(path) + extension;
        return new File(path);
    }

    public static String uniqueTempFileName(String name, String ext) {
        File temp = null;
        try {
            File defaultFilePath;
            while (name.length() < 3) {
                name = String.valueOf(name) + "x";
            }
            String defaultPath = (String)PersistentBlackboardPlugIn.get(JUMPWorkbench.getBlackboard()).get(ConfigPathPanel.TEMP_FILES_PATH_KEY);
            String suffix = "";
            if (ext != null) {
                suffix = "." + ext;
            }
            temp = !StringUtils.isEmpty((String)defaultPath) ? ((defaultFilePath = new File(defaultPath)).exists() ? File.createTempFile(name, suffix, defaultFilePath) : File.createTempFile(name, suffix)) : File.createTempFile(name, suffix);
            if (temp != null) {
                String tempName = temp.getAbsolutePath();
                temp.delete();
                return tempName;
            }
        }
        catch (IOException e) {
            LOGGER.error((Object)"", (Throwable)e);
        }
        return null;
    }

    public static boolean canOverwrite(JComponent parent, File file) {
        if (file.canWrite()) {
            int resultado = DialogFactory.showYesNoDialog(parent, I18N.getMessage("com.vividsolutions.jump.util.FileUtil.the-selected-file-{0}-already-exists-do-you-want-to-overwrite-it", new Object[]{file.getName()}), I18N.getString("com.vividsolutions.jump.util.FileUtil.overwrite-alert"));
            return resultado == 0;
        }
        if (file.exists()) {
            DialogFactory.showErrorDialog(parent, I18N.getMessage("com.vividsolutions.jump.util.FileUtil.the-selected-file-{0}-can-not-be-written-permission-denied", new Object[]{file.getName()}), I18N.getString("com.vividsolutions.jump.util.FileUtil.error-checking-write-rights"));
            return false;
        }
        return true;
    }

    public static String nameWithoutExtension(String name) {
        int dotPosition = name.lastIndexOf(46);
        return dotPosition < 0 ? name : name.substring(0, dotPosition);
    }

    public static String nameWithoutExtension(File file) {
        return FileUtil.nameWithoutExtension(file.getAbsolutePath());
    }

    public static void copyDirectory(File srcDir, File dstDir) throws IOException {
        if (srcDir.isDirectory()) {
            if (!dstDir.exists()) {
                dstDir.mkdir();
            }
            String[] children = srcDir.list();
            int i = 0;
            while (i < children.length) {
                FileUtil.copyDirectory(new File(srcDir, children[i]), new File(dstDir, children[i]));
                ++i;
            }
        } else {
            FileUtil.copy(srcDir, dstDir);
        }
    }

    public static void copy(File src, File dst) throws IOException {
        int len;
        FileOutputStream out = null;
        int retries = 0;
        boolean hecho = false;
        while (!hecho) {
            try {
                out = new FileOutputStream(dst);
                hecho = true;
            }
            catch (IOException e) {
                System.gc();
                if (retries++ == 10) {
                    LOGGER.warn((Object)I18N.getString("com.vividsolutions.jump.util.FileUtil.max-retries-number-reached-the-exception-will-be-thrown"));
                    throw e;
                }
                LOGGER.debug((Object)I18N.getMessage("com.vividsolutions.jump.util.FileUtil.the-file-{0}-can-not-be-written-Wait-one-second", new Object[]{dst.getAbsolutePath()}));
                try {
                    Thread.sleep(1000L);
                }
                catch (InterruptedException e1) {
                    LOGGER.error((Object)"", (Throwable)e1);
                }
            }
        }
        FileInputStream in = new FileInputStream(src);
        byte[] buf = new byte[1024];
        while ((len = ((InputStream)in).read(buf)) > 0) {
            ((OutputStream)out).write(buf, 0, len);
        }
        ((InputStream)in).close();
        ((OutputStream)out).close();
    }

    public static File createTemporalFile(String name, String ext) {
        String tempFileName = FileUtil.uniqueTempFileName(name, ext);
        if (tempFileName == null) {
            return null;
        }
        File file = new File(tempFileName);
        file.deleteOnExit();
        return file;
    }

    public static String addValidExtension(String fileName, String extension) {
        File tempFile = FileUtil.addValidExtension(new File(fileName), extension);
        return tempFile.getAbsolutePath();
    }

    public static File addValidExtension(File file, String extension) {
        File tempFile = null;
        String ext = FileUtil.getExtension(file).toLowerCase();
        if (ext.equals(extension.toLowerCase())) {
            String path = file.getAbsolutePath();
            path = String.valueOf(path.substring(0, path.length() - extension.length())) + extension;
            tempFile = new File(path);
        } else {
            String path = file.getAbsolutePath();
            if (!path.endsWith(".")) {
                path = String.valueOf(path) + ".";
            }
            path = String.valueOf(path) + extension;
            tempFile = new File(path);
        }
        return tempFile;
    }

    public static String parseISToStringUTF8(InputStream is) throws UnsupportedEncodingException {
        StringBuffer sb;
        block13: {
            sb = new StringBuffer();
            BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
            try {
                try {
                    String line = null;
                    while ((line = reader.readLine()) != null) {
                        sb.append(String.valueOf(line) + "\n");
                    }
                }
                catch (Exception ex) {
                    ex.getMessage();
                    try {
                        is.close();
                    }
                    catch (Exception exception) {}
                    break block13;
                }
            }
            catch (Throwable throwable) {
                try {
                    is.close();
                }
                catch (Exception exception) {
                    // empty catch block
                }
                throw throwable;
            }
            try {
                is.close();
            }
            catch (Exception exception) {
                // empty catch block
            }
        }
        String bom = FileUtil.getBOM("UTF-8");
        if (sb.toString().startsWith(bom)) {
            int index = sb.toString().indexOf(bom);
            return sb.toString().substring(index + bom.length());
        }
        return sb.toString();
    }

    public static String getBOM(String enc) throws UnsupportedEncodingException {
        if ("UTF-8".equals(enc)) {
            byte[] bom = new byte[]{-17, -69, -65};
            return new String(bom, enc);
        }
        if ("UTF-16BE".equals(enc)) {
            byte[] bom = new byte[]{-2, -1};
            return new String(bom, enc);
        }
        if ("UTF-16LE".equals(enc)) {
            byte[] bom = new byte[]{-1, -2};
            return new String(bom, enc);
        }
        if ("UTF-32BE".equals(enc)) {
            byte[] bom = new byte[]{0, 0, -2, -1};
            return new String(bom, enc);
        }
        if ("UTF-32LE".equals(enc)) {
            byte[] bom = new byte[]{0, 0, -1, -2};
            return new String(bom, enc);
        }
        return null;
    }

    public static String convertPathToSystemIndependentPath(String oldPath) {
        return StringUtils.replaceChars((String)oldPath, (char)'\\', (char)'/');
    }

    public static File findEnd(String str, File[] files, String[] ends) {
        int i = 0;
        while (i < ends.length) {
            String currentEnd = ends[i];
            int j = 0;
            while (j < files.length) {
                File currenFile = files[j];
                if (currenFile.getAbsolutePath().endsWith(currentEnd)) {
                    return currenFile;
                }
                ++j;
            }
            ++i;
        }
        return new File(String.valueOf(str.substring(0, str.length() - 3)) + ends[0]);
    }

    public static String createUniqueFileName(String directory, String fileBaseName, String ext) {
        File directoryFile;
        String result = null;
        if (StringUtils.isEmpty((String)directory)) {
            File tempFile = FileUtil.createTemporalFile(fileBaseName, ext);
            directory = tempFile.getParent();
        }
        if ((directoryFile = new File(directory)).canRead()) {
            File temp = null;
            try {
                temp = File.createTempFile(fileBaseName, "." + ext, directoryFile);
            }
            catch (IOException e) {
                LOGGER.error((Object)"", (Throwable)e);
            }
            if (temp != null) {
                result = temp.getAbsolutePath();
                temp.delete();
            }
        }
        return result;
    }

    public static String calculateMd5(String path) {
        return FileUtil.calculateMd5(path, null);
    }

    /*
     * Loose catch block
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    public static String calculateMd5(String path, TaskMonitorDialog progressDialog) {
        String output = null;
        try {
            try {
                MessageDigest digest = MessageDigest.getInstance("MD5");
                File f = new File(path);
                FileInputStream is = new FileInputStream(f);
                byte[] buffer = new byte[8192];
                int read = 0;
                int cont = 0;
                long length = f.length();
                try {
                    try {
                        boolean isCancelled = false;
                        while (!isCancelled && (read = ((InputStream)is).read(buffer)) > 0) {
                            digest.update(buffer, 0, read);
                            if (progressDialog == null) continue;
                            progressDialog.report((int)((long)(cont += read) / 1000L), (int)(length / 1000L), I18N.getString("com.vividsolutions.jump.util.FileUtil.analyzed-kbytes"));
                            isCancelled = progressDialog.isCancelRequested();
                        }
                        if (isCancelled) return output;
                        byte[] md5sum = digest.digest();
                        BigInteger bigInt = new BigInteger(1, md5sum);
                        output = bigInt.toString(16).toUpperCase();
                        return output;
                    }
                    catch (IOException e) {
                        throw new RuntimeException(I18N.getString("com.vividsolutions.jump.util.FileUtil.Unable-to-process-the-file-for-the-MD5-algorithm"), e);
                    }
                }
                finally {
                    try {
                        ((InputStream)is).close();
                    }
                    catch (IOException e) {
                        throw new RuntimeException(I18N.getString("com.vividsolutions.jump.util.FileUtil.Unable-to-close-the-input-stream-for-the-MD5-algorithm"), e);
                    }
                }
            }
            catch (NoSuchAlgorithmException e) {
                LOGGER.error((Object)"", (Throwable)e);
                if (progressDialog == null) return output;
                progressDialog.setVisible(false);
                return output;
            }
            catch (FileNotFoundException e) {
                LOGGER.error((Object)"", (Throwable)e);
                if (progressDialog == null) return output;
                progressDialog.setVisible(false);
                return output;
            }
            catch (Exception e) {
                LOGGER.error((Object)"", (Throwable)e);
                progressDialog.setExceptionMessage(e.getMessage());
                if (progressDialog == null) return output;
                {
                    catch (Throwable throwable) {
                        throw throwable;
                    }
                }
                progressDialog.setVisible(false);
                return output;
            }
        }
        finally {
            if (progressDialog != null) {
                progressDialog.setVisible(false);
            }
        }
    }
}


package ar.com.catgis;

import javax.swing.JFileChooser;
import java.io.File;
import java.util.prefs.Preferences;

public final class FileChooserSupport {

    private static final Preferences ROOT = Preferences.userNodeForPackage(FileChooserSupport.class).node("file-chooser");
    private static final String GLOBAL_KEY = "_global";

    private FileChooserSupport() {
    }

    public static JFileChooser createChooser(String key, String title) {
        JFileChooser chooser = new JFileChooser(resolveInitialDirectory(key));
        chooser.setDialogTitle(title);
        return chooser;
    }

    public static void rememberSelection(String key, JFileChooser chooser) {
        if (chooser == null) {
            return;
        }
        File selectedFile = chooser.getSelectedFile();
        if (selectedFile != null) {
            rememberFile(key, selectedFile);
            return;
        }
        rememberDirectory(key, chooser.getCurrentDirectory());
    }

    public static void rememberFile(String key, File file) {
        if (file == null) {
            return;
        }
        File directory = file.isDirectory() ? file : file.getParentFile();
        rememberDirectory(key, directory);
    }

    public static File resolveSuggestedFile(String key, File suggestedFile) {
        if (suggestedFile == null) {
            return null;
        }
        if (suggestedFile.isAbsolute() || suggestedFile.getParentFile() != null) {
            return suggestedFile;
        }
        return new File(resolveInitialDirectory(key), suggestedFile.getName());
    }

    private static File resolveInitialDirectory(String key) {
        File specific = readDirectory(key);
        if (specific != null) {
            return specific;
        }
        File global = readDirectory(GLOBAL_KEY);
        if (global != null) {
            return global;
        }
        String userHome = System.getProperty("user.home");
        return userHome == null || userHome.isBlank() ? new File(".") : new File(userHome);
    }

    private static File readDirectory(String key) {
        String stored = ROOT.get(normalizeKey(key), "").trim();
        if (stored.isEmpty()) {
            return null;
        }
        File file = new File(stored);
        return file.isDirectory() ? file : null;
    }

    private static void rememberDirectory(String key, File directory) {
        if (directory == null || !directory.isDirectory()) {
            return;
        }
        String path = directory.getAbsolutePath();
        ROOT.put(normalizeKey(key), path);
        ROOT.put(GLOBAL_KEY, path);
    }

    private static String normalizeKey(String key) {
        return key == null || key.isBlank() ? GLOBAL_KEY : key.trim().toLowerCase();
    }
}

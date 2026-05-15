package ar.com.catgis;

import javax.swing.Icon;
import javax.swing.JFileChooser;
import javax.swing.UIManager;
import javax.swing.filechooser.FileSystemView;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.prefs.Preferences;

public final class FileChooserSupport {

    private static final Preferences ROOT = Preferences.userNodeForPackage(FileChooserSupport.class).node("file-chooser");
    private static final String GLOBAL_KEY = "_global";
    private static final FileSystemView NATIVE_FILE_SYSTEM_VIEW = FileSystemView.getFileSystemView();

    private FileChooserSupport() {
    }

    public static JFileChooser createChooser(String key, String title) {
        File initialDirectory = resolveInitialDirectory(key);
        try {
            JFileChooser chooser = new JFileChooser(initialDirectory);
            chooser.setDialogTitle(title);
            if (initialDirectory != null && initialDirectory.isDirectory()) {
                chooser.setCurrentDirectory(initialDirectory);
            }
            return chooser;
        } catch (RuntimeException ex) {
            JFileChooser chooser = new JFileChooser(initialDirectory, new SafeFileSystemView(initialDirectory));
            chooser.setDialogTitle(title);
            if (initialDirectory != null && initialDirectory.isDirectory()) {
                chooser.setCurrentDirectory(initialDirectory);
            }
            return chooser;
        }
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

    static File resolveInitialDirectoryHint(String key) {
        return resolveInitialDirectory(key);
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

    private static final class SafeFileSystemView extends FileSystemView {
        private final File fallbackDirectory;

        private SafeFileSystemView(File fallbackDirectory) {
            this.fallbackDirectory = sanitizeDirectory(fallbackDirectory);
        }

        @Override
        public File createNewFolder(File containingDir) throws IOException {
            File parent = sanitizeDirectory(containingDir);
            File folder = new File(parent, "Nueva carpeta");
            int suffix = 2;
            while (folder.exists()) {
                folder = new File(parent, "Nueva carpeta " + suffix);
                suffix++;
            }
            if (!folder.mkdirs()) {
                throw new IOException("No se pudo crear la carpeta: " + folder.getAbsolutePath());
            }
            return folder;
        }

        @Override
        public File getDefaultDirectory() {
            return fallbackDirectory;
        }

        @Override
        public File getHomeDirectory() {
            return fallbackDirectory;
        }

        @Override
        public File[] getRoots() {
            File[] roots = File.listRoots();
            if (roots != null && roots.length > 0) {
                return roots;
            }
            return new File[]{fallbackDirectory};
        }

        @Override
        public boolean isRoot(File file) {
            if (file == null) {
                return false;
            }
            for (File root : getRoots()) {
                if (root != null && root.equals(file)) {
                    return true;
                }
            }
            return false;
        }

        @Override
        public Boolean isTraversable(File file) {
            return file != null && file.isDirectory();
        }

        @Override
        public String getSystemDisplayName(File file) {
            if (file == null) {
                return "";
            }
            try {
                String displayName = NATIVE_FILE_SYSTEM_VIEW.getSystemDisplayName(file);
                if (displayName != null && !displayName.isBlank()) {
                    return displayName;
                }
            } catch (RuntimeException ignored) {
            }
            String name = file.getName();
            return name == null || name.isBlank() ? file.getPath() : name;
        }

        @Override
        public String getSystemTypeDescription(File file) {
            if (file == null) {
                return null;
            }
            try {
                String description = NATIVE_FILE_SYSTEM_VIEW.getSystemTypeDescription(file);
                if (description != null && !description.isBlank()) {
                    return description;
                }
            } catch (RuntimeException ignored) {
            }
            return file.isDirectory() ? "Carpeta" : "Archivo";
        }

        @Override
        public Icon getSystemIcon(File file) {
            if (file != null && file.isDirectory()) {
                return AppIcons.openIcon();
            }
            try {
                Icon icon = NATIVE_FILE_SYSTEM_VIEW.getSystemIcon(file);
                if (icon != null) {
                    return icon;
                }
            } catch (RuntimeException ignored) {
            }
            return UIManager.getIcon(file != null && file.isDirectory() ? "FileView.directoryIcon" : "FileView.fileIcon");
        }

        @Override
        public boolean isParent(File folder, File file) {
            return folder != null && file != null && folder.equals(file.getParentFile());
        }

        @Override
        public File getChild(File parent, String fileName) {
            if (fileName == null || fileName.isBlank()) {
                return null;
            }
            return parent == null ? new File(fileName) : new File(parent, fileName);
        }

        @Override
        public boolean isFileSystem(File file) {
            return true;
        }

        @Override
        public boolean isHiddenFile(File file) {
            return file != null && file.isHidden();
        }

        @Override
        public boolean isFileSystemRoot(File dir) {
            return isRoot(dir);
        }

        @Override
        public boolean isDrive(File dir) {
            return dir != null && isRoot(dir);
        }

        @Override
        public boolean isFloppyDrive(File dir) {
            return false;
        }

        @Override
        public boolean isComputerNode(File dir) {
            return false;
        }

        @Override
        public File[] getFiles(File dir, boolean useFileHiding) {
            File directory = sanitizeDirectory(dir);
            File[] files = directory.listFiles();
            if (files == null) {
                return new File[0];
            }
            if (!useFileHiding) {
                return files;
            }
            List<File> visibleFiles = new ArrayList<>();
            for (File file : files) {
                if (file != null && !file.isHidden()) {
                    visibleFiles.add(file);
                }
            }
            return visibleFiles.toArray(File[]::new);
        }

        @Override
        public File getParentDirectory(File dir) {
            File parent = dir != null ? dir.getParentFile() : null;
            return parent != null ? parent : fallbackDirectory;
        }

        @Override
        public File createFileObject(File dir, String filename) {
            return dir == null ? new File(filename) : new File(dir, filename);
        }

        @Override
        public File createFileObject(String path) {
            return new File(path);
        }

        private static File sanitizeDirectory(File directory) {
            if (directory != null && directory.isDirectory()) {
                return directory;
            }
            String userHome = System.getProperty("user.home");
            File home = (userHome == null || userHome.isBlank()) ? null : new File(userHome);
            if (home != null && home.isDirectory()) {
                return home;
            }
            File current = new File(".").getAbsoluteFile();
            return current.isDirectory() ? current : new File("C:\\");
        }
    }
}

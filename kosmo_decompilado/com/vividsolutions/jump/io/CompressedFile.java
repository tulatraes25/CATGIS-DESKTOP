/*
 * Decompiled with CFR 0.152.
 */
package com.vividsolutions.jump.io;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class CompressedFile {
    public static String getInternalZipFnameByExtension(String extension, String compressedFile) throws Exception {
        FileInputStream IS_low = new FileInputStream(compressedFile);
        ZipInputStream fr_high = new ZipInputStream(IS_low);
        ZipEntry entry = fr_high.getNextEntry();
        while (entry != null) {
            String inside_zip_extension = entry.getName().substring(entry.getName().length() - extension.length());
            if (inside_zip_extension.compareToIgnoreCase(extension) == 0) {
                return entry.getName();
            }
            entry = fr_high.getNextEntry();
        }
        return null;
    }

    static InputStream openFileExtension(String extension, String compressedFile) throws Exception {
        if (compressedFile == null || compressedFile.length() == 0) {
            throw new Exception("openFileExtension- no compressed file given.");
        }
        String compressed_extension = compressedFile.substring(compressedFile.length() - 3);
        if (compressed_extension.compareToIgnoreCase(".gz") == 0) {
            FileInputStream IS_low = new FileInputStream(compressedFile);
            return new GZIPInputStream(IS_low);
        }
        if (compressed_extension.compareToIgnoreCase("zip") == 0) {
            FileInputStream IS_low = new FileInputStream(compressedFile);
            ZipInputStream fr_high = new ZipInputStream(IS_low);
            ZipEntry entry = fr_high.getNextEntry();
            while (entry != null) {
                String inside_zip_extension = entry.getName().substring(entry.getName().length() - extension.length());
                if (inside_zip_extension.compareToIgnoreCase(extension) == 0) {
                    return fr_high;
                }
                entry = fr_high.getNextEntry();
            }
            throw new Exception("couldnt find file with extension" + extension + " in compressed file " + compressedFile);
        }
        throw new Exception("couldnt determine compressed file type for file " + compressedFile + "- should end in .zip or .gz");
    }

    public static InputStream openFile(String fname, String compressedFile) throws Exception {
        if (compressedFile == null || compressedFile.length() == 0) {
            return new FileInputStream(fname);
        }
        String extension = compressedFile.substring(compressedFile.length() - 3);
        if (extension.compareToIgnoreCase(".gz") == 0) {
            FileInputStream IS_low = new FileInputStream(compressedFile);
            return new GZIPInputStream(IS_low);
        }
        if (extension.compareToIgnoreCase("zip") == 0) {
            FileInputStream IS_low = new FileInputStream(compressedFile);
            ZipInputStream fr_high = new ZipInputStream(IS_low);
            ZipEntry entry = fr_high.getNextEntry();
            while (entry != null) {
                if (entry.getName().compareToIgnoreCase(fname) == 0) {
                    return fr_high;
                }
                entry = fr_high.getNextEntry();
            }
            throw new Exception("couldnt find " + fname + " in compressed file " + compressedFile);
        }
        throw new Exception("couldnt determine compressed file type for file " + compressedFile + "- should end in .zip or .gz");
    }
}


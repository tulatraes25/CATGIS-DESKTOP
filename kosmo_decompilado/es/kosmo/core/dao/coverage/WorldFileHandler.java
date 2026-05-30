/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vividsolutions.jts.geom.Envelope
 *  org.apache.log4j.Logger
 */
package es.kosmo.core.dao.coverage;

import com.vividsolutions.jts.geom.Envelope;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.apache.log4j.Logger;
import org.saig.jump.lang.I18N;

public class WorldFileHandler {
    private static final Logger LOGGER = Logger.getLogger(WorldFileHandler.class);
    protected String worldFileName = null;
    protected String imageFileName = null;
    protected boolean useTFWExtensionIfPossible = true;

    public WorldFileHandler(String imageFileName, boolean allwaysLookForTFWExtension) {
        this.imageFileName = imageFileName;
        this.useTFWExtensionIfPossible = allwaysLookForTFWExtension;
        this.worldFileName = this.isWorldFileExistentForImage();
        if (this.worldFileName == null) {
            this.worldFileName = this.createListOfWorldFileNamesForImage().get(0);
        }
    }

    public boolean writeWorldFile(Envelope imageCoordinates, int imgWidth, int imgHeight) {
        double maxx = imageCoordinates.getMaxX();
        double minx = imageCoordinates.getMinX();
        double maxy = imageCoordinates.getMaxY();
        double miny = imageCoordinates.getMinY();
        try {
            FileWriter worldfileWriter = new FileWriter(this.worldFileName, false);
            double faktorA = (maxx - minx) / (double)imgWidth;
            double faktorB = 0.0;
            double faktorC = 0.0;
            double faktorD = (miny - maxy) / (double)imgHeight;
            double CoordX = minx;
            double CoordY = maxy;
            worldfileWriter.write(String.valueOf(Double.toString(faktorA)) + "\n");
            worldfileWriter.write(String.valueOf(Double.toString(faktorB)) + "\n");
            worldfileWriter.write(String.valueOf(Double.toString(faktorC)) + "\n");
            worldfileWriter.write(String.valueOf(Double.toString(faktorD)) + "\n");
            worldfileWriter.write(String.valueOf(Double.toString(CoordX + faktorA / 2.0)) + "\n");
            worldfileWriter.write(Double.toString(CoordY + faktorD / 2.0));
            worldfileWriter.close();
        }
        catch (IOException e) {
            LOGGER.error((Object)I18N.getMessage(this.getClass(), "pirolPlugIns.plugIns.PirolRasterImage.WorldFileHandler.world-file-was-not-written-{0}", new Object[]{e.getMessage()}));
            return false;
        }
        LOGGER.debug((Object)I18N.getMessage("pirolPlugIns.plugIns.PirolRasterImage.WorldFileHandler.world-file-{0}-was-written", new Object[]{this.worldFileName}));
        return true;
    }

    public boolean writeWorldFile(Envelope imageCoordinates, int imgWidth, int imgHeight, double imgResolution) {
        double minx = imageCoordinates.getMinX();
        double miny = imageCoordinates.getMinY();
        double maxy = miny + (double)imgHeight * imgResolution;
        try {
            FileWriter worldfileWriter = new FileWriter(this.worldFileName, false);
            double faktorA = imgResolution;
            double faktorB = 0.0;
            double faktorC = 0.0;
            double faktorD = -imgResolution;
            double CoordX = minx;
            double CoordY = maxy;
            worldfileWriter.write(String.valueOf(Double.toString(faktorA)) + "\n");
            worldfileWriter.write(String.valueOf(Double.toString(faktorB)) + "\n");
            worldfileWriter.write(String.valueOf(Double.toString(faktorC)) + "\n");
            worldfileWriter.write(String.valueOf(Double.toString(faktorD)) + "\n");
            worldfileWriter.write(String.valueOf(Double.toString(CoordX + faktorA / 2.0)) + "\n");
            worldfileWriter.write(Double.toString(CoordY + faktorD / 2.0));
            worldfileWriter.close();
        }
        catch (IOException e) {
            LOGGER.error((Object)I18N.getMessage("pirolPlugIns.plugIns.PirolRasterImage.WorldFileHandler.world-file-was-not-written-{0}", new Object[]{e.getMessage()}));
            return false;
        }
        LOGGER.debug((Object)I18N.getMessage("pirolPlugIns.plugIns.PirolRasterImage.WorldFileHandler.world-file-{0}-was-written", new Object[]{this.worldFileName}));
        return true;
    }

    public Envelope readWorldFile(int imgWidth, int imgHeight) {
        double CoordY;
        double CoordX;
        double faktorD;
        double faktorC;
        double faktorB;
        double faktorA;
        FileReader worldFileReader;
        try {
            worldFileReader = new FileReader(this.worldFileName);
        }
        catch (FileNotFoundException e1) {
            LOGGER.error((Object)(String.valueOf(I18N.getString("pirolPlugIns.plugIns.PirolRasterImage.WorldFileHandler.worldfile-not-found-{0}")) + e1.getMessage()));
            return null;
        }
        BufferedReader bufferedWorldFileReader = null;
        try {
            try {
                bufferedWorldFileReader = new BufferedReader(worldFileReader);
                faktorA = Double.parseDouble(bufferedWorldFileReader.readLine());
                faktorB = Double.parseDouble(bufferedWorldFileReader.readLine());
                faktorC = Double.parseDouble(bufferedWorldFileReader.readLine());
                faktorD = Double.parseDouble(bufferedWorldFileReader.readLine());
                CoordX = Double.parseDouble(bufferedWorldFileReader.readLine());
                CoordY = Double.parseDouble(bufferedWorldFileReader.readLine());
            }
            catch (Exception e) {
                LOGGER.error((Object)I18N.getMessage("pirolPlugIns.plugIns.PirolRasterImage.WorldFileHandler.can-not-read-worldfile-{0}", new Object[]{e.getMessage()}));
                if (bufferedWorldFileReader != null) {
                    try {
                        bufferedWorldFileReader.close();
                    }
                    catch (IOException e2) {
                        LOGGER.error((Object)e2);
                    }
                }
                return null;
            }
        }
        finally {
            if (bufferedWorldFileReader != null) {
                try {
                    bufferedWorldFileReader.close();
                }
                catch (IOException e) {
                    LOGGER.error((Object)e);
                }
            }
        }
        double minx = faktorA * 0.0 + faktorC * 0.0 + CoordX;
        double maxy = faktorB * 0.0 + faktorD * 0.0 + CoordY;
        double maxx = faktorA * (double)imgWidth + faktorC * (double)imgHeight + CoordX;
        double miny = faktorB * (double)imgWidth + faktorD * (double)imgHeight + CoordY;
        return new Envelope(minx, maxx, miny, maxy);
    }

    protected List<String> createListOfWorldFileNamesForImage() {
        String worldFileName = this.imageFileName.substring(0, this.imageFileName.lastIndexOf("."));
        String imageExtension = this.imageFileName.substring(this.imageFileName.lastIndexOf(".") + 1).toLowerCase();
        ArrayList<String> possibleWorldFileNames = new ArrayList<String>();
        possibleWorldFileNames.add(String.valueOf(worldFileName) + "." + imageExtension.substring(0, 1) + imageExtension.substring(imageExtension.length() - 1) + "w");
        possibleWorldFileNames.add((String.valueOf(worldFileName) + "." + imageExtension.substring(0, 1) + imageExtension.substring(imageExtension.length() - 1) + "w").toUpperCase());
        possibleWorldFileNames.add(String.valueOf(worldFileName) + "." + imageExtension + "w");
        possibleWorldFileNames.add(String.valueOf(worldFileName) + "." + (String.valueOf(imageExtension) + "w").toUpperCase());
        if (this.useTFWExtensionIfPossible) {
            possibleWorldFileNames.add(String.valueOf(worldFileName) + ".tfw");
            possibleWorldFileNames.add(String.valueOf(worldFileName) + ".tfw".toUpperCase());
        }
        return possibleWorldFileNames;
    }

    public String isWorldFileExistentForImage() {
        List<String> possibleWorldFileNames = this.createListOfWorldFileNamesForImage();
        File worldFile = null;
        int i = 0;
        while (i < possibleWorldFileNames.size()) {
            String wfName = possibleWorldFileNames.get(i);
            worldFile = new File(wfName);
            LOGGER.debug((Object)I18N.getMessage("pirolPlugIns.plugIns.PirolRasterImage.WorldFileHandler.checking-for-world-file-named-{0}", new Object[]{wfName}));
            if (worldFile.exists()) {
                this.worldFileName = wfName;
                return this.worldFileName;
            }
            ++i;
        }
        return null;
    }

    public boolean isUseTFWExtensionIfPossible() {
        return this.useTFWExtensionIfPossible;
    }

    public void setUseTFWExtensionIfPossible(boolean allwaysLookForTFWExtension) {
        this.useTFWExtensionIfPossible = allwaysLookForTFWExtension;
    }

    public String getImageFileName() {
        return this.imageFileName;
    }

    public void setImageFileName(String imageFileName) {
        this.imageFileName = imageFileName;
    }

    public String getWorldFileName() {
        return this.worldFileName;
    }
}


/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.log4j.Logger
 *  org.cresques.cts.ICoordTrans
 *  org.cresques.cts.IProjection
 *  org.cresques.geo.Projected
 *  org.cresques.io.DataSource
 *  org.cresques.io.GeoFile
 *  org.cresques.io.ZipFileFolder
 *  org.cresques.px.Extent
 *  org.cresques.px.IObjList
 */
package org.cresques.io;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Date;
import java.util.Hashtable;
import java.util.Vector;
import org.apache.log4j.Logger;
import org.cresques.cts.ICoordTrans;
import org.cresques.cts.IProjection;
import org.cresques.geo.Projected;
import org.cresques.io.DataSource;
import org.cresques.io.DxfGroup;
import org.cresques.io.DxfGroupVector;
import org.cresques.io.GeoFile;
import org.cresques.io.ZipFileFolder;
import org.cresques.px.Extent;
import org.cresques.px.IObjList;
import org.cresques.px.dxf.DxfEntityMaker;
import org.cresques.px.dxf.DxfHeaderManager;
import org.cresques.px.dxf.DxfHeaderVariables;

public class DxfFile
extends GeoFile {
    private static final Logger LOGGER = Logger.getLogger(DxfFile.class);
    private boolean cadFlag = true;
    long lineNr = 0L;
    String buf = null;
    BufferedReader fi;
    long l = 0L;
    int count = 0;
    DxfGroup grp = null;
    EntityFactory entityMaker = null;
    VarSettings headerManager;
    private boolean dxf3DFlag;

    public DxfFile(IProjection proj, String name, EntityFactory maker) {
        super(proj, name);
        this.entityMaker = maker;
        this.headerManager = new DxfHeaderManager();
    }

    public DxfFile(IProjection proj, String name, EntityFactory maker, VarSettings dxfVars) {
        super(proj, name);
        this.entityMaker = maker;
        this.headerManager = dxfVars;
    }

    public GeoFile load() {
        LOGGER.debug((Object)("Dxf: Cargando " + this.name + " ..."));
        try {
            if (ZipFileFolder.isUrl((String)this.name)) {
                ZipFileFolder zFolder = new ZipFileFolder(this.name);
                InputStream is = zFolder.getInputStream(this.name);
                return this.load(new InputStreamReader(is));
            }
            return this.load(new FileReader(this.name));
        }
        catch (Exception e) {
            LOGGER.error((Object)"", (Throwable)e);
            return null;
        }
    }

    public GeoFile load(Reader fr) throws Exception {
        LOGGER.debug((Object)("Dxf: Cargando '" + this.name + "' ..."));
        this.fi = new BufferedReader(fr);
        while ((this.grp = this.readGrp()) != null) {
            this.l += 2L;
            if (this.grp.equals(0, "EOF")) break;
            if (!this.grp.equals(0, "SECTION")) continue;
            this.readSection();
        }
        this.fi.close();
        this.extent.add(this.entityMaker.getExtent());
        LOGGER.debug((Object)("Dxf: '" + this.name + "' cargado. (" + this.l + " l\u00edneas)."));
        this.lineNr = this.l;
        return this;
    }

    private DxfGroup readGrp() throws NumberFormatException, IOException {
        DxfGroup g = DxfGroup.read(this.fi);
        if (g != null) {
            this.l += 2L;
        }
        return g;
    }

    private void readSection() throws NumberFormatException, Exception {
        do {
            this.grp = this.readGrp();
            LOGGER.debug((Object)("-1:" + this.grp));
            if (this.grp.code == 2) {
                if (((String)this.grp.data).compareTo("HEADER") == 0) {
                    this.readHeader();
                    continue;
                }
                if (((String)this.grp.data).compareTo("CLASSES") == 0) {
                    this.readAnySection();
                    continue;
                }
                if (((String)this.grp.data).compareTo("TABLES") == 0) {
                    this.readTables();
                    continue;
                }
                if (((String)this.grp.data).compareTo("BLOCKS") == 0) {
                    this.readBlocks();
                    continue;
                }
                if (((String)this.grp.data).compareTo("ENTITIES") == 0) {
                    this.readEntities();
                    continue;
                }
                if (((String)this.grp.data).compareTo("OBJECTS") == 0) {
                    this.readAnySection();
                    continue;
                }
                LOGGER.debug((Object)("DxfRead: Seccion " + this.grp.data));
                this.readAnySection();
                continue;
            }
            LOGGER.warn((Object)("Dxf: Codigo/Seccion desconocidos" + this.grp));
        } while (!this.grp.equals(0, "EOF") && !this.grp.equals(0, "ENDSEC"));
    }

    private void readTables() throws NumberFormatException, Exception {
        LOGGER.debug((Object)("Dxf: Seccion TABLAS, linea " + this.l + "grp =" + this.grp));
        int layerCnt = 0;
        String tableAct = "NONAME";
        Hashtable tables = new Hashtable();
        Vector<DxfGroupVector> table = new Vector<DxfGroupVector>();
        DxfGroupVector v = new DxfGroupVector();
        this.grp = this.readGrp();
        while (true) {
            if (this.grp.code == 0) {
                String data = (String)this.grp.getData();
                if (data.compareTo("ENDSEC") == 0 || data.compareTo("EOF") == 0) break;
                if (data.compareTo("ENDTAB") == 0) {
                    tables.put(tableAct, table);
                    table = new Vector();
                    this.grp = this.readGrp();
                    if (tableAct.compareTo("LAYER") != 0 || v.size() <= 0) continue;
                    this.entityMaker.createLayer(v);
                    LOGGER.debug((Object)("Dxf: Layer " + v.getDataAsString(2)));
                    ++layerCnt;
                    v.clear();
                    continue;
                }
                if (table.size() == 1) {
                    tableAct = v.getDataAsString(2);
                    LOGGER.debug((Object)("Dxf: Tabla " + tableAct));
                } else if (tableAct.compareTo("LAYER") == 0 && v.size() > 0) {
                    this.entityMaker.createLayer(v);
                    LOGGER.debug((Object)("Dxf: Layer " + v.getDataAsString(2)));
                    ++layerCnt;
                }
                v.clear();
                v.add(this.grp);
                while (true) {
                    this.grp = this.readGrp();
                    if (this.grp.code == 0) break;
                    v.add(this.grp);
                }
                table.add(v);
                continue;
            }
            LOGGER.warn((Object)"Dxf: Error de secuencia");
            this.grp = this.readGrp();
        }
        LOGGER.debug((Object)("Dxf: Seccion TABLAS: " + layerCnt + " Capas. "));
    }

    private void readAnySection() throws NumberFormatException, IOException {
        LOGGER.debug((Object)("Dxf: Seccion '" + (String)this.grp.getData() + "', linea " + this.l));
        do {
            this.grp = this.readGrp();
        } while (!this.grp.equals(0, "ENDSEC") && !this.grp.equals(0, "EOF"));
    }

    private void readHeader() throws NumberFormatException, Exception {
        LOGGER.debug((Object)("Dxf: Seccion HEADER, linea " + this.l));
        int variableCnt = 0;
        int cntVeces = 0;
        DxfGroupVector v = new DxfGroupVector();
        this.grp = this.readGrp();
        while (!this.grp.equals(0, "EOF")) {
            if (this.grp.code == 9 || this.grp.code == 0) {
                if (v.size() > 0) {
                    String lastVariable = (String)((DxfGroup)v.get((int)0)).data;
                    if (lastVariable.compareTo("$ACADVER") == 0) {
                        this.headerManager.setAcadVersion(v);
                    } else if (lastVariable.compareTo("$EXTMIN") == 0) {
                        if (v.hasCode(3)) {
                            this.headerManager.loadMinZFromHeader((Double)((DxfGroup)v.get((int)3)).data);
                        }
                    } else if (lastVariable.compareTo("$EXTMAX") == 0) {
                        if (v.hasCode(3)) {
                            this.headerManager.loadMaxZFromHeader((Double)((DxfGroup)v.get((int)3)).data);
                        }
                    } else if (lastVariable.compareTo("ENDSEC") == 0) break;
                }
                v.clear();
                v.add(this.grp);
                while (true) {
                    this.grp = this.readGrp();
                    if (this.grp.code == 9 || this.grp.code == 0) break;
                    v.add(this.grp);
                }
                ++variableCnt;
            }
            ++cntVeces;
        }
        LOGGER.debug((Object)("Dxf: Seccion HEADER, " + variableCnt + " variables, " + cntVeces + " veces."));
        LOGGER.debug((Object)("readHeader: ACAD Version: " + this.headerManager.getDxfHeaderVars().getAcadVersion()));
    }

    private void readEntities() throws NumberFormatException, Exception {
        LOGGER.debug((Object)("Dxf: Seccion ENTITIES, linea " + this.l));
        int entityCnt = 0;
        int unknownEntityCnt = 0;
        DxfGroupVector v = new DxfGroupVector();
        this.grp = this.readGrp();
        while (!this.grp.equals(0, "EOF")) {
            if (this.grp.code != 0) continue;
            if (v.size() > 0) {
                String lastEntity = (String)((DxfGroup)v.get((int)0)).data;
                if (lastEntity.compareTo("POLYLINE") == 0) {
                    this.entityMaker.createPolyline(v);
                } else if (lastEntity.compareTo("VERTEX") == 0) {
                    this.entityMaker.addVertex(v);
                } else if (lastEntity.compareTo("SEQEND") == 0) {
                    this.entityMaker.endSeq();
                } else if (lastEntity.compareTo("LWPOLYLINE") == 0) {
                    this.entityMaker.createLwPolyline(v);
                } else if (lastEntity.compareTo("LINE") == 0) {
                    this.entityMaker.createLine(v);
                } else if (lastEntity.compareTo("TEXT") == 0) {
                    this.entityMaker.createText(v);
                } else if (lastEntity.compareTo("MTEXT") == 0) {
                    this.entityMaker.createMText(v);
                } else if (lastEntity.compareTo("POINT") == 0) {
                    this.entityMaker.createPoint(v);
                } else if (lastEntity.compareTo("CIRCLE") == 0) {
                    this.entityMaker.createCircle(v);
                } else if (lastEntity.compareTo("ELLIPSE") == 0) {
                    this.entityMaker.createEllipse(v);
                } else if (lastEntity.compareTo("ARC") == 0) {
                    this.entityMaker.createArc(v);
                } else if (lastEntity.compareTo("INSERT") == 0) {
                    this.entityMaker.createInsert(v);
                } else if (lastEntity.compareTo("SOLID") == 0) {
                    this.entityMaker.createSolid(v);
                } else if (lastEntity.compareTo("SPLINE") == 0) {
                    this.entityMaker.createSpline(v);
                } else if (lastEntity.compareTo("ATTRIB") == 0) {
                    this.entityMaker.createAttrib(v);
                } else {
                    if (lastEntity.compareTo("ENDSEC") == 0) break;
                    LOGGER.debug((Object)("Dxf: Entidad " + lastEntity + " desconocida."));
                    ++unknownEntityCnt;
                }
            }
            v.clear();
            v.add(this.grp);
            while (true) {
                this.grp = this.readGrp();
                if (this.grp.code == 0) break;
                v.add(this.grp);
            }
            ++entityCnt;
        }
        LOGGER.info((Object)("DXF - ENTITIES section: " + entityCnt + " entities, " + unknownEntityCnt + " unknown"));
    }

    private void readBlocks() throws NumberFormatException, Exception {
        LOGGER.debug((Object)("Dxf: Seccion BLOCKS, linea " + this.l));
        int blkCnt = 0;
        int unknownEntityCnt = 0;
        DxfGroupVector v = new DxfGroupVector();
        this.grp = this.readGrp();
        while (!this.grp.equals(0, "EOF")) {
            if (this.grp.code != 0) continue;
            if (v.size() > 0) {
                String lastEntity = (String)((DxfGroup)v.get((int)0)).data;
                if (lastEntity.compareTo("BLOCK") == 0) {
                    this.entityMaker.createBlock(v);
                } else if (lastEntity.compareTo("POLYLINE") == 0) {
                    this.entityMaker.createPolyline(v);
                } else if (lastEntity.compareTo("VERTEX") == 0) {
                    this.entityMaker.addVertex(v);
                } else if (lastEntity.compareTo("SEQEND") == 0) {
                    this.entityMaker.endSeq();
                } else if (lastEntity.compareTo("LWPOLYLINE") == 0) {
                    this.entityMaker.createLwPolyline(v);
                } else if (lastEntity.compareTo("LINE") == 0) {
                    this.entityMaker.createLine(v);
                } else if (lastEntity.compareTo("TEXT") == 0) {
                    this.entityMaker.createText(v);
                } else if (lastEntity.compareTo("MTEXT") == 0) {
                    this.entityMaker.createMText(v);
                } else if (lastEntity.compareTo("POINT") == 0) {
                    this.entityMaker.createPoint(v);
                } else if (lastEntity.compareTo("CIRCLE") == 0) {
                    this.entityMaker.createCircle(v);
                } else if (lastEntity.compareTo("ARC") == 0) {
                    this.entityMaker.createArc(v);
                } else if (lastEntity.compareTo("INSERT") == 0) {
                    this.entityMaker.createInsert(v);
                } else if (lastEntity.compareTo("SOLID") == 0) {
                    this.entityMaker.createSolid(v);
                } else if (lastEntity.compareTo("SPLINE") == 0) {
                    this.entityMaker.createSpline(v);
                } else if (lastEntity.compareTo("ATTDEF") == 0) {
                    this.entityMaker.createAttdef(v);
                } else if (lastEntity.compareTo("ENDBLK") == 0) {
                    this.entityMaker.endBlk(v);
                } else {
                    if (lastEntity.compareTo("ENDSEC") == 0) break;
                    LOGGER.debug((Object)("Dxf: Entidad de bloque " + lastEntity + " desconocida."));
                    ++unknownEntityCnt;
                }
            }
            v.clear();
            v.add(this.grp);
            while (true) {
                this.grp = this.readGrp();
                if (this.grp.code == 0) break;
                v.add(this.grp);
            }
            ++blkCnt;
        }
        this.entityMaker.testBlocks();
        this.entityMaker.depureAttributes();
        LOGGER.info((Object)("Dxf - BLOCKS section: " + blkCnt + " block entities, " + unknownEntityCnt + " unknown"));
    }

    public IObjList getObjects() {
        return this.entityMaker.getObjects();
    }

    public void save(String fName) throws IOException {
        LOGGER.debug((Object)("save: fName = " + fName));
        long t1 = this.getTime();
        fName = DataSource.normalize((String)fName);
        FileWriter fw = new FileWriter(fName);
        fw.write(DxfGroup.toString(999, "TRANSLATION BY geo.cresques.io.DxfFile"));
        fw.write(DxfGroup.toString(999, "DATE : " + new Date().toString()));
        this.writeHeader(fw);
        this.writeTables(fw);
        this.writeBlocks(fw);
        this.writeEntities(fw);
        this.writeObjects(fw);
        fw.write(DxfGroup.toString(0, "EOF"));
        fw.flush();
        fw.close();
        long t2 = this.getTime();
        LOGGER.debug((Object)("DxfFile.save(): Tiempo salvando: " + (t2 - t1) / 1000L + " seg."));
    }

    public void writeHeader(FileWriter fw) throws IOException {
        fw.write(DxfGroup.toString(0, "SECTION"));
        fw.write(DxfGroup.toString(2, "HEADER"));
        fw.write(DxfGroup.toString(9, "$ACADVER"));
        fw.write(DxfGroup.toString(1, "AC1015"));
        fw.write(DxfGroup.toString(9, "$INSBASE"));
        fw.write(DxfGroup.toString(10, 0.0, 1));
        fw.write(DxfGroup.toString(20, 0.0, 1));
        fw.write(DxfGroup.toString(30, 0.0, 1));
        fw.write(DxfGroup.toString(9, "$EXTMIN"));
        fw.write(DxfGroup.toString(10, this.extent.minX(), 6));
        fw.write(DxfGroup.toString(20, this.extent.minY(), 6));
        if (this.dxf3DFlag) {
            fw.write(DxfGroup.toString(30, this.extent.minX(), 6));
        } else {
            fw.write(DxfGroup.toString(30, 0.0, 6));
        }
        fw.write(DxfGroup.toString(9, "$EXTMAX"));
        fw.write(DxfGroup.toString(10, this.extent.maxX(), 6));
        fw.write(DxfGroup.toString(20, this.extent.maxY(), 6));
        if (this.dxf3DFlag) {
            fw.write(DxfGroup.toString(30, this.extent.maxX(), 6));
        } else {
            fw.write(DxfGroup.toString(30, 0.0, 6));
        }
        fw.write(DxfGroup.toString(9, "$LIMMIN"));
        fw.write(DxfGroup.toString(10, this.extent.minX(), 6));
        fw.write(DxfGroup.toString(20, this.extent.minY(), 6));
        fw.write(DxfGroup.toString(9, "$LIMMAX"));
        fw.write(DxfGroup.toString(10, this.extent.maxX(), 6));
        fw.write(DxfGroup.toString(20, this.extent.maxY(), 6));
        fw.write(String.valueOf(DxfGroup.toString(9, "$ORTHOMODE")) + DxfGroup.toString(70, 0));
        fw.write(String.valueOf(DxfGroup.toString(9, "$REGENMODE")) + DxfGroup.toString(70, 1));
        fw.write(String.valueOf(DxfGroup.toString(9, "$FILLMODE")) + DxfGroup.toString(70, 1));
        fw.write(String.valueOf(DxfGroup.toString(9, "$QTEXTMODE")) + DxfGroup.toString(70, 0));
        fw.write(String.valueOf(DxfGroup.toString(9, "$MIRRTEXT")) + DxfGroup.toString(70, 1));
        fw.write(String.valueOf(DxfGroup.toString(9, "$DRAGMODE")) + DxfGroup.toString(70, 2));
        fw.write(String.valueOf(DxfGroup.toString(9, "$LTSCALE")) + DxfGroup.toString(40, 1.0, 1));
        fw.write(String.valueOf(DxfGroup.toString(9, "$OSMODE")) + DxfGroup.toString(70, 0));
        fw.write(String.valueOf(DxfGroup.toString(9, "$ATTMODE")) + DxfGroup.toString(70, 1));
        fw.write(String.valueOf(DxfGroup.toString(9, "$TEXTSIZE")) + DxfGroup.toString(40, 0.2, 1));
        fw.write(String.valueOf(DxfGroup.toString(9, "$TRACEWID")) + DxfGroup.toString(40, 0.05, 2));
        fw.write(String.valueOf(DxfGroup.toString(9, "$TEXTSTYLE")) + DxfGroup.toString(7, "STANDARD"));
        fw.write(String.valueOf(DxfGroup.toString(9, "$CLAYER")) + DxfGroup.toString(8, "0"));
        fw.write(String.valueOf(DxfGroup.toString(9, "$CELTYPE")) + DxfGroup.toString(6, "CONTINUOUS"));
        fw.write(String.valueOf(DxfGroup.toString(9, "$CECOLOR")) + DxfGroup.toString(62, 256));
        fw.write(String.valueOf(DxfGroup.toString(9, "$DIMSCALE")) + DxfGroup.toString(40, 1.0, 1));
        fw.write(String.valueOf(DxfGroup.toString(9, "$DIMASZ")) + DxfGroup.toString(40, 0.18, 2));
        fw.write(String.valueOf(DxfGroup.toString(9, "$DIMEXO")) + DxfGroup.toString(40, 0.0625, 4));
        fw.write(String.valueOf(DxfGroup.toString(9, "$DIMDLI")) + DxfGroup.toString(40, 0.38, 2));
        fw.write(String.valueOf(DxfGroup.toString(9, "$DIMRND")) + DxfGroup.toString(40, 0.0, 1));
        fw.write(String.valueOf(DxfGroup.toString(9, "$DIMDLE")) + DxfGroup.toString(40, 0.0, 1));
        fw.write(String.valueOf(DxfGroup.toString(9, "$DIMEXE")) + DxfGroup.toString(40, 0.18, 2));
        fw.write(String.valueOf(DxfGroup.toString(9, "$DIMTP")) + DxfGroup.toString(40, 0.0, 1));
        fw.write(String.valueOf(DxfGroup.toString(9, "$DIMTM")) + DxfGroup.toString(40, 0.0, 1));
        fw.write(String.valueOf(DxfGroup.toString(9, "$DIMTXT")) + DxfGroup.toString(40, 0.18, 2));
        fw.write(String.valueOf(DxfGroup.toString(9, "$DIMCEN")) + DxfGroup.toString(40, 0.09, 2));
        fw.write(String.valueOf(DxfGroup.toString(9, "$DIMTSZ")) + DxfGroup.toString(40, 0.0, 1));
        fw.write(String.valueOf(DxfGroup.toString(9, "$DIMTOL")) + DxfGroup.toString(70, 0));
        fw.write(String.valueOf(DxfGroup.toString(9, "$DIMLIM")) + DxfGroup.toString(70, 0));
        fw.write(String.valueOf(DxfGroup.toString(9, "$DIMTIH")) + DxfGroup.toString(70, 1));
        fw.write(String.valueOf(DxfGroup.toString(9, "$DIMTOH")) + DxfGroup.toString(70, 1));
        fw.write(String.valueOf(DxfGroup.toString(9, "$DIMSE1")) + DxfGroup.toString(70, 0));
        fw.write(String.valueOf(DxfGroup.toString(9, "$DIMSE2")) + DxfGroup.toString(70, 0));
        fw.write(String.valueOf(DxfGroup.toString(9, "$DIMTAD")) + DxfGroup.toString(70, 0));
        fw.write(String.valueOf(DxfGroup.toString(9, "$DIMZIN")) + DxfGroup.toString(70, 0));
        fw.write(String.valueOf(DxfGroup.toString(9, "$DIMBLK")) + DxfGroup.toString(1, ""));
        fw.write(String.valueOf(DxfGroup.toString(9, "$DIMASO")) + DxfGroup.toString(70, 1));
        fw.write(String.valueOf(DxfGroup.toString(9, "$DIMSHO")) + DxfGroup.toString(70, 1));
        fw.write(String.valueOf(DxfGroup.toString(9, "$DIMPOST")) + DxfGroup.toString(1, ""));
        fw.write(String.valueOf(DxfGroup.toString(9, "$DIMAPOST")) + DxfGroup.toString(1, ""));
        fw.write(String.valueOf(DxfGroup.toString(9, "$DIMALT")) + DxfGroup.toString(70, 0));
        fw.write(String.valueOf(DxfGroup.toString(9, "$DIMALTD")) + DxfGroup.toString(70, 2));
        fw.write(String.valueOf(DxfGroup.toString(9, "$DIMALTF")) + DxfGroup.toString(40, 25.4, 1));
        fw.write(String.valueOf(DxfGroup.toString(9, "$DIMLFAC")) + DxfGroup.toString(40, 1.0, 1));
        fw.write(String.valueOf(DxfGroup.toString(9, "$DIMTOFL")) + DxfGroup.toString(70, 0));
        fw.write(String.valueOf(DxfGroup.toString(9, "$DIMTVP")) + DxfGroup.toString(40, 0.0, 1));
        fw.write(String.valueOf(DxfGroup.toString(9, "$DIMTIX")) + DxfGroup.toString(70, 0));
        fw.write(String.valueOf(DxfGroup.toString(9, "$DIMSOXD")) + DxfGroup.toString(70, 0));
        fw.write(String.valueOf(DxfGroup.toString(9, "$DIMSAH")) + DxfGroup.toString(70, 0));
        fw.write(String.valueOf(DxfGroup.toString(9, "$DIMBLK1")) + DxfGroup.toString(1, ""));
        fw.write(String.valueOf(DxfGroup.toString(9, "$DIMBLK2")) + DxfGroup.toString(1, ""));
        fw.write(String.valueOf(DxfGroup.toString(9, "$DIMSTYLE")) + DxfGroup.toString(2, "STANDARD"));
        fw.write(String.valueOf(DxfGroup.toString(9, "$DIMCLRD")) + DxfGroup.toString(70, 0));
        fw.write(String.valueOf(DxfGroup.toString(9, "$DIMCLRE")) + DxfGroup.toString(70, 0));
        fw.write(String.valueOf(DxfGroup.toString(9, "$DIMCLRT")) + DxfGroup.toString(70, 0));
        fw.write(String.valueOf(DxfGroup.toString(9, "$DIMTFAC")) + DxfGroup.toString(40, 1.0, 1));
        fw.write(String.valueOf(DxfGroup.toString(9, "$DIMGAP")) + DxfGroup.toString(40, 0.09, 2));
        fw.write(String.valueOf(DxfGroup.toString(9, "$LUNITS")) + DxfGroup.toString(70, 2));
        fw.write(String.valueOf(DxfGroup.toString(9, "$LUPREC")) + DxfGroup.toString(70, 4));
        fw.write(String.valueOf(DxfGroup.toString(9, "$AXISMODE")) + DxfGroup.toString(70, 0));
        fw.write(DxfGroup.toString(9, "$AXISUNIT"));
        fw.write(DxfGroup.toString(10, 0.0, 1));
        fw.write(DxfGroup.toString(20, 0.0, 1));
        fw.write(String.valueOf(DxfGroup.toString(9, "$SKETCHINC")) + DxfGroup.toString(40, 0.1, 1));
        fw.write(String.valueOf(DxfGroup.toString(9, "$FILLETRAD")) + DxfGroup.toString(40, 0.0, 1));
        fw.write(String.valueOf(DxfGroup.toString(9, "$AUNITS")) + DxfGroup.toString(70, 0));
        fw.write(String.valueOf(DxfGroup.toString(9, "$AUPREC")) + DxfGroup.toString(70, 0));
        fw.write(String.valueOf(DxfGroup.toString(9, "$MENU")) + DxfGroup.toString(1, "acad"));
        fw.write(String.valueOf(DxfGroup.toString(9, "$ELEVATION")) + DxfGroup.toString(40, 0.0, 1));
        fw.write(String.valueOf(DxfGroup.toString(9, "$PELEVATION")) + DxfGroup.toString(40, 0.0, 1));
        fw.write(String.valueOf(DxfGroup.toString(9, "$THICKNESS")) + DxfGroup.toString(40, 0.0, 1));
        fw.write(String.valueOf(DxfGroup.toString(9, "$LIMCHECK")) + DxfGroup.toString(70, 0));
        fw.write(String.valueOf(DxfGroup.toString(9, "$BLIPMODE")) + DxfGroup.toString(70, 1));
        fw.write(String.valueOf(DxfGroup.toString(9, "$CHAMFERA")) + DxfGroup.toString(40, 0.0, 1));
        fw.write(String.valueOf(DxfGroup.toString(9, "$CHAMFERB")) + DxfGroup.toString(40, 0.0, 1));
        fw.write(String.valueOf(DxfGroup.toString(9, "$SKPOLY")) + DxfGroup.toString(70, 0));
        fw.write(String.valueOf(DxfGroup.toString(9, "$TDCREATE")) + DxfGroup.toString(40, 2453116.436828704, 9));
        fw.write(String.valueOf(DxfGroup.toString(9, "$TDUPDATE")) + DxfGroup.toString(40, 2453116.436828704, 9));
        fw.write(String.valueOf(DxfGroup.toString(9, "$TDINDWG")) + DxfGroup.toString(40, 0.0, 10));
        fw.write(String.valueOf(DxfGroup.toString(9, "$TDUSRTIMER")) + DxfGroup.toString(40, 0.0, 10));
        fw.write(String.valueOf(DxfGroup.toString(9, "$USRTIMER")) + DxfGroup.toString(70, 1));
        fw.write(String.valueOf(DxfGroup.toString(9, "$ANGBASE")) + DxfGroup.toString(50, 0.0, 1));
        fw.write(String.valueOf(DxfGroup.toString(9, "$ANGDIR")) + DxfGroup.toString(70, 0));
        fw.write(String.valueOf(DxfGroup.toString(9, "$PDMODE")) + DxfGroup.toString(70, 0));
        fw.write(String.valueOf(DxfGroup.toString(9, "$PDSIZE")) + DxfGroup.toString(40, 0.0, 1));
        fw.write(String.valueOf(DxfGroup.toString(9, "$PLINEWID")) + DxfGroup.toString(40, 0.0, 1));
        fw.write(String.valueOf(DxfGroup.toString(9, "$COORDS")) + DxfGroup.toString(70, 0));
        fw.write(String.valueOf(DxfGroup.toString(9, "$SPLFRAME")) + DxfGroup.toString(70, 0));
        fw.write(String.valueOf(DxfGroup.toString(9, "$SPLINETYPE")) + DxfGroup.toString(70, 6));
        fw.write(String.valueOf(DxfGroup.toString(9, "$SPLINESEGS")) + DxfGroup.toString(70, 10));
        fw.write(String.valueOf(DxfGroup.toString(9, "$ATTDIA")) + DxfGroup.toString(70, 0));
        fw.write(String.valueOf(DxfGroup.toString(9, "$ATTREQ")) + DxfGroup.toString(70, 1));
        fw.write(String.valueOf(DxfGroup.toString(9, "$HANDLING")) + DxfGroup.toString(70, 1));
        fw.write(String.valueOf(DxfGroup.toString(9, "$HANDSEED")) + DxfGroup.toString(5, "394B"));
        fw.write(String.valueOf(DxfGroup.toString(9, "$SURFTAB1")) + DxfGroup.toString(70, 6));
        fw.write(String.valueOf(DxfGroup.toString(9, "$SURFTAB2")) + DxfGroup.toString(70, 6));
        fw.write(String.valueOf(DxfGroup.toString(9, "$SURFTYPE")) + DxfGroup.toString(70, 6));
        fw.write(String.valueOf(DxfGroup.toString(9, "$SURFU")) + DxfGroup.toString(70, 6));
        fw.write(String.valueOf(DxfGroup.toString(9, "$SURFV")) + DxfGroup.toString(70, 6));
        fw.write(String.valueOf(DxfGroup.toString(9, "$UCSNAME")) + DxfGroup.toString(2, ""));
        fw.write(DxfGroup.toString(9, "$UCSORG"));
        fw.write(DxfGroup.toString(10, 0.0, 1));
        fw.write(DxfGroup.toString(20, 0.0, 1));
        fw.write(DxfGroup.toString(30, 0.0, 1));
        fw.write(DxfGroup.toString(9, "$UCSXDIR"));
        fw.write(DxfGroup.toString(10, 1.0, 1));
        fw.write(DxfGroup.toString(20, 0.0, 1));
        fw.write(DxfGroup.toString(30, 0.0, 1));
        fw.write(DxfGroup.toString(9, "$UCSYDIR"));
        fw.write(DxfGroup.toString(10, 0.0, 1));
        fw.write(DxfGroup.toString(20, 1.0, 1));
        fw.write(DxfGroup.toString(30, 0.0, 1));
        fw.write(String.valueOf(DxfGroup.toString(9, "$PUCSNAME")) + DxfGroup.toString(2, ""));
        fw.write(DxfGroup.toString(9, "$PUCSORG"));
        fw.write(DxfGroup.toString(10, 0.0, 1));
        fw.write(DxfGroup.toString(20, 0.0, 1));
        fw.write(DxfGroup.toString(30, 0.0, 1));
        fw.write(DxfGroup.toString(9, "$PUCSXDIR"));
        fw.write(DxfGroup.toString(10, 1.0, 1));
        fw.write(DxfGroup.toString(20, 0.0, 1));
        fw.write(DxfGroup.toString(30, 0.0, 1));
        fw.write(DxfGroup.toString(9, "$PUCSYDIR"));
        fw.write(DxfGroup.toString(10, 0.0, 1));
        fw.write(DxfGroup.toString(20, 1.0, 1));
        fw.write(DxfGroup.toString(30, 0.0, 1));
        fw.write(String.valueOf(DxfGroup.toString(9, "$USERI1")) + DxfGroup.toString(70, 0));
        fw.write(String.valueOf(DxfGroup.toString(9, "$USERI2")) + DxfGroup.toString(70, 0));
        fw.write(String.valueOf(DxfGroup.toString(9, "$USERI3")) + DxfGroup.toString(70, 0));
        fw.write(String.valueOf(DxfGroup.toString(9, "$USERI4")) + DxfGroup.toString(70, 0));
        fw.write(String.valueOf(DxfGroup.toString(9, "$USERI5")) + DxfGroup.toString(70, 0));
        fw.write(String.valueOf(DxfGroup.toString(9, "$USERR1")) + DxfGroup.toString(40, 0.0, 1));
        fw.write(String.valueOf(DxfGroup.toString(9, "$USERR2")) + DxfGroup.toString(40, 0.0, 1));
        fw.write(String.valueOf(DxfGroup.toString(9, "$USERR3")) + DxfGroup.toString(40, 0.0, 1));
        fw.write(String.valueOf(DxfGroup.toString(9, "$USERR4")) + DxfGroup.toString(40, 0.0, 1));
        fw.write(String.valueOf(DxfGroup.toString(9, "$USERR5")) + DxfGroup.toString(40, 0.0, 1));
        fw.write(String.valueOf(DxfGroup.toString(9, "$WORLDVIEW")) + DxfGroup.toString(70, 1));
        fw.write(String.valueOf(DxfGroup.toString(9, "$SHADEDGE")) + DxfGroup.toString(70, 3));
        fw.write(String.valueOf(DxfGroup.toString(9, "$SHADEDIF")) + DxfGroup.toString(70, 70));
        fw.write(String.valueOf(DxfGroup.toString(9, "$TILEMODE")) + DxfGroup.toString(70, 1));
        fw.write(String.valueOf(DxfGroup.toString(9, "$MAXACTVP")) + DxfGroup.toString(70, 16));
        fw.write(DxfGroup.toString(9, "$PINSBASE"));
        fw.write(DxfGroup.toString(10, 0.0, 1));
        fw.write(DxfGroup.toString(20, 0.0, 1));
        fw.write(DxfGroup.toString(30, 0.0, 1));
        fw.write(String.valueOf(DxfGroup.toString(9, "$PLIMCHECK")) + DxfGroup.toString(70, 0));
        fw.write(DxfGroup.toString(9, "$PEXTMIN"));
        fw.write(DxfGroup.toString(10, "-1.000000E+20"));
        fw.write(DxfGroup.toString(20, "-1.000000E+20"));
        fw.write(DxfGroup.toString(30, "-1.000000E+20"));
        fw.write(DxfGroup.toString(9, "$PEXTMAX"));
        fw.write(DxfGroup.toString(10, "-1.000000E+20"));
        fw.write(DxfGroup.toString(20, "-1.000000E+20"));
        fw.write(DxfGroup.toString(30, "-1.000000E+20"));
        fw.write(DxfGroup.toString(9, "$PLIMMIN"));
        fw.write(DxfGroup.toString(10, 0.0, 1));
        fw.write(DxfGroup.toString(20, 0.0, 1));
        fw.write(DxfGroup.toString(9, "$PLIMMAX"));
        fw.write(DxfGroup.toString(10, 12.0, 1));
        fw.write(DxfGroup.toString(20, 9.0, 1));
        fw.write(String.valueOf(DxfGroup.toString(9, "$UNITMODE")) + DxfGroup.toString(70, 0));
        fw.write(String.valueOf(DxfGroup.toString(9, "$VISRETAIN")) + DxfGroup.toString(70, 0));
        fw.write(String.valueOf(DxfGroup.toString(9, "$PLINEGEN")) + DxfGroup.toString(70, 1));
        fw.write(String.valueOf(DxfGroup.toString(9, "$PSLTSCALE")) + DxfGroup.toString(70, 0));
        fw.write(String.valueOf(DxfGroup.toString(9, "$TREEDEPTH")) + DxfGroup.toString(70, 3020));
        fw.write(String.valueOf(DxfGroup.toString(9, "$DWGCODEPAGE")) + DxfGroup.toString(3, "ansi_1252"));
        fw.write(DxfGroup.toString(0, "ENDSEC"));
    }

    public void writeTables(FileWriter fw) throws IOException {
        fw.write(DxfGroup.toString(0, "SECTION"));
        fw.write(DxfGroup.toString(2, "TABLES"));
        this.writeVPortTable(fw);
        this.writeLTypeTable(fw);
        this.writeLayerTable(fw);
        this.writeStyleTable(fw);
        this.writeViewTable(fw);
        this.writeUCSTable(fw);
        this.writeAppidTable(fw);
        this.writeDimStyleTable(fw);
        this.writeBlockRecordTable(fw);
        fw.write(DxfGroup.toString(0, "ENDSEC"));
    }

    public void writeVPortTable(FileWriter fw) throws IOException {
        fw.write(DxfGroup.toString(0, "TABLE"));
        fw.write(DxfGroup.toString(2, "VPORT"));
        fw.write(DxfGroup.toString(5, 8));
        fw.write(DxfGroup.toString(100, "AcDbSymbolTable"));
        fw.write(DxfGroup.toString(70, 0));
        fw.write(DxfGroup.toString(0, "ENDTAB"));
    }

    public void writeLTypeTable(FileWriter fw) throws IOException {
        fw.write(DxfGroup.toString(0, "TABLE"));
        fw.write(DxfGroup.toString(2, "LTYPE"));
        fw.write(DxfGroup.toString(5, 5));
        fw.write(DxfGroup.toString(100, "AcDbSymbolTable"));
        fw.write(DxfGroup.toString(70, 1));
        fw.write(DxfGroup.toString(0, "LTYPE"));
        fw.write(DxfGroup.toString(5, 14));
        fw.write(DxfGroup.toString(100, "AcDbSymbolTableRecord"));
        fw.write(DxfGroup.toString(100, "AcDbLinetypeTableRecord"));
        fw.write(DxfGroup.toString(2, "ByBlock"));
        fw.write(DxfGroup.toString(70, 0));
        fw.write(DxfGroup.toString(3, ""));
        fw.write(DxfGroup.toString(72, 65));
        fw.write(DxfGroup.toString(73, 0));
        fw.write(DxfGroup.toString(40, 0.0, 4));
        fw.write(DxfGroup.toString(0, "LTYPE"));
        fw.write(DxfGroup.toString(5, 15));
        fw.write(DxfGroup.toString(100, "AcDbSymbolTableRecord"));
        fw.write(DxfGroup.toString(100, "AcDbLinetypeTableRecord"));
        fw.write(DxfGroup.toString(2, "ByLayer"));
        fw.write(DxfGroup.toString(70, 0));
        fw.write(DxfGroup.toString(3, ""));
        fw.write(DxfGroup.toString(72, 65));
        fw.write(DxfGroup.toString(73, 0));
        fw.write(DxfGroup.toString(40, 0.0, 4));
        fw.write(DxfGroup.toString(0, "ENDTAB"));
    }

    public void writeLayerTable(FileWriter fw) throws IOException {
        fw.write(DxfGroup.toString(0, "TABLE"));
        fw.write(DxfGroup.toString(2, "LAYER"));
        fw.write(DxfGroup.toString(5, 2));
        fw.write(DxfGroup.toString(100, "AcDbSymbolTable"));
        fw.write(DxfGroup.toString(70, 1));
        fw.write(DxfGroup.toString(0, "LAYER"));
        fw.write(DxfGroup.toString(5, 10));
        fw.write(DxfGroup.toString(100, "AcDbSymbolTableRecord"));
        fw.write(DxfGroup.toString(100, "AcDbLayerTableRecord"));
        fw.write(DxfGroup.toString(2, "0"));
        fw.write(DxfGroup.toString(70, 0));
        fw.write(DxfGroup.toString(62, 7));
        fw.write(DxfGroup.toString(6, "CONTINUOUS"));
        fw.write(DxfGroup.toString(390, "F"));
        fw.write(DxfGroup.toString(0, "ENDTAB"));
    }

    public void writeStyleTable(FileWriter fw) throws IOException {
        fw.write(DxfGroup.toString(0, "TABLE"));
        fw.write(DxfGroup.toString(2, "STYLE"));
        fw.write(DxfGroup.toString(5, 3));
        fw.write(DxfGroup.toString(100, "AcDbSymbolTable"));
        fw.write(DxfGroup.toString(70, 0));
        fw.write(DxfGroup.toString(0, "STYLE"));
        fw.write(DxfGroup.toString(5, 11));
        fw.write(DxfGroup.toString(100, "AcDbSymbolTableRecord"));
        fw.write(DxfGroup.toString(100, "AcDbTextStyleTableRecord"));
        fw.write(DxfGroup.toString(2, "Standard"));
        fw.write(DxfGroup.toString(70, 0));
        fw.write(DxfGroup.toString(71, 0));
        fw.write(DxfGroup.toString(40, 0.0, 4));
        fw.write(DxfGroup.toString(41, 1.0, 4));
        fw.write(DxfGroup.toString(42, 2.5, 4));
        fw.write(DxfGroup.toString(50, 0.0, 4));
        fw.write(DxfGroup.toString(3, "txt"));
        fw.write(DxfGroup.toString(4, ""));
        fw.write(DxfGroup.toString(0, "ENDTAB"));
    }

    public void writeViewTable(FileWriter fw) throws IOException {
        fw.write(DxfGroup.toString(0, "TABLE"));
        fw.write(DxfGroup.toString(2, "VIEW"));
        fw.write(DxfGroup.toString(5, 6));
        fw.write(DxfGroup.toString(100, "AcDbSymbolTable"));
        fw.write(DxfGroup.toString(70, 0));
        fw.write(DxfGroup.toString(0, "ENDTAB"));
    }

    public void writeUCSTable(FileWriter fw) throws IOException {
        fw.write(DxfGroup.toString(0, "TABLE"));
        fw.write(DxfGroup.toString(2, "UCS"));
        fw.write(DxfGroup.toString(5, 7));
        fw.write(DxfGroup.toString(100, "AcDbSymbolTable"));
        fw.write(DxfGroup.toString(70, 0));
        fw.write(DxfGroup.toString(0, "ENDTAB"));
    }

    public void writeAppidTable(FileWriter fw) throws IOException {
        fw.write(DxfGroup.toString(0, "TABLE"));
        fw.write(DxfGroup.toString(2, "APPID"));
        fw.write(DxfGroup.toString(5, 9));
        fw.write(DxfGroup.toString(100, "AcDbSymbolTable"));
        fw.write(DxfGroup.toString(70, 1));
        fw.write(DxfGroup.toString(0, "APPID"));
        fw.write(DxfGroup.toString(5, 12));
        fw.write(DxfGroup.toString(100, "AcDbSymbolTableRecord"));
        fw.write(DxfGroup.toString(100, "AcDbRegAppTableRecord"));
        fw.write(DxfGroup.toString(2, "ACAD"));
        fw.write(DxfGroup.toString(70, 1));
        fw.write(DxfGroup.toString(0, "ENDTAB"));
    }

    public void writeDimStyleTable(FileWriter fw) throws IOException {
        fw.write(DxfGroup.toString(0, "TABLE"));
        fw.write(DxfGroup.toString(2, "DIMSTYLE"));
        fw.write(DxfGroup.toString(5, "A"));
        fw.write(DxfGroup.toString(100, "AcDbSymbolTable"));
        fw.write(DxfGroup.toString(70, 0));
        fw.write(DxfGroup.toString(100, "AcDbDimStyleTable"));
        fw.write(DxfGroup.toString(0, "ENDTAB"));
    }

    public void writeBlockRecordTable(FileWriter fw) throws IOException {
        fw.write(DxfGroup.toString(0, "TABLE"));
        fw.write(DxfGroup.toString(2, "BLOCK_RECORD"));
        fw.write(DxfGroup.toString(5, 1));
        fw.write(DxfGroup.toString(100, "AcDbSymbolTable"));
        fw.write(DxfGroup.toString(70, 1));
        fw.write(DxfGroup.toString(0, "BLOCK_RECORD"));
        fw.write(DxfGroup.toString(5, "1F"));
        fw.write(DxfGroup.toString(100, "AcDbSymbolTableRecord"));
        fw.write(DxfGroup.toString(100, "AcDbBlockTableRecord"));
        fw.write(DxfGroup.toString(2, "*Model_Space"));
        fw.write(DxfGroup.toString(340, "22"));
        fw.write(DxfGroup.toString(0, "BLOCK_RECORD"));
        fw.write(DxfGroup.toString(5, "1B"));
        fw.write(DxfGroup.toString(100, "AcDbSymbolTableRecord"));
        fw.write(DxfGroup.toString(100, "AcDbBlockTableRecord"));
        fw.write(DxfGroup.toString(2, "*Paper_Space"));
        fw.write(DxfGroup.toString(340, "1E"));
        fw.write(DxfGroup.toString(0, "BLOCK_RECORD"));
        fw.write(DxfGroup.toString(5, "23"));
        fw.write(DxfGroup.toString(100, "AcDbSymbolTableRecord"));
        fw.write(DxfGroup.toString(100, "AcDbBlockTableRecord"));
        fw.write(DxfGroup.toString(2, "*Paper_Space0"));
        fw.write(DxfGroup.toString(340, "26"));
        fw.write(DxfGroup.toString(0, "ENDTAB"));
    }

    public void writeBlocks(FileWriter fw) throws IOException {
        fw.write(DxfGroup.toString(0, "SECTION"));
        fw.write(DxfGroup.toString(2, "BLOCKS"));
        fw.write(DxfGroup.toString(0, "BLOCK"));
        fw.write(DxfGroup.toString(5, "20"));
        fw.write(DxfGroup.toString(100, "AcDbEntity"));
        fw.write(DxfGroup.toString(8, "0"));
        fw.write(DxfGroup.toString(100, "AcDbBlockBegin"));
        fw.write(DxfGroup.toString(2, "*Model_Space"));
        fw.write(DxfGroup.toString(70, 0));
        fw.write(DxfGroup.toString(10, 0.0, 4));
        fw.write(DxfGroup.toString(20, 0.0, 4));
        fw.write(DxfGroup.toString(30, 0.0, 4));
        fw.write(DxfGroup.toString(3, "*Model_Space"));
        fw.write(DxfGroup.toString(1, ""));
        fw.write(DxfGroup.toString(0, "ENDBLK"));
        fw.write(DxfGroup.toString(5, "21"));
        fw.write(DxfGroup.toString(100, "AcDbEntity"));
        fw.write(DxfGroup.toString(8, "0"));
        fw.write(DxfGroup.toString(100, "AcDbBlockEnd"));
        fw.write(DxfGroup.toString(0, "BLOCK"));
        fw.write(DxfGroup.toString(5, "1C"));
        fw.write(DxfGroup.toString(100, "AcDbEntity"));
        fw.write(DxfGroup.toString(67, 1));
        fw.write(DxfGroup.toString(8, "0"));
        fw.write(DxfGroup.toString(100, "AcDbBlockBegin"));
        fw.write(DxfGroup.toString(2, "*Paper_Space"));
        fw.write(DxfGroup.toString(70, 0));
        fw.write(DxfGroup.toString(10, 0.0, 4));
        fw.write(DxfGroup.toString(20, 0.0, 4));
        fw.write(DxfGroup.toString(30, 0.0, 4));
        fw.write(DxfGroup.toString(3, "*Paper_Space"));
        fw.write(DxfGroup.toString(1, ""));
        fw.write(DxfGroup.toString(0, "ENDBLK"));
        fw.write(DxfGroup.toString(5, "1D"));
        fw.write(DxfGroup.toString(100, "AcDbEntity"));
        fw.write(DxfGroup.toString(67, 1));
        fw.write(DxfGroup.toString(8, "0"));
        fw.write(DxfGroup.toString(100, "AcDbBlockEnd"));
        fw.write(DxfGroup.toString(0, "BLOCK"));
        fw.write(DxfGroup.toString(5, "24"));
        fw.write(DxfGroup.toString(100, "AcDbEntity"));
        fw.write(DxfGroup.toString(8, "0"));
        fw.write(DxfGroup.toString(100, "AcDbBlockBegin"));
        fw.write(DxfGroup.toString(2, "*Paper_Space0"));
        fw.write(DxfGroup.toString(70, 0));
        fw.write(DxfGroup.toString(10, 0.0, 4));
        fw.write(DxfGroup.toString(20, 0.0, 4));
        fw.write(DxfGroup.toString(30, 0.0, 4));
        fw.write(DxfGroup.toString(3, "*Paper_Space0"));
        fw.write(DxfGroup.toString(1, ""));
        fw.write(DxfGroup.toString(0, "ENDBLK"));
        fw.write(DxfGroup.toString(5, "25"));
        fw.write(DxfGroup.toString(100, "AcDbEntity"));
        fw.write(DxfGroup.toString(8, "0"));
        fw.write(DxfGroup.toString(100, "AcDbBlockEnd"));
        fw.write(DxfGroup.toString(0, "ENDSEC"));
    }

    public void writeEntities(FileWriter fw) throws IOException {
        fw.write(DxfGroup.toString(0, "SECTION"));
        fw.write(DxfGroup.toString(2, "ENTITIES"));
        if (this.cadFlag) {
            fw.write(((DxfEntityMaker)this.entityMaker).getEntities().toDxfString());
        }
        fw.write(DxfGroup.toString(0, "ENDSEC"));
    }

    public void writeObjects(FileWriter fw) throws IOException {
        fw.write(DxfGroup.toString(0, "SECTION"));
        fw.write(DxfGroup.toString(2, "OBJECTS"));
        fw.write(DxfGroup.toString(0, "DICTIONARY"));
        fw.write(DxfGroup.toString(5, "C"));
        fw.write(DxfGroup.toString(100, "AcDbDictionary"));
        fw.write(DxfGroup.toString(280, 0));
        fw.write(DxfGroup.toString(281, 1));
        fw.write(DxfGroup.toString(3, "ACAD_GROUP"));
        fw.write(DxfGroup.toString(350, "D"));
        fw.write(DxfGroup.toString(3, "ACAD_LAYOUT"));
        fw.write(DxfGroup.toString(350, "1A"));
        fw.write(DxfGroup.toString(3, "ACAD_MLINESTYLE"));
        fw.write(DxfGroup.toString(350, "17"));
        fw.write(DxfGroup.toString(3, "ACAD_PLOTSETTINGS"));
        fw.write(DxfGroup.toString(350, "19"));
        fw.write(DxfGroup.toString(3, "ACAD_PLOTSTYLENAME"));
        fw.write(DxfGroup.toString(350, "E"));
        fw.write(DxfGroup.toString(3, "AcDbVariableDictionary"));
        fw.write(DxfGroup.toString(350, "2C"));
        fw.write(DxfGroup.toString(0, "DICTIONARY"));
        fw.write(DxfGroup.toString(5, "D"));
        fw.write(DxfGroup.toString(100, "AcDbDictionary"));
        fw.write(DxfGroup.toString(280, 0));
        fw.write(DxfGroup.toString(281, 1));
        fw.write(DxfGroup.toString(0, "ACDBDICTIONARYWDFLT"));
        fw.write(DxfGroup.toString(5, "E"));
        fw.write(DxfGroup.toString(100, "AcDbDictionary"));
        fw.write(DxfGroup.toString(281, 1));
        fw.write(DxfGroup.toString(3, "Normal"));
        fw.write(DxfGroup.toString(350, "F"));
        fw.write(DxfGroup.toString(100, "AcDbDictionaryWithDefault"));
        fw.write(DxfGroup.toString(340, "F"));
        fw.write(DxfGroup.toString(0, "ACDBPLACEHOLDER"));
        fw.write(DxfGroup.toString(5, "F"));
        fw.write(DxfGroup.toString(0, "DICTIONARY"));
        fw.write(DxfGroup.toString(5, "17"));
        fw.write(DxfGroup.toString(100, "AcDbDictionary"));
        fw.write(DxfGroup.toString(280, 0));
        fw.write(DxfGroup.toString(281, 1));
        fw.write(DxfGroup.toString(3, "Standard"));
        fw.write(DxfGroup.toString(350, "18"));
        fw.write(DxfGroup.toString(0, "MLINESTYLE"));
        fw.write(DxfGroup.toString(5, "18"));
        fw.write(DxfGroup.toString(100, "AcDbMlineStyle"));
        fw.write(DxfGroup.toString(2, "STANDARD"));
        fw.write(DxfGroup.toString(70, 0));
        fw.write(DxfGroup.toString(3, ""));
        fw.write(DxfGroup.toString(62, 256));
        fw.write(DxfGroup.toString(51, 90.0, 4));
        fw.write(DxfGroup.toString(52, 90.0, 4));
        fw.write(DxfGroup.toString(71, 2));
        fw.write(DxfGroup.toString(49, 0.5, 4));
        fw.write(DxfGroup.toString(62, 256));
        fw.write(DxfGroup.toString(6, "BYLAYER"));
        fw.write(DxfGroup.toString(49, -0.5, 4));
        fw.write(DxfGroup.toString(62, 256));
        fw.write(DxfGroup.toString(6, "BYLAYER"));
        fw.write(DxfGroup.toString(0, "DICTIONARY"));
        fw.write(DxfGroup.toString(5, "19"));
        fw.write(DxfGroup.toString(100, "AcDbDictionary"));
        fw.write(DxfGroup.toString(280, 0));
        fw.write(DxfGroup.toString(281, 1));
        fw.write(DxfGroup.toString(0, "DICTIONARY"));
        fw.write(DxfGroup.toString(5, "1A"));
        fw.write(DxfGroup.toString(100, "AcDbDictionary"));
        fw.write(DxfGroup.toString(281, 1));
        fw.write(DxfGroup.toString(3, "Layout1"));
        fw.write(DxfGroup.toString(350, "1E"));
        fw.write(DxfGroup.toString(3, "Layout2"));
        fw.write(DxfGroup.toString(350, "26"));
        fw.write(DxfGroup.toString(3, "Model"));
        fw.write(DxfGroup.toString(350, "22"));
        fw.write(DxfGroup.toString(0, "LAYOUT"));
        fw.write(DxfGroup.toString(5, "1E"));
        fw.write(DxfGroup.toString(100, "AcDbPlotSettings"));
        fw.write(DxfGroup.toString(1, ""));
        fw.write(DxfGroup.toString(2, "C:\\Program Files\\AutoCAD 2002\\plotters\\DWF ePlot (optimized for plotting).pc3"));
        fw.write(DxfGroup.toString(4, ""));
        fw.write(DxfGroup.toString(6, ""));
        fw.write(DxfGroup.toString(40, 0.0, 4));
        fw.write(DxfGroup.toString(41, 0.0, 4));
        fw.write(DxfGroup.toString(42, 0.0, 4));
        fw.write(DxfGroup.toString(43, 0.0, 4));
        fw.write(DxfGroup.toString(44, 0.0, 4));
        fw.write(DxfGroup.toString(45, 0.0, 4));
        fw.write(DxfGroup.toString(46, 0.0, 4));
        fw.write(DxfGroup.toString(47, 0.0, 4));
        fw.write(DxfGroup.toString(48, 0.0, 4));
        fw.write(DxfGroup.toString(49, 0.0, 4));
        fw.write(DxfGroup.toString(140, 0.0, 4));
        fw.write(DxfGroup.toString(141, 0.0, 4));
        fw.write(DxfGroup.toString(142, 1.0, 4));
        fw.write(DxfGroup.toString(143, 1.0, 4));
        fw.write(DxfGroup.toString(70, 688));
        fw.write(DxfGroup.toString(72, 0));
        fw.write(DxfGroup.toString(73, 0));
        fw.write(DxfGroup.toString(74, 5));
        fw.write(DxfGroup.toString(7, ""));
        fw.write(DxfGroup.toString(75, 16));
        fw.write(DxfGroup.toString(147, 1.0, 4));
        fw.write(DxfGroup.toString(148, 0.0, 4));
        fw.write(DxfGroup.toString(149, 0.0, 4));
        fw.write(DxfGroup.toString(100, "AcDbLayout"));
        fw.write(DxfGroup.toString(1, "Layout1"));
        fw.write(DxfGroup.toString(70, 1));
        fw.write(DxfGroup.toString(71, 1));
        fw.write(DxfGroup.toString(10, 0.0, 4));
        fw.write(DxfGroup.toString(20, 0.0, 4));
        fw.write(DxfGroup.toString(11, 420.0, 4));
        fw.write(DxfGroup.toString(21, 297.0, 4));
        fw.write(DxfGroup.toString(12, 0.0, 4));
        fw.write(DxfGroup.toString(22, 0.0, 4));
        fw.write(DxfGroup.toString(32, 0.0, 4));
        fw.write(DxfGroup.toString(14, 1.0E20, 4));
        fw.write(DxfGroup.toString(24, 1.0E20, 4));
        fw.write(DxfGroup.toString(34, 1.0E20, 4));
        fw.write(DxfGroup.toString(15, -1.0E20, 4));
        fw.write(DxfGroup.toString(25, -1.0E20, 4));
        fw.write(DxfGroup.toString(35, -1.0E20, 4));
        fw.write(DxfGroup.toString(146, 0.0, 4));
        fw.write(DxfGroup.toString(13, 0.0, 4));
        fw.write(DxfGroup.toString(23, 0.0, 4));
        fw.write(DxfGroup.toString(33, 0.0, 4));
        fw.write(DxfGroup.toString(16, 1.0, 4));
        fw.write(DxfGroup.toString(26, 0.0, 4));
        fw.write(DxfGroup.toString(36, 0.0, 4));
        fw.write(DxfGroup.toString(17, 0.0, 4));
        fw.write(DxfGroup.toString(27, 1.0, 4));
        fw.write(DxfGroup.toString(37, 0.0, 4));
        fw.write(DxfGroup.toString(76, 0));
        fw.write(DxfGroup.toString(330, "1B"));
        fw.write(DxfGroup.toString(0, "LAYOUT"));
        fw.write(DxfGroup.toString(5, "22"));
        fw.write(DxfGroup.toString(100, "AcDbPlotSettings"));
        fw.write(DxfGroup.toString(1, ""));
        fw.write(DxfGroup.toString(2, "C:\\Program Files\\AutoCAD 2002\\plotters\\DWF ePlot (optimized for plotting).pc3"));
        fw.write(DxfGroup.toString(4, ""));
        fw.write(DxfGroup.toString(6, ""));
        fw.write(DxfGroup.toString(40, 0.0, 4));
        fw.write(DxfGroup.toString(41, 0.0, 4));
        fw.write(DxfGroup.toString(42, 0.0, 4));
        fw.write(DxfGroup.toString(43, 0.0, 4));
        fw.write(DxfGroup.toString(44, 0.0, 4));
        fw.write(DxfGroup.toString(45, 0.0, 4));
        fw.write(DxfGroup.toString(46, 0.0, 4));
        fw.write(DxfGroup.toString(47, 0.0, 4));
        fw.write(DxfGroup.toString(48, 0.0, 4));
        fw.write(DxfGroup.toString(49, 0.0, 4));
        fw.write(DxfGroup.toString(140, 0.0, 4));
        fw.write(DxfGroup.toString(141, 0.0, 4));
        fw.write(DxfGroup.toString(142, 1.0, 4));
        fw.write(DxfGroup.toString(143, 1.0, 4));
        fw.write(DxfGroup.toString(70, 1712));
        fw.write(DxfGroup.toString(72, 0));
        fw.write(DxfGroup.toString(73, 0));
        fw.write(DxfGroup.toString(74, 0));
        fw.write(DxfGroup.toString(7, ""));
        fw.write(DxfGroup.toString(75, 0));
        fw.write(DxfGroup.toString(147, 1.0, 4));
        fw.write(DxfGroup.toString(148, 0.0, 4));
        fw.write(DxfGroup.toString(149, 0.0, 4));
        fw.write(DxfGroup.toString(100, "AcDbLayout"));
        fw.write(DxfGroup.toString(1, "Model"));
        fw.write(DxfGroup.toString(70, 1));
        fw.write(DxfGroup.toString(71, 0));
        fw.write(DxfGroup.toString(10, 0.0, 4));
        fw.write(DxfGroup.toString(20, 0.0, 4));
        fw.write(DxfGroup.toString(11, 12.0, 4));
        fw.write(DxfGroup.toString(21, 9.0, 4));
        fw.write(DxfGroup.toString(12, 0.0, 4));
        fw.write(DxfGroup.toString(22, 0.0, 4));
        fw.write(DxfGroup.toString(32, 0.0, 4));
        fw.write(DxfGroup.toString(14, 0.0, 4));
        fw.write(DxfGroup.toString(24, 0.0, 4));
        fw.write(DxfGroup.toString(34, 0.0, 4));
        fw.write(DxfGroup.toString(15, 0.0, 4));
        fw.write(DxfGroup.toString(25, 0.0, 4));
        fw.write(DxfGroup.toString(35, 0.0, 4));
        fw.write(DxfGroup.toString(146, 0.0, 4));
        fw.write(DxfGroup.toString(13, 0.0, 4));
        fw.write(DxfGroup.toString(23, 0.0, 4));
        fw.write(DxfGroup.toString(33, 0.0, 4));
        fw.write(DxfGroup.toString(16, 1.0, 4));
        fw.write(DxfGroup.toString(26, 0.0, 4));
        fw.write(DxfGroup.toString(36, 0.0, 4));
        fw.write(DxfGroup.toString(17, 0.0, 4));
        fw.write(DxfGroup.toString(27, 1.0, 4));
        fw.write(DxfGroup.toString(37, 0.0, 4));
        fw.write(DxfGroup.toString(76, 0));
        fw.write(DxfGroup.toString(330, "1F"));
        fw.write(DxfGroup.toString(0, "LAYOUT"));
        fw.write(DxfGroup.toString(5, "26"));
        fw.write(DxfGroup.toString(100, "AcDbPlotSettings"));
        fw.write(DxfGroup.toString(1, ""));
        fw.write(DxfGroup.toString(2, "C:\\Program Files\\AutoCAD 2002\\plotters\\DWF ePlot (optimized for plotting).pc3"));
        fw.write(DxfGroup.toString(4, ""));
        fw.write(DxfGroup.toString(6, ""));
        fw.write(DxfGroup.toString(40, 0.0, 4));
        fw.write(DxfGroup.toString(41, 0.0, 4));
        fw.write(DxfGroup.toString(42, 0.0, 4));
        fw.write(DxfGroup.toString(43, 0.0, 4));
        fw.write(DxfGroup.toString(44, 0.0, 4));
        fw.write(DxfGroup.toString(45, 0.0, 4));
        fw.write(DxfGroup.toString(46, 0.0, 4));
        fw.write(DxfGroup.toString(47, 0.0, 4));
        fw.write(DxfGroup.toString(48, 0.0, 4));
        fw.write(DxfGroup.toString(49, 0.0, 4));
        fw.write(DxfGroup.toString(140, 0.0, 4));
        fw.write(DxfGroup.toString(141, 0.0, 4));
        fw.write(DxfGroup.toString(142, 1.0, 4));
        fw.write(DxfGroup.toString(143, 1.0, 4));
        fw.write(DxfGroup.toString(70, 688));
        fw.write(DxfGroup.toString(72, 0));
        fw.write(DxfGroup.toString(73, 0));
        fw.write(DxfGroup.toString(74, 5));
        fw.write(DxfGroup.toString(7, ""));
        fw.write(DxfGroup.toString(75, 16));
        fw.write(DxfGroup.toString(147, 1.0, 4));
        fw.write(DxfGroup.toString(148, 0.0, 4));
        fw.write(DxfGroup.toString(149, 0.0, 4));
        fw.write(DxfGroup.toString(100, "AcDbLayout"));
        fw.write(DxfGroup.toString(1, "Layout2"));
        fw.write(DxfGroup.toString(70, 1));
        fw.write(DxfGroup.toString(71, 2));
        fw.write(DxfGroup.toString(10, 0.0, 4));
        fw.write(DxfGroup.toString(20, 0.0, 4));
        fw.write(DxfGroup.toString(11, 12.0, 4));
        fw.write(DxfGroup.toString(21, 9.0, 4));
        fw.write(DxfGroup.toString(12, 0.0, 4));
        fw.write(DxfGroup.toString(22, 0.0, 4));
        fw.write(DxfGroup.toString(32, 0.0, 4));
        fw.write(DxfGroup.toString(14, 0.0, 4));
        fw.write(DxfGroup.toString(24, 0.0, 4));
        fw.write(DxfGroup.toString(34, 0.0, 4));
        fw.write(DxfGroup.toString(15, 0.0, 4));
        fw.write(DxfGroup.toString(25, 0.0, 4));
        fw.write(DxfGroup.toString(35, 0.0, 4));
        fw.write(DxfGroup.toString(146, 0.0, 4));
        fw.write(DxfGroup.toString(13, 0.0, 4));
        fw.write(DxfGroup.toString(23, 0.0, 4));
        fw.write(DxfGroup.toString(33, 0.0, 4));
        fw.write(DxfGroup.toString(16, 1.0, 4));
        fw.write(DxfGroup.toString(26, 0.0, 4));
        fw.write(DxfGroup.toString(36, 0.0, 4));
        fw.write(DxfGroup.toString(17, 0.0, 4));
        fw.write(DxfGroup.toString(27, 1.0, 4));
        fw.write(DxfGroup.toString(37, 0.0, 4));
        fw.write(DxfGroup.toString(76, 0));
        fw.write(DxfGroup.toString(330, "23"));
        fw.write(DxfGroup.toString(0, "DICTIONARY"));
        fw.write(DxfGroup.toString(5, 46));
        fw.write(DxfGroup.toString(100, "AcDbDictionary"));
        fw.write(DxfGroup.toString(281, 1));
        fw.write(DxfGroup.toString(3, "DIMASSOC"));
        fw.write(DxfGroup.toString(350, 48));
        fw.write(DxfGroup.toString(3, "HIDETEXT"));
        fw.write(DxfGroup.toString(350, 47));
        fw.write(DxfGroup.toString(0, "DICTIONARYVAR"));
        fw.write(DxfGroup.toString(5, 47));
        fw.write(DxfGroup.toString(100, "DictionaryVariables"));
        fw.write(DxfGroup.toString(280, 0));
        fw.write(DxfGroup.toString(1, 2));
        fw.write(DxfGroup.toString(0, "DICTIONARYVAR"));
        fw.write(DxfGroup.toString(5, 48));
        fw.write(DxfGroup.toString(100, "DictionaryVariables"));
        fw.write(DxfGroup.toString(280, 0));
        fw.write(DxfGroup.toString(1, 1));
        fw.write(DxfGroup.toString(0, "ENDSEC"));
    }

    public void reProject(ICoordTrans rp) {
        this.entityMaker.reProject(rp);
    }

    public void close() {
        this.entityMaker = null;
        this.fi = null;
        this.headerManager = null;
    }

    public boolean isCadFlag() {
        return this.cadFlag;
    }

    public void setCadFlag(boolean cadFlag) {
        this.cadFlag = cadFlag;
    }

    public boolean isDxf3DFlag() {
        return this.dxf3DFlag;
    }

    public void setDxf3DFlag(boolean dxf3DFlag) {
        this.dxf3DFlag = dxf3DFlag;
    }

    public static interface EntityFactory
    extends Projected {
        public void setAddingToBlock(boolean var1);

        public void createLayer(DxfGroupVector var1) throws Exception;

        public void createPolyline(DxfGroupVector var1) throws Exception;

        public void addVertex(DxfGroupVector var1) throws Exception;

        public void endSeq() throws Exception;

        public void createLwPolyline(DxfGroupVector var1) throws Exception;

        public void createLine(DxfGroupVector var1) throws Exception;

        public void createText(DxfGroupVector var1) throws Exception;

        public void createMText(DxfGroupVector var1) throws Exception;

        public void createPoint(DxfGroupVector var1) throws Exception;

        public void createCircle(DxfGroupVector var1) throws Exception;

        public void createEllipse(DxfGroupVector var1) throws Exception;

        public void createArc(DxfGroupVector var1) throws Exception;

        public void createInsert(DxfGroupVector var1) throws Exception;

        public void createSolid(DxfGroupVector var1) throws Exception;

        public void createSpline(DxfGroupVector var1) throws Exception;

        public void createAttdef(DxfGroupVector var1) throws Exception;

        public void createAttrib(DxfGroupVector var1) throws Exception;

        public void createBlock(DxfGroupVector var1) throws Exception;

        public void endBlk(DxfGroupVector var1) throws Exception;

        public void testBlocks();

        public Extent getExtent();

        public Vector getBlkList();

        public Vector getAttributes();

        public void depureAttributes();

        public IObjList getObjects();

        public boolean isDxf3DFile();
    }

    public static interface VarSettings {
        public void setAcadVersion(DxfGroupVector var1) throws Exception;

        public String getAcadVersion();

        public DxfHeaderVariables getDxfHeaderVars();

        public boolean isWritedDxf3D();

        public void loadMinZFromHeader(double var1);

        public void loadMaxZFromHeader(double var1);
    }
}


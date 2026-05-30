/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vividsolutions.jts.geom.Coordinate
 *  com.vividsolutions.jts.geom.CoordinateList
 *  com.vividsolutions.jts.geom.CoordinateSequence
 *  com.vividsolutions.jts.geom.CoordinateSequenceFactory
 *  com.vividsolutions.jts.geom.PrecisionModel
 */
package org.geotools.data.oracle.sdo;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.CoordinateList;
import com.vividsolutions.jts.geom.CoordinateSequence;
import com.vividsolutions.jts.geom.CoordinateSequenceFactory;
import com.vividsolutions.jts.geom.PrecisionModel;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import org.geotools.data.oracle.sdo.AttributeList;
import org.geotools.data.oracle.sdo.CoordinateAccess;
import org.geotools.data.oracle.sdo.CoordinateAccessFactory;
import org.geotools.data.oracle.sdo.OrdinateList;

public class Coordinates {
    private Coordinates() {
    }

    public static CoordinateSequence subList(CoordinateSequenceFactory factory, CoordinateSequence sequence, int fromIndex, int toIndex) {
        List sublist;
        if (fromIndex == 0 && toIndex == sequence.size()) {
            return sequence;
        }
        if (sequence instanceof List && (sublist = ((List)sequence).subList(fromIndex, toIndex)) instanceof CoordinateSequence) {
            return (CoordinateSequence)sublist;
        }
        if (sequence instanceof CoordinateAccess) {
            CoordinateAccess access = (CoordinateAccess)sequence;
            double[][] coordArray = access.toOrdinateArrays();
            Object[] attributeArray = access.toAttributeArrays();
            double[][] subCoordArray = new double[access.getDimension()][];
            Object[][] subAttributeArray = new Object[access.getNumAttributes()][];
            int i = 0;
            while (i < access.getDimension()) {
                subCoordArray[i] = new OrdinateList(coordArray[i], 0, 1, fromIndex, toIndex).toDoubleArray();
                ++i;
            }
            i = 0;
            while (i < access.getNumAttributes()) {
                subAttributeArray[i] = new AttributeList(attributeArray[i], 0, 1, fromIndex, toIndex).toObjectArray();
                ++i;
            }
            System.out.println("subCoordArray.length = " + subCoordArray.length);
            System.out.println("subCoordArray: ");
            System.out.print("X   ");
            int p = 0;
            while (p < subCoordArray[0].length) {
                System.out.print(String.valueOf(subCoordArray[0][p]) + " ");
                ++p;
            }
            System.out.print("\nY   ");
            p = 0;
            while (p < subCoordArray[1].length) {
                System.out.print(String.valueOf(subCoordArray[1][p]) + " ");
                ++p;
            }
            System.out.println("");
            System.out.println("subAttributeArray.length = " + subAttributeArray.length);
            System.out.println("subAttributeArray: ");
            System.out.print("Z   ");
            p = 0;
            while (p < subAttributeArray[0].length) {
                System.out.print(subAttributeArray[0][p] + " ");
                ++p;
            }
            System.out.print("\nT   ");
            p = 0;
            while (p < subAttributeArray[1].length) {
                System.out.print(subAttributeArray[1][p] + " ");
                ++p;
            }
            System.out.println("");
            CoordinateAccess c = ((CoordinateAccessFactory)factory).create(subCoordArray, (Object[])subAttributeArray);
            return c;
        }
        CoordinateList list = new CoordinateList(sequence.toCoordinateArray());
        Coordinate[] array = new Coordinate[toIndex - fromIndex];
        int index = 0;
        Iterator i = list.subList(fromIndex, toIndex).iterator();
        while (i.hasNext()) {
            array[index] = (Coordinate)i.next();
            ++index;
        }
        return factory.create(array);
    }

    public static CoordinateSequence reverse(CoordinateSequenceFactory factory, CoordinateSequence sequence) {
        if (sequence instanceof CoordinateAccess) {
            CoordinateAccess access = (CoordinateAccess)sequence;
            double[][] coordArray = access.toOrdinateArrays();
            Object[] attributeArray = access.toAttributeArrays();
            double[][] subCoordArray = new double[access.getDimension()][];
            Object[][] subAttributeArray = new Object[access.getNumAttributes()][];
            int i = 0;
            while (i < access.getDimension()) {
                subCoordArray[i] = new OrdinateList(coordArray[i], 0, 1, access.size() - 1, -1).toDoubleArray();
                ++i;
            }
            i = 0;
            while (i < access.getNumAttributes()) {
                subAttributeArray[i] = new AttributeList(attributeArray[i], 0, 1, access.size() - 1, -1).toObjectArray();
                ++i;
            }
            CoordinateAccess c = ((CoordinateAccessFactory)factory).create(subCoordArray, (Object[])subAttributeArray);
            return c;
        }
        CoordinateList list = new CoordinateList(sequence.toCoordinateArray());
        Collections.reverse(list);
        return factory.create(list.toCoordinateArray());
    }

    public static String toString(CoordinateSequence cs, int coordinate, NumberFormat nf) {
        StringBuffer buf = new StringBuffer();
        Coordinates.append(buf, cs, coordinate, nf);
        return buf.toString();
    }

    public static void append(StringBuffer buf, CoordinateSequence cs, int coordinate, NumberFormat nf) {
        if (cs instanceof CoordinateAccess) {
            CoordinateAccess ca = (CoordinateAccess)cs;
            Coordinates.append(buf, ca, coordinate, Coordinates.LEN(ca), nf);
        } else {
            Coordinates.append(buf, cs, coordinate, Coordinates.LEN(cs), nf);
        }
    }

    public static void append(StringBuffer buf, CoordinateSequence cs, int coordinate, int LEN, NumberFormat nf) {
        Coordinate c = cs.getCoordinate(coordinate);
        buf.append(nf.format(c.x));
        buf.append(" ");
        buf.append(nf.format(c.y));
        if (LEN == 3) {
            buf.append(" ");
            buf.append(nf.format(c.z));
        }
    }

    public static void append(StringBuffer buf, CoordinateAccess ca, int coordinate, int LEN, NumberFormat nf) {
        buf.append(nf.format(ca.getOrdinate(coordinate, 0)));
        int i = 1;
        while (i < LEN) {
            buf.append(" ");
            buf.append(nf.format(ca.getOrdinate(coordinate, i)));
            ++i;
        }
    }

    public static int LEN(CoordinateSequence cs) {
        return Coordinates.D(cs) + Coordinates.L(cs);
    }

    public static int D(CoordinateSequence cs) {
        if (cs instanceof CoordinateAccess) {
            return ((CoordinateAccess)cs).getDimension();
        }
        if (cs.size() > 0) {
            return Double.isNaN(cs.getCoordinate((int)0).z) ? 2 : 3;
        }
        return 3;
    }

    public static int L(CoordinateSequence cs) {
        if (cs instanceof CoordinateAccess) {
            return ((CoordinateAccess)cs).getNumAttributes();
        }
        return 0;
    }

    public static NumberFormat format(PrecisionModel pm) {
        DecimalFormatSymbols symbols = new DecimalFormatSymbols();
        symbols.setNaN("NaN");
        DecimalFormat f = new DecimalFormat();
        f.setDecimalFormatSymbols(symbols);
        if (pm == null) {
            f.setMaximumFractionDigits(0);
            return f;
        }
        f.setMinimumFractionDigits(0);
        f.setMaximumFractionDigits(pm.getMaximumSignificantDigits());
        return f;
    }

    public static String toString(CoordinateSequence cs, PrecisionModel pm) {
        StringBuffer buf = new StringBuffer();
        Coordinates.append(buf, cs, Coordinates.format(pm));
        return buf.toString();
    }

    public static void append(StringBuffer buf, CoordinateSequence cs, NumberFormat nf) {
        if (cs instanceof CoordinateAccess) {
            Coordinates.append(buf, (CoordinateAccess)cs, nf);
        } else {
            int LEN = Coordinates.LEN(cs);
            if (cs.size() == 0) {
                return;
            }
            Coordinates.append(buf, cs, 0, LEN, nf);
            if (cs.size() == 1) {
                return;
            }
            int i = 1;
            while (i < cs.size()) {
                buf.append(", ");
                Coordinates.append(buf, cs, i, LEN, nf);
                ++i;
            }
        }
    }

    public static void append(StringBuffer buf, CoordinateAccess ca, NumberFormat nf) {
        int LEN = Coordinates.LEN(ca);
        if (ca.size() == 0) {
            return;
        }
        Coordinates.append(buf, ca, 0, LEN, nf);
        if (ca.size() == 1) {
            return;
        }
        int i = 1;
        while (i < ca.size()) {
            buf.append(", ");
            Coordinates.append(buf, ca, i, LEN, nf);
            ++i;
        }
    }
}


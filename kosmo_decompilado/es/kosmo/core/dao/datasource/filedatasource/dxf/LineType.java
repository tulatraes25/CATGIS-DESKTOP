/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.commons.lang.StringUtils
 */
package es.kosmo.core.dao.datasource.filedatasource.dxf;

import es.kosmo.core.dao.datasource.filedatasource.dxf.LineTypeItem;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.lang.StringUtils;

public class LineType {
    String name = "";
    String description = "";
    LineTypeItem[] items = new LineTypeItem[0];

    public LineType(String name, String description) {
        this.name = name;
        this.description = description;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return this.description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LineTypeItem[] getItems() {
        return this.items;
    }

    public void setItems(LineTypeItem[] items) {
        this.items = items;
    }

    public double getLength() {
        double len = 0.0;
        LineTypeItem[] lineTypeItemArray = this.items;
        int n = this.items.length;
        int n2 = 0;
        while (n2 < n) {
            LineTypeItem item = lineTypeItemArray[n2];
            len += Math.abs(item.getLength());
            ++n2;
        }
        return len;
    }

    public static LineType parse(String ltype) {
        String name;
        String[] parts = ltype.split("!");
        String description = name = parts[0];
        ArrayList<LineTypeItem> items = new ArrayList<LineTypeItem>();
        double baseLen = 0.125;
        if (parts.length > 1) {
            description = StringUtils.repeat((String)parts[1].replace('_', ' '), (int)5);
            if (parts.length > 2) {
                baseLen = Double.parseDouble(parts[2]);
            }
            Pattern p = Pattern.compile("[-]+|[*]+|[_]+");
            Matcher m = p.matcher(parts[1]);
            while (m.find()) {
                String piece = m.group(0);
                int type = piece.startsWith("-") ? 0 : (piece.startsWith("*") ? 1 : 2);
                LineTypeItem item = new LineTypeItem(type, (double)piece.length() * baseLen);
                items.add(item);
            }
        }
        LineType result = new LineType(name, description);
        result.setItems(items.toArray(new LineTypeItem[0]));
        return result;
    }
}


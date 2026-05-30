/*
 * Decompiled with CFR 0.152.
 */
package org.saig.core.model.layout;

import es.kosmo.desktop.widgets.layout.PrintLayoutManagerPanel;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import org.saig.jump.widgets.print.PrintLayoutFrame;

public class PrintLayoutManager {
    private List<PrintLayoutFrame> layoutList = new ArrayList<PrintLayoutFrame>();
    private PrintLayoutManagerPanel frame;

    public PrintLayoutManagerPanel getPrintLayoutManagerFrame() {
        if (this.frame == null) {
            this.frame = new PrintLayoutManagerPanel();
        }
        return this.frame;
    }

    public void addLayout(PrintLayoutFrame printFrame) {
        this.layoutList.add(printFrame);
        Collections.sort(this.layoutList);
        this.getPrintLayoutManagerFrame().refresh();
    }

    public void remove(PrintLayoutFrame printFrame) {
        this.layoutList.remove(printFrame);
        this.getPrintLayoutManagerFrame().refresh();
    }

    public int indexOf(PrintLayoutFrame printFrame) {
        return this.layoutList.indexOf(printFrame);
    }

    public Iterator<PrintLayoutFrame> iterator() {
        return this.getPrintLayouts().iterator();
    }

    public PrintLayoutFrame getPrintLayoutPanel(String name) {
        Iterator<PrintLayoutFrame> i = this.iterator();
        while (i.hasNext()) {
            PrintLayoutFrame printLayout = i.next();
            if (!printLayout.getName().equals(name)) continue;
            return printLayout;
        }
        return null;
    }

    public PrintLayoutFrame getPrintLayoutFrame(int index) {
        return this.getPrintLayouts().get(index);
    }

    public int size() {
        return this.getPrintLayouts().size();
    }

    public List<PrintLayoutFrame> getPrintLayouts() {
        return this.layoutList;
    }

    public void clear() {
        int i = 0;
        while (i < this.layoutList.size()) {
            PrintLayoutFrame element = this.layoutList.get(i);
            element.dispose();
            ++i;
        }
        this.layoutList.clear();
        this.getPrintLayoutManagerFrame().refresh();
    }
}


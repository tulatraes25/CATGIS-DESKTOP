/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.log4j.Logger
 *  org.geotools.resources.Utilities
 *  org.opengis.util.Cloneable
 */
package org.saig.core.styling;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.apache.log4j.Logger;
import org.geotools.resources.Utilities;
import org.opengis.util.Cloneable;
import org.saig.core.filter.Expression;
import org.saig.core.filter.FilterFactory;
import org.saig.core.filter.IllegalFilterException;
import org.saig.core.styling.Displacement;
import org.saig.core.styling.ExternalGraphic;
import org.saig.core.styling.Graphic;
import org.saig.core.styling.Mark;
import org.saig.core.styling.MarkImpl;
import org.saig.core.styling.StyleVisitor;
import org.saig.core.styling.Symbol;

public class GraphicImpl
implements Graphic,
Cloneable {
    private static final Logger LOGGER = Logger.getLogger(GraphicImpl.class);
    private static final FilterFactory filterFactory = FilterFactory.createFilterFactory();
    private String geometryPropertyName = "";
    private List<ExternalGraphic> externalGraphics = new ArrayList<ExternalGraphic>();
    private List<Mark> marks = new ArrayList<Mark>();
    private List<Symbol> symbols = new ArrayList<Symbol>();
    private Expression rotation = null;
    private Expression size = null;
    private Displacement displacement = null;
    private Expression opacity = null;

    @Override
    public Expression getOpacity() {
        return this.opacity;
    }

    @Override
    public Expression getRotation() {
        return this.rotation;
    }

    @Override
    public Expression getSize() {
        return this.size;
    }

    @Override
    public Displacement getDisplacement() {
        return this.displacement;
    }

    @Override
    public void setDisplacement(Displacement offset) {
        this.displacement = offset;
    }

    @Override
    public void setOpacity(Expression opacity) {
        this.opacity = opacity;
    }

    public void setOpacity(double opacity) {
        try {
            this.setOpacity(filterFactory.createLiteralExpression(new Double(opacity)));
        }
        catch (IllegalFilterException mfe) {
            GraphicImpl.severe("setOpacity", "Problem setting Opacity", mfe);
        }
    }

    @Override
    public void setRotation(Expression rotation) {
        this.rotation = rotation;
        Iterator<Mark> itMarks = this.marks.iterator();
        while (itMarks.hasNext()) {
            itMarks.next().setRotation(rotation);
        }
    }

    public void setRotation(double rotation) {
        try {
            this.setRotation(filterFactory.createLiteralExpression(new Double(rotation)));
        }
        catch (IllegalFilterException mfe) {
            GraphicImpl.severe("setRotation", "Problem setting Rotation", mfe);
        }
    }

    @Override
    public void setSize(Expression size) {
        this.size = size;
        Iterator<Mark> itMarks = this.marks.iterator();
        while (itMarks.hasNext()) {
            itMarks.next().setSize(size);
        }
    }

    public void setSize(int size) {
        try {
            this.setSize(filterFactory.createLiteralExpression(new Integer(size)));
        }
        catch (IllegalFilterException mfe) {
            GraphicImpl.severe("setSize", "Problem setting Size", mfe);
        }
    }

    @Override
    public void setGeometryPropertyName(String name) {
        this.geometryPropertyName = name;
    }

    @Override
    public String getGeometryPropertyName() {
        return this.geometryPropertyName;
    }

    private static void severe(String method, String message, Exception exception) {
        LOGGER.error((Object)message, (Throwable)exception);
    }

    @Override
    public ExternalGraphic[] getExternalGraphics() {
        ArrayList<ExternalGraphic> exts = new ArrayList<ExternalGraphic>();
        for (Symbol s : this.symbols) {
            if (!(s instanceof ExternalGraphic)) continue;
            exts.add((ExternalGraphic)s);
        }
        return exts.toArray(new ExternalGraphic[0]);
    }

    @Override
    public void setExternalGraphics(ExternalGraphic[] externalGraphics) {
        this.externalGraphics.clear();
        int i = 0;
        while (i < this.symbols.size()) {
            Symbol symbol = this.symbols.get(i);
            if (symbol instanceof ExternalGraphic) {
                this.symbols.remove(i);
                continue;
            }
            ++i;
        }
        if (externalGraphics != null) {
            i = 0;
            while (i < externalGraphics.length) {
                this.addExternalGraphic(externalGraphics[i]);
                ++i;
            }
        }
    }

    @Override
    public void addExternalGraphic(ExternalGraphic externalGraphic) {
        this.externalGraphics.add(externalGraphic);
        this.symbols.add(externalGraphic);
    }

    @Override
    public Mark[] getMarks() {
        Mark[] ret = new Mark[]{};
        if (this.marks.size() > 0) {
            ret = this.marks.toArray(new Mark[0]);
        }
        return ret;
    }

    @Override
    public void setMarks(Mark[] marks) {
        this.marks.clear();
        int i = 0;
        while (i < this.symbols.size()) {
            Symbol symbol = this.symbols.get(i);
            if (symbol instanceof Mark) {
                this.symbols.remove(i);
                continue;
            }
            ++i;
        }
        if (marks == null) {
            return;
        }
        i = 0;
        while (i < marks.length) {
            if (marks.length == 1 || marks.length > 1 && !marks[i].isEmptyMark()) {
                this.addMark(marks[i]);
            }
            ++i;
        }
    }

    @Override
    public void addMark(Mark mark) {
        if (mark == null) {
            return;
        }
        this.marks.add(mark);
        this.symbols.add(mark);
        mark.setSize(this.size);
        mark.setRotation(this.rotation);
    }

    @Override
    public Symbol[] getSymbols() {
        Symbol[] ret = null;
        ret = this.symbols.size() > 0 ? this.symbols.toArray(new Symbol[this.symbols.size()]) : new Symbol[]{MarkImpl.createDefaultMark()};
        return ret;
    }

    @Override
    public void setSymbols(Symbol[] symbols) {
        this.symbols.clear();
        if (symbols != null) {
            int i = 0;
            while (i < symbols.length) {
                this.addSymbol(symbols[i]);
                ++i;
            }
        }
    }

    @Override
    public void addSymbol(Symbol symbol) {
        if (symbol instanceof ExternalGraphic) {
            this.addExternalGraphic((ExternalGraphic)symbol);
        }
        if (symbol instanceof Mark) {
            this.addMark((Mark)symbol);
        }
    }

    @Override
    public Expression getAttributeRotation() {
        return this.getRotation();
    }

    @Override
    public void setAttributeRotation(Expression attrRotation) {
        if (attrRotation != null) {
            this.setRotation(attrRotation);
        }
    }

    @Override
    public void accept(StyleVisitor visitor) {
        visitor.visit(this);
    }

    public Object clone() {
        GraphicImpl clone = new GraphicImpl();
        if (this.getDisplacement() != null) {
            clone.setDisplacement((Displacement)((Cloneable)this.getDisplacement()).clone());
        }
        clone.marks = new ArrayList<Mark>();
        clone.externalGraphics = new ArrayList<ExternalGraphic>();
        clone.symbols = new ArrayList<Symbol>();
        for (ExternalGraphic exGraphic : this.externalGraphics) {
            clone.addExternalGraphic((ExternalGraphic)((Cloneable)exGraphic).clone());
        }
        for (Mark mark : this.marks) {
            clone.addMark((Mark)((Cloneable)mark).clone());
        }
        clone.setGeometryPropertyName(this.getGeometryPropertyName());
        clone.setOpacity(this.getOpacity());
        clone.setRotation(this.getRotation());
        clone.setSize(this.getSize());
        return clone;
    }

    public int hashCode() {
        int PRIME = 1000003;
        int result = 0;
        if (this.geometryPropertyName != null) {
            result = 1000003 * result + this.geometryPropertyName.hashCode();
        }
        if (this.symbols != null) {
            result = 1000003 * result + this.symbols.hashCode();
        }
        if (this.rotation != null) {
            result = 1000003 * result + this.rotation.hashCode();
        }
        if (this.size != null) {
            result = 1000003 * result + this.size.hashCode();
        }
        if (this.opacity != null) {
            result = 1000003 * result + this.opacity.hashCode();
        }
        return result;
    }

    public boolean equals(Object oth) {
        if (this == oth) {
            return true;
        }
        if (oth instanceof GraphicImpl) {
            GraphicImpl other = (GraphicImpl)oth;
            return Utilities.equals((Object)this.geometryPropertyName, (Object)other.geometryPropertyName) && Utilities.equals((Object)this.size, (Object)other.size) && Utilities.equals((Object)this.rotation, (Object)other.rotation) && Utilities.equals((Object)this.opacity, (Object)other.opacity) && Utilities.equals(this.symbols, other.symbols);
        }
        return false;
    }
}


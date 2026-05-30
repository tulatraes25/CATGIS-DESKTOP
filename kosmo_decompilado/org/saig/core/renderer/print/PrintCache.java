/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vividsolutions.jts.geom.Envelope
 *  javax.measure.quantity.Length
 *  javax.measure.unit.Unit
 *  org.apache.log4j.Logger
 *  org.cresques.cts.ICoordTrans
 *  org.geotools.util.NumberRange
 *  org.opengis.referencing.operation.MathTransform2D
 */
package org.saig.core.renderer.print;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.feature.FeatureCollection;
import com.vividsolutions.jump.workbench.model.Layer;
import com.vividsolutions.jump.workbench.model.Layerable;
import com.vividsolutions.jump.workbench.model.WMSLayer;
import com.vividsolutions.jump.workbench.ui.renderer.style.ArrowTerminalDecorator;
import com.vividsolutions.jump.workbench.ui.renderer.style.CircleTerminalDecorator;
import com.vividsolutions.jump.workbench.ui.renderer.style.LineStringSegmentStyle;
import com.vividsolutions.jump.workbench.ui.renderer.style.LineStringVertexStyle;
import com.vividsolutions.jump.workbench.ui.renderer.style.Style;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.measure.quantity.Length;
import javax.measure.unit.Unit;
import org.apache.log4j.Logger;
import org.cresques.cts.ICoordTrans;
import org.geotools.util.NumberRange;
import org.opengis.referencing.operation.MathTransform2D;
import org.saig.core.dao.datasource.filedatasource.shape.geometry.IShapeGeometry;
import org.saig.core.dao.datasource.filedatasource.shape.geometry.ShapeGeometry;
import org.saig.core.dao.datasource.filedatasource.shape.geometry.ShapeGeometryConverter;
import org.saig.core.filter.ExpressionBuilder;
import org.saig.core.filter.Filter;
import org.saig.core.filter.parser.ParseException;
import org.saig.core.renderer.Renderer;
import org.saig.core.renderer.lite.LiteShape2;
import org.saig.core.renderer.print.PrintElement;
import org.saig.core.renderer.print.PrintLayerCache;
import org.saig.core.renderer.style.RuleStyle;
import org.saig.core.styling.Rule;
import org.saig.jump.util.LayerUtil;

public class PrintCache {
    private static Renderer staticRenderer = Renderer.getUniqueInstance();
    private static Logger LOGGER = Logger.getLogger(PrintCache.class);
    private AffineTransform affineTransform;
    private List<Object> cache = new ArrayList<Object>();

    public PrintCache(List<Layerable> layers, Envelope envelope, NumberRange scaleRange, double scale, AffineTransform aft, MathTransform2D transform, double factor, Unit<Length> units) {
        this.affineTransform = aft;
        for (Layerable layerObj : layers) {
            if (layerObj instanceof WMSLayer) {
                this.cache.add(layerObj);
                continue;
            }
            Layer layer = (Layer)layerObj;
            if (!layer.isEnabled()) continue;
            Filter fechaBajaFilter = null;
            if (layer.isVersionable()) {
                try {
                    fechaBajaFilter = (Filter)ExpressionBuilder.parse(layer.getUltimateFeatureCollectionWrapper().getFeatureSchema(), "isNull(" + layer.getEndDateField() + ")");
                }
                catch (ParseException e) {
                    LOGGER.error((Object)"", (Throwable)e);
                }
            }
            FeatureCollection fc = layer.getUltimateFeatureCollectionWrapper();
            Envelope layerEnvelope = null;
            try {
                layerEnvelope = fc.getEnvelope();
            }
            catch (Exception e1) {
                LOGGER.error((Object)"", (Throwable)e1);
                layerEnvelope = new Envelope();
            }
            Envelope viewEnvelope = new Envelope(envelope.getMinX(), envelope.getMaxX(), envelope.getMinY(), envelope.getMaxY());
            ICoordTrans coordTrans = layer.getCoordTrans();
            if (coordTrans != null) {
                Rectangle2D.Double view = new Rectangle2D.Double(envelope.getMinX(), envelope.getMinY(), envelope.getWidth(), envelope.getHeight());
                Rectangle2D viewConverted = layer.getCoordTrans().getInverted().convert((Rectangle2D)view);
                viewEnvelope = new Envelope(viewConverted.getMinX(), viewConverted.getMaxX(), viewConverted.getMinY(), viewConverted.getMaxY());
            }
            if (!viewEnvelope.intersects(layerEnvelope)) continue;
            Object[] rules = this.getRulesInScale(layer.getModelStyle().getSelectedFeatureTypeStyle().getRules(), scaleRange, scale, factor, units);
            List rulesWithFilter = (List)rules[0];
            RuleStyle elseRule = (RuleStyle)rules[1];
            boolean loadLitleShape = (Boolean)rules[2];
            List<Feature> features = null;
            try {
                features = fc.query(viewEnvelope);
            }
            catch (Exception e1) {
                LOGGER.error((Object)"", (Throwable)e1);
                features = new ArrayList<Feature>();
            }
            ArrayList<PrintElement> elements = new ArrayList<PrintElement>();
            for (Feature feature : features) {
                IShapeGeometry pathGeom = ShapeGeometryConverter.jts_to_igeometry(feature.getGeometry());
                if (coordTrans != null) {
                    pathGeom = (ShapeGeometry)pathGeom.cloneGeometry();
                    pathGeom.reProject(coordTrans);
                }
                LiteShape2 liteShape = null;
                try {
                    if (loadLitleShape) {
                        liteShape = staticRenderer.getTransformedShape(pathGeom.toJTSGeometry(), transform);
                    }
                }
                catch (Exception e) {
                    LOGGER.error((Object)"", (Throwable)e);
                }
                pathGeom.getPathShapeInt(aft);
                PrintElement printElement = new PrintElement(liteShape, feature, pathGeom, coordTrans);
                elements.add(printElement);
            }
            this.cache.add(new PrintLayerCache(layer.getName(), rulesWithFilter, elseRule, elements, layer.isRepeated(), layer.isOverlapping(), this.proccessJumpStyles(layer), fechaBajaFilter, layer.isCadLayer(), LayerUtil.isLinealLayer(layer), layerEnvelope.getWidth()));
        }
    }

    private List<Style> proccessJumpStyles(Layer layer) {
        ArrayList<Style> jumpStyles = new ArrayList<Style>();
        for (Style element : layer.getStyles()) {
            if (!(element instanceof ArrowTerminalDecorator) && !(element instanceof CircleTerminalDecorator) && !(element instanceof LineStringSegmentStyle) && !(element instanceof LineStringVertexStyle) || !element.isEnabled()) continue;
            element.initialize(layer);
            jumpStyles.add(element);
        }
        return jumpStyles;
    }

    public Iterator<Object> getIterator() {
        return this.cache.iterator();
    }

    private Object[] getRulesInScale(Rule[] rules, NumberRange scaleRange, double scale, double factor, Unit<Length> units) {
        RuleStyle elseRule = null;
        Boolean liteShape = new Boolean(false);
        ArrayList<RuleStyle> rulesInScale = new ArrayList<RuleStyle>();
        boolean hasPointSymbol = false;
        int i = 0;
        while (i < rules.length) {
            Rule rule = rules[i];
            if ((rule.getMinScaleDenominator() <= scale || Double.isNaN(rule.getMinScaleDenominator())) && (rule.getMaxScaleDenominator() >= scale || Double.isNaN(rule.getMaxScaleDenominator()))) {
                RuleStyle ruleStyle = new RuleStyle(rule, scaleRange, true, factor, factor, units);
                boolean bl = hasPointSymbol = hasPointSymbol || ruleStyle.hasPointSymbolizers();
                if (ruleStyle.isLiteShape()) {
                    liteShape = new Boolean(true);
                }
                if (rule.isElseFilter()) {
                    elseRule = ruleStyle;
                } else {
                    rulesInScale.add(ruleStyle);
                }
            }
            ++i;
        }
        return new Object[]{rulesInScale, elseRule, liteShape, hasPointSymbol};
    }

    public AffineTransform getAffineTransform() {
        return this.affineTransform;
    }
}


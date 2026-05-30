/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vividsolutions.jts.geom.Coordinate
 *  com.vividsolutions.jts.geom.Envelope
 *  com.vividsolutions.jts.geom.Geometry
 *  com.vividsolutions.jts.geom.LineSegment
 *  com.vividsolutions.jts.geom.LineString
 *  com.vividsolutions.jts.geom.MultiLineString
 *  com.vividsolutions.jts.geom.MultiPoint
 *  com.vividsolutions.jts.geom.MultiPolygon
 *  com.vividsolutions.jts.geom.Point
 *  com.vividsolutions.jts.geom.Polygon
 *  javax.measure.quantity.Length
 *  javax.measure.unit.Unit
 *  javax.media.jai.util.Range
 *  org.apache.log4j.Logger
 *  org.cresques.cts.ICoordTrans
 *  org.cresques.px.dxf.AcadColor
 *  org.geotools.referencing.operation.GeneralMatrix
 *  org.geotools.util.NumberRange
 *  org.opengis.referencing.FactoryException
 *  org.opengis.referencing.operation.MathTransform2D
 *  org.opengis.referencing.operation.Matrix
 */
package org.saig.core.renderer.print;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineSegment;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.MultiPoint;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.workbench.model.Layerable;
import com.vividsolutions.jump.workbench.model.WMSLayer;
import com.vividsolutions.jump.workbench.ui.LayerViewPanel;
import com.vividsolutions.jump.workbench.ui.Viewport;
import com.vividsolutions.jump.workbench.ui.renderer.style.Style;
import es.kosmo.core.renderer.label.LabelCacheImpl;
import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.LinearGradientPaint;
import java.awt.Paint;
import java.awt.RadialGradientPaint;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.TexturePaint;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RectangularShape;
import java.awt.image.BufferedImage;
import java.awt.print.PageFormat;
import java.awt.print.PrinterException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import javax.measure.quantity.Length;
import javax.measure.unit.Unit;
import javax.media.jai.util.Range;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import org.apache.log4j.Logger;
import org.cresques.cts.ICoordTrans;
import org.cresques.px.dxf.AcadColor;
import org.geotools.referencing.operation.GeneralMatrix;
import org.geotools.util.NumberRange;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.operation.MathTransform2D;
import org.opengis.referencing.operation.Matrix;
import org.saig.core.dao.coverage.Coverage;
import org.saig.core.dao.coverage.GridCoverageCollection;
import org.saig.core.dao.datasource.filedatasource.shape.geometry.IShapeGeometry;
import org.saig.core.dao.datasource.filedatasource.shape.geometry.ShapeGeometryConverter;
import org.saig.core.filter.Expression;
import org.saig.core.filter.Filter;
import org.saig.core.geometry.Circle;
import org.saig.core.geometry.Ellipse;
import org.saig.core.gui.swing.sldeditor.util.SymbolizerUtils;
import org.saig.core.renderer.Renderer;
import org.saig.core.renderer.RendererParameterWrapper;
import org.saig.core.renderer.lite.LabelCache;
import org.saig.core.renderer.lite.LiteShape2;
import org.saig.core.renderer.lite.StyledShapePainter;
import org.saig.core.renderer.print.PrintCache;
import org.saig.core.renderer.print.PrintElement;
import org.saig.core.renderer.print.PrintLayerCache;
import org.saig.core.renderer.print.wms.WMSPrintUtils;
import org.saig.core.renderer.style.GraphicStyle2D;
import org.saig.core.renderer.style.LineStyle2D;
import org.saig.core.renderer.style.MarkStyle2D;
import org.saig.core.renderer.style.PolygonStyle2D;
import org.saig.core.renderer.style.RuleStyle;
import org.saig.core.renderer.style.SLDStyleFactory;
import org.saig.core.renderer.style.Style2D;
import org.saig.core.styling.RasterSymbolizer;
import org.saig.core.styling.TextSymbolizer;
import org.saig.jump.widgets.print.Conversion;
import org.saig.jump.widgets.print.PrintLayoutFrame;
import org.saig.jump.widgets.print.actions.PrintOptions;
import org.saig.jump.widgets.print.elements.GraphicElements;
import org.saig.jump.widgets.print.elements.geometry.GeometryFrame;
import org.saig.jump.widgets.print.elements.geometry.LineElement;
import org.saig.jump.widgets.print.elements.image.ImageFrame;
import org.saig.jump.widgets.print.elements.legend.LegendFrame;
import org.saig.jump.widgets.print.elements.map.MapFrame;
import org.saig.jump.widgets.print.elements.north.NorthFrame;
import org.saig.jump.widgets.print.elements.scale.MapScale;
import org.saig.jump.widgets.print.elements.scale.ScaleFrame;
import org.saig.jump.widgets.print.elements.text.GraphicText;

public class PrintRenderer {
    private static final Logger LOGGER = Logger.getLogger(PrintRenderer.class);
    protected static final SLDStyleFactory styleFactory = new SLDStyleFactory();
    private static PrintCache printCache;
    private Point2D.Double offset = null;
    private Dimension imageSize = null;
    private AffineTransform m_MatrizTransf;
    private Rectangle rect = new Rectangle();
    private Rectangle2D.Double box = null;
    private Rectangle interseccion;
    private Envelope intersectionEnvelope;
    private Rectangle2D.Double rIntersection;
    private static Renderer staticRenderer;
    private static StyledShapePainter painter;
    private static NumberRange scaleRange;
    private static Rectangle viewBox;
    private Viewport viewport;
    private double viewWidth;
    private double printScale = 1.0;

    static {
        staticRenderer = Renderer.getUniqueInstance();
        painter = new StyledShapePainter();
    }

    public void fillCache(Envelope envelope, List<Layerable> layers, int pageWidth, double paperSize, Rectangle vistaGrafica, AffineTransform affineTransform, double factor, Unit<Length> units) throws NoninvertibleTransformException, FactoryException {
        try {
            MathTransform2D transform = (MathTransform2D)Renderer.mathTransformFactory.createAffineTransform((Matrix)new GeneralMatrix(affineTransform));
            printCache = new PrintCache(layers, envelope, scaleRange, PrintRenderer.calculatePrintScale(pageWidth, (int)vistaGrafica.getWidth(), paperSize, envelope.getWidth()), affineTransform, transform, factor, units);
        }
        catch (Exception e1) {
            LOGGER.error((Object)"", (Throwable)e1);
        }
    }

    public static void eraseCache() {
        LOGGER.info((Object)"limpiando la cache de impresi\u00f3n");
        printCache = null;
        viewBox = null;
        scaleRange = null;
    }

    public void print(Graphics2D printGraphics, List<GraphicElements> graphicsElements) {
        RenderingHints renderHints = new RenderingHints(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        renderHints.put(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        renderHints.put(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        printGraphics.setRenderingHints(renderHints);
        PageFormat format = null;
        GraphicElements elem = graphicsElements.get(0);
        format = elem.getParent().getPageFormat();
        Rectangle marginClip = new Rectangle((int)format.getImageableX(), (int)format.getImageableY(), (int)format.getImageableWidth(), (int)format.getImageableHeight());
        for (GraphicElements element : graphicsElements) {
            GraphicElements geometria;
            Composite comp;
            ImageIcon trueIcon;
            ImageIcon icono;
            if (element.getClass().equals(MapFrame.class)) {
                printGraphics.setClip(marginClip);
                MapFrame mapa = (MapFrame)element;
                LayerViewPanel panel = (LayerViewPanel)mapa.getGraphicElementsForPrint();
                Rectangle vistaGraficaImpresion = panel.getBounds();
                this.viewport = panel.getViewport();
                Rectangle newVista = new Rectangle((int)((double)vistaGraficaImpresion.x + format.getImageableX()), (int)((double)vistaGraficaImpresion.y + format.getImageableY()), vistaGraficaImpresion.width, vistaGraficaImpresion.height);
                Envelope envelopeImpresion = this.viewport.getEnvelopeInModelCoordinates();
                this.viewWidth = envelopeImpresion.getWidth();
                try {
                    this.printView(printGraphics, format, envelopeImpresion, mapa.getParent().getPage().getWidth(), format.getWidth(), newVista, mapa);
                }
                catch (Exception e) {
                    LOGGER.error((Object)"", (Throwable)e);
                }
                continue;
            }
            if (element.getClass().equals(GraphicText.class)) {
                printGraphics.setClip(marginClip);
                GraphicText texto = (GraphicText)element;
                AffineTransform oldTransform = printGraphics.getTransform();
                AffineTransform translationTransform = new AffineTransform(oldTransform);
                translationTransform.translate((double)texto.getPrintX() + format.getImageableX(), (double)texto.getPrintY() + format.getImageableY());
                printGraphics.setTransform(translationTransform);
                boolean doubleBuffered = texto.getGraphicElementsForPrint().isDoubleBuffered();
                texto.getGraphicElementsForPrint().setDoubleBuffered(false);
                texto.getGraphicElementsForPrint().print(printGraphics);
                texto.getGraphicElementsForPrint().setDoubleBuffered(doubleBuffered);
                printGraphics.setTransform(oldTransform);
                continue;
            }
            if (element.getClass().equals(NorthFrame.class)) {
                printGraphics.setClip(marginClip);
                NorthFrame norte = (NorthFrame)element;
                icono = norte.getNorthSymbol();
                trueIcon = (ImageIcon)((JLabel)norte.getGraphicElementsForPrint()).getIcon();
                int startX = (int)((double)norte.getPrintX() + format.getImageableX() + (double)((norte.getPrintWidth() - trueIcon.getIconWidth()) / 2));
                int startY = (int)((double)norte.getPrintY() + format.getImageableY() + (double)((norte.getPrintHeight() - trueIcon.getIconHeight()) / 2));
                comp = printGraphics.getComposite();
                printGraphics.setComposite(AlphaComposite.getInstance(3, 1.0f));
                printGraphics.drawImage(icono.getImage(), startX, startY, trueIcon.getIconWidth(), trueIcon.getIconHeight(), null);
                printGraphics.setComposite(comp);
                continue;
            }
            if (element.getClass().equals(ImageFrame.class)) {
                printGraphics.setClip(marginClip);
                ImageFrame imagen = (ImageFrame)element;
                icono = imagen.getImageSymbol();
                trueIcon = (ImageIcon)((JLabel)imagen.getGraphicElementsForPrint()).getIcon();
                int startX = (int)((double)imagen.getPrintX() + format.getImageableX() + (double)((imagen.getPrintWidth() - trueIcon.getIconWidth()) / 2));
                int startY = (int)((double)imagen.getPrintY() + format.getImageableY() + (double)((imagen.getPrintHeight() - trueIcon.getIconHeight()) / 2));
                comp = printGraphics.getComposite();
                printGraphics.setComposite(AlphaComposite.getInstance(3, 1.0f));
                printGraphics.drawImage(icono.getImage(), startX, startY, trueIcon.getIconWidth(), trueIcon.getIconHeight(), null);
                printGraphics.setComposite(comp);
                continue;
            }
            if (element.getClass().equals(ScaleFrame.class)) {
                printGraphics.setClip(marginClip);
                ScaleFrame scaleFrame = (ScaleFrame)element;
                MapScale scale = scaleFrame.getMapScale();
                AffineTransform oldTransform = printGraphics.getTransform();
                AffineTransform translationTransform = new AffineTransform(oldTransform);
                translationTransform.translate(scaleFrame.getPrintX() + (int)format.getImageableX(), scaleFrame.getPrintY() + (int)format.getImageableY());
                printGraphics.setTransform(translationTransform);
                Composite comp2 = printGraphics.getComposite();
                printGraphics.setComposite(AlphaComposite.getInstance(3, 1.0f));
                scale.print(printGraphics, 0, 0);
                printGraphics.setComposite(comp2);
                printGraphics.setTransform(oldTransform);
                continue;
            }
            if (element.getClass().equals(LegendFrame.class)) {
                printGraphics.setClip(marginClip);
                LegendFrame leyenda = (LegendFrame)element;
                leyenda.print(printGraphics, leyenda.getPrintX() + (int)format.getImageableX(), leyenda.getPrintY() + (int)format.getImageableY());
                continue;
            }
            if (element.getClass().equals(GeometryFrame.class)) {
                printGraphics.setClip(marginClip);
                geometria = (GeometryFrame)element;
                ((GeometryFrame)geometria).print(printGraphics, (int)format.getImageableX() + ((GeometryFrame)geometria).getPrintX(), (int)format.getImageableY() + ((GeometryFrame)geometria).getPrintY());
                continue;
            }
            if (!element.getClass().equals(LineElement.class)) continue;
            printGraphics.setClip(marginClip);
            geometria = (LineElement)element;
            ((LineElement)geometria).print(printGraphics, (int)format.getImageableX() + ((LineElement)geometria).getPrintX(), (int)format.getImageableY() + ((LineElement)geometria).getPrintY());
        }
    }

    private void printView(Graphics2D g, PageFormat format, Envelope envelope, int pageWidth, double paperSize, Rectangle vistaGrafica, MapFrame component) throws Exception {
        LOGGER.info((Object)("Clip 0 = " + g.getClip()));
        this.interseccion = vistaGrafica.intersection(g.getClipBounds());
        if (envelope != null && !this.interseccion.equals(vistaGrafica)) {
            double x1 = this.interseccion.getMinX();
            double y1 = this.interseccion.getMinY();
            Coordinate c1 = staticRenderer.pixelToWorld((int)x1, (int)y1, envelope, vistaGrafica);
            double x2 = this.interseccion.getMaxX();
            double y2 = this.interseccion.getMaxY();
            Coordinate c2 = staticRenderer.pixelToWorld((int)x2, (int)y2, envelope, vistaGrafica);
            this.intersectionEnvelope = new Envelope(c1, c2);
        } else {
            this.intersectionEnvelope = envelope;
        }
        this.initRect(format);
        this.box = new Rectangle2D.Double(Conversion.seventyTwoInch_To_Cm(vistaGrafica.getX()), Conversion.seventyTwoInch_To_Cm(vistaGrafica.getY()), Conversion.seventyTwoInch_To_Cm(vistaGrafica.getWidth()), Conversion.seventyTwoInch_To_Cm(vistaGrafica.getHeight()));
        Rectangle2D.Double boxIntersection = new Rectangle2D.Double(Conversion.seventyTwoInch_To_Cm(this.interseccion.getX()), Conversion.seventyTwoInch_To_Cm(this.interseccion.getY()), Conversion.seventyTwoInch_To_Cm(this.interseccion.getWidth()), Conversion.seventyTwoInch_To_Cm(this.interseccion.getHeight()));
        AffineTransform at = g.getTransform();
        g.translate(0, 0);
        g.scale(72.0 / (double)PrintOptions.printQuality, 72.0 / (double)PrintOptions.printQuality);
        LOGGER.info((Object)("Clip 1=" + g.getClip()));
        double scale = this.rect.getHeight() / Conversion.seventyTwoInch_To_Cm(format.getHeight());
        AffineTransform escalado = new AffineTransform();
        AffineTransform translacion = new AffineTransform();
        translacion.setToTranslation(this.rect.getMinX(), this.rect.getMinY());
        escalado.setToScale(scale, scale);
        this.m_MatrizTransf = new AffineTransform();
        this.m_MatrizTransf.setToIdentity();
        this.m_MatrizTransf.concatenate(translacion);
        this.m_MatrizTransf.concatenate(escalado);
        double[] m_area = new double[]{PrintLayoutFrame.TOP_MARGIN, PrintLayoutFrame.BOTTOM_MARGIN, PrintLayoutFrame.LEFT_MARGIN, PrintLayoutFrame.RIGHT_MARGIN};
        g.setClip((int)(this.rect.getMinX() + PrintRenderer.fromSheetDistance(m_area[2], this.m_MatrizTransf)), (int)(this.rect.getMinY() + PrintRenderer.fromSheetDistance(m_area[0], this.m_MatrizTransf)), (int)(this.rect.getWidth() - PrintRenderer.fromSheetDistance(m_area[2] + m_area[3], this.m_MatrizTransf)), (int)(this.rect.getHeight() - PrintRenderer.fromSheetDistance(m_area[0] + m_area[1], this.m_MatrizTransf)));
        LOGGER.info((Object)("Clip 2=" + g.getClip()));
        Rectangle2D.Double r = PrintRenderer.getBoundingBox(this.box, this.m_MatrizTransf);
        this.rIntersection = PrintRenderer.getBoundingBox(boxIntersection, this.m_MatrizTransf);
        Rectangle rclip = g.getClipBounds();
        LOGGER.info((Object)("Clip 3=" + r));
        if (this.interseccion.equals(vistaGrafica)) {
            g.clipRect((int)r.getMinX(), (int)r.getMinY(), (int)r.getWidth(), (int)r.getHeight());
        } else {
            g.clipRect((int)this.rIntersection.getMinX(), (int)this.rIntersection.getMinY(), (int)this.rIntersection.getWidth(), (int)this.rIntersection.getHeight());
        }
        this.offset = new Point2D.Double(r.x, r.y);
        this.imageSize = new Dimension((int)r.width, (int)r.height);
        this.printMap(component, envelope, pageWidth, paperSize, vistaGrafica, r, g);
        g.setClip(rclip.x, rclip.y, rclip.width, rclip.height);
        g.setTransform(at);
    }

    private void printMap(MapFrame mapa, Envelope envelope, int pageWidth, double paperSize, Rectangle vistaGrafica, Rectangle2D.Double r, Graphics2D g) throws Exception {
        AffineTransform affineTransform = this.calculateAffineTransform(new Rectangle2D.Double(envelope.getMinX(), envelope.getMinY(), envelope.getWidth(), envelope.getHeight()));
        if (affineTransform == null) {
            LOGGER.warn((Object)"La transformada es nula");
            return;
        }
        MathTransform2D transform = (MathTransform2D)Renderer.mathTransformFactory.createAffineTransform((Matrix)new GeneralMatrix(affineTransform));
        if (scaleRange == null) {
            double scaleDenominator = 1.0 / affineTransform.getScaleX();
            scaleRange = new NumberRange(scaleDenominator, scaleDenominator);
        }
        if (printCache == null) {
            LayerViewPanel panel = (LayerViewPanel)mapa.getGraphicElementsForPrint();
            List<Layerable> layers = panel.getLayerManager().getVisibleLayerables();
            Collections.reverse(layers);
            try {
                this.fillCache(envelope, layers, pageWidth, paperSize, vistaGrafica, affineTransform, 1.0 / (72.0 / (double)PrintOptions.printQuality), mapa.getUserLengthUnits());
            }
            catch (Exception e) {
                LOGGER.error((Object)"", (Throwable)e);
                throw new PrinterException(e.getMessage());
            }
        }
        if (viewBox == null) {
            viewBox = new Rectangle((int)r.x, (int)r.y, (int)r.width, (int)r.height);
        }
        g.clipRect(PrintRenderer.viewBox.x, PrintRenderer.viewBox.y, PrintRenderer.viewBox.width, PrintRenderer.viewBox.height);
        Iterator<Object> itElements = printCache.getIterator();
        while (itElements.hasNext()) {
            Object obj = itElements.next();
            if (obj instanceof WMSLayer) {
                WMSLayer wmsLayer = (WMSLayer)obj;
                Rectangle rectangle = this.rIntersection.getBounds();
                try {
                    WMSPrintUtils.printWMSLayer(wmsLayer, rectangle, envelope, g);
                }
                catch (Exception e) {
                    LOGGER.error((Object)"", (Throwable)e);
                }
                continue;
            }
            PrintLayerCache layerCache = (PrintLayerCache)obj;
            LabelCacheImpl labelCache = new LabelCacheImpl(layerCache.isOverlapping(), layerCache.isRepeated());
            labelCache.start();
            labelCache.startLayer(layerCache.getLayerName());
            List<PrintElement> elements = layerCache.getElements();
            for (PrintElement element : elements) {
                try {
                    if (!layerCache.isCad()) {
                        this.processShape(layerCache.getRulesWithFilter(), layerCache.getElseRule(), labelCache, element, g, envelope, layerCache.getJumpStyles(), affineTransform, layerCache.getFechaBajaFilter(), layerCache.getLayerWidth(), layerCache.isLineLayer(), transform, layerCache.getLayerName());
                        continue;
                    }
                    this.processCadShape(layerCache.getRulesWithFilter(), layerCache.getElseRule(), labelCache, element, g, envelope, layerCache.getJumpStyles(), affineTransform, layerCache.getFechaBajaFilter(), layerCache.getLayerWidth(), layerCache.getLayerName());
                }
                catch (Exception e) {
                    LOGGER.error((Object)"", (Throwable)e);
                    throw new PrinterException(e.getMessage());
                }
            }
            labelCache.endLayer(layerCache.getLayerName(), g, viewBox);
            labelCache.end(g, viewBox);
            labelCache.stop();
        }
        g.setStroke(new BasicStroke(1.0f));
        g.setColor(Color.BLACK);
        g.drawRect(PrintRenderer.viewBox.x, PrintRenderer.viewBox.y, PrintRenderer.viewBox.width, PrintRenderer.viewBox.height);
    }

    private void processShape(List<RuleStyle> rulesWithFilter, RuleStyle elseRule, LabelCache labelCache, PrintElement printElement, Graphics2D g, Envelope envelope, List<Style> jumpStyles, AffineTransform aft, Filter fechaBajaFilter, double layerWidth, boolean isLineLayer, MathTransform2D transform, String layerId) {
        boolean check = false;
        for (RuleStyle ruleStyle : rulesWithFilter) {
            if (ruleStyle.getRasterSymbol() != null) {
                RasterSymbolizer symbol = ruleStyle.getRasterSymbol();
                this.printRaster(g, printElement.getFeature(), symbol, printElement.getTransf());
                continue;
            }
            Filter filter = null;
            filter = fechaBajaFilter != null ? (ruleStyle.getFilter() != null ? ruleStyle.getFilter().and(fechaBajaFilter) : fechaBajaFilter) : ruleStyle.getFilter();
            if (filter != null && !filter.contains(printElement.getFeature())) continue;
            check = true;
            this.drawShape(g, ruleStyle, labelCache, printElement, layerWidth, isLineLayer, transform, layerId);
        }
        if (elseRule != null && !check) {
            if (elseRule.getRasterSymbol() != null) {
                RasterSymbolizer symbol = elseRule.getRasterSymbol();
                this.printRaster(g, printElement.getFeature(), symbol, printElement.getTransf());
                return;
            }
            if (fechaBajaFilter == null || fechaBajaFilter.contains(printElement.getFeature())) {
                this.drawShape(g, elseRule, labelCache, printElement, layerWidth, isLineLayer, transform, layerId);
                check = true;
            }
        }
        if (check && jumpStyles != null && jumpStyles.size() > 0) {
            Viewport vp = new Viewport(aft);
            for (Style element : jumpStyles) {
                try {
                    element.paint(printElement.getFeature(), g, vp);
                }
                catch (Exception e) {
                    LOGGER.error((Object)"", (Throwable)e);
                }
            }
        }
    }

    private void processCadShape(List<RuleStyle> rulesWithFilter, RuleStyle elseRule, LabelCache labelCache, PrintElement printElement, Graphics2D g, Envelope envelope, List<Style> jumpStyles, AffineTransform aft, Filter fechaBajaFilter, double layerWidth, String layerId) {
        this.drawCadShape(g, rulesWithFilter.iterator().next(), labelCache, printElement, layerWidth, layerId);
    }

    /*
     * Unable to fully structure code
     */
    private void drawCadShape(Graphics2D g2, RuleStyle ruleStyle, LabelCache labelCache, PrintElement printElement, double layerWidth, String layerId) {
        block30: {
            block29: {
                block31: {
                    if (printElement == null || ruleStyle == null) {
                        return;
                    }
                    feature = printElement.getFeature();
                    if (ruleStyle == null || feature.getGeometry() == null || feature.getGeometry().isEmpty()) {
                        return;
                    }
                    style = null;
                    featGeom = feature.getGeometry();
                    isPoint = featGeom instanceof Point != false || featGeom instanceof MultiPoint != false;
                    isText = ((String)feature.getAttribute("Entity")).equalsIgnoreCase("text");
                    isSolid = ((String)feature.getAttribute("Entity")).equalsIgnoreCase("solid");
                    isLine = featGeom instanceof LineString != false || featGeom instanceof MultiLineString != false;
                    isPolygon = featGeom instanceof Polygon != false || featGeom instanceof MultiPolygon != false;
                    color = feature.getAttribute("Color");
                    objColor = null;
                    if (color != null) {
                        objColor = AcadColor.getColor((int)((Number)color).intValue());
                    }
                    thicknessObj = feature.getAttribute("Thickness");
                    thickness = 1;
                    if (thicknessObj != null && (thickness = ((Number)thicknessObj).intValue()) == 0) {
                        thickness = 1;
                    }
                    style = isPoint != false ? PrintRenderer.styleFactory.createStyle(feature, SymbolizerUtils.getPointSymbolizer(objColor, thickness), (Range)PrintRenderer.scaleRange) : (isPolygon != false ? PrintRenderer.styleFactory.createStyle(feature, SymbolizerUtils.getPolygonSymbolizer(objColor, thickness), (Range)PrintRenderer.scaleRange) : PrintRenderer.styleFactory.createStyle(feature, SymbolizerUtils.getLineSymbolizer(objColor, thickness), (Range)PrintRenderer.scaleRange));
                    shape = printElement.getShape();
                    try {
                        try {
                            if (style instanceof MarkStyle2D && isPoint && !isText) {
                                if (objColor != null) {
                                    ((MarkStyle2D)style).setFill(objColor);
                                }
                                ((MarkStyle2D)style).setSize(thickness);
                                PrintRenderer.painter.paint(g2, shape, style, 1.0);
                            } else if (style instanceof PolygonStyle2D && isPolygon && isSolid && (polygonStyle2D = (PolygonStyle2D)style).getFill() != null) {
                                paint = this.getPaint(polygonStyle2D.getFill(), g2.getTransform(), shape.getBounds2D());
                                if (objColor != null) {
                                    paint = objColor;
                                }
                                if (!(featGeom instanceof Ellipse) && !(featGeom instanceof Circle)) {
                                    g2.setPaint(paint);
                                }
                                g2.fill(shape);
                            }
                            if (style instanceof LineStyle2D && (isPolygon || isLine) && (ls2d = (LineStyle2D)style).getStroke() != null) {
                                paint = this.getPaint(ls2d.getContour(), g2.getTransform(), shape.getBounds2D());
                                stroke = ls2d.getStroke();
                                if (objColor != null) {
                                    paint = objColor;
                                }
                                g2.setPaint(paint);
                                g2.setStroke(stroke);
                                g2.setComposite(ls2d.getContourComposite());
                                g2.draw(shape);
                            }
                            break block29;
                        }
                        catch (Exception e) {
                            PrintRenderer.LOGGER.error((Object)"", (Throwable)e);
                            if (!ruleStyle.hasTextSymbolizers() || shape == null || !isPoint) break block30;
                            ** for (textSymbol : ruleStyle.getTextSymbolizers())
                        }
                    }
                    catch (Throwable var24_48) {
                        if (!ruleStyle.hasTextSymbolizers() || shape == null || !isPoint) break block31;
                        ** for (textSymbol : ruleStyle.getTextSymbolizers())
                    }
lbl-1000:
                    // 1 sources

                    {
                        factor = 1.0;
                        scaleTextHeight = 1.0;
                        textHeight = (Number)feature.getAttribute("HeightText");
                        if (textSymbol.isScale()) {
                            max = textSymbol.getScaleMaxValue();
                            min = textSymbol.getScaleMinValue();
                            factor = max - Math.tan(Math.atan((max - min) / layerWidth)) * this.viewWidth;
                            scaleTextHeight = textHeight.floatValue() * (float)factor;
                            if (scaleTextHeight < min) {
                                scaleTextHeight = min;
                            }
                        } else {
                            scaleTextHeight = textHeight.doubleValue() * this.printScale;
                        }
                        textRotation = (Number)feature.getAttribute("RotationText");
                        labelCache.put(layerId, textSymbol, feature, shape, null, (Range)PrintRenderer.scaleRange, new Float(scaleTextHeight), objColor, new Double(textRotation.doubleValue()));
                        continue;
lbl67:
                        // 1 sources

                        break block30;
                    }
lbl-1000:
                    // 1 sources

                    {
                        factor = 1.0;
                        scaleTextHeight = 1.0;
                        textHeight = (Number)feature.getAttribute("HeightText");
                        if (textSymbol.isScale()) {
                            max = textSymbol.getScaleMaxValue();
                            min = textSymbol.getScaleMinValue();
                            factor = max - Math.tan(Math.atan((max - min) / layerWidth)) * this.viewWidth;
                            scaleTextHeight = textHeight.floatValue() * (float)factor;
                            if (scaleTextHeight < min) {
                                scaleTextHeight = min;
                            }
                        } else {
                            scaleTextHeight = textHeight.doubleValue() * this.printScale;
                        }
                        textRotation = (Number)feature.getAttribute("RotationText");
                        labelCache.put(layerId, textSymbol, feature, shape, null, (Range)PrintRenderer.scaleRange, new Float(scaleTextHeight), objColor, new Double(textRotation.doubleValue()));
                        continue;
                    }
                }
                throw var24_48;
            }
            if (ruleStyle.hasTextSymbolizers() && shape != null && isPoint) {
                for (TextSymbolizer textSymbol : ruleStyle.getTextSymbolizers()) {
                    factor = 1.0;
                    scaleTextHeight = 1.0;
                    textHeight = (Number)feature.getAttribute("HeightText");
                    if (textSymbol.isScale()) {
                        max = textSymbol.getScaleMaxValue();
                        min = textSymbol.getScaleMinValue();
                        factor = max - Math.tan(Math.atan((max - min) / layerWidth)) * this.viewWidth;
                        scaleTextHeight = textHeight.floatValue() * (float)factor;
                        if (scaleTextHeight < min) {
                            scaleTextHeight = min;
                        }
                    } else {
                        scaleTextHeight = textHeight.doubleValue() * this.printScale;
                    }
                    textRotation = (Number)feature.getAttribute("RotationText");
                    labelCache.put(layerId, textSymbol, feature, shape, null, (Range)PrintRenderer.scaleRange, new Float(scaleTextHeight), objColor, new Double(textRotation.doubleValue()));
                }
            }
        }
    }

    private void drawShape(Graphics2D g2, RuleStyle ruleStyle, LabelCache labelCache, PrintElement printElement, double layerWidth, boolean isLineLayer, MathTransform2D transform, String layerId) {
        if (printElement == null || ruleStyle == null) {
            return;
        }
        for (Style2D style : ruleStyle.getStyles()) {
            LineStyle2D ls2d;
            Paint paint;
            PolygonStyle2D polygonStyle2D;
            float angle;
            LineSegment segment;
            Point centroide;
            LineString lineStr;
            Style2D newStyle;
            LiteShape2 shape;
            IShapeGeometry geom;
            ICoordTrans ct;
            Feature feature;
            if (style instanceof MarkStyle2D) {
                feature = printElement.getFeature();
                ct = printElement.getTransf();
                geom = null;
                shape = printElement.getShape();
                newStyle = styleFactory.createStyle(feature, ruleStyle.getSymbol(style), (Range)scaleRange);
                if (isLineLayer) {
                    lineStr = (LineString)feature.getGeometry();
                    centroide = lineStr.getCentroid();
                    segment = new LineSegment(lineStr.getStartPoint().getCoordinate(), lineStr.getEndPoint().getCoordinate());
                    angle = (float)segment.angle();
                    try {
                        if (ct != null) {
                            geom = ShapeGeometryConverter.jts_to_igeometry((Geometry)centroide);
                            geom.reProject(ct);
                            shape = staticRenderer.getTransformedShape(ShapeGeometryConverter.java2d_to_jts(geom.getShp()), transform);
                        } else {
                            shape = staticRenderer.getTransformedShape((Geometry)centroide, transform);
                        }
                    }
                    catch (Exception e) {
                        LOGGER.error((Object)"", (Throwable)e);
                    }
                    ((MarkStyle2D)newStyle).setRotation(angle);
                }
                painter.paint(g2, shape, newStyle, 1.0);
            } else if (style instanceof GraphicStyle2D) {
                feature = printElement.getFeature();
                ct = printElement.getTransf();
                geom = null;
                shape = printElement.getShape();
                newStyle = styleFactory.createStyle(feature, ruleStyle.getSymbol(style), (Range)scaleRange);
                if (isLineLayer) {
                    lineStr = (LineString)feature.getGeometry();
                    centroide = lineStr.getCentroid();
                    segment = new LineSegment(lineStr.getStartPoint().getCoordinate(), lineStr.getEndPoint().getCoordinate());
                    angle = (float)segment.angle();
                    try {
                        if (ct != null) {
                            geom = ShapeGeometryConverter.jts_to_igeometry((Geometry)centroide);
                            geom.reProject(ct);
                            shape = staticRenderer.getTransformedShape(ShapeGeometryConverter.java2d_to_jts(geom.getShp()), transform);
                        } else {
                            shape = staticRenderer.getTransformedShape((Geometry)centroide, transform);
                        }
                    }
                    catch (Exception e) {
                        LOGGER.error((Object)"", (Throwable)e);
                    }
                    ((GraphicStyle2D)newStyle).setRotation(angle);
                }
                ((GraphicStyle2D)newStyle).setRotation(((GraphicStyle2D)newStyle).getRotation() * -1.0f);
                painter.paint(g2, shape, newStyle, 1.0);
            } else if (style instanceof PolygonStyle2D && (polygonStyle2D = (PolygonStyle2D)style).getFill() != null) {
                paint = this.getPaint(polygonStyle2D.getFill(), g2.getTransform(), printElement.getPathShape().getShp().getBounds2D());
                g2.setPaint(paint);
                g2.setComposite(polygonStyle2D.getFillComposite());
                g2.fill(printElement.getPathShape().getShp());
            }
            if (!(style instanceof LineStyle2D) || (ls2d = (LineStyle2D)style).getStroke() == null || ls2d.getGraphicStroke() != null) continue;
            paint = this.getPaint(ls2d.getContour(), g2.getTransform(), printElement.getPathShape().getShp().getBounds2D());
            Stroke stroke = ls2d.getStroke();
            g2.setPaint(paint);
            g2.setStroke(stroke);
            g2.setComposite(ls2d.getContourComposite());
            g2.draw(printElement.getPathShape().getShp());
        }
        if (ruleStyle.hasTextSymbolizers()) {
            Feature feature = printElement.getFeature();
            for (TextSymbolizer textSymbol : ruleStyle.getTextSymbolizers()) {
                double min;
                double max;
                double factor = 1.0;
                if (textSymbol.isScale() && (factor = (max = textSymbol.getScaleMaxValue()) - Math.tan(Math.atan((max - (min = textSymbol.getScaleMinValue())) / layerWidth)) * this.viewWidth) < min) {
                    factor = min;
                }
                Float height = null;
                if (textSymbol.getHeightAttribute() != null) {
                    Number textHeight = (Number)textSymbol.getHeightAttribute().getValue(feature);
                    if (textHeight != null) {
                        height = Float.valueOf(textHeight.floatValue() * (float)factor);
                    } else if (textSymbol.isScale()) {
                        height = Float.valueOf((float)factor);
                    }
                } else if (textSymbol.isScale()) {
                    height = Float.valueOf((float)factor);
                }
                Number rotation = null;
                if (textSymbol.getAttributeRotation() != null) {
                    rotation = (Number)textSymbol.getAttributeRotation().getValue(feature);
                }
                labelCache.put(layerId, textSymbol, printElement.getFeature(), printElement.getShape(), null, (Range)scaleRange, height, null, rotation);
            }
        }
    }

    private void initRect(PageFormat format) {
        double value1 = Conversion.seventyTwoInch_To_Cm(format.getWidth());
        double value2 = Conversion.seventyTwoInch_To_Cm(format.getHeight());
        this.rect.setRect(0.0, 0.0, value1 / 2.54 * (double)PrintOptions.printQuality, value2 / 2.54 * (double)PrintOptions.printQuality);
    }

    public static Rectangle2D.Double getBoundingBox(Rectangle2D r, AffineTransform at) {
        Point2D.Double pSheet = new Point2D.Double(r.getX(), r.getY());
        Point2D.Double pSX = new Point2D.Double(r.getMaxX(), r.getMinY());
        Point2D.Double pSY = new Point2D.Double(r.getMinX(), r.getMaxY());
        Point2D.Double pScreen = new Point2D.Double();
        Point2D.Double pScreenX = new Point2D.Double();
        Point2D.Double pScreenY = new Point2D.Double();
        try {
            at.transform(pSheet, pScreen);
            at.transform(pSX, pScreenX);
            at.transform(pSY, pScreenY);
        }
        catch (Exception e) {
            System.err.print(e.getMessage());
        }
        Rectangle2D.Double res = new Rectangle2D.Double();
        res.setRect(pScreen.getX(), pScreen.getY(), pScreen.distance(pScreenX), pScreen.distance(pScreenY));
        return res;
    }

    private AffineTransform calculateAffineTransform(Rectangle2D extent) {
        if (this.imageSize == null || extent == null || this.imageSize.getWidth() <= 0.0 || this.imageSize.getHeight() <= 0.0) {
            return null;
        }
        AffineTransform trans = new AffineTransform();
        AffineTransform escalado = new AffineTransform();
        AffineTransform translacion = new AffineTransform();
        double escalaX = this.imageSize.getWidth() / extent.getWidth();
        double escalaY = this.imageSize.getHeight() / extent.getHeight();
        double xCenter = extent.getCenterX();
        double yCenter = extent.getCenterY();
        Rectangle2D.Double adjustedExtent = new Rectangle2D.Double();
        double scale = 0.0;
        if (escalaX < escalaY) {
            scale = escalaX;
            double newHeight = this.imageSize.getHeight() / scale;
            ((Rectangle2D)adjustedExtent).setRect(xCenter - extent.getWidth() / 2.0, yCenter - newHeight / 2.0, extent.getWidth(), newHeight);
        } else {
            scale = escalaY;
            double newWidth = this.imageSize.getWidth() / scale;
            ((Rectangle2D)adjustedExtent).setRect(xCenter - newWidth / 2.0, yCenter - extent.getHeight() / 2.0, newWidth, extent.getHeight());
        }
        this.printScale = scale;
        translacion.setToTranslation(-((RectangularShape)adjustedExtent).getX(), -((RectangularShape)adjustedExtent).getY() - ((RectangularShape)adjustedExtent).getHeight());
        escalado.setToScale(scale, -scale);
        AffineTransform offsetTrans = new AffineTransform();
        offsetTrans.setToTranslation(this.offset.getX(), this.offset.getY());
        trans.setToIdentity();
        trans.concatenate(offsetTrans);
        trans.concatenate(escalado);
        trans.concatenate(translacion);
        return trans;
    }

    private Rectangle[] getTiles(Rectangle r) {
        int tileMaxWidth = 1500;
        int tileMaxHeight = 1500;
        int numCols = 1 + r.width / tileMaxWidth;
        int numRows = 1 + r.height / tileMaxHeight;
        double[][] srcPts = new double[numCols * numRows][8];
        Rectangle[] tile = new Rectangle[numCols * numRows];
        int yProv = r.y;
        int stepY = 0;
        while (stepY < numRows) {
            int altoAux = (double)(yProv + tileMaxHeight) > r.getMaxY() ? (int)r.getMaxY() - yProv : tileMaxHeight;
            int xProv = r.x;
            int stepX = 0;
            while (stepX < numCols) {
                int anchoAux = (double)(xProv + tileMaxWidth) > r.getMaxX() ? (int)r.getMaxX() - xProv : tileMaxWidth;
                int tileCnt = stepY * numCols + stepX;
                srcPts[tileCnt][0] = xProv;
                srcPts[tileCnt][1] = yProv;
                srcPts[tileCnt][2] = xProv + anchoAux + 1;
                srcPts[tileCnt][3] = yProv;
                srcPts[tileCnt][4] = xProv + anchoAux + 1;
                srcPts[tileCnt][5] = yProv + altoAux + 1;
                srcPts[tileCnt][6] = xProv;
                srcPts[tileCnt][7] = yProv + altoAux + 1;
                tile[tileCnt] = new Rectangle(xProv, yProv, anchoAux + 1, altoAux + 1);
                LOGGER.info((Object)("Tile " + tileCnt + "->" + xProv + "," + yProv + "," + (anchoAux + 1) + "," + (altoAux + 1)));
                xProv += tileMaxWidth;
                ++stepX;
            }
            yProv += tileMaxHeight;
            ++stepY;
        }
        LOGGER.info((Object)("Tiles:" + tile.length));
        return tile;
    }

    private void printRaster(Graphics2D graphics, Feature feature, RasterSymbolizer symbolizer, ICoordTrans transf) {
        Rectangle vista = this.rIntersection.getBounds();
        Envelope envelope = this.intersectionEnvelope;
        Rectangle[] tiles = this.getTiles(vista);
        int alpha = this.getOpacity(symbolizer);
        Coverage coverage = (Coverage)feature.getAttribute("IMAGE");
        List<Coverage> coverages = new ArrayList<Coverage>();
        if (coverage instanceof GridCoverageCollection) {
            GridCoverageCollection collection = (GridCoverageCollection)coverage;
            coverages = collection.getImageSelection(envelope);
        } else {
            coverages.add(coverage);
        }
        for (Coverage element : coverages) {
            int i = 0;
            while (i < tiles.length) {
                Rectangle view = tiles[i];
                PrintRenderer.drawPart(view.x, view.y, view.x + view.width, view.y + view.height, view.width, view.height, envelope, graphics, element, alpha, vista);
                ++i;
            }
        }
        System.gc();
    }

    private static void drawPart(int x, int y, int x1, int y1, int incX, int incY, Envelope envelope, Graphics2D outputGraphics, Coverage element, int alpha, Rectangle vistaGraficaImpresion) {
        Coordinate c1 = staticRenderer.pixelToWorld(x, y, envelope, vistaGraficaImpresion);
        Coordinate c2 = staticRenderer.pixelToWorld(x1, y1, envelope, vistaGraficaImpresion);
        Envelope newEnvelope = new Envelope(c1, c2);
        if (!element.getEnvelope().intersects(newEnvelope) || incX == 0 || incY == 0) {
            return;
        }
        RendererParameterWrapper renderPS = new RendererParameterWrapper(newEnvelope, x, y, incX, incY);
        renderPS.setAlpha(alpha);
        BufferedImage image = new BufferedImage(incX, incY, 2);
        Graphics2D graphics = (Graphics2D)image.getGraphics();
        element.getImage(graphics, renderPS);
        outputGraphics.drawImage(image, x, y, incX, incY, null);
    }

    private int getOpacity(RasterSymbolizer sym) {
        Expression exp = sym.getOpacity();
        if (exp == null) {
            return 255;
        }
        Object obj = exp.getValue(null);
        if (obj == null) {
            return 255;
        }
        Number num = null;
        if (obj instanceof Number) {
            num = (Number)obj;
        }
        if (num == null) {
            return 255;
        }
        return (int)(num.floatValue() * 255.0f);
    }

    private static double calculatePrintScale(int printWidth, int width, double paperSize, double worldWidth) {
        double k = Conversion.seventyTwoInch_To_Cm(paperSize) / (double)printWidth;
        double scale = worldWidth * (1.0 / k) * 100.0 / (double)width;
        return scale;
    }

    public static double fromSheetDistance(double d, AffineTransform at) {
        Point2D.Double pSheet1 = new Point2D.Double(0.0, 0.0);
        Point2D.Double pSheet2 = new Point2D.Double(1.0, 0.0);
        Point2D.Double pScreen1 = new Point2D.Double();
        Point2D.Double pScreen2 = new Point2D.Double();
        try {
            at.transform(pSheet1, pScreen1);
            at.transform(pSheet2, pScreen2);
        }
        catch (Exception e) {
            System.err.print(e.getMessage());
        }
        return pScreen1.distance(pScreen2) * d;
    }

    protected Paint getPaint(Paint paint, AffineTransform at, Rectangle2D anchor) {
        Paint newPaint;
        if (paint instanceof TexturePaint) {
            TexturePaint tp = (TexturePaint)paint;
            BufferedImage image = tp.getImage();
            Rectangle2D rect = tp.getAnchorRect();
            double width = rect.getWidth() * at.getScaleX();
            double height = rect.getHeight() * at.getScaleY();
            Rectangle2D.Double scaledRect = new Rectangle2D.Double(0.0, 0.0, width, height);
            newPaint = new TexturePaint(image, scaledRect);
        } else if (paint instanceof LinearGradientPaint) {
            double anchorHeight;
            LinearGradientPaint lgp = (LinearGradientPaint)paint;
            double anchorWidth = anchor.getWidth();
            if (anchorWidth == 0.0) {
                anchorWidth = 1.0;
            }
            if ((anchorHeight = anchor.getHeight()) == 0.0) {
                anchorHeight = 1.0;
            }
            Point2D.Double p1 = new Point2D.Double(anchorWidth / 250.0 * lgp.getStartPoint().getX() + anchor.getMinX(), anchorHeight / 250.0 * lgp.getStartPoint().getY() + anchor.getMinY());
            Point2D.Double p2 = new Point2D.Double(anchorWidth / 250.0 * lgp.getEndPoint().getX() + anchor.getMinX(), anchorHeight / 250.0 * lgp.getEndPoint().getY() + anchor.getMinY());
            newPaint = new LinearGradientPaint((float)((Point2D)p1).getX(), (float)((Point2D)p1).getY(), (float)((Point2D)p2).getX(), (float)((Point2D)p2).getY(), lgp.getFractions(), lgp.getColors(), lgp.getCycleMethod());
        } else if (paint instanceof RadialGradientPaint) {
            double anchorRadius = Math.max(anchor.getWidth(), anchor.getHeight());
            if (anchorRadius == 0.0) {
                anchorRadius = 1.0;
            }
            RadialGradientPaint rgp = (RadialGradientPaint)paint;
            Point2D.Double center = new Point2D.Double(anchor.getWidth() / 250.0 * rgp.getCenterPoint().getX() + anchor.getMinX(), anchor.getHeight() / 250.0 * rgp.getCenterPoint().getY() + anchor.getMinY());
            newPaint = new RadialGradientPaint((float)((Point2D)center).getX(), (float)((Point2D)center).getY(), (float)(anchorRadius / 250.0 * (double)rgp.getRadius()), rgp.getFractions(), rgp.getColors(), rgp.getCycleMethod());
        } else {
            newPaint = paint;
        }
        return newPaint;
    }
}


/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vividsolutions.jts.geom.Coordinate
 *  com.vividsolutions.jts.geom.Envelope
 *  com.vividsolutions.jts.geom.Geometry
 *  com.vividsolutions.jts.geom.GeometryCollection
 *  com.vividsolutions.jts.geom.GeometryFactory
 *  com.vividsolutions.jts.geom.Point
 *  javax.media.jai.util.Range
 *  org.apache.log4j.Logger
 *  org.geotools.factory.Hints
 *  org.geotools.feature.GeometryAttributeType
 *  org.geotools.feature.IllegalAttributeException
 *  org.geotools.map.MapContext
 *  org.geotools.referencing.CRS
 *  org.geotools.referencing.FactoryFinder
 *  org.geotools.referencing.operation.GeneralMatrix
 *  org.geotools.util.NumberRange
 *  org.opengis.referencing.crs.CoordinateReferenceSystem
 *  org.opengis.referencing.operation.MathTransform
 *  org.opengis.referencing.operation.MathTransform2D
 *  org.opengis.referencing.operation.MathTransformFactory
 *  org.opengis.referencing.operation.Matrix
 *  org.opengis.referencing.operation.NoninvertibleTransformException
 */
package org.saig.core.renderer;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.workbench.model.Layer;
import com.vividsolutions.jump.workbench.ui.LayerViewPanel;
import com.vividsolutions.jump.workbench.ui.Viewport;
import com.vividsolutions.jump.workbench.ui.renderer.style.CircleTerminalDecorator;
import com.vividsolutions.jump.workbench.ui.renderer.style.Style;
import com.vividsolutions.jump.workbench.ui.renderer.style.TerminalDecorator;
import es.kosmo.core.renderer.label.LabelCacheImpl;
import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Canvas;
import java.awt.Color;
import java.awt.Composite;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.MediaTracker;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.TexturePaint;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.imageio.ImageIO;
import javax.media.jai.util.Range;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import org.apache.log4j.Logger;
import org.geotools.factory.Hints;
import org.geotools.feature.GeometryAttributeType;
import org.geotools.feature.IllegalAttributeException;
import org.geotools.map.MapContext;
import org.geotools.referencing.CRS;
import org.geotools.referencing.FactoryFinder;
import org.geotools.referencing.operation.GeneralMatrix;
import org.geotools.util.NumberRange;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.MathTransform2D;
import org.opengis.referencing.operation.MathTransformFactory;
import org.opengis.referencing.operation.Matrix;
import org.saig.core.dao.coverage.Coverage;
import org.saig.core.filter.Expression;
import org.saig.core.model.feature.FeatureIterator;
import org.saig.core.renderer.LegendIconMaker;
import org.saig.core.renderer.RendererParameterWrapper;
import org.saig.core.renderer.RenderingHintsManager;
import org.saig.core.renderer.lite.Decimator;
import org.saig.core.renderer.lite.ImageLoader;
import org.saig.core.renderer.lite.LabelCache;
import org.saig.core.renderer.lite.LiteShape2;
import org.saig.core.renderer.lite.StyledShapePainter;
import org.saig.core.renderer.style.DynamicSymbolFactoryFinder;
import org.saig.core.renderer.style.MarkFactory;
import org.saig.core.renderer.style.SLDStyleFactory;
import org.saig.core.renderer.style.Style2D;
import org.saig.core.styling.ExternalGraphic;
import org.saig.core.styling.FeatureTypeStyle;
import org.saig.core.styling.Fill;
import org.saig.core.styling.Graphic;
import org.saig.core.styling.LineSymbolizer;
import org.saig.core.styling.Mark;
import org.saig.core.styling.PointSymbolizer;
import org.saig.core.styling.PolygonSymbolizer;
import org.saig.core.styling.RasterSymbolizer;
import org.saig.core.styling.Rule;
import org.saig.core.styling.Stroke;
import org.saig.core.styling.StyleFactory;
import org.saig.core.styling.Symbolizer;
import org.saig.core.styling.TextSymbolizer;
import org.saig.jump.widgets.print.Conversion;
import org.saig.jump.widgets.print.elements.GraphicElements;
import org.saig.jump.widgets.print.elements.image.ImageFrame;
import org.saig.jump.widgets.print.elements.legend.LegendFrame;
import org.saig.jump.widgets.print.elements.map.MapFrame;
import org.saig.jump.widgets.print.elements.north.NorthFrame;
import org.saig.jump.widgets.print.elements.scale.MapScale;
import org.saig.jump.widgets.print.elements.scale.ScaleFrame;
import org.saig.jump.widgets.print.elements.text.GraphicText;

public class Renderer {
    private static final Logger LOGGER = Logger.getLogger(Renderer.class);
    int error = 0;
    private static ImageLoader imageLoader = new ImageLoader();
    public static final MathTransformFactory mathTransformFactory;
    private static Renderer sInstance;
    public static BufferedImage imageCache;
    private static int pasadas;
    private static Set wellKnownMarks;
    private static Canvas obs;
    private static Point markCentrePoint;
    private static Set supportedGraphicFormats;
    private static final Composite DEFAULT_COMPOSITE;
    private static List printcache;
    private static Map leyendaIcons;
    private MapContext context;
    private boolean interactive = true;
    private boolean concatTransforms = false;
    private Envelope mapExtent = null;
    private Graphics2D outputGraphics;
    private Rectangle screenSize;
    private boolean optimizedDataLoadingEnabled;
    private boolean renderingStopRequested;
    private double scaleDenominator;
    private double generalizationDistance = 1.0;
    private SLDStyleFactory styleFactory = new SLDStyleFactory();
    private StyledShapePainter painter = new StyledShapePainter();
    private HashMap transformMap = new HashMap();
    private boolean canTransform = true;
    private RenderingHints hints;
    HashMap decimators = new HashMap();

    static {
        sInstance = null;
        imageCache = null;
        pasadas = 0;
        wellKnownMarks = new HashSet();
        obs = new Canvas();
        DEFAULT_COMPOSITE = AlphaComposite.getInstance(3, 1.0f);
        Hints hints = new Hints((RenderingHints.Key)Hints.LENIENT_DATUM_SHIFT, (Object)Boolean.TRUE);
        mathTransformFactory = FactoryFinder.getMathTransformFactory((Hints)hints);
        printcache = new ArrayList();
        leyendaIcons = new HashMap();
    }

    private Renderer() {
        this.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    }

    public static synchronized Renderer getUniqueInstance() {
        if (sInstance == null) {
            sInstance = new Renderer();
        }
        return sInstance;
    }

    public Renderer(MapContext context) {
        this.context = context;
        this.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    }

    public void setConcatTransforms(boolean flag) {
        this.concatTransforms = flag;
    }

    public boolean getConcatTransforms() {
        return this.concatTransforms;
    }

    protected void setScaleDenominator(double scaleDenominator) {
        this.scaleDenominator = scaleDenominator;
    }

    public void stopRendering() {
        this.renderingStopRequested = true;
    }

    public void applyFill(Graphics2D graphic, Fill fill, Feature feature) {
        if (fill == null) {
            return;
        }
        graphic.setColor(Color.decode((String)fill.getColor().getValue(feature)));
        LOGGER.debug((Object)("Setting fill: " + graphic.getColor().toString()));
        float opacity = 1.0f;
        if (fill.getOpacity() != null) {
            Number value = (Number)fill.getOpacity().getValue(feature);
            opacity = value.floatValue();
        }
        graphic.setComposite(AlphaComposite.getInstance(3, opacity));
        Graphic grd = fill.getGraphicFill();
        if (grd != null) {
            this.setTexture(graphic, grd, feature);
        } else {
            LOGGER.debug((Object)"no graphic fill set");
        }
    }

    private Mark getMark(Graphic graphic, Feature feature) {
        Mark[] marks = graphic.getMarks();
        Mark mark = null;
        int i = 0;
        while (i < marks.length) {
            String name = marks[i].getWellKnownName().getValue(feature).toString();
            if (wellKnownMarks.contains(name)) {
                mark = marks[i];
                break;
            }
            ++i;
        }
        return mark;
    }

    public void setTexture(Graphics2D graphic, Graphic gr, Feature feature) {
        BufferedImage image = this.getExternalGraphic(gr);
        if (image != null) {
            LOGGER.debug((Object)"got an image in graphic fill");
        } else {
            LOGGER.debug((Object)"going for the mark from graphic fill");
            Mark mark = this.getMark(gr, feature);
            if (mark == null) {
                mark = StyleFactory.createStyleFactory().getDefaultMark();
            }
            int size = 200;
            image = new BufferedImage(size, size, 2);
            Graphics2D g1 = image.createGraphics();
            g1.setRenderingHints(RenderingHintsManager.getRenderingHints());
            double rotation = 0.0;
            rotation = ((Number)gr.getRotation().getValue(feature)).doubleValue();
            GeometryFactory fac = new GeometryFactory();
            markCentrePoint = fac.createPoint(new Coordinate(0.0, 0.0));
            this.fillDrawMark(g1, markCentrePoint, mark, (int)((double)size * 0.9), rotation, 0, 0, feature);
            MediaTracker track = new MediaTracker(obs);
            track.addImage(image, 1);
            try {
                track.waitForID(1);
            }
            catch (InterruptedException e) {
                LOGGER.error((Object)e.toString());
            }
        }
        double width = image.getWidth();
        double height = image.getHeight();
        double unitSize = Math.max(width, height);
        int size = 6;
        size = ((Number)gr.getSize().getValue(feature)).intValue();
        double drawSize = (double)size / unitSize;
        LOGGER.debug((Object)("size = " + size + " unitsize " + unitSize + " drawSize " + drawSize));
        AffineTransform at = graphic.getTransform();
        double scaleX = drawSize / at.getScaleX();
        double scaleY = drawSize / -at.getScaleY();
        LOGGER.debug((Object)("scale " + scaleX + " " + scaleY));
        Rectangle2D.Double rect = new Rectangle2D.Double(0.0, 0.0, width *= scaleX, height *= scaleY);
        TexturePaint imagePaint = new TexturePaint(image, rect);
        graphic.setPaint(imagePaint);
        LOGGER.debug((Object)("applied TexturePaint " + imagePaint));
    }

    private BufferedImage getExternalGraphic(Graphic graphic) {
        ExternalGraphic[] extgraphics = graphic.getExternalGraphics();
        if (extgraphics != null) {
            int i = 0;
            while (i < extgraphics.length) {
                ExternalGraphic eg = extgraphics[i];
                BufferedImage img = this.getImage(eg);
                if (img != null) {
                    return img;
                }
                ++i;
            }
        }
        return null;
    }

    private BufferedImage getImage(ExternalGraphic eg) {
        LOGGER.debug((Object)("got a " + eg.getFormat()));
        if (Renderer.getSupportedGraphicFormats().contains(eg.getFormat().toLowerCase())) {
            LOGGER.debug((Object)"a java supported format");
            try {
                BufferedImage img = imageLoader.get(eg.getLocation(), this.isInteractive());
                LOGGER.debug((Object)("Image return = " + img));
                return img;
            }
            catch (MalformedURLException e) {
                LOGGER.error((Object)"ExternalGraphicURL was badly formed");
            }
        }
        return null;
    }

    private static synchronized Set getSupportedGraphicFormats() {
        if (supportedGraphicFormats == null) {
            supportedGraphicFormats = new HashSet();
            String[] types = ImageIO.getReaderMIMETypes();
            int i = 0;
            while (i < types.length) {
                supportedGraphicFormats.add(types[i]);
                ++i;
            }
        }
        return supportedGraphicFormats;
    }

    public void paint(Graphics2D graphics, Rectangle paintArea, List features, List simbolos, Viewport viewPort) {
        AffineTransform transform = null;
        try {
            transform = viewPort.getModelToViewTransform();
        }
        catch (NoninvertibleTransformException e1) {
            e1.printStackTrace();
        }
        AffineTransform pixelToWorld = null;
        try {
            pixelToWorld = transform.createInverse();
        }
        catch (NoninvertibleTransformException noninvertibleTransformException) {
            // empty catch block
        }
        Point2D.Double p1 = new Point2D.Double();
        Point2D.Double p2 = new Point2D.Double();
        pixelToWorld.transform(new Point2D.Double(paintArea.getX(), paintArea.getY()), p1);
        pixelToWorld.transform(new Point2D.Double(paintArea.getX() + paintArea.getWidth(), paintArea.getY() + paintArea.getHeight()), p2);
        double x1 = ((Point2D)p1).getX();
        double y1 = ((Point2D)p1).getY();
        double x2 = ((Point2D)p2).getX();
        double y2 = ((Point2D)p2).getY();
        Envelope envelope = new Envelope(Math.min(x1, x2), Math.max(x1, x2), Math.min(y1, y2), Math.max(y1, y2));
        this.paint(graphics, paintArea, envelope, features, simbolos);
    }

    public void paint(Graphics2D graphics, Rectangle paintArea, Envelope envelope, List features, List simbolos) {
        AffineTransform transform = this.worldToScreenTransform(envelope, paintArea);
        this.error = 0;
        if (this.hints != null) {
            graphics.setRenderingHints(this.hints);
        }
        if (graphics == null || paintArea == null) {
            LOGGER.info((Object)"renderer passed null arguments");
            return;
        }
        this.renderingStopRequested = false;
        AffineTransform at = transform;
        if (this.concatTransforms) {
            AffineTransform atg = graphics.getTransform();
            atg.concatenate(at);
            at = atg;
        }
        try {
            this.setScaleDenominator(Renderer.calculateScale(envelope, this.context.getCoordinateReferenceSystem(), paintArea.width, paintArea.height, 90.0));
        }
        catch (Exception e) {
            this.setScaleDenominator(1.0 / at.getScaleX());
        }
        CoordinateReferenceSystem destinationCrs = this.context.getCoordinateReferenceSystem();
        try {
            this.processStylers(graphics, features, simbolos, at, null, null, null);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        catch (IllegalAttributeException e) {
            e.printStackTrace();
        }
        LOGGER.debug((Object)("Style cache hit ratio: " + this.styleFactory.getHitRatio() + " , hits " + this.styleFactory.getHits() + ", requests " + this.styleFactory.getRequests()));
        if (this.error > 0) {
            LOGGER.error((Object)("Number of Errors during paint(Graphics2D, AffineTransform) = " + this.error));
        }
    }

    public static double calculateScale(Envelope envelope, CoordinateReferenceSystem coordinateReferenceSystem, int imageWidth, int imageHeight, double DPI) throws Exception {
        double diagonalGroundDistance = CRS.distance((Coordinate)new Coordinate(envelope.getMinX(), envelope.getMinY()), (Coordinate)new Coordinate(envelope.getMaxX(), envelope.getMaxY()), (CoordinateReferenceSystem)coordinateReferenceSystem);
        double diagonalPixelDistancePixels = Math.sqrt(imageWidth * imageWidth + imageHeight * imageHeight);
        double diagonalPixelDistanceMeters = diagonalPixelDistancePixels / DPI * 2.54 / 100.0;
        return diagonalGroundDistance / diagonalPixelDistanceMeters;
    }

    public void render(List features, List simbolos, Envelope map, Rectangle screenSize, Graphics2D outputGraphics, RendererParameterWrapper renderPs, List jumpStyles, Viewport viewPort) {
        if (outputGraphics == null) {
            LOGGER.info((Object)"renderer passed null graphics");
            return;
        }
        if (this.hints != null) {
            outputGraphics.setRenderingHints(this.hints);
        }
        this.renderingStopRequested = false;
        this.mapExtent = map;
        AffineTransform at = this.worldToScreenTransform(map, screenSize);
        this.scaleDenominator = 1.0 / outputGraphics.getTransform().getScaleX();
        try {
            this.processStylers(outputGraphics, features, simbolos, at, null, null, renderPs);
            if (jumpStyles != null && jumpStyles.size() > 0) {
                outputGraphics.translate(renderPs.getX(), renderPs.getY());
                for (Feature feature : features) {
                    try {
                        for (Style element : jumpStyles) {
                            element.paint(feature, outputGraphics, viewPort);
                        }
                    }
                    catch (Exception exception) {
                        // empty catch block
                    }
                }
            }
        }
        catch (IOException iOException) {
        }
        catch (IllegalAttributeException illegalAttributeException) {
            // empty catch block
        }
    }

    public AffineTransform worldToScreenTransform(Envelope mapExtent, Rectangle screenSize) {
        double scaleX = screenSize.getWidth() / mapExtent.getWidth();
        double scaleY = screenSize.getHeight() / mapExtent.getHeight();
        double tx = -mapExtent.getMinX() * scaleX;
        double ty = mapExtent.getMinY() * scaleY + screenSize.getHeight();
        AffineTransform at = new AffineTransform(scaleX, 0.0, 0.0, -scaleY, tx, ty);
        AffineTransform originTranslation = AffineTransform.getTranslateInstance(screenSize.x, screenSize.y);
        originTranslation.concatenate(at);
        return originTranslation != null ? originTranslation : at;
    }

    public Coordinate pixelToWorld(int x, int y, Envelope map) {
        if (this.outputGraphics == null) {
            LOGGER.info((Object)"no graphics yet deffined");
            return null;
        }
        AffineTransform at = this.worldToScreenTransform(map, this.screenSize);
        if (this.concatTransforms) {
            this.outputGraphics.getTransform().concatenate(at);
        } else {
            this.outputGraphics.setTransform(at);
        }
        try {
            Point2D result = at.inverseTransform(new Point2D.Double(x, y), new Point2D.Double());
            Coordinate c = new Coordinate(result.getX(), result.getY());
            return c;
        }
        catch (Exception exception) {
            return null;
        }
    }

    public Coordinate pixelToWorld_(int x, int y, Envelope map, Graphics2D outputGraphics, Rectangle screenSize) {
        if (outputGraphics == null) {
            LOGGER.info((Object)"no graphics yet deffined");
            return null;
        }
        AffineTransform at = this.worldToScreenTransform(map, screenSize);
        if (this.concatTransforms) {
            outputGraphics.getTransform().concatenate(at);
        } else {
            outputGraphics.setTransform(at);
        }
        try {
            Point2D result = at.inverseTransform(new Point2D.Double(x, y), new Point2D.Double());
            Coordinate c = new Coordinate(result.getX(), result.getY());
            return c;
        }
        catch (Exception exception) {
            return null;
        }
    }

    public Coordinate pixelToWorld(int x, int y, Envelope map, Rectangle screenSize) {
        AffineTransform at = this.worldToScreenTransform(map, screenSize);
        try {
            Point2D result = at.inverseTransform(new Point2D.Double(x, y), new Point2D.Double());
            Coordinate c = new Coordinate(result.getX(), result.getY());
            return c;
        }
        catch (Exception exception) {
            return null;
        }
    }

    private void processStylers(Graphics2D graphics, List features, List simbolos, AffineTransform at, CoordinateReferenceSystem destinationCrs, CoordinateReferenceSystem sourceCrs, RendererParameterWrapper renderPs) throws IOException, IllegalAttributeException {
        NumberRange scaleRange = new NumberRange(this.scaleDenominator, this.scaleDenominator);
        int i = 0;
        while (i < features.size() && !this.renderingStopRequested) {
            Feature feature = (Feature)features.get(i);
            List symbols = (List)simbolos.get(i);
            Symbolizer[] symbolizers = new Symbolizer[symbols.size()];
            symbols.toArray(symbolizers);
            try {
                this.processSymbolizers(graphics, feature, symbolizers, (Range)scaleRange, at, destinationCrs, renderPs);
            }
            catch (Exception e) {
                LOGGER.error((Object)"", (Throwable)e);
            }
            ++i;
        }
    }

    private void processSymbolizers(Graphics2D graphics, Feature feature, Symbolizer[] symbolizers, Range scaleRange, AffineTransform at, CoordinateReferenceSystem destinationCrs, RendererParameterWrapper renderPs) throws Exception {
        int m = 0;
        while (m < symbolizers.length) {
            if (symbolizers[m] != null && symbolizers[m].isActive()) {
                if (symbolizers[m] instanceof RasterSymbolizer) {
                    this.renderRaster(graphics, feature, (RasterSymbolizer)symbolizers[m], renderPs);
                } else {
                    Geometry g = feature.getGeometry();
                    MathTransform2D transform = null;
                    if (this.canTransform) {
                        try {
                            transform = (MathTransform2D)mathTransformFactory.createAffineTransform((Matrix)new GeneralMatrix(at));
                        }
                        catch (Exception exception) {
                            // empty catch block
                        }
                    }
                    LiteShape2 shape = this.getTransformedShape(g, transform);
                    Style2D style = this.styleFactory.createStyle(feature, symbolizers[m], scaleRange);
                    this.painter.paint(graphics, shape, style, this.scaleDenominator);
                }
            }
            ++m;
        }
    }

    public LiteShape2 getTransformedShape(Geometry g, MathTransform2D transform) throws Exception {
        LiteShape2 shape = new LiteShape2(g, (MathTransform)transform, this.getDecimator(transform), false);
        return shape;
    }

    private Decimator getDecimator(MathTransform2D mathTransform) throws org.opengis.referencing.operation.NoninvertibleTransformException {
        Decimator decimator = (Decimator)this.decimators.get(mathTransform);
        if (decimator == null) {
            decimator = mathTransform != null && !mathTransform.isIdentity() ? new Decimator(mathTransform.inverse()) : new Decimator(null);
            this.decimators.put(mathTransform, decimator);
        }
        return decimator;
    }

    private void renderRaster(Graphics2D graphics, Feature feature, RasterSymbolizer symbolizer, RendererParameterWrapper renderPs) {
        int alpha = this.getOpacity(symbolizer);
        renderPs.setAlpha(alpha);
        graphics.setComposite(AlphaComposite.getInstance(3, alpha));
        Coverage coverage = (Coverage)feature.getAttribute("IMAGE");
        coverage.getImage(graphics, renderPs);
        LOGGER.debug((Object)"Raster rendered");
    }

    private int calculateQuality(double widthMetricGraphic, int quality) {
        double ppp = widthMetricGraphic / 2.54;
        int width = (int)((double)quality * ppp);
        return width;
    }

    private double calculatePrintScale(RendererParameterWrapper renderPs, int printWidth, int width, double paperSize) {
        double k = Conversion.seventyTwoInch_To_Cm(paperSize) / (double)printWidth;
        double scale = (renderPs.getOriginalEnvelope().getMaxX() - renderPs.getOriginalEnvelope().getMinX()) * (1.0 / k) * 100.0 / (double)width;
        return scale;
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

    private Geometry findGeometry(Feature f, Symbolizer s) {
        String geomName = this.getGeometryPropertyName(s);
        Geometry geom = f.getGeometry();
        if (s instanceof PointSymbolizer) {
            geom = this.getCentroid(geom);
        }
        return geom;
    }

    public Geometry getCentroid(Geometry g) {
        if (g instanceof GeometryCollection) {
            GeometryCollection gc = (GeometryCollection)g;
            Coordinate[] pts = new Coordinate[gc.getNumGeometries()];
            int t = 0;
            while (t < gc.getNumGeometries()) {
                pts[t] = gc.getGeometryN(t).getCentroid().getCoordinate();
                ++t;
            }
            return g.getFactory().createMultiPoint(pts);
        }
        return g.getCentroid();
    }

    private CoordinateReferenceSystem findGeometryCS(Feature f, Symbolizer s) {
        String geomName = this.getGeometryPropertyName(s);
        if (geomName != null) {
            return ((GeometryAttributeType)f.getGeometry()).getCoordinateSystem();
        }
        return ((GeometryAttributeType)f.getGeometry()).getCoordinateSystem();
    }

    private String getGeometryPropertyName(Symbolizer s) {
        String geomName = null;
        if (s instanceof PolygonSymbolizer) {
            geomName = ((PolygonSymbolizer)s).getGeometryPropertyName();
        } else if (s instanceof PointSymbolizer) {
            geomName = ((PointSymbolizer)s).getGeometryPropertyName();
        } else if (s instanceof LineSymbolizer) {
            geomName = ((LineSymbolizer)s).getGeometryPropertyName();
        } else if (s instanceof TextSymbolizer) {
            geomName = ((TextSymbolizer)s).getGeometryPropertyName();
        }
        return geomName;
    }

    public boolean isInteractive() {
        return this.interactive;
    }

    public void setInteractive(boolean interactive) {
        this.interactive = interactive;
    }

    public boolean isOptimizedDataLoadingEnabled() {
        return this.optimizedDataLoadingEnabled;
    }

    public void setOptimizedDataLoadingEnabled(boolean b) {
        this.optimizedDataLoadingEnabled = b;
    }

    public double getGeneralizationDistance() {
        return this.generalizationDistance;
    }

    public void setGeneralizationDistance(double d) {
        this.generalizationDistance = d;
    }

    public void setRenderingHints(RenderingHints hints) {
        this.hints = hints;
    }

    public void setRenderingHint(RenderingHints.Key key, Object value) {
        if (this.hints == null) {
            this.hints = new RenderingHints(key, value);
        } else {
            this.hints.put(key, value);
        }
    }

    public void print(Graphics2D graphics, List graphicsElements, int pageWidth, int quality, double paperSize) {
        long t0 = System.currentTimeMillis();
        RenderingHints hints = new RenderingHints(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        hints.put(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        graphics.setRenderingHints(hints);
        graphics.setComposite(AlphaComposite.getInstance(3, 1.0f));
        for (GraphicElements element : graphicsElements) {
            ImageIcon icono;
            AffineTransform oldTransform;
            if (element.getClass().equals(MapFrame.class)) {
                List vectorialLayers;
                List rasterLayers;
                MapFrame mapa = (MapFrame)element;
                LayerViewPanel panel = (LayerViewPanel)mapa.getGraphicElementsForPrint();
                Rectangle vistaGraficaImpresion = panel.getBounds();
                Viewport viewPort = panel.getViewport();
                Envelope envelopeImpresion = panel.getViewport().getEnvelopeInModelCoordinates();
                int width = (int)vistaGraficaImpresion.getWidth();
                int height = (int)vistaGraficaImpresion.getHeight();
                graphics.setRenderingHints(hints);
                graphics.setComposite(AlphaComposite.getInstance(3, 1.0f));
                graphics.setClip(null);
                graphics.clipRect((int)vistaGraficaImpresion.getX(), (int)vistaGraficaImpresion.getY(), (int)vistaGraficaImpresion.getWidth(), (int)vistaGraficaImpresion.getHeight());
                double widthMetricGraphic = Conversion.seventyTwoInch_To_Cm(paperSize) / (double)pageWidth * (double)width;
                int widthQuality = this.calculateQuality(widthMetricGraphic, quality);
                int heightQuality = widthQuality * height / width;
                RendererParameterWrapper renderParameter = new RendererParameterWrapper(envelopeImpresion, 0, 0, widthQuality, heightQuality);
                if (printcache.isEmpty()) {
                    double scale = this.calculatePrintScale(renderParameter, pageWidth, (int)vistaGraficaImpresion.getWidth(), paperSize);
                    LOGGER.info((Object)("La escala de impresi\u00f3n es 1:" + (int)scale));
                    this.fillPrintCache(mapa, scale);
                }
                if (((List)(rasterLayers = (List)printcache.get(0)).get(0)).size() > 0 || ((List)rasterLayers.get(2)).size() > 0) {
                    Rectangle screenSize = new Rectangle();
                    screenSize.setBounds(renderParameter.getX(), renderParameter.getY(), renderParameter.getWidth(), renderParameter.getHeight());
                    if (imageCache == null) {
                        BufferedImage solucion = new BufferedImage(widthQuality, heightQuality, 1);
                        Graphics2D outputGraphics = (Graphics2D)solucion.getGraphics();
                        outputGraphics.setComposite(AlphaComposite.getInstance(3, 1.0f));
                        outputGraphics.setColor(Color.WHITE);
                        outputGraphics.fillRect(0, 0, widthQuality, heightQuality);
                        outputGraphics.setClip(null);
                        outputGraphics.clipRect(renderParameter.getX(), renderParameter.getY(), renderParameter.getWidth(), renderParameter.getHeight());
                        this.render((List)rasterLayers.get(0), (List)rasterLayers.get(1), envelopeImpresion, screenSize, outputGraphics, renderParameter, (List)rasterLayers.get(4), viewPort);
                        this.render((List)rasterLayers.get(2), (List)rasterLayers.get(3), envelopeImpresion, screenSize, outputGraphics, renderParameter, (List)rasterLayers.get(4), viewPort);
                        imageCache = solucion;
                    }
                    int barridas = 20;
                    int incremento = heightQuality / barridas;
                    double incImag = (double)vistaGraficaImpresion.height / (double)heightQuality;
                    int totalG = 0;
                    int y1 = 0;
                    int y2 = 0;
                    int i = 0;
                    while (totalG < heightQuality) {
                        y1 = (int)Math.round((double)totalG * incImag) + vistaGraficaImpresion.y;
                        y2 = (int)Math.round((double)(totalG + incremento) * incImag) + vistaGraficaImpresion.y;
                        graphics.drawImage(imageCache, vistaGraficaImpresion.x, y1, vistaGraficaImpresion.width + vistaGraficaImpresion.x, y2, 0, totalG, widthQuality, totalG + incremento, null);
                        totalG += incremento;
                        ++i;
                    }
                }
                if (((List)(vectorialLayers = (List)printcache.get(1)).get(0)).size() > 0 || ((List)vectorialLayers.get(2)).size() > 0) {
                    RendererParameterWrapper renderParameter2 = new RendererParameterWrapper(envelopeImpresion, vistaGraficaImpresion.x, vistaGraficaImpresion.y, width, height);
                    Rectangle screenSize2 = new Rectangle();
                    screenSize2.setBounds(renderParameter2.getX(), renderParameter2.getY(), renderParameter2.getWidth(), renderParameter2.getHeight());
                    this.render((List)vectorialLayers.get(0), (List)vectorialLayers.get(1), envelopeImpresion, screenSize2, graphics, renderParameter2, (List)vectorialLayers.get(4), viewPort);
                    this.render((List)vectorialLayers.get(2), (List)vectorialLayers.get(3), envelopeImpresion, screenSize2, graphics, renderParameter2, (List)vectorialLayers.get(4), viewPort);
                    ((LabelCache)printcache.get(2)).print(graphics, screenSize2);
                }
                graphics.setStroke(new BasicStroke(1.0f));
                graphics.setColor(Color.BLACK);
                graphics.drawRect(vistaGraficaImpresion.x, vistaGraficaImpresion.y, vistaGraficaImpresion.width, vistaGraficaImpresion.height);
                continue;
            }
            if (element.getClass().equals(GraphicText.class)) {
                graphics.setClip(null);
                GraphicText texto = (GraphicText)element;
                oldTransform = graphics.getTransform();
                AffineTransform translationTransform = new AffineTransform(oldTransform);
                translationTransform.translate(texto.getPrintX(), texto.getPrintY());
                graphics.setTransform(translationTransform);
                boolean doubleBuffered = texto.getGraphicElementsForPrint().isDoubleBuffered();
                texto.getGraphicElementsForPrint().setDoubleBuffered(false);
                texto.getGraphicElementsForPrint().print(graphics);
                texto.getGraphicElementsForPrint().setDoubleBuffered(doubleBuffered);
                graphics.setTransform(oldTransform);
                continue;
            }
            if (element.getClass().equals(NorthFrame.class)) {
                graphics.setClip(null);
                NorthFrame norte = (NorthFrame)element;
                icono = norte.getNorthSymbol();
                ImageIcon trueIcon = (ImageIcon)((JLabel)norte.getGraphicElementsForPrint()).getIcon();
                int startX = norte.getPrintX() + (norte.getPrintWidth() - trueIcon.getIconWidth()) / 2;
                int startY = norte.getPrintY() + (norte.getPrintHeight() - trueIcon.getIconHeight()) / 2;
                Composite comp = graphics.getComposite();
                graphics.setComposite(AlphaComposite.getInstance(3, 1.0f));
                graphics.drawImage(icono.getImage(), startX, startY, trueIcon.getIconWidth(), trueIcon.getIconHeight(), null);
                graphics.setComposite(comp);
                continue;
            }
            if (element.getClass().equals(ImageFrame.class)) {
                graphics.setClip(null);
                ImageFrame imagen = (ImageFrame)element;
                icono = imagen.getImageSymbol();
                ImageIcon trueIcon = (ImageIcon)((JLabel)imagen.getGraphicElementsForPrint()).getIcon();
                int startX = imagen.getPrintX() + (imagen.getPrintWidth() - trueIcon.getIconWidth()) / 2;
                int startY = imagen.getPrintY() + (imagen.getPrintHeight() - trueIcon.getIconHeight()) / 2;
                Composite comp = graphics.getComposite();
                graphics.setComposite(AlphaComposite.getInstance(3, 1.0f));
                graphics.drawImage(icono.getImage(), startX, startY, trueIcon.getIconWidth(), trueIcon.getIconHeight(), null);
                graphics.setComposite(comp);
                continue;
            }
            if (element.getClass().equals(ScaleFrame.class)) {
                graphics.setClip(null);
                ScaleFrame scaleFrame = (ScaleFrame)element;
                MapScale scale = scaleFrame.getMapScale();
                int width = scaleFrame.getPrintWidth();
                int height = scaleFrame.getPrintHeight();
                BufferedImage solucion = new BufferedImage(width, height, 2);
                Graphics2D graphics2 = (Graphics2D)solucion.getGraphics();
                graphics2.setColor(new Color(0, 0, 0, 0));
                graphics2.fillRect(0, 0, width, height);
                graphics2.setComposite(AlphaComposite.getInstance(3, 1.0f));
                scale.print(graphics2, 0, 0);
                Composite comp = graphics.getComposite();
                graphics.setComposite(AlphaComposite.getInstance(3, 1.0f));
                graphics.drawImage(solucion, null, scaleFrame.getPrintX(), scaleFrame.getPrintY());
                graphics.setComposite(comp);
                continue;
            }
            if (!element.getClass().equals(LegendFrame.class)) continue;
            graphics.setClip(null);
            LegendFrame leyenda = (LegendFrame)element;
            oldTransform = graphics.getTransform();
            AffineTransform translationTransform = new AffineTransform(oldTransform);
            translationTransform.translate(leyenda.getPrintX(), leyenda.getPrintY());
            graphics.setTransform(translationTransform);
            leyenda.print(graphics);
            graphics.setTransform(oldTransform);
        }
        LOGGER.info((Object)("Pasada de impresi\u00f3n n\u00famero " + ++pasadas + ". El tiempo empleado es:" + (System.currentTimeMillis() - t0)));
    }

    public static void clearPrintCache() {
        if (imageCache != null) {
            imageCache.getGraphics().dispose();
            imageCache = null;
        }
        pasadas = 0;
        printcache.clear();
        leyendaIcons.clear();
        System.gc();
    }

    private void fillPrintCache(MapFrame mapa, double scale) {
        LayerViewPanel panel = (LayerViewPanel)mapa.getGraphicElementsForPrint();
        Viewport viewPort = panel.getViewport();
        Rectangle vistaGrafica = panel.getBounds();
        Envelope envelope = panel.getViewport().getEnvelopeInModelCoordinates();
        List<Layer> layers = panel.getLayerManager().getVisibleLayers(false);
        Collections.reverse(layers);
        ArrayList vectorialLayers = new ArrayList();
        ArrayList rasterLayers = new ArrayList();
        int i = 0;
        while (i < 5) {
            vectorialLayers.add(new ArrayList());
            rasterLayers.add(new ArrayList());
            ++i;
        }
        LabelCacheImpl labelCache = new LabelCacheImpl(true, true);
        Iterator<Layer> iterLayer = layers.iterator();
        while (iterLayer.hasNext()) {
            boolean raster;
            ArrayList<Style> jumpStylesToApply;
            ArrayList symbolsElse;
            ArrayList<Feature> featuresElse;
            ArrayList symbols;
            ArrayList<Feature> features;
            block41: {
                features = new ArrayList<Feature>();
                symbols = new ArrayList();
                featuresElse = new ArrayList<Feature>();
                symbolsElse = new ArrayList();
                ArrayList<Symbolizer> elseSymbols = new ArrayList<Symbolizer>();
                jumpStylesToApply = new ArrayList<Style>();
                raster = false;
                Layer layer = iterLayer.next();
                List<Style> jumpStyles = layer.getStyles();
                for (Style jumpStyle : jumpStyles) {
                    if (!(jumpStyle instanceof TerminalDecorator) && !(jumpStyle instanceof CircleTerminalDecorator) || !jumpStyle.isEnabled()) continue;
                    jumpStyle.initialize(layer);
                    jumpStylesToApply.add(jumpStyle);
                }
                FeatureTypeStyle estilo = layer.getModelStyle().getSelectedFeatureTypeStyle();
                ArrayList<Rule> rulesInScale = new ArrayList<Rule>();
                ArrayList<Rule> rulesElse = new ArrayList<Rule>();
                Rule[] rules = estilo.getRules();
                int i2 = 0;
                while (i2 < rules.length) {
                    Rule rule = rules[i2];
                    if ((rule.getMinScaleDenominator() <= scale || Double.isNaN(rule.getMinScaleDenominator())) && (rule.getMaxScaleDenominator() >= scale || Double.isNaN(rule.getMaxScaleDenominator()))) {
                        if (rule.isElseFilter()) {
                            Symbolizer[] simbolos = rule.getSymbolizers();
                            int i1 = 0;
                            while (i1 < simbolos.length) {
                                elseSymbols.add(simbolos[i1]);
                                ++i1;
                            }
                            rulesElse.add(rule);
                        } else {
                            rulesInScale.add(rule);
                        }
                    }
                    ++i2;
                }
                FeatureIterator featureIterator = null;
                try {
                    try {
                        featureIterator = layer.getFeatureCollectionWrapper().queryIterator(envelope);
                        AffineTransform at = this.worldToScreenTransform(viewPort.getEnvelopeInModelCoordinates(), vistaGrafica);
                        MathTransform2D transform = null;
                        try {
                            transform = (MathTransform2D)mathTransformFactory.createAffineTransform((Matrix)new GeneralMatrix(at));
                        }
                        catch (Exception e) {
                            e.printStackTrace();
                        }
                        double scaleDenominator = 1.0;
                        NumberRange scaleRange = null;
                        try {
                            scaleDenominator = 1.0 / viewPort.getModelToViewTransform().getScaleX();
                            scaleRange = new NumberRange(scaleDenominator, scaleDenominator);
                        }
                        catch (NoninvertibleTransformException e) {
                            e.printStackTrace();
                        }
                        labelCache.start();
                        labelCache.startLayer(layer.getName());
                        while (featureIterator.hasNext()) {
                            Feature feature = featureIterator.next();
                            if (feature == null || feature.getGeometry() == null || feature.getGeometry().isEmpty()) continue;
                            ArrayList<Symbolizer> featureSymbols = new ArrayList<Symbolizer>();
                            ArrayList<Symbolizer> textSymbols = new ArrayList<Symbolizer>();
                            for (Rule rule : rulesInScale) {
                                if ((rule.getFilter() == null || !rule.getFilter().contains(feature)) && rule.getFilter() != null) continue;
                                Symbolizer[] symbolizerArray = rule.getSymbolizers();
                                int i3 = 0;
                                while (i3 < symbolizerArray.length) {
                                    if (!(symbolizerArray[i3] instanceof TextSymbolizer)) {
                                        featureSymbols.add(symbolizerArray[i3]);
                                        if (symbolizerArray[i3] instanceof RasterSymbolizer) {
                                            raster = true;
                                        }
                                    } else {
                                        textSymbols.add(symbolizerArray[i3]);
                                    }
                                    ++i3;
                                }
                            }
                            if (!featureSymbols.isEmpty()) {
                                features.add(feature);
                                symbols.add(featureSymbols);
                            } else {
                                featuresElse.add(feature);
                                symbolsElse.add(elseSymbols);
                            }
                            if (textSymbols.isEmpty()) continue;
                            LiteShape2 shape = null;
                            try {
                                shape = this.getTransformedShape(feature.getGeometry(), transform);
                            }
                            catch (Exception e) {
                                e.printStackTrace();
                            }
                            for (TextSymbolizer textSymbolizer : textSymbols) {
                                double factor = 1.0;
                                if (textSymbolizer.isScale()) {
                                    double max = textSymbolizer.getScaleMaxValue();
                                    double min = textSymbolizer.getScaleMinValue();
                                    if (factor < min) {
                                        factor = min;
                                    }
                                }
                                Float height = null;
                                if (textSymbolizer.getHeightAttribute() != null) {
                                    Number textHeight = (Number)textSymbolizer.getHeightAttribute().getValue(feature);
                                    if (textHeight != null) {
                                        height = Float.valueOf(textHeight.floatValue() * (float)factor);
                                    } else if (textSymbolizer.isScale()) {
                                        height = Float.valueOf((float)factor);
                                    }
                                } else if (textSymbolizer.isScale()) {
                                    height = Float.valueOf((float)factor);
                                }
                                Number rotation = null;
                                if (textSymbolizer.getAttributeRotation() != null) {
                                    rotation = (Number)textSymbolizer.getAttributeRotation().getValue(feature);
                                }
                                labelCache.put(layer.getName(), textSymbolizer, feature, shape, null, (Range)scaleRange, height, null, rotation);
                            }
                        }
                    }
                    catch (Exception e) {
                        LOGGER.error((Object)"", (Throwable)e);
                        if (featureIterator != null) {
                            featureIterator.close();
                        }
                        break block41;
                    }
                }
                catch (Throwable throwable) {
                    if (featureIterator != null) {
                        featureIterator.close();
                    }
                    throw throwable;
                }
                if (featureIterator != null) {
                    featureIterator.close();
                }
            }
            if (raster) {
                ((List)rasterLayers.get(0)).addAll(features);
                ((List)rasterLayers.get(1)).addAll(symbols);
                ((List)rasterLayers.get(2)).addAll(featuresElse);
                ((List)rasterLayers.get(3)).addAll(symbolsElse);
                ((List)rasterLayers.get(4)).addAll(jumpStylesToApply);
                continue;
            }
            ((List)vectorialLayers.get(0)).addAll(features);
            ((List)vectorialLayers.get(1)).addAll(symbols);
            ((List)vectorialLayers.get(2)).addAll(featuresElse);
            ((List)vectorialLayers.get(3)).addAll(symbolsElse);
            ((List)vectorialLayers.get(4)).addAll(jumpStylesToApply);
        }
        printcache.add(rasterLayers);
        printcache.add(vectorialLayers);
        printcache.add(labelCache);
    }

    private void fillDrawMark(Graphics2D graphic, Point point, Mark mark, int size, double rotation, int xOffset, int yOffset, Feature feature) {
        this.fillDrawMark(graphic, point.getX(), point.getY(), mark, size, rotation, xOffset, yOffset, feature);
    }

    private void applyStroke(Graphics2D graphic, Stroke stroke, Feature feature) {
        Graphic gr;
        BasicStroke stroke2d;
        if (stroke == null) {
            return;
        }
        double scale = graphic.getTransform().getScaleX();
        LOGGER.debug((Object)("line join = " + stroke.getLineJoin()));
        String joinType = stroke.getLineJoin() == null ? "mitre" : (String)stroke.getLineJoin().getValue(feature);
        if (joinType == null) {
            joinType = "mitre";
        }
        int joinCode = SLDStyleFactory.lookUpJoin(joinType);
        String capType = stroke.getLineCap() != null ? (String)stroke.getLineCap().getValue(feature) : "square";
        if (capType == null) {
            capType = "square";
        }
        int capCode = SLDStyleFactory.lookUpCap(capType);
        float[] dashes = stroke.getDashArray();
        if (dashes != null) {
            int i = 0;
            while (i < dashes.length) {
                dashes[i] = Math.max(1.0f, dashes[i] / (float)scale);
                ++i;
            }
        }
        Number value = (Number)stroke.getWidth().getValue(feature);
        float width = value.floatValue();
        value = (Number)stroke.getDashOffset().getValue(feature);
        float dashOffset = value.floatValue();
        value = (Number)stroke.getOpacity().getValue(feature);
        float opacity = value.floatValue();
        LOGGER.debug((Object)("width, dashoffset, opacity " + width + " " + dashOffset + " " + opacity));
        if (dashes != null && dashes.length > 0) {
            if ((double)width <= 1.0) {
                width = 0.0f;
            }
            stroke2d = new BasicStroke(width / (float)scale, capCode, joinCode, 5.0f, dashes, dashOffset / (float)scale);
        } else {
            if ((double)width <= 1.0) {
                width = 0.0f;
            }
            stroke2d = new BasicStroke(width / (float)scale, capCode, joinCode, 5.0f);
        }
        graphic.setComposite(AlphaComposite.getInstance(3, opacity));
        if (!graphic.getStroke().equals(stroke2d)) {
            graphic.setStroke(stroke2d);
        }
        Color color = Color.decode((String)stroke.getColor().getValue(feature));
        if (!graphic.getColor().equals(color)) {
            graphic.setColor(color);
        }
        if ((gr = stroke.getGraphicFill()) != null) {
            this.setTexture(graphic, gr, feature);
        } else {
            LOGGER.debug((Object)"no graphic fill set");
        }
    }

    private void fillDrawMark(Graphics2D graphic, double tx, double ty, Mark mark, int size, double rotation, int xOffset, int yOffset, Feature feature) {
        AffineTransform temp = graphic.getTransform();
        AffineTransform markAT = new AffineTransform();
        Shape shape = this.getShape(mark, feature);
        Point2D.Double mapCentre = new Point2D.Double(tx, ty);
        Point2D.Double graphicCentre = new Point2D.Double();
        temp.transform(mapCentre, graphicCentre);
        markAT.translate(((Point2D)graphicCentre).getX(), ((Point2D)graphicCentre).getY());
        double shearY = temp.getShearY();
        double scaleY = temp.getScaleY();
        double originalRotation = Math.atan(shearY / scaleY);
        LOGGER.debug((Object)("originalRotation " + originalRotation));
        markAT.rotate(rotation - originalRotation);
        Rectangle2D bounds = shape.getBounds2D();
        double unitSize = Math.max(bounds.getWidth(), bounds.getHeight());
        double drawSize = (double)size / unitSize;
        markAT.scale(drawSize, -drawSize);
        markAT.translate((double)xOffset / unitSize, (double)yOffset / unitSize);
        graphic.setTransform(markAT);
        if (mark.getFill() != null) {
            LOGGER.debug((Object)"applying fill to mark");
            this.applyFill(graphic, mark.getFill(), feature);
            graphic.fill(shape);
        }
        if (mark.getStroke() != null) {
            LOGGER.debug((Object)"applying stroke to mark");
            this.applyStroke(graphic, mark.getStroke(), feature);
            graphic.draw(shape);
        }
        graphic.setTransform(temp);
        if (mark.getFill() != null) {
            this.resetFill(graphic);
        }
    }

    private Shape getShape(Mark mark, Feature feature) {
        if (mark == null) {
            return null;
        }
        Expression name = mark.getWellKnownName();
        Iterator<MarkFactory> it = DynamicSymbolFactoryFinder.getMarkFactories();
        while (it.hasNext()) {
            MarkFactory factory = it.next();
            try {
                Shape shape = factory.getShape(null, name, feature);
                if (shape == null) continue;
                return shape;
            }
            catch (Exception e) {
                LOGGER.warn((Object)"Exception while scanning for the appropriate mark factory", (Throwable)e);
            }
        }
        return null;
    }

    private void resetFill(Graphics2D graphic) {
        LOGGER.debug((Object)"reseting the graphics");
        graphic.setComposite(DEFAULT_COMPOSITE);
    }

    public static void drawLegend(Graphics2D g, List layers, int xLeyenda, int yLeyenda, LegendFrame leyenda) {
        g.setClip(null);
        Color color = leyenda.getLabelMapLegendFont().getBackgroundColor();
        if (!leyenda.getLabelMapLegendFont().isOpaque()) {
            color = new Color(0, 0, 0, 0);
        } else {
            g.setComposite(AlphaComposite.getInstance(3, (float)color.getAlpha() / 255.0f));
            g.setColor(color);
            g.fillRect(xLeyenda, yLeyenda, leyenda.getPrintWidth(), leyenda.getPrintHeight());
        }
        BasicStroke strokeLeyenda = new BasicStroke(1.0f);
        g.setFont(leyenda.getLabelMapLegendFont().getFont());
        FontMetrics fm = g.getFontMetrics();
        g.setStroke(strokeLeyenda);
        int borderSize = 1;
        if (leyenda.getLabelMapLegendFont().getBorder() != null) {
            borderSize = leyenda.getLabelMapLegendFont().getBorderThickness() + 1;
        }
        int x = xLeyenda + borderSize;
        int y = yLeyenda + borderSize;
        for (Layer layer : layers) {
            if (layer.isRaster()) continue;
            Rule[] rules = layer.getModelStyle().getSelectedFeatureTypeStyle().getRules();
            int i = 0;
            while (i < rules.length && y < yLeyenda + leyenda.getPrintHeight()) {
                Rule rule = rules[i];
                Image image = null;
                if (leyendaIcons.containsKey(rule)) {
                    image = (Image)leyendaIcons.get(rule);
                } else {
                    image = LegendIconMaker.reallyMakeLegendIcon(fm.getHeight(), fm.getHeight(), color, rule.getSymbolizers());
                    leyendaIcons.put(rule, image);
                }
                String nombreLeyenda = rule.getTitle();
                String subCadena = "";
                int anchoAux = 2 * fm.getHeight();
                int j = 0;
                while (j < nombreLeyenda.length()) {
                    char c = nombreLeyenda.charAt(j);
                    int wc = fm.charWidth(c);
                    if (anchoAux + wc > leyenda.getPrintWidth()) break;
                    anchoAux += wc;
                    subCadena = String.valueOf(subCadena) + c;
                    ++j;
                }
                if (!subCadena.equals(nombreLeyenda)) {
                    subCadena = subCadena.length() > 3 ? String.valueOf(subCadena.substring(0, subCadena.length() - 3)) + "..." : "...";
                }
                g.drawImage(image, x, y, null);
                g.setStroke(strokeLeyenda);
                g.setColor(leyenda.getLabelMapLegendFont().getColor());
                g.drawString(subCadena, x + 2 * fm.getHeight(), y + (int)((double)fm.getHeight() * 0.75));
                y += (int)Math.ceil((double)fm.getHeight() * 1.25);
                ++i;
            }
        }
        if (leyenda.getLabelMapLegendFont().getBorder() != null) {
            int thickness = leyenda.getLabelMapLegendFont().getBorderThickness();
            g.setStroke(new BasicStroke(thickness));
            g.setColor(leyenda.getLabelMapLegendFont().getBorderColor());
            g.drawRect(xLeyenda + 1, yLeyenda + 1, leyenda.getPrintWidth() - thickness - 1, leyenda.getPrintHeight() - thickness - 1);
        }
    }

    public static int[] getGraphicCoordinates(Envelope envelope, Envelope newEnvelope, int width, int height) {
        int[] resultado = new int[4];
        double factorX = (double)width / envelope.getWidth();
        double factorY = (double)height / envelope.getHeight();
        double x0d = (newEnvelope.getMinX() - envelope.getMinX()) * factorX;
        double y0d = (envelope.getMaxY() - newEnvelope.getMaxY()) * factorY;
        double x1d = (newEnvelope.getMaxX() - envelope.getMinX()) * factorX;
        double y1d = (envelope.getMaxY() - newEnvelope.getMinY()) * factorY;
        resultado[0] = (int)Math.round(x0d);
        resultado[1] = (int)Math.round(y0d);
        resultado[2] = (int)Math.round(x1d);
        resultado[3] = (int)Math.round(y1d);
        return resultado;
    }

    public static int worldXtoPixel(double x, double anchoM, double anchoPx, double xmin) {
        double xIncrement = anchoM / anchoPx;
        int pixelX = (int)((x - xmin) / xIncrement);
        return pixelX;
    }

    public static int worldYtoPixel(double y, double altoM, double altoPx, double ymax) {
        double yIncrement = altoM / altoPx;
        int pixelY = (int)((ymax - y) / yIncrement);
        return pixelY;
    }
}


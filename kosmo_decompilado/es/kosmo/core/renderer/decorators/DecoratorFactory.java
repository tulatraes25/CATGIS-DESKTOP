/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.log4j.Logger
 */
package es.kosmo.core.renderer.decorators;

import es.kosmo.core.renderer.decorators.AbstractDecorator;
import es.kosmo.core.renderer.decorators.IDecorator;
import es.kosmo.core.renderer.decorators.impl.EndArrowMarkerDecorator;
import es.kosmo.core.renderer.decorators.impl.EndCircleMarkerDecorator;
import es.kosmo.core.renderer.decorators.impl.EndFeathersMarkerDecorator;
import es.kosmo.core.renderer.decorators.impl.IndexNumberVertexMarkerDecorator;
import es.kosmo.core.renderer.decorators.impl.MidLineOpenArrowMarkerDecorator;
import es.kosmo.core.renderer.decorators.impl.MidLineSolidArrowMarkerDecorator;
import es.kosmo.core.renderer.decorators.impl.MidSegmentArrowMarkerDecorator;
import es.kosmo.core.renderer.decorators.impl.MidSegmentCircleMarkerDecorator;
import es.kosmo.core.renderer.decorators.impl.PieChartMarkerDecorator;
import es.kosmo.core.renderer.decorators.impl.StartArrowMarkerDecorator;
import es.kosmo.core.renderer.decorators.impl.StartCircleMarkerDecorator;
import es.kosmo.core.renderer.decorators.impl.StartFeathersMarkerDecorator;
import es.kosmo.core.renderer.decorators.impl.XYZCoordinatesVertexMarkerDecorator;
import es.kosmo.core.renderer.decorators.impl.ZCoordinateVertexMarkerDecorator;
import es.kosmo.desktop.widgets.symbology.renderer.decorators.config.AbstractDecoratorConfigPanel;
import es.kosmo.desktop.widgets.symbology.renderer.decorators.config.ArrowMarkerDecoratorConfigPanel;
import es.kosmo.desktop.widgets.symbology.renderer.decorators.config.CircleMarkerDecoratorConfigPanel;
import es.kosmo.desktop.widgets.symbology.renderer.decorators.config.DefaultMarkerDecoratorConfigPanel;
import es.kosmo.desktop.widgets.symbology.renderer.decorators.config.PieChartMarkerDecoratorConfigPanel;
import es.kosmo.desktop.widgets.symbology.renderer.decorators.config.VertexMarkerDecoratorConfigPanel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.log4j.Logger;
import org.saig.jump.lang.I18N;

public class DecoratorFactory {
    private static final Logger LOGGER = Logger.getLogger(DecoratorFactory.class);
    protected static DecoratorFactory _instance;
    protected static Map<String, Class<? extends AbstractDecorator>> decoratorIdToClassMap;
    protected static Map<String, Class<? extends AbstractDecoratorConfigPanel>> decoratorIdToConfigPanelMap;

    static {
        decoratorIdToClassMap = new HashMap<String, Class<? extends AbstractDecorator>>();
        decoratorIdToConfigPanelMap = new HashMap<String, Class<? extends AbstractDecoratorConfigPanel>>();
    }

    private DecoratorFactory() {
        this.registerDefaultDecorators();
    }

    public static DecoratorFactory getInstance() {
        if (_instance == null) {
            _instance = new DecoratorFactory();
        }
        return _instance;
    }

    protected void registerDefaultDecorators() {
        LOGGER.info((Object)(String.valueOf(I18N.getString("es.kosmo.core.renderer.decorators.DecoratorFactory.Registering-default-decorators")) + "..."));
        this.registerDecorator(StartArrowMarkerDecorator.NAME, StartArrowMarkerDecorator.class, ArrowMarkerDecoratorConfigPanel.class);
        this.registerDecorator(EndArrowMarkerDecorator.NAME, EndArrowMarkerDecorator.class, ArrowMarkerDecoratorConfigPanel.class);
        this.registerDecorator(MidLineOpenArrowMarkerDecorator.NAME, MidLineOpenArrowMarkerDecorator.class, ArrowMarkerDecoratorConfigPanel.class);
        this.registerDecorator(MidLineSolidArrowMarkerDecorator.NAME, MidLineSolidArrowMarkerDecorator.class, ArrowMarkerDecoratorConfigPanel.class);
        this.registerDecorator(MidSegmentArrowMarkerDecorator.NAME, MidSegmentArrowMarkerDecorator.class, ArrowMarkerDecoratorConfigPanel.class);
        this.registerDecorator(EndCircleMarkerDecorator.NAME, EndCircleMarkerDecorator.class, CircleMarkerDecoratorConfigPanel.class);
        this.registerDecorator(StartCircleMarkerDecorator.NAME, StartCircleMarkerDecorator.class, CircleMarkerDecoratorConfigPanel.class);
        this.registerDecorator(MidSegmentCircleMarkerDecorator.NAME, MidSegmentCircleMarkerDecorator.class, CircleMarkerDecoratorConfigPanel.class);
        this.registerDecorator(EndFeathersMarkerDecorator.NAME, EndFeathersMarkerDecorator.class, ArrowMarkerDecoratorConfigPanel.class);
        this.registerDecorator(StartFeathersMarkerDecorator.NAME, StartFeathersMarkerDecorator.class, ArrowMarkerDecoratorConfigPanel.class);
        this.registerDecorator(XYZCoordinatesVertexMarkerDecorator.NAME, XYZCoordinatesVertexMarkerDecorator.class, VertexMarkerDecoratorConfigPanel.class);
        this.registerDecorator(ZCoordinateVertexMarkerDecorator.NAME, ZCoordinateVertexMarkerDecorator.class, VertexMarkerDecoratorConfigPanel.class);
        this.registerDecorator(IndexNumberVertexMarkerDecorator.NAME, IndexNumberVertexMarkerDecorator.class, VertexMarkerDecoratorConfigPanel.class);
        this.registerDecorator(PieChartMarkerDecorator.NAME, PieChartMarkerDecorator.class, PieChartMarkerDecoratorConfigPanel.class);
    }

    public void registerDecorator(String name, Class<? extends AbstractDecorator> decoratorClass) {
        this.registerDecorator(name, decoratorClass, DefaultMarkerDecoratorConfigPanel.class);
    }

    public void registerDecorator(String decoratorID, Class<? extends AbstractDecorator> decoratorClass, Class<? extends AbstractDecoratorConfigPanel> decoratorConfigPanelClass) {
        if (!this.containsDecorator(decoratorID)) {
            decoratorIdToClassMap.put(decoratorID, decoratorClass);
            decoratorIdToConfigPanelMap.put(decoratorID, decoratorConfigPanelClass);
            LOGGER.info((Object)I18N.getMessage("es.kosmo.core.renderer.decorators.DecoratorFactory.Registering-{0}-decorator", new Object[]{decoratorID}));
        } else {
            LOGGER.warn((Object)I18N.getMessage("es.kosmo.core.renderer.decorators.DecoratorFactory.The-decorator-{0}-has-been-already-registered", new Object[]{decoratorID}));
        }
    }

    protected void registerDecoratorConfigPanel(String decoratorID, Class<? extends AbstractDecorator> decoratorConfigPanelClass) {
        decoratorIdToClassMap.put(decoratorID, decoratorConfigPanelClass);
        LOGGER.info((Object)I18N.getMessage("es.kosmo.core.renderer.decorators.DecoratorFactory.Registering-the-configuration-panel-for-the-decorator-{0}", new Object[]{decoratorID}));
    }

    protected boolean containsDecorator(String decoratorID) {
        return decoratorIdToClassMap.containsKey(decoratorID);
    }

    public List<String> getDecorators() {
        return new ArrayList<String>(decoratorIdToClassMap.keySet());
    }

    public IDecorator getDecorator(String decoratorID) throws Exception {
        IDecorator relation;
        Class<? extends AbstractDecorator> decoratorClass = decoratorIdToClassMap.get(decoratorID);
        try {
            relation = decoratorClass.newInstance();
        }
        catch (IllegalAccessException e) {
            LOGGER.warn((Object)I18N.getMessage("es.kosmo.core.renderer.decorators.DecoratorFactory.An-access-error-has-been-produced-while-creating-a-{0}-decorator-type-instance", new Object[]{decoratorClass.getName()}));
            throw e;
        }
        catch (InstantiationException e) {
            LOGGER.warn((Object)I18N.getMessage("es.kosmo.core.renderer.decorators.DecoratorFactory.A-class-{0}-object-could-not-been-instantiated", new Object[]{decoratorClass.getName()}));
            throw e;
        }
        return relation;
    }

    public AbstractDecoratorConfigPanel getDecoratorConfigPanel(String decoratorID) throws Exception {
        AbstractDecoratorConfigPanel panel = null;
        if (decoratorIdToConfigPanelMap.containsKey(decoratorID)) {
            Class<? extends AbstractDecoratorConfigPanel> panelClass = decoratorIdToConfigPanelMap.get(decoratorID);
            panel = panelClass.newInstance();
        } else {
            LOGGER.warn((Object)(String.valueOf(I18N.getMessage("es.kosmo.core.renderer.decorators.DecoratorFactory.It-does-not-exist-a-decorator-configuration-panel-for-the-type-{0}", new Object[]{decoratorID})) + "." + I18N.getString("es.kosmo.core.renderer.decorators.DecoratorFactory.The-default-config-panel-will-be-used")));
            panel = this.buildDefaultConfigPanel();
        }
        return panel;
    }

    protected AbstractDecoratorConfigPanel buildDefaultConfigPanel() {
        return new DefaultMarkerDecoratorConfigPanel();
    }
}


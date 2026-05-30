/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vividsolutions.jts.util.Assert
 */
package com.vividsolutions.jump.workbench;

import com.vividsolutions.jts.util.Assert;
import com.vividsolutions.jump.task.TaskMonitor;
import com.vividsolutions.jump.workbench.WorkbenchContext;
import com.vividsolutions.jump.workbench.datasource.LoadDatasetPlugIn;
import com.vividsolutions.jump.workbench.datasource.SaveDatasetAsPlugIn;
import com.vividsolutions.jump.workbench.plugin.EnableCheck;
import com.vividsolutions.jump.workbench.plugin.PlugIn;
import com.vividsolutions.jump.workbench.plugin.PlugInContext;
import com.vividsolutions.jump.workbench.ui.GUIUtil;
import com.vividsolutions.jump.workbench.ui.LayerViewPanel;
import com.vividsolutions.jump.workbench.ui.MainMenuNames;
import com.vividsolutions.jump.workbench.ui.TitledPopupMenu;
import com.vividsolutions.jump.workbench.ui.WorkbenchFrame;
import com.vividsolutions.jump.workbench.ui.cursortool.CursorTool;
import com.vividsolutions.jump.workbench.ui.cursortool.DrawPolygonFenceTool;
import com.vividsolutions.jump.workbench.ui.cursortool.DrawRectangleFenceTool;
import com.vividsolutions.jump.workbench.ui.cursortool.FeatureInfoTool;
import com.vividsolutions.jump.workbench.ui.cursortool.MeasureTool;
import com.vividsolutions.jump.workbench.ui.cursortool.OrCompositeTool;
import com.vividsolutions.jump.workbench.ui.cursortool.SelectFeaturesTool;
import com.vividsolutions.jump.workbench.ui.cursortool.editing.EditingPlugIn;
import com.vividsolutions.jump.workbench.ui.plugin.AboutPlugIn;
import com.vividsolutions.jump.workbench.ui.plugin.AddNewCategoryPlugIn;
import com.vividsolutions.jump.workbench.ui.plugin.AddNewFeaturesPlugIn;
import com.vividsolutions.jump.workbench.ui.plugin.AddNewLayerPlugIn;
import com.vividsolutions.jump.workbench.ui.plugin.ClearSelectionPlugIn;
import com.vividsolutions.jump.workbench.ui.plugin.DeleteSelectedItemsPlugIn;
import com.vividsolutions.jump.workbench.ui.plugin.EditSelectedFeaturePlugIn;
import com.vividsolutions.jump.workbench.ui.plugin.EditablePlugIn;
import com.vividsolutions.jump.workbench.ui.plugin.FeatureInfoPlugIn;
import com.vividsolutions.jump.workbench.ui.plugin.FeatureInstaller;
import com.vividsolutions.jump.workbench.ui.plugin.FeatureStatisticsPlugIn;
import com.vividsolutions.jump.workbench.ui.plugin.FirstTaskFramePlugIn;
import com.vividsolutions.jump.workbench.ui.plugin.KeyboardPlugIn;
import com.vividsolutions.jump.workbench.ui.plugin.LayerStatisticsPlugIn;
import com.vividsolutions.jump.workbench.ui.plugin.MoveLayerablePlugIn;
import com.vividsolutions.jump.workbench.ui.plugin.NewTaskPlugIn;
import com.vividsolutions.jump.workbench.ui.plugin.OpenProjectPlugIn;
import com.vividsolutions.jump.workbench.ui.plugin.OptionsPlugIn;
import com.vividsolutions.jump.workbench.ui.plugin.PersistentBlackboardPlugIn;
import com.vividsolutions.jump.workbench.ui.plugin.RedoPlugIn;
import com.vividsolutions.jump.workbench.ui.plugin.RemoveSelectedCategoriesPlugIn;
import com.vividsolutions.jump.workbench.ui.plugin.RemoveSelectedLayersPlugIn;
import com.vividsolutions.jump.workbench.ui.plugin.SaveProjectAsPlugIn;
import com.vividsolutions.jump.workbench.ui.plugin.SaveProjectPlugIn;
import com.vividsolutions.jump.workbench.ui.plugin.SelectFeaturesInFencePlugIn;
import com.vividsolutions.jump.workbench.ui.plugin.UndoPlugIn;
import com.vividsolutions.jump.workbench.ui.plugin.ValidateSelectedLayersPlugIn;
import com.vividsolutions.jump.workbench.ui.plugin.VerticesInFencePlugIn;
import com.vividsolutions.jump.workbench.ui.plugin.ViewAttributesPlugIn;
import com.vividsolutions.jump.workbench.ui.plugin.ViewSchemaPlugIn;
import com.vividsolutions.jump.workbench.ui.plugin.analysis.CalculateAreasAndLengthsPlugIn;
import com.vividsolutions.jump.workbench.ui.plugin.clipboard.CopySelectedItemsPlugIn;
import com.vividsolutions.jump.workbench.ui.plugin.clipboard.CutSelectedItemsPlugIn;
import com.vividsolutions.jump.workbench.ui.plugin.clipboard.PasteItemsPlugIn;
import com.vividsolutions.jump.workbench.ui.plugin.scalebar.ScaleBarPlugIn;
import com.vividsolutions.jump.workbench.ui.plugin.wms.EditWMSQueryPlugIn;
import com.vividsolutions.jump.workbench.ui.renderer.style.ArrowLineStringSegmentStyle;
import com.vividsolutions.jump.workbench.ui.renderer.style.ArrowTerminalDecorator;
import com.vividsolutions.jump.workbench.ui.renderer.style.CircleTerminalDecorator;
import com.vividsolutions.jump.workbench.ui.renderer.style.VertexIndexLineSegmentStyle;
import com.vividsolutions.jump.workbench.ui.renderer.style.VertexXYLineSegmentStyle;
import com.vividsolutions.jump.workbench.ui.snap.InstallGridPlugIn;
import com.vividsolutions.jump.workbench.ui.style.ChangeStylesPlugIn;
import com.vividsolutions.jump.workbench.ui.zoom.PanTool;
import com.vividsolutions.jump.workbench.ui.zoom.ZoomNextPlugIn;
import com.vividsolutions.jump.workbench.ui.zoom.ZoomPreviousPlugIn;
import com.vividsolutions.jump.workbench.ui.zoom.ZoomToClickPlugIn;
import com.vividsolutions.jump.workbench.ui.zoom.ZoomToFencePlugIn;
import com.vividsolutions.jump.workbench.ui.zoom.ZoomToFullExtentPlugIn;
import com.vividsolutions.jump.workbench.ui.zoom.ZoomToLayerPlugIn;
import com.vividsolutions.jump.workbench.ui.zoom.ZoomToSelectedItemsPlugIn;
import com.vividsolutions.jump.workbench.ui.zoom.ZoomTool;
import es.kosmo.desktop.core.plugins.ToolInstanceManager;
import es.kosmo.desktop.plugins.analysis.AssignValueToFieldPlugIn;
import es.kosmo.desktop.plugins.analysis.CalculateAttributeByExpressionPlugIn;
import es.kosmo.desktop.plugins.analysis.ConvexHullLayerPlugIn;
import es.kosmo.desktop.plugins.category.MoveCategoryPlugIn;
import es.kosmo.desktop.plugins.config.ConfigLayerFilterPlugIn;
import es.kosmo.desktop.plugins.conversion.AffineTransformationPlugIn;
import es.kosmo.desktop.plugins.conversion.PrecisionReducerPlugIn;
import es.kosmo.desktop.plugins.conversion.TableToLayerPlugIn;
import es.kosmo.desktop.plugins.help.OpenKosmoDesktopDocumentsPlugIn;
import es.kosmo.desktop.plugins.help.OpenKosmoDesktopVideoTutorialsPlugIn;
import es.kosmo.desktop.plugins.sdi.LoadSDIServicePlugIn;
import es.kosmo.desktop.plugins.symbology.ChangeSelectedFeatureTypeStylePlugIn;
import es.kosmo.desktop.plugins.symbology.SLDExportPlugIn;
import es.kosmo.desktop.plugins.symbology.SLDImportPlugIn;
import java.awt.Component;
import java.lang.reflect.Field;
import javax.swing.ImageIcon;
import javax.swing.JMenu;
import javax.swing.JPopupMenu;
import org.openjump.core.ui.plugin.wms.ZoomToWMSPlugIn;
import org.openjump.core.ui.style.decoration.VertexZValueStyle;
import org.saig.jump.lang.I18N;
import org.saig.jump.plugin.balloon.BalloonEditablePlugIn;
import org.saig.jump.plugin.check.CheckTopologyRelationsPlugIn;
import org.saig.jump.plugin.check.CheckTopologyRulesPlugIn;
import org.saig.jump.plugin.config.ChangeCategoryLanguagePlugIn;
import org.saig.jump.plugin.config.ConfigLayerStatePlugIn;
import org.saig.jump.plugin.config.ConfigPlugIn;
import org.saig.jump.plugin.config.ConfigStrategyOfQueryPlugIn;
import org.saig.jump.plugin.config.EnabledLayerPlugIn;
import org.saig.jump.plugin.config.EnabledRulePlugIn;
import org.saig.jump.plugin.data.AddTablePlugIn;
import org.saig.jump.plugin.datasource.CalcPlugIn;
import org.saig.jump.plugin.datasource.DGNPlugIn;
import org.saig.jump.plugin.datasource.DWGPlugIn;
import org.saig.jump.plugin.datasource.DXFPlugIn;
import org.saig.jump.plugin.datasource.ExcelPlugIn;
import org.saig.jump.plugin.datasource.GMLPlugIn;
import org.saig.jump.plugin.datasource.ImageFilePlugIn;
import org.saig.jump.plugin.datasource.IndexedShapeFilePlugIn;
import org.saig.jump.plugin.datasource.JDBCPlugIn;
import org.saig.jump.plugin.datasource.SaveTextBalloonsAsXMLPlugIn;
import org.saig.jump.plugin.datasource.SaveTextBalloonsPlugIn;
import org.saig.jump.plugin.editing.CloseTracePlugIn;
import org.saig.jump.plugin.editing.CopySelectedItemsToEditableLayerPlugIn;
import org.saig.jump.plugin.editing.EditSelectedFeatureAttributesPlugIn;
import org.saig.jump.plugin.editing.RedrawLastPointPlugIn;
import org.saig.jump.plugin.editing.RemoveLastPointPlugIn;
import org.saig.jump.plugin.editing.ReverseTracePlugIn;
import org.saig.jump.plugin.extensions.ExtensionManagerPlugIn;
import org.saig.jump.plugin.info.LayerInfoPlugIn;
import org.saig.jump.plugin.info.ViewInfoPlugIn;
import org.saig.jump.plugin.print.PrintLayoutPlugIn;
import org.saig.jump.plugin.query.QueryWizardPlugIn;
import org.saig.jump.plugin.sdi.wms.ChangeWMSStyleDialogPlugIn;
import org.saig.jump.plugin.sdi.wms.ViewWMSLegendPlugIn;
import org.saig.jump.plugin.simbology.EditSelectedRulePlugIn;
import org.saig.jump.plugin.simbology.LoadLayerSimbologyPlugIn;
import org.saig.jump.plugin.simbology.SLDEditorPlugIn;
import org.saig.jump.plugin.simbology.SaveLayerSimbologyPlugIn;
import org.saig.jump.plugin.stats.CalculateStatsPlugIn;
import org.saig.jump.plugin.utils.ChangeCategoryVisibilityPlugIn;
import org.saig.jump.plugin.utils.ChangeDataSourceInMemoryPlugIn;
import org.saig.jump.plugin.utils.CrossesLayersPlugIn;
import org.saig.jump.plugin.utils.LoadCategoryPlugIn;
import org.saig.jump.plugin.utils.MoveLayerableToCategoryPlugIn;
import org.saig.jump.plugin.utils.SaveAllViewLayersToShapePlugIn;
import org.saig.jump.plugin.utils.SaveCategoryPlugIn;
import org.saig.jump.plugin.utils.SortTableModelPlugIn;
import org.saig.jump.plugin.utils.SortTableTableModelPlugIn;
import org.saig.jump.plugin.utils.bookmark.BookmarksManagerPlugIn;
import org.saig.jump.plugin.utils.conversion.ClosedLinesToPolygonsPlugIn;
import org.saig.jump.plugin.utils.conversion.ExplodeEntitiesPlugIn;
import org.saig.jump.plugin.utils.conversion.ExtractSegmentsPlugIn;
import org.saig.jump.plugin.utils.conversion.ExtractVertexLayerPlugIn;
import org.saig.jump.plugin.utils.conversion.GetCentroidsPlugIn;
import org.saig.jump.plugin.utils.conversion.GetLinesFromPointsPlugIn;
import org.saig.jump.plugin.utils.conversion.GetLinesFromPolygonsPlugIn;
import org.saig.jump.plugin.utils.conversion.GetPointsFromLinesPlugIn;
import org.saig.jump.plugin.utils.generalization.DouglasPeuckerSimplificationPlugIn;
import org.saig.jump.plugin.utils.generalization.TopologyPreservingSimplifierPlugIn;
import org.saig.jump.plugin.utils.hiperlink.HiperLinkConfigurationPlugIn;
import org.saig.jump.plugin.utils.hiperlink.HiperLinkCursorTool;
import org.saig.jump.plugin.utils.labels.ChangeLabelOverlappingPlugIn;
import org.saig.jump.plugin.utils.labels.ChangeLabelVisibilityPlugIn;
import org.saig.jump.plugin.utils.labels.ChangeRepeatedLabelsPlugIn;
import org.saig.jump.plugin.utils.labels.SaveLabelsAslayerPlugIn;
import org.saig.jump.plugin.utils.labels.ScaleTextInCADLayerPlugIn;
import org.saig.jump.plugin.utils.locator.LocatorPlugIn;
import org.saig.jump.plugin.utils.project.CloseProjectPlugIn;
import org.saig.jump.plugin.utils.project.OpenRecentProjectsPlugIn;
import org.saig.jump.plugin.utils.project.ViewProjectPlugIn;
import org.saig.jump.plugin.utils.relations.ConfigRelationPlugIn;
import org.saig.jump.plugin.utils.topology.CheckLineConnectionPlugIn;
import org.saig.jump.plugin.utils.topology.ConfigureTopologyRulesPlugIn;
import org.saig.jump.plugin.utils.topology.DiscoverGapsPlugIn;
import org.saig.jump.plugin.utils.window.ArrangeViewsPlugIn;
import org.saig.jump.plugin.utils.window.ChangeWindowNamePlugIn;
import org.saig.jump.plugin.utils.window.CloseAllTasksPlugIn;
import org.saig.jump.plugin.utils.window.GroupWindowViewPlugIn;
import org.saig.jump.plugin.view.ViewPlugIn;
import org.saig.jump.plugin.zoom.PanToClickTool;
import org.saig.jump.tools.editing.RecalculateXYPointSortNumerationPlugIn;
import org.saig.jump.tools.measuring.MeasureAreaTool;
import org.saig.jump.widgets.config.ConfigDialog;
import org.saig.jump.widgets.config.ConfigHiperLinkToolPanel;

public class JUMPConfiguration {
    private FirstTaskFramePlugIn firstTaskFramePlugIn = new FirstTaskFramePlugIn();
    private NewTaskPlugIn newTaskPlugIn = new NewTaskPlugIn();
    private PersistentBlackboardPlugIn persistentBPlugIn = new PersistentBlackboardPlugIn();
    private OpenProjectPlugIn openProjectPlugIn = new OpenProjectPlugIn();
    private OpenRecentProjectsPlugIn openRecentProjectsPlugIn = new OpenRecentProjectsPlugIn();
    private CloseProjectPlugIn closeProjectPlugin = new CloseProjectPlugIn();
    private SaveProjectAsPlugIn saveProjectAsPlugIn = new SaveProjectAsPlugIn();
    private SaveProjectPlugIn saveProjectPlugIn = new SaveProjectPlugIn(this.saveProjectAsPlugIn);
    private LoadDatasetPlugIn loadDatasetPlugIn = new LoadDatasetPlugIn();
    private SaveDatasetAsPlugIn saveDatasetAsPlugIn = new SaveDatasetAsPlugIn();
    private SaveTextBalloonsAsXMLPlugIn saveTextBalloonsAsXMLPlugIn = new SaveTextBalloonsAsXMLPlugIn();
    private SaveTextBalloonsPlugIn saveTextBalloonsPlugIn = new SaveTextBalloonsPlugIn();
    private ExtensionManagerPlugIn extensionManagerPlugIn = new ExtensionManagerPlugIn();
    private ConfigPlugIn configPlugIn = new ConfigPlugIn();
    private PrintLayoutPlugIn printLayoutPlugIn = new PrintLayoutPlugIn();
    private EditingPlugIn editingPlugIn = new EditingPlugIn();
    private UndoPlugIn undoPlugIn = new UndoPlugIn();
    private RedoPlugIn redoPlugIn = new RedoPlugIn();
    private AddNewFeaturesPlugIn addNewFeaturesPlugIn = new AddNewFeaturesPlugIn();
    private EditSelectedFeaturePlugIn editSelectedFeaturePlugIn = new EditSelectedFeaturePlugIn();
    private SelectFeaturesInFencePlugIn selectFeaturesInFencePlugIn = new SelectFeaturesInFencePlugIn();
    private CutSelectedItemsPlugIn cutSelectedItemsPlugIn = new CutSelectedItemsPlugIn();
    private CopySelectedItemsPlugIn copySelectedItemsPlugIn = new CopySelectedItemsPlugIn();
    private CopySelectedItemsToEditableLayerPlugIn copySelectedItemsToEditableLayerPlugIn = new CopySelectedItemsToEditableLayerPlugIn();
    private PasteItemsPlugIn pasteItemsPlugIn = new PasteItemsPlugIn();
    private DeleteSelectedItemsPlugIn deleteSelectedItemsPlugIn = new DeleteSelectedItemsPlugIn();
    private OptionsPlugIn optionsPlugIn = new OptionsPlugIn();
    private CrossesLayersPlugIn crossesLayersPlugIn = new CrossesLayersPlugIn();
    private AddNewLayerPlugIn addNewLayerPlugIn = new AddNewLayerPlugIn();
    private AddNewCategoryPlugIn addNewCategoryPlugIn = new AddNewCategoryPlugIn();
    private RemoveSelectedLayersPlugIn removeSelectedLayersPlugIn = new RemoveSelectedLayersPlugIn();
    private RemoveSelectedCategoriesPlugIn removeSelectedCategoriesPlugIn = new RemoveSelectedCategoriesPlugIn();
    private ClearSelectionPlugIn clearSelectionPlugIn = new ClearSelectionPlugIn();
    private FeatureInfoPlugIn featureInfoPlugIn = new FeatureInfoPlugIn();
    private VerticesInFencePlugIn verticesInFencePlugIn = new VerticesInFencePlugIn();
    private ZoomToFullExtentPlugIn zoomToFullExtentPlugIn = new ZoomToFullExtentPlugIn();
    private ZoomToSelectedItemsPlugIn zoomToSelectedItemsPlugIn = new ZoomToSelectedItemsPlugIn();
    private ZoomNextPlugIn zoomNextPlugIn = new ZoomNextPlugIn();
    private ZoomPreviousPlugIn zoomPreviousPlugIn = new ZoomPreviousPlugIn();
    private ScaleBarPlugIn scaleBarPlugIn = new ScaleBarPlugIn();
    private ViewInfoPlugIn viewInfoPlugIn = new ViewInfoPlugIn();
    private HiperLinkConfigurationPlugIn hiperLinkConfigPlugIn = new HiperLinkConfigurationPlugIn();
    private CalculateAreasAndLengthsPlugIn calculateAreasAndLengthsPlugIn = new CalculateAreasAndLengthsPlugIn();
    private AssignValueToFieldPlugIn assignValueToFieldPlugIn = new AssignValueToFieldPlugIn();
    private CalculateAttributeByExpressionPlugIn calculatePlugIn = new CalculateAttributeByExpressionPlugIn();
    private ConvexHullLayerPlugIn convexHullLayerPlugIn = new ConvexHullLayerPlugIn();
    private RecalculateXYPointSortNumerationPlugIn recalculateXYPointSortNumerationPlugIn = new RecalculateXYPointSortNumerationPlugIn();
    private ExplodeEntitiesPlugIn explodeEntitiesPlugIn = new ExplodeEntitiesPlugIn();
    private ExtractSegmentsPlugIn extractSegmentsPlugIn = new ExtractSegmentsPlugIn();
    private ExtractVertexLayerPlugIn extractVertexLayerPlugIn = new ExtractVertexLayerPlugIn();
    private GetCentroidsPlugIn getCentroidsPlugIn = new GetCentroidsPlugIn();
    private GetLinesFromPolygonsPlugIn getLinesFromPolygonsPlugIn = new GetLinesFromPolygonsPlugIn();
    private GetLinesFromPointsPlugIn getLinesFromPointsPlugIn = new GetLinesFromPointsPlugIn();
    private ClosedLinesToPolygonsPlugIn closedLinesToPolygonsPlugIn = new ClosedLinesToPolygonsPlugIn();
    private GetPointsFromLinesPlugIn getPointsFromLinesPlugIn = new GetPointsFromLinesPlugIn();
    private TableToLayerPlugIn tableToLayerPlugIn = new TableToLayerPlugIn();
    private PrecisionReducerPlugIn precisionReducerPlugIn = new PrecisionReducerPlugIn();
    private AffineTransformationPlugIn affineTransformationPlugIn = new AffineTransformationPlugIn();
    private DouglasPeuckerSimplificationPlugIn douglasPeuckerSimplificationPlugIn = new DouglasPeuckerSimplificationPlugIn();
    private TopologyPreservingSimplifierPlugIn topologyPreservingSimplifierPlugIn = new TopologyPreservingSimplifierPlugIn();
    private CheckLineConnectionPlugIn checkLineConnectionPlugIn = new CheckLineConnectionPlugIn();
    private CheckTopologyRulesPlugIn checkTopologyRulesPlugIn = new CheckTopologyRulesPlugIn();
    private CheckTopologyRelationsPlugIn checkTopologyRelationsPlugIn = new CheckTopologyRelationsPlugIn();
    private DiscoverGapsPlugIn discoverHolesPlugIn = new DiscoverGapsPlugIn();
    private ValidateSelectedLayersPlugIn validateSelectedLayersPlugIn = new ValidateSelectedLayersPlugIn();
    private LocatorPlugIn locatorPlugIn = new LocatorPlugIn();
    private SaveAllViewLayersToShapePlugIn saveAllViewLayersToShapePlugIn = new SaveAllViewLayersToShapePlugIn();
    private FeatureStatisticsPlugIn featureStatisticsPlugIn = new FeatureStatisticsPlugIn();
    private LayerStatisticsPlugIn layerStatisticsPlugIn = new LayerStatisticsPlugIn();
    private ViewPlugIn viewPlugIn = new ViewPlugIn();
    private CloseAllTasksPlugIn closeAllTasksPlugIn = new CloseAllTasksPlugIn(false);
    private GroupWindowViewPlugIn groupWindowViewPlugIn = new GroupWindowViewPlugIn();
    private ChangeWindowNamePlugIn changeWindowNamePlugIn = new ChangeWindowNamePlugIn();
    private ArrangeViewsPlugIn arrangeHorizontalPlugIn = new ArrangeViewsPlugIn(1);
    private ArrangeViewsPlugIn arrangeVerticalPlugIn = new ArrangeViewsPlugIn(2);
    private ArrangeViewsPlugIn arrangeCascadePlugIn = new ArrangeViewsPlugIn(3);
    private ArrangeViewsPlugIn arrangeAllPlugIn = new ArrangeViewsPlugIn(4);
    private KeyboardPlugIn keyboardPlugIn = new KeyboardPlugIn();
    private OpenKosmoDesktopDocumentsPlugIn openKosmoDesktopDocumentsPlugIn = new OpenKosmoDesktopDocumentsPlugIn();
    private OpenKosmoDesktopVideoTutorialsPlugIn openKosmoDesktopVideoTutorialsPlugIn = new OpenKosmoDesktopVideoTutorialsPlugIn();
    private AboutPlugIn aboutPlugIn = new AboutPlugIn();
    private InstallGridPlugIn installGridPlugIn = new InstallGridPlugIn();
    private IndexedShapeFilePlugIn indexedShapeFilePlugIn = new IndexedShapeFilePlugIn();
    private ImageFilePlugIn imageFilePlugIn = new ImageFilePlugIn();
    private JDBCPlugIn jdbcPlugIn = new JDBCPlugIn();
    private DXFPlugIn dxfPlugIn = new DXFPlugIn();
    private DGNPlugIn dgnPlugIn = new DGNPlugIn();
    private DWGPlugIn dwgPlugIn = new DWGPlugIn();
    private ExcelPlugIn excelPlugIn = new ExcelPlugIn();
    private CalcPlugIn calcPlugIn = new CalcPlugIn();
    private GMLPlugIn gmlPlugIn = new GMLPlugIn();
    private RemoveLastPointPlugIn removeLastPointPlugIn = new RemoveLastPointPlugIn();
    private RedrawLastPointPlugIn redrawLastPointPlugIn = new RedrawLastPointPlugIn();
    private CloseTracePlugIn closeTracePlugIn = new CloseTracePlugIn();
    private ReverseTracePlugIn reverseTracePlugIn = new ReverseTracePlugIn();
    private ZoomTool zoomTool = new ZoomTool();
    private PanTool panTool = new PanTool();
    private ZoomToLayerPlugIn zoomToLayerPlugIn = new ZoomToLayerPlugIn();
    private PanToClickTool panToClickTool = new PanToClickTool();
    private FeatureInfoTool featureInfoTool = new FeatureInfoTool();
    private MeasureTool measureTool = new MeasureTool();
    private MeasureAreaTool measureAreaTool = new MeasureAreaTool();
    private SelectFeaturesTool selectFeaturesTool = new SelectFeaturesTool();
    private AddTablePlugIn addTablePlugIn = new AddTablePlugIn();
    private QueryWizardPlugIn queryWizard = new QueryWizardPlugIn();
    private ViewProjectPlugIn viewProyectPlugIn = new ViewProjectPlugIn();
    private ConfigRelationPlugIn configRelationPlugIn = new ConfigRelationPlugIn();
    private ConfigureTopologyRulesPlugIn configureProjectTopologyRulesPlugIn = new ConfigureTopologyRulesPlugIn(false);
    private ConfigureTopologyRulesPlugIn configureLayerTopologyRulesPlugIn = new ConfigureTopologyRulesPlugIn(true);
    private LayerInfoPlugIn layerInfoPlugIn = new LayerInfoPlugIn();
    private EditWMSQueryPlugIn editWMSQueryPlugIn = new EditWMSQueryPlugIn();
    private LoadSDIServicePlugIn loadSDIServicePlugIn = new LoadSDIServicePlugIn();
    private ZoomToWMSPlugIn zoomToWMSPlugIn = new ZoomToWMSPlugIn();
    private ViewWMSLegendPlugIn viewWMSLegendPlugIn = new ViewWMSLegendPlugIn();
    private ChangeWMSStyleDialogPlugIn changeWMSStyleDialogPlugIn = new ChangeWMSStyleDialogPlugIn();
    private ScaleTextInCADLayerPlugIn scaleTextInDxfLayerPluIn = new ScaleTextInCADLayerPlugIn();
    private EnabledRulePlugIn enabledRulePlugIn = new EnabledRulePlugIn();
    private EditSelectedRulePlugIn editSelectedRulePlugIn = new EditSelectedRulePlugIn();
    private BookmarksManagerPlugIn bookmarksManagerPlugIn = new BookmarksManagerPlugIn();
    private ChangeCategoryLanguagePlugIn changeCategoryLanguagePlugIn = new ChangeCategoryLanguagePlugIn();
    private CursorTool fenceTool = new OrCompositeTool(){

        @Override
        public String getName() {
            return I18N.getString("JUMPConfiguration.fenceTool_name");
        }
    }.add(new DrawRectangleFenceTool()).add(new DrawPolygonFenceTool());
    private ZoomToFencePlugIn zoomToFencePlugIn = new ZoomToFencePlugIn();
    private HiperLinkCursorTool hiperLinkCursorTool = new HiperLinkCursorTool();
    private EditablePlugIn editablePlugIn = new EditablePlugIn(this.editingPlugIn);
    private BalloonEditablePlugIn balloonEditablePlugIn = new BalloonEditablePlugIn();
    private ViewSchemaPlugIn viewSchemaPlugIn = new ViewSchemaPlugIn(this.editingPlugIn);
    private SortTableModelPlugIn ascSortPlugIn = new SortTableModelPlugIn(true);
    private SortTableModelPlugIn descSortPlugIn = new SortTableModelPlugIn(false);
    private SortTableTableModelPlugIn ascTableSortPlugIn = new SortTableTableModelPlugIn(true);
    private SortTableTableModelPlugIn descTableSortPlugIn = new SortTableTableModelPlugIn(false);
    private CalculateStatsPlugIn calculateStatsPlugIn = new CalculateStatsPlugIn(1);
    private ChangeCategoryVisibilityPlugIn changeCategoryVisibilityPlugIn = new ChangeCategoryVisibilityPlugIn();
    private LoadCategoryPlugIn loadCategoryPlugIn = new LoadCategoryPlugIn();
    private SaveCategoryPlugIn saveCategoryPlugIn = new SaveCategoryPlugIn();
    private EnabledLayerPlugIn enabledLayerPlugIn = new EnabledLayerPlugIn();
    private ConfigLayerFilterPlugIn configLayerFilterPlugIn = new ConfigLayerFilterPlugIn();
    private ConfigStrategyOfQueryPlugIn configStrategyOfQueryPlugIn = new ConfigStrategyOfQueryPlugIn();
    private ChangeDataSourceInMemoryPlugIn changeDataSourceInMemory = new ChangeDataSourceInMemoryPlugIn();
    private ChangeLabelVisibilityPlugIn changeLabelVisibilityPlugIn = new ChangeLabelVisibilityPlugIn();
    private ChangeLabelOverlappingPlugIn changeLabelOverlappingPlugIn = new ChangeLabelOverlappingPlugIn();
    private ChangeRepeatedLabelsPlugIn changeRepeatedLabelsPlugIn = new ChangeRepeatedLabelsPlugIn();
    private SaveLabelsAslayerPlugIn saveLabelsAsLayerPlugIn = new SaveLabelsAslayerPlugIn();
    private ChangeStylesPlugIn changeStylesPlugIn = new ChangeStylesPlugIn();
    private SLDEditorPlugIn sldEditorPlugIn = new SLDEditorPlugIn();
    private LoadLayerSimbologyPlugIn loadLayerSimbologyPlugIn = new LoadLayerSimbologyPlugIn();
    private SaveLayerSimbologyPlugIn saveLayerSimbologyPlugIn = new SaveLayerSimbologyPlugIn();
    private SLDExportPlugIn exportSymbologyToSLDFormatPlugIn = new SLDExportPlugIn();
    private SLDImportPlugIn sldImportPlugIn = new SLDImportPlugIn();
    private ChangeSelectedFeatureTypeStylePlugIn changeSelectedFTSPlugIn = new ChangeSelectedFeatureTypeStylePlugIn();
    private ViewAttributesPlugIn viewAttributesPlugIn = new ViewAttributesPlugIn(this.editingPlugIn);
    private EditSelectedFeatureAttributesPlugIn editSelectedFeatureAttributesPlugIn = new EditSelectedFeatureAttributesPlugIn();
    private MoveLayerablePlugIn moveToFirstPlugIn = MoveLayerablePlugIn.FIRST;
    private MoveLayerablePlugIn moveUpPlugIn = MoveLayerablePlugIn.UP;
    private MoveLayerableToCategoryPlugIn moveLayerableToCategoryPlugIn = new MoveLayerableToCategoryPlugIn();
    private MoveLayerablePlugIn moveDownPlugIn = MoveLayerablePlugIn.DOWN;
    private MoveLayerablePlugIn moveToLastPlugIn = MoveLayerablePlugIn.LAST;
    private ZoomToClickPlugIn zoomInPlugIn = new ZoomToClickPlugIn(2.0);
    private ZoomToClickPlugIn zoomOutPlugIn = new ZoomToClickPlugIn(0.5);
    private ZoomToClickPlugIn zoomToClickPlugIn = new ZoomToClickPlugIn(1.0);
    private ConfigLayerStatePlugIn configLayerStatePlugIn = new ConfigLayerStatePlugIn();
    private MoveCategoryPlugIn moveCategoryToFirstPlugIn = MoveCategoryPlugIn.FIRST;
    private MoveCategoryPlugIn moveCategoryUpPlugIn = MoveCategoryPlugIn.UP;
    private MoveCategoryPlugIn moveCategoryDownPlugIn = MoveCategoryPlugIn.DOWN;
    private MoveCategoryPlugIn moveCategoryToLastPlugIn = MoveCategoryPlugIn.LAST;
    private FeatureInstaller featureInstaller;

    public void configure(WorkbenchContext workbenchContext, TaskMonitor monitor) throws Exception {
        this.featureInstaller = new FeatureInstaller(workbenchContext);
        this.configureStyles(workbenchContext);
        monitor.report(I18N.getString("JUMPConfiguration.loading-toolbar"));
        this.configureToolBar(workbenchContext);
        monitor.report(I18N.getString("JUMPConfiguration.loading-main-menus"));
        this.configureMainMenus(workbenchContext);
        monitor.report(I18N.getString("JUMPConfiguration.loading-pop-up-menus"));
        this.configurePopUpMenus(workbenchContext);
        monitor.report(I18N.getString("JUMPConfiguration.initializing-built-in-plugins"));
        this.initializeInternalPlugIns(workbenchContext);
        monitor.report(I18N.getString("JUMPConfiguration.loading-external-plug-ins"));
        this.initializeExternalPlugIns(workbenchContext);
        this.extensionManagerPlugIn.loadDefaultExtensions(workbenchContext);
    }

    private void configureWMSQueryNamePopupMenu(WorkbenchContext workbenchContext) {
        TitledPopupMenu wmsLayerNamePopupMenu = workbenchContext.getWorkbench().getFrame().getWMSLayerNamePopupMenu();
        this.featureInstaller.addPopupMenuItem(wmsLayerNamePopupMenu, this.enabledLayerPlugIn, true, true);
        this.featureInstaller.addPopupMenuItem(wmsLayerNamePopupMenu, this.editWMSQueryPlugIn, false, true);
        this.featureInstaller.addPopupMenuItem(wmsLayerNamePopupMenu, this.zoomToLayerPlugIn, false, true);
        wmsLayerNamePopupMenu.addSeparator();
        this.featureInstaller.addPopupMenuItem(wmsLayerNamePopupMenu, this.changeWMSStyleDialogPlugIn, false, true);
        this.featureInstaller.addPopupMenuItem(wmsLayerNamePopupMenu, this.viewWMSLegendPlugIn, false, true);
        wmsLayerNamePopupMenu.addSeparator();
        this.featureInstaller.addPopupMenuItem(wmsLayerNamePopupMenu, this.moveToFirstPlugIn, false, true);
        this.featureInstaller.addPopupMenuItem(wmsLayerNamePopupMenu, this.moveUpPlugIn, false, true);
        wmsLayerNamePopupMenu.add(this.moveLayerableToCategoryPlugIn.getMenu());
        this.featureInstaller.addPopupMenuItem(wmsLayerNamePopupMenu, this.moveDownPlugIn, false, true);
        this.featureInstaller.addPopupMenuItem(wmsLayerNamePopupMenu, this.moveToLastPlugIn, false, true);
        wmsLayerNamePopupMenu.addSeparator();
        this.featureInstaller.addPopupMenuItem(wmsLayerNamePopupMenu, this.removeSelectedLayersPlugIn, false, true);
        wmsLayerNamePopupMenu.addSeparator();
        this.featureInstaller.addPopupMenuItem(wmsLayerNamePopupMenu, this.layerInfoPlugIn, false, true);
    }

    private void configureRulePopUpMenu(WorkbenchContext workbenchContext) {
        TitledPopupMenu rulePopupMenu = workbenchContext.getWorkbench().getFrame().getRulePopupMenu();
        this.featureInstaller.addPopupMenuItem(rulePopupMenu, this.enabledRulePlugIn, true, true);
        this.featureInstaller.addPopupMenuItem(rulePopupMenu, this.editSelectedRulePlugIn, false, true);
    }

    private void configurePopUpMenus(WorkbenchContext workbenchContext) {
        this.configureAttributePopupMenu(workbenchContext);
        this.configureTableAttributePopupMenu(workbenchContext);
        this.configureWMSQueryNamePopupMenu(workbenchContext);
        this.configureRulePopUpMenu(workbenchContext);
        this.configureCategoryPopupMenu(workbenchContext);
        this.configureLayerPopupMenu(workbenchContext);
        this.configureTextBalloonPopupMenu(workbenchContext);
        this.configureLayerViewPanelPopupMenu(workbenchContext);
        this.configureLayerablePopupMenu(workbenchContext);
    }

    private void configureLayerablePopupMenu(WorkbenchContext workbenchContext) {
        TitledPopupMenu layerNamePopupMenu = workbenchContext.getWorkbench().getFrame().getLayerablePopupmenu();
        this.featureInstaller.addPopupMenuItem(layerNamePopupMenu, this.enabledLayerPlugIn, true, true);
        this.featureInstaller.addPopupMenuItem(layerNamePopupMenu, this.configStrategyOfQueryPlugIn, true, true);
        this.featureInstaller.addPopupMenuItem(layerNamePopupMenu, this.removeSelectedLayersPlugIn, false, true);
        layerNamePopupMenu.addSeparator();
        this.featureInstaller.addPopupMenuItem(layerNamePopupMenu, this.moveToFirstPlugIn, false, true);
        layerNamePopupMenu.add(this.moveLayerableToCategoryPlugIn.getMenu());
        this.featureInstaller.addPopupMenuItem(layerNamePopupMenu, this.moveToLastPlugIn, false, true);
    }

    private void configureCategoryPopupMenu(WorkbenchContext workbenchContext) {
        TitledPopupMenu categoryPopupMenu = workbenchContext.getWorkbench().getFrame().getCategoryPopupMenu();
        this.featureInstaller.addPopupMenuItem(categoryPopupMenu, this.loadDatasetPlugIn, false, true);
        this.featureInstaller.addPopupMenuItem(categoryPopupMenu, this.loadSDIServicePlugIn, false, true);
        categoryPopupMenu.addSeparator();
        this.featureInstaller.addPopupMenuItem(categoryPopupMenu, this.changeCategoryVisibilityPlugIn, true, true);
        categoryPopupMenu.addSeparator();
        this.featureInstaller.addPopupMenuItem(categoryPopupMenu, this.loadCategoryPlugIn, false, true);
        this.featureInstaller.addPopupMenuItem(categoryPopupMenu, this.saveCategoryPlugIn, false, true);
        categoryPopupMenu.addSeparator();
        this.featureInstaller.addPopupMenuItem(categoryPopupMenu, this.changeCategoryLanguagePlugIn, false, true);
        categoryPopupMenu.addSeparator();
        this.featureInstaller.addPopupMenuItem(categoryPopupMenu, this.removeSelectedCategoriesPlugIn, false, true);
        categoryPopupMenu.addSeparator();
        JMenu moveCategoryMenu = new JMenu(MainMenuNames.DISPLACEMENT);
        this.featureInstaller.addToCustomMenuMenuItem((PlugIn)this.moveCategoryToFirstPlugIn, new String[0], false, true, moveCategoryMenu);
        this.featureInstaller.addToCustomMenuMenuItem((PlugIn)this.moveCategoryUpPlugIn, new String[0], false, true, moveCategoryMenu);
        this.featureInstaller.addToCustomMenuMenuItem((PlugIn)this.moveCategoryDownPlugIn, new String[0], false, true, moveCategoryMenu);
        this.featureInstaller.addToCustomMenuMenuItem((PlugIn)this.moveCategoryToLastPlugIn, new String[0], false, true, moveCategoryMenu);
        categoryPopupMenu.add(moveCategoryMenu);
    }

    private void configureAttributePopupMenu(WorkbenchContext workbenchContext) {
        TitledPopupMenu attributeTabPopupMenu = workbenchContext.getWorkbench().getFrame().getAttributeTabLayerNamePopupMenu();
        this.featureInstaller.addPopupMenuItem(attributeTabPopupMenu, this.editablePlugIn, true, true);
        this.featureInstaller.addPopupMenuItem(attributeTabPopupMenu, this.viewSchemaPlugIn, false, true);
        attributeTabPopupMenu.addSeparator();
        this.featureInstaller.addPopupMenuItem(attributeTabPopupMenu, this.ascSortPlugIn, false, true);
        this.featureInstaller.addPopupMenuItem(attributeTabPopupMenu, this.descSortPlugIn, false, true);
        attributeTabPopupMenu.addSeparator();
        this.featureInstaller.addPopupMenuItem(attributeTabPopupMenu, this.cutSelectedItemsPlugIn, false, true);
        this.featureInstaller.addPopupMenuItem(attributeTabPopupMenu, this.copySelectedItemsPlugIn, false, true);
        this.featureInstaller.addPopupMenuItem(attributeTabPopupMenu, this.copySelectedItemsToEditableLayerPlugIn, false, true);
        this.featureInstaller.addPopupMenuItem(attributeTabPopupMenu, this.deleteSelectedItemsPlugIn, false, true);
        attributeTabPopupMenu.addSeparator();
        this.featureInstaller.addPopupMenuItem(attributeTabPopupMenu, this.calculateStatsPlugIn, false, true);
        this.featureInstaller.addPopupMenuItem(attributeTabPopupMenu, this.calculatePlugIn, false, true);
    }

    private void configureTableAttributePopupMenu(WorkbenchContext workbenchContext) {
        JPopupMenu tableAttributeTabPopupMenu = workbenchContext.getWorkbench().getFrame().getTableAttributeTabLayerNamePopupMenu();
        this.featureInstaller.addPopupMenuItem(tableAttributeTabPopupMenu, this.ascTableSortPlugIn, false, true);
        this.featureInstaller.addPopupMenuItem(tableAttributeTabPopupMenu, this.descTableSortPlugIn, false, true);
    }

    private void configureTextBalloonPopupMenu(WorkbenchContext workbenchContext) {
        TitledPopupMenu textBallonPopupMenu = workbenchContext.getWorkbench().getFrame().getTextBalloonLayerNamePopupMenu();
        this.featureInstaller.addPopupMenuItem(textBallonPopupMenu, this.balloonEditablePlugIn, false, true);
        this.featureInstaller.addPopupMenuItem(textBallonPopupMenu, this.removeSelectedLayersPlugIn, false, true);
        textBallonPopupMenu.addSeparator();
        JMenu moveMenu = new JMenu(MainMenuNames.DISPLACEMENT);
        this.featureInstaller.addToCustomMenuMenuItem((PlugIn)this.moveToFirstPlugIn, new String[0], false, true, moveMenu);
        this.featureInstaller.addToCustomMenuMenuItem((PlugIn)this.moveUpPlugIn, new String[0], false, true, moveMenu);
        moveMenu.add(this.moveLayerableToCategoryPlugIn.getMenu());
        this.featureInstaller.addToCustomMenuMenuItem((PlugIn)this.moveDownPlugIn, new String[0], false, true, moveMenu);
        this.featureInstaller.addToCustomMenuMenuItem((PlugIn)this.moveToLastPlugIn, new String[0], false, true, moveMenu);
        textBallonPopupMenu.add(moveMenu);
        textBallonPopupMenu.addSeparator();
        this.featureInstaller.addPopupMenuItem(textBallonPopupMenu, this.saveTextBalloonsAsXMLPlugIn, false, true);
        this.featureInstaller.addPopupMenuItem(textBallonPopupMenu, this.saveTextBalloonsPlugIn, false, true);
    }

    private void configureLayerPopupMenu(WorkbenchContext workbenchContext) {
        TitledPopupMenu layerNamePopupMenu = workbenchContext.getWorkbench().getFrame().getLayerNamePopupMenu();
        JMenu advancedConfigurationMenu = new JMenu(MainMenuNames.ADVANCED_CONFIGURATION);
        JMenu simbologyMenu = new JMenu(MainMenuNames.SIMBOLOGY);
        JMenu moveMenu = new JMenu(MainMenuNames.DISPLACEMENT);
        this.featureInstaller.addPopupMenuItem(layerNamePopupMenu, this.editablePlugIn, true, true);
        layerNamePopupMenu.addSeparator();
        this.featureInstaller.addPopupMenuItem(layerNamePopupMenu, this.removeSelectedLayersPlugIn, false, true);
        this.featureInstaller.addPopupMenuItem(layerNamePopupMenu, this.zoomToLayerPlugIn, false, true);
        this.featureInstaller.addPopupMenuItem(layerNamePopupMenu, this.validateSelectedLayersPlugIn, false, true);
        layerNamePopupMenu.addSeparator();
        this.featureInstaller.addToCustomMenuMenuItem((PlugIn)this.changeStylesPlugIn, new String[0], false, true, simbologyMenu);
        this.featureInstaller.addToCustomMenuMenuItem((PlugIn)this.sldEditorPlugIn, new String[0], false, true, simbologyMenu);
        simbologyMenu.addSeparator();
        this.featureInstaller.addToCustomMenuMenuItem(this.loadLayerSimbologyPlugIn, new String[0], String.valueOf(this.loadLayerSimbologyPlugIn.getName()) + "...", false, GUIUtil.toSmallIcon(this.loadLayerSimbologyPlugIn.getIcon()), this.loadLayerSimbologyPlugIn.getCheck(), simbologyMenu);
        this.featureInstaller.addToCustomMenuMenuItem(this.saveLayerSimbologyPlugIn, new String[0], String.valueOf(this.saveLayerSimbologyPlugIn.getName()) + "...", false, GUIUtil.toSmallIcon(this.saveLayerSimbologyPlugIn.getIcon()), this.saveLayerSimbologyPlugIn.getCheck(), simbologyMenu);
        simbologyMenu.addSeparator();
        this.featureInstaller.addToCustomMenuMenuItem(this.exportSymbologyToSLDFormatPlugIn, new String[0], String.valueOf(this.exportSymbologyToSLDFormatPlugIn.getName()) + "...", false, GUIUtil.toSmallIcon(this.exportSymbologyToSLDFormatPlugIn.getIcon()), this.exportSymbologyToSLDFormatPlugIn.getCheck(), simbologyMenu);
        this.featureInstaller.addToCustomMenuMenuItem(this.sldImportPlugIn, new String[0], String.valueOf(this.sldImportPlugIn.getName()) + "...", false, GUIUtil.toSmallIcon(this.sldImportPlugIn.getIcon()), this.sldImportPlugIn.getCheck(), simbologyMenu);
        simbologyMenu.addSeparator();
        simbologyMenu.add(this.changeSelectedFTSPlugIn.getMenu());
        this.featureInstaller.addToCustomMenuMenuItem((PlugIn)this.changeLabelVisibilityPlugIn, new String[0], true, true, simbologyMenu);
        this.featureInstaller.addToCustomMenuMenuItem((PlugIn)this.changeLabelOverlappingPlugIn, new String[0], true, true, simbologyMenu);
        this.featureInstaller.addToCustomMenuMenuItem((PlugIn)this.changeRepeatedLabelsPlugIn, new String[0], true, true, simbologyMenu);
        this.featureInstaller.addToCustomMenuMenuItem((PlugIn)this.scaleTextInDxfLayerPluIn, new String[0], false, true, simbologyMenu);
        layerNamePopupMenu.add(simbologyMenu);
        layerNamePopupMenu.addSeparator();
        this.featureInstaller.addToCustomMenuMenuItem((PlugIn)this.layerInfoPlugIn, new String[0], false, true, advancedConfigurationMenu);
        advancedConfigurationMenu.addSeparator();
        this.featureInstaller.addToCustomMenuMenuItem((PlugIn)this.enabledLayerPlugIn, new String[0], true, true, advancedConfigurationMenu);
        this.featureInstaller.addToCustomMenuMenuItem(this.configLayerFilterPlugIn, new String[0], String.valueOf(this.configLayerFilterPlugIn.getName()) + "...", false, GUIUtil.toSmallIcon(this.configLayerFilterPlugIn.getIcon()), this.configLayerFilterPlugIn.getCheck(), advancedConfigurationMenu);
        this.featureInstaller.addToCustomMenuMenuItem((PlugIn)this.changeDataSourceInMemory, new String[0], true, true, advancedConfigurationMenu);
        this.featureInstaller.addToCustomMenuMenuItem((PlugIn)this.configStrategyOfQueryPlugIn, new String[0], true, true, advancedConfigurationMenu);
        advancedConfigurationMenu.addSeparator();
        this.featureInstaller.addToCustomMenuMenuItem((PlugIn)this.hiperLinkConfigPlugIn, new String[0], false, true, advancedConfigurationMenu);
        this.featureInstaller.addToCustomMenuMenuItem((PlugIn)this.configRelationPlugIn, new String[0], false, true, advancedConfigurationMenu);
        this.featureInstaller.addToCustomMenuMenuItem((PlugIn)this.configureLayerTopologyRulesPlugIn, new String[0], false, true, advancedConfigurationMenu);
        layerNamePopupMenu.add(advancedConfigurationMenu);
        layerNamePopupMenu.addSeparator();
        this.featureInstaller.addPopupMenuItem(layerNamePopupMenu, this.viewSchemaPlugIn, false, true);
        this.featureInstaller.addPopupMenuItem(layerNamePopupMenu, this.viewAttributesPlugIn, false, true);
        layerNamePopupMenu.addSeparator();
        this.featureInstaller.addPopupMenuItem(layerNamePopupMenu, this.cutSelectedItemsPlugIn, false, true);
        this.featureInstaller.addPopupMenuItem(layerNamePopupMenu, this.copySelectedItemsPlugIn, false, true);
        this.featureInstaller.addPopupMenuItem(layerNamePopupMenu, this.copySelectedItemsToEditableLayerPlugIn, false, true);
        this.featureInstaller.addPopupMenuItem(layerNamePopupMenu, this.pasteItemsPlugIn, false, true);
        this.featureInstaller.addPopupMenuItem(layerNamePopupMenu, this.deleteSelectedItemsPlugIn, false, true);
        layerNamePopupMenu.addSeparator();
        this.featureInstaller.addPopupMenuItem(layerNamePopupMenu, this.saveDatasetAsPlugIn, false, true);
        layerNamePopupMenu.addSeparator();
        this.featureInstaller.addToCustomMenuMenuItem((PlugIn)this.moveToFirstPlugIn, new String[0], false, true, moveMenu);
        this.featureInstaller.addToCustomMenuMenuItem((PlugIn)this.moveUpPlugIn, new String[0], false, true, moveMenu);
        moveMenu.add(this.moveLayerableToCategoryPlugIn.getMenu());
        this.featureInstaller.addToCustomMenuMenuItem((PlugIn)this.moveDownPlugIn, new String[0], false, true, moveMenu);
        this.featureInstaller.addToCustomMenuMenuItem((PlugIn)this.moveToLastPlugIn, new String[0], false, true, moveMenu);
        layerNamePopupMenu.add(moveMenu);
    }

    private void configureLayerViewPanelPopupMenu(WorkbenchContext workbenchContext) {
        JPopupMenu popupMenu = LayerViewPanel.popupMenu();
        this.featureInstaller.addPopupMenuItem(popupMenu, this.featureInfoPlugIn, false, true);
        this.featureInstaller.addPopupMenuItem(popupMenu, this.verticesInFencePlugIn, false, true);
        popupMenu.addSeparator();
        this.featureInstaller.addPopupMenuItem(popupMenu, this.removeLastPointPlugIn, false, true);
        this.featureInstaller.addPopupMenuItem(popupMenu, this.redrawLastPointPlugIn, false, true);
        this.featureInstaller.addPopupMenuItem(popupMenu, this.reverseTracePlugIn, false, true);
        this.featureInstaller.addPopupMenuItem(popupMenu, this.closeTracePlugIn, false, true);
        popupMenu.addSeparator();
        this.featureInstaller.addPopupMenuItem(popupMenu, this.zoomInPlugIn, false, true);
        this.featureInstaller.addPopupMenuItem(popupMenu, this.zoomOutPlugIn, false, true);
        this.featureInstaller.addPopupMenuItem(popupMenu, this.zoomToClickPlugIn, false, true);
        this.featureInstaller.addPopupMenuItem(popupMenu, this.zoomToFencePlugIn, false, true);
        this.featureInstaller.addPopupMenuItem(popupMenu, this.zoomToSelectedItemsPlugIn, false, true);
        popupMenu.addSeparator();
        this.featureInstaller.addPopupMenuItem(popupMenu, this.selectFeaturesInFencePlugIn, false, true);
        popupMenu.addSeparator();
        this.featureInstaller.addPopupMenuItem(popupMenu, this.cutSelectedItemsPlugIn, false, true);
        this.featureInstaller.addPopupMenuItem(popupMenu, this.copySelectedItemsPlugIn, false, true);
        this.featureInstaller.addPopupMenuItem(popupMenu, this.copySelectedItemsToEditableLayerPlugIn, false, true);
        this.featureInstaller.addPopupMenuItem(popupMenu, this.deleteSelectedItemsPlugIn, false, true);
    }

    private void configureMainMenus(WorkbenchContext workbenchContext) throws Exception {
        this.configureFileMainMenu(workbenchContext);
        this.configureEditMainMenu(workbenchContext);
        this.configureViewMainMenu(workbenchContext);
        this.configureToolsMainMenu(workbenchContext);
        this.configureWindowMainMenu(workbenchContext);
        this.configureHelpMainMenu(workbenchContext);
    }

    private void configureHelpMainMenu(WorkbenchContext workbenchContext) {
        this.featureInstaller.addMainMenuItem(this.keyboardPlugIn, MainMenuNames.HELP, String.valueOf(this.keyboardPlugIn.getName()) + "...", GUIUtil.toSmallIcon(this.keyboardPlugIn.getIcon()), null);
        this.featureInstaller.addMenuSeparator(MainMenuNames.HELP);
        this.featureInstaller.addMainMenuItem(this.openKosmoDesktopDocumentsPlugIn, MainMenuNames.HELP, String.valueOf(this.openKosmoDesktopDocumentsPlugIn.getName()) + "...", this.openKosmoDesktopDocumentsPlugIn.getIcon(), null);
        this.featureInstaller.addMainMenuItem(this.openKosmoDesktopVideoTutorialsPlugIn, MainMenuNames.HELP, String.valueOf(this.openKosmoDesktopVideoTutorialsPlugIn.getName()) + "...", this.openKosmoDesktopVideoTutorialsPlugIn.getIcon(), null);
        this.featureInstaller.addMenuSeparator(MainMenuNames.HELP);
        this.featureInstaller.addMainMenuItem(this.aboutPlugIn, MainMenuNames.HELP, String.valueOf(this.aboutPlugIn.getName()) + "...", GUIUtil.toSmallIcon(this.aboutPlugIn.getIcon()), null);
    }

    private void configureWindowMainMenu(WorkbenchContext workbenchContext) {
        this.featureInstaller.addMainMenuItem((PlugIn)this.closeAllTasksPlugIn, MainMenuNames.WINDOW, false, true);
        this.featureInstaller.addMainMenuItem((PlugIn)this.groupWindowViewPlugIn, MainMenuNames.WINDOW, true, true);
        this.featureInstaller.addMainMenuItem((PlugIn)this.changeWindowNamePlugIn, MainMenuNames.WINDOW, false, true);
        this.featureInstaller.addMainMenuItem((PlugIn)this.arrangeHorizontalPlugIn, MainMenuNames.WINDOW, false, true);
        this.featureInstaller.addMainMenuItem((PlugIn)this.arrangeVerticalPlugIn, MainMenuNames.WINDOW, false, true);
        this.featureInstaller.addMainMenuItem((PlugIn)this.arrangeCascadePlugIn, MainMenuNames.WINDOW, false, true);
        this.featureInstaller.addMainMenuItem((PlugIn)this.arrangeAllPlugIn, MainMenuNames.WINDOW, false, true);
        this.featureInstaller.addMenuSeparator(MainMenuNames.WINDOW);
    }

    private void configureToolsMainMenu(WorkbenchContext workbenchContext) throws Exception {
        this.featureInstaller.addMainMenuItem(this.calculateAreasAndLengthsPlugIn, new String[]{MainMenuNames.TOOLS, MainMenuNames.TOOLS_CALCULATE}, String.valueOf(this.calculateAreasAndLengthsPlugIn.getName()) + "...", false, GUIUtil.toSmallIcon(this.calculateAreasAndLengthsPlugIn.getIcon()), this.calculateAreasAndLengthsPlugIn.getCheck());
        this.featureInstaller.addMainMenuItem(this.assignValueToFieldPlugIn, new String[]{MainMenuNames.TOOLS, MainMenuNames.TOOLS_CALCULATE}, String.valueOf(this.assignValueToFieldPlugIn.getName()) + "...", false, GUIUtil.toSmallIcon(this.assignValueToFieldPlugIn.getIcon()), this.assignValueToFieldPlugIn.getCheck());
        this.featureInstaller.addMainMenuItem(this.calculatePlugIn, new String[]{MainMenuNames.TOOLS, MainMenuNames.TOOLS_CALCULATE}, String.valueOf(this.calculatePlugIn.getName()) + "...", false, GUIUtil.toSmallIcon(this.calculatePlugIn.getIcon()), this.calculatePlugIn.getCheck());
        this.featureInstaller.addMainMenuItem(this.queryWizard, new String[]{MainMenuNames.TOOLS, MainMenuNames.TOOLS_CALCULATE}, String.valueOf(this.queryWizard.getName()) + "...", false, GUIUtil.toSmallIcon(this.queryWizard.getIcon()), this.queryWizard.getCheck());
        this.featureInstaller.addMainMenuItem(this.convexHullLayerPlugIn, new String[]{MainMenuNames.TOOLS, MainMenuNames.TOOLS_CALCULATE}, String.valueOf(this.convexHullLayerPlugIn.getName()) + "...", false, GUIUtil.toSmallIcon(this.convexHullLayerPlugIn.getIcon()), this.convexHullLayerPlugIn.getCheck());
        this.featureInstaller.addMainMenuItem(this.recalculateXYPointSortNumerationPlugIn, new String[]{MainMenuNames.TOOLS, MainMenuNames.TOOLS_CALCULATE}, String.valueOf(this.recalculateXYPointSortNumerationPlugIn.getName()) + "...", false, GUIUtil.toSmallIcon(this.recalculateXYPointSortNumerationPlugIn.getIcon()), this.recalculateXYPointSortNumerationPlugIn.getCheck());
        this.featureInstaller.addMainMenuItem(this.getCentroidsPlugIn, new String[]{MainMenuNames.TOOLS, MainMenuNames.TOOLS_CALCULATE}, String.valueOf(this.getCentroidsPlugIn.getName()) + "...", false, GUIUtil.toSmallIcon(this.getCentroidsPlugIn.getIcon()), this.getCentroidsPlugIn.getCheck());
        this.featureInstaller.addMainMenuItem((PlugIn)this.explodeEntitiesPlugIn, new String[]{MainMenuNames.TOOLS, MainMenuNames.TOOLS_CONVERSION}, false, true);
        this.featureInstaller.addMainMenuItem((PlugIn)this.extractSegmentsPlugIn, new String[]{MainMenuNames.TOOLS, MainMenuNames.TOOLS_CONVERSION}, false, true);
        this.featureInstaller.addMainMenuItem((PlugIn)this.extractVertexLayerPlugIn, new String[]{MainMenuNames.TOOLS, MainMenuNames.TOOLS_CONVERSION}, false, true);
        this.featureInstaller.addMainMenuItem((PlugIn)this.getLinesFromPolygonsPlugIn, new String[]{MainMenuNames.TOOLS, MainMenuNames.TOOLS_CONVERSION}, false, true);
        this.featureInstaller.addMainMenuItem((PlugIn)this.getLinesFromPointsPlugIn, new String[]{MainMenuNames.TOOLS, MainMenuNames.TOOLS_CONVERSION}, false, true);
        this.featureInstaller.addMainMenuItem((PlugIn)this.closedLinesToPolygonsPlugIn, new String[]{MainMenuNames.TOOLS, MainMenuNames.TOOLS_CONVERSION}, false, true);
        this.featureInstaller.addMainMenuItem((PlugIn)this.getPointsFromLinesPlugIn, new String[]{MainMenuNames.TOOLS, MainMenuNames.TOOLS_CONVERSION}, false, true);
        this.featureInstaller.addMainMenuItem((PlugIn)this.tableToLayerPlugIn, new String[]{MainMenuNames.TOOLS, MainMenuNames.TOOLS_CONVERSION}, false, true);
        this.featureInstaller.addMainMenuItem((PlugIn)this.affineTransformationPlugIn, new String[]{MainMenuNames.TOOLS, MainMenuNames.TOOLS_CONVERSION}, false, true);
        this.featureInstaller.addMainMenuItem((PlugIn)this.douglasPeuckerSimplificationPlugIn, new String[]{MainMenuNames.TOOLS, MainMenuNames.TOOLS_GENERALIZATION}, false, true);
        this.featureInstaller.addMainMenuItem((PlugIn)this.topologyPreservingSimplifierPlugIn, new String[]{MainMenuNames.TOOLS, MainMenuNames.TOOLS_GENERALIZATION}, false, true);
        this.featureInstaller.addMainMenuItem((PlugIn)this.precisionReducerPlugIn, new String[]{MainMenuNames.TOOLS, MainMenuNames.TOOLS_GENERALIZATION}, false, true);
        this.featureInstaller.addMainMenuItem((PlugIn)this.checkLineConnectionPlugIn, new String[]{MainMenuNames.TOOLS, MainMenuNames.TOOLS_TOPOLOGYC}, false, true);
        this.featureInstaller.addMainMenuItem((PlugIn)this.checkTopologyRulesPlugIn, new String[]{MainMenuNames.TOOLS, MainMenuNames.TOOLS_TOPOLOGYC}, false, true);
        this.featureInstaller.addMainMenuItem((PlugIn)this.checkTopologyRelationsPlugIn, new String[]{MainMenuNames.TOOLS, MainMenuNames.TOOLS_TOPOLOGYC}, false, true);
        this.featureInstaller.addMainMenuItem((PlugIn)this.discoverHolesPlugIn, new String[]{MainMenuNames.TOOLS, MainMenuNames.TOOLS_TOPOLOGYC}, false, true);
        this.featureInstaller.addMainMenuItem((PlugIn)this.validateSelectedLayersPlugIn, new String[]{MainMenuNames.TOOLS, MainMenuNames.TOOLS_TOPOLOGYC}, false, true);
        this.featureInstaller.addMainMenuItem((PlugIn)this.saveAllViewLayersToShapePlugIn, new String[]{MainMenuNames.TOOLS, MainMenuNames.TOOLS_UTILS}, false, true);
    }

    private void configureViewMainMenu(WorkbenchContext workbenchContext) {
        this.featureInstaller.addMainMenuItem(this.newTaskPlugIn, MainMenuNames.VIEW, String.valueOf(this.newTaskPlugIn.getName()) + "...", null, null);
        this.featureInstaller.addMainMenuItem(this.addNewLayerPlugIn, MainMenuNames.VIEW, String.valueOf(this.addNewLayerPlugIn.getName()) + "...", GUIUtil.toSmallIcon(this.addNewLayerPlugIn.getIcon()), this.addNewLayerPlugIn.getCheck());
        this.featureInstaller.addMainMenuItem((PlugIn)this.addNewCategoryPlugIn, MainMenuNames.VIEW, false, true);
        this.featureInstaller.addMenuSeparator(MainMenuNames.VIEW);
        this.featureInstaller.addMainMenuItem((PlugIn)this.loadDatasetPlugIn, MainMenuNames.VIEW, false, true);
        this.featureInstaller.addMainMenuItem((PlugIn)this.loadSDIServicePlugIn, MainMenuNames.VIEW, false, true);
        this.featureInstaller.addMainMenuItem((PlugIn)this.loadCategoryPlugIn, MainMenuNames.VIEW, false, true);
        this.featureInstaller.addMenuSeparator(MainMenuNames.VIEW);
        this.featureInstaller.addMainMenuItem((PlugIn)this.saveDatasetAsPlugIn, MainMenuNames.VIEW, false, true);
        this.featureInstaller.addMainMenuItem(this.saveCategoryPlugIn, MainMenuNames.VIEW, String.valueOf(this.saveCategoryPlugIn.getName()) + "...", GUIUtil.toSmallIcon(this.saveCategoryPlugIn.getIcon()), this.saveCategoryPlugIn.getCheck());
        this.featureInstaller.addMenuSeparator(MainMenuNames.VIEW);
        this.featureInstaller.addMainMenuItem((PlugIn)this.removeSelectedLayersPlugIn, MainMenuNames.VIEW, false, true);
        this.featureInstaller.addMainMenuItem((PlugIn)this.removeSelectedCategoriesPlugIn, MainMenuNames.VIEW, false, true);
        this.featureInstaller.addMainMenuItem(this.configLayerStatePlugIn, MainMenuNames.VIEW, String.valueOf(this.configLayerStatePlugIn.getName()) + "...", GUIUtil.toSmallIcon(this.configLayerStatePlugIn.getIcon()), ConfigLayerStatePlugIn.createEnableCheck(workbenchContext));
        this.featureInstaller.addMenuSeparator(MainMenuNames.VIEW);
        this.featureInstaller.addMainMenuItem((PlugIn)this.scaleBarPlugIn, MainMenuNames.VIEW, true, true);
        this.featureInstaller.addMenuSeparator(MainMenuNames.VIEW);
        this.featureInstaller.addMainMenuItem(this.viewInfoPlugIn, MainMenuNames.VIEW, String.valueOf(this.viewInfoPlugIn.getName()) + "...", GUIUtil.toSmallIcon(this.viewInfoPlugIn.getIcon()), this.viewInfoPlugIn.getCheck());
    }

    private void configureEditMainMenu(WorkbenchContext workbenchContext) throws Exception {
        this.featureInstaller.addMainMenuItem((PlugIn)this.undoPlugIn, MainMenuNames.EDIT, false, true);
        this.featureInstaller.addMainMenuItem((PlugIn)this.redoPlugIn, MainMenuNames.EDIT, false, true);
        this.featureInstaller.addMenuSeparator(MainMenuNames.EDIT);
        this.featureInstaller.addMainMenuItem((PlugIn)this.deleteSelectedItemsPlugIn, MainMenuNames.EDIT, false, true);
        this.featureInstaller.addMainMenuItem((PlugIn)this.copySelectedItemsPlugIn, MainMenuNames.EDIT, false, true);
        this.featureInstaller.addMainMenuItem((PlugIn)this.cutSelectedItemsPlugIn, MainMenuNames.EDIT, false, true);
        this.featureInstaller.addMainMenuItem((PlugIn)this.pasteItemsPlugIn, MainMenuNames.EDIT, false, true);
        this.featureInstaller.addMenuSeparator(MainMenuNames.EDIT);
        this.editingPlugIn.createMainMenuItem(new String[]{MainMenuNames.EDIT}, GUIUtil.toSmallIcon(this.editingPlugIn.getIcon()), workbenchContext, EditingPlugIn.createEnableCheck(workbenchContext));
        this.featureInstaller.addMainMenuItem((PlugIn)this.viewSchemaPlugIn, MainMenuNames.EDIT, false, true);
        this.featureInstaller.addMainMenuItem((PlugIn)this.viewAttributesPlugIn, MainMenuNames.EDIT, false, true);
        this.featureInstaller.addMenuSeparator(MainMenuNames.EDIT);
        this.featureInstaller.addMainMenuItem((PlugIn)this.changeStylesPlugIn, MainMenuNames.EDIT, false, true);
        this.featureInstaller.addMainMenuItem((PlugIn)this.sldEditorPlugIn, MainMenuNames.EDIT, false, true);
        this.featureInstaller.addMenuSeparator(MainMenuNames.EDIT);
        this.featureInstaller.addMainMenuItem((PlugIn)this.hiperLinkConfigPlugIn, MainMenuNames.EDIT, false, true);
        this.featureInstaller.addMainMenuItem((PlugIn)this.configRelationPlugIn, MainMenuNames.EDIT, false, true);
        this.featureInstaller.addMainMenuItem((PlugIn)this.configureLayerTopologyRulesPlugIn, MainMenuNames.EDIT, false, true);
        this.featureInstaller.addMenuSeparator(MainMenuNames.EDIT);
        this.featureInstaller.addMainMenuItem((PlugIn)this.featureInfoPlugIn, MainMenuNames.EDIT, false, true);
        this.featureInstaller.addMainMenuItem((PlugIn)this.clearSelectionPlugIn, MainMenuNames.EDIT, false, true);
        this.featureInstaller.addMainMenuItem((PlugIn)this.selectFeaturesInFencePlugIn, MainMenuNames.EDIT, false, true);
        this.featureInstaller.addMainMenuItem((PlugIn)this.crossesLayersPlugIn, MainMenuNames.EDIT, false, true);
        this.featureInstaller.addMainMenuItem((PlugIn)this.editSelectedFeaturePlugIn, MainMenuNames.EDIT, false, true);
        this.featureInstaller.addMenuSeparator(MainMenuNames.EDIT);
        this.featureInstaller.addMainMenuItem(this.optionsPlugIn, MainMenuNames.EDIT, String.valueOf(this.optionsPlugIn.getName()) + "...", GUIUtil.toSmallIcon(this.optionsPlugIn.getIcon()), null);
        this.featureInstaller.addMainMenuItem((PlugIn)this.layerInfoPlugIn, MainMenuNames.EDIT, false, true);
    }

    private void configureFileMainMenu(WorkbenchContext workbenchContext) {
        this.featureInstaller.addMainMenuItem(this.openProjectPlugIn, MainMenuNames.FILE, String.valueOf(this.openProjectPlugIn.getName()) + "...", null, null);
        this.featureInstaller.menuBarMenu(MainMenuNames.FILE).add((Component)this.openRecentProjectsPlugIn.getMenu(), 1);
        this.featureInstaller.addMenuSeparator(MainMenuNames.FILE);
        this.featureInstaller.addMainMenuItem(this.closeProjectPlugin, MainMenuNames.FILE, this.closeProjectPlugin.getName(), null, CloseProjectPlugIn.createEnableCheck(workbenchContext));
        this.featureInstaller.addMainMenuItem(this.saveProjectPlugIn, MainMenuNames.FILE, this.saveProjectPlugIn.getName(), null, SaveProjectPlugIn.createEnableCheck(workbenchContext));
        this.featureInstaller.addMainMenuItem(this.saveProjectAsPlugIn, MainMenuNames.FILE, String.valueOf(this.saveProjectAsPlugIn.getName()) + "...", null, SaveProjectAsPlugIn.createEnableCheck(workbenchContext));
        this.featureInstaller.addMenuSeparator(MainMenuNames.FILE);
        this.featureInstaller.addMainMenuItem(this.viewProyectPlugIn, MainMenuNames.FILE, String.valueOf(this.viewProyectPlugIn.getName()) + "...", GUIUtil.toSmallIcon(this.viewProyectPlugIn.getIcon()), null);
        this.featureInstaller.addMainMenuItem(this.extensionManagerPlugIn, MainMenuNames.FILE, String.valueOf(this.extensionManagerPlugIn.getName()) + "...", GUIUtil.toSmallIcon(this.extensionManagerPlugIn.getIcon()), ExtensionManagerPlugIn.createEnableCheck(workbenchContext));
        this.featureInstaller.addMenuSeparator(MainMenuNames.FILE);
        this.featureInstaller.addMainMenuItem(this.configPlugIn, MainMenuNames.FILE, String.valueOf(this.configPlugIn.getName()) + "...", GUIUtil.toSmallIcon(this.configPlugIn.getIcon()), ConfigPlugIn.createEnableCheck(workbenchContext));
    }

    private void configureStyles(WorkbenchContext workbenchContext) {
        WorkbenchFrame frame = workbenchContext.getWorkbench().getFrame();
        frame.addSwappableStyleClass(ArrowTerminalDecorator.FeathersStart.class);
        frame.addSwappableStyleClass(ArrowTerminalDecorator.FeathersEnd.class);
        frame.addSwappableStyleClass(ArrowTerminalDecorator.OpenStart.class);
        frame.addSwappableStyleClass(ArrowTerminalDecorator.OpenEnd.class);
        frame.addSwappableStyleClass(ArrowTerminalDecorator.SolidStart.class);
        frame.addSwappableStyleClass(ArrowTerminalDecorator.SolidEnd.class);
        frame.addSwappableStyleClass(ArrowTerminalDecorator.NarrowSolidStart.class);
        frame.addSwappableStyleClass(ArrowTerminalDecorator.NarrowSolidEnd.class);
        frame.addSwappableStyleClass(ArrowLineStringSegmentStyle.Open.class);
        frame.addSwappableStyleClass(ArrowLineStringSegmentStyle.NarrowSolid.class);
        frame.addSwappableStyleClass(ArrowLineStringSegmentStyle.Solid.class);
        frame.addSwappableStyleClass(CircleTerminalDecorator.Start.class);
        frame.addSwappableStyleClass(CircleTerminalDecorator.End.class);
        frame.addSwappableStyleClass(VertexXYLineSegmentStyle.VertexXY.class);
        frame.addSwappableStyleClass(VertexZValueStyle.VertexZValue.class);
        frame.addSwappableStyleClass(VertexIndexLineSegmentStyle.VertexIndex.class);
    }

    private void configureToolBar(WorkbenchContext workbenchContext) {
        WorkbenchFrame frame = workbenchContext.getWorkbench().getFrame();
        frame.getToolBar().addCursorTool(this.zoomTool.getName(), (CursorTool)this.zoomTool, (EnableCheck)null);
        frame.getToolBar().setDefaultCursorTool(this.zoomTool);
        frame.getToolBar().addCursorTool(this.panTool.getName(), (CursorTool)this.panTool, PanTool.createEnableCheck(workbenchContext));
        frame.getToolBar().addPlugIn(this.zoomPreviousPlugIn, workbenchContext);
        frame.getToolBar().addPlugIn(this.zoomNextPlugIn, workbenchContext);
        frame.getToolBar().addPlugIn(this.zoomToFullExtentPlugIn, workbenchContext);
        frame.getToolBar().addPlugIn(this.zoomToLayerPlugIn, workbenchContext);
        frame.getToolBar().addPlugIn(this.zoomToSelectedItemsPlugIn, workbenchContext);
        frame.getToolBar().addCursorTool(this.panToClickTool.getName(), (CursorTool)this.panToClickTool, PanToClickTool.createEnableCheck(workbenchContext));
        frame.getToolBar().addPlugIn(this.locatorPlugIn, workbenchContext);
        frame.getToolBar().addSeparator();
        frame.getToolBar().addPlugIn(this.loadDatasetPlugIn, workbenchContext);
        frame.getToolBar().addPlugIn(this.loadSDIServicePlugIn, workbenchContext);
        frame.getToolBar().addPlugIn(this.addTablePlugIn, workbenchContext);
        frame.getToolBar().addPlugIn(this.saveDatasetAsPlugIn, workbenchContext);
        frame.getToolBar().addCursorTool(this.featureInfoTool.getName(), (CursorTool)this.featureInfoTool, FeatureInfoTool.createEnableCheck(workbenchContext, this.featureInfoTool));
        frame.getToolBar().addCursorTool(this.hiperLinkCursorTool.getName(), (CursorTool)this.hiperLinkCursorTool, HiperLinkCursorTool.createEnableCheck(workbenchContext, this.hiperLinkCursorTool));
        ConfigPlugIn.getDialog().addConfigPanel(new ConfigHiperLinkToolPanel(workbenchContext.getBlackboard()), ConfigDialog.TOOLS_MAIN_CATEGORY_NAME, HiperLinkCursorTool.NAME);
        frame.getToolBar().addCursorTool(this.measureTool.getName(), (CursorTool)this.measureTool, MeasureTool.createEnableCheck(workbenchContext, this.measureTool));
        frame.getToolBar().addCursorTool(this.measureAreaTool.getName(), (CursorTool)this.measureAreaTool, MeasureAreaTool.createEnableCheck(workbenchContext));
        frame.getToolBar().addCursorTool(this.fenceTool.getName(), this.fenceTool, MeasureAreaTool.createEnableCheck(workbenchContext));
        frame.getToolBar().addSeparator();
        frame.getToolBar().addCursorTool(this.selectFeaturesTool.getName(), (CursorTool)this.selectFeaturesTool, SelectFeaturesTool.createEnableCheck(workbenchContext, this.selectFeaturesTool));
        frame.getToolBar().setDefaultEditingCursorTool(this.selectFeaturesTool);
        frame.getToolBar().addPlugIn(this.clearSelectionPlugIn, workbenchContext);
        frame.getToolBar().addPlugIn(this.editingPlugIn.getIcon(), this.editingPlugIn, EditingPlugIn.createEnableCheck(workbenchContext), workbenchContext);
        frame.getToolBar().addPlugIn(this.viewAttributesPlugIn.getIcon(), this.viewAttributesPlugIn, ViewAttributesPlugIn.createEnableCheck(workbenchContext), workbenchContext);
        frame.getToolBar().addPlugIn(GUIUtil.resize((ImageIcon)this.queryWizard.getIcon(), 20), this.queryWizard, QueryWizardPlugIn.createEnableCheck(workbenchContext), workbenchContext);
        frame.getToolBar().addPlugIn(this.configureProjectTopologyRulesPlugIn, workbenchContext);
        frame.getToolBar().addPlugIn(this.viewPlugIn.getIcon(), this.viewPlugIn, ViewPlugIn.createEnableCheck(workbenchContext), workbenchContext);
        frame.getToolBar().addSeparator();
        frame.getToolBar().addPlugIn(this.undoPlugIn.getIcon(), this.undoPlugIn, UndoPlugIn.createEnableCheck(workbenchContext), workbenchContext);
        frame.getToolBar().addPlugIn(this.redoPlugIn.getIcon(), this.redoPlugIn, RedoPlugIn.createEnableCheck(workbenchContext), workbenchContext);
        frame.getToolBar().addSeparator();
        frame.getToolBar().addPlugIn(GUIUtil.resize((ImageIcon)this.printLayoutPlugIn.getIcon(), 20), this.printLayoutPlugIn, PrintLayoutPlugIn.createEnableCheck(workbenchContext), workbenchContext);
        frame.getToolBar().addSeparator();
        frame.getToolBar().addPlugIn(this.extensionManagerPlugIn.getIcon(), null, this.extensionManagerPlugIn, "Ext", this.extensionManagerPlugIn.getCheck(), workbenchContext);
        frame.getToolBar().addSeparator();
        ToolInstanceManager toolInstanceManager = ToolInstanceManager.instance();
        toolInstanceManager.registerCursorTool(this.zoomTool);
        toolInstanceManager.registerCursorTool(this.panTool);
        toolInstanceManager.registerCursorTool(this.selectFeaturesTool);
    }

    private void initializeExternalPlugIns(WorkbenchContext workbenchContext) throws Exception {
        workbenchContext.getWorkbench().getPlugInManager().load();
    }

    private void initializeInternalPlugIns(WorkbenchContext workbenchContext) throws Exception {
        Field[] fields = this.getClass().getDeclaredFields();
        int i = 0;
        while (i < fields.length) {
            Object field = null;
            try {
                field = fields[i].get(this);
            }
            catch (IllegalAccessException e) {
                Assert.shouldNeverReachHere();
            }
            if (field instanceof PlugIn) {
                PlugIn plugIn = (PlugIn)field;
                plugIn.initialize(new PlugInContext(workbenchContext, null, null, null, null));
                ToolInstanceManager.instance().registerPlugIn(plugIn);
            }
            ++i;
        }
    }
}


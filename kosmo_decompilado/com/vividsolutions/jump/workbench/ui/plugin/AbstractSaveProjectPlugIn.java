/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.exolab.castor.mapping.Mapping
 *  org.exolab.castor.util.LocalConfiguration
 *  org.exolab.castor.xml.Marshaller
 */
package com.vividsolutions.jump.workbench.ui.plugin;

import com.vividsolutions.jump.feature.FeatureSchema;
import com.vividsolutions.jump.util.FileUtil;
import com.vividsolutions.jump.workbench.JUMPWorkbench;
import com.vividsolutions.jump.workbench.JUMPWorkbenchContext;
import com.vividsolutions.jump.workbench.model.Category;
import com.vividsolutions.jump.workbench.model.CategoryCollection;
import com.vividsolutions.jump.workbench.model.Layer;
import com.vividsolutions.jump.workbench.model.LayerSimbology;
import com.vividsolutions.jump.workbench.model.Project;
import com.vividsolutions.jump.workbench.model.Task;
import com.vividsolutions.jump.workbench.plugin.AbstractPlugIn;
import com.vividsolutions.jump.workbench.ui.GUIUtil;
import com.vividsolutions.jump.workbench.ui.WorkbenchFrame;
import com.vividsolutions.jump.workbench.ui.plugin.PersistentBlackboardPlugIn;
import com.vividsolutions.jump.workbench.ui.plugin.SaveProjectPlugIn;
import com.vividsolutions.jump.workbench.ui.renderer.style.Style;
import java.io.File;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import org.exolab.castor.mapping.Mapping;
import org.exolab.castor.util.LocalConfiguration;
import org.exolab.castor.xml.Marshaller;
import org.saig.core.model.data.Table;
import org.saig.core.model.project.ProjectManagerFrame;
import org.saig.core.util.TranslationWrapper;
import org.saig.jump.lang.I18N;
import org.saig.jump.util.LoadXMLMappings;
import org.saig.jump.widgets.print.Page;
import org.saig.jump.widgets.util.DialogFactory;

public abstract class AbstractSaveProjectPlugIn
extends AbstractPlugIn {
    protected void save(List<Task> tasks, List<Table> tables, List<Page> layouts, File file, WorkbenchFrame workbenchFrame) throws Exception {
        this.save(tasks, tables, layouts, file, workbenchFrame, true);
    }

    protected void save(List<Task> tasks, List<Table> tables, List<Page> layouts, File file, WorkbenchFrame workbenchFrame, boolean updateProjectTitle) throws Exception {
        StringWriter stringWriter = new StringWriter();
        String relativePath = (String)PersistentBlackboardPlugIn.get(workbenchFrame.getContext().getBlackboard()).get(SaveProjectPlugIn.LAYERS_RELATIVE_PATH);
        boolean useRelativePath = false;
        if (relativePath != null && !relativePath.equals("")) {
            int resultado = DialogFactory.showYesNoDialog(workbenchFrame, String.valueOf(I18N.getMessage("workbench.ui.plugin.AbstractSaveProjectPlugIn.you-have-configured-a-relative-path-to-the-layers-in-the-directory-{0}", new Object[]{relativePath})) + ".\n" + I18N.getString("workbench.ui.plugin.AbstractSaveProjectPlugIn.do-you-wnat-to-save-the-layers-relative-to-that-directory"), I18N.getString("workbench.ui.plugin.AbstractSaveProjectPlugIn.save-layers-relative-to-the-directory"));
            boolean bl = useRelativePath = resultado == 0;
            if (!relativePath.endsWith(File.separator)) {
                relativePath = String.valueOf(relativePath) + File.separator;
            }
        }
        Project currentProject = JUMPWorkbench.getFrameInstance().getContext().getProject();
        Project newProject = new Project();
        if (currentProject == null) {
            newProject.setCreationDate(new Date(System.currentTimeMillis()));
        } else {
            newProject.setCreationDate(currentProject.getCreationDate());
        }
        ProjectManagerFrame pmFrame = ((JUMPWorkbenchContext)JUMPWorkbench.getFrameInstance().getContext()).getProjectManagerFrame();
        String projectName = GUIUtil.nameWithoutExtension(file);
        String projectDescription = "";
        String projectAuthor = System.getProperty("user.name");
        if (pmFrame != null) {
            projectName = pmFrame.getProjectName();
            projectAuthor = pmFrame.getProjectAuthor();
            projectDescription = pmFrame.getProjectDescription();
        }
        newProject.setAvailablesLocales(currentProject.getAvailablesLocales());
        newProject.setActiveLocale(currentProject.getActiveLocale(), false);
        newProject.setName(projectName);
        newProject.setAuthor(projectAuthor);
        newProject.setVersion("3.0 RC1 (20130528)");
        newProject.setLastModificationDate(new Date(System.currentTimeMillis()));
        newProject.setDescription(projectDescription);
        newProject.setProjectFile(file);
        newProject.setTasks(tasks);
        for (Task task : tasks) {
            List<Layer> layers = task.getLayerManager().getLayersNoInternals();
            for (Layer layer : layers) {
                String query;
                if (layer.getUltimateFeatureCollectionWrapper() != null) {
                    FeatureSchema schema = layer.getFeatureSchema();
                    HashMap<String, String> publicNames = new HashMap<String, String>();
                    HashMap<String, Boolean> visibilities = new HashMap<String, Boolean>();
                    HashMap<String, Map<Locale, String>> translations = new HashMap<String, Map<Locale, String>>();
                    int i = 0;
                    while (i < schema.getAttributeCount()) {
                        String name = schema.getAttributeName(i);
                        if (name != null) {
                            String publicName = schema.getPublicName(i);
                            Boolean visibility = schema.getVisibility(i);
                            publicNames.put(name, publicName);
                            visibilities.put(name, visibility);
                            translations.put(name, (Map<Locale, String>)((Object)new TranslationWrapper(schema.getTranslations(i))));
                        }
                        ++i;
                    }
                    layer.setAttributePublicNames(new HashMap<String, String>());
                    layer.setAttributeVisibility(visibilities);
                    layer.setAttributeTranslationsMap(translations);
                    layer.setVista(layer.getUltimateFeatureCollectionWrapper().getEnvelope());
                }
                if (!useRelativePath || (query = layer.getDataSourceQuery().getQuery()) == null || !query.startsWith(relativePath)) continue;
                int size = relativePath.length();
                query = query.substring(size, query.length());
                layer.getDataSourceQuery().setQuery(query);
            }
        }
        newProject.setTables(tables);
        newProject.setLayouts(layouts);
        try {
            Mapping mapping = LoadXMLMappings.loadProjectMappings();
            Properties properties = LocalConfiguration.getInstance().getProperties();
            properties.setProperty("org.exolab.castor.indent", "false");
            Marshaller marshaller = new Marshaller((Writer)stringWriter);
            marshaller.setMapping(mapping);
            marshaller.marshal((Object)newProject);
        }
        finally {
            stringWriter.flush();
        }
        FileUtil.setContents(file.getAbsolutePath(), stringWriter.toString());
        if (updateProjectTitle) {
            workbenchFrame.setCurrentProjectTitle(projectName);
        }
        JUMPWorkbench.getFrameInstance().getContext().setProject(newProject);
    }

    protected void saveLayerGroup(Collection<Category> categories, File file) throws Exception {
        StringWriter stringWriter = new StringWriter();
        CategoryCollection catCollection = new CategoryCollection();
        String catCollectionName = GUIUtil.nameWithoutExtension(file);
        catCollection.setName(catCollectionName);
        catCollection.setCategories(new ArrayList<Category>(categories));
        try {
            Mapping mapping = LoadXMLMappings.loadLayerGroupMappings();
            Properties properties = LocalConfiguration.getInstance().getProperties();
            properties.setProperty("org.exolab.castor.indent", "false");
            Marshaller marshaller = new Marshaller((Writer)stringWriter);
            marshaller.setMapping(mapping);
            marshaller.marshal((Object)catCollection);
        }
        finally {
            stringWriter.flush();
        }
        FileUtil.setContents(file.getAbsolutePath(), stringWriter.toString());
    }

    protected void saveSymbology(Collection<Style> jumpStyles, org.saig.core.styling.Style modelStyle, int geometryType, File file) throws Exception {
        StringWriter stringWriter = new StringWriter();
        LayerSimbology simbology = new LayerSimbology();
        String layerSimbologyName = GUIUtil.nameWithoutExtension(file);
        simbology.setName(layerSimbologyName);
        simbology.setJumpStyles(jumpStyles);
        simbology.setModelStyle(modelStyle);
        simbology.setGeometryType(geometryType);
        try {
            Mapping mapping = LoadXMLMappings.loadLayerSimbologyMappings();
            Properties properties = LocalConfiguration.getInstance().getProperties();
            properties.setProperty("org.exolab.castor.indent", "false");
            Marshaller marshaller = new Marshaller((Writer)stringWriter);
            marshaller.setMapping(mapping);
            marshaller.marshal((Object)simbology);
        }
        finally {
            stringWriter.flush();
        }
        FileUtil.setContents(file.getAbsolutePath(), stringWriter.toString());
    }
}


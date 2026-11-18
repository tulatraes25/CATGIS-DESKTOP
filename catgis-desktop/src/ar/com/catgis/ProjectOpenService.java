package ar.com.catgis;

import ar.com.catgis.core.model.Layer;
import ar.com.catgis.core.model.LayerGroup;
import ar.com.catgis.core.model.Project;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.util.Locale;

/**
 * Headless CATGIS project opening: validates and parses a {@code .catgis}
 * project without any Swing side effect, so a standalone module (CATMAP) can
 * inspect a project before it touches the live workspace.
 */
public final class ProjectOpenService {

    private ProjectOpenService() {}

    /** Parsed project plus its saved view; no AppContext or GUI wiring. */
    public record ProjectInspection(Project project, boolean viewLoaded,
                                    double viewMinX, double viewMinY,
                                    double viewZoomFactor) {
        public int layerCount() {
            return project.getLayers().size();
        }
    }

    /** Parse a project file; throws with a user-facing message on invalid data. */
    public static ProjectInspection inspect(File file) throws IOException {
        if (file == null || !file.isFile()
                || !file.getName().toLowerCase(Locale.ROOT).endsWith(".catgis")) {
            throw new IOException("Seleccione un archivo .catgis valido.");
        }
        try (BufferedReader reader = ProjectOpenReadBudget.openChecked(file)) {
            ProjectOpenReadBudget budget = new ProjectOpenReadBudget();
            String firstLine = reader.readLine();
            budget.checkLine(firstLine);
            if (firstLine == null || !firstLine.trim().equals("CATGIS_PROJECT")) {
                throw new IOException("Archivo de proyecto invalido.");
            }

            Project project = new Project(stripExtension(file.getName()));
            project.setProjectFile(file);

            boolean viewLoaded = false;
            double viewMinX = 0;
            double viewMinY = 0;
            double viewZoomFactor = 1.0;
            boolean hasProjectPayload = false;
            boolean encodedProjects = false;

            String line;
            while ((line = reader.readLine()) != null) {
                budget.checkLine(line);
                if (line.trim().isEmpty()) {
                    continue;
                }
                if (ProjectRecordCodec.isV2Header(line)) {
                    hasProjectPayload = true;
                    encodedProjects = true;
                } else if (line.startsWith("PROJECT_CRS|")) {
                    hasProjectPayload = true;
                    String[] crsParts = line.split("\\|", -1);
                    ProjectOpenRecordIntegrity.requireProjectCrs(crsParts);
                    project.setProjectCRS(encodedProjects
                            ? ProjectRecordCodec.unescape(crsParts[1]) : crsParts[1].trim());
                } else if (line.startsWith("PROJECT_META|")) {
                    hasProjectPayload = true;
                    String[] metaParts = line.split("\\|", -1);
                    ProjectOpenRecordIntegrity.requireMetadata(metaParts);
                    LoadProjectAction.applyProjectMetadata(project,
                            metaParts[1].trim(),
                            encodedProjects ? ProjectRecordCodec.unescape(metaParts[2])
                                    : metaParts[2].trim());
                } else if (line.startsWith("PROJECT_LAYOUT_ITEM|")) {
                    hasProjectPayload = true;
                    project.addCatmapItem(ProjectOpenRecordIntegrity.parseLayoutItem(line));
                } else if (line.startsWith("PROJECT_LEGEND_ITEM|")) {
                    hasProjectPayload = true;
                    project.addCatmapLegendItem(ProjectOpenRecordIntegrity.parseLegendItem(line));
                } else if (line.startsWith("PROJECT_LAYER_GROUP|")) {
                    hasProjectPayload = true;
                    String[] groupParts = line.split("\\|", -1);
                    ProjectOpenRecordIntegrity.requireGroup(groupParts);
                    LayerGroup group = new LayerGroup(encodedProjects
                            ? ProjectRecordCodec.unescape(groupParts[1])
                            : groupParts[1].trim());
                    if (groupParts.length >= 3) {
                        group.setVisible(Boolean.parseBoolean(groupParts[2].trim()));
                    }
                    if (groupParts.length >= 4) {
                        group.setExpanded(Boolean.parseBoolean(groupParts[3].trim()));
                    }
                    project.addLayerGroup(group);
                } else if (line.startsWith("VIEW|")) {
                    hasProjectPayload = true;
                    double[] view = ProjectOpenRecordIntegrity.parseView(line);
                    viewMinX = view[0];
                    viewMinY = view[1];
                    viewZoomFactor = view[2];
                    viewLoaded = true;
                } else {
                    Layer layer = LoadProjectAction.parseLayer(line, encodedProjects);
                    if (layer == null) {
                        ProjectOpenRecordIntegrity.rejectMalformedLayerRecord(line);
                        continue;
                    }
                    hasProjectPayload = true;
                    if (layer.isInGroup()
                            && project.getLayerGroup(layer.getGroupName()) == null) {
                        project.addLayerGroup(layer.getGroupName());
                    }
                    project.addLayer(layer);
                }
            }

            if (!hasProjectPayload) {
                throw new IOException("Archivo de proyecto incompleto.");
            }
            LoadProjectAction.normalizeCatserverGrouping(project);
            ProjectLocalSourcePreflight.requireExistingLocalSources(project.getLayers());
            return new ProjectInspection(project, viewLoaded, viewMinX, viewMinY, viewZoomFactor);
        } catch (IOException io) {
            throw io;
        } catch (Exception invalid) {
            throw new IOException("No se pudo abrir el proyecto: " + file.getName(), invalid);
        }
    }

    private static String stripExtension(String name) {
        int dot = name.lastIndexOf('.');
        return dot > 0 ? name.substring(0, dot) : name;
    }
}

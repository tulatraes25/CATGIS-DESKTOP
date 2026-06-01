package ar.com.catgis;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.io.File;

public class ProjectSerializationTest {

    @Test
    public void testSerializeDeserializeRoundtrip() throws Exception {
        Project original = new Project("Test Project");
        Layer l1 = new Layer("Capa Puntos", "", "VECTOR");
        l1.setPointColor(new java.awt.Color(255, 0, 0));
        l1.setLineWidth(1.5f);
        l1.setPointSize(10);
        l1.setVisible(true);
        l1.setPointSymbolStyle(Layer.PointSymbolStyle.STAR);
        original.addLayer(l1);

        Layer l2 = new Layer("Capa Lineas", "", "VECTOR");
        l2.setLineColor(new java.awt.Color(0, 128, 255));
        l2.setLineWidth(2f);
        original.addLayer(l2);

        File tmp = File.createTempFile("test-project", ".catgis");
        tmp.deleteOnExit();
        ProjectSerializer.serialize(original, tmp);

        ProjectDeserializer.Result result = ProjectDeserializer.deserialize(tmp);
        assertNotNull(result.project);
        assertEquals(2, result.project.getLayers().size());
        assertEquals("Capa Puntos", result.project.getLayers().get(0).getName());
        assertTrue(result.project.getLayers().get(0).isVisible());
        tmp.delete();
    }

    @Test
    public void testDeserializeEmptyFile() throws Exception {
        File tmp = File.createTempFile("empty", ".catgis");
        tmp.deleteOnExit();
        ProjectDeserializer.Result result = ProjectDeserializer.deserialize(tmp);
        assertNotNull(result.project);
        assertEquals(0, result.project.getLayers().size());
        tmp.delete();
    }

    @Test
    public void testWarningOnCorruptLine() throws Exception {
        File tmp = File.createTempFile("corrupt", ".catgis");
        tmp.deleteOnExit();
        java.nio.file.Files.write(tmp.toPath(), "LAYER|Test|.|.|0,0,0|0,0,0|0,0,0|1|5|true|1.0|CIRCLE|".getBytes());
        ProjectDeserializer.Result result = ProjectDeserializer.deserialize(tmp);
        assertNotNull(result.project);
        assertEquals(1, result.project.getLayers().size());
        tmp.delete();
    }
}

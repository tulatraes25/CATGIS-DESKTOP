package ar.com.catgis;

import org.junit.jupiter.api.Test;

import java.io.DataInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Observes the JVM actually executing tests and actual compiled class headers. */
class JavaRuntimeIdentityContractTest {

    @Test
    void actualTestJvmMatchesSelectedTarget() throws Exception {
        int expected = expectedFeature();
        assertEquals(expected, Runtime.version().feature());
        assertEquals(Integer.toString(expected), System.getProperty("java.specification.version"));
        String destination = System.getProperty("catgis.certification.evidenceDirectory");
        if (destination != null) {
            String arch = System.getProperty("os.arch");
            assertTrue(arch.matches("[A-Za-z0-9_]+"));
            Path directory = Path.of(destination);
            Files.createDirectories(directory);
            Files.writeString(directory.resolve("actual-test.json"),
                    "{\"role\":\"actual-test\",\"feature\":" + Runtime.version().feature()
                    + ",\"expected\":" + expected + ",\"arch\":\"" + arch + "\"}", StandardCharsets.UTF_8);
        }
    }

    @Test
    void productionClassVersionMatchesSelectedCompiler() throws Exception {
        assertClassVersion("/ar/com/catgis/Main.class");
    }

    @Test
    void testClassVersionMatchesSelectedCompiler() throws Exception {
        assertClassVersion("/ar/com/catgis/JavaRuntimeIdentityContractTest.class");
    }

    private static int expectedFeature() throws Exception {
        String explicit = System.getProperty("catgis.certification.expectedJava");
        if (explicit != null) {
            assertTrue(explicit.equals("17") || explicit.equals("21"));
            return Integer.parseInt(explicit);
        }
        Properties properties = new Properties();
        try (InputStream input = Files.newInputStream(Path.of("gradle.properties"))) {
            properties.load(input);
        }
        String declared = properties.getProperty("catgisJavaVersion");
        assertNotNull(declared, "Do not infer target JVM from JAVA_HOME or a workflow label.");
        return Integer.parseInt(declared.trim());
    }

    private static void assertClassVersion(String resource) throws Exception {
        InputStream resourceStream = JavaRuntimeIdentityContractTest.class.getResourceAsStream(resource);
        assertNotNull(resourceStream, "A real compiled class is required.");
        try (DataInputStream input = new DataInputStream(resourceStream)) {
            assertEquals(0xCAFEBABE, input.readInt());
            assertEquals(0, input.readUnsignedShort(), "No preview bytecode in a certified baseline.");
            assertEquals(44 + expectedFeature(), input.readUnsignedShort());
        }
    }
}

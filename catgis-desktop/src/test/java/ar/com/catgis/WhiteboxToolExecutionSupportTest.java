package ar.com.catgis;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WhiteboxToolExecutionSupportTest {

    @Test
    void sendsRequiredAndOptionalParametersAndKeepsFilePathsWithSpacesIntact() {
        WhiteboxToolRegistry.ToolInfo hillshade = tool("Hillshade");
        String[] command = WhiteboxToolExecutionSupport.buildCommand(
                "C:\\Program Files\\WhiteboxTools\\whitebox_tools.exe",
                hillshade,
                Map.of("input", "C:\\GIS Files\\mi dem.tif",
                       "output", "C:\\GIS Files\\sombra.tif",
                       "altitude", "45")
        );

        assertArrayEquals(new String[]{
                "C:\\Program Files\\WhiteboxTools\\whitebox_tools.exe",
                "--run=Hillshade",
                "--input=C:\\GIS Files\\mi dem.tif",
                "--output=C:\\GIS Files\\sombra.tif",
                "--altitude=45"
        }, command);
    }

    @Test
    void refusesMissingRequiredDataInsteadOfLaunchingAnIncompleteProcess() {
        IllegalArgumentException failure = assertThrows(IllegalArgumentException.class,
                () -> WhiteboxToolExecutionSupport.buildCommand(
                        "whitebox_tools", tool("Slope"), Map.of("input", "mi_dem.tif")));
        assertTrue(failure.getMessage().contains("Salida"));
    }

    @Test
    void optionalBlankArgumentsAreOmittedAndExecutableMustBePresent() {
        assertArrayEquals(new String[]{
                "/usr/local/bin/whitebox_tools",
                "--run=Hillshade",
                "--input=dem.tif",
                "--output=shade.tif"
        }, WhiteboxToolExecutionSupport.buildCommand(
                "/usr/local/bin/whitebox_tools",
                tool("Hillshade"),
                Map.of("input", "dem.tif", "output", "shade.tif", "altitude", "  ")));

        assertThrows(IllegalArgumentException.class,
                () -> WhiteboxToolExecutionSupport.buildCommand(
                        " ", tool("Slope"), Map.of("input", "a.tif", "output", "b.tif")));
    }

    @Test
    void refusesUnregisteredOrForgedToolDefinitions() {
        WhiteboxToolRegistry.ToolInfo unknown = new WhiteboxToolRegistry.ToolInfo(
                "ArbitraryCommand", "Unknown", "Fake", "unreviewed", java.util.List.of());
        assertThrows(IllegalArgumentException.class,
                () -> WhiteboxToolExecutionSupport.buildCommand(
                        "/usr/bin/whitebox_tools", unknown, Map.of()));

        WhiteboxToolRegistry.ToolInfo forged = new WhiteboxToolRegistry.ToolInfo(
                "Slope", "Fake slope", "Fake", "forged", java.util.List.of(
                        new WhiteboxToolRegistry.ParamInfo("evil", "Untrusted",
                                "TEXT", false, "")));
        assertThrows(IllegalArgumentException.class,
                () -> WhiteboxToolExecutionSupport.buildCommand(
                        "/usr/bin/whitebox_tools", forged, Map.of("evil", "yes")),
                "Only parameters from the canonical tool registry may be used.");
    }

    private static WhiteboxToolRegistry.ToolInfo tool(String id) {
        return WhiteboxToolRegistry.getTools().stream()
                .filter(info -> id.equals(info.id()))
                .findFirst()
                .orElseThrow();
    }
}

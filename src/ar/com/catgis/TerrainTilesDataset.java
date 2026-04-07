package ar.com.catgis;

public enum TerrainTilesDataset implements OnlineDemDatasetOption {
    TERRARIUM_GLOBAL(
            "Terrain Tiles / Terrarium global",
            "AWS Open Data / Terrain Tiles",
            "EPSG:3857",
            "terrain_tiles"
    );

    private final String displayName;
    private final String sourceLabel;
    private final String sourceCrsCode;
    private final String outputCode;

    TerrainTilesDataset(String displayName, String sourceLabel, String sourceCrsCode, String outputCode) {
        this.displayName = displayName;
        this.sourceLabel = sourceLabel;
        this.sourceCrsCode = sourceCrsCode;
        this.outputCode = outputCode;
    }

    @Override
    public String getDisplayName() {
        return displayName;
    }

    @Override
    public String getSourceLabel() {
        return sourceLabel;
    }

    @Override
    public String getSourceCrsCode() {
        return sourceCrsCode;
    }

    @Override
    public String getOutputCode() {
        return outputCode;
    }

    @Override
    public String getTechnicalSummary() {
        return "Tiles Terrarium publicos sobre AWS Open Data, sin API key.";
    }

    @Override
    public String toString() {
        return I18n.t(displayName);
    }
}

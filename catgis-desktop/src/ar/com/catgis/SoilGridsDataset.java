package ar.com.catgis;

public enum SoilGridsDataset implements OnlineSoilDatasetOption {
    CLAY_0_5_Q50(
            "Arcilla 0-5 cm (mediana)",
            "SoilGrids / ISRIC",
            "EPSG:4326",
            "clay",
            "clay_0-5cm_Q0.5",
            "clay_0_5cm_q50",
            "Fraccion de arcilla modelada para la capa superficial del suelo.",
            "Resolucion global nominal: 250 m.",
            "Util para preparar mapas preliminares de susceptibilidad a escorrentia, erosion o retencion superficial junto con pendiente."
    ),
    SAND_0_5_Q50(
            "Arena 0-5 cm (mediana)",
            "SoilGrids / ISRIC",
            "EPSG:4326",
            "sand",
            "sand_0-5cm_Q0.5",
            "sand_0_5cm_q50",
            "Fraccion de arena modelada para la capa superficial del suelo.",
            "Resolucion global nominal: 250 m.",
            "Util para aproximar infiltracion relativa y contrastar sectores mas permeables frente a zonas de pendiente alta."
    ),
    SILT_0_5_Q50(
            "Limo 0-5 cm (mediana)",
            "SoilGrids / ISRIC",
            "EPSG:4326",
            "silt",
            "silt_0-5cm_Q0.5",
            "silt_0_5cm_q50",
            "Fraccion de limo modelada para la capa superficial del suelo.",
            "Resolucion global nominal: 250 m.",
            "Sirve para lecturas territoriales preliminares y para combinar con relieve cuando se evalua riesgo superficial."
    ),
    SOC_0_5_Q50(
            "Carbono organico 0-5 cm (mediana)",
            "SoilGrids / ISRIC",
            "EPSG:4326",
            "soc",
            "soc_0-5cm_Q0.5",
            "soc_0_5cm_q50",
            "Contenido de carbono organico modelado para la capa superficial del suelo.",
            "Resolucion global nominal: 250 m.",
            "Util como insumo territorial preliminar, no reemplaza cartografia edafologica de detalle."
    );

    private final String displayName;
    private final String sourceLabel;
    private final String sourceCrsCode;
    private final String mapName;
    private final String coverageId;
    private final String outputCode;
    private final String technicalSummary;
    private final String resolutionSummary;
    private final String riskUseHint;

    SoilGridsDataset(String displayName,
                     String sourceLabel,
                     String sourceCrsCode,
                     String mapName,
                     String coverageId,
                     String outputCode,
                     String technicalSummary,
                     String resolutionSummary,
                     String riskUseHint) {
        this.displayName = displayName;
        this.sourceLabel = sourceLabel;
        this.sourceCrsCode = sourceCrsCode;
        this.mapName = mapName;
        this.coverageId = coverageId;
        this.outputCode = outputCode;
        this.technicalSummary = technicalSummary;
        this.resolutionSummary = resolutionSummary;
        this.riskUseHint = riskUseHint;
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

    public String getMapName() {
        return mapName;
    }

    public String getCoverageId() {
        return coverageId;
    }

    @Override
    public String getOutputCode() {
        return outputCode;
    }

    @Override
    public String getTechnicalSummary() {
        return technicalSummary;
    }

    @Override
    public String getResolutionSummary() {
        return resolutionSummary;
    }

    @Override
    public String getRiskUseHint() {
        return riskUseHint;
    }

    @Override
    public String toString() {
        return I18n.t(displayName);
    }
}

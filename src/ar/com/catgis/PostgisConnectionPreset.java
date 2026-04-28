package ar.com.catgis;

public final class PostgisConnectionPreset {

    private final String id;
    private final String dialogTitle;
    private final String title;
    private final String helpText;
    private final String initialStatus;
    private final String bannerResourcePath;
    private final PostgisConnectionInfo defaults;

    private PostgisConnectionPreset(String id,
                                    String dialogTitle,
                                    String title,
                                    String helpText,
                                    String initialStatus,
                                    String bannerResourcePath,
                                    PostgisConnectionInfo defaults) {
        this.id = id != null ? id.trim() : "";
        this.dialogTitle = dialogTitle != null ? dialogTitle.trim() : "";
        this.title = title != null ? title.trim() : "";
        this.helpText = helpText != null ? helpText.trim() : "";
        this.initialStatus = initialStatus != null ? initialStatus.trim() : "";
        this.bannerResourcePath = bannerResourcePath != null ? bannerResourcePath.trim() : "";
        this.defaults = defaults != null ? defaults.copy() : new PostgisConnectionInfo();
    }

    public static PostgisConnectionPreset catserver() {
        PostgisConnectionInfo info = new PostgisConnectionInfo();
        info.setHost("localhost");
        info.setPort(5432);
        info.setDatabase("catserver");
        info.setSchema("");
        info.setUser("");
        info.setRememberPassword(true);

        return new PostgisConnectionPreset(
                "catserver",
                "Conectar CATSERVER",
                "CATSERVER",
                "CATSERVER es la puerta de entrada de CATGIS para conectar servidores PostgreSQL/PostGIS. "
                        + "Podes editar host, puerto, base, schema y credenciales segun el servidor que quieras probar. "
                        + "Si el servidor publica un catalogo de capas, CATGIS lo prioriza; si no, lista las capas espaciales disponibles directamente desde la base.",
                "Revisa host, base, schema y credenciales. CATGIS recordara esta conexion por separado y puede reutilizarla para volver a listar o cargar capas.",
                "/help/assets/catserver-connect.png",
                info
        );
    }

    public String getId() {
        return id;
    }

    public String getDialogTitle() {
        return dialogTitle;
    }

    public String getTitle() {
        return title;
    }

    public String getHelpText() {
        return helpText;
    }

    public String getInitialStatus() {
        return initialStatus;
    }

    public String getBannerResourcePath() {
        return bannerResourcePath;
    }

    public PostgisConnectionInfo getDefaults() {
        return defaults.copy();
    }

    public boolean hasBanner() {
        return !bannerResourcePath.isBlank();
    }

    public boolean hasProfileId() {
        return !id.isBlank();
    }
}

package ar.com.catgis;
import ar.com.catgis.core.model.Layer;

public class OnlineWfsLayer extends Layer implements ReadOnlyVectorLayerSource {

    private String providerName = "";
    private String serviceUrl = "";
    private String typeName = "";
    private String typeTitle = "";
    private String requestCrs = "";
    private String version = "2.0.0";
    private boolean readOnly = true;

    public OnlineWfsLayer(String name) {
        super(name, "", "ONLINE_WFS");
    }

    public String getProviderName() {
        return providerName;
    }

    public void setProviderName(String providerName) {
        this.providerName = providerName != null ? providerName : "";
    }

    public String getServiceUrl() {
        return serviceUrl;
    }

    public void setServiceUrl(String serviceUrl) {
        this.serviceUrl = serviceUrl != null ? serviceUrl : "";
        setPath(this.serviceUrl);
    }

    public String getTypeName() {
        return typeName;
    }

    public void setTypeName(String typeName) {
        this.typeName = typeName != null ? typeName : "";
    }

    public String getTypeTitle() {
        return typeTitle;
    }

    public void setTypeTitle(String typeTitle) {
        this.typeTitle = typeTitle != null ? typeTitle : "";
    }

    public String getRequestCrs() {
        return requestCrs;
    }

    public void setRequestCrs(String requestCrs) {
        this.requestCrs = CRSDefinitions.normalizeCode(requestCrs);
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version != null && !version.isBlank() ? version : "2.0.0";
    }

    public boolean isReadOnly() {
        return readOnly;
    }

    public void setReadOnly(boolean readOnly) {
        this.readOnly = readOnly;
    }

    @Override
    public String getReadOnlyReason() {
        return "La capa WFS seleccionada esta en modo lectura. Podes consultarla, verla en tabla y exportarla, pero no editarla.";
    }
}

package ar.com.catgis;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public final class PointSymbolCatalog {

    public static final String CATALOG_PREFIX = "catalog:";

    public static final Entry NONE = new Entry("", I18n.t("(Sin icono de catalogo)"), I18n.t("Catalogo"), "");

    private static final List<Entry> ENTRIES = List.of(
            entry("oil-well", "Pozo petrolero", "Petroleo y gas", "petroleo-gas/pozo-petrolero.svg"),
            entry("battery", "Bateria petrolera", "Petroleo y gas", "petroleo-gas/bateria-petrolera.svg"),
            entry("tank", "Tanque", "Petroleo y gas", "petroleo-gas/tanque.svg"),
            entry("fuel-station-tabler", "Surtidor / estacion de gas", "Petroleo y gas", "petroleo-gas/surtidor-tabler.svg"),
            entry("gas-station-tabler", "Estacion de gas", "Petroleo y gas", "petroleo-gas/estacion-gas-tabler.svg"),
            entry("tank-tabler", "Tanque tabler", "Petroleo y gas", "petroleo-gas/tanque-tabler.svg"),

            entry("windmill-tabler", "Molino eolico", "Energia", "energia/molino-eolico-tabler.svg"),
            entry("wind-farm-tabler", "Parque eolico", "Energia", "energia/parque-eolico-tabler.svg"),
            entry("wind-energy-tabler", "Energia eolica", "Energia", "energia/energia-eolica-tabler.svg"),
            entry("solar-panel-tabler", "Panel solar", "Energia", "energia/panel-solar-tabler.svg"),
            entry("solar-field-tabler", "Campo solar", "Energia", "energia/campo-solar-tabler.svg"),
            entry("solar-energy-tabler", "Energia solar", "Energia", "energia/energia-solar-tabler.svg"),
            entry("sun-energy-tabler", "Sol / energia", "Energia", "energia/sol-energia-tabler.svg"),
            entry("battery-energy-tabler", "Bateria de energia", "Energia", "energia/bateria-energia-tabler.svg"),
            entry("plug-energy-tabler", "Conexion electrica", "Energia", "energia/conexion-energia-tabler.svg"),
            entry("charging-station-tabler", "Estacion de carga", "Energia", "energia/estacion-carga-tabler.svg"),

            entry("car-tabler", "Auto", "Transporte", "transporte/auto-tabler.svg"),
            entry("pickup-tabler", "Camioneta", "Transporte", "transporte/camioneta-tabler.svg"),
            entry("truck-tabler", "Camion", "Transporte", "transporte/camion-tabler.svg"),
            entry("bus-tabler", "Bus", "Transporte", "transporte/bus-tabler.svg"),
            entry("bus-stop-tabler", "Parada de bus", "Transporte", "transporte/parada-bus-tabler.svg"),
            entry("tractor-tabler", "Tractor", "Transporte", "transporte/tractor-tabler.svg"),
            entry("utility-vehicle-tabler", "Vehiculo utilitario", "Transporte", "transporte/vehiculo-utilitario-tabler.svg"),

            entry("plant", "Planta industrial", "Industria", "infraestructura/planta-industrial.svg"),
            entry("factory-tabler", "Fabrica", "Industria", "infraestructura/fabrica-tabler.svg"),
            entry("factory-1-tabler", "Fabrica 1", "Industria", "industria/fabrica-1-tabler.svg"),
            entry("factory-2-tabler", "Fabrica 2", "Industria", "industria/fabrica-2-tabler.svg"),
            entry("warehouse-tabler", "Deposito / nave", "Industria", "infraestructura/deposito-tabler.svg"),
            entry("warehouse-2-tabler", "Almacen", "Industria", "industria/almacen-tabler.svg"),
            entry("industrial-facility-tabler", "Instalacion industrial", "Industria", "industria/instalacion-industrial-tabler.svg"),
            entry("industrial-pavilion-tabler", "Pabellon industrial", "Industria", "industria/pabellon-industrial-tabler.svg"),

            entry("tower-tabler", "Torre", "Infraestructura", "infraestructura/torre-tabler.svg"),
            entry("antenna-tabler", "Antena", "Infraestructura", "infraestructura/antena-tabler.svg"),
            entry("antenna-signal-tabler", "Antena de senal", "Infraestructura", "infraestructura/antena-senal-tabler.svg"),
            entry("broadcast-tower-tabler", "Torre broadcast", "Infraestructura", "infraestructura/torre-broadcast-tabler.svg"),
            entry("bridge-tabler", "Puente", "Infraestructura", "infraestructura/puente-tabler.svg"),
            entry("service-station-tabler", "Estacion / comercio", "Infraestructura", "infraestructura/estacion-servicio-tabler.svg"),
            entry("community-facility-tabler", "Instalacion comunitaria", "Infraestructura", "infraestructura/instalacion-comunitaria-tabler.svg"),

            entry("home-tabler", "Casa", "Edificaciones", "edificaciones/casa-tabler.svg"),
            entry("building-tabler", "Edificio", "Edificaciones", "edificaciones/edificio-tabler.svg"),
            entry("buildings-tabler", "Complejo de edificios", "Edificaciones", "edificaciones/complejo-edificios-tabler.svg"),
            entry("cottage-tabler", "Cabania", "Edificaciones", "edificaciones/cabania-tabler.svg"),
            entry("skyscraper-tabler", "Torre / rascacielos", "Edificaciones", "edificaciones/torre-edificio-tabler.svg"),

            entry("hospital-building-tabler", "Edificio hospital", "Salud y educacion", "salud-educacion/edificio-hospital-tabler.svg"),
            entry("hospital-tabler", "Hospital", "Salud y educacion", "salud-educacion/hospital-tabler.svg"),
            entry("school-tabler", "Escuela", "Salud y educacion", "salud-educacion/escuela-tabler.svg"),
            entry("school-bell-tabler", "Campana escolar", "Salud y educacion", "salud-educacion/campana-escuela-tabler.svg"),

            entry("phone-tabler", "Telefono", "Servicios", "servicios/telefono-tabler.svg"),
            entry("wifi-tabler", "Wifi", "Servicios", "servicios/wifi-tabler.svg"),
            entry("hydrant-tabler", "Hidrante", "Servicios", "servicios/hidrante-tabler.svg"),
            entry("firetruck-tabler", "Camion de bomberos", "Servicios", "servicios/camion-bomberos-tabler.svg"),
            entry("ambulance-tabler", "Ambulancia", "Servicios", "servicios/ambulancia-tabler.svg"),

            entry("water", "Agua", "Agua", "agua/agua.svg"),
            entry("droplet-tabler", "Gota de agua", "Agua", "agua/gota-tabler.svg"),
            entry("droplets-tabler", "Gotas de agua", "Agua", "agua/gotas-tabler.svg"),
            entry("bucket-water-tabler", "Balde / agua", "Agua", "agua/balde-agua-tabler.svg"),

            entry("flora", "Planta / flora", "Flora", "flora/planta.svg"),
            entry("tree-tabler", "Arbol", "Flora", "flora/arbol-tabler.svg"),
            entry("leaf-tabler", "Hoja", "Flora", "flora/hoja-tabler.svg"),
            entry("tree-2-tabler", "Arbol 2", "Flora", "flora/arbol-2-tabler.svg"),
            entry("forest-tabler", "Bosque", "Flora", "flora/bosque-tabler.svg"),
            entry("plant-2-tabler", "Planta 2", "Flora", "flora/planta-2-tabler.svg"),

            entry("fauna", "Fauna / animal", "Fauna", "fauna/animal.svg"),
            entry("fish-tabler", "Pez", "Fauna", "fauna/pez-tabler.svg"),
            entry("paw-tabler", "Huella animal", "Fauna", "fauna/huella-tabler.svg"),
            entry("dog-tabler", "Perro", "Fauna", "fauna/perro-tabler.svg"),
            entry("pig-tabler", "Cerdo", "Fauna", "fauna/cerdo-tabler.svg"),

            entry("field-route-tabler", "Ruta de relevamiento", "Campo y relevamiento", "campo-relevamiento/ruta-relevamiento-tabler.svg"),
            entry("satellite-tabler", "Satelite", "Campo y relevamiento", "campo-relevamiento/satelite-tabler.svg"),
            entry("camera-tabler", "Camara", "Campo y relevamiento", "campo-relevamiento/camara-tabler.svg"),
            entry("radio-tabler", "Radio", "Campo y relevamiento", "campo-relevamiento/radio-tabler.svg"),
            entry("field-pin-tabler", "Pin de campo", "Campo y relevamiento", "campo-relevamiento/pin-campo-tabler.svg"),

            entry("reference", "Punto de referencia", "Cartografia general", "cartografia/punto-referencia.svg"),
            entry("map-pin-tabler", "Pin de mapa", "Cartografia general", "cartografia/pin-tabler.svg"),
            entry("map-tabler", "Mapa", "Cartografia general", "cartografia/mapa-tabler.svg"),
            entry("world-map-tabler", "Mapa mundial", "Cartografia general", "cartografia/mapa-mundial-tabler.svg"),
            entry("map-search-tabler", "Busqueda en mapa", "Cartografia general", "cartografia/busqueda-mapa-tabler.svg")
    );

    private PointSymbolCatalog() {
    }

    public static List<Entry> entries() {
        return ENTRIES;
    }

    public static List<String> categories() {
        Set<String> categories = new LinkedHashSet<>();
        for (Entry entry : ENTRIES) {
            if (entry.category != null && !entry.category.isBlank()) {
                categories.add(entry.category);
            }
        }
        return new ArrayList<>(categories);
    }

    public static List<Entry> entriesForCategory(String category) {
        if (category == null || category.isBlank()) {
            return ENTRIES;
        }
        List<Entry> filtered = new ArrayList<>();
        for (Entry entry : ENTRIES) {
            if (category.equalsIgnoreCase(entry.category)) {
                filtered.add(entry);
            }
        }
        return filtered;
    }

    public static Entry findByReference(String reference) {
        if (reference == null || reference.isBlank()) {
            return null;
        }
        for (Entry entry : ENTRIES) {
            if (entry.reference.equalsIgnoreCase(reference.trim())) {
                return entry;
            }
        }
        return null;
    }

    public static String resolveResourcePath(String reference) {
        if (!isCatalogReference(reference)) {
            return null;
        }
        return "/icons/symbol-catalog/" + reference.substring(CATALOG_PREFIX.length());
    }

    public static boolean isCatalogReference(String reference) {
        return reference != null && reference.startsWith(CATALOG_PREFIX);
    }

    private static String catalogReference(String relativePath) {
        return CATALOG_PREFIX + relativePath;
    }

    private static Entry entry(String id, String label, String category, String relativePath) {
        return new Entry(id, I18n.t(label), I18n.t(category), catalogReference(relativePath));
    }

    public static final class Entry {
        private final String id;
        private final String label;
        private final String category;
        private final String reference;

        private Entry(String id, String label, String category, String reference) {
            this.id = id;
            this.label = label;
            this.category = category;
            this.reference = reference;
        }

        public String getId() {
            return id;
        }

        public String getLabel() {
            return label;
        }

        public String getCategory() {
            return category;
        }

        public String getReference() {
            return reference;
        }

        @Override
        public String toString() {
            if (reference == null || reference.isBlank()) {
                return label;
            }
            return category + " | " + label;
        }
    }
}

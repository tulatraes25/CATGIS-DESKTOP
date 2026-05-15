package ar.com.catgis;

import java.net.ConnectException;
import java.net.NoRouteToHostException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.Locale;
import java.util.concurrent.ExecutionException;

public final class PostgisErrorSupport {

    private PostgisErrorSupport() {
    }

    public static String validateConnectionInfo(PostgisConnectionInfo info, boolean requirePassword) {
        if (info == null) {
            return "No se pudo construir la conexion PostGIS.";
        }
        if (info.getHost() == null || info.getHost().isBlank()) {
            return "Completa el host o IP del servidor PostGIS.";
        }
        if (info.getPort() < 1 || info.getPort() > 65535) {
            return "El puerto PostGIS debe estar entre 1 y 65535.";
        }
        if (info.getDatabase() == null || info.getDatabase().isBlank()) {
            return "Completa el nombre de la base PostgreSQL.";
        }
        if (info.getUser() == null || info.getUser().isBlank()) {
            return "Completa el usuario PostgreSQL/PostGIS.";
        }
        if (requirePassword && (info.getPassword() == null || info.getPassword().isBlank())) {
            return "Ingresa la clave del usuario PostGIS.";
        }
        return "";
    }

    public static String toUserMessage(Throwable error, PostgisConnectionInfo info) {
        Throwable root = rootCause(error);
        String raw = safeMessage(root);
        String lower = raw.toLowerCase(Locale.ROOT);
        String target = info != null ? info.buildDisplayLabel() : "la conexion PostGIS";

        if (root instanceof UnknownHostException || lower.contains("unknown host")) {
            return "No se pudo resolver el host de " + target + ". Revisa el nombre o la IP del servidor.";
        }
        if (root instanceof NoRouteToHostException || lower.contains("no route to host")) {
            return "No hay ruta de red hacia " + target + ". Revisa conectividad, VPN o firewall.";
        }
        if (root instanceof SocketTimeoutException || lower.contains("timeout")) {
            return "La conexion PostGIS agoto el tiempo de espera. Revisa red, puerto o respuesta del servidor.";
        }
        if (root instanceof ConnectException || lower.contains("connection refused")) {
            return "El servidor rechazo la conexion. Revisa host, puerto y que PostgreSQL este levantado.";
        }
        if (lower.contains("password authentication failed") || lower.contains("authentication failed")) {
            return "Usuario o clave PostGIS incorrectos. Revisa las credenciales.";
        }
        if (lower.contains("permission denied")) {
            return "El usuario PostgreSQL/PostGIS no tiene permisos suficientes para completar la operación solicitada.";
        }
        if (lower.contains("must be owner of relation")) {
            return "El usuario actual no es dueño de la tabla y no puede reemplazarla o modificar su estructura.";
        }
        if (lower.contains("database") && lower.contains("does not exist")) {
            return "La base PostgreSQL indicada no existe en el servidor.";
        }
        if (lower.contains("schema") && lower.contains("does not exist")) {
            return "El schema indicado no existe en la base PostgreSQL.";
        }
        if (lower.contains("already exists")) {
            return "La tabla destino ya existe en PostGIS. Elegi crear otra tabla o reemplazarla.";
        }
        if (lower.contains("duplicate key value")) {
            return "La operación PostGIS encontró claves duplicadas. Revisá ids, anexado o restricciones de la tabla.";
        }
        if (lower.contains("violates not-null constraint")) {
            return "La tabla PostGIS exige campos obligatorios que la capa actual no está completando.";
        }
        if (lower.contains("role") && lower.contains("does not exist")) {
            return "El usuario PostgreSQL indicado no existe en el servidor.";
        }
        if (lower.contains("no pg_hba.conf")) {
            return "El servidor PostgreSQL rechazo el acceso por configuracion pg_hba.conf.";
        }
        if (lower.contains("connection is closed")) {
            return "La conexion PostGIS se cerro antes de terminar la operacion.";
        }
        if (raw.isBlank()) {
            return "No se pudo completar la operacion PostGIS por un error no identificado.";
        }
        return "No se pudo completar la operacion PostGIS. Detalle tecnico: " + raw;
    }

    private static Throwable rootCause(Throwable error) {
        Throwable current = error;
        while (current instanceof ExecutionException && current.getCause() != null) {
            current = current.getCause();
        }
        while (current != null && current.getCause() != null && current.getCause() != current) {
            current = current.getCause();
        }
        return current != null ? current : error;
    }

    private static String safeMessage(Throwable error) {
        if (error == null) {
            return "";
        }
        String message = error.getMessage();
        if (message != null && !message.isBlank()) {
            return message.trim();
        }
        return error.getClass().getSimpleName();
    }
}

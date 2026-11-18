package ar.com.catgis;

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

/**
 * T4/#156: transactional swap for PostGIS "replace table".
 *
 * <p>The new dataset is fully written to a staging table first. Only then, inside
 * one SQL transaction, the old table is dropped and the staging table renamed to
 * the target name and verified. A failure rolls back, preserving the previous
 * table; the staging table is dropped afterwards. The last valid user table is
 * never destroyed before the replacement exists.</p>
 */
public final class PostgisReplaceSwap {

    public static final String STAGE_MARKER = "_catgis_stage_";
    private static final int MAX_IDENTIFIER = 63;

    private PostgisReplaceSwap() {}

    public static String stageTableName(String targetTable) {
        requireIdentifier(targetTable);
        String token = UUID.randomUUID().toString().replace("-", "").substring(0, 8);
        int maxBase = MAX_IDENTIFIER - STAGE_MARKER.length() - token.length();
        String base = targetTable.length() > maxBase
                ? targetTable.substring(0, maxBase) : targetTable;
        return base + STAGE_MARKER + token;
    }

    /** Ordered statements executed inside the swap transaction. */
    public static List<String> plan(String schemaName, String targetTable, String stageTable) {
        requireIdentifier(schemaName);
        requireIdentifier(targetTable);
        requireIdentifier(stageTable);
        List<String> statements = new ArrayList<>();
        statements.add("DROP TABLE IF EXISTS " + qualified(schemaName, targetTable));
        statements.add("ALTER TABLE " + qualified(schemaName, stageTable)
                + " RENAME TO " + quote(targetTable));
        statements.add("SELECT count(*) FROM " + qualified(schemaName, targetTable));
        return statements;
    }

    /**
     * Executes the swap atomically. {@code expectedRows} must match the staging
     * table before replacing and the target table after the rename; otherwise the
     * transaction is rolled back and the previous table survives.
     */
    public static void swap(PostgisConnectionInfo info, String schemaName, String targetTable,
                     String stageTable, long expectedRows) throws Exception {
        List<String> statements = plan(schemaName, targetTable, stageTable);
        try (Connection connection = PostgisLoader.openJdbcConnection(info)) {
            boolean previousAutoCommit = connection.getAutoCommit();
            connection.setAutoCommit(false);
            try (Statement statement = connection.createStatement()) {
                long staged = count(statement, qualified(schemaName, stageTable));
                if (staged != expectedRows) {
                    throw new IOException("La tabla temporal PostGIS tiene " + staged
                            + " filas y se esperaban " + expectedRows
                            + "; se conserva la tabla anterior.");
                }
                statement.execute(statements.get(0));
                statement.execute(statements.get(1));
                long published = count(statement, qualified(schemaName, targetTable));
                if (published != expectedRows) {
                    throw new IOException("La tabla PostGIS publicada tiene " + published
                            + " filas y se esperaban " + expectedRows
                            + "; se revierte el reemplazo.");
                }
                connection.commit();
            } catch (Exception failure) {
                try {
                    connection.rollback();
                } catch (Exception rollbackFailure) {
                    failure.addSuppressed(rollbackFailure);
                }
                throw failure;
            } finally {
                connection.setAutoCommit(previousAutoCommit);
            }
        }
    }

    /** Best-effort cleanup of an orphaned staging table after a failed replace. */
    public static void dropIfExists(PostgisConnectionInfo info, String schemaName, String stageTable) {
        if (info == null || stageTable == null || stageTable.isBlank()) {
            return;
        }
        try (Connection connection = PostgisLoader.openJdbcConnection(info);
             Statement statement = connection.createStatement()) {
            statement.execute("DROP TABLE IF EXISTS " + qualified(schemaName, stageTable));
        } catch (Exception cleanupFailure) {
            CatgisLogger.warn("No se pudo eliminar la tabla temporal PostGIS "
                    + schemaName + "." + stageTable, cleanupFailure);
        }
    }

    private static long count(Statement statement, String qualifiedTable) throws Exception {
        try (ResultSet result = statement.executeQuery("SELECT count(*) FROM " + qualifiedTable)) {
            return result.next() ? result.getLong(1) : -1L;
        }
    }

    private static String qualified(String schemaName, String tableName) {
        return quote(schemaName) + "." + quote(tableName);
    }

    private static String quote(String identifier) {
        requireIdentifier(identifier);
        return "\"" + identifier + "\"";
    }

    static void requireIdentifier(String identifier) {
        if (identifier == null || !identifier.matches("^[A-Za-z_][A-Za-z0-9_]{0,62}$")) {
            throw new IllegalArgumentException("Identificador PostGIS invalido: " + identifier);
        }
    }

    /** Internal schema name may be uppercase in tests; PostgreSQL folds it. */
    static String normalize(String identifier) {
        return identifier == null ? "" : identifier.toLowerCase(Locale.ROOT);
    }
}

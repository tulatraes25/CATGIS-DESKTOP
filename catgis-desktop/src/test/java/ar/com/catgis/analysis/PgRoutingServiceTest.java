package ar.com.catgis.analysis;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PgRoutingServiceTest {

    @Test
    void isValidTableNameAcceptsNormal() {
        assertTrue(PgRoutingService.isValidTableName("edges"));
        assertTrue(PgRoutingService.isValidTableName("public.roads"));
        assertTrue(PgRoutingService.isValidTableName("schema_01.my_edges"));
        assertTrue(PgRoutingService.isValidTableName("MyTable"));
    }

    @Test
    void isValidTableNameRejectsNull() {
        assertFalse(PgRoutingService.isValidTableName(null));
    }

    @Test
    void isValidTableNameRejectsSqlInjection() {
        assertFalse(PgRoutingService.isValidTableName("edges; DROP TABLE users;--"));
        assertFalse(PgRoutingService.isValidTableName("edges' OR '1'='1"));
        assertFalse(PgRoutingService.isValidTableName("edges--"));
        assertFalse(PgRoutingService.isValidTableName("edges; SELECT 1"));
    }

    @Test
    void isValidTableNameRejectsSpecialChars() {
        assertFalse(PgRoutingService.isValidTableName(""));
        assertFalse(PgRoutingService.isValidTableName(" "));
        assertFalse(PgRoutingService.isValidTableName("edges with spaces"));
        assertFalse(PgRoutingService.isValidTableName("edges@table"));
    }

    @Test
    void isAvailableReturnsFalseForInvalidConnection() {
        assertFalse(PgRoutingService.isAvailable(
                "jdbc:postgresql://localhost:5432/nonexistent",
                "test", "test"));
    }
}

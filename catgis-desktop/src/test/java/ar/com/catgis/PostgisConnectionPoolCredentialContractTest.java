package ar.com.catgis;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class PostgisConnectionPoolCredentialContractTest {

    @Test
    void stablePoolIdentityForSameCredentialsWithoutPlaintext() {
        PostgisConnectionInfo info = base("unique-test-secret-9483");
        String first = PostgisConnectionFactory.poolKey(info);
        String second = PostgisConnectionFactory.poolKey(info.copy());

        assertEquals(first, second);
        assertFalse(first.contains(info.getPassword()));
        assertFalse(first.contains(info.getHost()));
        assertEquals(64, first.length());
    }

    @Test
    void changingOnlyPasswordMustNotReusePoolIdentity() {
        PostgisConnectionInfo first = base("password-before");
        PostgisConnectionInfo after = first.copy();
        after.setPassword("password-after");

        assertEquals(first.buildFingerprint(), after.buildFingerprint(),
                "Noncredential endpoint fingerprint should be unchanged.");
        assertNotEquals(PostgisConnectionFactory.poolKey(first),
                PostgisConnectionFactory.poolKey(after),
                "A password rotation must never select the old authenticated pool.");
    }

    @Test
    void differentDatabaseOrUserMustNotReusePoolIdentity() {
        PostgisConnectionInfo first = base("same-secret");
        PostgisConnectionInfo differentUser = first.copy();
        differentUser.setUser("other_user");
        PostgisConnectionInfo differentDb = first.copy();
        differentDb.setDatabase("other_database");

        assertNotEquals(PostgisConnectionFactory.poolKey(first),
                PostgisConnectionFactory.poolKey(differentUser));
        assertNotEquals(PostgisConnectionFactory.poolKey(first),
                PostgisConnectionFactory.poolKey(differentDb));
    }

    private static PostgisConnectionInfo base(String password) {
        PostgisConnectionInfo info = new PostgisConnectionInfo();
        info.setHost("localhost");
        info.setPort(5432);
        info.setDatabase("catgis_ci");
        info.setSchema("public");
        info.setUser("catgis_ci");
        info.setPassword(password);
        return info;
    }
}

package fpt.qn.mes;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.UUID;

import javax.crypto.spec.SecretKeySpec;

import org.jooq.DSLContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Transactional
@Rollback
@TestPropertySource(properties = {
    "app.jwt.secret=test-secret-key-min-32-bytes-long-for-hs256!!",
    "app.jwt.access-token-expiration=900000",
    "app.cors.allowed-origins=http://localhost:4200"
})
public abstract class AbstractIntegrationTest {

    @ServiceConnection
    static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:18-alpine")
            .withReuse(false);

    static {
        postgres.start();
    }

    private static final byte[] SECRET_BYTES = "test-secret-key-min-32-bytes-long-for-hs256!!"
            .getBytes(StandardCharsets.UTF_8);

    private static final JWSSigner signer;

    static {
        try {
            signer = new MACSigner(new SecretKeySpec(SECRET_BYTES, "HmacSHA256"));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Autowired
    private DSLContext dslCtx;

    /**
     * Seed admin user for integration tests.
     * Called from subclass @BeforeEach to ensure data exists within each transaction.
     */
    protected void seedAdminUser() {
        var userExists = dslCtx.fetchExists(
                dslCtx.selectFrom(fpt.qn.mes.jooq.tables.Users.USERS)
                        .where(fpt.qn.mes.jooq.tables.Users.USERS.USERNAME.eq("admin"))
        );
        if (!userExists) {
            dslCtx.insertInto(fpt.qn.mes.jooq.tables.Users.USERS)
                    .columns(fpt.qn.mes.jooq.tables.Users.USERS.ID,
                            fpt.qn.mes.jooq.tables.Users.USERS.USERNAME,
                            fpt.qn.mes.jooq.tables.Users.USERS.PASSWORD_HASH,
                            fpt.qn.mes.jooq.tables.Users.USERS.ACTIVE)
                    .values(UUID.fromString("00000000-0000-0000-0000-000000000001"),
                            "admin", "placeholder", true)
                    .execute();

            dslCtx.insertInto(fpt.qn.mes.jooq.tables.Roles.ROLES)
                    .columns(fpt.qn.mes.jooq.tables.Roles.ROLES.ID,
                            fpt.qn.mes.jooq.tables.Roles.ROLES.NAME)
                    .values(UUID.fromString("00000000-0000-0000-0000-000000000002"), "ADMIN")
                    .execute();

            dslCtx.insertInto(fpt.qn.mes.jooq.tables.UserRoles.USER_ROLES)
                    .columns(fpt.qn.mes.jooq.tables.UserRoles.USER_ROLES.USER_ID,
                            fpt.qn.mes.jooq.tables.UserRoles.USER_ROLES.ROLE_ID)
                    .values(UUID.fromString("00000000-0000-0000-0000-000000000001"),
                            UUID.fromString("00000000-0000-0000-0000-000000000002"))
                    .execute();
        }
    }

    protected String generateToken(String username, String role) {
        try {
            JWTClaimsSet claims = new JWTClaimsSet.Builder()
                    .subject(username)
                    .issueTime(new Date())
                    .expirationTime(new Date(System.currentTimeMillis() + 3600_000))
                    .claim("role", role)
                    .build();
            SignedJWT signedJWT = new SignedJWT(
                    new JWSHeader(JWSAlgorithm.HS256), claims);
            signedJWT.sign(signer);
            return signedJWT.serialize();
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate JWT", e);
        }
    }

    protected String generateToken(UUID userId, String role) {
        return generateToken(userId.toString(), role);
    }

    protected String generateAdminToken() {
        return generateToken("admin", "ADMIN");
    }
}

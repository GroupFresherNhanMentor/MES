package fpt.qn.mes;

import static fpt.qn.mes.jooq.Tables.ROLES;
import static fpt.qn.mes.jooq.Tables.USERS;
import static fpt.qn.mes.jooq.Tables.USER_ROLES;

import java.time.Instant;
import java.util.UUID;

import org.jooq.DSLContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestRestTemplate
@ActiveProfiles("test")
public abstract class AbstractIntegrationTest {

    @ServiceConnection
    static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:18-alpine")
            .withReuse(false);

    @ServiceConnection
    static final GenericContainer<?> redis = new GenericContainer<>("redis:7-alpine")
            .withExposedPorts(6379)
            .withReuse(false);

    static {
        postgres.start();
        redis.start();
    }

    @Autowired
    JwtEncoder jwtEncoder;

    @Autowired
    DSLContext dslCtx;

    protected void seedAdminUser() {
        UUID roleId = dslCtx.select(ROLES.ID)
                .from(ROLES)
                .where(ROLES.NAME.eq("ADMIN"))
                .fetchOne(ROLES.ID);
        if (roleId == null) {
            roleId = UUID.randomUUID();
            dslCtx.insertInto(ROLES, ROLES.ID, ROLES.NAME, ROLES.DESCRIPTION)
                    .values(roleId, "ADMIN", "System administrator")
                    .execute();
        }

        UUID userId = dslCtx.select(USERS.ID)
                .from(USERS)
                .where(USERS.USERNAME.eq("admin"))
                .fetchOne(USERS.ID);
        if (userId == null) {
            userId = UUID.randomUUID();
            dslCtx.insertInto(USERS, USERS.ID, USERS.USERNAME, USERS.PASSWORD_HASH, USERS.ACTIVE)
                    .values(userId, "admin", "placeholder", true)
                    .execute();
        }

        dslCtx.insertInto(USER_ROLES, USER_ROLES.USER_ID, USER_ROLES.ROLE_ID)
                .values(userId, roleId)
                .onConflictDoNothing()
                .execute();
    }

    protected String generateToken(String username, String role) {
        UUID userId = dslCtx.select(USERS.ID)
                .from(USERS)
                .where(USERS.USERNAME.eq(username))
                .fetchOne(USERS.ID);
        if (userId == null && "admin".equals(username)) {
            seedAdminUser();
            userId = dslCtx.select(USERS.ID)
                    .from(USERS)
                    .where(USERS.USERNAME.eq(username))
                    .fetchOne(USERS.ID);
        }
        if (userId == null) {
            throw new IllegalStateException("Test user not found: " + username);
        }
        return generateToken(userId, username);
    }

    protected String generateToken(UUID userId, String username) {
        Instant now = Instant.now();
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .subject(userId.toString())
                .issuedAt(now)
                .expiresAt(now.plusSeconds(900))
                .id(UUID.randomUUID().toString())
                .claim("username", username)
                .claim("token_type", "access")
                .issuer("factoryflow-test")
                .audience(java.util.List.of("factoryflow-api-test"))
                .build();
        return jwtEncoder.encode(JwtEncoderParameters.from(
                JwsHeader.with(MacAlgorithm.HS256).build(), claims)).getTokenValue();
    }

    protected String generateExpiredToken(UUID userId, String username) {
        Instant now = Instant.now();
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .subject(userId.toString())
                .issuedAt(now.minusSeconds(300))
                .expiresAt(now.minusSeconds(120))
                .id(UUID.randomUUID().toString())
                .claim("username", username)
                .claim("token_type", "access")
                .issuer("factoryflow-test")
                .audience(java.util.List.of("factoryflow-api-test"))
                .build();
        return jwtEncoder.encode(JwtEncoderParameters.from(
                JwsHeader.with(MacAlgorithm.HS256).build(), claims)).getTokenValue();
    }

    protected String generateAdminToken() {
        seedAdminUser();
        return generateToken("admin", "ADMIN");
    }
}

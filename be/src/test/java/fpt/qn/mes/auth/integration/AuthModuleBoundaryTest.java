package fpt.qn.mes.auth.integration;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;

class AuthModuleBoundaryTest {

    @Test
    void roleModuleNoLongerExistsAndImportsAreConsolidatedIntoAuth() throws IOException {
        Path sourceRoot = Path.of("src", "main", "java");
        assertThat(sourceRoot.resolve(Path.of("fpt", "qn", "mes", "role"))).doesNotExist();
        try (var files = Files.walk(sourceRoot)) {
            assertThat(files.filter(path -> path.toString().endsWith(".java"))
                    .map(path -> read(path))
                    .anyMatch(content -> content.contains("fpt.qn.mes.role")))
                    .isFalse();
        }
        for (String layer : java.util.List.of("domain", "application", "infrastructure", "presentation")) {
            assertThat(sourceRoot.resolve(Path.of("fpt", "qn", "mes", "auth", layer))).isDirectory();
        }

        Path authRoot = sourceRoot.resolve(Path.of("fpt", "qn", "mes", "auth"));
        try (var files = Files.walk(authRoot)) {
            assertThat(files.filter(path -> path.toString().endsWith(".java"))
                    .map(path -> read(path))
                    .anyMatch(content -> content.contains("fpt.qn.mes.user")))
                    .as("auth must remain upstream from user")
                    .isFalse();
        }

        Path authApplication = authRoot.resolve("application");
        try (var files = Files.walk(authApplication)) {
            assertThat(files.filter(path -> path.toString().endsWith(".java"))
                    .map(path -> read(path))
                    .anyMatch(content -> content.contains("org.jooq")))
                    .as("application code must not depend on jOOQ")
                    .isFalse();
        }

        Path userRoot = sourceRoot.resolve(Path.of("fpt", "qn", "mes", "user"));
        try (var files = Files.walk(userRoot)) {
            assertThat(files.filter(path -> path.toString().endsWith(".java"))
                    .map(path -> read(path))
                    .anyMatch(content -> content.contains("fpt.qn.mes.auth.application.service")))
                    .as("user may depend on auth ports, not auth services")
                    .isFalse();
        }
    }

    private String read(Path path) {
        try {
            return Files.readString(path);
        } catch (IOException ex) {
            throw new IllegalStateException(ex);
        }
    }
}

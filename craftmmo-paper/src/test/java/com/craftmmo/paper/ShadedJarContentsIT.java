package com.craftmmo.paper;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.jar.JarFile;
import org.junit.jupiter.api.Test;

final class ShadedJarContentsIT {
    @Test
    void shadedPluginExcludesProvidedAndTestInfrastructure() throws IOException {
        Path jar = Path.of("target", "craftmmo-paper-0.1.0-SNAPSHOT-plugin.jar");
        assertTrue(Files.isRegularFile(jar), "shaded plugin jar is missing");

        try (JarFile jarFile = new JarFile(jar.toFile())) {
            assertFalse(hasEntryStartingWith(jarFile, "io/papermc/paper/"));
            assertFalse(hasEntryStartingWith(jarFile, "org/bukkit/"));
            assertFalse(hasEntryStartingWith(jarFile, "org/junit/"));
            assertFalse(hasEntryStartingWith(jarFile, "org/testcontainers/"));
            assertFalse(hasEntryStartingWith(jarFile, "org/apache/maven/"));
            assertTrue(jarFile.getEntry("plugin.yml") != null);
        }
    }

    private static boolean hasEntryStartingWith(JarFile jarFile, String prefix) {
        return jarFile.stream().anyMatch(entry -> entry.getName().startsWith(prefix));
    }
}

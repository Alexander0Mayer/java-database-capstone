package com.project.back_end;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test") // Lädt application-test.properties mit Platzhaltern
class BackEndApplicationTests {

    @Test
    void contextLoads() {
        // Prüft nur, ob der Spring ApplicationContext korrekt startet
        // Die DB-Verbindung wird über Umgebungsvariablen konfiguriert
    }
    @Configuration
    static class TestConfig {
        @Bean
        PasswordEncoder passwordEncoder() {
            return new BCryptPasswordEncoder();
        }
    }

}

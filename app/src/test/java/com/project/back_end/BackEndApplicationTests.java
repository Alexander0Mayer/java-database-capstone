package com.project.back_end;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test") // Lädt application-test.properties mit Platzhaltern
class BackEndApplicationTests {

    @Test
    void contextLoads() {
        // Prüft nur, ob der Spring ApplicationContext korrekt startet
        // Die DB-Verbindung wird über Umgebungsvariablen konfiguriert
    }

}

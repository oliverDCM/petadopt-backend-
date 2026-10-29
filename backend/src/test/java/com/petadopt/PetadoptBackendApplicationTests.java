package com.petadopt;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@Disabled("Requiere PostgreSQL y variables JWT_SECRET/SPRING_DATASOURCE_*; habilitar con Testcontainers")
@SpringBootTest
class PetadoptBackendApplicationTests {

    @Test
    void contextLoads() {
    }

}

package dev.jettro.blogpromotor;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for the App
 */
class AppTest {
    
    @Test
    void testGetGreeting() {
        App app = new App();
        assertEquals("Hello Agent Blog Promotor!", app.getGreeting());
    }
}

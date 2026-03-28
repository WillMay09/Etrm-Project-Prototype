package com.fdm.group.Etrm_Project_Prototype;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class SimpleTest {
	
	  @Test
	    void testAddition() {
	        // Arrange
	        int a = 2;
	        int b = 3;
	        
	        // Act
	        int result = a + b;
	        
	        // Assert
	        assertEquals(5, result);
	    }
	    
	    @Test
	    void testString() {
	        String message = "Hello JUnit 5";
	        
	        assertNotNull(message);
	        assertTrue(message.contains("JUnit"));
	    }

}

package de.beusterse.abfalllro.utils;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Unit tests for JSONUtils
 *
 * Created by Felix Beuster on 1/2/2018.
 */

public class JSONUtilsTest {

    @Test
    public void isValidJSON_EmptyString_ReturnFalse() {
        assertFalse(JSONUtils.isValidJSON(null));
        assertFalse(JSONUtils.isValidJSON(""));
    }

    @Test
    public void isValidJSON_InvalidArrayString_ReturnFalse() {
        assertFalse(JSONUtils.isValidJSON("[1,2"));
    }

    @Test
    public void isValidJSON_InvalidObjectString_ReturnFalse() {
        assertFalse(JSONUtils.isValidJSON("{\"key\":\"value}"));
    }

    @Test
    public void isValidJSON_EmptyArrayString_ReturnTrue() {
        assertTrue(JSONUtils.isValidJSON("[]"));
    }

    @Test
    public void isValidJSON_EmptyObjectString_ReturnTrue() {
        assertTrue(JSONUtils.isValidJSON("{}"));
    }

    @Test
    public void isValidJSON_ValidArrayString_ReturnTrue() {
        assertTrue(JSONUtils.isValidJSON("[\"key\"]"));
    }

    @Test
    public void isValidJSON_ValidObjectString_ReturnTrue() {
        assertTrue(JSONUtils.isValidJSON("{\"key\":\"value\"}"));
    }
}

package de.beusterse.abfalllro.utils;

import android.support.test.runner.AndroidJUnit4;

import com.google.gson.JsonObject;

import static org.junit.Assert.*;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

/**
 * Android JUnit tests
 *
 * Created by Felix Beuster on 1/3/2018.
 */
@RunWith(AndroidJUnit4.class)
public class JSONUtilsAndroidTest {

    @Rule
    public MockitoRule rule = MockitoJUnit.rule();

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @Test
    public void getJsonObjectFromFile_noFile_returnNull() {
        JsonObject result   = JSONUtils.getJsonObjectFromInputStream( getClass().getResourceAsStream("no_file.json"));

        assertEquals(null, result);
    }

    @Test
    public void getJsonObjectFromFile_emptyFile_returnNull() {
        JsonObject result   = JSONUtils.getJsonObjectFromInputStream( getClass().getResourceAsStream("empty_file.json"));

        assertEquals(null, result);
    }

    @Test
    public void getJsonObjectFromFile_invalidFile_returnNull() {
        JsonObject result   = JSONUtils.getJsonObjectFromInputStream( getClass().getResourceAsStream("invalid_file.json"));

        assertEquals(null, result);
    }

    @Test
    public void getJsonObjectFromFile_validFile_returnJsonObject() {
        JsonObject result   = JSONUtils.getJsonObjectFromInputStream( getClass().getResourceAsStream("valid_file.json"));
        JsonObject expected = new JsonObject();
        expected.addProperty("key", "value");

        assertEquals(expected, result);
    }
}

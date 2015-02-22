package org.jeo.cli;

import org.jeo.json.JSONArray;
import org.junit.Test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class DriversTest extends CLITestSupport {

    @Test
    public void test() throws Exception {
        cli.handle("drivers");

        JSONArray arr = (JSONArray) jsonOutput();
        assertNotNull(arr);
        assertTrue(arr.size() > 0);
    }
}

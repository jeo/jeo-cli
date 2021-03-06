/* Copyright 2013 The jeo project. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.jeo.cli;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;

import com.google.common.io.ByteStreams;

import io.jeo.data.Cursor;
import io.jeo.json.JSONArray;
import io.jeo.json.JSONObject;
import io.jeo.TestData;
import io.jeo.data.mem.Memory;
import io.jeo.geojson.GeoJSONReader;
import io.jeo.json.parser.JSONParser;
import io.jeo.json.parser.ParseException;
import io.jeo.vector.Feature;
import io.jeo.vector.FeatureCursor;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;

import org.junit.BeforeClass;

/**
 * Base test for CLI command tests.
 * 
 * @author Justin Deoliveira, Boundless
 *
 */
public class CLITestSupport {

    protected ByteArrayOutputStream out;
    protected JeoCLI cli;

    @Before
    public void setUpCLI() throws IOException {
        out = new ByteArrayOutputStream();

        cli = new JeoCLI(new ByteArrayInputStream(new byte[]{}), new PrintStream(out, true));
        cli.throwErrors(true);
    }

    @BeforeClass
    public static void setUpData() throws IOException {
        Memory.open("test").put("states", TestData.states());
    }

    @After
    public void tearDownCLI() throws IOException {
        out.close();
    }

    @AfterClass
    public static void tearDownData() throws IOException {
        Memory.open("test").clear();
    }

    /**
     * Copies current cli output to the specified stream.
     */
    protected void dump(OutputStream stream) throws IOException {
        ByteStreams.copy(output(), stream);
    }

    /**
     * Returns the command output as an input stream.
     */
    protected InputStream output() throws IOException {
        return ByteStreams.newInputStreamSupplier(out.toByteArray()).getInput();
    }

    /**
     * Returns the command output as a feature cursor.
     */
    protected Cursor<Feature> featureOutput() throws IOException {
        GeoJSONReader reader = new GeoJSONReader();
        return reader.features(output());
    }

    /**
     * Returns the command output as a JSON object.
     * <p>
     * This method will return either a {@link JSONObject} or {@link JSONArray}.
     * </p>
     */
    protected Object jsonOutput() throws IOException {
        try {
            return new JSONParser().parse(new InputStreamReader(output()));
        } catch (ParseException e) {
            throw new IOException(e);
        }
    }
}

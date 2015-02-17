/* Copyright 2015 The jeo project. All rights reserved.
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
package org.jeo.cli.cmd;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import jdk.nashorn.api.scripting.ScriptObjectMirror;
import org.jeo.cli.JeoCLI;
import org.jeo.cli.validate.FileExistsValidator;
import org.jeo.data.Cursor;
import org.jeo.util.Pair;
import org.jeo.vector.FeatureCursor;
import org.jeo.vector.VectorDataset;
import org.jeo.vector.VectorQuery;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Paths;

@Parameters(commandNames="transform", commandDescription="Transforms a result set")
public class TransformCmd extends VectorCmd {

    @Parameter(names = {"-i", "--input"}, description = "Input data set")
    String dataRef;

    @Parameter(names = {"-o", "--output"}, description = "Output for results")
    VectorSink sink = new GeoJSONSink();

    @Parameter(names = {"-s", "--script"}, description = "Transform script", validateWith = FileExistsValidator.class)
    String script;

    ScriptEngineManager scriptMgr = new ScriptEngineManager();

    @Override
    protected void run(JeoCLI cli) throws Exception {
        Pair<FeatureCursor,VectorDataset> input = input(dataRef, new VectorQuery(), cli);

        FeatureCursor cursor = input.first;
        if (script != null) {
            cursor = runScript(cursor, script);
        }

        try {
            sink.encode(cursor, input.second, cli);
        }
        finally {
            cursor.close();
        }
    }

    FeatureCursor runScript(FeatureCursor cursor, String script) throws IOException, ScriptException {
        ScriptEngine eng = scriptMgr.getEngineByName("nashorn");
        try (Reader in = Files.newBufferedReader(Paths.get(script))) {
            eng.eval(in);

            Object obj = eng.get("transform");
            if (obj == null) {
                throw new IllegalStateException("Script must define a 'transform' function");
            }

            if (!(obj instanceof ScriptObjectMirror)) {
                throw new IllegalStateException("Unrecognized object: " + obj);
            }

            ScriptObjectMirror scriptObj = (ScriptObjectMirror) obj;
            if (!scriptObj.isFunction()) {
                throw new IllegalStateException("transform must be a function");
            }

            Object result = scriptObj.call(null, cursor);
            if (!(result instanceof Cursor)) {
                throw new IllegalStateException("transform function must return a cursor");
            }

            return FeatureCursor.wrap((Cursor)result);
        }
    }
}

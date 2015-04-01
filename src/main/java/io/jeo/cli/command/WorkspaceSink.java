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
package io.jeo.cli.command;

import io.jeo.cli.JeoCLI;
import io.jeo.cli.Util;
import io.jeo.data.Cursor;
import io.jeo.data.Drivers;
import io.jeo.data.Workspace;
import io.jeo.util.Disposer;
import io.jeo.util.Pair;
import io.jeo.vector.Feature;
import io.jeo.vector.Schema;
import io.jeo.vector.SchemaBuilder;
import io.jeo.vector.VectorDataset;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.util.Locale;

import static java.lang.String.format;

/**
 * Vector sink that writes data into a new dataset of a workspace.
 */
public class WorkspaceSink implements VectorSink {

    static Logger LOG = LoggerFactory.getLogger(WorkspaceSink.class);

    Pair<URI, String> ref;

    public WorkspaceSink(Pair<URI,String> ref) {
        this.ref = ref;
    }

    @Override
    public void encode(Cursor<Feature> cursor, VectorDataset source, JeoCLI cli) throws IOException {
        try (Disposer disposer = new Disposer()) {
            Workspace dest = disposer.open(Drivers.open(ref.first, Workspace.class));
            if (dest == null) {
                throw new IllegalArgumentException("Unable to open workspace: " + ref.first);
            }

            // buffer the cursor to read the first feature
            cursor = cursor.buffer(1);

            Schema schema = cursor.first()
                .orElseThrow(() -> new IllegalArgumentException("No data to write")).schema();

            cursor.rewind();

            String dsName = ref.second;
            if (dsName != null) {
                // rename the schema
                schema = SchemaBuilder.rename(schema, dsName);
            }

            dsName = schema.name();
            if (disposer.open(dest.get(dsName)) != null) {
                throw new IllegalStateException(
                format(Locale.ROOT, "Data set %s already exists in workspace %s", dsName, ref.first));
            }

            try {
                VectorDataset dataset = disposer.open(dest.create(schema));
                Util.copy(cursor, dataset, source, cli);
            }
            catch(UnsupportedOperationException e) {
                throw new IllegalStateException(
                    format(Locale.ROOT, "Workspace %s does not support creating data sets", ref.first));
            }

        }
    }


}

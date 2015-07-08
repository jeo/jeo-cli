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
import io.jeo.data.Dataset;
import io.jeo.data.Drivers;
import io.jeo.util.Disposer;
import io.jeo.util.Pair;
import io.jeo.vector.Feature;
import io.jeo.vector.Features;
import io.jeo.vector.Schema;
import io.jeo.vector.VectorDataset;

import java.io.IOException;
import java.net.URI;
import java.util.Locale;

import static java.lang.String.format;

public class DatasetSink implements VectorSink {

    Pair<URI,String> ref;

    public DatasetSink(Pair<URI,String> ref) {
        this.ref = ref;
    }

    @Override
    public void encode(Cursor<Feature> cursor, VectorDataset source, JeoCLI cli) throws IOException {
        try (Disposer disposer = new Disposer()) {
            try {
                if (disposer.open(Drivers.open(ref.first, Dataset.class)) != null) {
                    throw new IllegalStateException(format(Locale.ROOT, "Dataset %s already exists", ref.first));
                }
            }
            catch(Exception e) {
            }

            Schema schema = null;
            if (source != null) {
                schema = source.schema();
            }

            if (schema == null) {
                Cursor<Feature> buffered = cursor.buffer(1);
                schema = cursor.buffer(1).first()
                    .map((f) -> Features.schema(source != null ? source.name() : "features", f))
                    .orElseThrow(() -> new IllegalStateException("No data to read"));
                buffered.rewind();
            }

            VectorDataset target = disposer.open(Drivers.create(schema, ref.first, VectorDataset.class));
            Util.copy(cursor, target, source, cli);
        }
    }
}

package io.jeo.cli.command;

import io.jeo.cli.JeoCLI;
import io.jeo.data.Cursor;
import io.jeo.geobuf.GeobufWriter;
import io.jeo.vector.Feature;
import io.jeo.vector.FeatureCursor;
import io.jeo.vector.Schema;
import io.jeo.vector.VectorDataset;

import java.io.IOException;

/**
 * Encodes cursor as a geobuf to the cli output stream.
 */
public class GeobufSink implements VectorSink {
    @Override
    public void encode(Cursor<Feature> cursor, VectorDataset source, JeoCLI cli) throws IOException {
        try (GeobufWriter writer = new GeobufWriter(cli.output())) {
            writer.write(FeatureCursor.wrap(cursor));
        }
    }

    @Override
    public String toString() {
        return "gbf";
    }
}

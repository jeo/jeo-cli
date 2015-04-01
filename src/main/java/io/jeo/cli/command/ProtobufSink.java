package io.jeo.cli.command;

import io.jeo.cli.JeoCLI;
import io.jeo.data.Cursor;
import io.jeo.protobuf.ProtobufWriter;
import io.jeo.vector.Feature;
import io.jeo.vector.Schema;
import io.jeo.vector.VectorDataset;

import java.io.IOException;

/**
 * Encodes cursor as a protobuf to the cli output stream.
 */
public class ProtobufSink implements VectorSink {
    @Override
    public void encode(Cursor<Feature> cursor, VectorDataset source, JeoCLI cli) throws IOException {
        try (ProtobufWriter writer = new ProtobufWriter(cli.output())) {
            cursor = cursor.buffer(1);
            Schema schema = cursor.first().orElseThrow(
                () -> new IllegalArgumentException("No data to write")).schema();

            writer.schema(schema);
            cursor.rewind();

            while (cursor.hasNext()) {
                writer.feature(cursor.next());
            }
        }
    }

    @Override
    public String toString() {
        return "pbf";
    }
}

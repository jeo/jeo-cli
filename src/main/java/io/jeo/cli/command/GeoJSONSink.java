package io.jeo.cli.command;

import io.jeo.cli.JeoCLI;
import io.jeo.data.Cursor;
import io.jeo.geojson.GeoJSONWriter;
import io.jeo.vector.Feature;
import io.jeo.vector.VectorDataset;

import java.io.IOException;

/**
 * Encodes cursor as GeoJSON to the cli output stream.
 */
public class GeoJSONSink implements VectorSink {
    @Override
    public void encode(Cursor<Feature> cursor, VectorDataset source, JeoCLI cli) throws IOException {
        GeoJSONWriter w = new GeoJSONWriter(cli.console().getOutput());
        w.featureCollection(cursor);
        w.flush();
    }

    @Override
    public String toString() {
        return "geojson";
    }
}

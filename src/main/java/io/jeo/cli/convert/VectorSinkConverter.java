package io.jeo.cli.convert;

import com.beust.jcommander.IStringConverter;
import io.jeo.cli.Util;
import io.jeo.cli.command.DatasetSink;
import io.jeo.cli.command.GeoJSONSink;
import io.jeo.cli.command.ProtobufSink;
import io.jeo.cli.command.VectorSink;
import io.jeo.cli.command.WorkspaceSink;
import io.jeo.data.Dataset;
import io.jeo.data.Driver;
import io.jeo.data.Drivers;
import io.jeo.data.Workspace;
import io.jeo.util.Pair;

import java.net.URI;

public class VectorSinkConverter implements IStringConverter<VectorSink> {
    @Override
    public VectorSink convert(String str) {
        if ("pbf".equalsIgnoreCase(str)) {
            return new ProtobufSink();
        }
        if ("geojson".equalsIgnoreCase(str) || "json".equalsIgnoreCase(str)) {
            return new GeoJSONSink();
        }

        Pair<URI,String> ref = null;
        try {
            ref = Util.parseDataURI(str);
        }
        catch(IllegalArgumentException e) {
            throw new IllegalArgumentException("Unrecognized output: " + str);
        }

        Driver<?> drv = Drivers.find(ref.first);
        if (drv == null ){
            throw new IllegalArgumentException("No driver found for: " + ref.first);
        }

        if (Workspace.class.isAssignableFrom(drv.type())) {
            return new WorkspaceSink(ref);
        }
        else if (Dataset.class.isAssignableFrom(drv.type())) {
            return new DatasetSink(ref);
        }
        else {
            throw new IllegalArgumentException("Unsupported driver type: " + drv.name());
        }
    }
}

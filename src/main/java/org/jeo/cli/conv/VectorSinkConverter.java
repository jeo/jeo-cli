package org.jeo.cli.conv;

import com.beust.jcommander.IStringConverter;
import org.jeo.cli.Util;
import org.jeo.cli.cmd.DatasetSink;
import org.jeo.cli.cmd.GeoJSONSink;
import org.jeo.cli.cmd.JeoCmd;
import org.jeo.cli.cmd.ProtobufSink;
import org.jeo.cli.cmd.VectorSink;
import org.jeo.cli.cmd.WorkspaceSink;
import org.jeo.data.Dataset;
import org.jeo.data.Driver;
import org.jeo.data.Drivers;
import org.jeo.data.Workspace;
import org.jeo.util.Pair;

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

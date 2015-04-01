package io.jeo.cli.command;

import io.jeo.cli.JeoCLI;
import io.jeo.data.Dataset;
import io.jeo.protobuf.ProtobufCursor;
import io.jeo.protobuf.ProtobufReader;
import io.jeo.util.Function;
import io.jeo.util.Optional;
import io.jeo.util.Pair;
import io.jeo.vector.FeatureCursor;
import io.jeo.vector.VectorDataset;
import io.jeo.vector.VectorQuery;
import io.jeo.vector.VectorQueryPlan;

import java.io.IOException;
import java.util.Locale;

import static java.lang.String.format;

/**
 * Base class for commands that work on vector data.
 */
public abstract class VectorCmd extends JeoCmd {

    /**
     * Opens a vector dataset from a dataset uri.
     */
    protected Optional<VectorDataset> openVectorDataset(String ref) throws IOException {
        Optional<Dataset> data = openDataset(ref);
        if (data.isPresent() && !(data.get() instanceof VectorDataset)) {
            throw new IllegalArgumentException(ref + " is not a vector dataset");
        }

        return data.map(new Function<Dataset, VectorDataset>() {
            @Override
            public VectorDataset apply(Dataset value) {
                return (VectorDataset) value;
            }
        });
    }

    /**
     * Obtains an input cursor from stdin.
     */
    protected FeatureCursor cursorFromStdin(JeoCLI cli) throws IOException {
        // look for input from stdin
        // TODO: something better than just assuming pbf
        return new ProtobufCursor(new ProtobufReader(cli.console().getInput()).setReadUntilLastFeature());
    }

    /**
     * Obtains an input cursor.
     * <p>
     *  If <tt>dataRef</tt> is not null, {@link #openVectorDataset(String)} is used to obtain a data set
     *  and cursor. Otherwise {@link #cursorFromStdin(org.jeo.cli.JeoCLI)} is used to get a cursor from
     *  stdin.
     * </p>
     */
    protected Pair<FeatureCursor,VectorDataset> input(String dataRef, VectorQuery q, JeoCLI cli)
        throws IOException {

        FeatureCursor cursor = null;
        VectorDataset data = null;
        if (dataRef != null) {
            data = openVectorDataset(dataRef).orElseThrow(
                () -> new IllegalArgumentException(format(Locale.ROOT, "%s is not a data set", dataRef)));
            cursor = data.cursor(q);
        }
        else {
            // look for a direct cursor from stdin
            cursor = cursorFromStdin(cli);
            cursor = new VectorQueryPlan(q).apply(cursor);
        }

        return Pair.of(cursor, data);
    }
}

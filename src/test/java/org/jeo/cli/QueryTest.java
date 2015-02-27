package org.jeo.cli;

import com.google.common.collect.Sets;
import com.vividsolutions.jts.geom.Point;
import org.jeo.Tests;
import org.jeo.data.Cursor;
import org.jeo.data.Workspace;
import org.jeo.data.mem.MemVector;
import org.jeo.data.mem.MemWorkspace;
import org.jeo.data.mem.Memory;
import org.jeo.geom.Geom;
import org.jeo.geopkg.GeoPackage;
import org.jeo.protobuf.ProtobufReader;
import org.jeo.vector.Feature;
import org.jeo.vector.FeatureCursor;
import org.jeo.vector.Features;
import org.jeo.vector.Schema;
import org.jeo.vector.VectorDataset;
import org.jeo.vector.VectorQuery;
import org.junit.Test;

import java.io.File;
import java.util.Arrays;
import java.util.Set;

import static java.lang.String.format;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class QueryTest extends CLITestSupport {

    @Test
    public void testInput() throws Exception {
        cli.handle("query", "-i", "mem://test#states");

        assertEquals(49, featureOutput().count());
    }

    @Test
    public void testFilter() throws Exception {
        cli.handle("query", "-i", "mem://test#states", "-f", "STATE_ABBR = 'CA'");

        Cursor<Feature> cursor = featureOutput();
        assertEquals("California", cursor.first().get().get("STATE_NAME"));

        assertFalse(cursor.hasNext());
    }

    @Test
    public void testBBOX() throws Exception {
        cli.handle("query", "-i", "mem://test#states", "-b", "-106.649513,25.845198,-93.507217,36.493877");

        final Set<String> abbrs = Sets.newHashSet("MO", "OK", "TX", "NM", "AR", "LA");
        featureOutput().each(f -> abbrs.remove(f.get("STATE_ABBR")));

        assertTrue(abbrs.isEmpty());
    }

    @Test
    public void testLimit() throws Exception {
        cli.handle("query", "-i", "mem://test#states", "-l", "10");
        assertEquals(10, featureOutput().count());
    }

    @Test
    public void testSkip() throws Exception {
        cli.handle("query", "-i", "mem://test#states", "-s", "10");
        assertEquals(39, featureOutput().count());
    }

    @Test
    public void testProps() throws Exception {
        cli.handle("query", "-i", "mem://test#states", "-p", "STATE_ABBR");

        Feature f = featureOutput().first().get();
        assertNotNull(f.get("STATE_ABBR"));
        assertNull(f.get("STATE_NAME"));
    }

    @Test
    public void testOutputToWorkspace() throws Exception {
        cli.handle("query", "-i", "mem://test#states", "-f", "STATE_ABBR = 'CA'", "-o", "mem://foo");

        MemWorkspace mem = Memory.open("foo");
        VectorDataset target = (VectorDataset) mem.get("states");
        assertNotNull(target);

        assertEquals(1, target.count(new VectorQuery()));

        Feature f = target.cursor(new VectorQuery()).first().get();
        assertEquals("California", f.get("STATE_NAME"));
    }

    @Test
    public void testOutputToPBF() throws Exception {
        cli.handle("query", "-i", "mem://test#states", "-f", "STATE_ABBR = 'CA'", "-o", "pbf");

        ProtobufReader pbf = new ProtobufReader(output());
        Schema schema = pbf.schema();
        assertNotNull(schema);

        Feature f = pbf.feature(schema);
        assertEquals("California", f.get("STATE_NAME"));
    }

    @Test
    public void testOutputToGeopkg() throws Exception {
        File gpkg = new File(Tests.newTmpDir(), "foo.gpkg");

        Schema foo = Schema.build("foo").field("geo", Point.class).field("name", String.class).schema();
        MemVector mem = Memory.open("test").remove("foo").create(foo);
        mem.add(Features.create(null, foo, Geom.point(0,0), "zero"));
        mem.add(Features.create(null, foo, Geom.point(1,1), "one"));

        cli.handle("query", "-i", "mem://test#foo", "-c", "epsg:4326", "-o", gpkg.getPath());

        Workspace ws = GeoPackage.open(gpkg);
        VectorDataset ds = (VectorDataset) ws.get("foo");
        assertNotNull(ds);
        assertEquals(2, ds.count(new VectorQuery()));
    }
}

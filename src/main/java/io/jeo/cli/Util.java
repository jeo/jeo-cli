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
package io.jeo.cli;

import com.google.common.base.Throwables;
import io.jeo.data.Cursor;
import io.jeo.data.Transaction;
import io.jeo.data.Transactional;
import io.jeo.util.Disposer;
import io.jeo.util.Optional;
import io.jeo.util.Pair;
import io.jeo.vector.Feature;
import io.jeo.vector.FeatureCursor;
import io.jeo.vector.Features;
import io.jeo.vector.VectorDataset;
import io.jeo.vector.VectorQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * Cli utility class.
 */
public class Util {

    static Logger LOG = LoggerFactory.getLogger(Util.class);

    /**
     * Parses a data uri.
     * <p>
     * The fragment is stripped off the uri and returned separately.
     * </p>
     * @return A pair of (base uri, fragment)
     */
    public static Pair<URI,String> parseDataURI(String str) {
        try {
            URI uri = new URI(str);
            if (uri.getScheme() == null) {
                //assume a file based uri
                URI tmp = new File(uri.getPath()).toURI();
                uri = uri.getFragment() != null ?
                    new URI(tmp.getScheme(), null, tmp.getPath(), uri.getFragment()) : tmp;
            }

            // strip off fragment
            String frag = uri.getFragment();
            if (frag != null) {
                uri = new URI(
                    uri.getScheme(), uri.getUserInfo(), uri.getHost(), uri.getPort(), uri.getPath(), uri.getQuery(), null);
            }

            return Pair.of(uri, frag);
        }
        catch(URISyntaxException e) {
            throw new IllegalArgumentException("Invalid data source uri: " + str, e);
        }
    }

    /**
     * Copies a feature cursor into the <tt>target</tt> dataset.
     * <p>
     *  The <tt>source</tt> argument may be <code>null</code>.
     * </p>
     */
    public static void copy(Cursor<Feature> cursor, VectorDataset dataset, VectorDataset source, JeoCLI cli)
        throws IOException {

        try (Disposer disposer = new Disposer()) {
            Optional<Transaction> tx =
                Optional.of(dataset instanceof Transactional ? ((Transactional) dataset).transaction(null) : null);

            FeatureCursor target =
                disposer.open(dataset.cursor(new VectorQuery().append().transaction(tx.orElse(null))));

            try {
                ConsoleProgress progress = cli.progress(-1);
                if (source != null) {
                    progress.init((int) source.count(new VectorQuery()));
                }

                for (Feature f : cursor) {
                    progress.inc();

                    Feature g = target.next();
                    Features.copy(f, g);
                    target.write();
                }

                if (tx.isPresent()) {
                    tx.get().commit();
                }
            } catch (Exception e) {
                tx.ifPresent((t) -> {
                    try {
                        t.rollback();
                    } catch (IOException e1) {
                        LOG.debug("Error rolling back transaction", e1);
                    }
                });
                Throwables.propagateIfInstanceOf(e, IOException.class);
                throw new IOException(e);
            }
        }
    }
}

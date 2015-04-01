/* Copyright 2013 The jeo project. All rights reserved.
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

import java.io.IOException;
import java.io.PrintWriter;
import java.net.URI;
import java.util.Locale;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

import io.jeo.cli.JeoCLI;
import io.jeo.cli.Util;
import io.jeo.data.Dataset;
import io.jeo.data.Disposable;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import io.jeo.data.Drivers;
import io.jeo.data.Workspace;
import io.jeo.util.Disposer;
import io.jeo.util.Optional;
import io.jeo.util.Pair;

import static java.lang.String.format;

public abstract class JeoCmd {

    @Parameter(names={"-h", "--help"}, description="Provides help for this command", help=true)
    boolean help;

    @Parameter(names={"-x", "--debug"}, description="Runs command in debug mode", help=true)
    boolean debug;
    
    Disposer disposer = new Disposer();

    public final void exec(JeoCLI cli) throws Exception {
        if (help) {
            usage(cli);
            return;
        }

        if (debug) {
            setUpDebugLogging();
        }
        try {
            run(cli);
        }
        catch(Exception e) {
            if (cli.throwErrors()) {
                throw e;
            }
            if (debug) {
                print(e, cli);
            }
            else {
                cli.console().println(e.getMessage());
            }
        }
        finally {
            disposer.close();
        }
        cli.console().flush();
    }
    
    void setUpDebugLogging() {
        // check for jdk logging property, if present don't do anything
        if (System.getProperty("java.util.logging.config.file") == null) {
            Logger log = Logger.getLogger("org.jeo");
            log.setLevel(Level.ALL);

            ConsoleHandler handler = new ConsoleHandler();
            handler.setLevel(Level.ALL);
            log.addHandler(handler);
        }
    }

    protected abstract void run(JeoCLI cli) throws Exception;

    public void usage(JeoCLI cli) {
        JCommander jc = new JCommander(this);
        String cmd = this.getClass().getAnnotation(Parameters.class).commandNames()[0];
        jc.setProgramName("jeo " + cmd);
        jc.usage();
    }

    protected <T extends Disposable> T open(T obj) {
        return disposer.open(obj);
    }

    protected void print(Exception e, JeoCLI cli) {
        e.printStackTrace(new PrintWriter(cli.console().getOutput()));
    }

    /**
     * Opens a dataset from a data uri.
     */
    protected Optional<Dataset> openDataset(String ref) throws IOException {
        Pair<URI,String> uri = Util.parseDataURI(ref);

        Dataset dataset;

        if (uri.second != null) {
            // reference through a workspace
            Workspace ws = open(Drivers.open(uri.first, Workspace.class));
            if (ws == null) {
                throw new IllegalArgumentException("Unable to open workspace: " + uri.first);
            }

            dataset = open(ws.get(uri.second));
            if (dataset == null) {
                throw new IllegalArgumentException(
                    format(Locale.ROOT, "No dataset named %s in workspace: %s", uri.second, uri.first));
            }
        }
        else {
            // straight dataset reference
            try {
                dataset = open((Dataset) Drivers.open(uri.first));
            }
            catch(ClassCastException e) {
                throw new IllegalArgumentException(uri.first + " is not a dataset");
            }
        }

        return Optional.of(dataset);
    }
}

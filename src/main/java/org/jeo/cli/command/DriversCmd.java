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
package org.jeo.cli.command;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import org.jeo.cli.JeoCLI;
import org.jeo.data.Dataset;
import org.jeo.data.Driver;
import org.jeo.data.Drivers;
import org.jeo.data.Workspace;
import org.jeo.geojson.GeoJSONWriter;
import org.jeo.util.Key;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import org.jeo.util.Messages;

import static java.lang.String.format;

@Parameters(commandNames="drivers", commandDescription="List available format drivers")
public class DriversCmd extends JeoCmd {

    @Parameter(description="drivers", required=false)
    List<String> drivers;

    @Override
    protected void run(JeoCLI cli) throws Exception {
        GeoJSONWriter w = cli.newJSONWriter();

        if (drivers != null && !drivers.isEmpty()) {
            if (drivers.size() > 1) {
                w.array();
            }

            for (String driver : drivers) {
                Driver<?> drv = Drivers.find(driver);
                if (drv == null) {
                    throw new IllegalArgumentException("No such driver: " + driver);
                }

                w.object();
                printBrief(drv, w);

                w.key("aliases").array();
                for (String a : drv.aliases()) {
                    w.value(a);
                }
                w.endArray();


                Messages msgs = new Messages();
                if (!drv.isEnabled(msgs)) {
                    List<Throwable> errors = msgs.list();
                    if (!errors.isEmpty()) {
                        w.key("messages").array();
                        for (Throwable t : errors) {
                            w.value(format(Locale.ROOT, "%s: %s", t.getClass().getName(), t.getMessage()));
                        }
                        w.endArray();
                    }
                }

                w.key("type");
                Class<?> type = drv.type();
                if (Workspace.class.isAssignableFrom(type)) {
                    w.value("workspace");
                }
                else if (Dataset.class.isAssignableFrom(type)) {
                    w.value("dataset");
                }
                else {
                    w.value(drv.type().getSimpleName());
                }

                w.key("keys").object();
                for (Key<?> key : drv.keys()) {
                    w.key(key.name()).object();
                    w.key("type").value(key.type().getSimpleName());
                    if (key.def() != null) {
                        w.key("default").value(key.def());
                    }
                    w.endObject();
                }
                w.endObject();

                w.endObject();
            }
            if (drivers.size() > 1) {
                w.endArray();
            }
        }
        else {
            Iterator<Driver<?>> it = Drivers.list();
            
            w.array();
            while(it.hasNext()) {
                Driver<?> drv = it.next();
                
                w.object();

                printBrief(drv, w);
                
                w.endObject();
            }
            w.endArray();
        }

    }

    void printBrief(Driver<?> drv, GeoJSONWriter w) throws IOException {
        w.key("name").value(drv.name());
        w.key("enabled").value(drv.isEnabled(null));
    }
}

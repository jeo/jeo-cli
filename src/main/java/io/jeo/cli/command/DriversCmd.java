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
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import io.jeo.cli.JeoCLI;
import io.jeo.data.Dataset;
import io.jeo.data.Driver;
import io.jeo.data.Drivers;
import io.jeo.data.Workspace;
import io.jeo.geojson.GeoJSONWriter;
import io.jeo.json.JeoJSONWriter;
import io.jeo.util.Key;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import io.jeo.util.Messages;

import static java.lang.String.format;

@Parameters(commandNames="drivers", commandDescription="List available format drivers")
public class DriversCmd extends JeoCmd {

    @Parameter(description="drivers", required=false)
    List<String> drivers;

    @Override
    protected void run(JeoCLI cli) throws Exception {
        JeoJSONWriter w = cli.newJSONWriter();

        if (drivers != null && !drivers.isEmpty()) {
            if (drivers.size() > 1) {
                w.array();
            }

            for (String driver : drivers) {
                Driver<?> drv = Drivers.find(driver);
                if (drv == null) {
                    throw new IllegalArgumentException("No such driver: " + driver);
                }

                w.driver(drv, null);
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

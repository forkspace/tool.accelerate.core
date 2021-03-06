/*******************************************************************************
 * Copyright (c) 2016 IBM Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package com.ibm.liberty.starter.api.v1.temp;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

//Temporary class until we put services.json into Cloudant
@Path("v1/services")
public class ServiceFinder {

    private static final Pattern URI_PATH_PATTERN = Pattern.compile("[a-zA-Z0-9-_/.:]*");
    private static final Logger log = Logger.getLogger(ServiceFinder.class.getName());

    @GET
    @Path("/")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getServices() {
        String jsonLocation = null;
        try {
            jsonLocation = getJsonLocation();
            return readServicesFile(jsonLocation);
        } catch (URISyntaxException e) {
            return exceptionToResponse(e, "Unable to parse URI " + jsonLocation);
        } catch (IOException e) {
            return exceptionToResponse(e, "Error reading file " + jsonLocation);
        } catch (IllegalArgumentException e) {
            return exceptionToResponse(e, "Invalid environment variable com.ibm.liberty.starter.servicesJsonLocation set.");
        }

    }

    private String getJsonLocation() {
        String jsonLocation = System.getenv("com_ibm_liberty_starter_servicesJsonLocation");
        if (jsonLocation == null) {
            jsonLocation = "/services.json";
        } else {
            jsonLocation = jsonLocation + ".json";
        }
        checkPattern(URI_PATH_PATTERN, jsonLocation);
        return jsonLocation;
    }

    private static void checkPattern(Pattern pattern, String toCheck) {
        if (!pattern.matcher(toCheck).matches()) {
            throw new IllegalArgumentException("The string " + toCheck + " does not match the pattern " + pattern);
        }
    }

    private Response readServicesFile(String jsonLocation) throws URISyntaxException, IOException {
        URI uri = new URI(jsonLocation);
        try (InputStream is = ServiceFinder.class.getResourceAsStream(uri.getPath());
                        JsonReader reader = Json.createReader(is)) {
            JsonObject jsonData = reader.readObject();
            return Response.ok(jsonData.toString(), MediaType.APPLICATION_JSON_TYPE).build();
        }
    }

    private Response exceptionToResponse(Exception e, String message) {
        log.log(Level.WARNING, message, e);
        return Response.status(Status.BAD_REQUEST).entity(message).build();
    }

}

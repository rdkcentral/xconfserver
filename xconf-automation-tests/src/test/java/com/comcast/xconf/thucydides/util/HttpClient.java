/* 
 * If not stated otherwise in this file or this component's Licenses.txt file the 
 * following copyright and licenses apply:
 *
 * Copyright 2018 RDK Management
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.=
 *
 * Author: Stanislav Menshykov
 * Created: 03.03.16  18:33
 */
package com.comcast.xconf.thucydides.util;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

public class HttpClient {

    private final static ObjectMapper mapper = new ObjectMapper();

    public static int post(String url, Object content) throws IOException {
        HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
        conn.setDoOutput(true);
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        OutputStream os = conn.getOutputStream();
        os.write(mapper.writeValueAsBytes(content));
        os.flush();
        int result = conn.getResponseCode();
        conn.disconnect();

        return result;
    }

    public static <T> List<T> getAll(String url, Class<T> clazz) throws IOException {
        return mapper.readValue(get(url), mapper.getTypeFactory().constructCollectionType(List.class, clazz));
    }

    public static String get(String url) throws IOException {
        HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
        conn.setDoOutput(true);
        conn.setRequestProperty("Content-Type", "application/json");

        if (conn.getResponseCode() == 404) {
            return "NOT FOUND";
        } else {
            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String inputLine;
            StringBuffer response = new StringBuffer();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();

            return response.toString();
        }
    }

    public static int delete(String url, String id) throws IOException {
        HttpURLConnection conn = (HttpURLConnection) new URL(url + "/" + id).openConnection();
        conn.setDoOutput(true);
        conn.setRequestMethod("DELETE");
        conn.setRequestProperty("Content-Type", "text/plain");
        conn.connect();
        int result = conn.getResponseCode();
        conn.disconnect();

        return result;
    }
}

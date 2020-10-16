/*******************************************************************************
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
 * limitations under the License.
 *******************************************************************************/
package com.comcast.hydra.astyanax.util.lightweight;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * Provides methods to get specific parts from URI and build URI from parts.
 *
 */
public class UriUtil {
    private static final char PATH_SEPARATOR = '/';
    private static final String PATH_SEPARATOR_STR = String.valueOf(PATH_SEPARATOR);
    private static final String DATA_OBJECT_PREFIX = "data" + PATH_SEPARATOR;

    public static String getUrlLastPathComponent(URI uri) {
        return uri != null ? getUrlLastPathComponent(uri.toASCIIString()) : null;
    }

    public static String getUrlLastPathComponent(String url) {
        String result = url;
        if (url != null) {
            int index = url.lastIndexOf(PATH_SEPARATOR);
            if (index >= 0) {
                result = url.substring(index + 1);
            }
        }
        return result;
    }

    public static URI buildUri(String baseUrl, String type, Long id) {
        StringBuilder builder = new StringBuilder(baseUrl.length() + type.length() + 32);

        builder.append(baseUrl);
        if (!baseUrl.endsWith(PATH_SEPARATOR_STR)) {
            builder.append(PATH_SEPARATOR);
        }
        builder.append(DATA_OBJECT_PREFIX);
        builder.append(type).append(PATH_SEPARATOR);
        builder.append(id);

        String url = builder.toString();
        try {
            return new URI(url);
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("Failed to build URI by string: " + url, e);
        }
    }

    public static String buildIdWithType(String type, Long id) {
        return (type != null ? type : "null") + PATH_SEPARATOR + id;
    }

    public static String getUrlPenultimatePathComponent(URI uri) {
        return uri != null ? getUrlPenultimatePathComponent(uri.toASCIIString()) : null;
    }

    public static String getUrlPenultimatePathComponent(String url) {
        String result = null;
        if (url != null) {
            int lastIndex = url.lastIndexOf(PATH_SEPARATOR);
            if (lastIndex > 0) {
                int typeIndex = url.lastIndexOf(PATH_SEPARATOR, lastIndex - 1);
                if (typeIndex >= 0) {
                    result = url.substring(typeIndex + 1, lastIndex);
                } else {
                    result = url.substring(0, lastIndex);
                }
            }
        }
        return result;
    }

    public static String getUrlLast2PathComponents(URI uri) {
        return uri != null ? getUrlLast2PathComponents(uri.toASCIIString()) : null;
    }

    public static String getUrlLast2PathComponents(String url) {
        String result = url;
        if (url != null) {
            int index = url.lastIndexOf(PATH_SEPARATOR);
            if (index > 0) {
                index = url.lastIndexOf(PATH_SEPARATOR, index - 1);
                if (index >= 0) {
                    result = url.substring(index + 1);
                }
            }
        }
        return result;
    }
}

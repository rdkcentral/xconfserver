/*
 * If not stated otherwise in this file or this component's LICENSE file the
 * following copyright and licenses apply:
 *
 * Copyright 2018 RDK Management
 *
 * Licensed under the Apache License, Version 2.0 (the License);
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
 */

package com.comcast.xconf.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ImportHelper {

    public static final String IMPORTED = "IMPORTED";
    public static final String NOT_IMPORTED = "NOT_IMPORTED";

    public static Map<String, List<String>> buildImportResultMap() {
        Map<String, List<String>> importResult = new HashMap<>();
        importResult.put(IMPORTED, new ArrayList<String>());
        importResult.put(NOT_IMPORTED, new ArrayList<String>());
        return importResult;
    }
}

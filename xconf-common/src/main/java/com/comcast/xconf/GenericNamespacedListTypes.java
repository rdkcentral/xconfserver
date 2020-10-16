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
 * limitations under the License.
 *
 * Author: Stanislav Menshykov
 * Created: 11.11.15  16:59
 */
package com.comcast.xconf;

public class GenericNamespacedListTypes {

    public static final String STRING = "STRING";
    public static final String MAC_LIST = "MAC_LIST";
    public static final String IP_LIST = "IP_LIST";
    public static final String RI_MAC_LIST = "RI_MAC_LIST";

    public static Boolean isValidType(String type) {
        return STRING.equals(type) || MAC_LIST.equals(type) || IP_LIST.equals(type) || RI_MAC_LIST.equals(type);
    }
}

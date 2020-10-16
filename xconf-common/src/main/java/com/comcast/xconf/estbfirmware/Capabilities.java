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
 * Author: Igor Kostrov
 * Created: 5/19/2016
*/
package com.comcast.xconf.estbfirmware;

public enum Capabilities {
    /**
     * RCDL indicates that the STB is capable of performing HTTP firmware downloads using DNS resolved URIs.
     * The download will run in the eSTB. Until this, the eCM performed the download. eCM does not have
     * DNS and thus requires an IP address.
     * <p>
     * Warning!!! Due to a bug in STB code, we have RNG150 boxes that send this parameter but they are NOT
     * able to do HTTP firmware downloads. To handle this situation we are implementing a hack where
     * RNG150 boxes will always be told to do TFTP. Once we are confident that all RNG150s have been updated
     * to versions that actually do support HTTP, we will turn off the hack.
     */
    RCDL,
    /**
     * lets Xconf know that reboot has been decoupled from firmware download. If not specified in the
     * rebootImmediately response, the STB will still reboot immediately after firmware download.
     */
    rebootDecoupled,
    rebootCoupled,
    /**
     * Lets Xconf know that the STB can accept a full URL for location rather than just an IP address or
     * domain name.
     */
    supportsFullHttpUrl;
}

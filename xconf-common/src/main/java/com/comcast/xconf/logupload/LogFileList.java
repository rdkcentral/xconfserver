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
package com.comcast.xconf.logupload;

import com.comcast.hydra.astyanax.data.XMLPersistable;
import com.comcast.apps.dataaccess.annotation.CF;
import com.comcast.xconf.CfNames;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import java.util.List;

/**
 * User: ikostrov
 * Date: 24.09.14
 * Time: 19:06
 */
@CF(cfName = CfNames.LogUpload.LOG_FILE_LIST)
public class LogFileList extends XMLPersistable {

    @XmlElementWrapper
    @XmlElement(name = "entry")
    private List<LogFile> data;

    public List<LogFile> getData() {
        return data;
    }

    public void setData(List<LogFile> data) {
        this.data = data;
    }
}

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
 * Created: 1/29/16  2:52 PM
 */
package com.comcast.xconf.estbfirmware.logupload;

import com.comcast.apps.hesperius.ruleengine.domain.standard.StandardFreeArgType;
import com.comcast.apps.hesperius.ruleengine.main.api.FreeArg;
import com.comcast.xconf.logupload.LogUploaderContext;

public class LogUploadArgs {

    public final static FreeArg ESTB_IP = new FreeArg(StandardFreeArgType.STRING, LogUploaderContext.ESTB_IP);
    public final static FreeArg ESTB_MAC = new FreeArg(StandardFreeArgType.STRING, LogUploaderContext.ESTB_MAC);
    public final static FreeArg ECM_MAC = new FreeArg(StandardFreeArgType.STRING, LogUploaderContext.ECM_MAC);
    public final static FreeArg ENV = new FreeArg(StandardFreeArgType.STRING, LogUploaderContext.ENV);
    public final static FreeArg MODEL = new FreeArg(StandardFreeArgType.STRING, LogUploaderContext.MODEL);
    public final static FreeArg FIRMWARE_VERSION = new FreeArg(StandardFreeArgType.STRING, LogUploaderContext.FIRMWARE_VERSION);
    public final static FreeArg CONTROLLER_ID = new FreeArg(StandardFreeArgType.STRING, LogUploaderContext.CONTROLLER_ID);
    public final static FreeArg CHANNEL_MAP_ID = new FreeArg(StandardFreeArgType.STRING, LogUploaderContext.CHANNEL_MAP_ID);
    public final static FreeArg VOD_ID = new FreeArg(StandardFreeArgType.STRING, LogUploaderContext.VOD_ID);
    public final static FreeArg UPLOAD_IMMEDIATELY = new FreeArg(StandardFreeArgType.STRING, LogUploaderContext.UPLOAD_IMMEDIATELY);
}

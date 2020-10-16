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

package com.comcast.xconf.aspect;

import com.comcast.hydra.astyanax.data.IPersistable;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Date;


@Aspect
public class UpdateDateAspect {

    @Before("execution(* setOne(..)) && args(.., objectToSave)")
    public void setUpdatedTime(IPersistable objectToSave) {
        objectToSave.setUpdated(getCurrentDate());
    }

    public static Date getCurrentDate() {
        OffsetDateTime utc = OffsetDateTime.now(ZoneOffset.UTC);
        return Date.from(utc.toInstant());
    }

}
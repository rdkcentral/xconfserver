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

package com.comcast.xconf.shared.utils;

import com.comcast.hydra.astyanax.data.IPersistable;
import com.comcast.xconf.change.Change;
import com.comcast.xconf.change.ChangeOperation;
import org.apache.commons.collections.comparators.NullComparator;

import java.util.Comparator;

public class ChangeUtils {

    public static <T extends IPersistable> T getEntity(Change<T> change) {
        if (ChangeOperation.CREATE.equals(change.getOperation()) || ChangeOperation.UPDATE.equals(change.getOperation())) {
            return change.getNewEntity();
        }
        return change.getOldEntity();
    }

    public static Comparator<Change> ascByDateComparator() {
        return new Comparator<Change>() {
            @Override
            public int compare(Change o1, Change o2) {
                return new NullComparator()
                        .compare(o2.getUpdated(), o1.getUpdated());
            }
        };
    }
}

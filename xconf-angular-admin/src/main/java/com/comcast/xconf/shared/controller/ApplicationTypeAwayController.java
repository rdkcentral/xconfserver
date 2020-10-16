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
 * Created: 11/9/2017
*/
package com.comcast.xconf.shared.controller;

import com.comcast.hydra.astyanax.data.IPersistable;
import com.comcast.xconf.Applicationable;
import com.comcast.xconf.admin.core.Utils;
import com.comcast.xconf.shared.service.AbstractApplicationTypeAwareService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Collections;
import java.util.List;

public abstract class ApplicationTypeAwayController<T extends IPersistable & Comparable & Applicationable> extends AbstractController<T> {

    @Override
    public ResponseEntity exportAll() {
        List<T> entities = getService().getAll();
        HttpHeaders headers = Utils.createContentDispositionHeader(getAllEntitiesExportName() + getService().getApplicationTypeSuffix());
        return new ResponseEntity<>(entities, headers, HttpStatus.OK);
    }

    @Override
    public ResponseEntity exportOne(@PathVariable String id) {
        T entity = getService().getOne(id);
        HttpHeaders headers = Utils.createContentDispositionHeader(getOneEntityExportName() + entity.getId() + getService().getApplicationTypeSuffix());
        return new ResponseEntity<>(Collections.singleton(entity), headers, HttpStatus.OK);
    }

    public abstract AbstractApplicationTypeAwareService<T> getService();
}

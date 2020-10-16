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
package com.comcast.hydra.astyanax.data;

import com.comcast.hydra.astyanax.util.ReflectionUtils;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.map.annotate.JsonSerialize;

import java.util.*;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
public abstract class Persistable implements IPersistable {

	protected String id;
	protected Date updated;

	@HColumn(excluded = true)
	protected Map<String, Integer> ttlMap = new HashMap<String, Integer>();

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	@Override
	public Date getUpdated() {
		return updated;
	}

	public void setUpdated(Date updated) {
		this.updated = updated;
	}

	@Excluded
	public Map<String, Integer> getTtlMap() {
		return ttlMap;
	}

	@Excluded
	public void setTtlMap(Map<String, Integer> map) {
		this.ttlMap = map;
	}

	@Excluded
	public int getTTL(String column) {
		return ttlMap.containsKey(column) ? ttlMap.get(column) : 0;
	}

	@Excluded
	public void setTTL(String column, int value) {
		ttlMap.put(column, value);
	}

	@Excluded
	public void setAllTTLs(int value) {
		List<String> fields = ReflectionUtils.getPersistableColumnNames(getClass());
		for(String field : fields) {
			setTTL(field, value);
		}
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + " id: " + String.valueOf(id);
	}

	public void clearTTL() {
		setAllTTLs(0);
	}
}

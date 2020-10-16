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
package com.comcast.xconf;

import com.comcast.apps.dataaccess.annotation.CF;
import com.comcast.hydra.astyanax.data.XMLPersistable;
import org.apache.commons.collections.comparators.NullComparator;

/**
 * Models an environment like PROD, QA, VBN, etc.
 * <p>
 * For this class the id is meaningful - something like PROD, DEV, QA, VBN, etc.
 * Probably don't need description, but whatever.
 */
@CF(cfName = CfNames.Common.ENVIRONMENT)
public class Environment extends XMLPersistable implements Comparable<Environment>, XEnvModel {

	private String description;

	public Environment() {
	}

	public Environment(String id, String description) {
		setId(id);
		this.description = description;
	}

	@Override
	public void setId(String id) {
		this.id = (id != null) ? id.trim().toUpperCase() : null;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	@Override
	public int compareTo(Environment o) {
		String name1 = (id != null) ? id.toLowerCase() : null;
		String name2 = (o != null && o.id != null) ? o.id.toLowerCase() : null;
		return new NullComparator().compare(name1, name2);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		Environment that = (Environment) o;

		return description != null ? description.equals(that.description) : that.description == null;
	}

	@Override
	public int hashCode() {
		return description != null ? description.hashCode() : 0;
	}

	@Override
	public String toString() {
		final StringBuffer sb = new StringBuffer("Environment{");
		sb.append("id='").append(id).append('\'');
		sb.append(", description='").append(description).append('\'');
		sb.append('}');
		return sb.toString();
	}
}

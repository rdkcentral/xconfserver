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
 * Author: Yury Stagit
 * Created: 06/11/16  12:00 PM
 */

package com.comcast.xconf.rfc;

import com.comcast.apps.dataaccess.annotation.CF;
import com.comcast.apps.hesperius.ruleengine.main.impl.Rule;
import com.comcast.hydra.astyanax.data.IPersistable;
import com.comcast.xconf.XRule;
import com.fasterxml.jackson.annotation.JsonIgnore;
import org.apache.commons.collections.comparators.NullComparator;

import javax.naming.OperationNotSupportedException;
import java.util.Date;

@CF(cfName = "FeatureControlRule", keyType = String.class)
public class FeatureRuleLegacy implements IPersistable, Comparable<FeatureRuleLegacy>, XRule {

    private String id;
    private String name;
    private Rule rule;
    private String boundFeatureSetId;

    @Override
    public Rule getRule() {
        return rule;
    }

    public void setRule(Rule rule) {
        this.rule = rule;
    }

    @Override
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    @JsonIgnore
    public String getTemplateId() throws OperationNotSupportedException {
        throw new OperationNotSupportedException();
    }

    @Override
    @JsonIgnore
    public String getRuleType() {
        return this.getClass().getSimpleName();
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public void setId(String id) {
        this.id = id;
    }

    @Override
    public Date getUpdated() {
        return null;
    }

    @Override
    @JsonIgnore
    public void setUpdated(Date date) {

    }

    @Override
    public int getTTL(String s) {
        return 0;
    }

    @Override
    @JsonIgnore
    public void setTTL(String s, int i) {

    }

    @Override
    @JsonIgnore
    public void clearTTL() {

    }

    public String getBoundFeatureSetId() {
        return boundFeatureSetId;
    }

    public void setBoundFeatureSetId(String boundFeatureSetId) {
        this.boundFeatureSetId = boundFeatureSetId;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (rule != null ? rule.hashCode() : 0);
        result = 31 * result + (id != null ? id.hashCode() : 0);
        result = 31 * result + (name != null ? name.hashCode() : 0);
        return result;
    }

    @Override
    public int compareTo(FeatureRuleLegacy o) {
        String id1 = (name != null) ? name.toLowerCase() : null;
        String id2 = (o != null && o.name != null) ? o.name.toLowerCase() : null;
        return new NullComparator().compare(id1, id2);
    }
}

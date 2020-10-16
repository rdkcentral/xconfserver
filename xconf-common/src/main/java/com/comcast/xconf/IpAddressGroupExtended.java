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
 * Author: obaturynskyi
 * Created: 04.07.2014  13:46
 */
package com.comcast.xconf;

import com.comcast.apps.hesperius.ruleengine.domain.additional.data.IpAddressGroup;
import com.comcast.apps.dataaccess.annotation.CF;
import com.comcast.hydra.astyanax.data.IPersistable;
import org.apache.commons.collections.comparators.NullComparator;

import java.util.Date;

@CF(cfName = CfNames.Common.IP_ADDRESS_GROUP)
public class IpAddressGroupExtended extends IpAddressGroup implements IPersistable, Comparable<IpAddressGroupExtended> {

    public IpAddressGroupExtended(){
    }

    public IpAddressGroupExtended(IpAddressGroup ipgrp){
        this.setId(ipgrp.getId());
        this.setIpAddresses(ipgrp.getIpAddresses());
        this.setName(ipgrp.getName());
    }

    @Override
    public Date getUpdated() {
        return null;
    }

    @Override
    public void setUpdated(Date date) {

    }

    @Override
    public int getTTL(String s) {
        return 0;
    }

    @Override
    public void setTTL(String s, int i) {

    }

    @Override
    public void clearTTL() {

    }

    @Override
    public int compareTo(IpAddressGroupExtended o) {
        String name1 = (getName() != null) ? getName().toLowerCase() : null;
        String name2 = (o != null && o.getName() != null) ? o.getName().toLowerCase() : null;
        return new NullComparator().compare(name1, name2);
    }
}


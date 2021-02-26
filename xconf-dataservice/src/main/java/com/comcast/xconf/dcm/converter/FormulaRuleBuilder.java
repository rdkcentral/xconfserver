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
 * Author: ikostrov
 * Created: 26.05.15 14:56
*/
package com.comcast.xconf.dcm.converter;

import com.comcast.apps.hesperius.ruleengine.domain.standard.StandardFreeArgType;
import com.comcast.apps.hesperius.ruleengine.domain.standard.StandardOperation;
import com.comcast.apps.hesperius.ruleengine.main.api.FixedArg;
import com.comcast.apps.hesperius.ruleengine.main.api.FreeArg;
import com.comcast.apps.hesperius.ruleengine.main.api.Operation;
import com.comcast.apps.hesperius.ruleengine.main.impl.Condition;
import com.comcast.apps.hesperius.ruleengine.main.impl.Rule;
import com.comcast.apps.dataaccess.cache.dao.CachedSimpleDao;
import com.comcast.xconf.IpAddressGroupExtended;
import com.comcast.xconf.dcm.core.Utils;
import com.comcast.xconf.dcm.manager.web.FormulaDataObject;
import com.comcast.xconf.estbfirmware.factory.RuleFactory;
import com.comcast.xconf.service.GenericNamespacedListLegacyService;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Set;

import static com.comcast.apps.hesperius.ruleengine.domain.standard.StandardOperation.IN;
import static com.comcast.xconf.estbfirmware.factory.RuleFactory.ENV;
import static com.comcast.xconf.estbfirmware.factory.RuleFactory.IN_LIST;

@Component
public class FormulaRuleBuilder {

    private static final String GETTER_PREFIX = "get";
    public static final String PROP_ESTB_IP = "estbIP";
    public static final String PROP_ESTB_MAC = "estbMacAddress";
    public static final String PROP_ECM_MAC = "ecmMacAddress";
    public static final String PROP_ENV = "env";

    @Autowired
    private CachedSimpleDao<String, IpAddressGroupExtended> ipAddressGroupDAO;

    @Autowired
    private GenericNamespacedListLegacyService genericNamespacedListLegacyService;

    public FormulaRuleBuilder() {
    }

    public Rule buildRule(FormulaDataObject dataObject, String property) {
        if (PROP_ESTB_IP.equals(property)) {
            return getEstbIpRule(dataObject);
        } else if (PROP_ESTB_MAC.equals(property)) {
            return getEstbMacRule(dataObject);
        } else if (PROP_ECM_MAC.equals(property)) {
            return getEcmMacRule(dataObject);
        } else if (PROP_ENV.equals(property)) {
            return getEnvRule(dataObject);
        } else {
            return getAnotherRule(dataObject, property);
        }
    }

    public Rule getEstbIpRule(FormulaDataObject dataObject) {
        assertBlankProperty(dataObject.getEstbIP(), PROP_ESTB_IP);
        String ipAddressGroup = getIpAddressGroupName(dataObject);
        FreeArg freeArg = new FreeArg(StandardFreeArgType.STRING, PROP_ESTB_IP);
        return Rule.Builder.of(new Condition(freeArg, IN_LIST, FixedArg.from(ipAddressGroup))).build();
    }

    private String getIpAddressGroupName(FormulaDataObject dataObject) {
        IpAddressGroupExtended ipAddressGroup = genericNamespacedListLegacyService.getIpAddressGroup(dataObject.getEstbIP());
        // try to read old ipAddressGroup CF
        if (ipAddressGroup == null) {
            ipAddressGroup = ipAddressGroupDAO.getOne(dataObject.getEstbIP());
        }
        return ipAddressGroup.getName();
    }

    public Rule getEstbMacRule(FormulaDataObject dataObject) {
        assertBlankProperty(dataObject.getEstbMacAddress(), PROP_ESTB_MAC);
        FreeArg freeArg = new FreeArg(StandardFreeArgType.STRING, PROP_ESTB_MAC);
        Condition condition = new Condition(freeArg, IN_LIST, FixedArg.from(dataObject.getEstbMacAddress()));
        return Rule.Builder.of(condition).build();
    }

    public Rule getEcmMacRule(FormulaDataObject dataObject) {
        assertBlankProperty(dataObject.getEcmMacAddress(), PROP_ECM_MAC);
        FreeArg freeArg = new FreeArg(StandardFreeArgType.STRING, PROP_ECM_MAC);
        Condition condition = new Condition(freeArg, IN_LIST, FixedArg.from(dataObject.getEcmMacAddress()));
        return Rule.Builder.of(condition).build();
    }

    public Rule getEnvRule(FormulaDataObject dataObject) {
        Condition condition = null;
        if (CollectionUtils.isNotEmpty(dataObject.getEnvList())) {
            condition = buildConditionFromCollection(ENV, dataObject.getEnvList());
        } else if (StringUtils.isNotBlank(dataObject.getEnv())) {
            Set<String> set = Utils.propertySplitter(dataObject.getEnv(), "( OR )");
            condition = buildConditionFromCollection(ENV, set);
        } else {
            throw new RuntimeException("Can't create env rule. env=" + dataObject.getEnv() + "; envList=" + dataObject.getEnvList());
        }
        return Rule.Builder.of(condition).build();
    }

    public Rule getAnotherRule(FormulaDataObject dataObject, String property) {
        String value = getFieldValue(dataObject, property);
        Set<String> set = Utils.propertySplitter(value, "( OR )");
        FreeArg freeArg = new FreeArg(StandardFreeArgType.STRING, property);
        Condition condition = buildConditionFromCollection(freeArg, set);
        return Rule.Builder.of(condition).build();
    }

    private String getFieldValue(FormulaDataObject dataObject, String property) {
        Object o = invokeGetter(dataObject, property);
        if (o instanceof String) {
            String value = (String) o;
            assertBlankProperty(value, property);
            return value;
        } else {
            throw new RuntimeException("Can't create rule. Blank property: " + property);
        }
    }

    public Condition buildConditionFromCollection(FreeArg freeArg, Collection fixedArgValues) {
        Condition condition = null;
        if (fixedArgValues.size() > 1) {
            condition = new Condition(freeArg, IN, FixedArg.from(fixedArgValues));
        } else {
            FixedArg fixedArg = FixedArg.from(CollectionUtils.get(fixedArgValues, 0));
            condition = new Condition(freeArg, getOperation((String) fixedArg.getValue()), fixedArg);
        }
        return condition;
    }

    private Operation getOperation(String fixedArgValue) {
        char[] searchedChars = {'*', '?'};
        if (StringUtils.containsAny(fixedArgValue, searchedChars)) {
            return RuleFactory.MATCH;
        } else {
            return StandardOperation.IS;
        }
    }

    private void assertBlankProperty(String value, String name) {
        if (StringUtils.isBlank(value)) {
            throw new RuntimeException("Can't create rule. Blank property: " + name);
        }
    }


    public static Method getGetter(Object obj, String field) throws NoSuchMethodException {
        return getMethod(obj, GETTER_PREFIX + StringUtils.capitalize(field));
    }

    public static Object invokeGetter(Object obj, String field) {
        try {
            Method m = getGetter(obj, field);
            m.setAccessible(true);
            return m.invoke(obj);
        }
        catch(NoSuchMethodException nme) { }
        catch(IllegalAccessException iae) { }
        catch(InvocationTargetException ite) { }

        return null;
    }

    public static Method getMethod(Object obj, String name, Class... parameterTypes) throws NoSuchMethodException {
        return obj.getClass().getMethod(name, parameterTypes);
    }

}

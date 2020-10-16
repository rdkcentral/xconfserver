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
 * Author: slavrenyuk
 * Created: 6/6/14
 */
package com.comcast.apps.hesperius.ruleengine.domain;

import com.comcast.apps.hesperius.ruleengine.domain.standard.BaseEvaluator;
import com.comcast.apps.hesperius.ruleengine.domain.standard.StandardOperation;
import com.comcast.apps.hesperius.ruleengine.main.api.FreeArgType;
import com.comcast.apps.hesperius.ruleengine.main.api.IEvaluators;
import com.comcast.apps.hesperius.ruleengine.main.api.Operation;
import com.comcast.apps.hesperius.ruleengine.main.impl.Evaluators;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.google.common.base.Charsets;
import com.google.common.base.Function;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.common.hash.Hashing;

import javax.xml.bind.DataBindingException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

public final class RuleUtils {

    public interface EngineConstants {
        String CONFIG_FILE_NAME = "rule-engine-config.json";
    }

    private static final Map<Operation, Function<Integer, Boolean>> OPERATION_AND_EVALUATION = ImmutableMap.<Operation, Function<Integer, Boolean>>builder()
            .put(StandardOperation.IS,  new Function<Integer, Boolean>() { public Boolean apply(Integer input) { return input == 0; }})
            .put(StandardOperation.GT,  new Function<Integer, Boolean>() { public Boolean apply(Integer input) { return input  > 0; }})
            .put(StandardOperation.GTE, new Function<Integer, Boolean>() { public Boolean apply(Integer input) { return input >= 0; }})
            .put(StandardOperation.LT,  new Function<Integer, Boolean>() { public Boolean apply(Integer input) { return input  < 0; }})
            .put(StandardOperation.LTE, new Function<Integer, Boolean>() { public Boolean apply(Integer input) { return input <= 0; }})
            .build();

    private static final ObjectMapper mapper = new ObjectMapper()
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
            .setSerializationInclusion(JsonInclude.Include.NON_NULL);

    public static ObjectMapper getObjectMapper() {
        return mapper;
    }

    /**
     * returns evaluators for IS, GT, GTE, LT, LTE operations
     */
    public static <T extends Comparable<T>> IEvaluators generateComparingEvaluators(FreeArgType freeArgType, Class<T> fixedArgClass,
                                                                                    Function<String, T> freeArgConverter) {
        IEvaluators result = new Evaluators();
        for (Map.Entry<Operation, Function<Integer, Boolean>> entry : OPERATION_AND_EVALUATION.entrySet()) {
            result.add(new ComparingEvaluator<T>(freeArgType, entry.getKey(), fixedArgClass, freeArgConverter, entry.getValue()));
        }
        return result;
    }

    public static boolean fitsPercent(final String str, double percent) {
        final double OFFSET = (double)Long.MAX_VALUE + 1;
        final double RANGE = (double)Long.MAX_VALUE * 2 + 1;
        double hashCode = (double)Hashing.sipHash24().hashString(str, Charsets.UTF_8).asLong() + OFFSET; // from 0 to (2 * Long.MAX_VALUE + 1)
        double limit = percent / 100 * RANGE;  // from 0 to (2 * Long.MAX_VALUE + 1)
        return (hashCode <= limit);
    }

    public static boolean fitsPercent(final Long l, double percent) {
        final double OFFSET = (double)Long.MAX_VALUE + 1;
        final double RANGE = (double)Long.MAX_VALUE * 2 + 1;
        double hashCode = (double)Hashing.sipHash24().hashLong(l).asLong() + OFFSET; // from 0 to (2 * Long.MAX_VALUE + 1)
        double limit = percent / 100 * RANGE;  // from 0 to (2 * Long.MAX_VALUE + 1)
        return (hashCode <= limit);
    }

    /**
     * Hashes given object by marshalling it to json string first and then hashing it
     * since apparently it is far cheaper then creating recursive typed funnel by reflection and
     * saves from need to implement lots of funnels just to do hashing
     * @param source
     * @return double hash code computed using sipHash24 function
     */
    public static <T> boolean fitsPercent(final T source, double percent) {
        return fitsPercent(toJSON(source), percent);
    }


    /**
     * Provides Jackson aided marshalling facility
     *
     * @param entity entity to marshall
     * @param <T>    inferred type parameter
     * @return string(json) marshaled representation of object supplied
     */
    public static <T> String toJSON(T entity) {
        final ObjectWriter jsonWriter = mapper.writer();
        try {
            return jsonWriter.writeValueAsString(entity);
        } catch (IOException e) {
            throw new DataBindingException(e);
        }
    }

    /**
     * Provides Jackson aided JSON unmarshalling facility
     *
     * @param clazz      class to unmarshall against
     * @param marshalled marshalled representation
     * @param <T>        inferred from class type
     * @return instance if given class
     */
    @SuppressWarnings("unchecked")
    public static <T> T fromJSON(Class<T> clazz, String marshalled) {
        if (null == marshalled || marshalled.isEmpty()) {
            return null;
        } else {
            final ObjectReader jsonReader = mapper.reader(clazz);
            try {
                return jsonReader.readValue(marshalled);
            } catch (IOException e) {
                throw new DataBindingException(e);
            }
        }
    }

    /**
     * Provides Jackson aided JSON unmarshalling facility
     *
     * @param clazz      class to unmarshall against
     * @param resource marshalled representation
     * @param <T>        inferred from class type
     * @return instance if given class
     */
    @SuppressWarnings("unchecked")
    public static <T> T fromJSON(Class<T> clazz, final InputStream resource) {
        if (null == resource) {
            return null;
        } else {
            final ObjectReader jsonReader = mapper.reader(clazz);
            try {
                return jsonReader.readValue(resource);
            } catch (IOException e) {
                throw new DataBindingException(e);
            }
        }
    }

    protected static class ComparingEvaluator<T extends Comparable<T>> extends BaseEvaluator {
        protected final Function<String, T> freeArgConverter;
        protected final Function<Integer, Boolean> evaluation;

        protected ComparingEvaluator(FreeArgType freeArgType, Operation operation, Class<T> fixedArgClass,
                                     Function<String, T> freeArgConverter, Function<Integer, Boolean> evaluation) {
            super(freeArgType, operation, fixedArgClass);
            this.freeArgConverter = freeArgConverter;
            this.evaluation = evaluation;
        }

        /**
         * for operation IS, GT, GTE, LT, LTE fixedArg type is expected to be the same as actual freeArg type, i.e. T
         * except VOID IS, where freeArgType = VOID, operation = IS, fixedArgClass = Boolean means "IS TRUE" or "IS FALSE"
         */
        @Override
        @SuppressWarnings("unchecked")
        protected boolean evaluateInternal(String freeArgValue, Object fixedArgValue) {
            T freeArgActualValue = freeArgConverter.apply(freeArgValue);
            if (freeArgActualValue == null) {
                return false;
            }
            final int comparisonResult;
            final Class<?> fixedArgClass = getFixedArgClasses().iterator().next();
            if (fixedArgClass == Long.class) {
                comparisonResult = RuleUtils.compare(freeArgActualValue, Long.valueOf(String.valueOf(fixedArgValue)));
            } else {
                comparisonResult = RuleUtils.compare(freeArgActualValue, (T) fixedArgValue);
            }
            return evaluation.apply(comparisonResult);
        }
    }

    private static final <T extends Comparable> int compare(T o1, T o2) {
        if (o1 == null && o2 != null) {
            return -1;
        } else if (o1 == null && o2 == null) {
            return 0;
        } else if (o1 != null && o2 == null) {
            return 1;
        }
        return o1.compareTo(o2);
    }

    public static final Map<String, String> normalizeContext(final Map<String, String> context) {
        return Maps.transformValues(context, new Function<String, String>() {
            @Override
            public String apply(String value) {
                return value != null ? value.trim() : null;
            }
        });
    }
}

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
 * Created: 6/11/14
 */
package com.comcast.apps.hesperius.ruleengine.main.api;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;

import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.WRAPPER_OBJECT)
@JsonSubTypes({
        @JsonSubTypes.Type(value = FixedArg.FromBean.class, name = "bean"),
        @JsonSubTypes.Type(value = FixedArg.FromCollection.class, name = "collection"),
        @JsonSubTypes.Type(value = FixedArg.FromMap.class, name = "map")
})
public abstract class FixedArg<T> {

    private FixedArg() {
    }

    public abstract T getValue();

    public abstract void setValue(T value);

    public static <T> FixedArg<T> from(T value) {
        return new FromBean<T>(value);
    }

    public static <E> FixedArg<Collection<E>> from(Collection<E> value) {
        return new FromCollection<E>(value);
    }

    public static <K, V> FixedArg<Map<K, V>> from(Map<K, V> value) {
        return new FromMap<K, V>(value);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof FixedArg)) return false;

        final FixedArg fixedArg = (FixedArg) o;

        return Objects.equals(this.getValue(), fixedArg.getValue());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getValue());
    }

    /**
     * @param <T> NOT A COLLECTION OR MAP!
     */
    public static class FromBean<T> extends FixedArg<T> {

        @JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.WRAPPER_OBJECT)
        @JsonSerialize(using = FromBeanValueSerializer.class)
        private T value;

        private FromBean() {
        }

        private FromBean(T value) {
            this.value = value;
        }

        @Override
        public T getValue() {
            return value;
        }

        @Override
        public void setValue(T value) {
            this.value = value;
        }
    }

    public static class FromCollection<E> extends FixedArg<Collection<E>> {

        @JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.WRAPPER_OBJECT)
        protected Collection<E> value;

        private FromCollection() {
        }

        private FromCollection(Collection<E> value) {
            this.value = value;
        }

        @Override
        public Collection<E> getValue() {
            return value;
        }

        @Override
        public void setValue(Collection<E> value) {
            this.value = value;
        }
    }

    public static class FromMap<K, V> extends FixedArg<Map<K, V>> {

        @JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.WRAPPER_OBJECT)
        protected Map<K, V> value;

        private FromMap() {
        }

        private FromMap(Map<K, V> value) {
            this.value = value;
        }

        @Override
        public Map<K, V> getValue() {
            return value;
        }

        @Override
        public void setValue(Map<K, V> value) {
            this.value = value;
        }
    }


    public static class FromBeanValueSerializer extends JsonSerializer<Object> {

        @Override
        public void serialize(Object value, JsonGenerator jsonGenerator, SerializerProvider serializerProvider)
                throws IOException {

            jsonGenerator.writeObject(value);
        }

        @Override
        public void serializeWithType(Object value, JsonGenerator jsonGenerator, SerializerProvider serializerProvider,
                TypeSerializer typeSerializer) throws IOException {

            typeSerializer.writeTypePrefixForScalar(value, jsonGenerator);
            serialize(value, jsonGenerator, serializerProvider);
            typeSerializer.writeTypeSuffixForScalar(value, jsonGenerator);
        }

    }

}

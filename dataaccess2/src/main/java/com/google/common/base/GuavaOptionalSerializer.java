/*
 * If not stated otherwise in this file or this component's Licenses.txt file the
 * following copyright and licenses apply:
 *
 * Copyright 2019 RDK Management
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
 * @author Philipp Bura
 */
package com.google.common.base;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;


public class GuavaOptionalSerializer extends Serializer<Optional<?>> {

    public static void registerSerializers(final Kryo kryo) {
        kryo.register(Present.class, new GuavaOptionalSerializer());
        kryo.register(Absent.class, new GuavaOptionalSerializer());
    }

    public GuavaOptionalSerializer() {
        super();

    }

    @Override
    public void write(Kryo kryo, Output output, Optional<?> object) {
        if (object.isPresent())
            kryo.writeClassAndObject(output, object.get());
        else kryo.writeClassAndObject(output, Optional.absent());
    }

    @Override
    public Optional<?> read(Kryo kryo, Input input, Class<Optional<?>> type) {
        if (type.isAssignableFrom(Absent.class)) {
            return Optional.absent();
        } else {
            return Optional.of(kryo.readObject(input, type));
        }
    }

    @Override
    public Optional<?> copy(Kryo kryo, Optional<?> original) {
        if(original.isPresent()){
            return Optional.of(kryo.copy(original.get()));
        } else return Optional.absent();
    }
}

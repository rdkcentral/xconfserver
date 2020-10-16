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
 * @author Igor Kostrov (ikostrov@productengine.com)
 */
package com.comcast.apps.dataaccess.util;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import com.google.common.reflect.ClassPath;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Opcodes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;


public class AnnotationScanner {

    private static final Logger logger = LoggerFactory.getLogger(AnnotationScanner.class);

    public static Set<Class<?>> getAnnotatedClasses(final Class[] annotations, final String... packages) {
        try {
            final Set<String> packageSet = Sets.newHashSet(packages);
            final Set<String> annotationSet = Sets.newHashSet(FluentIterable.from(Arrays.asList(annotations)).transform(new Function<Class, String>() {

                @Override
                public String apply(Class input) {
                    return input.getSimpleName();
                }
            }));

            final ClassLoader cl = AnnotationScanner.class.getClassLoader();
            final ClassPath cp = ClassPath.from(cl);
            return Sets.newHashSet(FluentIterable.from(
                    Iterables.concat(
                            FluentIterable.from(packageSet)
                                    .transform(new Function<String, Iterable<ClassPath.ClassInfo>>() {
                                        @Override
                                        public Iterable<ClassPath.ClassInfo> apply(String input) {
                                            return FluentIterable.from(cp.getTopLevelClassesRecursive(input))
                                                    .filter(new Predicate<ClassPath.ClassInfo>() {
                                                        @Override
                                                        public boolean apply(ClassPath.ClassInfo input) {
                                                            return Sets.intersection(annotationSet, getAnnotationNames(input.url())).size() > 0;
                                                        }
                                                    });
                                        }
                                    })
                    )
            )
                    .transform(new Function<ClassPath.ClassInfo, Class<?>>() {
                        @Override
                        public Class<?> apply(ClassPath.ClassInfo input) {
                            logger.info("attempting to load {} with {}", input.getName(), cl.getClass().getSimpleName());
                            return input.load();
                        }
                    }));
        } catch (IOException ex) {
            logger.error("Exception caught when trying to scan classpath {}", ex.getMessage());
        }
        return Collections.EMPTY_SET;
    }

    private static Set<String> getAnnotationNames(final URL classLocation) {
        final Set<String> annotations = new HashSet<>();
        try (InputStream classStream = classLocation.openStream()) {
            final ClassReader reader = new ClassReader(classStream);

            reader.accept(new ClassVisitor(Opcodes.ASM4) {
                @Override
                public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
                    if (visible) {
                        annotations.add(desc.substring(desc.lastIndexOf("/") + 1, desc.lastIndexOf(";")));
                    }
                    return null;
                }
            }, ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES | ClassReader.SKIP_CODE);
        } catch (IOException ex) {
            logger.warn("exception caught while trying to load class from {}", classLocation);
        }
        return annotations;
    }
}

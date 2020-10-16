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
package com.comcast.hydra.astyanax.util;

import com.comcast.hydra.astyanax.data.Excluded;
import com.comcast.hydra.astyanax.data.HColumn;
import com.comcast.hydra.astyanax.data.SaveComposite;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Transformer;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ReflectionUtils {

    private static final Logger log = LoggerFactory.getLogger(ReflectionUtils.class);
    private static final Map<String, List<String>> cachedMethodNames = new HashMap<String, List<String>>(); //ordered column names
    private static final Map<String, Integer> cachedFieldCounts = new HashMap<String, Integer>();

	public static String getMethodSimpleName(Method method) {
		return method.getName().substring(method.getName().lastIndexOf(".")+1);
	}

    private static List<ColumnInfo> getRawInfoFromNames(Class clazz, List<ColumnInfo> columnNames) {
        Class c = clazz;
        do {
            for (Field field : c.getDeclaredFields()) {
                // Do not process static fields
                if(Modifier.isStatic(field.getModifiers())) {
                    continue;
                }
                HColumn column = field.getAnnotation(HColumn.class);
                if (column == null) {
                    try {
                        //check method overriding
                        Method getter = new PropertyDescriptor(field.getName(), clazz).getReadMethod();
                        if (getter != null) {
                            column = getter.getAnnotation(HColumn.class);
                        }
                    } catch (IntrospectionException e) {
                        log.error(e.getMessage());
                        continue;
                    }
                }
                if (column != null && column.excluded()) {
                    if (log.isInfoEnabled()) {
                        if (column.order() != -1) {
                            log.info("Field {} defines both order and excluded attributes. Possible mistake in bean definition of class {}",
                                    field.getName(), field.getDeclaringClass().getSimpleName());
                        }
                    }
                    continue;
                }
                SaveComposite complex = field.getAnnotation(SaveComposite.class);
                if (complex != null)
                    columnNames = getRawInfoFromNames(field.getType(), columnNames);
                columnNames.add(new ColumnInfo(field, column));
            }
            c = c.getSuperclass();
        } while (c != null);
        if (columnNames.isEmpty()) {
            throw new IllegalArgumentException(String.format("Column list is empty for class: %s",clazz.getSimpleName()));
        }
        return columnNames;
    }

    public static List<ColumnInfo> getRawInfoFromNames(Class clazz) {
        List<ColumnInfo> columnNames = getRawInfoFromNames(clazz, new ArrayList<ColumnInfo>());
        Collections.sort(columnNames, new Comparator<ColumnInfo>() {
            @Override
            public int compare(ColumnInfo o1, ColumnInfo o2) {
                // sort by HColumn(order) and then alphabetically
                int order1 = getOrder(o1.columnAnnotation);
                int order2 = getOrder(o2.columnAnnotation);
                if (order1 != -1 && order2 != -1) {
                    if (order1 == order2 && !o1.field.getName().equals(o2.field.getName())) {
                        throw new IllegalArgumentException(String.format(
                                "Column order duplication detected in class: %s, for fields '%s', '%s'",
                                o1.field.getDeclaringClass().getSimpleName(), o1.field.getName(), o2.field.getName()));
                    }
                    return order1 > order2 ? 1 : -1;
                } else if (order1 == -1 && order2 == -1) {
                    // both field without @HColumn
                    return o1.field.getName().compareTo(o2.field.getName());
                } else {
                    // some of the field decelerated with column and another is not, field with @HColumn with higher priority
                    return order1 > order2 ? -1 : 1;
                }
            }

            int getOrder(HColumn column) {
                return column != null ? column.order() : -1;
            }
        });
        return columnNames;
    }

    /**
     * Tries to resolve column definition from annotation of the bean using reflection
     *
     * @param clazz
     * @return
     */
    public static List<String> getColumnNamesFromFields(Class clazz) {
        List<ColumnInfo> columnNames = getRawInfoFromNames(clazz);

        return new ArrayList<String>(CollectionUtils.collect(columnNames.iterator(), new Transformer() {
            @Override
            public Object transform(Object input) {
                return ((ColumnInfo) input).field.getName();
            }
        }));
    }

    //==========================   was moved here from BaseDAO   =======================================
    /**
     * @return The names of persistable fields of the given object.
     */
    public static String[] getColumnNames(Class<?> clazz) {
        String cacheKey = clazz.getName();
        if (cachedMethodNames.containsKey(cacheKey)) {
            List<String> list = cachedMethodNames.get(cacheKey);
            return list.toArray(new String[list.size()]);
        }

        List<String> columnNames = getAndCacheColumnNames(clazz, cacheKey);

        String[] result = new String[columnNames.size()];
        columnNames.toArray(result);
        return result;
    }

    public static List<String> getAndCacheColumnNames(Class<?> clazz, String cacheKey) {
        List<String> columnNames = getColumnNamesFromFields(clazz);
        setColumnNames(cacheKey, columnNames);
        return columnNames;
    }

    public static void setColumnNames(String cacheKey, List<String> columnNames) {
        cachedMethodNames.put(cacheKey, columnNames);
    }

    public static int getAndCacheFieldCount(Class<?> clazz) {
        int fieldCount = getColumnNames(clazz).length;
        setFieldCount(clazz.getName(), fieldCount);
        return fieldCount;
    }

    /**
     * Gets the count of columns that are needed to save an object of type T. This is a maximum, as fields with
     * null values are not saved.
     *
     * @return The maximum number of columns needed to save an object of type T.
     */
    public static int getFieldCount(Class<?> clazz) {
        String className = clazz.getName();
        if (cachedFieldCounts.containsKey(className))
            return cachedFieldCounts.get(className);

        return getAndCacheFieldCount(clazz);
    }

    public static void setFieldCount(String className, int fieldCount) {
        cachedFieldCounts.put(className, fieldCount);
    }

    //===================== is used only in com.comcast.hydra.astyanax.data.Persistable=============================
    @Deprecated
    public static List<String> getPersistableColumnNames(Class<?> clazz) {
        List<String> columnNames = new ArrayList<String>();
        for(Method method : clazz.getMethods()) {
            if(methodHasAnnotation(method, Excluded.class))
                continue;
            String name = getMethodSimpleName(method);
            if(name.startsWith("get")) {
                columnNames.add(getterToFieldName(name));
            }
        }
        Collections.sort(columnNames);
        return columnNames;
    }

    public static boolean methodHasAnnotation(Method method, Class annotation) {
        for(Annotation a : method.getAnnotations()) {
            if(a.annotationType().equals(annotation)) {
                return true;
            }
        }
        return false;
    }

    public static String getterToFieldName(String getterName) {
        return StringUtils.uncapitalize(getterName.substring(3));
    }
    //===========================================================================================

    public static class ColumnInfo {

        public Field field;
        public HColumn columnAnnotation;

        ColumnInfo(Field field, HColumn column) {
            this.field = field;
            this.columnAnnotation = column;
        }

    }
}

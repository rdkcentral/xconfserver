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
package com.comcast.xconf.dcm.core;

import com.comcast.apps.dataaccess.cache.dao.CachedSimpleDao;
import com.comcast.apps.dataaccess.util.CloneUtil;
import com.comcast.apps.dataaccess.util.JsonUtil;
import com.comcast.hydra.astyanax.data.IPersistable;
import com.comcast.xconf.logupload.UploadProtocol;
import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.base.Optional;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.Iterables;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.validator.routines.UrlValidator;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.type.TypeFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class Utils {

    private static final Logger log = LoggerFactory
            .getLogger(Utils.class);

    public static Set<String> propertySplitter(String inputProperty, String regex) {
        if (inputProperty == null || inputProperty.isEmpty()) return null;
        String [] splittedProperty = inputProperty.split(regex);
        Set<String> resultedSet = new HashSet<String>();
        for (String item : splittedProperty) {
            String itemTrimmed = item.trim();
            if (!itemTrimmed.isEmpty()) {
                resultedSet.add(itemTrimmed);
            }
        }
        return resultedSet.size()>0 ? resultedSet : null;
    }

    public static String joinOr(final Iterable<String> parts) {
        return parts != null ? Joiner.on(" OR ").skipNulls().join(parts) : null;
    }

    public static final String DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss";

    public static boolean isValidDate(String time) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat(DATE_TIME_FORMAT);
            // turn it off to make date validation more strictly
            sdf.setLenient(false);
            sdf.parse(time);
            return true;
        }
        catch (Exception e1) {
            return false;
        }
    }

    /**
     * Compares dates. Check if dates are valid before using this method. If any isn't valid then 0 will be returned.
     * @param startDate start date
     * @param endDate end date
     * @return -1, 1 or 0
     */
    public static int compareDates(String startDate, String endDate) {
        SimpleDateFormat sdf = new SimpleDateFormat(DATE_TIME_FORMAT);

        try {
            Date start = sdf.parse(startDate);
            Date end = sdf.parse(endDate);
            return start.compareTo(end);
        } catch (ParseException e) {
            log.error("Wrong data format", e);
        }
        return 0;
    }

    public static String converterDateTimeToUTC(String time, String sourceTZ) {
        SimpleDateFormat sdf = new SimpleDateFormat(DATE_TIME_FORMAT);

        Date specifiedTime;
        try {
            if (sourceTZ != null)
                sdf.setTimeZone(TimeZone.getTimeZone(sourceTZ));
            else
                sdf.setTimeZone(TimeZone.getDefault()); // default to server's timezone
            specifiedTime = sdf.parse(time);
        }
        catch (Exception e1) {
            return time;
        }

        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));

        return sdf.format(specifiedTime);
    }

    public static Object invokeGetter(Object obj, Method method) {
        Object returnedValue = null;
        try {
            returnedValue = method.invoke(obj);
        }  catch (IllegalAccessException e) {
            log.error("Illegal access exception for method: "+method.getName(),e);
        } catch (InvocationTargetException e) {
            log.error("Invocation target exception for method: "+method.getName(),e);
        }
        return returnedValue;
    }


    @SuppressWarnings({ "rawtypes", "unchecked" })
	public static Method getGetter(Class clazz, String field) {
        Method method =null;
        try {
            method = clazz.getMethod("get" + StringUtils.capitalize(field));
        } catch (NoSuchMethodException e) {
            log.error("No such method: get"+StringUtils.capitalize(field)+"() ",e);
        }
        return method;
    }


    public static Map<String, String> combineListsIntoMap (List<String> listKeys, List<String> listValues) {
        Map<String, String> resultMap = new HashMap<String, String>();
        if ( listKeys == null || listValues == null || (listKeys.size() != listValues.size())) {
            return resultMap;
        }

        for (int i = 0; i< listKeys.size(); i++) {
            resultMap.put(listKeys.get(i),listValues.get(i));
        }
        return resultMap;
    }

    public static void mapToLists (Map<String, String> map,List<String> listKeys, List<String> listValues) {
        if (map == null || listKeys == null || listValues == null ) return;
        listKeys.addAll(map.keySet());
        listValues.addAll(map.values());
    }

    public static String serializeMapToJsonString (Map<String, String> map) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.writeValueAsString(map);
        } catch (IOException e) {
            log.error("Unable to serialize map to JSON string: "+map.toString(), e);
        }
        return null;
    }

    public static Map<String, String> deserializeMapFromJsonString (String jsonStr) {
        if (jsonStr == null || jsonStr.isEmpty()) return null;
        ObjectMapper mapper = new ObjectMapper();
        try {
            Map<String, String> result = mapper.readValue(jsonStr, TypeFactory.mapType(HashMap.class, String.class, String.class));
            return result;
        } catch (IOException e) {
            log.error("Unable to deserialize map from JSON string: "+jsonStr, e);
        }
        return null;
    }

    // TODO: more elegant solution
    public static <T extends IPersistable> void updateMapEntityIds(Map<String, T> mapToUpdate) {
        for (Map.Entry<String, T> entry : mapToUpdate.entrySet()) {
            entry.getValue().setId(entry.getKey());
        }
    }

    public static <K, T extends IPersistable> List<T> getPage(
            final CachedSimpleDao<K, T> dao, final int page, final int pageSize) {

        Set<T> set = new TreeSet<T>();
        Iterables.addAll(set, Optional.presentInstances(dao.asLoadingCache().asMap().values()));

        final List<T> result = new ArrayList<T>();
        final int size = page * pageSize;
        final int startIndex = size - pageSize;

        int i = 0;
        for (T value : set) {
            if (i++ < startIndex) {
                continue;
            }
            result.add(CloneUtil.clone(value));
            if (result.size() == pageSize) {
                break;
            }
        }

        return result;
    }

    public static <K, T extends IPersistable> int getNumberOfPages(
            final CachedSimpleDao<K, T> dao, final int pageSize) {
        return (int) Math.ceil(
                (double) Iterables.size(Optional.presentInstances(dao.asLoadingCache().asMap().values())) / pageSize
        );
    }

    public static final UrlValidator urlValidator;
    static {
        List<String> schemas = new ArrayList<String>();
        for (UploadProtocol protocol : UploadProtocol.values()) {
            schemas.add(protocol.name().toLowerCase());
        }
        urlValidator = new UrlValidator(schemas.toArray(new String[schemas.size()]));
    }

    public static boolean isValidUrl(String url) {
        return urlValidator.isValid(url);
    }

    public static boolean isValidUrl(UploadProtocol protocol, String host) {
        String url = (host.contains("://") || protocol == null) ? host : (protocol.toString().toLowerCase() + "://" + host);
        return isValidUrl(url);
    }

    public static Set<String> collectionToUpperCase(Collection<String> data) {
        return FluentIterable.from(data).transform(new Function<String, String>() {
            @Nullable
            @Override
            public String apply(@Nullable String input) {
                return input.toUpperCase();
            }
        }).toSet();
    }
}

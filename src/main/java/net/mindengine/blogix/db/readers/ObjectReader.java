/*******************************************************************************
* Copyright 2013 Ivan Shubin http://mindengine.net
* 
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
* 
*   http://www.apache.org/licenses/LICENSE-2.0
* 
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
******************************************************************************/
package net.mindengine.blogix.db.readers;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import net.mindengine.blogix.db.Entry;

import org.apache.commons.lang3.StringUtils;

public class ObjectReader<T> implements Reader<T> {

    private Map<String, Field> objectFields = null;
    private EntryReader entryConverter;
    private Class<T> objectType;
    
    public ObjectReader(Class<T> objectType, EntryReader entryConverter) {
        this.objectType = objectType;
        this.entryConverter = entryConverter;
    }

    @Override
    public T convert(String fileName) {
        Entry entry = entryConverter.convert(fileName);
        return convert(entry);
    }
    
    public T convert(Entry entry) {
        try {
            Constructor<T> constructor = objectType.getConstructor();
            T objectInstance = constructor.newInstance();
            
            Set<String> allEntryFields = entry.getAllFieldNames();
            
            for (String entryFieldName : allEntryFields) {
                resolveField(entryFieldName, entry, objectInstance);
            }
            
            Field field = getObjectFields().get("id");
            if (field != null) {
                setFieldValue(objectInstance, field, entry.id());
            }
            
            setEntryItselfIntoObject(entry, objectInstance);
            return objectInstance;
        } catch (Exception e) {
            throw new RuntimeException("Cannot bind entries to object " + objectType.getClass(), e);
        }
    }

    private void setEntryItselfIntoObject(Entry entry, T objectInstance) throws IllegalArgumentException, IllegalAccessException {
        Field entryField = getObjectFields().get("entry");
        if (entryField != null) {
            if (entryField.getType().equals(Entry.class)) {
                entryField.setAccessible(true);
                entryField.set(objectInstance, entry);
            }
        }
    }

    private void resolveField(String entryFieldName, Entry entry, T objectInstance) {
        Field field = getObjectFields().get(entryFieldName);
        if (field != null) {
            setFieldValue(objectInstance, field, entry.field(entryFieldName));
        }
    }

    private Map<String, Field> getObjectFields() {
        if (objectFields == null) {
            objectFields = getAllFieldsOfObjectType(objectType);
        }
        return objectFields;
    }

    private void setFieldValue(T objectInstance, Field field, String fieldValue) {
        Object convertedFieldValue = convertFieldValue(fieldValue, field);
        field.setAccessible(true);
        
        try {
            field.set(objectInstance, convertedFieldValue);
        } catch (Exception e) {
            throw new RuntimeException("Cannot set value to field " + field.toString());
        }
    }

    private Object convertFieldValue(String fieldValue, Field field) {
        if (fieldValue == null) {
            return null;
        }
        Class<?> type = field.getType();
        try {
            return convertStringValueToType(fieldValue, type);
        }
        catch (Exception e) {
            throw new IllegalArgumentException("Cannot convert value '" + StringUtils.abbreviate(fieldValue, 20) + "' to field " + field.toString(), e);
        }
    }

    private Object convertStringValueToType(String fieldValue, Class<?> type) {
        if (type.equals(String.class)) {
            return fieldValue;
        }
        else if (type.equals(Integer.class) || type.equals(int.class)) {
            return Integer.parseInt(fieldValue.trim());
        }
        else if (type.equals(Long.class) || type.equals(long.class)) {
            return Long.parseLong(fieldValue.trim());
        }
        else if (type.equals(Float.class) || type.equals(float.class)) {
            return Float.parseFloat(fieldValue.trim());
        }
        else if (type.equals(Double.class) || type.equals(double.class)) {
            return Double.parseDouble(fieldValue.trim());
        }
        else if (type.equals(Boolean.class) || type.equals(boolean.class)) {
            return Boolean.parseBoolean(fieldValue.trim());
        }
        else if (type.equals(Date.class)) {
            return parseDate(fieldValue);
        }
        else if (type.equals(String[].class)) {
            String[] items = fieldValue.split(",");
            String[] array = new String[items.length];
            for( int i=0; i<array.length; i++) {
                array[i] = (String) convertStringValueToType(items[i].trim(), String.class);
            }
            return array;
        }
        
        throw new IllegalArgumentException("Cannot convert value '" + StringUtils.abbreviate(fieldValue, 20) + "' to type " + type);
    }

    private Date parseDate(String fieldValue) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyy-MM-dd hh:mm");
        try {
            return sdf.parse(fieldValue.trim());
        } catch (ParseException e) {
            throw new RuntimeException("Error parsing date: " + fieldValue, e);
        }
    }

    private Map<String, Field> getAllFieldsOfObjectType(Class<?> objectType) {
        Map<String, Field> fields = convertOnlyNonStaticFieldsToMap(objectType.getDeclaredFields());
        
        Class<?> superClass = objectType.getSuperclass();
        if (superClass!=null) {
            fields.putAll(getAllFieldsOfObjectType(superClass));
        }
        return fields;
    }

    private Map<String, Field> convertOnlyNonStaticFieldsToMap(Field[] declaredFields) {
        Map<String, Field> fields = new HashMap<String, Field>();
        
        for (Field field : declaredFields) {
            if (!Modifier.isStatic(field.getModifiers())) {
                fields.put(field.getName(), field);
            }
        }
        return fields;
    }
}

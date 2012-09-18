package net.mindengine.blogix.db;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;


public class FileDb<T1> {

    public static final String ENTRY_SUFFIX = ".blogix";
    private File directory;
    private Class<T1> objectType;
    
    public FileDb(Class<T1> objectType, File directory) {
        this.objectType = objectType;
        this.directory = directory;
        if (!this.directory.exists() || !this.directory.isDirectory()) {
            throw new IllegalArgumentException(directory.getAbsolutePath() + " does not exist or is not a directory");
        }
    }

    public Entry findEntryById(String id) {
        return findByIdAndConvert(id, _entryConverter);
    }
    
    public T1 findById(String id) {
        return findByIdAndConvert(id, _objectConverter);
    }
    
    private <T> T findByIdAndConvert(String id, Converter<T> converter) {
        String finalId = id + ENTRY_SUFFIX;
        String[] fileNames = directory.list();
        for (String fileName : fileNames) {
            if (fileName.equals(finalId)) {
                return converter.convert(fileName);
            }
        }
        return null;
    }

    private interface Converter<T> {
        T convert(String fileName);
    }
    
    private final Converter<String> _idConverter = new Converter<String>() {
        @Override
        public String convert(String fileName) {
            return FileDb.this.extractPureIdWithoutSuffix(fileName);
        }
    };
    
    private final Converter<Entry> _entryConverter = new Converter<Entry>() {
        @Override
        public Entry convert(String fileName) {
            return FileDb.this.createEntry(fileName);
        }
    };
    
    private final Converter<T1> _objectConverter = new Converter<T1>() {
        private Map<String, Field> objectFields = null;
        
        @Override
        public T1 convert(String fileName) {
            Entry entry = _entryConverter.convert(fileName);
            try {
                Constructor<T1> constructor = FileDb.this.objectType.getConstructor();
                T1 objectInstance = constructor.newInstance();
                
                Set<String> allEntryFields = entry.getAllFieldNames();
                
                for (String entryFieldName : allEntryFields) {
                    resolveField(entryFieldName, entry, objectInstance);
                }
                
                Field field = getObjectFields().get("id");
                if (field != null) {
                    setFieldValue(objectInstance, field, entry.id());
                }
                
                return objectInstance;
            } catch (Exception e) {
                throw new RuntimeException("Cannot bind entries to object " + objectType.getClass(), e);
            }
        }

        private void resolveField(String entryFieldName, Entry entry, T1 objectInstance) {
            Field field = getObjectFields().get(entryFieldName);
            if (field != null) {
                setFieldValue(objectInstance, field, entry.field(entryFieldName));
            }
        }

        private Map<String, Field> getObjectFields() {
            if (objectFields == null) {
                objectFields = getAllFieldsOfObjectType(FileDb.this.objectType);
            }
            return objectFields;
        }

        private void setFieldValue(T1 objectInstance, Field field, String fieldValue) {
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
            else if (type.equals(Integer.class)) {
                return Integer.parseInt(fieldValue.trim());
            }
            else if (type.equals(Long.class)) {
                return Long.parseLong(fieldValue.trim());
            }
            else if (type.equals(Float.class)) {
                return Float.parseFloat(fieldValue.trim());
            }
            else if (type.equals(Double.class)) {
                return Double.parseDouble(fieldValue.trim());
            }
            else if (type.equals(Boolean.class)) {
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
            SimpleDateFormat sdf = new SimpleDateFormat("yyy-MM-dd hh:mm:ss");
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
    };
    
    public List<String> findAllIds() {
        return findAllByPattern(null, _idConverter);
    }
    
    public List<String> findAllIds(String patternText) {
        return findAllByPattern(Pattern.compile(patternText), _idConverter);
    }
    
    public List<Entry> findAllEntries() {
        return findAllByPattern(null, _entryConverter);
    }
    
    public List<Entry> findAllEntries(String patternText) {
        return findAllByPattern(Pattern.compile(patternText), _entryConverter);
    }
    
    public List<Entry> findEntriesByFieldContaining(String fieldName, String containingText) {
        List<Entry> entries = findAllEntries();
        List<Entry> fetched = new LinkedList<Entry>();
        for (Entry entry : entries) {
            String fieldValue = entry.field(fieldName);
            if (fieldValue != null && fieldValue.contains(containingText)) {
                fetched.add(entry);
            }
        }
        return fetched;
    }

    public List<String> findAttachments(final String id) {
        List<String> entries = new LinkedList<String>();
        String[] fileNames = directory.list();
        for (String fileName : fileNames) {
            if (!fileName.endsWith(ENTRY_SUFFIX)) {
                if (fileName.startsWith(id)) {
                    entries.add(fileName);
                }
            }
        }
        return entries;
    }

    public List<T1> findAll() {
        return findAllByPattern(null, _objectConverter);
    }
    
    

    private <T> List<T> findAllByPattern(Pattern pattern, Converter<T> converter) {
        List<T> entries = new LinkedList<T>();
        String[] fileNames = directory.list();
        boolean checkPass;
        for (String fileName : fileNames) {
            checkPass = true;
            if (fileName.endsWith(ENTRY_SUFFIX)) {
                if (pattern != null) {
                    Matcher matcher = pattern.matcher(fileName);
                    if (!matcher.matches()) {
                        checkPass = false;
                    }
                }
                if (checkPass) {
                    T converted = converter.convert(fileName);
                    if (converted != null) {
                        entries.add(converted);
                    }
                }
            }
        }
        return entries;
    }

    
    private String extractPureIdWithoutSuffix(String fileName) {
        return fileName.substring(0, fileName.length() - ENTRY_SUFFIX.length());
    }
    
    private Entry createEntry(String fileName) {
        File file = new File(directory.getAbsolutePath() + File.separator + fileName);
        return new Entry(file, extractPureIdWithoutSuffix(fileName));
    }

    

}

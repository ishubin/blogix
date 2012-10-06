package net.mindengine.blogix.db;

import java.io.File;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.mindengine.blogix.db.readers.EntryReader;
import net.mindengine.blogix.db.readers.IdReader;
import net.mindengine.blogix.db.readers.ObjectReader;
import net.mindengine.blogix.db.readers.Reader;


public class FileDb<T1 extends Comparable<T1>> {

    public static final String ENTRY_SUFFIX = ".blogix";
    private File directory;
    
    private final IdReader idConverter;
    private final EntryReader entryConverter;
    private final ObjectReader<T1> _objectConverter;
    
    public FileDb(Class<T1> objectType, File directory) {
        this.directory = directory;
        if (!this.directory.exists() || !this.directory.isDirectory()) {
            throw new IllegalArgumentException(directory.getAbsolutePath() + " does not exist or is not a directory");
        }
        idConverter = new IdReader(ENTRY_SUFFIX);
        entryConverter = new EntryReader(directory, ENTRY_SUFFIX);
        _objectConverter = new ObjectReader<T1>(objectType, entryConverter);
    }

    public Entry findEntryById(String id) {
        return findByIdAndConvert(id, entryConverter);
    }
    
    public T1 findById(String id) {
        return findByIdAndConvert(id, _objectConverter);
    }
    
    public EntryList<T1> findByFieldContaining(String fieldName, String value) {
        List<Entry> entries = findEntriesByFieldContaining(fieldName, value);
        return new EntryList<T1>(convertEntriesToObjects(entries));
    }

    private List<T1> convertEntriesToObjects(List<Entry> entries) {
        List<T1> objects = new LinkedList<T1>();
        for (Entry entry : entries) {
            objects.add(_objectConverter.convert(entry));
        }
        return objects;
    }

    private <T> T findByIdAndConvert(String id, Reader<T> converter) {
        String finalId = id + ENTRY_SUFFIX;
        String[] fileNames = directory.list();
        for (String fileName : fileNames) {
            if (fileName.equals(finalId)) {
                return converter.convert(fileName);
            }
        }
        return null;
    }
        
    public EntryList<String> findAllIds() {
        return findAllByPattern(null, idConverter);
    }
    
    public EntryList<String> findAllIds(String patternText) {
        return findAllByPattern(Pattern.compile(patternText), idConverter);
    }
    
    public EntryList<Entry> findAllEntries() {
        return findAllByPattern(null, entryConverter);
    }
    
    public EntryList<Entry> findAllEntries(String patternText) {
        return findAllByPattern(Pattern.compile(patternText), entryConverter);
    }
    
    public List<Entry> findEntriesByFieldContaining(String fieldName, String containingText) {
        EntryList<Entry> entries = findAllEntries();
        List<Entry> fetched = new LinkedList<Entry>();
        for (Entry entry : entries) {
            String fieldValue = entry.field(fieldName);
            if (fieldValue != null && fieldValue.contains(containingText)) {
                fetched.add(entry);
            }
        }
        return fetched;
    }

    public EntryList<String> findAttachments(final String id) {
        List<String> entries = new LinkedList<String>();
        String[] fileNames = directory.list();
        for (String fileName : fileNames) {
            if (!fileName.endsWith(ENTRY_SUFFIX)) {
                if (fileName.startsWith(id)) {
                    entries.add(fileName);
                }
            }
        }
        return new EntryList<String>(entries);
    }

    public EntryList<T1> findAll() {
        return findAllByPattern(null, _objectConverter);
    }
    
    

    private <T extends Comparable<T>> EntryList<T> findAllByPattern(Pattern pattern, Reader<T> converter) {
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
        return new EntryList<T>(entries);
    }

    
    
    
    


}

package net.mindengine.blogix.db;

import java.io.File;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.common.base.Predicates;


public class FileDb {

    public static final String ENTRY_SUFFIX = ".blogix";
    private File directory;

    public FileDb(File directory) {
        this.directory = directory;
        if (!this.directory.exists() || !this.directory.isDirectory()) {
            throw new IllegalArgumentException(directory.getAbsolutePath() + " does not exist or is not a directory");
        }
    }

    public Entry findById(String id) {
        String finalId = id + ENTRY_SUFFIX;
        String[] fileNames = directory.list();
        for (String fileName : fileNames) {
            if (fileName.equals(finalId)) {
                return createEntry(fileName);
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
    
    public List<String> findAllIds() {
        return findAllByPattern(null, _idConverter);
    }
    
    public List<String> findAllIds(String patternText) {
        return findAllByPattern(Pattern.compile(patternText), _idConverter);
    }
    
    public List<Entry> findAll() {
        return findAllByPattern(null, _entryConverter);
    }
    
    public List<Entry> findAll(String patternText) {
        return findAllByPattern(Pattern.compile(patternText), _entryConverter);
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

    public List<Entry> findByFieldContaining(String fieldName, String containingText) {
        List<Entry> entries = findAll();
        List<Entry> fetched = new LinkedList<Entry>();
        for (Entry entry : entries) {
            String fieldValue = entry.field(fieldName);
            if (fieldValue != null && fieldValue.contains(containingText)) {
                fetched.add(entry);
            }
        }
        return fetched;
    }
}

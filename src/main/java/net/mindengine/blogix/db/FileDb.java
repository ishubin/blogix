/*******************************************************************************
* Copyright 2012 Ivan Shubin http://mindengine.net
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
package net.mindengine.blogix.db;

import java.io.File;
import java.io.FileNotFoundException;
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
        
        if (id.endsWith("/")) {
            id = trimSlashAtTheEnd(id);
        }
        String finalId = id + ENTRY_SUFFIX;
        List<String> fileNames = allFilesInDirectoryIncludingSubfolders(directory, "");
        for (String fileName : fileNames) {
            if (fileName.equals(finalId)) {
                return converter.convert(fileName);
            }
        }
        return null;
    }
        
    private String trimSlashAtTheEnd(String id) {
        return id.substring(0, id.length() - 1);
    }

    /**
     * Scans recursively the specified directory and returns paths to all files.
     * @param directory
     * @param parentPath
     * @return
     */
    private List<String> allFilesInDirectoryIncludingSubfolders(File directory, String parentPath) {
        List<String> paths = new LinkedList<String>();
        
        File[] files = directory.listFiles();
        for (File file : files) {
            if (file.isFile()) {
                paths.add(parentPath + file.getName());
            }
            else if (file.isDirectory()) {
                paths.addAll(allFilesInDirectoryIncludingSubfolders(file, parentPath + file.getName() + File.separator));
            }
        }
        return paths;
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
        List<String> fileNames = allFilesInDirectoryIncludingSubfolders(directory, "");
        for (String fileName : fileNames) {
            if (!fileName.endsWith(ENTRY_SUFFIX)) {
                if (fileName.startsWith(id)) {
                    entries.add(trimEntryIdFromAttachment(id, fileName));
                }
            }
        }
        return new EntryList<String>(entries);
    }

    private String trimEntryIdFromAttachment(String id, String fileName) {
        return fileName.substring(id.length() + 1);
    }

    public EntryList<T1> findAll() {
        return findAllByPattern(null, _objectConverter);
    }
    
    

    private <T extends Comparable<T>> EntryList<T> findAllByPattern(Pattern pattern, Reader<T> converter) {
        List<T> entries = new LinkedList<T>();
        List<String> fileNames = allFilesInDirectoryIncludingSubfolders(directory, "");
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

    public File findAttachmentAsFile(String id, String fileName) throws FileNotFoundException {
        String fullAttachmentName = id + "." + fileName;
        File file = new File(directory.getAbsolutePath() + File.separator + fullAttachmentName);
        if (file.exists()) {
            return file;
        }
        else throw new FileNotFoundException(fullAttachmentName);
    }

    
    
    
    


}

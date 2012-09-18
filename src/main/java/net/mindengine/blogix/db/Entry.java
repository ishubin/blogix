package net.mindengine.blogix.db;

import java.io.File;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

public class Entry {

    private File file;
    private String id;
    
    private Map<String, String> data = null;

    public Entry(File file, String id) {
        this.file = file;
        this.id = id;
    }

    public String id() {
        return id;
    }

    public String field(String name) {
        return findData().get(name);
    }

    private Map<String, String> findData() {
        if (data == null) {
            try {
                data = new DbEntryParser(file).load();
            } catch (Exception e) {
                throw new RuntimeException("Error while parsing db entry in " + file.getAbsolutePath(), e);
            }
        }
        return data;
    }

    public String body() {
        return findData().get("body");
    }

    public Set<String> getAllFieldNames() {
        Map<String, String> data = findData();
        if (data != null) {
            return data.keySet();
        }
        else {
            return Collections.emptySet();
        }
    }

}

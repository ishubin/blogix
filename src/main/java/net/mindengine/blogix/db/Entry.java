package net.mindengine.blogix.db;

import java.io.File;
import java.util.Map;

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

}

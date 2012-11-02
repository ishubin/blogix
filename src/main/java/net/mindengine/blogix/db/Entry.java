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
import java.util.Collections;
import java.util.Map;
import java.util.Set;

public class Entry implements Comparable<Entry> {

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

    @Override
    public int compareTo(Entry otherParam) {
        return id.compareTo(otherParam.id);
    }

}

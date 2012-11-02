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
package net.mindengine.blogix.db.readers;

import java.io.File;

import net.mindengine.blogix.db.Entry;

public class EntryReader implements Reader<Entry> {
    private File directory;
    private String entrySuffix;
    public EntryReader(File directory, String entrySuffix) {
        this.directory = directory;
        this.entrySuffix = entrySuffix;
    }

    @Override
    public Entry convert(String fileName) {
        File file = new File(directory.getAbsolutePath() + File.separator + fileName);
        return new Entry(file, ReaderUtils.extractEntryIdFromFileName(fileName, entrySuffix));
    }
    
}

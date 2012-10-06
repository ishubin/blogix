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

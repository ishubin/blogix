package net.mindengine.blogix.db.readers;

public class IdReader implements Reader<String> {
    private String entrySuffix;

    public IdReader(String entrySuffix) {
        super();
        this.entrySuffix = entrySuffix;
    }

    @Override
    public String convert(String fileName) {
        return ReaderUtils.extractEntryIdFromFileName(fileName, entrySuffix);
    }
    
}

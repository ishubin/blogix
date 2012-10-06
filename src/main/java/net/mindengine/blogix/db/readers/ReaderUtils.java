package net.mindengine.blogix.db.readers;

public class ReaderUtils {

    public static String extractEntryIdFromFileName(String fileName, String entrySuffix) {
        return fileName.substring(0, fileName.length() - entrySuffix.length());
    }

}

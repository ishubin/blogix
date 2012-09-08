package net.mindengine.blogix.utils;

import java.io.File;
import java.io.FileNotFoundException;

public class BlogixFileUtils {

    /**
     * Search for a file first in a root folder of a project, then in project resources
     * @param path
     * @return
     * @throws FileNotFoundException 
     */
    public static File findFile(String path) throws FileNotFoundException {
        try {
            File file = new File(path);
            if ( !file.exists() ) {
                file = new File(BlogixFileUtils.class.getResource("/" + path).toURI());
            }
            return file;
        }
        catch (Exception e) {
            throw new FileNotFoundException(path);
        }
    }

}

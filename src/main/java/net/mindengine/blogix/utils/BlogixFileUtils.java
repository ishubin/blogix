package net.mindengine.blogix.utils;

import java.io.File;
import java.net.URISyntaxException;

public class BlogixFileUtils {

    /**
     * Search for a file first in a root folder of a project, then in project resources
     * @param path
     * @return
     * @throws URISyntaxException 
     */
    public static File findFile(String path) throws URISyntaxException {
        File tilesFile = new File(path);
        if ( !tilesFile.exists() ) {
            tilesFile = new File(BlogixFileUtils.class.getResource("/" + path).toURI());
        }
        return tilesFile;
    }

}

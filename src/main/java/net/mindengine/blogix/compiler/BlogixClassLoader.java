package net.mindengine.blogix.compiler;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;

public class BlogixClassLoader extends URLClassLoader {

    public BlogixClassLoader(String dirPath) {
        super(new URL[]{pathToUrl(dirPath)});
    }

    private static URL pathToUrl(String dirPath) {
        
        try {
            return new File(dirPath).toURI().toURL();
        } catch (MalformedURLException e) {
            throw new RuntimeException("Incorrect path " + dirPath, e);
        }
    }
    
}

package net.mindengine.blogix.config;

import java.util.Properties;

import net.mindengine.blogix.utils.BlogixFileUtils;

import org.apache.commons.io.FileUtils;

public class BlogixConfig {
    private static final String MARKUP_CLASS = "markup.class";
    private static final String DEFAULT_CONTROLLER_PACKAGES = "default.controller.packages";
    private static final String DEFAULT_PROVIDER_PACKAGES = "default.provider.packages";
    private static final String DEFAULT_PUBLIC_PATH = "public";
    private static final String PUBLIC_PATH = "public.path";
    private static final String BLOGIX_FILEDB_PATH = "blogix.db.path";
    
    private Properties properties = new Properties();
    
    private static final BlogixConfig _instance = new BlogixConfig();
    private BlogixConfig() {
        try {
            properties.load(FileUtils.openInputStream(BlogixFileUtils.findFile("conf/properties")));
        }
        catch (Exception e) {
            throw new RuntimeException("Cannot load blogix properties", e);
        }
    }
    
    public static BlogixConfig getConfig() {
        return _instance;
    }

    public String[] getDefaultProviderPackages() {
        String value = (String) properties.get(DEFAULT_PROVIDER_PACKAGES);
        if ( value != null && !value.isEmpty() ) {
            return value.trim().split(",");
        }
        return new String[]{};
    }


    public String[] getDefaultControllerPackages() {
        String value = (String) properties.get(DEFAULT_CONTROLLER_PACKAGES);
        if ( value != null && !value.isEmpty() ) {
            return value.trim().split(",");
        }
        return new String[]{};
    }

    public String getPublicPath() {
        return properties.getProperty(PUBLIC_PATH, DEFAULT_PUBLIC_PATH);
    }

    public String getMarkupClass() {
        return (String) properties.get(MARKUP_CLASS);
    }

    public String getBlogixFileDbPath() {
        return properties.getProperty(BLOGIX_FILEDB_PATH);
    }

    public void removeProperty(String propertyName) {
        properties.remove(propertyName);
    }

    public void setProperty(String name, String value) {
        properties.setProperty(MARKUP_CLASS, value);
    }
}

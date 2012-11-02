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

    public String getProperty(String name, String defaultValue) {
        return properties.getProperty(name, defaultValue);
    }
}

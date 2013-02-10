/*******************************************************************************
* Copyright 2013 Ivan Shubin http://mindengine.net
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
package net.mindengine.blogix.markup;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.mindengine.blogix.config.BlogixConfig;
import net.mindengine.blogix.utils.BlogixFileUtils;
import net.mindengine.blogix.web.FreemarkerResolver;
import freemarker.template.Configuration;
import freemarker.template.Template;

public class TextileMarkup implements Markup {
    
    
    private static final String _PLUGIN_SUFFIX = ".plugin";
    private static final int _PLUGIN_STRIP = _PLUGIN_SUFFIX.length();
    
    private Configuration templateConfiguration;
    
    Map<String, Template> plugins = new HashMap<String, Template>();
    
    public TextileMarkup() {
        String textilePluginsFolder = BlogixConfig.getConfig().getMarkupPluginsFolder();
        if (textilePluginsFolder!= null && !textilePluginsFolder.isEmpty()) {
            loadPluginsFromFolder(textilePluginsFolder);
        }
    }
    
    @Override
    public String apply(String unformatedText) {
        List<String> list = Arrays.asList(unformatedText.split("\\r?\\n"));
        return new TextileParser(plugins).parse(list.iterator());
    }

    private void loadPluginsFromFolder(String folderPath) {
        templateConfiguration = FreemarkerResolver.defaultTemplateConfiguration();
        
        
        File filePluginsFolder = BlogixFileUtils.findFile(folderPath);
        if (!filePluginsFolder.exists()) {
            throw new RuntimeException("Plugins folder doesn't exist: " + folderPath);
        }
        if (!filePluginsFolder.isDirectory()) {
            throw new RuntimeException("Plugins path is actually not a folder: " + folderPath);
        }
        
        File[] files = filePluginsFolder.listFiles();
        for (File templateFile : files) {
            if (templateFile.getName().endsWith(_PLUGIN_SUFFIX)) {
                try {
                    loadTemplate(stripPluginSuffix(templateFile.getName()), templateFile);
                } catch (Exception e) {
                    throw new RuntimeException("Could not load plugin: " + templateFile.getPath());
                }
            }
        }
    }

    private void loadTemplate(String pluginName, File templateFile) throws FileNotFoundException, IOException {
        Template template = new Template(pluginName, new FileReader(templateFile), templateConfiguration);
        plugins.put(pluginName, template);
    }

    private String stripPluginSuffix(String name) {
        return name.substring(0, name.length() - _PLUGIN_STRIP);
    }
        
}

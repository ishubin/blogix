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
package net.mindengine.blogix.markup.converters;

import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

import freemarker.template.Template;

public class PluginsConverter implements Converter {
    
    private Map<String, Template> plugins;

    public PluginsConverter(Map<String, Template> plugins) {
        this.plugins = plugins;
    }

    @Override
    public String convert(String definition, String text) {
        String pluginName = definition.substring(2).trim();
        if (plugins.containsKey(pluginName)) {
            
            Map<String, Object> model = readModel(text);
            
            Template template = plugins.get(pluginName);
            StringWriter sw = new StringWriter();
            try {
                template.process(model, sw);
            } catch (Exception e) {
                throw new RuntimeException("Error occured in plugin '" + pluginName + "'", e);
            }
            return sw.toString();
        }
        else throw new RuntimeException("Error: Plugin '" + pluginName + "' was not found");
    }

    private Map<String, Object> readModel(String text) {
        Map<String, Object> model = new HashMap<String, Object>();
        String lines[] = text.split("\\r?\\n");
        
        for (String line : lines) {
            int index = line.indexOf(":");
            if (index > 0) {
                
                String name = line.substring(0, index).trim();
                String value = "";
                if (index < line.length() - 1) {
                    value = line.substring(index + 1).trim();
                }
                model.put(name, value);
            }
        }
        
        return model;
    }

}

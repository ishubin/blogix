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
package net.mindengine.blogix.web.tiles;

import java.io.File;
import java.io.FileReader;

import freemarker.template.Configuration;
import freemarker.template.Template;

public class TemplateFile {
    private Template template;
    private File file;
    private long lastModified;
    private String viewPath;
    private Configuration templateConfiguration;
    
    public TemplateFile(String viewPath, File file, Configuration templateConfiguration) {
        this.file = file;
        this.viewPath = viewPath;
        this.templateConfiguration = templateConfiguration;
        loadTemplate();
    }

    private void loadTemplate() {
        this.lastModified = file.lastModified();
        try {
            template = new Template(viewPath, new FileReader(file), templateConfiguration);
        } catch (Exception e) {
            throw new RuntimeException("Could not load template: " + viewPath, e);
        }
    }

    public Template getTemplate() {
        if (file.lastModified() != lastModified) {
            loadTemplate();
        }
        
        return template;
    }
    
}

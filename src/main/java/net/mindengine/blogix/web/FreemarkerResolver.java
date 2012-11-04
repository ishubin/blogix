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
package net.mindengine.blogix.web;

import java.io.File;
import java.io.FileReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Map;

import net.mindengine.blogix.Blogix;
import net.mindengine.blogix.utils.BlogixFileUtils;
import freemarker.template.Configuration;
import freemarker.template.DefaultObjectWrapper;
import freemarker.template.Template;

public class FreemarkerResolver extends ViewResolver {
    
    private String templatesPath;
    private Configuration templateConfiguration;
    
    public FreemarkerResolver(Blogix blogix, String templatesPath) {
        super(blogix);
        this.templatesPath = templatesPath;
        this.templateConfiguration = defaultTemplateConfiguration();
    }

    @Override
    public boolean canResolve(String view) {
        try {
            File file = BlogixFileUtils.findFile(templatesPath + view);
            if (file != null && file.exists()) {
                return true;
            }
        } catch (Exception e) {
        }
        return false;
    }

    @Override
    public void resolveViewAndRender(Object model, String view, OutputStream outputStream) throws Exception {
        Map<String, Object> modelMap = getBlogix().convertModelToMap(model);
        Template template = new Template(view, new FileReader(BlogixFileUtils.findFile(templatesPath + view)), templateConfiguration);
        template.process(modelMap, new PrintWriter(outputStream));
    }

    public static Configuration defaultTemplateConfiguration() {
        Configuration configuration = new Configuration();
        configuration.setObjectWrapper(new DefaultObjectWrapper());
        configuration.setNumberFormat("0.######");
        return configuration;
    }

}

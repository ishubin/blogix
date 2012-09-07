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

public class FreemarkerResolver implements ViewResolver {
    
    private String templatesPath;
    private Configuration templateConfiguration;
    
    public FreemarkerResolver(String templatesPath) {
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
        Map<String, Object> modelMap = Blogix.convertModelToMap(model);
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

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

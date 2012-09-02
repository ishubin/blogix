package net.mindengine.blogix.web;

import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

import net.mindengine.blogix.utils.BlogixFileUtils;
import net.mindengine.blogix.web.tiles.Tile;
import freemarker.template.Configuration;
import freemarker.template.DefaultObjectWrapper;
import freemarker.template.Template;
import freemarker.template.TemplateException;

public class TilesRenderer {
    private static final String VIEW_MAIN_PATH = "view/";

    private Configuration templateConfiguration;
    
    private Map<String, Template> templates = new HashMap<String, Template>();
    
    public TilesRenderer() {
        templateConfiguration = new Configuration();
        templateConfiguration.setObjectWrapper(new DefaultObjectWrapper());
        templateConfiguration.setNumberFormat("0.######");
    }

    
    public String renderTemplate(Map<String, Object> modelMap, String viewPath) throws TemplateException, IOException, URISyntaxException {
        Template template = findTemplate(viewPath);
        StringWriter sw = new StringWriter();
        template.process(modelMap, sw);
        return sw.toString();
    }
    
    
    public void renderTile(Object model, Tile tile, OutputStream outputStream) throws IOException, TemplateException, URISyntaxException {
        Map<String, Object> modelMap = convertModelToMap(model);
        
        TilesProcessor tilesProcessor = new TilesProcessor(modelMap, tile, this);
        modelMap.put("tiles", tilesProcessor);
        
        Template template = findTemplate(tile.getValue());
        template.process(modelMap, new PrintWriter(outputStream));
    }

    private synchronized Template findTemplate(String viewPath) throws IOException, URISyntaxException {
        if (templates.containsKey(viewPath)) {
            return templates.get(viewPath);
        }
        else {
            Template template = new Template(viewPath, new FileReader(BlogixFileUtils.findFile(VIEW_MAIN_PATH + viewPath)), templateConfiguration);
            templates.put(viewPath, template);
            return template;
        }
    }

    @SuppressWarnings({ "unchecked" })
    private Map<String, Object> convertModelToMap(Object model) {
        Map<String, Object> modelMap = new HashMap<String, Object>();
        if ( model != null ) {
            if ( model instanceof Map ) {
                Map<Object, Object> map = (Map<Object, Object>)model;
                for ( Map.Entry<Object, Object> entry : map.entrySet() ) {
                    modelMap.put(entry.getKey().toString(), entry.getValue());
                }
            }
            else {
                modelMap.put("model", model);
            }
        }
        return modelMap;
    }

}

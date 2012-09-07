package net.mindengine.blogix.web;

import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

import net.mindengine.blogix.Blogix;
import net.mindengine.blogix.utils.BlogixFileUtils;
import net.mindengine.blogix.web.tiles.Tile;
import net.mindengine.blogix.web.tiles.TilesContainer;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;

public class TilesResolver implements ViewResolver {
    private Configuration templateConfiguration;
    private TilesContainer tilesContainer;
    private String templatesPath = "";
    
    private Map<String, Template> templates = new HashMap<String, Template>();
    
    public TilesResolver(TilesContainer tilesContainer, String templatesPath) {
        this.tilesContainer = tilesContainer;
        this.templatesPath = templatesPath;
        templateConfiguration = FreemarkerResolver.defaultTemplateConfiguration();
    }
    
    
    @Override
    public boolean canResolve(String view) {
        return tilesContainer.getTiles().containsKey(view);
    }


    @Override
    public void resolveViewAndRender(Object model, String view, OutputStream outputStream) throws Exception {
        Tile tile = tilesContainer.findTile(view);
        if ( tile == null ) {
            throw new IllegalArgumentException("Cannot find tile for view: " + view);
        }
        renderTile(model, tile, outputStream);
    }

    
    public String renderTemplate(Map<String, Object> modelMap, String viewPath) throws TemplateException, IOException, URISyntaxException {
        Template template = findTemplate(viewPath);
        StringWriter sw = new StringWriter();
        template.process(modelMap, sw);
        return sw.toString();
    }
    
    private void renderTile(Object model, Tile tile, OutputStream outputStream) throws IOException, TemplateException, URISyntaxException {
        Map<String, Object> modelMap = Blogix.convertModelToMap(model);
        
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
            Template template = new Template(viewPath, new FileReader(BlogixFileUtils.findFile(templatesPath + viewPath)), templateConfiguration);
            templates.put(viewPath, template);
            return template;
        }
    }


}

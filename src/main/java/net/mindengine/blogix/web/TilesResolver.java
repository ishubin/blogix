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
package net.mindengine.blogix.web;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

import net.mindengine.blogix.Blogix;
import net.mindengine.blogix.utils.BlogixFileUtils;
import net.mindengine.blogix.web.tiles.TemplateFile;
import net.mindengine.blogix.web.tiles.Tile;
import net.mindengine.blogix.web.tiles.TilesContainer;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;

public class TilesResolver extends ViewResolver {
    private Configuration templateConfiguration;
    private TilesContainer tilesContainer;
    private String templatesPath = "";
    
    private Map<String, TemplateFile> templates = new HashMap<String, TemplateFile>();
    
    public TilesResolver(Blogix blogix, TilesContainer tilesContainer, String templatesPath) {
        super(blogix);
        this.tilesContainer = tilesContainer;
        this.templatesPath = templatesPath;
        templateConfiguration = FreemarkerResolver.defaultTemplateConfiguration();
    }
    
    @Override
    public boolean canResolve(String view) {
        return tilesContainer.getTiles().containsKey(view);
    }

    @Override
    public void resolveViewAndRender(Map<String, Object> routeModel, Object objectModel, String view, OutputStream outputStream) throws Exception {
        Tile tile = tilesContainer.findTile(view);
        if ( tile == null ) {
            throw new IllegalArgumentException("Cannot find tile for view: " + view);
        }
        renderTile(routeModel, objectModel, tile, outputStream);
    }
    
    public String renderTemplate(Map<String, Object> modelMap, String viewPath) throws TemplateException, IOException, URISyntaxException {
        Template template = findTemplate(viewPath);
        StringWriter sw = new StringWriter();
        template.process(modelMap, sw);
        return sw.toString();
    }
    
    private void renderTile(Map<String, Object> routeModel, Object objectModel, Tile tile, OutputStream outputStream) throws IOException, TemplateException, URISyntaxException {
        Map<String, Object> modelMap = getBlogix().convertObjectToMapModel(routeModel, objectModel);
        
        TilesProcessor tilesProcessor = new TilesProcessor(modelMap, tile, this);
        modelMap.put("tiles", tilesProcessor);
        
        Template template = findTemplate(tile.getValue());
        template.process(modelMap, new PrintWriter(outputStream));
    }

    private synchronized Template findTemplate(String viewPath) throws IOException, URISyntaxException {
        if (templates.containsKey(viewPath)) {
            return templates.get(viewPath).getTemplate();
        }
        else {
            
            TemplateFile templateFile = new TemplateFile(viewPath, BlogixFileUtils.findFile(templatesPath + viewPath), templateConfiguration);
            templates.put(viewPath, templateFile);
            return templateFile.getTemplate();
        }
    }


}

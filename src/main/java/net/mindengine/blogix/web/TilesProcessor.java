package net.mindengine.blogix.web;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Map;

import freemarker.template.TemplateException;

import net.mindengine.blogix.web.tiles.Tile;

public class TilesProcessor {
    private TilesRenderer tilesRenderer;
    private Map<String, Object> modelMap;
    private Tile tile;

    public TilesProcessor(Map<String, Object> modelMap, Tile tile, TilesRenderer tilesRenderer) {
        this.tile = tile;
        this.modelMap = modelMap;
        this.tilesRenderer = tilesRenderer;
    }

    public String process(String tileName) throws TemplateException, IOException, URISyntaxException {
        Tile subTile = findSubTile(tileName);
        if ( subTile == null ) {
            throw new IllegalArgumentException("Cannot process tile: '" + tileName + "' for view '" + tile.getName() + "'");
        }
        return tilesRenderer.renderTemplate(modelMap, subTile.getValue());
    }

    private Tile findSubTile(String name) {
        
        if ( tile.getTiles() != null ) {
            return tile.getTiles().get(name);
        }
        return null;
    }

}

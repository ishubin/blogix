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

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Map;

import freemarker.template.TemplateException;

import net.mindengine.blogix.web.tiles.Tile;

public class TilesProcessor {
    private TilesResolver tilesRenderer;
    private Map<String, Object> modelMap;
    private Tile tile;

    public TilesProcessor(Map<String, Object> modelMap, Tile tile, TilesResolver tilesRenderer) {
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

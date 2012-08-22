package net.mindengine.blogix.web.tiles;

import java.util.Map;

public class Tile {
    private String name;
    private String value;
    private String extendsTile;
    
    private Map<String, Tile> tiles;

    public Map<String, Tile> getTiles() {
        return tiles;
    }

    public void setTiles(Map<String, Tile> tiles) {
        this.tiles = tiles;
    }

    public String getExtendsTile() {
        return extendsTile;
    }

    public void setExtendsTile(String extendsTile) {
        this.extendsTile = extendsTile;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}

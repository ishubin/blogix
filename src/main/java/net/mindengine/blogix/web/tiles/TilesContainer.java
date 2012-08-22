package net.mindengine.blogix.web.tiles;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;

public class TilesContainer {

    private Map<String, Tile> tiles;
    
    public void load(File file) throws IOException {
        TileLine rootTileLine = readAllTileLines(file);
        
        tiles = new HashMap<String, Tile>();
        if ( rootTileLine.children != null ) {
            for ( TileLine childTile : rootTileLine.children ) {
                Tile tile = convertTile(childTile);
                tiles.put(tile.getName(), tile);
            }
        }
    }

    private Tile convertTile(TileLine tileLine) {
        Tile tile = new Tile();
        tile.setName(tileLine.key);
        tile.setTiles(new HashMap<String, Tile>());
        if ( tileLine.isExtending()) {
            tile.setExtendsTile(tileLine.getExtendedBaseName());
            tile.setTiles(new HashMap<String, Tile>());
            extendTile(tile, tileLine, new LinkedList<String>());
        }
        else {
            tile.setValue(tileLine.value);
        }
        if ( tileLine.children != null ) {
            for ( TileLine childTileLine: tileLine.children) {
                Tile childTile = convertTile(childTileLine);
                tile.getTiles().put(childTile.getName(), childTile);
            }
        }
        
        return tile;
    }

    private void extendTile(Tile tile, TileLine lookOutsideTileLine, LinkedList<String> extensionChainList) {
        if ( extensionChainList.contains(lookOutsideTileLine.getExtendedBaseName())) {
            throw new IllegalArgumentException("There is a cross reference in extension chain of tiles");
        }
        extensionChainList.add(lookOutsideTileLine.getExtendedBaseName());
        
        TileLine baseTileLine = findBaseTileLineFor(lookOutsideTileLine);
        if ( baseTileLine.isExtending() ) {
            extendTile(tile, baseTileLine, extensionChainList);
        }
        else {
            tile.setValue(baseTileLine.value);
        }
        
        if ( baseTileLine.children != null ) {
            for (TileLine childLine : baseTileLine.children ) {
                tile.getTiles().put(childLine.key, convertTile(childLine));
            }
        }
        
    }

    private TileLine findBaseTileLineFor(TileLine lookOutsideTileLine) {
        if ( lookOutsideTileLine.key.equals(lookOutsideTileLine.getExtendedBaseName()) ) {
            throw new IllegalArgumentException("Wrong tile extension. Cannot extend from itself");
        }
        if ( lookOutsideTileLine.parent != null && lookOutsideTileLine.parent.children != null ) {
            for ( TileLine tileLine : lookOutsideTileLine.parent.children) {
                if (tileLine.key.equals(lookOutsideTileLine.getExtendedBaseName())) {
                    return tileLine;
                }
            }
        }
        
        throw new IllegalArgumentException("Cannot extend from tile '" + lookOutsideTileLine.getExtendedBaseName() + "'. Such tile is not defined");
    }

    private TileLine readAllTileLines(File file) throws IOException {
        LineIterator it = FileUtils.lineIterator(file, "UTF-8");
        /**
         * Setting a root tile which will be a container for all tiles
         */
        TileLine rootTileLine = new TileLine();
        rootTileLine.indentation = -1;
        
        TileLine currentTileLine = rootTileLine;
        try {
            while( it.hasNext() ) {
                String line = it.nextLine();
                TileLine tileLine = readKeyValue(currentTileLine, line);
                if ( tileLine != null ) {
                    currentTileLine = tileLine;
                }
            }
        }
        finally {
            LineIterator.closeQuietly(it);
        }
        return rootTileLine;
    }
    
    
    
    private TileLine readKeyValue(TileLine previousTile, String line) throws IOException {
        
        LineBuilder lineBuilder = new LineBuilder().processLine(line);
        if ( !lineBuilder.isAWhiteSpace()) {
            TileLine tileLine = lineBuilder.toTileLine();
            findParentForAttaching(previousTile, tileLine.indentation).attach(tileLine);
            return tileLine;
        }
        return null;
    }

    /*
     * Find a tile-line to which the current tile-line can be attached
     */
    private TileLine findParentForAttaching(TileLine lookupTile, int indentation) {
        if ( indentation > lookupTile.indentation ) {
            return lookupTile;
        }
        else if ( indentation == lookupTile.indentation) {
            return lookupTile.parent;
        }
        else {
            return findParentForAttaching(lookupTile.parent, indentation);
        }
    }

    private class LineBuilder {
        private static final char SPLITTER = ':';
        private static final char COMMENT = '#';
        private static final char SPACE = ' ';
        private static final int INDENTATION = 0;
        private static final int KEY = 1;
        private static final int VALUE = 2;
        private int indentation = 0;
        private StringBuilder key = new StringBuilder("");
        private StringBuilder value = new StringBuilder("");
        
        public boolean isAWhiteSpace() {
            return key.toString().isEmpty();
        }
        
        public TileLine toTileLine() {
            TileLine tileLine = new TileLine();
            tileLine.indentation = indentation;
            tileLine.key = key.toString().trim();
            tileLine.value = value.toString().trim();
            return tileLine;
        }
        /*
         * Used for identifying at which token the builder is working currently
         * 
         * 0 - indentation
         * 1 - key
         * 2 - value
         */
        private int state = INDENTATION;
        
        public LineBuilder processLine(String line) throws IOException {
            Reader reader = new StringReader(line); 
            int r;
            while( (r = reader.read()) != -1) {
               char ch = (char )r;
               if ( ch == COMMENT) {
                   return this;
               }
               switch (state) {
               case INDENTATION:
                   if ( ch == SPACE && state == INDENTATION) {
                       indentation++;
                   }
                   else { 
                       state = KEY;
                       key.append(ch);
                   }
                   break;
               case KEY:
                   if ( ch != SPLITTER) {
                       key.append(ch);
                   }
                   else {
                       state = VALUE;
                   }
                   break;
               case VALUE:
                   value.append(ch);
                   break;
               };
               
            }
            return this;
        }
    }


    public Tile findTile(String name) {
        if ( tiles != null ) {
            return tiles.get(name);
        }
        return null;
    }

    public Map<String, Tile> getTiles() {
        return tiles;
    }
    
    private class TileLine {
        private TileLine parent;
        private String key;
        private String value;
        private int indentation;
        private List<TileLine> children;
        
        public boolean isExtending() {
            return value.startsWith("@");
        }
        
        public String getExtendedBaseName() {
            return value.substring(1).trim();
        }
        
        public void attach(TileLine child) {
            child.parent = this;
            if ( children == null ) {
                children = new LinkedList<TilesContainer.TileLine>();
            }
            
            children.add(child);
        }
    }

}

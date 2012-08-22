package net.mindengine.blogix.tests.acceptance;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Map;

import net.mindengine.blogix.web.tiles.Tile;
import net.mindengine.blogix.web.tiles.TilesContainer;

import org.testng.annotations.Test;

public class TilesContainerAccTest {
    private TilesContainer container;
    
    @Test
    public void shouldLoadPropertiesFromSpecifiedFile() throws URISyntaxException, IOException {
        container  = new TilesContainer();
        container.load(new File(getClass().getResource("/tiles-test.cfg").toURI()));        
        assertThat(container.getTiles(), is(notNullValue()));
        assertThat(container.getTiles().size(), is(4));
    }
    
    @Test(dependsOnMethods="shouldLoadPropertiesFromSpecifiedFile")
    public void canLoadSimpleTileWithTrimedValue() {
        Tile simpleTile = container.findTile("simple-tile");
        assertThat(simpleTile, is(notNullValue()));
        assertThat(simpleTile.getName(), is("simple-tile"));
        assertThat(simpleTile.getValue(), is("view/simple-tile.ftl"));
        assertThat(simpleTile.getExtendsTile(), is(nullValue()));
        assertThat(simpleTile.getTiles().size(), is(0));
    }
    
    @Test(dependsOnMethods="shouldLoadPropertiesFromSpecifiedFile")
    public void canLoadTileWithSubtiles() throws Exception{
        assertThat(container.findTile("some-unexistent-tile"), is(nullValue()));
        
        Tile tile = container.findTile("main-tile");
        assertThat(tile, is(notNullValue()));
        
        assertThat(tile.getName(), is("main-tile"));
        assertThat(tile.getValue(), is("view/main-tile.ftl"));
        assertThat(tile.getExtendsTile(), is(nullValue()));
        Map<String, Tile> mainChildTiles = tile.getTiles();
        assertThat(mainChildTiles, is(notNullValue()));
        assertThat(mainChildTiles.get("header"), is(notNullValue()));
        assertThat(mainChildTiles.get("header").getValue(), is("view/header.ftl"));
        assertThat(mainChildTiles.get("header").getExtendsTile(), is(nullValue()));
        assertThat(mainChildTiles.get("header").getName(), is("header"));

        assertThat(mainChildTiles.get("content"), is(notNullValue()));
        assertThat(mainChildTiles.get("content").getValue(), is("view/content.ftl"));
        assertThat(mainChildTiles.get("content").getExtendsTile(), is(nullValue()));
        assertThat(mainChildTiles.get("content").getName(), is("content"));

        assertThat(mainChildTiles.get("footer"), is(notNullValue()));
        assertThat(mainChildTiles.get("footer").getValue(), is("view/footer.ftl"));
        assertThat(mainChildTiles.get("footer").getExtendsTile(), is(nullValue()));
        assertThat(mainChildTiles.get("footer").getName(), is("footer"));
    }
    
    @Test(dependsOnMethods="shouldLoadPropertiesFromSpecifiedFile")
    public void canExtendAllSubtilesFromParentTile() {
        Tile tile = container.findTile("child-tile");
        assertThat(tile, is(notNullValue()));
        
        assertThat(tile.getName(), is("child-tile"));
        assertThat(tile.getValue(), is("view/main-tile.ftl"));
        assertThat(tile.getExtendsTile(), is("main-tile"));
        Map<String, Tile> mainChildTiles = tile.getTiles();
        assertThat(mainChildTiles, is(notNullValue()));
        assertThat(mainChildTiles.get("header"), is(notNullValue()));
        assertThat(mainChildTiles.get("header").getValue(), is("view/header.ftl"));
        assertThat(mainChildTiles.get("header").getExtendsTile(), is(nullValue()));
        assertThat(mainChildTiles.get("header").getName(), is("header"));

        assertThat(mainChildTiles.get("content"), is(notNullValue()));
        assertThat(mainChildTiles.get("content").getValue(), is("view/child-content.ftl"));
        assertThat(mainChildTiles.get("content").getExtendsTile(), is(nullValue()));
        assertThat(mainChildTiles.get("content").getName(), is("content"));

        assertThat(mainChildTiles.get("footer"), is(notNullValue()));
        assertThat(mainChildTiles.get("footer").getValue(), is("view/footer.ftl"));
        assertThat(mainChildTiles.get("footer").getExtendsTile(), is(nullValue()));
        assertThat(mainChildTiles.get("footer").getName(), is("footer"));

    }
    
    @Test(dependsOnMethods="shouldLoadPropertiesFromSpecifiedFile")
    public void canExtendAllSubtilesFromExtendedTile() {
        Tile tile = container.findTile("2nd-extension-child-tile");
        assertThat(tile, is(notNullValue()));
        
        assertThat(tile.getName(), is("2nd-extension-child-tile"));
        assertThat(tile.getValue(), is("view/main-tile.ftl"));
        assertThat(tile.getExtendsTile(), is("child-tile"));
        Map<String, Tile> mainChildTiles = tile.getTiles();
        assertThat(mainChildTiles, is(notNullValue()));
        assertThat(mainChildTiles.get("header"), is(notNullValue()));
        assertThat(mainChildTiles.get("header").getValue(), is("view/header.ftl"));
        assertThat(mainChildTiles.get("header").getExtendsTile(), is(nullValue()));
        assertThat(mainChildTiles.get("header").getName(), is("header"));

        assertThat(mainChildTiles.get("content"), is(notNullValue()));
        assertThat(mainChildTiles.get("content").getValue(), is("view/child-content.ftl"));
        assertThat(mainChildTiles.get("content").getExtendsTile(), is(nullValue()));
        assertThat(mainChildTiles.get("content").getName(), is("content"));

        assertThat(mainChildTiles.get("footer"), is(notNullValue()));
        assertThat(mainChildTiles.get("footer").getValue(), is("view/2ndfooter.ftl"));
        assertThat(mainChildTiles.get("footer").getExtendsTile(), is(nullValue()));
        assertThat(mainChildTiles.get("footer").getName(), is("footer"));
    }
}

package net.mindengine.blogix.web;

import net.mindengine.blogix.Blogix;
import net.mindengine.blogix.utils.BlogixFileUtils;
import net.mindengine.blogix.web.tiles.TilesContainer;


public class DefaultViewResolver extends ChainedViewResolver {
    
    private TilesContainer tilesContainer = new TilesContainer();
    
    public DefaultViewResolver() {
        try {
            tilesContainer.load( BlogixFileUtils.findFile( "conf/tiles" ));
        } catch (Exception e) {
            throw new RuntimeException("Could not load tiles container");
        }
        
        initResolvers(new TilesResolver(tilesContainer, Blogix.VIEW_MAIN_PATH), new FreemarkerResolver(Blogix.VIEW_MAIN_PATH), new ClassMethodViewResolver());
    }

    

}

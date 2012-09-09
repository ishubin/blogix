package net.mindengine.blogix.web;

import net.mindengine.blogix.Blogix;
import net.mindengine.blogix.utils.BlogixFileUtils;
import net.mindengine.blogix.web.tiles.TilesContainer;


public class DefaultViewResolver extends ChainedViewResolver {
    
    private TilesContainer tilesContainer = new TilesContainer();
    private ClassLoader[] classLoader;
    
    public DefaultViewResolver(ClassLoader[] classLoaders) {
        this.classLoader = classLoaders;
        try {
            tilesContainer.load( BlogixFileUtils.findFile( "conf/tiles" ));
        } catch (Exception e) {
            throw new RuntimeException("Could not load tiles container");
        }
        
        initResolvers(new TilesResolver(tilesContainer, Blogix.VIEW_MAIN_PATH), new FreemarkerResolver(Blogix.VIEW_MAIN_PATH), new ClassMethodViewResolver(this.classLoader));
    }

    

}

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

import net.mindengine.blogix.Blogix;
import net.mindengine.blogix.utils.BlogixFileUtils;
import net.mindengine.blogix.web.tiles.TilesContainer;


public class DefaultViewResolver extends ChainedViewResolver {
    
    private TilesContainer tilesContainer = new TilesContainer();
    private ClassLoader[] classLoader;
    
    public DefaultViewResolver(Blogix blogix, ClassLoader[] classLoaders) {
        super(blogix);
        this.classLoader = classLoaders;
        try {
            tilesContainer.load( BlogixFileUtils.findFile( "conf/tiles" ));
        } catch (Exception e) {
            throw new RuntimeException("Could not load tiles container");
        }
        
        initResolvers(new TilesResolver(blogix, tilesContainer, Blogix.VIEW_MAIN_PATH), new FreemarkerResolver(blogix, Blogix.VIEW_MAIN_PATH), new ClassMethodViewResolver(blogix, this.classLoader));
    }

    

}

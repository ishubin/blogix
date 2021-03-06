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

import java.io.OutputStream;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import net.mindengine.blogix.Blogix;

public class ChainedViewResolver extends ViewResolver {

    public ChainedViewResolver(Blogix blogix) {
        super(blogix);
    }

    private List<ViewResolver> resolvers;
    
    
    protected void initResolvers(ViewResolver...viewResolvers) {
        resolvers = new LinkedList<ViewResolver>();
        for ( ViewResolver resolver : viewResolvers ) {
            resolvers.add(resolver);
        }
    }
    

    @Override
    public void resolveViewAndRender(Map<String, Object> routeModel, Object objectModel, String view, OutputStream outputStream) throws Exception {
        for (ViewResolver resolver : resolvers) {
            if (resolver.canResolve(view)) {
                resolver.resolveViewAndRender(routeModel, objectModel, view, outputStream);
                return;
            }
        }
        throw new RuntimeException("Couldn't resolve view: " + view);
    }

    @Override
    public boolean canResolve(String view) {
        for (ViewResolver resolver : resolvers) {
            if (resolver.canResolve(view)) {
                return true;
            }
        }
        return false;
    }

}

package net.mindengine.blogix.web;

import java.io.OutputStream;
import java.util.LinkedList;
import java.util.List;

public class ChainedViewResolver implements ViewResolver {
    
    private List<ViewResolver> resolvers;
    
    
    protected void initResolvers(ViewResolver...viewResolvers) {
        resolvers = new LinkedList<ViewResolver>();
        for ( ViewResolver resolver : viewResolvers ) {
            resolvers.add(resolver);
        }
    }
    

    @Override
    public void resolveViewAndRender(Object model, String view, OutputStream outputStream) throws Exception {
        for (ViewResolver resolver : resolvers) {
            if (resolver.canResolve(view)) {
                resolver.resolveViewAndRender(model, view, outputStream);
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

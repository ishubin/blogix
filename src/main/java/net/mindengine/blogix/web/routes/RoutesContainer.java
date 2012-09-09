package net.mindengine.blogix.web.routes;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class RoutesContainer {

    private List<Route> routes;
    private ClassLoader[] classLoaders;
    
    public List<Route> getRoutes() {
        return routes;
    }

    public void setRoutes(List<Route> routes) {
        this.routes = routes;
    }
    
    public RoutesContainer(ClassLoader[] classLoaders) {
        this.classLoaders = classLoaders;
    }

    public void load(File file, String[] defaultControllerPackages, String[] defaultProviderPackages) throws IOException {
        if (classLoaders == null) {
            throw new IllegalArgumentException("classLoaders are not specified");
        }
        
        RoutesParser parser = new DefaultRoutesParser(classLoaders, defaultControllerPackages, defaultProviderPackages);
        setRoutes(parser.parseRoutes(file));
    }

    public Route findRouteMatchingUri(String uri) {
        if ( routes == null ) {
            throw new RuntimeException("Routes container was not initialized");
        }
        
        for (Route route : routes) {
            if ( matches(route, uri) ) {
                return route;
            }
        }
        return null;
    }

    private boolean matches(Route route, String uri) {
        return route.getUrl().asRegexPattern().matcher(uri).matches();
    }

    public ClassLoader[] getClassLoaders() {
        return classLoaders;
    }

    public void setClassLoaders(ClassLoader[] classLoaders) {
        this.classLoaders = classLoaders;
    }
    
}

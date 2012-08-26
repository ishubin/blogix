package net.mindengine.blogix.web.routes;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class RoutesContainer {

    private List<Route> routes;
    
    private RoutesParser parser = new DefaultRoutesParser();

    public List<Route> getRoutes() {
        return routes;
    }

    public void setRoutes(List<Route> routes) {
        this.routes = routes;
    }

    public void load(File file) throws IOException {
        setRoutes(parser.parseRoutes(file));
    }
    
}

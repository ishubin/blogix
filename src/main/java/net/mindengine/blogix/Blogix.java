package net.mindengine.blogix;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.servlet.ServletOutputStream;

import net.mindengine.blogix.utils.BlogixFileUtils;
import net.mindengine.blogix.web.RouteInvoker;
import net.mindengine.blogix.web.TilesRenderer;
import net.mindengine.blogix.web.routes.Route;
import net.mindengine.blogix.web.routes.RoutesContainer;
import net.mindengine.blogix.web.tiles.Tile;
import net.mindengine.blogix.web.tiles.TilesContainer;

import org.apache.commons.io.FileUtils;

import freemarker.template.TemplateException;

public class Blogix {
    private static final HashMap<String, String> EMPTY_CONTROLLER_ARGS = new HashMap<String, String>();
    private static final String DEFAULT_CONTROLLER_PACKAGES = "default.controller.packages";
    private static final String DEFAULT_PROVIDER_PACKAGES = "default.provider.packages";

    
    private Properties properties = new Properties();
    
    private TilesContainer tilesContainer = new TilesContainer();
    private RoutesContainer routesContainer = new RoutesContainer();
    private RouteInvoker routeInvoker = new RouteInvoker();
    private TilesRenderer tilesRenderer = new TilesRenderer();
    
    public Blogix() throws IOException, URISyntaxException {
        getProperties().load(FileUtils.openInputStream(BlogixFileUtils.findFile("conf/properties")));
        
        try {
            tilesContainer.load( BlogixFileUtils.findFile( "conf/tiles" ));
            routesContainer.load( BlogixFileUtils.findFile( "conf/routes" ), getDefaultControllerPackages(), getDefaultProviderPackages()  );
        }
        catch (Exception e) {
            throw new RuntimeException("Could not load properties");
        }
    }
    
    private String[] getDefaultProviderPackages() {
        String value = (String) properties.get(DEFAULT_PROVIDER_PACKAGES);
        if ( value != null && !value.isEmpty() ) {
            return value.trim().split(",");
        }
        return new String[]{};
    }


    private String[] getDefaultControllerPackages() {
        String value = (String) properties.get(DEFAULT_CONTROLLER_PACKAGES);
        if ( value != null && !value.isEmpty() ) {
            return value.trim().split(",");
        }
        return new String[]{};
    }


    public Properties getProperties() {
        return properties;
    }

    public void setProperties(Properties properties) {
        this.properties = properties;
    }

    public TilesContainer getTilesContainer() {
        return tilesContainer;
    }

    public void setTilesContainer(TilesContainer tilesContainer) {
        this.tilesContainer = tilesContainer;
    }

    public RoutesContainer getRoutesContainer() {
        return routesContainer;
    }

    public void setRoutesContainer(RoutesContainer routesContainer) {
        this.routesContainer = routesContainer;
    }

    public RouteInvoker getRouteInvoker() {
        return routeInvoker;
    }

    public void setRouteInvoker(RouteInvoker routeInvoker) {
        this.routeInvoker = routeInvoker;
    }

    public TilesRenderer getTilesRenderer() {
        return tilesRenderer;
    }

    public void setTilesRenderer(TilesRenderer tilesRenderer) {
        this.tilesRenderer = tilesRenderer;
    }

    public void processUri(String uri, ServletOutputStream outputStream) throws IOException, TemplateException, URISyntaxException {
        Route route = findRouteMatchingUri(uri);
        
        if ( route != null ) {
            Object model = null;
            if ( route.getController() != null ) {
                model = routeInvoker.invokeRoute(route, uri);
            }
            
            String view = route.getView();
            Tile tile = tilesContainer.findTile(view);
            if ( tile == null ) {
                throw new IllegalArgumentException("Cannot find tile for view: " + view);
            }
            tilesRenderer.renderTile(model, tile, outputStream);
        }
        else throw new IllegalArgumentException("Cannot find route for uri: " + uri);
    }
    
    private Route findRouteMatchingUri(String uri) {
        if ( !uri.endsWith( "/" )) {
            uri = uri + "/";
        }
        return routesContainer.findRouteMatchingUri(uri);        
    }

    public void processRoute(Route route, Map<String,String> controllerArgs, OutputStream outputStream) throws IOException, TemplateException, URISyntaxException {
        Object model = null;
        if ( route.getController() != null ) {
            model = routeInvoker.invokeRoute(route, controllerArgs);
        }
        
        String view = route.getView();
        Tile tile = tilesContainer.findTile(view);
        if ( tile == null ) {
            throw new IllegalArgumentException("Cannot find tile for view: " + view);
        }
        tilesRenderer.renderTile(model, tile, outputStream);
    }

    public void processRoute(Route route, OutputStream outputStream) throws IOException, TemplateException, URISyntaxException  {
        processRoute(route, EMPTY_CONTROLLER_ARGS, outputStream);
    }


}

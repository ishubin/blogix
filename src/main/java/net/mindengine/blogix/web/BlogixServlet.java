package net.mindengine.blogix.web;

import java.io.IOException;
import java.util.Properties;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.mindengine.blogix.utils.BlogixFileUtils;
import net.mindengine.blogix.web.routes.Route;
import net.mindengine.blogix.web.routes.RoutesContainer;
import net.mindengine.blogix.web.tiles.Tile;
import net.mindengine.blogix.web.tiles.TilesContainer;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;

public class BlogixServlet extends HttpServlet {

    private static final String DEFAULT_CONTROLLER_PACKAGES = "default.controller.packages";

    private static final String DEFAULT_PROVIDER_PACKAGES = "default.provider.packages";

    /**
     * 
     */
    private static final long serialVersionUID = -4418319612555096438L;
    
    TilesContainer tilesContainer = new TilesContainer();
    RoutesContainer routesContainer = new RoutesContainer();
    RouteInvoker routeInvoker = new RouteInvoker();
    TilesRenderer tilesRenderer = new TilesRenderer();

    private Properties properties; 
    
    public BlogixServlet(Properties properties) {
        this.properties = properties;
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


    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) {
        String uri = req.getRequestURI();
        
        Route route = findRouteMatchingUri(uri);
        if ( route != null ) {
            try {
                Object model = null;
                if ( route.getController() != null ) {
                    model = routeInvoker.invokeRoute(route, uri);
                }
                
                String view = route.getView();
                Tile tile = tilesContainer.findTile(view);
                if ( tile == null ) {
                    throw new IllegalArgumentException("Cannot find tile for view: " + view);
                }
                res.setStatus(200);
                tilesRenderer.renderTile(model, tile, res.getOutputStream());
            }
            catch (Throwable e) {
                res.setStatus(400);
                
                printResponseText(res, ExceptionUtils.getMessage(e) + "\n" + ExceptionUtils.getStackTrace(e));
            }
            
            
        }
        else  {
            res.setStatus(404);
            printResponseText(res, "There is no route defined for this request");
        }
    }


    private void printResponseText(HttpServletResponse res, String responseText) {
        try {
            IOUtils.write(responseText, res.getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private Route findRouteMatchingUri(String uri) {
        if ( !uri.endsWith( "/" )) {
            uri = uri + "/";
        }
        return routesContainer.findRouteMatchingUri(uri);        
    }

}

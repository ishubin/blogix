package net.mindengine.blogix;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.servlet.ServletOutputStream;

import net.mindengine.blogix.utils.BlogixFileUtils;
import net.mindengine.blogix.web.DefaultViewResolver;
import net.mindengine.blogix.web.RouteInvoker;
import net.mindengine.blogix.web.ViewResolver;
import net.mindengine.blogix.web.routes.Route;
import net.mindengine.blogix.web.routes.RoutesContainer;

import org.apache.commons.io.FileUtils;

public class Blogix {
    private static final HashMap<String, String> EMPTY_CONTROLLER_ARGS = new HashMap<String, String>();
    private static final String DEFAULT_CONTROLLER_PACKAGES = "default.controller.packages";
    private static final String DEFAULT_PROVIDER_PACKAGES = "default.provider.packages";
    public static final String VIEW_MAIN_PATH = "view/";

    
    private Properties properties = new Properties();
    
    private ViewResolver viewResolver = new DefaultViewResolver();
    private RoutesContainer routesContainer = new RoutesContainer();
    private RouteInvoker routeInvoker = new RouteInvoker();
    
    public Blogix() throws IOException, URISyntaxException {
        getProperties().load(FileUtils.openInputStream(BlogixFileUtils.findFile("conf/properties")));
        
        try {
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

    public void processUri(String uri, ServletOutputStream outputStream) throws Exception {
        Route route = findRouteMatchingUri(uri);
        
        if ( route != null ) {
            Object model = null;
            if ( route.getController() != null ) {
                model = routeInvoker.invokeRoute(route, uri);
            }
            
            String view = route.getView();
            viewResolver.resolveViewAndRender(model, view, outputStream);
        }
        else throw new IllegalArgumentException("Cannot find route for uri: " + uri);
    }
    
    private Route findRouteMatchingUri(String uri) {
        return routesContainer.findRouteMatchingUri(uri);        
    }

    public void processRoute(Route route, Map<String,String> controllerArgs, OutputStream outputStream) throws Exception {
        Object model = null;
        if ( route.getController() != null ) {
            model = routeInvoker.invokeRoute(route, controllerArgs);
        }
        
        String view = route.getView();
        viewResolver.resolveViewAndRender(model, view, outputStream);
    }

    public void processRoute(Route route, OutputStream outputStream) throws Exception  {
        processRoute(route, EMPTY_CONTROLLER_ARGS, outputStream);
    }

    @SuppressWarnings("unchecked")
    public static Map<String, Object> convertModelToMap(Object model) {
        Map<String, Object> modelMap = new HashMap<String, Object>();
        if ( model != null ) {
            if ( model instanceof Map ) {
                Map<Object, Object> map = (Map<Object, Object>)model;
                for ( Map.Entry<Object, Object> entry : map.entrySet() ) {
                    modelMap.put(entry.getKey().toString(), entry.getValue());
                }
            }
            else {
                modelMap.put("model", model);
            }
        }
        return modelMap;
    }


}

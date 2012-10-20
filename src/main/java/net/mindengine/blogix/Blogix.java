package net.mindengine.blogix;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.servlet.ServletOutputStream;

import net.mindengine.blogix.compiler.BlogixClassLoader;
import net.mindengine.blogix.markup.DummyMarkup;
import net.mindengine.blogix.markup.Markup;
import net.mindengine.blogix.utils.BlogixFileUtils;
import net.mindengine.blogix.web.DefaultViewResolver;
import net.mindengine.blogix.web.RouteInvoker;
import net.mindengine.blogix.web.ViewResolver;
import net.mindengine.blogix.web.routes.Route;
import net.mindengine.blogix.web.routes.RoutesContainer;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.ClassUtils;

public class Blogix {
    private static final String DEFAULT_PUBLIC_PATH = "public";
    private static final String PUBLIC_PATH = "public.path";
    private static final String PUBLIC = "/public/";
    private static final ClassLoader[] CLASS_LOADERS = new ClassLoader[]{Blogix.class.getClassLoader(), new BlogixClassLoader("src")};
    private static final HashMap<String, String> EMPTY_CONTROLLER_ARGS = new HashMap<String, String>();
    private static final String DEFAULT_CONTROLLER_PACKAGES = "default.controller.packages";
    private static final String DEFAULT_PROVIDER_PACKAGES = "default.provider.packages";
    public static final String VIEW_MAIN_PATH = "view/";

    
    private Properties properties = new Properties();
    
    private ViewResolver viewResolver = new DefaultViewResolver(defaultClassLoaders());
    private RoutesContainer routesContainer = new RoutesContainer(defaultClassLoaders());
    private RouteInvoker routeInvoker = new RouteInvoker();
    private Markup markup;
    
    public Blogix() throws IOException, URISyntaxException {
        getProperties().load(FileUtils.openInputStream(BlogixFileUtils.findFile("conf/properties")));
        
        try {
            routesContainer.load( BlogixFileUtils.findFile( "conf/routes" ), getDefaultControllerPackages(), getDefaultProviderPackages()  );
        }
        catch (Exception e) {
            throw new RuntimeException("Could not load properties", e);
        }
    }
    
    public static ClassLoader[] defaultClassLoaders() {
        return CLASS_LOADERS;
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
        if (!isPublicResource(uri)) {
            processRoute(uri, outputStream);
        }
        else {
            processPublicResource(uri, outputStream);
        }
    }

    private void processPublicResource(String uri, ServletOutputStream outputStream) throws FileNotFoundException, IOException {
        //removing '/public/' part 
        uri = uri.substring(PUBLIC.length());
        File publicFile = findPublicResource(uri);
        IOUtils.copy(new FileInputStream((File)publicFile), outputStream);
    }

    private File findPublicResource(String uri) throws FileNotFoundException {
        String publicDirPath = properties.getProperty(PUBLIC_PATH, DEFAULT_PUBLIC_PATH);
        String fullPath = publicDirPath + File.separator + uri;
        return BlogixFileUtils.findFile(fullPath);
    }

    private boolean isPublicResource(String uri) {
        if (uri.startsWith(PUBLIC)) {
            return true;
        }
        else return false;
    }

    private void processRoute(String uri, ServletOutputStream outputStream) throws Exception {
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


    public Markup getMarkup() throws ClassNotFoundException, IllegalArgumentException, SecurityException, InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {
        if (markup == null) {
            markup = createMarkupFromProperties();
        }
        return markup;
    }

    private Markup createMarkupFromProperties() throws ClassNotFoundException, IllegalArgumentException, SecurityException, InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {
        String markupClassPath = (String) getProperties().get("markup.class");
        
        if (noMarkupDefined(markupClassPath)) {
            return new DummyMarkup();
        }
        else {
            return createMarkupFromClassPath(markupClassPath);
        }
    }

    private Markup createMarkupFromClassPath(String markupClassPath) throws ClassNotFoundException, IllegalArgumentException, SecurityException, InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {
        Class<?> configuredMarkupClass = Class.forName(markupClassPath);
        
        if (!implementsInterface(configuredMarkupClass, Markup.class)) {
            throw new RuntimeException("Class " + configuredMarkupClass.getName() + " does not implement " + Markup.class.getName());
        }
        return (Markup) configuredMarkupClass.getConstructor().newInstance();
    }

    private boolean noMarkupDefined(String markupClassPath) {
        return markupClassPath == null || markupClassPath.trim().isEmpty();
    }

    private boolean implementsInterface(Class<?> clazz, Class<Markup> interfaceClass) {
        return ClassUtils.getAllInterfaces(clazz).contains(interfaceClass);
    }

}

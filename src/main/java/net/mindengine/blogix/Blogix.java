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

import javax.servlet.ServletOutputStream;

import net.mindengine.blogix.compiler.BlogixClassLoader;
import net.mindengine.blogix.config.BlogixConfig;
import net.mindengine.blogix.markup.DummyMarkup;
import net.mindengine.blogix.markup.Markup;
import net.mindengine.blogix.utils.BlogixFileUtils;
import net.mindengine.blogix.web.DefaultViewResolver;
import net.mindengine.blogix.web.RouteInvoker;
import net.mindengine.blogix.web.ViewResolver;
import net.mindengine.blogix.web.routes.Route;
import net.mindengine.blogix.web.routes.RoutesContainer;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.ClassUtils;

public class Blogix {
    private static final String PUBLIC = "/public/";
    private static final ClassLoader[] CLASS_LOADERS = new ClassLoader[]{Blogix.class.getClassLoader(), new BlogixClassLoader("src")};
    private static final HashMap<String, String> EMPTY_CONTROLLER_ARGS = new HashMap<String, String>();
    public static final String VIEW_MAIN_PATH = "view/";

    
    private BlogixConfig config = BlogixConfig.getConfig();
    
    private ViewResolver viewResolver = new DefaultViewResolver(defaultClassLoaders());
    private RoutesContainer routesContainer = new RoutesContainer(defaultClassLoaders());
    private RouteInvoker routeInvoker = new RouteInvoker();
    private Markup markup;
    
    public Blogix() throws IOException, URISyntaxException {
        try {
            routesContainer.load( BlogixFileUtils.findFile( "conf/routes" ), config.getDefaultControllerPackages(), config.getDefaultProviderPackages()  );
        }
        catch (Exception e) {
            throw new RuntimeException("Could not load properties", e);
        }
    }
    
    public static ClassLoader[] defaultClassLoaders() {
        return CLASS_LOADERS;
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
        String publicDirPath = config.getPublicPath();
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
        String markupClassPath = config.getMarkupClass();
        
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

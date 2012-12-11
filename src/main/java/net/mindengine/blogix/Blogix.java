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
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;

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
import net.mindengine.blogix.web.routes.RouteURL;
import net.mindengine.blogix.web.routes.RoutesContainer;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.ClassUtils;

public class Blogix {
    private static final String PUBLIC = "/public/";
    private static final ClassLoader[] CLASS_LOADERS = new ClassLoader[]{Blogix.class.getClassLoader(), new BlogixClassLoader("src")};
    private static final HashMap<String, String> EMPTY_CONTROLLER_ARGS = new HashMap<String, String>();
    public static final String VIEW_MAIN_PATH = "view/";

    
    private BlogixConfig config = BlogixConfig.getConfig();
    
    private ViewResolver viewResolver = new DefaultViewResolver(this, defaultClassLoaders());
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
            Map<String, String> controllerArgs = createParametersMap(route.getUrl(), uri);
            processRoute(uri, route, controllerArgs, outputStream);
        }
        else throw new IllegalArgumentException("Cannot find route for uri: " + uri);
    }


    public void processRoute(String uri, Route route, Map<String,String> controllerArgs, OutputStream outputStream) throws Exception {
        Object objectModel = null;
        if ( route.getController() != null ) {
            objectModel = routeInvoker.invokeRoute(route, controllerArgs);
        }
        
        String view = route.getView();
        if (view != null) {
            
            Map<String, Object> routeModel = createRouteModel(route, uri);
            viewResolver.resolveViewAndRender(routeModel, objectModel, view, outputStream);
        }
        else resolveViewless(objectModel, outputStream);
    }


    /**
     * Resolves routes which do not have views. Can only resolve controllers which return file type.
     * @param objectModel
     * @param outputStream
     * @throws IOException 
     * @throws FileNotFoundException 
     */
    private void resolveViewless(Object objectModel, OutputStream outputStream) throws FileNotFoundException, IOException {
        if (objectModel != null && objectModel instanceof File) {
            File file = (File) objectModel;
            IOUtils.copy(new FileInputStream(file), outputStream);
        }
        else {
            throw new IllegalArgumentException("Can't render viewless route. Controller did not return a file");
        }
    }

    public void processRoute(String uri, Route route, OutputStream outputStream) throws Exception  {
        processRoute(uri, route, EMPTY_CONTROLLER_ARGS, outputStream);
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> convertObjectToMapModel(Map<String, Object> routeModel, Object model) {
        Map<String, Object> modelMap = new HashMap<String, Object>();
        
        if (routeModel != null) {
            modelMap.putAll(routeModel);
        }
        
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
        
        addMarkupToModel(modelMap);
        addUserCustomVariables(modelMap);
        return modelMap;
    }


    private void addMarkupToModel(Map<String, Object> modelMap) {
        if (!modelMap.containsKey("markup")) {
            modelMap.put("markup", getMarkup());
        }
    }

    private static void addUserCustomVariables(Map<String, Object> modelMap) {
        Map<String, String> properties = BlogixConfig.getConfig().getUserCustomProperties();
        modelMap.putAll(properties);
    }

    public Markup getMarkup()  {
        if (markup == null) {
            try {
                markup = createMarkupFromProperties();
            } catch (Exception e) {
                throw new RuntimeException("Cannot create markup", e);
            }
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

    
    private Map<String, String> createParametersMap(RouteURL url, String uri) {
        if ( !uri.endsWith("/") ) {
            uri = uri + "/";
        }
        
        if ( url.getParameters() != null && !url.getParameters().isEmpty() ) {
            Matcher matcher = url.asRegexPattern().matcher(uri);
            
            if ( matcher.find() ) {
                if ( matcher.groupCount() >= url.getParameters().size()) {
                    Map<String, String> parametersMap = new HashMap<String, String>();
                    int i = 0;
                    for (String parameter : url.getParameters() ) {
                        i++;
                        parametersMap.put(parameter, matcher.group(i));
                    }
                    return parametersMap;
                }
            }
            
            throw new IllegalArgumentException("Can't extract controller arguments from uri: " + uri);
        }
        return Collections.emptyMap();
    }


    private Map<String, Object> createRouteModel(Route route, String uri) {
        BlogixData data = new BlogixData();
        data.setCurrentUri(uri);
        data.setWayToRoot(findWayToRootFromUri(uri));
        
        
        Map<String, Object> routeModel = route.getModel();
        if (routeModel == null) {
            routeModel = new HashMap<String, Object>();
        }
        
        routeModel.put("blogix", data);
        return routeModel;
    }
    
    private String findWayToRootFromUri(String uri) {
        int amountOfSlashes = countAmountOfSlashesInUri(uri);
        if (amountOfSlashes > 1) {
            StringBuffer wayToRoot = new StringBuffer("..");
            for (int i = 1; i < amountOfSlashes - 1; i++) {
                wayToRoot.append("/..");
            }
            return wayToRoot.toString();
        }
        else return ".";
    }

    private int countAmountOfSlashesInUri(String uri) {
        int amountOfSlashes = 0;
        for (int i = 0; i < uri.length(); i++) {
            if (uri.charAt(i) == '/') {
                amountOfSlashes++;
            }
        }
        return amountOfSlashes;
    }

    private Route findRouteMatchingUri(String uri) {
        return routesContainer.findRouteMatchingUri(uri);        
    }
}

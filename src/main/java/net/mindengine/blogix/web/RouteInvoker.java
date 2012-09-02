package net.mindengine.blogix.web;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;

import net.mindengine.blogix.web.routes.Route;
import net.mindengine.blogix.web.routes.RouteURL;

public class RouteInvoker {

    public Object invokeRoute(Route route, String uri) throws Throwable {
        Map<String, String> parametersMap = createParametersMap(route.getUrl(), uri);
        Method controllerMethod = route.getController().getControllerMethod();
        
        Object[] arguments = createArguments(parametersMap, route.getController().getParameters(), controllerMethod.getParameterTypes());
        
        try {
            Object model = controllerMethod.invoke(null, arguments);
            return model;
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (IllegalAccessException e) {
            throw e;
        } catch (InvocationTargetException e) {
            throw e.getTargetException();
        }
    }

    private Object[] createArguments(Map<String, String> parametersMap, List<String> parameters, Class<?>[] parameterTypes) {
        Object [] args = new Object[parameterTypes.length];
        if ( parameters.size() < args.length) {
            throw new IllegalArgumentException("Controller method arguments definition is incorrect");
        }
        
        for ( int i=0; i<args.length; i++ ) {
            String parameterName = parameters.get(i);
            String value = parametersMap.get(parameterName);
            
            args[i] = convertArgument(value, parameterTypes[i]);
        }
        return args;
    }

    private Object convertArgument(String value, Class<?> clazz) {
        if ( String.class.equals(clazz) ) {
            return value;
        }
        else if ( Integer.class.equals(clazz) ) {
            return Integer.parseInt(value);
        }
        else if ( Long.class.equals(clazz) ) {
            return Long.parseLong(value);
        }
        else if ( Double.class.equals(clazz) ) {
            return Double.parseDouble(value);
        }
        else if ( Float.class.equals(clazz) ) {
            return Float.parseFloat(value);
        }
        
        throw new IllegalArgumentException("Cannot convert value '" + value + "' to " + clazz.getName());
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
    
    

}

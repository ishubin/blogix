/*******************************************************************************
* Copyright 2013 Ivan Shubin http://mindengine.net
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

    /*public Object invokeRoute(Route route, String uri) {
        Map<String, String> parametersMap = createParametersMap(route.getUrl(), uri);
        return invokeRoute(route, parametersMap);
    }*/
    
    public Object invokeRoute(Route route, Map<String, String> parametersMap) {
        Method controllerMethod = route.getController().getControllerMethod();
        Object[] arguments = createArguments(parametersMap, route.getController().getParameters(), controllerMethod.getParameterTypes());
        
        try {
            Object model = controllerMethod.invoke(null, arguments);
            return model;
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Not allowed to access controller for route " + route.getUrl().getOriginalUrl(), e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException("An error occured from controller for route " + route.getUrl().getOriginalUrl(), e.getTargetException());
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
}

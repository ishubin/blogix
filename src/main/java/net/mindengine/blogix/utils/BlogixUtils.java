package net.mindengine.blogix.utils;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import net.mindengine.blogix.web.routes.RouteParserException;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

public class BlogixUtils {

    
    public static Pair<Class<?>, Method> readClassAndMethodFromParsedString(String parsedString, String[] defaultPackages) {
        int id = StringUtils.lastIndexOf(parsedString, ".");
        
        if (id > 0 ) {
            String methodName = parsedString.substring(id + 1);
            String classPath = parsedString.substring(0, id);
            return findClassAndMethod(classPath, methodName, defaultPackages);
        }
        else throw new RouteParserException("Cannot parse controller definition '" + parsedString + "'");
    }

    private static Pair<Class<?>, Method> findClassAndMethod(String classPath, String methodName, String[] defaultPackages) {
        Class<?> controllerClass = null;
        
        for ( String defaultPackage : defaultPackages ) {
            try {
                controllerClass = Class.forName(defaultPackage + "." + classPath);
                break;
            }
            catch (Exception e) {
            }
            
        }
        if ( controllerClass == null ) {
            try {
                controllerClass = Class.forName(classPath);
            } catch (ClassNotFoundException e) {
                throw new RouteParserException("Cannot find a class for controller: " + classPath);
            }
        }
        
        Method method = findMethodInClass(controllerClass, methodName);
        if ( method != null ) {
            return new ImmutablePair<Class<?>, Method>(controllerClass, method);
        }
        else throw new RouteParserException("Cannot find method '" + methodName + "' for controller " + controllerClass.getName());
    }

    private static Method findMethodInClass(Class<?> controllerClass, String methodName) {
        Method[] methods = controllerClass.getMethods();
        for ( Method method : methods ) {
            if ( Modifier.isStatic(method.getModifiers()) && method.getName().equals(methodName) ) {
                return method;
            }
        }
        return null;
    }
}

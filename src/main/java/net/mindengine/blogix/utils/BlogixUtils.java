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
package net.mindengine.blogix.utils;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import net.mindengine.blogix.web.routes.RouteParserException;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

public class BlogixUtils {

    
    public static Pair<Class<?>, Method> readClassAndMethodFromParsedString(ClassLoader[] classLoaders, String parsedString, String[] defaultPackages) {
        int id = StringUtils.lastIndexOf(parsedString, ".");
        
        if (id > 0 ) {
            String methodName = parsedString.substring(id + 1);
            String classPath = parsedString.substring(0, id);
            return findClassAndMethod(classLoaders, classPath, methodName, defaultPackages);
        }
        else throw new RouteParserException("Cannot parse controller definition '" + parsedString + "'");
    }

    private static Pair<Class<?>, Method> findClassAndMethod(ClassLoader[] classLoaders, String classPath, String methodName, String[] defaultPackages) {
        Class<?> controllerClass = null;
        
        for ( String defaultPackage : defaultPackages ) {
            try {
                controllerClass = findClassInClassLoaders(classLoaders, defaultPackage + "." + classPath);
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

    private static Class<?> findClassInClassLoaders(ClassLoader[] classLoaders, String classPath) throws ClassNotFoundException {
        for (ClassLoader classLoader : classLoaders) {
            try {
                return classLoader.loadClass(classPath);
            } catch (ClassNotFoundException e) {
            }
        }
        throw new ClassNotFoundException(classPath);
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

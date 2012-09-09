package net.mindengine.blogix.web;

import java.io.File;
import java.io.FileInputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;

import net.mindengine.blogix.utils.BlogixUtils;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.tuple.Pair;

public class ClassMethodViewResolver implements ViewResolver {

    private ClassLoader[] classLoaders;
    
    public ClassMethodViewResolver(ClassLoader[] classLoaders) {
        this.classLoaders = classLoaders;
    }

    @Override
    public boolean canResolve(String view) {
        try {
            Pair<Class<?>, Method> resolver = extractClassAndMethod(view);
            if ( resolver != null ) {
                return true;
            }
        }
        catch(Exception ex) {
        }
        return false;
    }

    private Pair<Class<?>, Method> extractClassAndMethod(String view) {
        return BlogixUtils.readClassAndMethodFromParsedString(this.classLoaders, view, getDefaultPackages());
    }

    private String[] getDefaultPackages() {
        return new String[]{"views"};
    }

    @Override
    public void resolveViewAndRender(Object model, String view, OutputStream outputStream) throws Exception {
        Method method = extractClassAndMethod(view).getRight();
        
        if (!method.getReturnType().equals(String.class) && !method.getReturnType().equals(File.class)) {
            throw new IllegalArgumentException("Cannot resolve view: '" + view + "'. Reason method does not return String or File type");
        }
        
        Object result = null;
        if (method.getParameterTypes().length == 0 ) {
            result = method.invoke(null, null);
        }
        else if(method.getParameterTypes().length == 1) {
            result = method.invoke(null, model);
        }
        else throw new IllegalArgumentException("Cannot resolve view: '" + view + "'. Reason is to many method arguments for view resolver");
        
        
        
        if (result == null) {
            throw new IllegalArgumentException("Cannot resolve view: '" + view + "'. Reason method returned null");
        }
        
        if ( result instanceof String ) {
            IOUtils.write((String)result, outputStream);
        }
        else if ( result instanceof File ) {
            IOUtils.copy(new FileInputStream((File)result), outputStream);
        }
    }
    
}

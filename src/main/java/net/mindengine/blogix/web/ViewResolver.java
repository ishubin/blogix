package net.mindengine.blogix.web;

import java.io.OutputStream;


public interface ViewResolver {

    boolean canResolve(String view);
    void resolveViewAndRender(Object model, String view, OutputStream outputStream) throws Exception;

}

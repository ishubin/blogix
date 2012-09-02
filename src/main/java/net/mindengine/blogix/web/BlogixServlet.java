package net.mindengine.blogix.web;

import java.io.IOException;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.mindengine.blogix.Blogix;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;

public class BlogixServlet extends HttpServlet {

    
    /**
     * 
     */
    private static final long serialVersionUID = -4418319612555096438L;
    
    
    private Blogix blogix;
    
    public BlogixServlet(Blogix blogix) {
        this.blogix = blogix;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) {
        String uri = req.getRequestURI();
        try {
            res.setStatus(200);
            blogix.processUri(uri, res.getOutputStream());
        }
        catch (Throwable e) {
            res.setStatus(400);
            printResponseText(res, ExceptionUtils.getMessage(e) + "\n" + ExceptionUtils.getStackTrace(e));
        }
    }


    private void printResponseText(HttpServletResponse res, String responseText) {
        try {
            IOUtils.write(responseText, res.getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}

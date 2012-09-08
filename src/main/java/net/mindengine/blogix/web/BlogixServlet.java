package net.mindengine.blogix.web;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.mindengine.blogix.Blogix;
import net.mindengine.blogix.utils.BlogixFileUtils;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;

public class BlogixServlet extends HttpServlet {

    private Map<String, String> supportedMimeTypes;
    /**
     * 
     */
    private static final long serialVersionUID = -4418319612555096438L;
    
    
    private Blogix blogix;
    
    public BlogixServlet(Blogix blogix) {
        this.blogix = blogix;
        loadSupportedMimeTypes();
    }

    private void loadSupportedMimeTypes() {
        supportedMimeTypes = new HashMap<String, String>();
        try {
            File file = BlogixFileUtils.findFile("supported-mime-types.cfg");
            Properties props = new Properties();
            props.load(new FileReader(file));
            
            Set<Entry<Object, Object>> entrySet = props.entrySet();
            for (Entry<Object, Object> entry : entrySet) {
                supportedMimeTypes.put(entry.getKey().toString().trim(), entry.getValue().toString().trim());
            }
            
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) {
        String uri = req.getRequestURI();
        try {
            res.setStatus(200);
            String contentType = findContentTypeFor(uri);
            if ( contentType != null ) {
                res.setContentType(contentType);
            }
            blogix.processUri(uri, res.getOutputStream());
        }
        catch (Throwable e) {
            res.setStatus(400);
            printResponseText(res, ExceptionUtils.getMessage(e) + "\n" + ExceptionUtils.getStackTrace(e));
        }
    }


    private String findContentTypeFor(String uri) {
        int id = uri.lastIndexOf('.');
        if ( id>0 && id < uri.length() - 1) {
            String type = uri.substring(id + 1).toLowerCase();
            return supportedMimeTypes.get(type);
        }
        return null;
    }

    private void printResponseText(HttpServletResponse res, String responseText) {
        try {
            IOUtils.write(responseText, res.getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}

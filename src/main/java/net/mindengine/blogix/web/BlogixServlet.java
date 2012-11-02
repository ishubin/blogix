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
            File file = BlogixFileUtils.findFile("conf/supported-mime-types.cfg");
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

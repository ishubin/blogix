package net.mindengine.blogix.web;

import java.io.IOException;
import java.net.URISyntaxException;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.mindengine.blogix.utils.BlogixFileUtils;
import net.mindengine.blogix.web.tiles.TilesContainer;

public class BlogixServlet extends HttpServlet {

    /**
     * 
     */
    private static final long serialVersionUID = -4418319612555096438L;
    
    TilesContainer tilesContainer = new TilesContainer();
    
    public BlogixServlet() throws IOException, URISyntaxException {
        tilesContainer.load(BlogixFileUtils.findFile("conf/tiles"));
    }
    
    
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) {
        
    }

}

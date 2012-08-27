package net.mindengine.blogix.web;

import java.io.IOException;
import java.net.URISyntaxException;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;

import net.mindengine.blogix.utils.BlogixFileUtils;
import net.mindengine.blogix.web.routes.Route;
import net.mindengine.blogix.web.routes.RoutesContainer;
import net.mindengine.blogix.web.tiles.TilesContainer;

public class BlogixServlet extends HttpServlet {

    /**
     * 
     */
    private static final long serialVersionUID = -4418319612555096438L;
    
    TilesContainer tilesContainer = new TilesContainer();
    RoutesContainer routesContainer = new RoutesContainer(); 
    
    public BlogixServlet() throws IOException, URISyntaxException {
        tilesContainer.load( BlogixFileUtils.findFile( "conf/tiles" ) );
        routesContainer.load( BlogixFileUtils.findFile( "conf/routes" ) );
    }
    
    
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) {
        String uri = req.getRequestURI();
        
        Route route = findRouteMatchingUri(uri);
        if ( route != null ) { 
            printResponseText(res, "OK");
        }
        else  {
            res.setStatus(404);
            printResponseText(res, "There is no route defined for this request");
        }
    }


    private void printResponseText(HttpServletResponse res, String responseText) {
        try {
            IOUtils.write(responseText, res.getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private Route findRouteMatchingUri(String uri) {
        if ( !uri.endsWith( "/" )) {
            uri = uri + "/";
        }
        
        
        return null;
    }

}

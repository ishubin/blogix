package net.mindengine.blogix.export.Exporter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URISyntaxException;

import net.mindengine.blogix.Blogix;
import net.mindengine.blogix.web.routes.Route;

public class BlogixExporter {

    private static final String INDEX_HTML = "index.html";

    private File destinationDir;
    
    private Blogix blogix;
    
    public BlogixExporter() throws IOException, URISyntaxException {
        blogix = new Blogix();
    }

    public BlogixExporter(File destinationDir) throws IOException, URISyntaxException {
        this();
        this.setDestinationDir(destinationDir);
    }

    public File getDestinationDir() {
        return destinationDir;
    }

    public void setDestinationDir(File destinationDir) {
        this.destinationDir = destinationDir;
    }

    public void exportAll() {
        try {
            for ( Route route : blogix.getRoutesContainer().getRoutes() ) {
                exportRoute(route);
            }
        }
        catch (Exception e) {
            throw new RuntimeException("Could not export all routes", e);
        }
    }

    private void exportRoute(Route route) throws Exception {
        if ( route.getProvider() == null || !route.getUrl().isParameterized()) {
            exportSimpleRoute(route);
        }
    }

    private void exportSimpleRoute(Route route) throws Exception {
        File routeDir = new File(destinationDir.getAbsolutePath() + route.getUrl().getOriginalUrl());
        routeDir.mkdirs();
        
        File indexFile = new File(routeDir.getAbsoluteFile() + File.separator + INDEX_HTML);
        indexFile.createNewFile();
        blogix.processRoute(route, new FileOutputStream(indexFile));
    }

}

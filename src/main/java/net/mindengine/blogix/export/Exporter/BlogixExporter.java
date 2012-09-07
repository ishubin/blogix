package net.mindengine.blogix.export.Exporter;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URISyntaxException;
import java.util.Map;

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
        if ( route.getUrl().isParameterized()) {
            exportParameterizedRoute(route);
        }
        else {
            exportSimpleRoute(route);
        }
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private void exportParameterizedRoute(Route route) throws Exception {
        if ( route.getProvider() == null ) {
            throw new NullPointerException("Provider is not specified for parameterized route " + route.getUrl().getOriginalUrl());
        }
        Method providerMethod = route.getProvider().getProviderMethod();
        Map[] urlParameters = (Map[]) providerMethod.invoke(null, new Object[]{});
        for (Map urlParameterMap : urlParameters) {
            exportParameterizedRoute(route, (Map<String, String>) urlParameterMap);
        }
    }

    private void exportParameterizedRoute(Route route, Map<String, String> parametersMap) throws Exception {
        String url = route.getUrl().getOriginalUrl();
        for (Map.Entry<String, String> parameter : parametersMap.entrySet()) {
            url = url.replace("{" + parameter.getKey() + "}", parameter.getValue());
        }
        if (url.contains("{") || url.contains("}")) {
            throw new IllegalArgumentException("Cannot export parameterized route " + route.getUrl().getOriginalUrl());
        }

        blogix.processRoute(route, parametersMap, createFileOutputStreamForResponse(url));
    }

    private String extractPathToDirForFile(String fullPath) {
        int id = fullPath.lastIndexOf(File.separator);
        if ( id >=0 ) {
            return fullPath.substring(0, id+1);
        }
        else throw new IllegalArgumentException("Cannot extract path to dir for file " + fullPath);
    }

    private void exportSimpleRoute(Route route) throws Exception {
        blogix.processRoute(route, createFileOutputStreamForResponse(route.getUrl().getOriginalUrl()));
    }
    
    private FileOutputStream createFileOutputStreamForResponse(String url) throws IOException, FileNotFoundException {
        FileOutputStream responseOutputStream;
        File responseFile = null;
        String fullPath = destinationDir.getAbsolutePath() + url.replace("/", File.separator);
        
        if ( fullPath.endsWith(File.separator) ) {
            File routeDir = new File(fullPath);
            routeDir.mkdirs();
            responseFile = new File(fullPath + INDEX_HTML);
        }
        else {
            String dirPathOfFile = extractPathToDirForFile(fullPath);
            File routeDir = new File(dirPathOfFile);
            routeDir.mkdirs();
            responseFile = new File(fullPath);
        }
        
        
        responseFile.createNewFile();
        responseOutputStream = new FileOutputStream(responseFile);
        return responseOutputStream;
    }
}

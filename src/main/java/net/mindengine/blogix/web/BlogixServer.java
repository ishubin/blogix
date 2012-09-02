package net.mindengine.blogix.web;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Properties;

import net.mindengine.blogix.utils.BlogixFileUtils;

import org.apache.commons.io.FileUtils;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.nio.SelectChannelConnector;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

public class BlogixServer {
    private Integer port = 8080;
    
    private Properties properties = new Properties();
    
    private Server jettyServer;
    private BlogixServlet servlet;
    
    public BlogixServer() throws IOException, URISyntaxException {
        getProperties().load(FileUtils.openInputStream(BlogixFileUtils.findFile("conf/properties")));
    }
    
    public void startServer() throws Exception {
        jettyServer = new Server();
        jettyServer.addConnector(createConnector());
        jettyServer.setHandler(createContext());
        jettyServer.start();
        jettyServer.join();
    }

    private Connector createConnector() {
        Connector connector = new SelectChannelConnector();
        connector.setPort(port);
        return connector;
    }

    private ServletContextHandler createContext() throws Exception {
        ServletContextHandler context = new ServletContextHandler();
        context.addServlet(createServletHolder(), "/*");
        
        HandlerList handlers = new HandlerList();
        handlers.setHandlers(new Handler[] { context});
        return context;
    }

    private ServletHolder createServletHolder() throws Exception {
        ServletHolder holder = new ServletHolder();
        servlet = new BlogixServlet(getProperties());
        holder.setServlet(servlet);
        return holder;
    }
    
    public Integer getPort() {
        return port;
    }
    public void setPort(Integer port) {
        this.port = port;
    }

    public Properties getProperties() {
        return properties;
    }

    public void setProperties(Properties properties) {
        this.properties = properties;
    }
    
}

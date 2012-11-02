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

import java.io.IOException;
import java.net.URISyntaxException;

import net.mindengine.blogix.Blogix;

import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.nio.SelectChannelConnector;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

public class BlogixServer {
    private Integer port = 8080;
    
    private Server jettyServer;
    private BlogixServlet servlet;
    
    public BlogixServer() throws IOException, URISyntaxException {
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
        servlet = new BlogixServlet(new Blogix());
        holder.setServlet(servlet);
        return holder;
    }
    
    public Integer getPort() {
        return port;
    }
    public void setPort(Integer port) {
        this.port = port;
    }

}

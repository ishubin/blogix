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
package net.mindengine.blogix;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;

import net.mindengine.blogix.compiler.BlogixCompiler;
import net.mindengine.blogix.export.Exporter.BlogixExporter;
import net.mindengine.blogix.web.BlogixServer;



public class BlogixMain {
    
    public BlogixMain () {
    }
    
    public static void main(String[] args) throws Exception {
        BlogixMain bm = new BlogixMain();
        
        if (args.length>0) {
            String action = args[0];
            if (action.equals("run")) {
                bm.run();
            }
            else if (action.equals("compile")) {
                bm.compile();
            }
            else if (action.equals("export")) {
                bm.export();
            }
        }
    }

    private void export() throws IOException, URISyntaxException {
        File destinationDir = new File(System.getProperty("blogix.export.dest", "export"));
        if (!destinationDir.exists()) {
            destinationDir.mkdirs();
        }
        BlogixExporter exporter = new BlogixExporter(destinationDir);
        info("Exporting all routes...");
        exporter.exportAll();
    }

    private void compile() {
        BlogixCompiler compiler = new BlogixCompiler();
        File sourceDir = new File("src");
        if (!sourceDir.exists()) {
            throw new RuntimeException("There is no src folder");
        }
        compiler.setSourceDir(sourceDir);

        
        File binDir = new File("bin");
        if (!binDir.exists()) {
            binDir.mkdirs();
        }
        compiler.setClassesDir(binDir);
        
        info("Compiling project...");
        compiler.compile();
    }


    private void run() throws Exception {
        BlogixServer server  = new BlogixServer();
        info("Launching blogix web server...");
        server.startServer();
    }

    private void info(String text) {
        System.out.println(text);
    }
}

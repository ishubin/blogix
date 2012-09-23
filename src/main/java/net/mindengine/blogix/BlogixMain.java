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
        File destinationDir = new File("export");
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

package net.mindengine.blogix.tests.acceptance;

import java.beans.DesignMode;
import java.io.File;

import net.mindengine.blogix.export.Exporter.BlogixExporter;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.google.common.io.Files;

public class ExporterAccTest {
    
    private File destinationDir;
    
    @BeforeClass
    public void init() {
        destinationDir = Files.createTempDir();
    }
    
    @Test
    public void exportsAllRoutes() throws Exception {
        BlogixExporter exporter = new BlogixExporter(this.destinationDir);
        //exporter.exportAll();
        throw new Exception("Not finished test");
    }

}

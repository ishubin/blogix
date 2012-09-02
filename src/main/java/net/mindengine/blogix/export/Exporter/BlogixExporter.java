package net.mindengine.blogix.export.Exporter;

import java.io.File;

public class BlogixExporter {

    private File destinationDir;

    public BlogixExporter(File destinationDir) {
        this.setDestinationDir(destinationDir);
    }

    public File getDestinationDir() {
        return destinationDir;
    }

    public void setDestinationDir(File destinationDir) {
        this.destinationDir = destinationDir;
    }

}

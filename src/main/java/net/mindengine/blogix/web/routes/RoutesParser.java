package net.mindengine.blogix.web.routes;

import java.io.File;
import java.io.IOException;
import java.util.List;

public interface RoutesParser {

    List<Route> parseRoutes(File file) throws IOException;

}

package net.mindengine.blogix.web.routes;

public class RouteParserException extends RuntimeException {

    /**
     * 
     */
    private static final long serialVersionUID = 2131649402081052624L;

    public RouteParserException() {
        super();
    }

    public RouteParserException(String arg0, Throwable arg1) {
        super(arg0, arg1);
    }

    public RouteParserException(String arg0) {
        super(arg0);
    }

    public RouteParserException(Throwable arg0) {
        super(arg0);
    }
}

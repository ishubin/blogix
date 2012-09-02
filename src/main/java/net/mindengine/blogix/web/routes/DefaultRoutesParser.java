package net.mindengine.blogix.web.routes;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

public class DefaultRoutesParser implements RoutesParser {
    

    private static final char COMMENT = '#';
    private static final char SPACE = ' ';
    private static final String URL_PARAM_REGEX = "([a-zA-Z0-9_\\-]*)";
    private static final char URL_PARAM_START = '{';
    private static final char URL_PARAM_END = '}';
    private static final char METHOD_ARGUMENTS_START = '(';
    private static final char METHOD_ARGUMENTS_END = ')';
    private String[] defaultControllerPackages;
    private String[] defaultProviderPackages;
    

    public DefaultRoutesParser(String[] defaultControllerPackages, String[] defaultProviderPackages) {
        this.defaultControllerPackages = defaultControllerPackages;
        this.defaultProviderPackages = defaultProviderPackages;
    }

    @Override
    public List<Route> parseRoutes(File file) throws IOException {
        LineIterator it = FileUtils.lineIterator(file, "UTF-8");
        List<Route> routes = new LinkedList<Route>();
        
        while( it.hasNext() ) {
            String line = it.nextLine();
            Route route = parseLine(line.trim());
            if ( route != null ) {
                routes.add(route);
            }
        }
        return routes;
    }

    private Route parseLine(String line) throws IOException {
        return verified(new LineReader().readLine(line));
    }
    
    
    private Route verified(Route route) {
        if ( route != null  ) {
            /*
             * Checking that parameterized routes should always have controllers 
             */
            if ( route.getController() == null || route.getController().getControllerClass() == null || route.getController().getControllerMethod() == null ) {
                if ( route.getUrl().getParameters() != null && route.getUrl().getParameters().size() > 0 ) {
                    throw new RouteParserException("Controller is not defined for route: " + prettyPrintUrl(route));
                }
            }
            
            if ( route.getView() == null || route.getView().isEmpty() ) {
                throw new RouteParserException("View is not defined for route: " + prettyPrintUrl(route));
            }
            
            if ( route.getUrl().getParameters().size() > 0 ) {
                if ( route.getProvider() == null || route.getProvider().getProviderClass() == null || route.getProvider().getProviderMethod() == null ) {
                    throw new RouteParserException("Provider is not defined for parameterized route: " + prettyPrintUrl(route));
                }
                
                for ( String param : route.getUrl().getParameters() ) {
                    if (!route.getController().getParameters().contains(param)) {
                        throw new RouteParserException("Route url parameter '" + param + "' is not used in controller arguments for route: "+ prettyPrintUrl(route));
                    }
                }
            }
            else {
                if ( route.getProvider() != null) {
                    throw new RouteParserException("Non-parameterized route " + prettyPrintUrl(route) + " does not need a provider");
                }
            }
            
        }
        return route;
    }


    private static String prettyPrintUrl(Route route) {
        return route.getUrl().getOriginalUrl();
    }


    private abstract class State {
        protected Route route;
        public State(Route route) {
            this.route = route;
        }
        /**
         * 
         * @param ch - character in line
         * @return Next state for line parsing, if null is returned then it means that this line should not be processed anymore
         */
        public abstract State processChar(char ch);
        public void lineFinished() {
        }
        
        //Chain of states
        
        // url [urlparams]* -> controller -> controller args ->  view ->  provider
        //            \________________________________/
    }
    
    private class ParsingUrl extends State {
        

        public ParsingUrl(Route route) {
            super(route);
            route.setUrl(new RouteURL(""));
        }
        
        private StringBuffer url = new StringBuffer("");
        private StringBuffer originalUrl = new StringBuffer("");
        
        @Override
        public State processChar(char ch) {
            if ( ch == COMMENT ) {
                if ( alreadyStarted() ) {
                    return doneWithNextState();
                }
                else return doneCompletely();
            }
            else if (ch == SPACE) {
                if ( alreadyStarted() ) {
                    return doneWithNextState();
                }
            }
            else {
                if ( alreadyStarted() ) {
                    if ( ch == URL_PARAM_START) {
                        return new ParsingUrlParam(route, this);
                    }
                    else {
                        append(ch);
                    }
                }
                else if ( ch == '/') {
                    append(ch);
                }
                else throw new RouteParserException("Route url should start with /");
            }
            return this;
        }
        private State doneCompletely() {
            if ( !url.toString().endsWith( "/" ) ) {
                append('/');
            }
            route.getUrl().setUrlPattern( url.toString() );
            route.getUrl().setOriginalUrl(originalUrl.toString());
            
            return null;
        }
        private void append(char ch) {
            url.append(ch);
            originalUrl.append(ch);
        }
        
        public void append(String url, String toOriginalUrl) {
            this.url.append(url);
            this.originalUrl.append(toOriginalUrl);
        }

        private boolean alreadyStarted() {
            return url.length() > 0;
        }
        private State doneWithNextState() {
            doneCompletely();
            return new ParsingController(route);
        }
    }
    
    private class ParsingUrlParam extends State {
        private ParsingUrl parsingUrl;
        public ParsingUrlParam(Route route, ParsingUrl parsingUrl) {
            super(route);
            this.parsingUrl = parsingUrl;
        }
        
        StringBuffer param = new StringBuffer("");

        @Override
        public State processChar(char ch) {
            if (ch != URL_PARAM_END) {
                param.append(ch);
                return this;
            }
            else {
                parsingUrl.append(URL_PARAM_REGEX, URL_PARAM_START  + param.toString() + URL_PARAM_END);
                route.getUrl().getParameters().add(param.toString());
                return parsingUrl;
            }
        }
    }

    
    private class ParsingSkipWord extends State {
        private State nextState;

        public ParsingSkipWord(State nextState) {
            super(null);
            this.nextState = nextState;
        }

        @Override
        public State processChar(char ch) {
            if ( ch == SPACE ) {
                return nextState;
            }
            return this;
        }
        
    }
    
    private class ParsingController extends State {
        private StringBuffer controller = new StringBuffer("");
        
        public ParsingController(Route route) {
            super(route);
            route.setController(new ControllerDefinition());
        }

        @Override
        public State processChar(char ch) {
            if ( ch == '-') {
                if ( alreadyStarted() ) {
                    throw new IllegalArgumentException("Controller for route " + prettyPrintUrl(route) + " is incorrect");
                }
                else {
                    /*
                     * Means that there is no controller for handling this route
                     * Instead just the view tile will be processed without controller processing
                     */
                    route.setController(null);
                    return new ParsingSkipWord(new ParsingView(route));
                }
            }
            else if ( ch == SPACE ) {
                if ( alreadyStarted() ) {
                    return nextStateYetUnclearIfArgsAreThere();
                }
            }
            else if ( ch == METHOD_ARGUMENTS_START ) {
                if ( alreadyStarted() ) {
                    return nextStateParseArgs();
                }
                else throw new RouteParserException("There is no controller found in route " + prettyPrintUrl(route));
            }
            else {
                append(ch);
            }
            return this;
        }

        private State nextStateParseArgs() {
            processParsedString();
            return new ParsingArgs(route).definetelyParseArgs();
        }

        private void append(char ch) {
            controller.append(ch);
        }

        private State nextStateYetUnclearIfArgsAreThere() {
            processParsedString();
            return new ParsingArgs(route);
        }

        private void processParsedString() {
            Pair<Class<?>, Method> pair = readClassAndMethodFromParsedString(controller.toString(), DefaultRoutesParser.this.defaultControllerPackages);
            route.getController().setControllerClass(pair.getLeft());
            route.getController().setControllerMethod(pair.getRight());
            
            if (pair.getRight().getReturnType().equals(Void.TYPE )) {
                throw new RouteParserException("Controller " + pair.getLeft().getName() + "." + pair.getRight().getName() + " returns void type");
            }
        }

        private boolean alreadyStarted() {
            return controller.length() > 0;
        }
    }

    
    private class ParsingArgs extends State {
        public ParsingArgs(Route route) {
            super(route);
            route.getController().setParameters(new LinkedList<String>());
        }
        
        private boolean sureIfArgsAreThere = false;
        private StringBuffer argsBuffer = new StringBuffer("");

        public State definetelyParseArgs() {
            sureIfArgsAreThere = true;
            return this;
        }
        
        @Override
        public State processChar(char ch) {
            if ( !sureIfArgsAreThere && !alreadyStarted()) {
                if (ch == METHOD_ARGUMENTS_START ) {
                    sureIfArgsAreThere = true;
                }
                else if (ch != SPACE ) {
                    return nextStateWithFirstChar(ch);
                }
            }
            
            if ( ch == METHOD_ARGUMENTS_END ) {
                processMethodArguments();
                return nextState();
            }
            else if (ch != SPACE && ch != METHOD_ARGUMENTS_START ) {
                argsBuffer.append(ch);
            }
            return this;
        }

        private void processMethodArguments() {
            String argsStr = argsBuffer.toString();
            if ( !argsStr.isEmpty() ) {
                String [] args = argsStr.split(",");
                for ( String arg : args ) {
                    if ( !arg.isEmpty() ) {
                        route.getController().getParameters().add(arg);
                    }
                }
            }
        }

        private State nextState() {
            return new ParsingView(route);
        }

        private State nextStateWithFirstChar(char ch) {
            return new ParsingView(route).withAdditionalChar(ch);
        }

        private boolean alreadyStarted() {
            return argsBuffer.length() > 0 ;
        }
    }

    
    private abstract class ParsingSimpleString<T> extends State {
        private StringBuffer stringBuffer = new StringBuffer("");
        public ParsingSimpleString(Route route) {
            super(route);
        }

        @SuppressWarnings("unchecked")
        public T withAdditionalChar(char ch) {
            stringBuffer.append(ch);
            return (T)this;
        }
        
        @Override
        public State processChar(char ch) {
            if ( ch == SPACE ) {
                if ( alreadyStarted() ) {
                    return doneAndNowSwitchToNextState();
                }
            }
            else {
                stringBuffer.append(ch);
            }
            return this;
        }
        
        private boolean alreadyStarted() {
            return stringBuffer.length() > 0;
        }

        public String getParsedString() {
            return stringBuffer.toString();
        }
        public abstract State doneAndNowSwitchToNextState();
    }
    
    private class ParsingView extends ParsingSimpleString<ParsingView> {
        public ParsingView ( Route route ) {
            super(route);
        }

        @Override
        public State doneAndNowSwitchToNextState() {
            done();
            return new ParsingProvider(route);
        }

        private void done() {
            String view = getParsedString();
            if ( view.isEmpty() ) {
                throw new RouteParserException("View is not defined for route: " + prettyPrintUrl(route));
            }
            route.setView(view);
        }
        
        @Override
        public void lineFinished() {
            done();
        }
    }

    
    private class ParsingProvider extends ParsingSimpleString<ParsingProvider> {
        public ParsingProvider(Route route) {
            super(route);
        }

        @Override
        public State doneAndNowSwitchToNextState() {
            done();
            return null;
        }
        
        private void done() {
            String provider = getParsedString();
            if ( !provider.isEmpty() ) {
                RouteProviderDefinition rpd = new RouteProviderDefinition();
                Pair<Class<?>, Method> pair = readClassAndMethodFromParsedString(provider, DefaultRoutesParser.this.defaultProviderPackages);
                rpd.setProviderClass(pair.getLeft());
                rpd.setProviderMethod(pair.getRight());
                
                Class<?> returnType = pair.getRight().getReturnType();
                if ( !returnType.equals(Map[].class)) {
                    throw new RouteParserException("Provider " + pair.getLeft().getName() + "." + pair.getRight().getName() + " does not return Map[] type");
                }
                
                route.setProvider(rpd);
            }
        }

        @Override
        public void lineFinished() {
            done();
        }
        
    }
    
    private class LineReader {
        private Route route = new Route();
        private State state = new ParsingUrl(this.route);
        public Route readLine(String line) throws IOException {
            Reader reader = new StringReader(line);
            int r = -1;
            
            while( state != null && (r = reader.read()) != -1) {
               char ch = (char )r;
               if ( ch == COMMENT) {
                   state.lineFinished();
                   state = null;
               }
               else  {
                   state = state.processChar(ch);
               }
            }
            if ( state != null && r == -1 ) {
                state.lineFinished();
            }
            
            //Checking if the line actually contained route definition
            if ( route.getUrl().getUrlPattern() == null || !route.getUrl().getUrlPattern().trim().startsWith("/")) {
                return null;
            }
            else {
                return route;
            }
        }
    }
    
    
    
    private static Pair<Class<?>, Method> readClassAndMethodFromParsedString(String parsedString, String[] defaultPackages) {
        int id = StringUtils.lastIndexOf(parsedString, ".");
        
        if (id > 0 ) {
            String methodName = parsedString.substring(id + 1);
            String classPath = parsedString.substring(0, id);
            return findClassAndMethod(classPath, methodName, defaultPackages);
        }
        else throw new RouteParserException("Cannot parse controller definition '" + parsedString + "'");
    }

    private static Pair<Class<?>, Method> findClassAndMethod(String classPath, String methodName, String[] defaultPackages) {
        Class<?> controllerClass = null;
        
        for ( String defaultPackage : defaultPackages ) {
            try {
                controllerClass = Class.forName(defaultPackage + "." + classPath);
                break;
            }
            catch (Exception e) {
            }
            
        }
        if ( controllerClass == null ) {
            try {
                controllerClass = Class.forName(classPath);
            } catch (ClassNotFoundException e) {
                throw new RouteParserException("Cannot find a class for controller: " + classPath);
            }
        }
        
        Method method = findMethodInClass(controllerClass, methodName);
        if ( method != null ) {
            return new ImmutablePair<Class<?>, Method>(controllerClass, method);
        }
        else throw new RouteParserException("Cannot find method '" + methodName + "' for controller " + controllerClass.getName());
    }

    private static Method findMethodInClass(Class<?> controllerClass, String methodName) {
        Method[] methods = controllerClass.getMethods();
        for ( Method method : methods ) {
            if ( Modifier.isStatic(method.getModifiers()) && method.getName().equals(methodName) ) {
                return method;
            }
        }
        return null;
    }
}




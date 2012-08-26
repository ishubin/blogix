package net.mindengine.blogix.tests.acceptance;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.LinkedList;
import java.util.List;

import net.mindengine.blogix.components.MockedController;
import net.mindengine.blogix.web.routes.ControllerDefinition;
import net.mindengine.blogix.web.routes.Route;
import net.mindengine.blogix.web.routes.RouteParserException;
import net.mindengine.blogix.web.routes.RouteProviderDefinition;
import net.mindengine.blogix.web.routes.RoutesContainer;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import providers.DefaultMockedProvider;
import controllers.DefaultMockedController;

public class RoutesContainerAccTest {

    private static final String BASE_TEST = "shouldLoadFromSpecifiedFile";
    RoutesContainer container;
    
    @BeforeClass
    public void initialize() {
        container = new RoutesContainer();
    }
    
    @Test
    public void shouldLoadFromSpecifiedFile() throws URISyntaxException, IOException {
        container.load(new File(getClass().getResource("/routes-test.cfg").toURI()));
        
        assertThat("Routes list should be not null", container.getRoutes(), is(notNullValue()));
        assertThat("Routes list should contain 4 routes", container.getRoutes().size(), is(4));
    }
    
    @Test(dependsOnMethods = BASE_TEST)
    public void shouldIgnoreCommentsFollowingByHash() throws Exception {
        for ( Route route : container.getRoutes() ) {
            if ( route.getUrl().getUrlPattern().startsWith("/commented")) {
                throw new Exception("Commented route was processed as normal");
            }
        }
    }
    
    @Test(dependsOnMethods = BASE_TEST)
    public void shouldParseSimpleRoutesUrl() {
        assertThat(urlPatternInRoute(0), is("/"));
        assertThat(urlPatternInRoute(1), is("/another/route"));
        assertThat(urlPatternInRoute(2), is("/another/route2"));
        
        assertThat(urlParametersInRoute(0), is(empty()));
        assertThat(urlParametersInRoute(1), is(empty()));
        assertThat(urlParametersInRoute(2), is(empty()));
    }
    
    @Test(dependsOnMethods = BASE_TEST)
    public void shouldParseParameterizedRoute() {
        assertThat(urlPatternInRoute(3), is("/parameterized/rout/[a-zA-Z0-9_\\-]*/gap/[a-zA-Z0-9_\\-]*"));
        assertThat(urlParametersInRoute(3), is(list("param1","param2")));
    }
    
    @Test(dependsOnMethods = BASE_TEST)
    public void shouldFindControllerClassInControllersPackageByDefault() {
        assertThat(controllerInRoute(0).getControllerClass().getName(), is(MockedController.class.getName()));
        assertThat(controllerInRoute(0).getControllerMethod().getName(), is ("someMethod"));
        
        assertThat(controllerInRoute(1).getControllerClass().getName(), is(DefaultMockedController.class.getName()));
        assertThat(controllerInRoute(1).getControllerMethod().getName(), is ("someMethod"));
        
        assertThat(controllerInRoute(2).getControllerClass().getName(), is(DefaultMockedController.class.getName()));
        assertThat(controllerInRoute(2).getControllerMethod().getName(), is ("someParameterizedMethod"));
        
        assertThat(controllerInRoute(3).getControllerClass().getName(), is(DefaultMockedController.class.getName()));
        assertThat(controllerInRoute(3).getControllerMethod().getName(), is ("someParameterizedMethod"));
    }
    
    @Test(dependsOnMethods = BASE_TEST)
    public void shouldParseMethodArgumentsInParameterizedController() {
        assertThat(controllerInRoute(0).getParameters(), is (empty()));
        assertThat(controllerInRoute(1).getParameters(), is (empty()));
        assertThat(controllerInRoute(2).getParameters(), is (empty()));
        assertThat(controllerInRoute(3).getParameters(), is(not(empty())));
        assertThat(controllerInRoute(3).getParameters(), is(list("param1", "param2")));
    }
    
    @Test(dependsOnMethods = BASE_TEST)
    public void shouldReadViewNameAfterController() {
        assertThat(viewNameInRoute(0), is("some-view-name"));
        assertThat(viewNameInRoute(1), is("some-view-name-2"));
        assertThat(viewNameInRoute(2), is("some-view-name-3"));
        assertThat(viewNameInRoute(3), is("some-view-name-4"));
    }
    

    @Test(dependsOnMethods = BASE_TEST)
    public void shouldReadProvider() {
        assertThat(providerInRoute(0), is(nullValue()));
        assertThat(providerInRoute(1), is(nullValue()));
        assertThat(providerInRoute(2), is(nullValue()));
        
        RouteProviderDefinition rpd = providerInRoute(3);
        assertThat(rpd, is(notNullValue()));
        assertThat(rpd.getProviderClass().getName(), is (DefaultMockedProvider.class.getName()));
        assertThat(rpd.getProviderMethod().getName(), is ("someProviderMethod"));
    }
    
    private RouteProviderDefinition providerInRoute(int index) {
        return container.getRoutes().get(index).getProvider();
    }

    @Test(  expectedExceptions=RouteParserException.class,
            expectedExceptionsMessageRegExp="Provider is not defined for parameterized route: /route/\\{param1\\}/and/\\{param2\\}")
    public void shouldGiveErrorIfParameterizedRouteDoesNotHaveProviderSpecified() throws IOException, URISyntaxException {
        new RoutesContainer().load(new File(getClass().getResource("/routes-no-provider-for-parametrized-route-error.cfg").toURI()));
    }
    
    @Test(expectedExceptions=RouteParserException.class,
            expectedExceptionsMessageRegExp="Route url should start with /")
    public void shouldGiveErrorIfRouteDoesNotStartWithSlash () throws IOException, URISyntaxException {
        new RoutesContainer().load(new File(getClass().getResource("/routes-no-slash-url-error.cfg").toURI()));
    }
    
    @Test (expectedExceptions=RouteParserException.class,
            expectedExceptionsMessageRegExp="Route url parameter 'param1' is not used in controller arguments for route: /route/\\{param1\\}/and/\\{param2\\}")
    public void shouldGiveErrorIfUrlParamsDoNotMatchWithControllerArguments() throws IOException, URISyntaxException {
        new RoutesContainer().load(new File(getClass().getResource("/routes-no-param-arg-match-error.cfg").toURI()));
    }
    
    @Test (expectedExceptions=RouteParserException.class,
            expectedExceptionsMessageRegExp="Controller controllers.DefaultMockedController.voidMethod returns void type")
    public void shouldGiveErrorIfControllerMethodReturnsVoidType() throws IOException, URISyntaxException {
        new RoutesContainer().load(new File(getClass().getResource("/routes-void-controller-error.cfg").toURI()));
    }
    
    @Test (expectedExceptions=RouteParserException.class,
            expectedExceptionsMessageRegExp="Provider providers.DefaultMockedProvider.someNonArrayMethod does not return Map\\[\\] type")
    public void shouldGiveErrorIfProviderMethodDoesNotReturnArrayOfMapType() throws IOException, URISyntaxException {
        new RoutesContainer().load(new File(getClass().getResource("/routes-non-list-provider-error.cfg").toURI()));
    }
    
    @Test(  expectedExceptions=RouteParserException.class,
            expectedExceptionsMessageRegExp="Non-parameterized route /some-route does not need a provider")
    public void shouldGiveErrorIfSimpleUrlHasAProvider() throws IOException, URISyntaxException {
        new RoutesContainer().load(new File(getClass().getResource("/routes-simple-url-with-provider-error.cfg").toURI()));
    }
    
    @Test(  expectedExceptions=RouteParserException.class,
            expectedExceptionsMessageRegExp="Controller is not defined for route: /url")
    public void shouldGiveErrorIfControllerIsNotDefined() throws IOException, URISyntaxException {
        new RoutesContainer().load(new File(getClass().getResource("/routes-no-controller-error.cfg").toURI()));
    }
    
    @Test(expectedExceptions=RouteParserException.class,
            expectedExceptionsMessageRegExp="View is not defined for route: /url")
    public void shouldGiveErrorIfViewIsNotDefined() throws IOException, URISyntaxException {
        new RoutesContainer().load(new File(getClass().getResource("/routes-no-view-error.cfg").toURI()));
    }
    
    private ControllerDefinition controllerInRoute(int index) {
        return container.getRoutes().get(index).getController();
    }
    
    private String viewNameInRoute(int index) {
        return container.getRoutes().get(index).getView();
    }
    private List<String> urlParametersInRoute(int i) {
        return container.getRoutes().get(i).getUrl().getParameters();
    }
    
    private String urlPatternInRoute(int number) {
        return container.getRoutes().get(number).getUrl().getUrlPattern();
    }
    
    private List<String> list(String ... items) {
        List<String> list = new LinkedList<String>();
        for ( String item : items ) {
            list.add(item);
        }
        return list;
    }

}

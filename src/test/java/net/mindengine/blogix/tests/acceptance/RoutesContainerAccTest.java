package net.mindengine.blogix.tests.acceptance;

import static net.mindengine.blogix.tests.TestGroups.ACCEPTANCE;
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.mindengine.blogix.components.MockedController;
import net.mindengine.blogix.web.routes.ControllerDefinition;
import net.mindengine.blogix.web.routes.Route;
import net.mindengine.blogix.web.routes.RouteParserException;
import net.mindengine.blogix.web.routes.RouteProviderDefinition;
import net.mindengine.blogix.web.routes.RouteURL;
import net.mindengine.blogix.web.routes.RoutesContainer;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import providers.DefaultMockedProvider;
import controllers.DefaultMockedController;

@Test(groups=ACCEPTANCE)
public class RoutesContainerAccTest {

    private static final String[] DEFAULT_PROVIDER_PACKAGES = new String[]{"providers"};
    private static final String[] DEFAULT_CONTROLLER_PACKAGES = new String[]{"controllers"};
    private static final String BASE_TEST = "shouldLoadFromSpecifiedFile";
    RoutesContainer container;
    
    @BeforeClass
    public void initialize() {
        container = new RoutesContainer();
    }
    
    @Test
    public void shouldLoadFromSpecifiedFile() throws URISyntaxException, IOException {
        container.load(new File(getClass().getResource("/routes-test.cfg").toURI()), DEFAULT_CONTROLLER_PACKAGES, DEFAULT_PROVIDER_PACKAGES);
        assertThat("Routes list should be not null", container.getRoutes(), is(notNullValue()));
        assertThat("Routes list should contain 4 routes", container.getRoutes().size(), is(5));
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
        assertThat( urlInRoute(0).getUrlPattern(), is("/"));
        assertThat( urlInRoute(1).getUrlPattern(), is("/another/route/"));
        assertThat( urlInRoute(2).getUrlPattern(), is("/another/route2/"));
        assertThat( urlInRoute(4).getUrlPattern(), is("/simple/view/route/"));
        
        assertThat( urlInRoute(0).getParameters(), is( empty() ));
        assertThat( urlInRoute(1).getParameters(), is( empty() ));
        assertThat( urlInRoute(2).getParameters(), is( empty() ));
        assertThat( urlInRoute(4).getParameters(), is( empty() ));
    }
    
    @Test(dependsOnMethods = BASE_TEST)
    public void shouldParseSimpleRouteWithoutController() {
        assertThat( controllerInRoute(4), is (nullValue()));
        assertThat( viewNameInRoute(4), is ("some-simple-view"));
    }
    
    @Test(dependsOnMethods = BASE_TEST)
    public void shouldGenerateRegexPatternOnlyOnce () {
        for ( int i = 0; i < 4; i++) {
            Pattern pattern = urlInRoute(i).asRegexPattern();
            assertThat( pattern, is ( notNullValue() ));
            assertThat( urlInRoute(i).asRegexPattern(), is ( pattern ));
        }
    }
    
    @Test(dependsOnMethods = BASE_TEST,
            dataProvider="provideRegexCheckSamples")
    public void shouldGenerateProperRegexPatterns(String urlSample, boolean expectedToMatch) {
        Matcher matcher = urlInRoute(3).asRegexPattern().matcher(urlSample);
        
        if ( expectedToMatch ) {
            assertThat( urlSample + " text does not match the parameterized route regex pattern: " + urlInRoute(3).getUrlPattern() , matcher.matches(), is (true));
        }
        else assertThat( urlSample + " text matches the parameterized route regex pattern but it should not: " + urlInRoute(3).getUrlPattern(), matcher.matches(), is (false));
    }
    
    @DataProvider
    public Object[][] provideRegexCheckSamples() {
        return new Object[][]{
                {"/parameterized/rout/abc/gap/qwe/", true},
                {"/parameterized/rout/ab/c/gap/qwe/", false},
                {"/parameterized/rout/a/gap/q/", true},
                {"/parameterized/rout/9/gap/qwe/", true},
                {"/parameterized/rout/_c/gap/123/", true},
                {"/parameterized/rout/_-_/gap/11-/", true},
                {"/rout/abc/gap/qwe/", false}};
    }
    
    @Test(dependsOnMethods = BASE_TEST)
    public void shouldParseParameterizedRoute() {
        assertThat(urlInRoute(3).getUrlPattern(), is("/parameterized/rout/([a-zA-Z0-9_\\-]*)/gap/([a-zA-Z0-9_\\-]*)/"));
        assertThat(urlInRoute(3).getParameters(), is(list("param1","param2")));
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
            expectedExceptionsMessageRegExp="Provider is not defined for parameterized route: /route/\\{param1\\}/and/\\{param2\\}/")
    public void shouldGiveErrorIfParameterizedRouteDoesNotHaveProviderSpecified() throws IOException, URISyntaxException {
        new RoutesContainer().load(new File(getClass().getResource("/routes-no-provider-for-parametrized-route-error.cfg").toURI()), DEFAULT_CONTROLLER_PACKAGES, DEFAULT_PROVIDER_PACKAGES);
    }
    
    @Test(expectedExceptions=RouteParserException.class,
            expectedExceptionsMessageRegExp="Route url should start with /")
    public void shouldGiveErrorIfRouteDoesNotStartWithSlash () throws IOException, URISyntaxException {
        new RoutesContainer().load(new File(getClass().getResource("/routes-no-slash-url-error.cfg").toURI()), DEFAULT_CONTROLLER_PACKAGES, DEFAULT_PROVIDER_PACKAGES);
    }
    
    @Test (expectedExceptions=RouteParserException.class,
            expectedExceptionsMessageRegExp="Route url parameter 'param1' is not used in controller arguments for route: /route/\\{param1\\}/and/\\{param2\\}/")
    public void shouldGiveErrorIfUrlParamsDoNotMatchWithControllerArguments() throws IOException, URISyntaxException {
        new RoutesContainer().load(new File(getClass().getResource("/routes-no-param-arg-match-error.cfg").toURI()), DEFAULT_CONTROLLER_PACKAGES, DEFAULT_PROVIDER_PACKAGES);
    }
    
    @Test (expectedExceptions=RouteParserException.class,
            expectedExceptionsMessageRegExp="Controller controllers.DefaultMockedController.voidMethod returns void type")
    public void shouldGiveErrorIfControllerMethodReturnsVoidType() throws IOException, URISyntaxException {
        new RoutesContainer().load(new File(getClass().getResource("/routes-void-controller-error.cfg").toURI()), DEFAULT_CONTROLLER_PACKAGES, DEFAULT_PROVIDER_PACKAGES);
    }
    
    @Test (expectedExceptions=RouteParserException.class,
            expectedExceptionsMessageRegExp="Provider providers.DefaultMockedProvider.someNonArrayMethod does not return Map\\[\\] type")
    public void shouldGiveErrorIfProviderMethodDoesNotReturnArrayOfMapType() throws IOException, URISyntaxException {
        new RoutesContainer().load(new File(getClass().getResource("/routes-non-list-provider-error.cfg").toURI()), DEFAULT_CONTROLLER_PACKAGES, DEFAULT_PROVIDER_PACKAGES);
    }
    
    @Test(  expectedExceptions=RouteParserException.class,
            expectedExceptionsMessageRegExp="Non-parameterized route /some-route/ does not need a provider")
    public void shouldGiveErrorIfSimpleUrlHasAProvider() throws IOException, URISyntaxException {
        new RoutesContainer().load(new File(getClass().getResource("/routes-simple-url-with-provider-error.cfg").toURI()), DEFAULT_CONTROLLER_PACKAGES, DEFAULT_PROVIDER_PACKAGES);
    }
    
    @Test(  expectedExceptions=RouteParserException.class,
            expectedExceptionsMessageRegExp="View is not defined for route: /url/")
    public void shouldGiveErrorIfControllerAndViewAreNotDefined() throws IOException, URISyntaxException {
        new RoutesContainer().load(new File(getClass().getResource("/routes-no-controller-error.cfg").toURI()), DEFAULT_CONTROLLER_PACKAGES, DEFAULT_PROVIDER_PACKAGES);
    }
    
    @Test(expectedExceptions=RouteParserException.class,
            expectedExceptionsMessageRegExp="View is not defined for route: /url/")
    public void shouldGiveErrorIfViewIsNotDefined() throws IOException, URISyntaxException {
        new RoutesContainer().load(new File(getClass().getResource("/routes-no-view-error.cfg").toURI()), DEFAULT_CONTROLLER_PACKAGES, DEFAULT_PROVIDER_PACKAGES);
    }
    
    private ControllerDefinition controllerInRoute(int index) {
        return container.getRoutes().get(index).getController();
    }
    
    private String viewNameInRoute(int index) {
        return container.getRoutes().get(index).getView();
    }
    
    private RouteURL urlInRoute(int number) {
        return container.getRoutes().get(number).getUrl();
    }
    
    private List<String> list(String ... items) {
        List<String> list = new LinkedList<String>();
        for ( String item : items ) {
            list.add(item);
        }
        return list;
    }

}

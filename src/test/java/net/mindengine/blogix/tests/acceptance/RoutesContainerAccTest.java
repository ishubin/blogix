package net.mindengine.blogix.tests.acceptance;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;

import net.mindengine.blogix.components.MockedController;
import net.mindengine.blogix.web.routes.ControllerDefinition;
import net.mindengine.blogix.web.routes.Route;
import net.mindengine.blogix.web.routes.RouteProviderDefinition;
import net.mindengine.blogix.web.routes.RoutesContainer;

import org.hamcrest.Matchers;
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
    
    @SuppressWarnings("unchecked")
    @Test(dependsOnMethods = BASE_TEST)
    public void shouldParseParameterizedRoute() {
        assertThat(urlPatternInRoute(3), is("/parameterized/rout/[a-zA-Z0-9_\\-]*/gap/[a-zA-Z0-9_\\-]*"));
        assertThat(urlParametersInRoute(3), hasItems(is("param1"), is("param2")));
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
        assertThat(controllerInRoute(3).getParameters(), hasItems("param1", "param2"));
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

    @Test(dependsOnMethods = BASE_TEST)
    public void shouldGiveErrorIfParameterizedRouteDoesNotHaveProviderSpecified() {
        throw new RuntimeException("Test is not yet done");
    }
    
    @Test(dependsOnMethods = BASE_TEST)
    public void shouldGiveErrorIfRouteDoesNotStartWithSlash () {
        throw new RuntimeException("Test is not yet done");
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
}

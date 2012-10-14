package net.mindengine.blogix.tests.blog;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

import java.lang.reflect.Method;
import java.util.Map;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class BlogixProviderTest {

    
    private static Class<?> providerClass;
    
    @BeforeClass
    public void setup() throws ClassNotFoundException {
        /*
         * Loading the class after the property for filedb is set.
         * This is important since the Blog controller relies on a system property. And if this property is not set - blog will have an error in initialization.
         */
        System.setProperty("blogix.filedb.path", getClass().getResource("/controller-test-data").getFile());
        providerClass = Class.forName("net.mindengine.blogix.blog.providers.BlogixProvider");
    }
    
    
    @Test
    public void allHomePages() throws Exception {
        Map<String, Object> map[] = invokeProvider("allHomePages");
        assertThat(map, is(notNullValue()));
        assertThat(map.length, is(2));
        
        for (int i = 0; i < 2; i++) {
            assertThat(map[i].size(), is(1));
            assertThat((Integer)map[i].get("page"), is(i+1));
        }
    }
    

    @Test
    public void allPosts() throws Exception {
        Map<String, Object> map[] = invokeProvider("allPosts");
        assertThat(map, is(notNullValue()));
        assertThat(map.length, is(14));
        
        for (int i = 0; i < 2; i++) {
            assertThat(map[i].size(), is(1));
            String strId = (i<10 ? "0" : "") + (i + 1);
            assertThat((String)map[i].get("postId"), is("2012-01-01-title-" + strId));
        }
    }
    
    @Test
    public void allFilesForPosts() throws Exception {
        Map<String, Object> map[] = invokeProvider("allFilesForPosts");
        assertThat(map, is(notNullValue()));
        assertThat(map.length, is(1));
        
        assertThat(map[0].size(), is(2));
        assertThat((String)map[0].get("postId"), is("2012-01-01-title-02"));
        assertThat((String)map[0].get("fileName"), is("2012-01-01-title-02.file01.txt"));
    }
    
    @Test
    public void allSections() throws Exception {
        Map<String, Object> map[] = invokeProvider("allSections");
        assertThat(map, is(notNullValue()));
        assertThat(map.length, is(3));
        
        for (int i = 0; i < 3; i++) {
            assertThat(map[i].size(), is(1));
            assertThat("#" + i + " section should be", (String)map[i].get("section"), is("section" + (i + 1)));
        }
    }
    
    @Test
    public void allSectionsAndPages() throws Exception {
        Map<String, Object> map[] = invokeProvider("allSectionsAndPages");
        assertThat(map, is(notNullValue()));
        Object expected [][] = {{"section1", 1}, {"section1", 2}, {"section2", 1}, {"section2", 2}, {"section3", 1}};
        assertThat(map.length, is(expected.length));
        
        for (int i = 0; i < expected.length; i++) {
            assertThat("#" + i + " keys should be",map[i].size(), is(2));
            assertThat("#" + i + " section should be", map[i].get("section"), is(expected[i][0]));
            assertThat("#" + i + " page should be", map[i].get("page"), is(expected[i][1]));
        }
    }
    
    
    @SuppressWarnings("unchecked")
    private Map<String, Object>[] invokeProvider(String methodName) throws Exception {
        Method method  = providerClass.getMethod(methodName);
        return (Map<String, Object>[]) method.invoke(null);
    }
}

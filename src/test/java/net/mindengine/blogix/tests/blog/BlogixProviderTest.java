/*******************************************************************************
* Copyright 2012 Ivan Shubin http://mindengine.net
* 
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
* 
*   http://www.apache.org/licenses/LICENSE-2.0
* 
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
******************************************************************************/
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
    public void allCategories() throws Exception {
        Map<String, Object> map[] = invokeProvider("allCategories");
        assertThat(map, is(notNullValue()));
        assertThat(map.length, is(3));
        
        for (int i = 0; i < 3; i++) {
            assertThat(map[i].size(), is(1));
            assertThat("#" + i + " category should be", (String)map[i].get("category"), is("category" + (i + 1)));
        }
    }
    
    @Test
    public void allCategoriesAndPages() throws Exception {
        Map<String, Object> map[] = invokeProvider("allCategoriesAndPages");
        assertThat(map, is(notNullValue()));
        Object expected [][] = {{"category1", 1}, {"category1", 2}, {"category2", 1}, {"category2", 2}, {"category3", 1}};
        assertThat(map.length, is(expected.length));
        
        for (int i = 0; i < expected.length; i++) {
            assertThat("#" + i + " keys should be",map[i].size(), is(2));
            assertThat("#" + i + " category should be", map[i].get("category"), is(expected[i][0]));
            assertThat("#" + i + " page should be", map[i].get("page"), is(expected[i][1]));
        }
    }
    
    
    @SuppressWarnings("unchecked")
    private Map<String, Object>[] invokeProvider(String methodName) throws Exception {
        Method method  = providerClass.getMethod(methodName);
        return (Map<String, Object>[]) method.invoke(null);
    }
}

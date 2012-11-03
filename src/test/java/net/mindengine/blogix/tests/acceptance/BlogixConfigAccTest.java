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
package net.mindengine.blogix.tests.acceptance;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;

import java.util.HashMap;
import java.util.Map;

import net.mindengine.blogix.Blogix;

import org.testng.annotations.Test;

public class BlogixConfigAccTest {
    
    public static class CustomObject {
        private String name;
        private String value;
        public CustomObject(String name, String value) {
            super();
            this.name = name;
            this.value = value;
        }
        public String getName() {
            return name;
        }
        public void setName(String name) {
            this.name = name;
        }
        public String getValue() {
            return value;
        }
        public void setValue(String value) {
            this.value = value;
        }
    }

    
    @Test
    public void shouldPut_objects_toModelMap_as_modelKey() {
        Map<String, Object> model = Blogix.convertModelToMap(new CustomObject("name1", "value1"));
        assertThat("Model should not be null", model, is(notNullValue()));
        assertThat("Model should contain key", model, hasKey("model"));
        assertThat("Model 'model' property should be typeof", model.get("model"), instanceOf(CustomObject.class));
        CustomObject object = (CustomObject)model.get("model");
        assertThat("Model 'name' property should be", object.getName(), is("name1"));
        assertThat("Model 'value' property should be", object.getValue(), is("value1"));
    }
    
    @Test
    public void shouldCopyMap_intoModelMap() {
        Map<String, String> tempMap = new HashMap<String, String>();
        tempMap.put("key1", "keyvalue1");
        tempMap.put("key2", "keyvalue2");
        
        Map<String, Object> model = Blogix.convertModelToMap(tempMap);
        assertThat("Model should not be null", model, is(notNullValue()));
        assertThat("Model should not contain key", model, not(hasKey("model")));
        assertThat("Model should contain key", model, hasKey("key1"));
        assertThat("Model should contain key", model, hasKey("key2"));
        assertThat("Model 'key1' property should be", (String) model.get("key1"), is("keyvalue1"));
        assertThat("Model 'key2' property should be", (String) model.get("key2"), is("keyvalue2"));
        
    }
    
    @Test
    public void shouldAdd_userCustomProperties_toModelMap() {
        Map<String, Object> model = Blogix.convertModelToMap(new CustomObject("name1", "value1"));
        assertThat("Model should not be null", model, is(notNullValue()));
        assertThat("Model should contain key", model, hasKey("userProperty"));
        assertThat("Model userProperty should be", (String) model.get("userProperty"), is("custom value"));
    }

}

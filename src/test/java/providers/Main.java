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
package providers;

import java.util.HashMap;
import java.util.Map;

public class Main {
    
    @SuppressWarnings("rawtypes")
    public static Map[] provideArticles() {
        
        Map<String, String> article1 = new HashMap<String, String>();
        article1.put("id", "123");
        article1.put("date", "2012-01-12");
        
        Map<String, String> article2 = new HashMap<String, String>();
        article2.put("id", "1");
        article2.put("date", "2012-01-13");
        
        return new Map[]{article1, article2}; 
    }

}

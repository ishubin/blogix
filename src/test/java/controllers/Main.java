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
package controllers;

import java.io.File;
import java.net.URISyntaxException;

import models.Article;

public class Main {
    
    public static String home() {
        return "home value from controller";
    }
    
    public static Article showArticle(String date, String id) {
        
        if ( id == null || id.isEmpty() ) {
            throw new IllegalArgumentException("Can't find article with id: " + id);
        }
        return new Article(id, date, "Title for " + id, "Article description for " + id);
    }
    
    public static File file() {
        return new File(".");
    }
    
    public static File someFile() throws URISyntaxException {
        return new File(Main.class.getResource("/sample-files/someFileForViewlessRoute.txt").toURI());
    }
}

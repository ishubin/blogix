/*******************************************************************************
* Copyright 2013 Ivan Shubin http://mindengine.net
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
package net.mindengine.blogix.utils;

import java.io.File;
import java.io.FileNotFoundException;

public class BlogixFileUtils {

    /**
     * Search for a file first in a root folder of a project, then in project resources
     * @param path
     * @return
     * @throws FileNotFoundException 
     */
    public static File findFile(String path) {
        try {
            File file = new File(path);
            if ( !file.exists() ) {
                file = new File(BlogixFileUtils.class.getResource("/" + path).toURI());
            }
            return file;
        }
        catch (Exception e) {
            throw new RuntimeException("Cannot find file: " + path);
        }
    }

}

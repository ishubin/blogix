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
package views;

import java.io.File;
import java.net.URISyntaxException;

public class MyView {
    
    
    public static String customStringView() {
        return "this is a string content";
    }
    
    public static File customFileView(String argFromController) {
        try {
            return new File(MyView.class.getResource("/custom-file-for-response.txt").toURI());
        } catch (URISyntaxException e) {
            throw new RuntimeException("Couldn't find a file");
        }
    }
    
    
    public static File customImage() {
        try {
            return new File(MyView.class.getResource("/sample-files/customImage.jpg").toURI());
        } catch (URISyntaxException e) {
            throw new RuntimeException("Couldn't find a file");
        }
    }

}

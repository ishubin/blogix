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

import static org.hamcrest.Matchers.is;

import java.io.File;
import java.io.IOException;

import net.mindengine.blogix.compiler.BlogixClassLoader;
import net.mindengine.blogix.compiler.BlogixCompiler;
import net.mindengine.blogix.web.routes.RoutesContainer;

import org.apache.commons.io.FileUtils;
import org.hamcrest.MatcherAssert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class CompilerAccTest {
    
    private static final String TEST_PROJECT_ROUTES = "test-project" + File.separator + "conf" + File.separator + "routes";
    private File compilerOutputDir = new File("test-project" + File.separator + "bin");
    
    @BeforeClass
    public void beforeClass() throws IOException {
        if ( compilerOutputDir.exists() ) {
            FileUtils.cleanDirectory(compilerOutputDir);
        }
        else {
            FileUtils.forceMkdir(compilerOutputDir);
        }
    }
    
    @AfterClass
    public void afterClass() throws Exception {
        if (compilerOutputDir.exists()) {
            FileUtils.forceDelete(compilerOutputDir);
        }
    }

    @Test
    public void compilesAllSourcesSuccessfully() throws Exception {
        BlogixCompiler compiler = new BlogixCompiler();
        compiler.setSourceDir(new File("test-project" + File.separator + "src"));
        compiler.setClassesDir(compilerOutputDir);
        compiler.compile();
        
        assertThatFileExists(compilerOutputDir.getAbsolutePath() + File.separator + "controllers" + File.separator + "CustomController.class");
        assertThatFileExists(compilerOutputDir.getAbsolutePath() + File.separator + "models" + File.separator + "CustomModel.class");
    }
    
    
    @Test( dependsOnMethods = "compilesAllSourcesSuccessfully")
    public void loadsControllersSuccessfullyWithCompiledCode() throws Exception {
        RoutesContainer routesContainer = new RoutesContainer(new ClassLoader[]{getClass().getClassLoader(), new BlogixClassLoader(compilerOutputDir.getAbsolutePath())});
        routesContainer.load(new File(TEST_PROJECT_ROUTES), new String[]{"controllers"}, new String[]{"providers"});
    }

    private void assertThatFileExists(String filePath) {
        File file = new File(filePath);
        MatcherAssert.assertThat(filePath + " does not exist", file.exists(), is(true));
    }
}

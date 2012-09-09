package net.mindengine.blogix.tests.acceptance;

import static org.hamcrest.Matchers.is;

import java.io.File;
import java.io.IOException;

import net.mindengine.blogix.compiler.BlogixCompiler;

import org.apache.commons.io.FileUtils;
import org.hamcrest.MatcherAssert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class CompilerAccTest {
    
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

    private void assertThatFileExists(String filePath) {
        File file = new File(filePath);
        MatcherAssert.assertThat(filePath + " does not exist", file.exists(), is(true));
    }
}

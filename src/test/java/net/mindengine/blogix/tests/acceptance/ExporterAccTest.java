package net.mindengine.blogix.tests.acceptance;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;

import net.mindengine.blogix.export.Exporter.BlogixExporter;
import net.mindengine.blogix.tests.RequestSampleParser;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class ExporterAccTest {
    
    private static final String BASE_TESTS = "exportsAllRoutesSuccessfully";
    private File destinationDir;
    
    @BeforeClass
    public void init() throws IOException {
        destinationDir = new File("target" + File.separator + "exported-routes");
        if ( destinationDir.exists() ) {
            FileUtils.cleanDirectory(destinationDir);
        }
        else {
            FileUtils.forceMkdir(destinationDir);
        }
    }
    
    @AfterClass
    public void removeTempDirectories() throws IOException {
        if ( destinationDir.exists() ) {
            FileUtils.deleteDirectory(destinationDir);
        }
    }
    
    @Test
    public void exportsAllRoutesSuccessfully() throws Exception {
        BlogixExporter exporter = new BlogixExporter(this.destinationDir);
        exporter.exportAll();   
    }
    
    @Test(dependsOnMethods = BASE_TESTS,
            dataProvider="provideExportedFilesWithExpectedContent")
    public void exportsSimpleRoutes(String filePath, String expectedContent) throws Exception {
        assertThat(readFile(destinationDir.getPath() + filePath), is(expectedContent));
    }

    private String readFile(String path) throws IOException {
        return FileUtils.readFileToString(new File(path));
    }
    
    
    @Test
    public void shouldLoadTestRequestSamples() throws IOException, URISyntaxException {
        List<Pair<String, String>> checks = RequestSampleParser.loadRequestChecksFromFile(new File(getClass().getResource("/exported-samples.txt").toURI()));
        
        assertThat(checks.size(), is(8));
        assertThat(checks.get(0).getLeft(), is("/index.html"));
        assertThat(checks.get(1).getLeft(), is("/help/index.html"));
        assertThat(checks.get(2).getLeft(), is("/article/123/2012-01-12/index.html"));
        assertThat(checks.get(3).getLeft(), is("/article/1/2012-01-13/index.html"));
        assertThat(checks.get(4).getLeft(), is("/file/someFile.txt"));
        assertThat(checks.get(5).getLeft(), is("/no-tile/index.html"));
        assertThat(checks.get(6).getLeft(), is("/file/customView.txt"));
        assertThat(checks.get(7).getLeft(), is("/file/customFile.txt"));
    }
    
    @Test (dependsOnMethods = BASE_TESTS)
    public void exportsImageFile() throws Exception {
        File file = new File(destinationDir.getAbsolutePath() + File.separator + "file" + File.separator + "customImage.jpg");
        assertThat(file.getAbsolutePath() + " does not exist", file.exists(), is(true));
    }
    
    
    @DataProvider
    public String[][] provideExportedFilesWithExpectedContent() throws URISyntaxException, IOException {
        return RequestSampleParser.loadSamplesAsDataProvider(new File(getClass().getResource("/exported-samples.txt").toURI()));
    }

}

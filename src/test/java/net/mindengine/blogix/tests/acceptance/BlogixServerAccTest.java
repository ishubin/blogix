package net.mindengine.blogix.tests.acceptance;

import static net.mindengine.blogix.tests.TestGroups.ACCEPTANCE;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.LinkedList;
import java.util.List;

import net.mindengine.blogix.components.ThreadChecker;
import net.mindengine.blogix.components.ThreadRunnable;
import net.mindengine.blogix.web.BlogixServer;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.hamcrest.Matchers;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

@Test(groups={ACCEPTANCE})
public class BlogixServerAccTest {

    private static final String HTTP_LOCALHOST_8080 = "http://localhost:8080";
    private BlogixServer server;
    private ThreadChecker serverThread;
    
    private HttpClient httpClient = new DefaultHttpClient();
    
    @BeforeTest
    public void initialize() {
        server = new BlogixServer();
    }
    
    @Test
    public void serverShouldHaveDefault8080Port() {
        assertThat("Port should be 8080 be deafult", server.getPort(), is(8080));
    }
    
    @Test
    public void serverShouldStartupAndLockThread() throws Exception{
        final StringBuffer threadOutput = new StringBuffer();
        serverThread = ThreadChecker.runThread(new ThreadRunnable() {
            @Override
            public void run() throws Exception {
                threadOutput.append("<Started>");
                server.startServer();
                threadOutput.append("<Finished>");
            }
        });
        
        Thread.sleep(2000);
        serverThread.assertExceptions();
        assertThat( "Thread should be still running", serverThread.isRunning(), is(true) );
        assertThat( threadOutput.toString(), is( "<Started>" ) );
    }
    
    
    @Test( dependsOnMethods={"shouldLoadTestRequestSamples", "serverShouldStartupAndLockThread"},
            dataProvider="provideRequestUrlsWithExpectedResponses")
    public void serverShouldProcessRequestsAndGiveProperResponse(String requestUri, String expectedResponse) throws Exception {
        assertThat( "Incorrect configuration of sample requests", requestUri, Matchers.startsWith("/") );
        assertThat( readResponseFromUri( HTTP_LOCALHOST_8080 + requestUri ), is( expectedResponse ) );
    }
    
    private String readResponseFromUri(String requestUrl) throws Exception {
        HttpGet httpget = new HttpGet(requestUrl);
        HttpResponse response = httpClient.execute(httpget);
        int statusCode = response.getStatusLine().getStatusCode();
        if ( statusCode != 200 ) {
            throw new Exception(requestUrl + " returned " + statusCode + " status code");
        }
        
        HttpEntity entity = response.getEntity();
        try {
            if (entity != null) {
                return EntityUtils.toString(entity);
            }
            else throw new NullPointerException("Cannot read httpentity for request " + requestUrl);
        }
        catch (Exception e) {
            EntityUtils.consume(entity);
            throw e;
        }
    }

    @Test
    public void shouldLoadTestRequestSamples() throws IOException, URISyntaxException {
        List<Pair<String, String>> checks = loadRequestChecksFromFile(new File(getClass().getResource("/test-request-samples.txt").toURI()));
        
        assertThat(checks.size(), is(2));
        assertThat(checks.get(0), is(Pair.of("/url/some", "abc\ndef\n")));
        assertThat(checks.get(1), is(Pair.of("/url/some-2", "qwe")));
    }
    
    
    @DataProvider
    public String[][] provideRequestUrlsWithExpectedResponses() throws URISyntaxException, IOException {
        List<Pair<String, String>> checks = loadRequestChecksFromFile(new File(getClass().getResource("/request-samples.txt").toURI()));
        
        String[][] arr = new String[checks.size()][];
        int i=-1;
        for (Pair<String, String> check : checks) {
            i++;
            arr[i] = new String[]{check.getLeft(), check.getRight()};
        }
        return arr;
    }

    private List<Pair<String, String>> loadRequestChecksFromFile(File file) throws IOException {
        List<Pair<String, String>> samples = new LinkedList<Pair<String,String>>();
        LineIterator it = FileUtils.lineIterator(file, "UTF-8");
        
        SampleParser parser = new SampleParser(samples, it);
        while( it.hasNext() ) {
            parser = parser.process(it.nextLine());
        }
        parser.done();
        
        return samples;
    }
    
    private class SampleParser {
        private List<Pair<String, String>> samples;
        private String url = null;
        private StringBuffer response = new StringBuffer("");
        
        LineIterator lineIterator;
        public SampleParser(List<Pair<String, String>> samples, LineIterator lineIterator) {
            this.samples = samples;
            this.lineIterator = lineIterator;
        }
        public SampleParser process(String line) {
            if ( line.matches("[\\=]+") ) {
                if ( alreadyStarted() ) {
                    done();
                }
                return new SampleParser(samples, lineIterator);
            }
            else if ( alreadyStarted() ) {
                if ( response.length() > 0) {
                    response.append("\n");
                }
                response.append(line);
            }
            else {
                url = line;
            }
            return this;
        }
        public void done() {
            if ( !url.trim().isEmpty()) {
                samples.add(Pair.of(url, response.toString()));
            }
        }
        private boolean alreadyStarted() {
            return url != null && !url.isEmpty();
        }
    }
}




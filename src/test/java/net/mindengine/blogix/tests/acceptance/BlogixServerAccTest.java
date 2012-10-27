package net.mindengine.blogix.tests.acceptance;

import static net.mindengine.blogix.tests.TestGroups.ACCEPTANCE;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.List;

import net.mindengine.blogix.components.ThreadChecker;
import net.mindengine.blogix.components.ThreadRunnable;
import net.mindengine.blogix.tests.RequestSampleParser;
import net.mindengine.blogix.web.BlogixServer;

import org.apache.commons.io.FileUtils;
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
    public void initialize() throws IOException, URISyntaxException {
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
        String response = readResponseTextFromUri( HTTP_LOCALHOST_8080 + requestUri );
        assertThat( response , is( expectedResponse ) );
    }
    
    @Test( dependsOnMethods="serverShouldStartupAndLockThread",
            dataProvider="provideSampleFileResponses")
    public void shouldDownloadFileForFileResponses(String requestUri, String expectedContentType, File sampleFile) throws Exception {
        HttpResponse response = readResponseFromUri(HTTP_LOCALHOST_8080 + requestUri);
        
        try {
            assertThat("Content-Type header is not available", response.getFirstHeader("Content-Type"), is(notNullValue()));
            assertThat("Content-Type for uri '" + requestUri + "' is not as expected", expectedContentType, is(response.getFirstHeader("Content-Type").getValue()));
            if ( sampleFile != null ) {
                assertResponseStream(response.getEntity(), sampleFile);
            }
        }
        catch (Exception e) {
            throw e;
        }
        finally {
            EntityUtils.consume(response.getEntity());
        }
    }
    
    
    private void assertResponseStream(HttpEntity httpEntity, File sampleFile) throws Exception {
        StringBuffer responseBuffer = new StringBuffer();
        if (!isEqual(responseBuffer, httpEntity.getContent(), new FileInputStream(sampleFile))) {
            StringBuffer error = new StringBuffer();
            error.append("Response input stream is not equal to sample file:\n********** Stream ********\n");
            error.append(responseBuffer.toString());
            error.append("\n********* Expeted *********\n");
            error.append(FileUtils.readFileToString(sampleFile));
            throw new Exception(error.toString());
        }
    }
    
    private static boolean isEqual(StringBuffer responseBuffer, InputStream input1, InputStream input2)
            throws IOException {
        boolean error = false;
        try {
            byte[] buffer1 = new byte[1024];
            byte[] buffer2 = new byte[1024];
            try {
                int numRead1 = 0;
                int numRead2 = 0;
                while (true) {
                    numRead1 = input1.read(buffer1);
                    numRead2 = input2.read(buffer2);
                    
                    responseBuffer.append(new String(buffer1));
                    if (numRead1 > -1) {
                        if (numRead2 != numRead1) return false;
                        if (!Arrays.equals(buffer1, buffer2)) return false;
                    } else {
                        return numRead2 < 0;
                    }
                }
            } finally {
                input1.close();
            }
        } catch (IOException e) {
            error = true;
            throw e;
        } catch (RuntimeException e) {
            error = true;
            throw e;
        } finally {
            try {
                input2.close();
            } catch (IOException e) {
                if (!error) throw e;
            }
        }
    }


    private HttpResponse readResponseFromUri(String requestUrl) throws Exception {
        HttpGet httpget = new HttpGet(requestUrl);
        HttpResponse response = httpClient.execute(httpget);
        int statusCode = response.getStatusLine().getStatusCode();
        if ( statusCode != 200 ) {
            String error = EntityUtils.toString(response.getEntity());
            throw new Exception(requestUrl + " returned " + statusCode + " status code \n**************\n" + error + "\n****************\n");
        }
        return response;
    }

    private String readResponseTextFromUri(String requestUrl) throws Exception {
        HttpResponse response = readResponseFromUri(requestUrl);
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
        List<Pair<String, String>> checks = RequestSampleParser.loadRequestChecksFromFile(new File(getClass().getResource("/test-request-samples.txt").toURI()));
        
        assertThat(checks.size(), is(2));
        assertThat(checks.get(0), is(Pair.of("/url/some", "abc\ndef\n")));
        assertThat(checks.get(1), is(Pair.of("/url/some-2", "qwe")));
    }
    
    
    @DataProvider
    public String[][] provideRequestUrlsWithExpectedResponses() throws URISyntaxException, IOException {
        return RequestSampleParser.loadSamplesAsDataProvider(new File(getClass().getResource("/request-samples.txt").toURI()));
    }

    @DataProvider
    public Object[][] provideSampleFileResponses() throws Exception {
        List<Pair<String, String>> checks = RequestSampleParser.loadRequestChecksFromFile(new File(getClass().getResource("/file-request-samples.txt").toURI()));
        
        Object[][] arr = new Object[checks.size()][];
        int i=-1;
        for (Pair<String, String> check : checks) {
            i++;
            String url = check.getLeft();
            String[] right = check.getRight().split("\\|");
            String contentType = right[0].trim();
            String filePath = right[1].trim();
            File file = null;
            if ( !filePath.isEmpty() ) {
                file = new File(getClass().getResource("/" + filePath).toURI());
            }
            arr[i] = new Object[]{url, contentType, file};
        }
        return arr;
    }
}




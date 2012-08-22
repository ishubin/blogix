package net.mindengine.blogix.tests.acceptance;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import net.mindengine.blogix.components.ThreadChecker;
import net.mindengine.blogix.components.ThreadRunnable;
import net.mindengine.blogix.web.BlogixServer;

import org.testng.annotations.Test;

public class BlogixServerAccTest {

    private BlogixServer server;
    private ThreadChecker serverThread;
    
    @Test
    public void serverShouldHaveDefault8080Port() {
        server = new BlogixServer();
        assertThat("Port should be 8080 be deafult", server.getPort(), is(8080));
    }
    
    @Test(dependsOnMethods="serverShouldHaveDefault8080Port")
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
        assertThat("Thread should be still running", serverThread.isRunning(), is(true));
        assertThat(threadOutput.toString(), is("<Started>"));
    }
    
    
}

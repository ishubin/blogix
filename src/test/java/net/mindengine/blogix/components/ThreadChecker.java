package net.mindengine.blogix.components;

public class ThreadChecker {

    private Thread thread;
    private Boolean running;
    
    private Exception interruptedBy;
    
    private ThreadChecker(ThreadRunnable threadRunnable) {
        thread = new Thread(createRunnableFor(threadRunnable));
    }
    
    private Runnable createRunnableFor(final ThreadRunnable threadRunnable) {
        return new Runnable() {
            
            @Override
            public void run() {
                running = true;
                try {
                    threadRunnable.run();
                }
                catch (Exception e) {
                    interruptedBy = e;
                }
                running = false;
            }
        };
    }

    public static ThreadChecker runThread(ThreadRunnable threadRunnable) {
        return new ThreadChecker(threadRunnable).run();
    }

    private ThreadChecker run() {
        thread.start();
        return this;
    }

    public void assertExceptions() throws Exception {
        if ( interruptedBy != null ) {
            throw interruptedBy;
        }
        
    }

    public Boolean isRunning() {
        return running;
    }
    
}

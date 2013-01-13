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

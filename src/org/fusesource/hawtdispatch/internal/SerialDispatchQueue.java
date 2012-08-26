/**
 * Copyright (C) 2012 FuseSource, Inc.
 * http://fusesource.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.fusesource.hawtdispatch.internal;

import org.fusesource.hawtdispatch.DispatchQueue;
import org.fusesource.hawtdispatch.Metrics;
import org.fusesource.hawtdispatch.Task;
import org.fusesource.hawtdispatch.TaskWrapper;

import java.util.LinkedList;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 
 * @author <a href="http://hiramchirino.com">Hiram Chirino</a>
 */
public class SerialDispatchQueue extends AbstractDispatchObject implements HawtDispatchQueue {

    protected volatile String label;

    protected final AtomicBoolean triggered = new AtomicBoolean();
    protected final ConcurrentLinkedQueue<Task> externalQueue = new ConcurrentLinkedQueue<Task>();
    private final LinkedList<Task> localQueue = new LinkedList<Task>();
    private final LinkedList<Task> sourceQueue= new LinkedList<Task>();
    private final ThreadLocal<Boolean> executing = new ThreadLocal<Boolean>();
    private MetricsCollector metricsCollector = InactiveMetricsCollector.INSTANCE;

    public SerialDispatchQueue(String label) {
        this.label = label;
    }

    public void execute(Task task) {
        assert task != null;
        enqueue(metricsCollector.track(task));
    }

    @Deprecated
    public void execute(final Runnable runnable) {
        execute(new TaskWrapper(runnable));
    }

    @Deprecated()
    public void executeAfter(long delay, TimeUnit unit, Runnable runnable) {
        this.executeAfter(delay, unit, new TaskWrapper(runnable));
    }

    public LinkedList<Task> getSourceQueue() {
        return sourceQueue;
    }

    private void enqueue(Task runnable) {
        // We can take a shortcut...
        if( executing.get()!=null ) {
            localQueue.add(runnable);
        } else {
            externalQueue.add(runnable);
            triggerExecution();
        }
    }

    public void run() {
        HawtDispatchQueue original = HawtDispatcher.CURRENT_QUEUE.get();
        HawtDispatcher.CURRENT_QUEUE.set(this);
        executing.set(Boolean.TRUE);
        try {
            Task runnable;
            while( (runnable = externalQueue.poll())!=null ) {
                localQueue.add(runnable);
            }
            while(true) {
                if( isSuspended() ) {
                    return;
                }
                runnable = localQueue.poll();
                if( runnable==null ) {
                    return;
                }
                try {
                    runnable.run();
                } catch (Throwable e) {
                    Thread thread = Thread.currentThread();
                    thread.getUncaughtExceptionHandler().uncaughtException(thread, e);
                }
            }
        } finally {

            // Posts any deferred events.  This ensures
            // the next events generated by this dispatch
            // queue are received in order.
            for (Runnable runnable : sourceQueue) {
                runnable.run();
            }
            sourceQueue.clear();

            executing.remove();
            HawtDispatcher.CURRENT_QUEUE.set(original);
            triggered.set(false);
            boolean empty = externalQueue.isEmpty() && localQueue.isEmpty();
            if( !isSuspended() && !empty) {
                triggerExecution();
            }
        }
    }

    protected void triggerExecution() {
        if( triggered.compareAndSet(false, true) ) {
            getTargetQueue().execute(this);
        }
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public boolean isExecuting() {
        return executing.get()!=null;
    }

    public void assertExecuting() {
        assert isExecuting() : getDispatcher().assertMessage(getLabel());
    }


    @Override
    protected void onStartup() {
        triggerExecution();
    }

    @Override
    protected void onResume() {
        triggerExecution();
    }

    public QueueType getQueueType() {
        return QueueType.SERIAL_QUEUE;
    }

    public void executeAfter(long delay, TimeUnit unit, Task task) {
        getDispatcher().timerThread.addRelative(task, this, delay, unit);
    }

    public DispatchQueue createQueue(String label) {
        DispatchQueue rc = getDispatcher().createQueue(label);
        rc.setTargetQueue(this);
        return rc;
    }

    public HawtDispatcher getDispatcher() {
        HawtDispatchQueue target = getTargetQueue();
        if (target ==null ) {
            throw new UnsupportedOperationException();
        }
        return target.getDispatcher();
    }

    public SerialDispatchQueue isSerialDispatchQueue() {
        return this;
    }

    public ThreadDispatchQueue isThreadDispatchQueue() {
        return null;
    }

    public GlobalDispatchQueue isGlobalDispatchQueue() {
        return null;
    }

    public void profile(boolean on) {
        if( !on && metricsCollector==InactiveMetricsCollector.INSTANCE )
            return;

        if( on ) {
            metricsCollector = new ActiveMetricsCollector(this);
            getDispatcher().track(this);
        } else {
//            getDispatcher().untrack(this);
            metricsCollector = InactiveMetricsCollector.INSTANCE;
        }
    }

    public Metrics metrics() {
        return metricsCollector.metrics();
    }

    private int drains() {
        return getDispatcher().drains;
    }



    @Override
    public String toString() {
        if( label == null ) {
            return "serial queue";
        } else {
            return "serial queue { label: \""+label+"\" }";
        }
    }
}
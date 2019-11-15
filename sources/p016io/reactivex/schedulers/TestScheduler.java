package p016io.reactivex.schedulers;

import java.util.Queue;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.TimeUnit;
import p016io.reactivex.Scheduler;
import p016io.reactivex.Scheduler.Worker;
import p016io.reactivex.disposables.Disposable;
import p016io.reactivex.disposables.Disposables;
import p016io.reactivex.internal.disposables.EmptyDisposable;
import p016io.reactivex.internal.functions.ObjectHelper;

/* renamed from: io.reactivex.schedulers.TestScheduler */
public final class TestScheduler extends Scheduler {
    long counter;
    final Queue<TimedRunnable> queue = new PriorityBlockingQueue(11);
    volatile long time;

    /* renamed from: io.reactivex.schedulers.TestScheduler$TestWorker */
    final class TestWorker extends Worker {
        volatile boolean disposed;

        TestWorker() {
        }

        public void dispose() {
            this.disposed = true;
        }

        public boolean isDisposed() {
            return this.disposed;
        }

        public Disposable schedule(Runnable runnable, long j, TimeUnit timeUnit) {
            if (this.disposed) {
                return EmptyDisposable.INSTANCE;
            }
            long nanos = TestScheduler.this.time + timeUnit.toNanos(j);
            TestScheduler testScheduler = TestScheduler.this;
            long j2 = testScheduler.counter;
            testScheduler.counter = 1 + j2;
            final TimedRunnable timedRunnable = new TimedRunnable(this, nanos, runnable, j2);
            TestScheduler.this.queue.add(timedRunnable);
            return Disposables.fromRunnable(new Runnable() {
                public void run() {
                    TestScheduler.this.queue.remove(timedRunnable);
                }
            });
        }

        public Disposable schedule(Runnable runnable) {
            if (this.disposed) {
                return EmptyDisposable.INSTANCE;
            }
            TestScheduler testScheduler = TestScheduler.this;
            long j = testScheduler.counter;
            testScheduler.counter = 1 + j;
            final TimedRunnable timedRunnable = new TimedRunnable(this, 0, runnable, j);
            TestScheduler.this.queue.add(timedRunnable);
            return Disposables.fromRunnable(new Runnable() {
                public void run() {
                    TestScheduler.this.queue.remove(timedRunnable);
                }
            });
        }

        public long now(TimeUnit timeUnit) {
            return TestScheduler.this.now(timeUnit);
        }
    }

    /* renamed from: io.reactivex.schedulers.TestScheduler$TimedRunnable */
    static final class TimedRunnable implements Comparable<TimedRunnable> {
        final long count;
        final Runnable run;
        final TestWorker scheduler;
        final long time;

        TimedRunnable(TestWorker testWorker, long j, Runnable runnable, long j2) {
            this.time = j;
            this.run = runnable;
            this.scheduler = testWorker;
            this.count = j2;
        }

        public String toString() {
            return String.format("TimedRunnable(time = %d, run = %s)", new Object[]{Long.valueOf(this.time), this.run.toString()});
        }

        public int compareTo(TimedRunnable timedRunnable) {
            if (this.time == timedRunnable.time) {
                return ObjectHelper.compare(this.count, timedRunnable.count);
            }
            return ObjectHelper.compare(this.time, timedRunnable.time);
        }
    }

    public long now(TimeUnit timeUnit) {
        return timeUnit.convert(this.time, TimeUnit.NANOSECONDS);
    }

    public void advanceTimeBy(long j, TimeUnit timeUnit) {
        advanceTimeTo(this.time + timeUnit.toNanos(j), TimeUnit.NANOSECONDS);
    }

    public void advanceTimeTo(long j, TimeUnit timeUnit) {
        triggerActions(timeUnit.toNanos(j));
    }

    public void triggerActions() {
        triggerActions(this.time);
    }

    private void triggerActions(long j) {
        while (!this.queue.isEmpty()) {
            TimedRunnable timedRunnable = (TimedRunnable) this.queue.peek();
            if (timedRunnable.time > j) {
                break;
            }
            this.time = timedRunnable.time == 0 ? this.time : timedRunnable.time;
            this.queue.remove();
            if (!timedRunnable.scheduler.disposed) {
                timedRunnable.run.run();
            }
        }
        this.time = j;
    }

    public Worker createWorker() {
        return new TestWorker();
    }
}

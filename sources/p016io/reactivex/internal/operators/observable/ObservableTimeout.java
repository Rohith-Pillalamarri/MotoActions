package p016io.reactivex.internal.operators.observable;

import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicReference;
import p016io.reactivex.ObservableSource;
import p016io.reactivex.Observer;
import p016io.reactivex.disposables.Disposable;
import p016io.reactivex.exceptions.Exceptions;
import p016io.reactivex.functions.Function;
import p016io.reactivex.internal.disposables.DisposableHelper;
import p016io.reactivex.internal.disposables.ObserverFullArbiter;
import p016io.reactivex.internal.functions.ObjectHelper;
import p016io.reactivex.internal.observers.FullArbiterObserver;
import p016io.reactivex.observers.DisposableObserver;
import p016io.reactivex.observers.SerializedObserver;
import p016io.reactivex.plugins.RxJavaPlugins;

/* renamed from: io.reactivex.internal.operators.observable.ObservableTimeout */
public final class ObservableTimeout<T, U, V> extends AbstractObservableWithUpstream<T, T> {
    final ObservableSource<U> firstTimeoutIndicator;
    final Function<? super T, ? extends ObservableSource<V>> itemTimeoutIndicator;
    final ObservableSource<? extends T> other;

    /* renamed from: io.reactivex.internal.operators.observable.ObservableTimeout$OnTimeout */
    interface OnTimeout {
        void innerError(Throwable th);

        void timeout(long j);
    }

    /* renamed from: io.reactivex.internal.operators.observable.ObservableTimeout$TimeoutInnerObserver */
    static final class TimeoutInnerObserver<T, U, V> extends DisposableObserver<Object> {
        boolean done;
        final long index;
        final OnTimeout parent;

        TimeoutInnerObserver(OnTimeout onTimeout, long j) {
            this.parent = onTimeout;
            this.index = j;
        }

        public void onNext(Object obj) {
            if (!this.done) {
                this.done = true;
                dispose();
                this.parent.timeout(this.index);
            }
        }

        public void onError(Throwable th) {
            if (this.done) {
                RxJavaPlugins.onError(th);
                return;
            }
            this.done = true;
            this.parent.innerError(th);
        }

        public void onComplete() {
            if (!this.done) {
                this.done = true;
                this.parent.timeout(this.index);
            }
        }
    }

    /* renamed from: io.reactivex.internal.operators.observable.ObservableTimeout$TimeoutObserver */
    static final class TimeoutObserver<T, U, V> extends AtomicReference<Disposable> implements Observer<T>, Disposable, OnTimeout {
        private static final long serialVersionUID = 2672739326310051084L;
        final Observer<? super T> actual;
        final ObservableSource<U> firstTimeoutIndicator;
        volatile long index;
        final Function<? super T, ? extends ObservableSource<V>> itemTimeoutIndicator;

        /* renamed from: s */
        Disposable f483s;

        TimeoutObserver(Observer<? super T> observer, ObservableSource<U> observableSource, Function<? super T, ? extends ObservableSource<V>> function) {
            this.actual = observer;
            this.firstTimeoutIndicator = observableSource;
            this.itemTimeoutIndicator = function;
        }

        public void onSubscribe(Disposable disposable) {
            if (DisposableHelper.validate(this.f483s, disposable)) {
                this.f483s = disposable;
                Observer<? super T> observer = this.actual;
                ObservableSource<U> observableSource = this.firstTimeoutIndicator;
                if (observableSource != null) {
                    TimeoutInnerObserver timeoutInnerObserver = new TimeoutInnerObserver(this, 0);
                    if (compareAndSet(null, timeoutInnerObserver)) {
                        observer.onSubscribe(this);
                        observableSource.subscribe(timeoutInnerObserver);
                        return;
                    }
                    return;
                }
                observer.onSubscribe(this);
            }
        }

        public void onNext(T t) {
            long j = this.index + 1;
            this.index = j;
            this.actual.onNext(t);
            Disposable disposable = (Disposable) get();
            if (disposable != null) {
                disposable.dispose();
            }
            try {
                ObservableSource observableSource = (ObservableSource) ObjectHelper.requireNonNull(this.itemTimeoutIndicator.apply(t), "The ObservableSource returned is null");
                TimeoutInnerObserver timeoutInnerObserver = new TimeoutInnerObserver(this, j);
                if (compareAndSet(disposable, timeoutInnerObserver)) {
                    observableSource.subscribe(timeoutInnerObserver);
                }
            } catch (Throwable th) {
                Exceptions.throwIfFatal(th);
                dispose();
                this.actual.onError(th);
            }
        }

        public void onError(Throwable th) {
            DisposableHelper.dispose(this);
            this.actual.onError(th);
        }

        public void onComplete() {
            DisposableHelper.dispose(this);
            this.actual.onComplete();
        }

        public void dispose() {
            if (DisposableHelper.dispose(this)) {
                this.f483s.dispose();
            }
        }

        public boolean isDisposed() {
            return this.f483s.isDisposed();
        }

        public void timeout(long j) {
            if (j == this.index) {
                dispose();
                this.actual.onError(new TimeoutException());
            }
        }

        public void innerError(Throwable th) {
            this.f483s.dispose();
            this.actual.onError(th);
        }
    }

    /* renamed from: io.reactivex.internal.operators.observable.ObservableTimeout$TimeoutOtherObserver */
    static final class TimeoutOtherObserver<T, U, V> extends AtomicReference<Disposable> implements Observer<T>, Disposable, OnTimeout {
        private static final long serialVersionUID = -1957813281749686898L;
        final Observer<? super T> actual;
        final ObserverFullArbiter<T> arbiter;
        boolean done;
        final ObservableSource<U> firstTimeoutIndicator;
        volatile long index;
        final Function<? super T, ? extends ObservableSource<V>> itemTimeoutIndicator;
        final ObservableSource<? extends T> other;

        /* renamed from: s */
        Disposable f484s;

        TimeoutOtherObserver(Observer<? super T> observer, ObservableSource<U> observableSource, Function<? super T, ? extends ObservableSource<V>> function, ObservableSource<? extends T> observableSource2) {
            this.actual = observer;
            this.firstTimeoutIndicator = observableSource;
            this.itemTimeoutIndicator = function;
            this.other = observableSource2;
            this.arbiter = new ObserverFullArbiter<>(observer, this, 8);
        }

        public void onSubscribe(Disposable disposable) {
            if (DisposableHelper.validate(this.f484s, disposable)) {
                this.f484s = disposable;
                this.arbiter.setDisposable(disposable);
                Observer<? super T> observer = this.actual;
                ObservableSource<U> observableSource = this.firstTimeoutIndicator;
                if (observableSource != null) {
                    TimeoutInnerObserver timeoutInnerObserver = new TimeoutInnerObserver(this, 0);
                    if (compareAndSet(null, timeoutInnerObserver)) {
                        observer.onSubscribe(this.arbiter);
                        observableSource.subscribe(timeoutInnerObserver);
                        return;
                    }
                    return;
                }
                observer.onSubscribe(this.arbiter);
            }
        }

        public void onNext(T t) {
            if (!this.done) {
                long j = this.index + 1;
                this.index = j;
                if (this.arbiter.onNext(t, this.f484s)) {
                    Disposable disposable = (Disposable) get();
                    if (disposable != null) {
                        disposable.dispose();
                    }
                    try {
                        ObservableSource observableSource = (ObservableSource) ObjectHelper.requireNonNull(this.itemTimeoutIndicator.apply(t), "The ObservableSource returned is null");
                        TimeoutInnerObserver timeoutInnerObserver = new TimeoutInnerObserver(this, j);
                        if (compareAndSet(disposable, timeoutInnerObserver)) {
                            observableSource.subscribe(timeoutInnerObserver);
                        }
                    } catch (Throwable th) {
                        Exceptions.throwIfFatal(th);
                        this.actual.onError(th);
                    }
                }
            }
        }

        public void onError(Throwable th) {
            if (this.done) {
                RxJavaPlugins.onError(th);
                return;
            }
            this.done = true;
            dispose();
            this.arbiter.onError(th, this.f484s);
        }

        public void onComplete() {
            if (!this.done) {
                this.done = true;
                dispose();
                this.arbiter.onComplete(this.f484s);
            }
        }

        public void dispose() {
            if (DisposableHelper.dispose(this)) {
                this.f484s.dispose();
            }
        }

        public boolean isDisposed() {
            return this.f484s.isDisposed();
        }

        public void timeout(long j) {
            if (j == this.index) {
                dispose();
                this.other.subscribe(new FullArbiterObserver(this.arbiter));
            }
        }

        public void innerError(Throwable th) {
            this.f484s.dispose();
            this.actual.onError(th);
        }
    }

    public ObservableTimeout(ObservableSource<T> observableSource, ObservableSource<U> observableSource2, Function<? super T, ? extends ObservableSource<V>> function, ObservableSource<? extends T> observableSource3) {
        super(observableSource);
        this.firstTimeoutIndicator = observableSource2;
        this.itemTimeoutIndicator = function;
        this.other = observableSource3;
    }

    public void subscribeActual(Observer<? super T> observer) {
        if (this.other == null) {
            this.source.subscribe(new TimeoutObserver(new SerializedObserver(observer), this.firstTimeoutIndicator, this.itemTimeoutIndicator));
        } else {
            this.source.subscribe(new TimeoutOtherObserver(observer, this.firstTimeoutIndicator, this.itemTimeoutIndicator, this.other));
        }
    }
}

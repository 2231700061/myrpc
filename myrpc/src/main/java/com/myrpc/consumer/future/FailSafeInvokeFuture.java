package com.myrpc.consumer.future;

import com.myrpc.common.ReflectUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

import static com.myrpc.common.StackTraceUtils.stackTrace;

public class FailSafeInvokeFuture<V> extends AbstractFuture<V> {

	private static final Logger logger = LoggerFactory.getLogger(FailSafeInvokeFuture.class);

	private final InvokeFuture<V> future;

	public static <T> FailSafeInvokeFuture<T> with(InvokeFuture<T> future) {
		return new FailSafeInvokeFuture<>(future);
	}

	private FailSafeInvokeFuture(InvokeFuture<V> future) {
		this.future = future;
	}

	@Override
	public Class<V> returnType() {
		return future.returnType();
	}

	@Override
	public boolean isDone() {
		return future.isDone();
	}

	@Override
	public V get() {
		try {
			return future.get();
		} catch (Throwable t) {
			if (logger.isWarnEnabled()) {
				logger.warn("Ignored exception on [Fail-safe]: {}.", stackTrace(t));
			}
		}
		return (V) ReflectUtils.getTypeDefaultValue(returnType());
	}


	@Override
	public V get(long timeout, TimeUnit unit) {
		try {
			return future.get(timeout, unit);
		} catch (Throwable t) {
			if (logger.isWarnEnabled()) {
				logger.warn("Ignored exception on [Fail-safe]: {}.", stackTrace(t));
			}
		}
		return (V) ReflectUtils.getTypeDefaultValue(returnType());
	}

	@Override
	protected void notifyListener0(Listener<V> listener, int state, Object x) {
	}

	@Override
	protected void done(int state, Object x) {
	}

	public InvokeFuture<V> future() {
		return future;
	}
}

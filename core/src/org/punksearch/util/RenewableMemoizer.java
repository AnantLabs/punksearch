/***************************************************************************
 *                                                                         *
 *   PunkSearch - Searching over LAN                                       *
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package org.punksearch.util;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Modified version of Memoizer by Brian Goetz and Tim Peierls (borrowed from Java Concurrency in Practice book)
 * http://javaconcurrencyinpractice.com/listings/Memoizer.java
 * 
 * Added cache renewal (either active or passive)
 * 
 * @author Yury Soldak (ysoldak@gmail.com)
 * 
 */
public class RenewableMemoizer<A, V> implements Computable<A, V> {
	private static Log                        __log      = LogFactory.getLog(RenewableMemoizer.class);

	private final ConcurrentMap<A, Future<V>> cache      = new ConcurrentHashMap<A, Future<V>>();
	private final Computable<A, V>            computable;
	private final ConcurrentMap<A, Long>      timestamps = new ConcurrentHashMap<A, Long>();
	private final long                        timeout;

	public RenewableMemoizer(Computable<A, V> computable) {
		this(computable, Long.MAX_VALUE);
	}

	public RenewableMemoizer(Computable<A, V> computable, long timeout) {
		this(computable, timeout, false);
	}

	public RenewableMemoizer(Computable<A, V> computable, long timeout, boolean active) {
		this.computable = computable;
		this.timeout = timeout;
		if (active) {
			activateRenewal();
		}
	}

	private void activateRenewal() {
		TimerTask activeRenewTask = new TimerTask() {
			public void run() {
				for (A key : cache.keySet()) {
					try {
						compute(key);
					} catch (InterruptedException e) {
						break;
					}
				}
			}
		};
		new Timer(true).scheduleAtFixedRate(activeRenewTask, Math.round(timeout * 1.1), Math.round(timeout * 1.1));
	}

	public V compute(final A arg) throws InterruptedException {
		__log.debug("Compute: " + arg);
		while (true) {
			Future<V> f = cache.get(arg);
			if (f == null) {
				FutureTask<V> ft = makeFutureTask(arg);
				f = cache.putIfAbsent(arg, ft);
				if (f == null) {
					f = ft;
					ft.run();
				}
			} else {
				assert timestamps.get(arg) != null; // once put to timestamps value is never removed from it
				if (timestamps.get(arg) + timeout < System.currentTimeMillis()) {
					__log.debug("Item expired, removing from cache: " + arg);
					cache.remove(arg, f);
					continue;
				}
			}
			try {
				return f.get();
			} catch (CancellationException e) {
				cache.remove(arg, f);
			} catch (ExecutionException e) {
				throw LaunderThrowable.launderThrowable(e.getCause());
			}
		}
	}

	private FutureTask<V> makeFutureTask(final A arg) {
		return new FutureTask<V>(makeCallable(arg));
	}

	private Callable<V> makeCallable(final A arg) {
		Callable<V> eval = new Callable<V>() {
			public V call() throws InterruptedException {
				timestamps.put(arg, Long.MAX_VALUE);
				V value = computable.compute(arg);
				timestamps.put(arg, System.currentTimeMillis());
				return value;
			}
		};
		return eval;
	}

}

class LaunderThrowable {

	/**
	 * If the Throwable is an Error, throw it; if it is a RuntimeException return it, otherwise throw
	 * IllegalStateException
	 */
	public static RuntimeException launderThrowable(Throwable t) {
		if (t instanceof RuntimeException)
			return (RuntimeException) t;
		else if (t instanceof Error)
			throw (Error) t;
		else
			throw new IllegalStateException("Not unchecked", t);
	}

}
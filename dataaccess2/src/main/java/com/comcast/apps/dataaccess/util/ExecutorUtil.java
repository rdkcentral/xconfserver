/*
 * If not stated otherwise in this file or this component's Licenses.txt file the
 * following copyright and licenses apply:
 *
 * Copyright 2019 RDK Management
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * @author Roman Dolomansky (rdolomansky@productengine.com)
 */
package com.comcast.apps.dataaccess.util;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;


public class ExecutorUtil {

    private static Logger logger = LoggerFactory.getLogger(ExecutorUtil.class);

    private static final int numberOfEntriesToProcessSequentially = 10000;
    private static final int threadsAvailable = Runtime.getRuntime().availableProcessors();
    private static final BlockingQueue<Runnable> asyncTaskQueue = new ArrayBlockingQueue<>(30000);
    private static final ExecutorService executor = new ThreadPoolExecutor(
            threadsAvailable, threadsAvailable * 8, 100, TimeUnit.SECONDS, asyncTaskQueue, new ThreadPoolExecutor.DiscardOldestPolicy());
    private static final ListeningExecutorService asyncTaskProcessor = MoreExecutors.listeningDecorator(executor);

    public static ExecutorService getAsyncTaskProcessor() {
        return asyncTaskProcessor;
    }

    public static void doAsync(final Runnable task) {
        asyncTaskProcessor.submit(task);
    }

    /**
     * processes large amounts of data in mapreduce like fashion
     *
     * @param source     source iterable that must be processed
     * @param sourceSize size (since iterable does not allow to obtain size)
     * @param predicate  predicate to do filtering on
     * @return filtered on predicate and source parameters subset
     */
    public static <T> Iterable<T> doParallelFilter(final Iterable<T> source, final int sourceSize, final Predicate<? super T> predicate) {
        if (sourceSize == 0) {
            return Lists.newArrayList();
        } else if (sourceSize > numberOfEntriesToProcessSequentially) {
            final List<ListenableFuture<List<T>>> partitionFutures = new ArrayList<>(sourceSize / threadsAvailable);

            for (final List<T> partition : Iterables.partition(source, sourceSize / threadsAvailable)) {
                partitionFutures.add(asyncTaskProcessor.submit(new Callable<List<T>>() {
                    @Override
                    public List<T> call() throws Exception {
                        return Lists.newArrayList(Iterables.filter(partition, predicate));
                    }
                }));
            }
            try {
                return Iterables.concat(Futures.successfulAsList(partitionFutures).get());
            } catch (InterruptedException | ExecutionException e) {
                logger.error("Error happened during async processing ", e);
            }
            return Lists.newArrayList();
        } else {
            return Iterables.filter(source, predicate);
        }
    }
}

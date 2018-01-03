package com.doepiccoding.viewmodel.models;

import android.arch.lifecycle.LifecycleOwner;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModel;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicBoolean;

public abstract class PaginationViewModel<T> extends ViewModel {

    private static final String TAG = "PaginationViewModel";
    private AtomicBoolean shouldPropagateInitialLoad = new AtomicBoolean(false);
    private ConcurrentMap<String, Runnable> pagesIdsHandled = new ConcurrentHashMap();
    private MutableLiveData<String> pageId = new MutableLiveData();
    private MutableLiveData<List<T>> fullData = new MutableLiveData();
    private PaginationObserver clientObserver;
    private String transactionKey = UUID.randomUUID().toString();

    private Handler uiThreadPoster = new Handler(Looper.getMainLooper());
    private HandlerThread pipelineThread = new HandlerThread("PagesPipelineThread");
    private Handler pipelineHandler;

    {
        pipelineThread.start();
        pipelineHandler = new Handler(pipelineThread.getLooper());
    }

    public void observePagination(final @NonNull PaginationSubscriber paginationSubscriber) {
        clientObserver = paginationSubscriber.getPaginationObserver();
        LifecycleOwner lifecycleOwner = paginationSubscriber.getLifecycleOwner();
        if (clientObserver == null || lifecycleOwner == null) {
            throw new IllegalStateException("LifeCycleOwner and Observer can't be null");
        }

        pageId.observe(lifecycleOwner, pageObserver);
        fullData.observe(lifecycleOwner, fullDataObserver);

        //Wait until the first call to paginate...
        if (fullData.getValue() == null || fullData.getValue().isEmpty()) {
            shouldPropagateInitialLoad.set(false);
        } else {
            //Just resend current state
            Log.d(TAG, "PaginationViewModel [Lifecycle] Pass loaded data...");
            shouldPropagateInitialLoad.set(true);
            fullData.setValue(fullData.getValue());
        }
    }

    public void nextPage(String pageId) {
        this.pageId.setValue(pageId);
    }

    public synchronized void reset() {
        pageId.setValue(null);
        fullData.setValue(null);
        drainQueuedMessages();
        clientObserver.onReset();
        transactionKey = UUID.randomUUID().toString();
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        synchronized (PaginationViewModel.this) {
            Log.e(TAG, "Release resources!");
            pageId.removeObserver(pageObserver);
            fullData.removeObserver(fullDataObserver);
            drainQueuedMessages();
            pipelineThread.quit();
            clientObserver = null;
            uiThreadPoster = null;
        }
    }

    private void drainQueuedMessages() {
        //Drain all the queued messages out of the handler
        for(Map.Entry<String, Runnable> entry : pagesIdsHandled.entrySet()) {
            pipelineHandler.removeCallbacks(entry.getValue());
        }
        pagesIdsHandled.clear();
    }

    private Observer pageObserver = new Observer<String>() {
        @Override
        public void onChanged(@NotNull final String pageId) {
            synchronized (PaginationViewModel.this) {
                String key = getKey(pageId);
                if (TextUtils.isEmpty(pageId) || pagesIdsHandled.containsKey(key)) return;

                Runnable runnable = new PageRunnable(pageId);
                pagesIdsHandled.put(key, runnable);

                //Load in the background...
                pipelineHandler.post(runnable);
            }
        }
    };

    private Observer fullDataObserver = new Observer<List<T>>() {
        @Override
        public void onChanged(@Nullable List<T> data) {
            //Propagate delta data if necessary...
            if (shouldPropagateInitialLoad.getAndSet(false) && (data != null && !data.isEmpty())) {
                clientObserver.onReloaded(data);
            }
        }
    };

    public abstract List<T> getPageData(String pageId);

    private class PageRunnable implements Runnable {
        String key;
        String pageId;
        public PageRunnable(String pageId) {
            this.key = getKey(pageId);
            this.pageId = pageId;
        }
        @Override
        public void run() {

            //Check that the pageId still needs to be handled other way disregard this queued message
            if (!pagesIdsHandled.containsKey(key))return;

            //Load the page in the background thread...
            final List<T> newPageData = getPageData(pageId);

            synchronized (PaginationViewModel.this) {
                if (uiThreadPoster != null) {
                    uiThreadPoster.post(new Runnable() {
                        @Override
                        public void run() {
                            synchronized (PaginationViewModel.this) {
                                //Check that the pageId still needs to be handled other way disregard this queued message
                                if (!pagesIdsHandled.containsKey(key))return;
                                pagesIdsHandled.put(key, runnableCleaner);//Release the runnable but leave the key to track handled pages

                                //Get and assign the new values to the full data on the ui thread...
                                final List oldData = fullData.getValue() != null ? new ArrayList(fullData.getValue()) : new ArrayList();
                                oldData.addAll(newPageData);
                                fullData.setValue(oldData);
                                clientObserver.onNewPage(newPageData, pageId);
                            }
                        }
                    });
                }
            }
        }
    }

    private Runnable runnableCleaner = new Runnable() {public void run() {}};

    private String getKey(String pageId) {
        return transactionKey + pageId;
    }

    public interface PaginationObserver<T> {
        void onNewPage(@Nullable List<T> pageSubsetOnly, String pageId);
        void onReloaded(@Nullable List<T> entireData);
        void onReset();
    }

    public interface PaginationSubscriber {
        LifecycleOwner getLifecycleOwner();

        PaginationObserver getPaginationObserver();
    }
}

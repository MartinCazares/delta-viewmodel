package com.doepiccoding.viewmodel.models;

import android.arch.lifecycle.LifecycleOwner;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModel;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public abstract class CollectionViewModel<T> extends ViewModel {

    private static final String TAG = "DeltaViewModel";
    private static final int SET_IN_LAST_POSITION = -1;
    private AtomicBoolean shouldPropagateLoadAll = new AtomicBoolean(false);
    private MutableLiveData<List<T>> baseData = new MutableLiveData();
    private DeltaObserver clientObserver;

    public void observeDelta(final DeltaSubscriber deltaSubscriber) {
        shouldPropagateLoadAll.set(true);
        clientObserver = deltaSubscriber.getDeltaObserver();
        LifecycleOwner lifecycleOwner = deltaSubscriber.getLifecycleOwner();
        if (clientObserver == null || lifecycleOwner == null) {
            throw new IllegalStateException("LifeCycleOwner and Observer can't be null");
        }

        baseData.observe(lifecycleOwner, new Observer<List<T>>() {
            @Override
            public void onChanged(@Nullable List<T> objects) {
                //Propagate delta data if necessary...
                if (shouldPropagateLoadAll.getAndSet(false)) {
                    clientObserver.onLoaded(objects);
                }
            }
        });

        //Load all ONLY if really necessary...
        if (baseData.getValue() == null || baseData.getValue().isEmpty()) {
            //Load in the background...
            new Thread(new Runnable() {
                @Override
                public void run() {
                    Log.d(TAG, "DeltaViewModel [Lifecycle] Loading all data...");
                    loadData(loadInitialData());
                }
            }).start();
        }else {
            //Just resent current state
            Log.d(TAG, "DeltaViewModel [Lifecycle] Pass loaded data...");
            baseData.setValue(baseData.getValue());
        }
    }

    /**
     * This method should return the very first initial state of the
     * data set that you want to keep
     *
     * Note: It runs on a background thread, so, nothing UI Thread
     * dependent should be added in this method.
     */
    public abstract List<T> loadInitialData();

    /**
     * This method should be overriden if you need to
     * persist the data passed on updateData
     */
    public abstract void updatePersistedData(T updatedData, DeltaAction action, int position);


    public void addData(T data) {
        addData(data, SET_IN_LAST_POSITION);
    }

    public synchronized void addData(T data, int position) {

        //Notify the subclass that has to persist data if necessary...
        updatePersistedData(data, DeltaAction.ADD, position);

        //Update the base data, so that when required, the entire set will be available...
        List oldData = new ArrayList(baseData.getValue() != null ? baseData.getValue() : new ArrayList());
        if (position == SET_IN_LAST_POSITION) {
            oldData.add(data);
        }else {
            oldData.add(position, data);
        }
        baseData.setValue(oldData);

        //Propagate the callback down to the client...
        clientObserver.onChanged(data, DeltaAction.ADD, position == SET_IN_LAST_POSITION ? oldData.size() - 1 : position);
        Log.d(TAG, "DeltaViewModel [Lifecycle] Delegate down added data...");
    }

    public synchronized void removeData(@NonNull T data) {
        List oldData = new ArrayList(baseData.getValue());

        //Find object in the list...
        for (int i = 0, size = oldData.size(); i < size; i++) {
            if (data.equals(oldData.get(i))) {
                removeData(i);
                break;
            }
        }
    }

    public synchronized void removeData(int position) {

        T data;
        //Update the base data, so that when required, the entire set will be available...
        List oldData = new ArrayList(baseData.getValue());
        if (position >= 0) {
            data = (T) oldData.remove(position);
        } else {
            throw new IndexOutOfBoundsException(String.format("collection size: %d , index to remove: %d", oldData.size(), position));
        }

        //Notify the subclass that has to persist data if necessary...
        updatePersistedData(data, DeltaAction.REMOVE, position);


        baseData.setValue(oldData);

        //Propagate the callback down to the client...
        clientObserver.onChanged(data, DeltaAction.REMOVE, position);
        Log.d(TAG, "DeltaViewModel [Lifecycle] Delegate down removed data...");
    }

    private synchronized void loadData(List<T> data) {
        baseData.postValue(data);
    }

    public enum DeltaAction {
        ADD,
        REMOVE
    }

    public interface DeltaObserver<T> {
        void onChanged(@Nullable T t, DeltaAction action, int position);
        void onLoaded(@Nullable List<T> t);
    }

    public interface DeltaSubscriber {
        LifecycleOwner getLifecycleOwner();
        DeltaObserver getDeltaObserver();
    }
}

package com.stardust.scriptdroid.service;

import android.accessibilityservice.AccessibilityServiceInfo;
import android.content.Context;
import android.os.Build;
import android.util.Log;
import android.view.KeyEvent;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import com.nickandjerry.dynamiclayoutinflator.lib.DynamicLayoutInflator;
import com.stardust.scriptdroid.tool.AccessibilityServiceTool;
import com.stardust.view.accessibility.AccessibilityDelegate;
import com.stardust.view.accessibility.AccessibilityService;
import com.stardust.view.accessibility.AccessibilityServiceUtils;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * Created by Stardust on 2017/2/14.
 */

public class AccessibilityWatchDogService extends AccessibilityService {

    private static final String TAG = "AccessibilityWatchDog";

    private static final SortedMap<Integer, AccessibilityDelegate> mDelegates = new TreeMap<>();
    private static final Object LOCK = new Object();
    private static AccessibilityWatchDogService instance;
    private static boolean containsAllEventTypes = false;
    private static final Set<Integer> eventTypes = new HashSet<>();

    public static void addDelegate(int uniquePriority, AccessibilityDelegate delegate) {
        mDelegates.put(uniquePriority, delegate);
        Set<Integer> set = delegate.getEventTypes();
        if (set == null)
            containsAllEventTypes = true;
        else
            eventTypes.addAll(set);
    }

    public static boolean isEnable(Context context) {
        return AccessibilityServiceUtils.isAccessibilityServiceEnabled(context, AccessibilityWatchDogService.class);
    }

    public static AccessibilityWatchDogService getInstance() {
        return instance;
    }

    @Override
    public void onAccessibilityEvent(final AccessibilityEvent event) {
        if (event.getEventType() == AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED) {

        }
        Log.v(TAG, "onAccessibilityEvent: " + event);
        if (!containsAllEventTypes && !eventTypes.contains(event.getEventType()))
            return;
        for (Map.Entry<Integer, AccessibilityDelegate> entry : mDelegates.entrySet()) {
            AccessibilityDelegate delegate = entry.getValue();
            Set<Integer> types = delegate.getEventTypes();
            if (types != null && !delegate.getEventTypes().contains(event.getEventType()))
                continue;
            long start = System.currentTimeMillis();
            if (delegate.onAccessibilityEvent(AccessibilityWatchDogService.this, event))
                break;
            Log.v(TAG, "millis: " + (System.currentTimeMillis() - start) + " delegate: " + entry.getValue().getClass().getName());
        }
    }

    @Override
    protected boolean onKeyEvent(KeyEvent event) {
        Log.v(TAG, "onKeyEvent: " + event);
        return super.onKeyEvent(event);
    }

    @Override
    protected boolean onGesture(int gestureId) {
        Log.v(TAG, "onGesture: " + gestureId);
        return super.onGesture(gestureId);
    }

    @Override
    public void onInterrupt() {
    }

    @Override
    public void onDestroy() {
        instance = null;
        super.onDestroy();
    }

    @Override
    protected void onServiceConnected() {
        Log.v(TAG, "onServiceConnected: " + getServiceInfo().toString());
        instance = this;
        super.onServiceConnected();
        synchronized (LOCK) {
            LOCK.notifyAll();
        }
        // FIXME: 2017/2/12 有时在无障碍中开启服务后这里不会调用服务也不会运行，安卓的BUG???
    }

    public static void disable() {
        if (instance != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            instance.disableSelf();
        } else {
            AccessibilityServiceTool.goToAccessibilitySetting();
        }
    }

    public static void waitForEnabled(long timeOut) {
        synchronized (LOCK) {
            try {
                LOCK.wait(timeOut);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}

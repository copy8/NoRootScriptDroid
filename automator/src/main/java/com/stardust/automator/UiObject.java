package com.stardust.automator;

import android.app.UiAutomation;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.util.Pools;
import android.support.v4.view.accessibility.AccessibilityNodeInfoCompat;
import android.util.Log;
import android.view.View;
import android.view.accessibility.AccessibilityNodeInfo;

import com.stardust.view.accessibility.AccessibilityNodeInfoAllocator;
import com.stardust.view.accessibility.AccessibilityNodeInfoHelper;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static android.support.v4.view.accessibility.AccessibilityNodeInfoCompat.AccessibilityActionCompat.ACTION_CONTEXT_CLICK;
import static android.support.v4.view.accessibility.AccessibilityNodeInfoCompat.AccessibilityActionCompat.ACTION_SCROLL_DOWN;
import static android.support.v4.view.accessibility.AccessibilityNodeInfoCompat.AccessibilityActionCompat.ACTION_SCROLL_LEFT;
import static android.support.v4.view.accessibility.AccessibilityNodeInfoCompat.AccessibilityActionCompat.ACTION_SCROLL_RIGHT;
import static android.support.v4.view.accessibility.AccessibilityNodeInfoCompat.AccessibilityActionCompat.ACTION_SCROLL_TO_POSITION;
import static android.support.v4.view.accessibility.AccessibilityNodeInfoCompat.AccessibilityActionCompat.ACTION_SCROLL_UP;
import static android.support.v4.view.accessibility.AccessibilityNodeInfoCompat.AccessibilityActionCompat.ACTION_SET_PROGRESS;
import static android.support.v4.view.accessibility.AccessibilityNodeInfoCompat.AccessibilityActionCompat.ACTION_SHOW_ON_SCREEN;

/**
 * Created by Stardust on 2017/3/9.
 */

public class UiObject extends AccessibilityNodeInfoCompat {

    private static final String TAG = "UiObject";
    private static final boolean DEBUG = false;
    private static int notRecycledCount = 0;

    public static UiObject createRoot(AccessibilityNodeInfo root) {
        return new UiObject(root, null, true);
    }

    public static UiObject createRoot(AccessibilityNodeInfo root, AccessibilityNodeInfoAllocator allocator) {
        return new UiObject(root, allocator, true);
    }


    private AccessibilityNodeInfoAllocator mAllocator = null;
    private String mStackTrace = "";
    private boolean mIsRootNode = false;

    public UiObject(Object info) {
        this(info, null);
    }

    public UiObject(Object info, AccessibilityNodeInfoAllocator allocator, boolean isRootNode) {
        super(info);
        mIsRootNode = isRootNode;
        mAllocator = allocator;
        if (DEBUG)
            mStackTrace = Arrays.toString(Thread.currentThread().getStackTrace());
    }

    public UiObject(Object info, AccessibilityNodeInfoAllocator allocator) {
        this(info, allocator, false);
    }

    @Nullable
    public UiObject parent() {
        try {
            AccessibilityNodeInfoCompat parent = super.getParent();
            if (parent == null)
                return null;
            return new UiObject(parent.getInfo());
        } catch (IllegalStateException e) {
            // FIXME: 2017/5/5
            return null;
        }
    }

    @Nullable
    public UiObject child(int i) {
        try {
            AccessibilityNodeInfoCompat child = super.getChild(i);
            if (child == null)
                return null;
            return new UiObject(child.getInfo());
        } catch (IllegalStateException e) {
            // FIXME: 2017/5/5
            return null;
        }
    }

    public UiObjectCollection find(UiGlobalSelector selector) {
        return selector.findOf(this);
    }

    public UiObject findOne(UiGlobalSelector selector) {
        return selector.findOneOf(this);
    }

    public UiObjectCollection children() {
        ArrayList<AccessibilityNodeInfoCompat> list = new ArrayList<>(getChildCount());
        for (int i = 0; i < getChildCount(); i++) {
            list.add(getChild(i));
        }
        return UiObjectCollection.ofCompat(list);
    }

    public int childCount() {
        return getChildCount();
    }

    public Rect bounds() {
        return AccessibilityNodeInfoHelper.getBoundsInScreen(this);
    }

    public Rect boundsInParent() {
        return AccessibilityNodeInfoHelper.getBoundsInParent(this);
    }

    public int drawingOrder() {
        return getDrawingOrder();
    }

    public String id() {
        return getViewIdResourceName();
    }

    @NonNull
    public CharSequence text() {
        CharSequence t = getText();
        return t == null ? "" : t;
    }

    public CharSequence desc() {
        return getContentDescription();
    }

    public CharSequence className() {
        return getClassName();
    }

    public CharSequence packageName() {
        return getPackageName();
    }

    public boolean performAction(int action, ActionArgument... arguments) {
        Bundle bundle = argumentsToBundle(arguments);
        return performAction(action, bundle);
    }

    @Override
    public boolean performAction(int action, Bundle bundle) {
        try {
            return super.performAction(action, bundle);
        } catch (IllegalStateException e) {
            // FIXME: 2017/5/5
            return false;
        }
    }

    @Override
    public boolean performAction(int action) {
        try {
            return super.performAction(action);
        } catch (IllegalStateException e) {
            // FIXME: 2017/5/5
            return false;
        }


    }


    public AccessibilityNodeInfoAllocator getAllocator() {
        return mAllocator;
    }

    private static Bundle argumentsToBundle(ActionArgument[] arguments) {
        Bundle bundle = new Bundle();
        for (ActionArgument arg : arguments) {
            arg.putIn(bundle);
        }
        return bundle;
    }

    public boolean click() {
        return performAction(ACTION_CLICK);
    }

    public boolean longClick() {
        return performAction(ACTION_LONG_CLICK);
    }

    public boolean accessibilityFocus() {
        return performAction(ACTION_ACCESSIBILITY_FOCUS);
    }

    public boolean clearAccessibilityFocus() {
        return performAction(ACTION_CLEAR_ACCESSIBILITY_FOCUS);
    }

    public boolean focus() {
        return performAction(ACTION_FOCUS);
    }

    public boolean clearFocus() {
        return performAction(ACTION_CLEAR_FOCUS);
    }

    public boolean copy() {
        return performAction(ACTION_COPY);
    }

    public boolean paste() {
        return performAction(ACTION_PASTE);
    }

    public boolean select() {
        return performAction(ACTION_SELECT);
    }

    public boolean cut() {
        return performAction(ACTION_CUT);
    }

    public boolean collapse() {
        return performAction(ACTION_COLLAPSE);
    }

    public boolean expand() {
        return performAction(ACTION_EXPAND);
    }

    public boolean dismiss() {
        return performAction(ACTION_DISMISS);
    }

    public boolean show() {
        return performAction(ACTION_SHOW_ON_SCREEN.getId());
    }

    public boolean scrollForward() {
        return performAction(ACTION_SCROLL_FORWARD);
    }

    public boolean scrollBackward() {
        return performAction(ACTION_SCROLL_BACKWARD);
    }

    public boolean scrollUp() {
        return performAction(ACTION_SCROLL_UP.getId());
    }

    public boolean scrollDown() {
        return performAction(ACTION_SCROLL_DOWN.getId());
    }

    public boolean scrollLeft() {
        return performAction(ACTION_SCROLL_LEFT.getId());
    }

    public boolean scrollRight() {
        return performAction(ACTION_SCROLL_RIGHT.getId());
    }

    public boolean contextClick() {
        return performAction(ACTION_CONTEXT_CLICK.getId());
    }

    public boolean setSelection(int s, int e) {
        return performAction(ACTION_SET_SELECTION,
                new ActionArgument.IntActionArgument(ACTION_ARGUMENT_SELECTION_START_INT, s),
                new ActionArgument.IntActionArgument(ACTION_ARGUMENT_SELECTION_END_INT, e));
    }

    public boolean setText(String text) {
        return performAction(ACTION_SET_TEXT,
                new ActionArgument.CharSequenceActionArgument(ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, text));
    }

    public boolean setProgress(float value) {
        return performAction(ACTION_SET_PROGRESS.getId(),
                new ActionArgument.FloatActionArgument(ACTION_ARGUMENT_PROGRESS_VALUE, value));
    }

    public boolean scrollTo(int row, int column) {
        return performAction(ACTION_SCROLL_TO_POSITION.getId(),
                new ActionArgument.IntActionArgument(ACTION_ARGUMENT_ROW_INT, row),
                new ActionArgument.IntActionArgument(ACTION_ARGUMENT_COLUMN_INT, column));
    }

    @Override
    public AccessibilityNodeInfoCompat getChild(int index) {
        if (mAllocator == null)
            return super.getChild(index);
        return mAllocator.getChild(this, index);
    }

    @Override
    public AccessibilityNodeInfoCompat getParent() {
        if (mAllocator == null)
            return super.getParent();
        return mAllocator.getParent(this);
    }


    public boolean checkable() {
        return isCheckable();
    }


    public boolean checked() {
        return isChecked();
    }


    public boolean focusable() {
        return isFocusable();
    }


    public boolean focused() {
        return isFocused();
    }


    public boolean visibleToUser() {
        return isVisibleToUser();
    }


    public boolean accessibilityFocused() {
        return isAccessibilityFocused();
    }


    public boolean selected() {
        return isSelected();
    }

    public boolean clickable() {
        return isClickable();
    }


    public boolean longClickable() {
        return isLongClickable();
    }


    public boolean enabled() {
        return isEnabled();
    }


    public boolean password() {
        return isPassword();
    }


    public boolean scrollable() {
        return isScrollable();
    }

    @Override
    public List<AccessibilityNodeInfoCompat> findAccessibilityNodeInfosByText(String text) {
        if (mAllocator == null)
            return super.findAccessibilityNodeInfosByText(text);
        return mAllocator.findAccessibilityNodeInfosByText(this, text);
    }

    public List<UiObject> findByText(String text) {
        return compatListToUiObjectList(findAccessibilityNodeInfosByText(text), mAllocator);
    }

    @Override
    public List<AccessibilityNodeInfoCompat> findAccessibilityNodeInfosByViewId(String viewId) {
        if (mAllocator == null)
            return super.findAccessibilityNodeInfosByViewId(viewId);
        return mAllocator.findAccessibilityNodeInfosByViewId(this, viewId);
    }

    public List<UiObject> findByViewId(String viewId) {
        return compatListToUiObjectList(findAccessibilityNodeInfosByViewId(viewId), mAllocator);
    }

    public static List<UiObject> compatListToUiObjectList(List<AccessibilityNodeInfoCompat> compats, AccessibilityNodeInfoAllocator allocator) {
        List<UiObject> uiObjects = new ArrayList<>(compats.size());
        for (AccessibilityNodeInfoCompat compat : compats) {
            if (compat != null)
                uiObjects.add(new UiObject(compat.getInfo(), allocator));
        }
        return uiObjects;
    }

    public static List<UiObject> compatListToUiObjectList(List<AccessibilityNodeInfoCompat> compats) {
        return compatListToUiObjectList(compats, null);
    }

    @Override
    public void recycle() {
        try {
            super.recycle();
        } catch (Exception e) {
            Log.w(TAG, mStackTrace, e);
        }
    }

}

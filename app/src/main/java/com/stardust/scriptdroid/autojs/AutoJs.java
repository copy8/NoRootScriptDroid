package com.stardust.scriptdroid.autojs;

import android.accessibilityservice.AccessibilityService;
import android.content.Context;
import android.support.annotation.Nullable;

import com.stardust.autojs.ScriptEngineService;
import com.stardust.autojs.ScriptEngineServiceBuilder;
import com.stardust.autojs.engine.RhinoJavaScriptEngineManager;
import com.stardust.autojs.engine.ScriptEngineManager;
import com.stardust.autojs.runtime.AccessibilityBridge;
import com.stardust.autojs.runtime.ScriptStopException;
import com.stardust.autojs.runtime.api.AbstractShell;
import com.stardust.autojs.runtime.api.AppUtils;
import com.stardust.automator.AccessibilityEventCommandHost;
import com.stardust.automator.simple_action.SimpleActionPerformHost;
import com.stardust.pio.PFile;
import com.stardust.pio.UncheckedIOException;
import com.stardust.scriptdroid.App;
import com.stardust.scriptdroid.Pref;
import com.stardust.scriptdroid.R;
import com.stardust.scriptdroid.autojs.api.Shell;
import com.stardust.scriptdroid.ui.console.StardustConsole;
import com.stardust.util.Supplier;
import com.stardust.util.UiHandler;
import com.stardust.view.accessibility.AccessibilityInfoProvider;
import com.stardust.scriptdroid.external.floatingwindow.menu.layout_inspector.LayoutInspector;
import com.stardust.scriptdroid.external.floatingwindow.menu.record.accessibility.AccessibilityActionRecorder;
import com.stardust.scriptdroid.service.AccessibilityWatchDogService;
import com.stardust.scriptdroid.tool.AccessibilityServiceTool;
import com.stardust.scriptdroid.ui.console.JraskaConsole;
import com.stardust.view.accessibility.AccessibilityServiceUtils;

import java.io.IOException;


/**
 * Created by Stardust on 2017/4/2.
 */

public class AutoJs implements AccessibilityBridge {

    private static AutoJs instance;
    private static final String INIT_SCRIPT_PATH = "js/autojs_init.js";

    public static AutoJs getInstance() {
        return instance;
    }

    public static void initInstance(Context context) {
        instance = new AutoJs(context);
    }

    private final AccessibilityEventCommandHost mAccessibilityEventCommandHost = new AccessibilityEventCommandHost();
    private final SimpleActionPerformHost mSimpleActionPerformHost = new SimpleActionPerformHost();
    private final AccessibilityActionRecorder mAccessibilityActionRecorder = new AccessibilityActionRecorder();
    private final LayoutInspector mLayoutInspector = new LayoutInspector();
    private final ScriptEngineService mScriptEngineService;
    private final AccessibilityInfoProvider mAccessibilityInfoProvider;
    private final UiHandler mUiHandler;
    private final AppUtils mAppUtils;


    private AutoJs(final Context context) {
        mUiHandler = new UiHandler(context);
        mAppUtils = new AppUtils(context);
        mAccessibilityInfoProvider = new AccessibilityInfoProvider(context.getPackageManager());
        ScriptEngineManager manager = createScriptEngineManager(context);
        mScriptEngineService = new ScriptEngineServiceBuilder()
                .uiHandler(mUiHandler)
                .globalConsole(new JraskaConsole())
                .engineManger(manager)
                .runtime(new Supplier<com.stardust.autojs.runtime.ScriptRuntime>() {

                    @Override
                    public com.stardust.autojs.runtime.ScriptRuntime get() {
                        return new ScriptRuntime.Builder()
                                .setAppUtils(mAppUtils)
                                .setConsole(new StardustConsole(mUiHandler))
                                .setAccessibilityBridge(AutoJs.this)
                                .setUiHandler(mUiHandler)
                                .setShellSupplier(new Supplier<AbstractShell>() {
                                    @Override
                                    public AbstractShell get() {
                                        return new Shell(true);
                                    }
                                }).build();
                    }
                })
                .build();
        addAccessibilityServiceDelegates();
        mScriptEngineService.registerGlobalScriptExecutionListener(new ScriptExecutionGlobalListener());
    }

    private ScriptEngineManager createScriptEngineManager(Context context) {
        RhinoJavaScriptEngineManager manager = new RhinoJavaScriptEngineManager(context);
        try {
            manager.setInitScript(PFile.read(context.getAssets().open(INIT_SCRIPT_PATH)));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return manager;
    }

    private void addAccessibilityServiceDelegates() {
        AccessibilityWatchDogService.addDelegate(100, mAccessibilityInfoProvider);
        AccessibilityWatchDogService.addDelegate(300, mAccessibilityActionRecorder);
        // AccessibilityWatchDogService.addDelegate(400, mSimpleActionPerformHost);
        //AccessibilityWatchDogService.addDelegate(500, mAccessibilityEventCommandHost);
    }

    public AccessibilityActionRecorder getAccessibilityActionRecorder() {
        return mAccessibilityActionRecorder;
    }

    public AppUtils getAppUtils() {
        return mAppUtils;
    }

    public UiHandler getUiHandler() {
        return mUiHandler;
    }

    public LayoutInspector getLayoutInspector() {
        return mLayoutInspector;
    }

    @Override
    public AccessibilityEventCommandHost getCommandHost() {
        return mAccessibilityEventCommandHost;
    }

    @Override
    public SimpleActionPerformHost getActionPerformHost() {
        return mSimpleActionPerformHost;
    }

    @Nullable
    @Override
    public AccessibilityService getService() {
        return AccessibilityWatchDogService.getInstance();
    }

    @Override
    public void ensureServiceEnabled() {
        if (AccessibilityWatchDogService.getInstance() == null) {
            String errorMessage = null;
            if (AccessibilityServiceUtils.isAccessibilityServiceEnabled(App.getApp(), AccessibilityWatchDogService.class)) {
                errorMessage = App.getApp().getString(R.string.text_auto_operate_service_enabled_but_not_running);
            } else {
                if (Pref.enableAccessibilityServiceByRoot()) {
                    if (!AccessibilityServiceTool.enableAccessibilityServiceByRootAndWaitFor(2000)) {
                        errorMessage = App.getApp().getString(R.string.text_enable_accessibility_service_by_root_timeout);
                    }
                } else {
                    errorMessage = App.getApp().getString(R.string.text_no_accessibility_permission);
                }
            }
            if (errorMessage != null) {
                throw new ScriptStopException(errorMessage);
            }
        }
    }

    @Override
    public AccessibilityInfoProvider getInfoProvider() {
        return mAccessibilityInfoProvider;
    }

    public ScriptEngineService getScriptEngineService() {
        return mScriptEngineService;
    }
}

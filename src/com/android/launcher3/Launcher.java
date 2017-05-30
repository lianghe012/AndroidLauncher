
/*
 * Copyright (C) 2008 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.launcher3;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.animation.TimeInterpolator;
import android.animation.ValueAnimator;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityOptions;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.SearchManager;
import android.appwidget.AppWidgetHostView;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProviderInfo;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.ComponentCallbacks2;
import android.content.ComponentName;
import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.ContentObserver;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.StrictMode;
import android.os.SystemClock;
import android.os.storage.StorageManager;
import android.provider.Settings;
import android.text.Selection;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.method.TextKeyListener;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.HapticFeedbackConstants;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewAnimationUtils;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityEvent;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.inputmethod.InputMethodManager;
import android.widget.Advanceable;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import cn.jpush.android.api.JPushInterface;

import com.android.launcher3.DropTarget.DragObject;
import com.android.launcher3.PagedView.PageSwitchListener;
import com.android.launcher3.SavedWallpaperImages.ImageDb;
import com.android.launcher3.compat.AppWidgetManagerCompat;
import com.android.launcher3.compat.LauncherActivityInfoCompat;
import com.android.launcher3.compat.LauncherAppsCompat;
import com.android.launcher3.compat.PackageInstallerCompat;
import com.android.launcher3.compat.PackageInstallerCompat.PackageInstallInfo;
import com.android.launcher3.compat.UserHandleCompat;
import com.android.launcher3.compat.UserManagerCompat;
import com.android.launcher3.util.FileUtils;
import com.android.launcher3.util.MAnimationUtil;
import com.android.launcher3.util.PackageUtil;
import com.android.launcher3.util.SystemUIUtil;
import com.android.launcher3.util.XCSharedPreferences;
import com.android.push.MessageReceiver;
import com.android.push.NetWorkUtilsXC;
import com.android.push.NetworkStatus;
import com.android.push.PushContents;
import com.android.push.PushSetting;
import com.android.upgrade.BatteryReceiver;
import com.android.upgrade.GlobalConsts;
import com.android.upgrade.RomCheckNewVersion;
import com.android.upgrade.RomInstallDialog;
import com.android.upgrade.UpgradeInfoActivity;
import com.android.upgrade.XCDirectory;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Default launcher application.
 */
public class Launcher extends Activity
        implements View.OnClickListener, OnLongClickListener, LauncherModel.Callbacks,
                   View.OnTouchListener, PageSwitchListener, LauncherProviderChangeListener ,NetworkStatus{
    static final String TAG = "Launcher";
    static final boolean LOGD = false;
    
    static final String PS_APK_PACKAGE_NAME = "com.android.launcher1905";
    static final String PS_APK_CLASS_NAME = "com.android.launcherxc1905.XCCenterActivity";
    static final String DOPOOL_APK_PACKAGE_NAME = "com.android.dopool";
    static final String DOPOOL_APK_CLASS_NAME = "com.android.dopool.DoPoolActivity";

    static final boolean PROFILE_STARTUP = false;
    static final boolean DEBUG_WIDGETS = false;
    static final boolean DEBUG_STRICT_MODE = false;
    static final boolean DEBUG_RESUME_TIME = false;
    static final boolean DEBUG_DUMP_LOG = false;

    static final boolean ENABLE_DEBUG_INTENTS = false; // allow DebugIntents to run

    private static final int REQUEST_CREATE_SHORTCUT = 1;
    private static final int REQUEST_CREATE_APPWIDGET = 5;
    private static final int REQUEST_PICK_SHORTCUT = 7;
    private static final int REQUEST_PICK_APPWIDGET = 9;
    private static final int REQUEST_PICK_WALLPAPER = 10;

    private static final int REQUEST_BIND_APPWIDGET = 11;
    private static final int REQUEST_RECONFIGURE_APPWIDGET = 12;

    /**
     * IntentStarter uses request codes starting with this. This must be greater than all activity
     * request codes used internally.
     */
    protected static final int REQUEST_LAST = 100;

    static final String EXTRA_SHORTCUT_DUPLICATE = "duplicate";

    static final int SCREEN_COUNT = 5;
    static final int DEFAULT_SCREEN = 2;

    // To turn on these properties, type
    // adb shell setprop log.tag.PROPERTY_NAME [VERBOSE | SUPPRESS]
    static final String DUMP_STATE_PROPERTY = "launcher_dump_state";
    static final String DISABLE_ALL_APPS_PROPERTY = "launcher_noallapps";

    // The Intent extra that defines whether to ignore the launch animation
    static final String INTENT_EXTRA_IGNORE_LAUNCH_ANIMATION =
            "com.android.launcher3.intent.extra.shortcut.INGORE_LAUNCH_ANIMATION";

    // Type: int
    private static final String RUNTIME_STATE_CURRENT_SCREEN = "launcher.current_screen";
    // Type: int
    private static final String RUNTIME_STATE = "launcher.state";
    // Type: int
    private static final String RUNTIME_STATE_PENDING_ADD_CONTAINER = "launcher.add_container";
    // Type: int
    private static final String RUNTIME_STATE_PENDING_ADD_SCREEN = "launcher.add_screen";
    // Type: int
    private static final String RUNTIME_STATE_PENDING_ADD_CELL_X = "launcher.add_cell_x";
    // Type: int
    private static final String RUNTIME_STATE_PENDING_ADD_CELL_Y = "launcher.add_cell_y";
    // Type: boolean
    private static final String RUNTIME_STATE_PENDING_FOLDER_RENAME = "launcher.rename_folder";
    // Type: long
    private static final String RUNTIME_STATE_PENDING_FOLDER_RENAME_ID = "launcher.rename_folder_id";
    // Type: int
    private static final String RUNTIME_STATE_PENDING_ADD_SPAN_X = "launcher.add_span_x";
    // Type: int
    private static final String RUNTIME_STATE_PENDING_ADD_SPAN_Y = "launcher.add_span_y";
    // Type: parcelable
    private static final String RUNTIME_STATE_PENDING_ADD_WIDGET_INFO = "launcher.add_widget_info";
    // Type: parcelable
    private static final String RUNTIME_STATE_PENDING_ADD_WIDGET_ID = "launcher.add_widget_id";
    // Type: int[]
    private static final String RUNTIME_STATE_VIEW_IDS = "launcher.view_ids";

    static final String INTRO_SCREEN_DISMISSED = "launcher.intro_screen_dismissed";
    static final String FIRST_RUN_ACTIVITY_DISPLAYED = "launcher.first_run_activity_displayed";

    static final String FIRST_LOAD_COMPLETE = "launcher.first_load_complete";
    static final String ACTION_FIRST_LOAD_COMPLETE =
            "com.android.launcher3.action.FIRST_LOAD_COMPLETE";

    public static final String SHOW_WEIGHT_WATCHER = "debug.show_mem";
    public static final boolean SHOW_WEIGHT_WATCHER_DEFAULT = false;

    private static final String QSB_WIDGET_ID = "qsb_widget_id";
    private static final String QSB_WIDGET_PROVIDER = "qsb_widget_provider";

    public static final String USER_HAS_MIGRATED = "launcher.user_migrated_from_old_data";

    /** The different states that Launcher can be in. */
    private enum State { NONE, WORKSPACE, APPS_CUSTOMIZE, APPS_CUSTOMIZE_SPRING_LOADED };
    private State mState = State.WORKSPACE;
    private AnimatorSet mStateAnimation;

    private boolean mIsSafeModeEnabled;

    LauncherOverlayCallbacks mLauncherOverlayCallbacks = new LauncherOverlayCallbacksImpl();
    LauncherOverlay mLauncherOverlay;
    InsettableFrameLayout mLauncherOverlayContainer;

    static final int APPWIDGET_HOST_ID = 1024;
    public static final int EXIT_SPRINGLOADED_MODE_SHORT_TIMEOUT = 300;
    private static final int ON_ACTIVITY_RESULT_ANIMATION_DELAY = 500;
    private static final int ACTIVITY_START_DELAY = 1000;

    private static final Object sLock = new Object();
    private static int sScreen = DEFAULT_SCREEN;

    private HashMap<Integer, Integer> mItemIdToViewId = new HashMap<Integer, Integer>();
    private static final AtomicInteger sNextGeneratedId = new AtomicInteger(1);

    // How long to wait before the new-shortcut animation automatically pans the workspace
    private static int NEW_APPS_PAGE_MOVE_DELAY = 500;
    private static int NEW_APPS_ANIMATION_INACTIVE_TIMEOUT_SECONDS = 5;
    private static int NEW_APPS_ANIMATION_DELAY = 500;
    private static final int SINGLE_FRAME_DELAY = 16;

    private final BroadcastReceiver mCloseSystemDialogsReceiver
            = new CloseSystemDialogsIntentReceiver();
    private final ContentObserver mWidgetObserver = new AppWidgetResetObserver();

    private LayoutInflater mInflater;

    private Workspace mWorkspace;
    private View mLauncherView;
    private View mPageIndicators;
    private DragLayer mDragLayer;
    private DragController mDragController;
    private View mWeightWatcher;

    private AppWidgetManagerCompat mAppWidgetManager;
    private LauncherAppWidgetHost mAppWidgetHost;

    private ItemInfo mPendingAddInfo = new ItemInfo();
    private AppWidgetProviderInfo mPendingAddWidgetInfo;
    private int mPendingAddWidgetId = -1;

    private int[] mTmpAddItemCellCoordinates = new int[2];

    private FolderInfo mFolderInfo;

    private Hotseat mHotseat;
    private ViewGroup mOverviewPanel;

    private View mAllAppsButton;

    private SearchDropTargetBar mSearchDropTargetBar;
    private AppsCustomizeTabHost mAppsCustomizeTabHost;
    private AppsCustomizePagedView mAppsCustomizeContent;
    private boolean mAutoAdvanceRunning = false;
//    private AppWidgetHostView mQsb;
    private View mQsb;

    private Bundle mSavedState;
    // We set the state in both onCreate and then onNewIntent in some cases, which causes both
    // scroll issues (because the workspace may not have been measured yet) and extra work.
    // Instead, just save the state that we need to restore Launcher to, and commit it in onResume.
    private State mOnResumeState = State.NONE;

    private SpannableStringBuilder mDefaultKeySsb = null;

    private boolean mWorkspaceLoading = true;

    private boolean mPaused = true;
    private boolean mRestoring;
    private boolean mWaitingForResult;
    private boolean mOnResumeNeedsLoad;

    private ArrayList<Runnable> mBindOnResumeCallbacks = new ArrayList<Runnable>();
    private ArrayList<Runnable> mOnResumeCallbacks = new ArrayList<Runnable>();

    private Bundle mSavedInstanceState;

    private LauncherModel mModel;
    private IconCache mIconCache;
    private boolean mUserPresent = true;
    private boolean mVisible = false;
    private boolean mHasFocus = false;
    private boolean mAttached = false;

    private static LocaleConfiguration sLocaleConfiguration = null;

    private static HashMap<Long, FolderInfo> sFolders = new HashMap<Long, FolderInfo>();

    private View.OnTouchListener mHapticFeedbackTouchListener;

    // Related to the auto-advancing of widgets
    private final int ADVANCE_MSG = 1;
    private final int mAdvanceInterval = 20000;
    private final int mAdvanceStagger = 250;
    private long mAutoAdvanceSentTime;
    private long mAutoAdvanceTimeLeft = -1;
    private HashMap<View, AppWidgetProviderInfo> mWidgetsToAdvance =
        new HashMap<View, AppWidgetProviderInfo>();

    // Determines how long to wait after a rotation before restoring the screen orientation to
    // match the sensor state.
    private final int mRestoreScreenOrientationDelay = 500;

    private Drawable mWorkspaceBackgroundDrawable;

    private final ArrayList<Integer> mSynchronouslyBoundPages = new ArrayList<Integer>();
    private static final boolean DISABLE_SYNCHRONOUS_BINDING_CURRENT_PAGE = false;

    static final ArrayList<String> sDumpLogs = new ArrayList<String>();
    static Date sDateStamp = new Date();
    static DateFormat sDateFormat =
            DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT);
    static long sRunStart = System.currentTimeMillis();
    static final String CORRUPTION_EMAIL_SENT_KEY = "corruptionEmailSent";

    // We only want to get the SharedPreferences once since it does an FS stat each time we get
    // it from the context.
    private SharedPreferences mSharedPrefs;

    private static ArrayList<ComponentName> mIntentsOnWorkspaceFromUpgradePath = null;

    // Holds the page that we need to animate to, and the icon views that we need to animate up
    // when we scroll to that page on resume.
    private ImageView mFolderIconImageView;
    private Bitmap mFolderIconBitmap;
    private Canvas mFolderIconCanvas;
    private Rect mRectForFolderAnimation = new Rect();

    private BubbleTextView mWaitingForResume;
    private List<View> dragViews = new ArrayList<View>();
    private BatteryReceiver batteryReceiver;

    private Runnable mBuildLayersRunnable = new Runnable() {
        public void run() {
            if (mWorkspace != null) {
                mWorkspace.buildPageHardwareLayers();
            }
        }
    };
    
    private static PendingAddArguments sPendingAddItem;
    
    private static class PendingAddArguments {
        int requestCode;
        Intent intent;
        long container;
        long screenId;
        int cellX;
        int cellY;
        int appWidgetId;
    }

    private Stats mStats;

    FocusIndicatorView mFocusHandler;
    public static RomCheckNewVersion checkNewVersion;
    private MyHandler myHandler;
    private final int CHECK_APPS_SHOW = 99, SYSTEM_UI_CHECK = 101, DELAY_START_ANIM = 102, END_INIT_INSTALL_MSG = 103, ADD_ICON_IN_UITHREAD = 104;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
    	LauncherAppState.getInstance().mLauncher = this;
        if (DEBUG_STRICT_MODE) {
            StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
                    .detectDiskReads()
                    .detectDiskWrites()
                    .detectNetwork()   // or .detectAll() for all detectable problems
                    .penaltyLog()
                    .build());
            StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
                    .detectLeakedSqlLiteObjects()
                    .detectLeakedClosableObjects()
                    .penaltyLog()
                    .penaltyDeath()
                    .build());
        }

        if (mLauncherCallbacks != null) {
            mLauncherCallbacks.preOnCreate();
        }

        super.onCreate(savedInstanceState);

        LauncherAppState.setApplicationContext(getApplicationContext());
        LauncherAppState app = LauncherAppState.getInstance();
        LauncherAppState.getLauncherProvider().setLauncherProviderChangeListener(this);

        // Lazy-initialize the dynamic grid
        DeviceProfile grid = app.initDynamicGrid(this);

        // the LauncherApplication should call this, but in case of Instrumentation it might not be present yet
        mSharedPrefs = getSharedPreferences(LauncherAppState.getSharedPreferencesKey(),Context.MODE_PRIVATE);
        mIsSafeModeEnabled = getPackageManager().isSafeMode();
        mModel = app.setLauncher(this);
        mIconCache = app.getIconCache();
        mIconCache.flushInvalidIcons(grid);
        mDragController = new DragController(this);
        mInflater = getLayoutInflater();

        mStats = new Stats(this);

        mAppWidgetManager = AppWidgetManagerCompat.getInstance(this);

        mAppWidgetHost = new LauncherAppWidgetHost(this, APPWIDGET_HOST_ID);
        mAppWidgetHost.startListening();

        // If we are getting an onCreate, we can actually preempt onResume and unset mPaused here,
        // this also ensures that any synchronous binding below doesn't re-trigger another
        // LauncherModel load.
        mPaused = false;

        if (PROFILE_STARTUP) {
            android.os.Debug.startMethodTracing(
                    Environment.getExternalStorageDirectory() + "/launcher");
        }

        checkForLocaleChange();
        setContentView(R.layout.launcher);

        setupViews();
        grid.layout(this);

        registerContentObservers();

        lockAllApps();

        mSavedState = savedInstanceState;
        restoreState(mSavedState);

        if (PROFILE_STARTUP) {
            android.os.Debug.stopMethodTracing();
        }

        if (!mRestoring) {
            if (DISABLE_SYNCHRONOUS_BINDING_CURRENT_PAGE) {
                // If the user leaves launcher, then we should just load items asynchronously when
                // they return.
                mModel.startLoader(true, PagedView.INVALID_RESTORE_PAGE);
            } else {
                // We only load the page synchronously if the user rotates (or triggers a
                // configuration change) while launcher is in the foreground
                mModel.startLoader(true, mWorkspace.getRestorePage());
            }
        }

        // For handling default keys
        mDefaultKeySsb = new SpannableStringBuilder();
        Selection.setSelection(mDefaultKeySsb, 0);

        IntentFilter filter = new IntentFilter(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
        registerReceiver(mCloseSystemDialogsReceiver, filter);

        // On large interfaces, we want the screen to auto-rotate based on the current orientation
        unlockScreenOrientation(true);

        if (mLauncherCallbacks != null) {
            mLauncherCallbacks.onCreate(savedInstanceState);
            if (mLauncherCallbacks.hasLauncherOverlay()) {
                ViewStub stub = (ViewStub) findViewById(R.id.launcher_overlay_stub);
                mLauncherOverlayContainer = (InsettableFrameLayout) stub.inflate();
                mLauncherOverlay = mLauncherCallbacks.setLauncherOverlayView(
                        mLauncherOverlayContainer, mLauncherOverlayCallbacks);
                mWorkspace.setLauncherOverlay(mLauncherOverlay);
            }
        }

        if (shouldShowIntroScreen()) {
            showIntroScreen();
        } else {
        	if(!LauncherApplication.Hdmistate){
//                showFirstRunActivity();
//                showFirstRunClings();
        	}
        }
        
        if (checkNewVersion == null) {
        	checkNewVersion = new RomCheckNewVersion(this);
		}
        initBatteryReceiver();
//        moveFolderWSDToNSD();
//        if(LauncherApplication.customDialog != null)
//        	LauncherApplication.customDialog = null;
//        LauncherApplication.customDialog = new CustomDialog(Launcher.this, R.layout.dialog_layout, R.style.DialogTheme);
//        LauncherApplication.customDialog.setCanceledOnTouchOutside(false);
//		LauncherApplication.customDialog
//				.setOnKeyListener(new DialogInterface.OnKeyListener() {
//					@Override
//					public boolean onKey(DialogInterface dialog, int keyCode,
//							KeyEvent event) {
//						if (keyCode == KeyEvent.KEYCODE_BACK) {
//							return true;
//						} else {
//							return false; // 默认返回 false
//						}
//					}
//				});
		//初始化推送需要的
				initJPush();
				myHandler = new MyHandler();
				registeResterartXC1905apk();
				marginTop = -1;
				lRelayout = (RelativeLayout)findViewById(R.id.l_center);
				initWallpaper();
    }
   
    RelativeLayout firstLoadView = null;
    View rootview = null;
    ImageView arrowImg = null;
    ImageView fingerImg = null;
    TextView tvFirstLoad = null;
    boolean isFirstLoad = false;
    String firstRecordDir = null, firstRecordPath = null;
    private float marginTop = -1;
    RelativeLayout lRelayout = null;
//    public void firstLoad(){
//    	Log.i("super_hw", "Launcher_firstLoad()");
//    	firstRecordDir = "/data/data/" + getPackageName() + "/files/";
//    	firstRecordPath = firstRecordDir + "firstLoad";
//    	if(!new File(firstRecordDir).exists())
//    		new File(firstRecordDir).mkdir();
//    	if(new File(firstRecordDir).exists()){
//    		rootview = getWindow().getDecorView();
//    		
//			if (!isFirstLoad) {
//				if(!new File(firstRecordPath).exists()){
//					Log.i("super_hw", "Launcher_firstLoad() start");
//					try {
////						if(rootview.getSystemUiVisibility() == View.VISIBLE){
////						}
//						SystemUIUtil.removeNavigationBar();
//						rootview.setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
//						isFirstLoad = true;
//						if(systemuiThread != null){
//							systemuiThread.interrupt();
//							systemuiThread.start();
//						}
//						// TODO super_hw
//						firstLoadView = new RelativeLayout(this);
//						firstLoadView.setLayoutParams(new android.view.ViewGroup.LayoutParams(-1,-1));
//						firstLoadView.setBackground(getResources().getDrawable(R.drawable.firstload_bg));
//						
//						tvFirstLoad = new TextView(this);
//						android.widget.RelativeLayout.LayoutParams tvLoadLp = new android.widget.RelativeLayout.LayoutParams(-2, -2);
//						tvLoadLp.addRule(android.widget.RelativeLayout.CENTER_HORIZONTAL);
//						tvLoadLp.setMargins(0, getWindowManager().getDefaultDisplay().getHeight()/3, 0, 0);
//						tvFirstLoad.setSingleLine();
//						//    				tvFirstLoad.setTextSize(TypedValue.COMPLEX_UNIT_PX, 60);
//						tvFirstLoad.setTextSize(30);
//						tvFirstLoad.setTextColor(Color.WHITE);
//						tvFirstLoad.setText(getString(R.string.str_first_load_msg));
//						firstLoadView.addView(tvFirstLoad, tvLoadLp);
//						
//						arrowImg = new ImageView(this);
//						android.widget.RelativeLayout.LayoutParams arrowLp = new android.widget.RelativeLayout.LayoutParams(178, 242);
//						arrowLp.addRule(android.widget.RelativeLayout.CENTER_HORIZONTAL);
//						arrowLp.addRule(android.widget.RelativeLayout.ALIGN_PARENT_BOTTOM);
//						firstLoadView.addView(arrowImg, arrowLp);
//						arrowImg.setBackground(getResources().getDrawable(R.drawable.firstload_arrow));
//						
//						fingerImg = new ImageView(this);
//						android.widget.RelativeLayout.LayoutParams fingerLp = new android.widget.RelativeLayout.LayoutParams(178, 120);
//						fingerLp.addRule(android.widget.RelativeLayout.CENTER_HORIZONTAL);
//						fingerLp.addRule(android.widget.RelativeLayout.ALIGN_PARENT_BOTTOM);
//						firstLoadView.addView(fingerImg, fingerLp);
//						fingerImg.setBackground(getResources().getDrawable(R.drawable.firstload_finger));
//						
//						//提交右上角新增的View 
//						clickImg = new ImageView(this);
//                        android.widget.RelativeLayout.LayoutParams clickImgLp = new android.widget.RelativeLayout.LayoutParams(150, 150);
//                        clickImgLp.addRule(android.widget.RelativeLayout.ALIGN_PARENT_RIGHT);
//                        clickImgLp.addRule(android.widget.RelativeLayout.CENTER_VERTICAL);
//                        firstLoadView.addView(clickImg, clickImgLp);
//                        clickImg.setBackground(getResources().getDrawable(R.drawable.alpha0));
//                        clickImg.setOnClickListener(new OnClickListener() {
//                            
//                            @Override
//                            public void onClick(View v) {
//                                ++clickTime;
//                                if (clickFirst) {
//                                    cyc = true;
//                                    clickFirst = false;
//                                    new MyThread().start();
//                                }
//                            }
//                        });
//                   myHandler.sendEmptyMessageDelayed(DELAY_START_ANIM, 1000);
//						firstLoadView.setOnTouchListener(new View.OnTouchListener() {
//							public boolean onTouch(View v, MotionEvent event) {
//								return true;
//							}
//						});
//						getWindow().addContentView(firstLoadView, new android.view.ViewGroup.LayoutParams(-1,-1));
//					} catch (Exception e) {
//						e.printStackTrace();
//					}
//				} else {
//					isFirstLoad = false;
//				}
//			} else {
//				if(rootview.getSystemUiVisibility() == View.VISIBLE){
//					rootview.setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
//				}
//			}
//    	}
//    }
    
    ///////////////////////////////
    //导览点击事件
    ImageView clickImg = null;//点击次数
    private long clickTime; // 点击次数
    private boolean clickFirst = true;//首次点击
    private boolean cyc = false;//是否循环判断点击次数
    class MyThread extends Thread {
        @Override
        public void run() {
            while (cyc) {
                try {
                    sleep(1000 * 2);
                    if (clickTime>=4) {
                        cyc = false;
                        LauncherApplication.isFactoryModel = true;
                        startActivity(new Intent(Settings.ACTION_BLUETOOTH_SETTINGS));//直接进入手机中的蓝牙设置界面
                    }
                    clickTime = 0;
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    ///////////////////////////////

	private boolean isViewMarginSizeChange() {
    	if(lRelayout == null)
    		return false;
    	if(lRelayout.getY() <= 0)
    		return false;
    	if(marginTop <= 0){
    		marginTop = lRelayout.getY();
    		return false;
    	} else if(marginTop != lRelayout.getY()){
    		if(marginTop > lRelayout.getY()){
    			Log.i("super_hw", "type 1, marginTop:"+marginTop + " lRelayout.getY():" + lRelayout.getY());
    			return true;
    		} else {
    			Log.i("super_hw", "type 2, marginTop:"+marginTop + " lRelayout.getY():" + lRelayout.getY());
    			marginTop = lRelayout.getY();
    			return false;
    		}
    	}
    	return false;
    }
    
	/**
	 * 
	    * @Title: initWallpaper
	    * @Description: 初始化壁纸文件
	    * @param     参数
	    * @return void    返回类型
	    * @throws
	 */
    private void initWallpaper(){
     	final Context context = getApplicationContext();
    	if(XCSharedPreferences.getPreferences(context,"initWallpaperFlag") == null ||
    			(XCSharedPreferences.getPreferences(context,"initWallpaperFlag") != null && 
    			!XCSharedPreferences.getPreferences(context,"initWallpaperFlag").equals("true"))){
    		Log.e("initWallpaper","1");
    		if(!LauncherApplication.WallpaperFlag){
    			LauncherApplication.WallpaperFlag = true;
    			new Thread() {
    				public void run() {
    					ImageDb mDb = new ImageDb(context);
    					SQLiteDatabase db = mDb.getWritableDatabase();
    					if (db != null) {
    						Log.e("initWallpaper","2");
    						try {
    							for (int i = 1; i < 5; i++) {
    								FileUtils.copyAssetFile(context, "wallpaper"
    										+ i + ".jpg", context.getFilesDir()
    										.toString() + "/", null);
    								Bitmap thumbnail = BitmapFactory.decodeFile(
    										context.getFilesDir().toString() + "/"
    												+ "wallpaper" + i + ".jpg",
    										null);

    								File thumbFile = File.createTempFile(
    										"wallpaperthumb", "",
    										context.getFilesDir());
    								FileOutputStream thumbFileStream = context
    										.openFileOutput(thumbFile.getName(),
    												Context.MODE_PRIVATE);
    								thumbnail.compress(Bitmap.CompressFormat.JPEG,
    										95, thumbFileStream);
    								thumbFileStream.close();
    								try {
    									ContentValues values = new ContentValues();
    									try {
    										if (mDb.isRecordExists(db,
    												ImageDb.COLUMN_IMAGE_FILENAME,
    												"wallpaper" + i + ".jpg")) {
    											Log.e("initWallpaper","wallpaper"+i);
    											db.delete(
    													ImageDb.TABLE_NAME,
    													ImageDb.COLUMN_IMAGE_FILENAME
    															+ "= ?",
    													new String[] { "wallpaper"
    															+ i + ".jpg" });
    										}
    									} catch (Exception e) {
    									}
    									values.put(
    											ImageDb.COLUMN_IMAGE_THUMBNAIL_FILENAME,
    											thumbFile.getName());
    									values.put(ImageDb.COLUMN_IMAGE_FILENAME,
    											"wallpaper" + i + ".jpg");
    									db.insert(ImageDb.TABLE_NAME, null, values);
    								} catch (Exception e) {
    								}
    								XCSharedPreferences.savePreferences(context,
    										"initWallpaperFlag", "true");
    								Log.e("initWallpaper","end");
    							}
    						} catch (Exception e) {
    							e.printStackTrace();
    						} finally {
    							mDb = null;
    							db.close();
    						}
    					}
    				};
    			}.start();
    		}
    	}
    }
    
    
    class ImageDb extends SQLiteOpenHelper {
        final static int DB_VERSION = 1;
        final static String TABLE_NAME = "saved_wallpaper_images";
        final static String COLUMN_ID = "id";
        final static String COLUMN_IMAGE_THUMBNAIL_FILENAME = "image_thumbnail";
        final static String COLUMN_IMAGE_FILENAME = "image";

        Context mContext;

        public ImageDb(Context context) {
            super(context, context.getDatabasePath(WallpaperPickerActivity.WALLPAPER_IMAGES_DB).getPath(),
                    null, DB_VERSION);
            // Store the context for later use
            mContext = context;
        }

        public void moveFromCacheDirectoryIfNecessary(Context context) {
            // We used to store the saved images in the cache directory, but that meant they'd get
            // deleted sometimes-- move them to the data directory
            File oldSavedImagesFile = new File(context.getCacheDir(),
            		WallpaperPickerActivity.WALLPAPER_IMAGES_DB);
            File savedImagesFile = context.getDatabasePath(WallpaperPickerActivity.WALLPAPER_IMAGES_DB);
            if (oldSavedImagesFile.exists()) {
                oldSavedImagesFile.renameTo(savedImagesFile);
            }
        }
        @Override
        public void onCreate(SQLiteDatabase database) {
            database.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " (" +
                    COLUMN_ID + " INTEGER NOT NULL, " +
                    COLUMN_IMAGE_THUMBNAIL_FILENAME + " TEXT NOT NULL, " +
                    COLUMN_IMAGE_FILENAME + " TEXT NOT NULL, " +
                    "PRIMARY KEY (" + COLUMN_ID + " ASC) " +
                    ");");
        }
        
        public boolean isRecordExists(SQLiteDatabase database,
    			String idField, String fileName) {
    		Cursor c = database.query(TABLE_NAME, new String[] { idField },
    				idField + "=?", new String[] {fileName}, null,
    				null, null);
    		boolean ret = false;
    		if (c != null) {
    			c.moveToFirst();
    			while (!c.isAfterLast()) {
    				ret = true;
    				c.moveToNext();
    			}
    			c.close();
    		}
    		return ret;
    	}

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            if (oldVersion != newVersion) {
                // Delete all the records; they'll be repopulated as this is a cache
                db.execSQL("DELETE FROM " + TABLE_NAME);
            }
        }
    }
    private void clearFirstLoadView(){
    	try {
    		if(firstLoadView != null)
    			firstLoadView = null;
    		if(tvFirstLoad != null)
    			tvFirstLoad = null;
    		if(arrowImg != null)
    			arrowImg = null;
    		if(fingerImg != null){
    			fingerImg.clearAnimation();
    			fingerImg = null;
    		}
		} catch (Exception e) {
			e.printStackTrace();
		}
    }

	private Thread systemuiThread = new Thread(new Runnable() {
		public void run() {
			try {
				while (isFirstLoad) {
					Log.i("super_hw", "Launcher_firstLoad checking");
					Thread.sleep(300);
					if (!PackageUtil.getTopPackageName(Launcher.this).equals(Launcher.this.getPackageName()))
						continue;
					if (firstLoadView != null && rootview != null && fingerImg != null) {
						if (myHandler != null) {
							myHandler.removeMessages(SYSTEM_UI_CHECK);
							myHandler.sendEmptyMessage(SYSTEM_UI_CHECK);
						}
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	});

	private RelativeLayout factoryInstallGroup = null;
	private RelativeLayout factoryInstallShowGroup = null;
	private RelativeLayout factoryInstallBottomGroup = null;
	private LinearLayout factoryInstallCenterGroup = null;
	private ImageView factoryInstallLogo = null;
	private ImageView factoryInstallMsg = null;
	private ImageView factoryInstallLoading = null;
	private ImageView factoryInstallHideMsg = null;
	private boolean isStartFactoryInstall = false;
	private void showFactoryInstall(){
		try {
			SystemUIUtil.removeNavigationBar();
			rootview = getWindow().getDecorView();
			rootview.setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
			if(isStartFactoryInstall)
				return;
			isStartFactoryInstall = true;
			factoryInstallGroup = new RelativeLayout(this);
			factoryInstallGroup.setLayoutParams(new android.view.ViewGroup.LayoutParams(-1,-1));
			factoryInstallGroup.setBackground(getResources().getDrawable(R.drawable.firstload_bg));
			factoryInstallShowGroup = new RelativeLayout(this);
			int screenW = getWindowManager().getDefaultDisplay().getWidth();
			int screenH = getWindowManager().getDefaultDisplay().getHeight();
			RelativeLayout.LayoutParams lpShow = new RelativeLayout.LayoutParams(-1, screenH * 4 / 5);
			lpShow.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
			lpShow.addRule(RelativeLayout.CENTER_HORIZONTAL);
			factoryInstallShowGroup.setLayoutParams(lpShow);
//			factoryInstallShowGroup.setBackgroundColor(Color.YELLOW);
			factoryInstallGroup.addView(factoryInstallShowGroup);
			
			factoryInstallLogo = new ImageView(this);
			RelativeLayout.LayoutParams lpLogo = null;
			if(screenW <= screenH)
				lpLogo = new RelativeLayout.LayoutParams(screenW*918/1080, screenH*198/1920);
			else
				lpLogo = new RelativeLayout.LayoutParams(screenW*918/1920, screenH*198/1080);
			lpLogo.addRule(RelativeLayout.CENTER_HORIZONTAL);
			factoryInstallLogo.setLayoutParams(lpLogo);
			factoryInstallLogo.setBackground(getResources().getDrawable(R.drawable.init_install_logo));
			factoryInstallBottomGroup = new RelativeLayout(this);
			RelativeLayout.LayoutParams lpBottom = new RelativeLayout.LayoutParams(-1, -1);
			factoryInstallBottomGroup.setLayoutParams(lpBottom);
//			factoryInstallBottomGroup.setBackgroundColor(Color.CYAN);
			factoryInstallShowGroup.addView(factoryInstallLogo);
			factoryInstallShowGroup.addView(factoryInstallBottomGroup);
			
			factoryInstallCenterGroup = new LinearLayout(this);
			RelativeLayout.LayoutParams lpCenter = new RelativeLayout.LayoutParams(-2, -2);
			lpCenter.addRule(RelativeLayout.CENTER_IN_PARENT);
			factoryInstallCenterGroup.setGravity(Gravity.CENTER_HORIZONTAL);
			factoryInstallCenterGroup.setOrientation(LinearLayout.VERTICAL);
			factoryInstallCenterGroup.setLayoutParams(lpCenter);
			factoryInstallBottomGroup.addView(factoryInstallCenterGroup);
			
			factoryInstallMsg = new ImageView(this);
			factoryInstallLoading = new ImageView(this);
			factoryInstallHideMsg = new ImageView(this);
			LinearLayout.LayoutParams lpLoadingMsg = null;
			LinearLayout.LayoutParams lpLoading = null;
			LinearLayout.LayoutParams lpInstallHideMsg = null;
			ImageView lineImg1 = new ImageView(this);
			ImageView lineImg2 = new ImageView(this);
			LinearLayout.LayoutParams lpLine1 = null;
			LinearLayout.LayoutParams lpLine2 = null;
			if(screenW <= screenH) {
				lpLoadingMsg = new LinearLayout.LayoutParams(screenW*383/1035, screenH*150/1920);
				lpLoading = new LinearLayout.LayoutParams(screenW*550/1035, screenH*47/1920);
				lpInstallHideMsg = new LinearLayout.LayoutParams(screenW*490/1035, screenH*35/1920);
				lpLine1 = new LinearLayout.LayoutParams(-2, screenH*24/1920);
				lpLine2 = new LinearLayout.LayoutParams(-2, screenH*86/1920);
			} else {
				lpLoadingMsg = new LinearLayout.LayoutParams(screenW*383/1920, screenH*150/1035);
				lpLoading = new LinearLayout.LayoutParams(screenW*550/1920, screenH*47/1035);
				lpInstallHideMsg = new LinearLayout.LayoutParams(screenW*490/1920, screenH*35/1035);
				lpLine1 = new LinearLayout.LayoutParams(-2, screenH*24/1035);
				lpLine2 = new LinearLayout.LayoutParams(-2, screenH*86/1035);
			}
			factoryInstallMsg.setLayoutParams(lpLoadingMsg);
			factoryInstallLoading.setLayoutParams(lpLoading);
			factoryInstallHideMsg.setLayoutParams(lpInstallHideMsg);
			lineImg1.setLayoutParams(lpLine1);
			lineImg2.setLayoutParams(lpLine2);
			factoryInstallMsg.setBackground(getResources().getDrawable(R.drawable.init_install_msg));
			factoryInstallLoading.setBackground(getResources().getDrawable(R.drawable.init_install_anim));
			factoryInstallHideMsg.setBackground(getResources().getDrawable(R.drawable.init_install_hide_msg));
			factoryInstallCenterGroup.addView(factoryInstallMsg);
			factoryInstallCenterGroup.addView(lineImg1);
			factoryInstallCenterGroup.addView(factoryInstallLoading);
			factoryInstallCenterGroup.addView(lineImg2);
			factoryInstallCenterGroup.addView(factoryInstallHideMsg);
			
			factoryInstallGroup.setOnTouchListener(new View.OnTouchListener() {
				public boolean onTouch(View v, MotionEvent event) {
					return true;
				}
			});
			getWindow().addContentView(factoryInstallGroup, new android.view.ViewGroup.LayoutParams(-1,-1));
			((android.graphics.drawable.AnimationDrawable)factoryInstallLoading.getBackground()).start();
		} catch (Exception e) {
			Log.e("super_super", "Launcher_showFactoryInstall() Exception " + e.getMessage());
			e.printStackTrace();
		}
	}

	private void clearFactoryInstall(){
		try {
			if (factoryInstallGroup != null) {
				if(factoryInstallLoading != null)
					factoryInstallLoading.clearAnimation();
				ViewGroup vg = (ViewGroup) factoryInstallGroup.getParent();
				if (vg != null)
					vg.removeView(factoryInstallGroup);
				factoryInstallShowGroup = null;
				factoryInstallBottomGroup = null;
				factoryInstallCenterGroup = null;
				factoryInstallLogo = null;
				factoryInstallMsg = null;
				factoryInstallLoading = null;
				factoryInstallHideMsg = null;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void factoryInstallEnd(){
		Log.w("super_hw", "Launcher_factoryInstallEnd()");
		if(myHandler != null)
			myHandler.sendEmptyMessage(END_INIT_INSTALL_MSG);
	}

    private NetWorkUtilsXC network;// 网路判断工具类
    private void initJPush(){
    	  JPushInterface.init(getApplicationContext());
    	  registerMessageReceiver();  // used for receive msg
          //设置推送的tag 
          new PushSetting().pushSettingTag(this);
          new Thread() {
              public void run() {
                  try {
                      sleep(10000);
                      network = new NetWorkUtilsXC(Launcher.this);
                      network.regNetReceiver();// 网络注册监听
                      NetWorkUtilsXC.addDetectionNetworkActivity(Launcher.this);
                  } catch (InterruptedException e) {
                      e.printStackTrace();
                  }
              };
          }.start();
          GlobalConsts.netWorkList.clear();
          GlobalConsts.networkFlag = NetWorkUtilsXC.isConnectedNetwork(this);
    }
    
    private MessageReceiver mMessageReceiver;
    public void registerMessageReceiver() {
    	mMessageReceiver = new MessageReceiver();
        IntentFilter filter = new IntentFilter();
        filter.setPriority(IntentFilter.SYSTEM_HIGH_PRIORITY);
        filter.addAction(PushContents.MESSAGE_RECEIVED_ACTION);
        registerReceiver(mMessageReceiver, filter);
    }
    
    private BroadcastReceiver receiverResterartXC1905apk;

	private void registeResterartXC1905apk() {
		receiverResterartXC1905apk = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				if (intent.getAction().equals("requestRestart1905Apk")) {
					Log.e("receiverResterartXC1905apk","----------");
					new Thread("restartxc1905Apk") {
						public void run() {
							try {
								sleep(2000);
								LauncherApplication.START_GAME_BY_XC(
										getApplicationContext());
							} catch (InterruptedException e) {
								e.printStackTrace();
							}
						};
					}.start();
				}
			}
		};
		IntentFilter filter = new IntentFilter();
		filter.setPriority(IntentFilter.SYSTEM_HIGH_PRIORITY);
		filter.addAction("requestRestart1905Apk");
		registerReceiver(receiverResterartXC1905apk, filter);
	}
	
	private void unegisteResterartXC1905apk(){
		if(receiverResterartXC1905apk != null){
			unregisterReceiver(receiverResterartXC1905apk);
		}
	}


//    CustomDialog customDialog = null;
//    int time = 0;
//    private void moveFolderWSDToNSD(){
//    	if(LauncherApplication.restartTimes++ >= 1){
//    		if(!LauncherApplication.copyComple && LauncherApplication.isStartInit && LauncherApplication.tvApp1905_1Path){
//    			LauncherApplication.handler.sendEmptyMessage(1);
//    		}
//    		return;
//    	}
//    	new Thread(){
//    		public void run() {
//    			try {
//    				if(!new File(Environment.getExternalStorageDirectory()+"/tvApp1905").exists() ){
////    					Log.d("super_hw", "moveFolderWSDToNSD() step " + 1 + " times : " + time + " path : " + getTvApp1905Path());
//    					while (getTvApp1905Path() == null && time < 600){
////    						Log.d("super_hw", "moveFolderWSDToNSD() step " + 2 + " path:" + getTvApp1905Path() + " times : " + time);
//    						sleep(3000);
//    						time++;
//    					}
//    					if(LauncherApplication.tvApp1905_1Path){
//    						LauncherApplication.isStartInit = true;
////        					Log.d("super_hw", "moveFolderWSDToNSD() step " + 2);
//        					LauncherApplication.handler.sendEmptyMessage(1);
//            				FileUtils.copyFolder(getTvApp1905Path(),Environment.getExternalStorageDirectory()+"/tvApp1905");
//            				if(new File(Environment.getExternalStorageDirectory()+"/tvApp1905/"+"com.qihoo.appstore.apk").exists()){
////            					Log.d("super_hw", "moveFolderWSDToNSD() step " + 3);
//            					PackageUtil.startInstall(getApplicationContext(),Uri.fromFile(new File( Environment.getExternalStorageDirectory()+"/tvApp1905/"+"com.qihoo.appstore.apk")),"com.qihoo.appstore", null);
//            				}
////            				Log.d("super_hw", "moveFolderWSDToNSD() step " + 4);
//            				if(new File(Environment.getExternalStorageDirectory()+"/tvApp1905/"+"com.qihoo360.mobilesafe_mobilepad.apk").exists()){
//            					PackageUtil.startInstall(getApplicationContext(),Uri.fromFile(new File( Environment.getExternalStorageDirectory()+"/tvApp1905/"+"com.qihoo360.mobilesafe_mobilepad.apk")),"com.qihoo360.mobilesafe_mobilepad", null);
//            				}
//            				LauncherApplication.handler.sendEmptyMessageDelayed(2, 30 * 1000);
//    					}
//    				}else{
////    					Log.d("super_hw", "moveFolderWSDToNSD() step " + 11);
//    					while (getTvApp1905Path() == null && time < 600){
//    						sleep(3000);
//    						time++;
//    					}
////    					Log.d("super_hw", "moveFolderWSDToNSD() step " + 12);
////    					Log.d("super_hw", "moveFolderWSDToNSD() path " + Environment.getExternalStorageDirectory()+"/tvApp1905" + " --- " + getTvApp1905Path());
////    					Log.d("super_hw", "e_sdcard : " + FileUtils.getDirSize(Environment.getExternalStorageDirectory()+"/tvApp1905")+ " i_sdcard : " + FileUtils.getDirSize(getTvApp1905Path()));
//    					if(LauncherApplication.tvApp1905_1Path && FileUtils.getDirSize(Environment.getExternalStorageDirectory()+"/tvApp1905") != FileUtils.getDirSize(getTvApp1905Path())){
//    							LauncherApplication.isStartInit = true;
//    							FileUtils.deleteDir(Environment.getExternalStorageDirectory()+"/tvApp1905");
////    	    					Log.d("super_hw", "moveFolderWSDToNSD() step " + 13);
//    	    					LauncherApplication.handler.sendEmptyMessage(1);
//    	        				FileUtils.copyFolder(getTvApp1905Path(),Environment.getExternalStorageDirectory()+"/tvApp1905");
////    	        				Log.d("super_hw", "moveFolderWSDToNSD() step " + 14);
//    	        				if(new File(Environment.getExternalStorageDirectory()+"/tvApp1905/"+"com.qihoo.appstore.apk").exists()){
////    	        					Log.d("super_hw", "moveFolderWSDToNSD() step " + 15);
//    	        					PackageUtil.startInstall(getApplicationContext(),Uri.fromFile(new File( Environment.getExternalStorageDirectory()+"/tvApp1905/"+"com.qihoo.appstore.apk")),"com.qihoo.appstore", null);
//    	        				}
////    	        				Log.e("super_hw", "moveFolderWSDToNSD() step " + 16);
//    	        				if(new File(Environment.getExternalStorageDirectory()+"/tvApp1905/"+"com.qihoo360.mobilesafe_mobilepad.apk").exists()){
//    	        					PackageUtil.startInstall(getApplicationContext(),Uri.fromFile(new File( Environment.getExternalStorageDirectory()+"/tvApp1905/"+"com.qihoo360.mobilesafe_mobilepad.apk")),"com.qihoo360.mobilesafe_mobilepad", null);
//    	        				}
//    	        				LauncherApplication.handler.sendEmptyMessageDelayed(2, 30 * 1000);
//    					}
//    				}
//				} catch (Exception e) {
//					e.printStackTrace();
//				}
////    			Log.d("super_hw", "moveFolderWSDToNSD() step " + 8);
//    		};
//    	}.start();
//    }
    
    
//    private String getTvApp1905Path(){
//    	try {
//    		String mPath = null;
//    		StorageManager sm = (StorageManager) getApplicationContext()
//    				.getSystemService(Context.STORAGE_SERVICE);
//    		String[] paths = (String[]) sm.getClass()
//    				.getMethod("getVolumePaths", null)
//    				.invoke(sm, null);
////    		Log.d("super_hw", "getTvApp1905Path() -------------------  step " + 1);
//    		if (paths != null) {
////    			Log.d("super_hw", "getTvApp1905Path() -------------------  step " + 2);
//    			int length = paths.length;
//    			for (int i = 0; i < length; i++) {
//    				if (paths[i] != null) {
//    					File files = new File(paths[i]
//    							+ "/tvApp1905_1");
//    					if (files.exists()) {
//    						mPath = paths[i] + "/tvApp1905_1";
//    						LauncherApplication.tvApp1905_1Path = true;
//    						return mPath;
//    					}
//    				}
//    			}
//    		}
////    		Log.d("super_hw", "getTvApp1905Path() -------------------  step " + 3);
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//    	return null;
//    }

    private LauncherCallbacks mLauncherCallbacks;

    public void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        if (mLauncherCallbacks != null) {
            mLauncherCallbacks.onPostCreate(savedInstanceState);
        }
    }

    public boolean setLauncherCallbacks(LauncherCallbacks callbacks) {
        mLauncherCallbacks = callbacks;
        return true;
    }

    @Override
    public void onLauncherProviderChange() {
        if (mLauncherCallbacks != null) {
            mLauncherCallbacks.onLauncherProviderChange();
        }
    }

    /** To be overridden by subclasses to hint to Launcher that we have custom content */
    protected boolean hasCustomContentToLeft() {
        if (mLauncherCallbacks != null) {
            return mLauncherCallbacks.hasCustomContentToLeft();
        }
        return false;
    }

    /**
     * To be overridden by subclasses to populate the custom content container and call
     * {@link #addToCustomContentPage}. This will only be invoked if
     * {@link #hasCustomContentToLeft()} is {@code true}.
     */
    protected void populateCustomContentContainer() {
        if (mLauncherCallbacks != null) {
            mLauncherCallbacks.populateCustomContentContainer();
        }
    }

    /**
     * Invoked by subclasses to signal a change to the {@link #addCustomContentToLeft} value to
     * ensure the custom content page is added or removed if necessary.
     */
    protected void invalidateHasCustomContentToLeft() {
        if (mWorkspace == null || mWorkspace.getScreenOrder().isEmpty()) {
            // Not bound yet, wait for bindScreens to be called.
            return;
        }

        if (!mWorkspace.hasCustomContent() && hasCustomContentToLeft()) {
            // Create the custom content page and call the subclass to populate it.
            mWorkspace.createCustomContentContainer();
            populateCustomContentContainer();
        } else if (mWorkspace.hasCustomContent() && !hasCustomContentToLeft()) {
            mWorkspace.removeCustomContentPage();
        }
    }

    private void checkForLocaleChange() {
        if (sLocaleConfiguration == null) {
            new AsyncTask<Void, Void, LocaleConfiguration>() {
                @Override
                protected LocaleConfiguration doInBackground(Void... unused) {
                    LocaleConfiguration localeConfiguration = new LocaleConfiguration();
                    readConfiguration(Launcher.this, localeConfiguration);
                    return localeConfiguration;
                }

                @Override
                protected void onPostExecute(LocaleConfiguration result) {
                    sLocaleConfiguration = result;
                    checkForLocaleChange();  // recursive, but now with a locale configuration
                }
            }.execute();
            return;
        }

        final Configuration configuration = getResources().getConfiguration();

        final String previousLocale = sLocaleConfiguration.locale;
        final String locale = configuration.locale.toString();

        final int previousMcc = sLocaleConfiguration.mcc;
        final int mcc = configuration.mcc;

        final int previousMnc = sLocaleConfiguration.mnc;
        final int mnc = configuration.mnc;

        boolean localeChanged = !locale.equals(previousLocale) || mcc != previousMcc || mnc != previousMnc;

        if (localeChanged) {
            sLocaleConfiguration.locale = locale;
            sLocaleConfiguration.mcc = mcc;
            sLocaleConfiguration.mnc = mnc;

            mIconCache.flush();

            final LocaleConfiguration localeConfiguration = sLocaleConfiguration;
            new AsyncTask<Void, Void, Void>() {
                public Void doInBackground(Void ... args) {
                    writeConfiguration(Launcher.this, localeConfiguration);
                    return null;
                }
            }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, (Void) null);
        }
    }

    private static class LocaleConfiguration {
        public String locale;
        public int mcc = -1;
        public int mnc = -1;
    }

    private static void readConfiguration(Context context, LocaleConfiguration configuration) {
        DataInputStream in = null;
        try {
            in = new DataInputStream(context.openFileInput(LauncherFiles.LAUNCHER_PREFERENCES));
            configuration.locale = in.readUTF();
            configuration.mcc = in.readInt();
            configuration.mnc = in.readInt();
        } catch (FileNotFoundException e) {
            // Ignore
        } catch (IOException e) {
            // Ignore
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    // Ignore
                }
            }
        }
    }

    private static void writeConfiguration(Context context, LocaleConfiguration configuration) {
        DataOutputStream out = null;
        try {
            out = new DataOutputStream(context.openFileOutput(
                    LauncherFiles.LAUNCHER_PREFERENCES, MODE_PRIVATE));
            out.writeUTF(configuration.locale);
            out.writeInt(configuration.mcc);
            out.writeInt(configuration.mnc);
            out.flush();
        } catch (FileNotFoundException e) {
            // Ignore
        } catch (IOException e) {
            //noinspection ResultOfMethodCallIgnored
            context.getFileStreamPath(LauncherFiles.LAUNCHER_PREFERENCES).delete();
        } finally {
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                    // Ignore
                }
            }
        }
    }

    public Stats getStats() {
        return mStats;
    }

    public LayoutInflater getInflater() {
        return mInflater;
    }

    boolean isDraggingEnabled() {
        // We prevent dragging when we are loading the workspace as it is possible to pick up a view
        // that is subsequently removed from the workspace in startBinding().
        return !mModel.isLoadingWorkspace();
    }

    static int getScreen() {
        synchronized (sLock) {
            return sScreen;
        }
    }

    static void setScreen(int screen) {
        synchronized (sLock) {
            sScreen = screen;
        }
    }

    public static int generateViewId() {
        if (Build.VERSION.SDK_INT >= 17) {
            return View.generateViewId();
        } else {
            // View.generateViewId() is not available. The following fallback logic is a copy
            // of its implementation.
            for (;;) {
                final int result = sNextGeneratedId.get();
                // aapt-generated IDs have the high byte nonzero; clamp to the range under that.
                int newValue = result + 1;
                if (newValue > 0x00FFFFFF) newValue = 1; // Roll over to 1, not 0.
                if (sNextGeneratedId.compareAndSet(result, newValue)) {
                    return result;
                }
            }
        }
    }

    public int getViewIdForItem(ItemInfo info) {
        // This cast is safe given the > 2B range for int.
        int itemId = (int) info.id;
        if (mItemIdToViewId.containsKey(itemId)) {
            return mItemIdToViewId.get(itemId);
        }
        int viewId = generateViewId();
        mItemIdToViewId.put(itemId, viewId);
        return viewId;
    }

    /**
     * Returns whether we should delay spring loaded mode -- for shortcuts and widgets that have
     * a configuration step, this allows the proper animations to run after other transitions.
     */
    private long completeAdd(PendingAddArguments args) {
        long screenId = args.screenId;
        if (args.container == LauncherSettings.Favorites.CONTAINER_DESKTOP) {
            // When the screen id represents an actual screen (as opposed to a rank) we make sure
            // that the drop page actually exists.
            screenId = ensurePendingDropLayoutExists(args.screenId);
        }

        switch (args.requestCode) {
            case REQUEST_CREATE_SHORTCUT:
                completeAddShortcut(args.intent, args.container, screenId, args.cellX,
                        args.cellY);
                break;
            case REQUEST_CREATE_APPWIDGET:
                completeAddAppWidget(args.appWidgetId, args.container, screenId, null, null);
                break;
            case REQUEST_RECONFIGURE_APPWIDGET:
                completeRestoreAppWidget(args.appWidgetId);
                break;
        }
        // Before adding this resetAddInfo(), after a shortcut was added to a workspace screen,
        // if you turned the screen off and then back while in All Apps, Launcher would not
        // return to the workspace. Clearing mAddInfo.container here fixes this issue
        resetAddInfo();
        return screenId;
    }

    private void handleActivityResult(
            final int requestCode, final int resultCode, final Intent data) {
        // Reset the startActivity waiting flag
        setWaitingForResult(false);
        final int pendingAddWidgetId = mPendingAddWidgetId;
        mPendingAddWidgetId = -1;

        Runnable exitSpringLoaded = new Runnable() {
            @Override
            public void run() {
                exitSpringLoadedDragModeDelayed((resultCode != RESULT_CANCELED),
                        EXIT_SPRINGLOADED_MODE_SHORT_TIMEOUT, null);
            }
        };

        if (requestCode == REQUEST_BIND_APPWIDGET) {
            final int appWidgetId = data != null ?
                    data.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, -1) : -1;
            if (resultCode == RESULT_CANCELED) {
                completeTwoStageWidgetDrop(RESULT_CANCELED, appWidgetId);
                mWorkspace.removeExtraEmptyScreenDelayed(true, exitSpringLoaded,
                        ON_ACTIVITY_RESULT_ANIMATION_DELAY, false);
            } else if (resultCode == RESULT_OK) {
                addAppWidgetImpl(appWidgetId, mPendingAddInfo, null,
                        mPendingAddWidgetInfo, ON_ACTIVITY_RESULT_ANIMATION_DELAY);
            }
            return;
        } else if (requestCode == REQUEST_PICK_WALLPAPER) {
            if (resultCode == RESULT_OK && mWorkspace.isInOverviewMode()) {
                mWorkspace.exitOverviewMode(false);
            }
            return;
        }

        boolean isWidgetDrop = (requestCode == REQUEST_PICK_APPWIDGET ||
                requestCode == REQUEST_CREATE_APPWIDGET);

        final boolean workspaceLocked = isWorkspaceLocked();
        // We have special handling for widgets
        if (isWidgetDrop) {
            final int appWidgetId;
            int widgetId = data != null ? data.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, -1)
                    : -1;
            if (widgetId < 0) {
                appWidgetId = pendingAddWidgetId;
            } else {
                appWidgetId = widgetId;
            }

            final int result;
            if (appWidgetId < 0 || resultCode == RESULT_CANCELED) {
                Log.e(TAG, "Error: appWidgetId (EXTRA_APPWIDGET_ID) was not " +
                        "returned from the widget configuration activity.");
                result = RESULT_CANCELED;
                completeTwoStageWidgetDrop(result, appWidgetId);
                final Runnable onComplete = new Runnable() {
                    @Override
                    public void run() {
                        exitSpringLoadedDragModeDelayed(false, 0, null);
                    }
                };
                if (workspaceLocked) {
                    // No need to remove the empty screen if we're mid-binding, as the
                    // the bind will not add the empty screen.
                    mWorkspace.postDelayed(onComplete, ON_ACTIVITY_RESULT_ANIMATION_DELAY);
                } else {
                    mWorkspace.removeExtraEmptyScreenDelayed(true, onComplete,
                            ON_ACTIVITY_RESULT_ANIMATION_DELAY, false);
                }
            } else {
                if (!workspaceLocked) {
                    if (mPendingAddInfo.container == LauncherSettings.Favorites.CONTAINER_DESKTOP) {
                        // When the screen id represents an actual screen (as opposed to a rank)
                        // we make sure that the drop page actually exists.
                        mPendingAddInfo.screenId =
                                ensurePendingDropLayoutExists(mPendingAddInfo.screenId);
                    }
                    final CellLayout dropLayout = mWorkspace.getScreenWithId(mPendingAddInfo.screenId);

                    dropLayout.setDropPending(true);
                    final Runnable onComplete = new Runnable() {
                        @Override
                        public void run() {
                            completeTwoStageWidgetDrop(resultCode, appWidgetId);
                            dropLayout.setDropPending(false);
                        }
                    };
                    mWorkspace.removeExtraEmptyScreenDelayed(true, onComplete,
                            ON_ACTIVITY_RESULT_ANIMATION_DELAY, false);
                } else {
                    PendingAddArguments args = preparePendingAddArgs(requestCode, data, appWidgetId,
                            mPendingAddInfo);
                    sPendingAddItem = args;
                }
            }
            return;
        }

        if (requestCode == REQUEST_RECONFIGURE_APPWIDGET) {
            if (resultCode == RESULT_OK) {
                // Update the widget view.
                PendingAddArguments args = preparePendingAddArgs(requestCode, data,
                        pendingAddWidgetId, mPendingAddInfo);
                if (workspaceLocked) {
                    sPendingAddItem = args;
                } else {
                    completeAdd(args);
                }
            }
            // Leave the widget in the pending state if the user canceled the configure.
            return;
        }

        // The pattern used here is that a user PICKs a specific application,
        // which, depending on the target, might need to CREATE the actual target.

        // For example, the user would PICK_SHORTCUT for "Music playlist", and we
        // launch over to the Music app to actually CREATE_SHORTCUT.
        if (resultCode == RESULT_OK && mPendingAddInfo.container != ItemInfo.NO_ID) {
            final PendingAddArguments args = preparePendingAddArgs(requestCode, data, -1,
                    mPendingAddInfo);
            if (isWorkspaceLocked()) {
                sPendingAddItem = args;
            } else {
                completeAdd(args);
                mWorkspace.removeExtraEmptyScreenDelayed(true, exitSpringLoaded,
                        ON_ACTIVITY_RESULT_ANIMATION_DELAY, false);
            }
        } else if (resultCode == RESULT_CANCELED) {
            mWorkspace.removeExtraEmptyScreenDelayed(true, exitSpringLoaded,
                    ON_ACTIVITY_RESULT_ANIMATION_DELAY, false);
        }
        mDragLayer.clearAnimatedView();

    }

    @Override
    protected void onActivityResult(
            final int requestCode, final int resultCode, final Intent data) {
        handleActivityResult(requestCode, resultCode, data);
        if (mLauncherCallbacks != null) {
            mLauncherCallbacks.onActivityResult(requestCode, resultCode, data);
        }
    }

    private PendingAddArguments preparePendingAddArgs(int requestCode, Intent data, int
            appWidgetId, ItemInfo info) {
        PendingAddArguments args = new PendingAddArguments();
        args.requestCode = requestCode;
        args.intent = data;
        args.container = info.container;
        args.screenId = info.screenId;
        args.cellX = info.cellX;
        args.cellY = info.cellY;
        args.appWidgetId = appWidgetId;
        return args;
    }

    /**
     * Check to see if a given screen id exists. If not, create it at the end, return the new id.
     *
     * @param screenId the screen id to check
     * @return the new screen, or screenId if it exists
     */
    private long ensurePendingDropLayoutExists(long screenId) {
        CellLayout dropLayout =
                (CellLayout) mWorkspace.getScreenWithId(screenId);
        if (dropLayout == null) {
            // it's possible that the add screen was removed because it was
            // empty and a re-bind occurred
            mWorkspace.addExtraEmptyScreen();
            return mWorkspace.commitExtraEmptyScreen();
        } else {
            return screenId;
        }
    }

    private void completeTwoStageWidgetDrop(final int resultCode, final int appWidgetId) {
        CellLayout cellLayout =
                (CellLayout) mWorkspace.getScreenWithId(mPendingAddInfo.screenId);
        Runnable onCompleteRunnable = null;
        int animationType = 0;

        AppWidgetHostView boundWidget = null;
        if (resultCode == RESULT_OK) {
            animationType = Workspace.COMPLETE_TWO_STAGE_WIDGET_DROP_ANIMATION;
            final AppWidgetHostView layout = mAppWidgetHost.createView(this, appWidgetId,
                    mPendingAddWidgetInfo);
            boundWidget = layout;
            onCompleteRunnable = new Runnable() {
                @Override
                public void run() {
                    completeAddAppWidget(appWidgetId, mPendingAddInfo.container,
                            mPendingAddInfo.screenId, layout, null);
                    exitSpringLoadedDragModeDelayed((resultCode != RESULT_CANCELED),
                            EXIT_SPRINGLOADED_MODE_SHORT_TIMEOUT, null);
                }
            };
        } else if (resultCode == RESULT_CANCELED) {
            mAppWidgetHost.deleteAppWidgetId(appWidgetId);
            animationType = Workspace.CANCEL_TWO_STAGE_WIDGET_DROP_ANIMATION;
        }
        if (mDragLayer.getAnimatedView() != null) {
            mWorkspace.animateWidgetDrop(mPendingAddInfo, cellLayout,
                    (DragView) mDragLayer.getAnimatedView(), onCompleteRunnable,
                    animationType, boundWidget, true);
        } else if (onCompleteRunnable != null) {
            // The animated view may be null in the case of a rotation during widget configuration
            onCompleteRunnable.run();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        FirstFrameAnimatorHelper.setIsVisible(false);

        if (mLauncherCallbacks != null) {
            mLauncherCallbacks.onStop();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirstFrameAnimatorHelper.setIsVisible(true);

        if (mLauncherCallbacks != null) {
            mLauncherCallbacks.onStart();
        }
    }

    @Override
    protected void onResume() {
    	Log.d("super_hw", "Launcher onResume");
		if (LauncherApplication.isFactoryInstallModel) {
			showFactoryInstall();
		} else {
//			firstLoad();
		}
    	if(LauncherApplication.isFactoryModel)
    		clickFirst = true;
    	resumeEvent();
        long startTime = 0;
        if (DEBUG_RESUME_TIME) {
            startTime = System.currentTimeMillis();
            Log.v(TAG, "Launcher.onResume()");
        }

        if (mLauncherCallbacks != null) {
            mLauncherCallbacks.preOnResume();
        }

        super.onResume();

        // Restore the previous launcher state
        if (mOnResumeState == State.WORKSPACE) {
            showWorkspace(false);
        } else if (mOnResumeState == State.APPS_CUSTOMIZE) {
            showAllApps(false, mAppsCustomizeContent.getContentType(), false);
        }
        mOnResumeState = State.NONE;

        // Background was set to gradient in onPause(), restore to black if in all apps.
        setWorkspaceBackground(mState == State.WORKSPACE);

        mPaused = false;
        if (mRestoring || mOnResumeNeedsLoad) {
            setWorkspaceLoading(true);
            mModel.startLoader(true, PagedView.INVALID_RESTORE_PAGE);
            mRestoring = false;
            mOnResumeNeedsLoad = false;
        }
        if (mBindOnResumeCallbacks.size() > 0) {
            // We might have postponed some bind calls until onResume (see waitUntilResume) --
            // execute them here
            long startTimeCallbacks = 0;
            if (DEBUG_RESUME_TIME) {
                startTimeCallbacks = System.currentTimeMillis();
            }

            if (mAppsCustomizeContent != null) {
                mAppsCustomizeContent.setBulkBind(true);
            }
            for (int i = 0; i < mBindOnResumeCallbacks.size(); i++) {
                mBindOnResumeCallbacks.get(i).run();
            }
            if (mAppsCustomizeContent != null) {
                mAppsCustomizeContent.setBulkBind(false);
            }
            mBindOnResumeCallbacks.clear();
            if (DEBUG_RESUME_TIME) {
                Log.d(TAG, "Time spent processing callbacks in onResume: " +
                    (System.currentTimeMillis() - startTimeCallbacks));
            }
        }
        if (mOnResumeCallbacks.size() > 0) {
            for (int i = 0; i < mOnResumeCallbacks.size(); i++) {
                mOnResumeCallbacks.get(i).run();
            }
            mOnResumeCallbacks.clear();
        }

        // Reset the pressed state of icons that were locked in the press state while activities
        // were launching
        if (mWaitingForResume != null) {
            // Resets the previous workspace icon press state
            mWaitingForResume.setStayPressed(false);
        }

        // It is possible that widgets can receive updates while launcher is not in the foreground.
        // Consequently, the widgets will be inflated in the orientation of the foreground activity
        // (framework issue). On resuming, we ensure that any widgets are inflated for the current
        // orientation.
        getWorkspace().reinflateWidgetsIfNecessary();

        // Process any items that were added while Launcher was away.
        InstallShortcutReceiver.disableAndFlushInstallQueue(this);

        if (DEBUG_RESUME_TIME) {
            Log.d(TAG, "Time spent in onResume: " + (System.currentTimeMillis() - startTime));
        }

        if (mWorkspace.getCustomContentCallbacks() != null) {
            // If we are resuming and the custom content is the current page, we call onShow().
            // It is also poassible that onShow will instead be called slightly after first layout
            // if PagedView#setRestorePage was set to the custom content page in onCreate().
            if (mWorkspace.isOnOrMovingToCustomContent()) {
                mWorkspace.getCustomContentCallbacks().onShow(true);
            }
        }
        mWorkspace.updateInteractionForState();
        mWorkspace.onResume();

        PackageInstallerCompat.getInstance(this).onResume();

        if (mLauncherCallbacks != null) {
            mLauncherCallbacks.onResume();
        }
        //如果没有弹出就重新检测一次升级接口
        if (checkNewVersion.checkRomAgain) {
            checkNewVersion.checkRomAgain= false;
             checkNewVersion.reCheck();
        }
    }

    @Override
    protected void onPause() {
        // Ensure that items added to Launcher are queued until Launcher returns
    	  Log.d("super_hw", "onPause");
        InstallShortcutReceiver.enableInstallQueue();
        PackageInstallerCompat.getInstance(this).onPause();
        super.onPause();
        mPaused = true;
        mDragController.cancelDrag();
        mDragController.resetLastGestureUpTime();

        // We call onHide() aggressively. The custom content callbacks should be able to
        // debounce excess onHide calls.
        if (mWorkspace.getCustomContentCallbacks() != null) {
            mWorkspace.getCustomContentCallbacks().onHide();
        }

        if (mLauncherCallbacks != null) {
            mLauncherCallbacks.onPause();
        }
    }

    public interface CustomContentCallbacks {
        // Custom content is completely shown. {@code fromResume} indicates whether this was caused
        // by a onResume or by scrolling otherwise.
        public void onShow(boolean fromResume);

        // Custom content is completely hidden
        public void onHide();

        // Custom content scroll progress changed. From 0 (not showing) to 1 (fully showing).
        public void onScrollProgressChanged(float progress);

        // Indicates whether the user is allowed to scroll away from the custom content.
        boolean isScrollingAllowed();
    }

    public interface LauncherOverlay {

        /**
         * Touch interaction leading to overscroll has begun
         */
        public void onScrollInteractionBegin();

        /**
         * Touch interaction related to overscroll has ended
         */
        public void onScrollInteractionEnd();

        /**
         * Scroll progress, between 0 and 100, when the user scrolls beyond the leftmost
         * screen (or in the case of RTL, the rightmost screen).
         */
        public void onScrollChange(int progress, boolean rtl);

        /**
         * Screen has stopped scrolling
         */
        public void onScrollSettled();

        /**
         * This method can be called by the Launcher in order to force the LauncherOverlay
         * to exit fully immersive mode.
         */
        public void forceExitFullImmersion();
    }

    public interface LauncherOverlayCallbacks {
        /**
         * This method indicates whether a call to {@link #enterFullImmersion()} will succeed,
         * however it doesn't modify any state within the launcher.
         */
        public boolean canEnterFullImmersion();

        /**
         * Should be called to tell Launcher that the LauncherOverlay will take over interaction,
         * eg. by occupying the full screen and handling all touch events.
         *
         * @return true if Launcher allows the LauncherOverlay to become fully immersive. In this
         *          case, Launcher will modify any necessary state and assumes the overlay is
         *          handling all interaction. If false, the LauncherOverlay should cancel any
         *
         */
        public boolean enterFullImmersion();

        /**
         * Must be called when exiting fully immersive mode. Indicates to Launcher that it has
         * full control over UI and state.
         */
        public void exitFullImmersion();
    }

    class LauncherOverlayCallbacksImpl implements LauncherOverlayCallbacks {

        @Override
        public boolean canEnterFullImmersion() {
            return mState == State.WORKSPACE;
        }

        @Override
        public boolean enterFullImmersion() {
            if (mState == State.WORKSPACE) {
                // When fully immersed, disregard any touches which fall through.
                mDragLayer.setBlockTouch(true);
                return true;
            }
            return false;
        }

        @Override
        public void exitFullImmersion() {
            mDragLayer.setBlockTouch(false);
        }
    }

    protected boolean hasSettings() {
        if (mLauncherCallbacks != null) {
            return mLauncherCallbacks.hasSettings();
        }
        return false;
    }


    public void addToCustomContentPage(View customContent,
            CustomContentCallbacks callbacks, String description) {
        mWorkspace.addToCustomContentPage(customContent, callbacks, description);
    }

    // The custom content needs to offset its content to account for the QSB
    public int getTopOffsetForCustomContent() {
        return mWorkspace.getPaddingTop();
    }

    @Override
    public Object onRetainNonConfigurationInstance() {
        // Flag the loader to stop early before switching
        if (mModel.isCurrentCallbacks(this)) {
            mModel.stopLoader();
        }
        if (mAppsCustomizeContent != null) {
            mAppsCustomizeContent.surrender();
        }
        return Boolean.TRUE;
    }

    // We can't hide the IME if it was forced open.  So don't bother
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        mHasFocus = hasFocus;

        if (mLauncherCallbacks != null) {
            mLauncherCallbacks.onWindowFocusChanged(hasFocus);
        }
    }

    private boolean acceptFilter() {
        final InputMethodManager inputManager = (InputMethodManager)
                getSystemService(Context.INPUT_METHOD_SERVICE);
        return !inputManager.isFullscreenMode();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        final int uniChar = event.getUnicodeChar();
        final boolean handled = super.onKeyDown(keyCode, event);
        final boolean isKeyNotWhitespace = uniChar > 0 && !Character.isWhitespace(uniChar);
        if (!handled && acceptFilter() && isKeyNotWhitespace) {
            boolean gotKey = TextKeyListener.getInstance().onKeyDown(mWorkspace, mDefaultKeySsb,
                    keyCode, event);
            if (gotKey && mDefaultKeySsb != null && mDefaultKeySsb.length() > 0) {
                // something usable has been typed - start a search
                // the typed text will be retrieved and cleared by
                // showSearchDialog()
                // If there are multiple keystrokes before the search dialog takes focus,
                // onSearchRequested() will be called for every keystroke,
                // but it is idempotent, so it's fine.
                return onSearchRequested();
            }
        }

        // Eat the long press event so the keyboard doesn't come up.
        if (keyCode == KeyEvent.KEYCODE_MENU && event.isLongPress()) {
            return true;
        }
        return handled;
    }

    private String getTypedText() {
        return mDefaultKeySsb.toString();
    }

    private void clearTypedText() {
        mDefaultKeySsb.clear();
        mDefaultKeySsb.clearSpans();
        Selection.setSelection(mDefaultKeySsb, 0);
    }

    /**
     * Given the integer (ordinal) value of a State enum instance, convert it to a variable of type
     * State
     */
    private static State intToState(int stateOrdinal) {
        State state = State.WORKSPACE;
        final State[] stateValues = State.values();
        for (int i = 0; i < stateValues.length; i++) {
            if (stateValues[i].ordinal() == stateOrdinal) {
                state = stateValues[i];
                break;
            }
        }
        return state;
    }

    /**
     * Restores the previous state, if it exists.
     *
     * @param savedState The previous state.
     */
    @SuppressWarnings("unchecked")
    private void restoreState(Bundle savedState) {
        if (savedState == null) {
            return;
        }

        State state = intToState(savedState.getInt(RUNTIME_STATE, State.WORKSPACE.ordinal()));
        if (state == State.APPS_CUSTOMIZE) {
            mOnResumeState = State.APPS_CUSTOMIZE;
        }

        int currentScreen = savedState.getInt(RUNTIME_STATE_CURRENT_SCREEN,
                PagedView.INVALID_RESTORE_PAGE);
        if (currentScreen != PagedView.INVALID_RESTORE_PAGE) {
            mWorkspace.setRestorePage(currentScreen);
        }

        final long pendingAddContainer = savedState.getLong(RUNTIME_STATE_PENDING_ADD_CONTAINER, -1);
        final long pendingAddScreen = savedState.getLong(RUNTIME_STATE_PENDING_ADD_SCREEN, -1);

        if (pendingAddContainer != ItemInfo.NO_ID && pendingAddScreen > -1) {
            mPendingAddInfo.container = pendingAddContainer;
            mPendingAddInfo.screenId = pendingAddScreen;
            mPendingAddInfo.cellX = savedState.getInt(RUNTIME_STATE_PENDING_ADD_CELL_X);
            mPendingAddInfo.cellY = savedState.getInt(RUNTIME_STATE_PENDING_ADD_CELL_Y);
            mPendingAddInfo.spanX = savedState.getInt(RUNTIME_STATE_PENDING_ADD_SPAN_X);
            mPendingAddInfo.spanY = savedState.getInt(RUNTIME_STATE_PENDING_ADD_SPAN_Y);
            mPendingAddWidgetInfo = savedState.getParcelable(RUNTIME_STATE_PENDING_ADD_WIDGET_INFO);
            mPendingAddWidgetId = savedState.getInt(RUNTIME_STATE_PENDING_ADD_WIDGET_ID);
            setWaitingForResult(true);
            mRestoring = true;
        }

        boolean renameFolder = savedState.getBoolean(RUNTIME_STATE_PENDING_FOLDER_RENAME, false);
        if (renameFolder) {
            long id = savedState.getLong(RUNTIME_STATE_PENDING_FOLDER_RENAME_ID);
            mFolderInfo = mModel.getFolderById(this, sFolders, id);
            mRestoring = true;
        }

        // Restore the AppsCustomize tab
        if (mAppsCustomizeTabHost != null) {
            String curTab = savedState.getString("apps_customize_currentTab");
            if (curTab != null) {
                mAppsCustomizeTabHost.setContentTypeImmediate(
                        mAppsCustomizeTabHost.getContentTypeForTabTag(curTab));
                mAppsCustomizeContent.loadAssociatedPages(
                        mAppsCustomizeContent.getCurrentPage());
            }

            int currentIndex = savedState.getInt("apps_customize_currentIndex");
            mAppsCustomizeContent.restorePageForIndex(currentIndex);
        }
        mItemIdToViewId = (HashMap<Integer, Integer>)
                savedState.getSerializable(RUNTIME_STATE_VIEW_IDS);
    }

    /**
     * Finds all the views we need and configure them properly.
     */
    private void setupViews() {
        final DragController dragController = mDragController;

        mLauncherView = findViewById(R.id.launcher);
        mFocusHandler = (FocusIndicatorView) findViewById(R.id.focus_indicator);
        mDragLayer = (DragLayer) findViewById(R.id.drag_layer);
        mWorkspace = (Workspace) mDragLayer.findViewById(R.id.workspace);
        mWorkspace.setPageSwitchListener(this);
        mPageIndicators = mDragLayer.findViewById(R.id.page_indicator);

        mLauncherView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
        mWorkspaceBackgroundDrawable = getResources().getDrawable(R.drawable.transparent);

        // Setup the drag layer
        mDragLayer.setup(this, dragController);

        // Setup the hotseat
        mHotseat = (Hotseat) findViewById(R.id.hotseat);
        if (mHotseat != null) {
            mHotseat.setup(this);
            mHotseat.setOnLongClickListener(this);
        }

        
        
        mOverviewPanel = (ViewGroup) findViewById(R.id.overview_panel);
        View widgetButton = findViewById(R.id.widget_button);
        /**
         *设置小部件
         */
        widgetButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                if (!mWorkspace.isSwitchingState()) {
                    onClickAddWidgetButton(arg0);
                }
            }
        });
        widgetButton.setOnTouchListener(getHapticFeedbackTouchListener());

      
        View wallpaperButton = findViewById(R.id.wallpaper_button);
        wallpaperButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                if (!mWorkspace.isSwitchingState()) {
                    onClickWallpaperPicker(arg0);
                }
            }
        });
        wallpaperButton.setOnTouchListener(getHapticFeedbackTouchListener());

        View settingsButton = findViewById(R.id.settings_button);
        if (hasSettings()) {
            settingsButton.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View arg0) {
                    if (!mWorkspace.isSwitchingState()) {
                        onClickSettingsButton(arg0);
                    }
                }
            });
            settingsButton.setOnTouchListener(getHapticFeedbackTouchListener());
        } else {
            settingsButton.setVisibility(View.GONE);
        }

        mOverviewPanel.setAlpha(0f);

        // Setup the workspace
        mWorkspace.setHapticFeedbackEnabled(false);
        mWorkspace.setOnLongClickListener(this);
        mWorkspace.setup(dragController);
        dragController.addDragListener(mWorkspace);

        // Get the search/delete bar
        mSearchDropTargetBar = (SearchDropTargetBar)
                mDragLayer.findViewById(R.id.search_drop_target_bar);

        // Setup AppsCustomize
        mAppsCustomizeTabHost = (AppsCustomizeTabHost) findViewById(R.id.apps_customize_pane);
        mAppsCustomizeContent = (AppsCustomizePagedView)
                mAppsCustomizeTabHost.findViewById(R.id.apps_customize_pane_content);
        mAppsCustomizeContent.setup(this, dragController);

        // Setup the drag controller (drop targets have to be added in reverse order in priority)
        dragController.setDragScoller(mWorkspace);
        dragController.setScrollView(mDragLayer);
        dragController.setMoveTarget(mWorkspace);
        dragController.addDropTarget(mWorkspace);
        if (mSearchDropTargetBar != null) {
            mSearchDropTargetBar.setup(this, dragController);
            mSearchDropTargetBar.setQsbSearchBar(getQsbBar());
        }

        if (getResources().getBoolean(R.bool.debug_memory_enabled)) {
            Log.v(TAG, "adding WeightWatcher");
            mWeightWatcher = new WeightWatcher(this);
            mWeightWatcher.setAlpha(0.5f);
            ((FrameLayout) mLauncherView).addView(mWeightWatcher,
                    new FrameLayout.LayoutParams(
                            FrameLayout.LayoutParams.MATCH_PARENT,
                            FrameLayout.LayoutParams.WRAP_CONTENT,
                            Gravity.BOTTOM)
            );

            boolean show = shouldShowWeightWatcher();
            mWeightWatcher.setVisibility(show ? View.VISIBLE : View.GONE);
        }
    }

    /**
     * Sets the all apps button. This method is called from {@link Hotseat}.
     */
    public void setAllAppsButton(View allAppsButton) {
        mAllAppsButton = allAppsButton;
    }

    public View getAllAppsButton() {
        return mAllAppsButton;
    }

    /**
     * Creates a view representing a shortcut.
     *
     * @param info The data structure describing the shortcut.
     *
     * @return A View inflated from R.layout.application.
     */
    View createShortcut(ShortcutInfo info) {
        return createShortcut(R.layout.application,
                (ViewGroup) mWorkspace.getChildAt(mWorkspace.getCurrentPage()), info);
    }

    /**
     * Creates a view representing a shortcut inflated from the specified resource.
     *
     * @param layoutResId The id of the XML layout used to create the shortcut.
     * @param parent The group the shortcut belongs to.
     * @param info The data structure describing the shortcut.
     *
     * @return A View inflated from layoutResId.
     */
    public View createShortcut(int layoutResId, ViewGroup parent, ShortcutInfo info) {
        BubbleTextView favorite = (BubbleTextView) mInflater.inflate(layoutResId, parent, false);
        favorite.applyFromShortcutInfo(info, mIconCache, true);
        favorite.setOnClickListener(this);
        favorite.setOnFocusChangeListener(mFocusHandler);
        return favorite;
    }

    /**
     * Add a shortcut to the workspace.
     *
     * @param data The intent describing the shortcut.
     * @param cellInfo The position on screen where to create the shortcut.
     */
    private void completeAddShortcut(Intent data, long container, long screenId, int cellX,
            int cellY) {
    	Log.d("super_hw", "launcher completeAddShortcut");
        int[] cellXY = mTmpAddItemCellCoordinates;
        int[] touchXY = mPendingAddInfo.dropPos;
        CellLayout layout = getCellLayout(container, screenId);

        boolean foundCellSpan = false;

        ShortcutInfo info = mModel.infoFromShortcutIntent(this, data);
        if (info == null) {
            return;
        }
        final View view = createShortcut(info);

        // First we check if we already know the exact location where we want to add this item.
        if (cellX >= 0 && cellY >= 0) {
            cellXY[0] = cellX;
            cellXY[1] = cellY;
            foundCellSpan = true;

            // If appropriate, either create a folder or add to an existing folder
            if (mWorkspace.createUserFolderIfNecessary(view, container, layout, cellXY, 0,
                    true, null,null)) {
                return;
            }
            Log.d("super_hw", "Launcher line 1532, new DragObject");
            DragObject dragObject = new DragObject();
            dragObject.dragInfo = info;
            if (mWorkspace.addToExistingFolderIfNecessary(view, layout, cellXY, 0, dragObject,
                    true)) {
                return;
            }
        } else if (touchXY != null) {
            // when dragging and dropping, just find the closest free spot
            int[] result = layout.findNearestVacantArea(touchXY[0], touchXY[1], 1, 1, cellXY);
            foundCellSpan = (result != null);
        } else {
            foundCellSpan = layout.findCellForSpan(cellXY, 1, 1);
        }

        if (!foundCellSpan) {
            showOutOfSpaceMessage(isHotseatLayout(layout));
            return;
        }

        LauncherModel.addItemToDatabase(this, info, container, screenId, cellXY[0], cellXY[1], false);

        if (!mRestoring) {
            mWorkspace.addInScreen(view, container, screenId, cellXY[0], cellXY[1], 1, 1,
                    isWorkspaceLocked());
        }
    }

    static int[] getSpanForWidget(Context context, ComponentName component, int minWidth,
            int minHeight) {
    	/**
    	 * 得到widgetpadding
    	 */
        Rect padding = AppWidgetHostView.getDefaultPaddingForWidget(context, component, null);
        // We want to account for the extra amount of padding that we are adding to the widget
        // to ensure that it gets the full amount of space that it has requested
        int requiredWidth = minWidth + padding.left + padding.right;
        int requiredHeight = minHeight + padding.top + padding.bottom;
        return CellLayout.rectToCellForWidget(requiredWidth, requiredHeight, null);
    }
    
    
    

    static int[] getSpanForWidget(Context context, AppWidgetProviderInfo info) {
        return getSpanForWidget(context, info.provider, info.minWidth, info.minHeight);
    }

    static int[] getMinSpanForWidget(Context context, AppWidgetProviderInfo info) {
        return getSpanForWidget(context, info.provider, info.minResizeWidth, info.minResizeHeight);
    }

    static int[] getSpanForWidget(Context context, PendingAddWidgetInfo info) {
        return getSpanForWidget(context, info.componentName, info.minWidth, info.minHeight);
    }

    static int[] getMinSpanForWidget(Context context, PendingAddWidgetInfo info) {
        return getSpanForWidget(context, info.componentName, info.minResizeWidth,
                info.minResizeHeight);
    }

    /**
     * Add a widget to the workspace.
     *
     * @param appWidgetId The app widget id
     * @param cellInfo The position on screen where to create the widget.
     */
    private void completeAddAppWidget(final int appWidgetId, long container, long screenId,
            AppWidgetHostView hostView, AppWidgetProviderInfo appWidgetInfo) {
    	Log.i("super_hw", "Launcher_completeAddAppWidget()");
        if (appWidgetInfo == null) {
            appWidgetInfo = mAppWidgetManager.getAppWidgetInfo(appWidgetId);
        }

        // Calculate the grid spans needed to fit this widget
        CellLayout layout = getCellLayout(container, screenId);

        int[] minSpanXY = getMinSpanForWidget(this, appWidgetInfo);
        int[] spanXY = getSpanForWidget(this, appWidgetInfo);

        // Try finding open space on Launcher screen
        // We have saved the position to which the widget was dragged-- this really only matters
        // if we are placing widgets on a "spring-loaded" screen
        int[] cellXY = mTmpAddItemCellCoordinates;
        int[] touchXY = mPendingAddInfo.dropPos;
        int[] finalSpan = new int[2];
        boolean foundCellSpan = false;
        if (mPendingAddInfo.cellX >= 0 && mPendingAddInfo.cellY >= 0) {
            cellXY[0] = mPendingAddInfo.cellX;
            cellXY[1] = mPendingAddInfo.cellY;
            spanXY[0] = mPendingAddInfo.spanX;
            spanXY[1] = mPendingAddInfo.spanY;
            foundCellSpan = true;
        } else if (touchXY != null) {
            // when dragging and dropping, just find the closest free spot
            int[] result = layout.findNearestVacantArea(
                    touchXY[0], touchXY[1], minSpanXY[0], minSpanXY[1], spanXY[0],
                    spanXY[1], cellXY, finalSpan);
            spanXY[0] = finalSpan[0];
            spanXY[1] = finalSpan[1];
            foundCellSpan = (result != null);
        } else {
            foundCellSpan = layout.findCellForSpan(cellXY, minSpanXY[0], minSpanXY[1]);
        }

        if (!foundCellSpan) {
            if (appWidgetId != -1) {
                // Deleting an app widget ID is a void call but writes to disk before returning
                // to the caller...
                new AsyncTask<Void, Void, Void>() {
                    public Void doInBackground(Void ... args) {
                        mAppWidgetHost.deleteAppWidgetId(appWidgetId);
                        return null;
                    }
                }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, (Void) null);
            }
            showOutOfSpaceMessage(isHotseatLayout(layout));
            return;
        }

        // Build Launcher-specific widget info and save to database
        LauncherAppWidgetInfo launcherInfo = new LauncherAppWidgetInfo(appWidgetId,
                appWidgetInfo.provider);
        launcherInfo.spanX = spanXY[0];
        launcherInfo.spanY = spanXY[1];
        launcherInfo.minSpanX = mPendingAddInfo.minSpanX;
        launcherInfo.minSpanY = mPendingAddInfo.minSpanY;
        launcherInfo.user = mAppWidgetManager.getUser(appWidgetInfo);

        LauncherModel.addItemToDatabase(this, launcherInfo,
                container, screenId, cellXY[0], cellXY[1], false);

        if (!mRestoring) {
            if (hostView == null) {
                // Perform actual inflation because we're live
                launcherInfo.hostView = mAppWidgetHost.createView(this, appWidgetId, appWidgetInfo);
                launcherInfo.hostView.setAppWidget(appWidgetId, appWidgetInfo);
            } else {
                // The AppWidgetHostView has already been inflated and instantiated
                launcherInfo.hostView = hostView;
            }

            launcherInfo.hostView.setTag(launcherInfo);
            launcherInfo.hostView.setVisibility(View.VISIBLE);
            launcherInfo.notifyWidgetSizeChanged(this);

            launcherInfo.hostView.setOnFocusChangeListener(mFocusHandler);
            mWorkspace.addInScreen(launcherInfo.hostView, container, screenId, cellXY[0], cellXY[1],
                    launcherInfo.spanX, launcherInfo.spanY, isWorkspaceLocked());

            addWidgetToAutoAdvanceIfNeeded(launcherInfo.hostView, appWidgetInfo);
        }
        resetAddInfo();
    }

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (Intent.ACTION_SCREEN_OFF.equals(action)) {
                mUserPresent = false;
                mDragLayer.clearAllResizeFrames();
                updateRunning();

                // Reset AllApps to its initial state only if we are not in the middle of
                // processing a multi-step drop
                if (mAppsCustomizeTabHost != null && mPendingAddInfo.container == ItemInfo.NO_ID) {
                    showWorkspace(false);
                }
            } else if (Intent.ACTION_USER_PRESENT.equals(action)) {
                mUserPresent = true;
                updateRunning();
            } else if (ENABLE_DEBUG_INTENTS && DebugIntents.DELETE_DATABASE.equals(action)) {
                mModel.resetLoadedState(false, true);
                mModel.startLoader(false, PagedView.INVALID_RESTORE_PAGE,
                        LauncherModel.LOADER_FLAG_CLEAR_WORKSPACE);
            } else if (ENABLE_DEBUG_INTENTS && DebugIntents.MIGRATE_DATABASE.equals(action)) {
                mModel.resetLoadedState(false, true);
                mModel.startLoader(false, PagedView.INVALID_RESTORE_PAGE,
                        LauncherModel.LOADER_FLAG_CLEAR_WORKSPACE
                                | LauncherModel.LOADER_FLAG_MIGRATE_SHORTCUTS);
            } else if (LauncherAppsCompat.ACTION_MANAGED_PROFILE_ADDED.equals(action)
                    || LauncherAppsCompat.ACTION_MANAGED_PROFILE_REMOVED.equals(action)) {
                getModel().forceReload();
            }
        }
    };

    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();

        // Listen for broadcasts related to user-presence
        final IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        filter.addAction(Intent.ACTION_USER_PRESENT);
        // For handling managed profiles
        filter.addAction(LauncherAppsCompat.ACTION_MANAGED_PROFILE_ADDED);
        filter.addAction(LauncherAppsCompat.ACTION_MANAGED_PROFILE_REMOVED);
        if (ENABLE_DEBUG_INTENTS) {
            filter.addAction(DebugIntents.DELETE_DATABASE);
            filter.addAction(DebugIntents.MIGRATE_DATABASE);
        }
        registerReceiver(mReceiver, filter);
        FirstFrameAnimatorHelper.initializeDrawListener(getWindow().getDecorView());
        setupTransparentSystemBarsForLmp();
        mAttached = true;
        mVisible = true;
    }

    /**
     * Sets up transparent navigation and status bars in LMP.
     * This method is a no-op for other platform versions.
     */
    @TargetApi(19)
    private void setupTransparentSystemBarsForLmp() {
        // TODO(sansid): use the APIs directly when compiling against L sdk.
        // Currently we use reflection to access the flags and the API to set the transparency
        // on the System bars.
        if (Utilities.isLmpOrAbove()) {
            try {
                getWindow().getAttributes().systemUiVisibility |=
                        (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS
                        | WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
                Field drawsSysBackgroundsField = WindowManager.LayoutParams.class.getField(
                        "FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS");
                getWindow().addFlags(drawsSysBackgroundsField.getInt(null));

                Method setStatusBarColorMethod =
                        Window.class.getDeclaredMethod("setStatusBarColor", int.class);
                Method setNavigationBarColorMethod =
                        Window.class.getDeclaredMethod("setNavigationBarColor", int.class);
                setStatusBarColorMethod.invoke(getWindow(), Color.TRANSPARENT);
                setNavigationBarColorMethod.invoke(getWindow(), Color.TRANSPARENT);
            } catch (NoSuchFieldException e) {
                Log.w(TAG, "NoSuchFieldException while setting up transparent bars");
            } catch (NoSuchMethodException ex) {
                Log.w(TAG, "NoSuchMethodException while setting up transparent bars");
            } catch (IllegalAccessException e) {
                Log.w(TAG, "IllegalAccessException while setting up transparent bars");
            } catch (IllegalArgumentException e) {
                Log.w(TAG, "IllegalArgumentException while setting up transparent bars");
            } catch (InvocationTargetException e) {
                Log.w(TAG, "InvocationTargetException while setting up transparent bars");
            } finally {}
        }
    }

    @Override
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        mVisible = false;

        if (mAttached) {
            unregisterReceiver(mReceiver);
            mAttached = false;
        }
        updateRunning();
    }

    public void onWindowVisibilityChanged(int visibility) {
        mVisible = visibility == View.VISIBLE;
        updateRunning();
        // The following code used to be in onResume, but it turns out onResume is called when
        // you're in All Apps and click home to go to the workspace. onWindowVisibilityChanged
        // is a more appropriate event to handle
        if (mVisible) {
            mAppsCustomizeTabHost.onWindowVisible();
            if (!mWorkspaceLoading) {
                final ViewTreeObserver observer = mWorkspace.getViewTreeObserver();
                // We want to let Launcher draw itself at least once before we force it to build
                // layers on all the workspace pages, so that transitioning to Launcher from other
                // apps is nice and speedy.
                observer.addOnDrawListener(new ViewTreeObserver.OnDrawListener() {
                    private boolean mStarted = false;
                    public void onDraw() {
                        if (mStarted) return;
                        mStarted = true;
                        // We delay the layer building a bit in order to give
                        // other message processing a time to run.  In particular
                        // this avoids a delay in hiding the IME if it was
                        // currently shown, because doing that may involve
                        // some communication back with the app.
                        mWorkspace.postDelayed(mBuildLayersRunnable, 500);
                        final ViewTreeObserver.OnDrawListener listener = this;
                        mWorkspace.post(new Runnable() {
                                public void run() {
                                    if (mWorkspace != null &&
                                            mWorkspace.getViewTreeObserver() != null) {
                                        mWorkspace.getViewTreeObserver().
                                                removeOnDrawListener(listener);
                                    }
                                }
                            });
                        return;
                    }
                });
            }
            clearTypedText();
        }
    }

    private void sendAdvanceMessage(long delay) {
        mHandler.removeMessages(ADVANCE_MSG);
        Message msg = mHandler.obtainMessage(ADVANCE_MSG);
        mHandler.sendMessageDelayed(msg, delay);
        mAutoAdvanceSentTime = System.currentTimeMillis();
    }

    private void updateRunning() {
        boolean autoAdvanceRunning = mVisible && mUserPresent && !mWidgetsToAdvance.isEmpty();
        if (autoAdvanceRunning != mAutoAdvanceRunning) {
            mAutoAdvanceRunning = autoAdvanceRunning;
            if (autoAdvanceRunning) {
                long delay = mAutoAdvanceTimeLeft == -1 ? mAdvanceInterval : mAutoAdvanceTimeLeft;
                sendAdvanceMessage(delay);
            } else {
                if (!mWidgetsToAdvance.isEmpty()) {
                    mAutoAdvanceTimeLeft = Math.max(0, mAdvanceInterval -
                            (System.currentTimeMillis() - mAutoAdvanceSentTime));
                }
                mHandler.removeMessages(ADVANCE_MSG);
                mHandler.removeMessages(0); // Remove messages sent using postDelayed()
            }
        }
    }

    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == ADVANCE_MSG) {
                int i = 0;
                for (View key: mWidgetsToAdvance.keySet()) {
                    final View v = key.findViewById(mWidgetsToAdvance.get(key).autoAdvanceViewId);
                    final int delay = mAdvanceStagger * i;
                    if (v instanceof Advanceable) {
                       postDelayed(new Runnable() {
                           public void run() {
                               ((Advanceable) v).advance();
                           }
                       }, delay);
                    }
                    i++;
                }
                sendAdvanceMessage(mAdvanceInterval);
            }
        }
    };

    void addWidgetToAutoAdvanceIfNeeded(View hostView, AppWidgetProviderInfo appWidgetInfo) {
        if (appWidgetInfo == null || appWidgetInfo.autoAdvanceViewId == -1) return;
        View v = hostView.findViewById(appWidgetInfo.autoAdvanceViewId);
        if (v instanceof Advanceable) {
            mWidgetsToAdvance.put(hostView, appWidgetInfo);
            ((Advanceable) v).fyiWillBeAdvancedByHostKThx();
            updateRunning();
        }
    }

    void removeWidgetToAutoAdvance(View hostView) {
        if (mWidgetsToAdvance.containsKey(hostView)) {
            mWidgetsToAdvance.remove(hostView);
            updateRunning();
        }
    }

    public void removeAppWidget(LauncherAppWidgetInfo launcherInfo) {
        removeWidgetToAutoAdvance(launcherInfo.hostView);
        launcherInfo.hostView = null;
    }

    void showOutOfSpaceMessage(boolean isHotseatLayout) {
        int strId = (isHotseatLayout ? R.string.hotseat_out_of_space : R.string.out_of_space);
        Toast.makeText(this, getString(strId), Toast.LENGTH_SHORT).show();
    }

    public DragLayer getDragLayer() {
        return mDragLayer;
    }

    public Workspace getWorkspace() {
        return mWorkspace;
    }

    public Hotseat getHotseat() {
        return mHotseat;
    }

    public ViewGroup getOverviewPanel() {
        return mOverviewPanel;
    }

    public SearchDropTargetBar getSearchBar() {
        return mSearchDropTargetBar;
    }

    public LauncherAppWidgetHost getAppWidgetHost() {
        return mAppWidgetHost;
    }

    public LauncherModel getModel() {
        return mModel;
    }

    protected SharedPreferences getSharedPrefs() {
        return mSharedPrefs;
    }

    public void closeSystemDialogs() {
        getWindow().closeAllPanels();

        // Whatever we were doing is hereby canceled.
        setWaitingForResult(false);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        long startTime = 0;
        if (DEBUG_RESUME_TIME) {
            startTime = System.currentTimeMillis();
        }
        super.onNewIntent(intent);

        // Close the menu
        if (Intent.ACTION_MAIN.equals(intent.getAction())) {
            // also will cancel mWaitingForResult.
            closeSystemDialogs();

            final boolean alreadyOnHome = mHasFocus && ((intent.getFlags() &
                    Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT)
                    != Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);

            if (mWorkspace == null) {
                // Can be cases where mWorkspace is null, this prevents a NPE
                return;
            }
            Folder openFolder = mWorkspace.getOpenFolder();
            // In all these cases, only animate if we're already on home
            mWorkspace.exitWidgetResizeMode();

            boolean moveToDefaultScreen = mLauncherCallbacks != null ?
                    mLauncherCallbacks.shouldMoveToDefaultScreenOnHomeIntent() : true;
            if (alreadyOnHome && mState == State.WORKSPACE && !mWorkspace.isTouchActive() &&
                    openFolder == null && moveToDefaultScreen) {
                mWorkspace.moveToDefaultScreen(true);
            }

            closeFolder();
            exitSpringLoadedDragMode();

            // If we are already on home, then just animate back to the workspace,
            // otherwise, just wait until onResume to set the state back to Workspace
            if (alreadyOnHome) {
                showWorkspace(true);
            } else {
                mOnResumeState = State.WORKSPACE;
            }

            final View v = getWindow().peekDecorView();
            if (v != null && v.getWindowToken() != null) {
                InputMethodManager imm = (InputMethodManager)getSystemService(
                        INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
            }

            // Reset the apps customize page
            if (!alreadyOnHome && mAppsCustomizeTabHost != null) {
                mAppsCustomizeTabHost.reset();
            }

            if (mLauncherCallbacks != null) {
                mLauncherCallbacks.onHomeIntent();
            }
        }

        if (DEBUG_RESUME_TIME) {
            Log.d(TAG, "Time spent in onNewIntent: " + (System.currentTimeMillis() - startTime));
        }

        if (mLauncherCallbacks != null) {
            mLauncherCallbacks.onNewIntent(intent);
        }
    }

    @Override
    public void onRestoreInstanceState(Bundle state) {
        super.onRestoreInstanceState(state);
        for (int page: mSynchronouslyBoundPages) {
            mWorkspace.restoreInstanceStateForChild(page);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        if (mWorkspace.getChildCount() > 0) {
            outState.putInt(RUNTIME_STATE_CURRENT_SCREEN,
                    mWorkspace.getCurrentPageOffsetFromCustomContent());
        }
        super.onSaveInstanceState(outState);

        outState.putInt(RUNTIME_STATE, mState.ordinal());
        // We close any open folder since it will not be re-opened, and we need to make sure
        // this state is reflected.
        closeFolder();

        if (mPendingAddInfo.container != ItemInfo.NO_ID && mPendingAddInfo.screenId > -1 &&
                mWaitingForResult) {
            outState.putLong(RUNTIME_STATE_PENDING_ADD_CONTAINER, mPendingAddInfo.container);
            outState.putLong(RUNTIME_STATE_PENDING_ADD_SCREEN, mPendingAddInfo.screenId);
            outState.putInt(RUNTIME_STATE_PENDING_ADD_CELL_X, mPendingAddInfo.cellX);
            outState.putInt(RUNTIME_STATE_PENDING_ADD_CELL_Y, mPendingAddInfo.cellY);
            outState.putInt(RUNTIME_STATE_PENDING_ADD_SPAN_X, mPendingAddInfo.spanX);
            outState.putInt(RUNTIME_STATE_PENDING_ADD_SPAN_Y, mPendingAddInfo.spanY);
            outState.putParcelable(RUNTIME_STATE_PENDING_ADD_WIDGET_INFO, mPendingAddWidgetInfo);
            outState.putInt(RUNTIME_STATE_PENDING_ADD_WIDGET_ID, mPendingAddWidgetId);
        }

        if (mFolderInfo != null && mWaitingForResult) {
            outState.putBoolean(RUNTIME_STATE_PENDING_FOLDER_RENAME, true);
            outState.putLong(RUNTIME_STATE_PENDING_FOLDER_RENAME_ID, mFolderInfo.id);
        }

        // Save the current AppsCustomize tab
        if (mAppsCustomizeTabHost != null) {
            AppsCustomizePagedView.ContentType type = mAppsCustomizeContent.getContentType();
            String currentTabTag = mAppsCustomizeTabHost.getTabTagForContentType(type);
            if (currentTabTag != null) {
                outState.putString("apps_customize_currentTab", currentTabTag);
            }
            int currentIndex = mAppsCustomizeContent.getSaveInstanceStateIndex();
            outState.putInt("apps_customize_currentIndex", currentIndex);
        }
        outState.putSerializable(RUNTIME_STATE_VIEW_IDS, mItemIdToViewId);

        if (mLauncherCallbacks != null) {
            mLauncherCallbacks.onSaveInstanceState(outState);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.e("super_hw", "Launcher_onDestroy()");
        if(myHandler != null) {
        	myHandler.removeMessages(CHECK_APPS_SHOW);
        	myHandler.removeMessages(SYSTEM_UI_CHECK);
        	myHandler.removeMessages(DELAY_START_ANIM);
        	myHandler.removeMessages(ADD_ICON_IN_UITHREAD);
        	myHandler = null;
        }
        if(systemuiThread != null)
        	systemuiThread.interrupt();
        systemuiThread = null;
        clearFirstLoadView();
        isFirstLoad = false;
        if (mMessageReceiver!=null) {
        	unregisterReceiver(mMessageReceiver);
		}
        // Remove all pending runnables
        mHandler.removeMessages(ADVANCE_MSG);
        mHandler.removeMessages(0);
        mWorkspace.removeCallbacks(mBuildLayersRunnable);

        // Stop callbacks from LauncherModel
        LauncherAppState app = (LauncherAppState.getInstance());

        // It's possible to receive onDestroy after a new Launcher activity has
        // been created. In this case, don't interfere with the new Launcher.
        if (mModel.isCurrentCallbacks(this)) {
            mModel.stopLoader();
            app.setLauncher(null);
        }

        try {
            mAppWidgetHost.stopListening();
        } catch (NullPointerException ex) {
            Log.w(TAG, "problem while stopping AppWidgetHost during Launcher destruction", ex);
        }
        mAppWidgetHost = null;

        mWidgetsToAdvance.clear();

        TextKeyListener.getInstance().release();

        // Disconnect any of the callbacks and drawables associated with ItemInfos on the workspace
        // to prevent leaking Launcher activities on orientation change.
        if (mModel != null) {
            mModel.unbindItemInfosAndClearQueuedBindRunnables();
        }

        getContentResolver().unregisterContentObserver(mWidgetObserver);
        unregisterReceiver(mCloseSystemDialogsReceiver);

        mDragLayer.clearAllResizeFrames();
        ((ViewGroup) mWorkspace.getParent()).removeAllViews();
        mWorkspace.removeAllWorkspaceScreens();
        mWorkspace = null;
        mDragController = null;

        LauncherAnimUtils.onDestroyActivity();

        if(batteryReceiver != null)
        	unregisterReceiver(batteryReceiver);
        if (mLauncherCallbacks != null) {
            mLauncherCallbacks.onDestroy();
        }
        lRelayout = null;
        unegisteResterartXC1905apk();
//        if(customDialog != null){
//        	try {
//        		if(customDialog.isShowing()){
//        			customDialog.dismiss();
//        		}
//			} catch (Exception e) {
//				e.printStackTrace();
//			}
//        	customDialog = null;
//        }
//        LauncherApplication.handler = null;
    }

    public DragController getDragController() {
        return mDragController;
    }

    @Override
    public void startActivityForResult(Intent intent, int requestCode) {
        if (requestCode >= 0) {
            setWaitingForResult(true);
        }
        super.startActivityForResult(intent, requestCode);
    }

    /**
     * Indicates that we want global search for this activity by setting the globalSearch
     * argument for {@link #startSearch} to true.
     */
    @Override
    public void startSearch(String initialQuery, boolean selectInitialQuery,
            Bundle appSearchData, boolean globalSearch) {

        showWorkspace(true);

        if (initialQuery == null) {
            // Use any text typed in the launcher as the initial query
            initialQuery = getTypedText();
        }
        if (appSearchData == null) {
            appSearchData = new Bundle();
            appSearchData.putString("source", "launcher-search");
        }
        Rect sourceBounds = new Rect();
        if (mSearchDropTargetBar != null) {
            sourceBounds = mSearchDropTargetBar.getSearchBarBounds();
        }

        boolean clearTextImmediately = startSearch(initialQuery, selectInitialQuery,
                appSearchData, sourceBounds);
        if (clearTextImmediately) {
            clearTypedText();
        }
    }

    /**
     * Start a text search.
     *
     * @return {@code true} if the search will start immediately, so any further keypresses
     * will be handled directly by the search UI. {@code false} if {@link Launcher} should continue
     * to buffer keypresses.
     */
    public boolean startSearch(String initialQuery,
            boolean selectInitialQuery, Bundle appSearchData, Rect sourceBounds) {
        if (mLauncherCallbacks != null && mLauncherCallbacks.providesSearch()) {
            return mLauncherCallbacks.startSearch(initialQuery, selectInitialQuery, appSearchData,
                    sourceBounds);
        }

        startGlobalSearch(initialQuery, selectInitialQuery,
                appSearchData, sourceBounds);
        return false;
    }

    /**
     * Starts the global search activity. This code is a copied from SearchManager
     */
    private void startGlobalSearch(String initialQuery,
            boolean selectInitialQuery, Bundle appSearchData, Rect sourceBounds) {
        final SearchManager searchManager =
            (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        ComponentName globalSearchActivity = searchManager.getGlobalSearchActivity();
        if (globalSearchActivity == null) {
            Log.w(TAG, "No global search activity found.");
            return;
        }
        Intent intent = new Intent(SearchManager.INTENT_ACTION_GLOBAL_SEARCH);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setComponent(globalSearchActivity);
        // Make sure that we have a Bundle to put source in
        if (appSearchData == null) {
            appSearchData = new Bundle();
        } else {
            appSearchData = new Bundle(appSearchData);
        }
        // Set source to package name of app that starts global search if not set already.
        if (!appSearchData.containsKey("source")) {
            appSearchData.putString("source", getPackageName());
        }
        intent.putExtra(SearchManager.APP_DATA, appSearchData);
        if (!TextUtils.isEmpty(initialQuery)) {
            intent.putExtra(SearchManager.QUERY, initialQuery);
        }
        if (selectInitialQuery) {
            intent.putExtra(SearchManager.EXTRA_SELECT_QUERY, selectInitialQuery);
        }
        intent.setSourceBounds(sourceBounds);
        try {
            startActivity(intent);
        } catch (ActivityNotFoundException ex) {
            Log.e(TAG, "Global search activity not found: " + globalSearchActivity);
        }
    }

    public boolean isOnCustomContent() {
        return mWorkspace.isOnOrMovingToCustomContent();
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        if (!isOnCustomContent()) {
            // Close any open folders
            closeFolder();
            // Stop resizing any widgets
            mWorkspace.exitWidgetResizeMode();
            if (!mWorkspace.isInOverviewMode()) {
                // Show the overview mode
                showOverviewMode(true);
            } else {
                showWorkspace(true);
            }
        }
        if (mLauncherCallbacks != null) {
            return mLauncherCallbacks.onPrepareOptionsMenu(menu);
        }

        return false;
    }

    @Override
    public boolean onSearchRequested() {
        startSearch(null, false, null, true);
        // Use a custom animation for launching search
        return true;
    }

    public boolean isWorkspaceLocked() {
        return mWorkspaceLoading || mWaitingForResult;
    }

    public boolean isWorkspaceLoading() {
        return mWorkspaceLoading;
    }

    private void setWorkspaceLoading(boolean value) {
        boolean isLocked = isWorkspaceLocked();
        mWorkspaceLoading = value;
        if (isLocked != isWorkspaceLocked()) {
            onWorkspaceLockedChanged();
        }
    }

    private void setWaitingForResult(boolean value) {
        boolean isLocked = isWorkspaceLocked();
        mWaitingForResult = value;
        if (isLocked != isWorkspaceLocked()) {
            onWorkspaceLockedChanged();
        }
    }

    protected void onWorkspaceLockedChanged() {
        if (mLauncherCallbacks != null) {
            mLauncherCallbacks.onWorkspaceLockedChanged();
        }
    }

    private void resetAddInfo() {
        mPendingAddInfo.container = ItemInfo.NO_ID;
        mPendingAddInfo.screenId = -1;
        mPendingAddInfo.cellX = mPendingAddInfo.cellY = -1;
        mPendingAddInfo.spanX = mPendingAddInfo.spanY = -1;
        mPendingAddInfo.minSpanX = mPendingAddInfo.minSpanY = -1;
        mPendingAddInfo.dropPos = null;
    }

    void addAppWidgetImpl(final int appWidgetId, final ItemInfo info,
            final AppWidgetHostView boundWidget, final AppWidgetProviderInfo appWidgetInfo) {
        addAppWidgetImpl(appWidgetId, info, boundWidget, appWidgetInfo, 0);
    }

    void addAppWidgetImpl(final int appWidgetId, final ItemInfo info,
            final AppWidgetHostView boundWidget, final AppWidgetProviderInfo appWidgetInfo, int
            delay) {
        if (appWidgetInfo.configure != null) {
            mPendingAddWidgetInfo = appWidgetInfo;
            mPendingAddWidgetId = appWidgetId;

            // Launch over to configure widget, if needed
            mAppWidgetManager.startConfigActivity(appWidgetInfo, appWidgetId, this,
                    mAppWidgetHost, REQUEST_CREATE_APPWIDGET);

        } else {
            // Otherwise just add it
            Runnable onComplete = new Runnable() {
                @Override
                public void run() {
                    // Exit spring loaded mode if necessary after adding the widget
                    exitSpringLoadedDragModeDelayed(true, EXIT_SPRINGLOADED_MODE_SHORT_TIMEOUT,
                            null);
                }
            };
            completeAddAppWidget(appWidgetId, info.container, info.screenId, boundWidget,
                    appWidgetInfo);
            mWorkspace.removeExtraEmptyScreenDelayed(true, onComplete, delay, false);
        }
    }

    protected void moveToCustomContentScreen(boolean animate) {
        // Close any folders that may be open.
        closeFolder();
        mWorkspace.moveToCustomContentScreen(animate);
    }
    /**
     * Process a shortcut drop.
     *
     * @param componentName The name of the component
     * @param screenId The ID of the screen where it should be added
     * @param cell The cell it should be added to, optional
     * @param position The location on the screen where it was dropped, optional
     */
    void processShortcutFromDrop(ComponentName componentName, long container, long screenId,
            int[] cell, int[] loc) {
        resetAddInfo();
        mPendingAddInfo.container = container;
        mPendingAddInfo.screenId = screenId;
        mPendingAddInfo.dropPos = loc;

        if (cell != null) {
            mPendingAddInfo.cellX = cell[0];
            mPendingAddInfo.cellY = cell[1];
        }

        Intent createShortcutIntent = new Intent(Intent.ACTION_CREATE_SHORTCUT);
        createShortcutIntent.setComponent(componentName);
        processShortcut(createShortcutIntent);
    }

    /**
     * Process a widget drop.
     *
     * @param info The PendingAppWidgetInfo of the widget being added.
     * @param screenId The ID of the screen where it should be added
     * @param cell The cell it should be added to, optional
     * @param position The location on the screen where it was dropped, optional
     */
    void addAppWidgetFromDrop(PendingAddWidgetInfo info, long container, long screenId,
            int[] cell, int[] span, int[] loc) {
        resetAddInfo();
        mPendingAddInfo.container = info.container = container;
        mPendingAddInfo.screenId = info.screenId = screenId;
        mPendingAddInfo.dropPos = loc;
        mPendingAddInfo.minSpanX = info.minSpanX;
        mPendingAddInfo.minSpanY = info.minSpanY;

        if (cell != null) {
            mPendingAddInfo.cellX = cell[0];
            mPendingAddInfo.cellY = cell[1];
        }
        if (span != null) {
            mPendingAddInfo.spanX = span[0];
            mPendingAddInfo.spanY = span[1];
        }

        AppWidgetHostView hostView = info.boundWidget;
        int appWidgetId;
        if (hostView != null) {
            appWidgetId = hostView.getAppWidgetId();
            addAppWidgetImpl(appWidgetId, info, hostView, info.info);
        } else {
            // In this case, we either need to start an activity to get permission to bind
            // the widget, or we need to start an activity to configure the widget, or both.
            appWidgetId = getAppWidgetHost().allocateAppWidgetId();
            Bundle options = info.bindOptions;

            boolean success = mAppWidgetManager.bindAppWidgetIdIfAllowed(
                    appWidgetId, info.info, options);
            if (success) {
                addAppWidgetImpl(appWidgetId, info, null, info.info);
            } else {
                mPendingAddWidgetInfo = info.info;
                Intent intent = new Intent(AppWidgetManager.ACTION_APPWIDGET_BIND);
                intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
                intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_PROVIDER, info.componentName);
                mAppWidgetManager.getUser(mPendingAddWidgetInfo)
                    .addToIntent(intent, AppWidgetManager.EXTRA_APPWIDGET_PROVIDER_PROFILE);
                // TODO: we need to make sure that this accounts for the options bundle.
                // intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_OPTIONS, options);
                startActivityForResult(intent, REQUEST_BIND_APPWIDGET);
            }
        }
    }

    void processShortcut(Intent intent) {
    	Log.i("super_hw", "Launcher processShortcut()");
        Utilities.startActivityForResultSafely(this, intent, REQUEST_CREATE_SHORTCUT);
    }

    void processWallpaper(Intent intent) {
        startActivityForResult(intent, REQUEST_PICK_WALLPAPER);
    }

    FolderIcon addFolder(CellLayout layout, long container, final long screenId, int cellX,
            int cellY) {
        final FolderInfo folderInfo = new FolderInfo();
        folderInfo.title = getText(R.string.folder_name);

        // Update the model
        LauncherModel.addItemToDatabase(Launcher.this, folderInfo, container, screenId, cellX, cellY,
                false);
        sFolders.put(folderInfo.id, folderInfo);

        // Create the view
        FolderIcon newFolder =
            FolderIcon.fromXml(R.layout.folder_icon, this, layout, folderInfo, mIconCache);
        mWorkspace.addInScreen(newFolder, container, screenId, cellX, cellY, 1, 1,
                isWorkspaceLocked());
        // Force measure the new folder icon
        CellLayout parent = mWorkspace.getParentCellLayoutForView(newFolder);
        parent.getShortcutsAndWidgets().measureChild(newFolder);
        return newFolder;
    }

    void removeFolder(FolderInfo folder) {
        sFolders.remove(folder.id);
    }

    protected ComponentName getWallpaperPickerComponent() {
        if (mLauncherCallbacks != null) {
            return mLauncherCallbacks.getWallpaperPickerComponent();
        }
        return new ComponentName(getPackageName(), LauncherWallpaperPickerActivity.class.getName());
    }

    /**
     * Registers various content observers. The current implementation registers
     * only a favorites observer to keep track of the favorites applications.
     */
    private void registerContentObservers() {
        ContentResolver resolver = getContentResolver();
        resolver.registerContentObserver(LauncherProvider.CONTENT_APPWIDGET_RESET_URI,
                true, mWidgetObserver);
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
    	if(event.getKeyCode() == KeyEvent.KEYCODE_MENU && event.getAction() == KeyEvent.ACTION_DOWN){
    		if(mFocusHandler != null)
    			mFocusHandler.setAlpha(0);
    	}
    	
		if (event.getKeyCode() == KeyEvent.KEYCODE_DPAD_CENTER
				|| event.getKeyCode() == KeyEvent.KEYCODE_DPAD_UP
				|| event.getKeyCode() == KeyEvent.KEYCODE_DPAD_DOWN
				|| event.getKeyCode() == KeyEvent.KEYCODE_DPAD_LEFT
				|| event.getKeyCode() == KeyEvent.KEYCODE_DPAD_RIGHT) {
			if (mWorkspace != null && mWorkspace.isInOverviewMode())
				return true;
		}
        if (event.getAction() == KeyEvent.ACTION_DOWN) {
            switch (event.getKeyCode()) {
                case KeyEvent.KEYCODE_HOME:
                    return true;
                case KeyEvent.KEYCODE_VOLUME_DOWN:
                    if (Utilities.isPropertyEnabled(DUMP_STATE_PROPERTY)) {
                        dumpState();
                        return true;
                    }
                    break;
            }
        } else if (event.getAction() == KeyEvent.ACTION_UP) {
            switch (event.getKeyCode()) {
                case KeyEvent.KEYCODE_HOME:
                    return true;
//                case KeyEvent.KEYCODE_F6://蓝牙遥控器上的 电视/平板 键
//                	startApk(PS_APK_PACKAGE_NAME,PS_APK_CLASS_NAME);
//                	return true;
//                case KeyEvent.KEYCODE_MENU:
//                	startApk(DOPOOL_APK_PACKAGE_NAME, DOPOOL_APK_CLASS_NAME);
//                	break;
            }
        }

        return super.dispatchKeyEvent(event);
    }

    private void startApk(String pkg , String className){
		Intent intent = new Intent();
		ComponentName componentName = new ComponentName(pkg, className);
		intent.setComponent(componentName);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		startActivity(intent);
	}
    
    @Override
    public void onBackPressed() {
        if (mLauncherCallbacks != null && mLauncherCallbacks.handleBackPressed()) {
            return;
        }

        if (isAllAppsVisible()) {
            if (mAppsCustomizeContent.getContentType() ==
                    AppsCustomizePagedView.ContentType.Applications) {
                showWorkspace(true);
            } else {
                showOverviewMode(true);
            }
        } else if (mWorkspace.isInOverviewMode()) {
            mWorkspace.exitOverviewMode(true);
        } else if (mWorkspace.getOpenFolder() != null) {
            Folder openFolder = mWorkspace.getOpenFolder();
            if (openFolder.isEditingName()) {
                openFolder.dismissEditingName();
            } else {
                closeFolder();
            }
        } else {
            mWorkspace.exitWidgetResizeMode();

            // Back button is a no-op here, but give at least some feedback for the button press
            mWorkspace.showOutlinesTemporarily();
        }
    }

    /**
     * Re-listen when widgets are reset.
     */
    private void onAppWidgetReset() {
        if (mAppWidgetHost != null) {
            mAppWidgetHost.startListening();
        }
    }

    /**
     * Launches the intent referred by the clicked shortcut.
     *
     * @param v The view representing the clicked shortcut.
     */
    public void onClick(View v) {
        // Make sure that rogue clicks don't get through while allapps is launching, or after the
        // view has detached (it's possible for this to happen if the view is removed mid touch).
        if (v.getWindowToken() == null) {
            return;
        }

        if (!mWorkspace.isFinishedSwitchingState()) {
            return;
        }

        if (v instanceof Workspace) {
            if (mWorkspace.isInOverviewMode()) {
                mWorkspace.exitOverviewMode(true);
            }
            return;
        }

        if (v instanceof CellLayout) {
            if (mWorkspace.isInOverviewMode()) {
                mWorkspace.exitOverviewMode(mWorkspace.indexOfChild(v), true);
            }
        }

        Object tag = v.getTag();
        if (tag instanceof ShortcutInfo) {
        	ShortcutInfo shortcutInfo = (ShortcutInfo) tag;
        	if( shortcutInfo!=null&&shortcutInfo.intent != null &&shortcutInfo.intent.getComponent()!=null&& 
        	        shortcutInfo.intent.getComponent().getPackageName()!=null && 
        	        shortcutInfo.intent.getComponent().getPackageName().equals("com.starcor.mango")){
        		START_GAME_BY_XC(Launcher.this,"com.starcor.mango");
        	}else{
                onClickAppShortcut(v);
        	}
        } else if (tag instanceof FolderInfo) {
            if (v instanceof FolderIcon) {
                onClickFolderIcon(v);
            }
        } else if (v == mAllAppsButton) {
        	Log.w("super_hw", "Launcher onClick appbtn click");
            onClickAllAppsButton(v);
        } else if (tag instanceof AppInfo) {
            startAppShortcutOrInfoActivity(v);
        } else if (tag instanceof LauncherAppWidgetInfo) {
            if (v instanceof PendingAppWidgetHostView) {
                onClickPendingWidget((PendingAppWidgetHostView) v);
            }
        }
    }

    public void onClickPagedViewIcon(View v) {
        startAppShortcutOrInfoActivity(v);
        if (mLauncherCallbacks != null) {
            mLauncherCallbacks.onClickPagedViewIcon(v);
        }
    }

    public boolean onTouch(View v, MotionEvent event) {
        return false;
    }

    /**
     * Event handler for the app widget view which has not fully restored.
     */
    public void onClickPendingWidget(final PendingAppWidgetHostView v) {
        if (mIsSafeModeEnabled) {
            Toast.makeText(this, R.string.safemode_widget_error, Toast.LENGTH_SHORT).show();
            return;
        }

        final LauncherAppWidgetInfo info = (LauncherAppWidgetInfo) v.getTag();
        if (v.isReadyForClickSetup()) {
            int widgetId = info.appWidgetId;
            AppWidgetProviderInfo appWidgetInfo = mAppWidgetManager.getAppWidgetInfo(widgetId);
            if (appWidgetInfo != null) {
                mPendingAddWidgetInfo = appWidgetInfo;
                mPendingAddInfo.copyFrom(info);
                mPendingAddWidgetId = widgetId;

                AppWidgetManagerCompat.getInstance(this).startConfigActivity(appWidgetInfo,
                        info.appWidgetId, this, mAppWidgetHost, REQUEST_RECONFIGURE_APPWIDGET);
            }
        } else if (info.installProgress < 0) {
            // The install has not been queued
            final String packageName = info.providerName.getPackageName();
            showBrokenAppInstallDialog(packageName,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        startActivitySafely(v, LauncherModel.getMarketIntent(packageName), info);
                    }
                });
        } else {
            // Download has started.
            final String packageName = info.providerName.getPackageName();
            startActivitySafely(v, LauncherModel.getMarketIntent(packageName), info);
        }
    }

    /**
     * Event handler for the "grid" button that appears on the home screen, which
     * enters all apps mode.
     *
     * @param v The view that was clicked.
     */
    protected void onClickAllAppsButton(View v) {
        if (LOGD) Log.d(TAG, "onClickAllAppsButton");
        if (isAllAppsVisible()) {
            showWorkspace(true);
        } else {
            showAllApps(true, AppsCustomizePagedView.ContentType.Applications, false);
        }
        if (mLauncherCallbacks != null) {
            mLauncherCallbacks.onClickAllAppsButton(v);
        }
    }

    private void showBrokenAppInstallDialog(final String packageName,
            DialogInterface.OnClickListener onSearchClickListener) {
        new AlertDialog.Builder(this)
            .setTitle(R.string.abandoned_promises_title)
            .setMessage(R.string.abandoned_promise_explanation)
            .setPositiveButton(R.string.abandoned_search, onSearchClickListener)
            .setNeutralButton(R.string.abandoned_clean_this,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        final UserHandleCompat user = UserHandleCompat.myUserHandle();
                        mWorkspace.removeAbandonedPromise(packageName, user);
                    }
                })
            .create().show();
        return;
    }

    /**
     * Event handler for an app shortcut click.
     *
     * @param v The view that was clicked. Must be a tagged with a {@link ShortcutInfo}.
     */
    protected void onClickAppShortcut(final View v) {
        if (LOGD) Log.d(TAG, "onClickAppShortcut");
        Object tag = v.getTag();
        if (!(tag instanceof ShortcutInfo)) {
            throw new IllegalArgumentException("Input must be a Shortcut");
        }

        // Open shortcut
        final ShortcutInfo shortcut = (ShortcutInfo) tag;

        if (shortcut.isDisabled != 0) {
            int error = R.string.activity_not_available;
            if ((shortcut.isDisabled & ShortcutInfo.FLAG_DISABLED_SAFEMODE) != 0) {
                error = R.string.safemode_shortcut_error;
            }
            Toast.makeText(this, error, Toast.LENGTH_SHORT).show();
            return;
        }

        final Intent intent = shortcut.intent;

        // Check for special shortcuts
        if (intent.getComponent() != null) {
            final String shortcutClass = intent.getComponent().getClassName();

            if (shortcutClass.equals(MemoryDumpActivity.class.getName())) {
                MemoryDumpActivity.startDump(this);
                return;
            } else if (shortcutClass.equals(ToggleWeightWatcher.class.getName())) {
                toggleShowWeightWatcher();
                return;
            }
        }

        // Check for abandoned promise
        if ((v instanceof BubbleTextView)
                && shortcut.isPromise()
                && !shortcut.hasStatusFlag(ShortcutInfo.FLAG_INSTALL_SESSION_ACTIVE)) {
            showBrokenAppInstallDialog(
                    shortcut.getTargetComponent().getPackageName(),
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            startAppShortcutOrInfoActivity(v);
                        }
                    });
            return;
        }

        // Start activities
        startAppShortcutOrInfoActivity(v);

        if (mLauncherCallbacks != null) {
            mLauncherCallbacks.onClickAppShortcut(v);
        }
    }

    private void startAppShortcutOrInfoActivity(View v) {
        Object tag = v.getTag();
        final ShortcutInfo shortcut;
        final Intent intent;
        if (tag instanceof ShortcutInfo) {
            shortcut = (ShortcutInfo) tag;
            intent = shortcut.intent;
            int[] pos = new int[2];
            v.getLocationOnScreen(pos);
            intent.setSourceBounds(new Rect(pos[0], pos[1],
                    pos[0] + v.getWidth(), pos[1] + v.getHeight()));

        } else if (tag instanceof AppInfo) {
            shortcut = null;
            intent = ((AppInfo) tag).intent;
        } else {
            throw new IllegalArgumentException("Input must be a Shortcut or AppInfo");
        }

        boolean success = startActivitySafely(v, intent, tag);
        mStats.recordLaunch(intent, shortcut);

        if (success && v instanceof BubbleTextView) {
            mWaitingForResume = (BubbleTextView) v;
            mWaitingForResume.setStayPressed(true);
        }
    }

    /**
     * Event handler for a folder icon click.
     *
     * @param v The view that was clicked. Must be an instance of {@link FolderIcon}.
     */
    protected void onClickFolderIcon(View v) {
        if (LOGD) Log.d(TAG, "onClickFolder");
        if (!(v instanceof FolderIcon)){
            throw new IllegalArgumentException("Input must be a FolderIcon");
        }

        FolderIcon folderIcon = (FolderIcon) v;
        final FolderInfo info = folderIcon.getFolderInfo();
        Folder openFolder = mWorkspace.getFolderForTag(info);

        // If the folder info reports that the associated folder is open, then verify that
        // it is actually opened. There have been a few instances where this gets out of sync.
        if (info.opened && openFolder == null) {
            Log.d(TAG, "Folder info marked as open, but associated folder is not open. Screen: "
                    + info.screenId + " (" + info.cellX + ", " + info.cellY + ")");
            info.opened = false;
        }

        if (!info.opened && !folderIcon.getFolder().isDestroyed()) {
            // Close any open folder
            closeFolder();
            // Open the requested folder
            openFolder(folderIcon);
        } else {
            // Find the open folder...
            int folderScreen;
            if (openFolder != null) {
                folderScreen = mWorkspace.getPageForView(openFolder);
                // .. and close it
                closeFolder(openFolder);
                if (folderScreen != mWorkspace.getCurrentPage()) {
                    // Close any folder open on the current screen
                    closeFolder();
                    // Pull the folder onto this screen
                    openFolder(folderIcon);
                }
            }
        }

        if (mLauncherCallbacks != null) {
            mLauncherCallbacks.onClickFolderIcon(v);
        }
    }

    /**
     * Event handler for the (Add) Widgets button that appears after a long press
     * on the home screen.
     */
    protected void onClickAddWidgetButton(View view) {
        if (LOGD) Log.d(TAG, "onClickAddWidgetButton");
        if (mIsSafeModeEnabled) {
            Toast.makeText(this, R.string.safemode_widget_error, Toast.LENGTH_SHORT).show();
        } else {
            showAllApps(true, AppsCustomizePagedView.ContentType.Widgets, true);
            if (mLauncherCallbacks != null) {
                mLauncherCallbacks.onClickAddWidgetButton(view);
            }
        }
    }

    /**
     * Event handler for the wallpaper picker button that appears after a long press
     * on the home screen.
     */
    protected void onClickWallpaperPicker(View v) {
        if (LOGD) Log.d(TAG, "onClickWallpaperPicker");
        final Intent pickWallpaper = new Intent(Intent.ACTION_SET_WALLPAPER);
        pickWallpaper.setComponent(getWallpaperPickerComponent());
        startActivityForResult(pickWallpaper, REQUEST_PICK_WALLPAPER);

        if (mLauncherCallbacks != null) {
            mLauncherCallbacks.onClickWallpaperPicker(v);
        }
    }

    /**
     * Event handler for a click on the settings button that appears after a long press
     * on the home screen.
     */
    protected void onClickSettingsButton(View v) {
        if (LOGD) Log.d(TAG, "onClickSettingsButton");
        if (mLauncherCallbacks != null) {
            mLauncherCallbacks.onClickSettingsButton(v);
        }
    }

    public void onTouchDownAllAppsButton(View v) {
        // Provide the same haptic feedback that the system offers for virtual keys.
        v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
    }

    public void performHapticFeedbackOnTouchDown(View v) {
        // Provide the same haptic feedback that the system offers for virtual keys.
        v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
    }

    public View.OnTouchListener getHapticFeedbackTouchListener() {
        if (mHapticFeedbackTouchListener == null) {
            mHapticFeedbackTouchListener = new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    if ((event.getAction() & MotionEvent.ACTION_MASK) == MotionEvent.ACTION_DOWN) {
                        v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
                    }
                    return false;
                }
            };
        }
        return mHapticFeedbackTouchListener;
    }

    public void onDragStarted(View view) {
        if (isOnCustomContent()) {
            // Custom content screen doesn't participate in drag and drop. If on custom
            // content screen, move to default.
            moveWorkspaceToDefaultScreen();
        }

        if (mLauncherCallbacks != null) {
            mLauncherCallbacks.onDragStarted(view);
        }
    }

    /**
     * Called when the user stops interacting with the launcher.
     * This implies that the user is now on the homescreen and is not doing housekeeping.
     */
    protected void onInteractionEnd() {
        if (mLauncherCallbacks != null) {
            mLauncherCallbacks.onInteractionEnd();
        }
    }

    /**
     * Called when the user starts interacting with the launcher.
     * The possible interactions are:
     *  - open all apps
     *  - reorder an app shortcut, or a widget
     *  - open the overview mode.
     * This is a good time to stop doing things that only make sense
     * when the user is on the homescreen and not doing housekeeping.
     */
    protected void onInteractionBegin() {
        if (mLauncherCallbacks != null) {
            mLauncherCallbacks.onInteractionBegin();
        }
    }

    void startApplicationDetailsActivity(ComponentName componentName, UserHandleCompat user) {
        try {
            LauncherAppsCompat launcherApps = LauncherAppsCompat.getInstance(this);
            launcherApps.showAppDetailsForProfile(componentName, user);
        } catch (SecurityException e) {
            Toast.makeText(this, R.string.activity_not_found, Toast.LENGTH_SHORT).show();
            Log.e(TAG, "Launcher does not have permission to launch settings");
        } catch (ActivityNotFoundException e) {
            Toast.makeText(this, R.string.activity_not_found, Toast.LENGTH_SHORT).show();
            Log.e(TAG, "Unable to launch settings");
        }
    }

    // returns true if the activity was started
    boolean startApplicationUninstallActivity(ComponentName componentName, int flags,
            UserHandleCompat user) {
        if ((flags & AppInfo.DOWNLOADED_FLAG) == 0) {
            // System applications cannot be installed. For now, show a toast explaining that.
            // We may give them the option of disabling apps this way.
            int messageId = R.string.uninstall_system_app_text;
            Toast.makeText(this, messageId, Toast.LENGTH_SHORT).show();
            return false;
        } else {
            String packageName = componentName.getPackageName();
            String className = componentName.getClassName();
            Intent intent = new Intent(
                    Intent.ACTION_DELETE, Uri.fromParts("package", packageName, className));
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
                    Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
            if (user != null) {
                user.addToIntent(intent, Intent.EXTRA_USER);
            }
            startActivity(intent);
            return true;
        }
    }

	private void START_GAME_BY_XC(final Context context,
			final String pkgname) {
		PackageInfo pi = null;
		try {
			pi = context.getPackageManager().getPackageInfo(pkgname, 0);
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
		if (pi == null) {
			return;
		}
		Intent resolveIntent = new Intent(Intent.ACTION_MAIN);
		resolveIntent.addCategory(Intent.CATEGORY_LAUNCHER);
		resolveIntent.setPackage(pi.packageName);
		List<ResolveInfo> apps = context.getPackageManager()
				.queryIntentActivities(resolveIntent, 0);
		ResolveInfo ri = apps.iterator().next();
		if (ri != null) {
			String className = ri.activityInfo.name;
			Intent intent = new Intent(Intent.ACTION_MAIN);
			intent.setPackage(pkgname);
			intent.addCategory(Intent.CATEGORY_LAUNCHER);
			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
					| Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
			ComponentName cn = new ComponentName(pkgname, className);
			intent.addFlags(0x10000000);
			intent.setComponent(cn);
			context.startActivity(intent);
		}
	}
    
    boolean startActivity(View v, Intent intent, Object tag) {
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        try {
            // Only launch using the new animation if the shortcut has not opted out (this is a
            // private contract between launcher and may be ignored in the future).
            boolean useLaunchAnimation = (v != null) &&
                    !intent.hasExtra(INTENT_EXTRA_IGNORE_LAUNCH_ANIMATION);
            LauncherAppsCompat launcherApps = LauncherAppsCompat.getInstance(this);
            UserManagerCompat userManager = UserManagerCompat.getInstance(this);

            UserHandleCompat user = null;
            if (intent.hasExtra(AppInfo.EXTRA_PROFILE)) {
                long serialNumber = intent.getLongExtra(AppInfo.EXTRA_PROFILE, -1);
                user = userManager.getUserForSerialNumber(serialNumber);
            }

            Bundle optsBundle = null;
            if (useLaunchAnimation) {
                ActivityOptions opts = Utilities.isLmpOrAbove() ?
                        ActivityOptions.makeCustomAnimation(this, R.anim.task_open_enter, R.anim.no_anim) :
                        ActivityOptions.makeScaleUpAnimation(v, 0, 0, v.getMeasuredWidth(), v.getMeasuredHeight());
                optsBundle = opts.toBundle();
            }

            if (user == null || user.equals(UserHandleCompat.myUserHandle())) {
                // Could be launching some bookkeeping activity
                startActivity(intent, optsBundle);
            } else {
                // TODO Component can be null when shortcuts are supported for secondary user
                launcherApps.startActivityForProfile(intent.getComponent(), user,
                        intent.getSourceBounds(), optsBundle);
            }
            return true;
        } catch (SecurityException e) {
            Toast.makeText(this, R.string.activity_not_found, Toast.LENGTH_SHORT).show();
            Log.e(TAG, "Launcher does not have the permission to launch " + intent +
                    ". Make sure to create a MAIN intent-filter for the corresponding activity " +
                    "or use the exported attribute for this activity. "
                    + "tag="+ tag + " intent=" + intent, e);
        }
        return false;
    }

    boolean startActivitySafely(View v, Intent intent, Object tag) {
        boolean success = false;
        if (mIsSafeModeEnabled && !Utilities.isSystemApp(this, intent)) {
            Toast.makeText(this, R.string.safemode_shortcut_error, Toast.LENGTH_SHORT).show();
            return false;
        }
        try {
            success = startActivity(v, intent, tag);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(this, R.string.activity_not_found, Toast.LENGTH_SHORT).show();
            Log.e(TAG, "Unable to launch. tag=" + tag + " intent=" + intent, e);
        }
        return success;
    }

    /**
     * This method draws the FolderIcon to an ImageView and then adds and positions that ImageView
     * in the DragLayer in the exact absolute location of the original FolderIcon.
     */
    private void copyFolderIconToImage(FolderIcon fi) {
        final int width = fi.getMeasuredWidth();
        final int height = fi.getMeasuredHeight();

        // Lazy load ImageView, Bitmap and Canvas
        if (mFolderIconImageView == null) {
            mFolderIconImageView = new ImageView(this);
        }
        if (mFolderIconBitmap == null || mFolderIconBitmap.getWidth() != width ||
                mFolderIconBitmap.getHeight() != height) {
            mFolderIconBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            mFolderIconCanvas = new Canvas(mFolderIconBitmap);
        }

        DragLayer.LayoutParams lp;
        if (mFolderIconImageView.getLayoutParams() instanceof DragLayer.LayoutParams) {
            lp = (DragLayer.LayoutParams) mFolderIconImageView.getLayoutParams();
        } else {
            lp = new DragLayer.LayoutParams(width, height);
        }

        // The layout from which the folder is being opened may be scaled, adjust the starting
        // view size by this scale factor.
        float scale = mDragLayer.getDescendantRectRelativeToSelf(fi, mRectForFolderAnimation);
        lp.customPosition = true;
        lp.x = mRectForFolderAnimation.left;
        lp.y = mRectForFolderAnimation.top;
        lp.width = (int) (scale * width);
        lp.height = (int) (scale * height);

        mFolderIconCanvas.drawColor(0, PorterDuff.Mode.CLEAR);
        fi.draw(mFolderIconCanvas);
        mFolderIconImageView.setImageBitmap(mFolderIconBitmap);
        if (fi.getFolder() != null) {
            mFolderIconImageView.setPivotX(fi.getFolder().getPivotXForIconAnimation());
            mFolderIconImageView.setPivotY(fi.getFolder().getPivotYForIconAnimation());
        }
        // Just in case this image view is still in the drag layer from a previous animation,
        // we remove it and re-add it.
        if (mDragLayer.indexOfChild(mFolderIconImageView) != -1) {
            mDragLayer.removeView(mFolderIconImageView);
        }
        mDragLayer.addView(mFolderIconImageView, lp);
        if (fi.getFolder() != null) {
            fi.getFolder().bringToFront();
        }
    }

    private void growAndFadeOutFolderIcon(FolderIcon fi) {
        if (fi == null) return;
        PropertyValuesHolder alpha = PropertyValuesHolder.ofFloat("alpha", 0);
        PropertyValuesHolder scaleX = PropertyValuesHolder.ofFloat("scaleX", 1.5f);
        PropertyValuesHolder scaleY = PropertyValuesHolder.ofFloat("scaleY", 1.5f);

        FolderInfo info = (FolderInfo) fi.getTag();
        if (info.container == LauncherSettings.Favorites.CONTAINER_HOTSEAT) {
            CellLayout cl = (CellLayout) fi.getParent().getParent();
            CellLayout.LayoutParams lp = (CellLayout.LayoutParams) fi.getLayoutParams();
            cl.setFolderLeaveBehindCell(lp.cellX, lp.cellY);
        }

        // Push an ImageView copy of the FolderIcon into the DragLayer and hide the original
        copyFolderIconToImage(fi);
        fi.setVisibility(View.INVISIBLE);

        ObjectAnimator oa = LauncherAnimUtils.ofPropertyValuesHolder(mFolderIconImageView, alpha,
                scaleX, scaleY);
        if (Utilities.isLmpOrAbove()) {
            oa.setInterpolator(new LogDecelerateInterpolator(100, 0));
        }
        oa.setDuration(getResources().getInteger(R.integer.config_folderExpandDuration));
        oa.start();
    }

    private void shrinkAndFadeInFolderIcon(final FolderIcon fi) {
        if (fi == null) return;
        PropertyValuesHolder alpha = PropertyValuesHolder.ofFloat("alpha", 1.0f);
        PropertyValuesHolder scaleX = PropertyValuesHolder.ofFloat("scaleX", 1.0f);
        PropertyValuesHolder scaleY = PropertyValuesHolder.ofFloat("scaleY", 1.0f);

        final CellLayout cl = (CellLayout) fi.getParent().getParent();

        // We remove and re-draw the FolderIcon in-case it has changed
        mDragLayer.removeView(mFolderIconImageView);
        copyFolderIconToImage(fi);
        ObjectAnimator oa = LauncherAnimUtils.ofPropertyValuesHolder(mFolderIconImageView, alpha,
                scaleX, scaleY);
        oa.setDuration(getResources().getInteger(R.integer.config_folderExpandDuration));
        oa.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                if (cl != null) {
                    cl.clearFolderLeaveBehind();
                    // Remove the ImageView copy of the FolderIcon and make the original visible.
                    mDragLayer.removeView(mFolderIconImageView);
                    fi.setVisibility(View.VISIBLE);
                }
            }
        });
        oa.start();
    }

    /**
     * Opens the user folder described by the specified tag. The opening of the folder
     * is animated relative to the specified View. If the View is null, no animation
     * is played.
     *
     * @param folderInfo The FolderInfo describing the folder to open.
     */
    public void openFolder(FolderIcon folderIcon) {
        Folder folder = folderIcon.getFolder();
        FolderInfo info = folder.mInfo;

        info.opened = true;

        // Just verify that the folder hasn't already been added to the DragLayer.
        // There was a one-off crash where the folder had a parent already.
        if (folder.getParent() == null) {
            mDragLayer.addView(folder);
            mDragController.addDropTarget((DropTarget) folder);
        } else {
            Log.w(TAG, "Opening folder (" + folder + ") which already has a parent (" +
                    folder.getParent() + ").");
        }
        folder.animateOpen();
        growAndFadeOutFolderIcon(folderIcon);

        // Notify the accessibility manager that this folder "window" has appeared and occluded
        // the workspace items
        folder.sendAccessibilityEvent(AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED);
        getDragLayer().sendAccessibilityEvent(AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED);
    }

    public void closeFolder() {
        Folder folder = mWorkspace != null ? mWorkspace.getOpenFolder() : null;
        if (folder != null) {
            if (folder.isEditingName()) {
                folder.dismissEditingName();
            }
            closeFolder(folder);
        }
    }

    void closeFolder(Folder folder) {
        folder.getInfo().opened = false;

        ViewGroup parent = (ViewGroup) folder.getParent().getParent();
        if (parent != null) {
            FolderIcon fi = (FolderIcon) mWorkspace.getViewForTag(folder.mInfo);
            shrinkAndFadeInFolderIcon(fi);
        }
        folder.animateClosed();

        // Notify the accessibility manager that this folder "window" has disappeard and no
        // longer occludeds the workspace items
        getDragLayer().sendAccessibilityEvent(AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED);
    }

    public boolean onLongClick(View v) {
    	Log.i("super_hw", "Launcher onLongClick() view.toStr : " + v.toString());
        if (!isDraggingEnabled()) return false;
        if (isWorkspaceLocked()) return false;
        if (mState != State.WORKSPACE) return false;

        if (v instanceof Workspace) {
            if (!mWorkspace.isInOverviewMode()) {
                if (mWorkspace.enterOverviewMode()) {
                    mWorkspace.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS,
                            HapticFeedbackConstants.FLAG_IGNORE_VIEW_SETTING);
                    return true;
                } else {
                    return false;
                }
            } else {
                return false;
            }
        }

        CellLayout.CellInfo longClickCellInfo = null;
        View itemUnderLongClick = null;
        if (v.getTag() instanceof ItemInfo) {
            ItemInfo info = (ItemInfo) v.getTag();
            longClickCellInfo = new CellLayout.CellInfo(v, info);;
            itemUnderLongClick = longClickCellInfo.cell;
            resetAddInfo();
        }

        // The hotseat touch handling does not go through Workspace, and we always allow long press
        // on hotseat items.
        final boolean inHotseat = isHotseatLayout(v);
        boolean allowLongPress = inHotseat || mWorkspace.allowLongPress();
        if (allowLongPress && !mDragController.isDragging()) {
            if (itemUnderLongClick == null) {
                // User long pressed on empty space
                mWorkspace.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS,
                        HapticFeedbackConstants.FLAG_IGNORE_VIEW_SETTING);
                if (mWorkspace.isInOverviewMode()) {
                    mWorkspace.startReordering(v);
                } else {
                    mWorkspace.enterOverviewMode();
                }
            } else {
                final boolean isAllAppsButton = inHotseat && isAllAppsButtonRank(
                        mHotseat.getOrderInHotseat(
                                longClickCellInfo.cellX,
                                longClickCellInfo.cellY));
                if (!(itemUnderLongClick instanceof Folder || isAllAppsButton)) {
                    // User long pressed on an item
                    mWorkspace.startDrag(longClickCellInfo);
                }
            }
        }
        return true;
    }

    boolean isHotseatLayout(View layout) {
        return mHotseat != null && layout != null &&
                (layout instanceof CellLayout) && (layout == mHotseat.getLayout());
    }

    /**
     * Returns the CellLayout of the specified container at the specified screen.
     */
    CellLayout getCellLayout(long container, long screenId) {
        if (container == LauncherSettings.Favorites.CONTAINER_HOTSEAT) {
            if (mHotseat != null) {
                return mHotseat.getLayout();
            } else {
                return null;
            }
        } else {
            return (CellLayout) mWorkspace.getScreenWithId(screenId);
        }
    }

    public boolean isAllAppsVisible() {
        return (mState == State.APPS_CUSTOMIZE) || (mOnResumeState == State.APPS_CUSTOMIZE);
    }

    private void setWorkspaceBackground(boolean workspace) {
        mLauncherView.setBackground(workspace ?
                mWorkspaceBackgroundDrawable : null);
    }

    protected void changeWallpaperVisiblity(boolean visible) {
        int wpflags = visible ? WindowManager.LayoutParams.FLAG_SHOW_WALLPAPER : 0;
        int curflags = getWindow().getAttributes().flags
                & WindowManager.LayoutParams.FLAG_SHOW_WALLPAPER;
        if (wpflags != curflags) {
            getWindow().setFlags(wpflags, WindowManager.LayoutParams.FLAG_SHOW_WALLPAPER);
        }
        setWorkspaceBackground(visible);
    }

    private void dispatchOnLauncherTransitionPrepare(View v, boolean animated, boolean toWorkspace) {
        if (v instanceof LauncherTransitionable) {
            ((LauncherTransitionable) v).onLauncherTransitionPrepare(this, animated, toWorkspace);
        }
    }

    private void dispatchOnLauncherTransitionStart(View v, boolean animated, boolean toWorkspace) {
        if (v instanceof LauncherTransitionable) {
            ((LauncherTransitionable) v).onLauncherTransitionStart(this, animated, toWorkspace);
        }

        // Update the workspace transition step as well
        dispatchOnLauncherTransitionStep(v, 0f);
    }

    private void dispatchOnLauncherTransitionStep(View v, float t) {
        if (v instanceof LauncherTransitionable) {
            ((LauncherTransitionable) v).onLauncherTransitionStep(this, t);
        }
    }

    private void dispatchOnLauncherTransitionEnd(View v, boolean animated, boolean toWorkspace) {
        if (v instanceof LauncherTransitionable) {
            ((LauncherTransitionable) v).onLauncherTransitionEnd(this, animated, toWorkspace);
        }

        // Update the workspace transition step as well
        dispatchOnLauncherTransitionStep(v, 1f);
    }

    /**
     * Things to test when changing the following seven functions.
     *   - Home from workspace
     *          - from center screen
     *          - from other screens
     *   - Home from all apps
     *          - from center screen
     *          - from other screens
     *   - Back from all apps
     *          - from center screen
     *          - from other screens
     *   - Launch app from workspace and quit
     *          - with back
     *          - with home
     *   - Launch app from all apps and quit
     *          - with back
     *          - with home
     *   - Go to a screen that's not the default, then all
     *     apps, and launch and app, and go back
     *          - with back
     *          -with home
     *   - On workspace, long press power and go back
     *          - with back
     *          - with home
     *   - On all apps, long press power and go back
     *          - with back
     *          - with home
     *   - On workspace, power off
     *   - On all apps, power off
     *   - Launch an app and turn off the screen while in that app
     *          - Go back with home key
     *          - Go back with back key  TODO: make this not go to workspace
     *          - From all apps
     *          - From workspace
     *   - Enter and exit car mode (becuase it causes an extra configuration changed)
     *          - From all apps
     *          - From the center workspace
     *          - From another workspace
     */

    /**
     * Zoom the camera out from the workspace to reveal 'toView'.
     * Assumes that the view to show is anchored at either the very top or very bottom
     * of the screen.
     */
    private void showAppsCustomizeHelper(final boolean animated, final boolean springLoaded) {
        AppsCustomizePagedView.ContentType contentType = mAppsCustomizeContent.getContentType();
        showAppsCustomizeHelper(animated, springLoaded, contentType);
    }

    private void showAppsCustomizeHelper(final boolean animated, final boolean springLoaded,
                                         final AppsCustomizePagedView.ContentType contentType) {
        if (mStateAnimation != null) {
            mStateAnimation.setDuration(0);
            mStateAnimation.cancel();
            mStateAnimation = null;
        }

        boolean material = Utilities.isLmpOrAbove();

        final Resources res = getResources();

        final int duration = res.getInteger(R.integer.config_appsCustomizeZoomInTime);
        final int fadeDuration = res.getInteger(R.integer.config_appsCustomizeFadeInTime);
        final int revealDuration = res.getInteger(R.integer.config_appsCustomizeRevealTime);
        final int itemsAlphaStagger =
                res.getInteger(R.integer.config_appsCustomizeItemsAlphaStagger);

        final float scale = (float) res.getInteger(R.integer.config_appsCustomizeZoomScaleFactor);
        final View fromView = mWorkspace;
        final AppsCustomizeTabHost toView = mAppsCustomizeTabHost;

        final ArrayList<View> layerViews = new ArrayList<View>();

        Workspace.State workspaceState = contentType == AppsCustomizePagedView.ContentType.Widgets ?
                Workspace.State.OVERVIEW_HIDDEN : Workspace.State.NORMAL_HIDDEN;
        Animator workspaceAnim =
                mWorkspace.getChangeStateAnimation(workspaceState, animated, layerViews);
        if (!LauncherAppState.isDisableAllApps()
                || contentType == AppsCustomizePagedView.ContentType.Widgets) {
            // Set the content type for the all apps/widgets space
            mAppsCustomizeTabHost.setContentTypeImmediate(contentType);
        }

        // If for some reason our views aren't initialized, don't animate
        boolean initialized = getAllAppsButton() != null;

        if (animated && initialized) {
            mStateAnimation = LauncherAnimUtils.createAnimatorSet();
            final AppsCustomizePagedView content = (AppsCustomizePagedView)
                    toView.findViewById(R.id.apps_customize_pane_content);

            final View page = content.getPageAt(content.getCurrentPage());
            final View revealView = toView.findViewById(R.id.fake_page);

            final boolean isWidgetTray = contentType == AppsCustomizePagedView.ContentType.Widgets;
            if (isWidgetTray) {
                revealView.setBackground(res.getDrawable(R.drawable.quantum_panel_dark));
            } else {
                revealView.setBackground(res.getDrawable(R.drawable.quantum_panel));
            }

            // Hide the real page background, and swap in the fake one
            content.setPageBackgroundsVisible(false);
            revealView.setVisibility(View.VISIBLE);
            // We need to hide this view as the animation start will be posted.
            revealView.setAlpha(0);

            int width = revealView.getMeasuredWidth();
            int height = revealView.getMeasuredHeight();
            float revealRadius = (float) Math.sqrt((width * width) / 4 + (height * height) / 4);

            revealView.setTranslationY(0);
            revealView.setTranslationX(0);

            // Get the y delta between the center of the page and the center of the all apps button
            int[] allAppsToPanelDelta = Utilities.getCenterDeltaInScreenSpace(revealView,
                    getAllAppsButton(), null);

            float alpha = 0;
            float xDrift = 0;
            float yDrift = 0;
            if (material) {
                alpha = isWidgetTray ? 0.3f : 1f;
                yDrift = isWidgetTray ? height / 2 : allAppsToPanelDelta[1];
                xDrift = isWidgetTray ? 0 : allAppsToPanelDelta[0];
            } else {
                yDrift = 2 * height / 3;
                xDrift = 0;
            }
            final float initAlpha = alpha;

            revealView.setLayerType(View.LAYER_TYPE_HARDWARE, null);
            layerViews.add(revealView);
            PropertyValuesHolder panelAlpha = PropertyValuesHolder.ofFloat("alpha", initAlpha, 1f);
            PropertyValuesHolder panelDriftY =
                    PropertyValuesHolder.ofFloat("translationY", yDrift, 0);
            PropertyValuesHolder panelDriftX =
                    PropertyValuesHolder.ofFloat("translationX", xDrift, 0);

            ObjectAnimator panelAlphaAndDrift = ObjectAnimator.ofPropertyValuesHolder(revealView,
                    panelAlpha, panelDriftY, panelDriftX);

            panelAlphaAndDrift.setDuration(revealDuration);
            panelAlphaAndDrift.setInterpolator(new LogDecelerateInterpolator(100, 0));

            mStateAnimation.play(panelAlphaAndDrift);

            if (page != null) {
                page.setVisibility(View.VISIBLE);
                page.setLayerType(View.LAYER_TYPE_HARDWARE, null);
                layerViews.add(page);

                ObjectAnimator pageDrift = ObjectAnimator.ofFloat(page, "translationY", yDrift, 0);
                page.setTranslationY(yDrift);
                pageDrift.setDuration(revealDuration);
                pageDrift.setInterpolator(new LogDecelerateInterpolator(100, 0));
                pageDrift.setStartDelay(itemsAlphaStagger);
                mStateAnimation.play(pageDrift);

                page.setAlpha(0f);
                ObjectAnimator itemsAlpha = ObjectAnimator.ofFloat(page, "alpha", 0f, 1f);
                itemsAlpha.setDuration(revealDuration);
                itemsAlpha.setInterpolator(new AccelerateInterpolator(1.5f));
                itemsAlpha.setStartDelay(itemsAlphaStagger);
                mStateAnimation.play(itemsAlpha);
            }

            View pageIndicators = toView.findViewById(R.id.apps_customize_page_indicator);
            pageIndicators.setAlpha(0.01f);
            ObjectAnimator indicatorsAlpha =
                    ObjectAnimator.ofFloat(pageIndicators, "alpha", 1f);
            indicatorsAlpha.setDuration(revealDuration);
            mStateAnimation.play(indicatorsAlpha);

            if (material) {
                final View allApps = getAllAppsButton();
                int allAppsButtonSize = LauncherAppState.getInstance().
                        getDynamicGrid().getDeviceProfile().allAppsButtonVisualSize;
                float startRadius = isWidgetTray ? 0 : allAppsButtonSize / 2;
                Animator reveal = ViewAnimationUtils.createCircularReveal(revealView, width / 2,
                                height / 2, startRadius, revealRadius);
                reveal.setDuration(revealDuration);
                reveal.setInterpolator(new LogDecelerateInterpolator(100, 0));

                reveal.addListener(new AnimatorListenerAdapter() {
                    public void onAnimationStart(Animator animation) {
                        if (!isWidgetTray) {
                            allApps.setVisibility(View.INVISIBLE);
                        }
                    }
                    public void onAnimationEnd(Animator animation) {
                        if (!isWidgetTray) {
                            allApps.setVisibility(View.VISIBLE);
                        }
                    }
                });
                mStateAnimation.play(reveal);
            }

            mStateAnimation.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    dispatchOnLauncherTransitionEnd(fromView, animated, false);
                    dispatchOnLauncherTransitionEnd(toView, animated, false);

                    revealView.setVisibility(View.INVISIBLE);
                    revealView.setLayerType(View.LAYER_TYPE_NONE, null);
                    if (page != null) {
                        page.setLayerType(View.LAYER_TYPE_NONE, null);
                    }
                    content.setPageBackgroundsVisible(true);

                    // Hide the search bar
                    if (mSearchDropTargetBar != null) {
                        mSearchDropTargetBar.hideSearchBar(false);
                    }

                    // This can hold unnecessary references to views.
                    mStateAnimation = null;
                }

            });

            if (workspaceAnim != null) {
                mStateAnimation.play(workspaceAnim);
            }

            dispatchOnLauncherTransitionPrepare(fromView, animated, false);
            dispatchOnLauncherTransitionPrepare(toView, animated, false);
            final AnimatorSet stateAnimation = mStateAnimation;
            final Runnable startAnimRunnable = new Runnable() {
                public void run() {
                    // Check that mStateAnimation hasn't changed while
                    // we waited for a layout/draw pass
                    if (mStateAnimation != stateAnimation)
                        return;
                    dispatchOnLauncherTransitionStart(fromView, animated, false);
                    dispatchOnLauncherTransitionStart(toView, animated, false);

                    revealView.setAlpha(initAlpha);
                    if (Utilities.isLmpOrAbove()) {
                        for (int i = 0; i < layerViews.size(); i++) {
                            View v = layerViews.get(i);
                            if (v != null) {
                                if (Utilities.isViewAttachedToWindow(v)) v.buildLayer();
                            }
                        }
                    }
                    mStateAnimation.start();
                }
            };
            toView.bringToFront();
            toView.setVisibility(View.VISIBLE);
            toView.post(startAnimRunnable);
        } else {
            toView.setTranslationX(0.0f);
            toView.setTranslationY(0.0f);
            toView.setScaleX(1.0f);
            toView.setScaleY(1.0f);
            toView.setVisibility(View.VISIBLE);
            toView.bringToFront();

            if (!springLoaded && !LauncherAppState.getInstance().isScreenLarge()) {
                // Hide the search bar
                if (mSearchDropTargetBar != null) {
                    mSearchDropTargetBar.hideSearchBar(false);
                }
            }
            dispatchOnLauncherTransitionPrepare(fromView, animated, false);
            dispatchOnLauncherTransitionStart(fromView, animated, false);
            dispatchOnLauncherTransitionEnd(fromView, animated, false);
            dispatchOnLauncherTransitionPrepare(toView, animated, false);
            dispatchOnLauncherTransitionStart(toView, animated, false);
            dispatchOnLauncherTransitionEnd(toView, animated, false);
        }
    }

    /**
     * Zoom the camera back into the workspace, hiding 'fromView'.
     * This is the opposite of showAppsCustomizeHelper.
     * @param animated If true, the transition will be animated.
     */
    private void hideAppsCustomizeHelper(Workspace.State toState, final boolean animated,
            final boolean springLoaded, final Runnable onCompleteRunnable) {

        if (mStateAnimation != null) {
            mStateAnimation.setDuration(0);
            mStateAnimation.cancel();
            mStateAnimation = null;
        }

        boolean material = Utilities.isLmpOrAbove();
        Resources res = getResources();

        final int duration = res.getInteger(R.integer.config_appsCustomizeZoomOutTime);
        final int fadeOutDuration = res.getInteger(R.integer.config_appsCustomizeFadeOutTime);
        final int revealDuration = res.getInteger(R.integer.config_appsCustomizeConcealTime);
        final int itemsAlphaStagger =
                res.getInteger(R.integer.config_appsCustomizeItemsAlphaStagger);

        final float scaleFactor = (float)
                res.getInteger(R.integer.config_appsCustomizeZoomScaleFactor);
        final View fromView = mAppsCustomizeTabHost;
        final View toView = mWorkspace;
        Animator workspaceAnim = null;
        final ArrayList<View> layerViews = new ArrayList<View>();

        if (toState == Workspace.State.NORMAL) {
            workspaceAnim = mWorkspace.getChangeStateAnimation(
                    toState, animated, layerViews);
        } else if (toState == Workspace.State.SPRING_LOADED ||
                toState == Workspace.State.OVERVIEW) {
            workspaceAnim = mWorkspace.getChangeStateAnimation(
                    toState, animated, layerViews);
        }

        // If for some reason our views aren't initialized, don't animate
        boolean initialized = getAllAppsButton() != null;

        if (animated && initialized) {
            mStateAnimation = LauncherAnimUtils.createAnimatorSet();
            if (workspaceAnim != null) {
                mStateAnimation.play(workspaceAnim);
            }

            final AppsCustomizePagedView content = (AppsCustomizePagedView)
                    fromView.findViewById(R.id.apps_customize_pane_content);

            final View page = content.getPageAt(content.getNextPage());

            // We need to hide side pages of the Apps / Widget tray to avoid some ugly edge cases
            int count = content.getChildCount();
            for (int i = 0; i < count; i++) {
                View child = content.getChildAt(i);
                if (child != page) {
                    child.setVisibility(View.INVISIBLE);
                }
            }
            final View revealView = fromView.findViewById(R.id.fake_page);

            // hideAppsCustomizeHelper is called in some cases when it is already hidden
            // don't perform all these no-op animations. In particularly, this was causing
            // the all-apps button to pop in and out.
            if (fromView.getVisibility() == View.VISIBLE) {
                AppsCustomizePagedView.ContentType contentType = content.getContentType();
                final boolean isWidgetTray =
                        contentType == AppsCustomizePagedView.ContentType.Widgets;

                if (isWidgetTray) {
                    revealView.setBackground(res.getDrawable(R.drawable.quantum_panel_dark));
                } else {
                    revealView.setBackground(res.getDrawable(R.drawable.quantum_panel));
                }

                int width = revealView.getMeasuredWidth();
                int height = revealView.getMeasuredHeight();
                float revealRadius = (float) Math.sqrt((width * width) / 4 + (height * height) / 4);

                // Hide the real page background, and swap in the fake one
                revealView.setVisibility(View.VISIBLE);
                content.setPageBackgroundsVisible(false);

                final View allAppsButton = getAllAppsButton();
                revealView.setTranslationY(0);
                int[] allAppsToPanelDelta = Utilities.getCenterDeltaInScreenSpace(revealView,
                        allAppsButton, null);

                float xDrift = 0;
                float yDrift = 0;
                if (material) {
                    yDrift = isWidgetTray ? height / 2 : allAppsToPanelDelta[1];
                    xDrift = isWidgetTray ? 0 : allAppsToPanelDelta[0];
                } else {
                    yDrift = 2 * height / 3;
                    xDrift = 0;
                }

                revealView.setLayerType(View.LAYER_TYPE_HARDWARE, null);
                TimeInterpolator decelerateInterpolator = material ?
                        new LogDecelerateInterpolator(100, 0) :
                        new DecelerateInterpolator(1f);

                // The vertical motion of the apps panel should be delayed by one frame
                // from the conceal animation in order to give the right feel. We correpsondingly
                // shorten the duration so that the slide and conceal end at the same time.
                ObjectAnimator panelDriftY = LauncherAnimUtils.ofFloat(revealView, "translationY",
                        0, yDrift);
                panelDriftY.setDuration(revealDuration - SINGLE_FRAME_DELAY);
                panelDriftY.setStartDelay(itemsAlphaStagger + SINGLE_FRAME_DELAY);
                panelDriftY.setInterpolator(decelerateInterpolator);
                mStateAnimation.play(panelDriftY);

                ObjectAnimator panelDriftX = LauncherAnimUtils.ofFloat(revealView, "translationX",
                        0, xDrift);
                panelDriftX.setDuration(revealDuration - SINGLE_FRAME_DELAY);
                panelDriftX.setStartDelay(itemsAlphaStagger + SINGLE_FRAME_DELAY);
                panelDriftX.setInterpolator(decelerateInterpolator);
                mStateAnimation.play(panelDriftX);

                if (isWidgetTray || !material) {
                    float finalAlpha = material ? 0.4f : 0f;
                    revealView.setAlpha(1f);
                    ObjectAnimator panelAlpha = LauncherAnimUtils.ofFloat(revealView, "alpha",
                            1f, finalAlpha);
                    panelAlpha.setDuration(material ? revealDuration : 150);
                    panelAlpha.setInterpolator(decelerateInterpolator);
                    panelAlpha.setStartDelay(material ? 0 : itemsAlphaStagger + SINGLE_FRAME_DELAY);
                    mStateAnimation.play(panelAlpha);
                }

                if (page != null) {
                    page.setLayerType(View.LAYER_TYPE_HARDWARE, null);

                    ObjectAnimator pageDrift = LauncherAnimUtils.ofFloat(page, "translationY",
                            0, yDrift);
                    page.setTranslationY(0);
                    pageDrift.setDuration(revealDuration - SINGLE_FRAME_DELAY);
                    pageDrift.setInterpolator(decelerateInterpolator);
                    pageDrift.setStartDelay(itemsAlphaStagger + SINGLE_FRAME_DELAY);
                    mStateAnimation.play(pageDrift);

                    page.setAlpha(1f);
                    ObjectAnimator itemsAlpha = LauncherAnimUtils.ofFloat(page, "alpha", 1f, 0f);
                    itemsAlpha.setDuration(100);
                    itemsAlpha.setInterpolator(decelerateInterpolator);
                    mStateAnimation.play(itemsAlpha);
                }

                View pageIndicators = fromView.findViewById(R.id.apps_customize_page_indicator);
                pageIndicators.setAlpha(1f);
                ObjectAnimator indicatorsAlpha =
                        LauncherAnimUtils.ofFloat(pageIndicators, "alpha", 0f);
                indicatorsAlpha.setDuration(revealDuration);
                indicatorsAlpha.setInterpolator(new DecelerateInterpolator(1.5f));
                mStateAnimation.play(indicatorsAlpha);

                width = revealView.getMeasuredWidth();

                if (material) {
                    if (!isWidgetTray) {
                        allAppsButton.setVisibility(View.INVISIBLE);
                    }
                    int allAppsButtonSize = LauncherAppState.getInstance().
                            getDynamicGrid().getDeviceProfile().allAppsButtonVisualSize;
                    float finalRadius = isWidgetTray ? 0 : allAppsButtonSize / 2;
                    Animator reveal =
                            LauncherAnimUtils.createCircularReveal(revealView, width / 2,
                                    height / 2, revealRadius, finalRadius);
                    reveal.setInterpolator(new LogDecelerateInterpolator(100, 0));
                    reveal.setDuration(revealDuration);
                    reveal.setStartDelay(itemsAlphaStagger);

                    reveal.addListener(new AnimatorListenerAdapter() {
                        public void onAnimationEnd(Animator animation) {
                            revealView.setVisibility(View.INVISIBLE);
                            if (!isWidgetTray) {
                                allAppsButton.setVisibility(View.VISIBLE);
                            }
                        }
                    });

                    mStateAnimation.play(reveal);
                }

                dispatchOnLauncherTransitionPrepare(fromView, animated, true);
                dispatchOnLauncherTransitionPrepare(toView, animated, true);
                mAppsCustomizeContent.stopScrolling();
            }

            mStateAnimation.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    fromView.setVisibility(View.GONE);
                    dispatchOnLauncherTransitionEnd(fromView, animated, true);
                    dispatchOnLauncherTransitionEnd(toView, animated, true);
                    if (onCompleteRunnable != null) {
                        onCompleteRunnable.run();
                    }

                    revealView.setLayerType(View.LAYER_TYPE_NONE, null);
                    if (page != null) {
                        page.setLayerType(View.LAYER_TYPE_NONE, null);
                    }
                    content.setPageBackgroundsVisible(true);
                    // Unhide side pages
                    int count = content.getChildCount();
                    for (int i = 0; i < count; i++) {
                        View child = content.getChildAt(i);
                        child.setVisibility(View.VISIBLE);
                    }

                    // Reset page transforms
                    if (page != null) {
                        page.setTranslationX(0);
                        page.setTranslationY(0);
                        page.setAlpha(1);
                    }
                    content.setCurrentPage(content.getNextPage());

                    mAppsCustomizeContent.updateCurrentPageScroll();

                    // This can hold unnecessary references to views.
                    mStateAnimation = null;
                }
            });

            final AnimatorSet stateAnimation = mStateAnimation;
            final Runnable startAnimRunnable = new Runnable() {
                public void run() {
                    // Check that mStateAnimation hasn't changed while
                    // we waited for a layout/draw pass
                    if (mStateAnimation != stateAnimation)
                        return;
                    dispatchOnLauncherTransitionStart(fromView, animated, false);
                    dispatchOnLauncherTransitionStart(toView, animated, false);

                    if (Utilities.isLmpOrAbove()) {
                        for (int i = 0; i < layerViews.size(); i++) {
                            View v = layerViews.get(i);
                            if (v != null) {
                                if (Utilities.isViewAttachedToWindow(v)) v.buildLayer();
                            }
                        }
                    }
                    mStateAnimation.start();
                }
            };
            fromView.post(startAnimRunnable);
        } else {
            fromView.setVisibility(View.GONE);
            dispatchOnLauncherTransitionPrepare(fromView, animated, true);
            dispatchOnLauncherTransitionStart(fromView, animated, true);
            dispatchOnLauncherTransitionEnd(fromView, animated, true);
            dispatchOnLauncherTransitionPrepare(toView, animated, true);
            dispatchOnLauncherTransitionStart(toView, animated, true);
            dispatchOnLauncherTransitionEnd(toView, animated, true);
        }
    }

    @Override
    public void onTrimMemory(int level) {
        super.onTrimMemory(level);
        if (level >= ComponentCallbacks2.TRIM_MEMORY_UI_HIDDEN) {
            // The widget preview db can result in holding onto over
            // 3MB of memory for caching which isn't necessary.
            SQLiteDatabase.releaseMemory();

            // This clears all widget bitmaps from the widget tray
            if (mAppsCustomizeTabHost != null) {
                mAppsCustomizeTabHost.trimMemory();
            }
        }
    }

    protected void showWorkspace(boolean animated) {
        showWorkspace(animated, null);
    }

    protected void showWorkspace() {
        showWorkspace(true);
    }

    void showWorkspace(boolean animated, Runnable onCompleteRunnable) {
        if (mState != State.WORKSPACE || mWorkspace.getState() != Workspace.State.NORMAL) {
            boolean wasInSpringLoadedMode = (mState != State.WORKSPACE);
            mWorkspace.setVisibility(View.VISIBLE);
            hideAppsCustomizeHelper(Workspace.State.NORMAL, animated, false, onCompleteRunnable);

            // Show the search bar (only animate if we were showing the drop target bar in spring
            // loaded mode)
            if (mSearchDropTargetBar != null) {
                mSearchDropTargetBar.showSearchBar(animated && wasInSpringLoadedMode);
            }

            // Set focus to the AppsCustomize button
            if (mAllAppsButton != null) {
                mAllAppsButton.requestFocus();
            }
        }

        // Change the state *after* we've called all the transition code
        mState = State.WORKSPACE;

        // Resume the auto-advance of widgets
        mUserPresent = true;
        updateRunning();

        // Send an accessibility event to announce the context change
        getWindow().getDecorView()
                .sendAccessibilityEvent(AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED);

        onWorkspaceShown(animated);
    }

    void showOverviewMode(boolean animated) {
        mWorkspace.setVisibility(View.VISIBLE);
        hideAppsCustomizeHelper(Workspace.State.OVERVIEW, animated, false, null);
        mState = State.WORKSPACE;
        onWorkspaceShown(animated);
    }

    public void onWorkspaceShown(boolean animated) {
    }

    void showAllApps(boolean animated, AppsCustomizePagedView.ContentType contentType,
                     boolean resetPageToZero) {
        if (mState != State.WORKSPACE) return;

        if (resetPageToZero) {
            mAppsCustomizeTabHost.reset();
        }
        showAppsCustomizeHelper(animated, false, contentType);
        mAppsCustomizeTabHost.post(new Runnable() {
            @Override
            public void run() {
                // We post this in-case the all apps view isn't yet constructed.
                mAppsCustomizeTabHost.requestFocus();
            }
        });

        // Change the state *after* we've called all the transition code
        mState = State.APPS_CUSTOMIZE;

        // Pause the auto-advance of widgets until we are out of AllApps
        mUserPresent = false;
        updateRunning();
        closeFolder();

        // Send an accessibility event to announce the context change
        getWindow().getDecorView()
                .sendAccessibilityEvent(AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED);
    }

    void enterSpringLoadedDragMode() {
        if (isAllAppsVisible()) {
            hideAppsCustomizeHelper(Workspace.State.SPRING_LOADED, true, true, null);
            mState = State.APPS_CUSTOMIZE_SPRING_LOADED;
        }
    }

    void exitSpringLoadedDragModeDelayed(final boolean successfulDrop, int delay,
            final Runnable onCompleteRunnable) {
        if (mState != State.APPS_CUSTOMIZE_SPRING_LOADED) return;

        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (successfulDrop) {
                    // Before we show workspace, hide all apps again because
                    // exitSpringLoadedDragMode made it visible. This is a bit hacky; we should
                    // clean up our state transition functions
                    mAppsCustomizeTabHost.setVisibility(View.GONE);
                    showWorkspace(true, onCompleteRunnable);
                } else {
                    exitSpringLoadedDragMode();
                }
            }
        }, delay);
    }

    void exitSpringLoadedDragMode() {
        if (mState == State.APPS_CUSTOMIZE_SPRING_LOADED) {
            final boolean animated = true;
            final boolean springLoaded = true;
            showAppsCustomizeHelper(animated, springLoaded);
            mState = State.APPS_CUSTOMIZE;
        }
        // Otherwise, we are not in spring loaded mode, so don't do anything.
    }

    void lockAllApps() {
        // TODO
    }

    void unlockAllApps() {
        // TODO
    }

    protected void disableVoiceButtonProxy(boolean disable) {
        // NO-OP
    }

    public View getQsbBar() {
        if (mLauncherCallbacks != null && mLauncherCallbacks.providesSearch()) {
            return mLauncherCallbacks.getQsbBar();
        }

        if (mQsb == null) {
        /**
         *之前添加的是搜索框的widget
         *现改成添加一个空的View
         */
//            AppWidgetProviderInfo searchProvider = Utilities.getSearchWidgetProvider(this);
//            if (searchProvider == null) {
//                return null;
//            }
//
//            Bundle opts = new Bundle();
//            opts.putInt(AppWidgetManager.OPTION_APPWIDGET_HOST_CATEGORY,
//                    AppWidgetProviderInfo.WIDGET_CATEGORY_SEARCHBOX);
//
//            SharedPreferences sp = getSharedPreferences(
//                    LauncherAppState.getSharedPreferencesKey(), MODE_PRIVATE);
//            int widgetId = sp.getInt(QSB_WIDGET_ID, -1);
//            AppWidgetProviderInfo widgetInfo = mAppWidgetManager.getAppWidgetInfo(widgetId);
//            if (!searchProvider.provider.flattenToString().equals(
//                    sp.getString(QSB_WIDGET_PROVIDER, null))
//                    || (widgetInfo == null)
//                    || !widgetInfo.provider.equals(searchProvider.provider)) {
//                // A valid widget is not already bound.
//                if (widgetId > -1) {
//                    mAppWidgetHost.deleteAppWidgetId(widgetId);
//                    widgetId = -1;
//                }
//
//                // Try to bind a new widget
//                widgetId = mAppWidgetHost.allocateAppWidgetId();
//
//                if (!AppWidgetManagerCompat.getInstance(this)
//                        .bindAppWidgetIdIfAllowed(widgetId, searchProvider, opts)) {
//                    mAppWidgetHost.deleteAppWidgetId(widgetId);
//                    widgetId = -1;
//                }
//
//                sp.edit()
//                    .putInt(QSB_WIDGET_ID, widgetId)
//                    .putString(QSB_WIDGET_PROVIDER, searchProvider.provider.flattenToString())
//                    .commit();
//            }
//
//            if (widgetId != -1) {
//                mQsb = mAppWidgetHost.createView(this, widgetId, searchProvider);
//                mQsb.updateAppWidgetOptions(opts);
//                mQsb.setPadding(0, 0, 0, 0);
//                mSearchDropTargetBar.addView(mQsb);
//            }
        	mQsb = new View(getApplicationContext()); 
        	mQsb.setVisibility(View.INVISIBLE);
        	mSearchDropTargetBar.addView(mQsb);
        }
        return mQsb;
    }

    @Override
    public boolean dispatchPopulateAccessibilityEvent(AccessibilityEvent event) {
        final boolean result = super.dispatchPopulateAccessibilityEvent(event);
        final List<CharSequence> text = event.getText();
        text.clear();
        // Populate event with a fake title based on the current state.
        if (mState == State.APPS_CUSTOMIZE) {
            text.add(mAppsCustomizeTabHost.getContentTag());
        } else {
            text.add(getString(R.string.all_apps_home_button_label));
        }
        return result;
    }

    /**
     * Receives notifications when system dialogs are to be closed.
     */
    private class CloseSystemDialogsIntentReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            closeSystemDialogs();
        }
    }

    /**
     * Receives notifications whenever the appwidgets are reset.
     */
    private class AppWidgetResetObserver extends ContentObserver {
        public AppWidgetResetObserver() {
            super(new Handler());
        }

        @Override
        public void onChange(boolean selfChange) {
            onAppWidgetReset();
        }
    }

    /**
     * If the activity is currently paused, signal that we need to run the passed Runnable
     * in onResume.
     *
     * This needs to be called from incoming places where resources might have been loaded
     * while we are paused.  That is becaues the Configuration might be wrong
     * when we're not running, and if it comes back to what it was when we
     * were paused, we are not restarted.
     *
     * Implementation of the method from LauncherModel.Callbacks.
     *
     * @return true if we are currently paused.  The caller might be able to
     * skip some work in that case since we will come back again.
     */
    private boolean waitUntilResume(Runnable run, boolean deletePreviousRunnables) {
        if (mPaused) {
            Log.i(TAG, "Deferring update until onResume");
            if (deletePreviousRunnables) {
                while (mBindOnResumeCallbacks.remove(run)) {
                }
            }
            mBindOnResumeCallbacks.add(run);
            return true;
        } else {
            return false;
        }
    }

    private boolean waitUntilResume(Runnable run) {
        return waitUntilResume(run, false);
    }

    public void addOnResumeCallback(Runnable run) {
        mOnResumeCallbacks.add(run);
    }

    /**
     * If the activity is currently paused, signal that we need to re-run the loader
     * in onResume.
     *
     * This needs to be called from incoming places where resources might have been loaded
     * while we are paused.  That is becaues the Configuration might be wrong
     * when we're not running, and if it comes back to what it was when we
     * were paused, we are not restarted.
     *
     * Implementation of the method from LauncherModel.Callbacks.
     *
     * @return true if we are currently paused.  The caller might be able to
     * skip some work in that case since we will come back again.
     */
    public boolean setLoadOnResume() {
        if (mPaused) {
            Log.i(TAG, "setLoadOnResume");
            mOnResumeNeedsLoad = true;
            return true;
        } else {
            return false;
        }
    }

    /**
     * Implementation of the method from LauncherModel.Callbacks.
     */
    public int getCurrentWorkspaceScreen() {
        if (mWorkspace != null) {
            return mWorkspace.getCurrentPage();
        } else {
            return SCREEN_COUNT / 2;
        }
    }

    /**
     * Refreshes the shortcuts shown on the workspace.
     *
     * Implementation of the method from LauncherModel.Callbacks.
     */
    public void startBinding() {
        setWorkspaceLoading(true);

        // If we're starting binding all over again, clear any bind calls we'd postponed in
        // the past (see waitUntilResume) -- we don't need them since we're starting binding
        // from scratch again
        mBindOnResumeCallbacks.clear();

        // Clear the workspace because it's going to be rebound
        mWorkspace.clearDropTargets();
        mWorkspace.removeAllWorkspaceScreens();

        mWidgetsToAdvance.clear();
        if (mHotseat != null) {
            mHotseat.resetLayout();
        }
    }

    @Override
    public void bindScreens(ArrayList<Long> orderedScreenIds) {
        bindAddScreens(orderedScreenIds);

        // If there are no screens, we need to have an empty screen
        if (orderedScreenIds.size() == 0) {
            mWorkspace.addExtraEmptyScreen();
        }

        // Create the custom content page (this call updates mDefaultScreen which calls
        // setCurrentPage() so ensure that all pages are added before calling this).
        if (hasCustomContentToLeft()) {
            mWorkspace.createCustomContentContainer();
            populateCustomContentContainer();
        }
    }

    @Override
    public void bindAddScreens(ArrayList<Long> orderedScreenIds) {
        // Log to disk
    	Log.e("super_hw", "Launcher bindAddScreens()");
        Launcher.addDumpLog(TAG, "11683562 - bindAddScreens()", true);
        Launcher.addDumpLog(TAG, "11683562 -   orderedScreenIds: " +
                TextUtils.join(", ", orderedScreenIds), true);
        int count = orderedScreenIds.size();
        for (int i = 0; i < count; i++) {
            mWorkspace.insertNewWorkspaceScreenBeforeEmptyScreen(orderedScreenIds.get(i));
        }
    }

    private boolean shouldShowWeightWatcher() {
        String spKey = LauncherAppState.getSharedPreferencesKey();
        SharedPreferences sp = getSharedPreferences(spKey, Context.MODE_PRIVATE);
        boolean show = sp.getBoolean(SHOW_WEIGHT_WATCHER, SHOW_WEIGHT_WATCHER_DEFAULT);

        return show;
    }

    private void toggleShowWeightWatcher() {
        String spKey = LauncherAppState.getSharedPreferencesKey();
        SharedPreferences sp = getSharedPreferences(spKey, Context.MODE_PRIVATE);
        boolean show = sp.getBoolean(SHOW_WEIGHT_WATCHER, true);

        show = !show;

        SharedPreferences.Editor editor = sp.edit();
        editor.putBoolean(SHOW_WEIGHT_WATCHER, show);
        editor.commit();

        if (mWeightWatcher != null) {
            mWeightWatcher.setVisibility(show ? View.VISIBLE : View.GONE);
        }
    }

    int screenIndex = 2;
    int lpx = 0;
    int lpy = 0;
    private FirstLoadValues checkDeskApp(ArrayList<ItemInfo> sBgWorkspaceItems, ShortcutInfo sInfo, long container, int screen, int cellX, int cellY, int cellXCount, int cellYCount){
    	FirstLoadValues mValue = null;
    	// TODO 初次进来数据库确保为空
    	try {
    		screen = screenIndex;
    		cellX = lpx;
    		cellY = lpy;
    		lpx++;
    		if (cellX >= cellXCount) {
    			lpy ++;
    			cellY = lpy;
    			lpx = 0;
    			cellX = 0;
    			lpx ++;
    			if (cellY >= cellYCount){
    				lpy = 0;
    				cellY = 0;
    				screenIndex++;
    				screen = screenIndex;
    			}
    		}
    		mValue = new  FirstLoadValues(sInfo, container, screenIndex, cellX, cellY);
		} catch (Exception e) {
			e.printStackTrace();
		}
    	return mValue;
    }
    
    private List<AppInfo> initFirstDesktop(List<AppInfo> appsOrder, int cellXCount, int cellYCount){
    	int addCount = 0;
    	final int screenId = 1;
    	/** TODO add app */
    	for(String pkgname : FirstScreenInfo.desktopItems){
    		ShortcutInfo sInfo;
    		for(AppInfo appInfo : appsOrder){
    			if(appInfo.componentName.getPackageName().equals(pkgname)){
    				// TODO
    				sInfo = new ShortcutInfo(appInfo);
    				FirstLoadValues mValue = new  FirstLoadValues(sInfo, -100, screenId, addCount + 1, 2);
//    						checkDeskApp(sInfo, -100, -1, 0, 0, cellXCount, cellYCount);
    				if(mValue != null) {
    					addCount ++;
    					LauncherModel.addOrMoveItemInDatabase(this, mValue.sInfo, mValue.container, mValue.screen, mValue.cellX, mValue.cellY);
    				}
    				appInfo.isFirstScreenAdd = true;
    				continue;
    			}
    		}
    	}
    	
    	/** TODO add folder */
    	List<AppInfo> installedApps = new ArrayList<AppInfo>();
    	for(String pkgname : FirstScreenInfo.folderItems) {
    		for(AppInfo appInfo : appsOrder){
    			if(appInfo.componentName.getPackageName().equals(pkgname)){
    				appInfo.isFirstScreenAdd = true;
    				installedApps.add(appInfo);
    				continue;
    			}
    		}
    	}
    	FolderInfo fInfo = new FolderInfo();
    	fInfo.title = FirstScreenInfo.folderName;
    	for(AppInfo appInfo : installedApps){
    		fInfo.add(new ShortcutInfo(appInfo));
    	}
    	FirstLoadValues fValue = new FirstLoadValues(fInfo, -100, screenId, 1 + addCount % 3, 2 + addCount / 3);
    	LauncherModel.addOrMoveItemInDatabase(this, fValue.fInfo, fValue.container, fValue.screen, fValue.cellX, fValue.cellY);
    	int cIndex = 0;
    	int folderWidth = getFolderWidth(installedApps.size());
    	long folderId = LauncherAppState.getInstance().sLauncherProvider.get().mOpenHelper.getFolderId();
//    	Log.d("super_hw", "Launcher_initFolder, childCount : " + installedApps.size() + " folderWidth : " + folderWidth + " folderId : " + folderId);
    	/**  TODO 关键位置,清理掉文件夹内的appInfo,后面添加最新的,避免后续拖动的过程中出现异常  */
    	fInfo.contents.clear();
    	for (AppInfo appInfo : installedApps) {
    		ShortcutInfo sInfo = new ShortcutInfo(appInfo);
    		FirstLoadValues mValue = new FirstLoadValues(sInfo, folderId, 0, cIndex%folderWidth, cIndex/folderWidth);
			mValue.cellX = cIndex % folderWidth;
			mValue.cellY = cIndex / folderWidth;
			LauncherModel.addOrMoveItemInDatabase(this, mValue.sInfo, mValue.container, mValue.screen, mValue.cellX, mValue.cellY);
			fInfo.contents.add(sInfo);
			cIndex++;
		}
    	
    	/** TODO add shrotcut app */
    	addCount = 0;
    	init_shortcut:
    	for(String pkgname : FirstScreenInfo.shortcutItems){
    		ShortcutInfo sInfo;
    		for(AppInfo appInfo : appsOrder){
    			if(appInfo.componentName.getPackageName().equals(pkgname)){
    				// TODO
    				sInfo = new ShortcutInfo(appInfo);
    				int index = FirstScreenInfo.shortcutItems.indexOf(pkgname);
    				FirstLoadValues mValue = new  FirstLoadValues(sInfo, -101, index, index, 0);
//    				FirstLoadValues mValue = new  FirstLoadValues(sInfo, -101, addCount, addCount , 0);
    				if(mValue != null) {
    					addCount ++;
    					LauncherModel.addOrMoveItemInDatabase(this, mValue.sInfo, mValue.container, mValue.screen, mValue.cellX, mValue.cellY);
    				}
    				appInfo.isFirstScreenAdd = true;
    				continue init_shortcut;// TODO 同一个包名的只装一个(类似于相机和图库存在多个图标的应用)
    			}
    		}
    	}
    	return appsOrder;
    }
    
    private int getFolderWidth(int childCount){
    	int width = 1;
    	while(width*width <= childCount){
    		width++;
    	}
    	return width;
    }
    
    /**
     * 第一次初始化,将所有应用添加进数据库
     * */
    public void initAppsFirst(ArrayList<ItemInfo> sBgWorkspaceItems){
    	Log.e("super_hw", "Launcher initAppsFirst()");
//    	if(sBgWorkspaceItems.size() > 0)
    	if(sBgWorkspaceItems.size() > 6)
    		return;
    	List<ResolveInfo> apps = null;
    	List<AppInfo> appsOrder = new ArrayList<AppInfo>();
//    	HashMap<String, AppInfo> temp = new HashMap<String, AppInfo>();
    	Intent mainIntent = new Intent(Intent.ACTION_MAIN, null).addCategory(Intent.CATEGORY_LAUNCHER);
    	PackageManager pm = getPackageManager();
    	apps = pm.queryIntentActivities(mainIntent, 0);
    	
    	int count = 0;
    	int addCount = 0;
    	UserHandleCompat myUser = UserHandleCompat.myUserHandle();
    	LauncherAppsCompat launcherApps = LauncherAppsCompat.getInstance(this);
    	String packageName = "";
    	List<LauncherActivityInfoCompat> matches = new ArrayList<LauncherActivityInfoCompat>();
    	for(ResolveInfo info : apps){
    		count ++;
//    		Log.d("super_hw", "info.pkg : " + info.resolvePackageName + " toString : " + info.toString());
    		packageName = info.activityInfo.applicationInfo.packageName;
    		// TODO 当不可用时,后面的在LauncherModel_loadWorkspace()里面会将不可用的删除掉,所以从入口控制
    		if (!info.activityInfo.enabled) continue;
    		matches.clear();
    		matches = launcherApps.getActivityList(packageName, myUser);
			for (LauncherActivityInfoCompat laInfo : matches) {
				if (laInfo != null
						&& info != null
						&& info.activityInfo != null
						&& info.activityInfo.name != null
						&& laInfo.getComponentName() != null
						&& laInfo.getComponentName().getClassName() != null
						&& laInfo.getComponentName().getClassName().equals(info.activityInfo.name)) {
					AppInfo appInfo = new AppInfo(this, laInfo, myUser, new IconCache((LauncherApplication) this.getApplication()), null);
					appsOrder.add(appInfo);
				}
			}
    	}
    	Collections.sort(appsOrder, LauncherModel.getAppNameComparator());
    	LauncherAppState app = LauncherAppState.getInstance();
    	DeviceProfile grid = app.getDynamicGrid().getDeviceProfile();
    	int cellXCount = (int)grid.numColumns;
    	int cellYCount = (int)grid.numRows;
    	appsOrder = initFirstDesktop(appsOrder,cellXCount, cellYCount);
    	ShortcutInfo sInfo = null;
    	final List<Integer> screenIndexNormal = new ArrayList<Integer>();
    	Log.e("super_hw", "Launcher initAppsFirst(), cellXCount : " + cellXCount + " cellYCount : " + cellYCount);
		for(AppInfo appInfo : appsOrder){
			if(appInfo.isFirstScreenAdd) continue;
//			if(appInfo.componentName.getPackageName().equals(this.getPackageName())) continue;
			if(isHideIcon(appInfo.componentName.getPackageName())) continue;
			boolean isContains = false;;
			sInfo = new ShortcutInfo(appInfo);
			FirstLoadValues mValue = checkDeskApp(sBgWorkspaceItems, sInfo, -100, -1, 0, 0, cellXCount, cellYCount);
			if(mValue != null) {
				addCount ++;
				LauncherModel.addOrMoveItemInDatabase(this, mValue.sInfo, mValue.container, mValue.screen, mValue.cellX, mValue.cellY);
				for(int index : screenIndexNormal){
					if(index == mValue.screen)
						isContains = true;
				}
				if(!isContains)
					screenIndexNormal.add(mValue.screen);
			}
		}
    	Log.e("super_hw", "Launcher initAppsFirst(), count : " + count + " addCount : " + addCount);
    }

	private boolean isHideIcon(String pkgname) {
		if (pkgname == null || pkgname.length() <= 0 || FirstScreenInfo.hidePkgs.size() <= 0)
			return false;
		for (String pkg : FirstScreenInfo.hidePkgs) {
			if (pkg.equals(pkgname))
				return true;
		}
		return false;
	}

    /**  init screen info for all app */
    public void initScreenInfoToDatabase(List<Integer> screenIndexNormal){
    	Log.e("super_hw", "Launcher initScreenInfoToDatabase()");
    	final int screenCount = screenIndexNormal.size();
    	final ContentResolver cr = getContentResolver();
    	final Uri uri = LauncherSettings.WorkspaceScreens.CONTENT_URI;
    	/**  TODO 这个地方需要研究一下 同步与异步之间的差异 */
    	/*Runnable r = new Runnable() {
            @Override
            public void run() {
                ArrayList<ContentProviderOperation> ops = new ArrayList<ContentProviderOperation>();
                // Clear the table
                ops.add(ContentProviderOperation.newDelete(uri).build());
                int count = screenCount;
                for (int i = 0; i < count; i++) {
                    ContentValues v = new ContentValues();
                    long screenId = screenIndexNormal.get(i);
                    v.put(LauncherSettings.WorkspaceScreens._ID, screenId);
                    v.put(LauncherSettings.WorkspaceScreens.SCREEN_RANK, i + 1);
                    ops.add(ContentProviderOperation.newInsert(uri).withValues(v).build());
                }

                try {
                    cr.applyBatch(LauncherProvider.AUTHORITY, ops);
                } catch (Exception ex) {
                    throw new RuntimeException(ex);
                }

            }
        };
        mModel.runOnWorkerThreadByInit(r);*/
    	ArrayList<ContentProviderOperation> ops = new ArrayList<ContentProviderOperation>();
        ops.add(ContentProviderOperation.newDelete(uri).build());
        for (int i = 0; i < screenCount; i++) {
            ContentValues v = new ContentValues();
            long screenId = screenIndexNormal.get(i);
            v.put(LauncherSettings.WorkspaceScreens._ID, screenId);
            v.put(LauncherSettings.WorkspaceScreens.SCREEN_RANK, i + 1);
            ops.add(ContentProviderOperation.newInsert(uri).withValues(v).build());
        }

        try {
            cr.applyBatch(LauncherProvider.AUTHORITY, ops);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    public void bindAppsAdded(final ArrayList<Long> newScreens,
                              final ArrayList<ItemInfo> addNotAnimated,
                              final ArrayList<ItemInfo> addAnimated,
                              final ArrayList<AppInfo> addedApps) {
    	Log.w("super_hw", "bindAppsAdded");
//        Runnable r = new Runnable() {
//            public void run() {
//                bindAppsAdded(newScreens, addNotAnimated, addAnimated, addedApps);
//            }
//        };
        // TODO by super_hw 应用安装成功在onPause情况下也要能在桌面添加图标
//        if (waitUntilResume(r)) {
//            return;
//        }

        // Add the new screens
        if (newScreens != null) {
            bindAddScreens(newScreens);
        }

        // We add the items without animation on non-visible pages, and with
        // animations on the new page (which we will try and snap to).
        if (addNotAnimated != null && !addNotAnimated.isEmpty()) {
            bindItems(addNotAnimated, 0,
                    addNotAnimated.size(), false);
        }
        if (addAnimated != null && !addAnimated.isEmpty()) {
            bindItems(addAnimated, 0,
                    addAnimated.size(), true);
        }
        Log.w("super_hw", "尝试在这里修改获取本地状态?");
        if(addedApps != null){
        	for(AppInfo appInfo : addedApps){
        		try {
        			if (LauncherApplication.isFactoryInstallModel
        					&& FirstScreenInfo.shortcutItems.contains(appInfo.componentName.getPackageName())) {
						if (!isBottomShortcutOccupied(appInfo)) {
        					// TODO remove pkgname
							// TODO add bottom shortcut
							ShortcutInfo sInfo = new ShortcutInfo(appInfo);
							int index = FirstScreenInfo.shortcutItems.indexOf(appInfo.componentName.getPackageName());
							FirstLoadValues mValue = new  FirstLoadValues(sInfo, -101, index, index, 0);
							if(mValue != null) {
								View view = createShortcut(R.layout.application, (CellLayout)mWorkspace.getPageAt(mValue.screen), sInfo);
								mWorkspace.addInScreen(view, mValue.container, mValue.screen, mValue.cellX, mValue.cellY, 1, 1, false);
								LauncherModel.addOrMoveItemInDatabase(this, mValue.sInfo, mValue.container, mValue.screen, mValue.cellX, mValue.cellY);
								continue;
							}
        				}
        			}
				} catch (Exception e) {
					e.printStackTrace();
				}
        		if(isDesktopContains(appInfo)){
        			Log.w("super_hw", "桌面包含该应用图标,先不做任何处理.");
        		} else {
        			Log.w("super_hw", "桌面不包含该应用图标,准备找个页面添加.");
        			try {
        				int lastIndex = mWorkspace.getPageCount();
            			int lastLineX = 0,lastLineY = 0;
            			if(lastIndex <= 0) break;
            			CellLayout lastPage = (CellLayout)mWorkspace.getChildAt(lastIndex - 1);
            			int countX = lastPage.getCountX(), countY = lastPage.getCountY();
            			int [] lineRecord = new int[countY];
            			Log.w("super_hw", "countX:" + countX+ " countY:"+countY);
            			// TODO Launcher2 只有1个Child,Launcher3有2个Child
            			if(lastPage.getChildCount() != 2) break;
            			ViewGroup vg = (ViewGroup)lastPage.getChildAt(1);
            			// TODO 判断最后一页为空的情况
    					if (vg.getChildCount() == 0) {
    						int tempIndex = lastIndex;
    						while(mWorkspace.getChildAt(tempIndex-- -1) != null){
    							CellLayout lastPageTemp = (CellLayout)mWorkspace.getChildAt(tempIndex -1);
    							ViewGroup vgTemp = (ViewGroup)lastPageTemp.getChildAt(1);
    							if(vgTemp == null || vgTemp.getChildCount() <= 0) continue;
    							int [] lineRecordTemp = getScrennRect(vgTemp, countX, countY);
    							if(lineRecordTemp[countY-1] >= countX-1){ // TODO 这一页是满的
//    								Log.e("super_hw", "这一页是满的");
    								break;
    							} else {
    								lastPage = lastPageTemp;
    								lastIndex = tempIndex;
    								vg = (ViewGroup)lastPage.getChildAt(1);
//    								Log.e("super_hw", "这一页没满");
    								break;
    							}
    						}
    					}
            			for(int cIndex=0;cIndex < vg.getChildCount();cIndex++){
            				View child = vg.getChildAt(cIndex);
            				Object cTag = child.getTag();// TODO ShortcutInfo FolderInfo LauncherAppWidgetInfo
            				int cellX = 0,cellY = 0,spanX = 1,spanY = 1;
            				cellX = ((ItemInfo)cTag).cellX;
            				cellY = ((ItemInfo) cTag).cellY;
            				spanX = ((ItemInfo) cTag).spanX;
            				spanY = ((ItemInfo) cTag).spanY;
            				Log.i("super_hw", "cellX:" + cellX+ " cellY:"+cellY + " spanX:"+spanX+" spanY:"+spanY);
            				if (cellX + spanX - 1 > lineRecord[cellY + spanY - 1]) {
            					lineRecord[cellY + spanY - 1] = cellX + spanX - 1;
            				}
            				if (cellY + spanY - 1 > lastLineY) {
            					lastLineY = cellY + spanY - 1;
            				}
            			}
            			lastLineX = lineRecord[lastLineY];
            			Log.e("super_hw", "bindAppsAdded lineRecord : " + lineRecord[0] + " " + lineRecord[1]);
            			if (lastLineY == countY - 1){// TODO 最后一行的情况
            				if(lastLineX == countX - 1) {// TODO 最后一列的情况
            					lastLineX = 0;
            					lastLineY = 0;
            				} else {
            					lastLineX++;
            					lastIndex--;
            				}
            			} else {
            				if(lastLineX == countX - 1) {// TODO 最后一列的情况
            					lastLineX = 0;
            					lastLineY++;
            				} else {
            					if(vg.getChildCount() > 0)
            						lastLineX++;
            				}
            				lastIndex--;
            			}
            			
            			appInfo.cellX = lastLineX;
            			appInfo.cellY = lastLineY;
            			appInfo.container = -100;
            			// TODO 防止在安装过程中拖动,导致新增一页,使得最后一个screenId是-201,抛出异常
            			if(lastIndex + 1 > mWorkspace.getPageCount() || mWorkspace.getScreenIdForPageIndex(lastIndex) == -201){
            				mWorkspace.addEmptyScreenBySomeWay(appInfo,this,lastIndex);
            			} else {
            				long screenId = mWorkspace.getScreenIdForPageIndex(lastIndex);
            				appInfo.screenId = screenId;
            				Log.e("super_hw", "lastIndex : " + lastIndex + " screenId : " + screenId + " pageCount : " + mWorkspace.getPageCount());
            				ShortcutInfo sInfo = new ShortcutInfo(appInfo);
            				View view = createShortcut(R.layout.application, (CellLayout)mWorkspace.getPageAt(lastIndex), sInfo);
//            		view.setTag(new ShortcutInfo(appInfo));
            				mWorkspace.addInScreen(view, -100, screenId, lastLineX, lastLineY, 1, 1, false);
            				
            				Log.e("super_hw", "新增一个APP page : " + lastIndex + " screenId : " + screenId + " X : " + lastLineX + " Y : " + lastLineY);
            				LauncherModel.addItemInDatabase(this, sInfo, -100, screenId, lastLineX, lastLineY);
            			}
					} catch (Exception e) {
						e.printStackTrace();
					}
        		}
        	}
        }

        // Remove the extra empty screen
        mWorkspace.removeExtraEmptyScreen(false, false);

        if (!LauncherAppState.isDisableAllApps() &&
                addedApps != null && mAppsCustomizeContent != null) {
            mAppsCustomizeContent.addApps(addedApps);
        }
    }
    
    /**
     * 对应的底部快捷栏位置是否被占用
     * */
    private boolean isBottomShortcutOccupied(AppInfo appInfo) {
    	try {
			if(mHotseat == null)
				return false;
			ViewGroup mHotseatVg = (ViewGroup) ((ViewGroup) mHotseat.getChildAt(0)).getChildAt(1);
			for (int index = 0; index < mHotseatVg.getChildCount(); index++) {
				Log.e("super_super", "" + mHotseatVg.getChildAt(index).toString());
				if(mHotseatVg.getChildAt(index) instanceof BubbleTextView){
					BubbleTextView btv = (BubbleTextView)mHotseatVg.getChildAt(index);
					try {
						ShortcutInfo sInfo = (ShortcutInfo)btv.getTag();
//						if(sInfo.appInfo != null && sInfo.appInfo.componentName.getClassName().equals(appInfo.componentName.getClassName()))
//							return true;
						if(sInfo.screenId == FirstScreenInfo.shortcutItems.indexOf(appInfo.componentName.getPackageName()))
							return true;
					} catch (Exception e) {
						e.printStackTrace();
					}
				} else if(mHotseatVg.getChildAt(index) instanceof FolderIcon){
					FolderIcon fc = (FolderIcon)mHotseatVg.getChildAt(index);
					FolderInfo fInfo = (FolderInfo)fc.getTag();
					if(fInfo.screenId == FirstScreenInfo.shortcutItems.indexOf(appInfo.componentName.getPackageName()))
						return true;
//					for(ShortcutInfo fsInfo : fInfo.contents){
//						if(fsInfo.appInfo != null && fsInfo.appInfo.componentName.getClassName().equals(appInfo.componentName.getClassName()))
//							return true;
//					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
    	return false;
    }

    private int [] getScrennRect(ViewGroup vg, int countX, int countY){
    	int [] lineRecord = new int[countY];
    	int lastLineY = 0;
    	for(int index=0; index < vg.getChildCount(); index++){
			View child = vg.getChildAt(index);
			Object cTag = child.getTag();// TODO ShortcutInfo FolderInfo LauncherAppWidgetInfo
			int cellX = 0,cellY = 0,spanX = 1,spanY = 1;
			cellX = ((ItemInfo)cTag).cellX;
			cellY = ((ItemInfo) cTag).cellY;
			spanX = ((ItemInfo) cTag).spanX;
			spanY = ((ItemInfo) cTag).spanY;
			Log.i("super_hw", "cellX:" + cellX+ " cellY:"+cellY + " spanX:"+spanX+" spanY:"+spanY);
			if (cellX + spanX - 1 > lineRecord[cellY + spanY - 1]) {
				lineRecord[cellY + spanY - 1] = cellX + spanX - 1;
			}
			if (cellY + spanY - 1 > lastLineY) {
				lastLineY = cellY + spanY - 1;
			}
		}
    	return lineRecord;
    }
    
	private boolean isDesktopContains(AppInfo appInfo) {
		if (appInfo == null)
			return false;
		String pkgname = appInfo.componentName.getPackageName();
		ArrayList<ItemInfo> items = LauncherAppState.getInstance().mModel.getSBgWorkspaceItems();
		HashMap<Long, FolderInfo> folderItems = LauncherAppState.getInstance().mModel.getSBgFolders();
		if (items == null || items.size() <= 0)
			return false;
//		Log.d("super_hw", "isDesktopContains pkgname : " + pkgname + " items.size : " + items.size());
		try {
			for (ItemInfo item : items) {
				if(item instanceof ShortcutInfo){
					if(((ShortcutInfo)item).appInfo != null){
						if(((ShortcutInfo)item).appInfo.componentName.getPackageName().equals(pkgname))
							return true;
					} else {
						// TODO 拖拽一个widget添加到桌面后,appInfo为空,这里需要处理
//						Log.w("super_hw", "Launcher_isDesktopContains() shortcutInfo.info is null, ShortcutInfo:" + item.toString());
					}
				}
			}
			Set<Long> keys = folderItems.keySet();
			for(Long key : keys){
				FolderInfo folderInfo = folderItems.get(key);
				for(ShortcutInfo sInfo : folderInfo.contents){
					if(sInfo.appInfo != null){
						if(sInfo.appInfo.componentName.getPackageName().equals(pkgname))
							return true;
					} else {
						// TODO 拖拽一个widget添加到桌面的文件夹,appInfo为空,这里需要处理
//						Log.w("super_hw", "Launcher_isDesktopContains() folder.shortcutInfo.info is null, ShortcutInfo:" + sInfo.toString());
					}
 				}
 			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}
	private boolean isFolderContains(AppInfo appInfo){
		if (appInfo == null)
			return false;
		String pkgname = appInfo.componentName.getPackageName();
		HashMap<Long, FolderInfo> folderItems = LauncherAppState.getInstance().mModel.getSBgFolders();
		try {
			Set<Long> keys = folderItems.keySet();
			for(Long key : keys){
				FolderInfo folderInfo = folderItems.get(key);
				for(ShortcutInfo sInfo : folderInfo.contents){
					if(sInfo.appInfo.componentName.getPackageName().equals(pkgname))
						return true;
 				}
 			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}
	
	private boolean isShortcutHasAdded(String className){
		if (className == null)
			return false;
		ArrayList<ItemInfo> items = LauncherAppState.getInstance().mModel.getSBgWorkspaceItems();
		HashMap<Long, FolderInfo> folderItems = LauncherAppState.getInstance().mModel.getSBgFolders();
		if (items == null || items.size() <= 0) {
			return false;
		}
		try {
			for (ItemInfo item : items) {
				if(item instanceof ShortcutInfo){
					// TODO 只有ShortcutInfo为快捷键的appInfo为空
					if(((ShortcutInfo)item).appInfo == null){
						if(((ShortcutInfo)item).intent != null){
							try {
								ComponentName cpName =((ShortcutInfo)item).intent.getComponent();
								if(cpName != null && className.equals(cpName.getClassName()))
									return true;
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
					}
				}
			}
			Set<Long> keys = folderItems.keySet();
			for(Long key : keys){
				FolderInfo folderInfo = folderItems.get(key);
				for(ShortcutInfo sInfo : folderInfo.contents){
					if(sInfo.appInfo == null){
						if(((ShortcutInfo)sInfo).intent != null){
							try {
								ComponentName cpName =((ShortcutInfo)sInfo).intent.getComponent();
								if(cpName != null && className.equals(cpName.getClassName()))
									return true;
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
					}
 				}
 			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

    /**
     * Bind the items start-end from the list.
     *
     * Implementation of the method from LauncherModel.Callbacks.
     */
    public void bindItems(final ArrayList<ItemInfo> shortcuts, final int start, final int end,
                          final boolean forceAnimateIcons) {
    	Log.w("super_hw", "Launcher bindItems() 加载所有桌面图标的入口");
        Runnable r = new Runnable() {
            public void run() {
                bindItems(shortcuts, start, end, forceAnimateIcons);
            }
        };
        if (waitUntilResume(r)) {
            return;
        }

        // Get the list of added shortcuts and intersect them with the set of shortcuts here
        final AnimatorSet anim = LauncherAnimUtils.createAnimatorSet();
        final Collection<Animator> bounceAnims = new ArrayList<Animator>();
        final boolean animateIcons = forceAnimateIcons && canRunNewAppsAnimation();
        Workspace workspace = mWorkspace;
        long newShortcutsScreenId = -1;
        for (int i = start; i < end; i++) {
            final ItemInfo item = shortcuts.get(i);
            Log.i("super_hw", "Launcher bindItems(), add icon to screen. item.title : " + item.title);

            // Short circuit if we are loading dock items for a configuration which has no dock
            if (item.container == LauncherSettings.Favorites.CONTAINER_HOTSEAT &&
                    mHotseat == null) {
                continue;
            }

            switch (item.itemType) {
                case LauncherSettings.Favorites.ITEM_TYPE_APPLICATION:
                case LauncherSettings.Favorites.ITEM_TYPE_SHORTCUT:
                    ShortcutInfo info = (ShortcutInfo) item;
                    View shortcut = createShortcut(info);
                    /*
                     * TODO: FIX collision case
                     */
                    if (item.container == LauncherSettings.Favorites.CONTAINER_DESKTOP) {
                        CellLayout cl = mWorkspace.getScreenWithId(item.screenId);
                        if (cl != null && cl.isOccupied(item.cellX, item.cellY)) {
                            View v = cl.getChildAt(item.cellX, item.cellY);
                            Object tag = v.getTag();
                            String desc = "Collision while binding workspace item: " + item
                                    + ". Collides with " + tag;
                            if (LauncherAppState.isDogfoodBuild()) {
                                throw (new RuntimeException(desc));
                            } else {
                                Log.d(TAG, desc);
                            }
                        }
                    }

                    /**  add icon to screen, diff to 2 */
                    if (item.container == -100 && item.screenId > 0 && workspace.getScreenWithId(item.screenId) == null) {
                    	if(!workspace.addEmptyScreenByScreenId(item.screenId))
                    		continue;
                    }
                    workspace.addInScreenFromBind(shortcut, item.container, item.screenId, item.cellX,
                            item.cellY, 1, 1);
                    if (animateIcons) {
                        // Animate all the applications up now
                        shortcut.setAlpha(0f);
                        shortcut.setScaleX(0f);
                        shortcut.setScaleY(0f);
                        bounceAnims.add(createNewAppBounceAnimation(shortcut, i));
                        newShortcutsScreenId = item.screenId;
                    }
                    break;
                case LauncherSettings.Favorites.ITEM_TYPE_FOLDER:
                    FolderIcon newFolder = FolderIcon.fromXml(R.layout.folder_icon, this,
                            (ViewGroup) workspace.getChildAt(workspace.getCurrentPage()),
                            (FolderInfo) item, mIconCache);
                    /**  add icon to screen, diff to 2 */
                    workspace.addInScreenFromBind(newFolder, item.container, item.screenId, item.cellX,
                            item.cellY, 1, 1);
                    break;
                default:
                    throw new RuntimeException("Invalid Item Type");
            }
        }

        if (animateIcons) {
            // Animate to the correct page
            if (newShortcutsScreenId > -1) {
                long currentScreenId = mWorkspace.getScreenIdForPageIndex(mWorkspace.getNextPage());
                final int newScreenIndex = mWorkspace.getPageIndexForScreenId(newShortcutsScreenId);
                final Runnable startBounceAnimRunnable = new Runnable() {
                    public void run() {
                        anim.playTogether(bounceAnims);
                        anim.start();
                    }
                };
                if (newShortcutsScreenId != currentScreenId) {
                    // We post the animation slightly delayed to prevent slowdowns
                    // when we are loading right after we return to launcher.
                    mWorkspace.postDelayed(new Runnable() {
                        public void run() {
                            if (mWorkspace != null) {
                                mWorkspace.snapToPage(newScreenIndex);
                                mWorkspace.postDelayed(startBounceAnimRunnable,
                                        NEW_APPS_ANIMATION_DELAY);
                            }
                        }
                    }, NEW_APPS_PAGE_MOVE_DELAY);
                } else {
                    mWorkspace.postDelayed(startBounceAnimRunnable, NEW_APPS_ANIMATION_DELAY);
                }
            }
        }
        workspace.requestLayout();
    }

    /**
     * Implementation of the method from LauncherModel.Callbacks.
     */
    public void bindFolders(final HashMap<Long, FolderInfo> folders) {
        Runnable r = new Runnable() {
            public void run() {
                bindFolders(folders);
            }
        };
        if (waitUntilResume(r)) {
            return;
        }
        sFolders.clear();
        sFolders.putAll(folders);
    }

    /**
     * Add the views for a widget to the workspace.
     *
     * Implementation of the method from LauncherModel.Callbacks.
     */
    public void bindAppWidget(final LauncherAppWidgetInfo item) {
    	Log.i("super_hw", "Launcher_bindAppWidget()");
        Runnable r = new Runnable() {
            public void run() {
                bindAppWidget(item);
            }
        };
        if (waitUntilResume(r)) {
            return;
        }

        final long start = DEBUG_WIDGETS ? SystemClock.uptimeMillis() : 0;
        if (DEBUG_WIDGETS) {
            Log.d(TAG, "bindAppWidget: " + item);
        }
        final Workspace workspace = mWorkspace;

        AppWidgetProviderInfo appWidgetInfo;
        if (!mIsSafeModeEnabled
                && ((item.restoreStatus & LauncherAppWidgetInfo.FLAG_PROVIDER_NOT_READY) == 0)
                && ((item.restoreStatus & LauncherAppWidgetInfo.FLAG_ID_NOT_VALID) != 0)) {

            appWidgetInfo = mModel.findAppWidgetProviderInfoWithComponent(this, item.providerName);
            if (appWidgetInfo == null) {
                if (DEBUG_WIDGETS) {
                    Log.d(TAG, "Removing restored widget: id=" + item.appWidgetId
                            + " belongs to component " + item.providerName
                            + ", as the povider is null");
                }
                LauncherModel.deleteItemFromDatabase(this, item);
                return;
            }
            // Note: This assumes that the id remap broadcast is received before this step.
            // If that is not the case, the id remap will be ignored and user may see the
            // click to setup view.
            PendingAddWidgetInfo pendingInfo = new PendingAddWidgetInfo(appWidgetInfo, null, null);
            pendingInfo.spanX = item.spanX;
            pendingInfo.spanY = item.spanY;
            pendingInfo.minSpanX = item.minSpanX;
            pendingInfo.minSpanY = item.minSpanY;
            Bundle options =
                    AppsCustomizePagedView.getDefaultOptionsForWidget(this, pendingInfo);

            int newWidgetId = mAppWidgetHost.allocateAppWidgetId();
            boolean success = mAppWidgetManager.bindAppWidgetIdIfAllowed(
                    newWidgetId, appWidgetInfo, options);

            // TODO consider showing a permission dialog when the widget is clicked.
            if (!success) {
                mAppWidgetHost.deleteAppWidgetId(newWidgetId);
                if (DEBUG_WIDGETS) {
                    Log.d(TAG, "Removing restored widget: id=" + item.appWidgetId
                            + " belongs to component " + item.providerName
                            + ", as the launcher is unable to bing a new widget id");
                }
                LauncherModel.deleteItemFromDatabase(this, item);
                return;
            }

            item.appWidgetId = newWidgetId;

            // If the widget has a configure activity, it is still needs to set it up, otherwise
            // the widget is ready to go.
            item.restoreStatus = (appWidgetInfo.configure == null)
                    ? LauncherAppWidgetInfo.RESTORE_COMPLETED
                    : LauncherAppWidgetInfo.FLAG_UI_NOT_READY;

            LauncherModel.updateItemInDatabase(this, item);
        }

        if (!mIsSafeModeEnabled && item.restoreStatus == LauncherAppWidgetInfo.RESTORE_COMPLETED) {
            final int appWidgetId = item.appWidgetId;
            appWidgetInfo = mAppWidgetManager.getAppWidgetInfo(appWidgetId);
            if (DEBUG_WIDGETS) {
                Log.d(TAG, "bindAppWidget: id=" + item.appWidgetId + " belongs to component " + appWidgetInfo.provider);
            }

            item.hostView = mAppWidgetHost.createView(this, appWidgetId, appWidgetInfo);
            item.hostView.setOnFocusChangeListener(mFocusHandler);
        } else {
            appWidgetInfo = null;
            PendingAppWidgetHostView view = new PendingAppWidgetHostView(this, item,
                    mIsSafeModeEnabled);
            view.updateIcon(mIconCache);
            item.hostView = view;
            item.hostView.updateAppWidget(null);
            item.hostView.setOnClickListener(this);
            item.hostView.setOnFocusChangeListener(mFocusHandler);
        }

        item.hostView.setTag(item);
        item.onBindAppWidget(this);

        workspace.addInScreen(item.hostView, item.container, item.screenId, item.cellX,
                item.cellY, item.spanX, item.spanY, false);
        addWidgetToAutoAdvanceIfNeeded(item.hostView, appWidgetInfo);

        workspace.requestLayout();

        if (DEBUG_WIDGETS) {
            Log.d(TAG, "bound widget id="+item.appWidgetId+" in "
                    + (SystemClock.uptimeMillis()-start) + "ms");
        }
    }

    /**
     * Restores a pending widget.
     *
     * @param appWidgetId The app widget id
     * @param cellInfo The position on screen where to create the widget.
     */
    private void completeRestoreAppWidget(final int appWidgetId) {
        LauncherAppWidgetHostView view = mWorkspace.getWidgetForAppWidgetId(appWidgetId);
        if ((view == null) || !(view instanceof PendingAppWidgetHostView)) {
            Log.e(TAG, "Widget update called, when the widget no longer exists.");
            return;
        }

        LauncherAppWidgetInfo info = (LauncherAppWidgetInfo) view.getTag();
        info.restoreStatus = LauncherAppWidgetInfo.RESTORE_COMPLETED;

        mWorkspace.reinflateWidgetsIfNecessary();
        LauncherModel.updateItemInDatabase(this, info);
    }

    public void onPageBoundSynchronously(int page) {
        mSynchronouslyBoundPages.add(page);
    }

    /**
     * Callback saying that there aren't any more items to bind.
     *
     * Implementation of the method from LauncherModel.Callbacks.
     */
    public void finishBindingItems(final boolean upgradePath) {
        Runnable r = new Runnable() {
            public void run() {
                finishBindingItems(upgradePath);
            }
        };
        if (waitUntilResume(r)) {
            return;
        }
        if (mSavedState != null) {
            if (!mWorkspace.hasFocus()) {
                mWorkspace.getChildAt(mWorkspace.getCurrentPage()).requestFocus();
            }
            mSavedState = null;
        }

        mWorkspace.restoreInstanceStateForRemainingPages();

        setWorkspaceLoading(false);
        sendLoadingCompleteBroadcastIfNecessary();

        // If we received the result of any pending adds while the loader was running (e.g. the
        // widget configuration forced an orientation change), process them now.
        if (sPendingAddItem != null) {
            final long screenId = completeAdd(sPendingAddItem);

            // TODO: this moves the user to the page where the pending item was added. Ideally,
            // the screen would be guaranteed to exist after bind, and the page would be set through
            // the workspace restore process.
            mWorkspace.post(new Runnable() {
                @Override
                public void run() {
                    mWorkspace.snapToScreenId(screenId);
                }
            });
            sPendingAddItem = null;
        }

        if (upgradePath) {
            mWorkspace.getUniqueComponents(true, null);
            mIntentsOnWorkspaceFromUpgradePath = mWorkspace.getUniqueComponents(true, null);
        }
        PackageInstallerCompat.getInstance(this).onFinishBind();

        if (mLauncherCallbacks != null) {
            mLauncherCallbacks.finishBindingItems(upgradePath);
        }
    }

    private void sendLoadingCompleteBroadcastIfNecessary() {
        if (!mSharedPrefs.getBoolean(FIRST_LOAD_COMPLETE, false)) {
            String permission =
                    getResources().getString(R.string.receive_first_load_broadcast_permission);
            Intent intent = new Intent(ACTION_FIRST_LOAD_COMPLETE);
            sendBroadcast(intent, permission);
            SharedPreferences.Editor editor = mSharedPrefs.edit();
            editor.putBoolean(FIRST_LOAD_COMPLETE, true);
            editor.apply();
        }
    }

    public boolean isAllAppsButtonRank(int rank) {
        if (mHotseat != null) {
            return mHotseat.isAllAppsButtonRank(rank);
        }
        return false;
    }

    private boolean canRunNewAppsAnimation() {
        long diff = System.currentTimeMillis() - mDragController.getLastGestureUpTime();
        return diff > (NEW_APPS_ANIMATION_INACTIVE_TIMEOUT_SECONDS * 1000);
    }

    private ValueAnimator createNewAppBounceAnimation(View v, int i) {
        ValueAnimator bounceAnim = LauncherAnimUtils.ofPropertyValuesHolder(v,
                PropertyValuesHolder.ofFloat("alpha", 1f),
                PropertyValuesHolder.ofFloat("scaleX", 1f),
                PropertyValuesHolder.ofFloat("scaleY", 1f));
        bounceAnim.setDuration(InstallShortcutReceiver.NEW_SHORTCUT_BOUNCE_DURATION);
        bounceAnim.setStartDelay(i * InstallShortcutReceiver.NEW_SHORTCUT_STAGGER_DELAY);
        bounceAnim.setInterpolator(new SmoothPagedView.OvershootInterpolator());
        return bounceAnim;
    }

    public boolean useVerticalBarLayout() {
        return LauncherAppState.getInstance().getDynamicGrid().
                getDeviceProfile().isVerticalBarLayout();
    }

    protected Rect getSearchBarBounds() {
        return LauncherAppState.getInstance().getDynamicGrid().
                getDeviceProfile().getSearchBarBounds();
    }

    public void bindSearchablesChanged() {
        if (mSearchDropTargetBar == null) {
            return;
        }
        if (mQsb != null) {
            mSearchDropTargetBar.removeView(mQsb);
            mQsb = null;
        }
        mSearchDropTargetBar.setQsbSearchBar(getQsbBar());
    }

    /**
     * Add the icons for all apps.
     *
     * Implementation of the method from LauncherModel.Callbacks.
     */
    public void bindAllApplications(final ArrayList<AppInfo> apps) {
    	Log.w("super_hw", "Launcher bindAllApplications(), apps size : " + apps.size());
        if (LauncherAppState.isDisableAllApps()) {
            if (mIntentsOnWorkspaceFromUpgradePath != null) {
                if (LauncherModel.UPGRADE_USE_MORE_APPS_FOLDER) {
                    getHotseat().addAllAppsFolder(mIconCache, apps,
                            mIntentsOnWorkspaceFromUpgradePath, Launcher.this, mWorkspace);
                }
                mIntentsOnWorkspaceFromUpgradePath = null;
            }
            if (mAppsCustomizeContent != null) {
                mAppsCustomizeContent.onPackagesUpdated(
                        LauncherModel.getSortedWidgetsAndShortcuts(this));
            }
        } else {
            if (mAppsCustomizeContent != null) {
                mAppsCustomizeContent.setApps(apps);
                mAppsCustomizeContent.onPackagesUpdated(
                        LauncherModel.getSortedWidgetsAndShortcuts(this));
            }
        }
        if (mLauncherCallbacks != null) {
            mLauncherCallbacks.bindAllApplications(apps);
        }
        // TODO 启动后延迟检测并删除空白页
        new CountDownTimer(300,1000) {
			public void onTick(long millisUntilFinished) {
			}
			public void onFinish() {
				if(mWorkspace != null) {
					int emptyPageCount = 0;
					for(int index = 0;index < mWorkspace.getPageCount();index++){
						try {
							CellLayout group = mWorkspace.getScreenWithId(mWorkspace.getScreenIdForPageIndex(index));
							ViewGroup vg = (ViewGroup)group.getChildAt(1);
							if(vg.getChildCount() <= 0)
								emptyPageCount++;
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
					int times = 0;
					while(emptyPageCount-- > 0){
						// TODO super_hw : 这里需要remove多次,以便清空没一个空白页(目前这里一次只能清除一个空白页)
						mWorkspace.removeExtraEmptyScreenDelayed(false, null, times++ * 300, false);
					}
					// TODO 延时(times * 300) + 1000 来处理遍历应用图标事件
					// 用来解决安装应用过程中,横竖屏切换导致添加失败的问题,以及其他导致图标隐藏的问题
					Log.i("super_hw", "开始检查所有应用的桌面图标加载情况");
					if(myHandler != null) {
						myHandler.removeMessages(CHECK_APPS_SHOW);
						myHandler.sendEmptyMessageDelayed(CHECK_APPS_SHOW, times * 300 + 3000);// 15分钟检查一次
					}
				}
			}
		}.start();
    }

    /**
     * 用来检查所有应用的图标的加载情况,添加所有未加载的图标
     * */
    private void checkAllAppsIcon() {
    	if(LauncherAppState.getInstance().mModel != null){
    		if(mWorkspace != null){
    			if(mWorkspace.isDragOccuring()){
    				if(myHandler != null) {
    		    		myHandler.removeMessages(CHECK_APPS_SHOW);
    		    		myHandler.sendEmptyMessageDelayed(CHECK_APPS_SHOW, 3 * 1000);// 如果处于拖动状态3s检查一次
    		    	}
    				return;
    			}
    		}
    		List<ResolveInfo> apps = null;
    		ArrayList<AppInfo> appsOrder = new ArrayList<AppInfo>();
        	List<AppInfo> addedApps = new ArrayList<AppInfo>();
        	HashMap<String, AppInfo> temp = new HashMap<String, AppInfo>();
        	Intent mainIntent = new Intent(Intent.ACTION_MAIN, null).addCategory(Intent.CATEGORY_LAUNCHER);
        	PackageManager pm = getPackageManager();
        	apps = pm.queryIntentActivities(mainIntent, 0);
        	UserHandleCompat myUser = UserHandleCompat.myUserHandle();
        	LauncherAppsCompat launcherApps = LauncherAppsCompat.getInstance(this);
        	String packageName = "";
        	List<LauncherActivityInfoCompat> matches = new ArrayList<LauncherActivityInfoCompat>();
        	for(ResolveInfo info : apps){
        		packageName = info.activityInfo.applicationInfo.packageName;
        		// TODO 当不可用时,后面的在LauncherModel_loadWorkspace()里面会将不可用的删除掉,所以从入口控制
        		if (!info.activityInfo.enabled) continue;
        		matches.clear();
        		matches = launcherApps.getActivityList(packageName, myUser);
        		for (LauncherActivityInfoCompat laInfo : matches) {
        			AppInfo appInfo = new AppInfo(this, laInfo, myUser,new IconCache((LauncherApplication)this.getApplication()), null);
        			temp.put(appInfo.componentName.getPackageName(), appInfo);
                }
        	}
        	Set<String> keys = temp.keySet();
        	for(String key : keys){
        		appsOrder.add(temp.get(key));
        	}
        	for(AppInfo appInfo : appsOrder){
        		if(isHideIcon(appInfo.componentName.getPackageName())){
        			addedApps.add(appInfo);
        		} else if(isDesktopContains(appInfo)){
        			addedApps.add(appInfo);
        		}
        	}
        	for(AppInfo appInfo : addedApps){
        		appsOrder.remove(appInfo);
        	}
        	if(appsOrder != null && appsOrder.size() > 0){
//        		bindAppsAdded(null, null, null, appsOrder);
        		if(myHandler != null) {
        			myHandler.removeMessages(ADD_ICON_IN_UITHREAD);
        			Message msg = Message.obtain();
        			msg.what = ADD_ICON_IN_UITHREAD;
        			msg.obj = appsOrder;
        			myHandler.sendMessageDelayed(msg, 1000);
        		}
        	}
    	}
    	// TODO 延时循环遍历
    	if(myHandler != null) {
    		if(mWorkspace != null){
    			mWorkspace.removeExtraEmptyScreen(false, false);
    		}
    		myHandler.removeMessages(CHECK_APPS_SHOW);
    		myHandler.sendEmptyMessageDelayed(CHECK_APPS_SHOW, 15 * 60 * 1000);// 15分钟检查一次
    	}
    }
    
    private void addIconInUIThread(ArrayList<AppInfo> appsOrder){
    	try {
    		bindAppsAdded(null, null, null, appsOrder);
		} catch (Exception e) {
			e.printStackTrace();
		}
    }
    
    /**
     * A package was updated.
     *
     * Implementation of the method from LauncherModel.Callbacks.
     */
    public void bindAppsUpdated(final ArrayList<AppInfo> apps) {
        Runnable r = new Runnable() {
            public void run() {
                bindAppsUpdated(apps);
            }
        };
        if (waitUntilResume(r)) {
            return;
        }

        if (!LauncherAppState.isDisableAllApps() &&
                mAppsCustomizeContent != null) {
            mAppsCustomizeContent.updateApps(apps);
        }
    }

    @Override
    public void bindWidgetsRestored(final ArrayList<LauncherAppWidgetInfo> widgets) {
        Runnable r = new Runnable() {
            public void run() {
                bindWidgetsRestored(widgets);
            }
        };
        if (waitUntilResume(r)) {
            return;
        }
        mWorkspace.widgetsRestored(widgets);
    }

    /**
     * Some shortcuts were updated in the background.
     *
     * Implementation of the method from LauncherModel.Callbacks.
     */
    @Override
    public void bindShortcutsChanged(final ArrayList<ShortcutInfo> updated,
            final ArrayList<ShortcutInfo> removed, final UserHandleCompat user) {
        Runnable r = new Runnable() {
            public void run() {
                bindShortcutsChanged(updated, removed, user);
            }
        };
        if (waitUntilResume(r)) {
            return;
        }

        if (!updated.isEmpty()) {
            mWorkspace.updateShortcuts(updated);
        }

        if (!removed.isEmpty()) {
            HashSet<ComponentName> removedComponents = new HashSet<ComponentName>();
            for (ShortcutInfo si : removed) {
                removedComponents.add(si.getTargetComponent());
            }
            mWorkspace.removeItemsByComponentName(removedComponents, user);
            // Notify the drag controller
            mDragController.onAppsRemoved(new ArrayList<String>(), removedComponents);
        }
    }

    /**
     * Update the state of a package, typically related to install state.
     *
     * Implementation of the method from LauncherModel.Callbacks.
     */
    @Override
    public void updatePackageState(ArrayList<PackageInstallInfo> installInfo) {
        if (mWorkspace != null) {
            mWorkspace.updatePackageState(installInfo);
        }
    }

    /**
     * Update the label and icon of all the icons in a package
     *
     * Implementation of the method from LauncherModel.Callbacks.
     */
    @Override
    public void updatePackageBadge(String packageName) {
        if (mWorkspace != null) {
            mWorkspace.updatePackageBadge(packageName, UserHandleCompat.myUserHandle());
        }
    }

    /**
     * A package was uninstalled.  We take both the super set of packageNames
     * in addition to specific applications to remove, the reason being that
     * this can be called when a package is updated as well.  In that scenario,
     * we only remove specific components from the workspace, where as
     * package-removal should clear all items by package name.
     *
     * @param reason if non-zero, the icons are not permanently removed, rather marked as disabled.
     * Implementation of the method from LauncherModel.Callbacks.
     */
    @Override
    public void bindComponentsRemoved(final ArrayList<String> packageNames,
            final ArrayList<AppInfo> appInfos, final UserHandleCompat user, final int reason) {
        Runnable r = new Runnable() {
            public void run() {
                bindComponentsRemoved(packageNames, appInfos, user, reason);
            }
        };
//        if (waitUntilResume(r)) {
//            return;
//        }

        if (reason == 0) {
            HashSet<ComponentName> removedComponents = new HashSet<ComponentName>();
            for (AppInfo info : appInfos) {
                removedComponents.add(info.componentName);
            }
            if (!packageNames.isEmpty()) {
                mWorkspace.removeItemsByPackageName(packageNames, user);
            }
            if (!removedComponents.isEmpty()) {
                mWorkspace.removeItemsByComponentName(removedComponents, user);
            }
            // Notify the drag controller
            mDragController.onAppsRemoved(packageNames, removedComponents);

        } else {
            mWorkspace.disableShortcutsByPackageName(packageNames, user, reason);
        }

        // Update AllApps
        if (!LauncherAppState.isDisableAllApps() &&
                mAppsCustomizeContent != null) {
            mAppsCustomizeContent.removeApps(appInfos);
        }
    }

    /**
     * A number of packages were updated.
     */
    private ArrayList<Object> mWidgetsAndShortcuts;
    private Runnable mBindPackagesUpdatedRunnable = new Runnable() {
            public void run() {
                bindPackagesUpdated(mWidgetsAndShortcuts);
                mWidgetsAndShortcuts = null;
            }
        };
    public void bindPackagesUpdated(final ArrayList<Object> widgetsAndShortcuts) {
        if (waitUntilResume(mBindPackagesUpdatedRunnable, true)) {
            mWidgetsAndShortcuts = widgetsAndShortcuts;
            return;
        }

        // Update the widgets pane
        if (mAppsCustomizeContent != null) {
            mAppsCustomizeContent.onPackagesUpdated(widgetsAndShortcuts);
        }
    }

    private int mapConfigurationOriActivityInfoOri(int configOri) {
        final Display d = getWindowManager().getDefaultDisplay();
        int naturalOri = Configuration.ORIENTATION_LANDSCAPE;
        switch (d.getRotation()) {
        case Surface.ROTATION_0:
        case Surface.ROTATION_180:
            // We are currently in the same basic orientation as the natural orientation
            naturalOri = configOri;
            break;
        case Surface.ROTATION_90:
        case Surface.ROTATION_270:
            // We are currently in the other basic orientation to the natural orientation
            naturalOri = (configOri == Configuration.ORIENTATION_LANDSCAPE) ?
                    Configuration.ORIENTATION_PORTRAIT : Configuration.ORIENTATION_LANDSCAPE;
            break;
        }

        int[] oriMap = {
                ActivityInfo.SCREEN_ORIENTATION_PORTRAIT,
                ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE,
                ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT,
                ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE
        };
        // Since the map starts at portrait, we need to offset if this device's natural orientation
        // is landscape.
        int indexOffset = 0;
        if (naturalOri == Configuration.ORIENTATION_LANDSCAPE) {
            indexOffset = 1;
        }
        return oriMap[(d.getRotation() + indexOffset) % 4];
    }

    public void lockScreenOrientation() {
        if (Utilities.isRotationEnabled(this)) {
            setRequestedOrientation(mapConfigurationOriActivityInfoOri(getResources()
                    .getConfiguration().orientation));
        }
    }
    public void unlockScreenOrientation(boolean immediate) {
        if (Utilities.isRotationEnabled(this)) {
            if (immediate) {
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
            } else {
                mHandler.postDelayed(new Runnable() {
                    public void run() {
                        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
                    }
                }, mRestoreScreenOrientationDelay);
            }
        }
    }

    protected boolean isLauncherPreinstalled() {
        if (mLauncherCallbacks != null) {
            return mLauncherCallbacks.isLauncherPreinstalled();
        }
        PackageManager pm = getPackageManager();
        try {
            ApplicationInfo ai = pm.getApplicationInfo(getComponentName().getPackageName(), 0);
            if ((ai.flags & ApplicationInfo.FLAG_SYSTEM) != 0) {
                return true;
            } else {
                return false;
            }
        } catch (NameNotFoundException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * This method indicates whether or not we should suggest default wallpaper dimensions
     * when our wallpaper cropper was not yet used to set a wallpaper.
     */
    protected boolean overrideWallpaperDimensions() {
        if (mLauncherCallbacks != null) {
            return mLauncherCallbacks.overrideWallpaperDimensions();
        }
        return true;
    }

    /**
     * To be overridden by subclasses to indicate that there is an activity to launch
     * before showing the standard launcher experience.
     */
    protected boolean hasFirstRunActivity() {
        if (mLauncherCallbacks != null) {
            return mLauncherCallbacks.hasFirstRunActivity();
        }
        return false;
    }

    /**
     * To be overridden by subclasses to launch any first run activity
     */
    protected Intent getFirstRunActivity() {
        if (mLauncherCallbacks != null) {
            return mLauncherCallbacks.getFirstRunActivity();
        }
        return null;
    }

    private boolean shouldRunFirstRunActivity() {
        return !ActivityManager.isRunningInTestHarness() &&
                !mSharedPrefs.getBoolean(FIRST_RUN_ACTIVITY_DISPLAYED, false);
    }

    protected boolean hasRunFirstRunActivity() {
        return mSharedPrefs.getBoolean(FIRST_RUN_ACTIVITY_DISPLAYED, false);
    }

    public boolean showFirstRunActivity() {
        if (shouldRunFirstRunActivity() &&
                hasFirstRunActivity()) {
            Intent firstRunIntent = getFirstRunActivity();
            if (firstRunIntent != null) {
            	Log.e("jiaodian","showFirstRunActivity");
                startActivity(firstRunIntent);
                markFirstRunActivityShown();
                return true;
            }
        }
        return false;
    }

    private void markFirstRunActivityShown() {
        SharedPreferences.Editor editor = mSharedPrefs.edit();
        editor.putBoolean(FIRST_RUN_ACTIVITY_DISPLAYED, true);
        editor.apply();
    }

    /**
     * To be overridden by subclasses to indicate that there is an in-activity full-screen intro
     * screen that must be displayed and dismissed.
     */
    protected boolean hasDismissableIntroScreen() {
        if (mLauncherCallbacks != null) {
            return mLauncherCallbacks.hasDismissableIntroScreen();
        }
        return false;
    }

    /**
     * Full screen intro screen to be shown and dismissed before the launcher can be used.
     */
    protected View getIntroScreen() {
        if (mLauncherCallbacks != null) {
            return mLauncherCallbacks.getIntroScreen();
        }
        return null;
    }

    /**
     * To be overriden by subclasses to indicate whether the in-activity intro screen has been
     * dismissed. This method is ignored if #hasDismissableIntroScreen returns false.
     */
    private boolean shouldShowIntroScreen() {
        return hasDismissableIntroScreen() &&
                !mSharedPrefs.getBoolean(INTRO_SCREEN_DISMISSED, false);
    }

    protected void showIntroScreen() {
        View introScreen = getIntroScreen();
        changeWallpaperVisiblity(false);
        if (introScreen != null) {
            mDragLayer.showOverlayView(introScreen);
        }
        if (mLauncherOverlayContainer != null) {
            mLauncherOverlayContainer.setVisibility(View.INVISIBLE);
        }
    }

    public void dismissIntroScreen() {
        markIntroScreenDismissed();
        if (showFirstRunActivity()) {
            // We delay hiding the intro view until the first run activity is showing. This
            // avoids a blip.
            mWorkspace.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mDragLayer.dismissOverlayView();
                    if (mLauncherOverlayContainer != null) {
                        mLauncherOverlayContainer.setVisibility(View.VISIBLE);
                    }
                    showFirstRunClings();
                }
            }, ACTIVITY_START_DELAY);
        } else {
            mDragLayer.dismissOverlayView();
            if (mLauncherOverlayContainer != null) {
                mLauncherOverlayContainer.setVisibility(View.VISIBLE);
            }
            showFirstRunClings();
        }
        changeWallpaperVisiblity(true);
    }

    private void markIntroScreenDismissed() {
        SharedPreferences.Editor editor = mSharedPrefs.edit();
        editor.putBoolean(INTRO_SCREEN_DISMISSED, true);
        editor.apply();
    }

    private void showFirstRunClings() {
        // The two first run cling paths are mutually exclusive, if the launcher is preinstalled
        // on the device, then we always show the first run cling experience (or if there is no
        // launcher2). Otherwise, we prompt the user upon started for migration
        LauncherClings launcherClings = new LauncherClings(this);
        if (launcherClings.shouldShowFirstRunOrMigrationClings()) {
            if (mModel.canMigrateFromOldLauncherDb(this)) {
            	Log.e("jiaodian","showMigrationCling");
                launcherClings.showMigrationCling();
            } else {
            	Log.e("jiaodian","showLongPressCling");
                launcherClings.showLongPressCling(true);
            }
        }
    }

    void showWorkspaceSearchAndHotseat() {
        if (mWorkspace != null) mWorkspace.setAlpha(1f);
        if (mHotseat != null) mHotseat.setAlpha(1f);
        if (mPageIndicators != null) mPageIndicators.setAlpha(1f);
        if (mSearchDropTargetBar != null) mSearchDropTargetBar.showSearchBar(false);
    }

    void hideWorkspaceSearchAndHotseat() {
        if (mWorkspace != null) mWorkspace.setAlpha(0f);
        if (mHotseat != null) mHotseat.setAlpha(0f);
        if (mPageIndicators != null) mPageIndicators.setAlpha(0f);
        if (mSearchDropTargetBar != null) mSearchDropTargetBar.hideSearchBar(false);
    }

    /** TODO unused,没有一个地方调用该方法 */
    public ItemInfo createAppDragInfo(Intent appLaunchIntent) {
        // Called from search suggestion, not supported in other profiles.
        final UserHandleCompat myUser = UserHandleCompat.myUserHandle();
        LauncherAppsCompat launcherApps = LauncherAppsCompat.getInstance(this);
        LauncherActivityInfoCompat activityInfo = launcherApps.resolveActivity(appLaunchIntent,
                myUser);
        if (activityInfo == null) {
            return null;
        }
        return new AppInfo(this, activityInfo, myUser, mIconCache, null);
    }

    public ItemInfo createShortcutDragInfo(Intent shortcutIntent, CharSequence caption,
            Bitmap icon) {
        // Called from search suggestion, not supported in other profiles.
        return createShortcutDragInfo(shortcutIntent, caption, icon,
                UserHandleCompat.myUserHandle());
    }

    public ItemInfo createShortcutDragInfo(Intent shortcutIntent, CharSequence caption,
            Bitmap icon, UserHandleCompat user) {
        UserManagerCompat userManager = UserManagerCompat.getInstance(this);
        CharSequence contentDescription = userManager.getBadgedLabelForUser(caption, user);
        return new ShortcutInfo(shortcutIntent, caption, contentDescription, icon, user);
    }

    protected void moveWorkspaceToDefaultScreen() {
        mWorkspace.moveToDefaultScreen(false);
    }

    public void startDrag(View dragView, ItemInfo dragInfo, DragSource source) {
        dragView.setTag(dragInfo);
        mWorkspace.onExternalDragStartedWithItem(dragView);
        mWorkspace.beginExternalDragShared(dragView, source);
    }

    @Override
    public void onPageSwitch(View newPage, int newPageIndex) {
        if (mLauncherCallbacks != null) {
            mLauncherCallbacks.onPageSwitch(newPage, newPageIndex);
        }
    }

    /**
     * Prints out out state for debugging.
     */
    public void dumpState() {
        Log.d(TAG, "BEGIN launcher3 dump state for launcher " + this);
        Log.d(TAG, "mSavedState=" + mSavedState);
        Log.d(TAG, "mWorkspaceLoading=" + mWorkspaceLoading);
        Log.d(TAG, "mRestoring=" + mRestoring);
        Log.d(TAG, "mWaitingForResult=" + mWaitingForResult);
        Log.d(TAG, "mSavedInstanceState=" + mSavedInstanceState);
        Log.d(TAG, "sFolders.size=" + sFolders.size());
        mModel.dumpState();

        if (mAppsCustomizeContent != null) {
            mAppsCustomizeContent.dumpState();
        }
        Log.d(TAG, "END launcher3 dump state");
    }

    @Override
    public void dump(String prefix, FileDescriptor fd, PrintWriter writer, String[] args) {
        super.dump(prefix, fd, writer, args);
        synchronized (sDumpLogs) {
            writer.println(" ");
            writer.println("Debug logs: ");
            for (int i = 0; i < sDumpLogs.size(); i++) {
                writer.println("  " + sDumpLogs.get(i));
            }
        }
        if (mLauncherCallbacks != null) {
            mLauncherCallbacks.dump(prefix, fd, writer, args);
        }
    }

    public static void dumpDebugLogsToConsole() {
        if (DEBUG_DUMP_LOG) {
            synchronized (sDumpLogs) {
                Log.d(TAG, "");
                Log.d(TAG, "*********************");
                Log.d(TAG, "Launcher debug logs: ");
                for (int i = 0; i < sDumpLogs.size(); i++) {
                    Log.d(TAG, "  " + sDumpLogs.get(i));
                }
                Log.d(TAG, "*********************");
                Log.d(TAG, "");
            }
        }
    }

    public static void addDumpLog(String tag, String log, boolean debugLog) {
        addDumpLog(tag, log, null, debugLog);
    }

    public static void addDumpLog(String tag, String log, Exception e, boolean debugLog) {
        if (debugLog) {
            if (e != null) {
                Log.d(tag, log, e);
            } else {
                Log.d(tag, log);
            }
        }
        if (DEBUG_DUMP_LOG) {
            sDateStamp.setTime(System.currentTimeMillis());
            synchronized (sDumpLogs) {
                sDumpLogs.add(sDateFormat.format(sDateStamp) + ": " + tag + ", " + log
                    + (e == null ? "" : (", Exception: " + e)));
            }
        }
    }

    public void dumpLogsToLocalData() {
        if (DEBUG_DUMP_LOG) {
            new AsyncTask<Void, Void, Void>() {
                public Void doInBackground(Void ... args) {
                    boolean success = false;
                    sDateStamp.setTime(sRunStart);
                    String FILENAME = sDateStamp.getMonth() + "-"
                            + sDateStamp.getDay() + "_"
                            + sDateStamp.getHours() + "-"
                            + sDateStamp.getMinutes() + "_"
                            + sDateStamp.getSeconds() + ".txt";

                    FileOutputStream fos = null;
                    File outFile = null;
                    try {
                        outFile = new File(getFilesDir(), FILENAME);
                        outFile.createNewFile();
                        fos = new FileOutputStream(outFile);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    if (fos != null) {
                        PrintWriter writer = new PrintWriter(fos);

                        writer.println(" ");
                        writer.println("Debug logs: ");
                        synchronized (sDumpLogs) {
                            for (int i = 0; i < sDumpLogs.size(); i++) {
                                writer.println("  " + sDumpLogs.get(i));
                            }
                        }
                        writer.close();
                    }
                    try {
                        if (fos != null) {
                            fos.close();
                            success = true;
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    return null;
                }
            }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, (Void) null);
        }
    }
    
	public void resumeEvent() {
		if (dragViews == null || dragViews.size() <= 0)
			return;
		synchronized (dragViews) {
			try {
				for (View view : dragViews) {
					if (view.getVisibility() != View.VISIBLE) {
						// ========== TODO 用处处理从桌面拖至文件夹后,拖拽甩一个至顶部,会在桌面恢复一个拖拽至文件夹的应用图标
						Log.i("super_super", "Launcher_resumeEvent(), dragView : " + view.toString());
						if(view instanceof BubbleTextView){
							try {
								ShortcutInfo tag = (ShortcutInfo)view.getTag();
								AppInfo appInfo = tag.appInfo;
								if(appInfo != null){
									if(isFolderContains(appInfo))
										continue;
								} else {
									Log.i("super_hw", "Launcher resumeEvent(): dragView.tag.appinfo is null, maybe is a ShortcutIcon drag from WidgetPage!!!");
									if(tag.intent != null){
										try {
											ComponentName componentName = tag.intent.getComponent();
											if(isShortcutHasAdded(componentName.getClassName()))
												continue;
										} catch (Exception e) {
											e.printStackTrace();
										}
									} else {
										Log.w("super_hw", "Launcher_resumeEvent() unknow view.");
									}
								}
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
						// ========== TODO end
						view.setVisibility(View.VISIBLE);
//						dragViews.remove(view);
					}
				}
				// TODO 可能出现问题,做一个记录
				dragViews.clear();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	class MyHandler extends Handler{
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
			case CHECK_APPS_SHOW:
				Log.e("super_super", "CHECK_APPS_SHOW");
				new Thread(){
					public void run() {
						checkAllAppsIcon();
					};
				}.start();
				break;
			case ADD_ICON_IN_UITHREAD:
				try {
					ArrayList<AppInfo> appsOrder = (ArrayList<AppInfo>)msg.obj;
					bindAppsAdded(null, null, null, appsOrder);
				} catch (Exception e) {
					e.printStackTrace();
				}
				break;
			case SYSTEM_UI_CHECK:
				try {
					if (!isFirstLoad)
						return;
					if (firstLoadView == null || rootview == null || fingerImg == null)
						return;
					if(LauncherApplication.isFactoryModel)
						return;
					Log.w("super_hw", "systemui.visible : " + rootview.getSystemUiVisibility() + " isViewMarginSizeChange : " + isViewMarginSizeChange());
					if (rootview.getSystemUiVisibility() == View.SYSTEM_UI_FLAG_VISIBLE && isViewMarginSizeChange()) {
						ViewGroup vg = (ViewGroup) firstLoadView.getParent();
						if (vg != null)
							vg.removeView(firstLoadView);
						clearFirstLoadView();
						isFirstLoad = false;
						if (firstRecordPath != null)
							new File(firstRecordPath).createNewFile();
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
				break;
			case DELAY_START_ANIM:
				if(isFirstLoad){
					if(rootview == null || firstLoadView == null || fingerImg == null)
						return;
					// 获取状态栏高度
//					Rect frame = new Rect();  
//					getWindow().getDecorView().getWindowVisibleDisplayFrame(frame);  
//					int statusBarHeight = frame.top;
					if(fingerImg.getHeight() <= 0){
						myHandler.sendEmptyMessageDelayed(DELAY_START_ANIM, 1000);
						return;
					}
					MAnimationUtil.fligerAnim(fingerImg, getWindowManager().getDefaultDisplay().getHeight()
							* 2 / 3 - fingerImg.getHeight(), getWindowManager().getDefaultDisplay().getHeight()
							- fingerImg.getHeight() / 2);
				}
				break;
			case END_INIT_INSTALL_MSG:
				clearFactoryInstall();
//				firstLoad();
				break;
			}
		}
	}

	public void addDragView(View childView) {
		if (dragViews == null)
			return;
		synchronized (dragViews) {
			try {
				dragViews.add(childView);
			} catch (Exception e) {
			}
		}
	}

	public void removeDragView(View childView) {
		if (dragViews == null || childView == null)
			return;
		synchronized (dragViews) {
			try {
				dragViews.remove(childView);
			} catch (Exception e) {
			}
		}
	}

	public void removeDragView(String className) {
		if (dragViews == null || className == null || className.length() <= 0)
			return;
		synchronized (dragViews) {
			try {
				View view = null;
				for(View dView : dragViews){
					if(dView instanceof BubbleTextView) {
						ShortcutInfo info = (ShortcutInfo)dView.getTag();
						if(info.itemType == LauncherSettings.Favorites.ITEM_TYPE_SHORTCUT) {
							if(info.intent != null && info.intent.getComponent() != null){
								if(className.equals(info.intent.getComponent().getClassName())){
									view = dView;
									break;
								}
							}
						}
					}
				}
				if(view != null)
					dragViews.remove(view);
			} catch (Exception e) {
			}
		}
	}
	
	private void initBatteryReceiver(){
		try {
			batteryReceiver = new BatteryReceiver();
			IntentFilter filter=new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
			registerReceiver(batteryReceiver, filter);
		} catch (Exception e) {
			e.printStackTrace();
		}
    }


	@Override
	public void ondisconnect() {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void onconnect() {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void reSetStatusIcon(int i) {
		// TODO Auto-generated method stub
		
	}

}

interface LauncherTransitionable {
    View getContent();
    void onLauncherTransitionPrepare(Launcher l, boolean animated, boolean toWorkspace);
    void onLauncherTransitionStart(Launcher l, boolean animated, boolean toWorkspace);
    void onLauncherTransitionStep(Launcher l, float t);
    void onLauncherTransitionEnd(Launcher l, boolean animated, boolean toWorkspace);
}

interface DebugIntents {
    static final String DELETE_DATABASE = "com.android.launcher3.action.DELETE_DATABASE";
    static final String MIGRATE_DATABASE = "com.android.launcher3.action.MIGRATE_DATABASE";
}

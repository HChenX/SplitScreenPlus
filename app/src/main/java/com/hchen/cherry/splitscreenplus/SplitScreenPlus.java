/*
 * This file is part of SplitScreenPlus.

 * SplitScreenPlus is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * Copyright (C) 2023-2025 HChenX
 */
package com.hchen.cherry.splitscreenplus;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.ContentObserver;
import android.graphics.Rect;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.view.SurfaceControl;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;

import com.hchen.hooktool.HCBase;
import com.hchen.hooktool.hook.IHook;

import java.util.Objects;

/**
 * SplitScreenPlus
 * <pre>{@code
 *      // ON
 *      su -c settings put system sothx_project_treble_vertical_screen_split_enable 0
 *      // OFF
 *      su -c settings put system sothx_project_treble_vertical_screen_split_enable 1
 * }
 *
 * @author 焕晨HChen、Cherry
 */
public class SplitScreenPlus extends HCBase {
    private Context context;
    private Resources resources;
    private static boolean isRegistered = false;
    private static final String SPLIT_SCREEN_SETTINGS = "sothx_project_treble_vertical_screen_split_enable";
    private Object soScStageCoordinator = null;
    private Object stageCoordinator = null;
    private LinearLayout dragLayout = null;
    private Handler handler = null;

    @Override
    protected void onApplication(@NonNull Context context) {
        if (isRegistered) return;

        this.context = context;
        this.resources = context.getResources();
        context.getContentResolver().registerContentObserver(Settings.System.getUriFor(SPLIT_SCREEN_SETTINGS),
            false,
            new ContentObserver(new Handler(Looper.getMainLooper())) {
                @Override
                public void onChange(boolean selfChange) {
                    if (selfChange) return;
                    if (handler == null) return;
                    Object temp = soScStageCoordinator != null ? soScStageCoordinator : (stageCoordinator != null ? stageCoordinator : null);
                    if (temp == null) return;
                    Configuration configuration = (Configuration) getField(getField(temp, "mRootTaskInfo"), "configuration");
                    boolean canLeftRightSplit = canLeftRightSplit(configuration);

                    handler.post(() -> {
                        if (soScStageCoordinator != null) {
                            Object splitLayout = getField(soScStageCoordinator, "mSplitLayout");
                            if (splitLayout != null) {
                                callMethod(splitLayout, "release", new Class[]{SurfaceControl.Transaction.class}, (Object) null);
                                setField(soScStageCoordinator, "mSplitLayout", newInstance(
                                    "com.android.wm.shell.common.split.SplitLayout",
                                    "SoScStageCoordinatorSplitDivider",
                                    getField(soScStageCoordinator, "mContext"),
                                    getField(getField(soScStageCoordinator, "mRootTaskInfo"), "configuration"),
                                    soScStageCoordinator,
                                    getField(soScStageCoordinator, "mParentContainerCallbacks"),
                                    getField(soScStageCoordinator, "mDisplayController"),
                                    getField(soScStageCoordinator, "mDisplayImeController"),
                                    getField(soScStageCoordinator, "mTaskOrganizer")
                                ));
                            }
                        }
                        if (stageCoordinator != null) {
                            Object splitLayout = getField(stageCoordinator, "mSplitLayout");
                            if (splitLayout != null) {
                                callMethod(splitLayout, "release", new Class[]{SurfaceControl.Transaction.class}, (Object) null);
                                setField(stageCoordinator, "mSplitLayout", newInstance(
                                    "com.android.wm.shell.common.split.SplitLayout",
                                    "StageCoordinatorSplitDivider",
                                    getField(stageCoordinator, "mContext"),
                                    getField(getField(stageCoordinator, "mRootTaskInfo"), "configuration"),
                                    stageCoordinator,
                                    getField(stageCoordinator, "mParentContainerCallbacks"),
                                    getField(stageCoordinator, "mDisplayController"),
                                    getField(stageCoordinator, "mDisplayImeController"),
                                    getField(stageCoordinator, "mTaskOrganizer")
                                ));
                            }
                        }

                        setField(dragLayout, "mIsLeftRightSplit", canLeftRightSplit);
                        dragLayout.setOrientation(canLeftRightSplit ? LinearLayout.HORIZONTAL : LinearLayout.VERTICAL);
                        callMethod(dragLayout, "updateContainerMargins", canLeftRightSplit);
                        dragLayout.requestLayout();
                    });
                }
            }
        );
        isRegistered = true;
    }

    @Override
    protected void init() {
        hookMethod("com.android.wm.shell.common.split.SplitScreenUtils",
            "isLeftRightSplit",
            boolean.class, Configuration.class,
            new IHook() {
                @Override
                public void before() {
                    boolean allowLeftRightSplitInPortrait = (boolean) getArg(0);
                    Configuration configuration = (Configuration) getArg(1);
                    boolean b = configuration.smallestScreenWidthDp >= (isSplitScreenPlusEnabled() ? 1024 : 600);
                    Rect rect = (Rect) callMethod(getField(configuration, "windowConfiguration"), "getMaxBounds");
                    boolean isLeftRightSplit = (boolean) callThisStaticMethod("isLeftRightSplit", allowLeftRightSplitInPortrait, b, rect.width() >= rect.height());
                    setResult(isLeftRightSplit);

                    logD(TAG, "isLeftRightSplit: " + isLeftRightSplit + ", result: " + getResult());
                }
            }
        );

        hookMethod("com.android.wm.shell.draganddrop.DragLayout",
            "onAttachedToWindow",
            new IHook() {
                @Override
                public void after() {
                    dragLayout = (LinearLayout) thisObject();
                    handler = dragLayout.getHandler();
                    logD(TAG, "dragLayout: " + dragLayout + ", handler: " + handler);
                }
            }
        );

        hookMethod("com.android.wm.shell.sosc.SoScStageCoordinator",
            "onTaskAppeared",
            "android.app.ActivityManager$RunningTaskInfo", SurfaceControl.class,
            new IHook() {
                @Override
                public void before() {
                    soScStageCoordinator = thisObject();
                }
            }
        );

        hookMethod("com.android.wm.shell.splitscreen.StageCoordinator",
            "onTaskAppeared",
            "android.app.ActivityManager$RunningTaskInfo", SurfaceControl.class,
            new IHook() {
                @Override
                public void before() {
                    stageCoordinator = thisObject();
                }
            }
        );
    }

    /**
     * @noinspection SimplifiableConditionalExpression, ConstantValue
     */
    private boolean canLeftRightSplit(Configuration configuration) {
        boolean allowLeftRightSplitInPortrait = (
            resources == null ? true :
                (boolean) callStaticMethod("com.android.wm.shell.common.split.SplitScreenUtils", "allowLeftRightSplitInPortrait", resources)
        );

        return (boolean) callStaticMethod(
            "com.android.wm.shell.common.split.SplitScreenUtils",
            "isLeftRightSplit",
            allowLeftRightSplitInPortrait, configuration
        );
    }

    private boolean isSplitScreenPlusEnabled() {
        if (context == null) {
            logW(TAG, "[isSplitScreenPlusEnabled]: context is null!! will return false!!");
            return false;
        }
        return Objects.equals(Settings.System.getInt(context.getContentResolver(), SPLIT_SCREEN_SETTINGS, 0), 1);
    }
}

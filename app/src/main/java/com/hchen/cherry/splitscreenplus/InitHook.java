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

import static com.hchen.hooktool.HCInit.LOG_D;
import static com.hchen.hooktool.HCInit.LOG_I;

import androidx.annotation.NonNull;

import com.hchen.hooktool.HCEntrance;
import com.hchen.hooktool.HCInit;

import de.robv.android.xposed.callbacks.XC_LoadPackage;

/**
 * Hook 入口
 *
 * @author 焕晨HChen
 */
public class InitHook extends HCEntrance {
    @NonNull
    @Override
    public HCInit.BasicData initHC(@NonNull HCInit.BasicData basicData) {
        return basicData.setTag("SplitScreenPlus")
            .setLogLevel(BuildConfig.DEBUG ? LOG_D : LOG_I)
            .setModulePackageName(BuildConfig.APPLICATION_ID)
            .setLogExpandPath("com.hchen.cherry.splitscreenplus");
    }

    @NonNull
    @Override
    public String[] ignorePackageNameList() {
        return new String[]{
            "com.google.android.webview",
            "com.miui.contentcatcher"
        };
    }

    @Override
    public void onLoadPackage(@NonNull XC_LoadPackage.LoadPackageParam loadPackageParam) throws Throwable {
        HCInit.initLoadPackageParam(loadPackageParam);
        new SplitScreenPlus().onApplication().onLoadPackage();
    }
}

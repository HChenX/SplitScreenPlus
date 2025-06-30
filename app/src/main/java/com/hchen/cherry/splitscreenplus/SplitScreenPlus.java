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

import android.content.res.Configuration;
import android.graphics.Rect;

import com.hchen.hooktool.HCBase;
import com.hchen.hooktool.hook.IHook;
import com.hchen.hooktool.utils.SystemPropTool;

/**
 * SplitScreenPlus
 *
 * @author 焕晨HChen、Cherry
 */
public class SplitScreenPlus extends HCBase {
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
                }
            }
        );
    }

    private boolean isSplitScreenPlusEnabled() {
        return SystemPropTool.getProp("persist.config.sothx_project_treble_vertical_screen_split_enable", false);
    }
}

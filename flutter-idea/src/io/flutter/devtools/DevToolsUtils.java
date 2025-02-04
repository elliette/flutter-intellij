/*
 * Copyright 2019 The Chromium Authors. All rights reserved.
 * Use of this source code is governed by a BSD-style license that can be
 * found in the LICENSE file.
 */
package io.flutter.devtools;

import com.intellij.designer.designSurface.ZoomProvider;
import com.intellij.ide.ui.UISettingsUtils;
import com.intellij.openapi.editor.colors.EditorColorsManager;
import com.intellij.ui.ColorUtil;
import com.intellij.ui.JBColor;
import com.intellij.ui.scale.JBUIScale;
import com.intellij.ui.scale.Scaler;
import com.intellij.util.ui.UIUtil;
import org.jetbrains.annotations.NotNull;
import com.intellij.util.ui.JBUI;

import java.util.spi.ToolProvider;

public class DevToolsUtils {
  public static String findWidgetId(String url) {
    final String searchFor = "inspectorRef=";
    final String[] split = url.split("&");
    for (String part : split) {
      if (part.startsWith(searchFor)) {
        return part.substring(searchFor.length());
      }
    }
    return null;
  }

  public String getColorHexCode() {
    return ColorUtil.toHex(UIUtil.getEditorPaneBackground());
  }

  public Boolean getIsBackgroundBright() {
    return JBColor.isBright();
  }

  public @NotNull Float getFontSize() {
    EditorColorsManager manager = EditorColorsManager.getInstance();
    if (manager == null) {
      // Return the default normal font size if editor manager is not found.
      return UIUtil.getFontSize(UIUtil.FontSize.NORMAL);
    }
    return (float) manager.getGlobalScheme().getEditorFontSize();
  }

  public int getZoomLevel() {
    final UISettingsUtils uiSettingsUtils = UISettingsUtils.getInstance();
    final float ideScale = uiSettingsUtils.getCurrentIdeScale();
    if (ideScale != 1) {
      return Math.round(ideScale * 100);
    }
    return 100;
    // final float editorFontSize = uiSettingsUtils.getScaledEditorFontSize();
  }
}

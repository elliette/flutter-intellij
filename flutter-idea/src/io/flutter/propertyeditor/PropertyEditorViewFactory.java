/*
 * Copyright 2025 The Chromium Authors. All rights reserved.
 * Use of this source code is governed by a BSD-style license that can be
 * found in the LICENSE file.
 */
package io.flutter.propertyeditor;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.openapi.wm.ex.ToolWindowManagerListener;
import com.intellij.util.messages.MessageBusConnection;
import io.flutter.FlutterUtils;
import io.flutter.actions.RefreshToolWindowAction;
import io.flutter.bazel.WorkspaceCache;
import io.flutter.dart.DartPlugin;
import io.flutter.dart.DartPluginVersion;
import io.flutter.devtools.DevToolsIdeFeature;
import io.flutter.devtools.DevToolsUrl;
import io.flutter.run.daemon.DevToolsService;
import io.flutter.sdk.FlutterSdk;
import io.flutter.sdk.FlutterSdkVersion;
import io.flutter.utils.AsyncUtils;
import io.flutter.utils.OpenApiUtils;
import io.flutter.view.DevToolsViewUtils;
import kotlin.coroutines.Continuation;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;

public class PropertyEditorViewFactory implements ToolWindowFactory {
  @NotNull private static String TOOL_WINDOW_ID = "Flutter Property Editor";

  @NotNull
  private final DevToolsViewUtils devToolsViewUtils = new DevToolsViewUtils(TOOL_WINDOW_ID);

  @Override
  public Object isApplicableAsync(@NotNull Project project, @NotNull Continuation<? super Boolean> $completion) {
    FlutterSdk sdk = FlutterSdk.getFlutterSdk(project);
    FlutterSdkVersion sdkVersion = sdk == null ? null : sdk.getVersion();
    return sdkVersion != null && sdkVersion.canUsePropertyEditor();
  }

  @Override
  public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
    DartPluginVersion dartPluginVersion = DartPlugin.getDartPluginVersion();
    if (!dartPluginVersion.supportsPropertyEditor()) {
      devToolsViewUtils.presentLabel(toolWindow, "Flutter Property Editor requires a newer version of the Dart plugin.");
      return;
    }

    devToolsViewUtils.initDevToolsView(project, toolWindow, "propertyEditor");
  }
}

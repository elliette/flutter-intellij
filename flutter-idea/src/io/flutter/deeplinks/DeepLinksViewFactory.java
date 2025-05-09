/*
 * Copyright 2024 The Chromium Authors. All rights reserved.
 * Use of this source code is governed by a BSD-style license that can be
 * found in the LICENSE file.
 */
package io.flutter.deeplinks;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import io.flutter.FlutterUtils;
import io.flutter.actions.RefreshToolWindowAction;
import io.flutter.bazel.WorkspaceCache;
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

public class DeepLinksViewFactory implements ToolWindowFactory {
  @NotNull private static String TOOL_WINDOW_ID = "Flutter Deep Links";

  @NotNull
  private final DevToolsViewUtils devToolsViewUtils = new DevToolsViewUtils(TOOL_WINDOW_ID);

  @Override
  public Object isApplicableAsync(@NotNull Project project, @NotNull Continuation<? super Boolean> $completion) {
    FlutterSdk sdk = FlutterSdk.getFlutterSdk(project);
    FlutterSdkVersion sdkVersion = sdk == null ? null : sdk.getVersion();
    return sdkVersion != null && sdkVersion.canUseDeepLinksTool();
  }

  @Override
  public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
    devToolsViewUtils.initDevToolsView(project, toolWindow, "deep-links");
  }
}

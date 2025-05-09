/*
 * Copyright 2025 The Chromium Authors. All rights reserved.
 * Use of this source code is governed by a BSD-style license that can be
 * found in the LICENSE file.
 */
package io.flutter.view;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.VerticalFlowLayout;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ex.ToolWindowManagerListener;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.labels.LinkLabel;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentManager;
import com.intellij.util.messages.MessageBusConnection;
import com.intellij.util.ui.JBUI;
import com.intellij.util.ui.UIUtil;
import io.flutter.FlutterUtils;
import io.flutter.actions.RefreshToolWindowAction;
import io.flutter.bazel.WorkspaceCache;
import io.flutter.devtools.DevToolsIdeFeature;
import io.flutter.devtools.DevToolsUrl;
import io.flutter.run.daemon.DevToolsInstance;
import io.flutter.run.daemon.DevToolsService;
import io.flutter.sdk.FlutterSdk;
import io.flutter.sdk.FlutterSdkVersion;
import io.flutter.utils.AsyncUtils;
import io.flutter.utils.LabelInput;
import io.flutter.utils.OpenApiUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

public class DevToolsViewUtils {
  public DevToolsViewUtils(String toolWindowId) {
    this.toolWindowId = toolWindowId;
  }

  final String toolWindowId;
  boolean devToolsLoadedInBrowser = false;

  public void initDevToolsView(@NotNull Project project, @NotNull ToolWindow toolWindow,  @NotNull String devToolsRoute) {
    initDevToolsView(project, toolWindow, devToolsRoute, false, null);
  }

  public void initDevToolsView(@NotNull Project project, @NotNull ToolWindow toolWindow,  @NotNull String devToolsRoute, @NotNull boolean isHiddenRoute, @Nullable Consumer<EmbeddedBrowser> onBrowserLoaded) {
    loadDevToolsInEmbeddedBrowser(project, toolWindow, devToolsRoute, isHiddenRoute, onBrowserLoaded);
    maybeReloadDevToolsWhenVisible(project, toolWindow, devToolsRoute, isHiddenRoute, onBrowserLoaded);
  }

  public void presentLabel(ToolWindow toolWindow, String text) {
    final JBLabel label = new JBLabel("<html>" + text + "</html>", SwingConstants.CENTER);
    label.setForeground(UIUtil.getLabelDisabledForeground());
    replacePanelLabel(toolWindow, label);
  }

  public void presentClickableLabel(ToolWindow toolWindow, List<LabelInput> labels) {
    final JPanel panel = new JPanel(new GridLayout(0, 1));

    for (LabelInput input : labels) {
      if (input.listener == null) {
        final JLabel descriptionLabel = new JLabel("<html>" + input.text + "</html>");
        descriptionLabel.setBorder(JBUI.Borders.empty(5));
        descriptionLabel.setHorizontalAlignment(SwingConstants.CENTER);
        panel.add(descriptionLabel, BorderLayout.NORTH);
      }
      else {
        final LinkLabel<String> linkLabel = new LinkLabel<>("<html>" + input.text + "</html>", null);
        linkLabel.setBorder(JBUI.Borders.empty(5));
        linkLabel.setListener(input.listener, null);
        linkLabel.setHorizontalAlignment(SwingConstants.CENTER);
        panel.add(linkLabel, BorderLayout.SOUTH);
      }
    }

    final JPanel center = new JPanel(new VerticalFlowLayout(VerticalFlowLayout.CENTER));
    center.add(panel);
    replacePanelLabel(toolWindow, center);
  }

  private void loadDevToolsInEmbeddedBrowser(@NotNull Project project, @NotNull ToolWindow toolWindow, @NotNull String devToolsRoute, @NotNull boolean isHiddenRoute, @Nullable Consumer<EmbeddedBrowser> onBrowserLoaded) {
    presentLabel(toolWindow, "Loading " + toolWindowId + "...");
    FlutterSdk sdk = FlutterSdk.getFlutterSdk(project);
    FlutterSdkVersion sdkVersion = sdk == null ? null : sdk.getVersion();

    AsyncUtils.whenCompleteUiThread(
      DevToolsService.getInstance(project).getDevToolsInstance(),
      (instance, error) -> {
        final boolean inValidState = verifyDevToolsPanelStateIsValid(toolWindow, project, instance, error);
        if (!inValidState) {
          return;
        }

        final DevToolsUrl.Builder devToolsUrlBuilder = new DevToolsUrl.Builder();
        if (isHiddenRoute) {
          devToolsUrlBuilder.setHide(devToolsRoute);
        } else {
          devToolsUrlBuilder.setPage(devToolsRoute);
        }

        final DevToolsUrl devToolsUrl = devToolsUrlBuilder
          .setDevToolsHost(instance.host())
          .setDevToolsPort(instance.port())
          .setEmbed(true)
          .setFlutterSdkVersion(sdkVersion)
          .setWorkspaceCache(WorkspaceCache.getInstance(project))
          .setIdeFeature(DevToolsIdeFeature.TOOL_WINDOW)
          .build();

        OpenApiUtils.safeInvokeLater(() -> {
          Optional.ofNullable(
              FlutterUtils.embeddedBrowser(project))
            .ifPresent(embeddedBrowser -> {
              embeddedBrowser.openPanel(toolWindow, toolWindowId, devToolsUrl, System.out::println);
              // The "refresh" action refreshes the embedded browser, not the panel.
              // Therefore, we only show it once we have an embedded browser.
              toolWindow.setTitleActions(List.of(new RefreshToolWindowAction(toolWindowId)));
              devToolsLoadedInBrowser = true;
              if (onBrowserLoaded != null) {
                onBrowserLoaded.accept(embeddedBrowser);
              }
            });
        });
      }
    );
  }

  private void maybeReloadDevToolsWhenVisible(@NotNull Project project, @NotNull ToolWindow toolWindow, @NotNull String devToolsRoute, @Nullable boolean isHiddenRoute, @Nullable Consumer<EmbeddedBrowser> onBrowserLoaded) {
    MessageBusConnection connection = project.getMessageBus().connect();
    connection.subscribe(ToolWindowManagerListener.TOPIC, new ToolWindowManagerListener() {
      @Override
      public void toolWindowShown(@NotNull ToolWindow activatedToolWindow) {
        if (activatedToolWindow.getId().equals(toolWindowId)) {
          System.out.println(toolWindowId + " is visible!");
          if (!devToolsLoadedInBrowser) {
            loadDevToolsInEmbeddedBrowser(project, toolWindow, devToolsRoute, isHiddenRoute, onBrowserLoaded);
          }
        }
      }
    });
    Disposer.register(toolWindow.getDisposable(), connection);
  }

  private boolean verifyDevToolsPanelStateIsValid(ToolWindow toolWindow, Project project, DevToolsInstance instance, Throwable error) {
    if (!project.isOpen()) {
      presentLabel(toolWindow, "<h1>Project is not open.</h1>");
      return false;
    }

    final String restartDevToolsMessage = "</br></br><h2>Try switching to another Flutter panel and back again to re-start the server.</h2>";
    if (error != null) {
      presentLabel(toolWindow, "<h1>Flutter DevTools start-up failed.</h1>" + restartDevToolsMessage);
      return false;
    }

    if (instance == null) {
      presentLabel(toolWindow, "<h1>Flutter DevTools does not exist.</h1>" + restartDevToolsMessage);
      return false;
    }

    return true;
  }

  private void replacePanelLabel(ToolWindow toolWindow, JComponent label) {
    OpenApiUtils.safeInvokeLater(() -> {
      final ContentManager contentManager = toolWindow.getContentManager();
      if (contentManager.isDisposed()) {
        return;
      }

      final JPanel panel = new JPanel(new BorderLayout());
      panel.add(label, BorderLayout.CENTER);
      final Content content = contentManager.getFactory().createContent(panel, null, false);
      contentManager.removeAllContents(true);
      contentManager.addContent(content);
    });
  }
}

/*
 * Copyright 2022 The Chromium Authors. All rights reserved.
 * Use of this source code is governed by a BSD-style license that can be
 * found in the LICENSE file.
 */
package io.flutter.view;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.ui.content.ContentManager;
import com.intellij.ui.jcef.JBCefBrowser;
import com.teamdev.jxbrowser.zoom.Zoom;
import com.teamdev.jxbrowser.zoom.ZoomLevel;
import io.flutter.jxbrowser.JxBrowserManager;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.Dimension;
import java.util.Objects;

class EmbeddedJcefBrowserTab implements EmbeddedTab {
  private JBCefBrowser browser;

  public EmbeddedJcefBrowserTab() throws Exception {
    this.browser = new JBCefBrowser();
  }

  @Override
  public void loadUrl(String url) {
    browser.loadURL(url);
  }

  @Override
  public void close() {

  }

  @Override
  public void zoom(int zoomPercent) {

  }

  @Override
  public JComponent getTabComponent(ContentManager contentManager) {
    browser.getComponent().setPreferredSize(new Dimension(contentManager.getComponent().getWidth(), contentManager.getComponent().getHeight()));
    return browser.getComponent();
  }
}

public class EmbeddedJcefBrowser extends EmbeddedBrowser {
  private static final Logger LOG = Logger.getInstance(JxBrowserManager.class);

  public EmbeddedJcefBrowser(Project project) {
    super(project);
  }

  @NotNull
  public static EmbeddedJcefBrowser getInstance(@NotNull Project project) {
    return Objects.requireNonNull(project.getService(EmbeddedJcefBrowser.class));
  }

  public Logger logger() {
    return LOG;
  }

  @Override
  public EmbeddedTab openEmbeddedTab(ContentManager contentManager) throws Exception {
    return new EmbeddedJcefBrowserTab();
  }
}

package com.songs.core.ui.util

import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.Composable
import androidx.window.core.layout.WindowSizeClass

@Composable
fun isLargeScreen(): Boolean {
    val adaptiveInfo = currentWindowAdaptiveInfo(supportLargeAndXLargeWidth = true)
    val windowSizeClass = adaptiveInfo.windowSizeClass

    return windowSizeClass.isWidthAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_MEDIUM_LOWER_BOUND)

}

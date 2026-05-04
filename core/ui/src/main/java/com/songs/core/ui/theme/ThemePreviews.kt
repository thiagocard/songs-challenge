package com.songs.core.ui.theme

import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview

/**
 * Previews for tablets, both portrait and landscape orientations.
 */
@Preview(
    name = "Tablet Portrait",
    showBackground = true,
    device = "spec:width=800dp,height=1280dp,dpi=240,isRound=false,chinSize=0dp,orientation=portrait"
)
@Preview(
    name = "Tablet Landscape",
    showBackground = true,
    device = "spec:width=1280dp,height=800dp,dpi=240,isRound=false,chinSize=0dp,orientation=landscape"
)
annotation class TabletPreviews

/**
 * Previews for phones, both portrait and landscape orientations.
 */
@Preview(
    name = "Phone Portrait",
    showBackground = true,
    device = "spec:width=360dp,height=640dp,dpi=240,isRound=false,chinSize=0dp,orientation=portrait"
)
@Preview(
    name = "Phone Landscape",
    showBackground = true,
    device = "spec:width=640dp,height=360dp,dpi=240,isRound=false,chinSize=0dp,orientation=landscape"
)
annotation class PhonePreviews

@PhonePreviews
@TabletPreviews
annotation class AllPreviews


@Composable
fun ThemePreview(
    content: @Composable () -> Unit
) {
    SongsTheme {
        Surface { content() }
    }
}

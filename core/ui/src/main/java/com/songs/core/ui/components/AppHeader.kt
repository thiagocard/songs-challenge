package com.songs.core.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.songs.core.ui.theme.ThemePreview

/**
 * A reusable app header row with an optional navigation icon (start) and optional trailing content.
 *
 * @param title The title displayed in the header.
 * @param modifier Optional modifier.
 * @param navigationIcon Optional composable for the leading navigation icon (e.g. back button).
 * @param trailingContent Optional composable for the trailing action area.
 */
@Composable
fun AppHeader(
    title: String,
    modifier: Modifier = Modifier,
    navigationIcon: @Composable (() -> Unit)? = null,
    trailingContent: @Composable (() -> Unit)? = null,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (navigationIcon != null) {
                Box(
                    modifier = Modifier.offset(x = (-12).dp) // Adjust for default padding
                ) {
                    navigationIcon()
                }
            }
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onBackground
            )
        }
        if (trailingContent != null) {
            Row {
                trailingContent()
            }
        }
    }
}

@Preview
@Composable
private fun AppHeaderPreview() {
    ThemePreview {
        Surface {
            AppHeader(
                title = "Now Playing",
                navigationIcon = {
                    IconButton(onClick = {}) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onBackground,
                            modifier = Modifier.offset(x = (-12).dp)
                        )
                    }
                }
            )
        }
    }
}

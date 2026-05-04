package com.songs.core.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.songs.core.ui.R

@Composable
fun NavigationIcon(
    modifier: Modifier = Modifier,
    onNavigateUp: () -> Unit,
) {
    IconButton(
        modifier = modifier,
        onClick = onNavigateUp
    ) {
        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
            contentDescription = stringResource(R.string.back),
            tint = MaterialTheme.colorScheme.onBackground,
        )
    }
}

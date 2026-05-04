package com.songs.home.presentation.ui.songs

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.songs.core.ui.components.AppHeader
import com.songs.core.ui.resources.Colors
import com.songs.core.ui.theme.ThemePreview
import com.songs.feature.home.R

private const val HEADER_IN_OUT_ANIMATION_DURATION = 300
private const val SEARCH_IN_OUT_ANIMATION_DURATION = 200

@Composable
internal fun Header(
    isVisible: Boolean,
    isSearchActive: Boolean,
    onSearchActiveChanged: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    searchTextFieldValue: TextFieldValue,
    searchTextFieldValueChange: (TextFieldValue) -> Unit,
    onSearchTermChanged: (String) -> Unit,
) {
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current

    AnimatedVisibility(
        modifier = modifier,
        visible = isVisible,
        enter = expandVertically(animationSpec = tween(durationMillis = HEADER_IN_OUT_ANIMATION_DURATION)) +
                slideInVertically(
                    initialOffsetY = { -it },
                    animationSpec = tween(durationMillis = HEADER_IN_OUT_ANIMATION_DURATION)
                ),
        exit = shrinkVertically(animationSpec = tween(durationMillis = HEADER_IN_OUT_ANIMATION_DURATION)) +
                slideOutVertically(
                    targetOffsetY = { -it },
                    animationSpec = tween(durationMillis = HEADER_IN_OUT_ANIMATION_DURATION)
                ),
    ) {
        Column {
            AppHeader(
                title = stringResource(R.string.songs),
                trailingContent = {
                    IconButton(onClick = {
                        val next = !isSearchActive
                        onSearchActiveChanged(next)
                        if (!next) keyboardController?.hide()
                    }) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = stringResource(R.string.search_your_library),
                            tint = if (isSearchActive)
                                MaterialTheme.colorScheme.primary
                            else
                                MaterialTheme.colorScheme.onBackground,
                        )
                    }
                }
            )

            AnimatedVisibility(
                visible = isSearchActive,
                enter = expandVertically(animationSpec = tween(durationMillis = SEARCH_IN_OUT_ANIMATION_DURATION)),
                exit = shrinkVertically(animationSpec = tween(durationMillis = SEARCH_IN_OUT_ANIMATION_DURATION)),
            ) {
                LaunchedEffect(Unit) {
                    focusRequester.requestFocus()
                    keyboardController?.show()
                }
                Column {
                    Spacer(modifier = Modifier.height(16.dp))

                    TextField(
                        value = searchTextFieldValue,
                        maxLines = 1,
                        onValueChange = { newValue ->
                            searchTextFieldValueChange(newValue)
                            onSearchTermChanged(newValue.text)
                        },
                        placeholder = {
                            Text(
                                stringResource(R.string.search_your_library),
                                color = MaterialTheme.colorScheme.onBackground.copy(alpha = .3f)
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onBackground.copy(alpha = .3f)
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp)
                            .focusRequester(focusRequester),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                        keyboardActions = KeyboardActions(
                            onSearch = { keyboardController?.hide() }
                        ),
                        shape = RoundedCornerShape(16.dp),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Colors.DarkGray,
                            unfocusedContainerColor = Colors.DarkGray,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            disabledIndicatorColor = Color.Transparent
                        )
                    )
                }
            }
        }
    }
}

@Preview
@Composable
private fun Preview() {
    ThemePreview {
        Surface {
            Header(
                isVisible = true,
                isSearchActive = false,
                onSearchActiveChanged = {},
                searchTextFieldValue = TextFieldValue(""),
                searchTextFieldValueChange = {},
                onSearchTermChanged = {},
            )
        }
    }
}

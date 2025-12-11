package org.librevault.presentation.screens.gallery.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.material3.DrawerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import kotlinx.coroutines.launch
import org.librevault.R
import org.librevault.presentation.aliases.DeleteSelectionList

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GalleryTopBar(
    deleteSelection: DeleteSelectionList,
    drawerState: DrawerState,
    onDeleteSelected: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val selectionActive = deleteSelection.isNotEmpty()

    TopAppBar(
        scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(),
        title = {
            AnimatedContent(
                targetState = selectionActive,
                transitionSpec = {
                    fadeIn() + scaleIn() togetherWith fadeOut() + scaleOut()
                },
                label = "TopBarTitleAnim"
            ) { active ->
                if (active) {
                    // When deleting
                    Text(
                        text = stringResource(R.string.delete),
                    )
                } else {
                    // Normal mode
                    Text(
                        text = stringResource(R.string.app_name),
                    )
                }
            }
        },

        navigationIcon = {
            AnimatedVisibility(
                visible = !selectionActive,
                enter = fadeIn() + scaleIn(),
                exit = scaleOut() + fadeOut()
            ) {
                IconButton(
                    onClick = { coroutineScope.launch { drawerState.open() } }
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.baseline_menu_24),
                        contentDescription = stringResource(id = R.string.navigate_up)
                    )
                }
            }
        },

        actions = {
            AnimatedVisibility(
                visible = selectionActive,
                enter = fadeIn() + scaleIn(),
                exit = scaleOut() + fadeOut()
            ) {
                IconButton(onClick = onDeleteSelected) {
                    Icon(
                        painter = painterResource(id = R.drawable.baseline_delete_24),
                        contentDescription = stringResource(id = R.string.delete)
                    )
                }
            }
        }
    )
}
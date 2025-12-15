package org.librevault.presentation.screens.gallery.components

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource

@Composable
fun DrawerItem(
    modifier: Modifier = Modifier,
    @DrawableRes iconRes: Int,
    @StringRes labelRes: Int,
    selected: Boolean,
    onClick: () -> Unit,
) {
    DrawerItem(
        modifier = modifier,
        iconRes = iconRes,
        label = stringResource(id = labelRes),
        selected = selected,
        onClick = onClick
    )
}

@Composable
fun DrawerItem(
    modifier: Modifier = Modifier,
    @DrawableRes iconRes: Int,
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    NavigationDrawerItem(
        modifier = modifier,
        icon = {
            Icon(
                painter = painterResource(id = iconRes),
                contentDescription = label
            )
        },
        label = {
            Text(text = label)
        },
        selected = selected,
        onClick = onClick
    )
}
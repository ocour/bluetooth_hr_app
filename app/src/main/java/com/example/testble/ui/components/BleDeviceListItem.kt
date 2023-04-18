package com.example.testble.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import com.example.testble.R


@Composable
fun BleDeviceListItem(
    modifier: Modifier = Modifier,
    name: String?,
    address: String,
    onClick: () -> Unit
) {
    val headline = name ?: address

    ListItem(
        modifier = modifier.clickable{ onClick() },
        leadingContent = {
            Icon(
                painter = painterResource(id = R.drawable.icon_bluetooth),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onBackground
            )
        },
        headlineContent = {
            Text(
                text = headline,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onBackground
            )
        },
        supportingContent = {
            if(!name.isNullOrBlank()) {
                Text(
                    text = address,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
        },
        colors = ListItemDefaults.colors(
            containerColor = MaterialTheme.colorScheme.background
        )
    )
}

@Preview
@Composable
fun BleDeviceListItemPreview() {
    Column() {
        BleDeviceListItem(
            name = "Test bluetooth device",
            address = "00:11:22:33:FF:EE",
            onClick = {}
        )
        BleDeviceListItem(
            name = null,
            address = "00:11:22:33:FF:EE",
            onClick = {}
        )
    }
}
package com.example.testble.ui.components

import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview


@Composable
fun BleDeviceListHeader(
    modifier: Modifier = Modifier,
    headline: String,
    showSupporting: Boolean = true,
    supporting: String
) {
    ListItem(
        modifier = modifier,
        headlineContent = {
            Text(
                text = headline,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onBackground
            )
        },
        supportingContent = {
            if(showSupporting) {
                Text(
                    text = supporting,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
        },
        colors = ListItemDefaults.colors(
            containerColor = MaterialTheme.colorScheme.background
        )
    )
}

@Preview(showBackground = true)
@Composable
fun BleDeviceListHeaderPreview() {
    BleDeviceListHeader(
        headline = "Paired devices",
        supporting = "Turn bluetooth on to see paired devices."
    )
}
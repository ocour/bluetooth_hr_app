package com.example.testble.ui.components

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScanTopBar(
    modifier: Modifier = Modifier,
    onCheckedChanged: (Boolean) -> Unit,
    checked: Boolean,
    switchEnabled: Boolean = true,
    title: String
) {
    TopAppBar(
        modifier = modifier,
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.primary
        ),
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onPrimary,
                maxLines = 1
            )
        },
        actions = {
            if(checked) {
                CircularProgressIndicator(
                    modifier = Modifier.padding(start = 12.dp, end = 12.dp).size(32.dp),
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }
            Switch(
                enabled = switchEnabled,
                checked = checked,
                onCheckedChange = onCheckedChanged,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = MaterialTheme.colorScheme.primary,
                    checkedTrackColor = MaterialTheme.colorScheme.onPrimary,
                    uncheckedTrackColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    )
}

@Preview(showSystemUi = true)
@Composable
fun ScanBarPreview() {
    var checked by remember{ mutableStateOf(false) }

    Scaffold(
        topBar = {
            ScanTopBar(
                title = "Scan for devices",
                checked = checked,
                onCheckedChanged = { checked = it}
            )
        }
    ) {
        Text(text = "test", modifier = Modifier.padding(it))
    }
}
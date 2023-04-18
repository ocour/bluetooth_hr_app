package com.example.testble.ui.components

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConnectionTopBar(
    modifier: Modifier = Modifier,
    title: String,
    canNavigateBack: Boolean = true,
    onNavigationBack: () -> Unit = {},
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
        navigationIcon = {
            if(canNavigateBack) {
                IconButton(onClick = onNavigationBack) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack, contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
        }
    )
}
package com.example.testble.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.testble.R

@Composable
fun HeartRateCard(
    modifier: Modifier = Modifier,
    title: String,
    hearRate: Int?
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .size(160.dp)
            .padding(start = 12.dp, end = 12.dp, top = 8.dp, bottom = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 4.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceEvenly,
            horizontalAlignment = Alignment.Start
        ) {
            Row(
                modifier = modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Start
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineSmall
                )
            }
            Row(
                modifier = modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Start
            ) {
                Icon(
                    modifier = Modifier.fillMaxHeight(),
                    painter = painterResource(id = R.drawable.vital_sign),
                    contentDescription = null
                )
                Spacer(modifier = Modifier.padding(8.dp))
                Text(
                    text = hearRate.toString(),
                    style = MaterialTheme.typography.titleLarge
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HeartRateCardPreview() {
    HeartRateCard(
        hearRate = 100,
        title = "Heart rate monitor"
    )
}
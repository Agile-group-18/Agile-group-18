package org.grupp18.sortsmart

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.History
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.Alignment

@Composable
fun SearchScreen(
    onClose: () -> Unit
) {
    var searchText by remember { mutableStateOf("") }
    var suggestions by remember { mutableStateOf<List<SearchItem>>(emptyList()) }
    var selectedItem by remember { mutableStateOf<SearchItem?>(null) }

    // Call backend when user types
    LaunchedEffect(searchText) {
        if (searchText.isBlank()) {
            suggestions = emptyList()
        } else {
            try {
                suggestions = ItemRetrofitClient.apiService.searchItems(searchText).results
            } catch (e: Exception) {
                suggestions = emptyList()
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = searchText,
                onValueChange = {
                    searchText = it
                    selectedItem = null
                },
                placeholder = { Text("What do you want to recycle?") },
                modifier = Modifier.weight(1f),
                singleLine = true
            )

            IconButton(onClick = onClose) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close"
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text("Suggested")

        Spacer(modifier = Modifier.height(12.dp))

        val itemsToShow = suggestions

        itemsToShow.forEach { item ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        searchText = item.name
                        selectedItem = item
                    }
                    .padding(vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.History,
                    contentDescription = null
                )

                Spacer(modifier = Modifier.width(12.dp))

                Text(item.name)
            }
        }

        // Show category and button after select
        selectedItem?.let { item ->
            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Category: ${item.category?.name ?: "Unknown"}",
                style = MaterialTheme.typography.bodyLarge
            )

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = {
                    println("${item.name} added to waste basket")
                }
            ) {
                Text("Add to waste basket")
            }
        }
    }
}
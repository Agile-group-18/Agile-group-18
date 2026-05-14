package org.grupp18.sortsmart

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import coil.ImageLoader
import coil.compose.AsyncImage
import coil.decode.SvgDecoder
import coil.request.ImageRequest
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun SearchScreen(
    modifier: Modifier = Modifier,
    onClose: () -> Unit
) {
    var searchText by remember { mutableStateOf("") }
    var suggestions by remember { mutableStateOf<List<SearchItem>>(emptyList()) }
    var selectedItem by remember { mutableStateOf<ItemDetail?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showSuggestions by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // Image loader with SVG support for category icons
    val imageLoader = remember {
        ImageLoader.Builder(context)
            .components {
                add(SvgDecoder.Factory())
            }
            .build()
    }

    // Loads full item details when the user selects a search result
    fun selectItem(item: SearchItem) {
        searchText = item.name
        suggestions = emptyList()
        showSuggestions = false
        selectedItem = null

        scope.launch {
            try {
                selectedItem = ItemRetrofitClient.apiService.getItemBySlug(item.slug)
                errorMessage = null
            } catch (e: Exception) {
                errorMessage = e.localizedMessage ?: "Could not load item details"
            }
        }
    }

    // Updates search suggestions when the search text changes
    LaunchedEffect(searchText) {
        if (
            searchText.isBlank() ||
            searchText.length < 2 ||
            selectedItem?.name == searchText
        ) {
            suggestions = emptyList()
            errorMessage = null
            return@LaunchedEffect
        }

        delay(400)

        try {
            val response = ItemRetrofitClient.apiService.searchItems(searchText)
            suggestions = response.results
            showSuggestions = true
            errorMessage = null
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            e.printStackTrace()
            errorMessage = e.localizedMessage ?: "Unknown error"
            suggestions = emptyList()
        }
    }

    // Main layout for search input, suggestions, errors, and item details
    Column(
        modifier = modifier
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
                    showSuggestions = true
                },
                placeholder = { Text("What do you want to recycle?") },
                modifier = Modifier.weight(1f),
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    imeAction = ImeAction.Search
                ),
                keyboardActions = KeyboardActions(
                    onSearch = {
                        if (suggestions.isNotEmpty()) {
                            selectItem(suggestions.first())
                        }
                    }
                )
            )

            IconButton(onClick = onClose) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close"
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        errorMessage?.let {
            Text(
                text = "Error: $it",
                color = MaterialTheme.colorScheme.error
            )
            Spacer(modifier = Modifier.height(12.dp))
        }

        if (showSuggestions && suggestions.isNotEmpty() && selectedItem == null) {
            Text(
                text = "Suggested",
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(modifier = Modifier.height(12.dp))

            suggestions.forEach { item ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            selectItem(item)
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
        }

        selectedItem?.let { item ->
            Spacer(modifier = Modifier.height(24.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                elevation = CardDefaults.cardElevation(4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .padding(4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            AsyncImage(
                                model = ImageRequest.Builder(context)
                                    .data(item.category?.imageUrl)
                                    .crossfade(true)
                                    .build(),
                                imageLoader = imageLoader,
                                contentDescription = item.category?.name,
                                modifier = Modifier.size(48.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Text(
                            text = item.name,
                            style = MaterialTheme.typography.titleLarge
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text("Category", style = MaterialTheme.typography.titleSmall)
                    Text(item.category?.name ?: "Unknown")

                    Spacer(modifier = Modifier.height(12.dp))

                    Text("Leave at", style = MaterialTheme.typography.titleSmall)
                    Text(item.leaveAt ?: "Unknown")

                    Spacer(modifier = Modifier.height(12.dp))

                    Text("Processing", style = MaterialTheme.typography.titleSmall)
                    Text(item.processing ?: "No information available")

                    Spacer(modifier = Modifier.height(20.dp))

                    Button(
                        onClick = {
                            println("${item.name} added to waste basket")
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF2E7D32),
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text("Add to waste basket")
                    }
                }
            }
        }
    }
}
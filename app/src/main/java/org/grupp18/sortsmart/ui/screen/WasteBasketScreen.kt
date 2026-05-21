package org.grupp18.sortsmart.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.ImageLoader
import coil.compose.AsyncImage
import coil.decode.SvgDecoder
import coil.request.ImageRequest
import org.grupp18.sortsmart.data.model.ItemDetail

@Composable
fun WasteBasketScreen(
    items: List<ItemDetail>,
    isCalculatingRoute: Boolean,
    onDiscard: (ItemDetail) -> Unit,
    onShowRouteFewestStops: () -> Unit,
    onShowRouteShortest: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(20.dp)
    ) {

        Text(
            text = "Your Wastebasket",
            style = MaterialTheme.typography.headlineSmall
        )

        Spacer(modifier = Modifier.height(20.dp))

        if (items.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text("Your waste basket is empty")
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(items) { item ->
                    WasteBasketCard(
                        item = item,
                        onDiscard = {
                            onDiscard(item)
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))


        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = onShowRouteFewestStops,
                modifier = Modifier
                    .weight(1f)
                    .height(56.dp),
                enabled = !isCalculatingRoute && items.isNotEmpty(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF2E7D32),
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                if (isCalculatingRoute) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Text("Fewest Stops")
                }
            }

            OutlinedButton(
                onClick = onShowRouteShortest,
                modifier = Modifier
                    .weight(1f)
                    .height(56.dp),
                enabled = !isCalculatingRoute && items.isNotEmpty(),
                shape = RoundedCornerShape(16.dp)
            ) {
                if (isCalculatingRoute) {
                    CircularProgressIndicator(color = Color(0xFF2E7D32), modifier = Modifier.size(24.dp))
                } else {
                    Text("Shortest Route", color = Color(0xFF2E7D32))
                }
            }
        }

    }
}

@Composable
fun WasteBasketCard(
    item: ItemDetail,
    onDiscard: () -> Unit
) {
    val context = LocalContext.current

    val imageLoader = remember {
        ImageLoader.Builder(context)
            .components {
                add(SvgDecoder.Factory())
            }
            .build()
    }

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

            Text(
                "Category",
                style = MaterialTheme.typography.titleSmall
            )
            Text(item.category?.name ?: "Unknown")

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                "Leave at",
                style = MaterialTheme.typography.titleSmall
            )
            Text(item.leaveAt ?: "Unknown")

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                "Processing",
                style = MaterialTheme.typography.titleSmall
            )
            Text(item.processing ?: "No information available")

            Spacer(modifier = Modifier.height(20.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                IconButton(
                    onClick = onDiscard
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Remove from waste basket",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}
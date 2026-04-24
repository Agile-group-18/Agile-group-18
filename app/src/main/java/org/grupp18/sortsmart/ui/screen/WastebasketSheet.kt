package org.grupp18.sortsmart.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val GreenDark     = Color(0xFF2D5A1B)
private val GreenMedium   = Color(0xFF386B21)
private val GreenLight    = Color(0xFFD4E8C2)
private val NeutralSurface = Color(0xFFF5F5F0)

// Simple data model — replace with your real model later
data class WasteItem(val id: Int, val name: String, val category: String)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WastebasketSheet(
    onDismiss: () -> Unit,
    onAddMoreItems: () -> Unit,   // hook to search/scan screen
    onStartRecycling: () -> Unit,
) {
    // In-memory item list — swap with ViewModel state later
    var items by remember {
        mutableStateOf(
            listOf(
                WasteItem(1, "paper", "Paper"),
                WasteItem(2, "plastic bottle", "Plastic"),
                WasteItem(3, "glass jar", "Glass"),
            )
        )
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        // Matches the beige page background
        containerColor = NeutralSurface,
        // Large top corners like in the design
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        // Drag handle is shown by default — matches the gray pill in the design
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(top = 12.dp, bottom = 4.dp)
                    .width(40.dp)
                    .height(4.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFCCCCC4))
            )
        },
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .navigationBarsPadding()
                .padding(bottom = 24.dp)
        ) {
            // ── Header row ───────────────────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Delete,
                        contentDescription = null,
                        tint = Color(0xFF3A3D35),
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "Your Wastebasket",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1A1C17)
                    )
                }
                // Item count badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50.dp))
                        .background(GreenMedium)
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "${items.size} ITEMS",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        letterSpacing = 0.5.sp
                    )
                }
            }

            // ── Item list ────────────────────────────────────────────────────
            if (items.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No items yet — scan something!",
                        color = Color(0xFF888880),
                        fontSize = 14.sp
                    )
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 320.dp) // limits height so buttons are always visible
                ) {
                    items(items, key = { it.id }) { item ->
                        WasteItemRow(
                            item = item,
                            onDelete = { items = items.filter { it.id != item.id } }
                        )
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            // ── Add More Items button ─────────────────────────────────────────
            Button(
                onClick = onAddMoreItems,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(50.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = GreenLight,
                    contentColor = Color(0xFF1A1C17)
                ),
                elevation = ButtonDefaults.buttonElevation(0.dp)
            ) {
                Icon(
                    Icons.Outlined.Add,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "Add More Items",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(Modifier.height(10.dp))

            // ── Start Recycling button ────────────────────────────────────────
            Button(
                onClick = onStartRecycling,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(50.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = GreenDark,
                    contentColor = Color.White
                ),
                elevation = ButtonDefaults.buttonElevation(0.dp)
            ) {
                Icon(
                    Icons.Filled.Send,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "Start Recycling",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

// ── Single item row ───────────────────────────────────────────────────────────
@Composable
private fun WasteItemRow(
    item: WasteItem,
    onDelete: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White)
            .padding(horizontal = 14.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Category icon pill
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(GreenLight),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.Description,
                    contentDescription = null,
                    tint = GreenMedium,
                    modifier = Modifier.size(20.dp)
                )
            }
            Column {
                Text(
                    text = item.name,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF1A1C17)
                )
                Text(
                    text = item.category,
                    fontSize = 13.sp,
                    color = Color(0xFF888880)
                )
            }
        }
        // Delete button
        IconButton(onClick = onDelete) {
            Icon(
                imageVector = Icons.Outlined.Delete,
                contentDescription = "Remove",
                tint = Color(0xFF888880),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
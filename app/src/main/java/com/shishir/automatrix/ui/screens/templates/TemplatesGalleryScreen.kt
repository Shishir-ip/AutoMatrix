package com.shishir.automatrix.ui.screens.templates

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shishir.automatrix.domain.models.Routine
import com.shishir.automatrix.domain.templates.RoutineTemplate
import com.shishir.automatrix.domain.templates.TemplateCatalog
import com.shishir.automatrix.ui.components.FlowGlassCard
import com.shishir.automatrix.ui.components.FlowGradientButton
import com.shishir.automatrix.ui.components.IconHelper
import com.shishir.automatrix.ui.theme.LocalFlowGlassColors
import com.shishir.automatrix.ui.theme.StatusSuccess

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TemplatesGalleryScreen(
    onNavigateBack: () -> Unit,
    onImportTemplate: (Routine) -> Unit,
    onCustomizeTemplate: (Routine) -> Unit
) {
    val glassColors = LocalFlowGlassColors.current
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("All") }
    var previewTemplate by remember { mutableStateOf<RoutineTemplate?>(null) }
    val previewSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val categories = listOf("All", "Flagship", "Lifestyle", "Travel", "Power", "Work", "Alerts", "Audio")

    val templates = remember { TemplateCatalog.allTemplates }

    val filteredTemplates = remember(searchQuery, selectedCategory) {
        templates.filter { template ->
            val matchesCategory = when (selectedCategory) {
                "All" -> true
                "Flagship" -> template.tags.contains("Flagship")
                else -> template.category.equals(selectedCategory, ignoreCase = true)
            }
            val matchesSearch = searchQuery.isBlank() ||
                    template.name.contains(searchQuery, ignoreCase = true) ||
                    template.description.contains(searchQuery, ignoreCase = true) ||
                    template.tags.any { it.contains(searchQuery, ignoreCase = true) }

            matchesCategory && matchesSearch
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onNavigateBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = glassColors.textPrimary
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "Routine Templates",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = glassColors.textPrimary
                    )
                    Text(
                        text = "16 ready-to-use flagship automations",
                        style = MaterialTheme.typography.bodySmall,
                        color = glassColors.textSecondary
                    )
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Search field
            item {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search templates (e.g. Sleep, Driving, Battery)") },
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = null, tint = LocalFlowGlassColors.current.accent)
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Close, contentDescription = "Clear", tint = glassColors.textSecondary)
                            }
                        }
                    },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = LocalFlowGlassColors.current.accent,
                        unfocusedBorderColor = glassColors.glassBorder,
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent
                    )
                )
            }

            // Category Filter Pills
            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(categories, key = { it }) { category ->
                        val isSelected = selectedCategory == category
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isSelected) LocalFlowGlassColors.current.accent else glassColors.glassSurface)
                                .border(1.dp, if (isSelected) LocalFlowGlassColors.current.accent else glassColors.glassBorder, RoundedCornerShape(12.dp))
                                .clickable { selectedCategory = category }
                                .padding(horizontal = 14.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = category,
                                fontSize = 13.sp,
                                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                                color = if (isSelected) {
                                    if (glassColors.accent == Color.White) Color.Black else Color.White
                                } else glassColors.textPrimary
                            )
                        }
                    }
                }
            }

            // Template cards
            items(filteredTemplates, key = { it.id }, contentType = { "template_card" }) { template ->
                val templateColor = LocalFlowGlassColors.current.accent

                FlowGlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    onClick = { previewTemplate = template }
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(46.dp)
                                    .clip(CircleShape)
                                    .background(templateColor.copy(alpha = 0.12f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = IconHelper.getIconByName(template.iconName),
                                    contentDescription = null,
                                    tint = templateColor,
                                    modifier = Modifier.size(24.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(14.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = template.name,
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                        color = glassColors.textPrimary
                                    )
                                    if (template.tags.contains("Flagship")) {
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(LocalFlowGlassColors.current.accent.copy(alpha = 0.2f))
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                text = "TESTED",
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = LocalFlowGlassColors.current.accent
                                            )
                                        }
                                    }
                                }
                                Text(
                                    text = "${template.conditions.size} WHEN • ${template.actions.size} THEN",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = glassColors.textSecondary
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = template.description,
                            style = MaterialTheme.typography.bodyMedium,
                            color = glassColors.textSecondary,
                            lineHeight = 20.sp
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(glassColors.glassSurface)
                                    .border(1.dp, glassColors.glassBorder, RoundedCornerShape(12.dp))
                                    .clickable { previewTemplate = template }
                                    .padding(horizontal = 14.dp, vertical = 8.dp)
                            ) {
                                Text(
                                    text = "Preview & Customize",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = LocalFlowGlassColors.current.accent
                                )
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(templateColor)
                                    .clickable {
                                        val routine = template.toRoutine()
                                        onImportTemplate(routine)
                                    }
                                    .padding(horizontal = 14.dp, vertical = 8.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        tint = if (templateColor == Color.White) Color.Black else Color.White,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Use Routine",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = if (templateColor == Color.White) Color.Black else Color.White
                                    )
                                }
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(30.dp))
            }
        }
    }

    // Preview BottomSheet
    previewTemplate?.let { template ->
        ModalBottomSheet(
            onDismissRequest = { previewTemplate = null },
            sheetState = previewSheetState,
            containerColor = glassColors.glassSurface,
            shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = template.name,
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = glassColors.textPrimary
                    )
                    IconButton(onClick = { previewTemplate = null }) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = glassColors.textSecondary)
                    }
                }

                Text(
                    text = template.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = glassColors.textSecondary
                )

                Spacer(modifier = Modifier.height(16.dp))

                // WHEN triggers
                Text(
                    text = "WHEN (Conditions)",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = LocalFlowGlassColors.current.accent
                )
                Spacer(modifier = Modifier.height(8.dp))
                template.conditions.forEach { cond ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = IconHelper.getIconByName(cond.type.iconName),
                            contentDescription = null,
                            tint = LocalFlowGlassColors.current.accent,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = cond.title,
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = glassColors.textPrimary
                            )
                            Text(
                                text = cond.summary,
                                style = MaterialTheme.typography.bodySmall,
                                color = glassColors.textSecondary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // THEN actions
                Text(
                    text = "THEN (Actions)",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = LocalFlowGlassColors.current.accent
                )
                Spacer(modifier = Modifier.height(8.dp))
                template.actions.forEach { act ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = IconHelper.getIconByName(act.type.iconName),
                            contentDescription = null,
                            tint = LocalFlowGlassColors.current.accent,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = act.title,
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = glassColors.textPrimary
                            )
                            Text(
                                text = act.summary + if (act.restoreOnExit) " • (Reverts on exit)" else "",
                                style = MaterialTheme.typography.bodySmall,
                                color = glassColors.textSecondary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(16.dp))
                            .background(glassColors.glassSurface)
                            .border(1.dp, glassColors.glassBorder, RoundedCornerShape(16.dp))
                            .clickable {
                                val routine = template.toRoutine()
                                previewTemplate = null
                                onCustomizeTemplate(routine)
                            }
                            .padding(vertical = 14.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Customize in Builder",
                            fontWeight = FontWeight.SemiBold,
                            color = LocalFlowGlassColors.current.accent
                        )
                    }

                    FlowGradientButton(
                        text = "1-Tap Import",
                        onClick = {
                            val routine = template.toRoutine()
                            previewTemplate = null
                            onImportTemplate(routine)
                        },
                        modifier = Modifier.weight(1f),
                        icon = Icons.Default.Check
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

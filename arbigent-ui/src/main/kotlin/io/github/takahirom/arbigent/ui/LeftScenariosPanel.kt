package io.github.takahirom.arbigent.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import io.github.takahirom.arbigent.ArbigentTag
import io.github.takahirom.arbigent.ui.ArbigentAppStateHolder.ProjectDialogState
import org.jetbrains.jewel.foundation.theme.JewelTheme
import org.jetbrains.jewel.ui.component.*
import org.jetbrains.jewel.ui.icons.AllIconsKeys
import org.jetbrains.jewel.ui.painter.hints.Size
import org.jetbrains.jewel.ui.theme.colorPalette

@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun LeftScenariosPanel(
  scenarioAndDepths: List<Pair<ArbigentScenarioStateHolder, Int>>,
  scenariosWidth: Dp,
  selectedScenarioIndex: Int,
  appStateHolder: ArbigentAppStateHolder
) {
  val colors = LocalPremiumColors.current
  val typography = LocalPremiumTypography.current
  // Map to manage expanded/collapsed state (scenario holder -> expanded state)
  val expandedStates = remember { mutableStateMapOf<ArbigentScenarioStateHolder, Boolean>() }

  Column(
    modifier = Modifier
      .run {
        if (scenarioAndDepths.isEmpty()) {
          fillMaxSize()
        } else {
          width(scenariosWidth)
        }
      }
      .background(colors.background)
      .drawBehind {
        drawLine(
          color = colors.border,
          start = Offset(size.width, 0f),
          end = Offset(size.width, size.height),
          strokeWidth = 1.dp.toPx()
        )
      }
  ) {
    // Elegant Panel Header with Actions
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 12.dp, vertical = 10.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.SpaceBetween
    ) {
      Text(
        text = "Scenarios",
        style = typography.header1.copy(fontWeight = FontWeight.Bold),
        color = colors.textPrimary
      )
      Row(
        horizontalArrangement = Arrangement.spacedBy(6.dp)
      ) {
        IconActionButton(
          key = AllIconsKeys.FileTypes.AddAny,
          onClick = {
            appStateHolder.addScenario()
          },
          contentDescription = "Add",
          hint = Size(20)
        ) {
          Text("Add scenario", style = typography.body)
        }

        IconActionButton(
          key = AllIconsKeys.Diff.MagicResolve,
          onClick = {
            appStateHolder.projectDialogState.value = ProjectDialogState.ShowGenerateScenarioDialog
          },
          contentDescription = "Generate",
          hint = Size(20)
        ) {
          Text("Generate scenario", style = typography.body)
        }
      }
    }

    Box(modifier = Modifier.weight(1f)) {
      // Background Depth Guide lines - vertical tracks showing indentation hierarchy
      if (scenarioAndDepths.isNotEmpty()) {
        val maxDepth = scenarioAndDepths.maxOf { it.second }
        for (i in 0..maxDepth) {
          Box(
            modifier = Modifier
              .padding(start = 12.dp + 16.dp * i)
              .fillMaxHeight()
              .width(1.dp)
              .background(colors.border)
          )
        }
      }

      val lazyColumnState = rememberLazyListState()

      // Pre-compute visibility using derivedStateOf to avoid recomputation
      val visibleIndices by remember(scenarioAndDepths, expandedStates) {
        derivedStateOf {
          val indices = mutableSetOf<Int>()
          val ancestorStack = mutableListOf<Pair<Int, ArbigentScenarioStateHolder>>()

          scenarioAndDepths.forEachIndexed { index, (scenarioHolder, depth) ->
            // Pop ancestors that are at same or deeper level
            while (ancestorStack.isNotEmpty()) {
              val (ancestorIndex, _) = ancestorStack.last()
              if (scenarioAndDepths[ancestorIndex].second >= depth) {
                ancestorStack.removeLast()
              } else {
                break
              }
            }

            // Check if all ancestors are expanded
            val allAncestorsExpanded = ancestorStack.all { (_, ancestorHolder) ->
              expandedStates.getOrDefault(ancestorHolder, true)
            }

            if (allAncestorsExpanded) {
              indices.add(index)
            }

            // Add current item to ancestor stack for its potential children
            ancestorStack.add(index to scenarioHolder)
          }
          indices
        }
      }

      LazyColumn(
        state = lazyColumnState,
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(vertical = 8.dp)
      ) {
        itemsIndexed(scenarioAndDepths) { index, (scenarioStateHolder, depth) ->
          if (!visibleIndices.contains(index)) {
            return@itemsIndexed
          }

          // Check if this scenario has children (next item has greater depth)
          val hasChildren = index < scenarioAndDepths.size - 1 &&
            scenarioAndDepths[index + 1].second > depth

          val isSelected = index == selectedScenarioIndex
          val goal = scenarioStateHolder.goalState.text

          // Enhanced Scenario list item card
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .padding(
                start = 16.dp + 16.dp * depth,
                end = 12.dp,
                top = 2.dp,
                bottom = 2.dp
              )
              .shadow(
                elevation = if (isSelected) 2.dp else 0.dp,
                shape = RoundedCornerShape(8.dp)
              )
              .clip(RoundedCornerShape(8.dp))
              .background(if (isSelected) colors.surfaceHover else colors.surface)
              .border(
                width = 1.dp,
                color = if (isSelected) colors.primary.copy(alpha = 0.5f) else colors.border,
                shape = RoundedCornerShape(8.dp)
              )
              .clickable { appStateHolder.selectedScenarioIndex.value = index }
              .padding(8.dp)
          ) {
            Column {
              Row(
                verticalAlignment = Alignment.CenterVertically
              ) {
                // Show expand/collapse button if this scenario has children
                if (hasChildren) {
                  val isExpanded = expandedStates.getOrDefault(scenarioStateHolder, true)
                  val rotationAngle by animateFloatAsState(if (isExpanded) 0f else -90f)
                  IconActionButton(
                    onClick = {
                      expandedStates[scenarioStateHolder] = !isExpanded
                    },
                    key = AllIconsKeys.General.ChevronDown,
                    contentDescription = if (isExpanded) "Collapse" else "Expand",
                    hint = Size(14),
                    modifier = Modifier
                      .rotate(rotationAngle)
                      .padding(end = 4.dp)
                  )
                } else {
                  Spacer(modifier = Modifier.width(18.dp))
                }

                val runningInfo by scenarioStateHolder.arbigentScenarioRunningInfo.collectAsState()
                val scenarioType by scenarioStateHolder.scenarioTypeStateFlow.collectAsState()
                val isRunning by scenarioStateHolder.isRunning.collectAsState()
                val isAchieved by scenarioStateHolder.isAchieved.collectAsState()
                val isNewlyGenerated by scenarioStateHolder.isNewlyGenerated.collectAsState()

                Column(modifier = Modifier.weight(1f)) {
                  val titleText = if (scenarioType.isScenario()) {
                    "Goal: $goal"
                  } else {
                    val scenarioId by scenarioStateHolder.idStateFlow.collectAsState()
                    "Execution: $scenarioId"
                  }
                  Text(
                    text = titleText,
                    style = typography.body.copy(
                      fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium
                    ),
                    color = if (isSelected) colors.primary else colors.textPrimary,
                    maxLines = 2
                  )
                  if (runningInfo?.toString().orEmpty().isNotEmpty()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                      text = runningInfo.toString(),
                      style = typography.body.copy(fontSize = 11.sp),
                      color = colors.textSecondary
                    )
                  }
                }

                // Dynamic Status / Action Badges
                if (isAchieved) {
                  PassedMark(Modifier.size(16.dp))
                }

                if (isNewlyGenerated) {
                  Icon(
                    key = AllIconsKeys.Diff.MagicResolve,
                    contentDescription = "Newly Generated",
                    modifier = Modifier
                      .size(16.dp)
                      .clip(CircleShape)
                      .background(colors.primary.copy(alpha = 0.15f))
                  )
                }

                if (isRunning) {
                  CircularProgressIndicator(
                    modifier = Modifier
                      .size(16.dp)
                      .testTag("scenario_running")
                  )
                }
              }

              val tags by scenarioStateHolder.tags.collectAsState()
              if (tags.isNotEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                TagsList(
                  tags = tags,
                  colors = colors,
                  typography = typography,
                  onTagAdded = { scenarioStateHolder.addTag() },
                  onTagRemoved = { scenarioStateHolder.removeTag(it) },
                  onTagChanged = { tag, newName -> scenarioStateHolder.onTagChanged(tag, newName) }
                )
              }
            }
          }
        }
      }

      if (scenarioAndDepths.isEmpty()) {
        Box(
          modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
          contentAlignment = Alignment.Center
        ) {
          DefaultButton(
            modifier = Modifier.fillMaxWidth(0.8f),
            onClick = {
              appStateHolder.addScenario()
            }
          ) {
            Text("Add your first scenario", style = typography.body)
          }
        }
      }
    }
  }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TagsList(
  tags: Set<ArbigentTag>,
  colors: PremiumColors,
  typography: PremiumTypography,
  onTagAdded: () -> Unit,
  onTagRemoved: (String) -> Unit,
  onTagChanged: (String, String) -> Unit
) {
  FlowRow(
    horizontalArrangement = Arrangement.spacedBy(4.dp),
    verticalArrangement = Arrangement.spacedBy(4.dp),
    modifier = Modifier.padding(start = 18.dp, top = 2.dp)
  ) {
    tags.forEach { tag ->
      val tagName by tag.nameStateFlow.collectAsState()
      TagChip(
        tagName = tagName,
        colors = colors,
        typography = typography,
        onTagRemoved = onTagRemoved,
        onTagChanged = onTagChanged
      )
    }
    IconActionButton(
      onClick = onTagAdded,
      key = AllIconsKeys.General.Add,
      contentDescription = "Add tag",
      hint = Size(10),
      modifier = Modifier.size(16.dp)
    )
  }
}

@Composable
fun TagChip(
  tagName: String,
  colors: PremiumColors,
  typography: PremiumTypography,
  onTagRemoved: (String) -> Unit,
  onTagChanged: (String, String) -> Unit
) {
  var isEditing by remember { mutableStateOf(false) }
  Box(
    modifier = Modifier
      .clip(RoundedCornerShape(4.dp))
      .background(colors.primary.copy(alpha = 0.12f))
      .border(width = 0.5.dp, color = colors.primary.copy(alpha = 0.3f), shape = RoundedCornerShape(4.dp))
      .padding(horizontal = 6.dp, vertical = 2.dp)
  ) {
    if (isEditing) {
      val state = rememberTextFieldState(tagName)
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
      ) {
        TextField(
          state = state,
          modifier = Modifier
            .width(80.dp)
            .onKeyEvent { event ->
              if (event.key == Key.Enter) {
                onTagChanged(tagName, state.text.toString())
                isEditing = false
                true
              } else false
            },
          onKeyboardAction = {
            onTagChanged(tagName, state.text.toString())
            isEditing = false
          }
        )
        IconActionButton(
          onClick = {
            onTagRemoved(tagName)
            isEditing = false
          },
          key = AllIconsKeys.General.Remove,
          contentDescription = "Remove tag",
          hint = Size(10),
          modifier = Modifier.size(16.dp)
        )
      }
    } else {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
      ) {
        Text(
          text = tagName,
          style = typography.body.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold),
          color = colors.primary,
          modifier = Modifier.clickable { isEditing = true }
        )
        IconActionButton(
          onClick = { onTagRemoved(tagName) },
          key = AllIconsKeys.General.Close,
          contentDescription = "Remove tag",
          hint = Size(8),
          modifier = Modifier.size(12.dp)
        )
      }
    }
  }
}

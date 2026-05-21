package io.github.takahirom.arbigent.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.onClick
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.loadImageBitmap
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import io.github.takahirom.arbigent.*
import io.github.takahirom.arbigent.result.ArbigentScenarioDeviceFormFactor
import io.github.takahirom.arbigent.ui.components.AiOptionsComponent
import io.github.takahirom.arbigent.result.StepFeedback
import io.github.takahirom.arbigent.result.StepFeedbackEvent
import org.jetbrains.jewel.foundation.theme.JewelTheme
import org.jetbrains.jewel.ui.Orientation
import org.jetbrains.jewel.ui.component.Checkbox
import org.jetbrains.jewel.ui.component.CheckboxRow
import org.jetbrains.jewel.ui.component.CircularProgressIndicator
import org.jetbrains.jewel.ui.component.Divider
import org.jetbrains.jewel.ui.component.Dropdown
import org.jetbrains.jewel.ui.component.GroupHeader
import org.jetbrains.jewel.ui.component.Icon
import org.jetbrains.jewel.ui.component.IconActionButton
import org.jetbrains.jewel.ui.component.MenuScope
import org.jetbrains.jewel.ui.component.OutlinedButton
import org.jetbrains.jewel.ui.component.RadioButtonRow
import org.jetbrains.jewel.ui.component.Text
import org.jetbrains.jewel.ui.component.TextArea
import org.jetbrains.jewel.ui.component.TextField
import org.jetbrains.jewel.ui.component.styling.GroupHeaderStyle
import org.jetbrains.jewel.ui.component.styling.LocalGroupHeaderStyle
import org.jetbrains.jewel.ui.icons.AllIconsKeys
import org.jetbrains.jewel.ui.painter.hints.Size
import org.jetbrains.jewel.ui.theme.colorPalette
import java.io.File
import java.io.FileInputStream


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun Scenario(
  scenarioStateHolder: ArbigentScenarioStateHolder,
  stepFeedbacks: Set<StepFeedback>,
  dependencyScenarioMenu: MenuScope.() -> Unit,
  onAddSubScenario: (ArbigentScenarioStateHolder) -> Unit,
  scenarioCountById: (String) -> Int,
  onStepFeedback: (StepFeedbackEvent) -> Unit,
  onExecute: (ArbigentScenarioStateHolder) -> Unit,
  onDebugExecute: (ArbigentScenarioStateHolder) -> Unit,
  onCancel: (ArbigentScenarioStateHolder) -> Unit,
  onRemove: (ArbigentScenarioStateHolder) -> Unit,
  onShowFixedScenariosDialog: (ArbigentScenarioStateHolder, Int) -> Unit = { _, _ -> },
  getFixedScenarioById: (String) -> FixedScenario? = { null },
  mcpServerNames: List<String> = emptyList(),
) {
  val colors = LocalPremiumColors.current
  val typography = LocalPremiumTypography.current
  val arbigentScenarioExecutor: ArbigentScenarioExecutor? by scenarioStateHolder.arbigentScenarioExecutorStateFlow.collectAsState()
  val scenarioType by scenarioStateHolder.scenarioTypeStateFlow.collectAsState()
  val isRunning by scenarioStateHolder.isRunning.collectAsState()
  val isAchieved by scenarioStateHolder.isAchieved.collectAsState()
  val goal = scenarioStateHolder.goalState
  var goalTextAreaHeight by remember { mutableStateOf(96.dp) }
  var removeDialogShowing by remember { mutableStateOf(false) }
  Column(
    modifier = Modifier
      .fillMaxSize()
      .background(colors.background)
      .padding(12.dp)
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .clip(RoundedCornerShape(10.dp))
        .background(colors.surface)
        .border(1.dp, colors.border, RoundedCornerShape(10.dp))
        .padding(12.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.SpaceBetween
    ) {
      Column(
        modifier = Modifier.weight(1f)
      ) {
        Text(
          text = if (scenarioType.isScenario()) "Scenario Inspector" else "Execution Inspector",
          style = typography.header1,
          color = colors.textPrimary
        )
        val statusText = when {
          isRunning -> "Running"
          isAchieved -> "Achieved"
          else -> "Ready"
        }
        val statusColor = when {
          isRunning -> colors.primary
          isAchieved -> colors.success
          else -> colors.textSecondary
        }
        Text(
          text = statusText,
          style = typography.body,
          color = statusColor
        )
      }
      Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        if (isRunning) {
          OutlinedButton(onClick = { onCancel(scenarioStateHolder) }) {
            Text("Cancel", color = colors.error)
          }
        }
        OutlinedButton(onClick = { onDebugExecute(scenarioStateHolder) }) {
          Text("Debug")
        }
        OutlinedButton(onClick = { onAddSubScenario(scenarioStateHolder) }) {
          Text("Add Sub")
        }
        OutlinedButton(onClick = { removeDialogShowing = true }) {
          Text("Delete", color = colors.error)
        }
        OutlinedButton(onClick = { onExecute(scenarioStateHolder) }) {
          Text("Run")
        }
      }
    }

    if (removeDialogShowing) {
      Dialog(
        onDismissRequest = { removeDialogShowing = false }
      ) {
        Column(
          modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(colors.surface)
            .border(1.dp, colors.border, RoundedCornerShape(8.dp))
            .padding(12.dp)
        ) {
          Text("Are you sure you want to remove this scenario?")
          Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(
              onClick = {
                removeDialogShowing = false
              }
            ) {
              Text("Cancel")
            }
            OutlinedButton(
              onClick = {
                removeDialogShowing = false
                onRemove(scenarioStateHolder)
              }
            ) {
              Text("Remove")
            }
          }
        }
      }
    }

    Spacer(modifier = Modifier.height(10.dp))

    Column(
      modifier = Modifier
        .fillMaxWidth()
        .clip(RoundedCornerShape(10.dp))
        .background(colors.surface)
        .border(1.dp, colors.border, RoundedCornerShape(10.dp))
        .padding(12.dp)
    ) {
      Text(
        text = "Goal",
        style = typography.header2,
        color = colors.primary
      )
      TextArea(
        modifier = Modifier
          .fillMaxWidth()
          .padding(top = 6.dp)
          .testTag("goal")
          .height(goalTextAreaHeight),
        enabled = scenarioType.isScenario(),
        state = goal,
        placeholder = { Text("Goal") },
        textStyle = JewelTheme.editorTextStyle,
        decorationBoxModifier = Modifier.padding(horizontal = 8.dp)
      )
      Divider(
        orientation = Orientation.Horizontal,
        modifier = Modifier
          .fillMaxWidth()
          .pointerHoverIcon(PointerIcon.Hand)
          .pointerInput(Unit) {
            detectDragGestures { change, dragAmount ->
              change.consume()
              goalTextAreaHeight = (goalTextAreaHeight + dragAmount.y.toDp()).coerceAtLeast(48.dp)
            }
          },
        thickness = 8.dp
      )
    }

    Spacer(modifier = Modifier.height(10.dp))

    BoxWithConstraints(
      modifier = Modifier
        .fillMaxWidth()
        .clip(RoundedCornerShape(10.dp))
        .background(colors.surface)
        .border(1.dp, colors.border, RoundedCornerShape(10.dp))
        .padding(12.dp)
    ) {
      val maxHeightValue = maxHeight
      ExpandableSection(
        title = "Options",
        defaultExpanded = false,
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(
          modifier = Modifier
            .testTag("scenario_options")
            .heightIn(max = maxHeightValue * 0.7f)
            .verticalScroll(rememberScrollState())
            .wrapContentHeight(unbounded = true)
        ) {
          ScenarioOptions(scenarioStateHolder, scenarioCountById, dependencyScenarioMenu, onShowFixedScenariosDialog, getFixedScenarioById, mcpServerNames)
        }
      }
    }

    Spacer(modifier = Modifier.height(10.dp))

    arbigentScenarioExecutor?.let { arbigentScenarioExecutor ->
      val taskToAgents: List<List<ArbigentTaskAssignment>> by arbigentScenarioExecutor.taskAssignmentsHistoryFlow.collectAsState(
        arbigentScenarioExecutor.taskAssignmentsHistory()
      )
      if (taskToAgents.isNotEmpty()) {
        ContentPanel(taskToAgents, stepFeedbacks, onStepFeedback, modifier = Modifier.weight(1f))
      }
    }
  }
}


@OptIn(ExperimentalLayoutApi::class, ExperimentalFoundationApi::class)
@Composable
private fun ScenarioFundamentalOptions(
  scenarioStateHolder: ArbigentScenarioStateHolder,
  scenarioCountById: (String) -> Int,
  dependencyScenarioMenu: MenuScope.() -> Unit
) {
  val updatedScenarioStateHolder by rememberUpdatedState(scenarioStateHolder)
  FlowRow {
    // ID
    Column(
      modifier = Modifier.padding(8.dp).widthIn(min = 80.dp, max = 400.dp).width(IntrinsicSize.Min)
    ) {
      GroupHeader("Scenario ID")
      val idTextFieldState = rememberTextFieldState(updatedScenarioStateHolder.id)
      var duplicated by remember { mutableStateOf(false) }
      LaunchedEffect(updatedScenarioStateHolder.id) {
        idTextFieldState.edit {
          replace(0, idTextFieldState.text.length, updatedScenarioStateHolder.id)
        }
      }
      LaunchedEffect(Unit) {
        snapshotFlow { idTextFieldState.text.toString() }.collect { textFieldScenarioId ->
          if (updatedScenarioStateHolder.id == textFieldScenarioId) {
            duplicated = false
          } else if (scenarioCountById(textFieldScenarioId) == 0 && textFieldScenarioId.isNotBlank()) {
            updatedScenarioStateHolder.onScenarioIdChanged(textFieldScenarioId)
            duplicated = false
          } else {
            duplicated = true
          }
        }
      }
      TextField(
        state = idTextFieldState,
        modifier = Modifier.padding(4.dp).testTag("scenario_id"),
        placeholder = { Text("Scenario ID") },
      )
      if (duplicated) {
        Text(
          text = "This id is already used.",
          color = Color.Red
        )
      }
    }
    // Note
    Column(
      modifier = Modifier.padding(8.dp).widthIn(min = 80.dp, max = 400.dp).width(IntrinsicSize.Min)
    ) {
      GroupHeader("Note for humans")
      TextArea(
        modifier = Modifier
          .padding(4.dp)
          .height(80.dp)
          .testTag("note_for_humans"),
        textStyle = JewelTheme.editorTextStyle,
        state = updatedScenarioStateHolder.noteForHumans,
        placeholder = { Text("Note for humans") },
        decorationBoxModifier = Modifier.padding(horizontal = 8.dp),
      )
    }
    // Dependency
    Column(
      modifier = Modifier.padding(8.dp).width(160.dp)
    ) {
      GroupHeader("Scenario dependency")
      val dependency by updatedScenarioStateHolder.dependencyScenarioStateHolderStateFlow.collectAsState()

      Dropdown(
        modifier = Modifier
          .testTag("dependency_dropdown")
          .padding(4.dp),
        menuContent = dependencyScenarioMenu
      ) {
        Text(dependency?.goal ?: "Select dependency")
      }
    }
    // Scenario type
    Column(
      modifier = Modifier.padding(8.dp).width(160.dp)
    ) {
      GroupHeader {
        Text("Scenario type")
        IconActionButton(
          key = AllIconsKeys.General.Information,
          onClick = {},
          contentDescription = "Scenario type",
          hint = Size(16),
        ) {
          Text(
            text = "Scenario: The agent will try to achieve the goal. \n" +
              "Execution: Just execute the initializations and image assertions.",
          )
        }
      }
      val inputActionType by updatedScenarioStateHolder.scenarioTypeStateFlow.collectAsState()
      Row(
        verticalAlignment = Alignment.CenterVertically
      ) {
        RadioButtonRow(
          text = "Scenario",
          selected = inputActionType == ArbigentScenarioType.Scenario,
          onClick = {
            updatedScenarioStateHolder.scenarioTypeStateFlow.value = ArbigentScenarioType.Scenario
          }
        )
      }
      Row(
        verticalAlignment = Alignment.CenterVertically
      ) {
        RadioButtonRow(
          text = "Execution",
          selected = inputActionType == ArbigentScenarioType.Execution,
          onClick = {
            updatedScenarioStateHolder.scenarioTypeStateFlow.value = ArbigentScenarioType.Execution
          }
        )
      }
    }
    // Form factor
    Column(
      modifier = Modifier.padding(8.dp).width(160.dp)
    ) {
      val inputActionType by updatedScenarioStateHolder.deviceFormFactorStateFlow.collectAsState()
      GroupHeader("Device form factors")
      Row(
        verticalAlignment = Alignment.CenterVertically
      ) {
        RadioButtonRow(
          text = "Mobile",
          selected = inputActionType.isMobile(),
          onClick = {
            updatedScenarioStateHolder.deviceFormFactorStateFlow.value =
              ArbigentScenarioDeviceFormFactor.Mobile
          }
        )
      }
      Row(
        verticalAlignment = Alignment.CenterVertically
      ) {
        RadioButtonRow(
          text = "TV",
          selected = inputActionType.isTv(),
          onClick = {
            updatedScenarioStateHolder.deviceFormFactorStateFlow.value =
              ArbigentScenarioDeviceFormFactor.Tv
          }
        )
      }
      Row(
        verticalAlignment = Alignment.CenterVertically
      ) {
        RadioButtonRow(
          text = "Unspecified",
          selected = inputActionType.isUnspecified(),
          onClick = {
            updatedScenarioStateHolder.deviceFormFactorStateFlow.value =
              ArbigentScenarioDeviceFormFactor.Unspecified
          }
        )
      }
    }
    // Max retry and step count
    Column(
      modifier = Modifier.padding(8.dp).width(80.dp)
    ) {
      GroupHeader("Max retry count")
      Row(
        verticalAlignment = Alignment.CenterVertically
      ) {
        TextField(
          state = updatedScenarioStateHolder.maxRetryState,
          keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
          modifier = Modifier
            .padding(4.dp),
        )
      }
      GroupHeader("Max step count")
      Row(
        verticalAlignment = Alignment.CenterVertically
      ) {
        TextField(
          modifier = Modifier
            .padding(4.dp),
          state = updatedScenarioStateHolder.maxStepState,
          keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        )
      }
    }
  }
}

@OptIn(ExperimentalLayoutApi::class, ExperimentalFoundationApi::class)
@Composable
private fun ScenarioOptions(
  scenarioStateHolder: ArbigentScenarioStateHolder,
  scenarioCountById: (String) -> Int,
  dependencyScenarioMenu: MenuScope.() -> Unit,
  onShowFixedScenariosDialog: (ArbigentScenarioStateHolder, Int) -> Unit = { _, _ -> },
  getFixedScenarioById: (String) -> FixedScenario? = { null },
  mcpServerNames: List<String> = emptyList()
) {
  val updatedScenarioStateHolder by rememberUpdatedState(scenarioStateHolder)
  GroupHeader("Fundamental options")
  ScenarioFundamentalOptions(scenarioStateHolder, scenarioCountById, dependencyScenarioMenu)
  GroupHeader("Other options")
  FlowRow(modifier = Modifier.padding(4.dp)) {
    Column(
      modifier = Modifier.padding(8.dp).width(240.dp)
    ) {
      GroupHeader("AI Options")
      val aiOptions by updatedScenarioStateHolder.aiOptionsFlow.collectAsState()
      val currentOptions = aiOptions ?: ArbigentAiOptions()
      AiOptionsComponent(
        currentOptions = currentOptions,
        onOptionsChanged = updatedScenarioStateHolder::onAiOptionsChanged
      )
    }
    Column(
      modifier = Modifier.padding(8.dp).width(160.dp)
    ) {
      GroupHeader("Cache Options")
      val cacheOptions by updatedScenarioStateHolder.cacheOptionsFlow.collectAsState()
      Column {
        CheckboxRow(
          modifier = Modifier.padding(start = 16.dp),
          text = "Force disable Cache for this scenario",
          checked = cacheOptions?.forceCacheDisabled == true,
          onCheckedChange = { disabled ->
            updatedScenarioStateHolder.onOverrideCacheForceDisabledChanged(disabled)
          }
        )
      }
    }
    if (mcpServerNames.isNotEmpty()) {
      Column(
        modifier = Modifier.padding(8.dp).width(240.dp)
      ) {
        GroupHeader("MCP Options")
        val mcpOptions by updatedScenarioStateHolder.mcpOptionsFlow.collectAsState()
        io.github.takahirom.arbigent.ui.components.McpOptionsComponent(
          currentOptions = mcpOptions,
          availableServers = mcpServerNames,
          onOptionsChanged = updatedScenarioStateHolder::onMcpOptionsChanged
        )
      }
    }
    Column(
      modifier = Modifier.padding(8.dp).width(240.dp)
    ) {
      GroupHeader("Additional Actions")
      val additionalActions by updatedScenarioStateHolder.additionalActionsFlow.collectAsState()
      val currentActions = additionalActions ?: emptyList()

      Column {
        AdditionalActionsConstants.AVAILABLE_ACTIONS.forEach { actionName ->
          Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(vertical = 2.dp)
          ) {
            Checkbox(
              checked = currentActions.contains(actionName),
              onCheckedChange = { isChecked ->
                val updatedActions = if (isChecked) {
                  currentActions + actionName
                } else {
                  currentActions - actionName
                }
                updatedScenarioStateHolder.onAdditionalActionsChanged(
                  if (updatedActions.isEmpty()) null else updatedActions
                )
              },
              modifier = Modifier.testTag("scenario_additional_action_$actionName")
            )
            Text(
              text = actionName,
              modifier = Modifier.padding(start = 8.dp)
            )
          }
        }
      }
    }
    Column(
      modifier = Modifier.padding(8.dp).width(240.dp)
    ) {
      GroupHeader {
        Text("Initialization methods")
        // Add button
        IconActionButton(
          key = AllIconsKeys.General.Add,
          onClick = {
            updatedScenarioStateHolder.onAddInitializationMethod()
          },
          contentDescription = "Add initialization method",
          hint = Size(16),
        ) {
          Text(
            text = "Add initialization method",
          )
        }
      }
    }
    val initializeMethods by updatedScenarioStateHolder.initializationMethodStateFlow.collectAsState()
    initializeMethods.forEachIndexed { index, initializeMethod ->
      InitializationOptions(initializeMethod, updatedScenarioStateHolder, initializeMethods, index, onShowFixedScenariosDialog, getFixedScenarioById)
    }
    Column(
      modifier = Modifier.padding(8.dp).width(320.dp)
    ) {
      GroupHeader {
        Text("Image assertion")
        // Add button
        IconActionButton(
          key = AllIconsKeys.General.Add,
          onClick = {
            updatedScenarioStateHolder.onAddImageAssertion()
          },
          contentDescription = "Add image assertion",
          hint = Size(16),
        ) {
          Text(
            text = "Add image assertion",
          )
        }
        IconActionButton(
          key = AllIconsKeys.General.Information,
          onClick = {},
          contentDescription = "Image assertion",
          hint = Size(16),
        ) {
          Text(
            text = "The AI checks the screenshot when the goal is achieved. If the screenshot doesn't match the assertion, the goal is considered not achieved, and the agent will try other actions.",
          )
        }
      }
      Column {
        Text(
          modifier = Modifier
            .padding(4.dp),
          text = "History count"
        )
        TextField(
          modifier = Modifier
            .padding(4.dp),
          placeholder = { Text("History count") },
          state = updatedScenarioStateHolder.imageAssertionsHistoryCountState,
          keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        )
      }
      val imageAssertions by updatedScenarioStateHolder.imageAssertionsStateFlow.collectAsState()
      imageAssertions.forEachIndexed { index, imageAssertion: ArbigentImageAssertion ->
        Row(
          verticalAlignment = Alignment.CenterVertically
        ) {
          Column(
            Modifier
              .weight(1f)
          ) {
            Text(
              modifier = Modifier.padding(4.dp),
              text = "Image assertion ${index + 1}"
            )
            TextField(
              modifier = Modifier
                .padding(4.dp)
                .testTag("image_assertion"),
              placeholder = { Text("XX button should exist") },
              value = imageAssertion.assertionPrompt,
              onValueChange = {
                val newImageAssertions = imageAssertions.toMutableList()
                newImageAssertions[index] = imageAssertion.copy(assertionPrompt = it)
                updatedScenarioStateHolder.imageAssertionsStateFlow.value = newImageAssertions
              },
            )
          }

          IconActionButton(
            key = AllIconsKeys.General.Delete,
            onClick = {
              val newImageAssertions = imageAssertions.toMutableList()
              newImageAssertions.removeAt(index)
              updatedScenarioStateHolder.imageAssertionsStateFlow.value = newImageAssertions
            },
            contentDescription = "Remove",
            hint = Size(16),
          ) {
            Text(
              text = "Remove",
            )
          }
        }
      }
    }
  }
}

enum class InitializationMethodMenu(
  val type: String,
  val defaultContent: ArbigentScenarioContent.InitializationMethod
) {
  Noop("NoOp", ArbigentScenarioContent.InitializationMethod.Noop),
  Wait("Wait", ArbigentScenarioContent.InitializationMethod.Wait(0)),
  CleanupData("Cleanup app data", ArbigentScenarioContent.InitializationMethod.CleanupData("")),
  Back("Back", ArbigentScenarioContent.InitializationMethod.Back(1)),
  OpenLink("Open link", ArbigentScenarioContent.InitializationMethod.OpenLink("")),
  LaunchApp("Launch app", ArbigentScenarioContent.InitializationMethod.LaunchApp("", emptyMap())),
  MaestroYaml("Maestro YAML", ArbigentScenarioContent.InitializationMethod.MaestroYaml(""))
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun InitializationOptions(
  initializeMethod: ArbigentScenarioContent.InitializationMethod,
  scenarioStateHolder: ArbigentScenarioStateHolder,
  initializeMethods: List<ArbigentScenarioContent.InitializationMethod>,
  index: Int,
  onShowFixedScenariosDialog: (ArbigentScenarioStateHolder, Int) -> Unit = { _, _ -> },
  getFixedScenarioById: (String) -> FixedScenario? = { null }
) {
  Column(
    modifier = Modifier.padding(8.dp).width(240.dp)
      .testTag("initialization_method")
  ) {
    GroupHeader {
      Text("Initialization method ${index + 1}")
      IconActionButton(
        key = AllIconsKeys.General.Delete,
        onClick = {
          scenarioStateHolder.onRemoveInitializationMethod(index)
        },
        contentDescription = "Remove initialization method",
        hint = Size(16),
      ) {
        Text(
          text = "Remove initialization method",
        )
      }
    }
    Dropdown(
      modifier = Modifier.padding(4.dp),
      menuContent = {
        InitializationMethodMenu.entries.forEach { menu ->
          selectableItem(
            selected = initializeMethod::class == menu.defaultContent::class,
            onClick = {
              val newInitializeMethods = initializeMethods.toMutableList()
              newInitializeMethods[index] = menu.defaultContent
              scenarioStateHolder.onInitializationMethodChanged(index, menu.defaultContent)
            }
          ) {
            Text(menu.type)
          }
        }
      }
    ) {
      Text(
        InitializationMethodMenu.entries.firstOrNull { it.defaultContent::class == initializeMethod::class }?.type
          ?: "Select type"
      )
    }
    Row(
      verticalAlignment = Alignment.CenterVertically
    ) {
      if (initializeMethod is ArbigentScenarioContent.InitializationMethod.CleanupData) {
        TextField(
          modifier = Modifier
            .testTag("cleanup_pacakge")
            .padding(4.dp),
          value = (initializeMethod as? ArbigentScenarioContent.InitializationMethod.CleanupData)?.packageName
            ?: "",
          onValueChange = {
            scenarioStateHolder.onInitializationMethodChanged(
              index,
              ArbigentScenarioContent.InitializationMethod.CleanupData(it)
            )
          }
        )
      }
      Column {
        if (initializeMethod is ArbigentScenarioContent.InitializationMethod.Back) {
          var editingText by remember(initializeMethod) {
            mutableStateOf(
              (initializeMethod as? ArbigentScenarioContent.InitializationMethod.Back)?.times.toString()
            )
          }
          TextField(
            modifier = Modifier
              .padding(4.dp),
            value = editingText,
            placeholder = { Text("Times") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            onValueChange = {
              editingText = it
              scenarioStateHolder.onInitializationMethodChanged(
                index,
                ArbigentScenarioContent.InitializationMethod.Back(it.toIntOrNull() ?: 1)
              )
            },
          )
        }
      }
      Row {
        if (initializeMethod is ArbigentScenarioContent.InitializationMethod.Wait) {
          var editingText by remember(initializeMethod) {
            mutableStateOf(
              (initializeMethod as? ArbigentScenarioContent.InitializationMethod.Wait)?.durationMs.toString()
            )
          }
          TextField(
            modifier = Modifier
              .padding(4.dp),
            value = editingText,
            placeholder = { Text("Duration(ms)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            onValueChange = {
              editingText = it
              scenarioStateHolder.onInitializationMethodChanged(
                index,
                ArbigentScenarioContent.InitializationMethod.Wait(it.toLongOrNull() ?: 0)
              )
            },
          )
          Text(
            "ms",
            modifier = Modifier.align(Alignment.CenterVertically)
          )
        }
      }

      Row {
        if (initializeMethod is ArbigentScenarioContent.InitializationMethod.MaestroYaml) {
          var showDialog by remember { mutableStateOf(false) }
          val scenarioId = (initializeMethod as? ArbigentScenarioContent.InitializationMethod.MaestroYaml)?.scenarioId ?: ""

          // Look up the scenario title from the FixedScenarios collection
          val fixedScenario = getFixedScenarioById(scenarioId)
          val scenarioTitle = fixedScenario?.title ?: scenarioId

          Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
              text = if (scenarioTitle.isNotEmpty()) scenarioTitle else "Select Scenario",
              modifier = Modifier
                .padding(4.dp)
                .width(150.dp)
                .background(JewelTheme.globalColors.panelBackground)
                .padding(8.dp)
                .clickable { onShowFixedScenariosDialog(scenarioStateHolder, index) }
            )

            // Button to browse fixed scenarios
            IconActionButton(
              key = AllIconsKeys.General.OpenDisk,
              onClick = { onShowFixedScenariosDialog(scenarioStateHolder, index) },
              contentDescription = "Browse scenarios",
              hint = Size(16),
              modifier = Modifier.testTag("browse_scenarios_button")
            )
          }
        }
      }
      Column {
        if (initializeMethod is ArbigentScenarioContent.InitializationMethod.OpenLink) {
          TextField(
            modifier = Modifier
              .padding(4.dp),
            value = (initializeMethod as? ArbigentScenarioContent.InitializationMethod.OpenLink)?.link
              ?: "",
            onValueChange = {
              scenarioStateHolder.onInitializationMethodChanged(
                index,
                ArbigentScenarioContent.InitializationMethod.OpenLink(it)
              )
            },
          )
        }
      }
      var editingText by remember(initializeMethod) {
        mutableStateOf(
          (initializeMethod as? ArbigentScenarioContent.InitializationMethod.LaunchApp)?.packageName
            ?: ""
        )
      }
      LaunchAppInitializationSetting(
        initializeMethod = initializeMethod,
        editingText = editingText,
        onPackageChange = {
          editingText = it
          scenarioStateHolder.onInitializationMethodChanged(
            index,
            ArbigentScenarioContent.InitializationMethod.LaunchApp(
              packageName = it,
              launchArguments = (initializeMethod as? ArbigentScenarioContent.InitializationMethod.LaunchApp)?.launchArguments
                ?: emptyMap()
            )
          )
        },
        onArgumentsChange = { newArguments ->
          scenarioStateHolder.onInitializationMethodChanged(
            index,
            ArbigentScenarioContent.InitializationMethod.LaunchApp(
              packageName = editingText,
              launchArguments = newArguments
            )
          )
        },
        onArgumentRemove = { key ->
          val newArguments =
            (initializeMethod as? ArbigentScenarioContent.InitializationMethod.LaunchApp)?.launchArguments
              ?.toMutableMap() ?: mutableMapOf()
          newArguments.remove(key)
          scenarioStateHolder.onInitializationMethodChanged(
            index,
            ArbigentScenarioContent.InitializationMethod.LaunchApp(
              packageName = editingText,
              launchArguments = newArguments
            )
          )
        }
      )
    }
  }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun LaunchAppInitializationSetting(
  initializeMethod: ArbigentScenarioContent.InitializationMethod,
  editingText: String,
  onPackageChange: (String) -> Unit,
  onArgumentsChange: (Map<String, ArbigentScenarioContent.InitializationMethod.LaunchApp.ArgumentValue>) -> Unit,
  onArgumentRemove: (String) -> Unit
) {
  Column {
    if (initializeMethod is ArbigentScenarioContent.InitializationMethod.LaunchApp) {
      TextField(
        modifier = Modifier.padding(4.dp).testTag("launch_app_package"),
        value = editingText,
        placeholder = { Text("Package name") },
        onValueChange = onPackageChange
      )
      val arguments by rememberUpdatedState(
        (initializeMethod as? ArbigentScenarioContent.InitializationMethod.LaunchApp)?.launchArguments
          ?: emptyMap()
      )
      arguments.forEach { (key, value: ArbigentScenarioContent.InitializationMethod.LaunchApp.ArgumentValue) ->
        Row {
          TextField(
            modifier = Modifier.weight(1f).padding(4.dp),
            value = key,
            placeholder = { Text("key") },
            onValueChange = { newKey ->
              val newArguments = arguments.toMutableMap()
              newArguments.remove(key)
              newArguments[newKey] = value
              onArgumentsChange(newArguments)
            }
          )
          Dropdown(
            modifier = Modifier.weight(1f).padding(4.dp),
            menuContent = {
              listOf("String", "Boolean", "Int").forEach { type ->
                selectableItem(
                  selected = value::class.simpleName == type,
                  onClick = {
                    val newArguments = arguments.toMutableMap()
                    newArguments[key] = when (type) {
                      "String" -> ArbigentScenarioContent.InitializationMethod.LaunchApp.ArgumentValue.StringVal(
                        ""
                      )

                      "Boolean" -> ArbigentScenarioContent.InitializationMethod.LaunchApp.ArgumentValue.BooleanVal(
                        false
                      )

                      "Int" -> ArbigentScenarioContent.InitializationMethod.LaunchApp.ArgumentValue.IntVal(
                        0
                      )

                      else -> value
                    }
                    onArgumentsChange(newArguments)
                  }
                ) {
                  Text(type)
                }
              }
            }
          ) {
            Text(value.value::class.simpleName ?: "Select type")
          }
          if (value is ArbigentScenarioContent.InitializationMethod.LaunchApp.ArgumentValue.BooleanVal) {
            CheckboxRow(
              text = "Value",
              checked = value.value,
              onCheckedChange = {
                val newArguments = arguments.toMutableMap()
                newArguments[key] =
                  ArbigentScenarioContent.InitializationMethod.LaunchApp.ArgumentValue.BooleanVal(it)
                onArgumentsChange(newArguments)
              }
            )
          } else {
            TextField(
              modifier = Modifier.weight(1f).padding(4.dp),
              value = value.value.toString(),
              placeholder = { Text("value") },
              onValueChange = { newValue ->
                val newArguments = arguments.toMutableMap()
                newArguments[key] = when (value) {
                  is ArbigentScenarioContent.InitializationMethod.LaunchApp.ArgumentValue.StringVal -> ArbigentScenarioContent.InitializationMethod.LaunchApp.ArgumentValue.StringVal(
                    newValue
                  )

                  is ArbigentScenarioContent.InitializationMethod.LaunchApp.ArgumentValue.IntVal -> ArbigentScenarioContent.InitializationMethod.LaunchApp.ArgumentValue.IntVal(
                    newValue.toIntOrNull() ?: 0
                  )

                  else -> value
                }
                onArgumentsChange(newArguments)
              }
            )
          }
          IconActionButton(
            key = AllIconsKeys.General.Delete,
            onClick = {
              onArgumentRemove(key)
            },
            contentDescription = "Remove argument",
            hint = Size(16)
          ) {
            Text("Remove argument")
          }
        }
      }
      IconActionButton(
        key = AllIconsKeys.General.Add,
        onClick = {
          val newArguments = arguments.toMutableMap()
          newArguments[""] =
            ArbigentScenarioContent.InitializationMethod.LaunchApp.ArgumentValue.StringVal("")
          onArgumentsChange(newArguments)
        },
        contentDescription = "Add argument",
        hint = Size(16)
      ) {
        Text("Add argument")
      }
    }
  }
}

data class ScenarioSection(val goal: String, val isRunning: Boolean, val steps: List<StepItem>) {
  fun isAchieved(): Boolean {
    return steps.any { it.isAchieved() }
  }
}

data class StepItem(val step: ArbigentContextHolder.Step) {
  fun isAchieved(): Boolean {
    return step.agentAction is GoalAchievedAgentAction
  }
}

@Composable
fun buildSections(tasksToAgent: List<ArbigentTaskAssignment>): List<ScenarioSection> {
  val sections = mutableListOf<ScenarioSection>()
  for ((tasks, agent) in tasksToAgent) {
    val latestContext: ArbigentContextHolder? by agent.latestArbigentContextFlow.collectAsState(
      agent.latestArbigentContext()
    )
    val isRunning by agent.isRunningFlow.collectAsState()
    val nonNullContext = latestContext ?: continue
    val steps: List<ArbigentContextHolder.Step> by nonNullContext.stepsFlow.collectAsState(
      nonNullContext.steps()
    )
    sections += ScenarioSection(
      goal = tasks.goal,
      isRunning = isRunning,
      steps = steps.map { StepItem(it) })
  }
  return sections
}

private fun formatTimestamp(timestamp: Long): String {
  val date = java.time.Instant.ofEpochMilli(timestamp)
  return date.toString().replace('T', ' ').substringBefore('.')
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ContentPanel(
  tasksToAgentHistory: List<List<ArbigentTaskAssignment>>,
  stepFeedbacks: Set<StepFeedback>,
  onStepFeedback: (StepFeedbackEvent) -> Unit,
  modifier: Modifier
) {
  val colors = LocalPremiumColors.current
  val typography = LocalPremiumTypography.current
  var selectedHistory by remember(tasksToAgentHistory.size) { mutableStateOf(tasksToAgentHistory.lastIndex.coerceAtLeast(0)) }
  val tasksToAgent = tasksToAgentHistory.getOrNull(selectedHistory).orEmpty()
  var selectedStep: ArbigentContextHolder.Step? by remember(selectedHistory) { mutableStateOf(null) }

  Column(
    modifier = modifier
      .clip(RoundedCornerShape(10.dp))
      .background(colors.surface)
      .border(1.dp, colors.border, RoundedCornerShape(10.dp))
      .padding(10.dp)
  ) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.SpaceBetween
    ) {
      Text("Step Execution Timeline", style = typography.header1, color = colors.textPrimary)
      Dropdown(
        modifier = Modifier.padding(4.dp),
        menuContent = {
          tasksToAgentHistory.forEachIndexed { index, _ ->
            selectableItem(
              selected = index == selectedHistory,
              onClick = { selectedHistory = index },
            ) {
              Text("History #$index")
            }
          }
        }
      ) {
        Text("History #$selectedHistory")
      }
    }

    Spacer(modifier = Modifier.height(8.dp))
    Row(modifier = Modifier.fillMaxSize()) {
      val lazyColumnState = rememberLazyListState()
      val totalItemsCount by derivedStateOf { lazyColumnState.layoutInfo.totalItemsCount }
      LaunchedEffect(totalItemsCount) {
        lazyColumnState.animateScrollToItem(maxOf(totalItemsCount - 1, 0))
      }
      val sections: List<ScenarioSection> = buildSections(tasksToAgent)

      LazyColumn(
        state = lazyColumnState,
        modifier = Modifier
          .weight(1.2f)
          .fillMaxHeight(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        sections.forEachIndexed { index, section ->
          stickyHeader {
            val prefix = if (index + 1 == tasksToAgent.size) "Goal: " else "Dependency: "
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .background(colors.background.copy(alpha = 0.95f))
                .padding(vertical = 4.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text(
                text = prefix + section.goal + " (" + (index + 1) + "/" + tasksToAgent.size + ")",
                style = typography.header2,
                color = colors.textSecondary,
                maxLines = 1
              )
              Spacer(Modifier.weight(1f))
              if (section.isAchieved()) {
                PassedMark(
                  modifier = Modifier
                    .size(16.dp)
                    .background(colors.success)
                )
              }
            }
          }

          itemsIndexed(items = section.steps) { stepIndex, item ->
            val step = item.step
            val isSelected = step == selectedStep
            Column(
              modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(if (isSelected) colors.surfaceHover else colors.surface)
                .border(
                  1.dp,
                  if (isSelected) colors.primary else colors.border,
                  RoundedCornerShape(8.dp)
                )
                .clickable { selectedStep = step }
                .padding(10.dp)
            ) {
              Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
              ) {
                Text(
                  text = "Step ${stepIndex + 1}",
                  style = typography.header2,
                  color = colors.textPrimary
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                  text = formatTimestamp(step.timestamp),
                  style = typography.monospace,
                  color = colors.logTimeColor
                )
                Spacer(modifier = Modifier.weight(1f))
                if (step.cacheHit) {
                  Text(
                    "Cached",
                    modifier = Modifier
                      .clip(RoundedCornerShape(6.dp))
                      .background(colors.primary.copy(alpha = 0.15f))
                      .padding(horizontal = 6.dp, vertical = 2.dp),
                    style = typography.body,
                    color = colors.primary
                  )
                }
                if (step.agentAction is ExecuteMcpToolAgentAction) {
                  Text(
                    "MCP",
                    modifier = Modifier
                      .padding(start = 4.dp)
                      .clip(RoundedCornerShape(6.dp))
                      .background(colors.primary.copy(alpha = 0.15f))
                      .padding(horizontal = 6.dp, vertical = 2.dp),
                    style = typography.body,
                    color = colors.primary
                  )
                }
                if (step.isFailed()) {
                  Text(
                    "Failed",
                    modifier = Modifier
                      .padding(start = 4.dp)
                      .clip(RoundedCornerShape(6.dp))
                      .background(colors.errorBg)
                      .padding(horizontal = 6.dp, vertical = 2.dp),
                    style = typography.body,
                    color = colors.error
                  )
                } else if (item.isAchieved()) {
                  Text(
                    "Achieved",
                    modifier = Modifier
                      .padding(start = 4.dp)
                      .clip(RoundedCornerShape(6.dp))
                      .background(colors.successBg)
                      .padding(horizontal = 6.dp, vertical = 2.dp),
                    style = typography.body,
                    color = colors.success
                  )
                }
              }
              Spacer(modifier = Modifier.height(6.dp))
              Text(
                text = step.text(),
                style = typography.body,
                color = colors.textPrimary,
              )
              if (step.apiCallJsonLFilePath != null) {
                val feedbackHintText =
                  "Feedback data is stored locally in project result files(result.yaml). You can later use these evaluations to:\n" +
                    "• Fine-tune AI models\n• Optimize prompt sequences\n• Analyze response patterns\nNo data leaves your environment."
                Row {
                  val isGood = stepFeedbacks.any { it is StepFeedback.Good && it.stepId == step.stepId }
                  IconActionButton(
                    key = AllIconsKeys.Ide.Like,
                    onClick = {
                      onStepFeedback(
                        if (isGood) StepFeedbackEvent.RemoveGood(step.stepId) else StepFeedback.Good(step.stepId)
                      )
                    },
                    colorFilter = if (isGood) ColorFilter.tint(colors.success) else null,
                    contentDescription = feedbackHintText,
                    hint = Size(16),
                  ) { Text(feedbackHintText) }
                  val isBad = stepFeedbacks.any { it is StepFeedback.Bad && it.stepId == step.stepId }
                  IconActionButton(
                    key = AllIconsKeys.Ide.Dislike,
                    onClick = {
                      onStepFeedback(
                        if (isBad) StepFeedbackEvent.RemoveBad(step.stepId) else StepFeedback.Bad(step.stepId)
                      )
                    },
                    colorFilter = if (isBad) ColorFilter.tint(colors.error) else null,
                    contentDescription = feedbackHintText,
                    hint = Size(16),
                  ) { Text(feedbackHintText) }
                }
              }
            }
          }
          item {
            if (section.isRunning) {
              Box(modifier = Modifier.fillMaxWidth()) {
                CircularProgressIndicator(
                  modifier = Modifier.padding(8.dp).align(Alignment.Center)
                )
              }
            }
          }
        }
      }

      Spacer(modifier = Modifier.width(10.dp))
      Box(
        modifier = Modifier
          .weight(1.3f)
          .fillMaxHeight()
          .clip(RoundedCornerShape(10.dp))
          .background(colors.background)
          .border(1.dp, colors.border, RoundedCornerShape(10.dp))
          .padding(10.dp)
      ) {
        if (selectedStep == null) {
          Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
          ) {
            Text(
              text = "Select a step to inspect screenshots and details",
              style = typography.body,
              color = colors.textSecondary
            )
          }
        } else {
          StepInspectorDetail(selectedStep!!)
        }
      }
    }
  }
}

@Composable
private fun StepInspectorDetail(step: ArbigentContextHolder.Step) {
  val colors = LocalPremiumColors.current
  val typography = LocalPremiumTypography.current
  val clipboardManager = LocalClipboardManager.current
  val scrollableState = rememberScrollState()
  val annotatedPath = File(step.screenshotFilePath).getAnnotatedFilePath()

  Column(
    modifier = Modifier
      .fillMaxSize()
      .verticalScroll(scrollableState),
  ) {
    Text("Step Inspector", style = typography.header2, color = colors.primary)
    Spacer(modifier = Modifier.height(8.dp))

    Row(
      modifier = Modifier
        .fillMaxWidth()
        .height(280.dp),
      horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      ScreenshotThumbnail(
        title = "Screenshot",
        filePath = step.screenshotFilePath,
        modifier = Modifier.weight(1f)
      )
      ScreenshotThumbnail(
        title = "AI Annotated",
        filePath = annotatedPath,
        modifier = Modifier.weight(1f)
      )
    }

    Spacer(modifier = Modifier.height(8.dp))

    step.uiTreeStrings?.let {
      ExpandableSection(
        "All UI Tree (length=${it.allTreeString.length})",
        defaultExpanded = false,
        modifier = Modifier.fillMaxWidth()
      ) {
        Text(
          modifier = Modifier
            .padding(8.dp)
            .clickable {
              clipboardManager.setText(
                annotatedString = buildAnnotatedString { append(it.allTreeString) }
              )
            }
            .background(colors.surface)
            .padding(8.dp),
          text = it.allTreeString,
          style = typography.monospace,
          color = colors.textPrimary
        )
      }
      ExpandableSection(
        "Optimized UI Tree (length=${it.optimizedTreeString.length})",
        defaultExpanded = false,
        modifier = Modifier.fillMaxWidth()
      ) {
        Text(
          modifier = Modifier
            .padding(8.dp)
            .background(colors.surface)
            .padding(8.dp),
          text = it.optimizedTreeString,
          style = typography.monospace,
          color = colors.textPrimary
        )
      }
    }

    step.aiRequest?.let { request ->
      ExpandableSection(
        title = "AI Request",
        defaultExpanded = false,
        modifier = Modifier.fillMaxWidth()
      ) {
        Text(
          modifier = Modifier
            .padding(8.dp)
            .background(colors.surface)
            .padding(8.dp),
          text = request,
          style = typography.monospace,
          color = colors.textPrimary
        )
      }
    }
    step.aiResponse?.let { response ->
      ExpandableSection(
        title = "AI Response",
        defaultExpanded = true,
        modifier = Modifier.fillMaxWidth()
      ) {
        Text(
          modifier = Modifier
            .padding(8.dp)
            .background(colors.surface)
            .padding(8.dp),
          text = response,
          style = typography.monospace,
          color = colors.textPrimary
        )
      }
    }
  }
}

@Composable
private fun ScreenshotThumbnail(
  title: String,
  filePath: String,
  modifier: Modifier = Modifier
) {
  val colors = LocalPremiumColors.current
  val typography = LocalPremiumTypography.current
  var showBigImage by remember(filePath) { mutableStateOf(false) }
  val imageBitmap = remember(filePath) {
    runCatching { FileInputStream(filePath).use { loadImageBitmap(it) } }.getOrNull()
  }

  Column(modifier = modifier) {
    Text(text = title, style = typography.body, color = colors.textSecondary)
    Spacer(modifier = Modifier.height(4.dp))
    Box(
      modifier = Modifier
        .fillMaxSize()
        .clip(RoundedCornerShape(8.dp))
        .background(colors.surface)
        .border(1.dp, colors.border, RoundedCornerShape(8.dp))
        .clickable(enabled = imageBitmap != null) { showBigImage = true },
      contentAlignment = Alignment.Center
    ) {
      if (imageBitmap != null) {
        Image(
          bitmap = imageBitmap,
          contentDescription = title,
          modifier = Modifier.fillMaxSize()
        )
      } else {
        Text("No image", style = typography.body, color = colors.textSecondary)
      }
    }
  }

  if (showBigImage && imageBitmap != null) {
    Dialog(onDismissRequest = { showBigImage = false }) {
      Box(
        modifier = Modifier
          .fillMaxSize()
          .background(Color.Black.copy(alpha = 0.88f))
          .clickable { showBigImage = false }
          .padding(16.dp),
        contentAlignment = Alignment.Center
      ) {
        Image(
          bitmap = imageBitmap,
          contentDescription = "zoomed-$title",
          modifier = Modifier.fillMaxSize()
        )
      }
    }
  }
}

@Composable
fun PassedMark(modifier: Modifier = Modifier) {
  Icon(
    key = AllIconsKeys.Actions.Checked,
    contentDescription = "Achieved",
    modifier = modifier
      .size(32.dp)
      .clip(
        CircleShape
      )
      .background(JewelTheme.colorPalette.green(8))
  )
}

@Composable
fun GroupHeader(
  modifier: Modifier = Modifier,
  style: GroupHeaderStyle = LocalGroupHeaderStyle.current,
  content: @Composable RowScope.() -> Unit,
) {
  Row(modifier, verticalAlignment = Alignment.CenterVertically) {
    content()

    Divider(
      orientation = Orientation.Horizontal,
      modifier = Modifier.fillMaxWidth(),
      color = style.colors.divider,
      thickness = style.metrics.dividerThickness,
      startIndent = style.metrics.indent,
    )
  }
}

@Composable
fun ExpandableSection(
  title: String,
  defaultExpanded: Boolean = false,
  modifier: Modifier = Modifier,
  content: @Composable ColumnScope.() -> Unit,
) {
  var expanded by remember { mutableStateOf(defaultExpanded) }
  Column(modifier) {
    Row(
      verticalAlignment = Alignment.CenterVertically,
      modifier = Modifier
        .clickable { expanded = !expanded }
        .fillMaxWidth()
    ) {
      if (expanded) {
        Icon(
          key = AllIconsKeys.General.ArrowDown,
          contentDescription = "Collapse $title",
          hint = Size(28)
        )
      } else {
        Icon(
          key = AllIconsKeys.General.ArrowRight,
          contentDescription = "Expand $title",
          hint = Size(28)
        )
      }
      Text(title)
    }
    AnimatedVisibility(visible = expanded) {
      Column {
        content()
      }
    }
  }
}

package io.github.takahirom.arbigent.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.ExperimentalFoundationApi
import org.jetbrains.jewel.ui.component.TextArea
import org.jetbrains.jewel.ui.component.Text
import org.jetbrains.jewel.ui.component.DefaultButton
import org.jetbrains.jewel.ui.component.OutlinedButton
import androidx.compose.ui.Alignment
import org.jetbrains.jewel.ui.component.Checkbox

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ScenarioGenerationDialog(
  appStateHolder: ArbigentAppStateHolder,
  onCloseRequest: () -> Unit,
  onGenerate: (scenariosToGenerate: String, appUiStructure: String, customInstruction: String, useExistingScenarios: Boolean) -> Unit
) {
  val colors = LocalPremiumColors.current
  val typography = LocalPremiumTypography.current
  TestCompatibleDialog(
    onCloseRequest = onCloseRequest,
    title = "Generate Scenario",
    content = {
      val scrollState = rememberScrollState()

      // Define the TextFieldState variables at this level so they're accessible to the buttons
      val scenariosToGenerate: TextFieldState = remember {
        TextFieldState("")
      }
      val appUiStructure: TextFieldState = remember {
        TextFieldState(appStateHolder.promptFlow.value.appUiStructure)
      }
      val customInstruction: TextFieldState = remember {
        TextFieldState(appStateHolder.promptFlow.value.scenarioGenerationCustomInstruction)
      }

      LaunchedEffect(Unit) {
        snapshotFlow { appUiStructure.text }.collect { text ->
          if (text.isNotBlank()) {
            appStateHolder.onAppUiStructureChanged(text.toString())
          }
        }
      }

      LaunchedEffect(Unit) {
        snapshotFlow { customInstruction.text }.collect { text ->
          if (text.isNotBlank()) {
            appStateHolder.onScenarioGenerationCustomInstructionChanged(text.toString())
          }
        }
      }

      // State for the checkbox
      var useExistingScenarios by remember { mutableStateOf(false) }
      var includeNavigationModule by remember { mutableStateOf(true) }
      var includeAssertionModule by remember { mutableStateOf(true) }
      var includeFormModule by remember { mutableStateOf(true) }

      Column {
        Column(
          modifier = Modifier
            .padding(16.dp)
            .weight(1F)
            .verticalScroll(scrollState)
        ) {
          SettingsCard(title = "Scenarios to Generate") {
            Text(
              text = "Describe user journeys in bullet points or numbered steps.",
              style = typography.body,
              color = colors.textSecondary,
              modifier = Modifier.padding(start = 8.dp, end = 8.dp, bottom = 4.dp)
            )
            TextArea(
              state = scenariosToGenerate,
              modifier = Modifier
                .padding(8.dp)
                .height(140.dp)
                .testTag("scenarios_to_generate"),
              placeholder = { Text("e.g. 1) Login flow  2) Open profile page  3) Edit profile") },
              decorationBoxModifier = Modifier.padding(horizontal = 8.dp),
            )
          }
          Spacer(modifier = Modifier.height(10.dp))

          SettingsCard(title = "Generator Modules") {
            Text(
              text = "Enable modules to shape generated scenarios.",
              style = typography.body,
              color = colors.textSecondary,
              modifier = Modifier.padding(start = 8.dp, end = 8.dp, bottom = 4.dp)
            )
            Row(
              modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Checkbox(
                checked = includeNavigationModule,
                onCheckedChange = { includeNavigationModule = it }
              )
              Text("Navigation", modifier = Modifier.padding(start = 8.dp))
            }
            Row(
              modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Checkbox(
                checked = includeAssertionModule,
                onCheckedChange = { includeAssertionModule = it }
              )
              Text("Assertions", modifier = Modifier.padding(start = 8.dp))
            }
            Row(
              modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Checkbox(
                checked = includeFormModule,
                onCheckedChange = { includeFormModule = it }
              )
              Text("Form interactions", modifier = Modifier.padding(start = 8.dp))
            }
          }
          Spacer(modifier = Modifier.height(10.dp))

          SettingsCard(title = "App UI Structure (Optional)") {
            TextArea(
              state = appUiStructure,
              modifier = Modifier
                .padding(8.dp)
                .height(120.dp)
                .testTag("app_ui_structure"),
              placeholder = { Text("Provide major screens/components if available.") },
              decorationBoxModifier = Modifier.padding(horizontal = 8.dp),
            )
          }
          Spacer(modifier = Modifier.height(10.dp))

          SettingsCard(title = "Custom Instruction (Optional)") {
            TextArea(
              state = customInstruction,
              modifier = Modifier
                .padding(8.dp)
                .height(120.dp)
                .testTag("custom_instruction"),
              placeholder = { Text("Extra generation constraints or style preferences.") },
              decorationBoxModifier = Modifier.padding(horizontal = 8.dp),
            )
          }
        }

        // Checkbox for using existing scenarios
        Row(
          modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(colors.surface)
            .padding(8.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Checkbox(
            checked = useExistingScenarios,
            onCheckedChange = { useExistingScenarios = it },
            modifier = Modifier.testTag("use_existing_scenarios_checkbox")
          )
          Text(
            "Use existing scenarios as examples",
            modifier = Modifier.padding(start = 8.dp)
          )
        }

        // Buttons
        Row(
          modifier = Modifier.padding(8.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          DefaultButton(
            onClick = {
              val modulePrefix = buildList {
                if (includeNavigationModule) add("navigation")
                if (includeAssertionModule) add("assertion")
                if (includeFormModule) add("form")
              }.joinToString(", ")
              val moduleHint = if (modulePrefix.isBlank()) "" else "\nEnabled modules: $modulePrefix"
              val appUiStructureText = appUiStructure.text.toString()
              val customInstructionText = customInstruction.text.toString()
              onGenerate(
                scenariosToGenerate.text.toString(),
                appUiStructureText,
                customInstructionText + moduleHint,
                useExistingScenarios,
              )
              onCloseRequest()
            },
            modifier = Modifier.padding(end = 8.dp)
          ) {
            Text("Generate")
          }

          OutlinedButton(
            onClick = onCloseRequest,
            modifier = Modifier.padding(start = 8.dp)
          ) {
            Text("Close")
          }
        }
      }
    }
  )
}

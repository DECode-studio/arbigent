package io.github.takahirom.arbigent.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicSecureTextField
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import org.jetbrains.jewel.ui.component.DefaultButton
import org.jetbrains.jewel.ui.component.OutlinedButton
import org.jetbrains.jewel.ui.component.RadioButtonRow
import org.jetbrains.jewel.ui.component.Text
import org.jetbrains.jewel.ui.component.TextField

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AddAiProviderDialog(
  aiSettingStateHolder: AiSettingStateHolder,
  editingProvider: AiProviderSetting? = null,
  onCloseRequest: () -> Unit
) {
  val isEditMode = editingProvider != null
  val colors = LocalPremiumColors.current
  val typography = LocalPremiumTypography.current

  var selectedType by remember {
    mutableStateOf(
      when (editingProvider) {
        is AiProviderSetting.OpenAi -> "OpenAi"
        is AiProviderSetting.Gemini -> "Gemini"
        is AiProviderSetting.NvidiaNim -> "NvidiaNim"
        is AiProviderSetting.CustomOpenAiApiBasedAi -> "CustomOpenAiApiBasedAi"
        is AiProviderSetting.AzureOpenAi -> "AzureOpenAi"
        else -> "OpenAi"
      }
    )
  }

  val idState = remember {
    TextFieldState(editingProvider?.id ?: selectedType)
  }
  val modelNameState = remember {
    TextFieldState(
      when (editingProvider) {
        is AiProviderSetting.OpenAi -> editingProvider.modelName
        is AiProviderSetting.Gemini -> editingProvider.modelName
        is AiProviderSetting.NvidiaNim -> editingProvider.modelName
        is AiProviderSetting.CustomOpenAiApiBasedAi -> editingProvider.modelName
        is AiProviderSetting.AzureOpenAi -> editingProvider.modelName
        else -> ""
      }
    )
  }
  val apiKeyState = remember {
    TextFieldState(
      when (editingProvider) {
        is AiProviderSetting.OpenAi -> editingProvider.apiKey
        is AiProviderSetting.Gemini -> editingProvider.apiKey
        is AiProviderSetting.NvidiaNim -> editingProvider.apiKey
        is AiProviderSetting.CustomOpenAiApiBasedAi -> editingProvider.apiKey
        is AiProviderSetting.AzureOpenAi -> editingProvider.apiKey
        else -> ""
      }
    )
  }
  val baseUrlState = remember {
    TextFieldState((editingProvider as? AiProviderSetting.CustomOpenAiApiBasedAi)?.baseUrl ?: "")
  }
  val endpointState = remember {
    TextFieldState((editingProvider as? AiProviderSetting.AzureOpenAi)?.endpoint ?: "")
  }
  val nvidiaEndpointState = remember {
    TextFieldState((editingProvider as? AiProviderSetting.NvidiaNim)?.baseUrl ?: "https://integrate.api.nvidia.com/v1/")
  }
  val apiVersionState = remember {
    TextFieldState((editingProvider as? AiProviderSetting.AzureOpenAi)?.apiVersion ?: "2025-01-01-preview")
  }

  LaunchedEffect(selectedType, isEditMode) {
    if (!isEditMode) {
      idState.edit { replace(0, length, selectedType) }
    }
  }

  val existingIds = aiSettingStateHolder.aiSetting.aiSettings.map { it.id }
  val idText = idState.text.toString().trim()
  val modelNameText = modelNameState.text.toString().trim()
  val baseUrlText = baseUrlState.text.toString().trim()
  val endpointText = endpointState.text.toString().trim()
  val nvidiaEndpointText = nvidiaEndpointState.text.toString().trim()
  val apiVersionText = apiVersionState.text.toString().trim()
  val isIdDuplicate = if (isEditMode) {
    existingIds.filter { it != editingProvider?.id }.contains(idText)
  } else {
    existingIds.contains(idText)
  }
  val isIdValid = idText.isNotEmpty() && !isIdDuplicate
  val isBaseUrlEndsWithSlash = baseUrlText.isEmpty() || baseUrlText.endsWith("/")
  val isEndpointEndsWithSlash = endpointText.isEmpty() || endpointText.endsWith("/")
  val isNvidiaEndpointEndsWithSlash = nvidiaEndpointText.isEmpty() || nvidiaEndpointText.endsWith("/")

  val canSubmit = when (selectedType) {
      "NvidiaNim" -> isIdValid && modelNameText.isNotEmpty() && nvidiaEndpointText.isNotEmpty()
      "CustomOpenAiApiBasedAi" -> isIdValid && modelNameText.isNotEmpty() && baseUrlText.isNotEmpty()
      "AzureOpenAi" -> isIdValid && modelNameText.isNotEmpty() && endpointText.isNotEmpty() && apiVersionText.isNotEmpty()
      else -> isIdValid && modelNameText.isNotEmpty()
  }

  fun saveProvider() {
    val provider = when (selectedType) {
      "CustomOpenAiApiBasedAi" -> AiProviderSetting.CustomOpenAiApiBasedAi(
        id = idText,
        apiKey = apiKeyState.text.toString(),
        modelName = modelNameText,
        baseUrl = if (baseUrlText.endsWith("/")) baseUrlText else "$baseUrlText/"
      )

      "AzureOpenAi" -> AiProviderSetting.AzureOpenAi(
        id = idText,
        apiKey = apiKeyState.text.toString(),
        modelName = modelNameText,
        endpoint = if (endpointText.endsWith("/")) endpointText else "$endpointText/",
        apiVersion = apiVersionText
      )

      "NvidiaNim" -> AiProviderSetting.NvidiaNim(
        id = idText,
        apiKey = apiKeyState.text.toString(),
        modelName = modelNameText,
        baseUrl = if (nvidiaEndpointText.endsWith("/")) nvidiaEndpointText else "$nvidiaEndpointText/"
      )

      "Gemini" -> AiProviderSetting.Gemini(
        id = idText,
        apiKey = apiKeyState.text.toString(),
        modelName = modelNameText
      )

      else -> AiProviderSetting.OpenAi(
        id = idText,
        apiKey = apiKeyState.text.toString(),
        modelName = modelNameText
      )
    }

    if (isEditMode) {
      aiSettingStateHolder.updateAiProvider(provider)
    } else {
      aiSettingStateHolder.addAiProvider(provider)
    }
    onCloseRequest()
  }

  TestCompatibleDialog(
    onCloseRequest = onCloseRequest,
    title = if (isEditMode) "Edit AI Provider" else "Add New AI Provider",
    content = {
      Box(
        modifier = Modifier
          .fillMaxSize()
          .background(colors.background)
      ) {
        Column(
          modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
        ) {
          Text(
            text = "Set your provider credentials and model configuration.",
            color = colors.textSecondary,
            style = typography.body
          )
          Spacer(modifier = Modifier.height(12.dp))

          Column(
            modifier = Modifier
              .weight(1f)
              .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
          ) {
            SettingsCard(title = "AI Provider Type") {
              FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
              ) {
                RadioButtonRow(
                  text = "OpenAI",
                  selected = selectedType == "OpenAi",
                  onClick = { if (!isEditMode) selectedType = "OpenAi" },
                  enabled = !isEditMode
                )
                RadioButtonRow(
                  text = "Gemini",
                  selected = selectedType == "Gemini",
                  onClick = { if (!isEditMode) selectedType = "Gemini" },
                  enabled = !isEditMode
                )
                RadioButtonRow(
                  text = "NVIDIA NIM (Gemma/Llama)",
                  selected = selectedType == "NvidiaNim",
                  onClick = {
                    if (!isEditMode) {
                      selectedType = "NvidiaNim"
                      modelNameState.edit {
                        replace(0, length, "google/gemma-3n-e4b-it")
                      }
                    }
                  },
                  enabled = !isEditMode
                )
                RadioButtonRow(
                  text = "Custom OpenAI API Based AI",
                  selected = selectedType == "CustomOpenAiApiBasedAi",
                  onClick = { if (!isEditMode) selectedType = "CustomOpenAiApiBasedAi" },
                  enabled = !isEditMode
                )
                RadioButtonRow(
                  text = "Azure OpenAI",
                  selected = selectedType == "AzureOpenAi",
                  onClick = { if (!isEditMode) selectedType = "AzureOpenAi" },
                  enabled = !isEditMode
                )
              }
            }

            SettingsCard(title = "Basic Settings") {
              Text("Provider ID", color = colors.textSecondary, style = typography.body)
              TextField(
                state = idState,
                modifier = Modifier.padding(top = 6.dp).fillMaxWidth(),
                placeholder = { Text("Enter a unique ID for this provider") },
                enabled = !isEditMode
              )
              if (!isIdValid && idText.isNotEmpty()) {
                Text(
                  text = if (isIdDuplicate) {
                    "Error: ID already exists. Please choose a different ID."
                  } else {
                    "ID must not be empty"
                  },
                  color = colors.error,
                  modifier = Modifier.padding(top = 6.dp)
                )
              }

              Spacer(modifier = Modifier.height(10.dp))
              Text("Model Name", color = colors.textSecondary, style = typography.body)
              TextField(
                state = modelNameState,
                modifier = Modifier.padding(top = 6.dp).fillMaxWidth(),
                placeholder = { Text("Enter model name (e.g., gpt-4.1)") }
              )

              Spacer(modifier = Modifier.height(10.dp))
              Text("API Key", color = colors.textSecondary, style = typography.body)
              BasicSecureTextField(
                modifier = Modifier
                  .padding(top = 6.dp)
                  .fillMaxWidth(),
                decorator = {
                  Box(
                    Modifier
                      .fillMaxWidth()
                      .clip(RoundedCornerShape(8.dp))
                      .background(colors.surface)
                      .border(1.dp, colors.border, RoundedCornerShape(8.dp))
                      .padding(10.dp)
                  ) {
                    if (apiKeyState.text.isEmpty()) {
                      Text(
                        text = "Enter API Key (Saved in Keychain on Mac)",
                        color = colors.textSecondary
                      )
                    }
                    it()
                  }
                },
                state = apiKeyState,
              )
            }

            when (selectedType) {
              "NvidiaNim" -> {
                SettingsCard(title = "Connection Settings") {
                  Text("Endpoint", color = colors.textSecondary, style = typography.body)
                  TextField(
                    state = nvidiaEndpointState,
                    modifier = Modifier.padding(top = 6.dp).fillMaxWidth(),
                    placeholder = { Text("Enter endpoint URL (e.g., https://integrate.api.nvidia.com/v1/)") }
                  )
                  if (!isNvidiaEndpointEndsWithSlash) {
                    Text(
                      text = "Warning: URL should end with a slash (/)",
                      color = Color(0xFFF59E0B),
                      modifier = Modifier.padding(top = 6.dp)
                    )
                  }
                  Text(
                    text = "Use NVIDIA API key (NVIDIA_API_KEY). For Gemma models, set model like google/gemma-3n-e4b-it.",
                    color = colors.textSecondary,
                    style = typography.body,
                    modifier = Modifier.padding(top = 8.dp)
                  )
                }
              }

              "CustomOpenAiApiBasedAi" -> {
                SettingsCard(title = "Connection Settings") {
                  Text("Base URL", color = colors.textSecondary, style = typography.body)
                  TextField(
                    state = baseUrlState,
                    modifier = Modifier.padding(top = 6.dp).fillMaxWidth(),
                    placeholder = { Text("Enter base URL (e.g., http://localhost:11434/v1/)") }
                  )
                  if (!isBaseUrlEndsWithSlash) {
                    Text(
                      text = "Warning: URL should end with a slash (/)",
                      color = Color(0xFFF59E0B),
                      modifier = Modifier.padding(top = 6.dp)
                    )
                  }
                }
              }

              "AzureOpenAi" -> {
                SettingsCard(title = "Connection Settings") {
                  Text("Endpoint", color = colors.textSecondary, style = typography.body)
                  TextField(
                    state = endpointState,
                    modifier = Modifier.padding(top = 6.dp).fillMaxWidth(),
                    placeholder = { Text("Enter endpoint URL (e.g., https://{endpoint}/openai/deployments/{deployment-id}/)") }
                  )
                  if (!isEndpointEndsWithSlash) {
                    Text(
                      text = "Warning: URL should end with a slash (/)",
                      color = Color(0xFFF59E0B),
                      modifier = Modifier.padding(top = 6.dp)
                    )
                  }

                  Spacer(modifier = Modifier.height(10.dp))
                  Text("API Version", color = colors.textSecondary, style = typography.body)
                  TextField(
                    state = apiVersionState,
                    modifier = Modifier.padding(top = 6.dp).fillMaxWidth(),
                    placeholder = { Text("Enter API version") }
                  )
                }
              }
            }
          }

          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(top = 12.dp),
            horizontalArrangement = Arrangement.End
          ) {
            OutlinedButton(
              onClick = onCloseRequest,
              modifier = Modifier.padding(end = 8.dp)
            ) {
              Text("Cancel")
            }
            DefaultButton(
              onClick = { if (canSubmit) saveProvider() },
              enabled = canSubmit
            ) {
              Text(if (isEditMode) "Update Provider" else "Add Provider")
            }
          }
        }
      }
    }
  )
}

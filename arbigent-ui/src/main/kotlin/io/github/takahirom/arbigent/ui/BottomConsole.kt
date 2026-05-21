package io.github.takahirom.arbigent.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.takahirom.arbigent.ArbigentFiles
import io.github.takahirom.arbigent.ArbigentGlobalStatus
import io.github.takahirom.arbigent.ArbigentInternalApi
import kotlinx.coroutines.delay
import org.jetbrains.jewel.ui.Orientation
import org.jetbrains.jewel.ui.component.Divider
import org.jetbrains.jewel.ui.component.IconActionButton
import org.jetbrains.jewel.ui.component.Text
import org.jetbrains.jewel.ui.component.TextField
import org.jetbrains.jewel.ui.icons.AllIconsKeys
import org.jetbrains.jewel.ui.painter.hints.Size
import java.awt.Desktop
import java.time.ZoneId
import java.time.format.DateTimeFormatter

private enum class CopyState {
  None,
  FilteredCopied,
  AllCopied
}

@OptIn(ArbigentInternalApi::class)
@Composable
fun BottomConsole() {
  val colors = LocalPremiumColors.current
  val typography = LocalPremiumTypography.current
  val clipboardManager = LocalClipboardManager.current
  val globalStatus by ArbigentGlobalStatus.status.collectAsState(ArbigentGlobalStatus.status())

  var isExpanded by remember { mutableStateOf(false) }

  Box(
    modifier = Modifier
      .fillMaxWidth()
      .background(colors.surface)
      .border(1.dp, colors.border)
  ) {
    if (!isExpanded) {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .clickable { isExpanded = true }
          .padding(horizontal = 12.dp, vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        Box(
          modifier = Modifier
            .size(8.dp)
            .clip(RoundedCornerShape(999.dp))
            .background(colors.primary)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
          text = globalStatus.ifEmpty { "Idle" },
          style = typography.body,
          color = colors.textSecondary,
          maxLines = 1,
          modifier = Modifier.weight(1f)
        )
        Text(
          text = "Expand Console",
          style = typography.body.copy(fontWeight = FontWeight.SemiBold),
          color = colors.primary
        )
      }
      return@Box
    }

    var consoleHeight by remember { mutableStateOf(260.dp) }
    val queryState = rememberTextFieldState()
    val rawHistories by ArbigentGlobalStatus.console.collectAsState(ArbigentGlobalStatus.console())
    val filteredLogs by remember(rawHistories, queryState.text) {
      derivedStateOf {
        val query = queryState.text.toString().trim()
        if (query.isEmpty()) {
          rawHistories
        } else {
          rawHistories.filter { (_, status) -> status.contains(query, ignoreCase = true) }
        }
      }
    }

    val lazyListState = rememberLazyListState()
    var autoscrollEnabled by remember { mutableStateOf(true) }
    var copyState by remember { mutableStateOf(CopyState.None) }

    LaunchedEffect(copyState) {
      if (copyState != CopyState.None) {
        delay(1200)
        copyState = CopyState.None
      }
    }

    LaunchedEffect(filteredLogs, autoscrollEnabled) {
      if (autoscrollEnabled && filteredLogs.isNotEmpty()) {
        lazyListState.scrollToItem(filteredLogs.lastIndex)
      }
    }

    Column(
      modifier = Modifier
        .fillMaxWidth()
        .height(consoleHeight)
    ) {
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .height(6.dp)
          .pointerHoverIcon(PointerIcon.Hand)
          .pointerInput(Unit) {
            detectDragGestures { change, dragAmount ->
              change.consume()
              consoleHeight = (consoleHeight - dragAmount.y.toDp()).coerceIn(120.dp, 680.dp)
            }
          }
          .background(colors.border)
      )

      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 10.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = "AGENT CONSOLE",
          style = typography.header2.copy(fontWeight = FontWeight.Bold),
          color = colors.textPrimary,
          modifier = Modifier.weight(1f)
        )

        TextField(
          state = queryState,
          modifier = Modifier
            .width(220.dp)
            .height(28.dp),
          placeholder = { Text("Filter logs...") }
        )

        Spacer(modifier = Modifier.width(6.dp))

        IconActionButton(
          key = AllIconsKeys.Actions.Download,
          onClick = {
            try {
              val logFile = ArbigentFiles.logFile
              if (logFile == null) {
                ArbigentGlobalStatus.log("Log file path is not configured.")
                return@IconActionButton
              }
              logFile.parentFile?.mkdirs()
              if (!logFile.exists()) {
                logFile.writeText("")
              }
              if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().open(logFile)
              } else {
                ArbigentGlobalStatus.log("Desktop open is not supported on this environment.")
              }
            } catch (e: Exception) {
              ArbigentGlobalStatus.log("Failed to open log file: ${e.message}")
            }
          },
          contentDescription = "Open log file",
          hint = Size(20)
        )

        IconActionButton(
          key = if (copyState == CopyState.FilteredCopied) AllIconsKeys.Actions.Checked else AllIconsKeys.Actions.Copy,
          onClick = {
            clipboardManager.setText(
              buildAnnotatedString {
                filteredLogs.forEach { (instant, status) ->
                  append(instant.toString())
                  append(" ")
                  append(status)
                  append("\n")
                }
              }
            )
            copyState = CopyState.FilteredCopied
          },
          contentDescription = "Copy filtered logs",
          hint = Size(20)
        )

        IconActionButton(
          key = if (copyState == CopyState.AllCopied) AllIconsKeys.Actions.Checked else AllIconsKeys.Actions.Copy,
          onClick = {
            clipboardManager.setText(
              buildAnnotatedString {
                rawHistories.forEach { (instant, status) ->
                  append(instant.toString())
                  append(" ")
                  append(status)
                  append("\n")
                }
              }
            )
            copyState = CopyState.AllCopied
          },
          contentDescription = "Copy all logs",
          hint = Size(20)
        )

        IconActionButton(
          key = AllIconsKeys.General.Close,
          onClick = { isExpanded = false },
          contentDescription = "Collapse console",
          hint = Size(20)
        )
      }

      Divider(orientation = Orientation.Horizontal)

      Box(
        modifier = Modifier
          .weight(1f)
          .fillMaxWidth()
          .background(colors.background)
      ) {
        LazyColumn(
          state = lazyListState,
          modifier = Modifier.fillMaxSize(),
          contentPadding = PaddingValues(8.dp)
        ) {
          items(filteredLogs) { (instant, status) ->
            val timeString = instant.atZone(ZoneId.systemDefault()).format(
              DateTimeFormatter.ofPattern("HH:mm:ss.SSS")
            )
            val message = status.replace(";base64,.*?\"".toRegex(), ";base64,[omitted]\"")
            val logColor = when {
              status.contains("[ERROR]", ignoreCase = true) -> Color(0xFFEF4444)
              status.contains("[WARN]", ignoreCase = true) -> Color(0xFFFBBF24)
              status.contains("[INFO]", ignoreCase = true) -> Color(0xFF60A5FA)
              status.contains("[DEBUG]", ignoreCase = true) -> Color(0xFF71717A)
              else -> colors.textPrimary
            }

            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 1.dp)
            ) {
              Text(
                text = "[$timeString]",
                style = typography.monospace,
                color = colors.logTimeColor,
                modifier = Modifier.padding(end = 6.dp)
              )
              Text(
                text = message,
                style = typography.monospace,
                color = logColor,
                modifier = Modifier
                  .weight(1f)
                  .clickable {
                    clipboardManager.setText(buildAnnotatedString { append(message) })
                  }
              )
            }
          }
        }

        Box(
          modifier = Modifier
            .align(Alignment.BottomEnd)
            .padding(10.dp)
            .shadow(2.dp, RoundedCornerShape(16.dp))
            .clip(RoundedCornerShape(16.dp))
            .background(if (autoscrollEnabled) colors.primary else colors.surface)
            .border(1.dp, colors.border, RoundedCornerShape(16.dp))
            .clickable { autoscrollEnabled = !autoscrollEnabled }
            .padding(horizontal = 10.dp, vertical = 5.dp)
        ) {
          Text(
            text = if (autoscrollEnabled) "Auto-scroll ON" else "Auto-scroll OFF",
            style = typography.body.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold),
            color = if (autoscrollEnabled) Color.White else colors.textSecondary
          )
        }
      }
    }
  }
}

package io.github.takahirom.arbigent.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.jetbrains.jewel.ui.component.Text

@Composable
fun SettingsCard(
  title: String,
  modifier: Modifier = Modifier,
  content: @Composable ColumnScope.() -> Unit
) {
  val colors = LocalPremiumColors.current
  val typography = LocalPremiumTypography.current
  Column(
    modifier = modifier
      .fillMaxWidth()
      .shadow(2.dp, shape = RoundedCornerShape(12.dp))
      .clip(RoundedCornerShape(12.dp))
      .background(colors.surface)
      .border(1.dp, colors.border, RoundedCornerShape(12.dp))
      .padding(14.dp)
  ) {
    Text(
      text = title,
      style = typography.header2.copy(fontWeight = FontWeight.Bold),
      color = colors.primary
    )
    Spacer(modifier = Modifier.height(8.dp))
    content()
  }
}

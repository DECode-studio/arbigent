package io.github.takahirom.arbigent.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import org.jetbrains.jewel.foundation.theme.JewelTheme
import org.jetbrains.jewel.intui.standalone.theme.IntUiTheme
import org.jetbrains.jewel.intui.standalone.theme.default
import org.jetbrains.jewel.intui.standalone.theme.lightThemeDefinition
import org.jetbrains.jewel.intui.standalone.theme.darkThemeDefinition
import org.jetbrains.jewel.intui.window.decoratedWindow
import org.jetbrains.jewel.intui.window.styling.light
import org.jetbrains.jewel.intui.window.styling.dark
import org.jetbrains.jewel.ui.ComponentStyling
import org.jetbrains.jewel.window.styling.TitleBarStyle

val OutfitFontFamily = FontFamily.Default
val InterFontFamily = FontFamily.Default

data class PremiumColors(
  val background: Color,
  val surface: Color,
  val surfaceHover: Color,
  val primary: Color,
  val primaryHover: Color,
  val success: Color,
  val successBg: Color,
  val error: Color,
  val errorBg: Color,
  val border: Color,
  val textPrimary: Color,
  val textSecondary: Color,
  val logTimeColor: Color
)

val LightPremiumColors = PremiumColors(
  background = Color(0xFFF8F8FA),
  surface = Color(0xFFFFFFFF),
  surfaceHover = Color(0xFFF1F1F4),
  primary = Color(0xFF7C3AED),
  primaryHover = Color(0xFF6D28D9),
  success = Color(0xFF10B981),
  successBg = Color(0xFFECFDF5),
  error = Color(0xFFEF4444),
  errorBg = Color(0xFFFEF2F2),
  border = Color(0xFFE4E4E7),
  textPrimary = Color(0xFF09090B),
  textSecondary = Color(0xFF71717A),
  logTimeColor = Color(0xFF2563EB)
)

val DarkPremiumColors = PremiumColors(
  background = Color(0xFF09090B),
  surface = Color(0xFF18181B),
  surfaceHover = Color(0xFF27272A),
  primary = Color(0xFFA78BFA),
  primaryHover = Color(0xFF8B5CF6),
  success = Color(0xFF34D399),
  successBg = Color(0xFF064E3B),
  error = Color(0xFFF87171),
  errorBg = Color(0xFF7F1D1D),
  border = Color(0xFF27272A),
  textPrimary = Color(0xFFFAFAFA),
  textSecondary = Color(0xFFA1A1AA),
  logTimeColor = Color(0xFF60A5FA)
)

data class PremiumTypography(
  val display1: TextStyle,
  val header1: TextStyle,
  val header2: TextStyle,
  val body: TextStyle,
  val monospace: TextStyle
)

val ArbigentTypography = PremiumTypography(
  display1 = TextStyle(fontFamily = OutfitFontFamily, fontWeight = FontWeight.ExtraBold, fontSize = 22.sp),
  header1 = TextStyle(fontFamily = OutfitFontFamily, fontWeight = FontWeight.SemiBold, fontSize = 16.sp),
  header2 = TextStyle(fontFamily = InterFontFamily, fontWeight = FontWeight.Bold, fontSize = 13.sp),
  body = TextStyle(fontFamily = InterFontFamily, fontWeight = FontWeight.Medium, fontSize = 12.sp),
  monospace = TextStyle(fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Medium, fontSize = 11.5.sp)
)

val LocalPremiumColors = staticCompositionLocalOf { LightPremiumColors }
val LocalPremiumTypography = staticCompositionLocalOf { ArbigentTypography }

@Composable
fun AppTheme(
  isDark: Boolean = isSystemInDarkTheme(),
  content: @Composable () -> Unit
) {
  val colors = if (isDark) DarkPremiumColors else LightPremiumColors
  val jewelTheme = if (isDark) JewelTheme.darkThemeDefinition() else JewelTheme.lightThemeDefinition()
  val titleStyle = if (isDark) TitleBarStyle.dark() else TitleBarStyle.light()

  CompositionLocalProvider(
    LocalPremiumColors provides colors,
    LocalPremiumTypography provides ArbigentTypography
  ) {
    IntUiTheme(
      theme = jewelTheme,
      styling = ComponentStyling.default().decoratedWindow(
        titleBarStyle = titleStyle
      ),
      swingCompatMode = true,
    ) {
      content()
    }
  }
}
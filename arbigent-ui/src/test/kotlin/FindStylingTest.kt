package io.github.takahirom.arbigent.ui

import org.junit.Test
import java.lang.reflect.Modifier

class FindStylingTest {
  @Test
  fun testListStyling() {
    try {
      val clazz = Class.forName("org.jetbrains.jewel.window.DecoratedWindowKt")
      println("--- Class: ${clazz.name} ---")
      for (method in clazz.declaredMethods) {
        println("  Method: ${method.name} -> ${method.returnType.simpleName} (args: ${method.parameterTypes.map { it.simpleName }})")
      }
      for (field in clazz.declaredFields) {
        println("  Field: ${field.name} -> ${field.type.simpleName}")
      }
    } catch (e: Exception) {
      e.printStackTrace()
    }
  }
}

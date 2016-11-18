/*
 * Copyright 2000-2016 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.intellij.formatting.engine

import com.intellij.formatting.FormatTextRanges
import com.intellij.formatting.FormatterImpl
import com.intellij.formatting.engine.testModel.TestFormattingModel
import com.intellij.formatting.engine.testModel.getRoot
import com.intellij.formatting.toFormattingBlock
import com.intellij.openapi.editor.EditorFactory
import com.intellij.openapi.util.TextRange
import com.intellij.psi.codeStyle.CodeStyleSettings
import com.intellij.testFramework.LightPlatformTestCase
import junit.framework.TestCase
import org.junit.Test

class FormatterEngineTests : LightPlatformTestCase() {

  @Test
  fun `test simple alignment`() {
    doTest(
"""
[a0]fooooo [a1]foo
[a0]go [a1]boo
""",
"""
fooooo foo
go     boo
""")
  }

}

fun doTest(before: String, expectedText: String) {
  val root = getRoot(before.trimStart())
  val rootFormattingBlock = root.toFormattingBlock(0)
  val beforeText = root.text

  val document = EditorFactory.getInstance().createDocument(beforeText)
  val model = TestFormattingModel(rootFormattingBlock, document)

  val settings = CodeStyleSettings()
  val textRanges = FormatTextRanges(TextRange(0, document.textLength - 1), true)

  val formatter = FormatterImpl()
  formatter.format(model, settings, settings.indentOptions, textRanges)

  TestCase.assertEquals(document.text, expectedText.trimStart())
}
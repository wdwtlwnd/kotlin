/*
 * Copyright 2010-2016 JetBrains s.r.o.
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

package org.jetbrains.kotlin.idea.codeInsight.postfix

import com.intellij.codeInsight.template.postfix.templates.*
import com.intellij.openapi.editor.Document
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.util.Condition
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import org.jetbrains.kotlin.idea.codeInsight.surroundWith.expression.KotlinWithIfExpressionSurrounder
import org.jetbrains.kotlin.idea.intentions.getTopmostExpression
import org.jetbrains.kotlin.idea.intentions.negate
import org.jetbrains.kotlin.psi.KtExpression
import org.jetbrains.kotlin.psi.KtPsiFactory
import org.jetbrains.kotlin.utils.singletonOrEmptyList


class KtPostfixTemplateProvider : PostfixTemplateProvider {
    override fun getTemplates() = setOf(
            KtNotPostfixTemplate,
            KtIfExpressionPostfixTemplate,
            KtElseExpressionPostfixTemplate,
            KtNotNullPostfixTemplate("notnull"),
            KtNotNullPostfixTemplate("nn"),
            KtIsNullPostfixTemplate
    )

    override fun isTerminalSymbol(currentChar: Char) = currentChar == '.' || currentChar == '!'

    override fun afterExpand(file: PsiFile, editor: Editor) {
    }

    override fun preCheck(copyFile: PsiFile, realEditor: Editor, currentOffset: Int) = copyFile

    override fun preExpand(file: PsiFile, editor: Editor) {
    }
}

private object KtNotPostfixTemplate : NotPostfixTemplate(
        KtPostfixTemplatePsiInfo,
        TopmostExpressionPostfixTemplateSelector
)

private object KtIfExpressionPostfixTemplate : SurroundPostfixTemplateBase(
        "if", "if (expr)",
        KtPostfixTemplatePsiInfo, TopmostExpressionPostfixTemplateSelector
) {
    override fun getSurrounder() = KotlinWithIfExpressionSurrounder(withElse = false)
}

private object KtElseExpressionPostfixTemplate : SurroundPostfixTemplateBase(
        "else", "if (!expr)",
        KtPostfixTemplatePsiInfo, TopmostExpressionPostfixTemplateSelector
) {
    override fun getSurrounder() = KotlinWithIfExpressionSurrounder(withElse = false)
    override fun getWrappedExpression(expression: PsiElement?) = (expression as KtExpression).negate()
}

private class KtNotNullPostfixTemplate(val name: String) : SurroundPostfixTemplateBase(
        name, "if (expr != null)",
        KtPostfixTemplatePsiInfo, TopmostExpressionPostfixTemplateSelector
) {
    override fun getSurrounder() = KotlinWithIfExpressionSurrounder(withElse = false)
    override fun getTail() = "!= null"
}

private object KtIsNullPostfixTemplate : SurroundPostfixTemplateBase(
        "null", "if (expr == null)",
        KtPostfixTemplatePsiInfo, TopmostExpressionPostfixTemplateSelector
) {
    override fun getSurrounder() = KotlinWithIfExpressionSurrounder(withElse = false)
    override fun getTail() = "== null"
}

private object KtPostfixTemplatePsiInfo : PostfixTemplatePsiInfo() {
    override fun createExpression(context: PsiElement, prefix: String, suffix: String) =
            KtPsiFactory(context.project).createExpression(prefix + context.text + suffix)

    override fun getNegatedExpression(element: PsiElement) = (element as KtExpression).negate()

}

object TopmostExpressionPostfixTemplateSelector : PostfixTemplateExpressionSelectorBase(Condition { it is KtExpression }) {
    override fun getNonFilteredExpressions(context: PsiElement, document: Document, offset: Int) =
            getTopmostExpression(context).singletonOrEmptyList()
}

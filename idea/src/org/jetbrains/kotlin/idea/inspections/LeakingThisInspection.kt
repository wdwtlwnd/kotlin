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

package org.jetbrains.kotlin.idea.inspections

import com.intellij.codeInspection.LocalInspectionToolSession
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.psi.PsiElementVisitor
import org.jetbrains.kotlin.cfg.LeakingThisDescriptor
import org.jetbrains.kotlin.cfg.NonFinalClassLeakingThis
import org.jetbrains.kotlin.cfg.NonFinalPropertyLeakingThis
import org.jetbrains.kotlin.cfg.PropertyIsNullLeakingThis
import org.jetbrains.kotlin.idea.caches.resolve.analyzeFully
import org.jetbrains.kotlin.psi.KtClassOrObject
import org.jetbrains.kotlin.psi.KtVisitorVoid
import org.jetbrains.kotlin.resolve.BindingContext

class LeakingThisInspection : AbstractKotlinInspection() {
    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean, session: LocalInspectionToolSession): PsiElementVisitor {
        return object : KtVisitorVoid() {
            override fun visitClassOrObject(klass: KtClassOrObject) {
                val context = klass.analyzeFully()
                for (expression in context.getKeys(BindingContext.LEAKING_THIS)) {
                    val leakingThisDescriptor = context.get(BindingContext.LEAKING_THIS, expression)
                    val description = when (leakingThisDescriptor) {
                        is PropertyIsNullLeakingThis ->
                            "NPE risk: leaking this while not-null ${leakingThisDescriptor.property.name.asString()} is still null"
                        is NonFinalClassLeakingThis ->
                            "Leaking this in non-final class ${leakingThisDescriptor.klass.name.asString()}"
                        is NonFinalPropertyLeakingThis ->
                            "Leaking this accessing non-final property ${leakingThisDescriptor.property.name.asString()}"
                        else -> null
                    }
                    if (description != null) {
                        holder.registerProblem(expression, description, ProblemHighlightType.WEAK_WARNING)
                    }
                }
            }
        }
    }
}
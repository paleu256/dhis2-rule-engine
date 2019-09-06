package org.hisp.dhis.rules.models

import kotlin.jvm.JvmStatic


data class RuleActionShowOptionGroup(val content: String?,
                                     val optionGroup: String?,
                                     val field: String?) : RuleAction() {

    companion object {

        @JvmStatic fun create(content: String?, optionGroup: String, field: String) =
                RuleActionShowOptionGroup(content ?: "", optionGroup, field)

    }
}
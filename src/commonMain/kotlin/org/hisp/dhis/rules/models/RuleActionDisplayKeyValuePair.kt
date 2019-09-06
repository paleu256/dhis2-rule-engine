package org.hisp.dhis.rules.models

import kotlin.jvm.JvmStatic


data class RuleActionDisplayKeyValuePair(override var content: String?,
                                         override var data: String?,
                                         override var location: String?) : RuleActionText() {

    companion object {

        @JvmStatic fun createForFeedback(content: String?, data: String?): RuleActionDisplayKeyValuePair {
            return when {
                content == null && data == null -> throw IllegalArgumentException("Both content and data must not be null")
                else -> RuleActionDisplayKeyValuePair(content ?: "", data ?: "", LOCATION_FEEDBACK_WIDGET)
            }
        }

        @JvmStatic fun createForIndicators(content: String?, data: String?): RuleActionDisplayKeyValuePair {
            return when {
                content == null && data == null -> throw IllegalArgumentException("Both content and data must not be null")
                else -> RuleActionDisplayKeyValuePair(content ?: "", data ?: "", LOCATION_INDICATOR_WIDGET)
            }
        }
    }
}
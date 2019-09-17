package org.hisp.dhis.rules.functions

/*
 * Copyright (c) 2004-2018, University of Oslo
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 *
 * Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 * Neither the name of the HISP project nor the names of its contributors may
 * be used to endorse or promote products derived from this software without
 * specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

import org.hisp.dhis.rules.RuleVariableValue
import org.hisp.dhis.rules.wrap
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import kotlin.jvm.JvmStatic

actual class RuleFunctionAddDays : RuleFunction() {

    actual override fun evaluate(arguments: List<String?>, valueMap: Map<String, RuleVariableValue>?,
                                 supplementaryData: Map<String, List<String>>?): String {
        when {
            arguments.size != 2 -> throw IllegalArgumentException("Two arguments were expected, ${arguments.size} were supplied")
            else -> return addDays(arguments[0], arguments[1]).wrap()
        }
    }

    actual companion object {
        actual val D2_ADD_DAYS = "d2:addDays"

        @JvmStatic actual fun create() = RuleFunctionAddDays()

        /**
         * Function which will return the the date after adding/subtracting number of days.
         *
         * @param inputDate the date to add/subtract from.
         * @param days  number of days to add/subtract.
         * @return date after adding/subtracting days.
         */
        @JvmStatic actual fun addDays(inputDate: String?, days: String?): String {
            val formatter = DateTimeFormatter.ofPattern(DATE_PATTERN)

            try {
                val date = LocalDate.parse(inputDate, formatter)
                return date.plusDays(days!!.toLong()).toString()
            } catch (ex: DateTimeParseException) {
                throw RuntimeException(ex)
            }
        }
    }
}

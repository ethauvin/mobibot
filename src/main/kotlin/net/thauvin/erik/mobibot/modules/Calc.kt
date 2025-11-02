/*
 * Calc.kt
 *
 * Copyright 2004-2025 Erik C. Thauvin (erik@thauvin.net)
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *   Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 *
 *   Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 *
 *   Neither the name of this project nor the names of its contributors may be
 *   used to endorse or promote products derived from this software without
 *   specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package net.thauvin.erik.mobibot.modules

import net.objecthunter.exp4j.ExpressionBuilder
import net.objecthunter.exp4j.tokenizer.UnknownFunctionOrVariableException
import net.thauvin.erik.mobibot.Utils.bold
import net.thauvin.erik.mobibot.Utils.helpFormat
import org.pircbotx.hooks.types.GenericMessageEvent
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.text.DecimalFormat

/**
 * Performs a calculation.
 */
class Calc : AbstractModule() {
    private val logger: Logger = LoggerFactory.getLogger(Calc::class.java)

    override val name = "Calc"

    companion object {
        // Calc command
        private const val CALC_CMD = "calc"

        /**
         * Performs a calculation (e.g.: 1 + 1 * 2)
         */
        @JvmStatic
        @Throws(IllegalArgumentException::class)
        fun calculate(query: String): String {
            val decimalFormat = DecimalFormat("#.##")
            val calc = ExpressionBuilder(query).build()
            return query.replace(" ", "") + " = " + decimalFormat.format(calc.evaluate()).bold()
        }
    }

    init {
        addCommand(CALC_CMD)
        addHelp(
            "To solve a mathematical calculation:",
            helpFormat("%c $CALC_CMD <calculation>")
        )
    }

    override fun commandResponse(channel: String, cmd: String, args: String, event: GenericMessageEvent) {
        if (args.isNotBlank()) {
            try {
                event.respond(calculate(args))
            } catch (e: UnknownFunctionOrVariableException) {
                if (logger.isWarnEnabled) logger.warn("Unable to calculate: $args", e)
                event.respond("No idea. This is the kind of math I don't get.")
            } catch (e: IllegalArgumentException) {
                if (logger.isWarnEnabled) logger.warn("Failed to calculate: $args", e)
                event.respond("No idea. I must've some form of Dyscalculia.")
            }
        } else {
            helpResponse(event)
        }
    }
}

/*
 * CurrencyConverter.java
 *
 * Copyright (c) 2004-2020, Erik C. Thauvin (erik@thauvin.net)
 * All rights reserved.
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

package net.thauvin.erik.mobibot.modules;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import net.thauvin.erik.mobibot.Constants;
import net.thauvin.erik.mobibot.Mobibot;
import net.thauvin.erik.mobibot.Utils;
import net.thauvin.erik.mobibot.msg.ErrorMessage;
import net.thauvin.erik.mobibot.msg.Message;
import net.thauvin.erik.mobibot.msg.PublicMessage;
import org.apache.commons.lang3.StringUtils;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.Namespace;
import org.jdom2.input.SAXBuilder;

import javax.xml.XMLConstants;
import java.io.IOException;
import java.net.URL;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * The CurrentConverter module.
 *
 * @author <a href="https://erik.thauvin.net" target="_blank">Erik C. Thauvin</a>
 * @created Feb 11, 2004
 * @since 1.0
 */
@SuppressWarnings("PMD.UseConcurrentHashMap")
public final class CurrencyConverter extends ThreadedModule {
    // Currency command
    private static final String CURRENCY_CMD = "currency";
    // Rates keyword
    private static final String CURRENCY_RATES_KEYWORD = "rates";
    // Empty rate table.
    private static final String EMPTY_RATE_TABLE = "Sorry, but the exchange rate table is empty.";
    // Exchange rates
    private static final Map<String, String> EXCHANGE_RATES = new TreeMap<>();
    // Exchange rates table URL
    private static final String EXCHANGE_TABLE_URL = "https://www.ecb.europa.eu/stats/eurofxref/eurofxref-daily.xml";
    // Last exchange rates table publication date
    private static String pubDate = "";

    /**
     * Creates a new {@link CurrencyConverter} instance.
     */
    public CurrencyConverter() {
        super();
        commands.add(CURRENCY_CMD);
    }

    /**
     * Converts from a currency to another.
     *
     * <p>100 USD to EUR</p>
     *
     * @param query The query.
     * @return The {@link Message} contained the converted currency.
     */
    static Message convertCurrency(final String query) {
        final String[] cmds = query.split(" ");

        if (cmds.length == 4) {
            if (cmds[3].equals(cmds[1]) || "0".equals(cmds[0])) {
                return new PublicMessage("You're kidding, right?");
            } else {
                final String to = cmds[1].toUpperCase(Constants.LOCALE);
                final String from = cmds[3].toUpperCase(Constants.LOCALE);

                if (EXCHANGE_RATES.containsKey(to) && EXCHANGE_RATES.containsKey(from)) {
                    try {
                        final double amt = Double.parseDouble(cmds[0].replace(",", ""));
                        final double doubleFrom = Double.parseDouble(EXCHANGE_RATES.get(to));
                        final double doubleTo = Double.parseDouble(EXCHANGE_RATES.get(from));

                        return new PublicMessage(
                                NumberFormat.getCurrencyInstance(Constants.LOCALE).format(amt).substring(1)
                                + ' '
                                + cmds[1].toUpperCase(Constants.LOCALE)
                                + " = "
                                + NumberFormat.getCurrencyInstance(Constants.LOCALE)
                                              .format((amt * doubleTo) / doubleFrom)
                                              .substring(1)
                                + ' '
                                + cmds[3].toUpperCase(Constants.LOCALE));
                    } catch (NumberFormatException e) {
                        return new ErrorMessage("Let's try with some real numbers next time, okay?");
                    }
                } else {
                    return new ErrorMessage("Sounds like monopoly money to me!");
                }
            }
        }
        return new ErrorMessage("Invalid query. Let's try again.");
    }

    static List<String> currencyRates() {
        final List<String> rates = new ArrayList<>(33);
        for (final Map.Entry<String, String> rate : EXCHANGE_RATES.entrySet()) {
            rates.add("  " + rate.getKey() + ": " + StringUtils.leftPad(rate.getValue(), 8));
        }

        return rates;
    }

    static void loadRates() throws ModuleException {
        if (EXCHANGE_RATES.isEmpty()) {
            try {
                final SAXBuilder builder = new SAXBuilder();
                // See https://rules.sonarsourcecom/java/tag/owasp/RSPEC-2755
                builder.setProperty(XMLConstants.ACCESS_EXTERNAL_DTD, "");
                builder.setProperty(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
                builder.setIgnoringElementContentWhitespace(true);

                final Document doc = builder.build(new URL(EXCHANGE_TABLE_URL));
                final Element root = doc.getRootElement();
                final Namespace ns = root.getNamespace("");
                final Element cubeRoot = root.getChild("Cube", ns);
                final Element cubeTime = cubeRoot.getChild("Cube", ns);

                pubDate = cubeTime.getAttribute("time").getValue();

                final List<Element> cubes = cubeTime.getChildren();

                for (final Element cube : cubes) {
                    EXCHANGE_RATES.put(
                            cube.getAttribute("currency").getValue(),
                            cube.getAttribute("rate").getValue());
                }

                EXCHANGE_RATES.put("EUR", "1");
            } catch (JDOMException | IOException e) {
                throw new ModuleException(e.getMessage(),
                                          "An error has occurred while parsing the exchange rates table.",
                                          e);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void commandResponse(final Mobibot bot,
                                final String sender,
                                final String cmd,
                                final String args,
                                final boolean isPrivate) {
        synchronized (this) {
            if (!pubDate.equals(Utils.today())) {
                EXCHANGE_RATES.clear();
            }
        }

        super.commandResponse(bot, sender, cmd, args, isPrivate);
    }

    /**
     * Converts the specified currencies.
     */
    @SuppressFBWarnings("REDOS")
    @Override
    void run(final Mobibot bot, final String sender, final String cmd, final String query, final boolean isPrivate) {
        if (EXCHANGE_RATES.isEmpty()) {
            try {
                loadRates();
            } catch (ModuleException e) {
                bot.getLogger().warn(e.getDebugMessage(), e);
            }
        }

        if (EXCHANGE_RATES.isEmpty()) {
            bot.send(sender, EMPTY_RATE_TABLE, true);
        } else if (query.matches("\\d+([,\\d]+)?(\\.\\d+)? [a-zA-Z]{3}+ to [a-zA-Z]{3}+")) {
            final Message msg = convertCurrency(query);
            bot.send(sender, msg);
            if (msg.isError()) {
                helpResponse(bot, sender, isPrivate);
            }
        } else if (query.contains(CURRENCY_RATES_KEYWORD)) {
            bot.send(sender, "The currency rates for " + Utils.bold(pubDate) + " are:", isPrivate);
            bot.sendCommandsList(sender, currencyRates(), 3, isPrivate, false);
        } else {
            helpResponse(bot, sender, isPrivate);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void helpResponse(final Mobibot bot, final String sender, final boolean isPrivate) {
        if (EXCHANGE_RATES.isEmpty()) {
            try {
                loadRates();
            } catch (ModuleException e) {
                bot.getLogger().debug(e.getDebugMessage(), e);
            }
        }
        if (EXCHANGE_RATES.isEmpty()) {
            bot.send(sender, EMPTY_RATE_TABLE, isPrivate);
        } else {
            bot.send(sender, "To convert from one currency to another:", isPrivate);
            bot.send(sender, Utils.helpIndent(bot.getNick() + ": " + CURRENCY_CMD + " 100 USD to EUR"), isPrivate);
            bot.send(sender, "For a listing of current rates:", isPrivate);
            bot.send(sender,
                     Utils.helpIndent(bot.getNick() + ": " + CURRENCY_CMD) + ' ' + CURRENCY_RATES_KEYWORD,
                     isPrivate);
            bot.send(sender, "The supported currencies are: ", isPrivate);
            bot.sendCommandsList(sender, new ArrayList<>(EXCHANGE_RATES.keySet()), 11, isPrivate, false);
        }
    }
}

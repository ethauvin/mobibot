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
import net.thauvin.erik.mobibot.msg.NoticeMessage;
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
import java.util.List;
import java.util.Locale;
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
    /**
     * The rates keyword.
     */
    static final String CURRENCY_RATES_KEYWORD = "rates";

    // Currency command
    private static final String CURRENCY_CMD = "currency";
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
     * @throws ModuleException If an error occurs while converting.
     */
    static Message convertCurrency(final String query) throws ModuleException {
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
                Element cube;

                for (final Element rawCube : cubes) {
                    cube = rawCube;
                    EXCHANGE_RATES.put(
                            cube.getAttribute("currency").getValue(),
                            cube.getAttribute("rate").getValue());
                }

                EXCHANGE_RATES.put("EUR", "1");
            } catch (JDOMException e) {
                throw new ModuleException(query, "An error has occurred while parsing the exchange rates table.", e);
            } catch (IOException e) {
                throw new ModuleException(
                        query, "An error has occurred while fetching the exchange rates table.", e);
            }
        }

        if (EXCHANGE_RATES.isEmpty()) {
            return new ErrorMessage("Sorry, but the exchange rate table is empty.");
        } else {
            final String[] cmds = query.split(" ");

            if (cmds.length == 4) {
                if (cmds[3].equals(cmds[1]) || "0".equals(cmds[0])) {
                    return new ErrorMessage("You're kidding, right?");
                } else {
                    try {
                        final double amt = Double.parseDouble(cmds[0].replace(",", ""));
                        final double from =
                                Double.parseDouble(EXCHANGE_RATES.get(cmds[1].toUpperCase(Constants.LOCALE)));
                        final double to = Double.parseDouble(EXCHANGE_RATES.get(cmds[3].toUpperCase(Constants.LOCALE)));

                        return new PublicMessage(
                                NumberFormat.getCurrencyInstance(Locale.US).format(amt).substring(1)
                                + ' '
                                + cmds[1].toUpperCase(Constants.LOCALE)
                                + " = "
                                + NumberFormat.getCurrencyInstance(Locale.US)
                                              .format((amt * to) / from)
                                              .substring(1)
                                + ' '
                                + cmds[3].toUpperCase(Constants.LOCALE));
                    } catch (Exception e) {
                        throw new ModuleException("convertCurrency(" + query + ')',
                                                  "The supported currencies are: " + EXCHANGE_RATES.keySet(), e);
                    }
                }
            } else if (CURRENCY_RATES_KEYWORD.equals(query)) {

                final StringBuilder buff = new StringBuilder().append('[').append(pubDate).append("]: ");

                int i = 0;
                for (final Map.Entry<String, String> rate : EXCHANGE_RATES.entrySet()) {
                    if (i > 0) {
                        buff.append(", ");
                    }
                    buff.append(rate.getKey()).append(": ").append(rate.getValue());
                    i++;
                }

                return new NoticeMessage(buff.toString());
            }
        }
        return new ErrorMessage("The supported currencies are: " + EXCHANGE_RATES.keySet());
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
    void run(final Mobibot bot, final String sender, final String cmd, final String query) {
        if (StringUtils.isNotBlank(sender) && StringUtils.isNotBlank(query)) {
            if (query.matches("\\d+([,\\d]+)?(\\.\\d+)? [a-zA-Z]{3}+ to [a-zA-Z]{3}+")) {
                try {
                    final Message msg = convertCurrency(query);
                    if (msg.isError()) {
                        helpResponse(bot, sender, CURRENCY_CMD + ' ' + query, false);
                    }
                    bot.send(sender, msg);
                } catch (ModuleException e) {
                    bot.getLogger().warn(e.getDebugMessage(), e);
                    bot.send(sender, e.getMessage());
                }
            } else {
                helpResponse(bot, sender, CURRENCY_CMD + ' ' + query, true);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void helpResponse(final Mobibot bot, final String sender, final String args, final boolean isPrivate) {
        bot.send(sender, "To convert from one currency to another:");
        bot.send(sender, bot.helpIndent(bot.getNick() + ": " + CURRENCY_CMD + " [100 USD to EUR]"));

        if (args.endsWith(CURRENCY_CMD)) {
            bot.send(sender, "For a listing of currency rates:");
            bot.send(sender, bot.helpIndent(bot.getNick() + ": " + CURRENCY_CMD) + ' ' + CURRENCY_RATES_KEYWORD);
            bot.send(sender, "For a listing of supported currencies:");
            bot.send(sender, bot.helpIndent(bot.getNick() + ": " + CURRENCY_CMD));
        }
    }
}

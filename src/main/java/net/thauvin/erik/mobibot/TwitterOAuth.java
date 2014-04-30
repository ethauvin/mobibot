package net.thauvin.erik.mobibot;

import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;

import java.io.BufferedReader;
import java.io.InputStreamReader;

/**
 * The <code>TwitterOAuth</code> class.
 * <p>
 * Go to <a href="http://twitter.com/oauth_clients/new">http://twitter.com/oauth_clients/new</a> to register your bot.
 * </p>
 * Then execute:
 * <p>
 * <code>java -cp "mobibot.jar:lib/*" net.thauvin.erik.mobibot.TwitterOAuth &lt;consumerKey&gt; &lt;consumerSecret&gt;</code>
 * </p>
 * and follow the prompts/instructions.
 *
 * @author <a href="mailto:erik@thauvin.net">Erik C. Thauvin</a>
 * @author <a href="http://twitter4j.org/en/code-examples.html#oauth">http://twitter4j.org/en/code-examples.html#oauth</a>
 * @created Sep 13, 2010
 * @since 1.0
 */
public class TwitterOAuth
{
	public static void main(String args[])
			throws Exception
	{
		if (args.length == 2)
		{
			final twitter4j.Twitter twitter = new TwitterFactory().getInstance();
			twitter.setOAuthConsumer(args[0], args[1]);
			final RequestToken requestToken = twitter.getOAuthRequestToken();
			AccessToken accessToken = null;
			final BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
			while (null == accessToken)
			{
				System.out.println("Open the following URL and grant access to your account:");
				System.out.println(requestToken.getAuthorizationURL());
				System.out.print("Enter the PIN (if available) or just hit enter.[PIN]:");
				final String pin = br.readLine();
				try
				{
					if (pin.length() > 0)
					{
						accessToken = twitter.getOAuthAccessToken(requestToken, pin);
					}
					else
					{
						accessToken = twitter.getOAuthAccessToken();
					}

					System.out.println(
							"Please add the following to the bot's property file:" + "\n\n" + "twitter-consumerKey="
							+ args[0] + '\n' + "twitter-consumerSecret=" + args[1] + '\n' + "twitter-token="
							+ accessToken.getToken() + '\n' + "twitter-tokenSecret=" + accessToken.getTokenSecret()
					);
				}
				catch (TwitterException te)
				{
					if (401 == te.getStatusCode())
					{
						System.out.println("Unable to get the access token.");
					}
					else
					{
						te.printStackTrace();
					}
				}
			}
		}
		else
		{
			System.out.println("Usage: " + TwitterOAuth.class.getName() + " <consumerKey> <consumerSecret>");
		}

		System.exit(0);
	}
}
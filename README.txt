Some very basic instructions:

	{ clone with git or download the ZIP }
	git clone git://github.com/ethauvin/mobibot.git
	
	cd mobibot
	
	{ build with gradle }
	./gradlew

	cd deploy

	{ configure the properties }
	vi *.properties
	
	{ help }
	java -jar mobibot.jar -h

	{ twitter oauth token request }
	java -cp mobibot.jar net.thauvin.erik.mobibot.TwitterOAuth <consumerKey> <consumerSecret>

	{ launch }
	/usr/bin/nohup java -jar mobibot.jar &

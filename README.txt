Some very basic instructions:

	ant jar
	
	mkdir run
	
	cp dist/mobibot.jar run
	cp -R lib run
	cp properties/*.properties run
	
	cd run
	
	mkdir logs

	{ configure the properties }
	vi *.properties
	
	{ debug }
	java -jar mobibot.jar -debug
	
	{ launch }
	/usr/bin/nohup java -jar mobibot.jar &
	
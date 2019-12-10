App.class: App.java
	javac -cp libs/desktop.jar:libs/lib.jar:libs/junit-4.11.jar:libs/api_classic.jar:libs/bcprov-debug-jdk15to18-1.64.jar:libs/junit-4.11.jar App.java

test: App.class
	java -cp libs/lib.jar:libs/desktop.jar:libs/bcprov-debug-jdk15to18-1.64.jar:libs/junit-4.11.jar:. App

updatejars:
	md5sum libs/{desktop.jar,lib.jar}
	docker cp android2:/tmp/lib.jar libs
	docker cp android2:/tmp/desktop.jar libs
	@echo "------- after checksum --------"    
	md5sum libs/{desktop.jar,lib.jar}

clean:
	@rm -f App.class

.phony: clean

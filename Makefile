.PHONY: server health android-assemble

server:
	./server/run.sh

health:
	curl -sS http://localhost:8080/health; echo

android-assemble:
	cd android && ./gradlew :app:assembleDebug

default:
	@echo "open Android Studio, then [Build]->[Generate Signed APK]"

test:
	./gradlew assembleDebug

uninstall:
	adb -d uninstall org.shokai.voicetweet

install:
	@echo "install Signed APK"
	adb -d install mobile/mobile-release.apk

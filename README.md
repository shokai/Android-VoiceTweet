# VoiceTweet for Android Wear

Voice-input then Tweet.

[![Circle CI Status](https://circleci.com/gh/shokai/Android-VoiceTweet.png)](https://circleci.com/gh/shokai/Android-VoiceTweet)

- https://github.com/shokai/Android-VoiceTweet

## Install apk

- [latest release](https://github.com/shokai/Android-VoiceTweet/releases/latest)


# Develop

## Requirements

- Android
- [Android Wear](https://developer.android.com/wear/)
- [Android Studio](https://developer.android.com/sdk/)


## Twitter Config

- [Register your app on Twitter](http://apps.twitter.com/)
  - and get OAuth Consumer Key and Secret.
- in `mobile/src/main/java/org/shokai/voicetweet/`
  - copy `TwitterConfig.java.sample` to `TwitterConfig.java`
  - set OAuth Consumer Key and Secret.


## Build

use Android Studio or run `./gradlew assembleDebug`


# License

MIT
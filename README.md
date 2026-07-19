# GuitarTuna Popup Suppressor

[![Build](https://github.com/Seao3oiio/guitartuna-popup-suppressor/actions/workflows/build.yml/badge.svg)](https://github.com/Seao3oiio/guitartuna-popup-suppressor/actions/workflows/build.yml)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

A minimal legacy Xposed module that suppresses GuitarTuna's recurring
full-screen trial prompt. The module is scoped exclusively to
`com.ovelin.guitartuna`.

This project does not unlock paid features, change purchase records, modify
subscription state, or patch the GuitarTuna APK.

## How it works

GuitarTuna stores the app-start session and the last session that displayed the
conversion prompt in Unity PlayerPrefs. The module:

1. Pre-arms the prompt marker during `Application.attach`, before Unity can
   cache PlayerPrefs.
2. Intercepts direct reads of the marker as a fallback.
3. Runs only in GuitarTuna's main process.

The only persisted change is the popup session marker.

## Compatibility

Validated with:

- GuitarTuna `7.96.1` (`com.ovelin.guitartuna`)
- Vector `2.0 (3021)` using the legacy Xposed API
- Android 16

Other Xposed-compatible frameworks may work but have not been tested. Internal
PlayerPrefs names can change in future GuitarTuna releases, causing the module
to stop matching safely.

## Build

The project uses the latest stable toolchain available at the time of release:

- JDK `26.0.1`
- Gradle `9.6.1`
- Android Gradle Plugin `9.3.0`
- Android SDK and Build Tools `37`

Install JDK 26 and Android SDK 37, then run:

```sh
./gradlew :app:assembleDebug
```

The APK is written to:

```text
app/build/outputs/apk/debug/app-debug.apk
```

## Install

```sh
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

In Vector or another compatible Xposed manager:

1. Enable **GuitarTuna Popup Suppressor**.
2. Select only **GuitarTuna** (`com.ovelin.guitartuna`) as its scope.
3. Force-stop and reopen GuitarTuna.

Successful activation is visible in framework logs under the tag
`GuitarTunaPopupSuppressor`.

## Remove

Disable the module in the Xposed manager, then uninstall it:

```sh
adb uninstall io.github.seao3oiio.guitartuna.popupblocker
```

The popup marker remains at its last numeric value and requires no manual data
restoration.

## Disclaimer

This is an independent interoperability project. It is not affiliated with,
endorsed by, or sponsored by Yousician or GuitarTuna. GuitarTuna is a trademark
of its respective owner.

## License

[MIT](LICENSE)

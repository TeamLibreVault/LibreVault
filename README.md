# LibreVault for Android

LibreVault is an **open-source media vault** for Android that lets you securely hide, preview, restore, and manage your private images and videos — built with **Kotlin**, **Jetpack Compose**, and **Material 3**.

> ⚠️ **Current version:** `1.1.0-alpha01`  
> LibreVault is in **early alpha**. Expect bugs, missing features, and breaking changes.

[![Build Status](https://img.shields.io/badge/build-passing-brightgreen)](https://github.com/TeamLibreVault/LibreVault)
[![License](https://img.shields.io/badge/license-GNU%20GPL%20v3-blue.svg)](LICENSE)
[![Platform](https://img.shields.io/badge/platform-Android-lightgrey.svg)]()
[![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-Modern%20UI-blue?logo=jetpack-compose)](https://developer.android.com/jetpack/compose)

---

## ✨ Features (Alpha)

LibreVault currently supports:

- 🔐 **Biometric app lock** (uses device biometrics)
- 📸 **Add images and videos** to the vault
- 👀 **Preview media** directly inside the app
- ♻️ **Restore media** back to its original location
- 🗑️ **Delete media** from the vault
- ⏱ **Lock timeout support**
- 🔒 **Encrypted media storage**
- 🎨 **Material 3 UI** powered by Jetpack Compose
- ⚡ Fully written in **Kotlin**

---

## 🔐 Security & Encryption

LibreVault is designed with privacy and security in mind:

- A **base encryption key** is protected using the **Android Keystore**
- The base key is **decrypted only after successful authentication**
- User media is **encrypted and decrypted locally on-device**
- **Biometric authentication** is handled by the system (fingerprint / face / device credentials)
- No media or encryption keys ever leave the device

> ⚠️ While LibreVault uses modern Android security APIs, it is still **alpha software** and should not be considered a replacement for full device encryption.

---

## 📥 Download

You can download the latest APK from the **Latest Releases** page:

👉 **[Download LibreVault (Latest Release)](https://github.com/TeamLibreVault/LibreVault/releases/latest)**

> ℹ️ LibreVault is currently in **alpha**. It is recommended to back up your media before use.

---

## 🛠 Tech Stack

- **Language:** Kotlin  
- **UI:** Jetpack Compose + Material 3  
- **Security:** Android Keystore + system biometrics  
- **Architecture:** Modern Android app architecture  
- **Minimum SDK:** Android 8.0 (API 26)

---

## 🚧 Planned Features

- 🗂 Media organization (folders, tags)
- ☁️ Backup / export support
- 🔍 Search and filtering
- 🧪 Improved stability and performance

---

## 🤝 Contributing

Contributions are **very welcome** ❤️

- Fork the repository
- Create a feature branch
- Commit your changes
- Open a pull request

Bug reports, feature requests, and UI improvements are all appreciated.

---

## 🐛 Bug Reports & Feedback

If you encounter any issues, please open an issue on GitHub.

Feedback helps shape LibreVault’s future.

---

## 📄 License

Licensed under the **GNU GPL v3 License**.  
See the [LICENSE](LICENSE) file for details.

---

## ❤️ Acknowledgements

- Android Open Source Project
- Jetpack Compose & Material Design teams
- Open-source contributors

---

> LibreVault aims to be a **free, transparent, and privacy-respecting** media vault for Android.

<div align="center">

<img src="assets/header.svg" alt="PassGen — modern password generator and local password vault" width="100%" />

<p>
  <a href="https://github.com/MrCheeku/PassGen"><img src="https://img.shields.io/github/stars/MrCheeku/PassGen?style=for-the-badge&label=STARS&cacheSeconds=60&v=0" alt="GitHub stars" /></a>
  <a href="https://github.com/MrCheeku/PassGen"><img src="https://img.shields.io/github/last-commit/MrCheeku/PassGen?style=for-the-badge&label=UPDATED" alt="Last commit" /></a>
  <a href="https://github.com/MrCheeku/PassGen"><img src="https://img.shields.io/github/repo-size/MrCheeku/PassGen?style=for-the-badge&label=SIZE" alt="Repository size" /></a>
</p>

<p><strong>Strong password generation • Local vault storage • Encrypted backups</strong></p>

<a href="https://github.com/MrCheeku"><img src="https://img.shields.io/badge/ENGINEERED%20BY-Mr.Cheeku-111827?style=for-the-badge&logo=github&logoColor=white" alt="Engineered by Mr.Cheeku — open GitHub profile" /></a>

</div>

---

## 🔐 What is PassGen?

**PassGen** is a modern Android password generator and personal credential vault designed around a **local-first** workflow. The project combines secure password generation, credential organization, a master lock, and encrypted backup import/export in one focused app.

### ✨ Highlights

| Capability | What it does |
|---|---|
| **Password Generator** | Generates passwords from uppercase, lowercase, numbers, and symbols with configurable length from 6–64 characters. |
| **Password Vault** | Stores credential records including title, username, password, website, notes, favorites, and timestamps. |
| **Master Lock** | Supports an app-level PIN lock with salted PBKDF2-HMAC-SHA256 verification. |
| **At-Rest Encryption** | Uses AES-256-GCM for sensitive vault fields and Android Keystore-backed key material when available. |
| **Encrypted Backups** | Exports and imports the vault through an encrypted JSON envelope protected by a user-supplied password. |
| **Theme Support** | Includes persisted system, dark, and light theme preferences. |

> **Security note:** the current implementation includes a fallback local key path for environments where Android Keystore is unavailable. Review that behavior carefully before treating the project as a production security product.

---

## ✨ Core Experience

```text
Generate → Review Strength → Save Credential → Lock Vault → Export Encrypted Backup
```

PassGen's UI is built from focused Jetpack Compose screens and reusable components, including dashboard, generator, master-lock, settings, credential cards, password-strength UI, and add/edit flows.

---

## 🧰 Tech Stack

<div align="center">

### Android & UI
<img src="https://skillicons.dev/icons?i=android,kotlin,gradle" alt="Android, Kotlin, Gradle" />

### Architecture & Storage
<img src="https://img.shields.io/badge/Jetpack%20Compose-UI-4285F4?style=flat-square&logo=android&logoColor=white" alt="Jetpack Compose" />
<img src="https://img.shields.io/badge/Room-Local%20Database-6B4EFF?style=flat-square" alt="Room" />
<img src="https://img.shields.io/badge/Retrofit-Network%20Layer-48B983?style=flat-square" alt="Retrofit" />
<img src="https://img.shields.io/badge/OkHttp-HTTP%20Client-111827?style=flat-square" alt="OkHttp" />
<img src="https://img.shields.io/badge/Moshi-JSON-111827?style=flat-square" alt="Moshi" />

### Security
<img src="https://img.shields.io/badge/AES--256--GCM-Encryption-111827?style=flat-square" alt="AES-256-GCM" />
<img src="https://img.shields.io/badge/PBKDF2--HMAC--SHA256-Key%20Derivation-111827?style=flat-square" alt="PBKDF2 HMAC SHA-256" />
<img src="https://img.shields.io/badge/Android%20Keystore-Key%20Protection-111827?style=flat-square&logo=android&logoColor=white" alt="Android Keystore" />

</div>

---

## 🧭 Project Structure

> 🎨 **Interactive Mermaid architecture:** every major layer has its own color, arrows show how the folders connect, and the compact top-to-bottom layout is designed for smaller screens.

```mermaid
flowchart TB
    A[🔐 PassGen] --> B[📱 app/]
    B --> C[🧩 src/main/]
    C --> D[☕ java/com/example/]

    D --> E[🗄️ data/]
    E --> E1[AppDatabase.kt]
    E --> E2[VaultDao.kt]
    E --> E3[VaultEntity.kt]
    E --> E4[VaultItem.kt]
    E --> E5[VaultRepository.kt]

    D --> F[🛡️ security/]
    F --> F1[PasswordGenerator.kt]
    F --> F2[PasswordHealthAnalyzer.kt]
    F --> F3[PasswordStrength.kt]
    F --> F4[VaultSecurityManager.kt]

    D --> G[🎨 ui/]
    G --> G1[PassGenApp.kt]
    G --> G2[VaultViewModel.kt]
    G --> G3[components/]
    G --> G4[screens/]

    C --> H[📄 AndroidManifest.xml]
    A --> I[🎨 assets/]
    I --> I1[header.svg]
    I --> I2[footer.svg]
    A --> J[⚙️ build.gradle.kts]
    A --> K[⚙️ gradle.properties]
    A --> L[📋 metadata.json]
    A --> M[⚙️ settings.gradle.kts]
    A --> N[🔑 .env.example]

    classDef root fill:#7c3aed,stroke:#c4b5fd,color:#ffffff,stroke-width:3px;
    classDef app fill:#2563eb,stroke:#93c5fd,color:#ffffff,stroke-width:2px;
    classDef source fill:#0891b2,stroke:#67e8f9,color:#ffffff,stroke-width:2px;
    classDef data fill:#059669,stroke:#6ee7b7,color:#ffffff,stroke-width:2px;
    classDef security fill:#dc2626,stroke:#fca5a5,color:#ffffff,stroke-width:2px;
    classDef ui fill:#d97706,stroke:#fcd34d,color:#ffffff,stroke-width:2px;
    classDef assets fill:#db2777,stroke:#f9a8d4,color:#ffffff,stroke-width:2px;
    classDef config fill:#475569,stroke:#cbd5e1,color:#ffffff,stroke-width:2px;

    class A root;
    class B app;
    class C,D source;
    class E,E1,E2,E3,E4,E5 data;
    class F,F1,F2,F3,F4 security;
    class G,G1,G2,G3,G4 ui;
    class H app;
    class I,I1,I2 assets;
    class J,K,L,M,N config;

    linkStyle default stroke:#94a3b8,stroke-width:2px;
```

### 📱 Mobile-friendly note

This is **real Mermaid source — not a picture**. The compact `TB` layout keeps the hierarchy narrow, but GitHub controls Mermaid rendering in each client. If the GitHub Android app displays the Mermaid source instead of the rendered diagram, that is a limitation of that app/version and cannot be forced by README code.

<details>
<summary>📂 View Project Structure as Text</summary>

```text
🔐 PassGen
│
├── 📱 app/
│   └── 🧩 src/main/
│       ├── ☕ java/com/example/
│       │   ├── 🗄️ data/
│       │   │   ├── AppDatabase.kt
│       │   │   ├── VaultDao.kt
│       │   │   ├── VaultEntity.kt
│       │   │   ├── VaultItem.kt
│       │   │   └── VaultRepository.kt
│       │   ├── 🛡️ security/
│       │   │   ├── PasswordGenerator.kt
│       │   │   ├── PasswordHealthAnalyzer.kt
│       │   │   ├── PasswordStrength.kt
│       │   │   └── VaultSecurityManager.kt
│       │   └── 🎨 ui/
│       │       ├── PassGenApp.kt
│       │       ├── VaultViewModel.kt
│       │       ├── components/
│       │       └── screens/
│       └── 📄 AndroidManifest.xml
│
├── 🎨 assets/
│   ├── header.svg
│   └── footer.svg
│
├── ⚙️ build.gradle.kts
├── ⚙️ gradle.properties
├── 📋 metadata.json
├── ⚙️ settings.gradle.kts
└── 🔑 .env.example
```

</details>

### 🔄 How the pieces connect

```text
📱 App Entry
    │
    ├── 🎨 UI Layer ────────► Screens + Components
    │             │
    │             └──────────► VaultViewModel
    │                            │
    ├── 🗄️ Data Layer ──────────┴──► Room Database + Repository
    │
    └── 🛡️ Security Layer ─────────► Password Generation + Encryption + Lock
```

The structure is intentionally grouped by responsibility so a new contributor can quickly see where **UI**, **storage**, and **security** logic live.

---

## 🛡️ Security Design

PassGen currently implements several concrete security mechanisms:

- **SecureRandom** is used for password generation and security salts/IVs.
- Vault passwords and sensitive notes are encrypted at rest with **AES/GCM/NoPadding**, using a 256-bit Android Keystore key when available.
- Master-lock PINs are stored as salted **PBKDF2WithHmacSHA256** derived hashes rather than as plaintext PINs.
- Backup files use a separate password-derived key and AES-GCM encrypted payload.

This README describes the implementation currently present in the repository; it is not a claim of independent security auditing.

---

## 🚀 Getting Started

### Requirements

- Android Studio with a recent Android/Gradle toolchain
- JDK 11+
- Android SDK matching the project's configured compile/target SDK

### Clone with HTTPS

```bash
git clone https://github.com/MrCheeku/PassGen.git
cd PassGen
gradle assembleDebug
```

### Clone with SSH

SSH is useful when you regularly clone, pull, or push from your development machine. GitHub uses your SSH key pair for authentication; **keep the private key private** and only add the public key to your GitHub account.

```bash
git clone git@github.com:MrCheeku/PassGen.git
cd PassGen
gradle assembleDebug
```

> The repository currently uses Gradle project configuration but does not include a checked-in `gradlew` wrapper script. Opening the project in Android Studio is the easiest setup path.

The Android module is configured for `compileSdk` 36 and `targetSdk` 36, with Java source/target compatibility set to Java 11.

---

## 🧪 Testing

The Android module includes JVM tests, Android instrumentation tests, Compose UI test dependencies, Robolectric, and Roborazzi testing support.

```bash
gradle test
```

---

## 📦 Backup Format

Encrypted backups are represented as a small JSON envelope:

```json
{
  "app": "PassGen",
  "format": "encrypted_vault_v1",
  "salt": "…",
  "iv": "…",
  "ciphertext": "…"
}
```

The encrypted payload is generated from vault data and a user-supplied export password.

---

## 🌐 Official Links

<div align="center">

<a href="https://techwithcheeku.lovable.app/"><img src="https://img.shields.io/badge/Visit%20Tech%20with%20Cheeku-Website-7c3aed?style=for-the-badge&logo=googlechrome&logoColor=white" alt="Visit Tech with Cheeku website" /></a>

<a href="https://whatsapp.com/channel/0029Vb9OpwgD8SDvISwrn73Y"><img src="https://img.shields.io/badge/Join%20WhatsApp%20Channel-25D366?style=for-the-badge&logo=whatsapp&logoColor=white" alt="Join Tech with Cheeku WhatsApp channel" /></a>

<a href="https://discord.gg/GWJvzcxuN"><img src="https://img.shields.io/badge/Join%20My%20Discord-5865F2?style=for-the-badge&logo=discord&logoColor=white" alt="Join My Discord" /></a>

</div>

---

## 👨‍💻 Engineered By

<div align="center">

<a href="https://github.com/MrCheeku"><img src="https://avatars.githubusercontent.com/u/235286067?v=4" width="88" height="88" alt="Mr.Cheeku GitHub avatar" /></a>

### **Mr.Cheeku**

<a href="https://github.com/MrCheeku"><img src="https://img.shields.io/badge/Visit%20Developer%20Profile-↗-111827?style=for-the-badge&logo=github&logoColor=white" alt="Visit Mr.Cheeku's GitHub profile" /></a>

</div>

---

<div align="center">

<a href="https://github.com/MrCheeku/PassGen/issues">Report an issue</a>
&nbsp;•&nbsp;
<a href="https://github.com/MrCheeku/PassGen">View source</a>
&nbsp;•&nbsp;
<a href="https://github.com/MrCheeku">Developer profile</a>

<br /><br />

<img src="assets/footer.svg" alt="PassGen footer — local-first password security, engineered by Mr.Cheeku" width="100%" />

</div>

<!-- LIVE_STARS: 0 | automatically synced 2026-09-06T17:36:45.220Z -->

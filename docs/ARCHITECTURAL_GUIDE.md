# Architectural Deep Dive: Tier1 Bank Demo

This guide is designed to help you understand the "why" and "how" behind the application's design, preparing you for technical discussions and interviews.

## 1. Code Conventions & Structure

Modern Android development has evolved since the "linear" programming days, but it still follows clear principles of organization.

### Is it like a book? (Start, Middle, End)
Instead of a simple book, think of the app as a **Story Tree**:
- **The Start (The Roots):** `MainActivity.kt` and `AppNavHost.kt`. This is where the app "wakes up" and decides which page to show first.
- **The Middle (The Chapters):** Each "Screen" is a chapter. Each screen has its own **ViewModel** (the brain) and **UI** (the face). They operate independently, making the app modular.
- **The End (The Resolution):** The app reaches a "Success" state (e.g., reaching the Dashboard or finishing a transfer).

### Function Length & Complexity
We strictly follow **Separation of Concerns**:
- **Composability:** Instead of one giant 100-line function for a screen, we break it into smaller `@Composable` functions (e.g., `TransactionRow`, `LoadingContent`). This follows your rule of keeping functions short and focused.
- **Single Responsibility:** A function either *shows* something (View) or *changes* something (ViewModel), but rarely both.

## 2. The MVVM Pattern

MVVM (Model-View-ViewModel) is the gold standard for modern Android apps because it keeps the different parts of the app from "tangling" together.

| Component | Responsibility | Example in Tier1 Bank |
| :--- | :--- | :--- |
| **Model** | Data & Networking | `VerificationRepository.kt` fetches data from the server. |
| **View** | The User Interface | `LoginScreen.kt` shows the buttons and text fields. |
| **ViewModel** | State & Logic | `LoginViewModel.kt` handles the "is loading" state and button clicks. |

**The "Magic" of StateFlow:**
In MVVM, the ViewModel emits a "State" (like a news broadcast). The View (the screen) just listens to that broadcast and updates itself whenever the news changes. This means the UI is "dumb"—it just shows whatever the ViewModel tells it to.

## 3. Clear Roles: App vs. Server

You are absolutely correct: **The App is a "Dumb Client," and the Server is the "Authority."**

### Why do it this way?
1. **Security (Crucial):** Notice that your `GLIDE_CLIENT_SECRET` is never inside the Android app. If it were, a hacker could extract the secret from the APK. By keeping it on the **Node.js Server**, the secret stays safe.
2. **Decision Logic:** The server decides if a verification was successful. The app just asks, "Hey, what's the status for Attempt 123?" and the server replies "Success" or "Pending."

### The Flow of Responsibility:
1. **User Action:** User taps "Verify through Carrier" in the **App**.
2. **Request:** The app sends a request to your **Server**.
3. **Glide Integration:** The **Server** calls Glide Identity with the secret credentials to start the process.
4. **Handoff:** The server gives the app a `verificationUrl`.
5. **Browser:** The app opens the browser for the secure carrier check.
6. **Confirmation:** Once finished, the app asks the **Server** to confirm the final result.

## Summary for Interviews
If asked, you can confidently say:
> "We used **MVVM** with **Jetpack Compose** to ensure a clean separation between UI and logic. The application acts as a secure frontend client, while the **Node.js backend** serves as the security boundary, managing sensitive Glide Identity credentials and authoritative state transitions."

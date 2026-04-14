# Glide Identity: Magical Auth Integration Concept 🚀

This repository contains a full-stack proof-of-concept securely integrating Glide Identity's **Magical Auth** feature within a cohesive Android ecosystem designed to mirror a top-tier banking provider (Chase Mobile).

## Concept & Architecture
This system actively decouples the native Android application from the Verification SDK logic using Custom Chrome Tabs and Deep Linking, guaranteeing isolation and preventing native SDK bloat.

* **Native App (Kotlin / Jetpack Compose):** Handles core banking logic (Dashboards, Transfers). It never holds or computes verification logic. It merely dispatches intents to the browser and awaits an authorized callback.
* **Verification Environment (Node.js & Express):** Serves the Magical Auth user interface payload, hosts the `@glideidentity/glide-be-sdk-node` instance, and handles `/prepare`, `/report-invocation`, and `/process` API hooks.

---

## 🛠️ Developer Setup & Run Instructions

### 1. Launch the Backend Server

The Node environment bridges the mobile browser and Glide's carrier-grade checking network.

1. Navigate to the backend directory and install dependencies:
   ```bash
   cd backend
   npm install
   ```
2. **Configure Verification Credentials:**
   Open `backend/.env`. Insert your designated Glide `CLIENT_ID` and `CLIENT_SECRET` provided to you. 
   *(Note: if left as `STUB`, the server will seamlessly default to a mocked **Simulator Mode** for offline UI checking).*
3. Run the deployment environment:
   ```bash
   node server.js
   ```
   *The server binds to `0.0.0.0:3000` to ensure local network broadcast capability to your physical Android device.*

### 2. Deploy the Android Payload

The Android app requires a physical device connected to the **same Wi-Fi network** as the development server.

1. Open the root folder (`glide_tech_assessment`) in **Android Studio**.
2. **Crucial Local Device Config:** Open `app/src/main/java/com/johnny/tier1bankdemo/data/verification/VerificationRepository.kt` and change the `BASE_URL` constant. It must point to your development laptop's local IPv4 network address (e.g., `http://192.168.1.X:3000`).
3. Build and run the project onto your physical Android device via USB/Wireless ADB.

---

## 📱 Testing The Auth Flow

### Going Live (Cellular Network Validation)
To perform a true, genuine carrier-grade verification loop:
1. Ensure your actual Glide `CLIENT_ID` and `CLIENT_SECRET` are pasted into `.env`.
2. Disable Wi-Fi on your physical Android device. You must be connected entirely over **Cellular Data (5G/LTE)** for the SIM packet signatures to properly trace.
3. Trigger a fake money transfer inside the app. Un-check the `Simulator Mode` box inside the verifying browser tab, input the device's phone number, and hit "Verify".

### Developer Simulator Mode (Sandbox)
If credentials are not yet provisioned, you can still test UX flow and intent loops:
1. Leave the `.env` values set to `STUB`.
2. Inside the browser verification tab, ensure `Simulator Mode` stays toggled **On**.
3. The server will mock connection latency and instantly authorize the Deep-Link trajectory back into the Kotlin `tier1bank://` intent loop!

# Questify

Author: Ahri  

## App Summary
Questify is a lightweight, offline to‑do and habit tracker that stores data locally on the device. Add tasks, mark them complete, edit or delete items, and earn XP as you make progress.

## Key Features
- Create, edit, and delete tasks
- Mark tasks complete to gain XP
- Local-only data storage (no network transmission)
- Simple, lightweight UI intended for focused productivity
- No third-party SDKs

## Installation & Running (Desktop)
1. Import the project into Eclipse:  
   File → Import → Existing Projects into Workspace
2. Run the main class as a Java application. The project's entry point is the app's Main class (run the project's Main class from your IDE).
3. On first run you will be shown the Privacy Policy dialog — accept to enable saving. Once accepted, tasks will be saved locally.

## Data Location
Questify stores its files in a hidden folder in the user's home directory:
- Tasks: ~/.questify/tasks.txt
- App settings (including XP and acceptance): ~/.questify/config.properties

## Privacy & Data Safety (Short)
- Questify stores only task text, completion state, a small app config, and an internal identifier — all locally on the device.
- No data is transmitted to external servers and Questify does not share or sell data.
- Data is stored in plain text by the app; avoid putting sensitive personal information in task text.
- Not intended for children under 13.

## How to Remove All Questify Data
1. Close Questify.
2. Delete the .questify folder in your home directory:
   - Unix-like: rm -rf ~/.questify
   - Windows: delete the .questify folder in your user profile directory (e.g., C:\Users\<you>\.questify)

## First-time Run Notes
- You must accept the privacy policy dialog on first launch to enable saving. If you decline, the app will quit.

## Development / Testing Notes
- Target audience: General (ages 13+)
- No network or telemetry features included
- No external SDKs used

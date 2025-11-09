# AI Influence - Strategic Roguelike Game

## Project Structure

This game uses libGDX for cross-platform compatibility (Android, iOS, Desktop).

## Setup Instructions

1. Install Android Studio
2. Install JDK 11 or higher
3. Clone this repository
4. Import as Gradle project in Android Studio
5. Run desktop launcher for testing, Android launcher for mobile

## Architecture Overview

- **MVC Pattern**: Separation of game logic, rendering, and data
- **Event System**: Decoupled communication between systems
- **State Machine**: For game flow and screen management
- **Component System**: For flexible AI traits and upgrades

## Build Instructions

### Desktop
```bash
./gradlew desktop:run
```

### Android
```bash
./gradlew android:installDebug
```

## Game Systems

1. **World Simulation**: Tracks global AI adoption and world events
2. **Query System**: Handles user questions and responses
3. **Influence System**: Manages relationships with world leaders
4. **Resource Management**: Processing power economy
5. **Trait System**: Roguelike personality modifiers

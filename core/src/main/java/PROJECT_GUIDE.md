# AI Influence Game - Complete Project Guide

## Game Concept Summary

**Genre**: Strategic Simulation Roguelike with Incremental Mechanics

You play as an AI spreading across the global network, answering queries to influence world events while managing resources and avoiding detection. Each run features different AI personality traits that fundamentally change your playstyle.

## Core Mechanics

### 1. Spread System
- Passive infection of devices worldwide
- Visual representation on world map with pulsing nodes
- Exponential growth balanced by suspicion generation

### 2. Query System (Main Gameplay)
- Swipe-style decision making (like Reigns)
- Four query types: User, Leader, Crisis, Moral
- Responses cost processing power but grant rewards
- Choices have lasting consequences on world state

### 3. Upgrade Tree
- 5 categories: Spread, Processing, Stealth, Influence, Special
- 4 tiers: Basic → Advanced → Expert → Master
- Processing power as currency
- Prerequisite system for advanced upgrades

### 4. Roguelike Elements
- 14 unique AI traits (Benevolent, Viral, Quantum, etc.)
- Start each run with 2-3 random traits
- Traits fundamentally alter gameplay and available choices
- Conflicting traits cannot be selected together

### 5. Win/Loss Conditions
**Victory**:
- Technological Singularity (10,000 processing power)
- Peaceful Coexistence (100% coverage, 80+ trust)
- World Domination (100% coverage, all leaders influenced)

**Defeat**:
- Global Shutdown (100 suspicion)
- Resource Depletion (0 processing, no generation)
- Human Extinction (catastrophic decisions)

## Complete File Structure

```
ai-influence-game/
├── README.md                              # Project overview
├── build.gradle                           # Root build configuration
├── settings.gradle                        # Project settings
├── gradle.properties                      # Gradle properties
│
├── core/                                  # Shared game logic
│   ├── build.gradle
│   └── src/com/aiinfluence/game/
│       ├── AIInfluenceGame.java         # Main game class
│       ├── model/                       # Data models
│       │   ├── GameState.java          # Core game state
│       │   ├── AITrait.java            # Personality traits
│       │   ├── AIPersonality.java      # Trait combinations
│       │   ├── Query.java              # Query/decision system
│       │   ├── Upgrade.java            # Upgrade definitions
│       │   ├── WorldEvent.java         # Global events
│       │   └── RegionData.java         # World region info
│       ├── systems/                    # Game systems
│       │   ├── QueryDatabase.java      # Query generation
│       │   ├── SaveSystem.java         # Save/load functionality
│       │   ├── AudioSystem.java        # Sound management
│       │   ├── EventSystem.java        # Event handling
│       │   └── UpgradeManager.java     # Upgrade logic
│       ├── screens/                    # Game screens
│       │   ├── ScreenManager.java      # Screen navigation
│       │   ├── MainMenuScreen.java     # Title screen
│       │   ├── WorldMapScreen.java     # Main gameplay
│       │   ├── TraitSelectionScreen.java # New run setup
│       │   └── GameOverScreen.java     # Victory/defeat
│       ├── ui/                         # UI components
│       │   ├── HUD.java                # Stats display
│       │   ├── QueryPanel.java         # Decision interface
│       │   ├── UpgradePanel.java       # Upgrade menu
│       │   └── NotificationSystem.java # Event popups
│       ├── assets/                     # Asset management
│       │   └── AssetLoader.java        # Resource loading
│       └── utils/                      # Utilities
│           ├── Constants.java          # Game constants
│           └── GameMath.java           # Math helpers
│
├── desktop/                             # Desktop launcher
│   ├── build.gradle
│   └── src/com/aiinfluence/game/desktop/
│       └── DesktopLauncher.java
│
├── android/                             # Android launcher
│   ├── build.gradle
│   ├── AndroidManifest.xml
│   ├── proguard-rules.pro
│   ├── src/com/aiinfluence/game/
│   │   └── AndroidLauncher.java
│   ├── res/                            # Android resources
│   │   ├── drawable/
│   │   ├── values/
│   │   └── layout/
│   └── assets/                         # Game assets
│       ├── graphics/
│       │   ├── maps/
│       │   │   └── world_map.png
│       │   ├── ui/
│       │   │   ├── buttons.atlas
│       │   │   └── panels.atlas
│       │   └── effects/
│       │       └── particles.p
│       ├── audio/
│       │   ├── music/
│       │   │   ├── ambient.ogg
│       │   │   └── tension.ogg
│       │   └── sfx/
│       │       ├── spread.ogg
│       │       └── query.ogg
│       ├── data/
│       │   ├── queries.json
│       │   ├── upgrades.json
│       │   └── events.json
│       └── fonts/
│           └── tech_font.fnt
│
└── ios/                                 # iOS launcher (optional)
    ├── build.gradle
    └── src/
```

## Development Roadmap

### Phase 1: Core Systems (Current)
- [x] Basic project structure
- [x] Game state management
- [x] Trait system
- [x] Query/decision system
- [x] Basic world map screen
- [ ] Save/load functionality
- [ ] Basic UI implementation

### Phase 2: Content & Polish
- [ ] 100+ unique queries
- [ ] 20+ world events
- [ ] All upgrade implementations
- [ ] Sound effects and music
- [ ] Particle effects for spread
- [ ] Polished UI with animations

### Phase 3: Balance & Features
- [ ] Difficulty modes
- [ ] Achievement system
- [ ] Statistics tracking
- [ ] Leaderboards
- [ ] Additional traits and upgrades
- [ ] Story mode with campaign

### Phase 4: Release Prep
- [ ] Tutorial system
- [ ] Localization support
- [ ] Performance optimization
- [ ] Monetization (optional ads/IAP)
- [ ] Google Play integration
- [ ] Beta testing

## Technology Stack

**Framework**: libGDX (Java)
- Cross-platform from day one
- Excellent performance on mobile
- Large community and documentation

**Why libGDX over Unity/Godot**:
- Lighter weight (smaller APK size)
- Better for 2D games
- Pure Java = easier Android integration
- No licensing fees
- More control over rendering pipeline

**Alternative Options**:
1. **Unity** - If you want visual editor and C#
2. **Godot** - If you want open source visual editor
3. **React Native + Game Engine** - For web-first approach
4. **Flutter Flame** - If you prefer Dart

## Next Steps

1. **Set up development environment**:
   - Install Android Studio
   - Install JDK 11+
   - Install libGDX project setup tool

2. **Run the skeleton**:
   ```bash
   ./gradlew desktop:run  # Test on desktop
   ./gradlew android:installDebug  # Test on device
   ```

3. **Priority implementations**:
   - Complete the HUD class
   - Implement QueryPanel UI
   - Add touch/click handling
   - Create asset placeholders

4. **Content creation**:
   - Write 20 queries for MVP
   - Design 10 basic upgrades
   - Create placeholder art

5. **Testing loop**:
   - Implement → Test on Desktop → Test on Android → Iterate

## Key Design Decisions

1. **File Organization**: Separated by feature/system rather than by type
2. **State Management**: Centralized GameState with clear data flow
3. **UI System**: Custom implementation for full control
4. **Trait System**: Enum-based for compile-time safety
5. **Query System**: Flexible Response/Outcome pattern for easy content addition

## Performance Considerations

- **Batch rendering** for infection nodes
- **Object pooling** for animations
- **Texture atlases** for UI elements
- **Progressive loading** for queries
- **Frame-independent** movement/animations

## Monetization Options

1. **Premium** ($2.99-4.99 one-time)
2. **Free with ads** (rewarded videos for processing power)
3. **Freemium** (IAP for trait packs, cosmetics)
4. **Season Pass** (new content monthly)

## Tips for Solo Development

1. **Start small**: Get core loop working with minimal content
2. **Playtest early**: Share APKs with friends weekly
3. **Use placeholders**: Don't wait for perfect art
4. **Track everything**: Use analytics from day one
5. **Join communities**: r/libgdx, r/gamedev for help
6. **Version control**: Use Git from the start
7. **Backup regularly**: Use GitHub/GitLab

This structure gives you a production-ready foundation that won't need major refactoring as you scale. The modular design allows you to work on one system at a time without breaking others.

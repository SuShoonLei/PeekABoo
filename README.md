# 👻 PeekABoo

> *A stealth navigation game — sneak past the alien guard and reach the flag!*

![Java](https://img.shields.io/badge/Java-8%2B-orange?style=flat-square&logo=java)
![LibGDX](https://img.shields.io/badge/LibGDX-1.12%2B-red?style=flat-square)
![Platform](https://img.shields.io/badge/Platform-Desktop-blue?style=flat-square)
![Status](https://img.shields.io/badge/Status-Playable-brightgreen?style=flat-square)

---

## 🎮 About

**PeekABoo** is a 2D top-down stealth game built with [libGDX](https://libgdx.com/) as a university project for **CSC455 — Game AI**. The core mechanic is built around a **finite state machine (FSM)** that drives the guard's behaviour, a fundamental concept in game AI.

You play as a ghost trying to sneak across the map and reach a finish flag — without being spotted by a patrolling alien guard.

---

## 🕹️ Gameplay

| Goal | Sneak from the bottom-left to the flag on the top-right |
|------|----------------------------------------------------------|
| Win  | Touch the green finish flag |
| Lose | Get caught by the alien guard |

### Controls

| Key | Action |
|-----|--------|
| `↑ ↓ ← →` | Move the player |
| `R` | Restart the game |
| `SPACE` / `ENTER` | Start the game |

---

## 🤖 AI — Finite State Machine

The guard is powered by a **3-state finite state machine**:

```
         enters FOV
PATROL ─────────────▶ CHASE
   ▲                     │
   │   lost player &      │  player escapes
   │   returned home      ▼
   └──────────────── RETURN
```

| State | Behaviour | Colour |
|-------|-----------|--------|
| **PATROL** | Slowly sweeps head 270° (±135°) back and forth | 🟡 Yellow FOV |
| **CHASE** | Sprints directly toward the player | 🔴 Red FOV |
| **RETURN** | Walks back to original post | 🟠 Orange FOV |

### Field of View (FOV)
- **120°** wide cone in front of the guard
- **130px** detection range
- Visualised as a translucent triangle that changes colour by state

---

## 🗺️ Map Layout

```
[FLAG]          [Obstacle]  [Obstacle]
                            [GUARD]
        [Building]
[Obstacle]      [Obstacle]
[Obstacle]
[PLAYER START]
```

- 🏢 **Central building** — solid obstacle in the middle of the map
- 🟫 **6 brown obstacles** — create a winding path forcing the player near the guard
- 🟢 **Green flag** — pulsing finish zone at the top-right corner
- 🛸 **Pink alien** — the guard, patrols the building corner
- 👻 **Ghost** — the player character (switches to scared face when being chased)

---

## ✨ Features

- ✅ Finite state machine guard AI (Patrol / Chase / Return)
- ✅ 120° field of view cone with real-time detection
- ✅ Custom sprite characters (alien guard, ghost player)
- ✅ Player switches to scared sprite when being chased
- ✅ Obstacle collision system with wall sliding
- ✅ Pulsing animated finish flag
- ✅ Start, Win, and Game Over screens
- ✅ Fully configurable via `GameConfig.java`

---

## 🛠️ Tech Stack

| Tool | Purpose |
|------|---------|
| [Java 8](https://www.java.com/) | Programming language |
| [libGDX](https://libgdx.com/) | Game framework |
| [LWJGL3](https://www.lwjgl.org/) | Desktop backend |
| [Gradle](https://gradle.org/) | Build system |
| [IntelliJ IDEA](https://www.jetbrains.com/idea/) | IDE |

---

## 📁 Project Structure

```
PeekABoo/
├── core/src/main/java/com/SuShoonLeiK/PeekABoo/
│   ├── Main.java          # Entry point
│   ├── GameScreen.java    # Main game loop & rendering
│   ├── GameConfig.java    # All tunable constants (colours, speeds, sizes)
│   ├── Player.java        # Player movement & sprite switching
│   ├── Guard.java         # Guard AI state machine & FOV
│   ├── Building.java      # Central building obstacle
│   ├── Obstacle.java      # Map obstacles
│   └── FinishFlag.java    # Animated finish zone
├── assets/
│   ├── guard.png          # Pink alien sprite
│   ├── player.png         # Ghost sprite (normal)
│   └── player_dead.png    # Ghost sprite (scared, when chased)
└── lwjgl3/                # Desktop launcher
```

---

## 🚀 How to Run

### Requirements
- Java 8 or higher
- Git

### Clone & Run
```bash
git clone https://github.com/YOUR_USERNAME/PeekABoo.git
cd PeekABoo
./gradlew lwjgl3:run
```

### Build a JAR
```bash
./gradlew lwjgl3:jar
java -jar lwjgl3/build/libs/PeekABoo.jar
```

---

## ⚙️ Customisation

All game parameters live in **`GameConfig.java`** — no digging through code needed:

```java
// Change how fast the guard patrols
public static final float GUARD_PATROL_SPEED = 45f;

// Change the FOV cone size (degrees)
public static final float GUARD_FOV_HALF = 60f;   // 60 * 2 = 120° total

// Change player speed
public static final float PLAYER_SPEED = 120f;

// Change guard & player colours
public static final Color GUARD_COLOR_PATROL = new Color(1f, 0.85f, 0f, 1f);
public static final Color PLAYER_COLOR       = new Color(0f, 0.85f, 1f, 1f);
```

---

## 📚 Academic Context

This project was developed for **CSC455 — Game AI** at university. The primary learning objective was implementing a **finite state machine** as the core AI driver for a non-player character — a foundational technique used in commercial games.

**Key AI concepts demonstrated:**
- Finite State Machine (FSM) design and implementation
- Field of View (FOV) detection using vector angle comparison
- State transitions based on environmental conditions
- Return-to-post behaviour after losing a target

---

## 👤 Author

**Su Shoon Lei Khaing**
- GitHub: [@SuShoonLeiK](https://github.com/SuShoonLeiK)

---

## 📄 License

This project is for educational purposes as part of a university course assignment.

---

*Built with ❤️ and libGDX*

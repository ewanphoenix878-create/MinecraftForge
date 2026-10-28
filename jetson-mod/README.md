# Jetson Personal Aircraft (NeoForge 1.21.1)

A rideable Jetson-style quad-rotor for Minecraft 1.21.1.

## Recipe (needs Create)
```
P I P      P = create:propeller
I B I      I = iron ingot
P I P      B = block of iron
```

## How to use
- Right-click a block with the item to place it.
- Right-click to get in. Shift + right-click (or press E while seated) opens the fuel tank.
- Fuel: anything that burns in a furnace (coal, charcoal, coal blocks, lava buckets...). One coal is about 80 s of flight.
- **Space** climb, **Shift** descend, **W/A/S/D** forward / left / back / right. The aircraft turns with your view.
- Shift only lets you out once you've landed. Out of fuel, it drops to the ground (no fall damage).
- Hit it to break it and get the item back.

## Build
```
./gradlew build
```
Jar ends up in `build/libs/`. Drop it into your `mods` folder next to Create.

# DataSaver

A reimplemented library of https://github.com/FunnySaltyFish/ComposeDataSaver

## Add to project

[![JitPack Release](https://jitpack.io/v/Kyant0/DataSaver.svg)](https://jitpack.io/#Kyant0/DataSaver)

```kotlin
allprojects {
    repositories {
        maven("https://jitpack.io")
    }
}

implementation("com.github.Kyant0:DataSaver:2023.5.2")
```

## Usage

Init DataSaver in Application.kt:

```kotlin
DataSaver.init(noBackupFilesDir.absolutePath)
```

Then define a DataSaverMutableState:

```kotlin
var increment by remember { mutableSaveableStateOf("increment", 0) } // Secondly: In the next launch, increment: 1

SideEffect {
    increment += 1 // Firstly: increment: 1, saved to local and preserved forever
}
```

Supported functions

| Type                            | Function                   |
|---------------------------------|----------------------------|
| MutableSaveableState<**T**>     | mutableSaveableStateOf     |
| MutableSaveableListState<**T**> | mutableSaveableListStateOf |
| MutableSaveableMapState<K, V>   | mutableSaveableMapStateOf  |

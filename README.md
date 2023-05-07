# DataSaver

A reimplemented library of https://github.com/FunnySaltyFish/ComposeDataSaver

## Add to project

```kotlin
allprojects {
    repositories {
        maven("https://jitpack.io")
    }
}

implementation("com.github.Kyant0:DataSaver:2023.5.1")
```

## Usage

Init DataSaver in Application.kt:
```kotlin
DataSaver.init(noBackupFilesDir.absolutePath)
```

Then define a DataSaverMutableState:
```kotlin
var increment by rememberDataSaverState("increment", 0) // Secondly: In the next launch, increment: 1

SideEffect {
    increment += 1 // Firstly: increment: 1, saved to local and preserved forever
}
```

Supported functions
| Type | Function | Composable Function |
| ---- | ---- | ---- |
| DataSaverMutableState<**T**> | mutableDataSaverStateOf | rememberDataSaverState |
| DataSaverMutableListState<**T**> | mutableDataSaverListStateOf | rememberDataSaverListState |
| DataSaverMutableMapState<K, V> | mutableDataSaverMapStateOf | rememberDataSaverMapState |
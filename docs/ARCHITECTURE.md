# Architecture Decision Record — Tensor Launcher

## Overview

Tensor is an Android home-screen launcher built with Jetpack Compose following **Clean Architecture + DDD**. Every dependency points inward: the domain layer has zero Android imports.

```
┌───────────────────────────────────────────────────────┐
│  presentation  (Compose Screens, ViewModels)           │
│       ↓ depends on                                     │
│  domain        (UseCases, Port interfaces, Models)     │
│       ↑ implemented by                                 │
│  data          (Repositories, DataSources, Launchers)  │
└───────────────────────────────────────────────────────┘
```

---

## Package Map

```
com.tien.tensor/
├── domain/
│   ├── model/          AppInfo · ThemeId · ThemeConfig
│   ├── port/           AppRepository · ThemeRepository · AppLauncher  (interfaces)
│   └── usecase/        GetInstalledAppsUseCase · SearchAppsUseCase
│                       LaunchAppUseCase · GetThemeUseCase · SetThemeUseCase
│
├── data/
│   ├── source/         AppDataSource (PackageManager) · PreferencesDataSource (DataStore)
│   ├── repository/     AppRepositoryImpl · ThemeRepositoryImpl
│   ├── launcher/       AndroidAppLauncher
│   └── receiver/       BootReceiver
│
├── di/                 AppModule (manual DI, lazy singletons)
│
├── ui/
│   ├── theme/          LauncherColors · TensorSpacing · TerminalTypography · TensorTheme
│   └── component/      BlinkingCursor · TypewriterText · AppIcon · TerminalComponents
│
├── presentation/
│   ├── navigation/     AppDestination (HOME · APP_LIST · SETTINGS)
│   ├── launcher/       LauncherScreen · LauncherViewModel · LauncherUiState
│   ├── applist/        AppListScreen · AppListViewModel · AppListUiState
│   └── settings/       SettingsScreen · SettingsViewModel · SettingsUiState
│
├── TensorApplication.kt
└── MainActivity.kt
```

---

## Key Decisions

### DI: Manual over Hilt
No annotation processing overhead for a launcher. `AppModule` is a plain `object` with `lazy` vals. ViewModel factories are inline lambdas. If the project grows, Hilt can replace `AppModule` without touching the domain or data layers.

### Reactive App List via `callbackFlow`
`AppRepositoryImpl.getInstalledApps()` uses `callbackFlow` + a `BroadcastReceiver` for `PACKAGE_ADDED / REMOVED / CHANGED`. The flow is `flowOn(Dispatchers.IO)` so the PackageManager query never blocks the main thread.

### Theme as Domain Concept
`ThemeId` lives in `domain/model/` — it's a domain concept (user preference) not a UI one. The data layer stores it in DataStore. The UI layer maps `ThemeId → LauncherColors` in `ui/theme/`. This means the domain never imports Compose types.

### No-Hardcode Color Rule
`LauncherColors` is the single source of truth. `ui/theme/Color.kt` contains only comments directing devs to `LauncherColors.kt`. Screens access colors only via `LauncherTheme.colors` (backed by `LocalLauncherColors` CompositionLocal).

### Multi-Theme via CompositionLocal
`TensorTheme(themeId)` wraps both a `MaterialTheme` (so standard Compose components work) and `CompositionLocalProvider(LocalLauncherColors provides ...)` (so custom components get our tokens). Theme changes in `SettingsViewModel` propagate instantly via `StateFlow → collectAsStateWithLifecycle` in `MainActivity`.

### State-Based Navigation
Three screens are managed with `rememberSaveable { mutableStateOf(AppDestination.HOME) }` in `MainActivity`. Survives configuration changes without a NavController. Simple, zero-overhead for a 3-screen app.

### Terminal Animations
| Effect | Implementation |
|--------|----------------|
| Blinking cursor | `rememberInfiniteTransition` animating `alpha` 1f→0f, 530ms Reverse |
| Typewriter text | `LaunchedEffect` loops through chars with 55ms delay per character |
| App list entrance | `AnimatedVisibility` + `LaunchedEffect(delay(index * 30ms))` for stagger |
| Launch overlay | `AnimatedVisibility(fadeIn/fadeOut)` at bottom of LauncherScreen |

---

## Adding a New Theme

1. Add a value to `ThemeId` enum in `domain/model/ThemeId.kt`
2. Create a `LauncherColors` instance in `ui/theme/LauncherColors.kt`
3. Add the mapping in `themeColors()` switch in the same file

No other changes needed — the settings screen renders all `ThemeId.entries` automatically.

---

## Future Improvements

| Feature | Approach |
|---------|----------|
| Real recent apps | `UsageStatsManager` (requires `PACKAGE_USAGE_STATS` permission + user opt-in) |
| Custom font | Add `jetbrains_mono.ttf` to `res/font/`, replace `FontFamily.Monospace` in `Type.kt` |
| Pinned apps | Add `PinAppUseCase` + DataStore list; render pinned row in LauncherScreen |
| Gesture navigation | `BackHandler` / predictive-back API for swipe-to-app-drawer |
| Widget support | `AppWidgetHost` — requires its own screen/surface layer |

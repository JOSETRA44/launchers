# Security Arsenal — Plugin Architecture

The arsenal (`/arsenal`, or `▸ OPEN SECURITY ARSENAL` from `/sec`) is a
modular suite of **defensive, read-only** security tools. Every tool audits
the user's own device through public Android APIs; nothing is modified,
probed externally or transmitted anywhere. All analysis is local.

## The plugin contract

```
domain/model/ArsenalReport.kt   Severity · Finding · ModuleMeta · ModuleReport
domain/port/SecurityModule.kt   SecurityModule + ArsenalRegistry (ports)
domain/usecase/                 GetArsenalModulesUseCase · ObserveArsenalModuleUseCase
```

```kotlin
interface SecurityModule {
    val meta: ModuleMeta                 // id, name, tagline, isStreaming
    fun observe(): Flow<ModuleReport>    // cold: re-collecting re-runs the scan
}
```

The flow contract carries the whole lifecycle:

- **One-shot modules** emit a single `ModuleReport` and complete. RESCAN in
  the UI simply re-collects the cold flow.
- **Streaming modules** (`isStreaming = true`) emit continuously until the
  collector is cancelled — sampling stops the instant the user leaves.

Domain and presentation only ever see the port. Adding a tool requires no
changes to existing code:

1. Implement `SecurityModule` in `data/arsenal/`.
2. Append it to the `ArsenalRegistryImpl(listOf(...))` block in `AppModule`.

The hub discovers it automatically (cards render from `registry.all()`).

## Concurrency model

`ArsenalViewModel` launches one independent coroutine `Job` per plugin into
`viewModelScope`. A heavy scan (APP RISK walks every installed package on
`Dispatchers.Default`) never blocks live telemetry, which keeps streaming on
`Dispatchers.IO`. Reports land in a single `StateFlow<ArsenalUiState>` keyed
by module id; severity badges on the hub update in real time.

## Built-in modules

| Module | Mode | What it does |
|---|---|---|
| **INTEGRITY+** | one-shot | Screen lock, storage encryption (`DevicePolicyManager`), root indicators (su paths + test-keys build tags), ADB/dev-options surface, count of apps able to request package installs |
| **APP RISK** | one-shot | Heuristic risk score per installed app: granted dangerous-permission groups (CAM/MIC/LOC/SMS/…), sideloaded install source (`InstallSourceInfo`), debuggable flag, `targetSdk < 26`, surveillance combos (CAM+MIC+LOC). Score → LOW/MEDIUM/HIGH/CRITICAL |
| **NET INTEL** | streaming | Live `LinkProperties`/`NetworkCapabilities` of the default network: HTTP proxy on the link (possible MITM), Private DNS (DoT) state, captive portal, unvalidated network, VPN, resolvers, local addressing |
| **SYS TELEMETRY** | streaming (2.5 s) | Memory pressure (`ActivityManager`), storage headroom (`StatFs`), battery temperature/voltage, thermal throttling (`PowerManager.currentThermalStatus`), uptime |

## Severity model

`INFO < LOW < MEDIUM < HIGH < CRITICAL`. A module's hub badge shows its worst
finding; all-INFO reports render as `[CLEAN]`. Colors map to theme tokens
(`error` for HIGH/CRITICAL, `cursor` for MEDIUM, `terminalPrompt` for LOW,
`onSurface` for INFO) — no hardcoded colors.

## Permissions

The arsenal needs **no new permissions**: it reuses `QUERY_ALL_PACKAGES`
(app list already requires it), `ACCESS_NETWORK_STATE` and `ACCESS_WIFI_STATE`.
Everything else reads world-readable system state.

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

**Job lifecycle is bound to screen visibility, not the ViewModel.** The
ViewModel is activity-scoped, so `ArsenalScreen` drives it through a
`DisposableEffect`: `onScreenEnter()` (re)starts every plugin job on entry;
`onScreenExit()` cancels all jobs and clears transient state (scanning set,
selected detail) when the screen leaves the composition. Nothing streams in
the background after navigating away, and re-entry always lands on the hub —
cached reports are kept so cards show data instantly while fresh scans run.
While a detail panel is open, the system back gesture closes it (screen-local
`BackHandler`) exactly like the `[BACK]` button; only from the hub does back
leave the screen.

## Built-in modules

| Module | Mode | What it does |
|---|---|---|
| **INTEGRITY+** | one-shot | Screen lock, storage encryption (`DevicePolicyManager`), root indicators (su paths + test-keys build tags), ADB/dev-options surface, count of apps able to request package installs |
| **APP RISK** | one-shot | Heuristic risk score per installed app: granted dangerous-permission groups (CAM/MIC/LOC/SMS/…), sideloaded install source (`InstallSourceInfo`), debuggable flag, `targetSdk < 26`, surveillance combos (CAM+MIC+LOC). Score → LOW/MEDIUM/HIGH/CRITICAL |
| **ACCESS CONTROL** | one-shot | Enabled accessibility services (screen-reading/spyware vector), third-party notification listeners (can read 2FA codes), active device admins, default IME — the who-can-see-everything audit |
| **TRUST STORE** | one-shot | Enumerates `AndroidCAStore`: user-installed CA certificates (TLS MITM surface, flagged HIGH with subject CN) vs system authorities |
| **NET INTEL** | streaming | Live `LinkProperties`/`NetworkCapabilities` of the default network: HTTP proxy on the link (possible MITM), Private DNS (DoT) state, captive portal, unvalidated network, VPN, resolvers, local addressing |
| **SYS TELEMETRY** | streaming (2.5 s) | Memory pressure (`ActivityManager`), storage headroom (`StatFs`), battery temperature/voltage, thermal throttling (`PowerManager.currentThermalStatus`), uptime |
| **BLUETOOTH RECON** | one-shot | Adapter state + discoverability, bonded (paired) devices classified by type/bond (HID input, opaque bonds), a 4 s passive BLE scan of nearby advertisers with RSSI, and a **GPU passkey benchmark**: exhausts the 10⁶ BLE Legacy passkey space on the phone's GPU (OpenGL ES 3.1 compute shader, CPU fallback) to demonstrate why Legacy pairing is broken. Reads local state only — never injects pairing against third-party devices. Declares runtime `requiredPermissions` and offers a one-tap grant. |

## Severity model

`INFO < LOW < MEDIUM < HIGH < CRITICAL`. A module's hub badge shows its worst
finding; all-INFO reports render as `[CLEAN]`. Colors map to theme tokens
(`error` for HIGH/CRITICAL, `cursor` for MEDIUM, `terminalPrompt` for LOW,
`onSurface` for INFO) — no hardcoded colors.

## Permissions

Most modules need **no new permissions**: they reuse `QUERY_ALL_PACKAGES`
(app list already requires it), `ACCESS_NETWORK_STATE` and `ACCESS_WIFI_STATE`,
reading world-readable system state.

The one exception is **BLUETOOTH RECON**, which needs runtime grants for full
function: `BLUETOOTH_SCAN` + `BLUETOOTH_CONNECT` on API 31+ (declared
`neverForLocation`), or `ACCESS_FINE_LOCATION` on older releases for BLE
discovery. A module advertises what it needs via `ModuleMeta.requiredPermissions`;
the detail panel checks them and shows a one-tap **GRANT ACCESS** button for any
still missing, re-running the scan once granted. Modules degrade gracefully
without the grant (they show only the state they can read). The GPU passkey
benchmark needs no permission — it only touches the device's own GPU/CPU.

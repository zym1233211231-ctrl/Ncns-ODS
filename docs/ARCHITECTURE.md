# Architecture quick map

## Entry
MainActivity.onCreate:
- creates Game + GlRenderer + GlGameView + GameView
- Compose content chooses LauncherScreen / OlympusChronicles / AiArtScreen / game views.

## Home
`LauncherScreenKt.LauncherScreen(version, onPickArena, onPickChronicle, onPickAtelier)`.
`LauncherEntry` draws each entry. Navigation callbacks are Function0.
Credits are currently appended to the ODYSSEY title string in
`LauncherScreenKt$LauncherScreen$2$1.smali`.

## AI
- `com.olympus.chronicles.ai.LocalArtEngine` implements LocalImageGenerationProvider.
- `SdOnnxEngine` ONNX runtime; models text_encoder/unet/vae_decoder.
- `ChronicleApp.maybeLocalArt` generates on story events when AI enabled.
- `MythArtBridge.cache` writes success bitmap into `ChronicleApp.bitmaps`.
- `StorySceneUnderstandingEngine` builds character/scene/negative prompts.

## ARAM
- Game/Core in classes1.
- Stable hacks: `AramFix`, wave counter reset, BETA strings.

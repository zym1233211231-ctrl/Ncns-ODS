# THE GATES OF OLYMPUS — 主页视觉重构

本文件记录 `com.hextech.aram.LauncherScreenKt.LauncherScreen` 的完整替换方案、构建方式、
验证结果与已知缺口。

基线 APK：`base/current-v9.apk`（功能基线，未改动）。
产出 APK：`build/odyssey-111141-signed.apk`
SHA-256：`e3b9d0e77eee96a35f9add42463ecffd88609d86a02913024f70086f80c8afe0`

## 1. 为什么新增了一个 Kotlin 源模块

原主页是 dex 里的 Compose 字节码，`jadx/sources` 只能读、不能编译。直接手改 2 万行 smali
不可维护，所以主页被还原成一个可编译的 Compose 源模块：

```
homeui/
  build-home.sh                 # kotlinc + compose 插件 + D8 -> build/dex/classes3.dex
  toolchain/setup.sh            # 固定版本工具链下载（Kotlin 2.1.0 / Compose 1.7.0 / R8 8.5.35）
  src/com/hextech/aram/LauncherScreen.kt        # 入口 + 横竖屏布局 + 分段入场动画
  src/com/hextech/aram/olympus/Palette.kt       # 色板、性能档探测、SharedPreferences、字体
  src/com/hextech/aram/olympus/Temple.kt        # 纯 Canvas 神殿场景（可被玻璃面板复用）
  src/com/hextech/aram/olympus/Aether.kt        # AETHER GLASS 面板/主按钮/次级入口
  src/com/hextech/aram/olympus/Chrome.kt        # BasicText 排版、细线、菱形、箭头
  src/com/hextech/aram/olympus/Overlays.kt      # 设置面板、制作名单面板
  preview/                      # 桌面 Compose 渲染工具（可选，用来截图核对布局）
```

`build.sh` 会在汇编 classes2 之前自动执行 `homeui/build-home.sh`，把新主页编成
`classes3.dex` 一起打包；`BUILD_HOMEUI=0 ./build.sh` 可跳过。

### 为什么是 classes3.dex

APK 已有 `classes.dex`（13.5 MB，Compose runtime/ui 等库）和 `classes2.dex`（app）。
新主页追加为 `classes3.dex`：Android 5.0+ 原生多 dex，ART 会加载 APK 内所有
`classes*.dex`，不需要 multidex 支持库，也不需要对 20 MB 的 classes2.dex 重新 D8 合并。
`classes2.dex` 里旧的 `com/hextech/aram/LauncherScreenKt*`（12 个 smali 文件）已移到
`smali-removed/launcher-v9/` 备份并从汇编目录移除，否则 classes2 会遮蔽 classes3 的同名类。

### 入口契约（未改调用点）

`MainActivity$onCreate$2.smali` 的调用是：

```
invoke-static/range {v1 .. v6}, Lcom/hextech/aram/LauncherScreenKt;->LauncherScreen(
    Ljava/lang/String;Lkotlin/jvm/functions/Function0;Lkotlin/jvm/functions/Function0;
    Lkotlin/jvm/functions/Function0;Landroidx/compose/runtime/Composer;I)V
```

新文件 `LauncherScreen.kt` 的函数签名逐字节一致（编译产物已用 javap 核对），
三个回调语义保持：`onPickArena` / `onPickChronicle` / `onPickAtelier`。

## 2. 视觉与布局

### 横屏：61.8 / 38.2

`Row` 内 `weight(0.618f)` 的场景区 + `weight(0.382f)` 的玻璃操作区（全高、贴右缘、
左侧上下圆角 26dp）。内容在面板内垂直居中，内容宽度上限 430dp（平板不让文字拉满）。

### 竖屏：上神殿 / 下入口（不是横屏压缩）

`Column(weight 0.56 / 0.44)`：上方全宽场景，品牌区压在场景底部（渐变遮罩保证可读）；
下方是独立的底部玻璃 sheet（顶部 24dp 圆角）。标题字号按屏宽重新取值
（`screenW * 0.098`，夹在 25–40sp），与横屏的按高度取值（`screenH * 0.066`，19–34sp）
是两套独立公式。

### 安全区

页面根节点 `WindowInsets.safeDrawing` + `BoxWithConstraints`；玻璃面板在安全区内排版，
背景场景全出血画到屏幕边缘。刘海/挖孔/手势区不会压住按钮。

### 神殿场景（纯 Canvas，无图片资源）

深石墨 → 深海蓝 → 地平线暖光的天空；三层山脊、云带、神光光束、六柱神殿
（台阶、柱头柱础、檐部三竖线、山花、内殿门洞暖光）、两座火炬、尘埃、暗角。
比例跟随 `templeW` 而不是 `min(w,h)`，所以竖屏不会出现"矮胖柱廊"。
火炬/云/尘埃/光束由页面级时钟驱动；暗角、山体、大理石纹是静态的。

## 3. AETHER GLASS 与真实性能回退

一层面板 = 透射层 + 冷色着色 + 大理石纹/单条高光斜掠 + 1dp 青铜细边。

| 档位 | 判定 | 透射/折射 | 场景细节 |
| --- | --- | --- | --- |
| HIGH | API≥31、非低内存、≥6 核、memoryClass≥192 | `Modifier.blur(26dp)` 重绘背景 + 1.012 倍放大（折射感） | 2（尘埃/光束/柱身凹槽） |
| BALANCED | API≥31、非低内存 | `Modifier.blur(12dp)`；不支持 RenderEffect 时为 0 | 1 |
| LOW | 其余设备 / 用户手动选低 | 不做模糊，只保留透明渐变 + 边缘高光 | 0 |

`renderEffect = SDK>=31 && !isLowRamDevice` 是硬门槛：低于 API 31 时**不会**假装有模糊，
面板退化为"透明渐变 + 象牙白高光 + 青铜细边"。用户可在设置里强制 自动/高/均衡/低，
选择写入 `SharedPreferences("olympus.home.v1")`。透射层只重绘面板尺寸的区域，不是整屏。

设置/名单弹窗用 `AetherTint.Dialog`（更高不透明度），不带透射，保证正文可读。

## 4. 动效与主线程

- 入场：整页 900ms 淡入（场景曝光从 0 到 1），面板元素按 90ms 步进依次淡入右移 18dp。
  动画状态只在 `graphicsLayer{}`/`draw` 里读取，逐帧不触发重组。
- 按压：主按钮缩放到 0.986 并叠加暖色边缘高光；次级入口只做底色 + 左侧细线提亮。
- 环境时钟：`withFrameNanos` 驱动一个 `MutableState<Long>`，只被 `Canvas` 的 draw 读取；
  窗口不可见（`isShown && windowVisibility == VISIBLE`）时冻结；LOW 档每 3 帧更新一次。
- 页面切走后 Composable 离开组合，所有动画随之停止；GLSurfaceView / 游戏渲染完全未触碰。

## 5. 导航映射（不造假按钮）

| 入口 | 行为 | 说明 |
| --- | --- | --- |
| 开始征程 / ENTER OLYMPUS | `onPickArena` | 进入乱斗 StartScreen（原有行为） |
| 神话秘闻 / OLYMPUS CHRONICLES | `onPickChronicle` | 进入 OlympusChronicles（原有行为） |
| 神谕绘卷 / ORACLE ATELIER | `onPickAtelier` | 进入 AI 绘卷（原有行为，第三回调） |
| 英雄殿堂 / HEROES | `onPickArena` | 乱斗菜单本身就是选人界面（"选择英雄 / 神恩图鉴"），没有独立的 HEROES 路由 |
| 设置 / SETTINGS | 打开主页设置面板 | 玻璃档位、动态光影、视差；真实生效并持久化 |
| 制作名单 / CREDITS | 打开名单面板 | 保留 `ODYSSEY · MYTHIC ARENA` / `by NeverCryNow` / `别看了 只有郑壹鸣 凡人的故事凡人写` |
| 图鉴 | 文本提示"图鉴 · 竞技场内" | 主页没有独立图鉴路由，图鉴在乱斗菜单内；不做成可点击的假入口 |
| 战绩存档 | 文本提示"未开放" | 工程内没有本地战绩存储，不伪造 |

## 6. 构建与验证

```bash
export ANDROID_JAR=/path/to/android-33/android.jar
JAVA_HOME=/path/to/jdk21 ./homeui/toolchain/setup.sh     # 一次性
./build.sh                                                # 自动编 classes3.dex 并打包
```

最终产物的验证（本机实际执行）：

```text
apksigner verify --verbose       -> v2: true, v3: true
zipalign -c -p 4                 -> OK
classes3.dex                     -> 89 个类，与 classes.dex/2.dex 重复类 0
与方法引用审计（dexlib2）        -> 746 条 method ref，缺失 0
classes2 中 LauncherScreenKt 定义 -> 0（旧类已清除）
```

审计脚本会遍历 classes3.dex 的每个 method/field 引用，在 classes.dex + classes2.dex 的
类层次（含父类/接口）里解析，因此能提前发现 "引用了 APK 里不存在的 Compose API"
这类运行时 `NoSuchMethodError`。

## 7. 布局截图（桌面 Compose 渲染真实代码）

`preview/` 用同一份 `LauncherScreen.kt`/`Temple.kt`/`Aether.kt` 在桌面 Compose 1.7.0 上
渲染（只有 `Palette.kt` 的 Android 部分与 `LocalView/BackHandler` 用 stub 替代），
用于核对黄金比例、文字不溢出、面板不重叠：

- `docs/preview/home-land-872x393dp.png`（像素 2400×1080，density 2.75）
- `docs/preview/home-portrait-392x872dp.png`（1080×2400）
- `docs/preview/home-tablet-1280x800dp.png`（2560×1600，density 2.0）
- `docs/preview/sheet-settings.png`
- `docs/preview/sheet-credits.png`

渲染命令见 `preview/README.md`。它不能替代真机安装（见风险 1）。

## 8. 未完成项与已知风险

1. **没有真机安装/截图验证**：本机无 adb/模拟器，手机助手通道 Shizuku 未授权（`bound=false`），
   `app.install` 不可用。已验证的是编译、签名、对齐、dex 引用完整性 + 桌面等价渲染；
   首次真机启动仍建议盯一次 `logcat`（关注 `FontLoadFailedException` 与 blur）。
2. **图鉴/战绩存档没有主页目的地**（工程内不存在路由/存储），以"竞技场内 / 未开放"明示，
   没有做成可点击的假按钮。
3. **英雄殿堂复用 `onPickArena`**：乱斗菜单内有选人界面与神恩图鉴，但不是独立 HEROES 页面。
4. **设置面板是主页自有设置**，不是游戏内画质/摇杆设置（后者在局内 暂停→设置）。
5. **API < 31 无实时模糊/折射**，按表降级；这是平台限制（RenderEffect），不是开关。
6. **字体**：拉丁标题用 assets 里的 EB Garamond（OFL，可变字体，API 26+），加载失败自动回退
   `FontFamily.Serif`；中文用系统 Serif（设备上通常是 Noto Serif CJK），不额外打包 CJK 字体。
7. **极端小屏**（横屏高度 <360dp）面板内容会滚动，主按钮始终在首屏；这是有意的兜底。
8. **APK 体积** +约 1 MB（851 KB 字体 + 197 KB dex）。
9. `HomeSettings` 的档位变化会重建 `GlassProfile`（一次重组），不影响游戏线程。

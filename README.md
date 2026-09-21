# 口算练习

一款面向 **小学三年级** 的口算训练 Android 应用（Kotlin，单模块 `:app`）。

## 功能

- **混合题型**：加减乘除随机出题，固定混合（无题型选择）。加/减操作数 ≤ 100、和不超 100、减法结果非负。
- **练习流程**：设置页选择题目数量（10 / 20 / 50 / 100）→ 开始练习 → 成绩页。
- **趣味化设计**：
  - 正确答案：高音提示 + 短震动 + 题目弹跳动画，答对立即进入下一题。
  - 错误答案：低音提示 + 输入框抖动（连击 ≥ 3 触发双段震动），显示正确答案约 0.5 秒后继续。
  - 实时计时与结果页「平均每题用时」。
  - 童趣称号与鼓励语（如「口算小状元」）。
- **皇冠系统**：50 题或 100 题全对即获得一枚皇冠，按皇冠数划分等级：青铜 / 白银 / 黄金 / 钻石 / 王者。
- **个人纪录**：记录每种题量的最佳成绩、用时与最高连击，打破纪录会显示「新纪录」。
- **错题本**：答错的题目自动收藏，可逐题重练；重练答对后自动从错题本移除。错题卡片对齐展示算式 / 正确答案 / 你的答案。
- **成就页**：查看皇冠数、当前等级、升级所需皇冠数以及全部个人最佳纪录。

## 技术栈

- Kotlin + AndroidX（Navigation、Lifecycle、ViewBinding）
- SQLite 自建 `ArithmeticDbHelper` / `ArithmeticRepository`（无 Room / KSP），三张表：
  - `wrong_problems`（错题，v1）
  - `rewards`（皇冠奖励，v2）
  - `records`（个人纪录，v3）
- 纯 Kotlin 逻辑（题目生成、皇冠判定、称号评价）与 Android 解耦，可在宿主 JVM 上直接单测。

## 构建

环境要求：Android SDK（`local.properties` 中 `sdk.dir`）、Gradle 9.6.0（随 wrapper 自动下载）。本机由 foojay resolver 自动供应 JDK 25，无需本地安装 JDK。

> Windows / PowerShell：本机 `java` 不在 PATH，构建前需显式设置 `JAVA_HOME`，例如：
>
> ```powershell
> $env:JAVA_HOME = "C:\Program Files\Java\latest\jdk-27"
> ```

| 命令 | 说明 |
| --- | --- |
| `.\gradlew.bat assembleDebug` | 构建 Debug APK |
| `.\gradlew.bat installDebug` | 安装到已连接的设备 / 模拟器 |
| `.\gradlew.bat test` | 运行宿主 JVM 单元测试（题目生成、错题选择、皇冠判定、称号评价） |
| `.\gradlew.bat lint` | 运行 Lint 检查 |

Release 构建：`.\gradlew.bat assembleRelease`（R8 已关闭），产物在 `app/build/outputs/apk/release/app-release-unsigned.apk`。
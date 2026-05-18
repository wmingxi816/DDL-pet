# DDL-pet

DDL鼠鼠是一个本地优先的 Android MVP：把任务打卡、DDL 提醒和宠物养成放在一起。完成任务获得积分，积分可以喂食、解锁装扮和表情；每天 05:00 后首次打开应用会生成昨日总结。

## Build

```powershell
.\gradlew.bat testDebugUnitTest
.\gradlew.bat assembleDebug
```

当前项目路径包含中文字符，Android Gradle 在 Windows 上需要 `android.overridePathCheck=true`。


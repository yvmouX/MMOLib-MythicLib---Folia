# 构建指南

MythicLib 的 Folia 版构建。构建产物是可直接放入服务器 `plugins` 目录的插件 JAR。

## 环境要求

| 组件 | 版本 | 说明 |
| --- | --- | --- |
| JDK | **25** | spigot 26.2 的 jar 是 Java 25 编译的,JDK 21 及以下无法编译 NMS 模块(报"类文件版本错误") |
| Maven | 3.9+ | 建议用本机 `D:\dev\tool\apache-maven-3.9.16` |

确认当前 shell 的 `JAVA_HOME` 指向 JDK 25:

```bash
mvn -version   # 应显示 Java version: 25.x
```

如果不是,构建前先设置:

```bat
set "JAVA_HOME=D:\dev\java\jdk-25"
```

## 构建

```bat
mvn install -pl mythiclib-plugin,mythiclib-rpg,mythiclib-v26_1_2,mythiclib-dist
```

或者直接运行仓库自带的脚本(会自动把产物复制到传入的服务器目录):

```bat
server_install.bat D:\path\to\server
```

构建完成后,产物在项目根目录:

```
target\MythicLib-1.7.1-SNAPSHOT.jar
```

## 首次构建准备(新机器必看)

以下步骤只在本地 Maven 仓库缺少构件时需要,做过一次即可:

1. **网络代理**(可选但强烈建议):`~/.m2/settings.xml` 中已配置 `127.0.0.1:7897` 代理(jitpack 直连)。代理未开启时删除 `<proxies>` 段,或改用直连(直连 phoenix 等仓库很慢)。
2. **TLS 版本**:本机代理会中断 Java 默认的 TLS 1.3 握手,构建时需加:
   ```bat
   set "MAVEN_OPTS=-Djdk.tls.client.protocols=TLSv1.2"
   ```
   不需要代理/网络直连正常时可省略。
3. **缺失构件需手动安装到本地仓库**(本地仓库路径是 `D:\dev\tool\apache-maven-3.9.16\m2_repo`):
   - `org.spigotmc:spigot:26.2-R0.1-SNAPSHOT` jar:从 `https://nexus.phoenixdevt.fr/repository/maven-public/org/spigotmc/spigot/26.2-R0.1-SNAPSHOT/spigot-26.2-R0.1-SNAPSHOT.jar` 下载(约 25 MB),安装时**用最小 POM 替换原 POM**(原 POM 引用了不存在的 `spigot-parent:dev-SNAPSHOT`):
     ```bat
     mvn install:install-file -Dfile=spigot-26.2-R0.1-SNAPSHOT.jar -DgroupId=org.spigotmc -DartifactId=spigot -Dversion=26.2-R0.1-SNAPSHOT -Dpackaging=jar -DgeneratePom=false
     ```
   - `de.tobiyas:RacesAndClasses:1.2.6`:从 `https://mvn.lumine.io/repository/maven-public/de/tobiyas/RacesAndClasses/1.2.6/RacesAndClasses-1.2.6.jar` 下载后:
     ```bat
     mvn install:install-file -Dfile=RacesAndClasses-1.2.6.jar -DgroupId=de.tobiyas -DartifactId=RacesAndClasses -Dversion=1.2.6 -Dpackaging=jar
     ```

## 注意事项

- 本仓库的 `pom.xml` 和 `mythiclib-dist/pom.xml` **只保留了 `mythiclib-v26_1_2` 版本模块**(原项目含 1.14 ~ 26.1 共 22 个版本模块,它们的 spigot 构件在公共仓库已不可获取,这也是 README 推荐的做法)。若需要支持其他 MC 版本,需从 git 历史恢复相应模块,并先通过 BuildTools 生成对应版本的 spigot 构件(`--rev <版本> --remapped`)。
- 构建输出 26.2 单版本插件,请在 MC 26.2 的 Folia 服务器上运行。

## Folia 适配 (YLib)

本仓库已适配 Folia,依赖 [YLib](https://github.com/yvmouX/YLib)(JitPack 坐标 `com.github.yvmouX:YLib:1.0.0-beta5`):

- 插件所有调度均通过 YLib 的 `UniversalScheduler`(Folia 下自动使用 GlobalRegion/Region/Entity/Async 调度器)。
- YLib 在构建时由 maven-shade 打包进插件 jar,并重定位到 `io.lumine.mythic.lib.ylib`(`META-INF/services` 已通过 `ServicesResourceTransformer` 合并并重定位)。
- 技能 tick 器按实体/位置作用域调度,玩家数据 tick(药水效果、被动技能、在线检查)按玩家实体调度。
- 首次构建需联网从 JitPack 拉取 YLib 构件(本机 Maven 代理对 `jitpack.io` 走直连)。

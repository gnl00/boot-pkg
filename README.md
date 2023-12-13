# boot-pkg
> 实现外部 jar 的动态加载

## 前言

### 项目结构

```shell
boot-pkg
├── boot-pkg-core 核心功能
├── boot-pkg-lite 包含核心功能的最小实现
├── boot-pkg-spi  SPI 接口模块。暴露 BootSpi#load 方法给子类实现。类似 jdbc。
├── boot-pkg-spi-impl  SPI 实现模块 1，依赖 SPI 接口模块。类似 mysql-connector-java，是 jdbc 的 mysql 实现。
├── boot-pkg-spi-impl-a  SPI 接口模块 2，依赖 SPI 接口模块。类似 postgresql-connector-java，是 jdbc 的 pg 实现。
```

...

在开发中如果我们想要使用 jdbc 只需要引入 mysql-connect-java 或者 postgresql-connector-java 即可，不需要关心 jdbc 的实现细节。同样的，在此处我们也不需要关心 SPI 的实现细节，只需要引入 boot-pkg-spi-impl-xxx 即可。

...

### 功能

扯远了，回到正题。本项目主要实现的是 jar 包的动态加载功能，可自定义选择指定的 jar 包进行动态加载。

...

### 使用
1、将 boot-pkg-core 打包安装到本地仓库

2、启动 boot-pkg-lite，访问 `http://localhost:8080/lite` 查看 lite 的状态；访问 `http://localhost:8080/info` 查看运行平台相关信息。

3、启动成功后，将 boot-pkg-spi-impl 和 boot-pkg-spi-impl-a 打包，并分别从 target 文件夹中将 jar 包拷贝到 boot-pkg 的根目录下
此时项目结构大概如下：

```shell
boot-pkg
├── boot-pkg-core
├── boot-pkg-lite
├── boot-pkg-spi-1.0-SNAPSHOT.jar
├── boot-pkg-spi-a-1.0-SNAPSHOT.jar
```

4、访问 `http://localhost:8080/localJars` 查看本地 jar 包列表，可以看到刚刚拷贝的两个 jar 包

```json
{
    "boot-pkg-spi-impl-a-1.0-SNAPSHOT": ".\boot-pkg-spi-impl-a-1.0-SNAPSHOT.jar",
    "boot-pkg-spi-1.0-SNAPSHOT": ".\boot-pkg-spi-1.0-SNAPSHOT.jar"
}
```

5、访问 `http://localhost:8080/loadFromLocal?jarName=boot-pkg-spi-impl-a-1.0-SNAPSHOT` 加载 jar 包。

再次访问 `http://localhost:8080/info` 会发现此时系统属性多了一个键值对
```json
{
    "boot-pkg-spi-impl-a-1.0-SNAPSHOT": ".\boot-pkg-spi-impl-a-1.0-SNAPSHOT.jar"
}
```

*注意：这一步创建了一个新的 ClassLoader 来加载外部 jar 包，每次想要加载 Jar 包中的类必须使用加载了该 Jar 的 ClassLoader。*

6、访问 `http://localhost:8080/dynamicLoad` 将自定义的 ClassLoader 加载到 VM 中，并执行 spi 接口的实现方法。
可以从控制台看到两个输出：
```shell
BootSpiImpl #1
BootSpiImplAAA load #2
```

第一行输出是因为 boot-pkg-lite 项目中引入了 boot-pkg-spi-impl 的依赖，所以在启动的时候就已经加载了 BootSpiImpl 类，自定义的 SpiClassLoader 也能访问到它。
第二行输出是因为第 5 步使用自定义的 SpiClassLoader 来加载了 boot-pkg-spi-impl-a 的 jar 包，所以能够访问到 BootSpiImplAAA 类。

...

至此，就实现了外部 jar 的动态加载。

...

7、如果此时访问 `localhost:8080/classpathLoad`，会发现只有第一行输出，第二行输出消失了。这是因为此时使用的是 AppClassLoader，而外部的 jar 使用的是自定义的 SpiClassLoader 加载。所以 AppClassLoader 无法访问到该动态加载的 jar。

...

---

## java -cp 命令

```shell
# linux/unix
# 加载多个 JAR 文件
java -cp abc.jar:edf.jar:123.jar <main-class>
java -cp "./lib/*" org.springframework.boot.loader.JarLauncher
```
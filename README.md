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

4、访问 `http://localhost:8080/rootJars` 查看根目录 jar 包列表，可以看到之前拷贝的两个 jar 包

```json
{
    "boot-pkg-spi-impl-a-1.0-SNAPSHOT": ".\boot-pkg-spi-impl-a-1.0-SNAPSHOT.jar",
    "boot-pkg-spi-1.0-SNAPSHOT": ".\boot-pkg-spi-1.0-SNAPSHOT.jar"
}
```

5、访问 `http://localhost:8080/loadFromRoot?jarName=` 加载 jar 包。

*注意：这一步创建了一个新的 ClassLoader 来加载外部 jar 包，想要加载 Jar 包中的类必须使用加载了该 Jar 的 ClassLoader。*

6、访问 `http://localhost:8080/execute?pluginName=boot-pkg-spi-impl-a-1.0-SNAPSHOT` 将自定义的 ClassLoader 加载到 VM 中，并执行 SPI 接口的实现方法。
可以从控制台看到两个输出：

```shell
BootSpiImpl #1
BootSpiImplAAA load #2
```

第一行输出是因为 boot-pkg-lite 项目中引入了 boot-pkg-spi-impl 的依赖，所以在启动的时候就已经加载了 BootSpiImpl 类，自定义的 DynamicClassLoader 也能访问到它。
第二行输出是因为第 5 步使用自定义的 DynamicClassLoader 来加载了 boot-pkg-spi-impl-a 的 jar 包，所以能够访问到 BootSpiImplAAA 类。

...

至此，就实现了外部 jar 的动态加载。

...

7、如果此时访问 `localhost:8080/classpathLoad`，会发现只有第一行输出，第二行输出消失了。这是因为此时使用的是 AppClassLoader，而外部的 jar 使用的是自定义的 DynamicClassLoader 加载。所以 AppClassLoader 无法访问到该动态加载的 jar。

...

---

## Jar 包卸载

### 加载过程

要实现 jar 包的卸载，需要看一下

1、动态加载的时候 ClassLoader 都做了什么事情

看到 `java.net.URLClassLoader#addURL`

```java
public synchronized void addURL(URL url) {
  if (closed || url == null)
    return;
  synchronized (unopenedUrls) {
    if (! path.contains(url)) {
      unopenedUrls.addLast(url);
      path.add(url);
    }
  }
}
```

**将 URL 添加到 unopenedUrls 和 path 字段**。

很好解决，只要将 Jar 包 URL 从自定义 ClassLoader 中删除。

…

2、执行 jar 包的时候做了什么事情

执行的时候比较能藏，需要 debug 才能发现执行的时候涉及到 `URLClassPath#loaders` 和`URLClassPath#lmap` 这两个字段。

又要再进行一次反射获取到 URLClassPath.Loader 类

…

---

### ~~利用反射机制卸载~~

~~1、反射获取 URLClassLoader 的 ucp 字段；~~
~~2、然后再反射获取 URLClassPath 的 unopenedUrls 或者 paths 进行操作。~~
但由于使用的是 Java 17，受到模块化系统的限制，反射获取 URLClassLoader 时报错 `module java.base does not "opens java.net" to unnamed module`。
需要在 VM 添加参数

```shell
--add-opens=java.base/java.net=ALL-UNNAMED
```
...

获取 URLClassPath 报错 `module java.base does not export jdk.internal.loader to unnamed module`
需要在 VM 添加
```shell
--add-exports java.base/jdk.internal.loader=ALL-UNNAMED
```

...

### ~~Unsafe 类~~

Unsafe 和实际上也是利用反射机制。

…

> 上面这两个方法不太行。因为要进行太多次反射了，很麻烦。

...

### ClassLoader 代理

只要没有执行 `ServiceLoader.load`，自定义的 ClassLoader 中的 URLClassLoader  的 loaders 和 lmap 字段就不会存在值。

此时想要卸载就只要从 unopenedUrls 和 path 集合中删除对应的 URL 即可。

所以只要在执行的时候使用代理的 ClassLoader 来执行，卸载的时候从原来的 Class Loader 移除 URL 即可。

经过一番修改，卸载功能如下：

```java
public static boolean executeByName(String pluginName) {
  if (Objects.isNull(dynamicLoader)) {
    initLoader();
  }
  PluginMetadata plugin = installedPlugins.get(pluginName);
  if (Objects.isNull(plugin)) {
    log.error("plugin not found");
    return false;
  }
  // create a proxy classloader
  DynamicClassloader classLoader = plugin.getClassLoader();
  DynamicClassloader pcl = getProxyClassLoader(classLoader);
  dynamicLoader.load(pcl);
  pcl = null; // release resource
  return true;
}

private static DynamicClassloader getProxyClassLoader(DynamicClassloader cl) {
  Enhancer enhancer = new Enhancer();
  enhancer.setSuperclass(cl.getClass());
  enhancer.setCallback(NoOp.INSTANCE); // 设置 Callback 为 NoOp.INSTANCE，表示不对代理对象执行的方法进行任何处理
  return (DynamicClassloader) enhancer.create(new Class[]{URL[].class}, new Object[]{cl.getURLs()});
}
```

利用动态代理 ClassLoader 来执行目标方法，执行完成之后将代理对象释放。

> 不错，现在基本上完成了想要的功能。

…

### 自定义多个 ClassLoader

此外还可以考虑为每个插件自定义一个 ClassLoader，可以采用“强软弱虚”引用中的“弱引用”。在不需要该 ClassLoader 的之后就设置为 null，JVM 垃圾回收器一发现该对象不可达便立即回收。

…

该方法的缺点是：如果有很多个插件，那就需要创建很多个 ClassLoader。和使用动态代理相比哪一个方法更加适合？

> 如果你的需求是对代理对象的方法进行拦截、增强或修改，并且不需要频繁地加载和卸载插件，那么使用动态代理可能更适合。它提供了更灵活的控制和修改代理对象的能力。
>
> 如果你的需求是动态加载和卸载插件，并及时释放插件所占用的资源，那么自定义 ClassLoader 可能更适合。它可以实现更精确的资源管理，但需要考虑额外的插件管理和 ClassLoader 生命周期管理的复杂性。

> 而且使用自定义多个 ClassLoader 的方法不需要进行额外的反射操作来获取 URLClassLoader 和 URLClassPath。

…

> 目前采用此方法

…

**卸载流程**

1、访问 `http://localhost:8080/plugins` 查看已安装的插件列表

2、选择插件进行卸载 `http://localhost:8080/unload?pluginName=boot-pkg-spi-impl-a-1.0-SNAPSHOT`

3、再次执行 `http://localhost:8080/execute?pluginName=boot-pkg-spi-impl-a-1.0-SNAPSHOT`，会发现显示插件执行失败。

4、访问 `http://localhost:8080/plugins` 可以看到该插件已经被删除。

…

---

## java -cp 命令

```shell
# linux/unix
# 加载多个 JAR 文件
java -cp abc.jar:edf.jar:123.jar <main-class>
java -cp "./lib/*" org.springframework.boot.loader.JarLauncher
```

...

---

## 参考

**Unsafe**
- https://www.cnblogs.com/trunks2008/p/14720811.html
- https://tech.meituan.com/2019/02/14/talk-about-java-magic-class-unsafe.html
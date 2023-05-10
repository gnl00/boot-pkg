# boot-pkg
SpringBoot 打包与 SPI 动态加载测试

<br>

```shell
# 加载多个 JAR 文件
java -cp abc.jar:edf.jar:123.jar <main-class>
java -cp "./lib/*" org.springframework.boot.loader.JarLauncher
```
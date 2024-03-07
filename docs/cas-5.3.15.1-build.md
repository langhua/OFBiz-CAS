# 编译cas 5.3.15.1

上次编译cas 5.3.15.1，还是2020年7月，今天重新编译，发现编译不过了。本文记录了遇到的问题，及我使用的解决方法。

### 运行环境

JDK: Corretto 1.8.0_372
IDE: IntelliJ IDEA 2023.1.2 (Community Edition)
OS: Windows 11

<br>

### 编译中遇到的问题及解决方法

1. propdeps-plugin：

    ```text
    A problem occurred configuring root project 'cas-server'.
    > Could not resolve all artifacts for configuration ':classpath'.
       > Could not resolve io.spring.gradle:propdeps-plugin:0.0.10.RELEASE.
         Required by:
             project :
          > Could not resolve io.spring.gradle:propdeps-plugin:0.0.10.RELEASE.
             > Could not get resource 'https://repo.spring.io/plugins-release/io/spring/gradle/propdeps-plugin/0.0.10.RELEASE/propdeps-plugin-0.0.10.RELEASE.pom'.
                > Could not HEAD 'https://repo.spring.io/plugins-release/io/spring/gradle/propdeps-plugin/0.0.10.RELEASE/propdeps-plugin-0.0.10.RELEASE.pom'. Received status code 401 from server: 
    ```

   【解决方法】

   编辑根目录下的build.gradle，改用阿里云的maven仓库：

   ```gradle
           maven { url "https://repo.spring.io/plugins-release" }
   ```
   改为：
   ```gradle
           // maven { url "https://maven.aliyun.com/repository/public" }
           maven { url "https://maven.aliyun.com/repository/spring-plugin" }
   ```

<br>

2. duo-client问题：

   ```text
   > Could not resolve net.unicon.iam:duo-client:0.2.2.
     Required by:
         project :support:cas-server-support-duo-core
      > Could not resolve net.unicon.iam:duo-client:0.2.2.
         > Could not get resource 'https://repo.spring.io/plugins-release/net/unicon/iam/duo-client/0.2.2/duo-client-0.2.2.pom'.
            > Could not GET 'https://repo.spring.io/plugins-release/net/unicon/iam/duo-client/0.2.2/duo-client-0.2.2.pom'
   ```

   【解决方法】

   问题原因是https://dl.bintray.com/uniconiam/maven这个maven库不存在了。将gradle/dependencies.gradle中的net.unicon.iam:duo-client:0.2.2，改为com.duosecurity:duo-client:0.3.0即可，如下所示：

   ```gradle
           duojava                 : [
                   dependencies.create("net.unicon.iam:duo-java:$duoVersion") {
                       exclude(group: "org.slf4j", module: "slf4j-api")
                       force = true
                   },
   //                dependencies.create("net.unicon.iam:duo-client:0.2.2") {
   //                    exclude(group: "org.slf4j", module: "slf4j-api")
   //                    force = true
   //                }
                   dependencies.create("com.duosecurity:duo-client:0.3.0") {
                       exclude(group: "org.slf4j", module: "slf4j-api")
                       force = true
                   }
           ],
   ```

<br>

3. jcifs-ext问题：

   ```text
   Could not resolve all files for configuration ':support:cas-server-support-spnego:compileClasspath'.
   > Could not resolve org.samba.jcifs:jcifs-ext:0.9.4.
     Required by:
         project :support:cas-server-support-spnego
      > Could not resolve org.samba.jcifs:jcifs-ext:0.9.4.
         > Could not get resource 'https://repo.spring.io/plugins-release/org/samba/jcifs/jcifs-ext/0.9.4/jcifs-ext-0.9.4.pom'.
            > Could not GET 'https://repo.spring.io/plugins-release/org/samba/jcifs/jcifs-ext/0.9.4/jcifs-ext-0.9.4.pom'
   ```

   【解决方法】

   参考[SPNEGO-Authentication.md](https://github.com/apereo/cas/blob/v6.4.0/docs/cas-server-documentation/authentication/SPNEGO-Authentication.md#jcifs-sdk)中提到的解决方法，将[jcifs-ext.jar](https://github.com/apereo/cas/blob/v6.4.0/support/cas-server-support-spnego/lib/jcifs-ext.jar)下载到support/cas-server-support-spnego/lib/下，然后在support/cas-server-support-spnego/build.gradle和support/cas-server-support-spnego-webflow/build.gradle中添加该文件：

   ```gradle
   implementation files(project(":support:cas-server-support-spnego").file("lib/jcifs-ext.jar"))
   ```

   编辑gradle/dependencies.gradle，注释掉jcifs-ext的依赖：

   ```gradle
           jcifs                   : [
                   dependencies.create("jcifs:jcifs:$jcifsVersion") {
                       exclude(group: "javax.servlet", module: "servlet-api")
                       force = true
                   },
   //                dependencies.create("org.samba.jcifs:jcifs-ext:$jcifsExtVersion") {
   //                    exclude(group: "javax.servlet", module: "servlet-api")
   //                    force = true
   //                }
           ],
   
   ```

<br>

4. node下载和版本问题：

   ```text
   Execution failed for task ':webapp:cas-server-webapp:nodeSetup'.
   > Could not resolve all files for configuration ':webapp:cas-server-webapp:detachedConfiguration134'.
      > Could not resolve org.nodejs:node:8.11.2.
        Required by:
            project :webapp:cas-server-webapp
         > Could not resolve org.nodejs:node:8.11.2.
            > Could not get resource 'https://nodejs.org/dist/v8.11.2/ivy.xml'.
               > Could not GET 'https://nodejs.org/dist/v8.11.2/ivy.xml'. 
   ```

   【解决方法】

   原因是nodejs.org这个库无法访问，改用镜像网站即可。编辑webapp/gradle/webapp.gradle，改为镜像网站：

   ```gradle
   node {
      version = "${nodeVersion}"
      nodeModulesDir = project(":webapp").projectDir
      // https://mirrors.tuna.tsinghua.edu.cn/nodejs-release for China
      // or https://mirrors.dotsrc.org/nodejs/release
      distBaseUrl = 'https://mirrors.tuna.tsinghua.edu.cn/nodejs-release'
      download = true
   }
   ```

<br>

   再次编译时出现下面的错误：
   
   ```text
   > Task :webapp:cas-server-webapp:npm_cache_verify
   npm ERR! stream.push() after EOF
   
   npm ERR! A complete log of this run can be found in:
   npm ERR!     C:\Users\shi.jinghai-honor\AppData\Roaming\npm-cache\_logs\2024-03-07T07_23_57_255Z-debug.log
   
   > Task :webapp:cas-server-webapp:npm_cache_verify FAILED
   
   FAILURE: Build failed with an exception.
   ```

   【解决方法】

   编辑gradle.properties文件，nodeVersion改用8.12.0：


   ```properties
   # nodeVersion=8.11.2
   nodeVersion=8.12.0
   ```

   <br>   

5. jgit版本问题：

   ```text
      org/eclipse/jgit/api/Git has been compiled by a more recent version of the Java Runtime (class file version 55.0), this version of the Java Runtime only recognizes class file versions up to 52.0
   ```

   原因：编译时使用的jgit版本高了，需要指定一个较低的版本。

   【解决方法】

   在build.gradle文件中，添加符合JDK 1.8的jgit版本限定：

```gradle
    dependencies {
        ...
        classpath("org.eclipse.jgit:org.eclipse.jgit:4.11.0.201803080745-r") {
            force true
        }
        classpath("org.eclipse.jgit:org.eclipse.jgit.ui:4.11.0.201803080745-r") {
            force true
        }
    }
```

<br>

--- End ---

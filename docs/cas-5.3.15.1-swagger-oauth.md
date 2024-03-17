# 支持Swagger OAuth2认证流程

### 网址 /oauth/v2 是如何来的？

/oauth在plugins/cas/ofbiz-component.xml中定义的：

```xml
    <webapp name="oauth2"
        title="OAuth2"
        server="default-server"
        location="webapp/cas-5.3.15.1"
        mount-point="/oauth"
        app-bar-display="false"/>
```

/v2在cas的OAuth20Constants.java中定义的，原本是/oauth2.0：

```java
public interface OAuth20Constants {
...
-    String BASE_OAUTH20_URL = "/oauth2.0";
+    String BASE_OAUTH20_URL = "/v2";
     ...
     }
```

在cas的support/cas-server-support-oauth-core中运行'mvn clean package'，然后把support/cas-server-support-oauth-core/build/libs/cas-server-support-oauth-core-5.3.15.1.jar和support/cas-server-support-oauth/build/libs/cas-server-support-oauth-5.3.15.1.jar复制到本模块的lib目录下。

<br>

### 在webapp/openapi-demo/yaml/demo_zh.yaml中定义5个认证方式

这5个认证方式是：authorizationCode、implicit、password、clientCredentials和bearer，定义和使用方式如下：

```yaml
...
security:
  - codeLogin: []
  - implicitLogin: []
  - passwordLogin: []
  - clientLogin: []
  - oauth2Bearer: []
...
  securitySchemes:
    codeLogin:
      type: oauth2
      description: OAuth2授权码登录
      flows:
        authorizationCode:
          authorizationUrl: 'https://localhost:8443/oauth/v2/authorize'
          tokenUrl: 'https://localhost:8443/oauth/v2/accessToken'

    implicitLogin:
      type: oauth2
      description: OAuth2隐式登录
      flows:
        implicit:
          authorizationUrl: '/oauth/v2/authorize'
          tokenUrl: '/oauth/v2/accessToken'

    passwordLogin:
      type: oauth2
      description: OAuth2密码登录
      flows:
        password:
          tokenUrl: '/oauth/v2/accessToken'

    clientLogin:
      type: oauth2
      description: OAuth2客户端登录
      flows:
        clientCredentials:
          tokenUrl: '/oauth/v2/accessToken'

    oauth2Bearer:
      type: http
      description: OAuth2 access token in http bearer header
      scheme: bearer
```
<br>

### 修改logout

在验证5个认证方式之前，先修改logout方法，解决swagger-ui缺省情况下，无法退出OFBiz登录的问题。

### 授权码模式（Authorization code）




### 隐式授权模式（Implicit）



### 密码模式（password）


### 客户端模式（Client Credentials）


### 参考资料
1. [Swagger OAuth2.0](https://swagger.io/docs/specification/authentication/oauth2/)
2. [Logout button url](https://github.com/swagger-api/swagger-ui/issues/6443)
3. [Plugin API Wrap-Actions](https://swagger.io/docs/open-source-tools/swagger-ui/customization/plugin-api/#wrap-actions)

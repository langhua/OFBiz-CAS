OFBiz-CAS Plugin
====
[EN](README.md)

### 版权
[Apache License V2.0](LICENSE.txt)


### 贡献
如果你有改进或发现错误，强烈建议你简单地新拉一个请求（New pull request），把改进或错误说明下即可。不需要建单独建问题（issue），请求（request）的处理过程很容易追溯。


### 功能
1. Use OFBiz entity engine to store CAS TicketGrantingTicket, ServiceTicket, RegisteredService and etc.
2. Support OAuth login with OFBiz username and password.
3. Support OFBiz login policy, when max.failed.logins reached, the user login will be disabled for the minutes configured by login.disable.minutes.
4. Import service configurations from json files under runtime/cas/services into CasRegisteredService table when there is no data in the table.
5. The OAuth protocol supported as described in [https://apereo.github.io/cas/4.2.x/installation/OAuth-OpenId-Authentication.html](CAS document).


### 配置

** 在OFBiz 17.12上开发和测试 **

1. 修改${ofbiz.home}/build.gradle：

** 让log4j2-cas.xml配置生效：**

```groovy
def jvmArguments = ['-Xms1024M', '-Xmx2048M',
                    '-Dfile.encoding=UTF-8',
                    '-Dlog4j.configurationFile=log4j2.xml,log4j2-cas.xml']
...
compile 'com.lmax:disruptor:3.4.2'
```

** 让slf4j、 JCL and java.util.logging都通过slf4j-api代理到log4j2，最后由log4j2来写日志文件，保持与OFBiz等一致**

[原理参见](https://www.slf4j.org/legacy.html)

```groovy
subprojects {
    configurations {
        // compile-time plugin libraries
        pluginLibsCompile {
            exclude module:'log4j-slf4j-impl'
            exclude module:'log4j-jcl'
        }
        // runtime plugin libraries
        pluginLibsRuntime {
            exclude module:'log4j-slf4j-impl'
            exclude module:'log4j-jcl'
        }
        //compile-only libraries
        pluginLibsCompileOnly
    }
}
```

** 检查依赖关系的配置是否正确 **

```
./gradlew dependencies --configuration runtime > dependencies.txt
```

2. 把cas模块加入到${ofbiz.home}/plugins/component-load.xml中：

```xml
    <load-component component-location="cas"/>
```

3. 新建runtime/cas/services目录，在目录下新建本机测试所需的认证服务设置：

** runtime/cas/services/LocalhostHttps-10000002.json **

```json
{
  "@class" : "org.jasig.cas.support.oauth.services.OAuthRegisteredService",
  "clientId": "clientId",
  "clientSecret": "clientSecret",
  "bypassApprovalPrompt": true,
  "serviceId" : "^https://localhost:8443/.*",
  "name" : "OFBiz OAuth2",
  "id" : 10000002,
  "logo": "https://ofbiz.apache.org/images/ofbiz_logo.png",
  "attributeReleasePolicy" : {
    "@class" : "org.jasig.cas.services.ReturnAllowedAttributeReleasePolicy",
    "allowedAttributes" : [ "java.util.ArrayList", [ "partyId", "groupMembership", "firstName", "lastName" ] ],
    "principalAttributesRepository" : {
      "@class" : "org.jasig.cas.authentication.principal.DefaultPrincipalAttributesRepository"
    },
    "authorizedToReleaseCredentialPassword" : false,
    "authorizedToReleaseProxyGrantingTicket" : false
  }
}
```

** runtime/cas/services/LocalhostOAuthCallbackurl-10000003.json **

```json
{
  "@class": "org.jasig.cas.support.oauth.services.OAuthCallbackAuthorizeService",
  "serviceId": "https://localhost:8443/oauth/v2/callbackAuthorize",
  "name": "Localhost OAuth Callback url",
  "id": "10000003",
  "description": "Localhost OAuth Wrapper Callback Url",
  "evaluationOrder": "0",
  "logo": "https://ofbiz.apache.org/images/ofbiz_logo.png",
  "logoutType": "BACK_CHANNEL",
  "accessStrategy":
  {
    "@class": "org.jasig.cas.services.DefaultRegisteredServiceAccessStrategy",
    "enabled": "true",
    "ssoEnabled": "true",
    "requireAllAttributes": "true",
    "caseInsensitive": "false"
  }
}
```

4. 启动ofbiz：

```
./gradlew ofbiz
```


5. 登录测试：

** 认证：/oauth/v2/authorize **

```
例：https://localhost:8443/oauth/v2/authorize?response_type=code&client_id=clientId&redirect_uri=https://localhost:8443/webtools/control/ping
```

![](./cas-4.2.7-login.png)


** 授权：/oauth/v2/callbackAuthorize **

![](./cas-4.2.7-callback.png)


** 取得access_token：/oauth/v2/accessToken **

```
例：https://localhost:8443/oauth/v2/accessToken?grant_type=authorization_code&client_id=clientId&&client_secret=clientSecret&redirect_uri=https://localhost:8443/webtools/control/ping&code=CODE
```

![](./cas-4.2.7-accesstoken.png)


** 取得用户资料：/oauth/v2/profile **

```
例：https://localhost:8443/oauth/v2/profile?access_token=ACCESS_TOKEN
```

![](./cas-4.2.7-profile.png)



### 开发笔记

[详见开发笔记](DEV_README_zh.md)

OFBiz-CAS Plugin
====
[中文](README_zh.md)


### License
[Apache License V2.0](LICENSE.txt)


### Contributions
If you have already identified an enhancement or a bug, it is STRONGLY recommended that you simply submit a pull request to address the case. There is no need for special ceremony to create separate issues. The pull request IS the issue and it will be tracked and tagged as such.


### Functions
1. Use OFBiz entity engine to store CAS TicketGrantingTicket, ServiceTicket, RegisteredService and etc.
2. Support OAuth login with OFBiz username and password.
3. Support OFBiz login policy, when max.failed.logins reached, the user login will be disabled for the minutes configured by login.disable.minutes.
4. Import service configurations from json files under runtime/cas/services into CasRegisteredService table when there is no data in the table.
5. The OAuth protocol supported as described in [https://apereo.github.io/cas/5.3.x/installation/OAuth-OpenId-Authentication.html](CAS document).

![](https://alexbilbie.com/images/oauth-grants.svg)


### Environment
This plugin is for OFBiz 17.12 with Tomcat 8.5.x and CAS 5.3.x.


### Configurations

** Developed and tested in OFBiz 17.12 **

1. Modify ${ofbiz.home}/build.gradle:

** Make lombok work: **

```groovy
buildscript {
    repositories {
        jcenter()
		maven { url "https://plugins.gradle.org/m2/" }
    }
    dependencies {
        classpath "at.bxm.gradleplugins:gradle-svntools-plugin:latest.release"
		classpath "io.franzbecker:gradle-lombok:1.14"
    }
}

apply plugin: "io.franzbecker.gradle-lombok"
```

** Make jvmArguments in root project build.gradle accessible from plugins/cas/build.gradle **

```groovy
ext.jvmArguments = ['-Xms1024M', '-Xmx2048M', '-Dfile.encoding=UTF-8']
```

** Change Tomcat to 8.5.x **

```groovy
    compile 'org.apache.tomcat:tomcat-catalina-ha:8.5.35'
    compile 'org.apache.tomcat:tomcat-catalina:8.5.35'
    compile 'org.apache.tomcat:tomcat-jasper:8.5.35'
    compile 'org.apache.tomcat:tomcat-tribes:8.5.35'
```

** Change Spring to 4.3.20.RELEASE **

```groovy
    compile 'org.springframework:spring-test:4.3.20.RELEASE'
```


** Delegate slf4j、 JCL and java.util.logging to log4j2 by slf4j-api, and logs are written to files by log4j2 in compliance with OFBiz. **

[See SLF4J for details](https://www.slf4j.org/legacy.html)

```
./gradlew dependencies --configuration runtime > dependencies.txt
```
Check whether the log dependencies are correct

2. Add the cas plugin in ${ofbiz.home}/plugins/component-load.xml:

```xml
    <load-component component-location="cas"/>
```

3. Create directory runtime/cas/services and add service configuration files in the directory:

** runtime/cas/services/LocalhostHttps-10000002.json **

```json
{
  "@class" : "org.apereo.cas.support.oauth.services.OAuthRegisteredService",
  "clientId": "clientId",
  "clientSecret": "clientSecret",
  "bypassApprovalPrompt": true,
  "serviceId" : "^https://localhost:8443/.*",
  "name" : "OFBiz OAuth2",
  "id" : 10000002,
  "logo": "https://ofbiz.apache.org/images/ofbiz_logo.png",
  "evaluationOrder": 10,
  "supportedGrantTypes": [ "java.util.HashSet", [ "AUTHORIZATION_CODE", "PASSWORD", 
                                                  "CLIENT_CREDENTIALS", "REFRESH_TOKEN" ] ],
  "generateRefreshToken": true,
  "attributeReleasePolicy" : {
    "@class" : "org.apereo.cas.services.ReturnAllowedAttributeReleasePolicy",
    "allowedAttributes" : [ "java.util.ArrayList", 
                            [ "userLoginId", "partyId", "groupName", "firstName", "lastName", "currentPassword" ]
                          ],
    "principalAttributesRepository" : {
      "@class" : "org.apereo.cas.authentication.principal.DefaultPrincipalAttributesRepository"
    },
    "authorizedToReleaseCredentialPassword" : false,
    "authorizedToReleaseProxyGrantingTicket" : false
  },
  "accessStrategy" : {
    "@class" : "org.apereo.cas.services.DefaultRegisteredServiceAccessStrategy",
    "enabled" : true,
    "ssoEnabled" : true,
    "requireAllAttributes" : false
  }
}
```


4. Start OFBiz:

```
./gradlew ofbiz
```

5. Tests:

** Machine-to-Machine Authentication: /oauth/v2/accessToken **

```
Url example:  https://localhost:8443/oauth/v2/accessToken?grant_type=client_credentials&client_id=clientId&client_secret=clientSecret
```

** One Step Authentication: /oauth/v2/accessToken **

```
Url example:  https://localhost:8443/oauth/v2/accessToken?grant_type=password&client_id=clientId&username=admin&password=ofbiz
```


** Login: /oauth/v2/authorize **

```
Url example:  https://localhost:8443/oauth/v2/authorize?response_type=code&client_id=clientId&redirect_uri=https://localhost:8443/webtools/control/ping
```


** Authorize to the application: /oauth/v2/callbackAuthorize **



** Get access_token: /oauth/v2/accessToken **

```
Url example:  https://localhost:8443/oauth/v2/accessToken?grant_type=authorization_code&client_id=clientId&&client_secret=clientSecret&redirect_uri=https://localhost:8443/webtools/control/ping&code=CODE
```

** Get profile: /oauth/v2/profile **

```
Url example:  https://localhost:8443/oauth/v2/profile?access_token=ACCESS_TOKEN
```


### Development Note

[See development readme](DEV_README.md)

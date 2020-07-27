[English](README.md) | [中文](docs/README_ZH.md)


# OFBiz-CAS Plugin


### License
[Apache License V2.0](LICENSE)

<br/>

### Contributions
Welcome any kind of contributions to this plugin.

<br/>

### Environment
This plugin is for OFBiz 17.12.03 with Tomcat 9.0.31 and CAS 5.3.15.1.

<br/>

### Quick Start

**1. Checkout OFBiz 17.12.03 from https://github.com/apache/ofbiz-framework**

<br/>

**2. Apply patches under patches/ofbiz to OFBiz**

<br/>

**3. Download this plugin**

<br/>

**4. Deploy this plugin in plugins/drools/**

<br/>

**5. Gradle 5.0 is OK in my environment. If not in yours, please edit gradle/wrapper/gradle-wrapper.properties, change to use gradle 5.6:**

```
distributionUrl=https\://services.gradle.org/distributions/gradle-5.6-bin.zip
```

<br/>

**6. Install OFBiz seed data by command:**

```
gradle loadAll
```

<br/>

**7. Start OFBiz by command:**

```
gradle ofbiz
```

<br/>

**8. In browser, visit https://localhost:8443/oauth/login**

Username: admin

Password: ofbiz

After login successfully, you can see kie server information.

![kie server](docs/images/sandflower-kie-server-7.17.0.Final.png)

<br/>

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

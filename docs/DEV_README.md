The steps to build OFBiz CAS plugin
====
[中文](DEV_README_zh.md)

### Targets
1. Deploy/run CAS in OFBiz.
2. Read/write data of registered service, service ticket and ticket-granting ticket of CAS by OFBiz entity engine and HA ready.

### Core problem
CAS can support JPA as database storage, but OFBiz entity engine. The concepts of their implements are different. CAS resolves authz/authn problems delicately and flexibly by several tables, it maps data to java classes, it's object-oriented programming. OFBiz handles about a thousand tables, it chooses event-driven programming from the very beginning, this technical decision helps OFBiz can run faster and less memory consumption than object-oriented implement.

The core problem to integrate CAS with OFBiz entity engine is ORM, in CAS 4.2.7, there's several private fields in service ticket and ticket-granting ticket cannot be constructed/set publicly. Lucky enough we have ASM to help us resolve this problem at runtime.

### Core Implements

1. Apply plugins/cas/patches/cas/cas-server-core-tickets-4.2.7.patch to CAS 4.2.7

2. Build cas-server-core-tickets

3. Download the following jars and put them in cas root directory:

```
asm-5.1.jar
asm-util-5.1.jar
aspectjrt-1.8.6.jar
commons-lang3-3.4.jar
guava-18.0.jar
hibernate-jpa-2.1-api-1.0.0.Final.jar
joda-time-2.9.4.jar
slf4j-api-1.7.12.jar
spring-core-4.2.8.RELEASE.jar
validation-api-1.1.0.Final.jar
```

4. Run the following commands to dump the classes:

```
java -classpath ./*;./cas-server-core-tickets/build/libs/* org.objectweb.asm.util.ASMifier org.jasig.cas.ticket.AbstractTicket > AbstractTicketDump.java
java -classpath ./*;./cas-server-core-tickets/build/libs/* org.objectweb.asm.util.ASMifier org.jasig.cas.ticket.TicketGrantingTicketImpl > TicketGrantingTicketImplDump.java
java -classpath ./*;./cas-server-core-tickets/build/libs/* org.objectweb.asm.util.ASMifier org.jasig.cas.ticket.ServiceTicketImpl > ServiceTicketImplDump.java
java -classpath ./*;./cas-server-core-tickets/build/libs/* org.objectweb.asm.util.ASMifier org.jasig.cas.ticket.ProxyGrantingTicketImpl > ProxyGrantingTicketImplDump.java
```

5. Copy the 4 dump java files to plugins/cas/src/main/java/org/langhua/ofbiz/cas/asm

6. In OFBizTicketRegistry.java:

Load the dump classes at runtime:

```java
    private static Class<?> stClass;
    
    private static Class<?> tgtClass;
    
    static {
        try {
            LOADER.defineClass("org.jasig.cas.ticket.AbstractTicket", AbstractTicketDump.dump());
            stClass = LOADER.defineClass("org.jasig.cas.ticket.ServiceTicketImpl", ServiceTicketImplDump.dump());
            tgtClass = LOADER.defineClass("org.jasig.cas.ticket.TicketGrantingTicketImpl", TicketGrantingTicketImplDump.dump());
        } catch (Exception e) {
            stClass = null;
            tgtClass = null;
        }
    }
```

Construct ServiceTicket and TicketGrantingTicket:

```java
    private ServiceTicket getServiceTicket(final String ticketId, final TicketGrantingTicket tgt, final Service service,
            final boolean fromNewLogin, final ExpirationPolicy expirationPolicy,
            final Long creationTime, final Long lastTimeUsed) throws Exception {
        ServiceTicket serviceTicket = null;
        if (stClass == null) {
            serviceTicket = new ServiceTicketImpl(ticketId, (TicketGrantingTicketImpl) tgt, service, fromNewLogin, expirationPolicy);
        } else {
            for (Constructor<?> constructor : stClass.getConstructors()) {
                if (constructor.getParameterTypes().length == 7) {
                    serviceTicket = (ServiceTicket) constructor.newInstance(ticketId, tgt, service, fromNewLogin, expirationPolicy, creationTime, lastTimeUsed);
                    break;
                }
            }
        }
        return serviceTicket;
    }

    private TicketGrantingTicket getTicketGrantingTicket(String tgtId, Authentication authentication,
            ExpirationPolicy policy, Boolean expired, Long creationTime, Long lastTimeUsed) throws Exception {
        TicketGrantingTicket tgt = null;
        if (tgtClass == null) {
            tgt = new TicketGrantingTicketImpl(tgtId, authentication, policy);
        } else {
            for (Constructor<?> constructor : tgtClass.getConstructors()) {
                if (constructor.getParameterTypes().length == 8) {
                    tgt = (TicketGrantingTicket) constructor.newInstance(tgtId, null, null, authentication, policy, expired, creationTime, lastTimeUsed);
                    break;
                }
            }
        }
        return tgt;
    }
```

7. Use OFBiz implements in plugins/cas/webapp/oauth2/WEB-INF/deployerConfigContext.xml:

```
<?xml version="1.0" encoding="UTF-8"?>
<!--
| deployerConfigContext.xml centralizes into one file some of the declarative configuration that
| all CAS deployers will need to modify.
|
| This file declares some of the Spring-managed JavaBeans that make up a CAS deployment.
| The beans declared in this file are instantiated at context initialization time by the Spring
| ContextLoaderListener declared in web.xml.  It finds this file because this
| file is among those declared in the context parameter "contextConfigLocation".
|
| By far the most common change you will need to make in this file is to change the last bean
| declaration to replace the default authentication handler with
| one implementing your approach for authenticating usernames and passwords.
+-->

<beans xmlns="http://www.springframework.org/schema/beans"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xmlns:context="http://www.springframework.org/schema/context"
       xmlns:p="http://www.springframework.org/schema/p"
       xmlns:c="http://www.springframework.org/schema/c"
       xmlns:aop="http://www.springframework.org/schema/aop"
       xmlns:tx="http://www.springframework.org/schema/tx"
       xmlns:util="http://www.springframework.org/schema/util"
       xmlns:sec="http://www.springframework.org/schema/security"
       xsi:schemaLocation="http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans.xsd
       http://www.springframework.org/schema/tx http://www.springframework.org/schema/tx/spring-tx.xsd
       http://www.springframework.org/schema/aop http://www.springframework.org/schema/aop/spring-aop.xsd
       http://www.springframework.org/schema/context http://www.springframework.org/schema/context/spring-context.xsd
       http://www.springframework.org/schema/security http://www.springframework.org/schema/security/spring-security.xsd
       http://www.springframework.org/schema/util http://www.springframework.org/schema/util/spring-util.xsd">

    <context:component-scan base-package="org.langhua.ofbiz.cas" />

    <util:map id="authenticationHandlersResolvers">
        <!-- <entry key-ref="proxyAuthenticationHandler" value-ref="proxyPrincipalResolver" /> -->
        <entry key-ref="primaryAuthenticationHandler" value-ref="primaryPrincipalResolver" />
    </util:map>

    <util:list id="authenticationMetadataPopulators">
        <ref bean="successfulHandlerMetaDataPopulator" />
        <ref bean="rememberMeAuthenticationMetaDataPopulator" />
    </util:list>

    <bean id="attributeRepository" class="org.jasig.services.persondir.support.NamedStubPersonAttributeDao"/>

    <alias name="ofbizAuthenticationHandler" alias="primaryAuthenticationHandler" />
    
    <alias name="personDirectoryPrincipalResolver" alias="primaryPrincipalResolver" />

    <alias name="serviceThemeResolver" alias="themeResolver" />

    <alias name="ofbizServiceRegistryDao" alias="serviceRegistryDao" />
    
    <alias name="ofbizTicketRegistry" alias="ticketRegistry" />
    
    <alias name="ofbizTicketGrantingTicketUniqueIdGenerator" alias="ticketGrantingTicketUniqueIdGenerator"/>

    <alias name="ticketGrantingTicketExpirationPolicy" alias="grantingTicketExpirationPolicy" />
    <alias name="multiTimeUseOrTimeoutExpirationPolicy" alias="serviceTicketExpirationPolicy" />

    <alias name="anyAuthenticationPolicy" alias="authenticationPolicy" />
    <alias name="acceptAnyAuthenticationPolicyFactory" alias="authenticationPolicyFactory" />

    <bean id="auditTrailManager"
          class="org.jasig.inspektr.audit.support.Slf4jLoggingAuditTrailManager"
          p:entrySeparator="${cas.audit.singleline.separator:|}"
          p:useSingleLine="${cas.audit.singleline:false}"/>

    <alias name="neverThrottle" alias="authenticationThrottle" />

    <util:list id="monitorsList">
        <ref bean="memoryMonitor" />
        <ref bean="sessionMonitor" />
    </util:list>

    <alias name="defaultPrincipalFactory" alias="principalFactory" />
    <alias name="defaultAuthenticationTransactionManager" alias="authenticationTransactionManager" />
    <alias name="defaultPrincipalElectionStrategy" alias="principalElectionStrategy" />
    <alias name="tgcCipherExecutor" alias="defaultCookieCipherExecutor" />
</beans>
```

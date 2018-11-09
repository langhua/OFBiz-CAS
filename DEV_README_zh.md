The steps to build OFBiz CAS plugin
====
[EN](README.md)

### 目标
1. 在OFBiz中部署和运行。
2. 通过OFBiz实体引擎读写CAS的注册服务、服务凭证、凭证存根。

### 核心问题
CAS可以支持JPA，然而OFBiz的实体引擎不能被CAS直接使用。它们的实现理念不同。CAS通过几张数据库表来精巧、灵活地解决认证/授权问题，它把数据映射为Java类，采用了面向对象编程方式。OFBiz需要处理1000多张数据库表，从一开始便选择了事件驱动编程方式，这让它比面向对象编程方式，运行得更快，使用的内存更少。

整合的核心问题发生在对象的映射（ORM），在CAS 4.2.7中，服务凭证、凭证存根中有几个私有的数据项不能用公共的方法构建。很幸运, ASM可以帮助我们在系统运行时解决这个问题。

### 核心实现

1. 把plugins/cas/patches/cas/cas-server-core-tickets-4.2.7.patch这个补丁应用到CAS 4.2.7

2. 编译cas-server-core-tickets

3. 下载下面这些jar文件，并放入CAS的根目录：

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

4. 运行下列命令来把编译好的Java类转成ASM源码:

```
java -classpath ./*;./cas-server-core-tickets/build/libs/* org.objectweb.asm.util.ASMifier org.jasig.cas.ticket.AbstractTicket > AbstractTicketDump.java
java -classpath ./*;./cas-server-core-tickets/build/libs/* org.objectweb.asm.util.ASMifier org.jasig.cas.ticket.TicketGrantingTicketImpl > TicketGrantingTicketImplDump.java
java -classpath ./*;./cas-server-core-tickets/build/libs/* org.objectweb.asm.util.ASMifier org.jasig.cas.ticket.ServiceTicketImpl > ServiceTicketImplDump.java
java -classpath ./*;./cas-server-core-tickets/build/libs/* org.objectweb.asm.util.ASMifier org.jasig.cas.ticket.ProxyGrantingTicketImpl > ProxyGrantingTicketImplDump.java
```

5. 把这4个Java文件复制到plugins/cas/src/main/java/org/langhua/ofbiz/cas/asm

6. 在OFBizTicketRegistry.java中：

运行时加载转码后的Java类：

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

构建ServiceTicket和TicketGrantingTicket：

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

7. 把OFBiz对CAS的支持，配置到plugins/cas/webapp/oauth2/WEB-INF/deployerConfigContext.xml中：

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


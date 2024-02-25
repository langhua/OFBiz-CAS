/*******************************************************************************
 * Copyright 2018 Langhua Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package langhua.ofbiz.cas.oauth;

import org.apache.ofbiz.entity.Delegator;
import org.apache.ofbiz.entity.DelegatorFactory;
import org.apache.ofbiz.service.ServiceDispatcher;
import org.apereo.cas.audit.AuditableExecution;
import org.apereo.cas.authentication.AuthenticationSystemSupport;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.authentication.principal.PrincipalFactoryUtils;
import org.apereo.cas.authentication.principal.ServiceFactory;
import org.apereo.cas.authentication.principal.WebApplicationService;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.support.oauth.profile.OAuth20UserProfileDataCreator;
import org.pac4j.core.credentials.UsernamePasswordCredentials;
import org.pac4j.core.credentials.authenticator.Authenticator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * This this OFBizOAuthConfiguration.
 */
@Configuration("ofbizOAuthConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
@EnableTransactionManagement(proxyTargetClass = true)
public class OFBizOAuthConfiguration {

    @Autowired
    @Qualifier("registeredServiceAccessStrategyEnforcer")
    private AuditableExecution registeredServiceAccessStrategyEnforcer;

    @Autowired
    @Qualifier("webApplicationServiceFactory")
    private ServiceFactory<WebApplicationService> webApplicationServiceFactory;

    @Autowired
    @Qualifier("servicesManager")
    private ServicesManager servicesManager;

    @Autowired
    @Qualifier("defaultAuthenticationSystemSupport")
    private AuthenticationSystemSupport authenticationSystemSupport;

    @Value("${default.ofbiz.dispatcher.name:webtools}")
    private String localDispatcherName;

    @Value("${default.ofbiz.delegator.name:default}")
    private String delegatorName;

    public PrincipalFactory oauthPrincipalFactory() {
        return PrincipalFactoryUtils.newPrincipalFactory();
    }

    @Bean(name = "oAuthClientAuthenticator")
    public Authenticator<UsernamePasswordCredentials> oAuthClientAuthenticator() {
        return new OFBizOAuth20ClientAuthenticator(servicesManager,
                webApplicationServiceFactory,
                registeredServiceAccessStrategyEnforcer);
    }

    @Bean(name = "oAuthUserAuthenticator")
    public Authenticator<UsernamePasswordCredentials> oAuthUserAuthenticator() {
        Delegator delegator = DelegatorFactory.getDelegator(delegatorName);
        return new OFBizOAuth20UserAuthenticator(servicesManager,
                ServiceDispatcher.getLocalDispatcher(localDispatcherName, delegator));
    }

    @Bean(name = "oAuth2UserProfileDataCreator")
    public OAuth20UserProfileDataCreator oAuth2UserProfileDataCreator() {
        Delegator delegator = DelegatorFactory.getDelegator(delegatorName);
        return new OFBizOAuth20UserProfileDataCreator(servicesManager,
                PrincipalFactoryUtils.newPrincipalFactory(),
                delegator, ServiceDispatcher.getLocalDispatcher(localDispatcherName, delegator));
    }
}

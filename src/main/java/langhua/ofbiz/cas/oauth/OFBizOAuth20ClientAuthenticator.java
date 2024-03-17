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

import org.apache.ofbiz.base.util.Debug;
import org.apereo.cas.audit.AuditableContext;
import org.apereo.cas.audit.AuditableExecution;
import org.apereo.cas.audit.AuditableExecutionResult;
import org.apereo.cas.authentication.principal.ServiceFactory;
import org.apereo.cas.authentication.principal.WebApplicationService;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.support.oauth.services.OAuthRegisteredService;
import org.apereo.cas.support.oauth.util.OAuth20Utils;
import org.pac4j.core.context.WebContext;
import org.pac4j.core.credentials.UsernamePasswordCredentials;
import org.pac4j.core.credentials.authenticator.Authenticator;
import org.pac4j.core.exception.CredentialsException;
import org.pac4j.core.profile.CommonProfile;

/**
 * OFBiz OAuth20 Client Authenticator
 * 
 * used for endpoint /v2/accessToken with parameters grant_type=password&client_id=ID
 *                                                   &client_secret=<SECRET>
 *                                                   &username=USERNAME&password=PASSWORD
 */
public class OFBizOAuth20ClientAuthenticator implements Authenticator<UsernamePasswordCredentials> {

    public static final String module = OFBizOAuth20ClientAuthenticator.class.getName();
    private final ServicesManager servicesManager;
    private final ServiceFactory<WebApplicationService> webApplicationServiceFactory;
    private final AuditableExecution registeredServiceAccessStrategyEnforcer;
    
    public OFBizOAuth20ClientAuthenticator(ServicesManager servicesManager,
			ServiceFactory<WebApplicationService> webApplicationServiceFactory,
			AuditableExecution registeredServiceAccessStrategyEnforcer) {
		this.servicesManager = servicesManager;
		this.webApplicationServiceFactory = webApplicationServiceFactory;
		this.registeredServiceAccessStrategyEnforcer = registeredServiceAccessStrategyEnforcer;
	}

	@Override
    public void validate(final UsernamePasswordCredentials credentials, final WebContext context) throws CredentialsException {
        try {
            Debug.logInfo("OFBiz authenticating credential [%s]", module, credentials);

            final String id = credentials.getUsername();
            final String secret = credentials.getPassword();
            
            final OAuthRegisteredService registeredService = OAuth20Utils.getRegisteredOAuthServiceByClientId(this.servicesManager, id);
            if (registeredService == null) {
                throw new CredentialsException("Unable to locate registered service for " + id);
            }

            final AuditableContext audit = AuditableContext.builder()
                                                           .service(this.webApplicationServiceFactory
                                                        		        .createService(registeredService.getServiceId()))
                                                           .registeredService(registeredService)
                                                           .build();
            final AuditableExecutionResult accessResult = this.registeredServiceAccessStrategyEnforcer.execute(audit);
            accessResult.throwExceptionIfNeeded();

            if (!OAuth20Utils.checkClientSecret(registeredService, secret)) {
                throw new CredentialsException("Bad secret for client identifier: " + id);
            }

            final CommonProfile profile = new CommonProfile();
            profile.setId(id);
            credentials.setUserProfile(profile);
            Debug.logInfo("OFBiz authenticated user profile [%s]", module, profile);
        } catch (final Exception e) {
            throw new CredentialsException("Cannot login user using CAS internal authentication", e);
        }
    }
}
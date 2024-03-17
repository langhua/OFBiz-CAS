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
import org.apache.ofbiz.base.util.UtilMisc;

import org.apache.ofbiz.service.GenericServiceException;
import org.apache.ofbiz.service.LocalDispatcher;
import org.apache.ofbiz.service.ServiceUtil;
import org.apereo.cas.authentication.PreventedException;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.services.RegisteredServiceAccessStrategyUtils;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.util.OAuth20Utils;
import org.pac4j.core.context.WebContext;
import org.pac4j.core.credentials.UsernamePasswordCredentials;
import org.pac4j.core.credentials.authenticator.Authenticator;
import org.pac4j.core.exception.CredentialsException;
import org.pac4j.core.profile.CommonProfile;

import javax.security.auth.login.FailedLoginException;
import java.security.GeneralSecurityException;
import java.util.Map;

/**
 * OFBiz OAuth20 Authenticator
 * 
 */
public final class OFBizOAuth20UserAuthenticator implements Authenticator<UsernamePasswordCredentials> {
    private static final String module = OFBizOAuth20UserAuthenticator.class.getName();
    private final ServicesManager servicesManager;
    private final LocalDispatcher dispatcher;

    public OFBizOAuth20UserAuthenticator(ServicesManager servicesManager, LocalDispatcher dispatcher) {
		this.servicesManager = servicesManager;
		this.dispatcher = dispatcher;
	}

	@Override
    public void validate(final UsernamePasswordCredentials credentials, final WebContext context) throws CredentialsException {
        try {
            final String clientId = context.getRequestParameter(OAuth20Constants.CLIENT_ID);
            final RegisteredService registeredService = OAuth20Utils.getRegisteredOAuthServiceByClientId(this.servicesManager, clientId);
            RegisteredServiceAccessStrategyUtils.ensureServiceAccessIsAllowed(registeredService);

            final String username = credentials.getUsername();
            final String rawPassword = credentials.getPassword();
            try {
    			Map<String, Object> results = this.dispatcher.runSync("userLogin", UtilMisc.toMap("login.username", username, "login.password", rawPassword));
    			if (!ServiceUtil.isSuccess(results)) {
    				Debug.logInfo("%s failed to login OFBiz.", module, username);
    				// this is to disable browser pop username/password dialog to the users
    				context.setResponseHeader("WWW-Authenticate", "SandFlower realm=\"authentication required\"");
    				throw new GeneralSecurityException(ServiceUtil.getErrorMessage(results), new FailedLoginException());
    			}
    		} catch (GenericServiceException e) {
				context.setResponseHeader("WWW-Authenticate", "SandFlower realm=\"authentication required, maybe database is down\"");
    			throw new PreventedException(e);
    		}
            final CommonProfile profile = new CommonProfile();
            profile.setId(username);
            credentials.setUserProfile(profile);
        } catch (final Exception e) {
			context.setResponseHeader("WWW-Authenticate", "SandFlower realm=\"authentication required\"");
            throw new CredentialsException("Cannot login user using CAS internal authentication", e);
        }
    }
}
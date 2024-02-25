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
package langhua.ofbiz.cas.authentication;

import org.apache.ofbiz.base.util.Debug;
import org.apache.ofbiz.base.util.UtilMisc;
import org.apache.ofbiz.entity.Delegator;
import org.apache.ofbiz.entity.DelegatorFactory;
import org.apache.ofbiz.service.GenericServiceException;
import org.apache.ofbiz.service.LocalDispatcher;
import org.apache.ofbiz.service.ServiceDispatcher;
import org.apache.ofbiz.service.ServiceUtil;
import org.apereo.cas.authentication.AuthenticationHandlerExecutionResult;
import org.apereo.cas.authentication.PreventedException;
import org.apereo.cas.authentication.UsernamePasswordCredential;
import org.apereo.cas.authentication.handler.support.AbstractUsernamePasswordAuthenticationHandler;
import org.springframework.beans.factory.annotation.Autowired;

import javax.security.auth.login.FailedLoginException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Map;

/**
 * OFBiz Authentication Handler
 * 
 * Ref: https://apereo.github.io/2018/06/12/cas53-authn-handlers/
 * 
 */
public class OFBizAuthenticationHandler extends AbstractUsernamePasswordAuthenticationHandler {

    public static final String module = OFBizAuthenticationHandler.class.getName();
    private Delegator delegator;
    private LocalDispatcher dispatcher;
    
    @Autowired
    public OFBizAuthenticationHandler(final String localDispatcherName, final String delegatorName) {
    	super(OFBizAuthenticationHandler.class.getSimpleName(), null, null, null);
        this.delegator = DelegatorFactory.getDelegator(delegatorName);
        this.dispatcher = ServiceDispatcher.getLocalDispatcher(localDispatcherName, this.delegator);
    }

    @Override
	protected AuthenticationHandlerExecutionResult authenticateUsernamePasswordInternal(
			UsernamePasswordCredential credential, String originalPassword)
            throws GeneralSecurityException, PreventedException {

        final String username = credential.getUsername();
        final String rawPassword = credential.getPassword();
        try {
			Map<String, Object> results = this.dispatcher.runSync("userLogin", UtilMisc.toMap("login.username", username, "login.password", rawPassword));
			if (!ServiceUtil.isSuccess(results)) {
				Debug.logInfo("{} failed to login OFBiz.", module, username);
				throw new GeneralSecurityException(ServiceUtil.getErrorMessage(results), new FailedLoginException());
			}
		} catch (GenericServiceException e) {
			throw new PreventedException(e);
		}
        return createHandlerResult(credential, this.principalFactory.createPrincipal(username), new ArrayList<>());
    }
}

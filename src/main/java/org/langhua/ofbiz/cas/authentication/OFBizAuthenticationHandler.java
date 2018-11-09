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
package org.langhua.ofbiz.cas.authentication;

import org.apache.ofbiz.base.util.UtilMisc;
import org.apache.ofbiz.entity.Delegator;
import org.apache.ofbiz.entity.DelegatorFactory;
import org.apache.ofbiz.service.GenericServiceException;
import org.apache.ofbiz.service.LocalDispatcher;
import org.apache.ofbiz.service.ServiceDispatcher;
import org.apache.ofbiz.service.ServiceUtil;
import org.jasig.cas.authentication.HandlerResult;
import org.jasig.cas.authentication.PreventedException;
import org.jasig.cas.authentication.UsernamePasswordCredential;
import org.jasig.cas.authentication.handler.support.AbstractUsernamePasswordAuthenticationHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.security.auth.login.FailedLoginException;
import java.security.GeneralSecurityException;
import java.util.Map;

/**
 * OFBiz Authentication Handler
 */
@Component("ofbizAuthenticationHandler")
public class OFBizAuthenticationHandler extends AbstractUsernamePasswordAuthenticationHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(OFBizAuthenticationHandler.class);

    private Delegator delegator;
    
    private LocalDispatcher dispatcher;
    
    @Autowired
    public OFBizAuthenticationHandler(@Value("${default.ofbiz.dispatcher.name:main}")
                                      final String localDispatcherName,
                                      @Value("${default.ofbiz.delegator.name:default}")
                                      final String delegatorName) {
        this.delegator = DelegatorFactory.getDelegator(delegatorName);
        this.dispatcher = ServiceDispatcher.getLocalDispatcher(localDispatcherName, this.delegator);
    }

    @Override
    protected final HandlerResult authenticateUsernamePasswordInternal(final UsernamePasswordCredential credential)
            throws GeneralSecurityException, PreventedException {

        final String username = credential.getUsername();
        final String rawPassword = credential.getPassword();
        try {
			Map<String, Object> results = this.dispatcher.runSync("userLogin", UtilMisc.toMap("login.username", username, "login.password", rawPassword));
			if (!ServiceUtil.isSuccess(results)) {
				LOGGER.debug("{} failed to login OFBiz.", username);
				throw new GeneralSecurityException(ServiceUtil.getErrorMessage(results), new FailedLoginException());
			}
		} catch (GenericServiceException e) {
			throw new PreventedException(e);
		}
        return createHandlerResult(credential, this.principalFactory.createPrincipal(username), null);
    }
}

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
package org.langhua.ofbiz.cas.support.oauth;

import org.jasig.cas.authentication.principal.Service;
import org.jasig.cas.authentication.principal.ServiceFactory;
import org.jasig.cas.authentication.principal.WebApplicationService;
import org.jasig.cas.services.ReloadableServicesManager;
import org.jasig.cas.support.oauth.services.OAuthCallbackAuthorizeService;
import org.jasig.cas.web.AbstractServletContextInitializer;
import org.jasig.cas.web.support.WebUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.servlet.ServletContextEvent;
import javax.servlet.annotation.WebListener;

/**
 * Initializes the CAS root servlet context to make sure
 * OAuth endpoint can be activated by the main CAS servlet.
 * 
 */
@WebListener
@Component
public class OFBizOAuthServletContextListener extends AbstractServletContextInitializer {
	
	private static final String ENDPOINT_OAUTH2 = "/v2/*";

	private static final String ENDPOINT_OAUTH2_CALLBACK_AUTHORIZE = "/v2/callbackAuthorize";
	
	private static final String OFBIZ_OAUTH2_CONTROLLER_NAME = "ofbizOAuth20WrapperController";
	
    @Value("${server.prefix:https://localhost:8443/oauth}" + ENDPOINT_OAUTH2_CALLBACK_AUTHORIZE)
    private String callbackAuthorizeUrl;

    @Autowired
    @Qualifier("webApplicationServiceFactory")
    private ServiceFactory<WebApplicationService> webApplicationServiceFactory;

    @Override
    protected void initializeServletApplicationContext() {
        addControllerToCasServletHandlerMapping(ENDPOINT_OAUTH2, OFBIZ_OAUTH2_CONTROLLER_NAME);

        final ReloadableServicesManager servicesManager = getServicesManager();
        final Service callbackService = webApplicationServiceFactory.createService(this.callbackAuthorizeUrl);
        if (!servicesManager.matchesExistingService(callbackService))  {
            final OAuthCallbackAuthorizeService service = new OAuthCallbackAuthorizeService();
            service.setName("OAuth Callback url");
            service.setDescription("OAuth Wrapper Callback Url");
            service.setServiceId(this.callbackAuthorizeUrl);

            addRegisteredServiceToServicesManager(service);
            servicesManager.reload();
        }
    }

    @Override
    protected void initializeServletContext(final ServletContextEvent event) {
        if (WebUtils.isCasServletInitializing(event)) {
            addEndpointMappingToCasServlet(event, ENDPOINT_OAUTH2);
        }
    }
}

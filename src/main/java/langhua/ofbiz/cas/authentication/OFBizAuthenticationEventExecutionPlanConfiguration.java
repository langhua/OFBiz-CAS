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

import org.apereo.cas.authentication.AuthenticationEventExecutionPlan;
import org.apereo.cas.authentication.AuthenticationEventExecutionPlanConfigurer;
import org.apereo.cas.authentication.AuthenticationHandler;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * OFBiz authentication configuration
 * 
 * Ref: https://apereo.github.io/2018/06/12/cas53-authn-handlers/
 * 
 */
@Configuration("OFBizAuthenticationEventExecutionPlanConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class OFBizAuthenticationEventExecutionPlanConfiguration implements AuthenticationEventExecutionPlanConfigurer {
	
    @Value("${default.ofbiz.dispatcher.name:webtools}")
    private String localDispatcherName;
    
    @Value("${default.ofbiz.delegator.name:default}")
    private String delegatorName;
    
    @Bean
    public AuthenticationHandler ofbizAuthenticationHandler() {
        final OFBizAuthenticationHandler handler = new OFBizAuthenticationHandler(localDispatcherName, delegatorName);
        return handler;
    }

	@Override
	public void configureAuthenticationExecutionPlan(AuthenticationEventExecutionPlan plan) {
		plan.registerAuthenticationHandler(ofbizAuthenticationHandler());
	}
}
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
package org.langhua.ofbiz.cas.services;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.ServiceRegistry;
import org.apereo.cas.services.ServiceRegistryExecutionPlan;
import org.apereo.cas.services.ServiceRegistryExecutionPlanConfigurer;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * This this OFBizServiceRegistryConfiguration.
 *
 */
@Configuration("ofbizServiceRegistryConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
@EnableTransactionManagement(proxyTargetClass = true)
public class OFBizServiceRegistryConfiguration implements ServiceRegistryExecutionPlanConfigurer {

    @Bean
    @RefreshScope
    public ServiceRegistry ofbizServiceRegistry() {
        return new OFBizServiceRegistry();
    }

    @Override
    public void configureServiceRegistry(ServiceRegistryExecutionPlan plan) {
        plan.registerServiceRegistry(ofbizServiceRegistry());
    }
}

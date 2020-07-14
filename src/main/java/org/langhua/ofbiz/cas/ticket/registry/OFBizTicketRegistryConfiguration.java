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
package org.langhua.ofbiz.cas.ticket.registry;

import org.apache.ofbiz.base.util.UtilMisc;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.ticket.TicketCatalog;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.ticket.registry.TicketRegistryCleaner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import java.util.List;

/**
 * This this OFBizTicketRegistryConfiguration.
 *
 */
@Configuration("ofbizTicketRegistryConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
@EnableTransactionManagement(proxyTargetClass = true)
public class OFBizTicketRegistryConfiguration {

    @Bean
    public List<String> ticketPackagesToScan() {
        return UtilMisc.toList("org.langhua.ofbiz.cas", "org.pac4j", "org.apereo.cas");
    }

    @Autowired
    @Bean
    @RefreshScope
    public TicketRegistry ticketRegistry(@Qualifier("ticketCatalog") TicketCatalog ticketCatalog) {
        final OFBizTicketRegistry bean = new OFBizTicketRegistry();
        return bean;
    }

    @Bean
    public TicketRegistryCleaner ticketRegistryCleaner() {
        return OFBizTicketRegistryCleaner.getInstance();
    }
}

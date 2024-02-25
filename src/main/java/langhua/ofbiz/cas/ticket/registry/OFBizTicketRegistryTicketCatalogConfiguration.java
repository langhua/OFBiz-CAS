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
package langhua.ofbiz.cas.ticket.registry;

import org.apereo.cas.config.CasCoreTicketCatalogConfiguration;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.ticket.TicketCatalog;
import org.apereo.cas.ticket.TicketDefinition;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * This is OFBizTicketRegistryTicketCatalogConfiguration.
 *
 */
@Configuration("ofbizTicketRegistryMapsConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class OFBizTicketRegistryTicketCatalogConfiguration extends CasCoreTicketCatalogConfiguration {
    @Override
    protected void buildAndRegisterTicketGrantingTicketDefinition(TicketCatalog plan, TicketDefinition metadata) {
        metadata.getProperties().setCascade(true);
        super.buildAndRegisterTicketGrantingTicketDefinition(plan, metadata);
    }

    @Override
    protected void buildAndRegisterProxyGrantingTicketDefinition(TicketCatalog plan, TicketDefinition metadata) {
        metadata.getProperties().setCascade(true);
        super.buildAndRegisterProxyGrantingTicketDefinition(plan, metadata);
    }
}

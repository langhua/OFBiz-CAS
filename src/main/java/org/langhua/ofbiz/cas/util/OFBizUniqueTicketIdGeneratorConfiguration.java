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
package org.langhua.ofbiz.cas.util;

import org.apache.commons.lang3.tuple.Pair;
import org.apereo.cas.authentication.principal.SimpleWebApplicationServiceImpl;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.ticket.UniqueTicketIdGenerator;
import org.apereo.cas.ticket.UniqueTicketIdGeneratorConfigurer;
import org.apereo.cas.util.CollectionUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Collection;

/**
 * This is OFBizUniqueTicketIdGeneratorConfiguration.
 *
 */
@Configuration("ofbizUniqueTicketIdGeneratorConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class OFBizUniqueTicketIdGeneratorConfiguration implements UniqueTicketIdGeneratorConfigurer {
    
    @Value("${default.ofbiz.delegator.name:default}")
    String delegatorName;

    @Bean
    public UniqueTicketIdGenerator ofbizServiceTicketUniqueIdGenerator() {
        final OFBizUniqueTicketIdGenerator gen = new OFBizUniqueTicketIdGenerator(delegatorName);
        return gen;
    }

    @Override
    public Collection<Pair<String, UniqueTicketIdGenerator>> buildUniqueTicketIdGenerators() {
        return CollectionUtils.wrap(Pair.of(SimpleWebApplicationServiceImpl.class.getCanonicalName(), ofbizServiceTicketUniqueIdGenerator()));
    }
}

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

import org.apache.ofbiz.entity.Delegator;
import org.apache.ofbiz.entity.DelegatorFactory;
import org.jasig.cas.ticket.TicketGrantingTicket;
import org.jasig.cas.ticket.UniqueTicketIdGenerator;
import org.jasig.cas.ticket.proxy.ProxyGrantingTicket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Unique Ticket Id Generator compliant with OFBiz
 *
 */
@Component("ofbizTicketGrantingTicketUniqueIdGenerator")
public final class OFBizUniqueTicketIdGenerator implements UniqueTicketIdGenerator {

    private static final Logger LOGGER = LoggerFactory.getLogger(OFBizUniqueTicketIdGenerator.class);

    private Delegator delegator;

    /**
     * Instantiates a new OFBiz compliant unique ticket id generator.
     *
     * @param delegatorName the delegator name
     */
    @Autowired
    public OFBizUniqueTicketIdGenerator(@Value("${default.ofbiz.delegator.name:default}")
                                        final String delegatorName) {
        this.delegator = DelegatorFactory.getDelegator(delegatorName);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getNewTicketId(final String prefix) {
        String entityName = getEntityName(prefix);
        String ticketId = prefix + "-" + delegator.getNextSeqId(entityName);
        LOGGER.info("New ticket id[{}] generated.", ticketId);
        return ticketId;
    }

    private static String getEntityName(String prefix) {
        String entityName = null;
        if (prefix.equals(TicketGrantingTicket.PREFIX)
                || prefix.equals(ProxyGrantingTicket.PROXY_GRANTING_TICKET_PREFIX)) {
            entityName = "CasTicketGrantingTicket";
        } else {
            entityName = "CasServiceTicket";
        }
        return entityName;
    }
}

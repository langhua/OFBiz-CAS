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

import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import org.jasig.cas.logout.LogoutManager;
import org.jasig.cas.ticket.ServiceTicket;
import org.jasig.cas.ticket.Ticket;
import org.jasig.cas.ticket.TicketGrantingTicket;
import org.jasig.cas.ticket.registry.TicketRegistry;
import org.jasig.cas.ticket.registry.support.LockingStrategy;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;

import java.util.Collection;

/**
 * This is OFBizTicketRegistryCleaner.
 *
 */
@Transactional(transactionManager = "ticketTransactionManager", readOnly = true)
public class OFBizTicketRegistryCleaner extends TransactionTemplate implements Job {

	private static final long serialVersionUID = 1L;

	protected final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    @Qualifier("logoutManager")
    private LogoutManager logoutManager;

    @Autowired
    @Qualifier("ticketRegistry")
    private TicketRegistry ticketRegistry;

    @Autowired
    @Qualifier("ofbizLockingStrategy")
    private LockingStrategy ofbizLockingStrategy;
    
    @Override
    public void execute(final JobExecutionContext jobExecutionContext) throws JobExecutionException {
        try {
            SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this);

            logger.info("Beginning ticket cleanup.");
            logger.debug("Attempting to acquire ticket cleanup lock.");
            if (!this.ofbizLockingStrategy.acquire()) {
                logger.info("Could not obtain lock.  Aborting cleanup.");
                return;
            }
            logger.debug("Acquired lock.  Proceeding with cleanup.");
            
            logger.info("Beginning ticket cleanup...");
            final Collection<Ticket> ticketsToRemove = Collections2.filter(ticketRegistry.getTickets(), new Predicate<Ticket>() {
                @Override
                public boolean apply(final Ticket ticket) {
                    return ticket.isExpired();
                }
            });
            logger.info("{} expired tickets found.", ticketsToRemove.size());

            for (final Ticket ticket : ticketsToRemove) {
                if (ticket instanceof TicketGrantingTicket) {
                    logger.debug("Cleaning up expired ticket-granting ticket [{}]", ticket.getId());
                    logoutManager.performLogout((TicketGrantingTicket) ticket);
                    ticketRegistry.deleteTicket(ticket.getId());
                } else if (ticket instanceof ServiceTicket) {
                    logger.debug("Cleaning up expired service ticket [{}]", ticket.getId());
                    ticketRegistry.deleteTicket(ticket.getId());
                } else {
                    logger.warn("Unknown ticket type [{} found to clean", ticket.getClass().getSimpleName());
                }
            }
            logger.info("{} expired tickets removed.", ticketsToRemove.size());
            
        } catch (final Exception e) {
            logger.error(e.getMessage(), e);
        } finally {
            logger.debug("Releasing ticket cleanup lock.");
            this.ofbizLockingStrategy.release();
            logger.info("Finished ticket cleanup.");
        }
    }
}

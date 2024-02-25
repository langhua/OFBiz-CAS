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

import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;

import org.apache.ofbiz.base.util.Debug;
import org.apereo.cas.logout.LogoutManager;
import org.apereo.cas.ticket.ServiceTicket;
import org.apereo.cas.ticket.Ticket;
import org.apereo.cas.ticket.TicketGrantingTicket;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.ticket.registry.TicketRegistryCleaner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import java.util.Collection;

/**
 * This is OFBizTicketRegistryCleaner.
 *
 */
public class OFBizTicketRegistryCleaner implements TicketRegistryCleaner {
    public static final String module = OFBizTicketRegistryCleaner.class.getName();

    @Autowired
    @Qualifier("logoutManager")
    private LogoutManager logoutManager;

    @Autowired
    @Qualifier("ticketRegistry")
    private TicketRegistry ticketRegistry;
    
    @Override
    public void clean() {
        try {
            Debug.logInfo("Beginning ticket cleanup...", module);
            final Collection<Ticket> ticketsToRemove = Collections2.filter(ticketRegistry.getTickets(), new Predicate<Ticket>() {
                @Override
                public boolean apply(final Ticket ticket) {
                    return ticket.isExpired();
                }
            });
            Debug.logInfo(ticketsToRemove.size() + " expired tickets found.", module);

            for (final Ticket ticket : ticketsToRemove) {
                if (ticket instanceof TicketGrantingTicket) {
                    Debug.logInfo("Cleaning up expired ticket-granting ticket [" + ticket.getId() + "]", module);
                    logoutManager.performLogout((TicketGrantingTicket) ticket);
                    ticketRegistry.deleteTicket(ticket.getId());
                } else if (ticket instanceof ServiceTicket) {
                    Debug.logInfo("Cleaning up expired service ticket [" + ticket.getId() + "]", module);
                    ticketRegistry.deleteTicket(ticket.getId());
                } else {
                    Debug.logWarning("Unknown ticket type [" + ticket.getClass().getSimpleName() + "] found to clean", module);
                }
            }
            Debug.logInfo(ticketsToRemove.size() + " expired tickets removed.", module);
            
        } catch (final Exception e) {
            Debug.logError(e.getMessage(), module);
        } finally {
            Debug.logInfo("Finished ticket cleanup.", module);
        }
    }
    
    public int cleanTicket(final Ticket ticket) {
    	int ticketRemoved = 0;
    	if (ticket.isExpired()) {
    		ticketRemoved = ticketRegistry.deleteTicket(ticket.getId());
    	}
        return ticketRemoved;
    }

    private static TicketRegistryCleaner INSTANCE;

    /**
     * Gets instance.
     *
     * @return the instance
     */
	public static TicketRegistryCleaner getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new OFBizTicketRegistryCleaner();
        }
        return INSTANCE;
	}
}

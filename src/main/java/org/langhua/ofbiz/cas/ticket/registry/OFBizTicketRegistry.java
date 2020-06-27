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

import org.apache.ofbiz.base.util.UtilValidate;
import org.apache.ofbiz.base.util.collections.PagedList;
import org.apache.ofbiz.entity.Delegator;
import org.apache.ofbiz.entity.DelegatorFactory;
import org.apache.ofbiz.entity.GenericEntityException;
import org.apache.ofbiz.entity.GenericValue;
import org.apache.ofbiz.entity.condition.EntityCondition;
import org.apache.ofbiz.entity.condition.EntityOperator;
import org.apache.ofbiz.entity.transaction.TransactionUtil;
import org.apache.ofbiz.entity.util.EntityQuery;
import org.jasig.cas.authentication.Authentication;
import org.jasig.cas.authentication.principal.Service;
import org.jasig.cas.ticket.ExpirationPolicy;
import org.jasig.cas.ticket.ServiceTicket;
import org.jasig.cas.ticket.ServiceTicketImpl;
import org.jasig.cas.ticket.Ticket;
import org.jasig.cas.ticket.TicketGrantingTicket;
import org.jasig.cas.ticket.TicketGrantingTicketImpl;
import org.jasig.cas.ticket.proxy.ProxyGrantingTicket;
import org.jasig.cas.ticket.registry.AbstractDistributedTicketRegistry;
import org.joda.time.DateTime;
import org.langhua.ofbiz.cas.asm.AbstractTicketDump;
import org.langhua.ofbiz.cas.asm.ServiceTicketImplDump;
import org.langhua.ofbiz.cas.asm.TicketGrantingTicketImplDump;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SimpleScheduleBuilder;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.validation.constraints.NotNull;

/**
 * OFBiz implementation of a CAS {@link TicketRegistry}.
 *
 */
@Component("ofbizTicketRegistry")
@EnableTransactionManagement(proxyTargetClass = true)
@Transactional(transactionManager = "ticketTransactionManager", readOnly = false)
public class OFBizTicketRegistry extends AbstractDistributedTicketRegistry {

    private static final Logger LOGGER = LoggerFactory.getLogger(OFBizTicketRegistry.class);

    @Value("${ticket.registry.cleaner.viewsize:100}")
    private int viewSize;
    
    @Value("${ticket.registry.cleaner.maxresultsize:1000}")
    private int maxResultSize;
    
    private final static OFBizTicketClassLoader LOADER = new OFBizTicketClassLoader();

    private Delegator delegator;
    
    @Value("${ticket.registry.cleaner.repeatinterval:300}")
    private int refreshInterval;

    @Value("${ticket.registry.cleaner.startdelay:20}")
    private int startDelay;

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired(required = false)
    @Qualifier("scheduler")
    private Scheduler scheduler;
    
    /** ExpirationPolicy for Service Tickets. */
    @NotNull
    @Resource(name="serviceTicketExpirationPolicy")
    protected ExpirationPolicy serviceTicketExpirationPolicy;

    /** Expiration policy for ticket granting tickets. */
    @NotNull
    @Resource(name="grantingTicketExpirationPolicy")
    protected ExpirationPolicy ticketGrantingTicketExpirationPolicy;
    
    private static Class<?> stClass;
    
    private static Class<?> tgtClass;
    
    static {
        try {
            LOADER.defineClass("org.jasig.cas.ticket.AbstractTicket", AbstractTicketDump.dump());
            stClass = LOADER.defineClass("org.jasig.cas.ticket.ServiceTicketImpl", ServiceTicketImplDump.dump());
            tgtClass = LOADER.defineClass("org.jasig.cas.ticket.TicketGrantingTicketImpl", TicketGrantingTicketImplDump.dump());
        } catch (Exception e) {
            stClass = null;
            tgtClass = null;
        }
    }


    /**
     * Creates a new, empty registry with the specified initial capacity, load
     * factor, and concurrency level.
     *
     * @param delegatorName String
     */
    @Autowired
    public OFBizTicketRegistry(
                               @Value("${default.ofbiz.delegator.name:default}")
                               final String delegatorName) {
        this.delegator = DelegatorFactory.getDelegator(delegatorName);
    }
    
    public void updateTicket(final Ticket ticket) {
        String entityName = getEntityName(ticket.getId());
        if (UtilValidate.isEmpty(entityName)) {
            return ;
        }
        GenericValue ticketValue;
        try {
            ticketValue = EntityQuery.use(delegator)
                                     .from(entityName)
                                     .where("ticketId", ticket.getId())
                                     .cache(false)
                                     .queryOne();
            if (UtilValidate.isNotEmpty(ticketValue)) {
                ticketValue.set("numberOfTimesUsed", (long) ticket.getCountOfUses());
                ticketValue.set("creationTime", Long.valueOf(ticket.getCreationTime()));
                ticketValue.set("ticketgrantingticketId", ticket.getGrantingTicket().getId());
                try {
                    ticketValue.store();
                    LOGGER.debug("Updated ticket [{}].", ticket);
                } catch (GenericEntityException e) {
                    LOGGER.error("Failed to update ticket [{}]. " + e.getMessage(), ticket);
                }
            }
        } catch (GenericEntityException e1) {
            LOGGER.error("Failed to update ticket [{}]. " + e1.getMessage(), ticket);
        }
    }
    
    private static String getEntityName(String ticketId) {
        String entityName = null;
        if (ticketId.startsWith(TicketGrantingTicket.PREFIX)
                || ticketId.startsWith(ProxyGrantingTicket.PROXY_GRANTING_TICKET_PREFIX)) {
            entityName = "CasTicketGrantingTicket";
        } else {
            entityName = "CasServiceTicket";
        }
        return entityName;
    }

    @Override
    public void addTicket(final Ticket ticket) {
        String entityName = getEntityName(ticket.getId());
        String ticketId = ticket.getId();
        GenericValue ticketValue = null;
        if (ticket instanceof ServiceTicket) {
            ticketValue = delegator.makeValue(entityName, "ticketId", ticketId,
                                                          "numberOfTimesUsed", (long) ticket.getCountOfUses(),
                                                          "creationTime", Long.valueOf(ticket.getCreationTime()));
        } else {
            ticketValue = delegator.makeValue(entityName, "tgtId", ticketId,
                                                          "numberOfTimesUsed", (long) ticket.getCountOfUses(),
                                                          "creationTime", Long.valueOf(ticket.getCreationTime()));
        }
        if (UtilValidate.isNotEmpty(ticketValue)) {
            if (ticket.getGrantingTicket() != null) {
                if (ticket instanceof ServiceTicket) {
                    ticketValue.set("tgtId", ticket.getGrantingTicket().getId());
                } else {
                    ticketValue.set("ticketGrantingTicketId", ticket.getGrantingTicket().getId());
                }
            }
            try {
                if (ticket instanceof TicketGrantingTicket) {
                    ticketValue.set("authentication", ((TicketGrantingTicket) ticket).getAuthentication());
                    ticketValue.set("expirationPolicy", this.ticketGrantingTicketExpirationPolicy);
                } else if (ticket instanceof ServiceTicket) {
                    ticketValue.set("service", ((ServiceTicket) ticket).getService());
                    ticketValue.set("expirationPolicy", this.serviceTicketExpirationPolicy);
                }
                ticketValue.create();
                LOGGER.debug("Added ticket [{}] to registry.", ticket);
            } catch (GenericEntityException e) {
                LOGGER.error("Failed to add ticket [{}]. " + e.getMessage(), ticket);
            }
        }
    }

    public long deleteAll() {
        long count = 0L;
        long total1 = 0L;
        long total2 = 0L;
        int viewSize = 100;
        String entityName = "CasServiceTicket";
        try {
            do {
                count = EntityQuery.use(delegator)
                                   .from(entityName)
                                   .cache(false)
                                   .queryCount();
                TransactionUtil.begin();
                PagedList<GenericValue> tickets = EntityQuery.use(delegator)
                                                             .from(entityName)
                                                             .cache(false)
                                                             .cursorScrollInsensitive()
                                                             .queryPagedList(0, count > viewSize ? viewSize : (int) count);
                TransactionUtil.commit(true);
                try {
                    total1 += delegator.removeAll(tickets.getData());
                    LOGGER.debug("Total [{}] CasServiceTicket tickets have been removed.", total1);
                } catch (GenericEntityException e) {
                    LOGGER.error("Failed to remove CasServiceTicket tickets. " + e.getMessage());
                }
            } while (count != 0);
            
            entityName = "CasTicketGrantingTicket";
            do {
                count = EntityQuery.use(delegator)
                                   .from(entityName)
                                   .cache(false)
                                   .queryCount();
                TransactionUtil.begin();
                PagedList<GenericValue> tickets = EntityQuery.use(delegator)
                                                             .from(entityName)
                                                             .cache(false)
                                                             .cursorScrollInsensitive()
                                                             .queryPagedList(0, count > viewSize ? viewSize : (int) count);
                TransactionUtil.commit(true);
                try {
                    total2 += delegator.removeAll(tickets.getData());
                    LOGGER.debug("Total [{}] CasTicketGrantingTicket tickets have been removed.", total2);
                } catch (GenericEntityException e) {
                    LOGGER.error("Failed to remove CasTicketGrantingTicket tickets. " + e.getMessage());
                }
            } while (count != 0);
        } catch (GenericEntityException e1) {
            LOGGER.error("Failed to remove tickets. " + e1.getMessage());
        }

        return total1 + total2;
    }

    @Override
    public Ticket getTicket(final String ticketId) {
        return getRawTicket(ticketId);
    }

    /**
     * Gets the ticket from OFBiz, as is.
     * In removals, there is no need to distinguish between TGTs and PGTs since PGTs inherit from TGTs
     *
     * @param ticketId the ticket id
     * @return the raw ticket
     */
    public Ticket getRawTicket(final String ticketId) {
        Ticket ticket = null;
        try {
            String entityName = getEntityName(ticketId);
            if (entityName == null) {
                return ticket;
            }
            if ("CasServiceTicket".equals(entityName)) {
                GenericValue ticketValue = EntityQuery.use(delegator)
                                                      .from(entityName)
                                                      .cache(false)
                                                      .where("ticketId", ticketId)
                                                      .queryOne();
                if (UtilValidate.isEmpty(ticketValue)) {
                    return ticket;
                }
                String tgtId = ticketValue.getString("tgtId");
                TicketGrantingTicket tgt = null;
                if (UtilValidate.isNotEmpty(tgtId)) {
                    GenericValue tgtValue = EntityQuery.use(delegator)
                                                       .from("CasTicketGrantingTicket")
                                                       .cache(false)
                                                       .where("tgtId", tgtId)
                                                       .queryOne();
                    if (UtilValidate.isNotEmpty(tgtValue)) {
                        Authentication authentication = null;
                        if (tgtValue.get("authentication") != null) {
                            authentication = (Authentication) tgtValue.get("authentication");
                        }
                        ExpirationPolicy policy = null;
                        if (tgtValue.get("expirationPolicy") != null) {
                            policy = (ExpirationPolicy) tgtValue.get("expirationPolicy");
                        } else {
                            policy = this.ticketGrantingTicketExpirationPolicy;
                        }
                        Long creationTime = tgtValue.getLong("creationTime");
                        Long lastTimeUsed = tgtValue.getLong("lastTimeUsed");
                        Boolean expired = "Y".equals(tgtValue.getString("expired"));
                        try {
                            tgt = getTicketGrantingTicket(tgtId, authentication, policy, expired, creationTime, lastTimeUsed);
                        } catch (Exception e) {
                            LOGGER.error("Error getting TicketGrantingTicket.", e);
                        }
                    }
                }
                Service service = null;
                if (ticketValue.get("service") != null) {
                    service = (Service) ticketValue.get("service");
                }
                ExpirationPolicy expirationPolicy = null;
                if (ticketValue.get("expirationPolicy") != null) {
                    expirationPolicy = (ExpirationPolicy) ticketValue.get("expirationPolicy");
                } else {
                    expirationPolicy = this.serviceTicketExpirationPolicy;
                }
                Long creationTime = ticketValue.getLong("creationTime");
                Long lastTimeUsed = ticketValue.getLong("lastTimeUsed");
                try {
                    ticket = getServiceTicket(ticketId, tgt, service, false, expirationPolicy, creationTime, lastTimeUsed);
                } catch (Exception e) {
                    LOGGER.error("Error getting ServiceTicket.", e);
                }
            } else if ("CasTicketGrantingTicket".equals(entityName)) {
                GenericValue tgtValue = EntityQuery.use(delegator)
                                                   .from("CasTicketGrantingTicket")
                                                   .cache(false)
                                                   .where("tgtId", ticketId)
                                                   .queryOne();
                if (UtilValidate.isNotEmpty(tgtValue)) {
                    Authentication authentication = null;
                    if (tgtValue.get("authentication") != null) {
                        authentication = (Authentication) tgtValue.get("authentication");
                    }
                    ExpirationPolicy policy = null;
                    if (tgtValue.get("expirationPolicy") != null) {
                        policy = (ExpirationPolicy) tgtValue.get("expirationPolicy");
                    } else {
                        policy = this.ticketGrantingTicketExpirationPolicy;
                    }
                    Long creationTime = tgtValue.getLong("creationTime");
                    Long lastTimeUsed = tgtValue.getLong("lastTimeUsed");
                    Boolean expired = "Y".equals(tgtValue.getString("expired"));
                    try {
                        ticket = getTicketGrantingTicket(ticketId, authentication, policy, expired, creationTime, lastTimeUsed);
                    } catch (Exception e) {
                        LOGGER.error("Error getting TicketGrantingTicket.", e);
                    }
                }
            }
        } catch (final Exception e) {
            LOGGER.error("Error getting ticket [{}] from registry.", ticketId, e);
        }
        return ticket;
    }

    @Override
    public Collection<Ticket> getTickets() {
        List<Ticket> results = new ArrayList<Ticket>();
        String serviceTicketEntityName = "CasServiceTicket";
        String tgtEntityName = "CasTicketGrantingTicket";
        int viewIndex = 0;
        Set<String> tgtIdSet = new HashSet<String>();
        try {
            long count = EntityQuery.use(delegator)
                                    .from(serviceTicketEntityName)
                                    .cache(false)
                                    .queryCount();
            if (count > 0) {
                do {
                    TransactionUtil.begin();
                    PagedList<GenericValue> tickets = EntityQuery.use(delegator)
                                                                 .from(serviceTicketEntityName)
                                                                 .cache(false)
                                                                 .cursorScrollInsensitive()
                                                                 .queryPagedList(viewIndex, count > viewSize * (viewIndex + 1) ? viewSize : (int) (count - viewSize * viewIndex));
                    TransactionUtil.commit(true);
                    for (GenericValue ticketValue : tickets) {
                        String ticketId = ticketValue.getString("ticketId");
                        String tgtId = ticketValue.getString("tgtId");
                        TicketGrantingTicket tgt = null;
                        if (UtilValidate.isNotEmpty(tgtId)) {
                            GenericValue tgtValue = EntityQuery.use(delegator)
                                                               .from(tgtEntityName)
                                                               .cache(false)
                                                               .where("tgtId", tgtId)
                                                               .queryOne();
                            if (UtilValidate.isNotEmpty(tgtValue)) {
                                Authentication authentication = null;
                                if (tgtValue.get("authentication") != null) {
                                    authentication = (Authentication) tgtValue.get("authentication");
                                }
                                ExpirationPolicy policy = null;
                                if (tgtValue.get("expirationPolicy") != null) {
                                    policy = (ExpirationPolicy) tgtValue.get("expirationPolicy");
                                } else {
                                    policy = this.ticketGrantingTicketExpirationPolicy;
                                }
                                Long creationTime = tgtValue.getLong("creationTime");
                                Long lastTimeUsed = tgtValue.getLong("lastTimeUsed");
                                Boolean expired = "Y".equals(tgtValue.getString("expired"));
                                try {
                                    tgt = getTicketGrantingTicket(tgtId, authentication, policy, expired, creationTime, lastTimeUsed);
                                } catch (Exception e) {
                                    LOGGER.error("Error getting TicketGrantingTicket.", e);
                                }
                            }
                        }
                        Service service = null;
                        if (ticketValue.get("service") != null) {
                            service = (Service) ticketValue.get("service");
                        }
                        ExpirationPolicy expirationPolicy = null;
                        if (ticketValue.get("expirationPolicy") != null) {
                            expirationPolicy = (ExpirationPolicy) ticketValue.get("expirationPolicy");
                        } else {
                            expirationPolicy = this.serviceTicketExpirationPolicy;
                        }
                        Long creationTime = ticketValue.getLong("creationTime");
                        Long lastTimeUsed = ticketValue.getLong("lastTimeUsed");
                        Ticket ticket;
                        try {
                            ticket = getServiceTicket(ticketId, tgt, service, false, expirationPolicy, creationTime, lastTimeUsed);
                            results.add(ticket);
                        } catch (Exception e) {
                            LOGGER.error("Error getting ServiceTicket.", e);
                        }
                        if (tgt != null) {
                            results.add(tgt);
                            tgtIdSet.add(tgtId);
                        }
                    }
                    viewIndex += 1;
                } while (count > viewSize * viewIndex || results.size() >= maxResultSize);
            }
            
            count = EntityQuery.use(delegator)
                               .from(tgtEntityName)
                               .cache(false)
                               .queryCount();
            if (count > 0) {
                viewIndex = 0;
                EntityCondition entityCondition = EntityCondition.makeCondition("tgtId", EntityOperator.NOT_IN, tgtIdSet);
                do {
                    PagedList<GenericValue> tickets = null;
                    if (tgtIdSet.isEmpty()) {
                        TransactionUtil.begin();
                        tickets = EntityQuery.use(delegator)
                                             .from(tgtEntityName)
                                             .cache(false)
                                             .cursorScrollSensitive()
                                             .fetchSize(viewSize)
                                             .queryPagedList(viewIndex, count > viewSize * (viewIndex + 1) ? viewSize : (int) (count - viewSize * viewIndex));
                        TransactionUtil.commit(true);
                    } else {
                        TransactionUtil.begin();
                        tickets = EntityQuery.use(delegator)
                                             .from(tgtEntityName)
                                             .cache(false)
                                             .where(entityCondition)
                                             .cursorScrollSensitive()
                                             .queryPagedList(viewIndex, count > viewSize * (viewIndex + 1) ? viewSize : (int) (count - viewSize * viewIndex));
                        TransactionUtil.commit(true);
                    }
                    for (GenericValue tgtValue : tickets) {
                        String tgtId = tgtValue.getString("tgtId");
                        Authentication authentication = null;
                        if (tgtValue.get("authentication") != null) {
                            authentication = (Authentication) tgtValue.get("authentication");
                        }
                        ExpirationPolicy policy = null;
                        if (tgtValue.get("expirationPolicy") != null) {
                            policy = (ExpirationPolicy) tgtValue.get("expirationPolicy");
                        } else {
                            policy = this.ticketGrantingTicketExpirationPolicy;
                        }
                        try {
                            Long creationTime = tgtValue.getLong("creationTime");
                            Long lastTimeUsed = tgtValue.getLong("lastTimeUsed");
                            Boolean expired = "Y".equals(tgtValue.getString("expired"));
                            TicketGrantingTicket tgt = getTicketGrantingTicket(tgtId, authentication, policy, expired, creationTime, lastTimeUsed);
                               results.add(tgt);
                        } catch (Exception e) {
                            LOGGER.error("Error getting TicketGrantingTicket.", e);
                        }
                    }
                    viewIndex += 1;
                } while (count > viewSize * viewIndex || results.size() >= maxResultSize);
            }
        } catch (GenericEntityException e) {
            LOGGER.error("Error getting tickets.", e);
        }
        return results;
    }

    private ServiceTicket getServiceTicket(final String ticketId, final TicketGrantingTicket tgt, final Service service,
            final boolean fromNewLogin, final ExpirationPolicy expirationPolicy,
            final Long creationTime, final Long lastTimeUsed) throws Exception {
        ServiceTicket serviceTicket = null;
        if (stClass == null) {
            serviceTicket = new ServiceTicketImpl(ticketId, (TicketGrantingTicketImpl) tgt, service, fromNewLogin, expirationPolicy);
        } else {
            for (Constructor<?> constructor : stClass.getConstructors()) {
                if (constructor.getParameterTypes().length == 7) {
                    serviceTicket = (ServiceTicket) constructor.newInstance(ticketId, tgt, service, fromNewLogin, expirationPolicy, creationTime, lastTimeUsed);
                    break;
                }
            }
        }
        return serviceTicket;
    }

    private TicketGrantingTicket getTicketGrantingTicket(String tgtId, Authentication authentication,
            ExpirationPolicy policy, Boolean expired, Long creationTime, Long lastTimeUsed) throws Exception {
        TicketGrantingTicket tgt = null;
        if (tgtClass == null) {
            tgt = new TicketGrantingTicketImpl(tgtId, authentication, policy);
        } else {
            for (Constructor<?> constructor : tgtClass.getConstructors()) {
                if (constructor.getParameterTypes().length == 8) {
                    tgt = (TicketGrantingTicket) constructor.newInstance(tgtId, null, null, authentication, policy, expired, creationTime, lastTimeUsed);
                    break;
                }
            }
        }
        return tgt;
    }

    @Override
    public int sessionCount() {
        int count = -1;
        try {
            count = (int) EntityQuery.use(delegator)
                                     .from("CasTicketGrantingTicket")
                                     .cache(false)
                                     .queryCount();
        } catch (GenericEntityException e) {
            LOGGER.error("Error counting CasTicketGrantingTicket tickets.", e);
        }
        return count;
    }

    @Override
    public int serviceTicketCount() {
        int count = -1;
        try {
            count = (int) EntityQuery.use(delegator)
                                     .from("CasServiceTicket")
                                     .cache(false)
                                     .queryCount();
        } catch (GenericEntityException e) {
            LOGGER.error("Error counting CasServiceTicket tickets.", e);
        }
        return count;
    }

    @Override
    public boolean deleteSingleTicket(final String ticketId) {
        int totalCount = 0;
        if (ticketId.startsWith(TicketGrantingTicket.PREFIX)
                || ticketId.startsWith(ProxyGrantingTicket.PROXY_GRANTING_TICKET_PREFIX)) {
            totalCount = deleteTicketGrantingTickets(ticketId);
        } else {
            try {
                totalCount = delegator.removeByAnd("CasServiceTicket", "ticketId", ticketId);
            } catch (GenericEntityException e) {
                LOGGER.error("Error deleting CasServiceTicket ticket[{}].", ticketId, e);
            }
        }
        return totalCount != 0;
    }

    /**
     * Delete ticket granting tickets int.
     *
     * @param ticketId the ticket id
     * @return the int
     */
    private int deleteTicketGrantingTickets(final String ticketId) {
        int totalCount = 0;
        try {
            totalCount +=  delegator.removeByAnd("CasServiceTicket", "tgtId", ticketId);
            totalCount +=  delegator.removeByAnd("CasTicketGrantingTicket", "tgtId", ticketId);
        } catch (GenericEntityException e) {
            LOGGER.error("Error deleting CasTicketGrantingTicket ticket[{}].", ticketId, e);
        }
        return totalCount;
    }

    @Override
    protected boolean needsCallback() {
        return false;
    }

    /**
     * Schedule reloader job.
     */
    @PostConstruct
    public void scheduleCleanerJob() {
        try {
            if (shouldScheduleCleanerJob()) {
                logger.info("Preparing to schedule cleaner job");

                final JobDetail job = JobBuilder.newJob(OFBizTicketRegistryCleaner.class)
                    .withIdentity(this.getClass().getSimpleName().concat(UUID.randomUUID().toString()))
                    .build();

                final Trigger trigger = TriggerBuilder.newTrigger()
                    .withIdentity(this.getClass().getSimpleName().concat(UUID.randomUUID().toString()))
                    .startAt(DateTime.now().plusSeconds(this.startDelay).toDate())
                    .withSchedule(SimpleScheduleBuilder.simpleSchedule()
                        .withIntervalInSeconds(this.refreshInterval)
                        .repeatForever()).build();

                logger.debug("Scheduling {} job", this.getClass().getName());
                scheduler.getContext().put(getClass().getSimpleName(), this);
                scheduler.scheduleJob(job, trigger);
                logger.info("{} will clean tickets every {} seconds",
                    this.getClass().getSimpleName(),
                    this.refreshInterval);
            }
        } catch (final Exception e){
            logger.warn(e.getMessage(), e);
        }

    }

    private boolean shouldScheduleCleanerJob() {
        if (this.startDelay > 0 && this.applicationContext.getParent() == null && scheduler != null) {
            logger.debug("Found CAS servlet application context for ticket management");
            return true;
        }

        return false;
    }
    
    private static class OFBizTicketClassLoader extends ClassLoader {

        public Class<?> defineClass(final String name, final byte[] b) {
            return defineClass(name, b, 0, b.length);
        }
    }
}

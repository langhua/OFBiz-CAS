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

import org.apache.ofbiz.base.util.Debug;
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
import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.support.oauth.OAuthAccessTokenProperties;
import org.apereo.cas.configuration.model.support.oauth.OAuthProperties;
import org.apereo.cas.configuration.model.support.oauth.OAuthRefreshTokenProperties;
import org.apereo.cas.configuration.support.Beans;
import org.apereo.cas.ticket.AbstractTicket;
import org.apereo.cas.ticket.ExpirationPolicy;
import org.apereo.cas.ticket.ServiceTicket;
import org.apereo.cas.ticket.ServiceTicketImpl;
import org.apereo.cas.ticket.Ticket;
import org.apereo.cas.ticket.TicketGrantingTicket;
import org.apereo.cas.ticket.TicketGrantingTicketImpl;
import org.apereo.cas.ticket.accesstoken.AccessToken;
import org.apereo.cas.ticket.accesstoken.AccessTokenImpl;
import org.apereo.cas.ticket.accesstoken.OAuthAccessTokenExpirationPolicy;
import org.apereo.cas.ticket.code.OAuthCode;
import org.apereo.cas.ticket.code.OAuthCodeExpirationPolicy;
import org.apereo.cas.ticket.code.OAuthCodeImpl;
import org.apereo.cas.ticket.proxy.ProxyGrantingTicket;
import org.apereo.cas.ticket.refreshtoken.OAuthRefreshTokenExpirationPolicy;
import org.apereo.cas.ticket.refreshtoken.RefreshToken;
import org.apereo.cas.ticket.refreshtoken.RefreshTokenImpl;
import org.apereo.cas.ticket.registry.AbstractTicketRegistry;
import org.apereo.cas.util.serialization.SerializationUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.validation.constraints.NotNull;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

/**
 * OFBiz implementation of a CAS {@link TicketRegistry}. This implementation of
 * ticket registry is suitable for HA environments.
 *
 */
@EnableTransactionManagement(proxyTargetClass = true)
@Transactional(transactionManager = "ticketTransactionManager")
public class OFBizTicketRegistry extends AbstractTicketRegistry {
    public static final String module = OFBizTicketRegistry.class.getName();

    @Value("${ticket.registry.cleaner.viewsize:100}")
    private int viewSize;
    
    @Value("${ticket.registry.cleaner.maxresultsize:1000}")
    private int maxResultSize;
    
    @Value("${default.ofbiz.delegator.name:default}")
    private String delegatorName;
    
    private Delegator delegator = DelegatorFactory.getDelegator(delegatorName);
    
    /** ExpirationPolicy for Service Tickets. */
    @NotNull
    @Resource(name="serviceTicketExpirationPolicy")
    protected ExpirationPolicy serviceTicketExpirationPolicy;

    /** Expiration policy for ticket granting tickets. */
    @NotNull
    @Resource(name="grantingTicketExpirationPolicy")
    protected ExpirationPolicy ticketGrantingTicketExpirationPolicy;
    
    @Autowired
    private CasConfigurationProperties casProperties;
    
    protected OFBizTicketRegistry() {
        super();
    }

    @Override
    public Ticket updateTicket(Ticket ticket) {
        String entityName = getEntityName(ticket.getId());
        if (UtilValidate.isEmpty(entityName)) {
            return ticket;
        }
        GenericValue ticketValue;
        try {
            if (ticket instanceof ServiceTicket) {
                ticketValue = EntityQuery.use(delegator)
                                         .from(entityName)
                                         .where("ticketId", ticket.getId())
                                         .cache(false)
                                         .queryOne();
            } else {
                ticketValue = EntityQuery.use(delegator)
                                         .from(entityName)
                                         .where("tgtId", ticket.getId())
                                         .cache(false)
                                         .queryOne();
            }
            if (UtilValidate.isNotEmpty(ticketValue)) {
                ticketValue.set("numberOfTimesUsed", (long) ticket.getCountOfUses());
                if (ticket.getCreationTime() != null) {
                    ticketValue.set("creationTime", ticket.getCreationTime().toInstant().getEpochSecond());
                }
                if (ticket.getTicketGrantingTicket() != null) {
                    if (ticket instanceof ServiceTicket) {
                        ticketValue.set("tgtId", ticket.getTicketGrantingTicket().getId());
                    } else if (ticket instanceof TicketGrantingTicket) {
                        ticketValue.set("ticketGrantingticketId", ticket.getTicketGrantingTicket().getId());
                    }
                }
                try {
                    ticketValue.store();
                    if (Debug.verboseOn()) {
                        Debug.logVerbose("Updated ticket [{}].", module, ticket);
                    }
                } catch (GenericEntityException e) {
                    Debug.logError("Failed to update ticket [{}]. " + e.getMessage(), module, ticket);
                }
            }
        } catch (GenericEntityException e1) {
            Debug.logError("Failed to update ticket [{}]. " + e1.getMessage(), module, ticket);
        }
        return ticket;
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
    public void addTicket(Ticket ticket) {
        String entityName = getEntityName(ticket.getId());
        String ticketId = ticket.getId();
        GenericValue ticketValue = null;
        if (ticket instanceof ServiceTicket) {
            ticketValue = delegator.makeValue(entityName, "ticketId", ticketId,
                                                          "numberOfTimesUsed", (long) ticket.getCountOfUses(),
                                                          "creationTime", ticket.getCreationTime().toInstant().getEpochSecond());
        } else {
            ticketValue = delegator.makeValue(entityName, "tgtId", ticketId,
                                                          "numberOfTimesUsed", (long) ticket.getCountOfUses(),
                                                          "creationTime", ticket.getCreationTime().toInstant().getEpochSecond());
        }
        if (UtilValidate.isNotEmpty(ticketValue)) {
            if (ticket.getTicketGrantingTicket() != null) {
                if (ticket instanceof ServiceTicket) {
                    ticketValue.set("tgtId", ticket.getTicketGrantingTicket().getId());
                } else {
                    ticketValue.set("ticketGrantingTicketId", ticket.getTicketGrantingTicket().getId());
                }
            }
            try {
                if (ticket instanceof TicketGrantingTicket) {
                    ticketValue.setBytes("authentication", SerializationUtils.serialize(((TicketGrantingTicket) ticket).getAuthentication()));
                    ticketValue.setBytes("expirationPolicy", SerializationUtils.serialize(this.ticketGrantingTicketExpirationPolicy));
                } else if (ticket instanceof RefreshToken) {
                    ticketValue.setBytes("authentication", SerializationUtils.serialize(((RefreshToken) ticket).getAuthentication()));
                    ticketValue.setBytes("scopes", SerializationUtils.serialize((HashSet<String>) ((RefreshToken) ticket).getScopes()));
                    ticketValue.setBytes("service", SerializationUtils.serialize(((RefreshToken) ticket).getService()));
                    ticketValue.setBytes("expirationPolicy", SerializationUtils.serialize(refreshTokenExpirationPolicy()));
                } else if (ticket instanceof AccessToken) {
                    ticketValue.setBytes("authentication", SerializationUtils.serialize(((AccessToken) ticket).getAuthentication()));
                    ticketValue.setBytes("scopes", SerializationUtils.serialize((HashSet<String>) ((AccessToken) ticket).getScopes()));
                    ticketValue.setBytes("service", SerializationUtils.serialize(((AccessToken) ticket).getService()));
                    ticketValue.setBytes("expirationPolicy", SerializationUtils.serialize(accessTokenExpirationPolicy()));
                } else if (ticket instanceof OAuthCode) {
                    ticketValue.setBytes("authentication", SerializationUtils.serialize(((OAuthCode) ticket).getAuthentication()));
                    ticketValue.setBytes("scopes", SerializationUtils.serialize((HashSet<String>) ((OAuthCode) ticket).getScopes()));
                    ticketValue.setBytes("service", SerializationUtils.serialize(((OAuthCode) ticket).getService()));
                    ticketValue.setBytes("expirationPolicy", SerializationUtils.serialize(oAuthCodeExpirationPolicy()));
                } else if (ticket instanceof ServiceTicket) {
                    ticketValue.setBytes("service", SerializationUtils.serialize(((ServiceTicket) ticket).getService()));
                    ticketValue.setBytes("expirationPolicy", SerializationUtils.serialize(this.serviceTicketExpirationPolicy));
                }
                ticketValue.create();
                Debug.logInfo("Added ticket [{}] to registry.", module, ticket);
            } catch (GenericEntityException e) {
                Debug.logError("Failed to add ticket [{}]. " + e.getMessage(), module, ticket);
            }
        }
    }

    @Override
    public long deleteAll() {
        long count = 0L;
        long total1 = 0L;
        long total2 = 0L;
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
                    Debug.logInfo("Total [{}] CasServiceTicket tickets have been removed.", module, total1);
                } catch (GenericEntityException e) {
                    Debug.logError("Failed to remove CasServiceTicket tickets. " + e.getMessage(), module);
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
                    Debug.logInfo("Total [{}] CasTicketGrantingTicket tickets have been removed.", module, total2);
                } catch (GenericEntityException e) {
                    Debug.logError("Failed to remove CasTicketGrantingTicket tickets. " + e.getMessage(), module);
                }
            } while (count != 0);
        } catch (GenericEntityException e1) {
            Debug.logError("Failed to remove tickets. " + e1.getMessage(), module);
        }

        return total1 + total2;
    }

    @Override
    public Ticket getTicket(String ticketId) {
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
                TicketGrantingTicketImpl tgt = null;
                if (UtilValidate.isNotEmpty(tgtId)) {
                    GenericValue tgtValue = EntityQuery.use(delegator)
                                                       .from("CasTicketGrantingTicket")
                                                       .cache(false)
                                                       .where("tgtId", tgtId)
                                                       .queryOne();
                    if (UtilValidate.isNotEmpty(tgtValue)) {
                        tgt = getTicketGrantingTicket(tgtValue);
                    }
                }
                ticket = getAbstractTicket(ticketValue, tgt);
            } else if ("CasTicketGrantingTicket".equals(entityName)) {
                GenericValue tgtValue = EntityQuery.use(delegator)
                                                   .from("CasTicketGrantingTicket")
                                                   .cache(false)
                                                   .where("tgtId", ticketId)
                                                   .queryOne();
                if (UtilValidate.isNotEmpty(tgtValue)) {
                    ticket = getTicketGrantingTicket(tgtValue);
                }
            }
        } catch (final Exception e) {
            Debug.logError("Error getting ticket [{}] from registry. " + e.getMessage(), module, ticketId);
        }
        return ticket;
    }

    @Override
    public Collection<Ticket> getTickets() {
        return getTickets(viewSize, maxResultSize);
    }
    
    private Collection<Ticket> getTickets(int pageSize, int maxResult) {
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
                List<GenericValue> ticketsToRemove = new ArrayList<GenericValue>();
                do {
                    TransactionUtil.begin();
                    PagedList<GenericValue> tickets = EntityQuery.use(delegator)
                                                                 .from(serviceTicketEntityName)
                                                                 .cache(false)
                                                                 .cursorScrollInsensitive()
                                                                 .queryPagedList(viewIndex, count > pageSize * (viewIndex + 1) ? pageSize : (int) (count - pageSize * viewIndex));
                    TransactionUtil.commit(true);
                    for (GenericValue ticketValue : tickets) {
                        String tgtId = ticketValue.getString("tgtId");
                        TicketGrantingTicketImpl tgt = null;
                        if (UtilValidate.isNotEmpty(tgtId)) {
                            GenericValue tgtValue = EntityQuery.use(delegator)
                                                               .from(tgtEntityName)
                                                               .cache(false)
                                                               .where("tgtId", tgtId)
                                                               .queryOne();
                            if (UtilValidate.isNotEmpty(tgtValue)) {
                                tgt = getTicketGrantingTicket(tgtValue);
                            }
                        }
                        Ticket ticket;
                        try {
                            ticket = getAbstractTicket(ticketValue, tgt);
                            results.add(ticket);
                        } catch (Exception e) {
                            Debug.logWarning("Warning getting " + tgtEntityName + "[" + tgtId + "], it will be removed. " + e.getMessage(), module);
                            ticketsToRemove.add(ticketValue);
                        }
                        if (tgt != null) {
                            results.add(tgt);
                            tgtIdSet.add(tgtId);
                        }
                    }
                    viewIndex += 1;
                } while (count > pageSize * viewIndex || results.size() >= maxResult);
                if (!ticketsToRemove.isEmpty()) {
                    delegator.removeAll(ticketsToRemove);
                }
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
                                             .fetchSize(pageSize)
                                             .queryPagedList(viewIndex, count > pageSize * (viewIndex + 1) ? pageSize : (int) (count - pageSize * viewIndex));
                        TransactionUtil.commit(true);
                    } else {
                        TransactionUtil.begin();
                        tickets = EntityQuery.use(delegator)
                                             .from(tgtEntityName)
                                             .cache(false)
                                             .where(entityCondition)
                                             .cursorScrollSensitive()
                                             .queryPagedList(viewIndex, count > pageSize * (viewIndex + 1) ? pageSize : (int) (count - pageSize * viewIndex));
                        TransactionUtil.commit(true);
                    }
                    for (GenericValue tgtValue : tickets) {
                        TicketGrantingTicketImpl tgt = getTicketGrantingTicket(tgtValue);
                           results.add(tgt);
                    }
                    viewIndex += 1;
                } while (count > pageSize * viewIndex || results.size() >= maxResult);
            }
        } catch (GenericEntityException e) {
            Debug.logError("Error getting tickets. " + e.getMessage(), module);
        }
        return results;
    }

    @SuppressWarnings("unchecked")
    private AbstractTicket getAbstractTicket(GenericValue ticketValue, TicketGrantingTicketImpl tgt) {
        AbstractTicket abstractTicket = null;
        Service service = null;
        if (ticketValue.get("service") != null) {
            service = SerializationUtils.deserialize(ticketValue.getBytes("service"), Service.class);
        }
        ExpirationPolicy expirationPolicy = null;
        if (ticketValue.get("expirationPolicy") != null) {
            expirationPolicy = SerializationUtils.deserialize(ticketValue.getBytes("expirationPolicy"), ExpirationPolicy.class);
        } else {
            expirationPolicy = this.serviceTicketExpirationPolicy;
        }
        String ticketId = ticketValue.getString("ticketId");
        if (ticketId.startsWith(AccessToken.PREFIX)) {
            Authentication authentication = null;
            if (ticketValue.get("authentication") != null) {
                authentication = SerializationUtils.deserialize(ticketValue.getBytes("authentication"), Authentication.class);
            }
            HashSet<String> scopes = new HashSet<String>();
            if (ticketValue.get("scopes") != null) {
                scopes = SerializationUtils.deserialize(ticketValue.getBytes("scopes"), HashSet.class);
            }
            abstractTicket = new AccessTokenImpl(ticketId, service, authentication, expirationPolicy, tgt, scopes);
        } else if (ticketId.startsWith(ServiceTicket.PREFIX)) {
            abstractTicket = new ServiceTicketImpl(ticketId, tgt, service, false, expirationPolicy);
        } else if (ticketId.startsWith(OAuthCode.PREFIX)) {
            Authentication authentication = null;
            if (ticketValue.get("authentication") != null) {
                authentication = SerializationUtils.deserialize(ticketValue.getBytes("authentication"), Authentication.class);
            }
            HashSet<String> scopes = new HashSet<String>();
            if (ticketValue.get("scopes") != null) {
                scopes = SerializationUtils.deserialize(ticketValue.getBytes("scopes"), HashSet.class);
            }
            abstractTicket = new OAuthCodeImpl(ticketId, service, authentication, expirationPolicy, tgt, scopes);
        } else if (ticketId.startsWith(RefreshToken.PREFIX)) {
            Authentication authentication = null;
            if (ticketValue.get("authentication") != null) {
                authentication = SerializationUtils.deserialize(ticketValue.getBytes("authentication"), Authentication.class);
            }
            HashSet<String> scopes = new HashSet<String>();
            if (ticketValue.get("scopes") != null) {
                scopes = SerializationUtils.deserialize(ticketValue.getBytes("scopes"), HashSet.class);
            }
            abstractTicket = new RefreshTokenImpl(ticketId, service, authentication, expirationPolicy, tgt, scopes);
        }
        if (abstractTicket != null) {
            setAbstractTicketFields(abstractTicket, ticketValue);
        }
        return abstractTicket;
    }

    private void setAbstractTicketFields(AbstractTicket abstractTicket, GenericValue ticketValue) {
        if (UtilValidate.isNotEmpty(ticketValue.getLong("creationTime"))) {
            ZonedDateTime creationTime = ZonedDateTime.ofInstant(Instant.ofEpochSecond(ticketValue.getLong("creationTime")), ZoneOffset.UTC);
            abstractTicket.setCreationTime(creationTime);
        }
        if (UtilValidate.isNotEmpty(ticketValue.getLong("lastTimeUsed"))) {
            ZonedDateTime lastTimeUsed = ZonedDateTime.ofInstant(Instant.ofEpochSecond(ticketValue.getLong("lastTimeUsed")), ZoneOffset.UTC);
            abstractTicket.setLastTimeUsed(lastTimeUsed);
        }
        if (UtilValidate.isNotEmpty(ticketValue.getLong("previousTimeUsed"))) {
            ZonedDateTime previousTimeUsed = ZonedDateTime.ofInstant(Instant.ofEpochSecond(ticketValue.getLong("previousTimeUsed")), ZoneOffset.UTC);
            abstractTicket.setPreviousTimeUsed(previousTimeUsed);
        }
        if (UtilValidate.isNotEmpty(ticketValue.getLong("numberOfTimesUsed"))) {
            abstractTicket.setCountOfUses(ticketValue.getLong("numberOfTimesUsed").intValue());
        }
        if (UtilValidate.isNotEmpty(ticketValue.getBoolean("expired"))) {
            abstractTicket.setExpired(ticketValue.getBoolean("expired"));
        }
    }

    private TicketGrantingTicketImpl getTicketGrantingTicket(GenericValue tgtValue) {
        Authentication authentication = null;
        if (tgtValue.get("authentication") != null) {
            authentication = SerializationUtils.deserialize(tgtValue.getBytes("authentication"), Authentication.class);
        }
        ExpirationPolicy policy = null;
        if (tgtValue.get("expirationPolicy") != null) {
            policy = SerializationUtils.deserialize(tgtValue.getBytes("expirationPolicy"), ExpirationPolicy.class);
        } else {
            policy = this.ticketGrantingTicketExpirationPolicy;
        }
        TicketGrantingTicketImpl tgt = new TicketGrantingTicketImpl(tgtValue.getString("tgtId"), authentication, policy);
        setAbstractTicketFields(tgt, tgtValue);
        return tgt;
    }

    /**
     * Gets a stream which loads tickets from the database in batches instead of all at once to prevent OOM situations.
     * <p>
     * This method purposefully doesn't lock any rows, because the stream traversing can take an indeterminate
     * amount of time, and logging in to an application with an existing TGT will update the TGT row in the database.
     *
     * @return {@inheritDoc}
     */
    @Override
    public Stream<Ticket> getTicketsStream() {
        return getTickets(viewSize, 2 * viewSize).stream();
    }

    @Override
    public long sessionCount() {
        long count = -1;
        try {
            count = EntityQuery.use(delegator)
                               .from("CasTicketGrantingTicket")
                               .cache(false)
                               .queryCount();
        } catch (GenericEntityException e) {
            Debug.logError("Error counting CasTicketGrantingTicket tickets. " + e.getMessage(), module);
        }
        return count;
    }

    @Override
    public long serviceTicketCount() {
        long count = -1;
        try {
            count = EntityQuery.use(delegator)
                               .from("CasServiceTicket")
                               .cache(false)
                               .queryCount();
        } catch (GenericEntityException e) {
            Debug.logError("Error counting CasServiceTicket tickets. " + e.getMessage(), module);
        }
        return count;
    }

    @Override
    public boolean deleteSingleTicket(String ticketId) {
        int totalCount = 0;
        if (ticketId.startsWith(TicketGrantingTicket.PREFIX)
                || ticketId.startsWith(ProxyGrantingTicket.PROXY_GRANTING_TICKET_PREFIX)) {
            totalCount = deleteTicketGrantingTickets(ticketId);
        } else {
            try {
                totalCount = delegator.removeByAnd("CasServiceTicket", "ticketId", ticketId);
            } catch (GenericEntityException e) {
                Debug.logError("Error deleting CasServiceTicket ticket[" + ticketId + "]. " + e.getMessage(), module);
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
    private int deleteTicketGrantingTickets(String ticketId) {
        int totalCount = 0;
        try {
            totalCount +=  delegator.removeByAnd("CasServiceTicket", "tgtId", ticketId);
            totalCount +=  delegator.removeByAnd("CasTicketGrantingTicket", "tgtId", ticketId);
            totalCount +=  delegator.removeByAnd("CasTicketGrantingTicket", "ticketGrantingTicketId", ticketId);
        } catch (GenericEntityException e) {
            Debug.logError("Error deleting CasTicketGrantingTicket ticket[" + ticketId + "]. " + e.getMessage(), module);
        }
        return totalCount;
    }

    private ExpirationPolicy accessTokenExpirationPolicy() {
        final OAuthAccessTokenProperties oauth = casProperties.getAuthn().getOauth().getAccessToken();
        if (casProperties.getLogout().isRemoveDescendantTickets()) {
            return new OAuthAccessTokenExpirationPolicy(
                Beans.newDuration(oauth.getMaxTimeToLiveInSeconds()).getSeconds(),
                Beans.newDuration(oauth.getTimeToKillInSeconds()).getSeconds()
            );
        }
        return new OAuthAccessTokenExpirationPolicy.OAuthAccessTokenSovereignExpirationPolicy(
            Beans.newDuration(oauth.getMaxTimeToLiveInSeconds()).getSeconds(),
            Beans.newDuration(oauth.getTimeToKillInSeconds()).getSeconds()
        );
    }

    private ExpirationPolicy oAuthCodeExpirationPolicy() {
        final OAuthProperties oauth = casProperties.getAuthn().getOauth();
        return new OAuthCodeExpirationPolicy(oauth.getCode().getNumberOfUses(),
            oauth.getCode().getTimeToKillInSeconds());
    }

    private ExpirationPolicy refreshTokenExpirationPolicy() {
        final OAuthRefreshTokenProperties rtProps = casProperties.getAuthn().getOauth().getRefreshToken();
        final long timeout = Beans.newDuration(rtProps.getTimeToKillInSeconds()).getSeconds();
        if (casProperties.getLogout().isRemoveDescendantTickets()) {
            return new OAuthRefreshTokenExpirationPolicy(timeout);
        }
        return new OAuthRefreshTokenExpirationPolicy.OAuthRefreshTokenSovereignExpirationPolicy(timeout);
    }
}

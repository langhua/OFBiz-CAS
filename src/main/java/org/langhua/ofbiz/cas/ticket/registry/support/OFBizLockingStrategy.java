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
package org.langhua.ofbiz.cas.ticket.registry.support;

import org.apache.ofbiz.base.util.UtilDateTime;
import org.apache.ofbiz.entity.Delegator;
import org.apache.ofbiz.entity.DelegatorFactory;
import org.apache.ofbiz.entity.GenericEntityException;
import org.apache.ofbiz.entity.GenericValue;
import org.apache.ofbiz.entity.util.EntityQuery;
import org.jasig.cas.ticket.registry.support.LockingStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

/**
 * OFBiz implementation of an exclusive, non-reentrant lock.
 *
 */
@Component("ofbizLockingStrategy")
@Transactional(transactionManager = "ticketTransactionManager")
public class OFBizLockingStrategy implements LockingStrategy {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(OFBizLockingStrategy.class);
    
    private Delegator delegator;
    
    private final String LocksEntityName = "CasLocks";

    /**
     * Application identifier that identifies rows in the locking table,
     * each one of which may be for a different application or usage within
     * a single application.
     */
    private final String applicationId;

    /** Unique identifier that identifies the client using this lock instance. */
    private final String uniqueId;

    /** Amount of time in seconds lock may be held. */
    private final long lockTimeout;

    /**
     *
     * @param applicationId Application identifier that identifies a row in the lock
     *             table for which multiple clients vie to hold the lock.
     *             This must be the same for all clients contending for a
     *             particular lock.
     * @param uniqueId Identifier used to identify this instance in a row of the
     *             lock table.  Must be unique across all clients vying for
     *             locks for a given application ID.
     * @param lockTimeout Maximum amount of time in seconds lock may be held.
     *                  A value of zero indicates that locks are held indefinitely.
     *                  Use of a reasonable timeout facilitates recovery from node failures,
     *                  so setting to zero is discouraged.
     */
    @Autowired
    public OFBizLockingStrategy(@Value("${database.cleaner.appid:cas-ticket-registry-cleaner}")
                                final String applicationId,
                                @Value("${host.name:cas01.example.org}")
                                final String uniqueId,
                                @Value("${default.ofbiz.lock.timeout:3600}") /** 1 hour */
                                final long lockTimeout,
                                @Value("${default.ofbiz.delegator.name:default}")
                                final String delegatorName) {
        this.applicationId = applicationId;
        this.uniqueId = uniqueId;
        if (lockTimeout < 0) {
            throw new IllegalArgumentException("Lock timeout must be non-negative.");
        }
        this.lockTimeout = lockTimeout;
        this.delegator = DelegatorFactory.getDelegator(delegatorName);
    }
    
    @Override
    public void release() {
        try {
            GenericValue lockValue = EntityQuery.use(delegator)
                                                .from(LocksEntityName)
                                                .where("applicationId", applicationId)
                                                .cache(false)
                                                .queryOne();
            if (lockValue == null) {
                return;
            }
            final String owner = lockValue.getString("uniqueId");
            if (!this.uniqueId.equals(owner)) {
                throw new IllegalStateException("Cannot release lock owned by " + owner);
            }
            lockValue.set("uniqueId", null);
            lockValue.set("expirationDate", null);
            LOGGER.debug("Releasing [{}] lock held by [{}].", this.applicationId, this.uniqueId);
            lockValue.store();
        } catch (GenericEntityException e) {
            // do nothing
        }
    }
    
    @Override
    public String toString() {
        return this.uniqueId;
    }

    /**
     * Acquire the lock object.
     *
     * @param lockValue the GenericValue of CasLocks
     * @return true, if successful
     */
    public boolean acquire(GenericValue lockValue) {
        boolean success = true;
        try {
            if (lockValue == null) {
                lockValue = delegator.makeValue(LocksEntityName,
                                                "applicationId", this.applicationId);
            }
            lockValue.setString("uniqueId", this.uniqueId);
            if (this.lockTimeout > 0) {
                lockValue.set("expirationDate", Timestamp.valueOf(ZonedDateTime.now(ZoneOffset.UTC).plusSeconds(this.lockTimeout).toLocalDateTime()));
            } else {
                lockValue.set("expirationDate", null);
            }
            delegator.createOrStore(lockValue);
        } catch (GenericEntityException e) {
            success = false;
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("[{}] could not obtain [{}] lock.", this.uniqueId, this.applicationId, e);
            } else {
                LOGGER.info("[{}] could not obtain [{}] lock.", this.uniqueId, this.applicationId);
            }
        }
        return success;
    }

    @Override
    public boolean acquire() {
        GenericValue lockValue = null;
        try {
            lockValue = EntityQuery.use(delegator)
                                   .from(LocksEntityName)
                                   .where("applicationId", this.applicationId)
                                   .cache(false)
                                   .queryOne();
        } catch (GenericEntityException e) {
            // do nothing
        }

        boolean result = false;
        if (lockValue != null) {
            final Timestamp expDate = lockValue.getTimestamp("expirationDate");
            if (lockValue.getString("uniqueId") == null) {
                // No one currently possesses lock
                LOGGER.debug("[{}] trying to acquire [{}] lock.", this.uniqueId, this.applicationId);
                result = acquire(lockValue);
            } else if (expDate == null || UtilDateTime.nowTimestamp().after(expDate)) {
                // Acquire expired lock regardless of who formerly owned it
                LOGGER.debug("[{}] trying to acquire expired [{}] lock.", this.uniqueId, this.applicationId);
                result = acquire(lockValue);
            }
        } else {
            // First acquisition attempt for this applicationId
            LOGGER.debug("Creating [{}] lock initially held by [{}].", applicationId, uniqueId);
            result = acquire(null);
        }
        return result;
    }
}

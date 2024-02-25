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

import org.apereo.cas.configuration.model.core.util.EncryptionRandomizedSigningJwtCryptographyProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import java.io.Serializable;
import javax.persistence.LockModeType;

/**
 * Common properties for OFBiz ticket reg.
 *
 */
public class OFBizTicketRegistryProperties implements Serializable {

    /**
     * Default lock timeout is 1 hour.
     */
    public static final String DEFAULT_LOCK_TIMEOUT = "PT1H";

    private static final long serialVersionUID = -8053839523783801072L;

    /**
     * Ticket locking type. Acceptable values are
     * {@code READ,WRITE,OPTIMISTIC,OPTIMISTIC_FORCE_INCREMENT,PESSIMISTIC_READ,
     * PESSIMISTIC_WRITE,PESSIMISTIC_FORCE_INCREMENT,NONE}.
     */
    private LockModeType ticketLockType = LockModeType.NONE;

    /**
     * Indicates the lock duration when one is about to be acquired by the cleaner.
     */
    private String ofbizLockingTimeout = DEFAULT_LOCK_TIMEOUT;

    /**
     * Crypto settings for the registry.
     */
    @NestedConfigurationProperty
    private EncryptionRandomizedSigningJwtCryptographyProperties crypto = new EncryptionRandomizedSigningJwtCryptographyProperties();

    public OFBizTicketRegistryProperties() {
        this.crypto.setEnabled(false);
    }

	public LockModeType getTicketLockType() {
		return ticketLockType;
	}

	public void setTicketLockType(LockModeType ticketLockType) {
		this.ticketLockType = ticketLockType;
	}

	public String getOFBizLockingTimeout() {
		return ofbizLockingTimeout;
	}

	public void setOFBizLockingTimeout(String ofbizLockingTimeout) {
		this.ofbizLockingTimeout = ofbizLockingTimeout;
	}
}

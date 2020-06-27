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
package org.langhua.ofbiz.cas.services;

import org.apache.ofbiz.base.util.UtilValidate;
import org.apache.ofbiz.entity.Delegator;
import org.apache.ofbiz.entity.DelegatorFactory;
import org.apache.ofbiz.entity.GenericEntityException;
import org.apache.ofbiz.entity.GenericValue;
import org.apache.ofbiz.entity.util.EntityQuery;
import org.jasig.cas.services.AbstractRegisteredService;
import org.jasig.cas.services.LogoutType;
import org.jasig.cas.services.RegexRegisteredService;
import org.jasig.cas.services.RegisteredService;
import org.jasig.cas.services.RegisteredServiceAccessStrategy;
import org.jasig.cas.services.RegisteredServiceAttributeReleasePolicy;
import org.jasig.cas.services.RegisteredServiceProperty;
import org.jasig.cas.services.RegisteredServiceProxyPolicy;
import org.jasig.cas.services.RegisteredServicePublicKey;
import org.jasig.cas.services.RegisteredServiceUsernameAttributeProvider;
import org.jasig.cas.services.ServiceRegistryDao;
import org.jasig.cas.support.oauth.services.OAuthRegisteredService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Implementation of the OFBiz ServiceRegistryDao.
 *
 */
@Component("ofbizServiceRegistryDao")
@EnableTransactionManagement(proxyTargetClass = true)
public class OFBizServiceRegistryDaoImpl implements ServiceRegistryDao {

    private static final Logger LOGGER = LoggerFactory.getLogger(OFBizServiceRegistryDaoImpl.class);

    @Value("${default.ofbiz.delegator.name:default}")
    private String delegatorName;
    
    private final Delegator delegator = DelegatorFactory.getDelegator(delegatorName);
    
    private static final String entityName = "CasRegistedService";
    
    @Override
    public boolean delete(final RegisteredService registeredService) {
        try {
            GenericValue registeredServiceValue = EntityQuery.use(delegator)
                                                             .from(entityName)
                                                             .where("id", String.valueOf(registeredService.getId()))
                                                             .cache(false)
                                                             .queryOne();
            if (registeredServiceValue != null) {
                registeredServiceValue.remove();
            }
        } catch (GenericEntityException e) {
            LOGGER.error("Error while deleting a registered service[id:{}/name:{}].", registeredService.getId(), registeredService.getName(), e);
            return false;
        }
        return true;
    }

    @Override
    public List<RegisteredService> load() {
        List<RegisteredService> registeredServices = new ArrayList<>(); 
        try {
            List<GenericValue> registeredServiceValues = EntityQuery.use(delegator)
                                                                    .from(entityName)
                                                                    .cache(false)
                                                                    .queryList();
            for (GenericValue registeredServiceValue: registeredServiceValues) {
                if (registeredServiceValue != null) {
                    RegisteredService registeredService = getRegisteredService(registeredServiceValue);
                    if (registeredService != null) {
                        registeredServices.add(registeredService);
                    }
                }
            }
        } catch (GenericEntityException e) {
            LOGGER.error("Error while loading registered services", e);
        }
        return registeredServices;
    }

    @SuppressWarnings("unchecked")
    private RegisteredService getRegisteredService(GenericValue registeredServiceValue) {
        AbstractRegisteredService registeredService = null;
        if (registeredServiceValue == null) {
            return registeredService;
        }
        if (UtilValidate.isNotEmpty(registeredServiceValue.getString("clientId")) && UtilValidate.isNotEmpty(registeredServiceValue.getString("clientSecret"))) {
            registeredService = new OAuthRegisteredService();
            ((OAuthRegisteredService) registeredService).setClientId(registeredServiceValue.getString("clientId"));
            ((OAuthRegisteredService) registeredService).setClientSecret(registeredServiceValue.getString("clientSecret"));
            ((OAuthRegisteredService) registeredService).setBypassApprovalPrompt("Y".equals(registeredServiceValue.getString("clientId")) ? Boolean.TRUE : Boolean.FALSE);
        } else {
            registeredService = new RegexRegisteredService();
        }
        registeredService.setId(Long.parseLong(registeredServiceValue.getString("id")));
        registeredService.setServiceId(registeredServiceValue.getString("serviceId"));
        registeredService.setName(registeredServiceValue.getString("serviceName"));
        registeredService.setTheme(registeredServiceValue.getString("theme"));
        registeredService.setDescription(registeredServiceValue.getString("description"));
        registeredService.setProxyPolicy((RegisteredServiceProxyPolicy) registeredServiceValue.get("proxyPolicy"));
        registeredService.setEvaluationOrder(registeredServiceValue.getLong("evaluationOrder").intValue());
        registeredService.setUsernameAttributeProvider((RegisteredServiceUsernameAttributeProvider) registeredServiceValue.get("usernameAttr"));
        registeredService.setLogoutType(LogoutType.valueOf(registeredServiceValue.getString("logoutType")));
        registeredService.setAttributeReleasePolicy((RegisteredServiceAttributeReleasePolicy) registeredServiceValue.get("attributeReleasePolicy"));
        registeredService.setRequiredHandlers((Set<String>) registeredServiceValue.get("requiredHandlers"));
        try {
            registeredService.setLogo(new URL(registeredServiceValue.getString("logo")));
        } catch (MalformedURLException e) {
            // do nothing
        }
        try {
            registeredService.setLogoutUrl(new URL(registeredServiceValue.getString("logoutUrl")));
        } catch (MalformedURLException e) {
            // do nothing
        }
        registeredService.setAccessStrategy((RegisteredServiceAccessStrategy) registeredServiceValue.get("accessStrategy"));
        registeredService.setPublicKey((RegisteredServicePublicKey) registeredServiceValue.get("publicKey"));
        registeredService.setProperties((Map<String, RegisteredServiceProperty>) registeredServiceValue.get("properties"));
        return registeredService;
    }

    @Override
    public RegisteredService save(final RegisteredService registeredService) {
        AbstractRegisteredService returnRegisteredService = null;
        try {
            returnRegisteredService = (AbstractRegisteredService) registeredService.clone();
            long id = registeredService.getId();
            if (id == RegisteredService.INITIAL_IDENTIFIER_VALUE) {
                // we ONLY accept configured services from json service registry
                return null;
            }
            GenericValue registeredServiceValue = delegator.makeValue(entityName,
                                                                      "id", String.valueOf(id),
                                                                      "serviceId", registeredService.getServiceId(),
                                                                      "serviceName", returnRegisteredService.getName(),
                                                                      "theme", registeredService.getTheme(),
                                                                      "description", registeredService.getDescription(),
                                                                      "proxyPolicy", registeredService.getProxyPolicy(),
                                                                      "evaluationOrder", (long) registeredService.getEvaluationOrder(),
                                                                      "usernameAttr", registeredService.getUsernameAttributeProvider(),
                                                                      "logoutType", registeredService.getLogoutType() == null ? "NONE" : registeredService.getLogoutType().toString(),
                                                                      "attributeReleasePolicy", registeredService.getAttributeReleasePolicy(),
                                                                      "requiredHandlers", registeredService.getRequiredHandlers(),
                                                                      "logo", registeredService.getLogo() == null ? null : registeredService.getLogo().toString(),
                                                                      "logoutUrl", registeredService.getLogoutUrl() == null ? null : registeredService.getLogoutUrl().toString(),
                                                                      "accessStrategy", registeredService.getAccessStrategy(),
                                                                      "publicKey", registeredService.getPublicKey(),
                                                                      "properties", registeredService.getProperties()
                                                                     );
            if (registeredService instanceof OAuthRegisteredService) {
                registeredServiceValue.setString("clientId", ((OAuthRegisteredService) registeredService).getClientId());
                registeredServiceValue.setString("clientSecret", ((OAuthRegisteredService) registeredService).getClientSecret());
                registeredServiceValue.setString("bypassApprovalPrompt", ((OAuthRegisteredService) registeredService).isBypassApprovalPrompt() ? "Y" : "N");
            }

            try {
                delegator.createOrStore(registeredServiceValue);
                returnRegisteredService.setId(id);
            } catch (GenericEntityException e) {
                LOGGER.error("Error while saving a registered service.", e);
            }
        } catch (CloneNotSupportedException e) {
            LOGGER.error("Error while cloning a registered service.", e);
        }
        return returnRegisteredService;
    }

    @Override
    public RegisteredService findServiceById(final long id) {
        RegisteredService registeredService = null;
        try {
            GenericValue registeredServiceValue = EntityQuery.use(delegator)
                                                             .from(entityName)
                                                             .where("id", String.valueOf(id))
                                                             .cache(false)
                                                             .queryOne();
            if (registeredServiceValue != null) {
                registeredService = getRegisteredService(registeredServiceValue);
            }
        } catch (GenericEntityException e) {
            LOGGER.error("Error while finding a registered service[{}].", id, e);
        }
        return registeredService;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName();
    }
}

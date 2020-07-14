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

import org.apache.ofbiz.base.util.Debug;
import org.apache.ofbiz.base.util.UtilValidate;
import org.apache.ofbiz.entity.Delegator;
import org.apache.ofbiz.entity.DelegatorFactory;
import org.apache.ofbiz.entity.GenericEntityException;
import org.apache.ofbiz.entity.GenericValue;
import org.apache.ofbiz.entity.util.EntityQuery;
import org.apereo.cas.services.AbstractRegisteredService;
import org.apereo.cas.services.AbstractServiceRegistry;
import org.apereo.cas.services.RegexRegisteredService;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.services.RegisteredService.LogoutType;
import org.apereo.cas.services.RegisteredServiceAccessStrategy;
import org.apereo.cas.services.RegisteredServiceAttributeReleasePolicy;
import org.apereo.cas.services.RegisteredServiceMultifactorPolicy;
import org.apereo.cas.services.RegisteredServiceProperty;
import org.apereo.cas.services.RegisteredServiceProxyPolicy;
import org.apereo.cas.services.RegisteredServicePublicKey;
import org.apereo.cas.services.RegisteredServiceUsernameAttributeProvider;
import org.apereo.cas.support.events.service.CasRegisteredServiceLoadedEvent;
import org.apereo.cas.support.oauth.services.OAuthRegisteredService;
import org.apereo.cas.util.serialization.SerializationUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

/**
 * Implementation of the ServiceRegistry based on OFBiz.
 *
 */
@EnableTransactionManagement(proxyTargetClass = true)
public class OFBizServiceRegistry extends AbstractServiceRegistry {
    public static final String module = OFBizServiceRegistry.class.getName();

    @Value("${default.ofbiz.delegator.name:default}")
    private String delegatorName;
    
    private final Delegator delegator = DelegatorFactory.getDelegator(delegatorName);
    
    private static final String entityName = "CasRegisteredService";
    
    protected OFBizServiceRegistry() {
        super();
    }
    
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
            Debug.logError("Error while deleting a registered service[id:{}/name:{}].", module,
                           registeredService.getId(), registeredService.getName(), e);
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
                        publishEvent(new CasRegisteredServiceLoadedEvent(this, registeredService));
                    }
                }
            }
        } catch (GenericEntityException e) {
            Debug.logError("Error while loading registered services: " + e.getMessage(), module);
        }
        return registeredServices;
    }

    @Override
    public RegisteredService save(final RegisteredService registeredService) {
        long id = registeredService.getId();
        if (id == RegisteredService.INITIAL_IDENTIFIER_VALUE) {
            // we ONLY accept configured services from json service registry
            return null;
        }
        GenericValue registeredServiceValue = delegator.makeValue(entityName,
                                                                  "id", String.valueOf(id),
                                                                  "serviceId", registeredService.getServiceId(),
                                                                  "serviceName", registeredService.getName(),
                                                                  "theme", registeredService.getTheme(),
                                                                  "description", registeredService.getDescription(),
                                                                  "evaluationOrder", (long) registeredService.getEvaluationOrder(),
                                                                  "logoutType", registeredService.getLogoutType() == null ? "NONE" : registeredService.getLogoutType().toString(),
                                                                  "logo", registeredService.getLogo() == null ? null : registeredService.getLogo().toString(),
                                                                  "logoutUrl", registeredService.getLogoutUrl() == null ? null : registeredService.getLogoutUrl().toString()
                                                                 );
        if (UtilValidate.isNotEmpty(registeredService.getProxyPolicy())) {
            registeredServiceValue.setBytes("proxyPolicy", SerializationUtils.serialize(registeredService.getProxyPolicy()));
        }
        if (UtilValidate.isNotEmpty(registeredService.getUsernameAttributeProvider())) {
            registeredServiceValue.setBytes("usernameAttr", SerializationUtils.serialize(registeredService.getUsernameAttributeProvider()));
        }
        if (UtilValidate.isNotEmpty(registeredService.getAttributeReleasePolicy())) {
            registeredServiceValue.setBytes("attributeReleasePolicy", SerializationUtils.serialize(registeredService.getAttributeReleasePolicy()));
        }
        if (UtilValidate.isNotEmpty(registeredService.getMultifactorPolicy())) {
            registeredServiceValue.setBytes("multifactorPolicy", SerializationUtils.serialize(registeredService.getMultifactorPolicy()));
        }
        if (UtilValidate.isNotEmpty(registeredService.getRequiredHandlers())) {
            registeredServiceValue.setBytes("requiredHandlers", SerializationUtils.serialize((HashSet<String>) registeredService.getRequiredHandlers()));
        }
        if (UtilValidate.isNotEmpty(registeredService.getAccessStrategy())) {
            registeredServiceValue.setBytes("accessStrategy", SerializationUtils.serialize(registeredService.getAccessStrategy()));
        }
        if (UtilValidate.isNotEmpty(registeredService.getPublicKey())) {
            registeredServiceValue.setBytes("publicKey", SerializationUtils.serialize(registeredService.getPublicKey()));
        }
        if (UtilValidate.isNotEmpty(registeredService.getProperties())) {
            registeredServiceValue.setBytes("properties", SerializationUtils.serialize((HashMap<String, RegisteredServiceProperty>) registeredService.getProperties()));
        }
        if (registeredService instanceof OAuthRegisteredService) {
            registeredServiceValue.setString("clientId", ((OAuthRegisteredService) registeredService).getClientId());
            registeredServiceValue.setString("clientSecret", ((OAuthRegisteredService) registeredService).getClientSecret());
            registeredServiceValue.setString("bypassApprovalPrompt", ((OAuthRegisteredService) registeredService).isBypassApprovalPrompt() ? "Y" : "N");
            registeredServiceValue.setString("generateRefreshToken", ((OAuthRegisteredService) registeredService).isGenerateRefreshToken() ? "Y" : "N");
            registeredServiceValue.setString("jsonFormat", ((OAuthRegisteredService) registeredService).isJsonFormat() ? "Y" : "N");
            if (UtilValidate.isNotEmpty(((OAuthRegisteredService) registeredService).getSupportedGrantTypes())) {
                registeredServiceValue.setBytes("supportedGrantTypes", SerializationUtils.serialize(((OAuthRegisteredService) registeredService).getSupportedGrantTypes()));
            }
            if (UtilValidate.isNotEmpty(((OAuthRegisteredService) registeredService).getSupportedResponseTypes())) {
                registeredServiceValue.setBytes("supportedResponseTypes", SerializationUtils.serialize(((OAuthRegisteredService) registeredService).getSupportedResponseTypes()));
            }
        }

        try {
            delegator.createOrStore(registeredServiceValue);
        } catch (GenericEntityException e) {
            Debug.logError("Error while saving a registered service." + e.getMessage(), module);
        }
        return registeredService;
    }

    @Override
    public RegisteredService findServiceById(final long id) {
        return findServiceById(String.valueOf(id));
    }

    @Override
    public RegisteredService findServiceById(final String id) {
        RegisteredService registeredService = null;
        try {
            GenericValue registeredServiceValue = EntityQuery.use(delegator)
                                                             .from(entityName)
                                                             .where("id", id)
                                                             .cache(false)
                                                             .queryOne();
            if (registeredServiceValue != null) {
                registeredService = getRegisteredService(registeredServiceValue);
            }
        } catch (GenericEntityException e) {
            Debug.logError("Error while finding a registered service[{}]. " + e.getMessage(), module, id);
        }
        return registeredService;
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
            ((OAuthRegisteredService) registeredService).setBypassApprovalPrompt("Y".equals(registeredServiceValue.getString("bypassApprovalPrompt")) ? Boolean.TRUE : Boolean.FALSE);
            ((OAuthRegisteredService) registeredService).setGenerateRefreshToken("Y".equals(registeredServiceValue.getString("generateRefreshToken")) ? Boolean.TRUE : Boolean.FALSE);
            ((OAuthRegisteredService) registeredService).setJsonFormat("Y".equals(registeredServiceValue.getString("jsonFormat")) ? Boolean.TRUE : Boolean.FALSE);
            if (UtilValidate.isNotEmpty(registeredServiceValue.getBytes("supportedGrantTypes"))) {
                ((OAuthRegisteredService) registeredService).setSupportedGrantTypes(SerializationUtils.deserialize(registeredServiceValue.getBytes("supportedGrantTypes"), HashSet.class));
            }
            if (UtilValidate.isNotEmpty(registeredServiceValue.getBytes("supportedResponseTypes"))) {
                ((OAuthRegisteredService) registeredService).setSupportedResponseTypes(SerializationUtils.deserialize(registeredServiceValue.getBytes("supportedResponseTypes"), HashSet.class));
            }
        } else {
            registeredService = new RegexRegisteredService();
        }
        registeredService.setId(Long.parseLong(registeredServiceValue.getString("id")));
        registeredService.setServiceId(registeredServiceValue.getString("serviceId"));
        registeredService.setName(registeredServiceValue.getString("serviceName"));
        registeredService.setTheme(registeredServiceValue.getString("theme"));
        registeredService.setDescription(registeredServiceValue.getString("description"));
        if (UtilValidate.isNotEmpty(registeredServiceValue.getBytes("proxyPolicy"))) {
            registeredService.setProxyPolicy(SerializationUtils.deserialize(registeredServiceValue.getBytes("proxyPolicy"), RegisteredServiceProxyPolicy.class));
        }
        registeredService.setEvaluationOrder(registeredServiceValue.getLong("evaluationOrder").intValue());
        if (UtilValidate.isNotEmpty(registeredServiceValue.getBytes("usernameAttr"))) {
            registeredService.setUsernameAttributeProvider(SerializationUtils.deserialize(registeredServiceValue.getBytes("usernameAttr"), RegisteredServiceUsernameAttributeProvider.class));
        }
        registeredService.setLogoutType(LogoutType.valueOf(registeredServiceValue.getString("logoutType")));
        if (UtilValidate.isNotEmpty(registeredServiceValue.getBytes("attributeReleasePolicy"))) {
            registeredService.setAttributeReleasePolicy(SerializationUtils.deserialize(registeredServiceValue.getBytes("attributeReleasePolicy"), RegisteredServiceAttributeReleasePolicy.class));
        }
        if (UtilValidate.isNotEmpty(registeredServiceValue.getBytes("multifactorPolicy"))) {
            registeredService.setMultifactorPolicy(SerializationUtils.deserialize(registeredServiceValue.getBytes("multifactorPolicy"), RegisteredServiceMultifactorPolicy.class));
        }
        if (UtilValidate.isNotEmpty(registeredServiceValue.getBytes("requiredHandlers"))) {
            registeredService.setRequiredHandlers(SerializationUtils.deserialize(registeredServiceValue.getBytes("requiredHandlers"), HashSet.class));
        }
        registeredService.setLogo(registeredServiceValue.getString("logo"));
        try {
            registeredService.setLogoutUrl(new URL(registeredServiceValue.getString("logoutUrl")));
        } catch (MalformedURLException e) {
            // do nothing
        }
        if (UtilValidate.isNotEmpty(registeredServiceValue.getBytes("accessStrategy"))) {
            registeredService.setAccessStrategy(SerializationUtils.deserialize(registeredServiceValue.getBytes("accessStrategy"), RegisteredServiceAccessStrategy.class));
        }
        if (UtilValidate.isNotEmpty(registeredServiceValue.getBytes("publicKey"))) {
            try {
                registeredService.setPublicKey(SerializationUtils.deserialize(registeredServiceValue.getBytes("publicKey"), RegisteredServicePublicKey.class));
            } catch (Exception e) {
                // do nothing
            }
        }
        if (UtilValidate.isNotEmpty(registeredServiceValue.get("properties"))) {
            registeredService.setProperties(SerializationUtils.deserialize(registeredServiceValue.getBytes("properties"), HashMap.class));
        }
        return registeredService;
    }

    @Override
    public long size() {
        try {
            return EntityQuery.use(delegator)
                              .from(entityName)
                              .queryCount();
        } catch (GenericEntityException e) {
            return -1L;
        }
    }
}

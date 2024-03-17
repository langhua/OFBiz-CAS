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
package langhua.ofbiz.cas.oauth;

import org.apache.ofbiz.base.util.UtilValidate;
import org.apache.ofbiz.entity.Delegator;
import org.apache.ofbiz.entity.GenericValue;
import org.apache.ofbiz.entity.util.EntityQuery;
import org.apache.ofbiz.service.LocalDispatcher;
import org.apereo.cas.CasProtocolConstants;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.services.RegisteredService;
import org.apache.ofbiz.base.util.Debug;
import org.apache.ofbiz.base.util.UtilMisc;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.profile.OAuth20UserProfileDataCreator;
import org.apereo.cas.support.oauth.services.OAuthRegisteredService;
import org.apereo.cas.support.oauth.util.OAuth20Utils;
import org.apereo.cas.ticket.accesstoken.AccessToken;
import org.apereo.inspektr.audit.annotation.Audit;
import org.pac4j.core.context.J2EContext;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * OFBiz implementation of {@link OAuth20UserProfileDataCreator}.
 */
public class OFBizOAuth20UserProfileDataCreator implements OAuth20UserProfileDataCreator {
    public static final String module = OFBizOAuth20UserProfileDataCreator.class.getName();
    static final String userNameAttr = "username";
    static final String userProfileAttrs = "userprofile";

    /**
     * The services manager.
     */
    private final ServicesManager servicesManager;
    private final PrincipalFactory principalFactory;
    private final Delegator delegator;
    private final LocalDispatcher dispatcher;

    public OFBizOAuth20UserProfileDataCreator(ServicesManager servicesManager, PrincipalFactory principalFactory,
			Delegator delegator, LocalDispatcher dispatcher) {
		this.servicesManager = servicesManager;
		this.principalFactory = principalFactory;
		this.delegator = delegator;
		this.dispatcher = dispatcher;
	}

	@Override
    @Audit(action = "OAUTH2_USER_PROFILE_DATA",
            actionResolverName = "OAUTH2_USER_PROFILE_DATA_ACTION_RESOLVER",
            resourceResolverName = "OAUTH2_USER_PROFILE_DATA_RESOURCE_RESOLVER")
    public Map<String, Object> createFrom(final AccessToken accessToken, final J2EContext context) {
        Principal principal = accessToken.getAuthentication().getPrincipal();
        Debug.logInfo("Preparing user profile response based on CAS principal [%s]", module, principal);

        final Map<String, Object> map = new HashMap<>();
        if (principal != null && UtilValidate.isNotEmpty(principal.getId())) {
            // fetch the user's data
            String userId = principal.getId();
            Map<String, Object> attributes = principal.getAttributes();
            try {
                // person data
                GenericValue person = EntityQuery.use(this.delegator)
                        .from("UserLoginAndPartyDetails")
                        .where("userLoginId", userId)
                        .queryOne();
                if (UtilValidate.isNotEmpty(person)) {
                    String partyId = person.getString("partyId");
                    attributes.putAll(person.getAllFields());

                    // roles
                    // List<GenericValue> roleValues = EntityQuery.use(delegator)
                    // 		                                   .from("PartyRoleAndPartyDetail")
                    // 		                                   .where("partyId", partyId, "statusId", "PARTY_ENABLED")
                    // 		                                   .select("roleTypeId")
                    // 		                                   .distinct()
                    // 		                                   .queryList();
                    // if (UtilValidate.isNotEmpty(roleValues)) {
                    // 	Set<String> roles = new HashSet<>();
                    // 	for (GenericValue role : roleValues) {
                    // 		roles.add(role.getString("roleTypeId"));
                    // 	}
                    // 	if (UtilValidate.isNotEmpty(roles)) {
                    // 		// add roles to map directly
                    // 		map.put("roles", roles);
                    // 	}
                    // }

                    // permissions
                    List<GenericValue> permissionValues = EntityQuery.use(delegator)
                            .from("UserLoginSecurityGroup")
                            .where("userLoginId", userId)
                            .select("groupId")
                            .filterByDate()
                            .distinct()
                            .queryList();
                    if (UtilValidate.isNotEmpty(permissionValues)) {
                        Set<String> permissions = new HashSet<>();
                        for (GenericValue permission : permissionValues) {
                            permissions.add(permission.getString("groupId"));
                        }
                        if (UtilValidate.isNotEmpty(permissions)) {
                            // add permissions to map directly
                            map.put("permissions", permissions);
                        }
                    }
                    GenericValue userLogin = EntityQuery.use(delegator)
                            .from("UserLogin")
                            .where("userLoginId", userId).queryOne();
                    Map<String, Object> getUserPermissionListResult = this.dispatcher.runSync("getUserPermissionList", UtilMisc.toMap("userLogin", userLogin));
                    if (getUserPermissionListResult.get("result").equals("success")) {
                        map.put("roles", getUserPermissionListResult.get("permissions"));
                    }
                }
            } catch (Exception e) {
                Debug.logError(e.getMessage(), module);
            }
            if (UtilValidate.isNotEmpty(attributes)) {
                principal = this.principalFactory.createPrincipal(userId, attributes);
            }
        }

        finalizeProfileResponse(accessToken, context, map, principal);
        return map;
    }

    /**
     * Finalize profile response.
     *
     * @param accessTokenTicket the access token ticket
     * @param context
     * @param map               the map
     * @param principal         the authentication principal
     */
    protected void finalizeProfileResponse(final AccessToken accessToken, final J2EContext context,
                                           final Map<String, Object> map,
                                           final Principal principal) {
        final Service service = accessToken.getService();
        final RegisteredService rs = this.servicesManager.findServiceBy(service);
        String clientId = null;
        OAuthRegisteredService registeredService = null;
        if (rs != null && rs instanceof OAuthRegisteredService) {
            registeredService = (OAuthRegisteredService) rs;
            clientId = registeredService.getClientId();
        } else {
            clientId = service.getId();
            registeredService = OAuth20Utils.getRegisteredOAuthServiceByClientId(this.servicesManager, clientId);
        }
        if (registeredService != null) {
            map.put(OAuth20Constants.CLIENT_ID, clientId);
            if (registeredService.getName() != null) {
                map.put(CasProtocolConstants.PARAMETER_SERVICE, registeredService.getName());
            } else if (registeredService.getFriendlyName() != null) {
                map.put(CasProtocolConstants.PARAMETER_SERVICE, registeredService.getFriendlyName());
            } else if (registeredService.getDescription() != null) {
                map.put(CasProtocolConstants.PARAMETER_SERVICE, registeredService.getDescription());
            }

            Map<String, Object> attributes = registeredService.getAttributeReleasePolicy().getAttributes(principal, service, registeredService);
            if (UtilValidate.isNotEmpty(attributes)) {
                map.put(userNameAttr, principal.getId());
                map.put(userProfileAttrs, attributes);
                map.put(userProfileAttrs, attributes);
            }
        }
    }
}

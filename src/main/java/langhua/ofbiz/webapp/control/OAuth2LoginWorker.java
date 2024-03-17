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
package langhua.ofbiz.webapp.control;

import java.io.IOException;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;

import com.alipay.api.request.AlipaySystemOauthTokenRequest;
import com.alipay.api.response.AlipaySystemOauthTokenResponse;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.apache.ofbiz.base.util.*;
import org.apache.ofbiz.entity.Delegator;
import org.apache.ofbiz.entity.GenericEntityException;
import org.apache.ofbiz.entity.GenericValue;
import org.apache.ofbiz.entity.condition.EntityCondition;
import org.apache.ofbiz.entity.condition.EntityExpr;
import org.apache.ofbiz.entity.condition.EntityJoinOperator;
import org.apache.ofbiz.entity.model.ModelEntity;
import org.apache.ofbiz.entity.util.EntityQuery;
import org.apache.ofbiz.webapp.stats.VisitHandler;
import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.ticket.AbstractTicket;
import org.apereo.cas.ticket.ExpirationPolicy;
import org.apereo.cas.ticket.TicketGrantingTicketImpl;
import org.apereo.cas.ticket.accesstoken.AccessToken;
import org.apereo.cas.ticket.accesstoken.AccessTokenImpl;
import org.apereo.cas.util.serialization.SerializationUtils;

import com.google.gson.Gson;
import org.apache.ofbiz.base.util.UtilValidate;
import org.apache.ofbiz.base.util.UtilDateTime;

/**
 * OAuth2 Login Worker
 */
public class OAuth2LoginWorker {

    public final static String MODULE = OAuth2LoginWorker.class.getName();

    private static Gson gson = new Gson();

    /**
     * Check CAS OAuth2 Login
     *
     * @param request  The HTTP request object for the current JSP or Servlet request.
     * @param response The HTTP response object for the current JSP or Servlet request.
     * @return String
     */
    public static String oauth2CasCheckLogin(HttpServletRequest request, HttpServletResponse response) throws Exception {
        String credentials = request.getHeader("Authorization");
        HttpSession session = request.getSession();
        Object oldUserLogin = session.getAttribute("userLogin");
        Debug.logInfo("oauth2CasCheckLogin credentials:" + credentials, MODULE);

        if (credentials != null) {
            if (credentials.startsWith("Bearer ")) {
                String accessToken = credentials.replace("Bearer ", "");
                Debug.logInfo("Found HTTP Bearer access_token", MODULE);
                Delegator delegator = (Delegator) request.getAttribute("delegator");
                try {
                    if (accessToken.startsWith(AccessToken.PREFIX)) {
                        if (oldUserLogin != null) {
                            session.removeAttribute("userLogin");
                        }
                        GenericValue ticketValue = EntityQuery.use(delegator)
                                                              .from("CasServiceTicket")
                                                              .where("ticketId", accessToken)
                                                              .cache(false)
                                                              .queryOne();
                        if (UtilValidate.isNotEmpty(ticketValue)) {
                            Debug.logInfo("Found service ticket: " + ticketValue, MODULE);
                            AccessTokenImpl at = getAccessToken(delegator, ticketValue);
                            if (!at.isExpired() && at.getAuthentication() != null && at.getAuthentication().getPrincipal() != null) {
                                String userLoginId = at.getAuthentication().getPrincipal().getId();
                                Debug.logInfo("Found userLoginId: " + userLoginId, MODULE);
                                GenericValue userLogin = EntityQuery.use(delegator)
                                                                    .from("UserLogin")
                                                                    .where("userLoginId", userLoginId)
                                                                    .cache(false)
                                                                    .queryOne();
                                if (UtilValidate.isNotEmpty(userLogin) &&
                                        (UtilValidate.isEmpty(userLogin.getString("enabled")) || "Y".equals(userLogin.getString("enabled"))) &&
                                        (userLogin.get("disabledDateTime") == null || UtilDateTime.nowTimestamp().after(userLogin.getTimestamp("disabledDateTime")))) {
                                    Debug.logInfo("Found UserLogin: " + userLogin, MODULE);
                                    doOAuthLogin(userLogin, request);
                                    request.setAttribute("userLogin", userLogin);
                                } else {
                                    response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                                    return "error";
                                }
                            } else {
                                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                                return "error";
                            }
                        }
                    }
                } catch (Exception e) {
                    Debug.logError(e, MODULE);
                }
            }
        }
        return "success";
    }

    // TODO move this method to passport plugin
    public static String oauth2WechatMiniCheckLogin(HttpServletRequest request, HttpServletResponse response) throws Exception {
        HttpSession session = request.getSession();
        Object olduserLogin = session.getAttribute("userLogin");
        String credentials = request.getHeader("Authorization");
        if (Debug.verboseOn()) {
            Debug.logVerbose("oauth2WechatMiniCheckLogin credentials:" + credentials, MODULE);
        }
        if (credentials != null) {
            if (credentials.startsWith("Bearer ")) {
                String accessToken = credentials.replace("Bearer ", "");
                if (Debug.verboseOn()) {
                    Debug.logVerbose("Found HTTP Bearer access_token", MODULE);
                }
                Delegator delegator = (Delegator) request.getAttribute("delegator");
                try {
                    if (accessToken.startsWith("wechatMini-")) {
                        if (olduserLogin == null) {
                            session.removeAttribute("userLogin");
                        }
                        // check whether wechat token is expired
                        String openId = accessToken.replace("wechatMini-", "");
                        List<EntityExpr> andConditions = UtilMisc.toList(
                                EntityCondition.makeCondition("openId", openId),
                                EntityCondition.makeCondition("transferType", "wechat"));
                        EntityCondition whereCondition = EntityCondition.makeCondition(andConditions, EntityJoinOperator.AND);
                        GenericValue openIdSessionRel = EntityQuery.use(delegator)
                                                                   .from("OpenIdSessionRel")
                                                                   .where(whereCondition)
                                                                   .cache(false)
                                                                   .queryOne();
                        if (!UtilValidate.isNotEmpty(openIdSessionRel)) {
                            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Please login");
                            return "error";
                        }
                        long currentTime = System.currentTimeMillis();
                        if (currentTime > Long.parseLong(openIdSessionRel.get("expiredTime").toString())) {
                            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Token expired, please refresh it");
                            return "error";
                        } else {
                            request.setAttribute("accessToken", openId);
                            GenericValue userLogin = null;
                            GenericValue thirdMemberInfo = EntityQuery.use(delegator)
                                                                      .from("OauthMemberInfo")
                                                                      .where("wxopenId", openId)
                                                                      .cache(false)
                                                                      .queryOne();
                            if (UtilValidate.isNotEmpty(thirdMemberInfo)) {
                                userLogin = EntityQuery.use(delegator)
                                                       .from("UserLogin")
                                                       .where("partyId", thirdMemberInfo.get("partyId"))
                                                       .cache(false)
                                                       .queryOne();
                            }
                            if (!UtilValidate.isNotEmpty(userLogin)) {
                                userLogin = EntityQuery.use(delegator)
                                                       .from("UserLogin")
                                                       .where("userLoginId", "admin")
                                                       .cache(false)
                                                       .queryOne();
                            }
                            doOAuthLogin(userLogin, request);
                            request.setAttribute("userLogin", userLogin);
                        }
                    }
                } catch (Exception e) {
                    Debug.logError(e, MODULE);
                }
            } else if (credentials.startsWith("Bearer-wx-")) {
                if (olduserLogin == null) {
                    session.removeAttribute("userLogin");
                }
                String code = credentials.replace("Bearer-wx-", "");
                if (Debug.verboseOn()) {
                    Debug.logVerbose("Found HTTP Bearer access_token", MODULE);
                }
                Delegator delegator = (Delegator) request.getAttribute("delegator");
                try {
                    GenericValue appIdSystemProperty = EntityQuery.use(delegator)
                                                                  .from("SystemProperty")
                                                                  .where("systemResourceId", "Passport_WechatMini", "systemPropertyId", "appId").cache(false)
                                                                  .queryOne();
                    if (Debug.verboseOn()) {
                        Debug.logVerbose("appIdSystemProperty:" + appIdSystemProperty, MODULE);
                    }
                    String appId = appIdSystemProperty.get("systemPropertyValue").toString();
                    GenericValue appSecretSystemProperty = EntityQuery.use(delegator)
                                                                      .from("SystemProperty")
                                                                      .where("systemResourceId", "Passport_WechatMini", "systemPropertyId", "appSecret").cache(false)
                                                                      .queryOne();
                    if (Debug.verboseOn()) {
                        Debug.logVerbose("appSecretSystemProperty:" + appSecretSystemProperty, MODULE);
                    }
                    String appSecret = appSecretSystemProperty.get("systemPropertyValue").toString();
                    String jscode2session = UtilProperties.getPropertyValue("wechatLiteInfo.properties", "wechat.lite.jscode2session");
                    String requestUrl = jscode2session + "?" +
                                        "appid=" + appId + "&" +
                                        "secret=" + appSecret + "&" +
                                        "js_code=" + code + "&" +
                                        "grant_type=authorization_code";
                    if (Debug.verboseOn()) {
                        Debug.logVerbose("oauth2WechatMiniCheckLogin requestUrl:" + requestUrl, MODULE);
                    }
                    String resultStr = doGet(requestUrl);
                    @SuppressWarnings("unchecked")
                    HashMap<String, String> resultMap = gson.fromJson(resultStr, HashMap.class);
                    if (Debug.verboseOn()) {
                        Debug.logVerbose("resultMap:" + resultMap, MODULE);
                    }
                    long expiredTime = System.currentTimeMillis() + 7100 * 1000;
                    if (resultMap.containsKey("errcode")) {
                        response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Failed to get wechat mini openId");
                        return "error";
                    } else {
                        String openId = resultMap.get("openid");
                        if (Debug.verboseOn()) {
                            Debug.logVerbose("openId:" + openId, MODULE);
                        }
                        String sessionKey = resultMap.get("session_key");
                        if (Debug.verboseOn()) {
                            Debug.logVerbose("sessionKey:" + sessionKey, MODULE);
                        }
                        List<GenericValue> storeList = new ArrayList<GenericValue>();
                        List<EntityExpr> andConditions = UtilMisc.toList(
                                EntityCondition.makeCondition("openId", openId),
                                EntityCondition.makeCondition("transferType", "wechat"));
                        EntityCondition whereCondition = EntityCondition.makeCondition(andConditions, EntityJoinOperator.AND);
                        GenericValue openIdSessionRel = EntityQuery.use(delegator)
                                                                   .from("OpenIdSessionRel")
                                                                   .where(whereCondition)
                                                                   .cache(false)
                                                                   .queryOne();
                        if (Debug.verboseOn()) {
                            Debug.logVerbose("openIdSessionRel:" + openIdSessionRel, MODULE);
                        }
                        // if no data found by wechat openId, then insert it, else update
                        if (UtilValidate.isEmpty(openIdSessionRel)) {
                            if (Debug.verboseOn()) {
                                Debug.logVerbose("===createOpenIdSessionRel===", MODULE);
                            }
                            GenericValue newOpenIdSessionRel = delegator.makeValue("OpenIdSessionRel", UtilMisc.toMap("openId", openId, "transferType",
                                    "wechat", "sessionKey", sessionKey, "transferTime", UtilDateTime.nowTimestamp(), "expiredTime", String.valueOf(expiredTime)));
                            delegator.create(newOpenIdSessionRel);
                        } else {
                            if (Debug.verboseOn()) {
                                Debug.logVerbose("===updateOpenIdSessionRel===", MODULE);
                            }
                            openIdSessionRel.set("sessionKey", sessionKey);
                            openIdSessionRel.set("transferTime", UtilDateTime.nowTimestamp());
                            openIdSessionRel.set("expiredTime", String.valueOf(expiredTime));
                            storeList.add(openIdSessionRel);
                            delegator.storeAll(storeList);
                        }
                        //TODO load an administrator according to configuration rather than hard coded 'admin'
                        request.setAttribute("openId", openId);
                        GenericValue userLogin = null;
                        GenericValue thirdMemberInfo = EntityQuery.use(delegator)
                                                                  .from("OauthMemberInfo")
                                                                  .where("wxopenId", openId)
                                                                  .cache(false)
                                                                  .queryOne();
                        if (UtilValidate.isNotEmpty(thirdMemberInfo)) {
                            userLogin = EntityQuery.use(delegator)
                                                   .from("UserLogin")
                                                   .where("partyId", thirdMemberInfo.get("partyId"))
                                                   .cache(false)
                                                   .queryOne();
                        }
                        if (!UtilValidate.isNotEmpty(userLogin)) {
                            userLogin = EntityQuery.use(delegator)
                                                   .from("UserLogin")
                                                   .where("userLoginId", "admin")
                                                   .cache(false)
                                                   .queryOne();
                        }
                        doOAuthLogin(userLogin, request);
                        if (Debug.verboseOn()) {
                            Debug.logVerbose("=====oauth2WechatMiniCheckLogin======request:" + request, MODULE);
                        }
                    }
                } catch (Exception e) {
                    response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Failed to get openId");
                    Debug.logError(e, MODULE);
                    return "error";
                }
            }
        }

        return "success";
    }

    // TODO move this method to passport plugin
    public static String oauth2AlipayMiniCheckLogin(HttpServletRequest request, HttpServletResponse response) throws Exception {
        HttpSession session = request.getSession();
        Object olduserLogin = session.getAttribute("userLogin");
        String credentials = request.getHeader("Authorization");
        if (credentials != null) {
            if (credentials.startsWith("Bearer ")) {
                String accessToken = credentials.replace("Bearer ", "");
                if (Debug.verboseOn()) {
                    Debug.logVerbose("Found HTTP Bearer access_token", MODULE);
                }
                Delegator delegator = (Delegator) request.getAttribute("delegator");
                try {
                    if (accessToken.startsWith("alipayMini-")) {
                        if (olduserLogin == null) {
                            session.removeAttribute("userLogin");
                        }
                        // check whether alipay token expired
                        String openId = accessToken.replace("alipayMini-", "");
                        List<EntityExpr> andConditions = UtilMisc.toList(
                                EntityCondition.makeCondition("openId", openId),
                                EntityCondition.makeCondition("transferType", "alipay"));
                        EntityCondition whereCondition = EntityCondition.makeCondition(andConditions, EntityJoinOperator.AND);
                        GenericValue openIdSessionRel = EntityQuery.use(delegator)
                                                                   .from("OpenIdSessionRel")
                                                                   .where(whereCondition)
                                                                   .cache(false)
                                                                   .queryOne();
                        if (!UtilValidate.isNotEmpty(openIdSessionRel)) {
                            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Please login");
                            return "error";
                        }
                        long currentTime = System.currentTimeMillis();
                        if (currentTime > Long.parseLong(openIdSessionRel.get("expiredTime").toString())) {
                            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Token expired, please refresh it");
                            return "error";
                        } else {
                            request.setAttribute("accessToken", openId);
                            GenericValue userLogin = null;
                            GenericValue thirdMemberInfo = EntityQuery.use(delegator)
                                                                      .from("OauthMemberInfo")
                                                                      .where("aliopenId", openId)
                                                                      .cache(false)
                                                                      .queryOne();
                            if (UtilValidate.isNotEmpty(thirdMemberInfo)) {
                                userLogin = EntityQuery.use(delegator)
                                                       .from("UserLogin")
                                                       .where("partyId", thirdMemberInfo.get("partyId"))
                                                       .cache(false)
                                                       .queryOne();
                            }
                            if (!UtilValidate.isNotEmpty(userLogin)) {
                                userLogin = EntityQuery.use(delegator)
                                                       .from("UserLogin")
                                                       .where("userLoginId", "admin")
                                                       .cache(false)
                                                       .queryOne();
                            }
                            doOAuthLogin(userLogin, request);
                            request.setAttribute("userLogin", userLogin);
                            if (Debug.verboseOn()) {
                                Debug.logVerbose("=====oauth2AlipayMiniCheckLogin======request:" + request, MODULE);
                            }
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    Debug.logError(e, MODULE);
                }
            } else if (credentials.startsWith("Bearer-Ali-")) {
                if (olduserLogin == null) {
                    session.removeAttribute("userLogin");
                }
                String code = credentials.replace("Bearer-Ali-", "");
                if (Debug.verboseOn()) {
                    Debug.logVerbose("Found HTTP Bearer access_token", MODULE);
                }
                Delegator delegator = (Delegator) request.getAttribute("delegator");
                try {
                    GenericValue appIdSystemProperty = EntityQuery.use(delegator)
                                                                  .from("SystemProperty")
                                                                  .where("systemResourceId", "Passport_AlipayMini", "systemPropertyId", "appId").cache(false)
                                                                  .queryOne();
                    String appId = appIdSystemProperty.get("systemPropertyValue").toString();

                    GenericValue appPrivateKeySystemProperty = EntityQuery.use(delegator)
                                                                          .from("SystemProperty")
                                                                          .where("systemResourceId", "Passport_AlipayMini", "systemPropertyId", "appPrivateKey").cache(false)
                                                                          .queryOne();
                    String appPrivateKey = appPrivateKeySystemProperty.get("systemPropertyValue").toString();
                    GenericValue appPublicKeySystemProperty = EntityQuery.use(delegator)
                                                                         .from("SystemProperty")
                                                                         .where("systemResourceId", "Passport_AlipayMini", "systemPropertyId", "appPublicKey").cache(false)
                                                                         .queryOne();
                    String appPublicKey = appPublicKeySystemProperty.get("systemPropertyValue").toString();

                    String jscode2session = UtilProperties.getPropertyValue("alipayApi.properties", "alipay.lite.oauth.token");
                    AlipayClient alipayClient = new DefaultAlipayClient(jscode2session, appId, appPrivateKey, "json", "GBK", appPublicKey, "RSA2");
                    AlipaySystemOauthTokenRequest asotRequest = new AlipaySystemOauthTokenRequest();
                    asotRequest.setGrantType("authorization_code");
                    asotRequest.setCode(code);
                    AlipaySystemOauthTokenResponse asotrResponse = alipayClient.execute(asotRequest);

                    if (asotrResponse.isSuccess()) {
                        if (Debug.verboseOn()) {
                            Debug.logVerbose("Alipay token request success", MODULE);
                            Debug.logVerbose(gson.toJson(asotrResponse), MODULE);
                        }
                        String openId = asotrResponse.getUserId();
                        if (Debug.verboseOn()) {
                            Debug.logVerbose("openId:" + openId, MODULE);
                        }
                        String sessionKey = asotrResponse.getAccessToken();
                        if (Debug.verboseOn()) {
                            Debug.logVerbose("sessionKey:" + sessionKey, MODULE);
                        }
                        String refreshToken = asotrResponse.getRefreshToken();

                        long expiredTime = System.currentTimeMillis() + 3600 * 1000;
                        List<GenericValue> storeList = new ArrayList<GenericValue>();
                        List<EntityExpr> andConditions = UtilMisc.toList(
                                EntityCondition.makeCondition("openId", openId),
                                EntityCondition.makeCondition("transferType", "alipay"));
                        EntityCondition whereCondition = EntityCondition.makeCondition(andConditions, EntityJoinOperator.AND);
                        GenericValue openIdSessionRel = EntityQuery.use(delegator)
                                                                   .from("OpenIdSessionRel")
                                                                   .where(whereCondition)
                                                                   .cache(false)
                                                                   .queryOne();
                        if (Debug.verboseOn()) {
                            Debug.logVerbose("openIdSessionRel:" + openIdSessionRel, MODULE);
                        }
                        // if no data found by alipay openId, then insert it, else update
                        if (UtilValidate.isEmpty(openIdSessionRel)) {
                            if (Debug.verboseOn()) {
                                Debug.logVerbose("===createOpenIdSessionRel===", MODULE);
                            }
                            GenericValue newOpenIdSessionRel = delegator.makeValue("OpenIdSessionRel", UtilMisc.toMap("openId", openId, "transferType",
                                    "alipay", "sessionKey", sessionKey, "refreshToken", refreshToken, "transferTime", UtilDateTime.nowTimestamp(), "expiredTime", String.valueOf(expiredTime)));
                            delegator.create(newOpenIdSessionRel);
                        } else {
                            if (Debug.verboseOn()) {
                                Debug.logVerbose("===updateOpenIdSessionRel===", MODULE);
                            }
                            openIdSessionRel.set("sessionKey", sessionKey);
                            openIdSessionRel.set("refreshToken", refreshToken);
                            openIdSessionRel.set("transferTime", UtilDateTime.nowTimestamp());
                            openIdSessionRel.set("expiredTime", String.valueOf(expiredTime));
                            storeList.add(openIdSessionRel);
                            delegator.storeAll(storeList);
                        }
                        //TODO load an administrator according to configuration rather than hard coded 'admin'
                        request.setAttribute("openId", openId);
                        GenericValue userLogin = null;
                        GenericValue thirdMemberInfo = EntityQuery.use(delegator)
                                                                  .from("OauthMemberInfo")
                                                                  .where("aliopenId", openId)
                                                                  .cache(false)
                                                                  .queryOne();
                        if (UtilValidate.isNotEmpty(thirdMemberInfo)) {
                            userLogin = EntityQuery.use(delegator)
                                                   .from("UserLogin")
                                                   .where("partyId", thirdMemberInfo.get("partyId"))
                                                   .cache(false)
                                                   .queryOne();
                        }
                        if (!UtilValidate.isNotEmpty(userLogin)) {
                            userLogin = EntityQuery.use(delegator)
                                                   .from("UserLogin")
                                                   .where("userLoginId", "admin")
                                                   .cache(false)
                                                   .queryOne();
                        }
                        doOAuthLogin(userLogin, request);
                    } else {
                        if (Debug.verboseOn()) {
                            Debug.logVerbose("Failed to request Alipay token", MODULE);
                            Debug.logVerbose(gson.toJson(asotrResponse), MODULE);
                        }
                        response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Failed to get alipay openId");
                        return "error";
                    }
                } catch (Exception e) {
                    response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Failed to get alipay openId");
                    Debug.logError(e, MODULE);
                    return "error";
                }
            }
        }

        return "success";
    }

    @SuppressWarnings("unchecked")
    private static AccessTokenImpl getAccessToken(Delegator delegator, GenericValue oauthCodeValue) {
        TicketGrantingTicketImpl tgt = null;
        if (UtilValidate.isNotEmpty(oauthCodeValue.getString("tgtId"))) {
            tgt = getTicketGrantingTicket(delegator, oauthCodeValue.getString("tgtId"));
        }
        Service service = null;
        if (oauthCodeValue.get("service") != null) {
            service = SerializationUtils.deserialize(oauthCodeValue.getBytes("service"), Service.class);
        }
        ExpirationPolicy expirationPolicy = null;
        if (oauthCodeValue.get("expirationPolicy") != null) {
            expirationPolicy = SerializationUtils.deserialize(oauthCodeValue.getBytes("expirationPolicy"), ExpirationPolicy.class);
        }
        Authentication authentication = null;
        if (oauthCodeValue.get("authentication") != null) {
            authentication = SerializationUtils.deserialize(oauthCodeValue.getBytes("authentication"), Authentication.class);
        }
        HashSet<String> scopes = new HashSet<String>();
        if (oauthCodeValue.get("scopes") != null) {
            scopes = SerializationUtils.deserialize(oauthCodeValue.getBytes("scopes"), HashSet.class);
        }
        AccessTokenImpl atImpl = new AccessTokenImpl(oauthCodeValue.getString("ticketId"), service, authentication, expirationPolicy, tgt, scopes);
        setAbstractTicketFields(atImpl, oauthCodeValue);
        return atImpl;
    }

    private static void setAbstractTicketFields(AbstractTicket abstractTicket, GenericValue ticketValue) {
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

    private static TicketGrantingTicketImpl getTicketGrantingTicket(Delegator delegator, String tgtId) {
        Authentication authentication = null;
        GenericValue tgtValue = null;
        try {
            tgtValue = EntityQuery.use(delegator)
                    .from("CasTicketGrantingTicket")
                    .where("tgtId", tgtId)
                    .cache(false)
                    .queryOne();
        } catch (GenericEntityException e) {
            Debug.logError(e, MODULE);
        }
        TicketGrantingTicketImpl tgt = null;
        if (UtilValidate.isNotEmpty(tgtValue)) {
            Debug.logInfo("Found TGT value: " + tgtValue, MODULE);
            if (tgtValue.get("authentication") != null) {
                authentication = SerializationUtils.deserialize(tgtValue.getBytes("authentication"), Authentication.class);
            }
            ExpirationPolicy policy = null;
            if (tgtValue.get("expirationPolicy") != null) {
                policy = SerializationUtils.deserialize(tgtValue.getBytes("expirationPolicy"), ExpirationPolicy.class);
            }
            tgt = new TicketGrantingTicketImpl(tgtValue.getString("tgtId"), authentication, policy);
            setAbstractTicketFields(tgt, tgtValue);
        }
        return tgt;
    }

    private static void doOAuthLogin(GenericValue userLogin, HttpServletRequest request) {
        HttpSession session = request.getSession();
        session.setAttribute("userLogin", userLogin);

        ModelEntity modelUserLogin = userLogin.getModelEntity();
        if (modelUserLogin.isField("partyId")) {
            // if partyId is a field, then we should have these relations defined
            try {
                GenericValue person = userLogin.getRelatedOne("Person", false);
                GenericValue partyGroup = userLogin.getRelatedOne("PartyGroup", false);
                if (person != null) session.setAttribute("person", person);
                if (partyGroup != null) session.setAttribute("partyGroup", partyGroup);
            } catch (GenericEntityException e) {
                Debug.logError(e, "Error getting person/partyGroup info for session, ignoring...", MODULE);
            }
        }

        // let the visit know who the user is
        VisitHandler.setUserLogin(session, userLogin, false);
    }

    /**
     * get请求
     *
     * @return
     */
    public static String doGet(String url) {
        try {
            HttpClient client = HttpClientBuilder.create().build();
            //发送get请求
            HttpGet request = new HttpGet(url);
            HttpResponse response = client.execute(request);
            /**请求发送成功，并得到响应**/
            if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                /**读取服务器返回过来的json字符串数据**/
                String strResult = EntityUtils.toString(response.getEntity());
                return strResult;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }
}

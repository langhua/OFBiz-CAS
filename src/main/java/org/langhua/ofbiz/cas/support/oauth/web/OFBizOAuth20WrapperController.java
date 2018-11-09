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
package org.langhua.ofbiz.cas.support.oauth.web;

import org.apache.http.HttpStatus;
import org.jasig.cas.support.oauth.OAuthConstants;
import org.jasig.cas.support.oauth.OAuthUtils;
import org.jasig.cas.support.oauth.web.BaseOAuthWrapperController;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.Controller;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * This controller is the main entry point for OAuth version 2.0
 * wrapping in CAS, should be mapped to something like /v2/*. Dispatch
 * request to specific controllers : authorize, accessToken...
 *
 */
@Component("ofbizOAuth20WrapperController")
public final class OFBizOAuth20WrapperController extends BaseOAuthWrapperController {

    @Resource(name="authorizeController")
    private Controller authorizeController;

    @Resource(name="callbackAuthorizeController")
    private Controller callbackAuthorizeController;

    @Resource(name="accessTokenController")
    private Controller accessTokenController;

    @Resource(name="ofbizProfileController")
    private Controller ofbizProfileController;

    @Override
    protected ModelAndView internalHandleRequest(final String method, final HttpServletRequest request,
            final HttpServletResponse response) throws Exception {

        // authorize
        if (OAuthConstants.AUTHORIZE_URL.equals(method)) {
            return authorizeController.handleRequest(request, response);
        }
        // callback on authorize
        if (OAuthConstants.CALLBACK_AUTHORIZE_URL.equals(method)) {
            return callbackAuthorizeController.handleRequest(request, response);
        }
        //get access token
        if (OAuthConstants.ACCESS_TOKEN_URL.equals(method)) {
            return accessTokenController.handleRequest(request, response);
        }
        // get profile
        if (OAuthConstants.PROFILE_URL.equals(method)) {
            return ofbizProfileController.handleRequest(request, response);
        }

        // else error
        logger.error("Unknown method : {}", method);
        OAuthUtils.writeTextError(response, OAuthConstants.INVALID_REQUEST, HttpStatus.SC_OK);
        return null;
    }

    public Controller getAuthorizeController() {
        return authorizeController;
    }

    public Controller getCallbackAuthorizeController() {
        return callbackAuthorizeController;
    }

    public Controller getAccessTokenController() {
        return accessTokenController;
    }

    public Controller getProfileController() {
        return ofbizProfileController;
    }
}

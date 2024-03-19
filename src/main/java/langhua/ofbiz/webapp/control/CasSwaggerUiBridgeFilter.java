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

import org.apache.catalina.connector.RequestFacade;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.ofbiz.base.util.Debug;
import org.apache.ofbiz.base.util.StringUtil;
import org.apache.tomcat.util.http.Parameters;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;

public class CasSwaggerUiBridgeFilter extends OncePerRequestFilter {

    private static final String MODULE = CasSwaggerUiBridgeFilter.class.getName();
    private static final Logger logger = LogManager.getLogger();

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        Debug.logInfo("==== Request URI: " + request.getRequestURI(), MODULE);
        logger.debug("  ------------headers----------------");
        request.getHeaderNames().asIterator().forEachRemaining(headerName ->
                logger.debug("  - " + headerName + ": " + request.getHeader(headerName)));
        logger.debug("  ------------parameters----------------");
        request.getParameterNames().asIterator().forEachRemaining(paramter ->
                logger.debug("  - " + paramter + ": " + request.getParameter(paramter)));

        String authHeader = request.getHeader("authorization");
        String grantType = request.getParameter("grant_type");

        if (authHeader != null && ("password".equals(grantType) || "client_credentials".equals(grantType))) {
            try {
                authHeader = authHeader.replaceFirst("^Basic ", "");
                authHeader = new String(Base64.getDecoder().decode(authHeader), StandardCharsets.UTF_8);
                List<String> splits = StringUtil.split(authHeader, ":");
                logger.debug("  - authorization: " + splits);

                if (splits.size() > 0) {
                    String clientId = splits.get(0);
                    if (clientId != null && clientId.length() > 0) {
                        RequestFacade requestFacade = (RequestFacade) request;
                        Field declaredField = requestFacade.getClass().getDeclaredField("request");
                        declaredField.setAccessible(true);

                        Object requestObject = declaredField.get(request);
                        Field coyoteRequest = requestObject.getClass().getDeclaredField("coyoteRequest");
                        coyoteRequest.setAccessible(true);

                        Object cro = coyoteRequest.get(requestObject);
                        Field parametersField = cro.getClass().getDeclaredField("parameters");
                        parametersField.setAccessible(true);

                        Parameters parameters = (Parameters) parametersField.get(cro);
                        parameters.addParameter("client_id", clientId);
                        logger.debug("  - add parameter client_id=" + clientId);

                        if (splits.size() > 1) {
                            String clientSecret = splits.get(1);
                            if (clientSecret != null && clientSecret.length() > 0) {
                                parameters.addParameter("client_secret", clientSecret);
                                logger.debug("  - add parameter client_secret=" + clientSecret);
                            }
                        }
                    }
                }
            } catch (NoSuchFieldException | IllegalAccessException e) {
                Debug.logError(e, MODULE);
            }
        }

        Oauth2ResponseWrapper responseWrapper = new Oauth2ResponseWrapper(response);
        filterChain.doFilter(request, responseWrapper);
        logger.debug("  ------------response----------------");
        logger.debug("  - status: " + responseWrapper.getStatus());

        if (responseWrapper.getStatus() == HttpServletResponse.SC_UNAUTHORIZED) {
            responseWrapper.setHeader("WWW-Authenticate", "SandFlower realm=\"authentication required\"");
        }
    }
}


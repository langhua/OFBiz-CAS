package langhua.ofbiz.webapp.control;

import org.apache.catalina.connector.RequestFacade;
import org.apache.ofbiz.base.util.Debug;
import org.apache.ofbiz.base.util.StringUtil;
import org.apache.tomcat.util.http.MimeHeaders;
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

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        Debug.logInfo("==== Request URI: " + request.getRequestURI(), MODULE);
        String authHeader = request.getHeader("authorization");
        String grantType = request.getParameter("grant_type");

        if (authHeader != null) {
            try {
                RequestFacade requestFacade = (RequestFacade) request;
                Field declaredField = requestFacade.getClass().getDeclaredField("request");
                declaredField.setAccessible(true);

                Object requestObject = declaredField.get(request);
                Field coyoteRequest = requestObject.getClass().getDeclaredField("coyoteRequest");
                coyoteRequest.setAccessible(true);

                Object cro = coyoteRequest.get(requestObject);
                Field headers = cro.getClass().getDeclaredField("headers");
                headers.setAccessible(true);

                MimeHeaders mh = (MimeHeaders) headers.get(cro);
                Debug.logInfo("  - remove [authorization] header with \"" + authHeader + "\"", MODULE);
                mh.removeHeader("authorization");
                Debug.logInfo("  - set [Authorization] header with \"" + authHeader + "\"", MODULE);
                mh.addValue("Authorization").setString(authHeader);

                if (grantType.equals("password")) {
                    authHeader = authHeader.replaceFirst("^Basic ", "");
                    authHeader = new String(Base64.getDecoder().decode(authHeader), StandardCharsets.UTF_8);
                    List<String> splits = StringUtil.split(authHeader, ":");
                    Debug.logInfo("  - authorization: " + splits, MODULE);
                    if (splits.size() > 0) {
                        String clientId = splits.get(0);
                        if (clientId != null && clientId.length() > 0) {
                            Field parametersField = cro.getClass().getDeclaredField("parameters");
                            parametersField.setAccessible(true);
                            Parameters parameters = (Parameters) parametersField.get(cro);
                            parameters.addParameter("client_id", clientId);
                            Debug.logInfo("  - add parameter client_id=" + clientId, MODULE);
                            if (splits.size() > 1) {
                                String clientSecret = splits.get(1);
                                if (clientSecret != null && clientSecret.length() > 0) {
                                    parameters.addParameter("client_secret", clientSecret);
                                    Debug.logInfo("  - add parameter client_secret=" + clientSecret, MODULE);
                                }
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
        if (responseWrapper.getStatus() == HttpServletResponse.SC_UNAUTHORIZED) {
            responseWrapper.setHeader("WWW-Authenticate", "SandFlower realm=\"authentication required\"");
        }
    }
}


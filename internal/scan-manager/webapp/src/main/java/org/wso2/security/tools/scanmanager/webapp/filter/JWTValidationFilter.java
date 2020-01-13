/*
 *  Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.security.tools.scanmanager.webapp.filter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.wso2.security.tools.scanmanager.webapp.exception.ScanManagerWebappException;
import org.wso2.security.tools.scanmanager.webapp.util.Constants;
import org.wso2.security.tools.scanmanager.webapp.util.JwtTokenUtil;

import java.io.IOException;
import java.util.HashMap;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * Filter class to validate JWT token.
 */
@Component
public class JWTValidationFilter implements Filter {

    @Value("${security.key.store.name}")
    private String keyStoreName;

    @Value("${security.key.store.pwd}")
    private String keyStorePwd;

    @Value("${security.key.alias}")
    private String alias;

    @Value("${security.signing.algo}")
    private String algorithm;

    @Value("${jwt.issuer}")
    private String jwtIssuer;

    @Value("${jwt.audience}")
    private String jwtAudience;

    @Value("${jwt.expiry}")
    private String jwtExpiry;

    @Autowired
    @Qualifier("handlerExceptionResolver")
    private HandlerExceptionResolver resolver;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain)
            throws IOException, ServletException {

        String username;
        if (servletRequest instanceof HttpServletRequest) {
            HttpServletRequest request = (HttpServletRequest) servletRequest;
            HttpServletResponse response = (HttpServletResponse) servletResponse;
            HttpSession session = request.getSession();

            try {
                if (session.getAttribute(Constants.USERNAME) == null) {
                    //if the session does not exist
                    String token = request.getHeader(Constants.JWT_HEADER);
                    if (token != null) {
                        String[] tokenArray = token.split("\\.");

                        JwtTokenUtil.validateJWT(tokenArray, getJwtConfigList());
                        //get the username from claims and assign it it session as attribute
                        username = JwtTokenUtil.getUsernameFromJwt(tokenArray[1]);
                        session.setAttribute(Constants.USERNAME, username);

                        filterChain.doFilter(request, response);
                    } else {
                        throw new ScanManagerWebappException("JWT token value cannot be null in the request header.");
                    }
                } else {
                    filterChain.doFilter(request, response);
                }
            } catch (ScanManagerWebappException e) {
                resolver.resolveException(request, response, null, e);
                response.sendRedirect(Constants.ERROR_PAGE);
            }
        }
    }

    @Override
    public void destroy() {
    }

    /**
     * Assign the JWT validation configurations values.
     *
     * @return a map of configuration for JWT validation
     */
    private HashMap<String, String> getJwtConfigList() {
        HashMap<String, String> jwtConfigs = new HashMap<>();
        jwtConfigs.put(Constants.KEY_STORE_NAME, keyStoreName);
        jwtConfigs.put(Constants.KEY_STORE_PWD, keyStorePwd);
        jwtConfigs.put(Constants.JWT_AUDIENCE, jwtAudience);
        jwtConfigs.put(Constants.JWT_ISSUER, jwtIssuer);
        jwtConfigs.put(Constants.ALIAS, alias);
        jwtConfigs.put(Constants.ALGORITHM, algorithm);
        return jwtConfigs;
    }
}

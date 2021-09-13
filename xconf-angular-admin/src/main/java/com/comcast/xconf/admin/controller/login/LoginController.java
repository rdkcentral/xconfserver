/*
 * If not stated otherwise in this file or this component's Licenses.txt file the
 * following copyright and licenses apply:
 *
 * Copyright 2018 RDK Management
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Author: Igor Kostrov
 * Created: 12/10/2018
 */
package com.comcast.xconf.admin.controller.login;

import com.comcast.xconf.auth.AuthResponse;
import com.comcast.xconf.auth.AuthUtils;
import com.comcast.xconf.admin.service.login.LoginService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpServletRequest;
import java.util.Collections;
import java.util.Map;

@Controller
@RequestMapping(LoginController.URL_MAPPING)
public class LoginController {

    private static final Logger log = LoggerFactory.getLogger(LoginController.class);

    public static final String URL_MAPPING = "/ossLoginForm";
    private static final String DEFAULT_REDIRECT_TO = "/ux/";

    private String LOGIN_PAGE = "ossLoginPage";

    @Autowired
    private LoginService loginService;

    @RequestMapping(method = RequestMethod.GET)
    public String loginPage(HttpServletRequest request, final ModelMap model) {
        return LOGIN_PAGE;
    }

    @RequestMapping(method = RequestMethod.POST)
    public String loginPost(HttpServletRequest request, final ModelMap model) {
        String username = request.getParameter("username");
        String password = request.getParameter("password");

        log.info("trying to login - username: " + username);

        AuthResponse response = loginService.authenticate(username, password);
        if (response == null) {
            model.put("error", "username or password is incorrect");
            return LOGIN_PAGE;
        }

        Map<String, String> parameters = Collections.singletonMap("token", AuthUtils.convertIntoToken(response));
        return "redirect:" + AuthUtils.addParametersToUrl(DEFAULT_REDIRECT_TO, parameters);
    }
}
/*******************************************************************************
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
 *******************************************************************************/
package com.comcast.xconf;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.annotation.ResponseStatusExceptionResolver;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
public class ServiceMappingExceptionResolver extends ResponseStatusExceptionResolver {

	private static final Logger log = LoggerFactory
			.getLogger(ServiceMappingExceptionResolver.class);
    {
        setOrder(Ordered.HIGHEST_PRECEDENCE);
    }

	@Override
	protected ModelAndView doResolveException(HttpServletRequest request,
			HttpServletResponse response, Object handler, Exception ex) {

		StringBuilder sb = new StringBuilder();
		sb.append("\n method: ").append(request.getMethod());
		sb.append("\n pathInfo: ").append(request.getPathInfo());
		sb.append("\n pathTranslated: ").append(request.getPathTranslated());
		sb.append("\n queryString: ").append(request.getQueryString());
		sb.append("\n requestURI: ").append(request.getRequestURI());
		sb.append("\n requestURL: ").append(request.getRequestURL());
		sb.append("\n servletPath: ").append(request.getServletPath());

		log.error(sb.toString(), ex);

		return super.doResolveException(request, response, handler, ex);
	}
}
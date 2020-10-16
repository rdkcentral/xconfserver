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
package com.comcast.xconf.util;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.http.ResponseEntity;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.Collection;

/**
 * Created by YSTAGIT on 10/31/2016.
 */

@Aspect
public class LoggingAspect {

    @AfterReturning(pointcut = "execution(* com.comcast.xconf.queries.*.*.get*(..))", returning = "result")
    public void setAttributeAfterReturningGet(JoinPoint joinPoint, Object result) {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
        request.setAttribute("num_results", getNumberResults(result));
    }

    private int getNumberResults(Object result) {
        int number = (result == null) ? 0 : 1;

        //if result is null, the method returns 0
        if (number == 1) {
            if (result instanceof ResponseEntity) {
                result = ((ResponseEntity) result).getBody();
            }

            if (result instanceof Collection) {
                number = ((Collection) result).size();
            }
        }

        return number;
    }

}

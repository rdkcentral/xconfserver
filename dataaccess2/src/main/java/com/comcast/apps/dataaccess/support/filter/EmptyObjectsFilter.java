/*
 * If not stated otherwise in this file or this component's Licenses.txt file the
 * following copyright and licenses apply:
 *
 * Copyright 2019 RDK Management
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
 * @author Alexander Pletnev
 */
package com.comcast.apps.dataaccess.support.filter;

import com.comcast.apps.dataaccess.support.exception.WebAppException;
import org.springframework.http.HttpStatus;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PushbackInputStream;


public class EmptyObjectsFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        final HttpServletRequest bufferedRequest = new HttpServletRequestWrapper(request);

        String requestMethod = bufferedRequest.getMethod().toLowerCase();
        if (requestMethod.equals("post") || requestMethod.equals("put")) {
            new NotEmptyInputStream(bufferedRequest.getInputStream());
        }

        doFilter(bufferedRequest, response, filterChain);
    }

}

class NotEmptyInputStream extends FilterInputStream {
    public NotEmptyInputStream(InputStream in) {
        super(isNotEmpty(in));
    }

    private static InputStream isNotEmpty(InputStream inputStream) {
        PushbackInputStream pushbackInputStream = new PushbackInputStream(inputStream);
        try {
            int size;
            size = pushbackInputStream.read();
            if (size == -1) {
                throw new WebAppException(HttpStatus.BAD_REQUEST);
            }
            pushbackInputStream.unread(size);
        } catch (IOException e) {
            throw new WebAppException(e, HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return pushbackInputStream;
    }
}

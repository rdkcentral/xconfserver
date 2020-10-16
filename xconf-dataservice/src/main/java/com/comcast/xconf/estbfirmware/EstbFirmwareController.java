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
package com.comcast.xconf.estbfirmware;

import com.comcast.apps.hesperius.ruleengine.domain.additional.data.IpAddress;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import static com.comcast.xconf.util.RequestUtil.XCONF_HTTP_HEADER;

/**
 * This is the guy who actually handles firmware requests from the eSTB boxes.
 */
@Controller
public class EstbFirmwareController {

    // xconf/swu/stb?eStbMac=14:D4:FE:55:86:0A&env=PROD&model=PX001ANC

    private static final Logger log = LoggerFactory.getLogger(EstbFirmwareController.class);

    @Resource
    private EstbFirmwareService svc;

    /**
     * This method is what it's all about. The rest of the code in this package
     * lives for this method. What firmware config are we going to return?
     */
    @SuppressWarnings("rawtypes")
    @RequestMapping(value = "/xconf/swu/{applicationType}", produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity swuStb(HttpServletRequest request,
                                 @PathVariable String applicationType,
                                 @RequestHeader(value = XCONF_HTTP_HEADER, required = false) String xconfHttp,
                                 @RequestParam(value = "version", required = false) String version,
                                 @RequestParam MultiValueMap<String, String> params) {

        EstbFirmwareContext context = new EstbFirmwareContext(params);
        context.getContext().set(XCONF_HTTP_HEADER, xconfHttp);
        if (log.isDebugEnabled()) {
            log.debug("context: " + context);
        }
        ResponseEntity response = svc.getSwuResponse(request, context, applicationType, version);
        return response;
    }


    @RequestMapping(value = "/estbfirmware/checkMinimumFirmware", produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity checkMinimumFirmware(@RequestParam MultiValueMap<String, String> params) {

        EstbFirmwareContext context = new EstbFirmwareContext(params);
        ResponseEntity response = svc.getMinimumVersion(context);
        return response;
    }

    /**
     * returns log of the last communication between xconf and the given stb.
     */
    @RequestMapping(value = {"/estbfirmware/lastlog"}, produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity lastlog(@RequestParam("mac") String macAddress) {
        ResponseEntity response = svc.getLastLog(macAddress);
        return response;
    }

    /**
     * returns log of the last communication between xconf and the given stb
     * where xconf instructed stb to get different firmware.
     */
    @RequestMapping(value = {"/estbfirmware/changelogs"}, produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity changelogs(@RequestParam("mac") String macAddress) {
        ResponseEntity response = svc.getChangeLogs(macAddress);
        return response;
    }

    @RequestMapping(value = {"/xconf/swu/bse"}, produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity swuBSE(@RequestParam("ipAddress") IpAddress address) {
        ResponseEntity response = svc.getSwuBSE(address);
        return response;
    }

    @RequestMapping("/xconf/{applicationType}/runningFirmwareVersion/info")
    public ResponseEntity getRunningFirmwareVersion(HttpServletRequest request,
                                                    @PathVariable String applicationType,
                                                    @RequestHeader(value = XCONF_HTTP_HEADER, required = false) String xconfHttp,
                                                    @RequestParam MultiValueMap<String, String> params) {
        EstbFirmwareContext context = new EstbFirmwareContext(params);
        context.getContext().set(XCONF_HTTP_HEADER, xconfHttp);

        return svc.getRunningFirmwareVersion(request, context, applicationType);
    }

    @ExceptionHandler(RuntimeException.class)
    @ResponseBody
    public ResponseEntity<String> runtimeException(final Throwable e) {
        final HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        final StringBuilder errorMessage = new StringBuilder()
                .append("<h2>").append(status).append(" ").append(status.getReasonPhrase())
                .append("</h2><div>").append(e.getMessage()).append("</div>");
        log.error(errorMessage.toString(), e);
        return new ResponseEntity<String>(errorMessage.toString(), status);
    }

    public static class MinimumFirmwareCheckBean {
        public boolean hasMinimumFirmware = true;

        public MinimumFirmwareCheckBean(boolean hasMinimumFirmware) {
            this.hasMinimumFirmware = hasMinimumFirmware;
        }
    }

}

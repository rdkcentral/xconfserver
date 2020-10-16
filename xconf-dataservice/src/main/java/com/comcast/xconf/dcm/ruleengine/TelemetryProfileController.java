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
 * Author: phoenix
 * Created: 04/03/2015  15:08
 */
package com.comcast.xconf.dcm.ruleengine;

import com.comcast.apps.dataaccess.cache.dao.CachedSimpleDao;
import com.comcast.xconf.logupload.telemetry.PermanentTelemetryProfile;
import com.comcast.xconf.logupload.telemetry.PermanentTelemetryProfile.TelemetryProfileDescriptor;
import com.comcast.xconf.logupload.telemetry.TelemetryProfile;
import com.comcast.xconf.logupload.telemetry.TelemetryRule;
import com.comcast.xconf.logupload.telemetry.TimestampedRule;
import com.google.common.base.Preconditions;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import java.io.IOException;
import java.util.List;

import static com.comcast.xconf.logupload.telemetry.TelemetryRule.PermanentTelemetryRuleDescriptor;

@Controller
@RequestMapping(value = "/telemetry")
public class TelemetryProfileController {

    private static final Logger log = LoggerFactory.getLogger(TelemetryProfileController.class);
    private final TelemetryProfileService telemetryService;

    @Autowired
    private CachedSimpleDao<String, PermanentTelemetryProfile> permanentTelemetryDAO;

    @Autowired
    private CachedSimpleDao<String, TelemetryRule> telemetryRuleDAO;

    @Autowired
    private CachedSimpleDao<TimestampedRule, TelemetryProfile> temporaryTelemetryProfileDAO;

    @Autowired
    public TelemetryProfileController(final TelemetryProfileService telemetryProfileService) {
        this.telemetryService = telemetryProfileService;
    }

    @RequestMapping(value = "/create/{contextAttributeName}/{expectedValue}", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity createTelemetryEntryFor(
            @PathVariable("contextAttributeName") final String contextAttributeName,
            @PathVariable("expectedValue") final String expectedValue,
            @RequestBody final TelemetryProfile profile) {
        if(!contextAttributeName.equals("estbMacAddress")) return new ResponseEntity("only estbMacAddress allowed here", HttpStatus.BAD_REQUEST);
        final long now = DateTime.now(DateTimeZone.UTC).getMillis();
        if (now - profile.getExpires() > 0) throw new InvalidTimestampException(profile.getExpires());
        return new ResponseEntity(telemetryService.createTelemetryProfile(contextAttributeName, expectedValue, profile), HttpStatus.OK);
    }

    @RequestMapping(value = "/drop/{contextAttributeName}/{expectedValue}", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public List<TelemetryProfile> dropTelemetryEntryFor(
            @PathVariable("contextAttributeName") final String contextAttributeName,
            @PathVariable("expectedValue") final String expectedValue) {
        return telemetryService.dropTelemetryFor(contextAttributeName, expectedValue);
    }

    @RequestMapping(value = "/getAvailableRuleDescriptors", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public List<PermanentTelemetryRuleDescriptor> getDescriptors(@RequestParam(required = false) String applicationType) {
        return telemetryService.getAvailableDescriptors(applicationType);
    }

    @RequestMapping(value = "/getAvailableTelemetryDescriptors", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public List<TelemetryProfileDescriptor> getTelemetryDescriptors(@RequestParam(required = false) String applicationType) {
        return telemetryService.getAvailableProfileDescriptors(applicationType);
    }

    @RequestMapping(value = "/addTo/{ruleId}/{contextAttributeName}/{expectedValue}/{expires}", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity tempAddToPermanentRule(@PathVariable("contextAttributeName") final String contextAttributeName,
                                       @PathVariable("expectedValue") final String expectedValue,
                                       @PathVariable("ruleId") final String targetRule,
                                       @PathVariable("expires") long expires) {
        if(!contextAttributeName.equals("estbMacAddress")) return new ResponseEntity("only estbMacAddress allowed here", HttpStatus.BAD_REQUEST);
        final TelemetryRule rule = telemetryRuleDAO.getOne(targetRule);
        if (rule == null) {
            throw new NoSuchRuleException();
        }
        final PermanentTelemetryProfile profile = permanentTelemetryDAO.getOne(rule.getBoundTelemetryId());

        final TimestampedRule timedRule = telemetryService.createRuleForAttribute(contextAttributeName, expectedValue);

        profile.setExpires(expires);
        temporaryTelemetryProfileDAO.setOne(timedRule, profile);
        return new ResponseEntity(timedRule, HttpStatus.OK);
    }

    @RequestMapping(value = "/bindToTelemetry/{telemetryId}/{contextAttributeName}/{expectedValue}/{expires}", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity bindToTelemetry(@PathVariable("contextAttributeName") final String contextAttributeName,
                                       @PathVariable("expectedValue") final String expectedValue,
                                       @PathVariable("telemetryId") final String telemetryId,
                                       @PathVariable("expires") long expires) {
        if(!contextAttributeName.equals("estbMacAddress")) return new ResponseEntity("only estbMacAddress allowed here", HttpStatus.BAD_REQUEST);
        final PermanentTelemetryProfile profile = permanentTelemetryDAO.getOne(telemetryId);
        Preconditions.checkNotNull(profile, "no rule found for ID ({}) provided", telemetryId);

        final TimestampedRule timedRule = telemetryService.createRuleForAttribute(contextAttributeName, expectedValue);

        profile.setExpires(expires);

        temporaryTelemetryProfileDAO.setOne(timedRule, profile);
        return ResponseEntity.ok(timedRule);
    }

    @ExceptionHandler
    @ResponseStatus(value = HttpStatus.BAD_REQUEST, reason = "no rule to add to")
    public void handleValidationException(final NoSuchRuleException nsre) {
        log.error("", nsre);
    }

    @ExceptionHandler
    @ResponseBody
    public ResponseEntity handleNPE(final NullPointerException npe) {
        log.error("", npe);
        return new ResponseEntity(npe.getMessage(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(InvalidTimestampException.class)
    @ResponseBody
    public ResponseEntity handleInvalidTimestamp(final InvalidTimestampException ite, final ServletRequest request, final ServletResponse response) {
        final long suppliedMilis = ite.getSuppliedTimeMilis();
        try {
            final StringBuilder sb = new StringBuilder();
            sb.append("Timestamp supplied within TelemetryProfile posted is ")
                    .append(DateTime.now(DateTimeZone.UTC).minus(suppliedMilis).toString())
                    .append("behind current server time,\nwhich is ");
            DateTimeFormat.fullDateTime().printTo(sb, DateTime.now(DateTimeZone.UTC));
            return new ResponseEntity(sb.toString(), HttpStatus.BAD_REQUEST);

        } catch (IOException ioe) {
            log.error("", ioe);
        }
        return null;
    }

    public static class NoSuchRuleException extends IllegalArgumentException {
        @Override
        public synchronized Throwable fillInStackTrace() {
            return this;   // speeding up
        }
    }

    public static class InvalidTimestampException extends IllegalArgumentException {
        private final long suppliedTimeMilis;

        public InvalidTimestampException(long suppliedTimeMilis) {
            super();
            this.suppliedTimeMilis = suppliedTimeMilis;
        }

        @Override
        public synchronized Throwable fillInStackTrace() {
            return this;   // speeding up
        }

        public long getSuppliedTimeMilis() {
            return suppliedTimeMilis;
        }
    }
}

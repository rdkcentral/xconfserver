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
 *  Author: mdolina
 *  Created: 11/20/15 7:03 PM
 */
package com.comcast.xconf.admin.controller.firmware;

import com.comcast.apps.dataaccess.cache.dao.CachedSimpleDao;
import com.comcast.xconf.GenericNamespacedList;
import com.comcast.xconf.estbfirmware.factory.RuleFactory;
import com.comcast.xconf.estbfirmware.report.FirstReport;
import com.comcast.xconf.firmware.FirmwareRule;
import com.comcast.xconf.util.RuleUtil;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@RestController
@RequestMapping(value = FirmwareRuleReportPageController.URL_MAPPING)
public class FirmwareRuleReportPageController {

    public static final String URL_MAPPING = "/api/reportpage";

    @Resource
    private FirstReport rpt;
    @Autowired
    private CachedSimpleDao<String, FirmwareRule> firmwareRuleDao;
    @Autowired
    private CachedSimpleDao<String, GenericNamespacedList> genericNamespacedListDAO;

    @RequestMapping(method = RequestMethod.POST)
    public ResponseEntity getReport(@RequestBody Set<String> macRuleIds, HttpServletResponse response) throws IOException {
        response.setContentType("application/vnd.ms-excel");
        response.setHeader("Content-Disposition", "attachment; filename=report.xls");
        List<FirmwareRule> rules = firmwareRuleDao.getAll(macRuleIds);
        rpt.doReport(getMacAddresses(rules), response.getOutputStream());
        response.flushBuffer();

        return null;
    }

    private Set<String> getMacAddresses(List<FirmwareRule> macRules) {
        Set<String> result = new HashSet<>();
        for (FirmwareRule rule : macRules) {
            if (rule.getRule() != null) {
                List<String> macListIds = RuleUtil.getFixedArgsFromRuleByFreeArgAndOperation(rule.getRule(),
                                                                                    RuleFactory.MAC, RuleFactory.IN_LIST);
                if (CollectionUtils.isNotEmpty(macListIds)) {
                    for (String macListId : macListIds) {
                        GenericNamespacedList macList = genericNamespacedListDAO.getOne(macListId);
                        if (macList != null) {
                            result.addAll(macList.getData());
                        }
                    }
                }
            }
        }

        return result;
    }
}

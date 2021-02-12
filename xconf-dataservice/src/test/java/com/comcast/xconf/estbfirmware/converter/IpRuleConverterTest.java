package com.comcast.xconf.estbfirmware.converter;

import com.comcast.xconf.estbfirmware.IpRuleBean;
import com.comcast.xconf.firmware.FirmwareRule;
import com.comcast.xconf.queries.controllers.BaseQueriesControllerTest;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class IpRuleConverterTest extends BaseQueriesControllerTest {
    @Autowired
    private IpRuleConverter ipRuleConverter;

    @Test
    public void convertIpRuleBeanAndVerify() throws Exception {
        convertAndVerify(createDefaultIpRuleBean());
    }

    private void convertAndVerify(IpRuleBean bean) {
        FirmwareRule firmwareRule = ipRuleConverter.convertIpRuleBeanToFirmwareRule(bean);
        IpRuleBean converted = ipRuleConverter.convertFirmwareRuleToIpRuleBean(firmwareRule);

        Assert.assertEquals(bean, converted);
    }
}

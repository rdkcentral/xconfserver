package com.comcast.xconf.estbfirmware.converter;

import com.comcast.xconf.estbfirmware.EnvModelRuleBean;
import com.comcast.xconf.firmware.FirmwareRule;
import com.comcast.xconf.queries.controllers.BaseQueriesControllerTest;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class EnvModelRuleConverterTest extends BaseQueriesControllerTest {
    @Autowired
    private EnvModelRuleConverter envModelRuleConverter;

    @Test
    public void convertEnvModelRuleBeanAndVerify() throws Exception {
        convertAndVerify(createDefaultEnvModelRuleBean());
    }

    private void convertAndVerify(EnvModelRuleBean bean) {
        FirmwareRule firmwareRule = envModelRuleConverter.convertModelRuleBeanToFirmwareRule(bean);
        EnvModelRuleBean converted = envModelRuleConverter.convertFirmwareRuleToEnvModelRuleBean(firmwareRule);

        Assert.assertEquals(bean, converted);
    }
}

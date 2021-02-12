package com.comcast.xconf.estbfirmware.converter;

import com.comcast.xconf.estbfirmware.PercentageBean;
import com.comcast.xconf.firmware.FirmwareRule;
import com.comcast.xconf.queries.controllers.BaseQueriesControllerTest;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static com.comcast.xconf.firmware.ApplicationType.STB;

public class PercentageBeanConverterTest extends BaseQueriesControllerTest {
    @Autowired
    private PercentageBeanConverter percentageBeanConverter;

    @Test
    public void convertPercentageBeanAndVerify() throws Exception {
        convertAndVerify(createPercentageBean(STB));
    }

    private void convertAndVerify(PercentageBean bean) {
        FirmwareRule firmwareRule = percentageBeanConverter.convertIntoRule(bean);
        PercentageBean converted = percentageBeanConverter.convertIntoBean(firmwareRule);

        Assert.assertEquals(bean, converted);
    }
}

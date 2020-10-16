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
 * Author: slavrenyuk
 * Created: 6/18/14
 */
package com.comcast.xconf.logupload;

import com.comcast.apps.hesperius.ruleengine.domain.additional.data.IpAddressGroup;
import com.comcast.xconf.NamespacedList;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.Random;
import java.util.Set;

public class DCMRule {

    private static final Logger log = LoggerFactory.getLogger(DCMRule.class);
    private static final ExpressionParser parser = new SpelExpressionParser();

    private final Formula formula;
    private IpAddressGroup ipAddressGroup;
    private NamespacedList estbMacList;
    private NamespacedList ecmMacList;
    private final Settings settings;

    public DCMRule(Formula formula, IpAddressGroup ipAddressGroup, NamespacedList estbMacList, NamespacedList ecmMacList, Settings settings) {
        this.formula = formula;
        this.ipAddressGroup = ipAddressGroup;
        this.estbMacList = estbMacList;
        this.ecmMacList = ecmMacList;
        this.settings = settings;
    }

    public void apply(LogUploaderContext context, Settings output) {

        String ruleExpressionLocal = " " + formula.getRuleExpression() + " ";

        Boolean fieldEvaluationResult = ipAddressGroup != null && ipAddressGroup.isInRange(context.getEstbIP());
        ruleExpressionLocal = ruleExpressionLocal.replaceAll("estbIP", fieldEvaluationResult.toString());

        fieldEvaluationResult = estbMacList != null && estbMacList.getData() != null && estbMacList.getData().contains(context.getEstbMacAddress());
        ruleExpressionLocal = ruleExpressionLocal.replaceAll("estbMacAddress", fieldEvaluationResult.toString());

        fieldEvaluationResult = ecmMacList != null && ecmMacList.getData() != null && ecmMacList.getData().contains(context.getEcmMacAddress());
        ruleExpressionLocal = ruleExpressionLocal.replaceAll("ecmMacAddress", fieldEvaluationResult.toString());

        for (Map.Entry<String, Set<String>> entry : formula.getProperties().entrySet()) {

            String propName = entry.getKey();
            Set<String> propValue = entry.getValue();
            if (propValue == null || propValue.isEmpty()) {
                continue;
            }

            fieldEvaluationResult = false;
            for (String element : propValue) {
                String contextValue = context.getProperties().get(propName);
                if (contextValue != null && FilenameUtils.wildcardMatch(contextValue, element)) {
                    fieldEvaluationResult = true;
                    break;
                }
            }
            ruleExpressionLocal = ruleExpressionLocal.replaceAll(propName, fieldEvaluationResult.toString());
        }

        if (ruleExpressionLocal != null) {
            ruleExpressionLocal = ruleExpressionLocal.replaceAll(" NOT "," ! ");
            try {
                if (!parser.parseExpression(ruleExpressionLocal).getValue(Boolean.class)) {
                    return;
                }
            } catch (Exception  e) {
                log.warn("Rule expression evaluation for DCMRule with id=" + formula.getId() + " has failed");
            }
        }

        if((output.getGroupName() == null || output.getLusName() == null) && settings.getGroupName() != null) {

            output.copyDeviceSettings(settings);

            //if timeWindow is 0 then return non-random cron expression.
            //Randomize getSettings request time cron expression.This shall return random cron expression for each request, with in the range of initial cron and time window.
            String deviceSettingsCron = randomizeCronIfNecessary(output.getScheduleCron(), output.getScheduleDurationMinutes(), false, context.getEstbMacAddress(), "deviceSettingsCronExpression");
            if (StringUtils.isNotEmpty(deviceSettingsCron)) {
                output.setScheduleCron(deviceSettingsCron);
            }
            output.addRuleID(formula.getId());
            log.info("Received attributes from device: " + formula.toStringOnlyBaseProperties() + "  Applied rule for Device Settings: " + this.toString());

            // apply log upload settings
            output.setLusScheduleCron(null);
            output.setLusScheduleCronL1(null);
            output.setLusScheduleCronL2(null);
            output.setLusScheduleCronL3(null);

            boolean lusSettingsCopied = false;

            double randomPercentage = Math.random() * 100;
            if (randomPercentage <= formula.getPercentage()) {
                log.debug("This request has " + randomPercentage + " percentage number, which is less or equal to " + formula.getPercentage() + ". Log upload settings will be returned.");
                output.copyLusSetting(settings, true);
                output.setLusScheduleCron(settings.getLusScheduleCron());
                lusSettingsCopied = true;

                output.addRuleID(formula.getId());
                log.info("Received attributes from device: " + formula.toStringOnlyBaseProperties() + "  Applied rule for Log Upload Settings: " + this.toString());

            } else {
                log.debug("This request has " + randomPercentage + " percentage number, which is greater then " + formula.getPercentage() + ". Log upload settings will NOT be returned.");
                output.copyLusSetting(settings, false);

            }

            boolean isDayRandomized = "Whole Day Randomized".equals(settings.getSchedulerType());
            //XCONF-203 Randomize log upload time cron expression.This shall return random cron expression for each request ,with in the range of initial cron and time window.
            //if timeWindow is 0 then return non-random cron expression.
            String randomCronExp = randomizeCronIfNecessary(output.getLusScheduleCron(), output.getLusScheduleDurationMinutes(), isDayRandomized, context.getEstbMacAddress(), "logUploadCronTime");
            if (StringUtils.isNotEmpty(randomCronExp)) {
                output.setLusScheduleCron(randomCronExp);
            }

            int p1 = (formula.getPercentageL1() != null) ? formula.getPercentageL1() : 0;
            int p2 = (formula.getPercentageL2() != null) ? formula.getPercentageL2() : 0;
            int p3 = (formula.getPercentageL3() != null) ? formula.getPercentageL3() : 0;

            randomPercentage = Math.random() * 100;
            if (randomPercentage <= p1) {
                String lusScheduleCron = settings.getLusScheduleCronL1();
                String randomCron = randomizeCronIfNecessary(lusScheduleCron, settings.getLusScheduleDurationMinutes(), isDayRandomized, context.getEstbMacAddress(), "logUploadCronL1");
                output.setLusScheduleCronL1(StringUtils.isNotEmpty(randomCron) ? randomCron : lusScheduleCron);

            } else if (randomPercentage <= p1 + p2) {
                String lusScheduleCron = settings.getLusScheduleCronL2();
                String randomCron = randomizeCronIfNecessary(lusScheduleCron, settings.getLusScheduleDurationMinutes(), isDayRandomized, context.getEstbMacAddress(), "logUploadCronL2");
                output.setLusScheduleCronL2(StringUtils.isNotEmpty(randomCron) ? randomCron : lusScheduleCron);

            } else if (randomPercentage <= p1 + p2 + p3) {
                String lusScheduleCron = settings.getLusScheduleCronL3();
                String randomCron = randomizeCronIfNecessary(lusScheduleCron, settings.getLusScheduleDurationMinutes(), isDayRandomized, context.getEstbMacAddress(), "logUploadCronL3");
                output.setLusScheduleCronL3(StringUtils.isNotEmpty(randomCron) ? randomCron : lusScheduleCron);
            }

            if (!lusSettingsCopied && randomPercentage <= p1+p2+p3) {
                output.copyLusSetting(settings, true);

                output.addRuleID(formula.getId());
                log.info("Received attributes from device: " + formula.toStringOnlyBaseProperties() + "  Applied rule for Log Upload Settings: " + this.toString());
            }
        }

        if(output.getVodSettingsName() == null && settings.getVodSettingsName() != null) {
            output.copyVodSettings(settings);
            output.addRuleID(formula.getId());
            log.info("Received attributes from device: " + formula.toStringOnlyBaseProperties() + "  Applied rule for VOD settings: " + this.toString());
        }
    }

    private String randomizeCronIfNecessary(String expression, Integer timeWindow, boolean isDayRandomized, String estbMac, String cronName) {
        String randomCronExp = "";
        if (isDayRandomized || (StringUtils.isNotEmpty(expression) && timeWindow != null && timeWindow > 0)) {
            randomCronExp = randomizeCronEx(expression,timeWindow, isDayRandomized);
            if(StringUtils.isEmpty(randomCronExp)) {
                log.error("Invalid {}={} for estbMac={}", cronName, expression, estbMac);
            } else {
                String date = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(new Date());
                //log.info("original logUploadCronTime=" + expression + " randomized logUploadCronTime=" + randomCronExp + " for estbMac=" + estbMac + " at dcmTime=" + date);
                log.info("original {}={} randomized {}={} for estbMac={} at dcmTime={}", cronName, expression, cronName, randomCronExp, estbMac, date);
            }

        }
        return randomCronExp;
    }

    /**
     * Randomize the cron expression between the cron expression and upper bound as timeWindow.
     * Also depending on type random range is fixed.
     * @param expression   cron expression.
     * @param timeWindow   upper bound.
     * @param isDayRandomized DayRandomized/cron expression.
     * @return  String randomized cron expression.
     */
    private String randomizeCronEx(String expression, Integer timeWindow, boolean isDayRandomized) {
        String[] expressionArray = new String[] {"0", "0", "*", "*", "*"};
        int lowerMinutes = 0;
        int lowerHour = 0;
        int randomNumber;
        if(isDayRandomized) {
            randomNumber = new Random().nextInt(1440);
        }
        else {
            if(!validate(expression)) {
                return "";
            }
            expressionArray = expression.split(" ");
            lowerMinutes = Integer.parseInt(expressionArray[0]);
            lowerHour = Integer.parseInt(expressionArray[1]);
            randomNumber = new Random().nextInt(timeWindow);
        }
        //Get next random hour and random minute
        int newMin = lowerMinutes + randomNumber;
        //To tackle midnight boundaries.
        //If Minutes>= 60 extract out hour and add it to new hour value.
        //Being division and mod operators it will take care of while conditions,and remainder value shall
        //always be less than 60 for minutes and less than 24 or 0 for hours.
        int newHr = newMin / 60;
        newMin = newMin % 60;
        //If new hour value is >=24 i.e.  at 00 am or more then convert to AM values i.e. 0,1 etc
        newHr = lowerHour + newHr;
        newHr = newHr % 24;
        //As per ticket only hour and day need to be considered.
        StringBuilder builder = new StringBuilder() ;
        builder.append(Integer.toString(newMin)).append(" ").append(Integer.toString(newHr));
        for(int i = 2; i < expressionArray.length; i++)
        {
            builder.append(" ").append(expressionArray[i]) ;
        }

        return builder.toString();
    }

    /**
     * Validates hours and minutes section of  the cron expression.Ideally at the time of entering these details by the user it should be validated.
     *
     * @param expression  Cron expression.
     * @return  boolean for validation.
     */
    static boolean validate(String expression) {
        try
        {
            String[] split = expression.split(" ");
            if (split.length < 2) {
                return false;
            }
            int minutes = Integer.parseInt(split[0]);
            int hour = Integer.parseInt(split[1]);
            if (minutes <0 || hour <0)
                return false;
            return true;
        }
        catch(NumberFormatException ex)
        {
            log.error("Invalid cron expression:"+expression);
        }
        return false;
    }
}

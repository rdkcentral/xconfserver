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
 * Created: 4/29/14
 */
package com.comcast.xconf.logupload;


import org.hibernate.validator.constraints.NotBlank;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

public class Schedule {

    @NotNull
    private String type;

    /**
     * Cron expression for level one
     */
    @NotBlank
    private String expression;

    private String timeZone;

    private String expressionL1;
    private String expressionL2;
    private String expressionL3;

    @NotNull
    @Min(value=0)
    private Integer timeWindowMinutes;

    private String startDate;

    private String endDate;

    public Schedule() {
    }

    public Schedule(String type,
                    String expression,
                    String expressionL1,
                    String expressionL2,
                    String expressionL3,
                    Integer timeWindowMinutes,
                    String startDate,
                    String endDate,
                    String timeZone) {
        this.type = type;
        this.expression = expression;
        this.expressionL1 = expressionL1;
        this.expressionL2 = expressionL2;
        this.expressionL3 = expressionL3;
        this.timeWindowMinutes = timeWindowMinutes;
        this.startDate = startDate;
        this.endDate = endDate;
        this.timeZone = timeZone;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getExpression() {
        return expression;
    }

    public void setExpression(String expression) {
        this.expression = expression;
    }

    public Integer getTimeWindowMinutes() {
        return timeWindowMinutes;
    }

    public void setTimeWindowMinutes(Integer timeWindowMinutes) {
        this.timeWindowMinutes = timeWindowMinutes;
    }

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

    public String getExpressionL1() {
        return expressionL1;
    }

    public void setExpressionL1(String expressionL1) {
        this.expressionL1 = expressionL1;
    }

    public String getExpressionL2() {
        return expressionL2;
    }

    public void setExpressionL2(String expressionL2) {
        this.expressionL2 = expressionL2;
    }

    public String getExpressionL3() {
        return expressionL3;
    }

    public void setExpressionL3(String expressionL3) {
        this.expressionL3 = expressionL3;
    }

    public String getTimeZone() {
        return timeZone;
    }

    public void setTimeZone(String timeZone) {
        this.timeZone = timeZone;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Schedule schedule = (Schedule) o;

        if (type != null ? !type.equals(schedule.type) : schedule.type != null) return false;
        if (expression != null ? !expression.equals(schedule.expression) : schedule.expression != null) return false;
        if (timeZone != null ? !timeZone.equals(schedule.timeZone) : schedule.timeZone != null) return false;
        if (expressionL1 != null ? !expressionL1.equals(schedule.expressionL1) : schedule.expressionL1 != null)
            return false;
        if (expressionL2 != null ? !expressionL2.equals(schedule.expressionL2) : schedule.expressionL2 != null)
            return false;
        if (expressionL3 != null ? !expressionL3.equals(schedule.expressionL3) : schedule.expressionL3 != null)
            return false;
        if (timeWindowMinutes != null ? !timeWindowMinutes.equals(schedule.timeWindowMinutes) : schedule.timeWindowMinutes != null)
            return false;
        if (startDate != null ? !startDate.equals(schedule.startDate) : schedule.startDate != null) return false;
        return endDate != null ? endDate.equals(schedule.endDate) : schedule.endDate == null;

    }

    @Override
    public int hashCode() {
        int result = type != null ? type.hashCode() : 0;
        result = 31 * result + (expression != null ? expression.hashCode() : 0);
        result = 31 * result + (timeZone != null ? timeZone.hashCode() : 0);
        result = 31 * result + (expressionL1 != null ? expressionL1.hashCode() : 0);
        result = 31 * result + (expressionL2 != null ? expressionL2.hashCode() : 0);
        result = 31 * result + (expressionL3 != null ? expressionL3.hashCode() : 0);
        result = 31 * result + (timeWindowMinutes != null ? timeWindowMinutes.hashCode() : 0);
        result = 31 * result + (startDate != null ? startDate.hashCode() : 0);
        result = 31 * result + (endDate != null ? endDate.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("Schedule{");
        sb.append("type='").append(type).append('\'');
        sb.append(", expression='").append(expression).append('\'');
        sb.append(", timeZone='").append(timeZone).append('\'');
        sb.append(", expressionL1='").append(expressionL1).append('\'');
        sb.append(", expressionL2='").append(expressionL2).append('\'');
        sb.append(", expressionL3='").append(expressionL3).append('\'');
        sb.append(", timeWindowMinutes=").append(timeWindowMinutes);
        sb.append(", startDate='").append(startDate).append('\'');
        sb.append(", endDate='").append(endDate).append('\'');
        sb.append('}');
        return sb.toString();
    }
}

package com.comcast.xconf.search.telemetry;

import com.comcast.xconf.logupload.telemetry.TelemetryTwoRule;
import com.comcast.xconf.search.ContextOptional;
import com.comcast.xconf.search.XRulePredicates;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.function.Predicate;

@Service
public class TelemetryTwoRulePredicates extends XRulePredicates<TelemetryTwoRule> {

    public List<Predicate<TelemetryTwoRule>> getPredicates(ContextOptional context) {
        return getBaseRulePredicates(context);
    }
}
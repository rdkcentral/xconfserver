package com.comcast.xconf.search.telemetry;

import com.comcast.xconf.logupload.telemetry.TelemetryTwoProfile;
import com.comcast.xconf.search.ContextOptional;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

@Service
public class TelemetryTwoProfilePredicates {

    public Predicate<TelemetryTwoProfile> byName(String name) {
        return profile -> Objects.nonNull(profile)
                && StringUtils.containsIgnoreCase(profile.getName(), name);
    }

    public List<Predicate<TelemetryTwoProfile>> getPredicates(ContextOptional context) {
        List<Predicate<TelemetryTwoProfile>> predicates = new ArrayList<>();

        context.getName().ifPresent(name -> predicates.add(byName(name)));

        return predicates;
    }
}
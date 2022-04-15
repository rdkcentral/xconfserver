package com.comcast.xconf.service.change;

import com.comcast.apps.dataaccess.util.CloneUtil;
import com.comcast.xconf.change.Change;
import com.comcast.xconf.logupload.telemetry.PermanentTelemetryProfile;
import com.comcast.xconf.logupload.telemetry.TelemetryProfile;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class TelemetryProfileChangeUtils {

    public static PermanentTelemetryProfile mergeTelemetryProfileChange(PermanentTelemetryProfile mergeResult, Change<PermanentTelemetryProfile> change) {
        if (mergeResult == null) {
            return CloneUtil.clone(change.getNewEntity());
        }
        PermanentTelemetryProfile oldProfile = change.getOldEntity();
        PermanentTelemetryProfile updatedProfile = change.getNewEntity();
        if (!StringUtils.equals(oldProfile.getName(), updatedProfile.getName())) {
            mergeResult.setName(updatedProfile.getName());
        }
        if (!StringUtils.equals(oldProfile.getSchedule(), updatedProfile.getSchedule())) {
            mergeResult.setSchedule(updatedProfile.getSchedule());
        }
        if (!Objects.equals(oldProfile.getUploadProtocol(), updatedProfile.getUploadProtocol())) {
            mergeResult.setUploadProtocol(updatedProfile.getUploadProtocol());
        }
        if (!StringUtils.equals(oldProfile.getUploadRepository(), updatedProfile.getUploadRepository())) {
            mergeResult.setUploadRepository(updatedProfile.getUploadRepository());
        }
        return applyTelemetryElementChanges(change, mergeResult);
    }

    private static PermanentTelemetryProfile applyTelemetryElementChanges(Change<PermanentTelemetryProfile> change, PermanentTelemetryProfile mergeResult) {
        List<TelemetryProfile.TelemetryElement> oldTelemetryElements = change.getOldEntity().getTelemetryProfile();
        List<TelemetryProfile.TelemetryElement> updatedTelemetryElements = change.getNewEntity().getTelemetryProfile();
        for (TelemetryProfile.TelemetryElement updated : updatedTelemetryElements) {
            TelemetryProfile.TelemetryElement old = findTelemetryElementById(updated.getId(), oldTelemetryElements);
            TelemetryProfile.TelemetryElement merged = findTelemetryElementById(updated.getId(), mergeResult.getTelemetryProfile());
            if (isNewElement(updated) || removedBefore(old, updated, merged)) {
                mergeResult.getTelemetryProfile().add(updated);
                continue;
            }
            applyTelemetryElementChange(merged, old, updated);
        }
        removeTelemetryElementsFromMergeResult(getRemovedTelemetryElementIds(oldTelemetryElements, updatedTelemetryElements), mergeResult);
        return mergeResult;
    }

    private static boolean isNewElement(TelemetryProfile.TelemetryElement telemetryElement) {
        return StringUtils.isBlank(telemetryElement.getId()) && telemetryElement != null;
    }

    private static boolean removedBefore(TelemetryProfile.TelemetryElement old, TelemetryProfile.TelemetryElement updated, TelemetryProfile.TelemetryElement merged) {
        return !Objects.equals(old, updated) && merged == null;
    }

    private static void applyTelemetryElementChange(TelemetryProfile.TelemetryElement mergedElement, TelemetryProfile.TelemetryElement oldElement, TelemetryProfile.TelemetryElement newElement) {
        if (oldElement != null && mergedElement != null) {
            if (!StringUtils.equals(oldElement.getHeader(), newElement.getHeader())) {
                mergedElement.setHeader(newElement.getHeader());
            }
            if (!StringUtils.equals(oldElement.getContent(),newElement.getContent())) {
                mergedElement.setContent(newElement.getContent());
            }
            if (!StringUtils.equals(oldElement.getType(), newElement.getType())) {
                mergedElement.setType(newElement.getType());
            }
            if (!StringUtils.equals(oldElement.getPollingFrequency(), newElement.getPollingFrequency())) {
                mergedElement.setPollingFrequency(newElement.getPollingFrequency());
            }
        }
    }

    private static void removeTelemetryElementsFromMergeResult(List<String> idsToRemove, PermanentTelemetryProfile mergeResult) {
        for (String id : idsToRemove) {
            TelemetryProfile.TelemetryElement telemetryElementToRemove = findTelemetryElementById(id, mergeResult.getTelemetryProfile());
            mergeResult.getTelemetryProfile().remove(telemetryElementToRemove);
        }
    }

    private static List<String> getRemovedTelemetryElementIds(List<TelemetryProfile.TelemetryElement> oldElements, List<TelemetryProfile.TelemetryElement> newElements) {
        List<String> removedElements = new ArrayList<>();
        for (TelemetryProfile.TelemetryElement oldElement : oldElements) {
            if (findTelemetryElementById(oldElement.getId(), newElements) == null) {
                removedElements.add(oldElement.getId());
            }
        }
        return removedElements;
    }

    private static TelemetryProfile.TelemetryElement findTelemetryElementById(String id, List<TelemetryProfile.TelemetryElement> telemetryElements) {
        for (TelemetryProfile.TelemetryElement telemetryElement : telemetryElements) {
            if (StringUtils.equals(id, telemetryElement.getId())) {
                return telemetryElement;
            }
        }
        return null;
    }
}

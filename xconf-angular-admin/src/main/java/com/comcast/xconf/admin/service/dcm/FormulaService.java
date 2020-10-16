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
 * Author: Stanislav Menshykov
 * Created: 09.11.15  13:12
 */
package com.comcast.xconf.admin.service.dcm;

import com.comcast.apps.dataaccess.cache.dao.CachedSimpleDao;
import com.comcast.apps.dataaccess.support.exception.ValidationRuntimeException;
import com.comcast.hydra.astyanax.data.IPersistable;
import com.comcast.xconf.admin.validator.dcm.FormulaValidator;
import com.comcast.xconf.dcm.ruleengine.LogFileService;
import com.comcast.xconf.importing.OverwriteWrapper;
import com.comcast.xconf.logupload.*;
import com.comcast.xconf.permissions.DcmPermissionService;
import com.comcast.xconf.permissions.PermissionService;
import com.comcast.xconf.priority.PriorityUtils;
import com.comcast.xconf.search.ContextOptional;
import com.comcast.xconf.search.dcm.FormulaPredicates;
import com.comcast.xconf.shared.domain.EntityMessage;
import com.comcast.xconf.shared.service.AbstractApplicationTypeAwareService;
import com.comcast.xconf.shared.service.AbstractService;
import com.comcast.xconf.util.RuleUtil;
import com.comcast.xconf.validators.IValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.function.Predicate;

import static com.comcast.xconf.admin.core.Utils.nullifyUnwantedFields;

@Component
public class FormulaService extends AbstractApplicationTypeAwareService<DCMGenericRule> {

    private static final Logger log = LoggerFactory.getLogger(FormulaService.class);

    @Autowired
    private CachedSimpleDao<String, DCMGenericRule> dcmRuleDAO;

    @Autowired
    private CachedSimpleDao<String, VodSettings> vodSettingsDAO;

    @Autowired
    private CachedSimpleDao<String, DeviceSettings> deviceSettingsDAO;

    @Autowired
    private CachedSimpleDao<String, LogUploadSettings> logUploadSettingsDAO;

    @Autowired
    private LogFileService indexesLogFilesDAO;

    @Autowired
    private DeviceSettingsService deviceSettingsService;

    @Autowired
    private LogUploadSettingsService logUploadSettingsService;

    @Autowired
    private VodSettingsService vodSettingsService;

    @Autowired
    private FormulaValidator formulaValidator;

    @Autowired
    private FormulaPredicates formulaPredicates;

    @Autowired
    private DcmPermissionService permissionService;

    @Override
    public PermissionService getPermissionService() {
        return permissionService;
    }

    public List<String> getFormulasNames() {
        List<String> result = new ArrayList<>();
        for (DCMGenericRule entity : getAll()) {
            result.add(entity.getName());
        }

        return result;
    }

    @Override
    public DCMGenericRule create(DCMGenericRule formula) {
        beforeCreating(formula);
        beforeSaving(formula);
        saveAll(PriorityUtils.addNewItemAndReorganize(formula, getAll()));
        return formula;
    }

    @Override
    public DCMGenericRule update(DCMGenericRule formula) {
        beforeUpdating(formula);
        beforeSaving(formula);
        DCMGenericRule formulaToUpdate = getEntityDAO().getOne(formula.getId());
        saveAll(PriorityUtils.updateItemByPriorityAndReorganize(formula, getAll(), formulaToUpdate.getPriority()));
        return formula;
    }

    @Override
    public void normalizeOnSave(DCMGenericRule formula) {
        RuleUtil.normalizeConditions(formula);
    }

    public List<DCMGenericRule> changePriorities(String formulaId, Integer newPriority) {
        final DCMGenericRule formulaToUpdate = getOne(formulaId);
        Integer oldPriority = formulaToUpdate.getPriority();
        List<DCMGenericRule> reorganizedFormulas = PriorityUtils.updatePriorities(getAll(), formulaToUpdate.getPriority(), newPriority);
        saveAll(reorganizedFormulas);
        log.info("Priority of Formula " + formulaId + " has been changed, oldPriority=" + oldPriority + ", newPriority=" + newPriority);
        return reorganizedFormulas;
    }

    @Override
    public DCMGenericRule delete(final String id) {

        /**
         * since the DeviceSettins, LogUploadSettings and VodSettings are associated with DCMGenericRule,
         * we should remove also DeviceSettins, LogUploadSettings and VodSettings with the same ID
         */
        DCMGenericRule dcmGenericRule = super.delete(id);

        try {
            vodSettingsDAO.deleteOne(id);
        } catch (Exception e) {
            log.error("Failed to delete VodSettings with id=" + id + "  ", e);
        }
        try {
            deviceSettingsDAO.deleteOne(id);
        } catch (Exception e) {
            log.error("Failed to delete DeviceSettings with id=" + id + "  ", e);
        }
        try {
            logUploadSettingsDAO.deleteOne(id);
        } catch (Exception e) {
            log.error("Failed to delete LogUploadSettings with id=" + id + "  ", e);
        }
        try{
            //removing LogFiles associated to LogUploadSettings
            indexesLogFilesDAO.deleteAll(id);
        } catch (Exception e) {
            log.error("Failed to delete LogUploadSettings with id=" + id + "  ", e);
        }

        saveAll(PriorityUtils.packPriorities(getAll()));

        return dcmGenericRule;
    }

    public DCMRuleWithSettings getFormulaForExport(final String id) {
        final DCMRuleWithSettings result = new DCMRuleWithSettings(
                dcmRuleDAO.getOne(id),
                deviceSettingsDAO.getOne(id),
                logUploadSettingsDAO.getOne(id),
                vodSettingsDAO.getOne(id)
        );

        return nullifyUnwantedFields(result);
    }

    public List<DCMRuleWithSettings> getAllFormulasForExport() {
        final List<DCMRuleWithSettings> result = new ArrayList<>();
        for (DCMGenericRule formula : getAll()) {
            DCMRuleWithSettings dcmRuleWithSettings = new DCMRuleWithSettings(formula,
                    deviceSettingsDAO.getOne(formula.getId()),
                    logUploadSettingsDAO.getOne(formula.getId()),
                    vodSettingsDAO.getOne(formula.getId()));

            result.add(nullifyUnwantedFields(dcmRuleWithSettings));
        }

        return result;
    }

    public Map<String, Object> importFormulas(List<DCMRuleWithSettings> dcmRuleWithSettings, boolean overwrite) {
        Map<String, Object> entitiesMap = new HashMap<>();
        for (DCMRuleWithSettings dcmRuleWithSetting : dcmRuleWithSettings) {
            String id = dcmRuleWithSetting.getFormula().getId();
            try {
                importFormula(dcmRuleWithSetting, overwrite);
                entitiesMap.put(id, new EntityMessage(EntityMessage.ENTITY_STATUS.SUCCESS, id));
            } catch (Exception e) {
                entitiesMap.put(id, new EntityMessage(EntityMessage.ENTITY_STATUS.FAILURE, e.getMessage()));
            }
        }
        return entitiesMap;
    }

    public DCMRuleWithSettings importFormula(DCMRuleWithSettings formulaWithSettings, Boolean overwrite) {
        final DCMGenericRule formula = formulaWithSettings.getFormula();
        final DeviceSettings deviceSettings = formulaWithSettings.getDeviceSettings();
        final LogUploadSettings logUploadSettings = formulaWithSettings.getLogUploadSettings();
        final VodSettings vodSettings = formulaWithSettings.getVodSettings();

        if (deviceSettings != null) {
            deviceSettingsService.validateOnSave(deviceSettings);
        }

        if (logUploadSettings != null) {
            logUploadSettingsService.validateOnSave(logUploadSettings);
        }

        if (vodSettings != null) {
            vodSettingsService.validateOnSave(vodSettings);
        }

        if (formula == null) {
            throw new ValidationRuntimeException("Formula must be not empty");
        }

        if (overwrite) {
            update(formula);
        } else {
            create(formula);
        }

        saveEntity(deviceSettingsService, deviceSettings, overwrite);
        saveEntity(logUploadSettingsService, logUploadSettings, overwrite);
        saveEntity(vodSettingsService, vodSettings, overwrite);

        return formulaWithSettings;
    }

    public Map<String, List<String>> importFormulas(List<OverwriteWrapper<DCMRuleWithSettings>> wrappedFormulasWithSettings) {
        final List<String> failedToImport = new ArrayList<>();
        final List<String> successfulImportIds = new ArrayList<>();
        Collections.sort(wrappedFormulasWithSettings, new Comparator<OverwriteWrapper<DCMRuleWithSettings>>() {
            @Override
            public int compare(OverwriteWrapper<DCMRuleWithSettings> left, OverwriteWrapper<DCMRuleWithSettings> right) {
                int leftPriority = (left != null && left.getEntity() != null &&
                        left.getEntity().getFormula() != null && left.getEntity().getFormula().getPriority() != null) ?
                        left.getEntity().getFormula().getPriority() : 0;
                int rightPriority = (right != null && right.getEntity() != null &&
                        right.getEntity().getFormula() != null && right.getEntity().getFormula().getPriority() != null) ?
                        right.getEntity().getFormula().getPriority() : 0;

                return leftPriority - rightPriority;
            }
        });

        for (OverwriteWrapper<DCMRuleWithSettings> wrappedFormulaWithSettings : wrappedFormulasWithSettings) {
            DCMRuleWithSettings currentFormulaWithSettings = wrappedFormulaWithSettings.getEntity();
            String currentTemplateId = currentFormulaWithSettings.getFormula().getId();
            try {
                importFormula(currentFormulaWithSettings, wrappedFormulaWithSettings.getOverwrite());
                successfulImportIds.add(currentTemplateId);
            } catch (RuntimeException e) {
                failedToImport.add(e.getMessage());
            }
        }

        return new HashMap<String, List<String>>(){{
            put("success", successfulImportIds);
            put("failure", failedToImport);
        }};
    }

    private void saveAll(List<DCMGenericRule> formulasList) {
        for (DCMGenericRule formula : formulasList) {
            getEntityDAO().setOne(formula.getId(), formula);
        }
    }

    private void saveEntity(AbstractService service, IPersistable persistable, boolean overwrite) {
        if (persistable != null) {
            if (overwrite) {
                service.update(persistable);
            } else {
                service.create(persistable);
            }
        }
    }

    @Override
    public CachedSimpleDao<String, DCMGenericRule> getEntityDAO() {
        return dcmRuleDAO;
    }

    @Override
    public IValidator<DCMGenericRule> getValidator() {
        return formulaValidator;
    }

    @Override
    protected List<Predicate<DCMGenericRule>> getPredicatesByContext(Map<String, String> context) {
        return formulaPredicates.getPredicates(new ContextOptional(context));
    }
}

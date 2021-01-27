<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<!DOCTYPE html>
<!--
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
-->
<html data-ng-app="app" ng-strict-di>
<head>
    <meta charset="utf-8" />
    <meta http-equiv="X-UA-Compatible" content="IE=edge, chrome=1" />
    <meta name="viewport" content="width=device-width, initial-scale=1, maximum-scale=1, minimum-scale=1, user-scalable=no" />
    <%--<link rel="SHORTCUT ICON" href="../img/favicon-ads.ico" type="image/x-icon" />--%>
    <title data-ng-bind="title">Xconf Admin</title>
    <style>
        /* This helps the ng-show/ng-hide animations start at the right place. */
        /* Since Angular has this but needs to load, this gives us the class early. */
        .ng-hide {
            display: none!important;
        }
    </style>
    <c:choose>
        <c:when test="${profile eq 'dev'}">

            <link href="<c:url value="/bower_components/bootstrap/dist/css/bootstrap.min.css"/>" rel="stylesheet" />
            <link href="<c:url value="/node_modules/remixicon/fonts/remixicon.css"/>" rel="stylesheet" />
            <link href="<c:url value="/bower_components/angular-toastr/dist/angular-toastr.css"/>" rel="stylesheet" />
            <link href="<c:url value="/bower_components/angular-bootstrap/ui-bootstrap-csp.css"/>" rel="stylesheet" />

            <link href="<c:url value="/bower_components/angular-dialog-service/dist/dialogs.min.css"/>" rel="stylesheet" />
            <link href="<c:url value="/bower_components/ng-table/ng-table.css"/>" rel="stylesheet" />
            <link href="<c:url value="/bower_components/angular-ui-bootstrap-datetimepicker/datetimepicker.css"/>" rel="stylesheet" />
            <link href="<c:url value="/bower_components/angular-ui-select/dist/select.css"/>" rel="stylesheet" />
            <link href="<c:url value="/app/shared/file-select/file-select.css"/>" rel="stylesheet" />
            <link href="<c:url value="/app/shared/filtered-select/filtered-select.css"/>" rel="stylesheet" />
            <link href="<c:url value="/app/xconf/xconf_base.css"/>" rel="stylesheet" />
            <link href="<c:url value="/app/shared/styles/shared.css"/>" rel="stylesheet" />
            <link href="<c:url value="/app/shared/directives/custom-viewer-panel/custom-viewer-panel.css"/>" rel="stylesheet" />
            <link href="<c:url value="/app/shared/changelog/changelog.css"/>" rel="stylesheet" />
            <link href="<c:url value="/app/shared/statistics/statistics.css"/>" rel="stylesheet" />
            <link href="<c:url value="/app/xconf/styles/rules.css"/>" rel="stylesheet" />
            <link href="<c:url value="/app/xconf/styles/ruleview.css"/>" rel="stylesheet" />

            <script src="<c:url value="/bower_components/jquery/dist/jquery.js"/>"></script>
            <script src="<c:url value="/bower_components/jquery-ui/jquery-ui.js"/>"></script>
            <script src="<c:url value="/bower_components/angular/angular.js"/>"></script>
            <script src="<c:url value="/bower_components/angular-bootstrap/ui-bootstrap-tpls.min.js"/>"></script>
            <script src="<c:url value="/bower_components/angular-resource/angular-resource.js"/>"></script>
            <script src="<c:url value="/bower_components/angular-animate/angular-animate.js"/>"></script>
            <script src="<c:url value="/bower_components/angular-route/angular-route.js"/>"></script>
            <script src="<c:url value="/bower_components/angular-ui-router/release/angular-ui-router.js"/>"></script>
            <script src="<c:url value="/bower_components/angular-sanitize/angular-sanitize.js"/>"></script>
            <script src="<c:url value="/bower_components/bootstrap/dist/js/bootstrap.js"/>"></script>
            <script src="<c:url value="/bower_components/angular-toastr/dist/angular-toastr.js"/>"></script>
            <script src='<c:url value="/bower_components/angular-dialog-service/dist/dialogs.min.js"/>'></script>
            <script src='<c:url value="/bower_components/angular-dialog-service/dist/dialogs-default-translations.min.js"/>'></script>
            <script src="<c:url value="/bower_components/ng-table/ng-table.js"/>"></script>
            <script src="<c:url value="/bower_components/moment/moment.js"/>"></script>
            <script src="<c:url value="/bower_components/extras.angular.plus/ngplus-overlay.js"/>"></script>
            <script src='<c:url value="/bower_components/bower-javascript-ipv6/lib/browser/jsbn.js"/>'></script>
            <script src='<c:url value="/bower_components/bower-javascript-ipv6/lib/browser/jsbn2.js"/>'></script>
            <script src='<c:url value="/bower_components/bower-javascript-ipv6/ipv6.js"/>'></script>
            <script src='<c:url value="/bower_components/bower-javascript-ipv6/lib/browser/sprintf.js"/>'></script>
            <script src='<c:url value="/bower_components/ngstorage/ngStorage.js"/>'></script>
            <script src='<c:url value="/bower_components/angular-cookies/angular-cookies.js"/>'></script>
            <script src='<c:url value="/bower_components/angular-ui-bootstrap-datetimepicker/datetimepicker.js"/>'></script>
            <script src='<c:url value="/bower_components/underscore/underscore-min.js"/>'></script>
            <script src='<c:url value="/bower_components/angular-file-saver/dist/angular-file-saver.bundle.js"/>'></script>
            <script src='<c:url value="/bower_components/angular-ui-select/dist/select.js"/>'></script>
            <script src='<c:url value="/bower_components/google-diff-match-patch/diff_match_patch.js"/>'></script>
            <script src='<c:url value="/bower_components/angular-diff-match-patch/angular-diff-match-patch.js"/>'></script>

            <script src="<c:url value="/app/xconf/app.module.js"/>"></script>

            <script src="<c:url value="/app/xconf/config/config.module.js"/>"></script>
            <script src="<c:url value="/app/xconf/config/state.config.js"/>"></script>

            <script src="<c:url value="/app/shared/core/core.module.js"/>"></script>
            <script src="<c:url value="/app/shared/core/core.js"/>"></script>
            <script src="<c:url value="/app/shared/core/requests-service.js"/>"></script>
            <script src="<c:url value="/app/shared/core/alerts.service.js"/>"></script>

            <script src="<c:url value="/app/shared/directives/directives.module.js"/>"></script>
            <script src="<c:url value="/app/shared/directives/toggle-button.controller.js"/>"></script>
            <script src="<c:url value="/app/shared/directives/toggle-button.directive.js"/>"></script>
            <script src="<c:url value="/app/shared/directives/rule/rule-builder.directive.js"/>"></script>
            <script src="<c:url value="/app/shared/directives/rule/ruleview-editor.directive.js"/>"></script>
            <script src="<c:url value="/app/shared/directives/rule/ruleview.directive.js"/>"></script>
            <script src="<c:url value="/app/shared/directives/dropdown-multiselect.directive.js"/>"></script>
            <script src="<c:url value="/app/shared/directives/option-label-length.directive.js"/>"></script>
            <script src="<c:url value="/app/shared/directives/pagination/pagination.directive.js"/>"></script>
            <script src="<c:url value="/app/shared/directives/editable-map/editable-map.directive.js"/>"></script>
            <script src="<c:url value="/app/shared/directives/simplesearch/simplesearch.directive.js"/>"></script>
            <script src="<c:url value="/app/shared/directives/custom-viewer-panel/custom-viewer-panel.directive.js"/>"></script>
            <script src="<c:url value="/app/shared/directives/custom-progressbar/custom-progressbar.directive.js"/>"></script>

            <script src="<c:url value="/app/shared/changelog/changelog.module.js"/>"></script>
            <script src="<c:url value="/app/shared/changelog/changelog.service.js"/>"></script>
            <script src="<c:url value="/app/shared/changelog/changelog.controller.js"/>"></script>

            <script src="<c:url value="/app/shared/statistics/statistics.module.js"/>"></script>
            <script src="<c:url value="/app/shared/statistics/statistics.service.js"/>"></script>
            <script src="<c:url value="/app/shared/statistics/statistics.controller.js"/>"></script>

            <script src="<c:url value="/app/shared/services/services.module.js"/>"></script>
            <script src="<c:url value="/app/shared/services/utils.service.js"/>"></script>
            <script src="<c:url value="/app/shared/services/dialogs.service.js"/>"></script>
            <script src="<c:url value="/app/shared/services/auth-utils.service.js"/>"></script>
            <script src="<c:url value="/app/shared/services/auth.service.js"/>"></script>
            <script src="<c:url value="/app/shared/services/regexp_constants.js"/>"></script>
            <script src="<c:url value="/app/shared/services/global-validation.service.js"/>"></script>
            <script src="<c:url value="/app/shared/services/import.service.js"/>"></script>
            <script src="<c:url value="/app/shared/services/pagination.service.js"/>"></script>
            <script src="<c:url value="/app/shared/services/rulehelper.service.js"/>"></script>
            <script src="<c:url value="/app/shared/services/rule-validation.service.js"/>"></script>
            <script src="<c:url value="/app/shared/services/testpage-utils.service.js"/>"></script>
            <script src="<c:url value="/app/shared/services/synchttp.service.js"/>"></script>

            <script src="<c:url value="/app/shared/filters/filters.module.js"/>"></script>
            <script src="<c:url value="/app/shared/filters/startFrom.filter.js"/>"></script>

            <script src="<c:url value="/app/shared/file-select/file-select.directive.js"/>"></script>
            <script src="<c:url value="/app/shared/file-select/file-reader.service.js"/>"></script>

            <script src="<c:url value="/app/shared/filtered-select/filtered-select.module.js"/>"></script>
            <script src="<c:url value="/app/shared/filtered-select/filtered-select.controller.js"/>"></script>

            <script src="<c:url value="/app/shared/controller/controller.module.js"/>"></script>
            <script src="<c:url value="/app/shared/controller/main.controller.js"/>"></script>
            <script src="<c:url value="/app/shared/controller/edit.controller.js"/>"></script>

            <script src="<c:url value="/app/xconf/firmware/firmwareconfig/firmwareconfig.module.js"/>"></script>
            <script src="<c:url value="/app/xconf/firmware/firmwareconfig/firmwareconfig.service.js"/>"></script>
            <script src="<c:url value="/app/xconf/firmware/firmwareconfig/firmwareconfigs.controller.js"/>"></script>
            <script src="<c:url value="/app/xconf/firmware/firmwareconfig/firmwareconfig-edit.controller.js"/>"></script>
            <script src="<c:url value="/app/xconf/firmware/firmwareconfig/firmwareconfig-view.controller.js"/>"></script>
            <script src="<c:url value="/app/xconf/firmware/firmwareconfig/firmwareconfig.import.controller.js"/>"></script>


            <script src="<c:url value="/app/xconf/rfc/feature/feature.module.js"/>"></script>
            <script src="<c:url value="/app/xconf/rfc/feature/feature.service.js"/>"></script>
            <script src="<c:url value="/app/xconf/rfc/feature/feature.controller.js"/>"></script>
            <script src="<c:url value="/app/xconf/rfc/feature/feature-edit.controller.js"/>"></script>
            <script src="<c:url value="/app/xconf/rfc/feature/feature-import.controller.js"/>"></script>

            <script src="<c:url value="/app/xconf/rfc/featurerule/featurerule.module.js"/>"></script>
            <script src="<c:url value="/app/xconf/rfc/featurerule/featurerule.service.js"/>"></script>
            <script src="<c:url value="/app/xconf/rfc/featurerule/featurerule.validation.service.js"/>"></script>
            <script src="<c:url value="/app/xconf/rfc/featurerule/featurerule.controller.js"/>"></script>
            <script src="<c:url value="/app/xconf/rfc/featurerule/featurerule.view.controller.js"/>"></script>
            <script src="<c:url value="/app/xconf/rfc/featurerule/featurerule-edit.controller.js"/>"></script>
            <script src="<c:url value="/app/xconf/rfc/featurerule/featurerule-import.controller.js"/>"></script>

            <script src="<c:url value="/app/xconf/firmware/model/model.module.js"/>"></script>
            <script src="<c:url value="/app/xconf/firmware/model/model.service.js"/>"></script>
            <script src="<c:url value="/app/xconf/firmware/model/models.controller.js"/>"></script>
            <script src="<c:url value="/app/xconf/firmware/model/model-edit.controller.js"/>"></script>
            <script src="<c:url value="/app/xconf/firmware/model/model.import.controller.js"/>"></script>

            <script src="<c:url value="/app/xconf/firmwarerule/firmwarerule.module.js"/>"></script>
            <script src="<c:url value="/app/xconf/firmwarerule/firmwarerule.service.js"/>"></script>
            <script src="<c:url value="/app/xconf/firmwarerule/firmwarerule-view.controller.js"/>"></script>
            <script src="<c:url value="/app/xconf/firmwarerule/firmwarerules.controller.js"/>"></script>
            <script src="<c:url value="/app/xconf/firmwarerule/firmwarerule-validation.service.js"/>"></script>
            <script src="<c:url value="/app/xconf/firmwarerule/firmwarerule-edit.controller.js"/>"></script>
            <script src="<c:url value="/app/xconf/firmwarerule/firmwarerule-definePropertiesEditor.directive.js"/>"></script>
            <script src="<c:url value="/app/xconf/firmwarerule/firmwarerule-import.controller.js"/>"></script>


            <script src="<c:url value="/app/xconf/firmwareruletemplate/firmwareruletemplate.module.js"/>"></script>
            <script src="<c:url value="/app/xconf/firmwareruletemplate/firmwareruletemplate.service.js"/>"></script>
            <script src="<c:url value="/app/xconf/firmwareruletemplate/firmwareruletemplates.controller.js"/>"></script>
            <script src="<c:url value="/app/xconf/firmwareruletemplate/firmwareruletemplate-edit.controller.js"/>"></script>
            <script src="<c:url value="/app/xconf/firmwareruletemplate/firmwareruletemplate-definePropertiesEditor.directive.js"/>"></script>
            <script src="<c:url value="/app/xconf/firmwareruletemplate/firmwareruletemplate-import.controller.js"/>"></script>
            <script src="<c:url value="/app/xconf/firmwareruletemplate/firmwareruletemplate-view.controller.js"/>"></script>

            <script src="<c:url value="/app/xconf/firmware/environment/environment.module.js"/>"></script>
            <script src="<c:url value="/app/xconf/firmware/environment/environment.service.js"/>"></script>
            <script src="<c:url value="/app/xconf/firmware/environment/environments.controller.js"/>"></script>
            <script src="<c:url value="/app/xconf/firmware/environment/environment-edit.controller.js"/>"></script>
            <script src="<c:url value="/app/xconf/firmware/environment/environment.import.controller.js"/>"></script>

            <script src="<c:url value="/app/shared/directives/environment-model/environment-model.edit.directive.js"/>"></script>
            <script src="<c:url value="/app/shared/directives/environment-model/environment-model.view.directive.js"/>"></script>
            <script src="<c:url value="/app/shared/directives/environment-model/environment-model.import.directive.js"/>"></script>

            <script src="<c:url value="/app/xconf/telemetry/permanentprofile/permanentprofile.module.js"/>"></script>
            <script src="<c:url value="/app/xconf/telemetry/permanentprofile/permanentprofile.service.js"/>"></script>
            <script src="<c:url value="/app/xconf/telemetry/permanentprofile/permanentprofiles.controller.js"/>"></script>
            <script src="<c:url value="/app/xconf/telemetry/permanentprofile/permanentprofile.edit.controller.js"/>"></script>
            <script src="<c:url value="/app/xconf/telemetry/permanentprofile/permanentprofile.import.controller.js"/>"></script>
            <script src="<c:url value="/app/xconf/telemetry/permanentprofile/permanentprofile.view.controller.js"/>"></script>
            <script src="<c:url value="/app/xconf/telemetry/permanentprofile/permanentprofile.filter.js"/>"></script>

            <script src="<c:url value="/app/xconf/telemetry/telemetrytwoprofile/telemetrytwoprofile.module.js"/>"></script>
            <script src="<c:url value="/app/xconf/telemetry/telemetrytwoprofile/telemetrytwoprofile.service.js"/>"></script>
            <script src="<c:url value="/app/xconf/telemetry/telemetrytwoprofile/telemetrytwoprofile.filter.js"/>"></script>
            <script src="<c:url value="/app/xconf/telemetry/telemetrytwoprofile/telemetrytwoprofiles.controller.js"/>"></script>
            <script src="<c:url value="/app/xconf/telemetry/telemetrytwoprofile/telemetrytwoprofile.view.controller.js"/>"></script>
            <script src="<c:url value="/app/xconf/telemetry/telemetrytwoprofile/telemetrytwoprofile.edit.controller.js"/>"></script>
            <script src="<c:url value="/app/xconf/telemetry/telemetrytwoprofile/telemetrytwoprofile.import.controller.js"/>"></script>

            <script src="<c:url value="/app/xconf/settings/settingprofile/settingprofile.module.js"/>"></script>
            <script src="<c:url value="/app/xconf/settings/settingprofile/settingprofile.service.js"/>"></script>
            <script src="<c:url value="/app/xconf/settings/settingprofile/settingprofiles.controller.js"/>"></script>
            <script src="<c:url value="/app/xconf/settings/settingprofile/settingprofile.edit.controller.js"/>"></script>
            <script src="<c:url value="/app/xconf/settings/settingprofile/settingprofile.import.controller.js"/>"></script>

            <script src="<c:url value="/app/xconf/settings/settingrule/settingrule.module.js"/>"></script>
            <script src="<c:url value="/app/xconf/settings/settingrule/settingrule.service.js"/>"></script>
            <script src="<c:url value="/app/xconf/settings/settingrule/settingrules.controller.js"/>"></script>
            <script src="<c:url value="/app/xconf/settings/settingrule/settingrule.edit.controller.js"/>"></script>
            <script src="<c:url value="/app/xconf/settings/settingrule/settingrule.import.controller.js"/>"></script>

            <script src="<c:url value="/app/xconf/settings/testpage/testpage.module.js"/>"></script>
            <script src="<c:url value="/app/xconf/settings/testpage/testpage.service.js"/>"></script>
            <script src="<c:url value="/app/xconf/settings/testpage/testpage.controller.js"/>"></script>

            <script src="<c:url value="/app/xconf/telemetry/targetingrule/targetingrule.module.js"/>"></script>
            <script src="<c:url value="/app/xconf/telemetry/targetingrule/targetingrule.service.js"/>"></script>
            <script src="<c:url value="/app/xconf/telemetry/targetingrule/targetingrules.controller.js"/>"></script>
            <script src="<c:url value="/app/xconf/telemetry/targetingrule/targetingrule.edit.controller.js"/>"></script>
            <script src="<c:url value="/app/xconf/telemetry/targetingrule/targetingrule.import.controller.js"/>"></script>

            <script src="<c:url value="/app/xconf/telemetry/telemetrytwotargetingrule/telemetrytwotargetingrule.module.js"/>"></script>
            <script src="<c:url value="/app/xconf/telemetry/telemetrytwotargetingrule/telemetrytwotargetingrule.service.js"/>"></script>
            <script src="<c:url value="/app/xconf/telemetry/telemetrytwotargetingrule/telemetrytwotargetingrules.controller.js"/>"></script>
            <script src="<c:url value="/app/xconf/telemetry/telemetrytwotargetingrule/telemetrytwotargetingrule.view.controller.js"/>"></script>
            <script src="<c:url value="/app/xconf/telemetry/telemetrytwotargetingrule/telemetrytwotargetingrule.edit.controller.js"/>"></script>
            <script src="<c:url value="/app/xconf/telemetry/telemetrytwotargetingrule/telemetrytwotargetingrule.import.controller.js"/>"></script>

            <script src="<c:url value="/app/xconf/telemetry/testpage/testpage.module.js"/>"></script>
            <script src="<c:url value="/app/xconf/telemetry/testpage/testpage.service.js"/>"></script>
            <script src="<c:url value="/app/xconf/telemetry/testpage/testpage.controller.js"/>"></script>

            <script src="<c:url value="/app/xconf/telemetry/telemetrytwotestpage/telemetrytwotestpage.module.js"/>"></script>
            <script src="<c:url value="/app/xconf/telemetry/telemetrytwotestpage/telemetrytwotestpage.service.js"/>"></script>
            <script src="<c:url value="/app/xconf/telemetry/telemetrytwotestpage/telemetrytwotestpage.controller.js"/>"></script>

            <script src="<c:url value="/app/xconf/dcm/uploadRepository/uploadRepository.module.js"/>"></script>
            <script src="<c:url value="/app/xconf/dcm/uploadRepository/uploadRepository.service.js"/>"></script>
            <script src="<c:url value="/app/xconf/dcm/uploadRepository/uploadRepositories.controller.js"/>"></script>
            <script src="<c:url value="/app/xconf/dcm/uploadRepository/uploadRepository.edit.controller.js"/>"></script>
            <script src="<c:url value="/app/xconf/dcm/uploadRepository/uploadrepository-import.controller.js"/>"></script>

            <script src="<c:url value="/app/xconf/dcm/vodsettings/vodsettings.module.js"/>"></script>
            <script src="<c:url value="/app/xconf/dcm/vodsettings/vodsettings.service.js"/>"></script>
            <script src="<c:url value="/app/xconf/dcm/vodsettings/vodsettings.controller.js"/>"></script>
            <script src="<c:url value="/app/xconf/dcm/vodsettings/vodsettings.modal.view.js"/>"></script>
            <script src="<c:url value="/app/xconf/dcm/vodsettings/vodsettings.modal.edit.js"/>"></script>

            <script src="<c:url value="/app/xconf/dcm/formula/formula.module.js"/>"></script>
            <script src="<c:url value="/app/xconf/dcm/formula/formula.service.js"/>"></script>
            <script src="<c:url value="/app/xconf/dcm/formula/formula.validation.service.js"/>"></script>
            <script src="<c:url value="/app/xconf/dcm/formula/formulas.controller.js"/>"></script>
            <script src="<c:url value="/app/xconf/dcm/formula/formula.edit.controller.js"/>"></script>
            <script src="<c:url value="/app/xconf/dcm/formula/formula.modal.view.js"/>"></script>
            <script src="<c:url value="/app/xconf/dcm/formula/formula-import.controller.js"/>"></script>
            <script src="<c:url value="/app/xconf/dcm/formula/formula.validation.info.modal.controller.js"/>"></script>

            <script src="<c:url value="/app/xconf/namespacedlist/namespacedlist.module.js"/>"></script>
            <script src="<c:url value="/app/xconf/namespacedlist/namespacedlist.service.js"/>"></script>
            <script src="<c:url value="/app/xconf/namespacedlist/namespacedlist.controller.js"/>"></script>
            <script src="<c:url value="/app/xconf/namespacedlist/namespacedlist-import.controller.js"/>"></script>
            <script src="<c:url value="/app/xconf/namespacedlist/namespacedlist-view.controller.js"/>"></script>
            <script src="<c:url value="/app/xconf/namespacedlist/namespacedlist-edit.controller.js"/>"></script>
            <script src="<c:url value="/app/xconf/namespacedlist/namespacedlist-bulk-delete.controller.js"/>"></script>

            <script src="<c:url value="/app/xconf/dcm/devicesettings/devicesettings.module.js"/>"></script>
            <script src="<c:url value="/app/xconf/dcm/devicesettings/devicesettings.controller.js"/>"></script>
            <script src="<c:url value="/app/xconf/dcm/devicesettings/devicesettings.edit.controller.js"/>"></script>
            <script src="<c:url value="/app/xconf/dcm/devicesettings/devicesettings.service.js"/>"></script>
            <script src="<c:url value="/app/xconf/dcm/devicesettings/devicesettings-validation.service.js"/>"></script>
            <script src="<c:url value="/app/xconf/dcm/devicesettings/devicesettings.view.controller.js"/>"></script>

            <script src="<c:url value="/app/xconf/dcm/testpage/testpage.module.js"/>"></script>
            <script src="<c:url value="/app/xconf/dcm/testpage/testpage.service.js"/>"></script>
            <script src="<c:url value="/app/xconf/dcm/testpage/testpage.controller.js"/>"></script>

            <script src="<c:url value="/app/xconf/firmware/testpage/testpage.module.js"/>"></script>
            <script src="<c:url value="/app/xconf/firmware/testpage/testpage.service.js"/>"></script>
            <script src="<c:url value="/app/xconf/firmware/testpage/testpage.controller.js"/>"></script>

            <script src="<c:url value="/app/xconf/firmware/log/log.module.js"/>"></script>
            <script src="<c:url value="/app/xconf/firmware/log/log.service.js"/>"></script>
            <script src="<c:url value="/app/xconf/firmware/log/log.controller.js"/>"></script>

            <script src="<c:url value="/app/xconf/firmware/reportpage/reportpage.module.js"/>"></script>
            <script src="<c:url value="/app/xconf/firmware/reportpage/reportpage.service.js"/>"></script>
            <script src="<c:url value="/app/xconf/firmware/reportpage/reportpage.controller.js"/>"></script>

            <script src="<c:url value="/app/xconf/dcm/loguploadsettings/loguploadsettings.module.js"/>"></script>
            <script src="<c:url value="/app/xconf/dcm/loguploadsettings/loguploadsettings.edit.controller.js"/>"></script>
            <script src="<c:url value="/app/xconf/dcm/loguploadsettings/loguploadsettings.view.controller.js"/>"></script>
            <script src="<c:url value="/app/xconf/dcm/loguploadsettings/loguploadsettings.service.js"/>"></script>
            <script src="<c:url value="/app/xconf/dcm/loguploadsettings/loguploadsettings-validation.service.js"/>"></script>
            <script src="<c:url value="/app/xconf/dcm/loguploadsettings/loguploadsettings.controller.js"/>"></script>

            <script src="<c:url value="/app/xconf/firmware/roundrobinfilter/roundrobinfilter.module.js"/>"></script>
            <script src="<c:url value="/app/xconf/firmware/roundrobinfilter/roundrobinfilter.service.js"/>"></script>
            <script src="<c:url value="/app/xconf/firmware/roundrobinfilter/roundrobinfilter.controller.js"/>"></script>
            <script src="<c:url value="/app/xconf/firmware/roundrobinfilter/roundrobinfilter.edit.controller.js"/>"></script>
            <script src="<c:url value="/app/xconf/firmware/roundrobinfilter/roundrobinfilter.import.controller.js"/>"></script>
            <script src="<c:url value="/app/xconf/firmware/roundrobinfilter/roundrobinfilter.validator.service.js"/>"></script>

            <script src="<c:url value="/app/xconf/firmware/percentfilter/percentfilter.module.js"/>"></script>
            <script src="<c:url value="/app/xconf/firmware/percentfilter/percentfilter.service.js"/>"></script>
            <script src="<c:url value="/app/xconf/firmware/percentfilter/percentfilter.controller.js"/>"></script>
            <script src="<c:url value="/app/xconf/firmware/percentfilter/percentfilter.edit.controller.js"/>"></script>
            <script src="<c:url value="/app/xconf/firmware/percentfilter/percentfilter.import.controller.js"/>"></script>
            <script src="<c:url value="/app/xconf/firmware/percentfilter/percentfilter.view.controller.js"/>"></script>
            <script src="<c:url value="/app/xconf/firmware/percentfilter/distribution.directive.js"/>"></script>
            <script src="<c:url value="/app/xconf/firmware/percentfilter/percentfilter.validation.service.js"/>"></script>
            <script src="<c:url value="/app/xconf/firmware/percentfilter/distribution.filter.js"/>"></script>

            <script src="<c:url value="/app/xconf/firmware/percentfilter/percentage-bean.service.js"/>"></script>
            <script src="<c:url value="/app/xconf/firmware/percentfilter/percentage-bean.edit.controller.js"/>"></script>

            <script src="<c:url value="/app/xconf/firmware/activation-version/activation-version.module.js"/>"></script>
            <script src="<c:url value="/app/xconf/firmware/activation-version/activation-version.service.js"/>"></script>
            <script src="<c:url value="/app/xconf/firmware/activation-version/activation-versions.controller.js"/>"></script>
            <script src="<c:url value="/app/xconf/firmware/activation-version/activation-version.edit.controller.js"/>"></script>
            <script src="<c:url value="/app/xconf/firmware/activation-version/activation-version.import.controller.js"/>"></script>

            <script src="<c:url value="/app/xconf/migration/migration.module.js"/>"></script>
            <script src="<c:url value="/app/xconf/migration/migration.service.js"/>"></script>
            <script src="<c:url value="/app/xconf/migration/migration.controller.js"/>"></script>
            <script src="<c:url value="/app/xconf/migration/migration-result-view.controller.js"/>"></script>

            <script src="<c:url value="/app/xconf/changes/change.module.js"/>"></script>
            <script src="<c:url value="/app/xconf/changes/change.service.js"/>"></script>
            <script src="<c:url value="/app/xconf/changes/change.controller.js"/>"></script>

            <script src="<c:url value="/app/shared/pages/testpage/testpage.module.js"/>"></script>
            <script src="<c:url value="/app/shared/pages/testpage/testpage.service.js"/>"></script>
            <script src="<c:url value="/app/shared/pages/testpage/testpage.controller.js"/>"></script>

        </c:when>
        <c:otherwise>

            <link href="<c:url value="/app/compiled/vendor.css"/>" rel="stylesheet" />
            <link href="<c:url value="/app/compiled/xconf.css"/>" rel="stylesheet" />

            <script src="<c:url value="/app/compiled/vendor.js"/>"></script>
            <script src="<c:url value="/app/compiled/xconfUI.js"/>"></script>

        </c:otherwise>
    </c:choose>
</head>
<body>
    <div class="container">
        <!--
        Copyright (c) 2011-2020 Twitter, Inc.
        Copyright (c) 2011-2020 The Bootstrap Authors
        Licensed under the MIT license
        -->
        <nav class="navbar navbar-default navbar-static-top">
            <div class="container-flued">
                <div class="navbar-header">
                    <button type="button" class="navbar-toggle collapsed" data-toggle="collapse" data-target="#navbar" aria-expanded="false" aria-controls="navbar">
                        <span class="sr-only">Toggle navigation</span>
                        <span class="icon-bar"></span>
                        <span class="icon-bar"></span>
                        <span class="icon-bar"></span>
                    </button>
                    <a class="navbar-brand" href="#"><img src="<c:url value="/img/logo.png"/>" width="100" height="31"></a>
                </div>
                <div id="navbar" class="navbar-collapse collapse">
                    <ul class="nav navbar-nav">
                        <li class="dropdown" ng-show="authUtils.hasPermission(PERMISSION.READ_COMMON)">
                            <a href="" class="dropdown-toggle" data-toggle="dropdown" role="button"
                               aria-haspopup="true" aria-expanded="false">
                                Common <span class="caret"></span>
                            </a>
                            <ul class="dropdown-menu">
                                <li>
                                    <a ui-sref="environments">Environments</a>
                                </li>
                                <li>
                                    <a ui-sref="models">Models</a>
                                </li>
                                <li role="separator" class="divider"></li>
                                <li>
                                    <a ui-sref="namespacedlist({type: 'MAC_LIST'})">MAC Lists</a>
                                </li>
                                <li>
                                    <a ui-sref="namespacedlist({type: 'IP_LIST'})">IP Lists</a>
                                </li>
                            </ul>
                        </li>
                        <li class="dropdown" ng-show="authUtils.canReadFirmware()">
                            <a href="" class="dropdown-toggle" data-toggle="dropdown" role="button" aria-haspopup="true" aria-expanded="false">Firmware <span class="caret"></span></a>
                            <ul class="dropdown-menu">
                                <li>
                                    <a ui-sref="firmwareconfigs">Firmware Configs</a>
                                </li>
                                <li>
                                    <a ui-sref="firmwarerules">Firmware Rules</a>
                                </li>
                                <li>
                                    <a ui-sref="firmwareruletemplates"
                                       ng-show="authUtils.hasPermission(PERMISSION.READ_FIRMWARE_RULE_TEMPLATES)">
                                        Firmware Templates
                                    </a>
                                </li>
                                <li role="separator" class="divider"></li>
                                <li>
                                    <a ui-sref="roundrobinfilter">Download Location Filter</a>
                                </li>
                                <li>
                                    <a ui-sref="percentfilter">Percent Filter</a>
                                </li>
                                <li>
                                    <a ui-sref="activation-version">Activation version</a>
                                </li>
                                <li role="separator" class="divider"></li>
                                <li>
                                    <a ui-sref="logs">Logs</a>
                                </li>
                                <li>
                                    <a ui-sref="reportpage-firmware">Report page</a>
                                </li>
                                <li role="separator" class="divider"></li>
                                <li>
                                    <a ui-sref="testpage-firmware">Test page</a>
                                </li>
                            </ul>
                        </li>

                        <li class="dropdown" ng-show="authUtils.canReadDcm()">
                            <a href="" class="dropdown-toggle" data-toggle="dropdown" role="button" aria-haspopup="true" aria-expanded="false">DCM <span class="caret"></span></a>
                            <ul class="dropdown-menu">
                                <li>
                                    <a ui-sref="formulas">Formulas</a>
                                </li>
                                <li role="separator" class="divider"></li>
                                <li>
                                    <a ui-sref="devicesettings">Device Settings</a>
                                </li>
                                <li>
                                    <a ui-sref="loguploadsettings">Log Upload Settings</a>
                                </li>
                                <li>
                                    <a ui-sref="vodsettings">VOD Settings</a>
                                </li>
                                <li>
                                    <a ui-sref="uploadrepositories">Upload repository</a>
                                </li>
                                <li role="separator" class="divider"></li>
                                <li>
                                    <a ui-sref="testpage-dcm">Test page</a>
                                </li>
                            </ul>
                        </li>
                        <li class="dropdown" ng-show="authUtils.canReadTelemetry()">
                            <a href="" class="dropdown-toggle" data-toggle="dropdown" role="button" aria-haspopup="true" aria-expanded="false">Telemetry <span class="caret"></span></a>
                            <ul class="dropdown-menu">
                                <li>
                                    <a ui-sref="permanentprofiles">Permanent Profiles</a>
                                </li>
                                <li>
                                    <a ui-sref="targetingrules">Targeting Rules</a>
                                </li>
                                <li>
                                    <a ui-sref="telemetrytwoprofiles">Telemetry 2.0 Profiles</a>
                                </li>
                                <li>
                                    <a ui-sref="telemetrytwotargetingrules">Telemetry 2.0 Rules</a>
                                </li>
                                <li role="separator" class="divider"></li>
                                <li>
                                    <a ui-sref="testpage-telemetry">Test page</a>
                                </li>
                                <li>
                                    <a ui-sref="testpage-telemetrytwo">Telemetry 2.0 Test page</a>
                                </li>
                            </ul>
                        </li>
                        <li class="dropdown" ng-show="authUtils.canReadDcm()">
                            <a href="" class="dropdown-toggle" data-toggle="dropdown" role="button" aria-haspopup="true" aria-expanded="false">Settings <span class="caret"></span></a>
                            <ul class="dropdown-menu">
                                <li>
                                    <a ui-sref="settingprofiles">Setting Profiles</a>
                                </li>
                                <li>
                                    <a ui-sref="settingrules">Setting Rules</a>
                                </li>
                                <li role="separator" class="divider"></li>
                                <li>
                                    <a ui-sref="settings-testpage">Test page</a>
                                </li>
                            </ul>
                        </li>
                        <li class="dropdown" ng-show="authUtils.canReadDcm()">
                            <a href="" class="dropdown-toggle" data-toggle="dropdown" role="button" aria-haspopup="true" aria-expanded="false">RFC <span class="caret"></span></a>
                            <ul class="dropdown-menu">
                                <li>
                                    <a ui-sref='feature'>Feature</a>
                                </li>
                                <li>
                                    <a ui-sref='featurerule'>Feature Rule</a>
                                </li>
                                <li role="separator" class="divider"></li>
                                <li>
                                    <a ui-sref='testpage-rfc'>Test page</a>
                                </li>
                            </ul>
                        </li>
                        <li class="dropdown" ng-show="authUtils.hasPermission(PERMISSION.VIEW_TOOLS)">
                            <a href="" class="dropdown-toggle" data-toggle="dropdown" role="button" aria-haspopup="true" aria-expanded="false">Tools <span class="caret"></span></a>
                            <ul class="dropdown-menu">
                                <li>
                                    <a ui-sref='statistics'>Statistics</a>
                                </li>
                                <li>
                                    <a ui-sref='changelog'>ChangeLog</a>
                                </li>
                                <li>
                                    <a ui-sref='migration'>Migration</a>
                                </li>
                            </ul>
                        </li>
                        <li class="dropdown" ng-show="authUtils.canReadChanges()">
                            <a ui-sref="changes" class="dropdown-toggle" role="button" aria-haspopup="false" aria-expanded="false">Changes</a>
                        </li>
                    </ul>
                    <div class="navbar-right">
                        <ul class="nav navbar-nav">
                            <div class="btn-group-vertical" role="group">
                                <span type="button" class="btn-group application-type" role="group">Application</span>
                                <select role="group" class="btn-group form-control"
                                        ng-model="$root.applicationType"
                                        ng-disabled="!authUtils.isMultipleApplication()"
                                        ng-class="{'remove-button-class': !authUtils.isMultipleApplication()}"
                                        ng-change="changeApplicationType($root.applicationType)">
                                    <option ng-repeat="applicationType in $root.availableApplicationTypes" value="{{applicationType}}" ng-bind="applicationType"></option>
                                </select>
                            </div>
                        </ul>
                        <ul class="nav navbar-nav nav-logout">
                            <div class="pull-right logout-container">
                                <span ng-bind="currentUser.firstName"></span>
                                <a class="ri-logout-box-r-line logout ri-lg ri-align" href='./loginForm'></a>
                            </div>
                            <div class="date-time-container">
                                <div class="data-container">
                                    <span class="pull-right" ng-bind="currentTime | date : 'MM/dd/yyyy' : 'UTC'"></span>
                                </div>
                                <div class="time-container pull-rigth">
                                    <span class="label label-primary pull-left utc-time">UTC</span>
                                    <span class="pull-right" ng-bind="currentTime | date : 'HH:mm:ss' : 'UTC'"></span>
                                </div>
                            </div>
                        </ul>
                    </div>
                </div>
            </div>
        </nav>
        <div ui-view class="content"></div>
    </div>
</body>
</html>

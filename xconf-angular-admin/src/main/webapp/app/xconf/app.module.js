/*******************************************************************************
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
 *******************************************************************************/
(function() {
    'use strict';

    angular.module('app', [
        /*
         * Order is not important.
         */

        /*
         * Everybody has access to these.
         * We could place these under every feature area,
         * but this is easier to maintain.
         */
        'app.services',
        'app.core',

        /*
         * Feature areas
         */

        'ui.bootstrap',

        'ngAnimate',
        'ngCookies',
        'ngTable',
        'ngResource',
        'toastr',
        'dialogs.main',
        'ngStorage',
        'ui.bootstrap.datetimepicker',
        'ngFileSaver',


        'app.filters',
        'app.directives',
        'app.filtered-select',
        'app.firmwareconfig',
        'app.firmwarerule',
        'app.firmwareruletemplate',
        'app.model',
        'app.environment',
        'app.settingprofile',
        'app.settingrule',
        'app.permanentprofile',
        'app.telemetrytwoprofile',
        'app.targetingrule',
        'app.telemetrytwotargetingrule',
        'app.telemetrytwotestpage',
        'app.formula',
        'app.namespacedlist',
        'app.permanentprofileFilters',
        'app.telemetrytwoprofileFilters',
        'app.namespacedlist',
        'app.vodsettings',
        'app.uploadRepository',
        'app.devicesettings',
        'app.changeLog',
        'app.statistics',
        'app.testpage',
        'app.telemetryTestPage',
        'app.settingsTestPage',
        'app.firmwareTestPage',
        'app.log',
        'app.loguploadsettings',
        'app.roundrobinfilter',
        'app.firmwareReportPage',
        'app.percentfilter',
        'app.migration',
        'app.feature',
        'app.featurerule',
        'app.sharedTestPage',
        'app.distributionFilter',
        'app.controller',
        'app.activation-version',
        'app.change'
    ])
})();
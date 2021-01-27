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
 * Author: rdolomansky
 * Created: 4/28/15
 */

(function () {
    'use strict';

    angular
        .module('app.config')
        .config(state)
        .run(['$log', '$window', '$location', '$rootScope','$state', 'dialogs', '$cookies', '$localStorage', 'utilsService', 'alertsService', 'authUtilsService', 'PERMISSION', 'authService', 'dialogsService', 'AUTH_EVENT', 'APPLICATION_TYPES',
            function($log, $window, $location, $rootScope, $state, $dialogs, $cookies, $localStorage, utilsService, alertsService, authUtilsService, PERMISSION, authService, dialogsService, AUTH_EVENT, APPLICATION_TYPES) {
                $rootScope.$state = $state;
                $rootScope.authUtils = authUtilsService;
                $rootScope.PERMISSION = PERMISSION;
                $rootScope.currentUser = authService.getAuthInfo();
                $rootScope.currentTime = new Date();
                $rootScope.APPLICATION_TYPES = APPLICATION_TYPES;
                $rootScope.availableApplicationTypes = APPLICATION_TYPES;
                $rootScope.changeApplicationType = changeApplicationType;

                restoreApplicationType();

                $window.onfocus = function() {
                    $localStorage['xconfCurrentHash'] = $window.location.hash;
                };

                $rootScope.$on(AUTH_EVENT.UNAUTHORIZED, function(event, message) {
                    event.preventDefault();
                    delete $cookies.get('token');
                    dialogsService.showConfirmAsSingleton('Session expired', "Your session has expired. Would you like to log in again?", null, function() {
                        $localStorage['xconfCurrentHash'] = $window.location.hash;
                        $window.location.href='../ux';
                        /* provide loginForm*/
                    });
                });

                $rootScope.$on(AUTH_EVENT.NO_ACCESS, function(event, message) {
                    event.preventDefault();
                    dialogsService.showErrorAsSingleton('Access is denied', "You don't have permission to perform this action");
                });

                $rootScope.$on('$locationChangeSuccess', function(evt) {
                    $rootScope.currentTime = new Date();
                    var hash = $window.location.hash;
                    $localStorage['xconfCurrentHash'] = hash;
                });

                $rootScope.$on('$stateChangeStart', function(event, next) {
                    if (angular.isDefined(next.data)) {
                        var permissions = next.data.permissions;
                        var user = $rootScope.currentUser;
                        var isDevProfile = user && user.username === 'dev' &&
                            Array.isArray(user.permissions) && (user.permissions.indexOf(PERMISSION.PERMIT_ALL) > -1);

                        if (!$cookies.get('token')  && !isDevProfile) {
                            event.preventDefault();
                            $rootScope.$broadcast(AUTH_EVENT.UNAUTHORIZED);
                        } else if (!authUtilsService.hasOneOfPermissions(permissions)) {
                            event.preventDefault();
                            $rootScope.$broadcast(AUTH_EVENT.NO_ACCESS);
                        } else {
                            var applicationTypes = authUtilsService.getAvailableApplicationTypes(permissions);
                            $rootScope.availableApplicationTypes = applicationTypes;
                            if (applicationTypes.length > 0 && applicationTypes.indexOf($rootScope.applicationType) < 0) {
                                var activeType = applicationTypes[0];
                                $cookies.put('applicationType', activeType);
                                $rootScope.applicationType = activeType;
                            }
                        }
                    }
                });

                var isNewTab = initNewTabName();
                function initNewTabName() {
                    var isNewTab = !window.name; // true, if a new tab was opened

                    if (isNewTab) {
                        window.name = 'xconf';
                    }
                    return isNewTab;
                }

                removeTokenParamFromUrl();
                function removeTokenParamFromUrl() {
                    var search = window.location.search.substr(1);
                    var storedHash = $localStorage['xconfCurrentHash'];

                    if (search || storedHash) {
                        setTimeout(function () { // hack for chrome
                            if (search) {
                                window.location.search = getNewSearch(search);
                            }
                            if (!isNewTab && storedHash && storedHash !== '#/') {
                                console.log('Restoring hash: ' + storedHash);
                                $window.location.hash = storedHash;
                            }
                        }, 100);
                    }
                }

                function getNewSearch(search) {
                    var params = search.split('&');
                    for (var i = 0; i < params.length; i++) {
                        if (params[i].startsWith('token=')) {
                            params.splice(i, 1);
                        }
                    }

                    var newSearch = '';
                    if (params.length > 0) {
                        newSearch = '?' + params.join('&');
                    }
                    return newSearch;
                }

                function changeApplicationType(applicationType) {
                    $cookies.put('applicationType', applicationType);
                    $rootScope.$broadcast('applicationType:changed', {
                        applicationType: applicationType
                    });
                }

                function restoreApplicationType() {
                    if (!authUtilsService.isMultipleApplication()) {
                        var applicationType = authUtilsService.getApplicationType();
                        $rootScope.applicationType = applicationType;
                        $cookies.put('applicationType', applicationType);
                        return;
                    }
                    var savedApplicationType = $cookies.get('applicationType');
                    if (savedApplicationType) {
                        $rootScope.applicationType = savedApplicationType;
                    } else {
                        var application = $rootScope.APPLICATION_TYPES[0];
                        $rootScope.applicationType = application;
                        $cookies.put('applicationType', application);
                    }
                }
            }]);

    state.$inject = ['$stateProvider', '$urlRouterProvider', 'PERMISSION'];

    function state($stateProvider, $urlRouterProvider, PERMISSION) {

        $urlRouterProvider.otherwise(function($injector, $location) {
            var authUtilsService = $injector.get('authUtilsService');
            var hash = $injector.$localStorage ? $injector.$localStorage['xconfCurrentHash'] : null;
            var page = 'environments';
            if (hash) {
                page = hash;
            } else if (authUtilsService.hasOneOfPermissions([PERMISSION.READ_COMMON])) {
                page = 'environments';
            } else if (authUtilsService.canReadFirmware()) {
                page = 'firmwareconfigs';
            } else if (authUtilsService.canReadDcm()) {
                page = 'formulas';
            } else if (authUtilsService.canReadTelemetry()) {
                page = 'permanentprofiles';
            } else if (authUtilsService.hasPermission(PERMISSION.VIEW_TOOLS)) {
                page = 'statistics';
            }
            $injector.get('$state').go(page);
        });

        $stateProvider
            .state('firmwareconfigs', {
                controller: 'FirmwareConfigController',
                controllerAs: 'vm',
                url: '/firmwareconfig/all',
                templateUrl: 'app/xconf/firmware/firmwareconfig/firmwareconfigs.html',
                data: {
                    permissions: PERMISSION.READ_FIRMWARE_PERMISSIONS
                }
            })
            .state('firmwareconfig-edit', {
                controller: 'FirmwareConfigEditController',
                controllerAs: 'vm',
                url: '/firmwareconfig/edit/:firmwareConfigId',
                templateUrl: 'app/xconf/firmware/firmwareconfig/firmwareconfig-edit.html',
                data: {
                    permissions: PERMISSION.WRITE_FIRMWARE_PERMISSIONS
                }
            })
            .state('firmwareconfig-import', {
                controller: 'FirmwareConfigImportController',
                controllerAs: 'vm',
                url: '/firmwareconfig/import',
                templateUrl: 'app/xconf/firmware/firmwareconfig/firmwareconfig.import.html',
                data: {
                    permissions: PERMISSION.WRITE_FIRMWARE_PERMISSIONS
                }
            })

            .state('models', {
                controller: 'ModelController',
                controllerAs: 'vm',
                url: '/model/all',
                templateUrl: 'app/xconf/firmware/model/models.html',
                data: {
                    permissions: [PERMISSION.READ_COMMON]
                }
            })
            .state('model-edit', {
                controller: 'ModelEditController',
                controllerAs: 'vm',
                url: '/model/edit/:entityId',
                templateUrl: 'app/xconf/firmware/model/model-edit.html',
                data: {
                    permissions: [PERMISSION.WRITE_COMMON]
                }
            })
            .state('model-import', {
                controller: 'ModelImportController',
                controllerAs: 'vm',
                url: '/model/import',
                templateUrl: 'app/xconf/firmware/model/model.import.html',
                data: {
                    permissions: [PERMISSION.WRITE_COMMON]
                }
            })

            .state('environments', {
                controller: 'EnvironmentsController',
                controllerAs: 'vm',
                url: '/environment/all',
                templateUrl: 'app/xconf/firmware/environment/environments.html',
                data: {
                    permissions: [PERMISSION.READ_COMMON]
                }
            })
            .state('environment-edit', {
                controller: 'EnvironmentEditController',
                controllerAs: 'vm',
                url: '/environment/edit/:entityId',
                templateUrl: 'app/xconf/firmware/environment/environment-edit.html',
                data: {
                    permissions: [PERMISSION.WRITE_COMMON]
                }
            })
            .state('environment-import', {
                controller: 'EnvironmentImportController',
                controllerAs: 'vm',
                url: '/environment/import',
                templateUrl: 'app/xconf/firmware/environment/environment.import.html',
                data: {
                    permissions: [PERMISSION.WRITE_COMMON]
                }
            })
            .state('changelog', {
                controller: 'ChangeLogController',
                controllerAs: 'vm',
                url: '/changelog',
                templateUrl: 'app/shared/changelog/changelog.html',
                data: {
                    permissions: [PERMISSION.VIEW_TOOLS]
                }
            })
            .state('statistics', {
                controller: 'StatisticsController',
                controllerAs: 'vm',
                url: '/statistics',
                templateUrl: 'app/shared/statistics/statistics.html',
                data: {
                    permissions: [PERMISSION.VIEW_TOOLS]
                }
            })
            .state('changes', {
                controller: 'ChangeController',
                controllerAs: 'vm',
                url: '/changes',
                templateUrl: 'app/xconf/changes/change.html',
                data: {
                    permissions: PERMISSION.READ_CHANGES_PERMISSIONS
                }
            })
            .state('permanentprofiles', {
                controller: 'PermanentProfilesController',
                controllerAs: 'vm',
                url: '/permanentprofile/all',
                templateUrl: 'app/xconf/telemetry/permanentprofile/permanentprofiles.html',
                data: {
                    permissions: PERMISSION.READ_TELEMETRY_PERMISSIONS
                }
            })
            .state('permanentprofile-edit', {
                controller: 'PermanentProfileEditController',
                controllerAs: 'vm',
                url: '/permanentprofile/edit/:profileId',
                templateUrl: 'app/xconf/telemetry/permanentprofile/permanentprofile.edit.html',
                data: {
                    permissions: PERMISSION.WRITE_TELEMETRY_PERMISSIONS
                }
            })
            .state('permanentprofile-import', {
                controller: 'PermanentProfileImportController',
                controllerAs: 'vm',
                url: '/permanentprofile/import',
                templateUrl: 'app/xconf/telemetry/permanentprofile/permanentprofile.import.html',
                data: {
                    permissions: PERMISSION.WRITE_TELEMETRY_PERMISSIONS
                }
            })
            .state('telemetrytwoprofiles', {
                controller: 'TelemetryTwoProfilesController',
                controllerAs: 'vm',
                url: '/telemetrytwoprofiles/all',
                templateUrl: 'app/xconf/telemetry/telemetrytwoprofile/telemetrytwoprofiles.html',
                data: {
                    permissions: PERMISSION.READ_TELEMETRY_PERMISSIONS
                }
            })
            .state('telemetrytwoprofile-edit', {
                controller: 'TelemetryTwoProfileEditController',
                controllerAs: 'vm',
                url: '/telemetrytwoprofile/edit/:telemetryProfileId',
                templateUrl: 'app/xconf/telemetry/telemetrytwoprofile/telemetrytwoprofile.edit.html',
                data: {
                    permissions: PERMISSION.WRITE_TELEMETRY_PERMISSIONS
                }
            })
            .state('telemetrytwoprofile-import', {
                controller: 'TelemetryTwoProfileImportController',
                controllerAs: 'vm',
                url: '/telemetrytwoprofile/import',
                templateUrl: 'app/xconf/telemetry/telemetrytwoprofile/telemetrytwoprofile.import.html',
                data: {
                    permissions: PERMISSION.WRITE_TELEMETRY_PERMISSIONS
                }
            })
            .state('targetingrules', {
                controller: 'TargetingRulesController',
                controllerAs: 'vm',
                url: '/targetingrule/all',
                templateUrl: 'app/xconf/telemetry/targetingrule/targetingrules.html',
                data: {
                    permissions: PERMISSION.READ_TELEMETRY_PERMISSIONS
                }
            })
            .state('targetingrule-edit', {
                controller: 'TargetingRuleEditController',
                controllerAs: 'vm',
                url: '/targetingrule/edit/:ruleId',
                templateUrl: 'app/xconf/telemetry/targetingrule/targetingrule.edit.html',
                data: {
                    permissions: PERMISSION.WRITE_TELEMETRY_PERMISSIONS
                }
            })
            .state('targetingrule-import', {
                controller: 'TargetingRuleImportController',
                controllerAs: 'vm',
                url: '/targetingrule/import',
                templateUrl: 'app/xconf/telemetry/targetingrule/targetingrule.import.html',
                data: {
                    permissions: PERMISSION.WRITE_TELEMETRY_PERMISSIONS
                }
            })
            .state('telemetrytwotargetingrules', {
                controller: 'TelemetryTwoTargetingRulesController',
                controllerAs: 'vm',
                url: '/telemetrytwo/targetingrule/all',
                templateUrl: 'app/xconf/telemetry/telemetrytwotargetingrule/telemetrytwotargetingrules.html',
                data: {
                    permissions: PERMISSION.READ_TELEMETRY_PERMISSIONS
                }
            })
            .state('telemetrytwotargetingrule-edit', {
                controller: 'TelemetryTwoTargetingRuleEditController',
                controllerAs: 'vm',
                url: '/telemetrytwo/targetingrule/edit/:ruleId',
                templateUrl: 'app/xconf/telemetry/telemetrytwotargetingrule/telemetrytwotargetingrule.edit.html',
                data: {
                    permissions: PERMISSION.WRITE_TELEMETRY_PERMISSIONS
                }
            })
            .state('telemetrytwotargetingrule-import', {
                controller: 'TelemetryTwoTargetingRuleImportController',
                controllerAs: 'vm',
                url: '/telemetrytwo/targetingrule/import',
                templateUrl: 'app/xconf/telemetry/telemetrytwotargetingrule/telemetrytwotargetingrule.import.html',
                data: {
                    permissions: PERMISSION.WRITE_TELEMETRY_PERMISSIONS
                }
            })
            .state('testpage-telemetry', {
                controller: 'SharedTestPageController',
                controllerAs: 'vm',
                url: '/telemetry/testpage',
                templateUrl: 'app/shared/pages/testpage/testpage.html',
                data: {
                    permissions: PERMISSION.READ_TELEMETRY_PERMISSIONS,
                    pageType: 'TELEMETRY',
                    matchRuleApiUrl: 'api/telemetry/testpage/'
                }
            })
            .state('testpage-telemetrytwo', {
                controller: 'TelemetryTwoTestPageController',
                controllerAs: 'vm',
                url: '/telemetrytwo/testpage',
                templateUrl: 'app/xconf/telemetry/telemetrytwotestpage/telemetrytwotestpage.html',
                data: {
                    permissions: PERMISSION.READ_TELEMETRY_PERMISSIONS
                }
            })
            .state('settings-testpage', {
                controller: 'SharedTestPageController',
                controllerAs: 'vm',
                url: '/settings/testpage',
                templateUrl: 'app/shared/pages/testpage/testpage.html',
                data: {
                    permissions: PERMISSION.READ_DCM_PERMISSIONS,
                    pageType: 'SETTINGS',
                    matchRuleApiUrl: 'api/settings/testpage'
                }
            })

            .state('feature', {
                controller: 'FeatureController',
                controllerAs: 'vm',
                url: '/feature',
                templateUrl: 'app/xconf/rfc/feature/feature.html',
                data: {
                    permissions: PERMISSION.READ_DCM_PERMISSIONS
                }
            })

            .state('feature-edit', {
                controller: 'FeatureEditController',
                controllerAs: 'vm',
                url: '/feature/edit/:featureId',
                templateUrl: 'app/xconf/rfc/feature/feature-edit.html',
                data: {
                    permissions: PERMISSION.WRITE_DCM_PERMISSIONS
                }
            })

            .state('feature-import', {
                controller: 'FeatureImportController',
                controllerAs: 'vm',
                url: '/feature/import',
                templateUrl: 'app/xconf/rfc/feature/feature-import.html',
                data: {
                    permissions: PERMISSION.WRITE_DCM_PERMISSIONS
                }
            })

            .state('featurerule', {
                controller: 'FeatureRuleController',
                controllerAs: 'vm',
                url: '/featurerule',
                templateUrl: 'app/xconf/rfc/featurerule/featurerule.html',
                resolve : {
                    featureRulesSize: function(featureRuleService, alertsService) {
                        return featureRuleService.getFeatureRulesSize().then(
                            function (result) {
                                return result.data;
                            }, function(reason) {
                                alertsService.showError({message: reason.data, title: 'Error'});
                            }
                        );
                    }
                },
                data: {
                    permissions: PERMISSION.READ_DCM_PERMISSIONS
                }
            })

            .state('featurerule-edit', {
                controller: 'FeatureRuleEditController',
                controllerAs: 'vm',
                url: '/featurerule/edit/:featureRuleId?featureRulesSize',
                templateUrl: 'app/xconf/rfc/featurerule/featurerule-edit.html',
                data: {
                    permissions: PERMISSION.WRITE_DCM_PERMISSIONS
                }
            })

            .state('featurerule-import', {
                controller: 'FeatureRuleImportController',
                controllerAs: 'vm',
                url: '/featurerule/import',
                templateUrl: 'app/xconf/rfc/featurerule/featurerule-import.html',
                data: {
                    permissions: PERMISSION.WRITE_DCM_PERMISSIONS
                }
            })

            .state('settingprofiles', {
                controller: 'SettingProfilesController',
                controllerAs: 'vm',
                url: '/settingprofiles/all',
                templateUrl: 'app/xconf/settings/settingprofile/settingprofiles.html',
                data: {
                    permissions: PERMISSION.READ_DCM_PERMISSIONS
                }
            })
            .state('settingprofile-edit', {
                controller: 'SettingProfileEditController',
                controllerAs: 'vm',
                url: '/settingprofile/edit/:profileId',
                templateUrl: 'app/xconf/settings/settingprofile/settingprofile.edit.html',
                data: {
                    permissions: PERMISSION.WRITE_DCM_PERMISSIONS
                }
            })
            .state('settingprofile-import', {
                controller: 'SettingProfileImportController',
                controllerAs: 'vm',
                url: '/settingprofile/import',
                templateUrl: 'app/xconf/settings/settingprofile/settingprofile.import.html',
                data: {
                    permissions: PERMISSION.WRITE_DCM_PERMISSIONS
                }
            })

            .state('settingrules', {
                controller: 'SettingRulesController',
                controllerAs: 'vm',
                url: '/settingrules/all',
                templateUrl: 'app/xconf/settings/settingrule/settingrules.html',
                data: {
                    permissions: PERMISSION.READ_DCM_PERMISSIONS
                }
            })
            .state('settingrule-edit', {
                controller: 'SettingRuleEditController',
                controllerAs: 'vm',
                url: '/settingrule/edit/:ruleId',
                templateUrl: 'app/xconf/settings/settingrule/settingrule.edit.html',
                data: {
                    permissions: PERMISSION.WRITE_DCM_PERMISSIONS
                }
            })
            .state('settingrule-import', {
                controller: 'SettingRuleImportController',
                controllerAs: 'vm',
                url: '/settingrule/import',
                templateUrl: 'app/xconf/settings/settingrule/settingrule.import.html',
                data: {
                    permissions: PERMISSION.WRITE_DCM_PERMISSIONS
                }
            })

            .state('vodsettings', {
                controller: 'VodSettingsController',
                controllerAs: 'vm',
                url: '/vodsettings/all',
                templateUrl: 'app/xconf/dcm/vodsettings/vodsettings.html',
                resolve : {
                    vodSettingsSize: function(vodSettingsService, alertsService) {
                        return vodSettingsService.getSizeOfVodSettings().then(
                            function (result) {
                                return result.data;
                            }, function(reason) {
                                alertsService.showError({message: reason.data, title: 'Error'});
                            }
                        );
                    }
                },
                data: {
                    permissions: PERMISSION.READ_DCM_PERMISSIONS
                }
            })

            .state('formulas', {
                controller: 'FormulasController',
                controllerAs: 'vm',
                url: '/formulas/all',
                templateUrl: 'app/xconf/dcm/formula/formulas.html',
                resolve : {
                    formulasSize: function(formulaService, alertsService) {
                        return formulaService.getSizeOfFormulas().then(
                            function (result) {
                                return result.data;
                            }, function(reason) {
                                alertsService.showError({message: reason.data, title: 'Error'});
                            }
                        );
                    }
                },
                data: {
                    permissions: PERMISSION.READ_DCM_PERMISSIONS
                }
            })
            .state('formula-edit', {
                controller: 'FormulaEditController',
                controllerAs: 'vm',
                url: '/formula/edit/:ruleId?formulasSize',
                templateUrl: 'app/xconf/dcm/formula/formula.edit.html',
                data: {
                    permissions: PERMISSION.WRITE_DCM_PERMISSIONS
                }
            })
            .state('formula-import', {
                controller: 'FormulaImportController',
                controllerAs: 'vm',
                url: '/formula/import',
                templateUrl: 'app/xconf/dcm/formula/formula-import.html',
                resolve : {
                    uploadRepositories: function(uploadRepositoryService, alertsService) {
                        return uploadRepositoryService.getAll().then(function(result) {
                                return result.data;
                            }, function(reason) {
                                alertsService.showError({message: reason.data, title: 'Error'});
                            }
                        );
                    }
                },
                data: {
                    permissions: PERMISSION.WRITE_DCM_PERMISSIONS
                }
            })

            .state('namespacedlist', {
                controller: 'NamespacedListsController',
                controllerAs: 'vm',
                url: '/namespacedlist/:type',
                templateUrl: 'app/xconf/namespacedlist/namespacedlist.html',
                data: {
                    permissions: [PERMISSION.READ_COMMON]
                }
            })
            .state('namespacedlist-edit', {
                controller: 'NamespacedListEditController',
                controllerAs: 'vm',
                url: '/namespacedlist/edit/:id/:editMode/:type',
                templateUrl: 'app/xconf/namespacedlist/namespacedlist-edit.html',
                data: {
                    permissions: [PERMISSION.WRITE_COMMON]
                }
            })
            .state('namespacedlist-import', {
                controller: 'NamespacedListImportController',
                controllerAs: 'vm',
                url: '/namespacedlist/import/:type',
                templateUrl: 'app/xconf/namespacedlist/namespacedlist-import.html',
                data: {
                    permissions: [PERMISSION.WRITE_COMMON]
                }
            })

            .state('firmwarerules', {
                controller: 'FirmwareRulesController',
                controllerAs: 'vm',
                url: '/firmwarerules/:actionType',
                templateUrl: 'app/xconf/firmwarerule/firmwarerules.html',
                data: {
                    permissions: PERMISSION.READ_FIRMWARE_PERMISSIONS

                }
            })
            .state('firmwarerule-add', {
                controller: 'FirmwareRuleEditController',
                controllerAs: 'vm',
                url: '/firmwarerule/add/:actionType/:templateId',
                templateUrl: 'app/xconf/firmwarerule/firmwarerule-edit.html',
                data: {
                    permissions: PERMISSION.WRITE_FIRMWARE_PERMISSIONS

                }
            })
            .state('firmwarerule-edit', {
                controller: 'FirmwareRuleEditController',
                controllerAs: 'vm',
                url: '/firmwarerule/edit/:id',
                templateUrl: 'app/xconf/firmwarerule/firmwarerule-edit.html',
                data: {
                    permissions: PERMISSION.WRITE_FIRMWARE_PERMISSIONS

                }
            })
            .state('firmwarerule-import', {
                controller: 'FirmwareRuleImportController',
                controllerAs: 'vm',
                url: '/firmwarerule/import',
                templateUrl: 'app/xconf/firmwarerule/firmwarerule-import.html',
                data: {
                    permissions: PERMISSION.WRITE_FIRMWARE_PERMISSIONS

                }
            })

            .state('firmwareruletemplates', {
                controller: 'FirmwareRuleTemplatesController',
                controllerAs: 'vm',
                url: '/firmwareruletemplates/:actionType',
                templateUrl: 'app/xconf/firmwareruletemplate/firmwareruletemplates.html',
                data: {
                    permissions: [PERMISSION.READ_FIRMWARE_RULE_TEMPLATES]
                }
            })
            .state('firmwareruletemplate-add', {
                controller: 'FirmwareRuleTemplateEditController',
                controllerAs: 'vm',
                url: '/firmwareruletemplate/add/:actionType?templatesSize',
                templateUrl: 'app/xconf/firmwareruletemplate/firmwareruletemplate-edit.html',
                data: {
                    permissions: [PERMISSION.WRITE_FIRMWARE_RULE_TEMPLATES]
                }
            })
            .state('firmwareruletemplate-edit', {
                controller: 'FirmwareRuleTemplateEditController',
                controllerAs: 'vm',
                url: '/firmwareruletemplate/edit/:id?templatesSize',
                templateUrl: 'app/xconf/firmwareruletemplate/firmwareruletemplate-edit.html',
                data: {
                    permissions: [PERMISSION.WRITE_FIRMWARE_RULE_TEMPLATES]
                }
            })
            .state('firmwareruletemplate-import', {
                controller: 'FirmwareRuleTemplateImportController',
                controllerAs: 'vm',
                url: '/firmwareruletemplate/import',
                templateUrl: 'app/xconf/firmwareruletemplate/firmwareruletemplate-import.html',
                data: {
                    permissions: [PERMISSION.WRITE_FIRMWARE_RULE_TEMPLATES]
                }
            })

            .state('uploadrepositories', {
                controller: 'UploadRepositoriesController',
                controllerAs: 'vm',
                url: '/uploadrepository/all',
                templateUrl: 'app/xconf/dcm/uploadRepository/uploadRepositories.html',
                resolve : {
                    uploadRepositoriesSize: function(uploadRepositoryService, alertsService) {
                        return uploadRepositoryService.getSizeOfUploadRepositories().then(
                            function (result) {
                                return result.data;
                            }, function(reason) {
                                alertsService.showError({message: reason.data, title: 'Error'});
                            }
                        );
                    }
                },
                data: {
                    permissions: PERMISSION.READ_DCM_PERMISSIONS
                }
            })
            .state('uploadrepository-edit', {
                controller: 'UploadRepositoryEditController',
                controllerAs: 'vm',
                url: '/uploadrepository/edit/:uploadRepositoryId/:editMode',
                templateUrl: 'app/xconf/dcm/uploadRepository/uploadRepository.edit.html',
                data: {
                    permissions: PERMISSION.WRITE_DCM_PERMISSIONS
                }
            })
            .state('uploadrepository-import', {
                controller: 'UploadRepositoryImportController',
                controllerAs: 'vm',
                url: '/uploadrepository/import',
                templateUrl: 'app/xconf/dcm/uploadRepository/uploadrepository-import.html',
                data: {
                    permissions: PERMISSION.WRITE_DCM_PERMISSIONS
                }
            })

            .state('testpage-dcm', {
                controller: 'TestPageController',
                controllerAs: 'vm',
                url: '/dcm/testpage',
                templateUrl: 'app/xconf/dcm/testpage/testpage.html',
                data: {
                    permissions: PERMISSION.READ_DCM_PERMISSIONS
                }
            })

            .state('devicesettings', {
                controller: 'DeviceSettingsController',
                controllerAs: 'vm',
                url: '/deviceSettings',
                templateUrl: 'app/xconf/dcm/devicesettings/devicesettings.html',
                resolve : {
                    deviceSettingsSize: function(deviceSettingsService, alertsService) {
                        return deviceSettingsService.getSizeOfDeviceSettings().then(
                            function (result) {
                                return result.data;
                            }, function(reason) {
                                alertsService.showError({message: reason.data, title: 'Error'});
                            }
                        );
                    }
                },
                data: {
                    permissions: PERMISSION.READ_DCM_PERMISSIONS
                }
            })

            .state('testpage-firmware', {
                controller: 'FirmwareTestPageController',
                controllerAs: 'vm',
                url: '/firmware/testpage',
                templateUrl: 'app/xconf/firmware/testpage/testpage.html',
                data: {
                    permissions: PERMISSION.READ_FIRMWARE_PERMISSIONS

                }
            })

            .state('logs', {
                controller: 'LogController',
                controllerAs: 'vm',
                url: '/firmware/log',
                templateUrl: 'app/xconf/firmware/log/log.html',
                data: {
                    permissions: PERMISSION.READ_FIRMWARE_PERMISSIONS

                }
            })

            .state('loguploadsettings', {
                controller: 'LogUploadSettingsController',
                controllerAs: 'vm',
                url: '/logUploadSettings',
                templateUrl: 'app/xconf/dcm/loguploadsettings/loguploadsettings.html',
                resolve : {
                    logUploadSettingsSize: function(logUploadSettingsService, alertsService) {
                        return logUploadSettingsService.getSizeOfLogUploadSettings().then(
                            function (result) {
                                return result.data;
                            }, function(reason) {
                                alertsService.showError({message: reason.data, title: 'Error'});
                            }
                        );
                    }
                },
                data: {
                    permissions: PERMISSION.READ_DCM_PERMISSIONS
                }
            })

            .state('roundrobinfilter', {
                controller: 'RoundRobinFilterController',
                controllerAs: 'vm',
                url: '/roundrobinfilter',
                templateUrl: 'app/xconf/firmware/roundrobinfilter/roundrobinfilter.html',
                data: {
                    permissions: PERMISSION.READ_FIRMWARE_PERMISSIONS

                }
            })
            .state('roundrobinfilter-edit', {
                controller: 'RoundRobinFilterEditController',
                controllerAs: 'vm',
                url: '/roundrobinfilter/edit',
                templateUrl: 'app/xconf/firmware/roundrobinfilter/roundrobinfilter.edit.html',
                data: {
                    permissions: PERMISSION.WRITE_FIRMWARE_PERMISSIONS

                }
            })
            .state('roundrobinfilter-import', {
                controller: 'RoundRobinFilterImportController',
                controllerAs: 'vm',
                url: '/roundrobinfilter/import',
                templateUrl: 'app/xconf/firmware/roundrobinfilter/roundrobinfilter.import.html',
                data: {
                    permissions: PERMISSION.WRITE_FIRMWARE_PERMISSIONS

                }
            })

            .state('reportpage-firmware', {
                controller: 'FirmwareReportPageController',
                controllerAs: 'vm',
                url: '/firmware/reportpage',
                templateUrl: 'app/xconf/firmware/reportpage/reportpage.html',
                data: {
                    permissions: PERMISSION.READ_FIRMWARE_PERMISSIONS

                }
            })

            .state('percentfilter', {
                controller: 'PercentFilterController',
                controllerAs: 'vm',
                url: '/firmware/percentfilter',
                templateUrl: 'app/xconf/firmware/percentfilter/percentfilter.html',
                data: {
                    permissions: PERMISSION.READ_FIRMWARE_PERMISSIONS

                }
            }).state('percentfilter-edit', {
                controller: 'PercentFilterEditController',
                controllerAs: 'vm',
                url: '/firmware/percentfilter-edit/:envModelRuleName',
                templateUrl: 'app/xconf/firmware/percentfilter/percentfilter.edit.html',
                data: {
                    permissions: PERMISSION.WRITE_FIRMWARE_PERMISSIONS

                }
            }).state('percentfilter-import', {
                controller: 'PercentFilterImportController',
                controllerAs: 'vm',
                url: '/firmware/percentfilter-import',
                templateUrl: 'app/xconf/firmware/percentfilter/percentfilter.import.html',
                data: {
                    permissions: PERMISSION.WRITE_FIRMWARE_PERMISSIONS

                }
            }).state('migration', {
                controller: 'MigrationController',
                controllerAs: 'vm',
                url: '/migration',
                templateUrl: 'app/xconf/migration/migration.html',
                data: {
                    permissions: [PERMISSION.VIEW_TOOLS]
                }
            }).state('testpage-rfc', {
                controller: 'SharedTestPageController',
                controllerAs: 'vm',
                url: '/rfc/testpage',
                templateUrl: 'app/shared/pages/testpage/testpage.html',
                data: {
                    permissions: PERMISSION.READ_DCM_PERMISSIONS,
                    pageName: 'RFC Test Page',
                    matchRuleApiUrl: 'api/rfc/test',
                    pageType: 'FEATURE'
                }
            }).state('percentagebean-edit', {
                controller: 'PercentageBeanEditController',
                controllerAs: 'vm',
                url: '/percentagebean-edit/:id',
                templateUrl: 'app/xconf/firmware/percentfilter/percentage-bean.edit.html',
                data: {
                    permissions: PERMISSION.WRITE_FIRMWARE_PERMISSIONS

                }
            }).state('activation-version', {
                controller: 'ActivationVersionController',
                controllerAs: 'vm',
                url: '/activation-version',
                templateUrl: 'app/xconf/firmware/activation-version/activation-versions.html',
                data: {
                    permissions: PERMISSION.READ_FIRMWARE_PERMISSIONS

                }
            }).state('activation-version-edit', {
                controller: 'ActivationVersionEditController',
                controllerAs: 'vm',
                url: '/activation-version-edit/:id',
                templateUrl: 'app/xconf/firmware/activation-version/activation-version.edit.html',
                data: {
                    permissions: PERMISSION.WRITE_FIRMWARE_PERMISSIONS

                }
            }).state('activation-version-import', {
                controller: 'ActivationVersionImportController',
                controllerAs: 'vm',
                url: '/activation-version-import',
                templateUrl: 'app/xconf/firmware/activation-version/activation-version.import.html',
                data: {
                    permissions: PERMISSION.WRITE_FIRMWARE_PERMISSIONS

                }
            })
    }
})();

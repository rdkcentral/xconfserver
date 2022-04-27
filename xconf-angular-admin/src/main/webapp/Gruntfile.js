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
module.exports = function (grunt) {

    require('matchdep').filterDev('grunt-*').forEach(grunt.loadNpmTasks);

    grunt.initConfig({
        pkg: grunt.file.readJSON('package.json'),
        shell: {
            options: {
                stdout: true
            },
            npm_install: {
                command: 'npm install'
            }
        },

        concat: {
            vendor_styles: {
                dest: 'app/compiled/vendor.css',
                src: [
                    'bower_components/bootstrap/dist/css/bootstrap.min.css',
                    'node_modules/remixicon/fonts/remixicon.css',
                    'bower_components/angular-toastr/dist/angular-toastr.min.css',
                    'bower_components/angular-bootstrap/ui-bootstrap-csp.css',
                    'bower_components/angular-dialog-service/dist/dialogs.min.css',
                    'bower_components/ng-table/ng-table.min.css',
                    'bower_components/angular-ui-bootstrap-datetimepicker/datetimepicker.css',
                    'bower_components/angular-ui-select/dist/select.css'
                ]
            },

            vendor: {
                options: {
                    separator: '\n\n'
                },
                dest: 'app/compiled/vendor.js',
                src: [
                    'bower_components/jquery/dist/jquery.js',
                    'bower_components/jquery-ui/jquery-ui.js',
                    'bower_components/angular/angular.js',
                    'bower_components/angular-resource/angular-resource.js',
                    'bower_components/angular-animate/angular-animate.js',
                    'bower_components/angular-route/angular-route.js',
                    'bower_components/angular-ui-router/release/angular-ui-router.js',
                    'bower_components/angular-sanitize/angular-sanitize.js',
                    'bower_components/bootstrap/dist/js/bootstrap.js',
                    'bower_components/angular-bootstrap/ui-bootstrap-tpls.min.js',
                    'bower_components/angular-toastr/dist/angular-toastr.js',
                    'bower_components/angular-dialog-service/dist/dialogs.min.js',
                    'bower_components/angular-dialog-service/dist/dialogs-default-translations.min.js',
                    'bower_components/ng-table/ng-table.js',
                    'bower_components/moment/moment.js',
                    'bower_components/extras.angular.plus/ngplus-overlay.js',
                    'bower_components/bower-javascript-ipv6/lib/browser/jsbn.js',
                    'bower_components/bower-javascript-ipv6/lib/browser/jsbn2.js',
                    'bower_components/bower-javascript-ipv6/ipv6.js',
                    'bower_components/bower-javascript-ipv6/lib/browser/sprintf.js',
                    'bower_components/ngstorage/ngStorage.js',
                    'bower_components/angular-cookies/angular-cookies.js',
                    'bower_components/angular-ui-bootstrap-datetimepicker/datetimepicker.js',
                    'bower_components/underscore/underscore-min.js',
                    'bower_components/angular-file-saver/dist/angular-file-saver.bundle.js',
                    'bower_components/angular-ui-select/dist/select.js',
                    'bower_components/google-diff-match-patch/diff_match_patch.js',
                    'bower_components/angular-diff-match-patch/angular-diff-match-patch.js'
                ]
            },

            xconf_styles: {
                dest: 'app/compiled/xconf.css',
                src: [
                    'app/shared/**/*.css',
                    'app/xconf/**/*.css'
                ]
            },

            landing_styles: {
                dest: 'app/compiled/landing.css',
                src: [
                    'app/landing/**/*.css'
                ]
            },

            xconf_ui: {
                options: {
                    separator: '\n\n'
                },
                dest: 'app/compiled/xconfUI.js',
                src: [
                    'app/xconf/app.module.js',
                    'app/xconf/config/config.module.js',
                    'app/xconf/config/state.config.js',

                    'app/shared/**/*.module.js',
                    'app/shared/**/*.service.js',
                    'app/shared/**/*.directive.js',
                    'app/shared/**/*.controller.js',
                    'app/shared/**/*.filter.js',
                    'app/shared/services/regexp_constants.js',
                    'app/shared/core/core.js',
                    'app/shared/core/requests-service.js',

                    'app/xconf/**/*.module.js',
                    'app/xconf/**/*.service.js',
                    'app/xconf/**/*.controller.js',
                    'app/xconf/**/*.directive.js',
                    'app/xconf/**/*.modal.*.js',
                    'app/xconf/**/*.filter.js'
                ]
            }
        },
		copy: {
			fonts: {
				files: [{
					expand: true,
					dot: true,
                    flatten: true,
					src: [
					    'node_modules/remixicon/fonts/remixicon.ttf',
					    'node_modules/remixicon/fonts/remixicon.woff',
					    'node_modules/remixicon/fonts/remixicon.woff2'
                    ],
					dest: 'app/compiled/'
				}]
			}
		}
    });

    //installation-related
    grunt.registerTask('install', ['update']);
    grunt.registerTask('update', ['shell:npm_install', 'concat', 'copy']);

};

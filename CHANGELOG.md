
# Change Log
All notable changes to XConf project will be documented in this file.

## [3.6] - 2021-03-16

### Added
ICFAR-72	Add API for SSR x1-sign-redirect script<br>
ICFAR-73	Add API for SSR sky-sign-redirect script<br>
ICFAR-74	Add API for SSR upload_dump script<br>
ICFAR-240	Add the SSR API "rdkb_snmp.cgi" script<br>
ICFAR-281	Add the SSR API "rdkvlogupload.cgi" script<br>
ICFAR-311	Remove Legacy Code From Firmware Section<br>
ICFAR-312	Remove Legacy Code From DCM And RFC Sections<br>
ICFAR-380	XConf - Deployment of 3.6 release<br>

### Changed
ICFAR-80	Make Environment Type Optional For Percent Filters<br>
ICFAR-261	Telemetry 2.0 - Few UI Changes<br>
ICFAR-343	Upgrade jackson-databind Package<br>
ICFAR-345	XCONF DS - Use XBO Titan<br>
ICFAR-347	Upgrade Spring Packages<br>

### Fixed
ICFAR-349	Telemetry 2.0 - Add Validation When Deleting Profiles Attached to Rules<br>
ICFAR-351	TFTP protocol and URL is not set in Upload Repository field<br>
ICFAR-360	Fix jacoco-maven-plugin exceptions during XConf OSS build<br>
ICFAR-363	Rework SMART Logging Processing<br>

## [3.5] - 2021-02-03

### Added
ICFAR-36	Telemetry 2.0 Profile<br>
ICFAR-37	Telemetry 2.0 Targeting Rules<br>
ICFAR-38	Telemetry 2.0 API<br>
ICFAR-90	Integrate SMART into XCONF<br>
ICFAR-169	Add New Application Type for Sky<br>
ICFAR-185	Telemetry 2.0 Test Page<br>
ICFAR-280	XConf Admin Terraform scripts to read Jetty vault configs to set up `start.ini`<br>

### Fixed
ICFAR-170	Created/Imported Features Are Not Unique To An Application Type When Using APIs<br>
ICFAR-260	Telemetry 2.0 - Bound Profiles Not Showing On Original Telemetry Rules UI Page<br>
ICFAR-267	XConf OSS build failure<br>
ICFAR-304	406 Response Code Returned For Some DS APIs<br>
ICFAR-307	Issue with SMART DS Metric TFL Logs<br>

### Investigated 
ICFAR-76	Investigate Using IPv6 in AWS<br>
ICFAR-354	Investigate Service Failures During Reboot Window<br>
 
## [3.4] - 2020-11-16

### Added
XAPPS-5827	Add Tagging Service Support For More Xconf Sections	Task<br>
XAPPS-6390	Implement AMV backend API changes<br>
XAPPS-6484	XConf - Add API for SSR rdkb script<br>
XAPPS-6545	XCONF - Add API for Model Update<br>
ICFAR-41	Resolve RDK Code Audit Issues<br>
ICFAR-70	Add API for SSR S3 script<br>
ICFAR-71	Put All SSR Functionality Into XCONF DS<br>
ICFAR-156	Resolve RDK Code Audit Issues 2<br>
ICFAR-200	Packer and Terraform Setup<br>
ICFAR-201	Packer Build Setup<br>
ICFAR-202	Terraform Build Setup<br>
ICFAR-203	Implement Reading Properties Internally<br>
ICFAR-206	XCONF Admin - Terraform Build Setup<br>
ICFAR-207	XCONF Admin - Packer Build Setup<br>

### Changed
XAPPS-4563	Percent Filter Create/Edit Rule UI Page Changes<br>
XAPPS-5242	Normalize error messages per entity<br>
XAPPS-5859	Stop Having Telemetry Rule Calls To Tagging Service Include A Dummy MAC Address<br>

### Fixed
XAPPS-6439	XConf - Only Apply AMV Rule for Percent Filter<br>
XAPPS-6557	MAC List update was not sync across hosts<br>
XAPPS-6630	XML Response Is No Longer Ignoring Null Fields<br>
ICFAR-84	XML Response Is No Longer Ignoring Null Fields (Admin Service)<br>
ICFAR-216	A Few UI Icons Are Now Missing<br>

### Investigated
XAPPS-5591	Latency Spike During Reboot Window<br>
ICFAR-76	Investigate Using IPv6 in AWS<br>
ICFAR-214	runningFirmwareVersion/info API should work only with percent rules<br>
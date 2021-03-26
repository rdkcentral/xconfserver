
# Change Log
All notable changes to XConf project will be documented in this file.

## [3.6] - 2021-03-16

### Added
- ICFAR-72	Add API for SSR x1-sign-redirect script
- ICFAR-73	Add API for SSR sky-sign-redirect script
- ICFAR-74	Add API for SSR upload_dump script
- ICFAR-240	Add the SSR API "rdkb_snmp.cgi" script
- ICFAR-281	Add the SSR API "rdkvlogupload.cgi" script
- ICFAR-311	Remove Legacy Code From Firmware Section
- ICFAR-312	Remove Legacy Code From DCM And RFC Sections
- ICFAR-380	XConf - Deployment of 3.6 release

### Changed
- ICFAR-80	Make Environment Type Optional For Percent Filters
- ICFAR-261	Telemetry 2.0 - Few UI Changes
- ICFAR-343	Upgrade jackson-databind Package
- ICFAR-345	XCONF DS - Use XBO Titan
- ICFAR-347	Upgrade Spring Packages

### Fixed
- ICFAR-349	Telemetry 2.0 - Add Validation When Deleting Profiles Attached to Rules
- ICFAR-351	TFTP protocol and URL is not set in Upload Repository field
- ICFAR-360	Fix jacoco-maven-plugin exceptions during XConf OSS build
- ICFAR-363	Rework SMART Logging Processing

## [3.5] - 2021-02-03

### Added
- ICFAR-36	Telemetry 2.0 Profile
- ICFAR-37	Telemetry 2.0 Targeting Rules
- ICFAR-38	Telemetry 2.0 API
- ICFAR-90	Integrate SMART into XCONF
- ICFAR-169	Add New Application Type for Sky
- ICFAR-185	Telemetry 2.0 Test Page
- ICFAR-280	XConf Admin Terraform scripts to read Jetty vault configs to set up `start.ini`

### Fixed
- ICFAR-170	Created/Imported Features Are Not Unique To An Application Type When Using APIs
- ICFAR-260	Telemetry 2.0 - Bound Profiles Not Showing On Original Telemetry Rules UI Page
- ICFAR-267	XConf OSS build failure
- ICFAR-304	406 Response Code Returned For Some DS APIs
- ICFAR-307	Issue with SMART DS Metric TFL Logs

### Investigated 
- ICFAR-76	Investigate Using IPv6 in AWS
- ICFAR-354	Investigate Service Failures During Reboot Window
 
## [3.4] - 2020-11-16

### Added
- XAPPS-5827	Add Tagging Service Support For More Xconf Sections	Task
- XAPPS-6390	Implement AMV backend API changes
- XAPPS-6484	XConf - Add API for SSR rdkb script
- XAPPS-6545	XCONF - Add API for Model Update
- ICFAR-41	Resolve RDK Code Audit Issues
- ICFAR-70	Add API for SSR S3 script
- ICFAR-71	Put All SSR Functionality Into XCONF DS
- ICFAR-156	Resolve RDK Code Audit Issues 2
- ICFAR-200	Packer and Terraform Setup
- ICFAR-201	Packer Build Setup
- ICFAR-202	Terraform Build Setup
- ICFAR-203	Implement Reading Properties Internally
- ICFAR-206	XCONF Admin - Terraform Build Setup
- ICFAR-207	XCONF Admin - Packer Build Setup

### Changed
- XAPPS-4563	Percent Filter Create/Edit Rule UI Page Changes
- XAPPS-5242	Normalize error messages per entity
- XAPPS-5859	Stop Having Telemetry Rule Calls To Tagging Service Include A Dummy MAC Address

### Fixed
- XAPPS-6439	XConf - Only Apply AMV Rule for Percent Filter
- XAPPS-6557	MAC List update was not sync across hosts
- XAPPS-6630	XML Response Is No Longer Ignoring Null Fields
- ICFAR-84	XML Response Is No Longer Ignoring Null Fields (Admin Service)
- ICFAR-216	A Few UI Icons Are Now Missing

### Investigated
- XAPPS-5591	Latency Spike During Reboot Window
- ICFAR-76	Investigate Using IPv6 in AWS
- ICFAR-214	runningFirmwareVersion/info API should work only with percent rules
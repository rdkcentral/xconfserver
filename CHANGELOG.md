
# Change Log
All notable changes to XConf project will be documented in this file.

## [3.6] - 2021-03-16

### Added
- ICFAR-311	Remove Legacy Code From Firmware Section  [#24](https://github.com/rdkcentral/xconfserver/pull/24)
- ICFAR-312	Remove Legacy Code From DCM And RFC Sections [#36](https://github.com/rdkcentral/xconfserver/pull/36)
- ICFAR-380	XConf - Deployment of 3.6 release

### Changed
- ICFAR-80	Make Environment Type Optional For Percent Filters [#39](https://github.com/rdkcentral/xconfserver/pull/39)
- ICFAR-261	Telemetry 2.0 - Few UI Changes [#50](https://github.com/rdkcentral/xconfserver/pull/50), [#29](https://github.com/rdkcentral/xconfserver/pull/29)
- ICFAR-347	Upgrade Spring Packages [#32](https://github.com/rdkcentral/xconfserver/pull/32)

### Fixed
- ICFAR-349	Telemetry 2.0 - Add Validation When Deleting Profiles Attached to Rules [#41](https://github.com/rdkcentral/xconfserver/pull/41)
- ICFAR-351	TFTP protocol and URL is not set in Upload Repository field [#34](https://github.com/rdkcentral/xconfserver/pull/34)
- ICFAR-360	Fix jacoco-maven-plugin exceptions during XConf OSS build [#35](https://github.com/rdkcentral/xconfserver/pull/35)

## [3.5] - 2021-02-03

### Added
- ICFAR-36	Telemetry 2.0 Profile [#5](https://github.com/rdkcentral/xconfserver/pull/5)
- ICFAR-37	Telemetry 2.0 Targeting Rules [#5](https://github.com/rdkcentral/xconfserver/pull/5)
- ICFAR-38	Telemetry 2.0 API [#5](https://github.com/rdkcentral/xconfserver/pull/5)
- ICFAR-169	Add New Application Type for Sky [#20](https://github.com/rdkcentral/xconfserver/pull/20)
- ICFAR-185	Telemetry 2.0 Test Page [#5](https://github.com/rdkcentral/xconfserver/pull/5)
- ICFAR-280	XConf Admin Terraform scripts to read Jetty vault configs to set up `start.ini`

### Fixed
- ICFAR-170	Created/Imported Features Are Not Unique To An Application Type When Using APIs [#18](https://github.com/rdkcentral/xconfserver/pull/18)
- ICFAR-260	Telemetry 2.0 - Bound Profiles Not Showing On Original Telemetry Rules UI Page [#13](https://github.com/rdkcentral/xconfserver/pull/13)
- ICFAR-267	XConf OSS build failure [#10](https://github.com/rdkcentral/xconfserver/pull/10)
- ICFAR-304	406 Response Code Returned For Some DS APIs

### Investigated 
- ICFAR-76	Investigate Using IPv6 in AWS
- ICFAR-354	Investigate Service Failures During Reboot Window
 
## [3.4] - 2020-11-16

### Added
- XAPPS-6390	Implement AMV backend API changes
- XAPPS-6545	XCONF - Add API for Model Update
- ICFAR-41	Resolve RDK Code Audit Issues [#1](https://github.com/rdkcentral/xconfserver/pull/1)
- ICFAR-156	Resolve RDK Code Audit Issues 2 [#1](https://github.com/rdkcentral/xconfserver/pull/1)
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
- ICFAR-216	A Few UI Icons Are Now Missing [#2](https://github.com/rdkcentral/xconfserver/pull/2)
- ICFAR-214	runningFirmwareVersion/info API should work only with percent rules [#2](https://github.com/rdkcentral/xconfserver/pull/2)

### Investigated
- XAPPS-5591	Latency Spike During Reboot Window
- ICFAR-76	Investigate Using IPv6 in AWS

# Table of Contents
<!--ts-->
* [XConf Overview](#xconf-overview)
* [Architecture](#architecture)
* [Run application](#run-application)
    * [Local run](#local-run)
    * [Using docker-compose](#using-docker-compose)
* [Configuration](#configuration)
    * [mTLS support](#mtls-support)
* [Endpoints](#endpoints)
    * [XConf Primary API](#xconf-primary-api)
    * [Device Configuration Manager](#device-configuration-manager-dcm)
    * [RDK Feature Control](#rdk-feature-control-rfc)
* [Examples](#examples)
    * [Get STB firmware version](#get-stb-firmware-version)
    * [Get STB settings](#get-stb-settings)
    * [Get Feature Settings](#get-feature-settings)
    * [Rule Structure](#rule-structure)
<!--te-->


#XConf Overview

***Important Note about Licensing:
The XConf Server uses the Bootstrap framework.  When building XConf Server, Bootstrap and other modules are pulled in by NPM.  Bootstrap contains a small set of Glyphicons.  According to the Bootstrap license, Bootstrap and the included Glyphicons are available to use commercially for free.  However, any party building and using XConf Server should review the licenses and make their own determination.

Xconf is slated to be the single entity for managing firmware on set-top boxes both in the field and in various 
warehouses and test environments.

Xconf's primary purpose is to tell set-top boxes (STBs) what version of firmware they should be running. 
Xconf does not push firmware to the STB, nor is not involved in any way in the actual download / upgrade process. 
It simply tells the STB which version to use. Xconf also tells STBs when, where (host), and how (protocol) to get the firmware.

Xconf consists of two web applications, Xconf DataService and Xconf Admin. 
Xconf DataService is the app that the STBs talk to. Xconf Admin allows humans to enter all the information necessary 
for Xconf to provide the correct information to STBs.

The interface between STBs and Xconf is simple. STBs make HTTP requests to Xconf sending information like MAC address, 
environment, and model. Xconf then applies various rules to determine which firmware information to return. 
The information is returned in JSON format.

## Architecture
Repository contains both projects: admin and dataservice.

* Java 8
* Spring MVC
* Angular 1
* Cassandra DB

## Run application
### Local run
Related to `xconf-dataservice` and `xconf-angular-admin`
1. Run cassandra DB and create a corresponding scheme using `schema.cql` file from `xconf-angular-admin` or `xconf-dataservice` module.
2. Use sample.service.properties to add/override specific environments properties.
3. Create service.properties file with needed application properties in `resources` folder. 
4. Build project:
```shell
mvn clean install
```
5. Run application with the following command:
```shell
mvn jetty:run -DappConfig=${path-to-service-properties} -f pom.xml
```

or using Intellij IDEA:
create maven task, put following command line option:
```shell
jetty:run -DappConfig=${path-to-service-properties} -f pom.xml
```

NOTE: XConf UI is compiled using `frontend-maven-plugin` during `run` and `install` phase

### Using docker or podman-compose

[docker-compose](https://docs.docker.com/compose/) or
[podman-compose](https://docs.podman.io/en/latest/markdown/podman-compose.1.html)*
can be used to quickly create an XConf environment for testing purposes.
This includes a working Cassandra database server, the Angular-based frontend
and backend API server.

* `podman-compose` users: please use a recent version of podman-compose (at least `v1.4.0`),
as earlier versions do not implement the healthcheck based service dependencies
needed to defer service start until the Cassandara DB is ready.

To bring up the test environment:

```
# docker-compose
docker-compose up -d
# or podman-compose
podman-compose up -d
```

The status of the test environment can be viewed with the `ps` subcommand:

```
$ docker-compose ps
           Name                         Command                  State                            Ports
-----------------------------------------------------------------------------------------------------------------------------
xconfserver_cassandra_1      docker-entrypoint.sh cassa ...   Up (healthy)   7000/tcp, 7001/tcp, 7199/tcp, 9042/tcp, 9160/tcp
xconfserver_initdb_1         /usr/bin/init.sh                 Up (healthy)   7000/tcp, 7001/tcp, 7199/tcp, 9042/tcp, 9160/tcp
xconfserver_xconfangular_1   /docker-entrypoint.sh java ...   Up             0.0.0.0:19093->8080/tcp,:::19093->8080/tcp
xconfserver_xconfdata_1      /docker-entrypoint.sh java ...   Up             0.0.0.0:19092->8080/tcp,:::19092->8080/tcp
```

The frontend application will be port forwarded from port `19093`, while the API will be forwarded from port `19092`.

The default administrator credentials are `admin/admin`.

Please be aware that the docker-compose file included is only intended for local development use,
the security settings on the included components are not appropriate for an internet-facing instance.

## Configuration
### mTLS support
To enable mTLS support to connect to cassandra endpoint following properties should be used:
```
cassandra.useSsl=true
ssl.truststore.path=/path/to/your/truststore/file
ssl.truststore=Base64 encoded truststore
ssl.truststore.password=truststore-password
ssl.keystore.path=/path/to/your/keystore/file
ssl.keystore=Base64 encoded keystore
ssl.keystore.password=keystore-password
```
`ssl.truststore.path` or `ssl.truststore` can be used at the same time. Priority is `ssl.truststore`.
If `ssl.truststore` is set `ssl.truststore.path` is ignored.
The same is applied for `ssl.keystore.path` and `ssl.keystore`. Priority is `ssl.keystore`.
If `ssl.keystore` is set `ssl.keystore.path` is ignored.
## Endpoints

### XConf Primary API

| PATH | METHOD | MAIN QUERY PARAMETERS | DESCRIPTION |
|------|--------|-----------------------|-------------|
| `/xconf/swu/{applicationType}` | `GET/POST` |`eStbMac`,<br>`ipAddress`,<br>`env`,<br>`model`,<br>`firmwareVersion`,<br>`partnerId`,<br>`accountId`,<br>`{tag name}` - any tag from Tagging Service,<br>`controllerId`,<br>`channelMapId`,<br>`vodId`| Returns firmwareVersion to STB box <br> `{applicationType}` - for now supported `stb`, `xhome`, `rdkcloud` |
| `/xconf/swu/bse` | `GET/PUT/POST` | `ipAddress` - required | Returns BSE configuration |
| `/estbfirmware/changelogs` | `GET/PUT/POST` | `mac` - required | Returns logs of the communication between xconf and the given stb where xconf instructed stb to get different firmware |
| `/estbfirmware/lastlog` | `GET/PUT/POST` | `mac` - required | Returns log of the last communication between xconf and the given stb |
| `/estbfirmware/checkMinimumFirmware` | `GET/PUT/POST` | `mac` - required | Return if device has Minimum Firmware version |
| `/xconf/{applicationType}`<br>`/runningFirmwareVersion/info` | `GET/PUT/POST` | `mac` - required | Return if device has Activation Minimum Firmware and Minimum Firmware version |

#### Headers 
For `/xconf/swu/{applictionType}` API: <br>
`HA-Haproxy-xconf-http` to indicate if connection is secure


### Device Configuration Manager (DCM)

Remote devices like set top boxes and DVRs have settings to control certain activities. For instance, STBs need to know when to upload log files, or when to check for a new firmware update. In order to remotely manage a large population of devices, we need a solution that lets support staff define instructions and get the instructions to the devices.

| PATH | METHOD | MAIN QUERY PARAMETERS | DESCRIPTION |
|------|--------|-----------------------|-------------|
| `/loguploader/getSettings`<br>`/{applicationType}` | `GET/POST` | `estbMacAddress`,<br>`ipAddress`,<br>`env`,<br>`model`,<br>`firmwareVersion`,<br>`partnerId`,<br>`accountId`,<br>`{tag name}` - any tag from Tagging Service,<br>`checkNow` - boolean,<br>`version`,<br>`settingsType`,<br>`ecmMacAddress`,<br>`controllerId`,<br>`channelMapId`,<br>`vodId` | Returns settings to STB box <br> `{applicationType}` - for now supported `stb`, `xhome`, `rdkcloud`, <br>field is optional and `stb` application is used by default |
| `/loguploader/getT2Settings`<br>`/{applicatoionType}` | `GET` | The same as a previous | Returns telemetry configuration in the new format. If the component name has been defined for an entry, <br>the response will be in the new format. The second and third columns for that entry will not be used in the response. <br>The content field comes from the fifth column (component name). The type field will be a constant string `<event>` |
| `/loguploader/getTelemetryProfiles`<br>`/{applicationType}` | `GET` | The same as a previous | Returns Telemetry 2.0 profiles based on Telemetry 2.0 rules |

### RDK Feature Control (RFC)

| PATH | METHOD | MAIN QUERY PARAMETERS | DESCRIPTION |
|------|--------|-----------------------|-------------|
| `/featureControl/getSettings`<br>`/{applicationType}` | `GET/POST` | `estbMacAddress`,<br>`ipAddress`,<br>`env`,<br>`model`,<br>`firmwareVersion`,<br>`partnerId`,<br>`accountId`,<br>`{tag name}` - any tag from Tagging Service,<br>`ecmMacAddress`,<br>`controllerId`,<br>`channelMapId`,<br>`vodId`| Returns enabled/disable features <br> `{applicationType}` - for now supported `stb`, `xhome`, `rdkcloud`, field is optional and `stb` application is used by default |

#### Headers
`HA-Haproxy-xconf-http` - indicate if connection is secure <br>
`configsethash` - hash of previous response to return `304 Not Modified` http status

## Examples

### Get STB firmware version
#### Request
```shell script
curl --location --request GET 'https://${xconfds-path}/xconf/swu/stb?eStbMac=AA:AA:AA:AA:AA:AA&env=DEV&model=TEST_MODEL&ipAddress=10.10.10.10'
```
#### Positive response
```json
{
    "firmwareDownloadProtocol": "http",
    "firmwareFilename": "FIRMWARE-NAME.bin",
    "firmwareLocation": "http://ssr.ccp.xcal.tv/cgi-bin/x1-sign-redirect.pl?K=10&F=stb_cdl",
    "firmwareVersion": "FIRMWARE_VERSION",
    "rebootImmediately": true
}
```

### Get STB settings
#### Request
```shell script
curl --location --request GET 'https://${xconfds-path}/loguploader/getSettings/stb?estbMacAddress=AA:AA:AA:AA:AA:AA&env=DEV&model=TEST_MODEL&ipAddress=10.10.10.10'
```
#### Positive response
```json
{
  "urn:settings:GroupName": "TEST_GROUP_NAME_1",
  "urn:settings:CheckOnReboot": false,
  "urn:settings:CheckSchedule:cron": "19 7 * * *",
  "urn:settings:CheckSchedule:DurationMinutes": 180,
  "urn:settings:LogUploadSettings:Message": null,
  "urn:settings:LogUploadSettings:Name": "LUS-NAME",
  "urn:settings:LogUploadSettings:NumberOfDays": 0,
  "urn:settings:LogUploadSettings:UploadRepositoryName": "TEST-NAME",
  "urn:settings:LogUploadSettings:UploadRepository:URL": "https://upload-repository-url.com",
  "urn:settings:LogUploadSettings:UploadRepository:uploadProtocol": "HTTP",
  "urn:settings:LogUploadSettings:UploadOnReboot": false,
  "urn:settings:LogUploadSettings:UploadImmediately": false,
  "urn:settings:LogUploadSettings:upload": true,
  "urn:settings:LogUploadSettings:UploadSchedule:cron": "8 20 * * *",
  "urn:settings:LogUploadSettings:UploadSchedule:levelone:cron": null,
  "urn:settings:LogUploadSettings:UploadSchedule:leveltwo:cron": null,
  "urn:settings:LogUploadSettings:UploadSchedule:levelthree:cron": null,
  "urn:settings:LogUploadSettings:UploadSchedule:DurationMinutes": 420,
  "urn:settings:VODSettings:Name": null,
  "urn:settings:VODSettings:LocationsURL": null,
  "urn:settings:VODSettings:SRMIPList": null,
  "urn:settings:TelemetryProfile": {
    "id": "c34518e8-0af5-4524-b96d-c2efb1904458",
    "telemetryProfile": [
      {
        "header": "MEDIA_ERROR_NETWORK_ERROR",
        "content": "NETWORK ERROR(10)",
        "type": "receiver.log",
        "pollingFrequency": "0"
      }
    ],
    "schedule": "*/15 * * * *",
    "expires": 0,
    "telemetryProfile:name": "RDKV_DEVprofile",
    "uploadRepository:URL": "https://upload-repository-host.tv",
    "uploadRepository:uploadProtocol": "HTTP"
  }
}
```

### Get Feature Settings
#### Request
```shell
curl --location --request GET 'http://${xconfds-path}/featureControl/getSettings?estbMacAddress=AA:AA:AA:AA:AA:AA' \
--header 'Accept: application/json'
```

#### Positive Response
```json
{
    "featureControl": {
        "features": [
            {
                "name": "TEST_INSPECTOR",
                "effectiveImmediate": false,
                "enable": true,
                "configData": {},
                "featureInstance": "TEST_INSPECTOR"
            }
        ]
    }
}
```

### Rule Structure
There are 6 different rule types: FirmwareRule, DCM Rule (Formula), TelemetryRule, TelemetryTwoRule, SettingRule and FeatureRule (RFC Rule).

Extended from `Rule` object: `DCM Rule`, `TelemetryRule`, `TelemetryTwoRule`. It means that rule object itself has rule structure, corresponding rule fields like `negated`, `condition`, `compoundParts` and `relation` are located in root json object itself.

Otherwise there is rule field.

`TelemetryRule` json extended by `Rule` object:

```json
{
    "negated": false,
    "compoundParts": [
    {
        "negated": false,
        "condition":
        {
            "freeArg":
            {
                "type": "STRING",
                "name": "model"
            },
            "operation": "IS",
            "fixedArg":
            {
                "bean":
                {
                    "value":
                    {
                        "java.lang.String": "TEST_MODEL1"
                    }
                }
            }
        },
        "compoundParts": []
    },
    {
        "negated": false,
        "relation": "OR",
        "condition":
        {
            "freeArg":
            {
                "type": "STRING",
                "name": "model"
            },
            "operation": "IS",
            "fixedArg":
            {
                "bean":
                {
                    "value":
                    {
                        "java.lang.String": "TEST_MODEL2"
                    }
                }
            }
        },
        "compoundParts": []
    }],
    "boundTelemetryId": "ad10dd05-d2ff-4d00-8f52-b0ca6956cde6",
    "id": "fb4210ac-8187-4cba-9301-eb8f27fcdaa8",
    "name": "Arris_SVG",
    "applicationType": "stb"
}
```


`FeatureRule` json, which contains `Rule` object
```json
{
    "id": "018bfb79-4aaf-426e-9e45-17e26d52ad49",
    "name": "Test",
    "rule":
    {
        "negated": false,
        "condition":
        {
            "freeArg":
            {
                "type": "STRING",
                "name": "estbMacAddress"
            },
            "operation": "IS",
            "fixedArg":
            {
                "bean":
                {
                    "value":
                    {
                        "java.lang.String": "AA:AA:AA:AA:AA:AA"
                    }
                }
            }
        },
        "compoundParts": []
    },
    "priority": 13,
    "featureIds": ["68add112-cdc9-47be-ae3b-86e753c8d23e"],
    "applicationType": "stb"
}
```

#### Rule Object
There are following fields there:<br>
`negated` - means condition is with `not` operand.<br>
`condition` - key and value statement.<br>
`compoundParts` - list with multiple conditions.<br>
`relation` - operation between multiple conditions, possible values `OR`, `AND`.

#### Condition structure
Each condition has `freeArg` and `fixedArg` field.
freeArg typed key.
fixedArg value meaning.

If rule has only one condition there are no `compoundParts`, `relation` field is empty.
If there are more than one condition - they are located in `compoundParts` object. First condition does not have any relation, next one has a relation.

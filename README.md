XConf Open Source Software

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
* Java 8
* Spring MVC
* Angular 1
* Cassandra DB

## Run app
### Local run
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

## Endpoints

### XConf Primary API

| PATH | METHOD | HEADERS | MAIN QUERY PARAMETERS | DESCRIPTION |
|------|--------|---------|-----------------------|-------------|
| `/xconf/swu/{applicationType}` | `GET/POST` | `HA-Haproxy-xconf-http` - indicate if connection is secure |`eStbMac`<br>`ipAddress`<br>`env`<br>`model`<br>`firmwareVersion`<br>`partnerId`<br>`accountId`<br>`{tag name}` - any tag from Tagging Service<br><br>`controllerId`<br>`channelMapId`<br>`vodId`| Returns firmwareVersion to STB box <br> `{applicationType}` - for now supported `stb`, `xhome`, `rdkcloud` |
| `/xconf/swu/bse` | `GET/PUT/POST` | | `ipAddress` - required | Returns BSE configuration |
| `/estbfirmware/changelogs` | `GET/PUT/POST` | | `mac` - required | Returns logs of the communication between xconf and the given stb where xconf instructed stb to get different firmware |
| `/estbfirmware/lastlog` | `GET/PUT/POST` | | `mac` - required | Returns log of the last communication between xconf and the given stb |
| `/estbfirmware/checkMinimumFirmware` | `GET/PUT/POST` | | `mac` - required | Return if device has Minimum Firmware version |
| `/xconf/{applicationType}/runningFirmwareVersion/info` | `GET/PUT/POST` | | `mac` - required | Return if device has Activation Minimum Firmware and Minimum Firmware version |

### Device Configuration Manager (DCM)

Remote devices like set top boxes and DVRs have settings to control certain activities. For instance, STBs need to know when to upload log files, or when to check for a new firmware update. In order to remotely manage a large population of devices, we need a solution that lets support staff define instructions and get the instructions to the devices.

| PATH | METHOD | MAIN QUERY PARAMETERS | DESCRIPTION |
|------|--------|-----------------------|-------------|
| `/loguploader/getSettings/{applicationType}` | `GET/POST` | `estbMacAddress`<br>`ipAddress`<br>`env`<br>`model`<br>`firmwareVersion`<br>`partnerId`<br>`accountId`<br>`{tag name}` - any tag from Tagging Service<br>`checkNow` - boolean<br>`version`<br>`settingsType` `ecmMacAddress`<br>`controllerId`<br>`channelMapId`<br>`vodId` | Returns settings to STB box <br> `{applicationType}` - for now supported `stb`, `xhome`, `rdkcloud`, field is optional and `stb` application is used by default |
| `/loguploader/getT2Settings/{applicatoionType}` | `GET` | The same as a previous | Returns telemetry configuration in the new format. If the component name has been defined for an entry, the response will be in the new format. The second and third columns for that entry will not be used in the response. The content field comes from the fifth column (component name). The type field will be a constant string `<event>` |
| `/loguploader/getTelemetryProfiles/{applicationType}` | `GET` | The same as a previous | Returns Telemetry 2.0 profiles based on Telemetry 2.0 rules |

### RDK Feature Control (RFC)

| PATH | METHOD | HEADERS | MAIN QUERY PARAMETERS | DESCRIPTION |
|------|--------|---------|-----------------------|-------------|
| `/featureControl/getSettings/{applicationType}` | `GET/POST` | `HA-Haproxy-xconf-http` - indicate if connection is secure<br>`configsethash` - hash of previous response to return `304 Not Modified` http status | `estbMacAddress`<br>`ipAddress`<br>`env`<br>`model`<br>`firmwareVersion`<br>`partnerId`<br>`accountId`<br>`{tag name}` - any tag from Tagging Service<br>`ecmMacAddress`<br>`controllerId`<br>`channelMapId`<br>`vodId`| Returns enabled/disable features <br> `{applicationType}` - for now supported `stb`, `xhome`, `rdkcloud`, field is optional and `stb` application is used by default |

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
  "urn:settings:LogUploadSettings:Message": "Don't upload your logs, but check for updates on this schedule.",
  "urn:settings:LogUploadSettings:Name": null,
  "urn:settings:LogUploadSettings:NumberOfDays": null,
  "urn:settings:LogUploadSettings:UploadRepositoryName": null,
  "urn:settings:LogUploadSettings:UploadOnReboot": null,
  "urn:settings:LogUploadSettings:UploadImmediately": false,
  "urn:settings:LogUploadSettings:upload": false,
  "urn:settings:LogUploadSettings:UploadSchedule:cron": null,
  "urn:settings:LogUploadSettings:UploadSchedule:levelone:cron": null,
  "urn:settings:LogUploadSettings:UploadSchedule:leveltwo:cron": null,
  "urn:settings:LogUploadSettings:UploadSchedule:levelthree:cron": null,
  "urn:settings:LogUploadSettings:UploadSchedule:DurationMinutes": null,
  "urn:settings:VODSettings:Name": null,
  "urn:settings:VODSettings:LocationsURL": null,
  "urn:settings:VODSettings:SRMIPList": null,
  "urn:settings:TelemetryProfile": {
    "id": "b56ea714-1603-4f7a-b679-b87438159bac",
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
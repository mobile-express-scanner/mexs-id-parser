![mexs_icon](https://user-images.githubusercontent.com/68763346/88448044-1c037580-ce6c-11ea-8d29-2f5c2351ab2f.png)

# MExS ID Parser
A library for parsing text data from live/scanned identity documents into an `IdData` object.

## Background
Mobile Express Scanner (MExS) was developed to support the swab and serological testing operations conducted by Singapore’s Multi-Ministry Taskforce against the Coronavirus Disease 2019 (COVID-19). MExS uses optical character recognition (OCR) to extract text data from identity documents and applies an algorithm to parse specific data fields of interest (such as name and ID number). Paired with a commercial-off-the-shelf label printer, labels can then be printed seamlessly and used on the test request forms, sample vials, and sample manifest list. The deployment of MExS has decreased the time taken for test registration, minimised errors by automating data extraction, and shortened the interaction time between our healthcare personnel and patients with unknown COVID-19 status.

Although MExS was primarily developed for test registration in support of COVID-19 operations, the team sees potential applications beyond simply a test registration system. Since deployment, the team has also received interest from various organisations to expand MExS for other uses. The team hopes that by open sourcing the MExS ID Parser library, other organisations can leverage the library to build similar OCR-based applications to suit their own needs.

MExS was developed by Army Engineers from [Headquarters Maintenance & Engineering Support, Singapore Army](https://www.mindef.gov.sg/web/portal/army/our-forces/formations/formations-detail/maintenance-engineering-support/maintenance-engineering-support). See more of The Singapore Army on [Facebook](https://www.facebook.com/oursingaporearmy/).

![img_org_logos](https://user-images.githubusercontent.com/68763346/88453706-fc844100-ce9b-11ea-8e06-ab92ee89e9fc.png)

## Features
The library has been tested to work with the following Singapore identity documents and machine-readable international passports (see ICAO Document 9303).

![img_capabilities](https://user-images.githubusercontent.com/68763346/88448115-aa77f700-ce6c-11ea-9275-786f76aa575f.png)

As with most OCR and computer vision technology, results may vary depending on hardware, visual conditions, and quality of the original document.

## Getting Started
Create a new `IdParser` object with the following parameters:

1. Current year (YYYY).
2. 2-D String array of nationality, country name, ISO 3166-1 alpha-3 country code (e.g. `"Singaporean", "Singapore", "SGP"`).
3. Desired return type of either nationality, country name or country code.

```
String[][] mTopNationalities = {{"Singaporean", "Singapore", "SGP"}};
IdParser idParser = new IdParser(2020, mTopNationalities, NAT_RETURN_TYPE.NATIONALITY);
```

Call method `process`, passing in the block of detected text. An `IdData` object, which encapsulates the data fields extracted by the `IdParser`, will be returned.

```
String mBlock = ...    // String returned from OCR
IdData idData = idParser.process(mBlock);
```

Access `IdData` properties to obtain the desired data fields.

```
idData.getName();
idData.getSgId();
idData.getNationality();
...
```

## Code Sample
A sample MExS application can be accessed [here](https://github.com/mobile-express-scanner/mexs-sample-android). The sample application is a fully functional version which can be used immediately with minimal modifications. Do note that the actual version of MExS deployed for COVID-19 test registration is slightly different.

You are responsible for complying to any laws and regulations (e.g. the Personal Data Protection Act 2012) applicable to your collection of personal data.

## Installation
Step 1: Add the following repository to your project's build.gradle file.

```
repositories {
	maven { url 'https://jitpack.io' }
}
```

Step 2: Add the dependency for the library to your app's build.gradle file.

```
dependencies {
	implementation 'com.github.mobile-express-scanner:mexs-id-parser:1.0.0'
}
```

## Attributions
MExS uses third-party, open-source libraries (see attribution [here](ATTRIBUTION.md)).

## License

Copyright 2020 Headquarters Maintenance & Engineering Support, Singapore Army

Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.

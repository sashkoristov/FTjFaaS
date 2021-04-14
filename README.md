# Fault Tolerance jFaaS (FTjFaaS)

This project provides a middleware service for monitoring the execution of individual functions of a serverless application. It supports monitoring of functions on all widely-known FaaS systems AWS Lambda, IBM Cloud Functions, Google Cloud Functions, and Microzoft Azure Functions.

*FTjFaaS* is an integrated part of the [xAFCL Enactment Engine (xAFCL EE)](https://github.com/sashkoristov/enactmentengine), which can simultaneously execute individual functions of a serverless workflow application (Function Choreographies - *FCs*) across multiple FaaS systems and define and apply fault tolerance for each function of the FC.

*FTjFaaS* integrates the component [jFaaS](https://github.com/sashkoristov/jFaaS) for portable execution of individual FC functions (supported all widely-known FaaS systems AWS Lambda, IBM Cloud Functions, Google Cloud Functions, Microzoft Azure Functions, and Alibaba).

## *FTjFaaS design*

*FTjFaaS* introduces a single `InvokeMonitor` interface, which is implemented in LambdaMonitor, OpenWhiskMonitor, GoogleFunctionMonitor, and AzureMonitor classes for each FaaS system respectively. 

## Build
````
gradle shadowJar
````
The generated **FTjFaaS.jar** file can be found in the **build/libs/** folder.

## Contributions

Several bachelor theses at department of computer science, University of Innsbruck, supervised by Dr. Sashko Ristov contributed to this project:

- "Fault-tolerant execution of serverless functions across multiple FaaS systems", Matteo Bernard, Battaglin, SS2020 (AWS and IBM implementations)
- "G2GA: Portable execution of workflows in Google Cloud Functions across multiple FaaS platforms", Anna Kapeller and Felix Petschko, SS2021 (Google and Azure implementations)

## Support

If you need any additional information, please do not hesitate to contact sashko@dps.uibk.ac.at.
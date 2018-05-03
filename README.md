Clarice Performing Arts Center 
Podio -> Attendify
=========================


The application is hosted on a Google App Engine Instance

See the [Google App Engine standard environment documentation][ae-docs] for more
detailed instructions.

[ae-docs]: https://cloud.google.com/appengine/docs/java/

* [Java 8](http://www.oracle.com/technetwork/java/javase/downloads/index.html)
* [Maven](https://maven.apache.org/download.cgi) (at least 3.5)
* [Google Cloud SDK](https://cloud.google.com/sdk/) (aka gcloud)
* [Google Cloud Podiotojson app](https://console.cloud.google.com/appengine?project=podiotojson&folder=&organizationId=&serviceId=default&duration=PT1H)

## Setup

Access to podiotojson Google Cloud app - Contact someone at the Clarice first if you are not already added

    gcloud init
    gcloud components install app-engine-java
    gcloud auth application-default login


## Maven
### Running locally

    mvn appengine:run

### Deploying

    mvn appengine:deploy


## Potential Issues


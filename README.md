Clarice Performing Arts Center 
Podio -> Attendify
=========================

This application pulls in Podio event data and transforms it into a JSON feed for Attendify. It's very basic and just responds with the JSON feed on the root URL. It does use memcached to cache parsed info for up to 10 minutes which should reduce API hits to Podio without reducing data accuracy -- Attendify is configured to auto-poll every 15 minutes to fetch new data. 

The application is hosted on a Google App Engine Instance.

See the [Google App Engine standard environment documentation][ae-docs] for more
detailed instructions.

[ae-docs]: https://cloud.google.com/appengine/docs/java/

## Requirements

* [Java 8](http://www.oracle.com/technetwork/java/javase/downloads/index.html)
* [Maven](https://maven.apache.org/download.cgi) (at least 3.5)
* [Google Cloud SDK](https://cloud.google.com/sdk/) (aka gcloud)
* [Google Cloud Podiotojson app](https://console.cloud.google.com/appengine?project=podiotojson&folder=&organizationId=&serviceId=default&duration=PT1H)
* Access to podiotojson Google Cloud app - Contact someone at the Clarice first if you are not already added

## Setup

1. Rename PodioToJSONConfig.java.example to PodioToJSONConfig.java
2. Put in credentials needed
3. Run the following commands in your terminal to install gcloud tools


    gcloud init
    gcloud components install app-engine-java
    gcloud auth application-default login

### Maven
#### Running locally

You can run the app locally to quickly debug changes without affecting the production version of the site. This creates a server at http://localhost:8080/ by default.

    mvn appengine:run

#### Deploying

Run this to push new changes to the Google App Engine instance.

    mvn appengine:deploy

### Attendify Configuration

If the URL needs to be updated or auto-sync settings changed, they can be updated from teh Attendify dashboard. You will need access granted by someone at the Clarice. 

https://developers.attendify.com/dashboard

## Potential Issues

### Podio Category IDs

Some of the Podio category IDs defined in `PodioToJSON` had changed in 2018. Example:

    private static int EVENTSAPPID = 20459011;
    private static int FACULTYAPPID = 20459008;
    private static int STAFFAPPID = 17882615;
    private static int MEMBERSAPPID = 20458996;
    private static int VENUESAPPID = 17882575;
    private static int FESTIVALINFOAPPID = 17882858;
    private static int LOCALINFOAPPID = 17883266;

Luckily these are relatively easy to find, simply navigate to the event's Podio page and open the HTML page source. There will be a list of `<li>` elements for the top navigation, and they will have the attribute `data-app-id` defined. Example for Members App ID `20458996`:

    <li class="app tooltip tooltip-delayed tooltip-html tooltip-convert-newlines app-20458996 " data-app-id="20458996" data-tooltip-gravity="nw" data-tooltip-template-data="{&quot;name&quot;:&quot;Participants&quot;}" data-tooltip-template="space_nav">
          <a href="....">
           <span class="icon app-icon-24 icon-6"></span>
           <span class="title">Participants</span>
         </a>
    </li> 



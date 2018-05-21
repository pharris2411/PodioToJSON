package edu.umd.clarice;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.nio.charset.StandardCharsets;

import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;

import com.podio.APIFactory;
import com.podio.ResourceFactory;
import com.podio.item.FieldValuesView;
import com.podio.item.ItemAPI;
import com.podio.item.ItemBadge;
import com.podio.item.ItemsResponse;
import com.podio.oauth.OAuthClientCredentials;
import com.podio.oauth.OAuthUsernameCredentials;

import javax.cache.Cache;
import javax.cache.CacheException;
import javax.cache.CacheFactory;
import javax.cache.CacheManager;
import com.google.appengine.api.memcache.jsr107cache.GCacheFactory;


public class PodioToJSON {
    private static int EVENTSAPPID = 20459011;
    private static int FACULTYAPPID = 20459008;
    private static int STAFFAPPID = 17882615;
    private static int MEMBERSAPPID = 20458996;
    private static int VENUESAPPID = 17882575;
    private static int FESTIVALINFOAPPID = 17882858;
    private static int LOCALINFOAPPID = 17883266;
    private SimpleDateFormat formatPodio = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private SimpleDateFormat formatAttendify = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");

    private boolean filterToCurrentYear = true;
    private String currentYear = String.valueOf(Calendar.getInstance().get(Calendar.YEAR));

    public String connect() throws ParseException, JSONException, FileNotFoundException, UnsupportedEncodingException {

        PodioToJSONConfig config = new PodioToJSONConfig();


        // I was not able to get the google app engine memcache server to actually expire things after 10 minutes. It always seemed to live forever
        // Cache cache = null;
        // try {
        //     CacheFactory cacheFactory = CacheManager.getInstance().getCacheFactory();
        //     Map<Object, Object> properties = new HashMap<>();
        //     properties.put(GCacheFactory.EXPIRATION_DELTA, 600);
        //     cache = cacheFactory.createCache(properties);
            // if(cache.containsKey("podio")){
            //     System.out.println("Trying to load cached podio");
            //     String value = (String) cache.get("podio");
            //     // String str = new String(value, StandardCharsets.UTF_8);
            //     return value;
            // }
            // else {
            //     System.out.println("Cache loaded, did not find podio entry");
            // }

        // } catch (CacheException e) {
        //     System.out.println("Unable to configure cache manager!");
        // }

        TimeZone tz = TimeZone.getTimeZone("EST5EDT");
        formatAttendify.setTimeZone(tz);
        formatPodio.setTimeZone(tz);

        ResourceFactory resourceFactory = new ResourceFactory(
                new OAuthClientCredentials(config.apiUsername, config.apiKey),
                new OAuthUsernameCredentials(config.loginEmail, config.loginPass));
        APIFactory apiFactory = new APIFactory(resourceFactory);
        ItemAPI itemAPI = apiFactory.getAPI(ItemAPI.class);

        List<Map<String, String>> venuesMaps = null;
        List<Map<String, String>> scheduleMaps = null;
        List<Map<String, String>> facultyMaps = null;
        List<Map<String, String>> staffMaps = null;
        List<Map<String, String>> membersMaps = null;
        List<Map<String, String>> festivalInfoMaps = null;
        List<Map<String, String>> localInfoMaps = null;

        try{
            scheduleMaps = getSchedule(itemAPI);
        } catch(Exception e){
            System.out.println("Error getSchedule: ");
            e.printStackTrace();
        }
        try{
            facultyMaps = getFaculty(itemAPI);
        } catch(Exception e){
            System.out.println("Error getFaculty: ");
            e.printStackTrace();
        }
        try{
            staffMaps = getStaff(itemAPI);
        } catch(Exception e){
            System.out.println("Error getStaff: ");
            e.printStackTrace();
        }
        try{
            membersMaps = getMembers(itemAPI);
        } catch(Exception e){
            System.out.println("Error getMembers: ");
            e.printStackTrace();
        }
        try{
            festivalInfoMaps = getFestivalInfo(itemAPI);
        } catch(Exception e){
            System.out.println("Error getFestivalInfo: ");
            e.printStackTrace();
        }
        try{
            localInfoMaps = getLocalInfo(itemAPI);
        } catch(Exception e){
            System.out.println("Error getLocalInfo: ");
            e.printStackTrace();
        }

        // venuesMap = getVenues(itemAPI);

        // if(scheduleMaps!= null) 
        //     System.out.println(scheduleMaps.size() + " " + scheduleMaps);
        if(facultyMaps!= null) 
            System.out.println(facultyMaps.size() + " " + facultyMaps);
        if(staffMaps!= null) 
            System.out.println(staffMaps.size() + " " + staffMaps);
        if(membersMaps!= null) 
            System.out.println(membersMaps.size() + " " + membersMaps);
        // // if(venuesMaps!= null) 
        //     // System.out.println(venuesMaps.size() + " " + venuesMaps);
        // if(festivalInfoMaps!= null) 
        //     System.out.println(festivalInfoMaps.size() + " " + festivalInfoMaps);
        // if(localInfoMaps!= null) 
        //     System.out.println(localInfoMaps.size() + " " + localInfoMaps);
		/*for(Map<String, String> s : scheduleMaps){
			System.out.println(s);
		}*/
        JSONObject json = new JSONObject();
        if(scheduleMaps != null && scheduleMaps.size() > 0){
            json.append("features", buildScheduleJSON(scheduleMaps, facultyMaps, staffMaps, membersMaps));
        }
        if(facultyMaps != null && facultyMaps.size() > 0){
            json.append("features", buildFacultyJSON(facultyMaps));
        }
        if(staffMaps != null && staffMaps.size() > 0){
            json.append("features", buildStaffJSON(staffMaps));
        }
        if(membersMaps != null && membersMaps.size() > 0){
            json.append("features", buildMembersJSON(membersMaps));
        }
		// if(venuesMaps != null && venuesMaps.size() > 0){
			// json.append("features", buildVenuesJSON(venuesMaps));
		// }
        if(festivalInfoMaps != null && festivalInfoMaps.size() > 0){
            json.append("features", buildFestivalInfoJSON(festivalInfoMaps));
        }
        if(localInfoMaps != null && localInfoMaps.size() > 0){
            json.append("features", buildLocalInfoJSON(localInfoMaps));
        }
        //System.out.println(json.toString(4));
        // PrintWriter writer = new PrintWriter(dir + "data.json", "UTF-8");
        // writer.println(json.toString(4));
        // writer.close();
        String jsonString = json.toString(4);
        // if(cache != null){
        //     System.out.println("Caching podio results");
        //     cache.put("podio", jsonString);
        // }

        return jsonString;
    }

    private JSONObject buildScheduleJSON(List<Map<String, String>> scheduleMaps, List<Map<String, String>> facultyMaps, List<Map<String, String>> staffMaps, List<Map<String, String>> membersMaps) throws JSONException{
        //Build schedule JSON
        JSONObject scheduleJSON = new JSONObject();
        for(Map<String, String> m : scheduleMaps){
            if(m.containsKey("startTime") && m.containsKey("endTime") && m.containsKey("title")) {
                JSONObject j = new JSONObject();
                j.put("type", "schedule-session");
                j.put("title", m.get("title"));
                j.put("id", m.get("id"));
                j.put("description", m.get("description"));
                j.put("location", m.get("venue0")); //Using venue, and only getting one venue
                if(m.get("numProfileIds") != null){
                    for(int i=0; i < Integer.valueOf(m.get("numProfileIds")); i++){
                        if(checkId(m.get("profileId" + i), m.get("id"), facultyMaps, staffMaps, membersMaps))
                            j.append("speakers",  m.get("profileId" + i));
                    }
                }
                j.put("startTime", m.get("startTime"));
                j.put("endTime", m.get("endTime"));
                if(m.containsKey("trackName")){
                    JSONObject track = new JSONObject();
                    track.put("name", m.get("trackName"));
                    j.append("tracks", track);
                }
                scheduleJSON.append("sessions", j); //still need files
            }
        }
        scheduleJSON.put("name", "Schedule");
        scheduleJSON.put("type", "feature-schedule");
        scheduleJSON.put("id", "1"); //random id
        scheduleJSON.put("icon", "14");
        JSONObject settingsJSON = new JSONObject();
        settingsJSON.put("type", "schedule-settings");
        settingsJSON.put("timeFormat", "12");
        settingsJSON.put("timeZone", "US/Eastern");
        settingsJSON.put("multiTrack", true);
        scheduleJSON.put("settings", settingsJSON);
        return scheduleJSON;
    }

    private JSONObject buildFacultyJSON(List<Map<String, String>> facultyMaps) throws JSONException{
        JSONObject facultyJSON = new JSONObject();
        for(Map<String, String> m : facultyMaps){
            if(m.containsKey("firstName") && m.containsKey("lastName")) {
                if(filterToCurrentYear && (!m.containsKey("yearParticipating") || !m.get("yearParticipating").equals(currentYear))){
                    System.out.println("Skipping over " + m.get("firstName") + " " + m.get("lastName") + " as the yearParticipating is " + m.get("yearParticipating"));
                    continue;
                } else {
                    System.out.println("Including " + m.get("firstName") + " " + m.get("lastName") + " as the yearParticipating is " + m.get("yearParticipating"));
                }

                JSONObject j = new JSONObject();
                j.put("type", "speaker");
                j.put("firstName", m.get("firstName"));
                j.put("lastName", m.get("lastName"));
                j.put("id", m.get("id"));
				/*if(m.get("numCategory") != null){
					for(int i=0; i < Integer.valueOf(m.get("numCategory")); i++){
							j.append("groups",  m.get("category" + i));
					}
				}*/
                if(m.get("numSessions") != null){
                    for(int i=0; i < Integer.valueOf(m.get("numSessions")); i++){
                        j.append("sessions",  m.get("session" + i));
                    }
                }
                j.put("company", m.get("company"));
                j.put("position", m.get("position"));
                //j.put("photo", m.get("photoURI"));
                facultyJSON.append("speakers", j);
            }
        }
        facultyJSON.put("name", "Faculty");
        facultyJSON.put("type", "feature-speakers");
        facultyJSON.put("sorting", "lastName");
        facultyJSON.put("id", "2"); //random id
        facultyJSON.put("icon", "156");	// Music note, according to inspect element
        return facultyJSON;
    }

    private JSONObject buildStaffJSON(List<Map<String, String>> staffMaps) throws JSONException{
        //Build staff JSON
        JSONObject staffJSON = new JSONObject();
        for(Map<String, String> m : staffMaps){


            if(m.containsKey("firstName") && m.containsKey("lastName")) {

                if(filterToCurrentYear && (!m.containsKey("yearParticipating") || !m.get("yearParticipating").equals(currentYear))){
                    System.out.println("Skipping over " + m.get("firstName") + " " + m.get("lastName") + " as the yearParticipating is " + m.get("yearParticipating"));
                    continue;
                }

                JSONObject j = new JSONObject();
                j.put("type", "speaker");
                j.put("firstName", m.get("firstName"));
                j.put("lastName", m.get("lastName"));
                j.put("id", m.get("id"));
                j.put("description", m.get("description"));
                j.put("position", m.get("position"));
                j.put("email", m.get("email0")); //Only getting one email
                j.put("phone", m.get("phone0")); //Only getting one phone
                //j.put("photo", m.get("photoURI"));
                staffJSON.append("speakers", j);
            }
        }
        staffJSON.put("name", "Staff");
        staffJSON.put("type", "feature-speakers");
        staffJSON.put("sorting", "lastName");
        staffJSON.put("id", "3"); //random id
        staffJSON.put("icon", "72");
        return staffJSON;
    }

    private JSONObject buildMembersJSON(List<Map<String, String>> membersMaps) throws JSONException{
        //Build participants JSON
        JSONObject membersJSON = new JSONObject();
        for(Map<String, String> m : membersMaps){
            if(m.containsKey("firstName") && m.containsKey("lastName")) {
                JSONObject j = new JSONObject();
                j.put("type", "speaker");
                j.put("firstName", m.get("firstName"));
                j.put("lastName", m.get("lastName"));
                j.put("id", m.get("id"));
                j.put("company", m.get("company") + " " + m.get("degree"));
                j.put("position", m.get("position"));
                j.put("email", m.get("email0")); //Only getting one email
                j.put("phone", m.get("phone0")); //Only getting one phone
				/*if(m.get("numCategory") != null){
					for(int i=0; i < Integer.valueOf(m.get("numCategory")); i++){
							j.append("groups",  m.get("category" + i));
					}
				}*/
                membersJSON.append("speakers", j);
            }
        }
        membersJSON.put("name", "Participants");
        membersJSON.put("type", "feature-speakers");
        membersJSON.put("sorting", "lastName");
        membersJSON.put("id", "4"); //random id
        membersJSON.put("icon", "69");
        return  membersJSON;
    }

    private JSONObject buildVenuesJSON(List<Map<String, String>> venuesMaps) throws JSONException{
        //Build venues JSON
        //Not using 'is a good venue for', 'venue size', 'venue managed by', 'Contacts at Venue', or 'photoURI'
        //Perhaps add this stuff to description?
        JSONObject venuesJSON = new JSONObject();
        for(Map<String, String> m : venuesMaps){
            JSONObject j = new JSONObject();
            j.put("type", "place");
            j.put("id", m.get("id"));
            j.put("address", m.get("address"));
            j.put("name", m.get("name"));
            if(m.get("lat") != null)
                j.put("lat", Double.parseDouble(m.get("lat")));
            if(m.get("lng") != null)
                j.put("lng", Double.parseDouble(m.get("lng")));
            if(m.get("description") == null)	//Requires a description for some reason
                j.put("description", "");
            else								//Not actually ever extracting description from podio yet
                j.put("description", m.get("description"));
            venuesJSON.append("places", j);
        }
        venuesJSON.put("name", "Map");
        venuesJSON.put("type", "feature-maps");
        venuesJSON.put("sorting", "manual");
        venuesJSON.put("id", "5"); //random id
        venuesJSON.put("icon", "48");
        return venuesJSON;
    }

    private JSONObject buildFestivalInfoJSON(List<Map<String, String>> festivalInfoMaps) throws JSONException{
        //Build festivalInfo JSON
        JSONObject festivalInfoJSON = new JSONObject();
        for(Map<String, String> m : festivalInfoMaps){
            if(m.containsKey("title") && m.containsKey("content")) {
                JSONObject j = new JSONObject();
                j.put("type", "about-section");
                j.put("id", m.get("id"));
                j.put("title", m.get("title"));
                j.put("content", m.get("content"));

                festivalInfoJSON.append("sections", j);
            }
        }
        festivalInfoJSON.put("name", "Festival Info");
        festivalInfoJSON.put("type", "feature-about");
        festivalInfoJSON.put("sorting", "manual");
        festivalInfoJSON.put("id", "6"); //random id
        festivalInfoJSON.put("icon", "95");
        return festivalInfoJSON;
    }

    private JSONObject buildLocalInfoJSON(List<Map<String, String>> localInfoMaps) throws JSONException{
        //Build localInfo JSON
        JSONObject localInfoJSON = new JSONObject();
        for(Map<String, String> m : localInfoMaps){
            if(m.containsKey("title") && m.containsKey("content")) {
                JSONObject j = new JSONObject();
                j.put("type", "about-section");
                j.put("id", m.get("id"));
                j.put("title", m.get("title"));
                j.put("content", m.get("content"));

                localInfoJSON.append("sections", j);
            }
        }
        localInfoJSON.put("name", "Local Info");
        localInfoJSON.put("type", "feature-about");
        localInfoJSON.put("sorting", "manual");
        localInfoJSON.put("id", "7"); //random id
        localInfoJSON.put("icon", "2");
        return localInfoJSON;
    }

    private List<Map<String,String>> getSchedule(ItemAPI itemAPI) throws ParseException{
        int offset = 0;
        int total = 0;
        List<Map<String,String>> ret = new ArrayList<Map<String,String>>();
        do {
            ItemsResponse ir = itemAPI.getItems(EVENTSAPPID, 500, offset, null, null);
            total = ir.getTotal();
            offset+=500;
            for (ItemBadge ib : ir.getItems()) {
                Map<String,String> map = new HashMap<String,String>();
                map.put("id", String.valueOf(ib.getId()));
                for(FieldValuesView fvv : ib.getFields()){
                    if(fvv.getExternalId().equals("date")){
                        Date date = formatPodio.parse((String) fvv.getValues().get(0).get("start"));
                        String iso = formatAttendify.format(date);
                        map.put("startTime", iso);
                    }
                    else if(fvv.getExternalId().equals("title"))
                        map.put("title", (String) fvv.getValues().get(0).get("value"));
                    else if(fvv.getExternalId().equals("event-type")){
                        map.put("trackName", (String) ((Map)fvv.getValues().get(0).get("value")).get("text"));
                    }
                    else if(fvv.getExternalId().equals("end-date-time")){
                        Date date = formatPodio.parse((String) fvv.getValues().get(0).get("start"));
                        String iso = formatAttendify.format(date);
                        map.put("endTime", iso);
                    }
                    else if(fvv.getExternalId().equals("instructorconductor")){
                        int num=0;
                        for(int i=0; i<fvv.getValues().size(); i++){
                            map.put("profileId" + i, ((Map)fvv.getValues().get(i).get("value")).get("item_id").toString());
                            num++;
                        }
                        map.put("numProfileIds", String.valueOf(num));
                    }
					/*else if(fvv.getExternalId().equals("file")){
						System.out.println(fvv.getValues().get(0).get("value"));
						int num=0;
						for(int i=0; i<fvv.getValues().size(); i++){
							map.put("profileId" + i, ((Map)fvv.getValues().get(i).get("value")).get("item_id").toString());
							num++;
						}
						map.put("numProfileIds", String.valueOf(num));
					} */else if(fvv.getExternalId().equals("venue")) {
                        for(int i=0; i<fvv.getValues().size(); i++){
                            map.put("venue" + i, ((Map)fvv.getValues().get(i).get("value")).get("title").toString());
                        }
                    } else if(fvv.getExternalId().equals("event-description"))
                        map.put("description", (String) fvv.getValues().get(0).get("value"));
                }
                ret.add(map);
            }
        } while (offset <= total);
        return ret;
    }

    private List<Map<String,String>> getFaculty(ItemAPI itemAPI){
        int offset = 0;
        int total = 0;
        List<Map<String,String>> ret = new ArrayList<Map<String,String>>();
        do {
            ItemsResponse ir = itemAPI.getItems(FACULTYAPPID, 500, offset, null, null);
            total = ir.getTotal();
            offset+=500;
            for (ItemBadge ib : ir.getItems()) {
                Map<String,String> map = new HashMap<String,String>();
                map.put("id", String.valueOf(ib.getId()));
                for(FieldValuesView fvv : ib.getFields()){
                    if(fvv.getExternalId().equals("name"))
                        map.put("firstName", (String) fvv.getValues().get(0).get("value"));
                    else if(fvv.getExternalId().equals("last-name-2"))
                        map.put("lastName", (String) fvv.getValues().get(0).get("value"));
                    else if(fvv.getExternalId().equals("company-2"))
                        map.put("company", (String) fvv.getValues().get(0).get("value"));
                    else if(fvv.getExternalId().equals("position-2"))
                        map.put("position", (String) fvv.getValues().get(0).get("value"));
                    else if(fvv.getExternalId().equals("category")){
                        for(int i=0; i<fvv.getValues().size(); i++){
                            map.put("category" + i, html2text(((Map)fvv.getValues().get(i).get("value")).get("text").toString()));
                        }
                        map.put("numCategory", String.valueOf(fvv.getValues().size()));
                    }
                    else if(fvv.getExternalId().equals("shown"))
                        map.put("shown", (String) fvv.getValues().get(0).get("value"));
                    else if(fvv.getExternalId().equals("image-2"))
                        map.put("photoURI", ((Map)fvv.getValues().get(0).get("value")).get("link").toString());
                    else if(fvv.getExternalId().equals("year-participating")){
                        map.put("yearParticipating",  (String) ((Map)fvv.getValues().get(0).get("value")).get("text"));
                        map.put("yearParticipatingActive",  (String) ((Map)fvv.getValues().get(0).get("value")).get("status"));
                    }
                }
                // if(map.get("shown").equals("1.0000"))
                    // ret.add(map);
            }
        } while (offset <= total);
        return ret;
    }

    private List<Map<String,String>> getStaff(ItemAPI itemAPI){
        int offset = 0;
        int total = 0;
        List<Map<String,String>> ret = new ArrayList<Map<String,String>>();
        do {
            ItemsResponse ir = itemAPI.getItems(STAFFAPPID, 500, offset, null, null);
            total = ir.getTotal();
            offset+=500;
            for (ItemBadge ib : ir.getItems()) {
                Map<String,String> map = new HashMap<String,String>();
                map.put("id", String.valueOf(ib.getId()));
                for(FieldValuesView fvv : ib.getFields()){
                    if(fvv.getExternalId().equals("name"))
                        map.put("firstName", (String) fvv.getValues().get(0).get("value"));
                    else if(fvv.getExternalId().equals("last-name"))
                        map.put("lastName", (String) fvv.getValues().get(0).get("value"));
                    else if(fvv.getExternalId().equals("description")){
                        map.put("description", (String) fvv.getValues().get(0).get("value"));
                    }
                    else if(fvv.getExternalId().equals("position"))
                        map.put("position", (String) fvv.getValues().get(0).get("value"));
                    else if(fvv.getExternalId().equals("email")){
                        for(int i=0; i<fvv.getValues().size(); i++){
                            map.put("email" + i, (String) fvv.getValues().get(i).get("value"));
                        }
                    } else if(fvv.getExternalId().equals("phone")){
                        for(int i=0; i<fvv.getValues().size(); i++){
                            map.put("phone" + i, (String) fvv.getValues().get(i).get("value"));
                        }
                    } else if(fvv.getExternalId().equals("image-2")){
                        //System.out.println(fvv.getValues().get(0));
                        map.put("photoURI", ((Map)fvv.getValues().get(0).get("value")).get("link").toString());
                    } else if(fvv.getExternalId().equals("year-participating")){
                       
                        map.put("yearParticipating",  (String) ((Map)fvv.getValues().get(0).get("value")).get("text"));
                        map.put("yearParticipatingActive",  (String) ((Map)fvv.getValues().get(0).get("value")).get("status"));
                    }
                }
                ret.add(map);
            }
        } while (offset <= total);
        return ret;
    }

    private List<Map<String,String>> getMembers(ItemAPI itemAPI){
        int offset = 0;
        int total = 0;
        List<Map<String,String>> ret = new ArrayList<Map<String,String>>();
        do {
            ItemsResponse ir = itemAPI.getItems(MEMBERSAPPID, 500, offset, null, null);
            total = ir.getTotal();
            System.out.println("Total members " + total);
            offset+=500;
            for (ItemBadge ib : ir.getItems()) {
                Map<String,String> map = new HashMap<String,String>();
                map.put("id", String.valueOf(ib.getId()));
                for(FieldValuesView fvv : ib.getFields()){
                    if(fvv.getExternalId().equals("title"))
                        map.put("firstName", (String) fvv.getValues().get(0).get("value"));
                    else if(fvv.getExternalId().equals("last-name"))
                        map.put("lastName", (String) fvv.getValues().get(0).get("value"));
                    else if(fvv.getExternalId().equals("current-year"))
                        map.put("shown", (String) fvv.getValues().get(0).get("value"));
                    else if(fvv.getExternalId().equals("current-institution"))
                        map.put("company", html2text(fvv.getValues().get(0).get("value").toString()));
                    else if(fvv.getExternalId().equals("degree"))
                        map.put("degree", html2text(fvv.getValues().get(0).get("value").toString()));
                    else if(fvv.getExternalId().equals("instrument")){
                        map.put("position", html2text(((Map)fvv.getValues().get(0).get("value")).get("text").toString()));
                    }
                    else if(fvv.getExternalId().equals("category")){
                        for(int i=0; i<fvv.getValues().size(); i++){
                            map.put("category" + i, html2text(((Map)fvv.getValues().get(i).get("value")).get("text").toString()));
                        }
                        map.put("numCategory", String.valueOf(fvv.getValues().size()));
                    }
                    else if(fvv.getExternalId().equals("email-2")){
                        for(int i=0; i<fvv.getValues().size(); i++){
                            map.put("email" + i, (String) fvv.getValues().get(i).get("value"));
                        }
                    } else if(fvv.getExternalId().equals("cell-phone")){
                        for(int i=0; i<fvv.getValues().size(); i++){
                            map.put("phone" + i, (String) fvv.getValues().get(i).get("value"));
                        }
                    }
                }
                // if(map.get("shown").equals("1.0000"))
                    // ret.add(map);
            }
        } while (offset <= total);
        return ret;
    }

    private List<Map<String,String>> getVenues(ItemAPI itemAPI){
        int offset = 0;
        int total = 0;
        List<Map<String,String>> ret = new ArrayList<Map<String,String>>();
        do {
            ItemsResponse ir = itemAPI.getItems(VENUESAPPID, 500, offset, null, null);
            total = ir.getTotal();
            offset+=500;
            for (ItemBadge ib : ir.getItems()) {
                Map<String,String> map = new HashMap<String,String>();
                map.put("id", String.valueOf(ib.getId()));
                for(FieldValuesView fvv : ib.getFields()){
                    if(fvv.getExternalId().equals("name-of-venue"))
                        map.put("name", (String) fvv.getValues().get(0).get("value"));
                    else if(fvv.getExternalId().equals("address-of-venue")) {
                        map.put("address", (String) fvv.getValues().get(0).get("formatted"));
                        Double lat =  (Double) fvv.getValues().get(0).get("lat");
                        Double lng =  (Double) fvv.getValues().get(0).get("lng");
                        if(lat != null)
                            map.put("lat", lat.toString());
                        if(lng != null)
                            map.put("lng", lng.toString());
                    } else if(fvv.getExternalId().equals("could-be-a-good-venue-for"))
                        map.put("is a good venue for", (String) fvv.getValues().get(0).get("value"));
                    else if(fvv.getExternalId().equals("venue-size"))
                        map.put("venue size", ((Map)fvv.getValues().get(0).get("value")).get("text").toString());
                    else if(fvv.getExternalId().equals("venue-managed-by"))
                        map.put("venue managed by", ((Map)fvv.getValues().get(0).get("value")).get("text").toString());
                    else if(fvv.getExternalId().equals("contacts-at-venue"))
                        map.put("Contacts at Venue", ((Map)fvv.getValues().get(0).get("value")).get("profile_id").toString());
                    else if(fvv.getExternalId().equals("images-of-the-venue"))
                        map.put("photoURI", ((Map)fvv.getValues().get(0).get("value")).get("link").toString());

                }
                ret.add(map);
            }
        } while (offset <= total);
        return ret;
    }

    private List<Map<String,String>> getFestivalInfo(ItemAPI itemAPI){
        int offset = 0;
        int total = 0;
        List<Map<String,String>> ret = new ArrayList<Map<String,String>>();
        do {
            ItemsResponse ir = itemAPI.getItems(FESTIVALINFOAPPID, 500, offset, null, null);
            total = ir.getTotal();
            offset+=500;
            for (ItemBadge ib : ir.getItems()) {
                Map<String,String> map = new HashMap<String,String>();
                map.put("id", String.valueOf(ib.getId()));
                for(FieldValuesView fvv : ib.getFields()){
                    if(fvv.getExternalId().equals("title"))
                        map.put("title", (String) fvv.getValues().get(0).get("value"));
                    else if(fvv.getExternalId().equals("about"))
                        map.put("content", (String) fvv.getValues().get(0).get("value"));
                }
                ret.add(map);
            }
        } while (offset <= total);
        return ret;
    }

    private List<Map<String,String>> getLocalInfo(ItemAPI itemAPI){
        int offset = 0;
        int total = 0;
        List<Map<String,String>> ret = new ArrayList<Map<String,String>>();
        do {
            ItemsResponse ir = itemAPI.getItems(LOCALINFOAPPID, 500, offset, null, null);
            total = ir.getTotal();
            offset+=500;
            for (ItemBadge ib : ir.getItems()) {
                Map<String,String> map = new HashMap<String,String>();
                map.put("id", String.valueOf(ib.getId()));
                for(FieldValuesView fvv : ib.getFields()){
                    if(fvv.getExternalId().equals("title"))
                        map.put("title", (String) fvv.getValues().get(0).get("value"));
                    else if(fvv.getExternalId().equals("about"))
                        map.put("content", (String) fvv.getValues().get(0).get("value"));
                }
                ret.add(map);
            }
        } while (offset <= total);
        return ret;
    }

    public static String html2text(String html) {
        return Jsoup.parse(html).text();
    }

    //Check to make sure the speaker ID in the schedule item exists
    //Also links speakers back to the schedule sessions
    private boolean checkId(String id, String sessionId, List<Map<String, String>> facultyMaps, List<Map<String, String>> staffMaps, List<Map<String, String>> membersMaps){
        for(Map<String, String> m : facultyMaps){
            if(m.get("id") != null && m.get("id").equals(id)){
                if(m.get("numSessions") != null){
                    int numSessions = Integer.valueOf(m.get("numSessions"));
                    m.put("session" + numSessions, sessionId);
                    m.put("numSessions", "" + (numSessions+1));

                }
                else{
                    m.put("session0", sessionId);
                    m.put("numSessions", "1");
                }
                return true;
            }
        }
        for(Map<String, String> m : staffMaps){
            if(m.get("id") != null && m.get("id").equals(id)){
                if(m.get("numSessions") != null){
                    int numSessions = Integer.valueOf(m.get("numSessions"));
                    m.put("session" + numSessions, sessionId);
                    m.put("numSessions", "" + (numSessions+1));

                }
                else{
                    m.put("session0", sessionId);
                    m.put("numSessions", "1");
                }
                return true;
            }
        }
        for(Map<String, String> m : membersMaps){
            if(m.get("id") != null && m.get("id").equals(id)){
                if(m.get("numSessions") != null){
                    int numSessions = Integer.valueOf(m.get("numSessions"));
                    m.put("session" + numSessions, sessionId);
                    m.put("numSessions", "" + (numSessions+1));

                }
                else{
                    m.put("session0", sessionId);
                    m.put("numSessions", "1");
                }
                return true;
            }
        }
        return false;
    }
}


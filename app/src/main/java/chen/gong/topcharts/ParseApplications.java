package chen.gong.topcharts;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.StringReader;
import java.util.ArrayList;

/**
 * Created by gongchen on 1/2/17.
 */
public class ParseApplications {
    private static final String TAG = "ParseApplications";
    private ArrayList<FeedEntry> applications;

    public ParseApplications() {
        this.applications = new ArrayList<>();
    }

    public ArrayList<FeedEntry> getApplications() {
        return applications;
    }

    public boolean parse(String xmlData){
        boolean status = true;
        FeedEntry currentRecord = null;
        boolean inEntry = false;
        String textValue = "";
        try {
            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            factory.setNamespaceAware(true);
            XmlPullParser xpp = factory.newPullParser();
            xpp.setInput(new StringReader(xmlData));
            int eventType = xpp.getEventType();//Determine the current state of a parser
            while(eventType != XmlPullParser.END_DOCUMENT){//Extract
                String tagName = xpp.getName();//tagName can be null, if parser not in a tag,
                switch (eventType){//<tag> TEXT </tag>
                    case XmlPullParser.START_TAG:
//                        Log.d(TAG, "parse: Starting tag for " + tagName);
                        if("entry".equalsIgnoreCase(tagName)){
                            inEntry = true;
                            currentRecord = new FeedEntry();
                        }
                        break;
                    case XmlPullParser.TEXT://Data is available
                        textValue = xpp.getText();
                        break;
                    case XmlPullParser.END_TAG:
//                        Log.d(TAG, "parse: Ending tag for " + tagName);
                        if(inEntry) {//Check pullParser is inside an entry tag
                            if ("entry".equalsIgnoreCase(tagName)) {//Call method on String
                                applications.add(currentRecord);
                                inEntry = false;//Reach end tag of an entry
                            } else if ("name".equalsIgnoreCase(tagName)) {
                                currentRecord.setName(textValue);
                            } else if ("artist".equalsIgnoreCase(tagName)) {
                                currentRecord.setArtist(textValue);
                            } else if ("releaseDate".equalsIgnoreCase(tagName)) {
                                currentRecord.setReleaseDate(textValue);
                            } else if ("summary".equalsIgnoreCase(tagName)) {
                                currentRecord.setSummary(textValue);
                            } else if ("image".equalsIgnoreCase(tagName)) {
                                currentRecord.setImageURL(textValue);
                            }
                        }
                        break;
                    default://Nothing to do
                }
                eventType = xpp.next(); //next() gets <> or </>. Then getName() gets tag name.
            }
//            for (FeedEntry app : applications){
//                Log.d(TAG, "************");
//                Log.d(TAG,app.toString());
//            }

        }catch (Exception e){
            status = false;
            e.printStackTrace();
        }
        return status;

    }
}

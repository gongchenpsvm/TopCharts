package chen.gong.topcharts;

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
}

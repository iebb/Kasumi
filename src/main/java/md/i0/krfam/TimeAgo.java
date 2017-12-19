package md.i0.krfam;

// *** Original codeName for TimeAgo from user samsad on stackoverflow *** //
// ***		http://stackoverflow.com/users/1061943/samsad		   *** //
// *** 			http://stackoverflow.com/q/26585121			       *** //


import android.content.Context;
import android.content.res.Resources;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class TimeAgo {


    public static final List<Long> times = Arrays.asList(
            TimeUnit.DAYS.toMillis(365),
            TimeUnit.DAYS.toMillis(30),
            TimeUnit.DAYS.toMillis(1),
            TimeUnit.HOURS.toMillis(1),
            TimeUnit.MINUTES.toMillis(1),
            TimeUnit.SECONDS.toMillis(1));

    public static final List<String> _timesString = Arrays.asList("year", "month", "day", "hour", "minute", "second");
    public static final List<String> _timesStrings = Arrays.asList("years", "months", "days", "hours", "minutes", "seconds");

    public static String toDuration(long duration, List<String> timesString, List<String> timesStrings, String Justnow, String ago) {
        StringBuffer res = new StringBuffer();
        for (int i = 0; i < times.size(); i++) {
            Long current = times.get(i);
            long temp = duration / current;
            if (temp > 0) {
                res.append(temp).append(" ").append(temp > 1 ? timesStrings.get(i) : timesString.get(i)).append(ago);
                break;
            }
        }
        if ("".equals(res.toString()))
            return Justnow;
        else
            return res.toString();
    }
    public static String toDuration(long duration) {
        return toDuration(duration, _timesString, _timesStrings, "Just Now", " ago");
    }
    public static String toDuration(long duration, Context ctx) {

        Resources res = ctx.getResources();
        List<String> _timesString = Arrays.asList(
                res.getString(R.string.year),
                res.getString(R.string.month),
                res.getString(R.string.day),
                res.getString(R.string.hour),
                res.getString(R.string.minute),
                res.getString(R.string.second));
        List<String> _timesStrings = Arrays.asList(
                res.getString(R.string.years),
                res.getString(R.string.months),
                res.getString(R.string.days),
                res.getString(R.string.hours),
                res.getString(R.string.minutes),
                res.getString(R.string.seconds));
        return toDuration(duration, _timesString, _timesStrings, res.getString(R.string.now), res.getString(R.string.ago));
    }
}

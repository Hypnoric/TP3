package hypnoric.tp3;

/**
 * Created by Eric on 2015-04-24.
 */
import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

@Root
public class Meeting
{
    public Meeting()
    {
    }

    public Meeting(String date, String place)
    {
        SetDate(date);
        SetPlace(place);
    }

    @Element
    private String Date;

    public String GetDate()
    {
        return Date;
    }

    public void SetDate(String date)
    {
        Date = date;
    }

    @Element
    private String Place;

    public String GetPlace()
    {
        return Place;
    }

    public void SetPlace(String place)
    {
        Place = place;
    }

    @Override
    public boolean equals(Object inObject)
    {
        if (inObject instanceof Meeting)
        {
            Meeting inMeeting = (Meeting)inObject;
            //return this.PhotoPath.equalsIgnoreCase(inPerson.PhotoPath)
            return this.Place.equalsIgnoreCase(inMeeting.Place)
                    && this.Date.equalsIgnoreCase(inMeeting.Date);
        }
        return false;
    }
}

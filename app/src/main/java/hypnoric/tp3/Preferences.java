package hypnoric.tp3;

/**
 * Created by Eric on 2015-04-24.
 */
import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

@Root
public class Preferences
{
    public Preferences()
    {
    }

    public Preferences(String photoPath, String courriel, String groupe, boolean restaurant, boolean parc, boolean cinema, double latitude, double longitude, boolean meetingAccepte)
    {
        SetPhotoPath(photoPath);
        SetCourriel(courriel);
        SetGroupe(groupe);
        SetRestaurant(restaurant);
        SetParc(parc);
        SetCinema(cinema);
        SetLatitude(latitude);
        SetLongitude(longitude);
        SetMeetingAccepte(meetingAccepte);
    }

    @Element
    private double Latitude;

    public double GetLatitude()
    {
        return Latitude;
    }

    public void SetLatitude(double latitude)
    {
        Latitude = latitude;
    }

    @Element
    private double Longitude;

    public double GetLongitude()
    {
        return Longitude;
    }

    public void SetLongitude(double longitude)
    {
        Longitude = longitude;
    }

    @Element
    private String PhotoPath;

    public String GetPhotoPath()
    {
        return PhotoPath;
    }

    public void SetPhotoPath(String photoPath)
    {
        PhotoPath = photoPath;
    }

    @Element
    private String Courriel;

    public String GetCourriel()
    {
        return Courriel;
    }

    public void SetCourriel(String courriel)
    {
        Courriel = courriel;
    }

    @Element
    private String Groupe;

    public String GetGroupe()
    {
        return Groupe;
    }

    public void SetGroupe(String groupe)
    {
        Groupe = groupe;
    }

    @Element
    private boolean MeetingAccepte;

    public boolean GetMeetingAccepte()
    {
        return MeetingAccepte;
    }

    public void SetMeetingAccepte(boolean meetingAccepte)
    {
        MeetingAccepte = meetingAccepte;
    }

    @Element
    private boolean Restaurant;

    public boolean GetRestaurant()
    {
        return Restaurant;
    }

    public void SetRestaurant(boolean restaurant)
    {
        Restaurant = restaurant;
    }

    @Element
    private boolean Parc;

    public boolean GetParc()
    {
        return Parc;
    }

    public void SetParc(boolean parc)
    {
        Parc = parc;
    }

    @Element
    private boolean Cinema;

    public boolean GetCinema()
    {
        return Cinema;
    }

    public void SetCinema(boolean cinema)
    {
        Cinema = cinema;
    }

    @Override
    public boolean equals(Object inObject)
    {
        if (inObject instanceof Preferences)
        {
            Preferences inPerson = (Preferences)inObject;
            //return this.PhotoPath.equalsIgnoreCase(inPerson.PhotoPath)
            return this.Courriel.equalsIgnoreCase(inPerson.Courriel)
                    && this.Groupe.equalsIgnoreCase(inPerson.Groupe)
                    && this.Restaurant == inPerson.Restaurant
                    && this.Parc == inPerson.Parc
                    && this.Cinema == inPerson.Cinema
                    && this.MeetingAccepte == inPerson.MeetingAccepte;
        }
        return false;
    }
}

package saberapplications.pawpads;

/**
 * Created by blaze on 10/21/2015.
 */
public class User{
    String username, password, email;
    Double lat, lng;

    public User (Double lat, Double lng, String user, String pass){
        this.username = user;
        this.password = pass;
        this.lat = lat;
        this.lng = lng;
    }

    public User (String user, String pass){
        this.username = user;
        this.password = pass;
    }

    public User (String user, String pass, String email){
        this.username = user;
        this.password = pass;
        this.email = email;
    }

}

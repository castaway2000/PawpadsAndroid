package saberapplications.pawpads;

/**
 * Created by blaze on 10/21/2015.
 */
public class User {
    String name, username, password;
    int age;

    public User (String name, int age, String username, String password){
        this.name = name;
        this.age = age;
        this.username = username;
        this.password = password;
    }

    public User (String username, String password){
        this.username = username;
        this.password = password;
        this.age = -1;
        this.name = "";
    }

}

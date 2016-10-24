package saberapplications.pawpads.model;

import com.google.gson.Gson;

/**
 * Created by Stanislav Volnjanskij on 19.10.16.
 */

public class UserProfile {
    int age;
    String gender;
    String hobby;
    int backgroundId=-1;
    private String about;

    public static UserProfile createFromJson(String json){
        Gson gson = new Gson();

        UserProfile out=gson.fromJson(json, UserProfile.class);
        if (out==null){
            out=new UserProfile();
        }
        return out;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public String getGender() {
        return gender!=null ? gender :"";
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getHobby() {
        return hobby;
    }

    public void setHobby(String hobby) {
        this.hobby = hobby;
    }

    public int getBackgroundId() {
        return backgroundId;
    }

    public void setBackgroundId(int backgroundId) {
        this.backgroundId = backgroundId;
    }

    public String getAbout() {
        return about;
    }

    public void setAbout(String about) {
        this.about = about;
    }
}

package nl.remcohoeneveld.birthdaynotifications.Model;

import java.util.Date;
import java.util.UUID;

import nl.remcohoeneveld.birthdaynotifications.Helper.UniqueIDHelper;

public class Birthday {

    public Date date_of_birth;
    public String full_name;
    public String nickname;
    public String uniqueID;
    public Integer notificationID;

    public Birthday(){
    }

    public Birthday(Date dateOfBirth, String fullName, String nickname) {
        this.date_of_birth = dateOfBirth;
        this.full_name = fullName;
        this.nickname = nickname;
        this.uniqueID = UUID.randomUUID().toString();
        this.notificationID = UniqueIDHelper.createUniqueId();
    }

    public Date getDate_of_birth(){
        return date_of_birth;
    }

    public String getFull_name(){
        return full_name;
    }

    public String getNickname(){
        return nickname;
    }

}

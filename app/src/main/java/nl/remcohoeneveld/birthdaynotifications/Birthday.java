package nl.remcohoeneveld.birthdaynotifications;

import java.util.Date;

public class Birthday {

    public Date date_of_birth;
    public String full_name;
    public String nickname;

    public Birthday(){

    }

    public Birthday(Date dateOfBirth, String fullName, String nickname) {
        this.date_of_birth = dateOfBirth;
        this.full_name = fullName;
        this.nickname = nickname;
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

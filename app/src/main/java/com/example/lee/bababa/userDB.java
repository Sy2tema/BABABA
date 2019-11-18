package com.example.lee.bababa;

//사용자의 정보를 저장하는 DB
public class userDB {
    public String gender;
    public String userName;
    public String userId;
    public String userAge;
    public String userBirthday;

    userDB() {

    }

    userDB(String userId, String userName, String gender, String userAge, String userBirthday) {
        this.gender = gender;
        this.userAge = userAge;
        this.userBirthday = userBirthday;
        this.userId = userId;
        this.userName = userName;
    }
}

package com.example.rhrn.RightHereRightNow.firebase_entry;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Matthew Moua on 2/17/2017.
 */
public class User {

    //User's information is public to be stored in the database
    public String   FirstName,
                    LastName,
                    DisplayName,
                    handle,
                    Email,
                    Phone,
                    Address,
                    City,
                    State,
                    zip,
                    id,
                    uid, // added user id, easier to use with FirebaseUser instance
                    AboutMe,
                    ProfilePicture;
    public ArrayList<String> followers; //TODO: Decide what is best to store followers -> uid? name?
    //public ArrayList<String> UsersMessaged;
    //public ArrayList<Map<String,Object>> UsersMessaged = new ArrayList<Map<String,Object>>();
    public int  ActivityPoints,
                LikesReceived;

    public String   fullName;

    //Private password to not allow storage on Firebase database
    private String Password;

    public User() {
        FirstName = LastName = DisplayName = handle = Email = Phone = Address = City = State = id = uid = AboutMe = null;
        followers = new ArrayList<String>();
        //ActivityPoints = 0;
        //LikesReceived = 0;
    }

    //Copy constructor to assign email to a user's full name
    public User(String aEmail, String fullname) {
        Email       = aEmail;
        fullName    = fullname;
    }

    public User(String aFirstName,String aLastName, String aDisplayName, String aHashTag,String aEmail, String aPassword,
                String aPhone,String aAddress,String aCity,String aState,String aId, String aUid, int activityPoints, int likesReceived) {
        FirstName   = aFirstName;
        LastName    = aLastName;
        DisplayName = aDisplayName;
        handle      = aHashTag;
        Email       = aEmail;
        Password    = aPassword;
        Phone       = aPhone;
        Address     = aAddress;
        City        = aCity;
        State       = aState;
        id          = aId;
        uid         = aUid;
        ActivityPoints = activityPoints;
        LikesReceived = likesReceived;
    }

    //Since phone is optional, need a constructor for one without phone
    public User(String aFirstName, String aLastName, String adisplayName, String aHashTag,String aEmail, String aPassword,
                String aAddress, String aCity, String aState, String aId, String aUid, int activityPoints, int likesReceived) {
        FirstName   = aFirstName;
        LastName    = aLastName;
        DisplayName = adisplayName;
        handle      = aHashTag;
        fullName    = FirstName + " " + String.valueOf(LastName);
        Email       = aEmail;
        Password    = aPassword;
        Address     = aAddress;
        City        = aCity;
        State       = aState;
        id          = aId;
        uid         = aUid;
        ActivityPoints = activityPoints;
        LikesReceived = likesReceived;
    }



    /*public String getFirstName() {
        return FirstName;
    }

    public String getLastName() {
        return LastName;
    }

    public String getUserEmail() {
        return Email;
    }

    public String getPhone() {
        return Phone;
    }

    public String getAddress() {
        return Address;
    }

    public String getCity() {
        return City;
    }

    public String getState() {
        return State;
    }*/
}

package com.codewithkomal.fyp;

public class PatientHelperClass {

    String userId, fname, email, password, age, phoneNo, gender, location;
    public PatientHelperClass(){}

    public PatientHelperClass(String id, String fname, String email, String age, String phoneNo, String gender, String location) {
        this.userId = id;
        this.fname = fname;
        //this.uname = uname;
        this.email = email;
        //this.password = password;
        this.age = age;
        this.phoneNo = phoneNo;
        this.gender = gender;
        this.location = location;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getFname() {
        return fname;
    }

    public void setFname(String fname) {
        this.fname = fname;
    }

    /*public String getUname() {
        return uname;
    }

    public void setUname(String uname) {
        this.uname = uname;
    }*/

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getAge() {
        return age;
    }

    public void setAge(String age) {
        this.age = age;
    }

    public String getPhoneNo() {
        return phoneNo;
    }

    public void setPhoneNo(String phoneNo) {
        this.phoneNo = phoneNo;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }
}

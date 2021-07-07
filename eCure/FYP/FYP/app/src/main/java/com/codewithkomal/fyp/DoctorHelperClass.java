package com.codewithkomal.fyp;

public class DoctorHelperClass {
    //these names should be same as the one's in Firebase DB
    String userId,fullName, hospitalName, email,address,qualification,specializationField,instituteName, experience,password,age,phoneNo, gender;
    String latitude, longitude;
    DoctorHelperClass(){}

    public DoctorHelperClass(String id,String fullName, String email, String address, String qualification, String specializationField, String instituteName, String experience, String age, String phoneNo, String gender, String latitude, String longitude, String hospital) {
        this.userId = id;
        this.fullName = fullName;
        //this.userName = userName;
        this.email = email;
        this.address = address;
        this.qualification = qualification;
        this.specializationField = specializationField;
        this.instituteName = instituteName;
        this.experience = experience;
        //this.password = password;
        this.age = age;
        this.phoneNo = phoneNo;
        this.gender = gender;
        this.latitude = latitude;
        this.longitude = longitude;
        this.hospitalName = hospital;
    }

    public String getHospitalName() {
        return hospitalName;
    }

    public void setHospitalName(String hospitalName) {
        this.hospitalName = hospitalName;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    /*public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }*/

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getQualification() {
        return qualification;
    }

    public void setQualification(String qualification) {
        this.qualification = qualification;
    }

    public String getSpecializationField() {
        return specializationField;
    }

    public void setSpecializationField(String specializationField) {
        this.specializationField = specializationField;
    }

    public String getInstituteName() {
        return instituteName;
    }

    public void setInstituteName(String instituteName) {
        this.instituteName = instituteName;
    }

    public String getExperience() {
        return experience;
    }

    public void setExperience(String experience) {
        this.experience = experience;
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

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }
}

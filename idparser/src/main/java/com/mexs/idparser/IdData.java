package com.mexs.idparser;

public class IdData {

    private String name;
    private String sgId;
    private String nationality;
    private String dob;
    private String gender;
    private String address;

    private Boolean nameConfident;
    private Boolean sgIdConfident;
    private Boolean nationalityConfident;
    private Boolean dobConfident;
    private Boolean genderConfident;
    private Boolean addressConfident;

    public IdData() {
        this.name = "";
        this.sgId = "";
        this.nationality = "";
        this.dob = "";
        this.gender = "";
        this.address = "";

        nameConfident = false;
        sgIdConfident = false;
        nationalityConfident = false;
        dobConfident = false;
        genderConfident = false;
        addressConfident = false;
    }

    public String getName() {
        return name;
    }

    public String getSgId() {
        return sgId;
    }

    public String getNationality() {
        return nationality;
    }

    public String getDob() {
        return dob;
    }

    public String getGender() {
        return gender;
    }

    public String getAddress() {
        return address;
    }

    public Boolean isNameConfident() {
        return nameConfident;
    }

    public Boolean isSgIdConfident() {
        return sgIdConfident;
    }

    public Boolean isNationalityConfident() {
        return nationalityConfident;
    }

    public Boolean isDobConfident() {
        return dobConfident;
    }

    public Boolean isGenderConfident() {
        return genderConfident;
    }

    public Boolean isAddressConfident() {
        return addressConfident;
    }

    public Boolean isConfidentAll(){
        return isNameConfident() && isSgIdConfident() && isNationalityConfident() && isDobConfident() && isGenderConfident() && isAddressConfident();
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setSgId(String sgId) {
        this.sgId = sgId;
    }

    public void setNationality(String nationality) {
        this.nationality = nationality;
    }

    public void setDob(String dob) {
        this.dob = dob;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public void setNameConfident(Boolean nameConfident) {
        this.nameConfident = nameConfident;
    }

    public void setSgIdConfident(Boolean sgIdConfident) {
        this.sgIdConfident = sgIdConfident;
    }

    public void setNationalityConfident(Boolean nationalityConfident) {
        this.nationalityConfident = nationalityConfident;
    }

    public void setDobConfident(Boolean dobConfident) {
        this.dobConfident = dobConfident;
    }

    public void setGenderConfident(Boolean genderConfident) {
        this.genderConfident = genderConfident;
    }

    public void setAddressConfident(Boolean addressConfident) {
        this.addressConfident = addressConfident;
    }

}
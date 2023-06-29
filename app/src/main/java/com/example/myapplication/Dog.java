package com.example.myapplication;

import java.io.Serializable;

public class Dog implements Serializable {
    private String name;
    private String breed;
    private String age;
    private String gender;
    private boolean isVaccinated;

    public Dog() {
        // 기본 생성자
    }

    public Dog(String name, String breed, String age, String gender, boolean isVaccinated) {
        this.name = name;
        this.breed = breed;
        this.age = age;
        this.gender = gender;
        this.isVaccinated = isVaccinated;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getBreed() {
        return breed;
    }

    public void setBreed(String breed) {
        this.breed = breed;
    }

    public String getAge() {
        return age;
    }

    public void setAge(String age) {
        this.age = age;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public boolean isVaccinated() {
        return isVaccinated;
    }

    public void setVaccinated(boolean vaccinated) {
        isVaccinated = vaccinated;
    }
}

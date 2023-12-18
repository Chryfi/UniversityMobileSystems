package com.chryfi.jogjoy.data;

import java.util.Objects;

public class User {
    private String username;
    private String password;
    private float weight;
    private float height;
    private Gender gender;

    public User(String username, String password, float weight, float height, Gender gender) {
        this.username = username;
        this.password = password;
        this.weight = weight;
        this.height = height;
        this.gender = gender;
    }

    public String getUsername() {
        return this.username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return this.password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public float getWeight() {
        return this.weight;
    }

    public void setWeight(float weight) {
        this.weight = weight;
    }

    public float getHeight() {
        return this.height;
    }

    public void setHeight(float height) {
        this.height = height;
    }

    public Gender getGender() {
        return this.gender;
    }

    public void setGender(Gender gender) {
        this.gender = gender;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof User)) return false;
        User user = (User) o;
        return Float.compare(user.weight, this.weight) == 0
                && Float.compare(user.height, this.height) == 0
                && this.username.equals(user.username)
                && this.password.equals(user.password)
                && this.gender == user.gender;
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.username, this.password, this.weight, this.height, this.gender);
    }

    public enum Gender {
        MALE("m"),
        FEMALE("f"),
        OTHER("other");

        private final String value;

        Gender(String value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return this.value;
        }

        /**
         * @param value
         * @return the gender enum which has the matching string value, or null if it was not found.
         */
        public static Gender fromString(String value) {
            switch (value) {
                case "m":
                    return MALE;
                case "f":
                    return FEMALE;
                case "other":
                    return OTHER;
            }

            return null;
        }
    }
}

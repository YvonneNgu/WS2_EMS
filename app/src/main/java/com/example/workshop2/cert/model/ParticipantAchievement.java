package com.example.workshop2.cert.model;

import android.os.Parcel;
import android.os.Parcelable;

public class ParticipantAchievement implements Parcelable {
    private String id;
    private String name;
    private String achievement;
    private boolean selected;

    public ParticipantAchievement(String id, String name, String achievement) {
        this.id = id;
        this.name = name;
        this.achievement = achievement;
        this.selected = false;
    }

    protected ParticipantAchievement(Parcel in) {
        id = in.readString();
        name = in.readString();
        achievement = in.readString();
        selected = in.readByte() != 0;
    }

    public static final Creator<ParticipantAchievement> CREATOR = new Creator<ParticipantAchievement>() {
        @Override
        public ParticipantAchievement createFromParcel(Parcel in) {
            return new ParticipantAchievement(in);
        }

        @Override
        public ParticipantAchievement[] newArray(int size) {
            return new ParticipantAchievement[size];
        }
    };

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getAchievement() {
        return achievement;
    }

    public void setAchievement(String achievement) {
        this.achievement = achievement;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(name);
        dest.writeString(achievement);
        dest.writeByte((byte) (selected ? 1 : 0));
    }
}


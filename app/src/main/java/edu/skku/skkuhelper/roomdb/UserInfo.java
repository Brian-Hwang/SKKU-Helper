package edu.skku.skkuhelper.roomdb;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class UserInfo {
    @NonNull
    @PrimaryKey(autoGenerate = false)
    public String userTOKEN;
    public String userId;
    public String userName;
    public int tag;
}

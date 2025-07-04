// User.java (unchanged)
package com.example.fakeapp;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "users")
public class User {
    @PrimaryKey(autoGenerate = true)
    public int id;

    public String phone;

    @ColumnInfo(name = "email")
    public String email;

    public String password;
}

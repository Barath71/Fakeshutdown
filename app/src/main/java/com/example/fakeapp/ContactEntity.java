// ContactEntity.java
package com.example.fakeapp;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
        foreignKeys = @ForeignKey(
                entity = User.class,
                parentColumns = "id",
                childColumns = "userId",
                onDelete = ForeignKey.CASCADE
        ),
        indices = @Index("userId")
)
public class ContactEntity {
    @PrimaryKey(autoGenerate = true)
    public int id;

    public String name;
    public String phone;

    public int userId;   // FK back to User.id
}

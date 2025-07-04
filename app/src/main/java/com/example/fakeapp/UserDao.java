package com.example.fakeapp;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface UserDao {

    // USER operations
    @Insert
    void insert(User user);

    @Query("SELECT * FROM users WHERE email = :email AND password = :password LIMIT 1")
    User login(String email, String password);

    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    User getUserByEmail(String email);

    // CONTACT operations (filtered by userId)
    @Insert
    void insertContact(ContactEntity contact);

    @Query("SELECT * FROM ContactEntity WHERE userId = :userId")
    List<ContactEntity> getContactsByUserId(int userId);

    // EMAIL operations (filtered by userId)
    @Insert
    void insertEmail(EmailEntity emailEntity);

    @Query("SELECT * FROM EmailEntity WHERE userId = :userId")
    List<EmailEntity> getEmailsByUserId(int userId);
}

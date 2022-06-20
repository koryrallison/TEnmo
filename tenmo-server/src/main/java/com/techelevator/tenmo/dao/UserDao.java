package com.techelevator.tenmo.dao;

import com.techelevator.tenmo.model.User;

import java.util.List;

public interface UserDao {

    User findById(long userId);

    List<User> findAll();

    List<User> findAllOthers(long userId);

    User findByUsername(String username);

    int findIdByUsername(String username);

    boolean create(String username, String password);
}

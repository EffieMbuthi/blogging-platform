package org.example.service;

import org.example.dao.UserDAO;
import org.example.model.User;
import java.sql.SQLException;
import java.util.List;

public class UserService {
    private UserDAO userDAO;

    public UserService() {
        this.userDAO = new UserDAO();
    }

    public void createUser(String name, String email) throws SQLException {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Name cannot be empty.");
        }
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Email cannot be empty.");
        }
        userDAO.createUser(name, email);
    }

    public List<User> getAllUsers() throws SQLException {
        return userDAO.findAllUsers();
    }
}
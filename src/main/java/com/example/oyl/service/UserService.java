package com.example.oyl.service;


import com.example.oyl.domain.User;
import com.example.oyl.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    public void register(User user) {
        userRepository.save(user);

    }

    public boolean login(String username, String password) {
        Optional<User> optionalUser = userRepository.findByUsername(username);
        return optionalUser.isPresent() && optionalUser.get().getPassword().equals(password);

    }


}

package com.example.oyl.service;


import com.example.oyl.domain.User;
import com.example.oyl.dto.UserSignupRequestDTO;
import com.example.oyl.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public interface UserService {

    void signup(UserSignupRequestDTO requestDTO);


}

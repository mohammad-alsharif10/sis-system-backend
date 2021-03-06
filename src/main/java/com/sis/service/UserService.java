package com.sis.service;

import com.sis.dto.security.LoginDTO;
import com.sis.dto.security.RegisterDTO;
import com.sis.dto.security.UserDto;
import com.sis.entity.mapper.UserMapper;
import com.sis.entity.security.User;
import com.sis.exception.InvalidUserNameOrPasswordException;
import com.sis.exception.UserNameOrEmailAlreadyExistException;
import com.sis.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService  extends BaseServiceImp<User>{

	@Autowired
	private UserRepository userRepository;
	@Autowired
	private UserMapper userMapper;
	@Autowired
	private PasswordEncoder passwordEncoder;

	@Override
    public JpaRepository<User, Long> Repository() {
        return userRepository;
    }

    /**
     * Create new user account
     *
     * @param accountDTO (username , email , password)
     * @return
     * @throws UserNameOrEmailAlreadyExistException
     */
    public UserDto createAccount(final RegisterDTO accountDTO) {

        if (userRepository.findByEmailOrUsername(accountDTO.getEmail(), accountDTO.getUsername()).isPresent())
            throw new UserNameOrEmailAlreadyExistException(accountDTO.getUsername(), accountDTO.getPassword());

        //map accountDTO to User
        final User user = new User();
        user.setFirstname(accountDTO.getFirstname());
        user.setLastname(accountDTO.getLastname());
        user.setEmail(accountDTO.getEmail());
        user.setPassword(passwordEncoder.encode(accountDTO.getPassword()));
		user.setUsername(accountDTO.getUsername());		
		//Save user
		return userMapper.toDTO(userRepository.save(user));
    }

    public UserDto login(final LoginDTO loginDTO) {
        User user = userRepository.findByUsername(loginDTO.getUsername()).orElseThrow(InvalidUserNameOrPasswordException::new);
        boolean isPasswordMatch = passwordEncoder.matches(loginDTO.getPassword(), user.getPassword());
        if (!isPasswordMatch)
            throw new InvalidUserNameOrPasswordException();
        return userMapper.toDTO(user);
	}

}

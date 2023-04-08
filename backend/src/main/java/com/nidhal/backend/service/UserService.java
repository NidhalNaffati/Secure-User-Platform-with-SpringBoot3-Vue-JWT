package com.nidhal.backend.service;


import com.nidhal.backend.entity.User;
import com.nidhal.backend.exception.EmailAlreadyExistsException;
import com.nidhal.backend.exception.PasswordDontMatchException;
import com.nidhal.backend.exception.UserNotFoundException;
import com.nidhal.backend.repository.TokenRepository;
import com.nidhal.backend.repository.UserRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * The UserService class handles business logic related to user accounts, such as saving and retrieving users,
 * updating passwords, and validating user credentials.
 */
@Service
@AllArgsConstructor @Slf4j
public class UserService {
    private final UserRepository userRepository;
    private final TokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;


    /**
     * Deletes all users from the database who have not yet activated their accounts.
     */
    @Transactional
    public void deleteUnactivatedUsers() {
        List<User> inactiveUsers = userRepository.findAllByEnabledIsFalse();
        for (User user : inactiveUsers) {
            tokenRepository.deleteAllByUserId(user.getId());
            log.info("deleting user: " + user.getEmail());
            userRepository.delete(user);
        }
    }

    /**
     * Determines whether a user account with the specified email address already exists.
     *
     * @param email the email address to check for
     * @return true if a user account with the specified email address exists, false otherwise
     */
    public boolean emailExists(String email) {
        return userRepository.existsByEmail(email);
    }


    /**
     * Finds a user account by the specified email address.
     *
     * @param email the email address to search for
     * @return the User object corresponding to the specified email address
     * @throws UserNotFoundException if no user account with the specified email address exists
     */
    public User findUserByEmail(String email) {
        return userRepository
                .findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("no user with email: " + email + " found"));
    }

    /**
     * Saves a new user account to the database, after checking that no other account with the same email address exists.
     *
     * @param user the User object to save
     * @throws EmailAlreadyExistsException if a user account with the same email address already exists
     */
    public User saveUser(User user) {
        String email = user.getEmail(); // get the email that the user have provided

        // check if the email already exists
        if (emailExists(email)) {
            // if the email already exists, throw an exception
            throw new EmailAlreadyExistsException();
        } else { // if the email doesn't exist, save the user

            String password = user.getPassword(); // get the password that the user have provided
            user.setPassword(passwordEncoder.encode(password)); // encode the password

           /* Set<Role> roles = user.getRoles(); // get the roles that the user have provided

            user.setRoles(roles); // set the roles*/

            userRepository.save(user); // save the user
        }
        return user;
    }

    /**
     * Updates the password for the user account with the specified email address, after validating that the new password and the
     * confirmation password match.
     *
     * @param email           the email address of the user account to update
     * @param password        the new password to set
     * @param confirmPassword the confirmation password to match against the new password
     * @throws UserNotFoundException      if no user account with the specified email address exists
     * @throws PasswordDontMatchException if the new password and the confirmation password do not match
     */
    public void updatePassword(String email, String password, String confirmPassword) {

        if (emailExists(email)) { // check if the email exists
            User user = findUserByEmail(email); // get the user

            if (password.equals(confirmPassword)) { // check if the password and the confirmPassword matches.
                user.setPassword(passwordEncoder.encode(confirmPassword)); // encode the new password
                userRepository.save(user); // save the user
            } else { // if the password doesn't match, throw an exception
                throw new PasswordDontMatchException();
            }
        } else { // if the email doesn't exist, throw an exception
            throw new UserNotFoundException("no user with email: " + email + " found");
        }
    }

    /**
     * Validates the specified email address and password, and returns the corresponding User object if the credentials are valid.
     *
     * @param email    the email address to validate
     * @param password the password to validate
     * @return the User object corresponding to the specified email address and password
     * @throws BadCredentialsException if the specified email address and password are invalid
     */
    public User validateCredentials(String email, String password) {

        User user = userRepository
                .findByEmail(email) // get the user by email
                .orElseThrow(
                        // if the user doesn't exist, throw an exception
                        () -> new BadCredentialsException("Invalid credentials")
                );

        // check if the password matches
        if (!passwordEncoder.matches(password, user.getPassword()))
            throw new BadCredentialsException("Invalid credentials");

        return user;
    }

    /**
     * Enables the user account with the specified email address.
     *
     * @param email the email address of the user account to enable
     * @throws UserNotFoundException if no user account with the specified email address exists
     */
    public void enableUser(String email) {
        // get the user by email
        User user = findUserByEmail(email);
        // enable the user
        user.setEnabled(true);
        // save the user
        userRepository.save(user);
    }

}
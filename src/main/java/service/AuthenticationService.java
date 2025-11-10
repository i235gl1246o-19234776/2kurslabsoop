package service;

import model.entity.User;
import repository.UserRepository;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.SQLException;
import java.util.Optional;

public class AuthenticationService {

    private final UserRepository userRepository;

    public AuthenticationService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }


    public String hashPassword(String password) {
        return BCrypt.hashpw(password, BCrypt.gensalt());
    }

    public boolean authenticateUser(String name, String password) throws SQLException {
        Optional<User> userOpt = userRepository.findByName(name);
        if (userOpt.isPresent()) {
            String storedHash = userOpt.get().getPasswordHash();
            return BCrypt.checkpw(password, storedHash);
        }
        return false;
    }

    public void registerUser(String name, String password) throws SQLException {
        String hashedPassword = hashPassword(password);
        User newUser = new User();
        newUser.setName(name);
        newUser.setPasswordHash(hashedPassword);

        userRepository.createUser(newUser);
    }
}

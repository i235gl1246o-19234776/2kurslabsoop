package model.service;

import model.entity.User;
import repository.dao.UserRepository;

import java.sql.SQLException;
import java.util.Optional;
import java.util.logging.Logger;
import java.util.logging.Level;

public class AuthenticationService {
    private static final Logger logger = Logger.getLogger(AuthenticationService.class.getName());
    private final UserRepository userRepository;

    public AuthenticationService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public boolean authenticateUser(String username, String password) throws SQLException {
        Optional<User> userOpt = userRepository.findByName(username);

        if (userOpt.isEmpty()) {
            logger.warning("Пользователь не найден: " + username);
            return false;
        }

        User user = userOpt.get();

        // ПРЯМОЕ сравнение паролей без хеширования
        String storedPassword = user.getPasswordHash(); // теперь это просто пароль

        // Если пароль в базе NULL - аутентификация не пройдена
        if (storedPassword == null) {
            logger.warning("Password is NULL for user: " + username);
            return false;
        }

        // Простое сравнение строк
        boolean isAuthenticated = storedPassword.equals(password);

        if (isAuthenticated) {
            logger.info("User authenticated successfully: " + username);
        } else {
            logger.warning("Password mismatch for user: " + username);
        }

        return isAuthenticated;
    }
}
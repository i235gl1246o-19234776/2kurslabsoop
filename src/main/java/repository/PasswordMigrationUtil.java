package repository;

import repository.dao.UserRepository;
import model.entity.User;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.SQLException;
import java.util.List;
import java.util.logging.Logger;

public class PasswordMigrationUtil {
    private static final Logger logger = Logger.getLogger(PasswordMigrationUtil.class.getName());

    public static void main(String[] args) {
        try {
            DatabaseConnection connection = new DatabaseConnection();
            UserRepository userRepository = new UserRepository(connection);

            // Получаем всех пользователей
            List<User> users = userRepository.findAll();

            for (User user : users) {
                String currentHash = user.getPasswordHash();

                // Если пароль не в BCrypt формате, хэшируем его
                if (!isValidBcryptHash(currentHash)) {
                    String newHash = BCrypt.hashpw(currentHash, BCrypt.gensalt());
                    userRepository.updatePasswordHash(user.getId(), newHash);
                    logger.info("Migrated password for user: " + user.getName());
                }
            }

            logger.info("Password migration completed successfully");

        } catch (SQLException e) {
            logger.severe("Error during password migration: " + e.getMessage());
        }
    }

    private static boolean isValidBcryptHash(String hash) {
        if (hash == null || hash.length() < 60) {
            return false;
        }
        return hash.startsWith("$2a$") || hash.startsWith("$2b$") || hash.startsWith("$2y$");
    }
}
package core;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class VerifyHash {

    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(); // <-- Как в SecurityConfig

        String rawPassword = "admin123";
        String expectedHash = "$2a$10$St1FV5.S1V3te4VijF2NleUNOu4H8YkQv4OjqwNUvOt3UFU6xcrtm";

        boolean matches = encoder.matches(rawPassword, expectedHash);

        System.out.println("Пароль: " + rawPassword);
        System.out.println("Хэш из БД: " + expectedHash);
        System.out.println("Совпадают? " + matches);

        String newHash = encoder.encode(rawPassword);
        System.out.println("Новый хэш для '" + rawPassword + "': " + newHash);
    }

}

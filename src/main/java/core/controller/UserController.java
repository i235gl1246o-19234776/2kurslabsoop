package core.controller;

import core.entity.*;
import core.repository.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@RestController
@RequestMapping("/api/users")
public class UserController {


    @Autowired
    private UserRepository userRepository;

    @GetMapping
    public ResponseEntity<List<UserEntity>> getAllUsers(){
        log.info("Запрос на получение всех пользователей");
        List<UserEntity> users = userRepository.findAll();
        return ResponseEntity.ok(users);
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserEntity> getUserById(@PathVariable Long id){
        log.info("Запрос на получение пользователя с ID: {}", id);
        Optional<UserEntity> user = userRepository.findById(id);
        return user.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/name/{name}")
    public ResponseEntity<UserEntity> getUserByName(@PathVariable String name){
        log.info("Запрос на поиск пользователя по имени: {}", name);
        UserEntity user = userRepository.findByName(name);
        if (user != null){
            return ResponseEntity.ok(user);
        }
        else{
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/exist/{name}")
    public ResponseEntity<Boolean> userExists(@PathVariable String name){
        log.info("Запрос на проверку существования пользователя: {}", name);
        boolean exists = userRepository.existsByName(name);
        return ResponseEntity.ok(exists);
    }

    @PostMapping
    public ResponseEntity<UserEntity> createUser(@RequestBody UserEntity user){
        log.info("Запрос на создание пользователя: {}", user.getName());
        user.setId(null);
        UserEntity savedUser = userRepository.save(user);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedUser);
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserEntity> updateUser(@PathVariable Long id, @RequestBody UserEntity userDetails){
        log.info("Запрос на обновление пользователя с ID: {}", id);
        Optional<UserEntity> existingUserOpt = userRepository.findById(id);
        if (existingUserOpt.isPresent()){
            UserEntity existingUser = existingUserOpt.get();
            existingUser.setName(userDetails.getName());
            existingUser.setPasswordHash(userDetails.getPasswordHash());
            UserEntity updateUser = userRepository.save(existingUser);
            log.info("Пользователь с ID {} обновлен", id);
            return ResponseEntity.ok(updateUser);
        }
        else{
            log.warn("Попытка обновить несуществующего пользователя с ID: {}", id);
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id){
        log.info("Запрос на удаление пользователя с ID: {}", id);
        if (userRepository.existsById(id)){
            userRepository.deleteById(id);
            log.info("Пользователь с ID {} удален", id);
            return ResponseEntity.noContent().build();
        }
        else{
            log.warn("Попытка удалить несуществующего пользователя с ID: {}", id);
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/authenticate")
    public ResponseEntity<?> authenticateUser(@RequestBody AuthRequest authRequest){
        log.info("Запрос на аутентификацию пользователя: {}", authRequest.getName());
        UserEntity user = userRepository.findByName(authRequest.getName());
        if (user != null && user.getPasswordHash().equals(authRequest.getPassword())){
            log.info("Аутентификация прошла успешно для пользователя: {}",user.getName());
            return ResponseEntity.ok(user);
        }
        else{
            log.warn("Аутентификая не удалась для пользователя: {}", authRequest.getName());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("authenticated", false));
        }

    }

    static class AuthRequest{
        private String name;
        private String password;

        public String getName(){ return name; }
        public void setName(String name){ this.name = name; }
        public String getPassword(){ return password; }
        public void setPassword(String password) { this.password = password; }
    }
}

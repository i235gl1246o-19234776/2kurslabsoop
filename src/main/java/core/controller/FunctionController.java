package core.controller;

import core.entity.*;
import core.repository.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


import java.util.List;
import java.util.Optional;

@Slf4j
@RestController
@RequestMapping("/api/functions")
public class FunctionController {

    @Autowired
    private FunctionRepository functionRepository;

    @Autowired
    private UserRepository userRepository;

    @GetMapping
    public ResponseEntity<List<FunctionEntity>> getFunctionsByUserId(@RequestParam(required = false) Long userId) {
        if (userId != null){
            log.info("Запрос на получение функция для пользователя ID: {}", userId);
            List<FunctionEntity> functions = functionRepository.findByUser_Id(userId);
            return ResponseEntity.ok(functions);
        }
        else{
            log.info("Запрос на получение всех функций");
            List<FunctionEntity> functions = functionRepository.findAll();
            return ResponseEntity.ok(functions);
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<FunctionEntity> getFunctionById(@PathVariable Long id){
        log.info("Запрос на получение функции с ID: {}", id);
        Optional<FunctionEntity> function = functionRepository.findById(id);
        return function.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<FunctionEntity> createFunction(@RequestBody FunctionEntity function){
        log.info("Запрос на создание функции для пользователя ID: {}",function.getUser().getId());
        UserEntity user = userRepository.findById(function.getUser().getId())
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));
        function.setUser(user);
        function.setId(null);
        FunctionEntity savedFunction = functionRepository.save(function);
        log.info("Функция создана с ID: {}", savedFunction.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(savedFunction);
    }

    @PutMapping("/{id}")
    public ResponseEntity<FunctionEntity> updateFunction(@PathVariable Long id, @RequestBody FunctionEntity functionDetails){
        log.info("Запрос на обновление функции с ID: {}", id);
        Optional<FunctionEntity> existingFunctionOpt = functionRepository.findById(id);
        if (existingFunctionOpt.isPresent()) {
            FunctionEntity existingFunction = existingFunctionOpt.get();
            existingFunction.setTypeFunction(functionDetails.getTypeFunction());
            existingFunction.setFunctionName(functionDetails.getFunctionName());
            existingFunction.setFunctionExpression(functionDetails.getFunctionExpression());
            if (!existingFunction.getUser().getId().equals(functionDetails.getUser().getId())) {
                UserEntity newUser = userRepository.findById(functionDetails.getUser().getId())
                        .orElseThrow(() -> new RuntimeException("Пользователь не найден"));
                existingFunction.setUser(newUser);
            }
            FunctionEntity updatedFunction = functionRepository.save(existingFunction);
            log.info("Функция с ID {} обновлена", id);
            return ResponseEntity.ok(updatedFunction);
        } else {
            log.warn("Попытка обновить несуществующую функцию с ID: {}", id);
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteFunction(@PathVariable Long id, @RequestParam(required = false) Long userId) {
        log.info("Запрос на удаление функции с ID: {} (проверка userId: {})", id, userId);
        Optional<FunctionEntity> functionOpt = functionRepository.findById(id);
        if (functionOpt.isPresent()) {
            FunctionEntity function = functionOpt.get();
            if (userId != null && !function.getUser().getId().equals(userId)) {
                log.warn("Попытка удаления функции {} пользователем {}, не являющимся владельцем", id, userId);
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
            functionRepository.delete(function);
            log.info("Функция с ID {} удалена", id);
            return ResponseEntity.noContent().build();
        } else {
            log.warn("Попытка удалить несуществующую функцию с ID: {}", id);
            return ResponseEntity.notFound().build();
        }
    }
}

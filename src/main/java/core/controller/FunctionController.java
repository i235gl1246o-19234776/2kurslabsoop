package core.controller;

import core.entity.*;
import core.repository.*;
import core.dto.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api/functions")
public class FunctionController {

    @Autowired
    private FunctionRepository functionRepository;

    @Autowired
    private UserRepository userRepository;

    @GetMapping
    public ResponseEntity<List<FunctionDto>> getFunctions(
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String type) {

        log.info("Запрос на получение функций с параметрами: userId={}, name={}, type={}", userId, name, type);

        List<FunctionEntity> functions;

        if (userId != null) {
            functions = functionRepository.findByUser_Id(userId);
        } else {
            functions = functionRepository.findAll();
        }

        if (name != null) {
            functions = functions.stream()
                    .filter(f -> f.getFunctionName().equals(name))
                    .collect(Collectors.toList());
        }

        if (type != null) {
            FunctionEntity.FunctionType typeEnum = FunctionEntity.FunctionType.valueOf(type.toLowerCase());
            functions = functions.stream()
                    .filter(f -> f.getTypeFunction() == typeEnum)
                    .collect(Collectors.toList());
        }

        List<FunctionDto> functionDtos = functions.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());

        log.info("Возвращено {} функций", functionDtos.size());
        return ResponseEntity.ok(functionDtos);
    }

    @GetMapping("/{id}")
    public ResponseEntity<FunctionDto> getFunctionById(
            @PathVariable Long id,
            @RequestParam(required = false) Long userId) {
        log.info("Запрос на получение функции с ID: {} (проверка userId: {})", id, userId);
        Optional<FunctionEntity> funcOpt = functionRepository.findById(id);
        if (funcOpt.isPresent()) {
            FunctionEntity func = funcOpt.get();
            if (userId != null && !func.getUser().getId().equals(userId)) {
                log.warn("Попытка доступа к функции {}, не принадлежащей пользователю {}", id, userId);
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
            FunctionDto functionDto = convertToDto(func);
            log.info("Функция с ID {} найдена", id);
            return ResponseEntity.ok(functionDto);
        } else {
            log.warn("Функция с ID {} не найдена", id);
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping
    public ResponseEntity<FunctionDto> createFunction(@RequestBody FunctionDto functionDto) {
        log.info("Запрос на создание функции: {}", functionDto);

        if (functionDto.getUserId() == null) {
            log.error("ID пользователя не может быть null при создании функции");
            return ResponseEntity.badRequest().build();
        }

        try {
            log.info("Поиск пользователя с ID: {}", functionDto.getUserId());
            UserEntity user = userRepository.findById(functionDto.getUserId())
                    .orElseThrow(() -> {
                        log.error("Пользователь с ID {} не найден в базе данных", functionDto.getUserId());
                        return new RuntimeException("Пользователь не найден");
                    });

            log.info("Найден пользователь: {} (ID: {})", user.getName(), user.getId());

            FunctionEntity funcEntity = new FunctionEntity();
            funcEntity.setUser(user);

            String typeString = functionDto.getTypeFunction();
            log.info("Получен тип функции: {}", typeString);

            try {
                FunctionEntity.FunctionType typeEnum = FunctionEntity.FunctionType.valueOf(typeString.toLowerCase());
                funcEntity.setTypeFunction(typeEnum);
                log.info("Установлен тип функции: {}", typeEnum);
            } catch (IllegalArgumentException e) {
                log.error("Неверный тип функции: '{}'. Доступные типы: {}",
                        typeString,
                        Arrays.toString(FunctionEntity.FunctionType.values()));
                return ResponseEntity.badRequest().build();
            }

            funcEntity.setFunctionName(functionDto.getFunctionName());
            funcEntity.setFunctionExpression(functionDto.getFunctionExpression());

            log.info("Сохранение функции в базу...");
            FunctionEntity savedEntity = functionRepository.save(funcEntity);
            FunctionDto savedDto = convertToDto(savedEntity);

            log.info("Функция '{}' создана с ID: {}", savedDto.getFunctionName(), savedDto.getId());
            return ResponseEntity.status(HttpStatus.CREATED).body(savedDto);

        } catch (RuntimeException e) {
            log.error("Ошибка при создании функции: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<FunctionDto> updateFunction(
            @PathVariable Long id,
            @RequestBody FunctionDto functionDtoDetails) {
        log.info("Запрос на обновление функции с ID: {}", id);
        Optional<FunctionEntity> funcOpt = functionRepository.findById(id);
        if (funcOpt.isPresent()) {
            FunctionEntity func = funcOpt.get();
            func.setTypeFunction(FunctionEntity.FunctionType.valueOf(
                    functionDtoDetails.getTypeFunction().toLowerCase()));
            func.setFunctionName(functionDtoDetails.getFunctionName());
            func.setFunctionExpression(functionDtoDetails.getFunctionExpression());

            if (!func.getUser().getId().equals(functionDtoDetails.getUserId())) {
                UserEntity newUser = userRepository.findById(functionDtoDetails.getUserId())
                        .orElseThrow(() -> new RuntimeException("Пользователь не найден при обновлении функции"));
                func.setUser(newUser);
            }

            FunctionEntity updatedEntity = functionRepository.save(func);
            FunctionDto updatedDto = convertToDto(updatedEntity);

            log.info("Функция с ID {} обновлена", id);
            return ResponseEntity.ok(updatedDto);
        } else {
            log.warn("Попытка обновить несуществующую функцию с ID: {}", id);
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteFunction(
            @PathVariable Long id,
            @RequestParam(required = false) Long userId) {

        log.info("Запрос на удаление функции с ID: {} (проверка userId: {})", id, userId);

        Optional<FunctionEntity> funcOpt = functionRepository.findById(id);

        if (funcOpt.isPresent()) {
            FunctionEntity func = funcOpt.get();

            if (userId != null && !func.getUser().getId().equals(userId)) {
                log.warn("Попытка удаления функции {}, не принадлежащей пользователю {}", id, userId);
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            functionRepository.delete(func);
            log.info("Функция с ID {} удалена", id);
            return ResponseEntity.noContent().build();
        } else {
            log.warn("Попытка удалить несуществующую функцию с ID: {}", id);
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/search")
    public ResponseEntity<List<FunctionDto>> searchFunctions(
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) String functionName,
            @RequestParam(required = false) String typeFunction) {

        log.info("Запрос на расширенный поиск функций с параметрами: userId={}, functionName={}, typeFunction={}", userId, functionName, typeFunction);

        List<FunctionEntity> functions;

        if (userId != null) {
            functions = functionRepository.findByUser_Id(userId);
        } else {
            functions = functionRepository.findAll();
        }

        if (functionName != null) {
            functions = functions.stream()
                    .filter(f -> f.getFunctionName().toLowerCase().contains(functionName.toLowerCase()))
                    .collect(Collectors.toList());
        }

        if (typeFunction != null) {
            try {
                FunctionEntity.FunctionType typeEnum = FunctionEntity.FunctionType.valueOf(typeFunction.toLowerCase());
                functions = functions.stream()
                        .filter(f -> f.getTypeFunction() == typeEnum)
                        .collect(Collectors.toList());
            } catch (IllegalArgumentException e) {
                log.warn("Неверный тип функции: {}", typeFunction);
                return ResponseEntity.badRequest().build();
            }
        }

        List<FunctionDto> functionDtos = functions.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());

        log.info("Возвращено {} функций", functionDtos.size());
        return ResponseEntity.ok(functionDtos);
    }

    private FunctionDto convertToDto(FunctionEntity funcEntity) {
        List<Long> tabulatedIds = funcEntity.getTabulatedValues() != null ?
                funcEntity.getTabulatedValues().stream()
                        .map(TabulatedFunctionEntity::getId)
                        .collect(Collectors.toList()) : new ArrayList<>();

        List<Long> operationIds = funcEntity.getOperations() != null ?
                funcEntity.getOperations().stream()
                        .map(OperationEntity::getId)
                        .collect(Collectors.toList()) : new ArrayList<>();


        Long userId = funcEntity.getUser() != null ? funcEntity.getUser().getId() : null;

        return new FunctionDto(
                funcEntity.getId(),
                userId,
                funcEntity.getTypeFunction().name().toLowerCase(),
                funcEntity.getFunctionName(),
                funcEntity.getFunctionExpression(),
                tabulatedIds,
                operationIds
        );
    }
}
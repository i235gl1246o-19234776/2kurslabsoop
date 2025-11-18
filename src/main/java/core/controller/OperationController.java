package core.controller;

import core.entity.*;
import core.repository.*;
import core.dto.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api/operations")
public class OperationController {

    @Autowired
    private OperationRepository operationRepository;

    @Autowired
    private FunctionRepository functionRepository;

    @Autowired
    private UserRepository userRepository;

    private boolean hasAccessToFunction(Long functionId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            log.warn("Проверка доступа: пользователь не аутентифицирован.");
            return false;
        }

        String currentRole = auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .findFirst()
                .orElse("");
        log.debug("Проверка доступа к функции {}: текущая роль = '{}'", functionId, currentRole);

        if ("ROLE_ADMIN".equals(currentRole)) {
            log.debug("Проверка доступа: доступ разрешён для администратора.");
            return true;
        }

        String currentUsername = auth.getName();
        UserEntity currentUser = userRepository.findByName(currentUsername);
        if (currentUser == null) {
            log.warn("Проверка доступа: аутентифицированный пользователь '{}' не найден в БД.", currentUsername);
            return false;
        }

        Optional<FunctionEntity> functionOpt = functionRepository.findById(functionId);
        if (functionOpt.isEmpty()) {
            log.warn("Проверка доступа: функция с ID {} не найдена.", functionId);
            return false;
        }

        UserEntity owner = functionOpt.get().getUser();
        boolean hasAccess = owner.getId().equals(currentUser.getId());
        log.debug("Проверка доступа: пользователь '{}' имеет доступ к функции {}: {}", currentUsername, functionId, hasAccess);
        return hasAccess;
    }


    @GetMapping("/{id}")
    public ResponseEntity<OperationDto> getOperationById(@PathVariable Long id) {
        log.info("Запрос на получение операции с ID: {}", id);

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            log.warn("Попытка доступа без аутентификации к операции ID: {}", id);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Optional<OperationEntity> opOpt = operationRepository.findById(id);
        if (opOpt.isPresent()) {
            OperationEntity op = opOpt.get();
            Long functionId = op.getFunction().getId();

            if (!hasAccessToFunction(functionId)) {
                log.warn("Пользователь '{}' не имеет доступа к операции {}, принадлежащей функции {}", auth.getName(), id, functionId);
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            OperationDto operationDto = convertToDto(op);
            log.info("Операция с ID {} найдена", id);
            return ResponseEntity.ok(operationDto);
        } else {
            log.warn("Операция с ID {} не найдена", id);
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping
    public ResponseEntity<OperationDto> createOperation(@RequestBody OperationDto operationDto) {
        log.info("Запрос на создание операции для функции ID: {}", operationDto.getFunctionId());

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            log.warn("Попытка создания операции без аутентификации");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Optional<FunctionEntity> functionOpt = functionRepository.findById(operationDto.getFunctionId());
        if (functionOpt.isEmpty()) {
            log.warn("Функция с ID {} не найдена при создании операции", operationDto.getFunctionId());
            return ResponseEntity.notFound().build();
        }

        if (!hasAccessToFunction(operationDto.getFunctionId())) {
            log.warn("Пользователь '{}' пытается создать операцию для функции {}, к которой не имеет доступа", auth.getName(), operationDto.getFunctionId());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        OperationEntity opEntity = new OperationEntity();
        opEntity.setFunction(functionOpt.get());
        opEntity.setOperationsTypeId(operationDto.getOperationsTypeId());

        OperationEntity savedEntity = operationRepository.save(opEntity);
        OperationDto savedDto = convertToDto(savedEntity);
        log.info("Операция создана с ID: {} для функции ID: {}", savedDto.getId(), savedDto.getFunctionId());
        return ResponseEntity.status(HttpStatus.CREATED).body(savedDto);
    }

    @PutMapping("/{id}")
    public ResponseEntity<OperationDto> updateOperation(@PathVariable Long id, @RequestBody OperationDto operationDtoDetails) {
        log.info("Запрос на обновление операции с ID: {}", id);

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            log.warn("Попытка обновления операции без аутентификации к операции ID: {}", id);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Optional<OperationEntity> opOpt = operationRepository.findById(id);
        if (opOpt.isPresent()) {
            OperationEntity op = opOpt.get();
            Long functionId = op.getFunction().getId();

            if (!hasAccessToFunction(functionId)) {
                log.warn("Пользователь '{}' не имеет прав на обновление операции {}, принадлежащей функции {}", auth.getName(), id, functionId);
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            op.setOperationsTypeId(operationDtoDetails.getOperationsTypeId());

            OperationEntity updatedEntity = operationRepository.save(op);
            OperationDto updatedDto = convertToDto(updatedEntity);
            log.info("Операция с ID {} обновлена", id);
            return ResponseEntity.ok(updatedDto);
        } else {
            log.warn("Попытка обновить несуществующую операцию с ID: {}", id);
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteOperation(@PathVariable Long id) {
        log.info("Запрос на удаление операции с ID: {}", id);

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            log.warn("Попытка удаления операции без аутентификации к операции ID: {}", id);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Optional<OperationEntity> opOpt = operationRepository.findById(id);
        if (opOpt.isPresent()) {
            OperationEntity op = opOpt.get();
            Long functionId = op.getFunction().getId();

            if (!hasAccessToFunction(functionId)) {
                log.warn("Пользователь '{}' не имеет прав на удаление операции {}, принадлежащей функции {}", auth.getName(), id, functionId);
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            operationRepository.delete(op);
            log.info("Операция с ID {} удалена", id);
            return ResponseEntity.noContent().build();
        } else {
            log.warn("Попытка удалить несуществующую операцию с ID: {}", id);
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/function/{functionId}")
    public ResponseEntity<Void> deleteAllOperationsByFunctionId(@PathVariable Long functionId) {
        log.info("Запрос на удаление всех операций для функции ID: {}", functionId);

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            log.warn("Попытка удаления операций без аутентификации для функции ID: {}", functionId);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Optional<FunctionEntity> functionOpt = functionRepository.findById(functionId);
        if (functionOpt.isEmpty()) {
            log.warn("Функция с ID {} не найдена при удалении операций", functionId);
            return ResponseEntity.notFound().build();
        }

        if (!hasAccessToFunction(functionId)) {
            log.warn("Пользователь '{}' пытается удалить операции функции {}, к которой не имеет доступа", auth.getName(), functionId);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        List<OperationEntity> operations = operationRepository.findAll().stream()
                .filter(op -> op.getFunction().getId().equals(functionId))
                .collect(Collectors.toList());

        operationRepository.deleteAll(operations);
        log.info("Удалено {} операций для функции ID: {}", operations.size(), functionId);
        return ResponseEntity.noContent().build();
    }

    private OperationDto convertToDto(OperationEntity opEntity) {
        Long functionId = opEntity.getFunction() != null ? opEntity.getFunction().getId() : null;
        return new OperationDto(
                opEntity.getId(),
                functionId,
                opEntity.getOperationsTypeId()
        );
    }
}
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
@RequestMapping("/api/operations")
public class OperationController {

    @Autowired
    private OperationRepository operationRepository;

    @Autowired
    private FunctionRepository functionRepository;

    @GetMapping("/{id}")
    public ResponseEntity<OperationEntity> getOperationById(@PathVariable Long id, @RequestParam(required = false) Long functionId) {
        log.info("Запрос на получение операции с ID: {} (проверка functionId: {})", id, functionId);
        Optional<OperationEntity> opOpt = operationRepository.findById(id);
        if (opOpt.isPresent()) {
            OperationEntity op = opOpt.get();
            if (functionId != null && !op.getFunction().getId().equals(functionId)) {
                log.warn("Попытка получения операции {} функции {}, не являющейся владельцем", id, functionId);
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
            return ResponseEntity.ok(op);
        } else {
            log.warn("Попытка получить несуществующую операцию с ID: {}", id);
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping
    public ResponseEntity<OperationEntity> createOperation(@RequestBody OperationEntity operation) {
        log.info("Запрос на создание операции для функции ID: {}", operation.getFunction().getId());
        FunctionEntity function = functionRepository.findById(operation.getFunction().getId())
                .orElseThrow(() -> new RuntimeException("Функция не найдена"));
        operation.setFunction(function);
        operation.setId(null);
        OperationEntity savedOperation = operationRepository.save(operation);
        log.info("Операция создана с ID: {}", savedOperation.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(savedOperation);
    }

    @PutMapping("/{id}")
    public ResponseEntity<OperationEntity> updateOperation(@PathVariable Long id, @RequestBody OperationEntity operationDetails) {
        log.info("Запрос на обновление операции с ID: {}", id);
        Optional<OperationEntity> existingOpOpt = operationRepository.findById(id);
        if (existingOpOpt.isPresent()) {
            OperationEntity existingOp = existingOpOpt.get();
            existingOp.setOperationsTypeId(operationDetails.getOperationsTypeId());
            if (!existingOp.getFunction().getId().equals(operationDetails.getFunction().getId())) {
                FunctionEntity newFunction = functionRepository.findById(operationDetails.getFunction().getId())
                        .orElseThrow(() -> new RuntimeException("Функция не найдена"));
                existingOp.setFunction(newFunction);
            }
            OperationEntity updatedOp = operationRepository.save(existingOp);
            log.info("Операция с ID {} обновлена", id);
            return ResponseEntity.ok(updatedOp);
        } else {
            log.warn("Попытка обновить несуществующую операцию с ID: {}", id);
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteOperation(@PathVariable Long id, @RequestParam(required = false) Long functionId) {
        log.info("Запрос на удаление операции с ID: {} (проверка functionId: {})", id, functionId);
        Optional<OperationEntity> opOpt = operationRepository.findById(id);
        if (opOpt.isPresent()) {
            OperationEntity op = opOpt.get();
            if (functionId != null && !op.getFunction().getId().equals(functionId)) {
                log.warn("Попытка удаления операции {} функции {}, не являющейся владельцем", id, functionId);
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

    @DeleteMapping("/function")
    public ResponseEntity<Void> deleteAllOperationsByFunctionId(@RequestParam Long functionId) {
        log.info("Запрос на удаление всех операций для функции ID: {}", functionId);
        List<OperationEntity> operations = operationRepository.findAll().stream()
                .filter(op -> op.getFunction() != null && op.getFunction().getId().equals(functionId))
                .toList();
        operationRepository.deleteAll(operations);
        log.info("Все {} операции для функции ID {} удалены", operations.size(), functionId);
        return ResponseEntity.noContent().build();
    }
}
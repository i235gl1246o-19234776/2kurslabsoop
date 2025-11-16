package core.controller;

import core.entity.*;
import core.repository.*;
import core.dto.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

    @GetMapping("/{id}")
    public ResponseEntity<OperationDto> getOperationById(@PathVariable Long id, @RequestParam(required = false) Long functionId) {
        log.info("Запрос на получение операции с ID: {} (проверка functionId: {})", id, functionId);
        Optional<OperationEntity> opOpt = operationRepository.findById(id);
        if (opOpt.isPresent()) {
            OperationEntity op = opOpt.get();
            if (functionId != null && !op.getFunction().getId().equals(functionId)) {
                log.warn("Попытка доступа к операции {}, не принадлежащей функции {}", id, functionId);
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build(); // 403 Forbidden
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
        FunctionEntity function = functionRepository.findById(operationDto.getFunctionId())
                .orElseThrow(() -> new RuntimeException("Функция не найдена при создании операции"));

        OperationEntity opEntity = new OperationEntity();
        opEntity.setFunction(function);
        opEntity.setOperationsTypeId(operationDto.getOperationsTypeId());

        OperationEntity savedEntity = operationRepository.save(opEntity);
        OperationDto savedDto = convertToDto(savedEntity);
        log.info("Операция создана с ID: {}", savedDto.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(savedDto);
    }

    @PutMapping("/{id}")
    public ResponseEntity<OperationDto> updateOperation(@PathVariable Long id, @RequestBody OperationDto operationDtoDetails) {
        log.info("Запрос на обновление операции с ID: {}", id);
        Optional<OperationEntity> opOpt = operationRepository.findById(id);
        if (opOpt.isPresent()) {
            OperationEntity op = opOpt.get();
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
    public ResponseEntity<Void> deleteOperation(@PathVariable Long id, @RequestParam(required = false) Long functionId) {
        log.info("Запрос на удаление операции с ID: {} (проверка functionId: {})", id, functionId);
        Optional<OperationEntity> opOpt = operationRepository.findById(id);
        if (opOpt.isPresent()) {
            OperationEntity op = opOpt.get();
            if (functionId != null && !op.getFunction().getId().equals(functionId)) {
                log.warn("Попытка удаления операции {}, не принадлежащей функции {}", id, functionId);
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build(); // 403 Forbidden
            }
            operationRepository.delete(op);
            log.info("Операция с ID {} удалена", id);
            return ResponseEntity.noContent().build(); // 204 No Content
        } else {
            log.warn("Попытка удалить несуществующую операцию с ID: {}", id);
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/function/{functionId}")
    public ResponseEntity<Void> deleteAllOperationsByFunctionId(@PathVariable Long functionId) {
        log.info("Запрос на удаление всех операций для функции ID: {}", functionId);

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
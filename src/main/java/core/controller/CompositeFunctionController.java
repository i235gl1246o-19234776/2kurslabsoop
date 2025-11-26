package core.controller;

import core.dto.CompositeFunctionDto;
import core.dto.FunctionDto;
import core.entity.FunctionEntity;
import core.entity.UserEntity;
import core.repository.FunctionRepository;
import core.repository.UserRepository;
import functions.CompositeFunction;
import functions.MathFunction;
import core.utils.MathFunctionRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api/functions")
public class CompositeFunctionController {

    @Autowired
    private FunctionRepository functionRepository;

    @Autowired
    private UserRepository userRepository;

    @PostMapping("/composite")
    public ResponseEntity<FunctionDto> createCompositeFunction(
            @RequestBody CompositeFunctionDto compositeDto) {
        log.info("Запрос на создание сложной функции: {}", compositeDto);

        try {
            // Проверяем существование пользователя
            UserEntity user = userRepository.findById(compositeDto.getUserId())
                    .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

            // Получаем базовую и внешнюю функции
            MathFunction baseFunction = MathFunctionRegistry.getFunctionByName(compositeDto.getBaseFunctionName());
            MathFunction outerFunction = MathFunctionRegistry.getFunctionByName(compositeDto.getOuterFunctionName());

            // Создаем сложную функцию
            CompositeFunction compositeFunction = new CompositeFunction(outerFunction, baseFunction);

            // Создаем сущность функции
            FunctionEntity functionEntity = new FunctionEntity();
            functionEntity.setUser(user);
            functionEntity.setTypeFunction(FunctionEntity.FunctionType.analytic);
            functionEntity.setFunctionName(compositeDto.getCustomName() != null ?
                    compositeDto.getCustomName() :
                    "Сложная функция " + System.currentTimeMillis());
            functionEntity.setFunctionExpression(
                    compositeDto.getOuterFunctionName() + " ∘ " + compositeDto.getBaseFunctionName()
            );

            // Сохраняем в базу данных
            FunctionEntity savedEntity = functionRepository.save(functionEntity);

            // Создаем DTO для ответа
            FunctionDto resultDto = new FunctionDto();
            resultDto.setId(savedEntity.getId());
            resultDto.setUserId(user.getId());
            resultDto.setTypeFunction("analytic");
            resultDto.setFunctionName(functionEntity.getFunctionName());
            resultDto.setFunctionExpression(functionEntity.getFunctionExpression());

            log.info("Сложная функция создана успешно с ID: {}", resultDto.getId());
            return ResponseEntity.status(HttpStatus.CREATED).body(resultDto);
        } catch (Exception e) {
            log.error("Ошибка при создании сложной функции: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new FunctionDto(null, null, null, "Ошибка: " + e.getMessage(), null, null, null));
        }
    }

    @GetMapping("/composite-functions")
    public ResponseEntity<List<String>> getAvailableCompositeFunctions() {
        log.info("Запрос на получение доступных сложных функций");

        try {
            List<String> compositeFunctionNames = MathFunctionRegistry.getAvailableCompositeFunctionNames();
            return ResponseEntity.ok(compositeFunctionNames);
        } catch (Exception e) {
            log.error("Ошибка при получении списка сложных функций: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
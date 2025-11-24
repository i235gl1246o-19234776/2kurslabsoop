package core.controller;

import core.entity.FunctionEntity;
import core.entity.UserEntity;
import core.repository.FunctionRepository;
import core.repository.UserRepository;
import core.dto.FunctionDto;
import core.dto.CompositeFunctionDto;
import functions.CompositeFunction;
import functions.MathFunction;
import core.utils.MathFunctionRegistry;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/functions")
public class CompositeFunctionController {

    @Autowired
    private FunctionRepository functionRepository;

    @Autowired
    private UserRepository userRepository;

    // Карта для хранения созданных сложных функций
    private static final Map<String, Class<? extends MathFunction>> COMPOSITE_FUNCTIONS = new HashMap<>();

    @PostMapping("/composite")
    public ResponseEntity<FunctionDto> createCompositeFunction(
            @RequestBody CompositeFunctionDto compositeDto) {
        log.info("Запрос на создание сложной функции: {}", compositeDto);

        try {
            // Проверяем существование пользователя
            UserEntity user = userRepository.findById(compositeDto.getUserId())
                    .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

            // Получаем базовые функции
            MathFunction baseFunction = MathFunctionRegistry.getFunctionByName(compositeDto.getBaseFunctionName());
            MathFunction outerFunction = MathFunctionRegistry.getFunctionByName(compositeDto.getOuterFunctionName());

            // Создаем сложную функцию
            CompositeFunction compositeFunction = new CompositeFunction(outerFunction, baseFunction);

            // Динамически создаем класс для сложной функции
            String compositeClassName = "Composite_" +
                    compositeDto.getBaseFunctionName().replaceAll("\\s+", "_") + "_" +
                    compositeDto.getOuterFunctionName().replaceAll("\\s+", "_");

            // Сохраняем сложную функцию в реестре
            // (В реальном приложении здесь был бы код для динамической генерации класса)
            // Для демонстрации просто регистрируем функцию с именем

            // Создаем сущность функции
            FunctionEntity functionEntity = new FunctionEntity();
            functionEntity.setUser(user);
            functionEntity.setTypeFunction(FunctionEntity.FunctionType.analytic);
            functionEntity.setFunctionName(compositeDto.getCustomName() != null ?
                    compositeDto.getCustomName() : compositeClassName);
            functionEntity.setFunctionExpression(
                    compositeDto.getOuterFunctionName() + "(" + compositeDto.getBaseFunctionName() + "(x))"
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

            // Возвращаем результат
            return ResponseEntity.ok(resultDto);
        } catch (Exception e) {
            log.error("Ошибка при создании сложной функции: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().build();
        }
    }
}
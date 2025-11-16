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
@RequestMapping("/api/tabulated-points")
public class TabulatedFunctionController {

    @Autowired
    private TabulatedFunctionRepository tabulatedFunctionRepository;

    @Autowired
    private FunctionRepository functionRepository;

    @GetMapping("/function/{functionId}")
    public ResponseEntity<List<TabulatedFunctionDto>> getAllPointsByFunctionId(@PathVariable Long functionId) {
        log.info("Запрос на получение всех точек для функции ID: {}", functionId);
        List<TabulatedFunctionEntity> points = tabulatedFunctionRepository.findByFunction_Id(functionId);
        List<TabulatedFunctionDto> pointDtos = points.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
        log.info("Возвращено {} точек для функции ID: {}", pointDtos.size(), functionId);
        return ResponseEntity.ok(pointDtos);
    }

    @GetMapping("/function/{functionId}/x/{xValue}")
    public ResponseEntity<TabulatedFunctionDto> getPointByFunctionIdAndX(@PathVariable Long functionId, @PathVariable Double xValue) {
        log.info("Запрос на поиск точки для функции ID: {} по X: {}", functionId, xValue);
        List<TabulatedFunctionEntity> points = tabulatedFunctionRepository.findByFunction_Id(functionId);
        Optional<TabulatedFunctionEntity> pointOpt = points.stream()
                .filter(p -> p.getXVal().equals(xValue))
                .findFirst();

        if (pointOpt.isPresent()) {
            TabulatedFunctionDto pointDto = convertToDto(pointOpt.get());
            log.info("Точка найдена для функции ID: {} и X: {}", functionId, xValue);
            return ResponseEntity.ok(pointDto);
        } else {
            log.warn("Точка с X={} не найдена для функции ID: {}", xValue, functionId);
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/function/{functionId}/range")
    public ResponseEntity<List<TabulatedFunctionDto>> getPointsInRange(@PathVariable Long functionId, @RequestParam Double fromX, @RequestParam Double toX) {
        log.info("Запрос на получение точек для функции ID: {} в диапазоне от {} до {}", functionId, fromX, toX);
        List<TabulatedFunctionEntity> points = tabulatedFunctionRepository.findByFunction_Id(functionId);
        List<TabulatedFunctionDto> rangeDtos = points.stream()
                .filter(p -> p.getXVal() >= fromX && p.getXVal() <= toX)
                .map(this::convertToDto)
                .collect(Collectors.toList());
        log.info("Найдено {} точек в диапазоне [{}, {}] для функции ID: {}", rangeDtos.size(), fromX, toX, functionId);
        return ResponseEntity.ok(rangeDtos);
    }

    @PostMapping
    public ResponseEntity<TabulatedFunctionDto> createPoint(@RequestBody TabulatedFunctionDto pointDto) {
        log.info("Запрос на создание точки для функции ID: {}", pointDto.getFunctionId());

        if (pointDto.getXVal() == null || pointDto.getYVal() == null) {
            log.error("xVal и/или yVal не могут быть null при создании точки");
            return ResponseEntity.badRequest().build();
        }

        FunctionEntity function = functionRepository.findById(pointDto.getFunctionId())
                .orElseThrow(() -> new RuntimeException("Функция не найдена при создании точки"));

        TabulatedFunctionEntity pointEntity = new TabulatedFunctionEntity();
        pointEntity.setFunction(function);
        pointEntity.setXVal(pointDto.getXVal());
        pointEntity.setYVal(pointDto.getYVal());

        TabulatedFunctionEntity savedEntity = tabulatedFunctionRepository.save(pointEntity);
        TabulatedFunctionDto savedDto = convertToDto(savedEntity);
        log.info("Точка создана с ID: {}", savedDto.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(savedDto);
    }

    @PutMapping("/{id}")
    public ResponseEntity<TabulatedFunctionDto> updatePoint(@PathVariable Long id, @RequestBody TabulatedFunctionDto pointDtoDetails) {
        log.info("Запрос на обновление точки с ID: {}", id);
        Optional<TabulatedFunctionEntity> pointOpt = tabulatedFunctionRepository.findById(id);
        if (pointOpt.isPresent()) {
            TabulatedFunctionEntity point = pointOpt.get();
            point.setXVal(pointDtoDetails.getXVal());
            point.setYVal(pointDtoDetails.getYVal());
            TabulatedFunctionEntity updatedEntity = tabulatedFunctionRepository.save(point);
            TabulatedFunctionDto updatedDto = convertToDto(updatedEntity);
            log.info("Точка с ID {} обновлена", id);
            return ResponseEntity.ok(updatedDto);
        } else {
            log.warn("Попытка обновить несуществующую точку с ID: {}", id);
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePoint(@PathVariable Long id) {
        log.info("Запрос на удаление точки с ID: {}", id);
        if (tabulatedFunctionRepository.existsById(id)) {
            tabulatedFunctionRepository.deleteById(id);
            log.info("Точка с ID {} удалена", id);
            return ResponseEntity.noContent().build();
        } else {
            log.warn("Попытка удалить несуществующую точку с ID: {}", id);
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/function/{functionId}")
    public ResponseEntity<Void> deleteAllPointsByFunctionId(@PathVariable Long functionId) {
        log.info("Запрос на удаление всех точек для функции ID: {}", functionId);
        List<TabulatedFunctionEntity> points = tabulatedFunctionRepository.findByFunction_Id(functionId);
        tabulatedFunctionRepository.deleteAll(points);
        log.info("Удалено {} точек для функции ID: {}", points.size(), functionId);
        return ResponseEntity.noContent().build();
    }

    private TabulatedFunctionDto convertToDto(TabulatedFunctionEntity pointEntity) {
        Long functionId = pointEntity.getFunction() != null ? pointEntity.getFunction().getId() : null;
        return new TabulatedFunctionDto(
                pointEntity.getId(),
                functionId,
                pointEntity.getXVal(),
                pointEntity.getYVal()
        );
    }
}
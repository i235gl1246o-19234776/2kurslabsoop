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

@Slf4j //
@RestController
@RequestMapping("/api/tabulated-points")
public class TabulatedFunctionController {

    @Autowired
    private TabulatedFunctionRepository tabulatedFunctionRepository;

    @Autowired
    private FunctionRepository functionRepository;

    @GetMapping("/function/{functionId}")
    public ResponseEntity<List<TabulatedFunctionEntity>> getAllPointsByFunctionId(@PathVariable Long functionId) {
        log.info("Запрос на получение всех точек для функции ID: {}", functionId);
        List<TabulatedFunctionEntity> points = tabulatedFunctionRepository.findByFunction_Id(functionId);
        return ResponseEntity.ok(points);
    }

    @PostMapping
    public ResponseEntity<TabulatedFunctionEntity> createPoint(@RequestBody TabulatedFunctionEntity point) {
        log.info("Запрос на создание точки для функции ID: {}", point.getFunction().getId());
        FunctionEntity function = functionRepository.findById(point.getFunction().getId())
                .orElseThrow(() -> new RuntimeException("Функция не найдена"));
        point.setFunction(function);
        point.setId(null);
        TabulatedFunctionEntity savedPoint = tabulatedFunctionRepository.save(point);
        log.info("Точка создана с ID: {}", savedPoint.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(savedPoint);
    }

    @PutMapping("/{id}")
    public ResponseEntity<TabulatedFunctionEntity> updatePoint(@PathVariable Long id, @RequestBody TabulatedFunctionEntity pointDetails) {
        log.info("Запрос на обновление точки с ID: {}", id);
        Optional<TabulatedFunctionEntity> existingPointOpt = tabulatedFunctionRepository.findById(id);
        if (existingPointOpt.isPresent()) {
            TabulatedFunctionEntity existingPoint = existingPointOpt.get();
            existingPoint.setXVal(pointDetails.getXVal());
            existingPoint.setYVal(pointDetails.getYVal());
            if (!existingPoint.getFunction().getId().equals(pointDetails.getFunction().getId())) {
                FunctionEntity newFunction = functionRepository.findById(pointDetails.getFunction().getId())
                        .orElseThrow(() -> new RuntimeException("Функция не найдена"));
                existingPoint.setFunction(newFunction);
            }
            TabulatedFunctionEntity updatedPoint = tabulatedFunctionRepository.save(existingPoint);
            log.info("Точка с ID {} обновлена", id);
            return ResponseEntity.ok(updatedPoint);
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
        log.info("Все {} точки для функции ID {} удалены", points.size(), functionId);
        return ResponseEntity.noContent().build();
    }
}
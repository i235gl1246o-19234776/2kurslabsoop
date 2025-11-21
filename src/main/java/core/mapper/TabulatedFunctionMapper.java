
package core.mapper;

import core.dto.TabulatedFunctionDto;
import core.entity.FunctionEntity;
import core.entity.TabulatedFunctionEntity;
import core.repository.UserRepository;
import core.utils.MathFunctionRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class TabulatedFunctionMapper {

    private final UserRepository userRepository;

    public TabulatedFunctionEntity toEntity(TabulatedFunctionDto dto) {
        TabulatedFunctionEntity entity = new TabulatedFunctionEntity();
        entity.setXVal(dto.getXVal());
        entity.setYVal(dto.getYVal());

        if (dto.getFunctionId() != null) {
            FunctionEntity functionEntity = new FunctionEntity();
            functionEntity.setId(dto.getFunctionId());
            entity.setFunction(functionEntity);
        }
        return entity;
    }

    public TabulatedFunctionDto toDto(TabulatedFunctionEntity entity) {
        return new TabulatedFunctionDto(
                entity.getId(),
                entity.getFunction() != null ? entity.getFunction().getId() : null,
                entity.getXVal(),
                entity.getYVal()
        );
    }

    public List<String> getAvailableMathFunctionNames() {
        return MathFunctionRegistry.getAvailableFunctionNames();
    }

    public void updateEntityFromDto(TabulatedFunctionEntity entity, TabulatedFunctionDto dto) {
        entity.setXVal(dto.getXVal());
        entity.setYVal(dto.getYVal());
    }

    public List<TabulatedFunctionEntity> toEntityList(List<TabulatedFunctionDto> dtoList) {
        return dtoList.stream()
                .map(this::toEntity)
                .collect(Collectors.toList());
    }

    public List<TabulatedFunctionDto> toDtoList(List<TabulatedFunctionEntity> entityList) {
        return entityList.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }
}
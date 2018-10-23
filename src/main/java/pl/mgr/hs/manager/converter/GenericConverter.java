package pl.mgr.hs.manager.converter;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by dominik on 20.10.18.
 */
public interface GenericConverter<D, E> {

    E createEntity(D dto);

    D createDto(E entity);

    E updateEntity(E entity, D dto);

    default List<D> createDtos(List<E> entities) {
        return entities.stream()
                .map(this::createDto)
                .collect(Collectors.toList());
    }
}

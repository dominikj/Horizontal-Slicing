package pl.mgr.hs.manager.converter;

import java.util.List;
import java.util.stream.Collectors;

/** Created by dominik on 20.10.18. */
public interface GenericConverter<D, E> {

  default E createEntity(D dto) {
    // NOOP
    return null;
  }

  default D createDto(E entity) {
    // NOOP
    return null;
  }

  default E updateEntity(E entity, D dto) {
    // NOOP
    return null;
  }

  default List<D> createDtos(List<E> entities) {
    return entities.stream().map(this::createDto).collect(Collectors.toList());
  }
}

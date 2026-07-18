package tech.sangdang.lmscoreapi.common.exception;

import org.springframework.http.HttpStatus;

public class ObjectNotFoundException extends BusinessException {

  private static final HttpStatus STATUS = HttpStatus.NOT_FOUND;

  public ObjectNotFoundException(String objectType, Object identifier) {
    super(toCode(objectType), toMessage(objectType, identifier), STATUS);
  }

  public ObjectNotFoundException(Class<?> objectType, Object identifier) {
    this(objectType.getSimpleName(), identifier);
  }

  public static ObjectNotFoundException of(Class<?> objectType, Object identifier) {
    return new ObjectNotFoundException(objectType, identifier);
  }

  private static String toCode(String objectType) {
    return camelToSnakeUpper(objectType) + "_NOT_FOUND";
  }

  private static String toMessage(String objectType, Object identifier) {
    return objectType + " not found: " + identifier;
  }

  private static String camelToSnakeUpper(String value) {
    return value.replaceAll("([a-z])([A-Z])", "$1_$2").toUpperCase();
  }
}

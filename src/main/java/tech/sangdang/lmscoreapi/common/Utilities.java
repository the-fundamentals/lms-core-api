package tech.sangdang.lmscoreapi.common;

import lombok.experimental.UtilityClass;
import tech.sangdang.lmscoreapi.common.exception.GenericBadRequestException;

import java.time.LocalTime;
import java.time.format.DateTimeParseException;

@UtilityClass
public class Utilities {
  public static LocalTime parseTimeOrError(String value) {
    try {
      return LocalTime.parse(value);
    } catch (DateTimeParseException | NullPointerException e) {
      throw GenericBadRequestException.of(
          "INVALID_TIME", "startTime and endTime must be ISO-8601 times (HH:mm[:ss])");
    }
  }
}

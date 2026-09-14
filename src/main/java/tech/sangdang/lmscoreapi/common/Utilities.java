package tech.sangdang.lmscoreapi.common;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import lombok.experimental.UtilityClass;
import org.jspecify.annotations.Nullable;
import tech.sangdang.lmscoreapi.common.exception.GenericBadRequestException;

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

  public static void requireInclusiveDateRange(
      @Nullable LocalDate startDate, @Nullable LocalDate endDate) {
    if (startDate != null && endDate != null && startDate.isAfter(endDate)) {
      throw GenericBadRequestException.of(
          "INVALID_DATE_RANGE", "startDate must not be after endDate");
    }
  }
}

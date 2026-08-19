package tech.sangdang.lmscoreapi.common.utility;

import lombok.experimental.UtilityClass;
import net.fortuna.ical4j.model.property.RRule;
import net.fortuna.ical4j.validate.ValidationException;
import org.jspecify.annotations.Nullable;

import java.time.temporal.Temporal;

@UtilityClass
public class RRuleValidation {
  public static boolean validateRecurrenceRule(@Nullable String rule) {
    if (rule == null) {
      return false;
    }

    try {
      RRule<Temporal> rrule = new RRule<>(rule);
      rrule.validate();
      return true;
    } catch (IllegalArgumentException | ValidationException e) {
      return false;
    }
  }
}

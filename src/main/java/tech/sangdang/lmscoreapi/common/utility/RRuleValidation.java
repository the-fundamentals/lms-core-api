package tech.sangdang.lmscoreapi.common.utility;

import java.time.temporal.Temporal;
import lombok.experimental.UtilityClass;
import net.fortuna.ical4j.model.property.RRule;
import net.fortuna.ical4j.validate.ValidationException;
import org.jspecify.annotations.Nullable;

@UtilityClass
public class RRuleValidation {
  public static RRule<Temporal> validateRecurrenceRule(@Nullable String rule) {
    if (rule == null) {
      return null;
    }

    try {
      RRule<Temporal> rrule = new RRule<>(rule);
      rrule.validate();
      return rrule;
    } catch (IllegalArgumentException | ValidationException e) {
      return null;
    }
  }
}

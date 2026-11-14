package tech.sangdang.lmscoreapi.modules.management.dom;

/** Why a classroom session was cancelled. Null while status is not CANCELLED. */
public enum ClassroomSessionCancelCause {
  /** Cancelled via the session cancel endpoint. */
  MANUAL,
  /** Cancelled because the generating schedule recurrence was truncated. */
  REMOVED_FROM_SERIES
}

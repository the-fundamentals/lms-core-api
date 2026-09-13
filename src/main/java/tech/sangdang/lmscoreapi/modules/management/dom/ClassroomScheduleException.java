package tech.sangdang.lmscoreapi.modules.management.dom;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.UUID;

@Getter
@Setter
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
@Table(ClassroomScheduleException.TABLE_NAME)
public class ClassroomScheduleException {
    public static final String  TABLE_NAME = "classroom_schedule_exception";

    private @Id UUID id;
    private @CreatedDate LocalDateTime createdDate;
    private @LastModifiedDate LocalDateTime lastModifiedDate;
    private UUID classroomId;
    private LocalDate date;
    private LocalTime startTime;
    private LocalTime endTime;
    private ClassroomScheduleExceptionType type;
 }

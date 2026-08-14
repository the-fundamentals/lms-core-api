package tech.sangdang.lmscoreapi.modules.utility.app.dto;

import java.util.UUID;
import org.jspecify.annotations.NonNull;

public record ConfirmUploadPrivateCommand(@NonNull String objectKey, UUID accountProfileId) {}

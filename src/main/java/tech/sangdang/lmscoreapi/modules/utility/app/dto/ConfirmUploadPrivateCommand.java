package tech.sangdang.lmscoreapi.modules.utility.app.dto;

import org.jspecify.annotations.NonNull;

import java.util.UUID;

public record ConfirmUploadPrivateCommand(
        @NonNull String objectKey, UUID accountProfileId) {}

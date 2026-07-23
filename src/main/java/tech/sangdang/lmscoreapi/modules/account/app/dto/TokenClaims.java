package tech.sangdang.lmscoreapi.modules.account.app.dto;

import java.time.Instant;
import java.util.List;

public record TokenClaims(String sub, String email, List<String> roles, Instant authTime) {}

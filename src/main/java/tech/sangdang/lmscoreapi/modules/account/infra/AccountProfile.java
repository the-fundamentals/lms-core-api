package tech.sangdang.lmscoreapi.modules.account.infra;

/**
 * Snapshot of account identity data. Source of truth is an external auth/identity service; this API
 * caches and denormalizes selected fields onto classroom members.
 */
public record AccountProfile(String accountId, String email, String name) {}

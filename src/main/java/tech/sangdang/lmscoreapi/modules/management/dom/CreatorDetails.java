package tech.sangdang.lmscoreapi.modules.management.dom;

import tech.sangdang.lmscoreapi.modules.account.dom.AccountProfile;

/**
 * Denormalized creator snapshot on ADHOC resources (sessions, invoices) — accounts are a separate
 * aggregate; copy at create time (same idea as {@link ClassroomMember} name/email/avatar).
 */
public record CreatorDetails(String name, String email, String avatar) {

  public static CreatorDetails from(AccountProfile profile) {
    String first = profile.getFirstName() == null ? "" : profile.getFirstName().trim();
    String last = profile.getLastName() == null ? "" : profile.getLastName().trim();
    String name = (first + " " + last).trim();
    return new CreatorDetails(
        name.isEmpty() ? profile.getEmail() : name, profile.getEmail(), profile.getAvatarKey());
  }
}

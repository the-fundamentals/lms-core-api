package tech.sangdang.lmscoreapi.modules.utility.support;

import java.time.LocalDateTime;
import java.util.UUID;
import tech.sangdang.lmscoreapi.modules.utility.dom.StorageGrants;

public final class StorageFixtures {

  public static final String COGNITO_SUB = "a1b2c3d4-e5f6-7890-abcd-ef1234567890";
  public static final String LANDING_ZONE_BUCKET = "lms-local-landing-zone";
  public static final String PUBLIC_STORE_BUCKET = "lms-local-public";
  public static final String UPLOAD_URL = "https://example.local/upload";
  public static final String DOWNLOAD_URL = "https://example.local/download";
  public static final String PUBLIC_KEY_PREFIX = "public/";
  public static final String PRIVATE_KEY_PREFIX = "private/";
  public static final String PUBLIC_OBJECT_KEY = "public/540ba3e4-8026-45e9-9834-54aa0eb1a17a";
  public static final String PRIVATE_OBJECT_KEY = "private/540ba3e4-8026-45e9-9834-54aa0eb1a17a";
  public static final String BAD_PUBLIC_OBJECT_KEY = "private/not-a-public-key";
  public static final String BAD_PRIVATE_OBJECT_KEY = "public/not-a-private-key";
  public static final UUID OWNER_ID = UUID.fromString("3fa85f64-5717-4562-b3fc-2c963f66afa6");
  public static final Long GRANT_ID = 1L;
  public static final LocalDateTime CREATED_AT = LocalDateTime.of(2026, 7, 14, 0, 0, 0);
  public static final LocalDateTime MODIFIED_AT = LocalDateTime.of(2026, 7, 14, 0, 0, 0);

  private StorageFixtures() {}

  public static StorageGrants storageGrant(String objectKey) {
    return new StorageGrants()
        .setId(GRANT_ID)
        .setOwnerId(OWNER_ID)
        .setObjectKey(objectKey)
        .setObjectBucket(PUBLIC_STORE_BUCKET)
        .setCreatedDate(CREATED_AT)
        .setLastModifiedDate(MODIFIED_AT);
  }
}

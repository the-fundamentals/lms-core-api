package tech.sangdang.lmscoreapi.common.persistence;

import java.sql.SQLException;
import org.postgresql.util.PGobject;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;
import org.springframework.data.convert.WritingConverter;
import tech.sangdang.lmscoreapi.modules.management.dom.CreatorDetails;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.json.JsonMapper;

/** JDBC mapping for ADHOC {@code created_by_details} JSON snapshots. */
public final class CreatorDetailsConverters {

  private CreatorDetailsConverters() {}

  @WritingConverter
  public static final class ToJsonb implements Converter<CreatorDetails, PGobject> {
    private final JsonMapper jsonMapper;

    public ToJsonb(JsonMapper jsonMapper) {
      this.jsonMapper = jsonMapper;
    }

    @Override
    public PGobject convert(CreatorDetails source) {
      try {
        PGobject pg = new PGobject();
        pg.setType("jsonb");
        pg.setValue(jsonMapper.writeValueAsString(source));
        return pg;
      } catch (SQLException | JacksonException e) {
        throw new IllegalStateException("Failed to write created_by_details JSON", e);
      }
    }
  }

  @ReadingConverter
  public static final class FromString implements Converter<String, CreatorDetails> {
    private final JsonMapper jsonMapper;

    public FromString(JsonMapper jsonMapper) {
      this.jsonMapper = jsonMapper;
    }

    @Override
    public CreatorDetails convert(String source) {
      try {
        return jsonMapper.readValue(source, CreatorDetails.class);
      } catch (JacksonException e) {
        throw new IllegalStateException("Failed to read created_by_details JSON", e);
      }
    }
  }

  @ReadingConverter
  public static final class FromPGobject implements Converter<PGobject, CreatorDetails> {
    private final FromString delegate;

    public FromPGobject(JsonMapper jsonMapper) {
      this.delegate = new FromString(jsonMapper);
    }

    @Override
    public CreatorDetails convert(PGobject source) {
      return delegate.convert(source.getValue());
    }
  }
}

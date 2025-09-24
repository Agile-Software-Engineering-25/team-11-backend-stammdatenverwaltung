package com.ase.stammdatenverwaltung.services;

import com.ase.stammdatenverwaltung.dto.CreateRoleTable;
import com.ase.stammdatenverwaltung.dto.CreateRoleTable.ColumnDef;
import com.ase.stammdatenverwaltung.dto.CreateRoleTable.DataType;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class RoleTableService {

  private final JdbcTemplate jdbc;

  /** Legt eine neue Tabelle in Postgres an (optional Spalten von Basistabelle übernehmen). */
  @Transactional
  public void createRoleTable(CreateRoleTable req) {
    final String table = sanitize(req.getRoleName());
    log.debug("Create table requested: {}", table);

    ensureTableNotExists(table);

    final List<String> columnDefs = new ArrayList<String>();
    if (req.getInheritsFrom() != null && !req.getInheritsFrom().trim().isEmpty()) {
      final String base = sanitize(req.getInheritsFrom());
      ensureTableExists(base);
      columnDefs.addAll(readColumnDefs(base));
    }

    // zusätzliche Spalten aus Request ergänzen
    if (req.getColumns() != null) {
      for (int i = 0; i < req.getColumns().size(); i++) {
        ColumnDef c = req.getColumns().get(i);
        final String colName = sanitize(c.getName());

        // Kollision vermeiden
        for (int j = 0; j < columnDefs.size(); j++) {
          // simple Prüfung: alles vor dem ersten Leerzeichen ist der Spaltenname
          String existingName = columnDefs.get(j);
          int space = existingName.indexOf(' ');
          if (space > 0) {
            existingName = existingName.substring(0, space);
          }
          if (existingName.equals(colName)) {
            throw new IllegalArgumentException("Spalte existiert bereits: " + colName);
          }
        }

        // Postgres-SQL-Typ inline bestimmen (kein separater mapToPgType)
        String sqlType;
        DataType t = c.getType();
        if (t == DataType.STRING) {
          Integer len = c.getLength();
          sqlType = "VARCHAR(" + (len != null ? len.intValue() : 255) + ")";
        } else if (t == DataType.INT) {
          sqlType = "INTEGER";
        } else if (t == DataType.BIGINT) {
          sqlType = "BIGINT";
        } else if (t == DataType.BOOL) {
          sqlType = "BOOLEAN";
        } else if (t == DataType.DATE) {
          sqlType = "DATE";
        } else if (t == DataType.TIMESTAMP) {
          sqlType = "TIMESTAMP";
        } else if (t == DataType.DECIMAL) {
          Integer p = (c.getPrecision() != null ? c.getPrecision() : Integer.valueOf(10));
          Integer s = (c.getScale() != null ? c.getScale() : Integer.valueOf(2));
          sqlType = "NUMERIC(" + p + "," + s + ")";
        } else {
          throw new IllegalArgumentException("Unbekannter Datentyp: " + t);
        }

        String notNull = c.isNullable() ? "" : " NOT NULL";
        columnDefs.add(colName + " " + sqlType + notNull);
      }
    }

    if (columnDefs.isEmpty()) {
      // minimal – CREATE TABLE darf nicht leer sein
      columnDefs.add("id BIGINT");
    }

    // 3) DDL zusammenbauen & ausführen
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < columnDefs.size(); i++) {
      if (i > 0) sb.append(", ");
      sb.append(columnDefs.get(i));
    }
    String ddl = "CREATE TABLE " + table + " (" + sb + ")";
    log.info("Executing DDL: {}", ddl);

    try {
      jdbc.execute(ddl);
    } catch (DataAccessException dae) {
      throw new IllegalArgumentException("DDL fehlgeschlagen: " + dae.getMessage(), dae);
    }
  }

  // ---------- Helpers (Postgres only) ----------

  /** prüft, dass Tabelle in public NICHT existiert. */
  private void ensureTableNotExists(String table) {
    Integer cnt =
        jdbc.queryForObject(
            "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema='public' AND table_name=?",
            Integer.class,
            table);
    if (cnt != null && cnt.intValue() > 0) {
      throw new IllegalArgumentException("Tabelle existiert bereits: " + table);
    }
  }

  /** prüft, dass Basistabelle existiert. */
  private void ensureTableExists(String table) {
    Integer cnt =
        jdbc.queryForObject(
            "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema='public' AND table_name=?",
            Integer.class,
            table);
    if (cnt == null || cnt.intValue() == 0) {
      throw new IllegalArgumentException("Basistabelle nicht gefunden: " + table);
    }
  }

  /** liest bestehende Spalten (Name + typ + NOT NULL) aus Postgres (public-Schema). */
  private List<String> readColumnDefs(String table) {
    final String sql =
        "SELECT column_name, data_type" +
            "  FROM information_schema.columns " +
            " WHERE table_schema='public' AND table_name=? " +
            " ORDER BY ordinal_position";

    return jdbc.query(
        sql,
        new RowMapper<String>() {
          @Override
          public String mapRow(ResultSet rs, int rowNum) throws SQLException {
            String name = rs.getString("column_name");
            if (name != null) name = name.toLowerCase(Locale.ROOT);

            String dataType = rs.getString("data_type");
            if (dataType == null) dataType = "";

            return name + " " + dataType;
          }
        },
        table);
  }

  /** einfache Identifier-Validierung zur SQL-Injection-Prävention */
  private String sanitize(String ident) {
    if (ident == null || !ident.matches("^[a-z][a-z0-9_]*$")) {
      throw new IllegalArgumentException("Ungültiger Bezeichner: " + ident);
    }
    return ident;
  }
}



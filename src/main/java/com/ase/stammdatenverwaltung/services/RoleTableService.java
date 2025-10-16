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

/**
 * Service class for managing role tables. Provides logic for correct create query with proper
 * columns and names.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class RoleTableService {

  private final JdbcTemplate jdbc;

  private final int maxLength = 255;

  /** Creates new table for roles (takes existing columns from given baseclass) */
  @Transactional
  public void createRoleTable(CreateRoleTable tabelle) {
    final String table = sanitize(tabelle.getRoleName());
    log.debug("Create table requested: {}", table);

    ensureTableNotExists(table);

    final List<String> columnDefs = new ArrayList<String>();
    if (tabelle.getInheritsFrom() != null && !tabelle.getInheritsFrom().trim().isEmpty()) {
      final String basistabelle = sanitize(tabelle.getInheritsFrom());
      ensureTableExists(basistabelle);
      columnDefs.addAll(readColumnDefs(basistabelle));
    }

    // zusätzliche Spalten aus Request ergänzen
    if (tabelle.getColumns() != null) {
      for (int i = 0; i < tabelle.getColumns().size(); i++) {
        ColumnDef spalte = tabelle.getColumns().get(i);
        final String colName = sanitize(spalte.getName());

        // einfachen Datentyp auf Postgres abbilden
        DataType type = spalte.getType();
        final String sqlType;

        switch (type) {
          case STRING:
            {
              Integer length = spalte.getLength();
              sqlType = "VARCHAR(" + (length != null ? length : maxLength) + ")";
              break;
            }
          case INT: sqlType = "INTEGER"; break;
          case BOOL: sqlType = "BOOLEAN"; break;
          case DECIMAL: sqlType = "NUMERIC(10,2)"; break;
          default:
            throw new IllegalArgumentException("Unbekannter Datentyp: " + type);
        }
        columnDefs.add(colName + " " + sqlType);
      }
    }

    // Damit CREATE TABLE nicht leer ist
    if (columnDefs.isEmpty()) {
      columnDefs.add("id BIGINT");
    }

    // DDL zusammenbauen & ausführen
    String ddl = "CREATE TABLE " + table + " (" + String.join(", ", columnDefs) + ")";
    log.info("Executing DDL: {}", ddl);

    try {
      jdbc.execute(ddl);
    } catch (DataAccessException dae) {
      throw new IllegalArgumentException("DDL fehlgeschlagen: " + dae.getMessage(), dae);
    }
  }

  // prüft dass Tabelle nicht in Public existiert
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

  // prüft, dass Basistabelle existiert.
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

  // liest bestehende Spalten (Name + typ + NOT NULL)
  private List<String> readColumnDefs(String table) {
    final String sql =
        "SELECT column_name, data_type"
            + "  FROM information_schema.columns "
            + " WHERE table_schema='public' AND table_name=? "
            + " ORDER BY ordinal_position";

    return jdbc.query(
        sql,
        new RowMapper<String>() {
          @Override
          public String mapRow(ResultSet rs, int rowNum) throws SQLException {
            String name = rs.getString("column_name");
            if (name != null) {
              name = name.toLowerCase(Locale.ROOT);
            }

            String dataType = rs.getString("data_type");
            if (dataType == null) {
              dataType = "";
            }

            return name + " " + dataType;
          }
        },
        table);
  }

  // einfache Identifier-Validierung zur SQL-Injection-Prävention
  private String sanitize(String ident) {
    if (ident == null || !ident.matches("^[a-z][a-z0-9_]*$")) {
      throw new IllegalArgumentException("Ungültiger Bezeichner: " + ident);
    }
    return ident;
  }
}

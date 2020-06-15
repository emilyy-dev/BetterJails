package me.fefo.betterjails.utils;

import com.mysql.jdbc.Driver;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;
import java.util.UUID;

public final class SQLConnector {
  private final String user, password;
  private final String connectionURL;
  private Connection connection;

  public SQLConnector(@NotNull String host,
                      @NotNull String databaseName,
                      @NotNull String user,
                      @NotNull String password) {
    if (!host.contains(":")) {
      host += ":3306";
    }

    this.user = user;
    this.password = password;

    connectionURL = "jdbc:mysql://" + host +
                    "/" + databaseName +
                    "?connectTimeout=30000" +
                    "&socketTimeout=30000";
  }

  public void closeSession() {
    try {
      if (!connection.isClosed()) {
        connection.close();
      }
    } catch (SQLException e) {
      e.printStackTrace();
    }

    connection = null;
  }

  public boolean openSession() {
    try {
      final Driver driver = new Driver();
      final Properties props = new Properties();
      props.put("user", user);
      props.put("password", password);
      connection = driver.connect(connectionURL, props);
      return true;
    } catch (Exception e) {
      e.printStackTrace();
      connection = null;
      return false;
    }
  }

  public ResultSet SQLStatement(@NotNull String statement,
                                @Nullable Object... conditionalValues) {
    try {
      if (connection == null) {
        return null;
      }

      final PreparedStatement st = connection.prepareStatement(statement,
                                                               ResultSet.TYPE_SCROLL_INSENSITIVE,
                                                               ResultSet.CONCUR_READ_ONLY);

      if (conditionalValues != null) {
        for (int i = 0; i < conditionalValues.length; ) {
          final Object val = conditionalValues[i++];

          if (val instanceof UUID) {
            st.setString(i, val.toString());
          } else if (val instanceof String) {
            st.setString(i, (String)val);
          } else if (val instanceof Integer) {
            st.setInt(i, (int)val);
          } else if (val instanceof Long) {
            st.setLong(i, (long)val);
          } else if (val instanceof Float) {
            st.setFloat(i, (float)val);
          } else if (val instanceof Double) {
            st.setDouble(i, (double)val);
          } else if (val instanceof java.util.Date) {
            st.setDate(i, new java.sql.Date(((java.util.Date)val).getTime()));
          } else if (val instanceof Character) {
            st.setString(i, val.toString());
          } else if (val instanceof Short) {
            st.setShort(i, ((short)val));
          } else if (val instanceof Boolean) {
            st.setBoolean(i, ((boolean)val));
          } else if (val instanceof Byte) {
            st.setByte(i, ((byte)val));
          }
        }
      }

      return st.executeQuery();
    } catch (Exception e) {
      e.printStackTrace();
      return null;
    }
  }

  public boolean SQLStatementNoReturn(@NotNull String statement,
                                      @Nullable Object... conditionalValues) {
    try {
      if (connection == null) {
        return false;
      }

      final PreparedStatement st = connection.prepareStatement(statement,
                                                               ResultSet.TYPE_SCROLL_INSENSITIVE,
                                                               ResultSet.CONCUR_READ_ONLY);

      if (conditionalValues != null) {
        for (int i = 0; i < conditionalValues.length; ) {
          final Object val = conditionalValues[i++];

          if (val instanceof UUID) {
            st.setString(i, val.toString());
          } else if (val instanceof String) {
            st.setString(i, (String)val);
          } else if (val instanceof Integer) {
            st.setInt(i, (int)val);
          } else if (val instanceof Long) {
            st.setLong(i, (long)val);
          } else if (val instanceof Float) {
            st.setFloat(i, (float)val);
          } else if (val instanceof Double) {
            st.setDouble(i, (double)val);
          } else if (val instanceof java.util.Date) {
            st.setDate(i, new java.sql.Date(((java.util.Date)val).getTime()));
          } else if (val instanceof Character) {
            st.setString(i, val.toString());
          } else if (val instanceof Short) {
            st.setShort(i, ((short)val));
          } else if (val instanceof Boolean) {
            st.setBoolean(i, ((boolean)val));
          } else if (val instanceof Byte) {
            st.setByte(i, ((byte)val));
          }
        }
      }
      st.execute();
      return true;
    } catch (Exception e) {
      e.printStackTrace();
      return false;
    }
  }
}

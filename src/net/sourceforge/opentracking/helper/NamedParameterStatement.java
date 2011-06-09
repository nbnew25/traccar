package net.sourceforge.opentracking.helper;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.LinkedList;
import java.util.Date;

public class NamedParameterStatement {

    /**
     * Native statement
     */
    private final PreparedStatement statement;

    /**
     * Index mapping
     */
    private final Map indexMap;

    /**
     * Initialize statement
     */
    public NamedParameterStatement(Connection connection, String query)
            throws SQLException {

        indexMap = new HashMap();
        String parsedQuery = parse(query, indexMap);
        statement = connection.prepareStatement(parsedQuery);
    }

    /**
     * Parse query
     */
    static final String parse(String query, Map paramMap) {

        int length = query.length();
        StringBuffer parsedQuery = new StringBuffer(length);
        boolean inSingleQuote = false;
        boolean inDoubleQuote = false;
        int index = 1;

        for(int i = 0; i < length; i++) {

            char c = query.charAt(i);

            // String end
            if (inSingleQuote) {
                if (c == '\'') inSingleQuote = false;
            } else if (inDoubleQuote) {
                if (c == '"') inDoubleQuote = false;
            } else {

                // String begin
                if (c == '\'') {
                    inSingleQuote = true;
                } else if (c == '"') {
                    inDoubleQuote = true;
                } else if (c == ':' && i + 1 < length &&
                        Character.isJavaIdentifierStart(query.charAt(i + 1))) {

                    // Identifier name
                    int j = i + 2;
                    while (j < length && Character.isJavaIdentifierPart(query.charAt(j))) j++;

                    String name = query.substring(i + 1, j);
                    c = '?';
                    i += name.length();

                    // Add to list
                    List indexList = (List) paramMap.get(name);
                    if (indexList == null) {
                        indexList = new LinkedList();
                        paramMap.put(name, indexList);
                    }
                    indexList.add(new Integer(index));

                    index++;
                }
            }

            parsedQuery.append(c);
        }

        return parsedQuery.toString();
    }

    /**
     * Execute query with result
     */
    public ResultSet executeQuery() throws SQLException {
        return statement.executeQuery();
    }


    /**
     * Executes query without result
     */
    public int executeUpdate() throws SQLException {
        return statement.executeUpdate();
    }

    /**
     * Immediately closes the statement
     */
    public void close() throws SQLException {
        statement.close();
    }

    public void setInt(String name, int value) throws SQLException {

        List indexList = (List) indexMap.get(name);
        if (indexList != null) for (Object index: indexList) {
            statement.setInt((Integer) index, value);
        }
    }

    public void setLong(String name, long value) throws SQLException {

        List indexList = (List) indexMap.get(name);
        if (indexList != null) for (Object index: indexList) {
            statement.setLong((Integer) index, value);
        }
    }

    public void setBoolean(String name, boolean value) throws SQLException {

        List indexList = (List) indexMap.get(name);
        if (indexList != null) for (Object index: indexList) {
            statement.setBoolean((Integer) index, value);
        }
    }

    public void setDouble(String name, double value) throws SQLException {

        List indexList = (List) indexMap.get(name);
        if (indexList != null) for (Object index: indexList) {
            statement.setDouble((Integer) index, value);
        }
    }

    public void setTimestamp(String name, Date value) throws SQLException {

        List indexList = (List) indexMap.get(name);
        if (indexList != null) for (Object index: indexList) {
            statement.setTimestamp(
                    (Integer) index,
                    new Timestamp(value.getTime()));
        }
    }

}
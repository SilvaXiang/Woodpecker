package edu.ecnu.woodpecker.sql;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.jsp.jstl.sql.Result;
import javax.servlet.jsp.jstl.sql.ResultSupport;

import com.alipay.oceanbase.OBGroupDataSource;

import edu.ecnu.woodpecker.constant.DataType;
import edu.ecnu.woodpecker.constant.LogLevelConstant;
import edu.ecnu.woodpecker.constant.ProcedureParameterIO;
import edu.ecnu.woodpecker.constant.SignConstant;
import edu.ecnu.woodpecker.executor.Executor;
import edu.ecnu.woodpecker.log.WpLog;

/**
 * ֧��SQL���ܲ��� ��Ҫ���ܣ���ȡ���ݿ����ӣ���ȡSQL�ʹ洢���̵�ִ������ ִ��SQL�ʹ洢���̣�������ƣ����ݿ�ʵ���ĵ����ɾ��
 */
public class BasicSQLOperation
{

    /**
     * ���ǵ�������ͬ���ݿ����ӵķ�ʽ��ͬ�Լ����õ�jar����ͬ�����ﻹ����Ҫ���۵ĵط�
     * 
     * @param IP ���ݿ����IP
     * @param port ���ݿ����˿�
     * @param dbName ���ݿ�ʵ������ ������CEDAR��ʹ�ã�
     * @param userName �û���
     * @param password ����
     * @param dbType ���ݿ����ϵͳ����
     * @return Connection ���ݿ�����
     */
    private static Map<Connection, String> connExists=new HashMap<>();
    public static Connection getConnection(String IP, String port, String dbName, String userName, String password, DbmsBrand dbType)
            throws Exception
    {
        Connection conn = null;
        switch (dbType)
        {
        case CEDAR_DATA_SOURCE:
            Map<String, String> confParas = new HashMap<String, String>();
            confParas.put("username", userName);
            confParas.put("password", password);
            confParas.put("clusterAddress", IP + SignConstant.COLON_CHAR + port);
            OBGroupDataSource obGroupDataSource = new OBGroupDataSource();
            obGroupDataSource.setDataSourceConfig(confParas);
            obGroupDataSource.init();
            conn = obGroupDataSource.getConnection();
            break;
        case CEDAR:
            String driver = "com.mysql.jdbc.Driver";
            String URL = "jdbc:mysql://" + IP + SignConstant.COLON_CHAR + port + "/mysql?useServerPrepStmts=true";
            Class.forName(driver);
            conn = DriverManager.getConnection(URL, userName, password);
            break;
        case MYSQL:
            URL = "jdbc:mysql://" + IP + SignConstant.COLON_CHAR + port + "/" + dbName;
            try
            {
                conn = DriverManager.getConnection(URL, userName, password);
            }
            catch (SQLException e)
            {
                if (e.getMessage().matches("Unknown database.*"))
                {
                    // Ĭ����woodpecker�⣬û�п��򽨿�
                    conn = DriverManager.getConnection("jdbc:mysql://" + IP + SignConstant.COLON_CHAR + port, userName, password);
                    Statement statement = conn.createStatement();
                    statement.executeUpdate("create database " + dbName);
                    statement.executeQuery("use " + dbName);
                }
                else
                    throw e;
            }
            break;
        case POSTGRESQL:
            driver = "org.postgresql.Driver";
            URL = "jdbc:postgresql://" + IP + SignConstant.COLON_CHAR + port + "/" + dbName;
            Class.forName(driver);
            conn = DriverManager.getConnection(URL, userName, password);
        default:
            WpLog.recordLog(LogLevelConstant.ERROR, "Unsupported DBMS: %s", dbType);
            throw new Exception("Unsupported DBMS");
        }

        return conn;
    }

    /**
     * ����һ����ִͨ���� Statement
     * 
     * @param conn ���ݿ�����
     * @return ����һ����ִͨ���� Statement
     */
    public static Statement getStatement(Connection conn) throws Exception
    {
        return conn.createStatement();
    }

    /**
     * ����һ��Ԥ�����ִ���� PreparedStatement
     * 
     * @param conn ���ݿ�����
     * @param sql ��Ԥ�����SQL
     * @return ����һ��Ԥ�����ִ���� PreparedStatement
     */
    public static PreparedStatement getPreparedStatement(Connection conn, String sql) throws Exception
    {
        return conn.prepareStatement(sql);
    }

    /**
     * ����һ���洢����ִ���� CallableStatement
     * 
     * @param conn ���ݿ�����
     * @param procedure �洢������
     * @return ����һ���洢����ִ���� CallableStatement
     */
    public static CallableStatement getCallableStatement(Connection conn, String procedure) throws Exception
    {
        return conn.prepareCall(procedure);
    }

    /**
     * ʹ����ִͨ����ִ��һ��SQL��䣬�з��ؽ����
     * 
     * @param stmt ��ִͨ����
     * @param sql ��ִ��SQL���
     * @param hasException �Ƿ���׳��쳣
     * @return ���ؽ����
     */
    public static Result stmtExecuteQuery(Statement stmt, String sql, boolean hasException) throws Exception
    {
        if (hasException)
        {
            try
            {
                stmt.executeQuery(sql);
            }
            catch (Exception e)
            {
                // ����֮�е��쳣����¼��־INFO
                WpLog.recordLog(LogLevelConstant.INFO, "Expected exception in statement query");
                Executor.setExceptionString(e.toString());
                return null;
            }
            // ����һ������ȷִ��˵�������쳣ȴû�ף���ʾ��������
            WpLog.recordLog(LogLevelConstant.ERROR, "It should fail but succeed");
            throw new Exception("there is no expected exception");
        }
        else
        {
            try
            {
                return ResultSupport.toResult(stmt.executeQuery(sql));
            }
            catch (SQLException e)
            {
                // �������쳣ȴ���쳣���д���˰������󣬼�¼��־
                WpLog.recordLog(LogLevelConstant.ERROR, "Unexpected exception in statement query");
                throw new Exception(WpLog.getExceptionInfo(e));
            }
        }
    }

    /**
     * ʹ����ִͨ����ִ��һ��SQL��䣬�޷��ؽ����
     * 
     * @param stmt ��ִͨ����
     * @param sql ��ִ��SQL���
     * @param hasException �Ƿ���׳��쳣
     * @return Ӱ������
     */
    public static int stmtExecuteUpdate(Statement stmt, String sql, boolean hasException) throws Exception
    {
        if (hasException)
        {
            try
            {
                stmt.executeUpdate(sql);
            }
            catch (Exception e)
            {
                // ����֮�е��쳣����¼��־INFO
                WpLog.recordLog(LogLevelConstant.INFO, "expected exception in statement update");
                Executor.setExceptionString(e.toString());
                return -1;
            }
            // ����һ������ȷִ��˵�������쳣ȴû�ף���ʾ��������
            WpLog.recordLog(LogLevelConstant.ERROR, "it should fail but succeed");
            throw new Exception("there is no expected exception");
        }
        else
        {
            try
            {
                return stmt.executeUpdate(sql);
            }
            catch (SQLException e)
            {
                // �������쳣ȴ���쳣���д���˰������󣬼�¼��־
                WpLog.recordLog(LogLevelConstant.ERROR, "unexpected exception in statement update");
                throw new Exception(WpLog.getExceptionInfo(e));
            }
        }
    }

    /**
     * ʹ��Ԥ����ִ����ִ��һ��SQL��䣬�з��ؽ����
     * 
     * @param pstmt Ԥ����ִ����
     * @param dataTypes ��Ҫ���õĲ��� ��������
     * @param objects ��Ҫ���õĲ���������ֵ����Ϊ���������������Ϳ��ܲ�һ�����ʲ���Object����
     * @return ���ؽ����
     */
    public static Result pstmtExecuteQuery(PreparedStatement pstmt, DataType[] dataTypes, boolean hasException, Object... objects)
            throws Exception
    {
        if (hasException)
        {
            try
            {
                if (dataTypes != null && objects != null)
                    setParameters(pstmt, dataTypes, objects);
                pstmt.executeQuery();
            }
            catch (Exception e)
            {
                // ����֮�е��쳣����¼��־INFO
                WpLog.recordLog(LogLevelConstant.INFO, "expected exception in prepared statement query");
                Executor.setExceptionString(e.toString());
                return null;
            }
            // ����һ������ȷִ��˵�������쳣ȴû�ף���ʾ��������
            WpLog.recordLog(LogLevelConstant.ERROR, "it should fail but succeed");
            throw new Exception("there is no expected exception");
        }
        else
        {
            try
            {
                // ��Ԥ����ִ���������ø���������ֵ
                if (dataTypes != null && objects != null)
                    setParameters(pstmt, dataTypes, objects);
                return ResultSupport.toResult(pstmt.executeQuery());
            }
            catch (SQLException e)
            {
                // �������쳣ȴ���쳣���д���˰������󣬼�¼��־
                WpLog.recordLog(LogLevelConstant.ERROR, "unexpected exception in prepared statement query");
                throw new Exception(WpLog.getExceptionInfo(e));
            }
        }
    }

    /**
     * ʹ��Ԥ����ִ����ִ��һ��SQL��䣬�޷��ؽ����
     * 
     * @param pstmt Ԥ����ִ����
     * @param dataTypes ��Ҫ���õĲ��� ��������
     * @param objects ��Ҫ���õĲ���������ֵ
     * @return Ӱ������
     */
    public static int pstmtExecuteUpdate(PreparedStatement pstmt, DataType[] dataTypes, boolean hasException, Object... objects) throws Exception
    {
        if (hasException)
        {
            try
            {
                // ��Ԥ����ִ���������ø���������ֵ
                if (dataTypes != null && objects != null)
                    setParameters(pstmt, dataTypes, objects);
                pstmt.executeUpdate();
            }
            catch (Exception e)
            {
                // ����֮�е��쳣����¼��־INFO
                WpLog.recordLog(LogLevelConstant.INFO, "expected exception in prepared statement update");
                Executor.setExceptionString(e.toString());
                return -1;
            }
            // ����һ������ȷִ��˵�������쳣ȴû�ף���ʾ��������
            WpLog.recordLog(LogLevelConstant.ERROR, "it should fail but succeed");
            throw new Exception("there is no expected exception");
        }
        else
        {
            try
            {
                // ��Ԥ����ִ���������ø���������ֵ
                if (dataTypes != null && objects != null)
                    setParameters(pstmt, dataTypes, objects);
                return pstmt.executeUpdate();
            }
            catch (SQLException e)
            {
                // �������쳣ȴ���쳣���д���˰������󣬼�¼��־
                WpLog.recordLog(LogLevelConstant.ERROR, "unexpected exception in prepared statement update");
                throw new Exception(WpLog.getExceptionInfo(e));
            }
        }
    }

    /**
     * ʹ�ô洢����ִ����ִ��һ���洢���̣��з��ؽ����
     * 
     * @param cstmt �洢����ִ����
     * @param dataTypes ��Ҫ���õĲ��� ��������
     * @param paraTypes ��Ҫ���õĲ��� ����������ͣ�in��out��inOut��
     * @param objects ��Ҫ���õĲ����ľ���ֵ
     * @param parameters ��Ҫ���õĲ�����
     * @return ���ؽ����
     */
    public static Result cstmtExecuteQuery(CallableStatement cstmt, DataType[] dataTypes, ProcedureParameterIO[] paraTypes, Object[] objects,
            String[] parameterNames) throws Exception
    {
        if (dataTypes != null && objects != null)
        {
            // ��洢����ִ���������ø���������ֵ
            setParameters(cstmt, dataTypes, paraTypes, objects);
            // �Դ洢���̵�out��inOut��������ע��
            registerParameters(cstmt, dataTypes, paraTypes, objects);
        }
        // ִ�д洢����
        Result result = ResultSupport.toResult(cstmt.executeQuery());
        if (dataTypes != null && objects != null)
        {
            // ��ȡ�洢���̵�out��inOut����
            getParameters(cstmt, dataTypes, paraTypes, parameterNames);
        }
        return result;
    }

    /**
     * ʹ�ô洢����ִ����ִ��һ���洢���̣��޷��ؽ����
     * 
     * @param cstmt �洢����ִ����
     * @param dataTypes ��Ҫ���õĲ��� ��������
     * @param paraTypes ��Ҫ���õĲ��� ����������ͣ�in��out��inOut��
     * @param objects ��Ҫ���õĲ����ľ���ֵ
     * @param parameterNames ��Ҫ���õĲ�����
     * @return Ӱ������
     */
    public static int cstmtExecuteUpdate(CallableStatement cstmt, DataType[] dataTypes, ProcedureParameterIO[] paraTypes, Object[] objects,
            String[] parameterNames) throws Exception
    {
        if (dataTypes != null && objects != null)
        {
            // ��洢����ִ���������ø���������ֵ
            setParameters(cstmt, dataTypes, paraTypes, objects);
            // �Դ洢���̵�out��inOut��������ע��
            registerParameters(cstmt, dataTypes, paraTypes, objects);
        }
        // ִ�д洢����
        int rows = cstmt.executeUpdate();
        if (dataTypes != null && objects != null)
        {
            // ��ȡ�洢���̵�out��inOut����
            getParameters(cstmt, dataTypes, paraTypes, parameterNames);
        }
        return rows;
    }

    /**
     * �������
     * 
     * @param connection ���ݿ�����
     * @param operator �����������
     */
    public static void runTransaction(Connection connection, TransactionOperator operator) throws Exception
    {
        switch (operator)
        {
        case START:
            connection.setAutoCommit(false);
            if(connExists.containsKey(connection))
            {
                throw new Exception("ERROR");
            }
            connExists.put(connection, connection.toString());
            //connection.commit();
            break;
        case COMMIT:
            connExists.remove(connection);
            connection.commit();
            connection.setAutoCommit(true);
            break;
        case ROLLBACK:
            connExists.remove(connection);
            connection.rollback();
            connection.setAutoCommit(true);
            break;
        default:
            WpLog.recordLog(LogLevelConstant.ERROR, "Unsupported transaction operator: %s", operator);
            throw new Exception("Unsupported transaction operator");
        }
    }

    /**
     * ����һ�����ݿ�ʵ�� �ļ��з�Ϊ�����֣���һ����Ϊ����Ͳ������ݵ�SQL��䣻�ڶ�����Ϊɾ��SQL��䡣
     * ������֮��ķָ���Ϊ��[-]+����SQL���֮��ķָ���Ϊ��\n�����ߡ�\r\n�� ִ�е�һ����SQL
     * 
     * @param file ����Դ�ļ�
     * @param encodingFormat �ļ���������
     * @param conn ���ݿ�����
     * @return ����ɹ��򷵻�True�����򷵻�False
     */
    public static boolean importDBI(File file, String encodingFormat, Connection conn) throws Exception
    {
        boolean result = false;
        BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), encodingFormat));
        String inputLine = null;
        Statement stmt = conn.createStatement();
        while ((inputLine = reader.readLine()) != null)
        {
            if (inputLine.matches("(\\s*)|(\\s*#.*)"))
            {
                // ���л���ע��
                continue;
            }

            if (inputLine.matches("[-]+"))
                break;
            stmt.executeUpdate(inputLine);
        }
        result = true;

        if (reader != null)
            reader.close();
        return result;
    }

    /**
     * ɾ��һ�����ݿ�ʵ�� �ļ��з�Ϊ�����֣���һ����Ϊ����Ͳ������ݵ�SQL��䣻�ڶ�����Ϊɾ��SQL��䡣
     * ������֮��ķָ���Ϊ��[-]+����SQL���֮��ķָ���Ϊ��\n�����ߡ�\r\n�� ִ�еڶ�����SQL
     * 
     * @param file ����Դ�ļ�
     * @param encodingFormat �ļ���������
     * @param conn ���ݿ�����
     * @return ɾ���ɹ��򷵻�True�����򷵻�False
     */
    public static boolean clearDBI(File file, String encodingFormat, Connection conn) throws Exception
    {
        boolean result = false;
        BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), encodingFormat));
        String inputLine = null;
        Statement stmt = conn.createStatement();
        boolean flag = false;
        while ((inputLine = reader.readLine()) != null)
        {
            if (inputLine.matches("(\\s*)|(\\s*#.*)"))
            {
                // ���л���ע��
                continue;
            }

            if (inputLine.matches("[-]+"))
            {
                flag = true;
                continue;
            }

            if (flag)
                stmt.executeUpdate(inputLine);
        }
        result = true;

        if (reader != null)
            reader.close();

        return result;
    }

    /**
     * ��Ԥ����ִ���������ø���������ֵ
     * 
     * @param pstmt Ԥ����ִ����
     * @param dataTypes ��Ҫ���õĲ���
     * @param objects ��Ҫ���õĲ���������ֵ
     */
    private static void setParameters(PreparedStatement pstmt, DataType[] dataTypes, Object[] objects) throws Exception
    {
        for (int i = 0; i < dataTypes.length; i++)
        {
            switch (dataTypes[i])
            {
            case INT:
                pstmt.setInt(i + 1, Integer.parseInt(objects[i].toString()));
                break;
            case LONG:
                pstmt.setLong(i + 1, Long.parseLong(objects[i].toString()));
                break;
            case FLOAT:
                pstmt.setFloat(i + 1, Float.parseFloat(objects[i].toString()));
                break;
            case DOUBLE:
                pstmt.setDouble(i + 1, Double.parseDouble(objects[i].toString()));
                break;
            case STRING:
                pstmt.setString(i + 1, objects[i].toString());
                break;
            case DECIMAL:
                pstmt.setBigDecimal(i + 1, new BigDecimal(objects[i].toString()));
                break;
            case BOOLEAN:
                pstmt.setBoolean(i + 1, Boolean.parseBoolean(objects[i].toString()));
                break;
            case TIMESTAMP:
                pstmt.setTimestamp(i + 1, Timestamp.valueOf(objects[i].toString()));
                break;
            default:
                WpLog.recordLog(LogLevelConstant.ERROR, "Unsupported data type: %s", dataTypes[i]);
                throw new Exception(String.format("Unsupported data type: %s", dataTypes[i]));
            }
        }
    }

    /**
     * ��洢����ִ���������ø���������ֵ
     * 
     * @param cstmt �洢����ִ����
     * @param dataTypes ��Ҫ���õĲ���
     * @param paraTypes ��Ҫ���õĲ��� ����������ͣ�in��out��inOut��
     * @param objects ��Ҫ���õĲ���������ֵ
     */
    private static void setParameters(CallableStatement cstmt, DataType[] dataTypes, ProcedureParameterIO[] paraTypes, Object[] objects) throws Exception
    {
        // ֻ��in��inOut���Ͳ�����Ҫ������ֵ
        for (int i = 0; i < dataTypes.length
                && (paraTypes[i] == ProcedureParameterIO.IN || paraTypes[i] == ProcedureParameterIO.IN_OUT); i++)
        {
            switch (dataTypes[i])
            {
            case INT:
                cstmt.setInt(i + 1, Integer.parseInt(objects[i].toString()));
                break;
            case LONG:
                cstmt.setLong(i + 1, Long.parseLong(objects[i].toString()));
                break;
            case FLOAT:
                cstmt.setFloat(i + 1, Float.parseFloat(objects[i].toString()));
                break;
            case DOUBLE:
                cstmt.setDouble(i + 1, Double.parseDouble(objects[i].toString()));
                break;
            case STRING:
                cstmt.setString(i + 1, objects[i].toString());
                break;
            case DECIMAL:
                cstmt.setBigDecimal(i + 1, new BigDecimal(objects[i].toString()));
                break;
            case BOOLEAN:
                cstmt.setBoolean(i + 1, Boolean.parseBoolean(objects[i].toString()));
                break;
            case TIMESTAMP:
                cstmt.setTimestamp(i + 1, Timestamp.valueOf(objects[i].toString()));
                break;
            default:
                WpLog.recordLog(LogLevelConstant.ERROR, "Unsupported data type: %s", dataTypes[i]);
                throw new Exception(String.format("Unsupported data type: %s", dataTypes[i]));
            }
        }
    }

    /**
     * �Դ洢���̵�out��inOut��������ע��
     * 
     * @param cstmt �洢����ִ����
     * @param dataTypes ��Ҫ���õĲ���
     * @param paraTypes ��Ҫ���õĲ��� ����������ͣ�in��out��inOut��
     * @param objects ��Ҫ���õĲ���������ֵ
     */
    private static void registerParameters(CallableStatement cstmt, DataType[] dataTypes, ProcedureParameterIO[] paraTypes, Object[] objects) throws Exception
    {
        // ֻ��out��inOut���Ͳ�����Ҫ����ע��
        for (int i = 0; i < dataTypes.length; i++)
        {
            if (paraTypes[i] == ProcedureParameterIO.OUT || paraTypes[i] == ProcedureParameterIO.IN_OUT)
            {
                switch (dataTypes[i])
                {
                case INT:
                    cstmt.registerOutParameter(i + 1, Types.INTEGER);
                    break;
                case LONG:
                    cstmt.registerOutParameter(i + 1, Types.BIGINT);
                    break;
                case FLOAT:
                    cstmt.registerOutParameter(i + 1, Types.FLOAT);
                    break;
                case DOUBLE:
                    cstmt.registerOutParameter(i + 1, Types.DOUBLE);
                    break;
                case STRING:
                    cstmt.registerOutParameter(i + 1, Types.VARCHAR);
                    break;
                case DECIMAL:
                    cstmt.registerOutParameter(i + 1, Types.DECIMAL);
                    break;
                case BOOLEAN:
                    cstmt.registerOutParameter(i + 1, Types.BOOLEAN);
                    break;
                case TIMESTAMP:
                    cstmt.registerOutParameter(i + 1, Types.TIMESTAMP);
                    break;
                default:
                    WpLog.recordLog(LogLevelConstant.ERROR, "Unsupported data type: %s", dataTypes[i]);
                    throw new Exception(String.format("Unsupported data type: %s", dataTypes[i]));
                }
            }
        }
    }

    /**
     * ��ȡ�洢���̵�out��inOut����
     * 
     * @param cstmt �洢����ִ����
     * @param dataTypes ��Ҫ���õĲ���
     * @param paraTypes ��Ҫ���õĲ��� ����������ͣ�in��out��inOut�� 0��in��1��out��2��inOut
     * @param parameterNames ��Ҫ���õĲ�����
     */
    private static void getParameters(CallableStatement cstmt, DataType[] dataTypes, ProcedureParameterIO[] paraTypes, String[] parameterNames) throws Exception
    {
        Map<String, Object> varValueMap = Executor.getVarValueMap();
        for (int i = 0; i < dataTypes.length; i++)
        {
            // ֻ��out��inOut���Ͳ�����Ҫ���д���
            if (paraTypes[i] == ProcedureParameterIO.OUT || paraTypes[i] == ProcedureParameterIO.IN_OUT)
            {
                switch (dataTypes[i])
                {
                case INT:
                    varValueMap.put(parameterNames[i], cstmt.getInt(i + 1));
                    break;
                case LONG:
                    varValueMap.put(parameterNames[i], cstmt.getLong(i + 1));
                    break;
                case FLOAT:
                    varValueMap.put(parameterNames[i], cstmt.getFloat(i + 1));
                    break;
                case DOUBLE:
                    varValueMap.put(parameterNames[i], cstmt.getDouble(i + 1));
                    break;
                case STRING:
                    varValueMap.put(parameterNames[i], cstmt.getString(i + 1));
                    break;
                case DECIMAL:
                    varValueMap.put(parameterNames[i], cstmt.getBigDecimal(i + 1));
                    break;
                case BOOLEAN:
                    varValueMap.put(parameterNames[i], cstmt.getBoolean(i + 1));
                    break;
                case TIMESTAMP:
                    varValueMap.put(parameterNames[i], cstmt.getTimestamp(i + 1));
                    break;
                default:
                    WpLog.recordLog(LogLevelConstant.ERROR, "Unsupported data type: %s", dataTypes[i]);
                    throw new Exception(String.format("Unsupported data type: %s", dataTypes[i]));
                }
            }
        }
    }

}

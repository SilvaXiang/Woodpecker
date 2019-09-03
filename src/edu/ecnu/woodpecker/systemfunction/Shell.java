package edu.ecnu.woodpecker.systemfunction;

import java.sql.Connection;
import java.sql.Statement;

import edu.ecnu.woodpecker.constant.LogLevelConstant;
import edu.ecnu.woodpecker.constant.SQLConstant;
import edu.ecnu.woodpecker.controller.CedarOperation;
import edu.ecnu.woodpecker.controller.clusterinfo.ClustersInfo;
import edu.ecnu.woodpecker.log.Recorder;
import edu.ecnu.woodpecker.log.WpLog;
import edu.ecnu.woodpecker.sql.BasicSQLOperation;
import edu.ecnu.woodpecker.sql.DbmsBrand;
import edu.ecnu.woodpecker.util.Log;
import edu.ecnu.woodpecker.util.Util;

public class Shell
{

    /**
     * ��ͣ��ǰ�߳�ָ����ʱ��
     * 
     * @param time ʱ��
     * @param timeUnit ʱ�䵥λ
     */
    public static void sleep(int time, String timeUnit)
    {
        if (timeUnit.equals("microsecond"))
        {
            time = time / 1000;
            sleep(time);
        }
        else if (timeUnit.equals("millisecond"))
        {
            sleep(time);
        }
        else if (timeUnit.equals("second"))
        {
            time = time * 1000;
            sleep(time);
        }
        else if (timeUnit.equals("minute"))
        {
            time = time * 1000 * 60;
            sleep(time);
        }
        else if (timeUnit.equals("hour"))
        {
            time = time * 1000 * 60 * 60;
            sleep(time);
        }
        else
        {
            System.out.println("��λ����");
            Recorder.FunctionRecord(Log.getRecordMetadata(), "time unit's input is wrong",
                    LogLevelConstant.ERROR);
        }
    }

    /**
     * ��ʾ��ָ��ip��ַ��ִ��shell���Ĭ��·�����û���Ŀ¼��
     * 
     * @param command shell����
     * @param ip ip��ַ
     */
    public static void shellCommand(String command, String ip)
    {

        Util.exec(ip, ClustersInfo.getUserName(), ClustersInfo.getPassword(),
                ClustersInfo.getConnectionPort(), command);

    }

    private static void sleep(int time)
    {
        try
        {
            Thread.sleep(time);
            Recorder.FunctionRecord(Log.getRecordMetadata(), "SLEEP " + time, LogLevelConstant.INFO);
        }
        catch (InterruptedException e)
        {
            WpLog.recordLog(LogLevelConstant.ERROR, WpLog.getExceptionInfo(e));
        }
    }

    /**
     * �����Ƿ�ɹ�
     * 
     * @param IP
     * @param port
     * @return
     * @throws Exception
     */
    public static boolean isCreateTable(String IP, String port)
    {
        Connection conn=null;
        try
        {

            conn = BasicSQLOperation.getConnection(IP, port, SQLConstant.TEST_DB,
                    CedarOperation.getDatabaseUser(), CedarOperation.getDatabasePassword(),
                    DbmsBrand.CEDAR);
            Statement stmt = BasicSQLOperation.getStatement(conn);
            // ����
            String sql1="set @@session.ob_query_timeout=9000000000;";
            BasicSQLOperation.stmtExecuteUpdate(stmt, sql1, false);
            String sql = "create table woodpecker_test_pyc (id int primary key , c1 int,c2 varchar(20))";
            BasicSQLOperation.stmtExecuteUpdate(stmt, sql, false);
            stmt.close();
            conn.close();
        }
        catch (Exception e)
        {
            WpLog.recordLog(LogLevelConstant.ERROR, "%s %s", IP, WpLog.getExceptionInfo(e));
        }

        return true;
    }

    /**
     * ����ɾ���Ƿ�ɹ�
     * 
     * @param IP
     * @param port
     * @return
     */
    public static boolean isUpdateTable(String IP, String port)
    {
        Connection conn;
        int rows = -1;
        try
        {
            conn = BasicSQLOperation.getConnection(IP, port, SQLConstant.TEST_DB,
                    CedarOperation.getDatabaseUser(), CedarOperation.getDatabasePassword(),
                    DbmsBrand.CEDAR);
            Statement stmt = BasicSQLOperation.getStatement(conn);
            // ��������
            String sql1="set @@session.ob_query_timeout=9000000000;";
            BasicSQLOperation.stmtExecuteUpdate(stmt, sql1, false);
            String sql = "replace into woodpecker_test_pyc values(1,1,'aaaa')";
            rows = BasicSQLOperation.stmtExecuteUpdate(stmt, sql, false);
            stmt.close();
        }
        catch (Exception e)
        {
            WpLog.recordLog(LogLevelConstant.ERROR, "%s %s", IP, WpLog.getExceptionInfo(e));
        }
        if (rows != 1)
        {
            return false;
        }

        rows = -1;
        try
        {
            conn = BasicSQLOperation.getConnection(IP, port, SQLConstant.TEST_DB,
                    CedarOperation.getDatabaseUser(), CedarOperation.getDatabasePassword(),
                    DbmsBrand.CEDAR);
            String sql = "delete from woodpecker_test_pyc where id = 1";
            Statement stmt1 = BasicSQLOperation.getStatement(conn);
            rows = BasicSQLOperation.stmtExecuteUpdate(stmt1, sql, false);
            stmt1.close();
            conn.close();
        }
        catch (Exception e)
        {
            WpLog.recordLog(LogLevelConstant.ERROR, "%s %s", IP, WpLog.getExceptionInfo(e));
        }
        if (rows != 1)
        {
            return false;
        }
        return true;
    }

    /**
     * �ж�ɾ���Ƿ�ɹ�
     * 
     * @param IP
     * @param port
     * @return
     */
    public static boolean isDeleteTable(String IP, String port)
    {
        Connection conn;
        int rows = -1;
        try
        {
            conn = BasicSQLOperation.getConnection(IP, port, SQLConstant.TEST_DB,
                    CedarOperation.getDatabaseUser(), CedarOperation.getDatabasePassword(),
                    DbmsBrand.CEDAR);
            Statement stmt = BasicSQLOperation.getStatement(conn);
            // ɾ��
            String sql1="set @@session.ob_query_timeout=9000000000;";
            BasicSQLOperation.stmtExecuteUpdate(stmt, sql1, false);
            String sql = "drop table woodpecker_test_pyc ";
            rows = BasicSQLOperation.stmtExecuteUpdate(stmt, sql, false);
            stmt.close();
            conn.close();
        }
        catch (Exception e)
        {
            WpLog.recordLog(LogLevelConstant.ERROR, "%s %s", IP, WpLog.getExceptionInfo(e));
        }
        if (rows == 0)
        {
            return true;
        }
        return false;
    }

}

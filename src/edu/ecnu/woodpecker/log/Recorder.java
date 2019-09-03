package edu.ecnu.woodpecker.log;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import edu.ecnu.woodpecker.constant.FileConstant;
import edu.ecnu.woodpecker.constant.LogLevelConstant;
import edu.ecnu.woodpecker.sql.IdealResultSet;
import edu.ecnu.woodpecker.stresstest.PerformResult;
import edu.ecnu.woodpecker.util.Log;

import javax.servlet.jsp.jstl.sql.Result;

public class Recorder
{
    static Logger logger = Logger.getLogger(Recorder.class.getName());
    
    /**
     * ��־����
     */
    private static int logRecordLength = 80;
    
    /**
     * log4j�����ļ�·��
     */
    private static String log4jConfigPath = FileConstant.LOG4J_CONFIG_PATH;
    
    static
    {
        PropertyConfigurator.configure(log4jConfigPath);
    }

    /**
     * �˽ӿ��ɸ�����ģ����ã�����ģ����Ϣ��״̬��Ϣ��״̬��Ϣ���𣬽�������Ϣ����һ����ʽ��ϳ�һ��������״̬��¼д��״̬��¼�ļ�
     * 
     * @param moduleInfo ģ����Ϣ
     * @param stateInfo ״̬��Ϣ
     * @param stateLevel ״̬��Ϣ���� INFO��ERROR��WARN��DEBUG��FATAL
     */
    public static void FunctionRecord(String moduleInfo, String stateInfo, String stateLevel)
    {
        StringBuilder tmp = new StringBuilder(moduleInfo);
        for (int i = tmp.length(); i <= logRecordLength; i++)
        {
            tmp.append(" ");
        }
        moduleInfo = tmp.toString();
        String message = moduleInfo + stateInfo;
        if (stateLevel.equals(LogLevelConstant.INFO))
        {
            logger.info(message);
        }
        else if (stateLevel.equals(LogLevelConstant.ERROR))
        {
            logger.error(message);
        }
        else if (stateLevel.equals(LogLevelConstant.WARN))
        {
            logger.warn(message);
        }
        else if (stateLevel.equals(LogLevelConstant.DEBUG))
        {
            logger.debug(message);
        }
    }

    /**
     * ��SQLģ����ã�������������Ϣ����
     * 
     * @param res �����
     * @param moduleInfo ģ����Ϣ
     * @param stateLevel ��Ϣ����
     */
    public static void SQLRecord(IdealResultSet res, String moduleInfo, String stateLevel)
    {
        // PropertyConfigurator.configure(log4jConfigPath);
        StringBuilder tmp = new StringBuilder(moduleInfo);
        for (int i = tmp.length(); i <= logRecordLength; i++)
        {
            tmp.append(" ");
        }
        moduleInfo = tmp.toString();
        String message = moduleInfo + FileConstant.LINUX_LINE_FEED;
        String[][] data = res.getData();
        int rowCount = data.length;
        int columnCount = 0;
        if (rowCount >= 0)
        {
            columnCount = data[0].length;
        }

        for (int i = 0; i < rowCount; i++)
        {
            for (int j = 0; j < columnCount; j++)
                message += data[i][j] + "  ";
            message += FileConstant.LINUX_LINE_FEED;
        }

        if (stateLevel.equals(LogLevelConstant.INFO))
        {
            logger.info(message);
        }
        else if (stateLevel.equals(LogLevelConstant.ERROR))
        {
            logger.error(message);
        }
        else if (stateLevel.equals(LogLevelConstant.WARN))
        {
            logger.warn(message);
        }
        else if (stateLevel.equals(LogLevelConstant.DEBUG))
        {
            logger.debug(message);
        }
    }

    /**
     * ��SQLģ����ã�������������Ϣ����
     * 
     * @param res �����
     * @param moduleInfo ģ����Ϣ
     * @param stateLevel ��Ϣ����
     */
    public static void SQLRecord(Result res, String moduleInfo, String stateLevel)
    {
        // PropertyConfigurator.configure(log4jConfigPath);
        StringBuilder tmp = new StringBuilder(moduleInfo);
        for (int i = tmp.length(); i <= logRecordLength; i++)
        {
            tmp.append(" ");
        }
        moduleInfo = tmp.toString();
        String message = moduleInfo + FileConstant.LINUX_LINE_FEED;
        int rowCount = res.getRowCount();
        int columnCount = res.getColumnNames().length;
        Object[][] resData = res.getRowsByIndex();
        for (int i = 0; i < rowCount; i++)
        {
            for (int j = 0; j < columnCount; j++)
            {
                message += resData[i][j] + "  ";
            }
            message += FileConstant.LINUX_LINE_FEED;
        }

        if (stateLevel.equals(LogLevelConstant.INFO))
        {
            logger.info(message);
        }
        else if (stateLevel.equals(LogLevelConstant.ERROR))
        {
            logger.error(message);
        }
        else if (stateLevel.equals(LogLevelConstant.WARN))
        {
            logger.warn(message);
        }
        else if (stateLevel.equals(LogLevelConstant.DEBUG))
        {
            logger.debug(message);
        }
    }

    /**
     * ��SQLģ����ã�������������Ϣ����
     * 
     * @param res1 �����1 Դ���
     * @param res2 �����1 �������
     * @param moduleInfo ģ����Ϣ
     * @param stateLevel ��Ϣ����
     */
    public static void SQLRecord(Result res1, IdealResultSet res2, String moduleInfo,
            String stateLevel)
    {
        // PropertyConfigurator.configure(log4jConfigPath);
        StringBuilder tmp = new StringBuilder(moduleInfo);
        for (int i = tmp.length(); i <= logRecordLength; i++)
        {
            tmp.append(" ");
        }
        moduleInfo = tmp.toString();
        String message = moduleInfo + FileConstant.LINUX_LINE_FEED;
        int rowCount = res1.getRowCount();
        int columnCount = res1.getColumnNames().length;
        Object[][] resData = res1.getRowsByIndex();
        for (int i = 0; i < rowCount; i++)
        {
            for (int j = 0; j < columnCount; j++)
            {
                message += resData[i][j] + "  ";
            }
            message += FileConstant.LINUX_LINE_FEED;
        }
        message += "---------------------------------------------------------------------------------"
                + FileConstant.LINUX_LINE_FEED;
        String[][] data = res2.getData();
        rowCount = data.length;
        columnCount = 0;
        if (rowCount >= 0)
        {
            columnCount = data[0].length;
        }

        for (int i = 0; i < rowCount; i++)
        {
            for (int j = 0; j < columnCount; j++)
            {
                message += data[i][j] + "  ";
            }
            message += FileConstant.LINUX_LINE_FEED;
        }

        if (stateLevel.equals(LogLevelConstant.INFO))
        {
            logger.info(message);
        }
        else if (stateLevel.equals(LogLevelConstant.ERROR))
        {
            logger.error(message);
        }
        else if (stateLevel.equals(LogLevelConstant.WARN))
        {
            logger.warn(message);
        }
        else if (stateLevel.equals(LogLevelConstant.DEBUG))
        {
            logger.debug(message);
        }
    }

    /**
     * �ɿ��������ã����뼴�����еĲ��԰�����Ψһ��ʶ
     * 
     * @param moduleInfo ģ����Ϣ
     * @param stateInfo ģ����Ϣ
     */
    public static void WorkflowControllerRecord(String moduleInfo, String stateInfo)
    {
        // PropertyConfigurator.configure(log4jConfigPath);
        String message = moduleInfo;
        StringBuilder tmp = new StringBuilder(moduleInfo);
        for (int i = tmp.length(); i <= logRecordLength; i++)
        {
            tmp.append(" ");
        }
        moduleInfo = tmp.toString();
        message = String.format("%s{%s}\r\n", moduleInfo, stateInfo);
        // message = moduleInfo + "{" + stateInfo + "}" + FileConstant.LINUX_LINE_FEED;
        logger.info(message);
    }

    /**
     * ��ѹ��ģ�����
     * 
     * @param res1 ѹ������
     * @param moduleInfo ģ����Ϣ
     * @param stressInfo ѹ����Ϣ
     */
    public static void StressTestRecord(PerformResult res1, String moduleInfo, String stressInfo)
    {
        // PropertyConfigurator.configure(log4jConfigPath);
        StringBuilder tmp = new StringBuilder(moduleInfo);
        for (int i = tmp.length(); i <= logRecordLength; i++)
        {
            tmp.append(" ");
        }
        moduleInfo = tmp.toString();
        String message = moduleInfo + FileConstant.LINUX_LINE_FEED;
        message += "StressInfo: " + stressInfo + FileConstant.LINUX_LINE_FEED;
        boolean isTransaction = res1.isTransaction();
        if (isTransaction)
        {
            message += "AvgTPS:  " + res1.getAvgTPS() + FileConstant.LINUX_LINE_FEED;
        }
        else
        {
            message += "AvgQPS:  " + res1.getAvgQPS() + FileConstant.LINUX_LINE_FEED;
        }
        message += "AvgResponseTime:  " + res1.getAvgResponseTime() + "us" + FileConstant.LINUX_LINE_FEED;
        message += "TP50ResponseTime:  " + res1.getTP50ResponseTime() + "us" + FileConstant.LINUX_LINE_FEED;
        message += "TP90ResponseTime:  " + res1.getTP90ResponseTime() + "us" + FileConstant.LINUX_LINE_FEED;
        message += "TP99ResponseTime:  " + res1.getTP99ResponseTime() + "us" + FileConstant.LINUX_LINE_FEED;
        logger.info(message);
    }

    public static void main(String[] args)
    {
        FunctionRecord(Log.getRecordMetadata(), "aaaaa", "INFO");
        FunctionRecord(Log.getRecordMetadata(), "aaaaa", "ERROR");
        FunctionRecord(Log.getRecordMetadata(), "aaaaa", "WARN");
        FunctionRecord(Log.getRecordMetadata(), "aaaaa", "DEBUG");

        PerformResult rs = new PerformResult();
        rs.setTransaction(true);
        rs.setAvgTPS(100);
        rs.setAvgResponseTime(111);
        rs.setTP50ResponseTime(222);
        rs.setTP90ResponseTime(22222);
        rs.setTP99ResponseTime(123);
        StressTestRecord(rs, "dddd", "ffff");
    }
}

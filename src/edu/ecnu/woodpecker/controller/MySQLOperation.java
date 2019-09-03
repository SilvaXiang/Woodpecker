package edu.ecnu.woodpecker.controller;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Method;

import org.apache.commons.cli.CommandLine;

import edu.ecnu.woodpecker.constant.CLIParameterConstant;
import edu.ecnu.woodpecker.constant.FileConstant;
import edu.ecnu.woodpecker.constant.LogLevelConstant;
import edu.ecnu.woodpecker.constant.SignConstant;
import edu.ecnu.woodpecker.executor.Executor;
import edu.ecnu.woodpecker.executor.Parser;
import edu.ecnu.woodpecker.log.WpLog;
import edu.ecnu.woodpecker.util.Util;

/**
 * MySQL���ݿ�Ĳ�����
 * 
 */
public class MySQLOperation extends TestController implements DatabaseOperation
{
    /**
     * MySQL��װ��ʽ��true��Ϊ���а棬false��RPM��װ��
     */
    private static boolean release;

    /**
     * MySQL��װĿ¼��RPM���Ϊ�գ�release�治��Ϊ��
     */
    private static String MySQLRoot = null;

    /**
     * MySQL����server���˻�������
     */
    private static String serverUser = null;
    private static String serverPassword = null;

    /**
     * MySQL���˻�������
     */
    private static String databaseUser = null;
    private static String databasePassword = null;

    /**
     * MySQL��������IP�Ͷ˿�
     */
    private static String IP = null;
    private static int port;
    
    private MySQLOperation(){}
    
    private static class SingletonHolder
    {
        private static MySQLOperation instance = new MySQLOperation();
    }
    
    public static MySQLOperation getInstance()
    {
        return SingletonHolder.instance;
    }

    @Override
    /**
     * ��ȡMySQL�����ļ���Ϣ����ʼ��
     * 
     * @param configFilePath MySQL�����ļ�·��
     */
    public void initialize(String configFilePath)
    {
        WpLog.recordLog(LogLevelConstant.INFO, "Initialize MySQL parameters");
        serverUser = TestController.getServerUserName();
        serverPassword = TestController.getServerPassword();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(configFilePath), FileConstant.UTF_8)))
        {
            String line = null;
            StringBuilder stringBuilder = new StringBuilder();
            while ((line = br.readLine()) != null)
            {
                line = line.trim();
                // ���л���ע����
                if (line.matches("^(#{1}.*)|(\\s*+)$"))
                    continue;
                // ��ȡ�������Ӧ�ĺ�������
                stringBuilder.append("set").append(line.substring(0, line.indexOf(SignConstant.ASSIGNMENT_CHAR)).trim());
                stringBuilder.setCharAt(3, Character.toUpperCase(stringBuilder.charAt(3)));
                for (int fromIndex = 0; fromIndex < stringBuilder.length();)
                {
                    fromIndex = stringBuilder.indexOf(SignConstant.UNDERLINE_STR, fromIndex);
                    if (fromIndex == -1)
                        break;
                    stringBuilder.deleteCharAt(fromIndex);
                    stringBuilder.setCharAt(fromIndex, Character.toUpperCase(stringBuilder.charAt(fromIndex)));
                }
                String methodName = stringBuilder.toString();
                // ��ȡ�������ֵ
                String confValue = line.substring(line.indexOf(SignConstant.ASSIGNMENT_CHAR) + 1).trim();
                int index = confValue.indexOf(SignConstant.SHARP_CHAR);
                confValue = index == -1 ? confValue : confValue.substring(0, index).trim();
                stringBuilder.delete(0, stringBuilder.length());
                // ʹ�÷��䣬���з�����õ�set����Ҫ�������������String
                Method method = MySQLOperation.class.getMethod(methodName, String.class);
                method.invoke(null, confValue);
            }
        }
        catch (Exception e)
        {
            WpLog.recordLog(LogLevelConstant.ERROR, WpLog.getExceptionInfo(e));
            System.err.println("There are errors when initializing parameters");
            exit(1);
        }
        // �����ļ���û������MySQL Root·����Ϊ��
        MySQLRoot = MySQLRoot == null ? "" : MySQLRoot;
    }

    @Override
    public void enter(CommandLine line)
    {
        // ��MySQL��Cedar����Ч
        boolean needStart = line.hasOption(CLIParameterConstant.DEPLOY) ? true : false;
        // MySQL���н����Ƿ�ر�
        boolean needClose = line.hasOption(CLIParameterConstant.CLOSE_MYSQL) ? true : false;
        // ����MySQL
        if (needStart && !startMySQL())
        {
            // ����MySQLʧ�ܣ������������
            WpLog.recordLog(LogLevelConstant.ERROR, "Start MySQL unsuccessfully and stop Woodpecker");
            exit(1);
        }
        // ����MySQLû�м�Ⱥ�ȸ������Ҫ��cedar�������ÿ����԰������������͹ر�
        for (String group : groupSet)
        {
            TestController.currentGroup = group;
            File[] caseFiles = new File(testCasePath + group).listFiles();
            for (File caseFile : caseFiles)
            {
                WpLog.recordTestflow("caseid: %s/%s", group, caseFile.getName());
                File midResult = Parser.parse(caseFile);
                if (midResult == null)
                {
                    // ����ʧ�ܣ��˰�������
                    WpLog.recordLog(LogLevelConstant.ERROR, "%s parse unsuccessfully and skip", caseFile.getName());
                    continue;
                }

                boolean retrySucceed = false;
                boolean isPass = Executor.execute(midResult, Parser.getVarValueMap(), Parser.getVarTypeMap());
                if (!isPass)
                {
                    // �״�����ûͨ��
                    WpLog.recordLog(LogLevelConstant.ERROR,
                            "%s execute unsuccessfully first time", caseFile.getName());
                    // ����retryCount��
                    for (int i = 0; i < retryCount; i++)
                    {
                        // �ر�MySQL�������ϴ�����ʧ�ܲ��������ݣ�ת��MySQL��־����������MySQL
                        if (needClose)
                        {
                            closeMySQL(TestStatusConstant.FAIL);
                            startMySQL();
                        }
                        // ����ִ��
                        WpLog.recordTestflow("caseid: %s/%s", group, caseFile.getName());
                        isPass = Executor.execute(midResult, Parser.getVarValueMap(), Parser.getVarTypeMap());
                        if (isPass)
                        {
                            // ��i+1�����Գɹ�
                            WpLog.recordLog(LogLevelConstant.INFO,
                                    "%s execute successfully after retry %d times", caseFile.getName(), i + 1);
                            retrySucceed = true;
                            break;
                        }
                    }

                    if (!retrySucceed)
                    {
                        // ����retryCount������û��ͨ����������case
                        WpLog.recordLog(LogLevelConstant.ERROR,
                                "%s execute unsuccessfully after retry %d times", caseFile.getName(), retryCount);
                        // �ر�MySQL�������ݣ�ת��MySQL��־������MySQL
                        if (needClose)
                        {
                            closeMySQL(TestStatusConstant.FAIL);
                            startMySQL();
                        }
                        continue;
                    }
                }
                else
                {
                    // �˰���ͨ��
                    WpLog.recordLog(LogLevelConstant.INFO,
                            "%s execute successfully first time", caseFile.getName());
                }
            }
        }
        // ��¼��־���˴ο�����н�����
        WpLog.recordLog(LogLevelConstant.INFO, "Framework has executed all test cases and prepares to stop");
        // ��ʼ�������ɱ���
        WpLog.generateReport();
        WpLog.generateStressReport();
        if (needClose)
        {
            closeMySQL(TestStatusConstant.PASS);
        }
    }

    /**
     * ����MySQL�����Զ����ɹ�����false
     * 
     * @return �����ɹ�Ϊtrue��ʧ��Ϊfalse
     */
    private static boolean startMySQL()
    {
        if (MySQLOperation.release)
        {
            // ���а�ֱ��ȥ��װĿ¼���� TODO

            return true;
        }

        // RPM��ʹ��ϵͳ���������������û���Ҫ��Ȩ��
        String cmd = "service mysqld start";
        for (int i = 0; i < retryCount + 1; i++)
        {
            String[] result = execShell(IP, serverUser, serverPassword, cmd);
            if (result[0].matches("Starting\\s+mysqld:\\s+\\[\\s+OK\\s+]\\s*"))
            {
                System.out.println("����MySQL�ɹ�");
                return true;
            }
            else if (result.length != 1 || result[0].matches("Starting\\s+mysqld:\\s+\\[\\s*FAILED\\s*]\\s*"))
            {
                System.out.println("����MySQLʧ��");
            }
        }
        return false;
    }

    /**
     * �ر�MySQL�����Զ����ɹ�����false�������ر�ʱ������ݺ���־�ļ���������ʱת����־�ļ�
     * 
     * @param type �ر����ͣ�normal/unnormal
     * @return �رճɹ�Ϊtrue��ʧ��Ϊfalse
     */
    private static boolean closeMySQL(String type)
    {
        if (MySQLOperation.release)
        {
            // ���а�ֱ��ȥ��װĿ¼�ر� TODO

            return true;
        }

        // RPM��ʹ��ϵͳ����ر�
        String cmd = "service mysqld stop";
        if (type.equals(TestStatusConstant.PASS))
        {
            boolean isStopSuccessful = false;
            for (int i = 0; i < retryCount + 1; i++)
            {
                String[] result = execShell(IP, serverUser, serverPassword, cmd);
                if (result[0].matches("Stopping\\s+mysqld:\\s+\\[\\s+OK\\s+]\\s*"))
                {
                    System.out.println("�ر�MySQL�ɹ�");
                    isStopSuccessful = true;
                }
                else if (result.length != 1 || result[0].matches("Stopping\\s+mysqld:\\s+\\[\\s*FAILED\\s*]\\s*"))
                {
                    System.out.println("�ر�MySQLʧ��");
                }
            }
            if (!isStopSuccessful)
            {
                return false;
            }

            // ������ݺ���־�ļ�
            String rmDataLog = "rm -rf /var/lib/mysql/woodpecker && rm -f /var/log/mysqld.log";
            execShell(IP, serverUser, serverPassword, rmDataLog);
            return true;
        }
        else if (type.equals(TestStatusConstant.FAIL))
        {
            boolean isStopSuccessful = false;
            for (int i = 0; i < retryCount + 1; i++)
            {
                String[] result = execShell(IP, serverUser, serverPassword, cmd);
                if (result[0].matches("Stopping\\s+mysqld:\\s+\\[\\s+OK\\s+]\\s*"))
                {
                    System.out.println("�ر�MySQL�ɹ�");
                    isStopSuccessful = true;
                }
                else if (result.length != 1 || result[0].matches("Stopping\\s+mysqld:\\s+\\[\\s*FAILED\\s*]\\s*"))
                {
                    System.out.println("�ر�MySQLʧ��");
                }
            }
            if (!isStopSuccessful)
            {
                return false;
            }

            // ת����־�ļ���������ݺ���־�ļ�
            String storeLog = "scp /var/log/mysqld.log " + serverUser + "@" + IP + ":~" + serverUser + "/MySQL_log_store_by_Woodpecker";
            String rmDataLog = "rm -rf /var/lib/mysql/woodpecker && rm -f /var/log/mysqld.log";
            execShell(IP, serverUser, serverPassword, storeLog + " && " + rmDataLog);
            return true;
        }
        return false;
    }

    /**
     * Execute command in specified host
     * 
     * @param host The IP of host
     * @param serverUser
     * @param serverPassword
     * @param command 
     * @return The information of command
     */
    private static String[] execShell(String host, String serverUser, String serverPassword, String command)
    {
        String result = Util.exec(host, serverUser, serverPassword, 22, command);
        return result.split(FileConstant.WIN_LINE_FEED_STR);
    }

    public static String getIP()
    {
        return IP;
    }

    public static void setIP(String IP)
    {
        MySQLOperation.IP = IP;
    }

    public static int getPort()
    {
        return port;
    }

    public static void setPort(String port)
    {
        MySQLOperation.port = Integer.parseInt(port);
    }

    public static void setDatabaseUser(String databaseUser)
    {
        MySQLOperation.databaseUser = databaseUser;
    }

    public static void setDatabasePassword(String databasePassword)
    {
        MySQLOperation.databasePassword = databasePassword;
    }

    public static void setRelease(String release)
    {
        MySQLOperation.release = Boolean.parseBoolean(release);
    }

    public static void setMySQLRoot(String MySQLRoot)
    {
        MySQLOperation.MySQLRoot = MySQLRoot;
    }

    public static String getDatabaseUser()
    {
        return databaseUser;
    }

    public static String getDatabasePassword()
    {
        return databasePassword;
    }
    
}

/**
 * ���԰���ִ��״̬������
 * 
 */
final class TestStatusConstant
{
    public final static String PASS = "normal";
    public final static String FAIL = "unnormal";
}
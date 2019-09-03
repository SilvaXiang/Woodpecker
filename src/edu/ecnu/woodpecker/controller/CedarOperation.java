package edu.ecnu.woodpecker.controller;

import java.io.File;

import org.apache.commons.cli.CommandLine;

import edu.ecnu.woodpecker.constant.CLIParameterConstant;
import edu.ecnu.woodpecker.constant.ConfigConstant;
import edu.ecnu.woodpecker.constant.FileConstant;
import edu.ecnu.woodpecker.constant.LogLevelConstant;
import edu.ecnu.woodpecker.environment.CedarCompileInfo;
import edu.ecnu.woodpecker.environment.CedarEnvirOperation;
import edu.ecnu.woodpecker.executor.Executor;
import edu.ecnu.woodpecker.executor.Parser;
import edu.ecnu.woodpecker.log.WpLog;
import edu.ecnu.woodpecker.systemfunction.CedarSystemOperator;

/**
 * Cedar
 */
public class CedarOperation extends TestController implements DatabaseOperation
{
    /**
     * Cedar database's user name and password
     */
    private static String databaseUser = null;
    private static String databasePassword = null;
    
    private CedarOperation()
    {}

    private static class SingletonHolder
    {
        private static CedarOperation instance = new CedarOperation();
    }

    public static CedarOperation getInstance()
    {
        return SingletonHolder.instance;
    }

    @Override
    public void enter(CommandLine line)
    {
        // �༭ģʽ�ͱ���ѡ��ֻ��Cedar��Ч
        boolean editMode = line.hasOption(CLIParameterConstant.EDIT_MODE) ? true : false;
        boolean needCompile = line.hasOption(CLIParameterConstant.COMPILE_CEDAR) ? true : false;
        // ��MySQL��Cedar����Ч
        boolean needDeploy = line.hasOption(CLIParameterConstant.DEPLOY) ? true : false;
        try
        {
            initialize(null);
        }
        catch (Exception e)
        {
            WpLog.recordLog(LogLevelConstant.ERROR, "initialize fail, exception: %s", WpLog.getExceptionInfo(e));
        }
        handleOnlyCommandLine(line);
        if (needCompile && !editMode)
        {
            if (!compileCedar())
            {
                // ����ʧ�ܣ���¼��־�������˴ο�ܵ�����
                WpLog.recordLog(LogLevelConstant.ERROR, "Compile Cedar unsuccessfully and stop Woodpecker");
                exit(1);
            }
        }

        boolean disableRestart = line.hasOption(CLIParameterConstant.DISABLE_RESTART) ? true : false;
        // ���ÿ�������Ĳ��԰�������ִ��
        for (String group : groupSet)
        {
            TestController.currentGroup = group;
            if (editMode || !needDeploy)
            {
                if (editMode)
                {
                    // �༭ģʽ�³�ʼ��deploy�������ļ�
                    CedarEnvirOperation.initializeCluster(testEnvironmentConfigPath + FileConstant.CEDAR_COMPILE_FILE_NAME,
                            testEnvironmentConfigPath + group + FileConstant.CONFIG_FILE_SUFFIX, FileConstant.UTF_8);
                }
                else if (!needDeploy)
                {
                    // ��һ�����в�����������Ⱥ��Ĭ�ϴ�ʱ�Ѿ��������ˣ����ʺ����ڱ�дCedar���԰���ʱ
                    CedarEnvirOperation.readCompileConf(testEnvironmentConfigPath + group + FileConstant.CONFIG_FILE_SUFFIX, FileConstant.UTF_8);
                }
            }
            // ���������������Ⱥ
            if (needDeploy && !editMode && !deployCedar(group))
            {
                // ����ʧ�ܣ���¼��־�������˴ο�ܵ����У���Ϊ�Ѿ�������ݺ���־��
                WpLog.recordLog(LogLevelConstant.ERROR, "Deploy Cedar unsuccessfully and stop Woodpecker");
                exit(1);
            }
            WpLog.recordTestflow("%s/%s", testEnvironmentConfigPath, group);
            if (needDeploy && !editMode && !startCedar())
            {
                // ����ʧ�ܣ���¼��־�������˴ο�ܵ����У���Ϊ�Ѿ�������ݺ���־��
                WpLog.recordLog(LogLevelConstant.ERROR, "Start Cedar unsuccessfully and stop Woodpecker");
                exit(1);
            }
            needDeploy = true;

            // ��ÿ������ڵĲ��԰�������ִ�У���¼��־
            File[] caseFiles = new File(testCasePath + group).listFiles();
            for (File caseFile : caseFiles)
            {
                WpLog.recordTestflow("caseid:%s/%s", group, caseFile.getName());
                boolean retrySucceed = false;
                File midResult = Parser.parse(caseFile);
                if (midResult == null)
                {
                    // ����ʧ�ܣ���¼��־���˰�������
                    WpLog.recordLog(LogLevelConstant.ERROR,
                            "%s parse unsuccessfully and skip", caseFile.getName());
                    continue;
                }

                boolean isPass = Executor.execute(midResult, Parser.getVarValueMap(), Parser.getVarTypeMap());
                if (!isPass)
                {
                    // �״�����ûͨ������¼��־
                    WpLog.recordLog(LogLevelConstant.ERROR,
                            "%s execute unsuccessfully first time", caseFile.getName());
                    // ����retryCount��
                    for (int i = 0; i < retryCount; i++)
                    {
                        if (!disableRestart)
                        {
                            // ɱ��Ⱥ�������ϴ�����ʧ�ܲ��������ݣ�ת��Cedar��־������������Ⱥ
//                            closeCedar(TestStatusConstant.FAIL);
//                            startCedar();
                        }
                        // ����ִ��
                        WpLog.recordTestflow("caseid:%s/%s", group, caseFile.getName());
                        isPass = Executor.execute(midResult, Parser.getVarValueMap(), Parser.getVarTypeMap());
                        if (isPass)
                        {
                            // ��¼��־����i+1�����Գɹ�
                            WpLog.recordLog(LogLevelConstant.INFO, "%s execute successfully after retry %d times", caseFile.getName(),
                                    i + 1);
                            retrySucceed = true;
                            break;
                        }
                    }

                    if (!retrySucceed)
                    {
                        // ��¼��־������retryCount������û��ͨ����������case��
                        WpLog.recordLog(LogLevelConstant.ERROR,
                                "%s execute unsuccessfully after retry %d times", caseFile.getName(), retryCount);
                        if (!disableRestart)
                        {
                            // ɱ��Ⱥ�������ݣ�ת��Cedar��־��������Ⱥ
//                            closeCedar(TestStatusConstant.FAIL);
//                            startCedar();
                        }
                        continue;
                    }
                }
                else
                {
                    // ��¼��־����caseFile����ͨ��
                    WpLog.recordLog(LogLevelConstant.INFO,
                            "%s execute successfully first time", caseFile.getName());
                }// end of if(!isPass)

                if (!disableRestart)
                {
                    // һ������ִ�н������жϼ�Ⱥ�Ƿ���������������ɱ��Ⱥ�������ݡ�ת��Cedar��־��������Ⱥ�������򲻱�
//                    int clusterState = CedarSystemOperator.isClusterAvailable();
                    int clusterState =0;
//                    if (clusterState != 0)
//                    {
//                        // ��Ⱥ������
//                        closeCedar(TestStatusConstant.FAIL);
//                        startCedar();
//                    }
                }
            }// end of for (File caseFile : caseFiles)

            if (!editMode)
            {
                // һ��case������ɱ��Ⱥ�������ݣ�����־
                // closeCedar(TestStatusConstant.PASS);
            }

        }// end of for (String group : groupSet)

        WpLog.recordLog(LogLevelConstant.INFO, "Framework has executed all test cases and prepares to stop");
        // ��ʼ�������ɱ���
        WpLog.generateReport();
        WpLog.generateStressReport();
        if (!editMode)
        {
            // Operator.initializeCEDAR("deploy");
        }
    }

    @Override
    public void initialize(String configFilePath)
    {
        databaseUser = null == CedarCompileInfo.getDatabaseUser() ? ConfigConstant.ADMIN_NAME : CedarCompileInfo.getDatabaseUser();
        databasePassword = null == CedarCompileInfo.getDatabasePassword() ? ConfigConstant.ADMIN_PASSWORD : CedarCompileInfo.getDatabasePassword();
    }

    /**
     * ���룬���ʧ��������retryCount�Σ���ʧ���򷵻�false
     * 
     * @return ����ʧ�ܷ���false
     */
    private static boolean compileCedar()
    {
        WpLog.recordLog(LogLevelConstant.INFO, "Start to compile Cedar");
        // �״α���
        boolean isSuccessful = CedarEnvirOperation.compileCEDAR();
        if (!isSuccessful)
        {
            // ���ԣ���¼��־
            WpLog.recordLog(LogLevelConstant.ERROR, "Compile Cedar unsuccessfully first time");
            for (int i = 0; i < retryCount; i++)
            {
                isSuccessful = CedarEnvirOperation.compileCEDAR();
                if (isSuccessful)
                {
                    WpLog.recordLog(LogLevelConstant.INFO,
                            "Compile Cedar successfully after retry %d times", i + 1);
                    return true;
                }
            }

            WpLog.recordLog(LogLevelConstant.ERROR,
                    "Compile Cedar unsuccessfully after retry %d times", retryCount);
            return false;
        }
        WpLog.recordLog(LogLevelConstant.INFO, "Compile Cedar successfully");
        return true;
    }

    /**
     * ����Cedar��Ⱥ�����ʧ��������retryCount�Σ���ʧ���򷵻�false
     * 
     * @param groupName ÿ��������������
     * @return ����ɹ�Ϊtrue
     */
    private static boolean deployCedar(String groupName)
    {
        WpLog.recordLog(LogLevelConstant.INFO, "Start to deploy Cedar");
        String deployConfigFilePath = testEnvironmentConfigPath + groupName + FileConstant.CONFIG_FILE_SUFFIX;
        boolean isSuccessful = CedarEnvirOperation.deployCEDAR(deployConfigFilePath, FileConstant.UTF_8);
        if (!isSuccessful)
        {
            // ����
            WpLog.recordLog(LogLevelConstant.ERROR, "Deploy Cedar unsuccessfully first time");
            for (int i = 0; i < retryCount; i++)
            {
                isSuccessful = CedarEnvirOperation.deployCEDAR(deployConfigFilePath, FileConstant.UTF_8);
                if (isSuccessful)
                {
                    WpLog.recordLog(LogLevelConstant.INFO,
                            "Deploy Cedar successfully after retry %d times", i + 1);
                    return true;
                }
            }
            WpLog.recordLog(LogLevelConstant.ERROR,
                    "Deploy Cedar unsuccessfully after retry %d times", retryCount);
            return false;
        }
        WpLog.recordLog(LogLevelConstant.INFO, "Deploy Cedar successfully");
        return true;
    }

    /**
     * ����Cedar��Ⱥ�����ʧ��������retryCount�Σ���ʧ���򷵻�false
     * 
     * @return �����ɹ�Ϊtrue
     */
    private static boolean startCedar()
    {
        boolean isSuccessful = CedarEnvirOperation.startCEDAR();
        if (!isSuccessful)
        {
            // ����
            WpLog.recordLog(LogLevelConstant.ERROR, "Start Cedar unsuccessfully first time");
            for (int i = 0; i < retryCount; i++)
            {
                isSuccessful = CedarEnvirOperation.startCEDAR();
                if (isSuccessful)
                {
                    WpLog.recordLog(LogLevelConstant.INFO,
                            "Start Cedar successfully after retry %d times", i + 1);
                    return true;
                }
            }

            WpLog.recordLog(LogLevelConstant.ERROR,
                    "Start Cedar unsuccessfully after retry %d times", retryCount);
            return false;
        }

        WpLog.recordLog(LogLevelConstant.INFO, "Start Cedar successfully");
        return true;
    }

    /**
     * �ر�Cedar��Ⱥ�����ʧ��������retryCount�Σ���ʧ���򷵻�false
     * 
     * @param type ��Ⱥ�ر����ͣ�normal/unnormal
     * @return �رճɹ�Ϊtrue
     */
    private static boolean closeCedar(String type)
    {
        boolean isSuccessful = CedarEnvirOperation.closeCEDAR(type);
        if (!isSuccessful)
        {
            // ����
            WpLog.recordLog(LogLevelConstant.ERROR, "Close Cedar unsuccessfully first time");
            for (int i = 0; i < retryCount; i++)
            {
                isSuccessful = CedarEnvirOperation.closeCEDAR(type);
                if (isSuccessful)
                {
                    WpLog.recordLog(LogLevelConstant.INFO,
                            "Close Cedar successfully after retry %d times", i + 1);
                    return true;
                }
            }

            WpLog.recordLog(LogLevelConstant.ERROR,
                    "Close Cedar unsuccessfully after retry %d times", retryCount);
            return false;
        }

        WpLog.recordLog(LogLevelConstant.INFO, "Close Cedar successfully");
        return true;
    }

    /**
     * handle -compile_cedar_only and -deploy_only command
     * 
     * @param line
     */
    private static void handleOnlyCommandLine(CommandLine line)
    {
        if (line.hasOption(CLIParameterConstant.COMPILE_CEDAR_ONLY))
        {
            if (!compileCedar())
            {
                // compile unsuccessfully and exit framework
                WpLog.recordLog(LogLevelConstant.ERROR, "Compile Cedar unsuccessfully and stop Woodpecker");
                exit(1);
            }
            WpLog.recordLog(LogLevelConstant.INFO, "Compile Cedar successfully");
            if (!line.hasOption(CLIParameterConstant.DEPLOY_ONLY))
                exit(0);
        }
        if (line.hasOption(CLIParameterConstant.DEPLOY_ONLY))
        {
            String group = groupSet.iterator().next(); // only deploy first group
            if (!deployCedar(group) || !startCedar())
            {
                // deploy unsuccessfully and exit framework
                WpLog.recordLog(LogLevelConstant.ERROR, "Deploy Cedar unsuccessfully and stop Woodpecker");
                exit(1);
            }
            WpLog.recordLog(LogLevelConstant.INFO, "Deploy Cedar successfully");
            exit(0);
        }
    }

    public static void setDatabaseUser(String userName)
    {
        CedarOperation.databaseUser = userName;
    }

    public static void setDatabasePassword(String password)
    {
        CedarOperation.databasePassword = password;
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

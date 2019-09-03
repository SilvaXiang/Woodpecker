package edu.ecnu.woodpecker.environment;

public class CedarCompileInfo
{
    /**
     * ���ݿ��û���������
     */
    private static String databaseUser = null;
    private static String databasePassword = null;

    /**
     * �������û���������
     */
    private static String userName = null;
    private static String password = null;

    /**
     * ��Ҫ�����Դ������IP��ַ
     */
    private static String srcIP = null;

    /**
     * Դ�������ļ����ڵ�ַIP
     */
    private static String makeIP = null;

    /**
     * ��Ҫ�����Դ������·������home/userName�����·����
     */
    private static String srcPath = null;

    /**
     * Դ�������ļ�����·������home/userName�����·����
     */
    private static String makePath = null;

    /**
     * ����ʹ�õ���CPU������
     */
    private static int core;

    /**
     * �Ƿ����CEDAR��tools�ļ��У�ֵ��true��false���֣�true��ʾ����
     */
    private static boolean compileTools;
    /**
     * ���Ӷ˿ں�
     */
    private static int connectionPort;

    // private static int port;

    public static String getDatabaseUser()
    {
        return databaseUser;
    }

    public static void setDatabaseUser(String databaseUser)
    {
        CedarCompileInfo.databaseUser = databaseUser;
    }

    public static String getDatabasePassword()
    {
        return databasePassword;
    }

    public static void setDatabasePassword(String databasePassword)
    {
        CedarCompileInfo.databasePassword = databasePassword;
    }

    public static String getSrcIP()
    {
        return srcIP;
    }

    public static void setSrcIP(String srcIP)
    {
        CedarCompileInfo.srcIP = srcIP;
    }

    public static String getMakeIP()
    {
        return makeIP;
    }

    public static void setMakeIP(String makeIP)
    {
        CedarCompileInfo.makeIP = makeIP;
    }

    public static String getSrcPath()
    {
        return srcPath;
    }

    public static void setSrcPath(String srcPath)
    {
        CedarCompileInfo.srcPath = srcPath;
    }

    public static String getMakePath()
    {
        return makePath;
    }

    public static void setMakePath(String makePath)
    {
        CedarCompileInfo.makePath = makePath;
    }

    public static int getCore()
    {
        return core;
    }

    public static void setCore(int core)
    {
        CedarCompileInfo.core = core;
    }

    public static boolean isCompileTools()
    {
        return compileTools;
    }

    public static void setCompileTools(boolean compileTools)
    {
        CedarCompileInfo.compileTools = compileTools;
    }

    public static int getConnectionPort()
    {
        return connectionPort;
    }

    public static void setConnectionPort(int connectionPort)
    {
        CedarCompileInfo.connectionPort = connectionPort;
    }

    public static String getUserName()
    {
        return userName;
    }

    public static void setUserName(String userName)
    {
        CedarCompileInfo.userName = userName;
    }

    public static String getPassword()
    {
        return password;
    }

    public static void setPassword(String password)
    {
        CedarCompileInfo.password = password;
    }

}

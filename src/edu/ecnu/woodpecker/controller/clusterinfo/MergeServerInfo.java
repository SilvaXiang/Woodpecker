package edu.ecnu.woodpecker.controller.clusterinfo;

/**
 * MergeServer��Ϣ��
 * 
 */
public class MergeServerInfo extends ServerInfo
{
    /**
     * MergeServer����˿�
     */
    private int servicePort;

    /**
     * MergeServer��MySQLЭ��˿�
     */
    private int MySQLPort;

    private boolean isListener;

    public MergeServerInfo()
    {
        // Ĭ�ϳ�ʼ�����˿ں��ǲ���С��0��
        this.servicePort = -1;
        this.MySQLPort = -1;
        this.isListener = false;
    }

    public MergeServerInfo(int servicePort, int MySQLPort, boolean isListener)
    {
        this.servicePort = servicePort;
        this.MySQLPort = MySQLPort;
        this.isListener = isListener;
    }

    public boolean isListener()
    {
        return isListener;
    }

    public void setListener(boolean isListener)
    {
        this.isListener = isListener;
    }

    public int getServicePort()
    {
        return servicePort;
    }

    public void setServicePort(int servicePort)
    {
        this.servicePort = servicePort;
    }

    public int getMySQLPort()
    {
        return MySQLPort;
    }

    public void setMySQLPort(int MySQLPort)
    {
        this.MySQLPort = MySQLPort;
    }
}
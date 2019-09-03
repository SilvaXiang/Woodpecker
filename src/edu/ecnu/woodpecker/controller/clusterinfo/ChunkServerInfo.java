package edu.ecnu.woodpecker.controller.clusterinfo;

/**
 * ChunkServer��Ϣ��
 * 
 */
public class ChunkServerInfo extends ServerInfo
{
    /**
     * ChunkServer����˿�
     */
    private int servicePort;

    /**
     * ChunkServer APP����
     */
    private String appName = null;

    public ChunkServerInfo()
    {
        servicePort = -1;
        appName = "";
    }

    /**
     * @param servicePort ����˿�
     * @param appName APP����
     */
    public ChunkServerInfo(int servicePort, String appName)
    {
        this.servicePort = servicePort;
        this.appName = appName;
    }

    public String getAppName()
    {
        return appName;
    }

    public void setAppName(String appName)
    {
        this.appName = appName;
    }

    public void setServicePort(int servicePort)
    {
        this.servicePort = servicePort;
    }

    public int getServicePort()
    {
        return this.servicePort;
    }
}

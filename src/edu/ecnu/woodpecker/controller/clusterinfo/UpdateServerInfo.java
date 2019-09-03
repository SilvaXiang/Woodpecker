package edu.ecnu.woodpecker.controller.clusterinfo;

/**
 * UpdateServer��Ϣ��
 * 
 */
public class UpdateServerInfo extends ServerInfo
{
    /**
     * UpdateServer����˿�
     */
    private int servicePort;

    /**
     * UpdateServer�ϲ��˿�
     */
    private int mergePort;

    public UpdateServerInfo()
    {
        servicePort = -1;
        mergePort = -1;
    }

    public UpdateServerInfo(int servicePort, int mergePort)
    {
        this.servicePort = servicePort;
        this.mergePort = mergePort;
    }

    public int getMergePort()
    {
        return mergePort;
    }

    public void setMergePort(int mergePort)
    {
        this.mergePort = mergePort;
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
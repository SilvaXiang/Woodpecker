package edu.ecnu.woodpecker.controller.clusterinfo;

/**
 * Server��Ϣ������
 * 
 */
public abstract class ServerInfo
{
//    /**
//     * �����ַ
//     */
//    private String deployPath = null;
    /**
     * ��server��IP��ַ
     */
    private String IP = null;

    /**
     * server�󶨵��������ƣ�Network Interface Card
     */
    private String NIC = null;

    /**
     * ��server�Ƿ�����
     */
    private boolean isDown;

    /**
     * ��server�Ƿ�������Ⱥ������Ϊtrue
     */
    // private boolean isMaster;

    /**
     * ��server��ǰ���ڼ�ȺRootServer��IP��ַ
     */
    private String curClusterRSIP = null;

    /**
     * ��server��ǰ���ڼ�ȺRootServer�Ķ˿�
     */
    private int curClusterRSPort;

    /**
     * ��server�Ľ��̺�
     */
    private int PID;

    public int getPID()
    {
        return PID;
    }

    public void setPID(int pID)
    {
        PID = pID;
    }

    public int getCurClusterRSPort()
    {
        return curClusterRSPort;
    }

    public void setCurClusterRSPort(int curClusterRSPort)
    {
        this.curClusterRSPort = curClusterRSPort;
    }

    public String getIP()
    {
        return IP;
    }

    public void setIP(String iP)
    {
        IP = iP;
    }

    public String getCurClusterRSIP()
    {
        return curClusterRSIP;
    }

    public void setCurClusterRSIP(String curClusterRSIP)
    {
        this.curClusterRSIP = curClusterRSIP;
    }

    public boolean isDown()
    {
        return this.isDown;
    }

    public void setIsDown(boolean isDown)
    {
        this.isDown = isDown;
    }

    // public boolean isMaster()
    // {
    // return this.isMaster;
    // }
    //
    // public void setIsMaster(boolean isMaster)
    // {
    // this.isMaster = isMaster;
    // }

    public String getNIC()
    {
        return NIC;
    }

    public void setNIC(String nIC)
    {
        NIC = nIC;
    }

//    public String getDeployPath()
//    {
//        return deployPath;
//    }
//
//    public void setDeployPath(String deployPath)
//    {
//        this.deployPath = deployPath;
//    }
}
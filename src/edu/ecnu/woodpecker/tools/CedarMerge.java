package edu.ecnu.woodpecker.tools;

import edu.ecnu.woodpecker.constant.FileConstant;
import edu.ecnu.woodpecker.controller.TestController;

/**
 * Woodpecker��������֮һ���ܹ���CEDAR����ÿ�պϲ�����ÿ�պϲ���ɺ�ʹ�ÿ���̨������Ѻϲ����
 * 
 */
public class CedarMerge implements Initializable
{
    /**
     * ������IP�����Ӷ˿�
     */
    private String IP = null;
    private int connectionPort;

    private String serverUserName = null;
    private String serverPassword = null;

    /**
     * CEDAR��Ŀ¼��UPS�ϲ���������˿ڡ�RS��ѯ�ϲ�״̬����˿�
     */
    private String cedarDirectory = null;
    private String UPSIP = null;
    private int UPSMergePort;
    private String RSIP = null;
    private int RSQueryPort;

    public CedarMerge()
    {
        connectionPort = 22;
        UPSMergePort = -1;
        RSQueryPort = -1;
    }

    /**
     * ����ÿ�պϲ�
     */
    private void merge()
    {
        System.out.println("Start to merge in " + IP);
        String cmd = cedarDirectory + "bin/ups_admin -a " + UPSIP + " -p " + UPSMergePort + " -t major_freeze";
        String result = edu.ecnu.woodpecker.util.Util.exec(IP, serverUserName, serverPassword, connectionPort, cmd);
        if (result.matches("\\s*\\[major_freeze]\\s*err=0\\s*"))
            System.out.println("-----------------------Major freeze succeed-----------------------");
        else
            System.out.println("Major freeze fail");
    }

    /**
     * ���ϲ�ѯ�Ƿ�ϲ����
     */
    private void isMergeDone()
    {
        String cmd = cedarDirectory + "bin/rs_admin -r " + RSIP + " -p " + RSQueryPort + " stat -o merge";
        String result = null;
        String[] parts = null;
        long startTime = System.currentTimeMillis();
        System.out.println("Wait to major freeze end");
        while (true)
        {
            result = edu.ecnu.woodpecker.util.Util.exec(IP, serverUserName, serverPassword, connectionPort, cmd);
            parts = result.split(FileConstant.WIN_LINE_FEED_STR);
            for (String ele : parts)
            {
                // System.out.println(ele);
                if (ele.matches("\\s*merge:\\s*DONE\\s*"))
                {
                    for (int i = 0; i < 20; i++)
                        System.out.println("------------------------Major freeze end------------------------");
                    System.out.println("Major freeze time: " + (System.currentTimeMillis() - startTime) / 1000 + " second");
                    return;
                }
            }
            try
            {
                Thread.sleep(3000);
            }
            catch (InterruptedException e)
            {
                e.printStackTrace();
            }
        }
    }

    public void start() throws Exception
    {
        TestController.initializeParameter();
        initialize(this, FileConstant.CEDAR_MERGE_CONFIG_PATH);
        merge();
        isMergeDone();
    }

    public void setIP(String IP)
    {
        this.IP = IP;
    }

    public void setServerUserName(String serverUserName)
    {
        this.serverUserName = serverUserName;
    }

    public void setServerPassword(String serverPassword)
    {
        this.serverPassword = serverPassword;
    }

    public void setCedarDirectory(String cedarDirectory)
    {
        this.cedarDirectory = cedarDirectory.charAt(cedarDirectory.length() - 1) != FileConstant.FILE_SEPARATOR_CHAR
                ? cedarDirectory + FileConstant.FILE_SEPARATOR_CHAR : cedarDirectory;
    }

    public void setUPSMergePort(String UPSMergePort)
    {
        this.UPSMergePort = Integer.parseInt(UPSMergePort);
    }

    public void setRSQueryPort(String RSQueryPort)
    {
        this.RSQueryPort = Integer.parseInt(RSQueryPort);
    }

    public void setUPSIP(String UPSIP)
    {
        this.UPSIP = UPSIP;
    }

    public void setRSIP(String RSIP)
    {
        this.RSIP = RSIP;
    }

    /**
     * ÿ�պϲ����ߵ����
     * 
     * @param args
     */
    public static void main(String[] args)
    {
        try
        {
            new CedarMerge().start();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

}

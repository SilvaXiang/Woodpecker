package edu.ecnu.woodpecker.systemfunction;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import edu.ecnu.woodpecker.constant.CedarConstant;
import edu.ecnu.woodpecker.constant.FileConstant;
import edu.ecnu.woodpecker.constant.LogLevelConstant;
import edu.ecnu.woodpecker.constant.ShellConstant;
import edu.ecnu.woodpecker.constant.SignConstant;
import edu.ecnu.woodpecker.controller.clusterinfo.Cluster;
import edu.ecnu.woodpecker.controller.clusterinfo.ClustersInfo;
import edu.ecnu.woodpecker.controller.clusterinfo.MergeServerInfo;
import edu.ecnu.woodpecker.log.Recorder;
import edu.ecnu.woodpecker.util.Log;
import edu.ecnu.woodpecker.util.Util;

public class CEDARCluster
{
    /**
     * �жϾ�̬�����Ƿ��ռ����
     * 
     * @return
     */
    public static boolean isGatherDone()
    {
        update();
        StringBuilder gather = new StringBuilder(CedarConstant.ISGATHERDOWN);
        int index = gather.indexOf("ip");
        gather.replace(index, index + 2, ClustersInfo.getMasterRS().getCurClusterRSIP());
        index = gather.indexOf("port");
        gather.replace(index, index + 4, String.valueOf(ClustersInfo.getMasterRS().getPort()));
        String command = ShellConstant.OPENDIR.replace("dirName", ClustersInfo.getDeployPath()
                + "/bin" + ";")
                + gather;
        // System.out.println(command);
        String result = Util.exec(ClustersInfo.getMasterUPS().getCurClusterRSIP(),
                ClustersInfo.getUserName(), ClustersInfo.getPassword(),
                ClustersInfo.getConnectionPort(), command);

        // System.out.println(result);
        for (String item : result.split(FileConstant.LINUX_LINE_FEED))
        {
            if (item.toUpperCase().equals("DONE"))
            {
                Recorder.FunctionRecord(Log.getRecordMetadata(), ClustersInfo.getMasterUPS()
                        .getCurClusterRSIP() + " gather statistics successfully",
                        LogLevelConstant.INFO);
                return true;
            }
        }
        Recorder.FunctionRecord(Log.getRecordMetadata(), ClustersInfo.getMasterUPS()
                .getCurClusterRSIP() + " gather statistics unsuccessfully", LogLevelConstant.ERROR);
        return false;
    }

    /**
     * ����̬�����ռ�
     * 
     * @return
     */
    public static boolean gatherStatistics()
    {
        update();
        StringBuilder gather = new StringBuilder(CedarConstant.GATHER);
        int index = gather.indexOf("ip");
        gather.replace(index, index + 2, ClustersInfo.getMasterRS().getCurClusterRSIP());
        index = gather.indexOf("port");
        gather.replace(index, index + 4, String.valueOf(ClustersInfo.getMasterRS().getPort()));
        String command = ShellConstant.OPENDIR.replace("dirName", ClustersInfo.getDeployPath()
                + "/bin" + ";")
                + gather;
        // System.out.println(command);
        String result = Util.exec(ClustersInfo.getMasterUPS().getCurClusterRSIP(),
                ClustersInfo.getUserName(), ClustersInfo.getPassword(),
                ClustersInfo.getConnectionPort(), command);

        // System.out.println(result);
        for (String item : result.split(FileConstant.LINUX_LINE_FEED))
        {
            if (item.toUpperCase().equals("OKAY"))
            {
                Recorder.FunctionRecord(Log.getRecordMetadata(), ClustersInfo.getMasterUPS()
                        .getCurClusterRSIP() + " send gather statistics request successfully",
                        LogLevelConstant.INFO);
                return true;
            }
        }
        Recorder.FunctionRecord(Log.getRecordMetadata(), ClustersInfo.getMasterUPS()
                .getCurClusterRSIP() + " send gather statistics request unsuccessfully",
                LogLevelConstant.ERROR);
        return false;
    }

    /**
     * ����С�ϲ�
     * 
     * @return
     */
    public static boolean minorMerge()
    {
        update();
        StringBuilder truncate = new StringBuilder(CedarConstant.TRUNCATE);
        int index = truncate.indexOf("ip");
        truncate.replace(index, index + 2, ClustersInfo.getMasterUPS().getCurClusterRSIP());
        index = truncate.indexOf("port");
        truncate.replace(index, index + 4,
                String.valueOf(ClustersInfo.getMasterUPS().getServicePort()));
        String command = ShellConstant.OPENDIR.replace("dirName", ClustersInfo.getDeployPath()
                + "/bin" + ";")
                + truncate;
        System.out.println(command);
        String result = Util.exec(ClustersInfo.getMasterUPS().getCurClusterRSIP(),
                ClustersInfo.getUserName(), ClustersInfo.getPassword(),
                ClustersInfo.getConnectionPort(), command);

        System.out.println(result);
        for (String item : result.split(FileConstant.LINUX_LINE_FEED))
        {
            if (item.equals("[major_freeze] err=0"))
            {
                Recorder.FunctionRecord(Log.getRecordMetadata(), ClustersInfo.getMasterUPS()
                        .getCurClusterRSIP() + " send merge request successfully",
                        LogLevelConstant.INFO);
                return true;
            }
        }
        Recorder.FunctionRecord(Log.getRecordMetadata(), ClustersInfo.getMasterUPS()
                .getCurClusterRSIP() + " send merge request unsuccessfully", LogLevelConstant.ERROR);
        return false;
    }

    /**
     * ����ÿ�պϲ�
     * 
     * @return ����ÿ�պϲ��Ƿ�ɹ�
     */
    public static boolean merge()
    {
        update();
        StringBuilder merge = new StringBuilder(CedarConstant.MERGE);
        int index = merge.indexOf("ip");
        merge.replace(index, index + 2, ClustersInfo.getMasterUPS().getCurClusterRSIP());
        index = merge.indexOf("port");
        merge.replace(index, index + 4,
                String.valueOf(ClustersInfo.getMasterUPS().getServicePort()));
        String command = ShellConstant.OPENDIR.replace("dirName", ClustersInfo.getDeployPath()
                + "/bin" + ";")
                + merge;
        // System.out.println(command);
        String result = Util.exec(ClustersInfo.getMasterUPS().getCurClusterRSIP(),
                ClustersInfo.getUserName(), ClustersInfo.getPassword(),
                ClustersInfo.getConnectionPort(), command);

        // System.out.println(result);
        for (String item : result.split(FileConstant.LINUX_LINE_FEED))
        {
            if (item.equals("[major_freeze] err=0"))
            {
                Recorder.FunctionRecord(Log.getRecordMetadata(), ClustersInfo.getMasterUPS()
                        .getCurClusterRSIP() + " send merge request successfully",
                        LogLevelConstant.INFO);
                return true;
            }
        }
        Recorder.FunctionRecord(Log.getRecordMetadata(), ClustersInfo.getMasterUPS()
                .getCurClusterRSIP() + " send merge request unsuccessfully", LogLevelConstant.ERROR);
        return false;
    }

    /**
     * ������Ⱥ��������ѡ��������л���ѡ�١� ����ѡ�ٺ󷵻ز���ȴ�ѡ����ɡ�
     * 
     * @return ��������ѡ���Ƿ�ɹ�
     */
    public static boolean reelect()
    {
        try
        {
            Thread.sleep(1000);
        }
        catch (InterruptedException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        update();
        StringBuilder reelect = new StringBuilder(CedarConstant.REELECT);
        int index = reelect.indexOf("ip");
        reelect.replace(index, index + 2, ClustersInfo.getMasterRS().getCurClusterRSIP());
        index = reelect.indexOf("port");
        reelect.replace(index, index + 4,
                String.valueOf(ClustersInfo.getMasterRS().getCurClusterRSPort()));
        String command = ShellConstant.OPENDIR.replace("dirName", ClustersInfo.getDeployPath()
                + "/bin" + ";")
                + reelect;
        // System.out.println(command);
        String result = Util.exec(ClustersInfo.getMasterRS().getCurClusterRSIP(),
                ClustersInfo.getUserName(), ClustersInfo.getPassword(),
                ClustersInfo.getConnectionPort(), command);

        // System.out.println(result);
        for (String item : result.split(FileConstant.LINUX_LINE_FEED))
        {
            if (item.toUpperCase().equals("OKAY"))
            {
                Recorder.FunctionRecord(Log.getRecordMetadata(), ClustersInfo.getMasterRS()
                        .getCurClusterRSIP() + " send reelect request successfully",
                        LogLevelConstant.INFO);
                return true;
            }
        }

        Recorder.FunctionRecord(Log.getRecordMetadata(), ClustersInfo.getMasterRS()
                .getCurClusterRSIP() + " send reelect request unsuccessfully",
                LogLevelConstant.ERROR);
        return false;
    }

    /**
     * ��ʾ��Ⱥ�ϲ��Ƿ���ɣ�����ؽ��Ϊ��������
     * 
     * @return 0�������м�Ⱥ�ϲ���� 1������һ������Ⱥδ�ϲ���ɣ����༯Ⱥ�ϲ��� 2������������Ⱥδ�ϲ���ɣ����༯Ⱥ�ϲ���
     *         3��������Ⱥδ�ϲ���ɣ����౸��Ⱥ�ϲ��� 4����һ������Ⱥ�ϲ���ɣ����༯Ⱥδ�ϲ���� 5�������м�Ⱥ��δ�ϲ���ɡ�
     */
    public static Integer isMergeDown()
    {
        update();
        Map<Integer, String> RSList = ClustersInfo.getRSList();
        boolean main = false, slave1 = false, slave2 = false;
        String command = null;

        for (Iterator<Integer> iter = RSList.keySet().iterator(); iter.hasNext();)
        {
            int clusterID = (Integer) iter.next();
            String ip = RSList.get(clusterID);
            int clusterRole = ClustersInfo.getCluster(clusterID).getClusterRole();

            StringBuilder merge = new StringBuilder(CedarConstant.ISMERGEDOWN);
            int index = merge.indexOf("ip");
            merge.replace(index, index + 2, ip);
            index = merge.indexOf("port");
            merge.replace(index, index + 4,
                    String.valueOf(ClustersInfo.getMasterRS().getCurClusterRSPort()));

            command = ShellConstant.OPENDIR.replace("dirName", ClustersInfo.getDeployPath()
                    + "/bin" + ";")
                    + merge;

            // System.out.println(command);
            String result = Util.exec(ip, ClustersInfo.getUserName(), ClustersInfo.getPassword(),
                    ClustersInfo.getConnectionPort(), command);

            // System.out.println(result);
            for (String item : result.split(FileConstant.LINUX_LINE_FEED))
            {
                if (item.split(SignConstant.COLON_STR).length > 1)
                {
                    if (item.split(SignConstant.COLON_STR)[1].trim().toUpperCase().equals("DONE"))
                    {
                        if (clusterRole == 1)
                        {
                            main = true;
                        }
                        else if (clusterRole == -1 && slave1)
                        {
                            slave2 = true;
                        }
                        else if (clusterRole == -1 && !slave1)
                        {
                            slave1 = true;
                        }
                    }
                }
            }
        }
        if (RSList.size() == 1 && main)
        {
            // System.out.println("ÿ�պϲ����");
            Recorder.FunctionRecord(Log.getRecordMetadata(), " merge down", LogLevelConstant.INFO);
            return 0;
        }
        else if (main && slave1 && slave2)
        {
            // System.out.println("ÿ�պϲ����");
            Recorder.FunctionRecord(Log.getRecordMetadata(), " merge down", LogLevelConstant.INFO);
            return 0;
        }
        else if (main && (slave1 || slave2))
        {
            // System.out.println("����Ⱥÿ�պϲ���ɣ���һ������Ⱥÿ�պϲ����");
            Recorder.FunctionRecord(Log.getRecordMetadata(),
                    "master merge down , one slave merge down", LogLevelConstant.INFO);
            return 1;
        }
        else if (main && !slave1 && !slave2)
        {
            // System.out.println("ֻ������Ⱥÿ�պϲ����");
            Recorder.FunctionRecord(Log.getRecordMetadata(), "only master merge down",
                    LogLevelConstant.INFO);
            return 2;
        }
        else if (!main && slave1 && slave2)
        {
            // System.out.println("����Ⱥÿ�պϲ���ɣ�����Ⱥδ���");
            Recorder.FunctionRecord(Log.getRecordMetadata(), "only two slave merge down",
                    LogLevelConstant.INFO);
            return 3;
        }
        else if (!main && (slave1 || slave2))
        {
            // System.out.println("ֻ��һ������Ⱥÿ�պϲ����");
            Recorder.FunctionRecord(Log.getRecordMetadata(), "only one slave merge down",
                    LogLevelConstant.INFO);
            return 4;
        }
        else if (!main && !slave1 && !slave2)
        {
            // System.out.println("ÿ�պϲ�δ���");
            Recorder.FunctionRecord(Log.getRecordMetadata(), "no one merge down",
                    LogLevelConstant.INFO);
            return 5;
        }
        if (RSList.size() == 1 && !main)
        {
            // System.out.println("ÿ�պϲ�δ���");
            Recorder.FunctionRecord(Log.getRecordMetadata(), "no one merge down",
                    LogLevelConstant.INFO);
            return 5;
        }
        // System.out.println("ÿ�պϲ�δ���");
        return -1;
    }

    /**
     * ��ʾ��n���ڵȴ����м�Ⱥ�ϲ���ɣ�����ָ��ʱ�����м�Ⱥδ�ϲ�����򷵻ش��󣬱�ʾ��caseʧ�ܡ�
     * 
     * @param time ָ��ʱ��n��
     * @return ��ȷ�����
     */
    public static boolean awaitMergeDoneOld(int time)
    {
        Shell.sleep(time, "second");
        int num = isMergeDown();
        if (num == 0)
        {
            // System.out.println("�ȴ�" + time + "��ÿ�պϲ����");
            Recorder.FunctionRecord(Log.getRecordMetadata(), "merge down after waiting " + time
                    + " second ", LogLevelConstant.INFO);
            return true;
        }
        // System.out.println("�ȴ�" + time + "��ÿ�պϲ�δ���");
        Recorder.FunctionRecord(Log.getRecordMetadata(), "merge unfinished after waiting " + time
                + " second ", LogLevelConstant.INFO);
        return false;
 
    }

    public static boolean awaitMergeDone(int time)
    {
        long starttime=System.currentTimeMillis();
        while(isMergeDown()!=0)
        {
            if((System.currentTimeMillis()-starttime)/1000 > (long)time)
            {
                Recorder.FunctionRecord(Log.getRecordMetadata(), "merge unfinished after waiting " + time
                        + " second ", LogLevelConstant.INFO);
                return false;
            }
        }
        Recorder.FunctionRecord(Log.getRecordMetadata(), "merge down after waiting " + (System.currentTimeMillis()-starttime)/1000
                + " second ", LogLevelConstant.INFO);
        return true;
        
        
        
       
    }
    /**
     * ��ʾ��ǰ��Ⱥ�Ƿ�����
     * 
     * @return
     */
    public static boolean existMaster()
    {
        update();

        return ClustersInfo.existMaster();
    }

    /**
     * ���ݼ�Ⱥ״������ȫ�ֶ���
     */
    public static void update()
    {
        String command = null;
        Map<Integer, String> RSList = ClustersInfo.getRSList();
        Map<Integer, String> serverList = new HashMap<Integer, String>();

        // ��ѯ��Ⱥ��Ϣ
        for (Iterator<Integer> iter = RSList.keySet().iterator(); iter.hasNext();)
        {
            int clusterID = (Integer) iter.next();
            String ip = RSList.get(clusterID);
            StringBuilder getrole = new StringBuilder(CedarConstant.GETROLE);
            int index = getrole.indexOf("ip");
            getrole.replace(index, index + 2, ip);
            index = getrole.indexOf("port");
            getrole.replace(index, index + 4,
                    String.valueOf(ClustersInfo.getMasterRS().getCurClusterRSPort()));
            command = ShellConstant.OPENDIR.replace("dirName", ClustersInfo.getDeployPath()
                    + "/bin" + ";")
                    + getrole;

            // System.out.println("aa" + command);
            String result = Util.exec(ip, ClustersInfo.getUserName(), ClustersInfo.getPassword(),
                    ClustersInfo.getConnectionPort(), command);
            for (String item : result.split(FileConstant.LINUX_LINE_FEED))
            {
                if (item.split(SignConstant.ASSIGNMENT_STR).length > 1)
                {
                    String type = item.split(SignConstant.ASSIGNMENT_STR)[1].trim().toUpperCase();
                    if (type.equals("MASTER") || type.equals("SLAVE") || type.equals("INIT"))
                    {
                        Recorder.FunctionRecord(Log.getRecordMetadata(), ip + " is " + type,
                                LogLevelConstant.INFO);

                        serverList.put(clusterID, type);
                    }
                }
            }
        }

        // ���ݼ�Ⱥ��Ϣ ����ȫ�ֶ���
        for (Iterator<Integer> iter = serverList.keySet().iterator(); iter.hasNext();)
        {
            int clusterID = (Integer) iter.next();
            String type = serverList.get(clusterID);
            Cluster cluster = ClustersInfo.getCluster(clusterID);
            if (type.equals("MASTER"))
            {
                cluster.setClusterRole(1);
            }
            else if (type.equals("SLAVE"))
            {
                cluster.setClusterRole(-1);
            }
            else if (type.equals("INIT"))
            {
                cluster.setClusterRole(0);
            }
        }
        // ClustersInfo.systemaa();
    }

    /**
     * ����kill_server���ص��ַ�����Ϣ�����ø�server���ڼ�ȺΪ����Ⱥ
     * 
     * @param arg kill_server���ص��ַ�����Ϣ
     * @return �����Ƿ�ɹ�
     */
    public static boolean setMaster(String arg)
    {
        if (arg == "")
        {
            Recorder.FunctionRecord(Log.getRecordMetadata(),
                    "The input string is empty, set master unsuccessfully", LogLevelConstant.ERROR);
            return false;
        }

        boolean isExistMaster = existMaster();
        if (isExistMaster)
        {
            Recorder.FunctionRecord(Log.getRecordMetadata(),
                    "The cluster has master, set master unsuccessfully", LogLevelConstant.ERROR);

            return false;
        }

        String command = null;

        String RSip = arg.split(SignConstant.COMMA_STR)[1];
        String RSport = arg.split(SignConstant.COMMA_STR)[2];

        StringBuilder setmaster = new StringBuilder(CedarConstant.SETMASTER);
        int index = setmaster.indexOf("ip");
        setmaster.replace(index, index + 2, RSip);
        index = setmaster.indexOf("port");
        setmaster.replace(index, index + 4, RSport);
        command = ShellConstant.OPENDIR.replace("dirName", arg.split(SignConstant.COMMA_STR)[5]
                + "/bin" + ";")
                + setmaster;

        String result = Util.exec(RSip, ClustersInfo.getUserName(), ClustersInfo.getPassword(),
                ClustersInfo.getConnectionPort(), command);
        System.out.println(command);
        for (String item : result.split(FileConstant.LINUX_LINE_FEED))
        {
            if (item.toUpperCase().equals("OKAY"))
            {
                Recorder.FunctionRecord(Log.getRecordMetadata(),
                        "send set master request successfully", LogLevelConstant.INFO);
            }
        }

        Shell.sleep(1, "minute");
        StringBuilder getrole = new StringBuilder(CedarConstant.GETROLE);
        index = getrole.indexOf("ip");
        getrole.replace(index, index + 2, RSip);
        index = getrole.indexOf("port");
        getrole.replace(index, index + 4, RSport);
        command = ShellConstant.OPENDIR.replace("dirName", ClustersInfo.getDeployPath() + "/bin"
                + ";")
                + getrole;

        System.out.println(command);
        result = Util.exec(RSip, ClustersInfo.getUserName(), ClustersInfo.getPassword(),
                ClustersInfo.getConnectionPort(), command);
        Recorder.FunctionRecord(Log.getRecordMetadata(), "select " + RSip + "whether the master",
                LogLevelConstant.INFO);

        System.out.println(result);
        for (String item : result.split(FileConstant.LINUX_LINE_FEED))
        {
            if (item.split(SignConstant.ASSIGNMENT_STR).length > 1)
            {
                if (item.split(SignConstant.ASSIGNMENT_STR)[1].trim().toUpperCase()
                        .equals("MASTER"))
                {
                    // ����ȫ�ֶ���
                    update(RSip);
                    Recorder.FunctionRecord(Log.getRecordMetadata(), RSip
                            + " is master,set master successfully", LogLevelConstant.INFO);

                    return true;
                }
            }
        }

        Recorder.FunctionRecord(Log.getRecordMetadata(), RSip + " set master unsuccessfully",
                LogLevelConstant.ERROR);
        return false;
    }

    /**
     * ��Ⱥ�Ƿ�ɷ�������ؽ��Ϊ�������� 0�������м�Ⱥ�ɷ��� 1������һ������Ⱥ���ɷ������༯Ⱥ�ɷ��� 2������������Ⱥ���ɷ������༯Ⱥ�ɷ���
     * 3��������Ⱥ���ɷ��񣬱���Ⱥ���ɷ��� 4����һ������Ⱥ�ɷ������༯Ⱥ���ɷ��� 5�������м�Ⱥ���ɷ���
     * 
     * @return
     */
    // TODO 34 ���ڶ༯Ⱥ�������ɷ��񼴷���5
    public static Integer isClusterAvailable()
    {
        update();
        // int num = ClustersInfo.getRSList().size();
        boolean isMasterAvailable = false;

        boolean createTableAvailable = false;
        boolean deleteTableAvailable = false;
        boolean masterUpdateAvailable = false;
        Map<Integer, String> rsList = ClustersInfo.getRSList();
        int rsNum = rsList.size();

        int slaveAvailable = 0;
        if (rsNum == 1)
        {
            // ����
            MergeServerInfo ms = ClustersInfo.getAliveMasterMS();
            if (ms == null)
            {
                Recorder.FunctionRecord(Log.getRecordMetadata(), "the master has no alive ms",
                        LogLevelConstant.INFO);

                return 5;
            }
            else
            {
                createTableAvailable = Shell.isCreateTable(ms.getIP(),
                        String.valueOf(ms.getMySQLPort()));
                // ����Ⱥ����
                masterUpdateAvailable = Shell.isUpdateTable(ms.getIP(),
                        String.valueOf(ms.getMySQLPort()));
                deleteTableAvailable = Shell.isDeleteTable(ms.getIP(),
                        String.valueOf(ms.getMySQLPort()));
            }
            if (createTableAvailable && deleteTableAvailable && masterUpdateAvailable)
            {
                Recorder.FunctionRecord(Log.getRecordMetadata(), "cluster is available",
                        LogLevelConstant.INFO);
                return 0;
            }
            else
            {
                return 5;
            }
        }
        else
        {
            MergeServerInfo ms = null;
            for (Iterator<Integer> iter = rsList.keySet().iterator(); iter.hasNext();)
            {
                int clusterID = (Integer) iter.next();
                Cluster cluster = ClustersInfo.getCluster(clusterID);
                int role = cluster.getClusterRole();

                if (role == 1)
                {
                    // ����
                    ms = cluster.getAliveRandomMS();
                    if (ms == null)
                    {
                        Recorder.FunctionRecord(Log.getRecordMetadata(),
                                "the master has no alive ms", LogLevelConstant.INFO);
                        return 5;
                    }
                    else
                    {
                        createTableAvailable = Shell.isCreateTable(ms.getIP(),
                                String.valueOf(ms.getMySQLPort()));
                        // ����Ⱥ����
                        masterUpdateAvailable = Shell.isUpdateTable(ms.getIP(),
                                String.valueOf(ms.getMySQLPort()));
                        CedarSystemOperator.sleep(3, "second");
                    }
                }
            }
            for (Iterator<Integer> iter = rsList.keySet().iterator(); iter.hasNext();)
            {
                int clusterID = (Integer) iter.next();
                Cluster cluster = ClustersInfo.getCluster(clusterID);
                int role = cluster.getClusterRole();

                if (role == -1)
                {

                    MergeServerInfo ms1 = cluster.getAliveRandomMS();
                    if (ms1 == null)
                    {
                        Recorder.FunctionRecord(Log.getRecordMetadata(),
                                "the slave has no alive ms", LogLevelConstant.INFO);
                    }
                    if (Shell.isUpdateTable(ms1.getIP(), String.valueOf(ms1.getMySQLPort())))
                    {
                        slaveAvailable++;
                    }
                }
            }
            if (ms != null)
            {
                deleteTableAvailable = Shell.isDeleteTable(ms.getIP(),
                        String.valueOf(ms.getMySQLPort()));
            }
            if (deleteTableAvailable && masterUpdateAvailable)
            {
                isMasterAvailable = true;
            }
            if (isMasterAvailable && slaveAvailable == rsNum - 1)
            {
                Recorder.FunctionRecord(Log.getRecordMetadata(), "clusters are all avaliable",
                        LogLevelConstant.INFO);
                return 0;
            }
            else if (isMasterAvailable && slaveAvailable == rsNum - 2)
            {
                Recorder.FunctionRecord(Log.getRecordMetadata(), "only one slave is not avaliable",
                        LogLevelConstant.INFO);
                return 1;
            }
            else if (isMasterAvailable && slaveAvailable == rsNum - 3)
            {
                Recorder.FunctionRecord(Log.getRecordMetadata(), "only two slave is not avaliable",
                        LogLevelConstant.INFO);
                return 2;
            }
            // else if (!isMasterAvailable && slaveAvailable
            // == rsNum - 1)
            // {
            // Recorder.FunctionRecord(Log.getRecordMetadata(),
            // " only master is not avaliable",
            // LogLevel.INFO);
            // return 3;
            // }
            // else if (!isMasterAvailable && slaveAvailable
            // == 1)
            // {
            // Recorder.FunctionRecord(Log.getRecordMetadata(),
            // " only one slave is avaliable",
            // LogLevel.INFO);
            // return 4;
            // }
            else if (!isMasterAvailable && slaveAvailable == 0)
            {
                Recorder.FunctionRecord(Log.getRecordMetadata(), "no one cluster is  avaliable",
                        LogLevelConstant.INFO);
                return 5;
            }
        }

        return -1;
    }

    /**
     * ��ʾ��n���ڵȴ����м�Ⱥ�ɷ��񣬳���ָ��ʱ�����м�Ⱥ���ɷ����򷵻ش��󣬱�ʾ��caseʧ��
     * 
     * @param time ָ��ʱ��n��
     * @return
     */
    public static boolean awaitAvailable(int time)
    {
        Shell.sleep(time, "second");
        if (isClusterAvailable() == 0)
        {
            Recorder.FunctionRecord(Log.getRecordMetadata(), "the cluster is available after "
                    + time + " second", LogLevelConstant.INFO);

            return true;
        }

        Recorder.FunctionRecord(Log.getRecordMetadata(), "the cluster is not available after "
                + time + " second", LogLevelConstant.INFO);
        return false;
    }

    /**
     * ���������������ȫ�ֶ���
     */
    public static void update(String ip)
    {
        Map<Integer, String> RSList = ClustersInfo.getRSList();
        for (Iterator<Integer> iter = RSList.keySet().iterator(); iter.hasNext();)
        {
            int clusterID = (Integer) iter.next();
            String ipx = RSList.get(clusterID);
            Cluster cluster = ClustersInfo.getCluster(clusterID);
            if (ip == ipx)
            {
                cluster.setClusterRole(1);
            }
            else
            {
                cluster.setClusterRole(-1);
            }
        }
        // ClustersInfo.systemaa();
    }
}

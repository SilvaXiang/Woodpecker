package edu.ecnu.woodpecker.controller.clusterinfo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * ������Ⱥ����Ϣ�Ķ��壬�ǿ�����ģ�����ɲ��� �˶����ʾ���м�Ⱥ�Ľ�ɫ��ȫ��Ϊ��̬��Ա�����;�̬��Ա����
 * 
 */
public class ClustersInfo
{
    /**
     * ������м�Ⱥ��Ϣ������б�
     */
    private static List<Cluster> clusters = null;

    /**
     * �����ַ
     */
    private static String deployPath = null;

    /**
     * �û���
     */
    private static String userName = null;

    /**
     * ����
     */
    private static String password = null;

    /**
     * port
     */
    private static int connectionPort;

    /**
     * TODO ���ݲ��Ի���ģ���е������ļ���ʼ����Ⱥ��Ϣ��
     * 
     * @return ��ʼ���ɹ���Ϊtrue
     */
    public static void initialize(List<Map<String, String>> serverList)
    {

        clusters = new ArrayList<Cluster>();
        int j = 0;
        RootServerInfo rs = null;
        Cluster cluster = null;

        if (serverList.size() != 0)
        {
            setDeployPath(serverList.get(0).get("deployPath"));
            setUserName(serverList.get(0).get("userName"));
            setPassword(serverList.get(0).get("password"));
            setConnectionPort(Integer.valueOf(serverList.get(0).get("connectionPort")));
            for (Map<String, String> server : serverList)
            {
                String type = server.get("serverType");

                if (type.equals("RootServer"))
                {
                    rs = new RootServerInfo();
                    cluster = new Cluster();
                    clusters.add(cluster);
                }
                if (type.equals("RootServer"))
                {
                    cluster.setClusterID(j);
                    rs.setClusterID(j);
                    // System.out.println("------ClusterID"+rs.getClusterID());
                    if (j == 0)
                    {
                        cluster.setClusterRole(1);
                    }
                    else
                    {
                        cluster.setClusterRole(-1);
                    }
                    rs.setCurClusterRSIP(server.get("RSIP"));
                    rs.setCurClusterRSPort(Integer.valueOf(server.get("RSPort")));
                    rs.setIP(server.get("IP"));
                    rs.setIsDown(false);
                    rs.setNIC(server.get("NIC"));
                    rs.setPID(Integer.valueOf(server.get("PID")));
                    rs.setPort(Integer.valueOf(server.get("port")));
                    cluster.addRootServer(rs);
                    j++;
                }
                else if (type.equals("UpdateServer"))
                {
                    UpdateServerInfo ups = new UpdateServerInfo();
                    ups.setCurClusterRSIP(server.get("RSIP"));
                    ups.setCurClusterRSPort(Integer.valueOf(server.get("RSPort")));
                    ups.setIP(server.get("IP"));
                    ups.setIsDown(false);
                    ups.setMergePort(Integer.valueOf(server.get("mergePort")));
                    ups.setNIC(server.get("NIC"));
                    ups.setPID(Integer.valueOf(server.get("PID")));
                    ups.setServicePort(Integer.valueOf(server.get("servicePort")));
                    cluster.addUpdateServer(ups);
                }
                else if (type.equals("MergeServer"))
                {
                    MergeServerInfo ms = new MergeServerInfo();
                    ms.setCurClusterRSIP(server.get("RSIP"));
                    ms.setCurClusterRSPort(Integer.valueOf(server.get("RSPort")));
                    ms.setIP(server.get("IP"));
                    ms.setIsDown(false);
                    ms.setNIC(server.get("NIC"));
                    ms.setPID(Integer.valueOf(server.get("PID")));
                    ms.setListener(Boolean.valueOf(server.get("isListener")));
                    ms.setServicePort(Integer.valueOf(server.get("servicePort")));
                    ms.setMySQLPort(Integer.valueOf(server.get("MySQLPort")));
                    cluster.addMergeServer(ms);
                }
                else if (type.equals("ListenerMergeServer"))
                {
                    MergeServerInfo ms = new MergeServerInfo();
                    ms.setCurClusterRSIP(server.get("RSIP"));
                    ms.setCurClusterRSPort(Integer.valueOf(server.get("RSPort")));
                    ms.setIP(server.get("IP"));
                    ms.setIsDown(false);
                    ms.setNIC(server.get("NIC"));
                    ms.setPID(Integer.valueOf(server.get("PID")));
                    ms.setListener(Boolean.valueOf(server.get("isListener")));
                    ms.setServicePort(Integer.valueOf(server.get("servicePort")));
                    ms.setMySQLPort(Integer.valueOf(server.get("MySQLPort")));
                    cluster.addMergeServer(ms);
                }
                else if (type.equals("ChunkServer"))
                {
                    ChunkServerInfo cs = new ChunkServerInfo();
                    cs.setCurClusterRSIP(server.get("RSIP"));
                    cs.setCurClusterRSPort(Integer.valueOf(server.get("RSPort")));
                    cs.setIP(server.get("IP"));
                    cs.setIsDown(false);
                    cs.setNIC(server.get("NIC"));
                    cs.setPID(Integer.valueOf(server.get("PID")));
                    cs.setServicePort(Integer.valueOf(server.get("servicePort")));
                    cs.setAppName(server.get("appName"));
                    // System.out.println("--dd--appName:"+cs.getAppName());
                    cluster.addChunkServer(cs);
                }
            }
        }
        else
        {
            setAllServerDown();
        }
        // for(Cluster clusterx : clusters)
        // {
        // System.out.println("--dd--Clusterdep:"+getDeployPath());
        // System.out.println("--dd--ClusterID:"+clusterx.getClusterID());
        // System.out.println("3333��"+clusterx.getAllAliveServer().size());
        // for(ServerInfo server
        // :clusterx.getAllAliveServer() )
        // {
        // System.out.println("3333xxx"+server.getIP()+"--"+server.getPID());
        // }
        // }
    }

    /**
     * ����ע���ChunkServer��MergeServer����Ϣ��ӽ�ȫ�ֱ���
     * 
     * @param serverList
     */
    public static void addServer(List<Map<String, String>> serverList)
    {
        for (Map<String, String> server : serverList)
        {
            String type = server.get("serverType");
            if (type.equals("MergeServer"))
            {
                MergeServerInfo ms = new MergeServerInfo();
                ms.setCurClusterRSIP(server.get("RSIP"));
                ms.setCurClusterRSPort(Integer.valueOf(server.get("RSPort")));
                ms.setIP(server.get("IP"));
                ms.setIsDown(false);
                ms.setNIC(server.get("NIC"));
                ms.setPID(Integer.valueOf(server.get("PID")));
                ms.setListener(Boolean.valueOf(server.get("isListener")));
                ms.setServicePort(Integer.valueOf(server.get("servicePort")));
                ms.setMySQLPort(Integer.valueOf(server.get("MySQLPort")));
                for (Cluster cluster : clusters)
                {
                    if (cluster.getRandomRS().getCurClusterRSIP().equals(server.get("RSIP")))
                    {
                        cluster.addMergeServer(ms);
                    }
                }
            }
            else if (type.equals("ChunkServer"))
            {
                ChunkServerInfo cs = new ChunkServerInfo();
                cs.setCurClusterRSIP(server.get("RSIP"));
                cs.setCurClusterRSPort(Integer.valueOf(server.get("RSPort")));
                cs.setIP(server.get("IP"));
                cs.setIsDown(false);
                cs.setNIC(server.get("NIC"));
                cs.setPID(Integer.valueOf(server.get("PID")));
                cs.setServicePort(Integer.valueOf(server.get("servicePort")));
                cs.setAppName(server.get("appName"));
                for (Cluster cluster : clusters)
                {
                    if (cluster.getRandomRS().getCurClusterRSIP().equals(server.get("RSIP")))
                    {
                        cluster.addChunkServer(cs);
                    }
                }
            }
        }
    }

    /**
     * ����ע���ChunkServer��MergeServer����Ϣ��ӽ�����
     * 
     * @param ServerOperation
     *            ChunkServerInfo��MergeServerInfo����
     * @param cluCategory ��Ⱥ���ͣ�master��slave
     */
    public static void addNewServerInfo(ServerInfo server, int clusterID)
    {
        // ȷ����Ⱥ��ɫ
        int index = -1;
        for (int i = 0; i < 3; i++)
        {
            if (clusterID == clusters.get(i).getClusterID())
            {
                index = i;
            }
        }
        // ȷ��server����
        String serverName = server.getClass().getName();
        if (serverName.endsWith("MergeServerInfo"))
        {
            clusters.get(index).addMergeServer((MergeServerInfo) server);
        }
        else if (serverName.endsWith("ChunkServerInfo"))
        {
            clusters.get(index).addChunkServer((ChunkServerInfo) server);
        }
        else if (serverName.endsWith("RootServerInfo"))
        {
            clusters.get(index).addRootServer((RootServerInfo) server);
        }
        else if (serverName.endsWith("UpdateServerInfo"))
        {
            clusters.get(index).addUpdateServer((UpdateServerInfo) server);
        }
    }

    /**
     * ����ע���ChunkServer��MergeServer����Ϣ��ӽ�����
     * 
     * @param ServerOperation
     *            ChunkServerInfo��MergeServerInfo����
     * @param cluCategory ��Ⱥ���ͣ�master��slave
     */
    public static void addServerInfo(ServerInfo server, String cluCategory)
    {
        // ȷ����Ⱥ��ɫ
        int clusterRole = cluCategory.equals("master") ? 1 : -1;
        int index = -1;
        if (clusterRole == -1)
        {
            List<Integer> indexList = indexOfAll(-1);
            index = new Random().nextInt(indexList.size());
        }
        else
        {
            index = indexOf(1);
        }

        // ȷ��server����
        String serverName = server.getClass().getName();
        if (serverName.endsWith("MergeServerInfo"))
        {
            clusters.get(index).addMergeServer((MergeServerInfo) server);
        }
        else
        {
            clusters.get(index).addChunkServer((ChunkServerInfo) server);
        }
    }

    /**
     * ��ȡ��Ⱥrs��Ϣ
     * 
     * @return
     */
    public static Map<Integer, String> getRSList()
    {
        Map<Integer, String> rsList = new HashMap<Integer, String>();
        RootServerInfo rs = null;

        for (Cluster cluster : clusters)
        {
            // System.out.println("getRSList----ClusterID:"
            // + cluster.getClusterID());
            rs = cluster.getRandomRS();
            if (rs != null)
            {
                rsList.put(cluster.getClusterID(), rs.getCurClusterRSIP());
            }
        }
        return rsList;

    }

    // public static void systemaa()
    // {
    // for (Cluster clusterx : clusters)
    // {
    // System.out.println("-----role:" +
    // clusterx.getClusterRole());
    // System.out.println("--systemaa--ClusterID:" +
    // clusterx.getClusterID());
    // //
    // System.out.println("systemaa��" +
    // clusterx.getAllServer().size());
    // for (ServerInfo server : clusterx.getAllServer())
    // {
    // System.out.println("systemaa" + server.getIP() + "--"
    // + server.getPID() + "--"
    // + server.isDown());
    // }
    // }
    // }

    /**
     * TODO �������д洢�ļ�Ⱥ��Ϣ
     * 
     * @return ���³ɹ���Ϊtrue
     */
    public static boolean update(ServerInfo server)
    {
        for (Cluster cluster : clusters)
        {
            for (ServerInfo newServer : cluster.getAllServer())
            {
                if (newServer.getClass().getName().equals(server.getClass().getName())
                        && newServer.getIP().equals(server.getIP()))
                {
                    newServer.setIsDown(server.isDown());
                    newServer.setPID(server.getPID());
                    return true;
                }
            }
            ServerInfo newServer = cluster.getRandomRS();
            if (newServer.getCurClusterRSIP().equals(server.getCurClusterRSIP()))
            {
                int clusterID = cluster.getClusterID();
                addNewServerInfo(server, clusterID);
                return true;
            }
        }
        //
        // for (Cluster clusterx : clusters)
        // {
        // //
        // System.out.println("--update--Clusterdep:"+getDeployPath());
        // System.out.println("--update--ClusterID:" +
        // clusterx.getClusterID());
        // //
        // System.out.println("update��"+clusterx.getAllServer().size());
        // for (ServerInfo server1 :
        // clusterx.getAllServer())
        // {
        // System.out.println("updateaaa" + server1.getIP()
        // + "--" + server1.getPID() + "--"
        // + server1.getClass().getName() + "---" +
        // server1.isDown());
        // }
        // }
        return false;
    }

    /**
     * ��������ȺRootServer��Ӧ��server��Ϣ
     * 
     * @return RootServerInfo����
     */
    public static RootServerInfo getMasterRS()
    {
        int index = indexOf(1);
        if (index < 0)
        {
            return null;
        }
        RootServerInfo rs = clusters.get(index).getRandomRS();
        return rs == null ? null : rs;
    }

    /**
     * ��������ȺUpdateServer��Ӧ��server��Ϣ
     * 
     * @return UpdateServerInfo�����null
     */
    public static UpdateServerInfo getMasterUPS()
    {
        int index = indexOf(1);
        if (index < 0)
        {
            return null;
        }
        Cluster cluster = clusters.get(index);
        UpdateServerInfo ups = cluster.getRandomUPS();
        return ups == null ? null : ups;
    }

    /**
     * ���������Ⱥ������MergeServer��ѡ��һ����������server��Ϣ���Ҳ���һ��MS �򷵻�null
     * 
     * @return MergeServerInfo�����null
     */
    public static MergeServerInfo getMasterMS()
    {

        int index = indexOf(1);
        if (index < 0)
        {
            return null;
        }
        Cluster cluster = clusters.get(index);
        MergeServerInfo ms = cluster.getRandomMS();
        return ms == null ? null : ms;
    }

    /**
     * ���������Ⱥ������ChunkServer��ѡ��һ����������server��Ϣ���Ҳ���һ��CS �򷵻�null
     * 
     * @return ChunkServerInfo�����null
     */
    public static ChunkServerInfo getMasterCS()
    {
        int index = indexOf(1);
        if (index < 0)
        {
            return null;
        }
        Cluster cluster = clusters.get(index);
        ChunkServerInfo cs = cluster.getRandomCS();
        return cs == null ? null : cs;
    }

    /**
     * ��ȡ���һ������Ⱥ��һ��rs��Ϣ
     * 
     * @return һ������Ⱥ��rs��Ϣ���б�
     */
    public static ServerInfo getSlaveRS()
    {
        int index = indexOf(-1);
        if (index < 0)
        {
            return null;
        }
        Cluster cluster = clusters.get(index);
        RootServerInfo rs = cluster.getRandomRS();
        return rs == null ? null : rs;
    }

    /**
     * ��������ȺUpdateServer��Ӧ��server��Ϣ
     * 
     * @return UpdateServerInfo�����null
     */
    public static UpdateServerInfo getSlaveUPS()
    {
        int index = indexOf(-1);
        if (index < 0)
        {
            return null;
        }
        Cluster cluster = clusters.get(index);
        UpdateServerInfo ups = cluster.getRandomUPS();
        return ups == null ? null : ups;
    }

    /**
     * ����ӱ���Ⱥ������ChunkServer��ѡ��һ����������server��Ϣ���Ҳ���һ��CS �򷵻�null
     * 
     * @return ChunkServerInfo�����null
     */
    public static ChunkServerInfo getSlaveCS()
    {
        int index = indexOf(-1);
        if (index < 0)
        {
            return null;
        }
        Cluster cluster = clusters.get(index);
        ChunkServerInfo cs = cluster.getRandomCS();
        return cs == null ? null : cs;
    }

    /**
     * ����ӱ���Ⱥ������MergeServer��ѡ��һ����������server��Ϣ���Ҳ���һ��MS �򷵻�null
     * 
     * @return MergeServerInfo�����null
     */
    public static MergeServerInfo getSlaveMS()
    {

        int index = indexOf(-1);
        if (index < 0)
        {
            return null;
        }
        Cluster cluster = clusters.get(index);
        MergeServerInfo ms = cluster.getRandomMS();
        return ms == null ? null : ms;
    }

    /**
     * ��ȡ��ǰ����Ⱥ����δ����server����Ϣ��������Щserver���ߵ��·��ز�ȫ
     * 
     * @return server��Ϣ���б�
     */
    public static List<ServerInfo> getAliveMasterInfo()
    {
        List<ServerInfo> serverList = new ArrayList<ServerInfo>();

        int index = indexOf(1);
        if (index < 0)
        {
            return null;
        }
        Cluster cluster = clusters.get(index);

        // RS
        RootServerInfo rs = cluster.getAliveRandomRS();
        if (rs != null)
        {
            serverList.add(rs);
        }

        // UPS
        UpdateServerInfo ups = cluster.getAliveRandomUPS();
        if (ups != null)
        {
            serverList.add(ups);
        }

        // CS and MS
        serverList.addAll(cluster.getAllAliveCS());
        serverList.addAll(cluster.getAllAliveMS());

        return serverList;
    }

    /**
     * ��������ȺRootServer��Ӧ��server��Ϣ�������RS�������򷵻�null
     * 
     * @return RootServerInfo�����null
     */
    public static RootServerInfo getAliveMasterRS()
    {
        int index = indexOf(1);
        if (index < 0)
        {
            return null;
        }
        RootServerInfo rs = clusters.get(index).getAliveRandomRS();
        return rs == null ? null : rs;
    }

    /**
     * ��������ȺUpdateServer��Ӧ��server��Ϣ����UPS�����򷵻�null
     * 
     * @return UpdateServerInfo�����null
     */
    public static UpdateServerInfo getAliveMasterUPS()
    {
        int index = indexOf(1);
        if (index < 0)
        {
            return null;
        }
        Cluster cluster = clusters.get(index);
        UpdateServerInfo ups = cluster.getAliveRandomUPS();
        return ups == null ? null : ups;
    }

    /**
     * ���������Ⱥ������MergeServer��ѡ��һ��δ���ߵģ�������server��Ϣ���Ҳ���һ��δ���ߵ�MS
     * �򷵻�null
     * 
     * @return MergeServerInfo�����null
     */
    public static MergeServerInfo getAliveMasterMS()
    {

        int index = indexOf(1);
        if (index < 0)
        {
            return null;
        }
        Cluster cluster = clusters.get(index);
        MergeServerInfo ms = cluster.getAliveRandomMS();
        return ms == null ? null : ms;
    }

    /**
     * δ���ߵ�����Ⱥ������MergeServer��ѡ�񣬷�����server��Ϣ�� �򷵻�null
     * 
     * @return MergeServerInfo�����null
     */
    public static List<MergeServerInfo> getAliveMasterMSs()
    {
        List<MergeServerInfo> msList = new ArrayList<MergeServerInfo>();
        // MergeServerInfo ms = null;

        List<Integer> indexList = indexOfAll(1);
        Cluster cluster = null;
        // for (int index : indexList)
        // {
        // cluster = clusters.get(index);
        // ms = cluster.getAliveRandomMS();
        // if (ms != null)
        // {
        // msList.add(ms);
        // }
        // }
        for (int index : indexList)
        {
            cluster = clusters.get(index);
            for (MergeServerInfo ms : cluster.getAllAliveMS())
            {
                if (ms != null)
                {
                    msList.add(ms);
                }
            }
        }
        return msList;
    }

    /**
     * ��������ȺChunkServerInfo��Ӧ��server��Ϣ����CS�����򷵻�null
     * 
     * @return ChunkServerInfo�����null
     */
    public static ChunkServerInfo getAliveMasterCS()
    {
        int index = indexOf(1);
        if (index < 0)
        {
            return null;
        }
        Cluster cluster = clusters.get(index);
        ChunkServerInfo cs = cluster.getAliveRandomCS();
        return cs == null ? null : cs;
    }

    /**
     * δ���ߵ�����Ⱥ������ChunkServer��ѡ�񣬷�����server��Ϣ�� �򷵻�null
     * 
     * @return MergeServerInfo�����null
     */
    public static List<ChunkServerInfo> getAliveMasterCSs()
    {
        List<ChunkServerInfo> csList = new ArrayList<ChunkServerInfo>();

        List<Integer> indexList = indexOfAll(1);
        Cluster cluster = null;
        for (int index : indexList)
        {
            cluster = clusters.get(index);
            for (ChunkServerInfo cs : cluster.getAllAliveCS())
            {
                if (cs != null)
                {
                    csList.add(cs);
                }
            }
        }
        return csList;
    }

    /**
     * ��ȡ���м�Ⱥδ���ߵ�server��Ϣ
     * 
     * @return ���м�Ⱥδ����server��Ϣ���б�
     */
    public static List<ServerInfo> getAllAliveServerInfo()
    {
        List<ServerInfo> serverList = new ArrayList<ServerInfo>();
        for (Cluster ele : clusters)
        {
            serverList.addAll(ele.getAllAliveServer());
        }

        return serverList;
    }

    /**
     * ��ȡ���б���Ⱥδ����server����Ϣ
     * 
     * @return ���б���Ⱥδ����server��Ϣ���б�
     */
    public static List<ServerInfo> getAllAliveSlaveInfo()
    {
        List<ServerInfo> serverList = new ArrayList<ServerInfo>();
        List<Integer> indexList = indexOfAll(-1);
        for (int index : indexList)
        {
            serverList.addAll(clusters.get(index).getAllAliveServer());
        }

        return serverList;
    }

    /**
     * ��ȡ���һ������Ⱥ������δ���ߵ�server��Ϣ
     * 
     * @return ����Ⱥ������server��Ϣ���б�
     */
    public static List<ServerInfo> getAliveSlaveInfo()
    {
        int index = indexOf(-1);
        if (index < 0)
        {
            return null;
        }
        return clusters.get(index).getAllAliveServer();
    }

    /**
     * ��ȡ���һ������Ⱥ��һ��δ���ߵ�rs��Ϣ
     * 
     * @return һ������Ⱥ��rs��Ϣ���б�
     */
    public static ServerInfo getAliveSlaveRS()
    {
        int index = indexOf(-1);
        if (index < 0)
        {
            return null;
        }
        Cluster cluster = clusters.get(index);
        RootServerInfo rs = cluster.getAliveRandomRS();
        return rs == null ? null : rs;
    }

    /**
     * ��ȡ���һ������Ⱥ��һ��δ���ߵ�rs��Ϣ
     * 
     * @return һ������Ⱥ��rs��Ϣ���б�
     */
    public static ServerInfo getAliveSlaveUPS()
    {
        int index = indexOf(-1);
        if (index < 0)
        {
            return null;
        }
        Cluster cluster = clusters.get(index);
        UpdateServerInfo ups = cluster.getAliveRandomUPS();
        return ups == null ? null : ups;
    }

    /**
     * �������б���Ⱥδ����RootServer��Ӧ��server��Ϣ�� �б�Ϊ����������б���Ⱥ��UPS��������
     * 
     * @return RootServerInfo�����б�
     */
    public static List<RootServerInfo> getAliveSlaveRSs()
    {
        List<RootServerInfo> rsList = new ArrayList<RootServerInfo>();
        RootServerInfo rs = null;

        List<Integer> indexList = indexOfAll(-1);
        for (int index : indexList)
        {
            rs = clusters.get(index).getAliveRandomRS();
            if (rs != null)
            {
                rsList.add(rs);
            }
        }

        return rsList;
    }

    /**
     * �������б���Ⱥδ����UpdateServer��Ӧ��server��Ϣ��
     * �б�Ϊ����������б���Ⱥ��UPS��������
     * 
     * @return UpdateServerInfo�����б�
     */
    public static List<UpdateServerInfo> getAliveSlaveUPSs()
    {
        List<UpdateServerInfo> upsList = new ArrayList<UpdateServerInfo>();
        UpdateServerInfo ups = null;

        List<Integer> indexList = indexOfAll(-1);
        for (int index : indexList)
        {
            ups = clusters.get(index).getAliveRandomUPS();
            if (ups != null)
            {
                upsList.add(ups);
            }
        }

        return upsList;
    }

    /**
     * ÿ������Ⱥ��ѡ��δ���ߵ�MergeServer��������server��Ϣ
     * 
     * @return MergeServerInfo�����б�
     */
    public static List<MergeServerInfo> getAliveSlaveMSs()
    {
        List<MergeServerInfo> msList = new ArrayList<MergeServerInfo>();

        List<Integer> indexList = indexOfAll(-1);
        Cluster cluster = null;
        for (int index : indexList)
        {
            cluster = clusters.get(index);
            for (MergeServerInfo ms : cluster.getAllAliveMS())
            {
                if (ms != null)
                {
                    msList.add(ms);
                }
            }
        }

        return msList;
    }

    /**
     * ÿ������Ⱥ��ѡ��δ���ߵ�ChunkServerInfo��������server��Ϣ
     * 
     * @return ChunkServerInfo�����б�
     */
    public static List<ChunkServerInfo> getAliveSlaveCSs()
    {
        List<ChunkServerInfo> csList = new ArrayList<ChunkServerInfo>();

        List<Integer> indexList = indexOfAll(-1);
        Cluster cluster = null;
        for (int index : indexList)
        {
            cluster = clusters.get(index);
            for (ChunkServerInfo cs : cluster.getAllAliveCS())
            {
                if (cs != null)
                {
                    csList.add(cs);
                }
            }
        }

        return csList;
    }

    /**
     * ����clusterID����cluster
     * 
     * @param clusterID
     * @return
     */
    public static Cluster getCluster(int clusterID)
    {
        for (Cluster cluster : clusters)
        {
            if (cluster.getClusterID() == clusterID)
            {
                return cluster;
            }
        }
        return null;
    }

    /**
     * �жϵ�ǰ����δ���ߵļ�Ⱥ���Ƿ��������Ⱥ��
     * 
     * @return ��������Ⱥ�򷵻�true
     */
    public static boolean existMaster()
    {
        int index = indexOf(1);
        return index == -1 ? false : true;
    }

    /**
     * ����ǰ����Ⱥ������server��isDown��Ϊtrue
     */
    public static void setMasterDown()
    {
        int index = indexOf(1);
        if (index >= 0)
        {
            Cluster cluster = clusters.get(index);
            cluster.setAllDown();
        }

    }

    /**
     * �����м�Ⱥ��server��isDown��Ϊtrue
     */
    public static void setAllServerDown()
    {
        for (Cluster ele : clusters)
        {
            ele.setAllDown();
        }
    }

    /**
     * �����б���Ⱥ��server���isDown��Ϊtrue
     */
    public static void setAllSlaveDown()
    {
        List<Integer> indexList = indexOfAll(-1);
        for (int index : indexList)
        {
            clusters.get(index).setAllDown();
        }
    }

    /**
     * ��һ������Ⱥ��ָ��server���isDown��Ϊtrue������Ϊ�˱���Ⱥ��δ����server��Ϣ���б�
     * 
     * @param serverList ����Ⱥserver��Ϣ�б�
     */
    public static void setSlaveDown(List<ServerInfo> serverList)
    {
        Cluster cluster = null;
        String IP = serverList.get(0).getIP();
        int PID = serverList.get(0).getPID();

        List<Integer> indexList = indexOfAll(-1);
        for (int index : indexList)
        {
            cluster = clusters.get(index);
            if (cluster.contain(IP, PID))
            {
                cluster.setServerDown(serverList);
                break;
            }
        }
    }

    public static void setServerDown(List<ServerInfo> serverList)
    {
        Cluster cluster = null;
        String IP = serverList.get(0).getIP();
        int PID = serverList.get(0).getPID();

        List<Integer> indexList = indexOfAll(-1);
        for (int index : indexList)
        {
            cluster = clusters.get(index);
            if (cluster.contain(IP, PID))
            {
                cluster.setServerDown(serverList);
                break;
            }
        }
    }

    /**
     * ����IP��PID��Ӧ��server���ڵļ�Ⱥ��Ϊ����Ⱥ
     * 
     * @param IP server��IP��ַ
     * @param PID server�Ľ��̺�
     */
    public static void setMaster(String IP, int PID)
    {
        for (Cluster cluster : clusters)
        {
            if (cluster.contain(IP, PID))
            {
                clusters.get(indexOf(1)).setClusterRole(-1);
                cluster.setClusterRole(1);
                break;
            }
        }
    }

    /**
     * ��������ļ�Ⱥ��ɫ�����ؼ�Ⱥ�����б��λ�ã����ڱ���Ⱥ�����ص������һ������Ⱥ��λ�ã������׸�����Ⱥ
     * ��λ�ã�����Ҳ����򷵻�-1
     * 
     * @param clusterRole ��Ⱥ��ɫ��1��������Ⱥ��0�����ʼ����-1������Ⱥ
     * @return ��Ⱥλ�û�-1
     */
    private static int indexOf(int clusterRole)
    {
        Cluster cluster = null;
        int index = -1;
        switch (clusterRole)
        {
        case -1:
            // ����Ⱥ
            List<Integer> indexList = new ArrayList<Integer>();
            for (int i = 0, len = clusters.size(); i < len; i++)
            {
                cluster = clusters.get(i);
                if (cluster.getClusterRole() == -1)
                {
                    indexList.add(i);
                }
            }

            // ���ѡ��һ������Ⱥ
            if (indexList.size() == 0)
            {
                return -1;
            }
            int position = new Random().nextInt(indexList.size());
            index = indexList.get(position);
            break;
        default:
            // ����Ⱥ���ʼ��״̬
            boolean flag = false;
            for (int i = 0, len = clusters.size(); i < len; i++)
            {
                cluster = clusters.get(i);
                if (cluster.getClusterRole() == clusterRole)
                {
                    index = i;
                    flag = true;
                    break;
                }
            }
            if (!flag)
            {
                return -1;
            }
            break;
        }// end of switch(clusterRole)

        return index;
    }

    /**
     * ��������ļ�Ⱥ��ɫ�����ؼ�Ⱥ�����б�����λ�õ��б�
     * 
     * @param clusterRole ��Ⱥ��ɫ��1��������Ⱥ��0�����ʼ����-1������Ⱥ
     * @return ��Ⱥ����λ�õ��б�
     */
    private static List<Integer> indexOfAll(int clusterRole)
    {
        Cluster cluster = null;
        List<Integer> indexList = new ArrayList<Integer>();

        for (int i = 0, len = clusters.size(); i < len; i++)
        {
            cluster = clusters.get(i);
            if (cluster.getClusterRole() == clusterRole)
            {
                indexList.add(i);
            }
        }

        return indexList;
    }

    public static void main(String[] args)
    {

    }

    public static String getDeployPath()
    {
        return deployPath;
    }

    public static void setDeployPath(String deployPath)
    {
        ClustersInfo.deployPath = deployPath;
    }

    public static String getUserName()
    {
        return userName;
    }

    public static void setUserName(String userName)
    {
        ClustersInfo.userName = userName;
    }

    public static String getPassword()
    {
        return password;
    }

    public static void setPassword(String password)
    {
        ClustersInfo.password = password;
    }

    public static int getConnectionPort()
    {
        return connectionPort;
    }

    public static void setConnectionPort(int connectionPort)
    {
        ClustersInfo.connectionPort = connectionPort;
    }

}

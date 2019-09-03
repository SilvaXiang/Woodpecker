package edu.ecnu.woodpecker.controller.clusterinfo;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import edu.ecnu.woodpecker.constant.LogLevelConstant;
import edu.ecnu.woodpecker.log.WpLog;

/**
 * Cedar������Ⱥ��Ϣ�Ķ��� �˶����Ӧһ������Ⱥ�򱸼�Ⱥ������server����Ϣ
 * 
 */
public class Cluster
{
    /**
     * һ����Ⱥ��RootServer��UpdateServer��ChunkServer��
     * MergeServer����Ϣ Ϊ�պ�CEDAR��չ���ǣ��ݽ�RS��UPS���б���ʽ�洢
     */
    private List<RootServerInfo> RSList = null;
    private List<UpdateServerInfo> UPSList = null;
    private List<ChunkServerInfo> CSList = null;
    private List<MergeServerInfo> MSList = null;

    /**
     * ��Ⱥ��ɫ��1Ϊ����Ⱥ��-1Ϊ����Ⱥ��0Ϊ��ʼ��״̬
     */
    private int clusterRole;

    /**
     * ��ȺID����ʶһ����Ⱥ
     */
    private int clusterID;

    public Cluster()
    {
        RSList = new ArrayList<RootServerInfo>(1);
        UPSList = new ArrayList<UpdateServerInfo>(1);
        CSList = new ArrayList<ChunkServerInfo>();
        MSList = new ArrayList<MergeServerInfo>();
    }

    /**
     * ����RS���˼�Ⱥ��
     * 
     * @param rs
     */
    public void addRootServer(RootServerInfo rs)
    {
        RSList.add(rs);
    }

    /**
     * ����UPS���˼�Ⱥ��
     * 
     * @param ups
     */
    public void addUpdateServer(UpdateServerInfo ups)
    {
        UPSList.add(ups);
    }

    /**
     * ����MS���˼�Ⱥ��
     * 
     * @param ms    
     */
    public void addMergeServer(MergeServerInfo ms)
    {
        MSList.add(ms);
    }

    /**
     * ����CS���˼�Ⱥ��
     * 
     * @param cs
     */
    public void addChunkServer(ChunkServerInfo cs)
    {
        CSList.add(cs);
    }

    /**
     * ���ش˼�Ⱥ������server���б�
     * @return  ����server���б�
     */
    public List<ServerInfo> getAllServer()
    {
        List<ServerInfo> serverList = new ArrayList<ServerInfo>();
        
        serverList.addAll(RSList);
        serverList.addAll(UPSList);
        serverList.addAll(CSList);
        serverList.addAll(MSList);
        
        return serverList;
    }

    /**
     * �Ӵ˼�Ⱥ���������һ��RS��Ŀǰֻ��һ��RS
     * 
     * @return a random RS info
     */
    public RootServerInfo getRandomRS()
    {
        try
        {
            return RSList.get(0);
        }
        catch (Exception e)
        {
            // ��ʼ��û�����RS����
            WpLog.recordLog(LogLevelConstant.ERROR, "there is no RootServer info");
        }
        return null;
    }
    
    /**
     * �Ӵ˼�Ⱥ���������һ��UPS��Ŀǰֻ��һ��UPS
     * 
     * @return һ��δ���ߵ�UPS�����UPS�����򷵻�null
     */
    public UpdateServerInfo getRandomUPS()
    {
        try
        {
            return UPSList.get(0);
        }catch(Exception e)
        {
            //��¼��־����ʼ��û�����UPS����
            WpLog.recordLog(LogLevelConstant.ERROR, "there is no UpdateServer info");
        }
        return null;
    }

    /**
     * �Ӵ˼�Ⱥ���������һ��MS ���10����Ȼ�Ҳ���δ����MS��ʹ�ñ���Ѱ�ң��Ծ��Ҳ����򷵻�null
     * 
     * @return MS���Ҳ����򷵻�null
     */
    public MergeServerInfo getRandomMS()
    {
        MergeServerInfo ms = null;
        
        int index = new Random().nextInt(MSList.size());
        ms = MSList.get(index);
        if (null == ms)
            WpLog.recordLog(LogLevelConstant.ERROR, "merge server info is null, index=%d", index);
        return ms;
    }

    /**
     * �Ӵ˼�Ⱥ���������CS ���10����Ȼ�Ҳ���δ����CS��ʹ�ñ���Ѱ�ң��Ծ��Ҳ����򷵻�null
     * 
     * @return CS���Ҳ����򷵻�null
     */
    public ChunkServerInfo getRandomCS()
    {
        ChunkServerInfo cs = null;
        int index = new Random().nextInt(CSList.size());
        cs = CSList.get(index);
        if (null == cs)
            WpLog.recordLog(LogLevelConstant.ERROR, "chunk server info is null, index=%d", index);
        return cs;
    }

    /**
     * �Ӵ˼�Ⱥ���������һ��δ���ߵ�RS��Ŀǰֻ��һ��RS
     * 
     * @return һ��δ���ߵ�RS�����RS�����򷵻�null
     */
    public RootServerInfo getAliveRandomRS()
    {
        try
        {
            return RSList.get(0).isDown() ? null : RSList.get(0);
        }
        catch (Exception e)
        {
            // ��¼��־����ʼ��û�����RS����
            WpLog.recordLog(LogLevelConstant.ERROR, "root server info is null");
        }
        return null;
    }

    /**
     * �Ӵ˼�Ⱥ���������һ��δ���ߵ�UPS��Ŀǰֻ��һ��UPS
     * 
     * @return һ��δ���ߵ�UPS�����UPS�����򷵻�null
     */
    public UpdateServerInfo getAliveRandomUPS()
    {
        try
        {
            return UPSList.get(0).isDown()? null:UPSList.get(0);
        }catch(Exception e)
        {
            //��¼��־����ʼ��û�����UPS����
            WpLog.recordLog(LogLevelConstant.ERROR, "update server info is null");
        }
        return null;
    }

    /**
     * �Ӵ˼�Ⱥ���������һ��δ���ߵ�MS ���10����Ȼ�Ҳ���δ����MS��ʹ�ñ���Ѱ�ң��Ծ��Ҳ����򷵻�null
     * 
     * @return δ����MS���Ҳ����򷵻�null
     */
    public MergeServerInfo getAliveRandomMS()
    {
        MergeServerInfo ms = null;
        
        //���10�β���δ����MS
        for (int i = 0; i < 10; i++)
        {
            int index = new Random().nextInt(MSList.size());
            ms = MSList.get(index);
            if (!ms.isDown())
            {
                WpLog.recordLog(LogLevelConstant.INFO, "get alive merge server: %s, index=%d", ms, index);
                return ms;
            }
        }
    
        // ʹ�ñ�������δ����MS
        for (MergeServerInfo msi : MSList)
        {
            if (!msi.isDown())
            {
                WpLog.recordLog(LogLevelConstant.INFO, "get alive merge server: %s", msi);
                return msi;
            }
        }
        WpLog.recordLog(LogLevelConstant.ERROR, "no alive merge server");
        return null;
    }

    /**
     * �Ӵ˼�Ⱥ���������һ��δ���ߵ�CS ���10����Ȼ�Ҳ���δ����CS��ʹ�ñ���Ѱ�ң��Ծ��Ҳ����򷵻�null
     * 
     * @return δ����CS���Ҳ����򷵻�null
     */
    public ChunkServerInfo getAliveRandomCS()
    {
        ChunkServerInfo cs = null;
        for (int i = 0; i < 10; i++)
        {
            int index = new Random().nextInt(CSList.size());
            cs = CSList.get(index);
            if (!cs.isDown())
            {
                WpLog.recordLog(LogLevelConstant.INFO, "get alive chunk server: %s, index=%d", cs, index);
                return cs;
            }
        }
    
        // ʹ�ñ�������
        for (ChunkServerInfo csi : CSList)
        {
            if (!csi.isDown())
            {
                WpLog.recordLog(LogLevelConstant.INFO, "get alive chunk server: %s", csi);
                return csi;
            }
        }
    
        return null;
    }

    /**
     * �Ӵ˼�Ⱥ���������һ��RS��Ŀǰֻ��һ��RS
     * @return
     */
    public RootServerInfo getALLAliveRS()
    {
        try
        {
            return RSList.get(0);
        }
        catch (Exception e)
        {
            // ��¼��־����ʼ��û�����RS����
            WpLog.recordLog(LogLevelConstant.ERROR, "no alive root server");
        }
        return null;
    }
    
    /**
     * ���ش˼�Ⱥ������δ����MS���б�
     * @return  ����δ����MS���б�
     */
    public List<MergeServerInfo> getAllAliveMS()
    {
        List<MergeServerInfo> msList = new ArrayList<MergeServerInfo>();
        for(MergeServerInfo ms:MSList)
        {
            if(!ms.isDown())
            {
                msList.add(ms);
            }
        }
        
        return msList;
    }

    /**
     * ���ش˼�Ⱥ������δ����CS���б�
     * @return  ����δ����CS���б�
     */
    public List<ChunkServerInfo> getAllAliveCS()
    {
        List<ChunkServerInfo> csList = new ArrayList<ChunkServerInfo>();
        for(ChunkServerInfo cs:CSList)
        {
            if(!cs.isDown())
            {
                csList.add(cs);
            }
        }
        
        return csList;
    }
    
    /**
     * ���ش˼�Ⱥ������δ����server���б�
     * @return  ����δ����server���б�
     */
    public List<ServerInfo> getAllAliveServer()
    {
        List<ServerInfo> serverList = new ArrayList<ServerInfo>();
        
        // RS and UPS
        ServerInfo server = getAliveRandomRS();
        if(server!=null)
        {
            serverList.add(server);
        }
        server = getAliveRandomUPS();
        if(server!=null)
        {
            serverList.add(server);
        }
        
        //CS and MS
        serverList.addAll(getAllAliveCS());
        serverList.addAll(getAllAliveMS());
        
        return serverList;
    }
    /**
     * ���˼�Ⱥ������server���isDown����Ϊtrue
     */
    public void setAllDown()
    {
        for(RootServerInfo rs:RSList)
        {
            rs.setIsDown(true);
        }
        for(UpdateServerInfo ups: UPSList)
        {
            ups.setIsDown(true);
        }
        for(ChunkServerInfo cs:CSList)
        {
            cs.setIsDown(true);
        }
        for(MergeServerInfo ms: MSList)
        {
            ms.setIsDown(true);
        }
    }
    
    /**
     * �˼�Ⱥ���Ƿ������IP��PID��Ӧ��server
     * �����򷵻�true
     */
    public boolean contain(String IP,int PID)
    {
        //RS and UPS
        for(RootServerInfo rs:RSList)
        {
            if(rs.getIP().equals(IP)&&rs.getPID()==PID)
            {
                return true;
            }
        }
        for(UpdateServerInfo ups: UPSList)
        {
            if(ups.getIP().equals(IP) && ups.getPID()==PID)
            {
                return true;
            }
        }
        
        //CS and MS
        for(ChunkServerInfo cs: CSList)
        {
            if(cs.getIP().equals(IP) && cs.getPID()==PID)
            {
                return true;
            }
        }
        for(MergeServerInfo ms: MSList)
        {
            if(ms.getIP().equals(IP)&& ms.getPID()==PID)
            {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * ���˼�Ⱥ��ָ��server��isDown��Ϊtrue
     * �����server�϶��ڴ˼�Ⱥ��
     * @param serverList    ָ��server���б�
     */
    public void setServerDown(List<ServerInfo> serverList)
    {
        String IP = null;
        int PID=-1;
        for(ServerInfo server:serverList)
        {
            IP=server.getIP();
            PID=server.getPID();
            getServer(IP,PID).setIsDown(true);
        }
    }
    /**
     * ���˼�Ⱥ��ָ��server��isDown��Ϊfalse
     * �����server�϶��ڴ˼�Ⱥ��
     * @param serverList    ָ��server���б�
     */
    public void setServerUp(List<ServerInfo> serverList)
    {
        String IP = null;
        int PID=-1;
        for(ServerInfo server:serverList)
        {
            IP=server.getIP();
            PID=server.getPID();
            getServer(IP,PID).setIsDown(false);
        }
    }
    public int getClusterRole()
    {
        return clusterRole;
    }

    public void setClusterRole(int clusterRole)
    {
        this.clusterRole = clusterRole;
    }

    public int getClusterID()
    {
        return clusterID;
    }

    public void setClusterID(int clusterID)
    {
        this.clusterID = clusterID;
    }
    
    /**
     * ����IP��PID���ض�Ӧ��server���󣬲���ʧ��ʱ����null
     * @param IP    IP��ַ
     * @param PID   ���̺�
     * @return  IP��PID��Ӧ��server�����null
     */
    private ServerInfo getServer(String IP,int PID)
    {
        for(RootServerInfo rs:RSList)
        {
            if(rs.getIP().equals(IP)&& rs.getPID()==PID)
            {
                return rs;
            }
        }
        
        for(UpdateServerInfo ups:UPSList)
        {
            if(ups.getIP().equals(IP) && ups.getPID()==PID)
            {
                return ups;
            }
        }
        
        for(ChunkServerInfo cs:CSList)
        {
            if(cs.getIP().equals(IP)&& cs.getPID()==PID)
            {
                return cs;
            }
        }
        
        for(MergeServerInfo ms:MSList)
        {
            if(ms.getIP().equals(IP)&& ms.getPID()==PID)
            {
                return ms;
            }
        }
        
        return null;
    }
}

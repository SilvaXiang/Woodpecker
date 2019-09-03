package edu.ecnu.woodpecker.systemfunction;

public class CedarSystemOperator
{
    /**
     * ��ͣ��ǰ�߳�ָ����ʱ��
     * 
     * @param time ʱ��
     * @param timeUnit ʱ�䵥λ
     */
    public static void sleep(int time, String timeUnit)
    {
        Shell.sleep(time, timeUnit);
    }

    /**
     * ��ʾ��ָ��ip��ַ��ִ��shell���Ĭ��·�����û���Ŀ¼��
     * 
     * @param command shell����
     * @param ip ip��ַ
     */
    public static void shellCommand(String command, String ip)
    {
        Shell.shellCommand(command, ip);
    }

    /**
     * ��ʾɱCEDAR��server�����ر�ɱserver����Ϣ ������ ɱ������Ⱥ����server type=master num���� ĳ������Ⱥ�����n��MS
     * type=ms_slave num=n
     * 
     * @param type server����
     * @param num server��Ŀ
     * @return ��ɱserver����Ϣ
     */
    public static String killServer(String type, int num)
    {
        return CEDARServer.killServer(type, num);
    }

    /**
     * ����arg��ĳ����ɱserver
     * 
     * @param arg ��kill_server���ص��ַ�����Ϣ���洢�б�ɱserver����Ϣ��
     * @return �����Ƿ�ɹ�
     */
    public static boolean startServer(String arg)
    {
        return CEDARServer.startServer(arg);
    }

    /**
     * ��ָ��ip�����һ��server��server������servertype����
     * 
     * @param ip
     * @param serverType ��ms_master
     * @param NIC ������
     */
    public static void addServer(String ip, String serverType, String NIC)
    {
        CEDARServer.addServer(ip, serverType, NIC);
    }

    /**
     * ������Ⱥ��������ѡ��������л���ѡ�١� ����ѡ�ٺ󷵻ز���ȴ�ѡ����ɡ�
     * 
     * @return ��������ѡ���Ƿ�ɹ�
     */
    public static boolean reelect()
    {
        return CEDARCluster.reelect();
    }

    /**
     * ����ÿ�պϲ�
     * 
     * @return ����ÿ�պϲ��Ƿ�ɹ�
     */
    public static boolean merge()
    {
        return CEDARCluster.merge();
    }

    /**
     * ��ʾ��Ⱥ�ϲ��Ƿ���ɣ�����ؽ��Ϊ��������
     * 
     * @return 0�������м�Ⱥ�ϲ���� 1������һ������Ⱥδ�ϲ���ɣ����༯Ⱥ�ϲ��� 2������������Ⱥδ�ϲ���ɣ����༯Ⱥ�ϲ���
     *         3��������Ⱥδ�ϲ���ɣ����౸��Ⱥ�ϲ��� 4����һ������Ⱥ�ϲ���ɣ����༯Ⱥδ�ϲ���� 5�������м�Ⱥ��δ�ϲ���ɡ�
     */
    public static Integer isMergeDown()
    {
        return CEDARCluster.isMergeDown();
    }

    /**
     * ��ʾ��ǰ��Ⱥ�Ƿ�����
     * 
     * @return
     */
    public static boolean existMaster()
    {
        return CEDARCluster.existMaster();
    }

    /**
     * ����kill_server���ص��ַ�����Ϣ�����ø�server���ڼ�ȺΪ����Ⱥ
     * 
     * @param arg kill_server���ص��ַ�����Ϣ
     * @return �����Ƿ�ɹ�
     */
    public static boolean setMaster(String arg)
    {
        return CEDARCluster.setMaster(arg);
    }

    /**
     * ��ʾ��n���ڵȴ����м�Ⱥ�ϲ���ɣ�����ָ��ʱ�����м�Ⱥδ�ϲ�����򷵻ش��󣬱�ʾ��caseʧ�ܡ�
     * 
     * @param time ָ��ʱ��n��
     * @return ��ȷ�����
     */
    public static boolean awaitMergeDone(int time)
    {
        return CEDARCluster.awaitMergeDone(time);
    }

    /**
     * ��Ⱥ�Ƿ�ɷ�������ؽ��Ϊ�������� 0�������м�Ⱥ�ɷ��� 1������һ������Ⱥ���ɷ������༯Ⱥ�ɷ��� 2������������Ⱥ���ɷ������༯Ⱥ�ɷ���
     * 3��������Ⱥ���ɷ��񣬱���Ⱥ���ɷ��� 4����һ������Ⱥ�ɷ������༯Ⱥ���ɷ��� 5�������м�Ⱥ���ɷ���
     * 
     * @return
     */
    public static Integer isClusterAvailable()
    {
        return CEDARCluster.isClusterAvailable();
    }

    /**
     * ��ʾ��n���ڵȴ����м�Ⱥ�ɷ��񣬳���ָ��ʱ�����м�Ⱥ���ɷ����򷵻ش��󣬱�ʾ��caseʧ��
     * 
     * @param time ָ��ʱ��n��
     * @return
     */
    public static boolean awaitAvailable(int time)
    {
        return CEDARCluster.awaitAvailable(time);
    }

    /**
     * Start to gather statistical information
     * 
     * @return True when starting successfully
     */
    public static boolean gatherStatistics()
    {
        return CEDARCluster.gatherStatistics();
    }

    /**
     * 
     * @return True when gather is done
     */
    public static boolean isGatherDone()
    {
        return CEDARCluster.isGatherDone();
    }
}

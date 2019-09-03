package edu.ecnu.woodpecker.environment;

public interface CedarEnvirOperation
{
    /**
     * ��ȡ�����ļ�
     * 
     * @param filePath ���������ļ���ַ+�ļ��� ����"F:/compile.txt";
     * @param encoding �����ʽ
     * @return ��ȡ���������ļ��Ƿ�ɹ�
     */
    public static boolean readCompileConf(String filePath, String encoding)
    {
        return CedarConfigInitializer.read(filePath, encoding);
    }

    /**
     * ����CEDAR
     * 
     * @param filePath ���������ļ���ַ+�ļ��� ����"F:/compile.txt";
     * @param encoding �����ʽ
     * @return �����Ƿ�ɹ�
     */
    public static boolean compileCEDAR()
    {
        // Configurate.read(filePath,encoding);
        return CedarCompiler.compileCEDAR();
    }

    /**
     * ����CEDAR
     * 
     * @param filePath ���벿���ļ���ַ+�ļ��� ����"F:/1RS_1UPS_3MS_3CS.txt";
     * @param encoding �����ʽ
     * @return �����Ƿ�ɹ�
     */
    public static boolean deployCEDAR(String filePath, String encoding)
    {
        boolean confCorrect = CedarConfigInitializer.read(filePath, encoding);
        return confCorrect ? CedarDeployer.deployCEDAR() : false;
    }

    /**
     * �������õ�CEDAR�ļ�
     * 
     * @param ip ������õ�CEDAR�ļ����͵���ip��ַ��
     * @return �����Ƿ�ɹ�
     */
    public static boolean remoteReplicate(String ip)
    {
        return CedarDeployer.remoteReplicate(ip);
    }

    /**
     * ���������ļ���Ⱥ
     * 
     * @return ��Ⱥ�Ƿ������ɹ�
     */
    public static boolean startCEDAR()
    {
        return CedarDeployer.startCEDAR();
    }

    /**
     * �رռ�Ⱥ
     * 
     * @param type ��Ⱥ�ر����ͣ�normal/unnormal normal�������رռ�Ⱥ������server��ɾ����־�������ļ�
     *            unnormal:�����رռ�Ⱥ������server����log�ļ�ת���ɾ����־�������ļ�
     * @return ��Ⱥ�Ƿ�رճɹ�
     */
    public static boolean closeCEDAR(String type)
    {
        return CedarDeployer.closeCEDAR(type);
    }

    /**
     * ��ʼ����Ⱥ
     * 
     * @param type ��ʼ����Ⱥ���ͣ�complie/deploy compile:ɾ����Ⱥ�б��������CEDAR�ļ�
     *            deploy��ɾ����Ⱥ�в��������CEDAR�ļ�
     * @return ��ʼ����Ⱥ�Ƿ�ɹ�
     */
    public static boolean initializeCEDAR(String type)
    {
        return CedarDeployer.initializeCEDAR(type);
    }

    /**
     * ע�������
     * 
     * @param ip ip��ַ������̨������ע��
     * @param serverType server���� MergeServer/ChunkServer
     * @param clusterType cluster���� master/slave
     * @param NIC �˿ں�
     * @return ע��������Ƿ�ɹ�
     */
    public static boolean registerServer(String ip, String serverType, String clusterType,
            String NIC)
    {
        return CedarDeployer.registerServer(ip, serverType, clusterType, NIC);
    }

    /**
     * ��ȡNIC
     * 
     * @param ip ip��ַ
     */
    public static String getNIC(String ip)
    {
        return CedarDeployer.getNIC(ip);
    }

    /**
     * ��ʼ����Ⱥ��Ϣ �ڼ�Ⱥ�Ѿ���������ֱ��ִ�а���ʱ���ø÷�����ʼ����Ⱥ��Ϣ
     * 
     * @param compileFilePath �����ļ�·��
     * @param deployFilePath �����ļ�·��
     * @param encoding �����ʽ
     */
    public static boolean initializeCluster(String compileFilePath, String deployFilePath,
            String encoding)
    {
        return CedarDeployer.initializeCluster(compileFilePath, deployFilePath, encoding);
    }
}

package edu.ecnu.woodpecker.executor;

import java.io.File;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

import edu.ecnu.woodpecker.constant.FileConstant;
import edu.ecnu.woodpecker.constant.LogLevelConstant;
import edu.ecnu.woodpecker.controller.TestController;
import edu.ecnu.woodpecker.executor.parser.WoodpeckerParser;
import edu.ecnu.woodpecker.log.WpLog;

/**
 * parse one case file into one middle result file
 */
public class Parser
{
    /**
     * ����-ֵ��keyΪ��������valueΪ������Ӧ��ֵ
     */
    private static Map<String, Object> varValueMap = null;

    /**
     * ����-���ͱ�keyΪ��������valueΪ��������������
     */
    private static Map<String, String> varTypeMap = null;

    /**
     * �м�����·��
     */
    private static String MIDDLE_RESULT_PATH = null;

    /**
     * ���԰�����Ŀ¼·��
     */
    private static String caseDirectoryPath = null;

    /**
     * �ɿ��������ã�������Ĳ��԰����ű��ļ����н���ִ�У��������ɵ��м�����
     * 
     * @param caseFile ���԰����ű��ļ�
     * @return �������ɵ��м��������������ʧ���򷵻�null
     */
    public static File parse(File caseFile)
    {
        WpLog.recordLog(LogLevelConstant.INFO, "Start to parse %s", caseFile.getName());
        varValueMap = new HashMap<String, Object>(30);
        varTypeMap = new HashMap<String, String>(30);
        WoodpeckerParser.setVarMap(varValueMap, varTypeMap);
        String midResultPath = getMiddleResultPath(caseFile);
        midResultPath = midResultPath.replace(FileConstant.CASE_FILE_SUFFIX, FileConstant.MIDDLE_RESULT_FILE_SUFFIX);

        try (PrintWriter midResultPW = new PrintWriter(midResultPath, FileConstant.UTF_8);)
        {
            WoodpeckerParser.setMidResultPW(midResultPW);
            boolean canParse = WoodpeckerParser.parseCaseFile(caseFile);
            if (canParse)
            {
                midResultPW.flush();
                midResultPW.close();
                WpLog.recordLog(LogLevelConstant.INFO, "Parse %s successfully", caseFile.getName());
                return new File(midResultPath);
            }
        }
        catch (Exception e)
        {
            // Parse case error
            WpLog.recordLog(LogLevelConstant.ERROR, WpLog.getExceptionInfo(e));
            e.printStackTrace();
        }
        return null;
    }

    /**
     * ��������԰����ļ���һģһ����Ŀ¼�ṹ
     * 
     * @param caseDirectoryPath ���԰����ļ���·�������Woodpecker��Ŀ��·����
     * @param middleResultPath �м��������·��
     */
    public static void geneMidResultDirectory(String caseDirectoryPath, String middleResultPath) throws Exception
    {
        WpLog.recordLog(LogLevelConstant.INFO, "Generate middle result directory: %s", middleResultPath);
        Parser.MIDDLE_RESULT_PATH = middleResultPath;
        Parser.caseDirectoryPath = caseDirectoryPath;

        File caseDir = new File(caseDirectoryPath);
        if (!caseDir.exists())
            caseDir.mkdirs();
        // MIDDLE_RESULT_PATH������뱣����'/'
        MIDDLE_RESULT_PATH += FileConstant.FILE_SEPARATOR;
        File midResultDir = new File(MIDDLE_RESULT_PATH);
        if (!midResultDir.exists())
            midResultDir.mkdirs();
        geneDirectory(caseDir, MIDDLE_RESULT_PATH.substring(0, MIDDLE_RESULT_PATH.length() - 1));
    }

    private Parser()
    {}

    /**
     * �ݹ麯������destPath·����������srcFile·����һ����Ŀ¼�ṹ
     * 
     * @param srcFile ���԰����ļ���·��
     * @param destPath �м�����·��
     */
    private static void geneDirectory(File srcFile, String destPath) throws Exception
    {
        File[] files = srcFile.listFiles();
        for (File ele : files)
        {
            if (ele.isDirectory())
            {
                String path = destPath + FileConstant.FILE_SEPARATOR + ele.getName();
                WpLog.recordLog(LogLevelConstant.INFO, "generate directory: %s", path);
                new File(path).mkdir();
                geneDirectory(ele, path);
            }
        }
    }

    /**
     * �õ�����԰�����Ӧ���м������ļ���·��
     * 
     * @param caseFile ���԰����ļ�
     * @return ��������԰����ļ���Ӧ���м������ļ���·��
     */
    private static String getMiddleResultPath(File caseFile)
    {
        // ��Windowsƽ̨Ŀ¼�ָ���'\'��Ϊ'/'
        String path = caseFile.getPath().replace("\\", FileConstant.FILE_SEPARATOR);
        MIDDLE_RESULT_PATH = MIDDLE_RESULT_PATH.replace("\\", FileConstant.FILE_SEPARATOR);
        if (caseDirectoryPath.endsWith("//"))
            caseDirectoryPath = caseDirectoryPath.substring(0, caseDirectoryPath.length() - 1);
        if (MIDDLE_RESULT_PATH.endsWith("//"))
            MIDDLE_RESULT_PATH = MIDDLE_RESULT_PATH.substring(0, MIDDLE_RESULT_PATH.length() - 1);
        path = path.replace(caseDirectoryPath, MIDDLE_RESULT_PATH);
        return path;
    }

    public static Map<String, Object> getVarValueMap()
    {
        return varValueMap;
    }

    public static Map<String, String> getVarTypeMap()
    {
        return varTypeMap;
    }

    /**
     * ��������Ԫ�������
     * 
     * @param args
     */
    public static void main(String[] args)
    {
        varValueMap = new HashMap<String, Object>();
        varTypeMap = new HashMap<String, String>();
        String[] inputPath = new String[1];
        inputPath[0] = "./test.case";
        TestController.setCurrentGroup("example");
        TestController.setDatabaseInstancePath("./database_instance/");
        TestController.setIdealResultSetPath("./ideal_result_set/");
        TestController.setDatabase("mysql");

        WoodpeckerParser.setVarMap(varValueMap, varTypeMap);

        try
        {
            PrintWriter midResultPW = new PrintWriter("./test.mr", FileConstant.UTF_8);
            WoodpeckerParser.setMidResultPW(midResultPW);
            WoodpeckerParser.parseCaseFile(new File(inputPath[0]));
            midResultPW.flush();
            midResultPW.close();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}

package edu.ecnu.woodpecker.executor.keyword;

import edu.ecnu.woodpecker.constant.LogLevelConstant;
import edu.ecnu.woodpecker.constant.SignConstant;
import edu.ecnu.woodpecker.environment.CedarEnvirOperation;
import edu.ecnu.woodpecker.executor.Executor;
import edu.ecnu.woodpecker.executor.GrammarType;
import edu.ecnu.woodpecker.log.WpLog;
import edu.ecnu.woodpecker.systemfunction.CedarSystemOperator;
import edu.ecnu.woodpecker.util.Util;

public class SystemTestProcessor extends Executor implements Keyword
{
    public SystemTestProcessor()
    {}

    @Override
    public void handle(String keyword, GrammarType type) throws Exception
    {
        WpLog.recordLog(LogLevelConstant.INFO, "SYS: %s", keyword);
        switch (type)
        {
        case FIRST_GRAMMAR:
            String[] parts = Util.removeBlankElement(keyword.split("\\[|]"));
            if (parts.length == 3)
            {
                // shell����
                testSystem(parts[1], parts[2]);
            }
            else
            {
                // ϵͳ����
                testSystem(parts[1]);
            }
            break;
        case SECOND_GRAMMAR:
            // Index 0 is variable name, 1 is keyword
            String[] keywordParts = Util.removeBlankElement(keyword.split(SignConstant.ASSIGNMENT_STR, 2));
            handleSecondGrammar(keywordParts[0], keywordParts[1]);
            break;
        case THIRD_GRAMMAR:
            // Index 0 is data type and variable name, 1 is keyword
            keywordParts = Util.removeBlankElement(keyword.split(SignConstant.ASSIGNMENT_STR, 2));
            // Index 0 is data type, 1 is variable name
            String[] decVar = Util.removeBlankElement(keywordParts[0].split("\\s"));
            handleThirdGrammar(decVar[0], decVar[1], keywordParts[1]);
            break;
        default:
            WpLog.recordLog(LogLevelConstant.ERROR, "Grammar error");
            throw new Exception("Grammar error");
        }
    }

    /**
     * 
     * @param variableName
     * @param keyword Starts with "sys["
     * @throws Exception
     */
    private void handleSecondGrammar(String variableName, String keyword) throws Exception
    {
        WpLog.recordLog(LogLevelConstant.INFO, "2th grammar, SYS keyword");
        try
        {
            // sys�ؼ��֣�parts���飬�±�0Ϊsys��1ΪsysStatement��2ΪIP��ַ
            String[] parts = Util.removeBlankElement(keyword.split("\\[|]"));
            if (parts.length == 3)
            {
                // shell����
                testSystem(parts[1], parts[2]);
            }
            else
            {
                // ϵͳ����
                Object result = testSystem(parts[1]);
                WpLog.recordLog(LogLevelConstant.DEBUG, variableName + ": " + result);
                varValueMap.put(variableName, result);
            }
        }
        catch (Exception e)
        {
            WpLog.recordLog(LogLevelConstant.ERROR, WpLog.getExceptionInfo(e));
            throw new Exception(String.format("%s in %s line %d", WpLog.getExceptionInfo(e), caseFileName, lineNumber));
        }
    }

    /**
     * 
     * @param dataType The data type of variable
     * @param variableName
     * @param keyword Starts with "sys["
     * @throws Exception
     */
    private void handleThirdGrammar(String dataType, String variableName, String keyword) throws Exception
    {
        WpLog.recordLog(LogLevelConstant.INFO, "3th grammar, SYS keyword");
        try
        {
            // sys�ؼ��֣�parts���飬�±�0Ϊsys��1Ϊsystem statement��2ΪIP��ַ
            String[] parts = Util.removeBlankElement(keyword.split("\\[|]"));
            if (parts.length == 3)
            {
                // shell����
                testSystem(parts[1], parts[2]);
            }
            else
            {
                // ϵͳ����
                Object result = testSystem(parts[1]);
                WpLog.recordLog(LogLevelConstant.DEBUG, variableName + ": " + result);
                varValueMap.put(variableName, result);
            }
        }
        catch (Exception e)
        {
            WpLog.recordLog(LogLevelConstant.ERROR, WpLog.getExceptionInfo(e));
            throw new Exception(String.format("%s in %s line %d", WpLog.getExceptionInfo(e), caseFileName, lineNumber));
        }
    }

    /**
     * Handle shell
     * 
     * @param shell
     * @param IP The target server's IP which will execute shell
     * @throws Exception
     */
    private void testSystem(String shell, String IP) throws Exception
    {
        CedarSystemOperator.shellCommand(shell, IP);
    }

    /**
     * 
     * @param systemStatement
     * @return Object, if the system statement has no result and will return null
     * @throws Exception
     */
    private Object testSystem(String systemStatement) throws Exception
    {
        Object result = null;
        // �±�0Ϊ���1��֮������Ϊ���ܴ��ڵĲ���������
        String[] parts = Util.removeBlankElement(systemStatement.split("\\s+"));

        if (parts.length == 3)
        {
            // ���server���ܺ�ɱCEDAR server���ܵĲ��ֲ���,
            // add_server -option ip��kill_server -option n
            if (parts[0].startsWith("add_server"))
            {
                CedarSystemOperator.addServer(parts[2], parts[1].substring(1), CedarEnvirOperation.getNIC(parts[2]));
            }

            if (parts[0].startsWith("kill_server"))
            {
                // ������������Ŀ��kill_server
                result = CedarSystemOperator.killServer(parts[1], Integer.parseInt(parts[2]));
            }
        }
        else if (parts.length == 2)
        {
            // await_available n; await_merge_done n; kill_server -option;
            // start_server variable; set_master variable
            if (parts[0].equals("await_available"))
            {
                result = CedarSystemOperator.awaitAvailable(Integer.parseInt(parts[1]));
            }
            if (parts[0].equals("await_merge_done"))
            {
                result = CedarSystemOperator.awaitMergeDone(Integer.parseInt(parts[1]));
            }
            if (parts[0].equals("kill_server"))
            {
                // ֻ��һ��������kill_server���ڶ���������Ч
                result = CedarSystemOperator.killServer(parts[1], 0);
            }
            if (parts[0].equals("start_server"))
            {
                result = CedarSystemOperator.startServer((String) varValueMap.get(parts[1]));
            }
            if (parts[0].equals("set_master"))
            {
                result = CedarSystemOperator.setMaster((String) varValueMap.get(parts[1]));
            }
        }
        else if (parts.length == 1)
        {
            // is_cluster_available; merge; is_merge_done; reelect; exist_master; gather_statistics; is_gather_done
            switch (parts[0])
            {
            case "is_cluster_available":
                result = CedarSystemOperator.isClusterAvailable();
                break;
            case "merge":
                result = CedarSystemOperator.merge();
                break;
            case "is_merge_done":
                result = CedarSystemOperator.isMergeDown();
                break;
            case "reelect":
                result = CedarSystemOperator.reelect();
                break;
            case "exist_master":
                result = CedarSystemOperator.existMaster();
                break;
            case "gather_statistics":
                result = CedarSystemOperator.gatherStatistics();
                break;
            case "is_gather_done":
                result = CedarSystemOperator.isGatherDone();
                break;
            default:
                WpLog.recordLog(LogLevelConstant.ERROR, "Unknown option in SYS keyword");
                throw new Exception();
            }
        }

        return result;
    }
}

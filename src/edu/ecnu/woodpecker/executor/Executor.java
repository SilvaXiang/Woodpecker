package edu.ecnu.woodpecker.executor;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.jsp.jstl.sql.Result;

import org.wltea.expression.ExpressionEvaluator;
import org.wltea.expression.datameta.Variable;

import edu.ecnu.woodpecker.constant.ConfigConstant;
import edu.ecnu.woodpecker.constant.DataType;
import edu.ecnu.woodpecker.constant.DataTypeConstant;
import edu.ecnu.woodpecker.constant.DataValueConstant;
import edu.ecnu.woodpecker.constant.FileConstant;
import edu.ecnu.woodpecker.constant.LogLevelConstant;
import edu.ecnu.woodpecker.constant.SignConstant;
import edu.ecnu.woodpecker.controller.TestController;
import edu.ecnu.woodpecker.executor.keyword.ConnectionGetter;
import edu.ecnu.woodpecker.executor.keyword.Keyword;
import edu.ecnu.woodpecker.executor.keyword.STKeywordProcessRealize;
import edu.ecnu.woodpecker.log.WpLog;
import edu.ecnu.woodpecker.sql.DbmsBrand;
import edu.ecnu.woodpecker.stresstest.Dispatcher;
import edu.ecnu.woodpecker.stresstest.PerformResult;
import edu.ecnu.woodpecker.util.Util;

/**
 * �Խ��������ɵ��м���������ִ�У���̬��
 * 
 */
public class Executor
{
    /**
     * ���д��һ���м������ļ�������Statement����������������������в���IO Exception
     */
    private static String[] midResultStatement = null;

    /**
     * midResultStatement���±������������м�������ִ��˳��
     */
    public static int index;

    /**
     * Use it to save time when load class into jvm
     */
    private static Map<String, Class<?>> cache = null;

    /**
     * ����-ֵ��keyΪ��������valueΪ������Ӧ��ֵ��ֵΪ�ַ���ʱ����˫���ţ�ʹ��ʱ��ȥ��
     */
    protected static Map<String, Object> varValueMap = null;

    /**
     * ����-���ͱ�keyΪ��������valueΪ��������������
     */
    protected static Map<String, String> varTypeMap = null;

    /**
     * keyΪԤ�����洢����ִ������������value�ǲ������ͣ���洢���̻�Ԥ�������λ��һһ��Ӧ
     */
    protected static Map<String, List<String>> preparedParametersMap = null;
    
    /**
     * keyΪԤ����ʱִ������������valueΪ��Ӧ��sql���
     */
    protected static Map<String, String> pstatExecuteMap = null; 
    
    /**
     * ���ݿ�Ĳ�ѯ���������jstl��Result��洢ResultSet������
     */
    protected static Result result = null;

    /**
     * ��ǰִ�а���������
     */
    protected static String caseFileName = null;

    /**
     * ��¼ѹ�����Եĸ��ر�ţ���������ʱ��Ҫֹͣ�������е�ѹ������
     */
    protected static List<Integer> stressTestList = new ArrayList<Integer>();

    /**
     * ��¼���ܲ��Եĸ��ر�ţ���������ʱ��Ҫ�ȴ����е����ܲ��Խ���
     */
    protected static List<Integer> performTestList = new ArrayList<Integer>();

    /**
     * ���ڴ���MySQL���쳣
     */
    protected static Connection connection = null;

    /**
     * ��ǰ����ִ�а������ڵ��������
     */
    protected static String currentGroup = null;

    /**
     * SQL��PSQL�ؼ���������֮�е��쳣����Ŵ˴��쳣��ԭ��
     */
    protected static String exceptionString = null;

    /**
     * �к�
     */
    protected static int lineNumber;

    
    
    /**
     * ���ڴ�����ж������connection
     */
    protected static HashMap<String, Connection> connMap = new HashMap<>();
    
    /**
     * ��ǰ����ִ��SQLʱ�õ�connection
     */
    protected static Connection curConn = null;
    
    /**
     * ���ڴ�����ж������statement
     */
    protected static HashMap<String, Statement> statMap = new HashMap<>();
    
    /**
     * ��ǰ����ִ��SQLʱ�õ�statement
     */
    protected static Statement curStat = null;
    
    
    public Executor()
    {}

    /**
     * ִ�н��������ɵ��м������ļ�
     * 
     * @param midResult �м������ļ�
     * @return �˰����Ƿ�ͨ��
     */
    public static boolean execute(File midResult, Map<String, Object> varValueMap, Map<String, String> varTypeMap)
    {
        WpLog.recordLog(LogLevelConstant.INFO, "Start to execute %s", midResult.getName());
        caseFileName = midResult.getName().substring(0, midResult.getName().indexOf(SignConstant.DOT_CHAR)) + FileConstant.CASE_FILE_SUFFIX;
        Executor.currentGroup = TestController.getCurrentGroup();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(midResult), FileConstant.UTF_8)))
        {
            // Initialize some Executor parameters
            initialize(varValueMap, varTypeMap);
            // Initialize stress test module's dispatcher
            Dispatcher.initialize(TestController.getServerUserName(), TestController.getServerPassword());

            List<String> mrStaList = new ArrayList<String>();
            String line = null;
            while ((line = br.readLine()) != null)
            {
                line = line.trim();
                // �����Թ�
                if (line.length() != 0)
                    mrStaList.add(line);
            }
            midResultStatement = (String[]) mrStaList.toArray(new String[0]);
            index = 0;

            for (; index < midResultStatement.length; index++)
                assignStatement();

            // ����ѹ�����Ժ����ܲ���
            if (!stressTestList.isEmpty())
            {
                WpLog.recordLog(LogLevelConstant.INFO, "Stop all stress test task");
                for (int ele : stressTestList)
                    Dispatcher.stopStressTest(ele);
            }
            if (!performTestList.isEmpty())
            {
                WpLog.recordLog(LogLevelConstant.INFO, "Get all perform test result");
                List<PerformResult> performResults = Dispatcher.getAllPerformResult(performTestList);
                // �����д����־���Լ��洢չʾ���
                for (int i = 0; i < performResults.size(); i++)
                {
                    WpLog.recordStressTestResult(performResults.get(i), String.format("Workload number = %d", performTestList.get(i)));
                }
            }
        }
        catch (Exception e)
        {
            // �ӵ��쳣˵���в���ִ��ʧ��
            WpLog.recordLog(LogLevelConstant.ERROR, WpLog.getExceptionInfo(e));
            // �����MySQL������쳣����
            if (TestController.getDatabase() == DbmsBrand.MYSQL)
            {
                WpLog.recordLog(LogLevelConstant.ERROR, "Handle exception in MySQL, drop database woodpecker");
                try
                {
                    connection.createStatement().executeUpdate("Drop database if exists woodpecker");
                }
                catch (SQLException e1)
                {
                    e1.printStackTrace();
                }
            }
            return false;
        }
        finally
        {
            // һ���������������������������ͷ�
            WpLog.recordLog(LogLevelConstant.INFO, "Close all connections which are created in %s", caseFileName);
            for (Map.Entry<String, String> entry : varTypeMap.entrySet())
            {
                if (entry.getValue().equals(DataTypeConstant.CONNECTION))
                {
                    Connection connection = (Connection) varValueMap.get(entry.getKey());
                    if (connection != null)
                    {
                        try
                        {
                            connection.close();
                        }
                        catch (SQLException e)
                        {}
                    }
                }
            }
        }
        return true;
    }

    /**
     * ���ù��߼���˱��ʽ�����
     * 
     * @param expression ���ʽ
     * @return true or false
     * @throws Exception ������execute��������ִ���Ƿ�ɹ�
     */
    protected static Object calExpression(String expression) throws Exception
    {
        WpLog.recordLog(LogLevelConstant.INFO, "Calculate %s", expression);
        // ����ʽ�����е����б�����
        List<String> list = new ArrayList<String>();
        String[] parts = expression.split("\\p{Punct}");
        for (String ele : parts)
        {
            if (!ele.trim().matches("^[-+]?(([0-9]+)([.]([0-9]+))?|([.]([0-9]+))?)$") && !ele.trim().equals(DataValueConstant.TRUE)
                    && !ele.trim().equals(DataValueConstant.FALSE) && !ele.trim().equals(DataValueConstant.NULL_LOWER))
            {
                list.add(ele.trim());
            }
        }
        return useIKExpression(expression, list.toArray(new String[0]));
    }

    /**
     * ��������ı��������飬����BasicSQLOperation���Ӧ���������ͱ�����飬�����еĲ���������getParamValues�������ص�����һһ��Ӧ
     * 
     * @param variables ��������ֵ������
     * @param statement Ԥ�����洢����ִ��������
     * @return
     */
    protected static DataType[] getParamTypes(String[] variables, String statementName) throws Exception
    {
        if (variables == null)
            return null;
        DataType[] paramTypes = new DataType[variables.length];
        String type = null;
        for (int i = 0; i < paramTypes.length; i++)
        {
            type = varTypeMap.containsKey(variables[i]) ? varTypeMap.get(variables[i]).trim()
                    : preparedParametersMap.get(statementName).get(i);
            paramTypes[i] = DataType.of(type);
        }
        return paramTypes;
    }

    /**
     * ��������ı��������飬���ر�����Ӧ��ֵ������
     * 
     * @param variables ����������
     * @return
     */
    protected static Object[] getParamValues(String[] variables)
    {
        if (variables == null)
            return null;
        Object[] paramValues = new Object[variables.length];
        for (int i = 0; i < paramValues.length; i++)
        {
            paramValues[i] = varValueMap.containsKey(variables[i].trim()) ? varValueMap.get(variables[i].trim())
                    : (variables[i].matches("(\"|').*(\"|')") ? variables[i].substring(1, variables[i].length() - 1) : variables[i]);
        }
        return paramValues;
    }

    /**
     * ��ϰʹ��Lambda���ʽ
     * 
     */
    @FunctionalInterface
    protected interface Action
    {
        /**
         * ����ĳ������������ʵ��������ȫ��
         * 
         * @param varName ĳ��������������
         * @return
         */
        public String getRealType(String varName);
    }

    @SuppressWarnings("unchecked")
    protected static <T> T getComparableObject(Action action, String varName) throws ClassNotFoundException
    {
        return (T) Class.forName(action.getRealType(varName)).cast(varValueMap.get(varName));
    }
    
    /**
     * Initialize some parameters in Executor class
     * 
     * @param varValueMap
     * @param varTypeMap
     */
    private static void initialize(Map<String, Object> varValueMap, Map<String, String> varTypeMap)
    {
        // Set variable map
        Executor.varValueMap = varValueMap;
        Executor.varTypeMap = varTypeMap;
        Executor.preparedParametersMap = new HashMap<>();
        Executor.pstatExecuteMap = new HashMap<>();
        lineNumber = -1;
        cache = new HashMap<>();
        
        try
        {
            connection = ConnectionGetter.getConnection(ConfigConstant.MASTER_LOWER);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    /**
     * �����ķ����ͣ�������Ӧ���ķ�������
     * 
     * @throws Exception ������execute��������ִ���Ƿ�ɹ�
     */
    public static void assignStatement() throws Exception
    {
        if (midResultStatement[index].matches("[0-9]+,0:.*$"))
        {
            // 0�ķ�
            variableStatement();
            return;
        }
        if (midResultStatement[index].matches("[0-9]+,1:.*$"))
        {
            // 1�ķ�
            functionStatement();
            return;
        }
        if (midResultStatement[index].matches("[0-9]+,2:.*$"))
        {
            // 2�ķ�
            variableFunctionStatement();
            return;
        }
        if (midResultStatement[index].matches("[0-9]+,3:.*$"))
        {
            // 3�ķ�
            declareFunctionStatement();
            return;
        }
        if (midResultStatement[index].matches("[0-9]+,4:.*$"))
        {
            // 4�ķ�
            ifStatement();
            return;
        }
        if (midResultStatement[index].matches("[0-9]+,5:.*$"))
        {
            // 5�ķ�
            whileStatement();
            return;
        }
        if (midResultStatement[index].matches("[0-9]+,7:.*$"))
        {
            // 7�ķ�������st���漰�Ĺؼ���
            WpLog.recordLog(LogLevelConstant.INFO, "seven grammar");
            String keyword = midResultStatement[index].substring(midResultStatement[index].indexOf(SignConstant.COLON_CHAR) + 1).trim();
            //lineNumber = getLineNumber();
            STKeywordProcessRealize.stList.add(keyword);
        }
        if (midResultStatement[index].matches("[0-9]+,8:.*$"))
        {
            //8�ķ�������txn_loadingʱ��ʼִ��stmap�е���ز���
            WpLog.recordLog(LogLevelConstant.INFO, "eight grammar");
            String keyword = midResultStatement[index].substring(midResultStatement[index].indexOf(SignConstant.COLON_CHAR) + 1).trim();
            //lineNumber = getLineNumber();
            STKeywordProcessRealize.stList.add(keyword);
            STKeywordProcessRealize.execute();
            return;
        }
    }

    /**
     * ����0�ķ���Ҫô��ֻ�б���������Ҫô�Ǳ�������ͬʱ�и�ֵ
     * 
     * @throws Exception
     */
    private static void variableStatement() throws Exception
    {
        String keyword = midResultStatement[index].substring(midResultStatement[index].indexOf(SignConstant.COLON_STR) + 1).trim();
        lineNumber = getLineNumber();
        try
        {
            if (keyword.matches("[\\p{Alnum}<,\\s]+>?(\\s)+[\\p{Alnum}_$]+(\\s)*=.*"))
            {
                WpLog.recordLog(LogLevelConstant.INFO, keyword);
                // ��ʱ�������Ӹ�ֵ
                int equIndex = keyword.indexOf(SignConstant.ASSIGNMENT_CHAR);
                // leftPart���ҽ�������Ԫ�أ�0Ԫ���Ǳ������ͣ�1Ԫ���Ǳ�������������������϶������ո�
                String[] leftPart = Util.removeBlankElement(keyword.substring(0, equIndex).split("\\s+"));
                String rightPart = keyword.substring(equIndex + 1).trim();
                // ��������ֵ
                assignValueToVarValueMap(leftPart[0], leftPart[1], rightPart);
                return;
            }
        }
        catch (Exception e)
        {
            throw new Exception(String.format("lineNumber: %d has errors", lineNumber));
        }

        if (keyword.matches("[\\p{Alnum}<,\\s]+>?(\\s)+[\\p{Alnum}_$]+(\\s)*"))
        {
            WpLog.recordLog(LogLevelConstant.INFO, keyword);
            // ��ʱֻ����������������varTypeMap��varValueMap�Ѿ��ڽ������׶���ɣ�ִ������ʱ�޶���
            
            // ��Ҫ���ĵ�ǰconn or stat������varTypeMap���жϱ������͡�  update for remove conn and stat in SQL
            String[] part = Util.removeBlankElement(keyword.split("\\s"));
            if(part[0].equalsIgnoreCase("connection") || part[0].equalsIgnoreCase("conn"))
            {
                if(connMap.containsKey(part[1]))
                {
                    curConn = connMap.get(part[1]);
                }
                else
                {
                    throw new Exception(String.format("lineNumber: %d has errors:connection undefined before!", lineNumber));
                }
            }
            if(part[0].equalsIgnoreCase("statement") || part[0].equalsIgnoreCase("stat"))
            {
                if(statMap.containsKey(part[1]))
                {
                    curStat = statMap.get(part[1]);
                }
                else
                {
                    throw new Exception(String.format("lineNumber: %d has errors:statement undefined before!", lineNumber));
                }
            }
            return;
        }
    }

    /**
     * Handle first grammar which only possesses keyword
     * 
     * @throws Exception
     */
    private static void functionStatement() throws Exception
    {
        WpLog.recordLog(LogLevelConstant.INFO, "First grammar");
        String keyword = midResultStatement[index].substring(midResultStatement[index].indexOf(SignConstant.COLON_CHAR) + 1).trim();
        lineNumber = getLineNumber();
        // Get the class with keyword
        String className = null;
        //���Դ���error�����ؼ��ֺ�û��[�����
        if(keyword.indexOf(SignConstant.LBRACKET) != -1)
        {
            className = KeywordConstant.keywordClassMap.get(keyword.substring(0, keyword.indexOf(SignConstant.LBRACKET)));
        }
        else
        {
            className = KeywordConstant.keywordClassMap.get(keyword.toString());
        }
        Class<?> keywordClass = getClass(className);
        // Use state pattern
        Keyword state = (Keyword) keywordClass.newInstance();
        state.handle(keyword, GrammarType.FIRST_GRAMMAR);
    }

    /**
     * ����2�ķ������ùؼ��ֶ��Ѿ������ı������и�ֵ�����
     * 
     * @throws Exception
     */
    private static void variableFunctionStatement() throws Exception
    {
        WpLog.recordLog(LogLevelConstant.INFO, "Second grammar");
        String keyword = midResultStatement[index].substring(midResultStatement[index].indexOf(SignConstant.COLON_STR) + 1).trim();
        lineNumber = getLineNumber();
        // Get the class with keyword
        int beginIndex = keyword.indexOf(SignConstant.ASSIGNMENT_CHAR) + 1;
        int endIndex = keyword.indexOf(SignConstant.LBRACKET);
        String className = KeywordConstant.keywordClassMap.get(keyword.substring(beginIndex, endIndex).trim());
        Class<?> keywordClass = getClass(className);
        // Use state pattern
        Keyword state = (Keyword) keywordClass.newInstance();
        state.handle(keyword, GrammarType.SECOND_GRAMMAR);
    }

    /**
     * ����3�ķ����������������ùؼ��ָ�ֵ�����
     * 
     * @throws Exception ������execute��������ִ���Ƿ�ɹ�
     */
    protected static void declareFunctionStatement() throws Exception
    {
        WpLog.recordLog(LogLevelConstant.INFO, "Third grammar");
        // Remove line number and grammar type
        String keyword = midResultStatement[index].substring(midResultStatement[index].indexOf(SignConstant.COLON_CHAR) + 1).trim();
        lineNumber = getLineNumber();
        // Get class with keyword
        int beginIndex = keyword.indexOf(SignConstant.ASSIGNMENT_CHAR) + 1;
        int endIndex = keyword.indexOf(SignConstant.LBRACKET);
        String className = KeywordConstant.keywordClassMap.get(keyword.substring(beginIndex, endIndex).trim());
        Class<?> keywordClass = getClass(className);
        // Use state pattern
        Keyword state = (Keyword) keywordClass.newInstance();
        state.handle(keyword, GrammarType.THIRD_GRAMMAR);
    }

    /**
     * ����4�ķ�����if��䣬��ͬ������elseһ����
     * 
     * @param index midResultStatement���±�����
     * @throws Exception ������execute��������ִ���Ƿ�ɹ�
     */
    private static void ifStatement() throws Exception
    {
        WpLog.recordLog(LogLevelConstant.INFO, "4th grammar, if statement");
        lineNumber = getLineNumber();
        String keywords = midResultStatement[index].substring(midResultStatement[index].indexOf(":") + 1).trim();
        // �±�1Ϊif����б��ʽ�ַ���
        String[] parts = Util.removeBlankElement(keywords.split("\\[|]|\\{"));
        if (calExpression(parts[1]).equals(true) ? true : false)
        {
            WpLog.recordLog(LogLevelConstant.INFO, "%s expression is true", keywords);
            // ���ʽΪ��
            blockStatement();

            if (index + 1 < midResultStatement.length)
            {
                // ����Խ��
                if (midResultStatement[index + 1].matches("[0-9]+,4\\s*:\\s*else\\s*\\{\\s*$"))
                {
                    // �Թ�else��
                    index++;
                    int braceCount = 1;
                    for (; braceCount > 0;)
                    {
                        index++;
                        if (midResultStatement[index].matches(".*\\{\\s*$"))
                        {
                            // ����'{'
                            braceCount++;
                            continue;
                        }
                        if (midResultStatement[index].matches("[0-9]+,6\\s*:\\s*}\\s*$"))
                        {
                            // ����'}'
                            braceCount--;
                        }
                    }
                }
            }
        }
        else
        {
            WpLog.recordLog(LogLevelConstant.INFO, "%s expression is false", keywords);
            // ���ʽΪ�٣��Թ�if����
            int braceCount = 1;
            for (; braceCount > 0;)
            {
                index++;
                if (midResultStatement[index].matches(".*\\{\\s*$"))
                {
                    // ����'{'
                    braceCount++;
                    continue;
                }
                if (midResultStatement[index].matches("[0-9]+,6\\s*:\\s*}\\s*$"))
                {
                    // ����'}'
                    braceCount--;
                }
            }

            if (index + 1 < midResultStatement.length)
            {
                // ����Խ��
                if (midResultStatement[index + 1].matches("[0-9]+,4\\s*:\\s*else\\s*\\{\\s*$"))
                {
                    // ����else��
                    index++;
                    blockStatement();
                }
            }
        }
    }

    /**
     * ����5�ķ�����while���
     * 
     * @param index midResultStatement���±�����
     * @throws Exception ������execute��������ִ���Ƿ�ɹ�
     */
//    private static void whileStatement() throws Exception
//    {
//        WpLog.recordLog(LogLevelConstant.INFO, "5th grammar, while statement");
//        String keyword = midResultStatement[index].substring(midResultStatement[index].indexOf(SignConstant.COLON_STR) + 1);
//        // �±�1Ϊѭ������
//        String[] parts = Util.removeBlankElement(keyword.split("\\[|]|\\{"));
//        int loopCount = Integer.parseInt(parts[1]);
//
//        int begin = blockStatement(true);
//        for (loopCount--; loopCount > 0; loopCount--)
//            blockStatement(begin, index);
//    }
    private static void whileStatement() throws Exception
    {
        WpLog.recordLog(LogLevelConstant.INFO, "5th grammar, while statement");
        String keyword = midResultStatement[index].substring(midResultStatement[index].indexOf(SignConstant.COLON_STR) + 1);
        // �±�1Ϊѭ������
        String[] parts = Util.removeBlankElement(keyword.split("\\[|]|\\{"));

        int begin = blockStatement(true);
        while(calExpression(parts[1]).equals(true))
        {
            blockStatement(begin, index);
        }
            
    }

    /**
     * block��������1��������Ӧ�ķ�����������'}'���أ���ʼʱindexλ����'{'�У�����ʱindex��'}'��
     * 
     * @throws Exception ������execute��������ִ���Ƿ�ɹ�
     */
    private static void blockStatement() throws Exception
    {
        int braceCount = 1;
        for (; braceCount > 0;)
        {
            index++;
            // 6�ķ��������Ҵ�����
            if (midResultStatement[index].matches("[0-9]+,6\\s*:\\s*\\}\\s*$"))
                braceCount--;
            else
                assignStatement();
        }
    }

    /**
     * block��������2��ִ��һ���block��������䣬�����������ʼλ�ã���ʼʱindexλ����'{'�У�����ʱindex��'}'��
     * 
     * @param store ���Ϊfalse����block�������1һ��
     * @return ��block��������ʼλ�ã���'{'������
     * @throws Exception ������execute��������ִ���Ƿ�ɹ�
     */
    private static int blockStatement(boolean store) throws Exception
    {
        if (!store)
        {
            // Ŀǰ�˷�֧����ʹ��
            blockStatement();
            return -1;
        }

        int begin = index;
        int braceCount = 1;
        for (; braceCount > 0;)
        {
            index++;
            // 6�ķ��������Ҵ�����
            if (midResultStatement[index].matches("[0-9]+,6\\s*:\\s*\\}\\s*$"))
                braceCount--;
            else
                assignStatement();
        }
        return begin;
    }

    /**
     * block��������3��ִ��һ�������������һ��
     * 
     * @param begin
     * @param end
     * @throws Exception ������execute��������ִ���Ƿ�ɹ�
     */
    private static void blockStatement(int begin, int end) throws Exception
    {
        int storeIndex = index;
        index = begin + 1;
        for (; index < end; index++)
            assignStatement();
        index = storeIndex;
    }

   

    /**
     * ��ȡ��ǰindexָ���еĲ��԰����е��кţ�������־��¼������к�
     * 
     * @return
     */
    private static int getLineNumber()
    {
        return Integer.parseInt(midResultStatement[index].substring(0, midResultStatement[index].indexOf(SignConstant.COMMA_STR)));
    }

    /**
     * ��IK Expression������ʽ��ֵ
     * 
     * @param expression ���ʽ�ַ���
     * @param variables ���ʽ����������
     * @return ���ʽ��ֵ
     */
    private static Object useIKExpression(String expression, String[] variables) throws Exception
    {
        WpLog.recordLog(LogLevelConstant.INFO, "Start to use IK Expression");
        // ����IK Expression�������
        List<Variable> varValues = new ArrayList<Variable>();
        for (String ele : variables)
        {
            if (!varValueMap.containsKey(ele) || varValueMap.get(ele) == null)
                throw new Exception(String.format("%s is undefined or null", ele));
            switch (varTypeMap.get(ele))
            {
            case DataTypeConstant.INT_SHORT:
                varValues.add(Variable.createVariable(ele, (int) varValueMap.get(ele)));
                break;
            case DataTypeConstant.BOOLEAN_SHORT:
                varValues.add(Variable.createVariable(ele, (boolean) varValueMap.get(ele)));
                break;
            case DataTypeConstant.CHAR_SHORT:
                varValues.add(Variable.createVariable(ele, (char) varValueMap.get(ele)));
                break;
            case DataTypeConstant.LONG_SHORT:
                varValues.add(Variable.createVariable(ele, (long) varValueMap.get(ele)));
                break;
            case DataTypeConstant.FLOAT_SHORT:
                varValues.add(Variable.createVariable(ele, (float) varValueMap.get(ele)));
                break;
            case DataTypeConstant.DOUBLE_SHORT:
                varValues.add(Variable.createVariable(ele, (double) varValueMap.get(ele)));
                break;
            case DataTypeConstant.STRING_SHORT:
                String value = (String) varValueMap.get(ele);
                if (value.charAt(0) == SignConstant.DOUBLE_QUOTE_CHAR && value.charAt(value.length() - 1) == SignConstant.DOUBLE_QUOTE_CHAR)
                    value = value.substring(1, value.length() - 1);
                varValues.add(Variable.createVariable(ele, value));
                break;
            case DataTypeConstant.DECIMAL_SHORT:
                // ��Java�е�BigDecimal��SQL�е�Decimal��Ӧ
                varValues.add(Variable.createVariable(ele, (BigDecimal) varValueMap.get(ele)));
                break;
            default:
                // Object����
                varValues.add(Variable.createVariable(ele, varValueMap.get(ele)));
                break;
            }
        }
        // ������ʽ
        return ExpressionEvaluator.evaluate(expression, varValues);
    }

    /**
     * ���ݱ������ͽ�������ֵ����ȷ�����ʹ���varValueMap�У�ֻ֧��int, long, boolean, char, String, Decimal, List
     * <E>, float, double���������Ͳ�����varValueMap��value������null
     * 
     * @param dataType ��������
     * @param varName ������
     * @param value ������ֵ������ǰ�����Ⱦ���trim
     */
    private static void assignValueToVarValueMap(String dataType, String varName, String value) throws Exception
    {
        // value������
        if (dataType.equals(DataTypeConstant.INT_SHORT))
        {
            try
            {
                // value��10�������͵�ֵ
                if (value.matches("-?[0-9]([0-9])*"))
                    varValueMap.put(varName, Integer.parseInt(value));

                // value��16�������͵�ֵ
                if (value.matches("-?0[x,X]([0-9,a-f,A-F])+"))
                {
                    value = value.substring(2);
                    varValueMap.put(varName, Integer.parseInt(value, 16));
                }

                // value��8�������͵�ֵ
                if (value.matches("-?0[1-9]([0-9])*"))
                    varValueMap.put(varName, Integer.parseInt(value, 8));
            }
            catch (Exception e1)
            {
                // ����int��Χ,����־����������˵��ֵ�����޶ȣ���caseֱ�Ӵ���
                throw new Exception(String.format("lineNumber: %d the value is out of range in %s", lineNumber, caseFileName));
            }
            return;
        }

        // value�ǳ�����
        if (dataType.equals(DataTypeConstant.LONG_SHORT))
        {
            try
            {
                // value��10�������͵�ֵ
                if (value.matches("-?[1-9]([0-9])*"))
                    varValueMap.put(varName, Long.parseLong(value));

                // value��16�������͵�ֵ
                if (value.matches("-?0[x,X]([0-9,a-f,A-F])+"))
                {
                    value = value.substring(2);
                    varValueMap.put(varName, Long.parseLong(value, 16));
                }

                // value��8�������͵�ֵ
                if (value.matches("-?0[1-9]([0-9])*"))
                    varValueMap.put(varName, Long.parseLong(value, 8));
            }
            catch (Exception e1)
            {
                // ����long��Χ,����־����������˵��ֵ�����޶ȣ���caseֱ�Ӵ���
                throw new Exception(String.format("lineNumber: %d the value is out of range in %s", lineNumber, caseFileName));
            }
            return;
        }

        // value��float���͵�ֵ
        if (dataType.equals(DataTypeConstant.FLOAT_SHORT))
        {
            try
            {
                varValueMap.put(varName, Float.parseFloat(value));
            }
            catch (Exception e)
            {
                // ����float��Χ������־����������˵��ֵ�����޶ȣ���caseֱ�Ӵ���
                throw new Exception(String.format("lineNumber: %d the value is out of range in %s", lineNumber, caseFileName));
            }
            return;
        }

        // value��double���͵�ֵ
        if (dataType.equals(DataTypeConstant.DOUBLE_SHORT))
        {
            try
            {
                varValueMap.put(varName, Double.parseDouble(value));
            }
            catch (Exception e)
            {
                // ����double��Χ������־����������˵��ֵ�����޶ȣ���caseֱ�Ӵ���
                throw new Exception(String.format("lineNumber: %d the value is out of range in %s", lineNumber, caseFileName));
            }
            return;
        }

        // value��boolean���͵�ֵ
        if (dataType.equals(DataTypeConstant.BOOLEAN_SHORT))
        {
            try
            {
                varValueMap.put(varName, Boolean.parseBoolean(value));
            }
            catch (Exception e)
            {
                // ����־����������˵��ʽ�д���caseֱ�Ӵ���
                throw new Exception(String.format("lineNumber: %d the value's format is wrong in %s", lineNumber, caseFileName));
            }
            return;
        }

        // value��char���͵�ֵ
        if (dataType.equals(DataTypeConstant.CHAR_SHORT))
        {
            try
            {
                varValueMap.put(varName, value.charAt(1));
            }
            catch (Exception e)
            {
                // ����־����������˵��ʽ�д���caseֱ�Ӵ���
                throw new Exception(String.format("lineNumber: %d the value's format is wrong in %s", lineNumber, caseFileName));
            }
            return;
        }

        // value��String���͵�ֵ��˳��ȥ��˫����
        if (dataType.equals(DataTypeConstant.STRING_SHORT))
        {
            try
            {
                varValueMap.put(varName, value.substring(1, value.length() - 1));
            }
            catch (Exception e)
            {
                // ����־����������˵��ʽ�д���caseֱ�Ӵ���
                throw new Exception(String.format("lineNumber: %d the value's format is wrong in %s", lineNumber, caseFileName));
            }
            return;
        }

        // value��Decimal���͵�ֵ
        if (dataType.equals(DataTypeConstant.DECIMAL_SHORT))
        {
            try
            {
                varValueMap.put(varName, new BigDecimal(value));
            }
            catch (Exception e)
            {
                // ����־����������˵��ʽ�д���caseֱ�Ӵ���
                throw new Exception(String.format("lineNumber: %d the value's format is wrong in %s", lineNumber, caseFileName));
            }
            return;
        }

        // value��List���͵�ֵ
        if (dataType.startsWith(DataTypeConstant.LIST))
        {
            // Ĭ��dataType���治�����κοո�����յ�����
            String[] values = Util.removeBlankElement(value.split("\\s+|,|\\[|]"));
            switch (dataType.substring(5, dataType.length() - 1).trim())
            {
            case DataTypeConstant.INT_SHORT:
                List<Integer> intList = new ArrayList<Integer>();
                for (String ele : values)
                    intList.add((int) convert(DataTypeConstant.INT_SHORT, ele));
                varValueMap.put(varName, intList);
                break;

            case DataTypeConstant.LONG_SHORT:
                List<Long> longList = new ArrayList<Long>();
                for (String ele : values)
                    longList.add((long) convert(DataTypeConstant.LONG_SHORT, ele));
                varValueMap.put(varName, longList);
                break;

            case DataTypeConstant.BOOLEAN_SHORT:
                List<Boolean> booleanList = new ArrayList<Boolean>();
                for (String ele : values)
                    booleanList.add((boolean) convert(DataTypeConstant.BOOLEAN_SHORT, ele));
                varValueMap.put(varName, booleanList);
                break;

            case DataTypeConstant.CHAR_SHORT:
                List<Character> charList = new ArrayList<Character>();
                for (String ele : values)
                    charList.add((char) convert(DataTypeConstant.CHAR_SHORT, ele));
                varValueMap.put(varName, charList);
                break;

            case DataTypeConstant.FLOAT_SHORT:
                List<Float> floatList = new ArrayList<Float>();
                for (String ele : values)
                    floatList.add((float) convert(DataTypeConstant.FLOAT_SHORT, ele));
                varValueMap.put(varName, floatList);
                break;

            case DataTypeConstant.DOUBLE_SHORT:
                List<Double> doubleList = new ArrayList<Double>();
                for (String ele : values)
                    doubleList.add((double) convert(DataTypeConstant.DOUBLE_SHORT, ele));
                varValueMap.put(varName, doubleList);
                break;

            case DataTypeConstant.STRING_SHORT:
                List<String> stringList = new ArrayList<String>();
                for (String ele : values)
                    stringList.add((String) convert(DataTypeConstant.STRING_SHORT, ele));
                varValueMap.put(varName, stringList);
                break;

            case DataTypeConstant.DECIMAL_SHORT:
                List<BigDecimal> bdList = new ArrayList<BigDecimal>();
                for (String ele : values)
                    bdList.add((BigDecimal) convert(DataTypeConstant.DECIMAL_SHORT, ele));
                varValueMap.put(varName, bdList);
                break;
            case DataTypeConstant.TIMESTAMP_SHORT:
                List<Timestamp> timestampList = new ArrayList<>();
                for (String ele : values)
                    timestampList.add((Timestamp) convert(DataTypeConstant.TIMESTAMP_SHORT, ele));
                varValueMap.put(varName, timestampList);
                break;
            default:
                throw new Exception(String.format("lineNumber: %d, List data type is wrong in %s", lineNumber, caseFileName));
            }
            return;
        }

        // value��Timestamp���͵�ֵ����ʽ���ϱ���yyyy-[m]m-[d]d [h]h:[m]m:[s]s[.f...]
        if (dataType.equals(DataTypeConstant.TIMESTAMP_SHORT))
        {
            varValueMap.put(varName, Timestamp.valueOf(value.substring(1, value.length() - 1)));
            return;
        }

        // ��������ֱ�ӷ��أ�������
        return;
    }

    /**
     * ���ַ���ת��Ϊָ���������͵�ֵ
     * 
     * @param dataType �������ͣ�ֻ֧��int,long,char,boolean,float,double,String,Decimal,Timestamp
     * @param value ֵ��һ��ֻ��һ��
     * @return ����ȷʱ����null
     */
    private static Object convert(String dataType, String value) throws Exception
    {
        // value������
        if (dataType.equals(DataTypeConstant.INT_SHORT))
        {
            try
            {
                // value��10�������͵�ֵ
                if (value.matches("-?[1-9]([0-9])*"))
                    return Integer.parseInt(value);
                // value��16�������͵�ֵ
                if (value.matches("-?0[x,X]([0-9,a-f,A-F])+"))
                {
                    value = value.substring(2);
                    return Integer.parseInt(value, 16);
                }
                // value��8�������͵�ֵ
                if (value.matches("-?0[1-9]([0-9])*"))
                    return Integer.parseInt(value, 8);
            }
            catch (Exception e1)
            {
                // ����int��Χ,����־����������˵��ֵ�����޶ȣ���caseֱ�Ӵ���
                throw new Exception(String.format("lineNumber: %d the value is out of range in %s", lineNumber, caseFileName));
            }
            return null;
        }
        // value�ǳ�����
        if (dataType.equals(DataTypeConstant.LONG_SHORT))
        {
            try
            {
                // value��10�������͵�ֵ
                if (value.matches("-?[1-9]([0-9])*"))
                    return Long.parseLong(value);
                // value��16�������͵�ֵ
                if (value.matches("-?0[x,X]([0-9,a-f,A-F])+"))
                {
                    value = value.substring(2);
                    return Long.parseLong(value, 16);
                }
                // value��8�������͵�ֵ
                if (value.matches("-?0[1-9]([0-9])*"))
                    return Long.parseLong(value, 8);
            }
            catch (Exception e1)
            {
                // ����long��Χ,����־����������˵��ֵ�����޶ȣ���caseֱ�Ӵ���
                throw new Exception(String.format("lineNumber: %d the value is out of range in %s", lineNumber, caseFileName));
            }
            return null;
        }
        // value��float���͵�ֵ
        if (dataType.equals(DataTypeConstant.FLOAT_SHORT))
        {
            try
            {
                return Float.parseFloat(value);
            }
            catch (Exception e)
            {
                // ����float��Χ������־����������˵��ֵ�����޶ȣ���caseֱ�Ӵ���
                throw new Exception(String.format("lineNumber: %d the value is out of range in %s", lineNumber, caseFileName));
            }
        }
        // value��double���͵�ֵ
        if (dataType.equals(DataTypeConstant.DOUBLE_SHORT))
        {
            try
            {
                return Double.parseDouble(value);
            }
            catch (Exception e)
            {
                // ����double��Χ������־����������˵��ֵ�����޶ȣ���caseֱ�Ӵ���
                throw new Exception(String.format("lineNumber: %d the value is out of range in %s", lineNumber, caseFileName));
            }
        }
        // value��boolean���͵�ֵ
        if (dataType.equals(DataTypeConstant.BOOLEAN_SHORT))
        {
            try
            {
                return Boolean.parseBoolean(value);
            }
            catch (Exception e)
            {
                // ����־����������˵��ʽ�д���caseֱ�Ӵ���
                throw new Exception(String.format("lineNumber: %d the value's format is wrong in %s", lineNumber, caseFileName));
            }
        }
        // value��char���͵�ֵ
        if (dataType.equals(DataTypeConstant.CHAR_SHORT))
        {
            try
            {
                return value.charAt(1);
            }
            catch (Exception e)
            {
                // ����־����������˵��ʽ�д���caseֱ�Ӵ���
                throw new Exception(String.format("lineNumber: %d the value's format is wrong in %s", lineNumber, caseFileName));
            }
        }
        // value��String���͵�ֵ��˳��ȥ��˫����
        if (dataType.equals(DataTypeConstant.STRING_SHORT))
        {
            try
            {
                return value.substring(1, value.length() - 1);
            }
            catch (Exception e)
            {
                // ����־����������˵��ʽ�д���caseֱ�Ӵ���
                throw new Exception(String.format("lineNumber: %d the value's format is wrong in %s", lineNumber, caseFileName));
            }
        }
        // value��Decimal���͵�ֵ
        if (dataType.equals(DataTypeConstant.DECIMAL_SHORT))
        {
            try
            {
                return new BigDecimal(value);
            }
            catch (Exception e)
            {
                // ����־����������˵��ʽ�д���caseֱ�Ӵ���
                throw new Exception(String.format("lineNumber: %d the value's format is wrong in %s", lineNumber, caseFileName));
            }
        }
        // value��Timestamp���͵�ֵ��˳��ȥ��˫����
        if (dataType.equals(DataTypeConstant.TIMESTAMP_SHORT))
        {
            try
            {
                return Timestamp.valueOf(value.substring(1, value.length() - 1));
            }
            catch (Exception e)
            {
                // ����־����������˵��ʽ�д���caseֱ�Ӵ���
                throw new Exception(String.format("lineNumber: %d the value's format is wrong in %s", lineNumber, caseFileName));
            }
        }
        return null;
    }

    /**
     * Get the keyword class according to class name, if it doesn't exists in cache, it
     * should be load into jvm
     * 
     * @param className Include package name
     * @return
     */
    private static Class<?> getClass(String className)
    {
        if (cache.containsKey(className))
            return cache.get(className);
        else
        {
            try
            {
                Class<?> keywordClass = Class.forName(className);
                cache.put(className, keywordClass);
                return keywordClass;
            }
            catch (ClassNotFoundException e)
            {
                e.printStackTrace();
                // It shouldn't happen, system must exit when it happen
                System.exit(1);
            }
            return null;
        }
    }

    public static void setVarValueMap(Map<String, Object> varValueMap)
    {
        Executor.varValueMap = varValueMap;
    }

    public static Map<String, Object> getVarValueMap()
    {
        return Executor.varValueMap;
    }

    public static void setVarTypeMap(Map<String, String> varTypeMap)
    {
        Executor.varTypeMap = varTypeMap;
    }

    public static void setExceptionString(String exceptionString)
    {
        Executor.exceptionString = exceptionString;
    }
    
    public static Connection getConnection()
    {
        return Executor.connection;
    }
    
    public static String getMIdresult(int index)
    {
    	String str = null;
    	if(index >= midResultStatement.length)
    		str = "outofindex";
    	else
    		str = midResultStatement[index].toString();
        return str;
    }
}
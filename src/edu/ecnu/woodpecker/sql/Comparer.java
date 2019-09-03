package edu.ecnu.woodpecker.sql;

import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.servlet.jsp.jstl.sql.Result;

import edu.ecnu.woodpecker.constant.DataTypeConstant;
import edu.ecnu.woodpecker.constant.LogLevelConstant;
import edu.ecnu.woodpecker.constant.SignConstant;
import edu.ecnu.woodpecker.log.WpLog;

/**
 * ֧�ֲ��Է��ؽ��������ȷ������ĶԱ�
 */
public class Comparer
{

    /**
     * ֧������Ԫ�صĶԱ� ֧�ֵ����������У� �������ͣ�boolean��char��int��long��float��double
     * �������ͣ�Boolean��Character��Integer��Long��Float��Double��String��
     * BigDecimal��ResultSet��IdealResultSet. input1��input2�������ͱ�����ͬ������ͬ�򷵻�false��
     * ResultSet��IdealResultSet���߼��Ͽ���Ϊ����ͬ���������ͣ��Ƚ�ʱIdealResultSet
     * ������Ϊ��һ��������������������֮���֧�֡�==���Ƚ� �����������ݴ������Զ�ת��Ϊ��Ӧ�Ķ�������
     * 
     * @param input1 ���Աȵ�����Ԫ��֮һ
     * @param input2 ���Աȵ�����Ԫ��֮һ
     * @param operator �Ƚ����������Ϊ��>��>=��<��<=��==
     * @param isSetType True means the result set's row order is not matter
     * @return �ȽϽ����input1��input2����򷵻�true�����򷵻�false
     */
    public static <T> boolean verify(T input1, String operator, T input2, boolean isSetType)
    {
        String dataType1 = input1.getClass().getName();
        String dataType2 = input2.getClass().getName();
        boolean result = false;

        if (dataType1.matches(".*IdealResultSet") && dataType2.matches(".*ResultImpl"))
        {
        	if (!operator.equals(SignConstant.EQUAL) && 
            		!operator.equals(SignConstant.ALL_ARE) &&
            		!operator.equals(SignConstant.CONTAIN))
            {
                WpLog.recordLog(LogLevelConstant.ERROR,
                        "Unsupport relation operator %s when compare IdealResultSet and ResultSet", operator, null);
                return false;
            }

            // ���÷������IdealResultSet��equals����
            try
            {
                return (boolean) input1.getClass().getMethod("equals", Result.class, boolean.class).invoke(input1, input2, isSetType);
            }
            catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException
                    | InvocationTargetException e)
            {
                WpLog.recordLog(LogLevelConstant.ERROR, WpLog.getExceptionInfo(e));
                return result;
            }
        }
        else if (dataType1.matches(".*ResultImpl") && dataType2.matches(".*ResultImpl"))
        {
            // �Աȵ����������������ʵ��ѯ�õ���
        	if (!operator.equals(SignConstant.EQUAL) &&
            		!operator.equals(SignConstant.ALL_ARE) &&
            		!operator.equals(SignConstant.CONTAIN))
            {
                WpLog.recordLog(LogLevelConstant.ERROR,
                        "Unsupport relation operator %s when compare IdealResultSet and ResultSet", operator);
                return false;
            }
        	if(operator.equals(SignConstant.EQUAL))
            {
            	return equals((Result) input1, (Result) input2, isSetType, false, false);
            }
            else if(operator.equals(SignConstant.ALL_ARE))
            {
            	return equals((Result) input1, (Result) input2, true, true, false);
            }
            else
            {
            	return equals((Result) input1, (Result) input2, true, false, true);
            }
        }
        else if (!dataType1.equals(dataType2))
        {
            return false;
        }

        if (operator.equals(SignConstant.EQUAL))
        {
            result = input1.equals(input2);
        }
        else if (operator.equals(SignConstant.GT_STR))
        {
            result = greaterThan(input1, input2);
        }
        else if (operator.equals(SignConstant.GTOE))
        {
            result = equalOrGreaterThan(input1, input2);
        }
        else if (operator.equals(SignConstant.LT_STR))
        {
            result = lessThan(input1, input2);
        }
        else if (operator.equals(SignConstant.LTOE))
        {
            result = equalOrLessThan(input1, input2);
        }
        else if (operator.equals(SignConstant.NON_EQUAL))
        {
            result = !input1.equals(input2);
        }
        else
        {
            WpLog.recordLog(LogLevelConstant.ERROR, "Unsupport relation operator %s", operator);
            return false;
        }
        return result;
    }

    /**
     * ֧����������ĶԱ� ֧�ֵ����������У�Boolean[]��Character[]��Integer[]��Long[]��Float[]��
     * Double[]��String[]��BigDecimal[] input1��input2�������ͱ�����ͬ������ͬ�򷵻�false��
     * 
     * @param input1 ���Աȵ�����Ԫ��֮һ
     * @param input2 ���Աȵ�����Ԫ��֮һ
     * @param operator �Ƚ����������Ϊ��>��>=��<��<=��==
     * @return �ȽϽ����input1��input2��Ԫ�ذ�������򷵻�true�����򷵻�false
     */
    public static <T> boolean verify(T[] input1, String operator, T[] input2)
    {
        if (input1.length != input2.length)
            return false;
        for (int i = 0; i < input1.length; i++)
        {
            boolean result = verify(input1[i], operator, input2[i], false);
            if (result == false)
                return false;
        }
        return true;
    }
    
    /**
     * ֧����������ĶԱ� ֧�ֵ����������У�Boolean[]��Character[]��Integer[]��Long[]��Float[]��
     * Double[]��String[]��BigDecimal[] input1[]��input2�������ͱ�����ͬ������ͬ�򷵻�false��
     * 
     * @param input1[] ���Աȵ�����Ԫ��֮һ
     * @param input2 ���Աȵ�����Ԫ��֮һ
     * @param operator ΪALL_ARE
     * @return �ȽϽ����input1��input2��Ԫ�ذ�������򷵻�true�����򷵻�false
     */
    public static <T> boolean verify(T[] input1, String operator, T input2)
    {
        for (int i = 0; i < input1.length; i++)
        {
            boolean result = verify(input1[i], SignConstant.EQUAL, input2, false);
            if (result == false)
                return false;
        }
        return true;
    }

    /**
     * ֧�������б�ĶԱ� ֧�ֵ����������У�List<Boolean>��List<Character>��List<Integer>��List<Long>�� List
     * <Float>��List<Double>��List<String>��List <BigDecimal>
     * input1��input2�������ͱ�����ͬ������ͬ�򷵻�false��
     * 
     * @param input1 ���Աȵ�����Ԫ��֮һ
     * @param input2 ���Աȵ�����Ԫ��֮һ
     * @param operator �Ƚ����������Ϊ��>��>=��<��<=��==
     * @return �ȽϽ����input1��input2��Ԫ�ذ�������򷵻�true�����򷵻�false
     */
    public static <T> boolean verify(List<T> input1, String operator, List<T> input2)
    {
        if (input1.size() != input2.size())
            return false;
        for (int i = 0; i < input1.size(); i++)
        {
            boolean result = verify(input1.get(i), operator, input2.get(i), false);
            if (result == false)
                return false;
        }
        return true;
    }

    /**
     * ��>���Ƚϵ�ʵ�� ֧�ֵ����������У�Integer��Long��Float��Double��BigDecimal
     * 
     * @param input1 ��Ԫ��
     * @param input2 ��Ԫ��
     * @return �ȽϵĽ��
     */
    private static <T> boolean greaterThan(T input1, T input2)
    {
        boolean result = false;
        String dataType = input1.getClass().getName();
        if (dataType.equals(DataTypeConstant.INT_FULL))
        {
            result = new Integer(input1.toString()) > new Integer(input2.toString()) ? true : false;
        }
        else if (dataType.equals(DataTypeConstant.LONG_FULL))
        {
            result = new Long(input1.toString()) > new Long(input2.toString()) ? true : false;
        }
        else if (dataType.equals(DataTypeConstant.FLOAT_FULL))
        {
            result = new Float(input1.toString()) > new Float(input2.toString()) ? true : false;
        }
        else if (dataType.equals(DataTypeConstant.DOUBLE_FULL))
        {
            result = new Double(input1.toString()) > new Double(input2.toString()) ? true : false;
        }
        else if (dataType.equals(DataTypeConstant.DECIMAL_FULL))
        {
            result = new BigDecimal(input1.toString()).subtract(new BigDecimal(input2.toString())).doubleValue() > 0 ? true : false;
        }
        return result;
    }

    /**
     * ��>=���Ƚϵ�ʵ�� ֧�ֵ����������У�Integer��Long��Float��Double��BigDecimal
     * 
     * @param input1 ��Ԫ��
     * @param input2 ��Ԫ��
     * @return �ȽϵĽ��
     */
    private static <T> boolean equalOrGreaterThan(T input1, T input2)
    {
        boolean result = false;
        String dataType = input1.getClass().getName();
        if (dataType.equals(DataTypeConstant.INT_FULL))
        {
            result = new Integer(input1.toString()) >= new Integer(input2.toString()) ? true : false;
        }
        else if (dataType.equals(DataTypeConstant.LONG_FULL))
        {
            result = new Long(input1.toString()) >= new Long(input2.toString()) ? true : false;
        }
        else if (dataType.equals(DataTypeConstant.FLOAT_FULL))
        {
            result = new Float(input1.toString()) >= new Float(input2.toString()) ? true : false;
        }
        else if (dataType.equals(DataTypeConstant.DOUBLE_FULL))
        {
            result = new Double(input1.toString()) >= new Double(input2.toString()) ? true : false;
        }
        else if (dataType.equals(DataTypeConstant.DECIMAL_FULL))
        {
            result = new BigDecimal(input1.toString()).subtract(new BigDecimal(input2.toString())).doubleValue() >= 0 ? true : false;
        }
        return result;
    }

    /**
     * ��<���Ƚϵ�ʵ�� ֧�ֵ����������У�Integer��Long��Float��Double��BigDecimal
     * 
     * @param input1 ��Ԫ��
     * @param input2 ��Ԫ��
     * @return �ȽϵĽ��
     */
    private static <T> boolean lessThan(T input1, T input2)
    {
        boolean result = false;
        String dataType = input1.getClass().getName();
        if (dataType.equals(DataTypeConstant.INT_FULL))
        {
            result = new Integer(input1.toString()) < new Integer(input2.toString()) ? true : false;
        }
        else if (dataType.equals(DataTypeConstant.LONG_FULL))
        {
            result = new Long(input1.toString()) < new Long(input2.toString()) ? true : false;
        }
        else if (dataType.equals(DataTypeConstant.FLOAT_FULL))
        {
            result = new Float(input1.toString()) < new Float(input2.toString()) ? true : false;
        }
        else if (dataType.equals(DataTypeConstant.DOUBLE_FULL))
        {
            result = new Double(input1.toString()) < new Double(input2.toString()) ? true : false;
        }
        else if (dataType.equals(DataTypeConstant.DECIMAL_FULL))
        {
            result = new BigDecimal(input1.toString()).subtract(new BigDecimal(input2.toString())).doubleValue() < 0 ? true : false;
        }
        return result;
    }

    /**
     * ��<=���Ƚϵ�ʵ�� ֧�ֵ����������У�Integer��Long��Float��Double��BigDecimal
     * 
     * @param input1 ��Ԫ��
     * @param input2 ��Ԫ��
     * @return �ȽϵĽ��
     */
    private static <T> boolean equalOrLessThan(T input1, T input2)
    {
        boolean result = false;
        String dataType = input1.getClass().getName();
        if (dataType.equals(DataTypeConstant.INT_FULL))
        {
            result = new Integer(input1.toString()) <= new Integer(input2.toString()) ? true : false;
        }
        else if (dataType.equals(DataTypeConstant.LONG_FULL))
        {
            result = new Long(input1.toString()) <= new Long(input2.toString()) ? true : false;
        }
        else if (dataType.equals(DataTypeConstant.FLOAT_FULL))
        {
            result = new Float(input1.toString()) <= new Float(input2.toString()) ? true : false;
        }
        else if (dataType.equals(DataTypeConstant.DOUBLE_FULL))
        {
            result = new Double(input1.toString()) <= new Double(input2.toString()) ? true : false;
        }
        else if (dataType.equals(DataTypeConstant.DECIMAL_FULL))
        {
            result = new BigDecimal(input1.toString()).subtract(new BigDecimal(input2.toString())).doubleValue() <= 0 ? true : false;
        }
        return result;
    }

    /**
     * �Ա�������ʵ������Ƿ����
     * 
     * @param result1
     * @param result2
     * @param isSetType True means result set's row order is not matter
     * @return
     */
    private static boolean equals(Result result1, Result result2, boolean isSetType, boolean isAllAre, boolean isContain)
    {
        // ��С��һ�������
    	if(result1.getColumnNames().length != result2.getColumnNames().length)
    		return false;
    	if(!isAllAre && !isContain)
    	{
    		if (result1.getRowCount() != result2.getRowCount())
    			return false;
    	}
    	else
    	{
    		if(isContain)
    			if (result1.getRowCount() < result2.getRowCount())
        			return false;
    	}
        // Compare strictly
        Object[][] realData1 = result1.getRowsByIndex();
        Object[][] realData2 = result2.getRowsByIndex();
        if (!isSetType)
        {
            for (int i = 0; i < realData1.length; i++)
            {
                for (int j = 0; j < realData1[0].length; j++)
                {
                    if (realData1[i][j] == null || realData2[i][j] == null)
                    {
                        if (realData1[i][j] != realData2[i][j])
                            return false;
                        continue;
                    }
                    if (!realData1[i][j].equals(realData2[i][j]))
                        return false;
                }
            }
            return true;
        }
        // Compare with set type, violent method
        if(isContain)
        {
          //�Á�̎��Y�����а���������ͬԪ�أ�ͨ�^compareSet�������^��һ���Y�������ѽ������^��Ԫ�ء�
            Set<Integer> comparedSet = new HashSet<>();
            for (int j = 0; j < realData2.length; j++)
            {
                boolean findOneRowMatch = false;
                for (int i = 0; i < realData1.length; i++)
                {
                    if (comparedSet.contains(j))
                        continue;
                    if (equalsOneRow(realData1[i], realData2[j]))
                    {
                        findOneRowMatch = true;               
                        comparedSet.add(j);
                        break;
                    }
                } 
                if (!findOneRowMatch)
                    return false;
            } 
        }
        else
        {
            //�Á�̎��Y�����а���������ͬԪ�أ�ͨ�^compareSet�������^��һ���Y�������ѽ������^��Ԫ�ء�
            Set<Integer> comparedSet = new HashSet<>();
            for (int i = 0; i < realData1.length; i++)
            {
                boolean findOneRowMatch = false;
                for (int j = 0; j < realData2.length; j++)
                {
                    if(!isAllAre)
                    {
                        if (comparedSet.contains(j))
                            continue;
                    } 
                    if (equalsOneRow(realData1[i], realData2[j]))
                    {
                        findOneRowMatch = true;
                        if(!isAllAre)
                        {
                            comparedSet.add(j);
                        }
                        break;
                    }
                } 
                if (!findOneRowMatch)
                    return false;
            }
        }
            
        
        
        
        return true;
    }

    /**
     * Return the compare result of two rows
     * 
     * @param row1
     * @param row2
     * @return
     */
    private static boolean equalsOneRow(Object[] row1, Object[] row2)
    {
        if (row1.length != row2.length)
            return false;
        for (int i = 0; i < row1.length; i++)
        {
            System.out.println(row1[i]);
            System.out.println(row2[i]);
            if(row1[i] == null && row2[i] == null)
            {
                continue;
            }
            if(row1[i] == null || row2[i] == null)
            {
                return false;
            }
            if (!row1[i].equals(row2[i]))
                return false;
        }
        return true;
    }
}

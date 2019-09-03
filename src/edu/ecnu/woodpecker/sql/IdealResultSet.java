package edu.ecnu.woodpecker.sql;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.servlet.jsp.jstl.sql.Result;

import edu.ecnu.woodpecker.constant.DataType;
import edu.ecnu.woodpecker.constant.DataValueConstant;
import edu.ecnu.woodpecker.constant.FileConstant;
import edu.ecnu.woodpecker.constant.LogLevelConstant;
import edu.ecnu.woodpecker.controller.TestController;
import edu.ecnu.woodpecker.log.WpLog;

/**
 * �Զ���ķ��ؽ������������ ��Ҫ���ܣ��ṩ���������͵�֧�֣�֧����ȷ������ĵ���
 */
public class IdealResultSet
{

    /**
     * ���ؽ����ÿ�����Ե���������
     */
    private DataType[] dataTypes = null;

    /**
     * ���ַ������ʹ洢��������
     */
    private String[][] data = null;

    public IdealResultSet()
    {}

    public void setDataTypes(DataType[] dataTypes)
    {
        this.dataTypes = dataTypes;
    }

    public void setData(List<ArrayList<String>> data)
    {
        int rowSize = data.size();
        int columnSize = data.get(0).size();
        this.data = new String[rowSize][columnSize];
        for (int i = 0; i < rowSize; i++)
            data.get(i).toArray(this.data[i]);
    }

    public String[][] getData()
    {
        return data;
    }

    /**
     * ��һ�����ݿ�������ؽ�������бȽ� Ĭ�����ݿ�������ؽ������schema���Լ���ͬ���Դ˲���������ж�
     * 
     * @param result ���ؽ�������Ƚ϶���
     * @param isSetType True means result set's row order is not matter
     * @return ��ȫ��ͬ����true�����򷵻�false
     */
    public boolean equals(Result result, boolean isSetType)
    {
        WpLog.recordLog(LogLevelConstant.INFO, "compare query result set with ideal result set");
        WpLog.recordQueryAndIdealResultSet(LogLevelConstant.INFO, result, this);
        int rowCount = result.getRowCount();
        int columnCount = result.getColumnNames().length;
        if (rowCount != data.length || columnCount != dataTypes.length)
        {
            // The sizes of two result set are different
            return false;
        }

        Object[][] realResultSetData = result.getRowsByIndex();
        // Compare strictly
        if (!isSetType)
        {
            for (int i = 0; i < rowCount; i++)
            {
                for (int j = 0; j < columnCount; j++)
                {
                    if (!equalsOneElement(realResultSetData[i][j], i, j))
                        return false;
                }
            }
            return true;
        }
        // Compare with set type, violent method
        Set<Integer> comparedSet = new HashSet<>();
        for (int i = 0; i < rowCount; i++)
        {
            boolean findOneRowMatch = false;
            for (int j = 0; j < rowCount; j++)
            {
                if (comparedSet.contains(j))
                    continue;
                if (equalsOneRow(i, realResultSetData[j]))
                {
                    findOneRowMatch = true;
                    comparedSet.add(j);
                    break;
                }
            }
            if (!findOneRowMatch)
                return false;
        }
        return true;
    }

    @Override
    public String toString()
    {
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < data.length; i++)
            stringBuilder.append(FileConstant.TAB_CHAR).append(Arrays.toString(data[i])).append(FileConstant.LINUX_LINE_FEED);
        return stringBuilder.toString();
    }
    
    /**
     * Get the data according to specified row and column
     * @param row
     * @param column
     * @return
     */
    public Object getDataByIndex(int row, int column)
    {
        return data[row][column];
    }
    
    /**
     * ���ļ��е�����ȷ�Ľ����
     * 
     * @param file �������ŵ��ļ�
     * @param idealResultSets
     * @param positions 
     */
    public static void importIRS(File file, IdealResultSet[] idealResultSets, int[] positions)
    {
        WpLog.recordLog(LogLevelConstant.INFO, "Start to import ideal result set");
        for (int i = 0; i < idealResultSets.length; i++)
            importOneResult(file, idealResultSets[i], positions[i]);
    }
    
    /**
     * ��ͬ�����֮��ķָ���Ϊ��һ�С�[-]+.*������ͬ��¼֮��ķָ���Ϊ����\n�����ߡ�\r\n���� ��ͬԪ��֮��ķָ���Ϊ����|��
     * 
     * @param file
     * @param idealResultSet
     * @param position
     */
    private static void importOneResult(File file, IdealResultSet idealResultSet, int position)
    {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), FileConstant.UTF_8)))
        {
            WpLog.recordLog(LogLevelConstant.INFO, "Import one ideal result set");
            String inputLine = null;
            int index = 0; // To index the number of ideal result set
            List<ArrayList<String>> data = new ArrayList<ArrayList<String>>();
            ArrayList<String> rowValue = new ArrayList<String>();

            while ((inputLine = reader.readLine()) != null)
            {
                if (inputLine.matches("\\s*(--).*"))  // Split line
                {
                    if (index == position)
                    {
                        // Import an ideal result set
                        idealResultSet.setData(data);
                        WpLog.recordIdealResultSet(LogLevelConstant.INFO, idealResultSet);
                        break;
                    }
                    index++;
                    continue;
                }
                if (index == position)
                {
                    // Get an row of an ideal result set
                    rowValue.addAll(Arrays.asList(importOneLine(inputLine.split("\\|"))));
                    data.add(rowValue);
                    rowValue = new ArrayList<String>();
                }
            }
        }
        catch (IOException e)
        {
            WpLog.recordLog(LogLevelConstant.ERROR, WpLog.getExceptionInfo(e));
        }
    }

    /**
     * ÿ�ε���һ�����ݣ�ÿ�е�String����trim�������ַ����Ὣ���ߵĵ�����ȥ��
     * 
     * @param arrays ��'|'����ÿ�еõ�������
     * @return
     */
    private static String[] importOneLine(String[] arrays)
    {
        WpLog.recordLog(LogLevelConstant.INFO, "Import one line into ideal result set");
        List<String> list = new ArrayList<String>();
        int countSingleQuotation = 0;
        StringBuilder strColumnValue = new StringBuilder();
        for (String ele : arrays)
        {
            // ����
            if (ele.matches("^\\s*$"))
                continue;

            if (ele.matches("^\\s*'.*'\\s*$") && !ele.trim().endsWith("\\'"))
            {
                // һ���������ַ���
                ele = ele.replace("\\'", "'");
                ele = ele.replace("\\\\", "\\");
                list.add(ele.trim().substring(1, ele.trim().length() - 1));
            }
            else if (ele.matches("^\\s*'.*$"))
            {
                // һ���ַ�����ͷ����ֹ�����ַ������пո�պñ��ָ���Բ�trim
                countSingleQuotation++;
                ele = ele.replace("\\'", "'");
                ele = ele.replace("\\\\", "\\");
                strColumnValue.append(ele);
            }
            else if (ele.matches("^.*\'\\s*$"))
            {
                // һ���ַ�������
                countSingleQuotation--;
                ele = ele.replace("\\'", "'");
                ele = ele.replace("\\\\", "\\");
                strColumnValue.append("|" + ele);
                String tmp = strColumnValue.toString().trim();
                // ȥ���ַ������ߵĵ�����
                list.add(tmp.substring(1, tmp.length() - 1).trim());
                strColumnValue.delete(0, strColumnValue.length());
            }
            else if (countSingleQuotation != 0)
            {
                // �ַ�����һ����ɲ���
                ele = ele.replace("\\'", "'");
                ele = ele.replace("\\\\", "\\");
                strColumnValue.append("|" + ele);
            }
            else
            {
                // ���ַ���
                ele = ele.trim();
                // ����Ǵ�дNULL��תΪСд
                if(ele.equals(DataValueConstant.NULL_UPPER))
                    list.add(ele.toLowerCase());
                else
                    list.add(ele);
            }
        }
        return list.toArray(new String[0]);
    }
    
    /**
     * 
     * @param row The row index of ideal result set
     * @param realResultSetDataRow The row of real result set
     * @return
     */
    private boolean equalsOneRow(int row, Object[] realResultSetDataRow)
    {
        int columnCount = realResultSetDataRow.length;
        for (int column = 0; column < columnCount; column++)
        {
            if (!equalsOneElement(realResultSetDataRow[column], row, column))
                return false;
        }
        return true;
    }
    
    /**
     * Return the compare result of one element of real data with ideal result set's
     * specified element
     * 
     * @param realDataElement
     * @param row  The row position of ideal result set
     * @param column  The column position of ideal result set
     * @return false if exist non-equal element
     */
    private boolean equalsOneElement(Object realDataElement, int row, int column)
    {
        // ���в�����򷵻�false
        boolean equal = true;
        switch (dataTypes[column])
        {
        case INT:
            if (data[row][column].equals(DataValueConstant.NULL_LOWER))
                equal = realDataElement == null ? true : false;
            else
            {
                if (TestController.getDatabase() == DbmsBrand.CEDAR)
                {
                    // Ϊĳ�����ݿⵥ����ʵ���߼�������int���ص�ʱ����long��ʾ
                    equal = realDataElement == null ? false : Long.parseLong(data[row][column]) == Long.parseLong(realDataElement.toString());
                    break;
                }
                equal = realDataElement == null ? false : Integer.parseInt(data[row][column]) == Integer.parseInt(realDataElement.toString());
            }
            break;
        case LONG:
            if (data[row][column].equals(DataValueConstant.NULL_LOWER))
                equal = realDataElement == null ? true : false;
            else
            {
                equal = realDataElement == null ? false : Long.parseLong(data[row][column]) == Long.parseLong(realDataElement.toString());
            }
            break;
        case FLOAT:
            if (data[row][column].equals(DataValueConstant.NULL_LOWER))
                equal = realDataElement == null ? true : false;
            else
            {
                equal = realDataElement == null ? false : Float.parseFloat(data[row][column]) == Float.parseFloat(realDataElement.toString());
            }
            break;
        case DOUBLE:
            if (data[row][column].equals(DataValueConstant.NULL_LOWER))
                equal = realDataElement == null ? true : false;
            else
            {
                equal = realDataElement == null ? false : Double.parseDouble(data[row][column]) == Double.parseDouble(realDataElement.toString());
            }
            break;
        case STRING:
            if (data[row][column].equals(DataValueConstant.NULL_LOWER))
                equal = realDataElement == null ? true : false;
            else
                equal = realDataElement == null ? false : data[row][column].equals((String) realDataElement);
            break;
        case DECIMAL:
            if (data[row][column].equals(DataValueConstant.NULL_LOWER))
                equal = realDataElement == null ? true : false;
            else
            {
                equal = realDataElement == null ? false
                        : new BigDecimal(data[row][column]).compareTo(new BigDecimal(realDataElement.toString())) == 0 ? true : false;
            }
            break;
        case BOOLEAN:
            if (data[row][column].equals(DataValueConstant.NULL_LOWER))
                equal = realDataElement == null ? true : false;
            else
            {
                if (realDataElement.toString().matches("(\\p{Digit})+"))
                {
                    int real = Integer.parseInt(realDataElement.toString());
                    boolean ideal = Boolean.parseBoolean(data[row][column]);
                    equal = (real == 1 && ideal) || (real == 0 && !ideal) ? true : false;
                }
                else
                {
                    equal = Boolean.parseBoolean(data[row][column]) == Boolean.parseBoolean(realDataElement.toString()) ? true
                            : false;
                }
            }
            break;
        case TIMESTAMP:
            if (data[row][column].equals(DataValueConstant.NULL_LOWER))
                equal = realDataElement == null ? true : false;
            else
            {
                equal = realDataElement == null ? false
                        : Timestamp.valueOf(data[row][column]).compareTo((Timestamp) realDataElement) == 0 ? true : false;
            }
            break;
        default:
            WpLog.recordLog(LogLevelConstant.ERROR, "Unsupported data type: %s", dataTypes[column].getShortName());
            equal = false;
        }
        return equal;
    }
}

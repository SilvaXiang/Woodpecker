package edu.ecnu.woodpecker.util;

/**
 * ����־��ص���
 */
public final class Log
{
    /**
     * ���ص��ú��������࣬���������ļ��к�
     * 
     * @return
     */
    public static String getRecordMetadata()
    {
        StackTraceElement element = Thread.currentThread().getStackTrace()[2];
        return String.format("%s->%s:%d", element.getClassName(), element.getMethodName(), element.getLineNumber());
    }
}

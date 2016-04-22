package com.sandroid.freetracker;

import android.net.Uri;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;


public class RunerDBProviderTest {
    //Основной мок объект
    private RunerDBProvider mockedDBProvider;

    //Константы
    public final Uri testUri= Uri.parse("Test");;
    public final String[] testStringArray=  {"test"};
    public final String testString = "test";

    @Before
    public void setUp() throws Exception {
        mockedDBProvider = mock(RunerDBProvider.class);
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void testOnCreate() throws Exception {
        //onCreate будет возвращать true
        when(mockedDBProvider.onCreate()).thenReturn(true);
        //тест
        assertTrue(mockedDBProvider.onCreate());
        //проверка вызова
        verify(mockedDBProvider).onCreate();
    }

    @Test
    public void testQuery() throws Exception {
        //select будет возвращать null
        when(mockedDBProvider.query(Uri.EMPTY, testStringArray, testString, testStringArray,testString)).thenReturn(null);
        //тест
        assertNull(mockedDBProvider.query(Uri.EMPTY, testStringArray, testString, testStringArray, testString));
        //проверка вызова
        verify(mockedDBProvider).query(Uri.EMPTY, testStringArray, testString, testStringArray, testString);
    }

    @Test
    public void testGetType() throws Exception {
        //тест
        assertEquals(null, mockedDBProvider.getType(Uri.EMPTY));
        //проверка вызова
        verify(mockedDBProvider).getType(Uri.EMPTY);
    }

    @Test
    public void testInsert() throws Exception {
        //Инсерт будет возвращать пустой URY
        when(mockedDBProvider.insert(Uri.EMPTY, null)).thenReturn(Uri.EMPTY);
        //тест
        assertEquals(Uri.EMPTY, mockedDBProvider.insert(Uri.EMPTY, null));
        //проверка вызова
        verify(mockedDBProvider).insert(Uri.EMPTY, null);
    }

    @Test
    public void testBulkInsert() throws Exception {
        //Пустая вставка будет возвращать 1
        when(mockedDBProvider.bulkInsert(Uri.EMPTY, null)).thenReturn(1);
        //тест
        assertEquals(1, mockedDBProvider.bulkInsert(Uri.EMPTY, null));
        //проверка вызова
        verify(mockedDBProvider).bulkInsert(Uri.EMPTY, null);
    }

    @Test
    public void testDelete() throws Exception {
        //Удаление будет возвращать 1
        when(mockedDBProvider.delete(Uri.EMPTY, testString, testStringArray)).thenReturn(1);
        //тест
        assertEquals(1, mockedDBProvider.delete(Uri.EMPTY, testString, testStringArray));
        //проверка вызова
        verify(mockedDBProvider).delete(Uri.EMPTY, testString, testStringArray);
    }

    @Test
    public void testUpdate() throws Exception {
        //Update будет возвращать 1
        when(mockedDBProvider.update(Uri.EMPTY, null, testString, testStringArray)).thenReturn(1);
        //тест
        assertEquals(1, mockedDBProvider.update(Uri.EMPTY, null, testString, testStringArray));
        //проверка вызова
        verify(mockedDBProvider).update(Uri.EMPTY, null, testString, testStringArray);
    }
 }
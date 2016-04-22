package com.sandroid.freetracker;

import junit.framework.TestCase;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.TimeZone;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


public class WayTest extends TestCase {

    public Way TestWay;     //Основной маршрут
    public Way NullWay;     //Пустой маршрут
    public Long date1;      //Дата для точки маршрута 1
    public Long date2;      //Дата для точки маршрута 2
    public Long date3;      //Дата для точки маршрута 3

    @Before
    public void setUp() throws Exception {
        //Создаю пустой  маршрут  id будет 888
        NullWay = new Way(888);
        //Создаю TestWay id будет 999
        TestWay = new Way(999);

        //создаю даты для точек маршрута
        date1 =Long.valueOf("1460272898524");
        date2 =Long.valueOf("1460282898525");
        date3 =Long.valueOf("1460292898526");

        //создаю точки маршрута
        PointLocation testPoint1 = new PointLocation(0.0, 0.0, 0, 0, 1.0, 1, date1);
        PointLocation testPoint2 = new PointLocation(35.0, 1.0, 1, 1, 1.0, 3, date2);

        //добавляю точки маршрута в TestWay
        TestWay.AddLocationToWay(testPoint1);
        TestWay.AddLocationToWay(testPoint2);
    }

    @After
    public void tearDown() throws Exception {
        //Пока нечего очищать.
    }
    //TEST//////////////////////////////////////////////////////////////////////////

    @Test
    public void testGetWayID() throws Exception {
        //Получение ID маршрута
        assertEquals(999, TestWay.GetWayID());
        assertEquals(888, NullWay.GetWayID());
    }

    @Test
    public void testIsEmpty() throws Exception {
        //метод Way.IsEmpty() проверяет наличие точек маршрута в обьекте Way
        assertTrue(NullWay.isEmpty());
        assertFalse(TestWay.isEmpty());
    }

    @Test
    public void testGetLastLocations() throws Exception {
        //Создаю точку которая должна ровняться последней точке маршрута.
        PointLocation Point = new PointLocation(35.0, 1.0, 1, 1, 1.0, 3, date2);
        //Буду сравнивать все элементы PointLocation.
        assertEquals(Point.GetPointAccuracy(), TestWay.GetLastLocations().GetPointAccuracy());
        assertEquals(Point.GetPointAltitude(), TestWay.GetLastLocations().GetPointAltitude());
        assertEquals(Point.GetPointBearing(), TestWay.GetLastLocations().GetPointBearing());
        assertEquals(Point.GetPointLatitude(), TestWay.GetLastLocations().GetPointLatitude());
        assertEquals(Point.GetPointLongitude(), TestWay.GetLastLocations().GetPointLongitude());
        assertEquals(Point.GetPointSpeed(), TestWay.GetLastLocations().GetPointSpeed());
        assertEquals(Point.GetPointTime(), TestWay.GetLastLocations().GetPointTime());
    }

    @Test
    public void testGetAllLocations() throws Exception {
        //В TestWay должно быть 2 элемента. Проверяю.
        assertEquals(2, TestWay.GetAllLocations().size());
    }

    @Test
    public void testAddLocationToWay() throws Exception {
        //Добавлу в TestWay новую локацию и проверю размер массива, должно стать 3 элемента.
        TestWay.AddLocationToWay(new PointLocation(3.0, 3.0, 3, 3, 3.0, 3, date3));
        assertEquals(3, TestWay.GetAllLocations().size());
    }

    @Test
    public void testGetStartTime() throws Exception {
        //Время старта должно ровняться времени, которое содержится в первой точке маршрута.
        assertEquals(date1, TestWay.GetStartTime());
    }

    @Test
    public void testGetDuration() throws Exception {

        SimpleDateFormat TestDataFormat = new SimpleDateFormat("HH:mm:ss");
        //задаю формат времени GMT.
        // !!!!Пока работает по GMT, в будущем буду подстраиваться под, локальное.
        TestDataFormat.setTimeZone(TimeZone.getTimeZone("GMT"));

        //Проверка по используемому алгоритму: (Конечная дата) - (Начальная дата).
        assertEquals(TestDataFormat.format(date2 - date1), TestWay.GetDuration());
        //Проверка константой
        assertEquals("02:46:40", TestWay.GetDuration());
    }

    @Test
    public void testGetAverageSpeed() throws Exception {
        //При подготовке в двух точках скорость определена как 1 и 3.
        //Иисходя из этого средняя скорость должна быть 2. Дополнительно привожу к double.
        assertEquals(2.0, TestWay.GetAverageSpeed().doubleValue());
    }

    @Test
    public void testGetDistance() throws Exception {
        assertEquals(BigDecimal.valueOf(0), TestWay.GetDistance());

        //Тест через mockito
        //Создаю фейковый обьект Way
        Way mockedWay = mock(Way.class);
        //GetDistance будет возвращать 10
        when(mockedWay.GetDistance()).thenReturn(BigDecimal.valueOf(10));
        //Тест
        assertEquals(BigDecimal.valueOf(10), mockedWay.GetDistance());
        //Проверка
        verify(mockedWay).GetDistance();
    }
}
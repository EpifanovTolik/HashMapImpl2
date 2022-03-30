
import org.junit.jupiter.api.BeforeEach;

import static org.junit.jupiter.api.Assertions.*;

class SelfHashMapTest {

    private final SelfHashMap<Integer, String> selfHashMap = new SelfHashMap<>();

    /**
     * метод вызываем перед каждым тестом, чтобы не повторять код
     */
    @BeforeEach
    public void setUp() {
        selfHashMap.put(5, "A");
        selfHashMap.put(7, "C");
        selfHashMap.put(-9, "P");
        selfHashMap.put(20, "D");
        selfHashMap.put(-4, "R");
    }

    /**
     * проверяем, вернет ли метод нужное значение по ключу
     */
    @org.junit.jupiter.api.Test
    void get() {
        String actual = selfHashMap.get(5);
        String expected = "A";
        assertEquals(expected,actual);
    }

    /**
     * проверяем, будет ли значение по этому ключу в мапе, если вставить этим методом put
     */
    @org.junit.jupiter.api.Test
    void put() {
        selfHashMap.put(10, "T");
        String actual = selfHashMap.get(10);
        String expected = "T";
        assertEquals(expected,actual);

    }

    /**
     * проверяем, очистится ли наш мап методом clear
     */
    @org.junit.jupiter.api.Test
    void clear() {
        selfHashMap.clear();
        int actual = selfHashMap.size();
        int expected = 0;
        assertEquals(expected,actual);
    }

    /**
     * проверяем, совпадет ли размер
     */
    @org.junit.jupiter.api.Test
    void size() {
        int actual = selfHashMap.size();
        int expected = 5;
        assertEquals(expected,actual);
    }
}
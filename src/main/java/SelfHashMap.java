import java.io.Serializable;
import java.util.*;

public class SelfHashMap<K, V> extends AbstractMap<K, V> implements Map<K, V>, Cloneable, Serializable{

    /**
     * лоад фактор по умолчанию
     */
    static final float DEFAULT_LOAD_FACTOR = 0.75f;
    /**
     *  длина по умолчанию
     */
    private static final int DEFAULT_LENGTH = 16;
    /**
     * Длина массива
     */
    private int arrayLength;

    /**
     * Количество элементов на карте
     */
    private int size;

    private float loadFactor;

    /**
     * Массив узлов нодов
     */
    private Node<K, V>[] tables;

    public SelfHashMap() {
        this(DEFAULT_LENGTH, DEFAULT_LOAD_FACTOR);
    }

    /**
     *
     * @param length длина инициализации масссива
     */
    public SelfHashMap(int length) {
        this(length, DEFAULT_LOAD_FACTOR);
    }

    /**
     *   @param length длина инициализации массива
     * @param  loadFactor коэффициент загрузки
     */
    public SelfHashMap(int length, float loadFactor) {
        if (length <= 0) {
            throw new IllegalArgumentException("Длина инициализации должна быть больше 0");
        }
        if (loadFactor <= 0) {
            throw new IllegalArgumentException("Коэффициент нагрузки должен быть больше 0");
        }

        this.arrayLength = length;
        this.loadFactor = loadFactor;
        tables = new Node[length];
    }

    /**
     *
     * @param key ключ
     * @return  Вычисляем значение хеша на основе хеш-кода объекта.
     *          По значению хеша и длине связанного списка получаем индекс позиции вставки
     */
    @Override
    public V get(Object key) {
        // Вычисляем значение хеша на основе хеш-кода объекта
        int index = indexForArray(hash(key), arrayLength);
        //По значению хеша и длине связанного списка получаем индекс позиции вставки
        Node<K, V> node = tables[index];
        for (Node<K, V> n = node; n != null; n = n.next) {
            if ((key == null && null == n.getKey()) || (key != null && key.equals(n.getKey()))) {
                return n.value;
            }
        }
        return null;
    }

    /**
     *
     * @return  Если в i есть данные и ключ тот же, перезаписать.
     *          Если в позиции i нет данных или есть данные в позиции i, но ключ - это новый ключ, добавить узел
     *
     */
    @Override
    public V put(K key, V value) {
        // Рассчитать хеш-значение ключа
        int hashCode = hash(key);
        // Рассчитать место, где он должен храниться.
        int index = indexForArray(hashCode, arrayLength);
        Node<K, V> node = tables[index];
        //Если в позиции i нет данных добавьте узел
        if (node == null) {
            tables[index] = new Node(key, value, hashCode, null);
        } else {
            for (Node<K, V> n = node; n != null; n = n.next) {
                // Если ключ уже существует, перезаписываем и возвращаем
                K nodeKey = n.getKey();
                if ((key == null && null == nodeKey) || (key != null && key.equals(nodeKey))) {
                    V oldValue = n.getValue();
                    n.setValue(value);
                    return oldValue;
                }
                // Ключ не существует, если он считается последним в очереди, создается новый Узел и помещается в конец очереди
                if (n.next == null) {
                    n.next = new Node<>(key, value, hashCode, null);
                    break;
                }
            }
        }
        // Определяем, хотим ли развернуть, если просто заменить значение без добавления элементов, оно не будет выполнено
        if (++size > arrayLength * loadFactor) {
            resize();
        }
        return null;
    }

    /**
     * очистить
     */
    @Override
    public void clear() {
        tables = new Node[arrayLength];
        size = 0;
    }

    /**
     *
     * @return размер
     */
    @Override
    public int size() {
        return size;
    }

    /**
     *
     * @return возвращаем набор элементов коллекции
     */
    @Override
    public Set<Entry<K, V>> entrySet() {
        Set<Entry<K, V>> set = new HashSet<>();
        for (Node<K, V> node : tables) {
            while (node != null) {
                set.add(node);
                node = node.next;
            }
        }
        return set;
    }

    /**
     * Получить значение хеш-функции
     *
     * @param  key передан в ключ
     * @return  Если ключ равен нулю, вернуть 0, а остальные вернут значение hashCode ()
     */
    public int hash(Object key) {
        return key == null ? 0 : key.hashCode();
    }

    /**
     * Получить приведенную ниже таблицу массивов в соответствии с хеш-значением и длиной массива
     * После изменения алгоритма хеширования  можно напрямую изменить здесь
     *
     * @param hashCode
     * @param arrayLength
     * @return
     */
    private int indexForArray(int hashCode, int arrayLength) {
        int index = Math.abs(hashCode) % arrayLength;
        return index;
    }

    /**
     *
     * Расширение (Перемещение  массива при изменении количества бакетов)
     * 1. Получите длину нового массива и создать новый массив.
     * 2. Перенести данные из старого массива в новый массив.
     * 3. Заменить старый массив
     */
    private void resize() {
        // Получаем длину нового массива и создать новый массив
        int newLength = arrayLength * 2;
        Node<K, V>[] newTables = new Node[newLength];
        Set<Entry<K, V>> entrySet = entrySet();
        int newSize = 0;
        for (Entry entry : entrySet) { // пройти старый массив
            Node<K, V> node = (Node<K, V>) entry;  // Получить каждый элемент старого массив
            node.next = null;
            int index = indexForArray(node.hashCode, arrayLength);
            Node<K, V> indexNode = newTables[index]; //Пересчитать положение каждого элемента в массиве
            if (indexNode == null) {
                newTables[index] = node; // Поместить элемент в массив
            } else {
                while (indexNode.next != null) {
                    indexNode = indexNode.next; // Доступ к элементам в следующей цепочке ввода
                }
                indexNode.next = node;
            }
        }
        tables = newTables;
        arrayLength = newLength;
    }


    class Node<K, V> implements Entry<K, V> {
        private K key;
        private V value;
        /**
         * Следующий узел связанного списка
         */
        private Node<K, V> next;
        /**
         * Хеш-значение ключа
         */
        private int hashCode;

        public Node(K key, V value, int hashCode, Node<K, V> next) {
            this.key = key;
            this.value = value;
            this.hashCode = hashCode;
            this.next = next;
        }

        @Override
        public K getKey() {
            return key;
        }

        @Override
        public V getValue() {
            return value;
        }

        @Override
        public V setValue(V value) {
            this.value = value;
            return value;
        }

        @Override
        public String toString() {
            return key + "=" + value;
        }

    }


    //сортировка---------------------------------------

    /**
     *
     * Сортируем по значению мапу.
     * Сортировку взял из Collections.(свою реализацию никак не смог прикрутить по дженерикам, интерфейсам и тд )
     *
     * @return    Тут сначала принимаем мап. Далее создаем лист для сортировки . Потом сравниваем значения.
     *            Далее результат помещаем в мап
     *
     */
    public static   <K, V extends Comparable<? super V>> Map<K, V>
    sortByValue(Map<K, V> map ) //принимаем мап
    {
        List<Map.Entry<K, V>> list =
                new LinkedList<>(map.entrySet());  //создаем лист для сортировки
        Collections.sort( list, new Comparator<Map.Entry<K, V>>()
        {
            @Override
            public int compare(Map.Entry<K, V> o1, Map.Entry<K, V> o2)  //сравниваем значения
            {
                return (o1.getValue()).compareTo( o2.getValue() );
            }
        } );

        Map<K, V> result = new LinkedHashMap<>();  //результат помещаем в мап
        for (Map.Entry<K, V> entry : list)
        {
            result.put(entry.getKey(), entry.getValue());
        }
        return result;
    }
}
import java.io.Serializable;
import java.util.*;

public class SelfHashMap<K, V> extends AbstractMap<K, V> implements Map<K, V>, Cloneable, Serializable {

    /**
     * Коэффициент загрузки по умолчанию
     */
    static final float DEFAULT_LOAD_FACTOR = 0.75f;
    /**
     * Начальная длина по умолчанию
     */
    private static final int DEFAULT_LENGTH = 16;
    /**
     * Длина столов
     */
    private int arrayLength;

    /**
     * Количество элементов на карте
     */
    private int size;

    private float loadFactor;

    /**
     * Массив узлов хранения
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



//    private static final int CUTOFF = 3;
//
//    public static <T extends Comparable<? super T>> void quicksort(T[] a) {
//        quicksort(a, 0, a.length - 1);
//    }
//
//    public static <T extends Comparable<? super T>> void quicksort(T[] a, int left, int right) {
//        if (left + CUTOFF <= right) {
//            T pivot = median3(a, left, right);
//
//            // Begin partitioning
//            int i = left, j = right - 1;
//            for (; ; ) {
//                while (a[++i].compareTo(pivot) < 0) {
//                }
//                while (a[--j].compareTo(pivot) > 0) {
//                }
//                if (i < j)
//                    swapReferences(a, i, j);
//                else
//                    break;
//            }
//
//            swapReferences(a, i, right - 1);   // Restore pivot
//
//            quicksort(a, left, i - 1);    // Sort small elements
//            quicksort(a, i + 1, right);   // Sort large elements
//        } else  // Do an insertion sort on the subarray
//            insertionSort(a, left, right);
//    }
//
//    public static <T> void swapReferences(T[] a, int index1, int index2) {
//        T tmp = a[index1];
//        a[index1] = a[index2];
//        a[index2] = tmp;
//    }
//
//    public static <T extends Comparable<? super T>> T median3(T[] a, int left, int right) {
//        int center = (left + right) / 2;
//        if (a[center].compareTo(a[left]) < 0)
//            swapReferences(a, left, center);
//        if (a[right].compareTo(a[left]) < 0)
//            swapReferences(a, left, right);
//        if (a[right].compareTo(a[center]) < 0)
//            swapReferences(a, center, right);
//
//        // Place pivot at position right - 1
//        swapReferences(a, center, right - 1);
//        return a[right - 1];
//    }
//
//    public static <T extends Comparable<? super T>> void insertionSort(T[] a, int left, int right) {
//        for (int p = left + 1; p <= right; p++) {
//            T tmp = a[p];
//            int j;
//            for (j = p; j > left && tmp.compareTo(a[j - 1]) < 0; j--)
//                a[j] = a[j - 1];
//            a[j] = tmp;
//        }
//    }

}
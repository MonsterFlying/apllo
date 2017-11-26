package com.gofobao.framework;

import com.gofobao.framework.helper.RedisHelper;
import com.google.common.base.Converter;
import com.google.common.collect.Lists;
import org.junit.Before;
import org.junit.Test;
import org.springframework.data.redis.core.StringRedisTemplate;
import redis.clients.jedis.Jedis;

import java.lang.reflect.Field;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * Created by xin on 2017/11/6.
 */

public class Test1 {
    public void filter() {
        List list = new ArrayList();
        for (int i = 1; i <= 5; ++i) {
            list.add(i);
        }
        list.stream().filter(integer -> (int) integer % 2 == 1)
                .forEach(System.out::println);
    }

    @Test
    public void reverse() {
        List list = new ArrayList();
        list.add(2);
        list.add(3);
        list.add(1);
        list.add(5);
        list.add(4);
        list.stream().sorted().forEach(System.out::println);
    }

    @Test
    public void filter1() {
        List<Integer> list = Lists.newArrayList(1, null, 5, 3, null, 2);
        list.stream().filter(param -> param != null).collect(Collectors.toList()).forEach(s -> System.out.println(s));
        list.stream().limit(3l).forEach(System.out::println);

    }

    @Test
    public void max() {
        List<Integer> list = Lists.newArrayList(1, 5, 2, 4, 8, 9, 0);
        IntSummaryStatistics statistics = list.stream().mapToInt(x -> x).summaryStatistics();
        System.out.println(statistics.getMax());
        System.out.println(statistics.getMin());
        System.out.println(statistics.getCount());
        System.out.println(statistics.getAverage());
    }

    @Test
    public void inner() {
        List<Integer> list = Lists.newArrayList(1, 5, 2, 4, 8, 9, 0);
        long count = 10;
        list.stream().forEach(param -> {
            System.out.println(count);
        });
    }

    @Test
    public void toUpper() {
        List<String> list = Lists.newArrayList("apple", "watermenon", "strawberry");
        String s = list.stream().map(x -> x.toUpperCase()).collect(Collectors.joining(","));
        System.out.println(s);
    }

    @Test
    public void groupby() {
        List<Employee> employees = Lists.newArrayList();
        for (int i = 0; i < 30; i++) {
            Employee employee = new Employee();
            employee.setId((int) Math.floor(Math.random() * (10 - 1) + 1));
            employee.setName("zhangsan" + i);
            employee.setAge((int) (Math.random() * (30 - 10) + 10));
            employee.setCity("HongKong");
            employees.add(employee);
        }
        Map<Integer, List<Employee>> map = employees.stream().collect(Collectors.groupingBy(Employee::getId, Collectors.toList()));
        System.out.println(map);
    }

    @Test
    public void reduce() {
        Function<String, Integer> function = Integer::valueOf;
        Function<Integer, String> back = String::valueOf;
        Function<Integer, String> back1 = a -> {
            System.out.println(a);
            //Function<Employee, Integer> employeeFunction = Employee::getAge;
            return "11";
        };
        String result = back1.apply(111);
        System.out.println(result);
    }

    @Test
    public void match() {
        List<String> list = Lists.newArrayList("apple", "watermenon", "strawberry");
        Boolean flag = list.stream().anyMatch(s -> s.startsWith("a"));
        System.out.println(flag);
    }

    @Test
    public void option() {
        Optional<Integer> optional = Optional.of(100);
        Integer result = optional.get();
        Predicate<Integer> predicate = s -> s > 10;
    }

    @Test
    public void parrelStream() {
        List<UUID> list = Lists.newArrayList();
        for (int i = 0; i < 10000000; i++) {
            list.add(UUID.randomUUID());
            System.out.println(i);
        }
        long t1 = System.currentTimeMillis();
        list.parallelStream().sorted();
        long t2 = System.currentTimeMillis();
        System.out.println(t2 - t1);
    }

    @Test
    public void test() {
        String s1 = "a" + ":" + "b";
        String s2 = "a" + ":" + "b";
        System.out.println(s1.hashCode());
        System.out.println(s2.hashCode());
        System.out.println(s1 == s2);
    }

    @Test
    public void clazz() {
        String s = "Hello World";
        System.out.println("before: " + s);
        System.out.println("before: " + s.hashCode());
        Field value = null;
        try {
            value = String.class.getDeclaredField("value");
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
        value.setAccessible(true);
        char[] v = new char[0];
        try {
            v = (char[]) value.get(s);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        v[5] = '-';
        System.out.println("after: " + s);
        System.out.println("after" + s.hashCode());

    }

    @Test
    public void jedis() {
        //连接redis服务器，192.168.0.100:6379
        Jedis jedis = new Jedis("192.168.1.5", 6379);
        //权限认证
        System.out.println(jedis.get("username"));
    }

    @Test
    public void lambda() {
        Supplier<String> flag = "abc"::toUpperCase;
        String result = flag.get();
        System.out.println(result);
        EmployeeInterface employee = Employee::new;
        System.out.println(employee);
    }

    @Test
    public void test2() {
        List<String> list = Arrays.asList("av", "asdf", "sad", "324", "43");
        String b = "avasdf";
        List<String> collect = list.stream().filter(b::contains).collect(Collectors.toList());
        System.out.println(collect);
    }

    @Test
    public void test3() {
        List<String> list = Arrays.asList("av","asdf","sad","324","43");
        List<String> collect = list.stream().map(String::toUpperCase).collect(Collectors.toList());
        System.out.println(collect);
    }


}
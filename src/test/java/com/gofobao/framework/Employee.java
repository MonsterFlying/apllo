package com.gofobao.framework;

import org.apache.poi.ss.formula.functions.T;

import java.io.Serializable;

/**
 * Created by xin on 2017/11/7.
 */
public class Employee implements Serializable,EmployeeInterface {
    private  int id;
    private String name;
    private int age;
    private String city;

    public  int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    @Override
    public String toString() {
        return "Employee{" +
                "name='" + name + '\'' +
                '}';
    }

    @Override
    public Employee get() {
        return null;
    }
}

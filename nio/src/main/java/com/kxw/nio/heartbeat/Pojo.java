package com.kxw.nio.heartbeat;

import java.io.Serializable;
import java.util.List;

/**
 * @author songfeng
 * @version 1.0
 * @category com.feng.test.longconnection
 * @since 2015-10-16
 * <a href='http://songfeng-123.iteye.com/blog/2251775'>@link</a>
 */
public class Pojo implements Serializable {

    /**
     * 序列化
     */
    private static final long serialVersionUID = -8868529619983791261L;

    private String name;

    private int age;

    private List<String> likeThing;

    public Pojo(String name, int age, List<String> likeThing) {
        super();
        this.name = name;
        this.age = age;
        this.likeThing = likeThing;
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

    public List<String> getLikeThing() {
        return likeThing;
    }

    public void setLikeThing(List<String> likeThing) {
        this.likeThing = likeThing;
    }
}

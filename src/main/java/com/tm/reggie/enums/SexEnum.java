package com.tm.reggie.enums;

import com.baomidou.mybatisplus.annotation.IEnum;
import com.fasterxml.jackson.annotation.JsonFormat;

//注解为了解决枚举类返回前端只展示构造器名称的问题。
@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum SexEnum implements IEnum<Integer> {
    MAN(1, "男"),
    WOMAN(0, "女");

    private Integer code;
    private String name;

    SexEnum(Integer code, String name) {
        this.code = code;
        this.name = name;
    }

    @Override
    public Integer getValue() {
        return code;
    }

    public String getName() {
        return name;
    }

}
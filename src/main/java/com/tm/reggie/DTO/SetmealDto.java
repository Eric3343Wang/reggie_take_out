package com.tm.reggie.DTO;


import com.tm.reggie.entity.Setmeal;
import com.tm.reggie.entity.SetmealDish;
import lombok.Data;

import java.util.List;


@Data
public class SetmealDto extends Setmeal {

    private List<SetmealDish> setmealDishes;

    private String categoryName;
}

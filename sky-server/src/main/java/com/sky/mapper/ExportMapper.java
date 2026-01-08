package com.sky.mapper;

import org.apache.ibatis.annotations.Mapper;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Mapper
public interface  ExportMapper {



    Double sumByMap(HashMap<Object, Object> map);

    Integer countByMap(HashMap<Object, Object> countByMap);

    Integer totaiOrder(HashMap<Object, Object> map);

    Integer newUser(HashMap<Object, Object> map);

    List<Map<String, Object>> top10Sales(HashMap<Object, Object> map);
}

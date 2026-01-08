package com.sky.mapper;

import com.github.pagehelper.Page;
import com.sky.dto.EmployeePageQueryDTO;
import com.sky.entity.Employee;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface EmployeeMapper {

    /**
     * 根据用户名查询员工
     * @param username
     * @return
     */
    @Select("select * from employee where username = #{username}")
    Employee getByUsername(String username);


    void insert(Employee employee);
    Page<Employee> pageQuery(EmployeePageQueryDTO employeePageQueryDTO);

    void updateStaus(@Param("status") Integer status, @Param("id") Long id);


    Employee getById(Integer id);

    void update();


    void editPassword(Employee employee);
}

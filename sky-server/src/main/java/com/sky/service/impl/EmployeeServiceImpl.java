package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.constant.PasswordConstant;
import com.sky.constant.StatusConstant;
import com.sky.context.BaseContext;
import com.sky.dto.EmployeeDTO;
import com.sky.dto.EmployeeLoginDTO;
import com.sky.dto.EmployeePageQueryDTO;
import com.sky.dto.PasswordEditDTO;
import com.sky.entity.Employee;
import com.sky.exception.AccountLockedException;
import com.sky.exception.AccountNotFoundException;
import com.sky.exception.PasswordEditFailedException;
import com.sky.exception.PasswordErrorException;
import com.sky.mapper.EmployeeMapper;
import com.sky.properties.JwtProperties;
import com.sky.result.PageResult;
import com.sky.service.EmployeeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
public class EmployeeServiceImpl implements EmployeeService {
   @Autowired
   private JwtProperties jwtProperties;
    @Autowired
    private EmployeeMapper employeeMapper;

    /**
     * 员工登录
     *
     * @param employeeLoginDTO
     * @return
     */
    public Employee login(EmployeeLoginDTO employeeLoginDTO) {
        String username = employeeLoginDTO.getUsername();
        String password = employeeLoginDTO.getPassword();

        //1、根据用户名查询数据库中的数据
        Employee employee = employeeMapper.getByUsername(username);

        //2、处理各种异常情况（用户名不存在、密码不对、账号被锁定）
        if (employee == null) {
            //账号不存在

            throw new AccountNotFoundException(MessageConstant.ACCOUNT_NOT_FOUND);
        }

        //密码比对
        //
         password = DigestUtils.md5DigestAsHex(password.getBytes());
        log.info("password:{}",password);
        if (!password.equals(employee.getPassword())) {
            //密码错误
            throw new PasswordErrorException(MessageConstant.PASSWORD_ERROR);
        }

        if (employee.getStatus() == StatusConstant.DISABLE) {
            //账号被锁定
            throw new AccountLockedException(MessageConstant.ACCOUNT_LOCKED);
        }

        //3、返回实体对象
        return employee;

    }
    @Override
    //添加员工
    public void save (EmployeeDTO employeeDTO){
        //将DTO复制到实体对象
        Employee employee = new Employee();
        BeanUtils.copyProperties(employeeDTO, employee);
        employee.setPassword(DigestUtils.md5DigestAsHex(PasswordConstant.DEFAULT_PASSWORD.getBytes()));
        employee.setStatus(StatusConstant.ENABLE);
        employee.setCreateTime(LocalDateTime.now());
        employee.setUpdateTime(LocalDateTime.now());

        employee.setCreateUser( BaseContext.getCurrentId());
        employee.setUpdateUser( BaseContext.getCurrentId());

        employeeMapper.insert(employee);
    }
    @Override
    public PageResult  pageQuery(EmployeePageQueryDTO employeeDTO) {
        PageHelper.startPage(employeeDTO.getPage(), employeeDTO.getPageSize());

       Page<Employee> page = employeeMapper.pageQuery(employeeDTO);
       Long total = page.getTotal();
       List<Employee> records = page.getResult();
        return new PageResult(total, records);

    }

    @Override
    public void startOrStop(Integer status, Long id) {

        employeeMapper.updateStaus(status,id);
    }

    @Override
    public void getById(Integer id) {
        employeeMapper.getById(id);
    }

    @Override
    public void update(EmployeeDTO employeeDTO) {
        Employee employee = new Employee();
        BeanUtils.copyProperties(employeeDTO,employee);
        employeeMapper.update();
    }


    @Override
    public void editPassword(PasswordEditDTO passwordEditDTO) {
        Long currentId = BaseContext.getCurrentId();
        int id = currentId.intValue();
        Employee employee=employeeMapper.getById(id);
        String password = employee.getPassword();
        String oldPassword = passwordEditDTO.getOldPassword();
        String s = DigestUtils.md5DigestAsHex(oldPassword.getBytes());
        if(!s.equals(password)){
          throw new PasswordEditFailedException(MessageConstant.PASSWORD_EDIT_FAILED);
        }

        String newPassword = passwordEditDTO.getNewPassword();
        String s1 = DigestUtils.md5DigestAsHex(newPassword.getBytes());
        employee.setPassword(s1);
        employee.setId(currentId);
        employee.setUpdateTime(LocalDateTime.now());
        employee.setUpdateUser(currentId);
        employeeMapper.editPassword(employee);
    }
}

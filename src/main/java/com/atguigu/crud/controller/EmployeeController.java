package com.atguigu.crud.controller;

import com.atguigu.crud.bean.Employee;
import com.atguigu.crud.bean.Msg;
import com.atguigu.crud.service.EmployeeService;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Controller
public class EmployeeController {

    @Autowired
    EmployeeService employeeService;

//    单个批量二合一
//	 * 批量删除：1-2-3
//            * 单个删除：1

    @RequestMapping(value = "/emp/{ids}",method = RequestMethod.DELETE)
    @ResponseBody
    public Msg deleteEmpById(@PathVariable("ids")String ids){
        //区分批量删除和单个删除
        if(ids.contains("-")){
            ArrayList<Integer> del_ids = new ArrayList<>();
            String[] str_ids = ids.split("-");
            //组装ID的集合
            for (String str_id : str_ids) {
                int i = Integer.parseInt(str_id);
                del_ids.add(i);
            }
            employeeService.deleteBatch(del_ids);
        }else{
            int id = Integer.parseInt(ids);
            employeeService.deleteEmp(id);
        }

        return Msg.success();
    }



    //如果直接方式sjax=put请求，只封装路径上的；请求体中有数据但是employee封装不上
    // 原因：
    //	 * Tomcat：
    //	 * 		1、将请求体中的数据，封装一个map。
    //	 * 		2、request.getParameter("empName")就会从这个map中取值。
    //	 * 		3、SpringMVC封装POJO对象的时候。
    //	 * 				会把POJO中每个属性的值，request.getParamter("email");
    //	 * AJAX发送PUT请求引发的血案：
    //	 * 		PUT请求，请求体中的数据，request.getParameter("empName")拿不到
    //	 * 		Tomcat一看是PUT不会封装请求体中的数据为map，只有POST形式的请求才封装请求体为map
    //解决方案；
    //	 * 我们要能支持直接发送PUT之类的请求还要封装请求体中的数据
    //	 * 1、配置上HttpPutFormContentFilter；
    //	 * 2、他的作用；将请求体中的数据解析包装成一个map。
    //	 * 3、request被重新包装，request.getParameter()被重写，就会从自己封装的map中取数据

    //员工更新方法
    @RequestMapping(value = "/emp/{empId}",method = RequestMethod.PUT)
    @ResponseBody
    public Msg updateEmp(Employee employee){
        employeeService.updateEmp(employee);
        return Msg.success();
    }



    //查询员工
    @RequestMapping(value = "/emp/{id}",method = RequestMethod.GET)
    @ResponseBody
    public Msg getEmp(@PathVariable("id") Integer id){
        Employee employee=employeeService.getEmp(id);
        return Msg.success().add("emp",employee);
    }



    @ResponseBody
    @RequestMapping("/checkuser")
    //用户名是否可用
    public Msg checkuser(@RequestParam("empName") String empName){
        //先判断用户名是否合法的表达式
        String regs ="(^[a-zA-Z0-9_-]{6,16}$)|(^[\u2E80-\u9FFF]{2,5})/";
        boolean matches = empName.matches(regs);
        if(!matches){
            return Msg.fail().add("va_msg","用户名可以是2-5位中文或者6-16位英文和数字的组合 houduan");
        }
        //用户名重复校验
        boolean b = employeeService.checkUser(empName);
        if(b){
            return Msg.success();
        }else {
            return Msg.fail().add("va_msg","用户名不可用");
        }
    }


//    员工保存
//    支持jsr303校验
//    导入hibernate-validator
    @ResponseBody
    @RequestMapping(value = "/emp",method = RequestMethod.POST)
    public Msg saveEmp(@Valid Employee employee, BindingResult result){
        if(result.hasErrors()){
//            校验失败，应该返回失败并且在模态中显示
            HashMap<String, Object> map = new HashMap<>();
            List<FieldError> fieldErrors = result.getFieldErrors();
            for (FieldError fieldError : fieldErrors) {
                System.out.println("错误的字段名"+fieldError.getField());
                System.out.println("错误信息"+fieldError.getDefaultMessage());
                map.put(fieldError.getField(),fieldError.getDefaultMessage());
            }
            return Msg.fail().add("errorFields",map);
        }else {
            employeeService.saveEmp(employee);
            return Msg.success();
        }

    }


//  2.0使用ajax json
//    使用jackson包 只要在springmvc中配置了annotation driver就可以将响应到浏览器的Java对象转换为Json格式的字符串
    @RequestMapping("/emps")
    @ResponseBody
    public Msg getEmpsWithJson(@RequestParam(value = "pn",defaultValue ="1")Integer pn){
        PageHelper.startPage(pn, 5);
        // startPage后面紧跟的这个查询就是一个分页查询
        List<Employee> emps = employeeService.getALL();
        // 使用pageInfo包装查询后的结果，只需要将pageInfo交给页面就行了。
        // 封装了详细的分页信息,包括有我们查询出来的数据，传入连续显示的页数
        PageInfo page = new PageInfo(emps, 5);
        return Msg.success().add("pageInfo",page);
    }



    //查询员工数据（分页）
//    @RequestMapping("/emps")
    public String getEmps(@RequestParam(value = "pn",defaultValue ="1")Integer pn, Model model){
        // 引入PageHelper分页插件
        // 在查询之前只需要调用，传入页码，以及每页的大小
        PageHelper.startPage(pn, 5);
        // startPage后面紧跟的这个查询就是一个分页查询
        List<Employee> emps = employeeService.getALL();
        // 使用pageInfo包装查询后的结果，只需要将pageInfo交给页面就行了。
        // 封装了详细的分页信息,包括有我们查询出来的数据，传入连续显示的页数
        PageInfo page = new PageInfo(emps, 5);
        model.addAttribute("pageInfo",page);

        return "list";
    }
}

package com.atguigu.crud.test;


import com.atguigu.crud.bean.Employee;
import com.github.pagehelper.PageInfo;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.List;

//使用spring测试模块提供的测试请求功能，测试crud请求的正确性
@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration(locations = {"classpath:applicationContext.xml","classpath:springMVC.xml"})
public class MVCTest {

    //传入springmvc的ioc
    @Autowired
    WebApplicationContext context;

//    虚拟mvc请求,获取处理结果
    MockMvc mockMvc;

    @Before
    public void initMockMVC(){
        mockMvc= MockMvcBuilders.webAppContextSetup(context).build();
    }

@Test
    public void testPage() throws Exception{
        //模拟请求拿到返回值
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get("/emps").param("pn", "1")).andReturn();

        //请求成功后，请求域中会有pageInfo
        MockHttpServletRequest request=result.getRequest();
        PageInfo pageInfo =(PageInfo) request.getAttribute("pageInfo");
        System.out.println("当前页码"+pageInfo.getPageNum());
        System.out.println("总页码"+pageInfo.getPages());
        System.out.println("总计路数"+pageInfo.getTotal());
        System.out.println("需要连续显示的页码");
        int[] navigatepageNums = pageInfo.getNavigatepageNums();
        for(int i:navigatepageNums){
            System.out.print(" " +i);
        }
        List<Employee> list = pageInfo.getList();
        for (Employee employee : list) {
            System.out.println("ID:"+employee.getEmpId()+"==>name:"+employee.getEmpName());
        }
    }

}

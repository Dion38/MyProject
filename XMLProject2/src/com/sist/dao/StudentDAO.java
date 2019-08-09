package com.sist.dao;
import java.util.*;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;

import java.io.*;
public class StudentDAO {
	private static SqlSessionFactory ssf;
	static{
		try{
			// XML을 읽어 온다 = SRC
			Reader reader = Resources.getResourceAsReader("Config.xml");
			ssf = new SqlSessionFactoryBuilder().build(reader);
		}catch (Exception e) {
			System.out.println(e.getMessage());
		}
	}
	public static List<StudentVO> studentAllData(){
		return ssf.openSession().selectList("studentAllData");
	}
}

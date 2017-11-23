package com.example.sales.util;

import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import com.example.sales.model.Sales;

public class Test {

	public static void main(String a[]) {
		EntityManagerFactory emf = Persistence.createEntityManagerFactory("sales-personnel-server");
		System.out.println(emf);
//		EntityManager em = emf.createEntityManager();
		Sales sale = new Sales();
		sale.setId("11");
		sale.setName("asas");
		sale.setSalesValue(100);
	}
	
}
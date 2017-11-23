package com.example.sales.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name="SP_SALES")
public class Sales {

	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	@Column(name="ID")
	private String id;

	@Column(length=255, name="NAME")
	private String name;
	
	@Column(name="SALES_VALUE")
	private int salesValue;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getSalesValue() {
		return salesValue;
	}

	public void setSalesValue(int salesValue) {
		this.salesValue = salesValue;
	}
	
	
}

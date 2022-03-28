package com.test4.cgi;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Scanner;

import com.fasterxml.jackson.databind.ObjectMapper;

class Person{
	private String name;
	private Integer age;
	private String gender;
	private double probability;
	private int count;

	public Person() {}

	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public Integer getAge() {
		return age;
	}
	public void setAge(Integer age) {
		this.age = age;
	}
	public String getGender() {
		return gender;
	}
	public void setGender(String gender) {
		this.gender = gender;
	}

	public double getProbability() {
		return probability;
	}

	public void setProbability(double probability) {
		this.probability = probability;
	}

	public int getCount() {
		return count;
	}

	public void setCount(int count) {
		this.count = count;
	}

	@Override
	public String toString() {
		return "Person [name=" + name + ", age=" + age + ", gender=" + gender + ", probability=" + probability + "]";
	}
}

public class App 
{
	//send request and return Person object
	public static Person[] sendRequest(String url) throws Exception{
		HttpURLConnection httpRequestConnection = (HttpURLConnection) new URL(url.toString()).openConnection();
		int statusCode = httpRequestConnection.getResponseCode();
		if(statusCode == 429){
			throw new Exception("429: You have reached API limits...!!");
		}else if(statusCode == 422){
			throw new Exception("422: Missing/Invalid 'name' parameter...!!");
		}
		

		// reading response
		BufferedReader response = new BufferedReader(new InputStreamReader(httpRequestConnection.getInputStream()));
		String resLine;
		StringBuffer content = new StringBuffer();
		while((resLine = response.readLine()) != null){
			content.append(resLine);
		}
		httpRequestConnection.disconnect();
		return new ObjectMapper().readValue(content.toString(), Person[].class);
	}

	public static Person[] getAge(String personName) throws Exception{
		StringBuffer url = new StringBuffer("https://api.agify.io/?name[]=");
		url.append(personName);
		return sendRequest(url.toString());
	}

	public static Person[] getGender(Person[] personObj, String personName) throws Exception{
		StringBuffer url = new StringBuffer("https://api.genderize.io/?name[]=");
		url.append(personName);
		Person ps[] = sendRequest(url.toString());
		
		// combine in parameter array of objects
		int c =0;
		for(Person obj : ps){
			personObj[c].setGender(obj.getGender());
			personObj[c].setProbability(obj.getProbability());
			c++;
		}
		return personObj;
	}

	public static void predictAgeNGender(String personName){
		try{
			if(personName == null){
				System.out.println("Invalid Person Name. It cannot be null");
			}else{	// If user pass multiple name
				personName = personName.replaceAll(",", "&name[]=");
			}
			Person[] personObj = getAge(personName);
			personObj = getGender(personObj, personName);
			
			// print
			for(Person tempObj : personObj)
				System.out.println(tempObj.toString());

		}catch (Exception e) {
			e.printStackTrace();
//			System.err.println(e.getMessage());
		}
	}

	public static void main( String[] args){
		try{
			Scanner sc = new Scanner(System.in);
			System.out.println("Enter Name of Person (comma seperated if multiple):");
			String name = sc.nextLine();
			
			predictAgeNGender(name);
		}catch (Exception e) {
			e.printStackTrace();
		}
	}
}

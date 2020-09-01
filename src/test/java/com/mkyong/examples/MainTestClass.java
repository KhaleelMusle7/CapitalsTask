package com.mkyong.examples;

import static io.restassured.RestAssured.given;
import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import static org.junit.jupiter.api.Assertions.*;
import java.util.ArrayList;
import org.junit.jupiter.api.Test;
import io.restassured.RestAssured;
import io.restassured.path.json.JsonPath;

public class MainTestClass {

	int capitalNo = 45;
	int successStatusCode = 200;
	String countriesApiResponse;
	String capitalApiResponse;
	String capital;
	ArrayList<String> currencyFromCountry;
	ArrayList<String> currencyFromCapital;
	JsonPath js1;
	JsonPath js2;

	@Test
	public void positiveScenario() {

		RestAssured.baseURI = "https://restcountries.eu";

		/////////// Countries API Call /////////

		countriesApiResponse = given().when().get("/rest/v2/all").then().extract().asString();
		js1 = new JsonPath(countriesApiResponse);
		capital = js1.get("capital[" + capitalNo + "]");
		currencyFromCountry = js1.get("currencies[" + capitalNo + "].code");

		System.out.println("currencyFromCountry is : " + currencyFromCountry);
		System.out.println("pulledCapital is : " + capital);

		/////////// Capital API Call /////////

		capitalApiResponse = given().when().get("https://restcountries.eu/rest/v2/capital/" + capital + "")

		//// 1st Positive Assertion - Status code check - is 200 ////

				.then().assertThat().statusCode(successStatusCode).extract().asString();

		js2 = new JsonPath(capitalApiResponse);
		currencyFromCapital = js2.get("currencies[0].code");

		System.out.println("currencyFromCapital is : " + currencyFromCapital);

		//// 2nd Positive Assertion - Validating the "Capital" API matches the currency code in
		//// the "Countries" API ////

		assertEquals(currencyFromCapital, currencyFromCountry);

		//// 3rd Positive Assertion - Response body schema validation ////

		given().when().get("https://restcountries.eu/rest/v2/capital/" + capital + "").then().assertThat()
				.body(matchesJsonSchemaInClasspath("capitalApiSchema.json"));

	}

	@Test
	public void negativeScenario() {

		RestAssured.baseURI = "https://restcountries.eu";

		/////////// Countries API Call /////////

		countriesApiResponse = given().when().get("/rest/v2/all").then().extract().asString();
		js1 = new JsonPath(countriesApiResponse);
		capital = js1.get("capital[10]");
		currencyFromCountry = js1.get("currencies[" + capitalNo + "].code");
		System.out.println("currencyFromCountry is : " + currencyFromCountry);

		//// 1st Negative Assertion - Status code check - is not 400 ////

		int statusCode = given().when().get("https://restcountries.eu/rest/v2/capital/" + capital + "").then()
				.assertThat().statusCode(200).extract().statusCode();

		assertNotEquals(statusCode, 400);
		
		/////////// Capital API Call /////////

		capitalApiResponse = given().when().get("https://restcountries.eu/rest/v2/capital/" + capital + "").asString();

		js2 = new JsonPath(capitalApiResponse);
		currencyFromCapital = js2.get("currencies[0].code");

		System.out.println("currencyFromCapital is : " + currencyFromCapital);

		//// 2nd Negative Assertion - Validating the "Capital" API does not match a particular currency code in
		//// the "Countries" API ////

		assertNotEquals(currencyFromCapital, "CAD");

	}

}
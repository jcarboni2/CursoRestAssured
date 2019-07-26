package br.ce.jhenck;

import static io.restassured.RestAssured.given;

import org.junit.Test;
import org.xml.sax.SAXParseException;

import io.restassured.matcher.RestAssuredMatchers;
import io.restassured.module.jsv.JsonSchemaValidator;

public class SchemaTest {
	
	@Test
	public void deveValidarSchemaXml() {
		given()
			.log().all()
		.when()
			.get("https://restapi.wcaquino.me/usersXML")
		.then()
			.log().all()
			.statusCode(200)
			.body(RestAssuredMatchers.matchesXsdInClasspath("users.xsd"));
	}

	@Test(expected=SAXParseException.class)
	public void naoDeveValidarSchemaXmlInvalido() {
		given()
			.log().all()
		.when()
			.get("https://restapi.wcaquino.me/InvalidUsersXML")
		.then()
			.log().all()
			.statusCode(200)
			.body(RestAssuredMatchers.matchesXsdInClasspath("users.xsd"));
	}
	
	@Test
	public void deveValidarSchemaJson() {
		given()
			.log().all()
		.when()
			.get("https://restapi.wcaquino.me/users")
		.then()
			.log().all()
			.statusCode(200)
			.body(JsonSchemaValidator.matchesJsonSchemaInClasspath("users.json"));
	}
}

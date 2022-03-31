package in.solaceits;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.lang.model.element.Modifier;

import org.springframework.stereotype.Component;

import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeSpec;
import com.sun.jdi.Value;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.SignatureAlgorithm;

public class JWTUGeneration {

	public static void generate() {
		TypeSpec jwtUtil = TypeSpec.classBuilder("JwtUtil")
				.addModifiers(Modifier.PUBLIC)
				.addAnnotation(Component.class)
				.addField(
						FieldSpec.builder(String.class, "jwtSecret")
						.addModifiers(Modifier.PRIVATE)
						.addAnnotation(
								AnnotationSpec.builder(Value.class).addMember("value", "$L{app.secret}", "$")
								.build()).build())
				.addMethod(
						MethodSpec.methodBuilder("generateToken")
						.addModifiers(Modifier.PUBLIC)
						.addParameter(
								ParameterizedTypeName.get(Map.class, String.class,Object.class), "claims"
								)
						.addParameter(String.class, "subject")
						.addStatement("return Jwts.builder()\r\n"
								+ "				.setClaims(claims)\r\n"
								+ "				.setSubject(subject)\r\n"
								+ "				.setIssuer(\"SOLACEITS\")\r\n"
								+ "				.setIssuedAt(new Date(System.currentTimeMillis()))\r\n"
								+ "				.setExpiration(new Date(System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(30)))\r\n"
								+ "				.signWith($L.HS512, secret)\r\n"
								+ "				.compact()", SignatureAlgorithm.class.getName())
						.returns(String.class).build()
						)
				.addMethod(
						MethodSpec.methodBuilder("getClaims")
						.addModifiers(Modifier.PUBLIC)
						.addParameter(String.class, "token")
						.addStatement(
								"return Jwts.parser()\r\n"
								+ "				.setSigningKey(secret)\r\n"
								+ "				.parseClaimsJws(token)\r\n"
								+ "				.getBody();"
								)
						.returns(Claims.class).build()
						)
				.addMethod(
						MethodSpec.methodBuilder("getExpiryDate")
						.addModifiers(Modifier.PUBLIC)
						.addParameter(String.class, "token")
						.addStatement(
								"return getClaims(token).getExpiration();"
								)
						.returns(Date.class).build()
						)
				.addMethod(
						MethodSpec.methodBuilder("getUserName")
						.addModifiers(Modifier.PUBLIC)
						.addParameter(String.class, "token")
						.addStatement(
								"return getClaims(token).getSubject();"
								)
						.returns(String.class).build()
						
						)
				.addMethod(
						MethodSpec.methodBuilder("generateToken")
						.addModifiers(Modifier.PUBLIC)
						.addParameter(String.class, "username")
						.addStatement(
								"Map<String, Object> claims = new $L<>();\r\n"
								+ "		return generateToken(claims, username);", HashMap.class.getName()
								)
						.returns(String.class).build()
				        )
				.addMethod(
						MethodSpec.methodBuilder("isTokenExpired")
						.addModifiers(Modifier.PUBLIC)
						.addParameter(String.class, "token")
						.addStatement(
								"final Date expiration = getExpDate(token);\r\n"
								+ "		return expiration.before(new Date());", Date.class.getName()
								)
						.returns(boolean.class).build()
						)
				.addMethod(
						MethodSpec.methodBuilder("isTokenExpired")
						.addModifiers(Modifier.PUBLIC)
						.addParameter(String.class, "token")
						.addParameter(String.class, "username")
						.addStatement(
								"String usernameInToken = getUsername(token);\r\n"
								+ "		return (usernameInToken.equals(username) && !isTokenExpired(token));"
								)
						.returns(boolean.class).build()
						)
				.build();
				
		JavaFile javaFile = JavaFile
				.builder("in.solaceits",jwtUtil)
				.indent("	")
				.build();
				
		try {
			javaFile.writeTo(new File("F:\\Download"));
		} catch (IOException e) {
			e.printStackTrace();
		}				
	}
	
}

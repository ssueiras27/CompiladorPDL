import java.io.File;
import java.util.concurrent.Semaphore;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Set;
import java.util.TreeSet;


public class AnalizadorLexico{
	private int estado; // estado interno del automata
	private String lexema;
	private int cont;
	private int valor;
	public ArrayList<Token> tokens = new ArrayList<>();
	private Set<String> Tabla_PR = new TreeSet<>();
	public TablaSimbolos TS;
	public AnalizadorSintacticoSemantico AS;
	public GestorErrores GE;
	public int linea = 1;
	
	public boolean tokenGenerado;
	public Token lastToken;
	public char lastCar = '\0';
	
	public FileInputStream fis;
	
	public Semaphore darToken = new Semaphore(0);
	
	public AnalizadorLexico(String filepath) {
		File fichero = new File(filepath);
		if (!fichero.exists()) {
			System.out.println(fichero.getName() + " no existe.");
			return;
		}
		if (!(fichero.isFile() && fichero.canRead())) {
			System.out.println(fichero.getName() + " no se pudo leer.");
			return;
		}
		try {
			fis = new FileInputStream(fichero);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		estado = 0;
		Tabla_PR.add("function");
		Tabla_PR.add("boolean");
		Tabla_PR.add("int");
		Tabla_PR.add("string");
		Tabla_PR.add("while");
		Tabla_PR.add("if");
		Tabla_PR.add("return");
		Tabla_PR.add("let");
		Tabla_PR.add("true");
		Tabla_PR.add("false");
		Tabla_PR.add("print");
		Tabla_PR.add("input");
	}

public Token pedirToken() {
	tokenGenerado = false;
	while (!tokenGenerado) {
		char car;
		try {
			if (fis.available() > 0) {
				if(lastCar!='\0') {
					car = lastCar;
					lastCar = '\0';
				}else {
					car = (char) fis.read();
				}
				if(car == '\n')linea++;
				Leer(car);
			}else {
				GenToken("eof", null);
				fis.close();
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}	
		System.out.println("[AL] Mandando Token AL->AS: "+ lastToken);
		return lastToken;
	}
	
	private void Leer(char car) {
		switch (estado) {
		case 0:
			switch (car) {
			case '"':
				lexema = "";
				cont = 0;
				estado = 1;
				break;
			case '%':
				estado = 7;
				break;
			case '&':
				estado = 9;
				break;
			case '/':
				estado = 11;
				break;
			case '!':
				GenToken("neg", null);
				estado = 0;
				break;
			case '(':
				GenToken("parab", null);
				estado = 0;
				break;
			case ')':
				GenToken("parcerr", null);
				estado = 0;
				break;
			case '{':
				GenToken("llavab", null);
				estado = 0;
				break;
			case '}':
				GenToken("llavcerr", null);
				estado = 0;
				break;
			case '>':
				GenToken("mayor", null);
				estado = 0;
				break;
			case '<':
				GenToken("menor", null);
				estado = 0;
				break;
			case '+':
				GenToken("suma", null);
				estado = 0;
				break;
			case '-':
				GenToken("resta", null);
				estado = 0;
				break;
			case '=':
				GenToken("asig", null);
				estado = 0;
				break;
			case ';':
				GenToken("ptocoma", null);
				estado = 0;
				break;
			case ',':
				GenToken("coma", null);
				estado = 0;
				break;
			default:
				if (Character.isAlphabetic(car)) {
					lexema = "" + car;
					estado = 5;
				} else if (Character.isDigit(car)) {
					valor = Character.getNumericValue(car);
					estado = 3;
				} else {
					// ERROR CARACTER NO RECONOCIBLE O DEL
				}
				break;
			}
			break;
		case 1:
			if (car == '"') {
				if (cont > 64) {
					GE.newError("Cadena demasiado larga", linea, "Lexico");
					estado = 0;
				} else {
					GenToken("cadena", lexema);
					estado = 0;
				}
			} else {
				lexema = lexema + car;
				cont++;
			}
			break;
		case 3:
			if (Character.isDigit(car)) {
				valor = valor * 10 + Character.getNumericValue(car);
			} else {
				if (valor > 32767) {
					GE.newError("Número demasiado grande", linea, "Lexico");
					estado = 0;
				} else {
					GenToken("entero", valor);
					estado = 0;
					lastCar = car;
				}
			}
			break;
		case 5:
			if (Character.isDigit(car) || Character.isAlphabetic(car) || car == '_') {
				lexema = lexema + car;
			} else {
				if (Tabla_PR.contains(lexema)) {
					GenToken(lexema, null);
				}else {
					if (TS.getPosTS(lexema) == -1){
						System.out.println("----------------------------------------- "+AS.ZonaDecl);
						TS.newEntrada(lexema, AS.ZonaDecl);
					}
					GenToken("id", TS.getPosTS(lexema));
				}
				estado = 0;
				lastCar = car;
			}
			break;
		case 7:
			if (car == '=') {
				GenToken("asigmod", null);
				estado = 0;
			} else {
				GE.newError("Caracter no esperado '"+car+"', se esperaba '='", linea, "Lexico");
				estado = 0;
			}
			break;
		case 9:
			if (car == '&') {
				GenToken("and", null);
				estado = 0;
			} else {
				GE.newError("Caracter no esperado '"+car+"', se esperaba '&'", linea, "Lexico");
				estado = 0;
			}
			break;
		case 11:
			if (car == '/') {
				estado = 12;
			} else {
				GE.newError("Inicio de comentario erroneo '"+car+"', se esperaba '/'", linea, "Lexico");
				estado = 0;
			}
			break;
		case 12:
			if (car == '\n') {
				estado = 0;
			}
			break;
		}
	}

	private void GenToken(String ntoken, Object prop) {
		Token newToken = new Token(ntoken, prop);
		tokens.add(newToken);
		lastToken = newToken;
		tokenGenerado = true;
	}
	
}

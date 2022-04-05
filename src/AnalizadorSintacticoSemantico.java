import java.io.FileWriter;
import java.util.concurrent.Semaphore;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class AnalizadorSintacticoSemantico{
	public Queue<Token> cola;
	private ArrayList<Integer> Parser;
	private Token sigToken;
	public AnalizadorLexico AL;
	public GestorErrores GE;
	private int DespG;
	private int DespL;
	public boolean ZonaDecl = false;
	
	public Semaphore pedirToken = new Semaphore(0);
	public TablaSimbolos TS;

	public AnalizadorSintacticoSemantico() {
		cola = new ConcurrentLinkedQueue<>();
		Parser = new ArrayList<>();
	}

	public void Analizar() {
		System.out.println("[AS] Iniciandose");
		DespG = 0;
		sigToken = getSigToken();
		P();
	}

	private Token getSigToken() {
		Token tok = AL.pedirToken();
		System.out.println("[AS] Token recibido: <" + tok.getnToken()+" , "+tok.getprop() + ">");
		System.out.println("[AS] Parser: " + Parser);
		return tok;
	}

	private void Equipara(String s) {
		if (sigToken.getnToken().equals(s)) {
			System.out.println("[AS] Equipara: " + s);
			sigToken = getSigToken();
		} else {
			GE.newError("Token no esperado \""+sigToken.getnToken()+"\", se esperaba \""+s+"\"", AL.linea, "Sintactico");
		}
	}

	private void Print(int n) {
		Parser.add(n);
	}

	private void Aceptada() {
		System.out.println("[AS] PARSER FINAL" + Parser);
		GenTokens();
		GenParse();
		GenTS();
		GenGE();
		
		System.out.println(GE);
		System.out.println(TS);
		GenParse();
	}

	private void GenGE() {
		try {
			FileWriter escritor = new FileWriter("C:\\\\Users\\\\santi\\\\eclipse-workspace\\\\PDL\\\\src\\\\Errores.txt");
			escritor.write(GE + "");
			escritor.close();
		} catch (IOException e) {
			System.out.println("No se pudo Generar el fichero de tokens");
			e.printStackTrace();
		}
		
	}

	private void GenTS() {
		try {
			FileWriter escritor = new FileWriter("C:\\\\Users\\\\santi\\\\eclipse-workspace\\\\PDL\\\\src\\\\TablaSimbolos.txt");
			escritor.write(TS + "");
			escritor.close();
		} catch (IOException e) {
			System.out.println("No se pudo Generar el fichero de tokens");
			e.printStackTrace();
		}
		
	}

	private void GenTokens() {
		try {
			FileWriter escritor = new FileWriter("C:\\\\Users\\\\santi\\\\eclipse-workspace\\\\PDL\\\\src\\\\Tokens.txt");
			for(Token t : AL.tokens) {
				escritor.write(t +"\n");
			}
			escritor.close();
		} catch (IOException e) {
			System.out.println("No se pudo Generar el fichero de tokens");
			e.printStackTrace();
		}
	}

	private void GenParse() {
		try {
			FileWriter escritor = new FileWriter("C:\\\\Users\\\\santi\\\\eclipse-workspace\\\\PDL\\\\src\\\\Parse.txt");
			escritor.write("Descendente");
			for (Integer i : Parser) {
				escritor.write(" " + i);
			}
			escritor.close();
		} catch (IOException e) {
			System.out.println("No se pudo Generar el fichero de tokens");
			e.printStackTrace();
		}
	}

	private void P() {
		if (sigToken.getnToken().equals("let") || sigToken.getnToken().equals("if")
				|| sigToken.getnToken().equals("while") || sigToken.getnToken().equals("id")
				|| sigToken.getnToken().equals("return") || sigToken.getnToken().equals("print")
				|| sigToken.getnToken().equals("input")) {
			Print(1);
			B();
			P();
		} else if (sigToken.getnToken().equals("function")) {
			Print(2);
			F();
			P();
		} else if (sigToken.getnToken().equals("eof")) {
			Print(3);
			Aceptada();
		} else {
			GE.newError("Token no esperado \""+sigToken.getnToken()+"\"", AL.linea, "Sintactico");
		}
	}

	private HashMap<String, String> B() {
		if (sigToken.getnToken().equals("let")) {
			Print(4);
			Equipara("let");
			String tipoT=T(); 
			int posTSid = (int) sigToken.getprop();
			int l=AL.linea;
			String lexId = TS.buscarLexemaTS(posTSid);
			ZonaDecl = true;
			Equipara("id");
			ZonaDecl = false;
			String tipoS3 = S3(tipoT);
			Equipara("ptocoma");
			if(!tipoS3.equals("tipo_error")) {
				TS.insertarTipoTS(posTSid, tipoT);
				if(TS.TSL==null) {
					TS.insertarDesplazamientoTS(posTSid, DespG);
					DespG = DespG + getAncho(tipoT);
				}else {
					TS.insertarDesplazamientoTS(posTSid, DespL);
					DespL = DespL + getAncho(tipoT);
				}
			}else{
				GE.newError("Declaracion incorrecta\""+lexId+"\"",l, "Semantico");
			}
			return null;
		} else if (sigToken.getnToken().equals("if")) {
			Print(5);
			Equipara("if");
			Equipara("parab");
			int l=AL.linea;
			String tipoE = E();
			Equipara("parcerr");
			HashMap<String, String> Sparams = S();
			if(tipoE.equals("log")){
				 HashMap<String,String> Bparams = new HashMap<>();
				 Bparams.put("tipo", Sparams.get("tipo"));
				 Bparams.put("tipoRet", Sparams.get("tipoRet"));
				 return Bparams;
			}else {
				GE.newError("Condicion de if(E) erronea", l, "Semantico");
				return null;
			}
		} else if (sigToken.getnToken().equals("while")) {
			Print(7);
			Equipara("while");
			Equipara("parab");
			int l=AL.linea;
			String tipoE = E();
			Equipara("parcerr");
			Equipara("llavab");
			HashMap<String,String> Cparams=C();
			Equipara("llavcerr");
			if(tipoE.equals("log")){
				 HashMap<String,String> Bparams = new HashMap<>();
				 Bparams.put("tipo", Cparams.get("tipo"));
				 Bparams.put("tipoRet", Cparams.get("tipoRet"));
				 return Bparams;
			}else {
				GE.newError("Condicion del while(E) erronea", l, "Semantico");
				return null;
			}
			
		} else if (sigToken.getnToken().equals("id") || sigToken.getnToken().equals("return")
				|| sigToken.getnToken().equals("print") || sigToken.getnToken().equals("input")) {
			Print(6);
			HashMap<String,String> Sparams = S();
			HashMap<String,String> Bparams = new HashMap<>();
			 Bparams.put("tipo", Sparams.get("tipo"));
			 Bparams.put("tipoRet", Sparams.get("tipoRet"));
			 return Bparams;
		} else {
			GE.newError("Token no esperado \""+sigToken.getnToken()+"\"", AL.linea, "Sintactico");
		}
		return null;
	}

	private int getAncho(String tipoT) {
		int result = 0;
		switch (tipoT) {
		case "ent":
			result = 1;
			break;
		case "log":
			result = 1;
			break;
		case "cad":
			result = 64;
			break;
		}
		return result;
	}
	

	private String T() {
		if (sigToken.getnToken().equals("int")) {
			Print(8);
			Equipara("int");
			return "ent";
		} else if (sigToken.getnToken().equals("boolean")) {
			Print(9);
			Equipara("boolean");
			return "log";
		} else if (sigToken.getnToken().equals("string")) {
			Print(10);
			Equipara("string");
			return "cad";
		} else {
			GE.newError("Token no esperado \""+sigToken.getnToken()+"\"", AL.linea, "Sintactico");
			return "tipo_error";
		}
	}

	private HashMap<String,String> S() {
		if (sigToken.getnToken().equals("id")) {
			Print(11);
			int posTSid = (int) sigToken.getprop();
			String lexId = TS.buscarLexemaTS(posTSid);
			int l=AL.linea;
			Equipara("id");
			if(TS.buscarTipoTS(posTSid)==null){
				TS.insertarTipoTS(posTSid, "ent");
				TS.insertarDesplazamientoTS(posTSid, DespG);
				DespG = DespG + getAncho("ent");
			}
			HashMap<String,String> S2params = S2();
			if(S2params.get("tipo")!=null&&S2params.get("tipo").equals("function")) {
				if(TS.buscarParametrosTS(posTSid).equals(S2params.get("params"))) {
					HashMap<String,String> Sparams = new HashMap<>();
					Sparams.put("tipo", "tipo_ok");
					return Sparams;
				}else {
					HashMap<String,String> Sparams = new HashMap<>();
					Sparams.put("tipo", "tipo_error");
					GE.newError("Los parametros de la funcion \""+lexId+"\" son incorrectos", l, "Semantico");
					return Sparams;
				}
			}else if(TS.buscarTipoTS(posTSid).equals(S2params.get("tipo"))){
				HashMap<String,String> Sparams = new HashMap<>();
				Sparams.put("tipo", "tipo_ok");
				return Sparams;
			}else {
				HashMap<String,String> Sparams = new HashMap<>();
				Sparams.put("tipo", "tipo_error");
				GE.newError("Identificador ilegal para ese tipo \""+lexId+"\"", l, "Semantico");
				return Sparams;
			}
		} else if (sigToken.getnToken().equals("return")) {
			Print(12);
			Equipara("return");
			String tipoX = X();
			Equipara("ptocoma");
			HashMap<String,String> Sparams = new HashMap<>();
			Sparams.put("tipo", "tipo_ok");
			Sparams.put("tipoRet", tipoX);
			return Sparams;
		} else if (sigToken.getnToken().equals("print")) {
			Print(13);
			Equipara("print");
			Equipara("parab");
			int l=AL.linea;
			String tipoE = E();
			Equipara("parcerr");
			Equipara("ptocoma");
			if(tipoE.equals("ent") || tipoE.equals("cad")) {
				HashMap<String,String> Sparams = new HashMap<>();
				Sparams.put("tipo", "tipo_ok");
				return Sparams;
			}else{
				HashMap<String,String> Sparams = new HashMap<>();
				Sparams.put("tipo", "tipo_error");
				GE.newError("Uso incorrecto \"print(id)\" argumento invalido", l, "Semantico");
				return Sparams;
			}
		} else if (sigToken.getnToken().equals("input")) {
			Print(14);
			Equipara("input");
			Equipara("parab");
			Equipara("id");
			Equipara("parcerr");
			Equipara("ptocoma");
			HashMap<String,String> Sparams = new HashMap<>();
			Sparams.put("tipo", "tipo_ok");
			return Sparams;
		} else {
			GE.newError("Token no esperado \""+sigToken.getnToken()+"\"", AL.linea, "Sintactico");
		}
		return null;
	}

	private HashMap<String,String> S2() {
		if (sigToken.getnToken().equals("asig")) {
			Print(15);
			Equipara("asig");
			String tipoE = E();
			Equipara("ptocoma");
			HashMap<String,String> Sparams = new HashMap<>();
			Sparams.put("tipo", tipoE);
			return Sparams;
		} else if (sigToken.getnToken().equals("parab")) {
			Print(16);
			Equipara("parab");
			String tipoL=L();
			Equipara("parcerr");
			Equipara("ptocoma");
			HashMap<String,String> Sparams = new HashMap<>();
			Sparams.put("tipo", "function");
			Sparams.put("params", tipoL);
			return Sparams;
		} else if (sigToken.getnToken().equals("asigmod")) {
			Print(17);
			Equipara("asigmod");
			String tipoE =E();
			Equipara("ptocoma");
			HashMap<String,String> Sparams = new HashMap<>();
			Sparams.put("tipo", tipoE);
			return Sparams;
		} else {
			GE.newError("Token no esperado \""+sigToken.getnToken()+"\"", AL.linea, "Sintactico");
		}
		return null;
	}

	private String X() {
		if (sigToken.getnToken().equals("neg") || sigToken.getnToken().equals("id")
				|| sigToken.getnToken().equals("parab") || sigToken.getnToken().equals("entero")
				|| sigToken.getnToken().equals("cadena") || sigToken.getnToken().equals("true")
				|| sigToken.getnToken().equals("false")) {
			Print(18);
			String tipoE =E();
			return tipoE;
		} else if (sigToken.getnToken().equals("ptocoma")) {
			Print(19);
			return "vacio";

		} else {
			GE.newError("Token no esperado \""+sigToken.getnToken()+"\"", AL.linea, "Sintactico");
		}
		return null;
	}

	private HashMap<String,String> C() {
		if (sigToken.getnToken().equals("let") || sigToken.getnToken().equals("if")
				|| sigToken.getnToken().equals("while") || sigToken.getnToken().equals("id")
				|| sigToken.getnToken().equals("return") || sigToken.getnToken().equals("print")
				|| sigToken.getnToken().equals("input")) {
			Print(20);
			HashMap<String,String> Bparams = B();
			HashMap<String,String> C1params = C();
			if(C1params.get("tipo").equals("vacio"))
			{
				HashMap<String,String> Cparams = new HashMap<String,String>();
				Cparams.put("tipo",Bparams.get("tipo"));
				Cparams.put("tipoRet",Bparams.get("tipoRet"));
				return Cparams;

			}
			else{
				HashMap<String,String> Cparams = new HashMap<String,String>();
				Cparams.put("tipo",C1params.get("tipo"));
				Cparams.put("tipoRet",C1params.get("tipoRet"));
				return Cparams;
			}

		} else if (sigToken.getnToken().equals("llavcerr")) {
			Print(21);
			HashMap<String,String> Cparams = new HashMap<String,String>();
			Cparams.put("tipo","vacio");
			return Cparams;
		} else {
			GE.newError("Token no esperado \""+sigToken.getnToken()+"\"", AL.linea, "Sintactico");
		}
		return null;
	}

	private String L() {
		if (sigToken.getnToken().equals("neg") || sigToken.getnToken().equals("id")
				|| sigToken.getnToken().equals("parab") || sigToken.getnToken().equals("entero")
				|| sigToken.getnToken().equals("cadena") || sigToken.getnToken().equals("true")
				|| sigToken.getnToken().equals("false")) {
			Print(22);
			
			String tipoE = E();
			String tipoQ =Q();
			if(tipoQ.equals("vacio"))
			{
				return tipoE;
		
			}else{
				return tipoE+" "+tipoQ;
				}
		} else if (sigToken.getnToken().equals("parcerr")) {
			Print(23);
			return "vacio";
		} else {
			GE.newError("Token no esperado \""+sigToken.getnToken()+"\"", AL.linea, "Sintactico");
		}
		return null;
	}

	private String Q() {
		if (sigToken.getnToken().equals("coma")) {
			Print(24);
			Equipara("coma");
			String tipoE = E();
			String tipoQ1 =Q();
			if(tipoQ1.equals("vacio"))
			{
				return tipoE;
			}
			else{
				return tipoE+" "+tipoQ1;
			}
		} else if (sigToken.getnToken().equals("parcerr")) {
			Print(25);
			return "vacio";
		} else {
			GE.newError("Token no esperado \""+sigToken.getnToken()+"\"", AL.linea, "Sintactico");
		}
		return null;
	}

	private void F() {
		if (sigToken.getnToken().equals("function")) {
			Print(26);
			Equipara("function");
			int posTSid = (int) sigToken.getprop();
			Equipara("id");
			int l=AL.linea;
			String tipoRetH = H();
			TS.generarTablaLocal();
			DespL = 0;
			ZonaDecl = true;
			Equipara("parab");
			String tipoA =A();
			Equipara("parcerr");
			ZonaDecl = false;
			Equipara("llavab");
			HashMap<String,String> Cparams = C();
			Equipara("llavcerr");
			if(!Cparams.get("tipoRet").equals(tipoRetH) || Cparams.get("tipoRet").equals("tipo_error")) {
				GE.newError("Tipo Devuelto de la funcion \""+Cparams.get("tipoRet")+"\" erroneo, se esperaba \""+tipoRetH+"\" ", l, "Semantico");
			}else {
				TS.insertarFunction(posTSid,tipoRetH, tipoA , tipoA.split(" ").length);
				TS.destruirTablaLocal(posTSid);
			}
		} else {
			GE.newError("Token no esperado \""+sigToken.getnToken()+"\"", AL.linea, "Sintactico");
		}
	}

	private String H() {
		if (sigToken.getnToken().equals("int") || sigToken.getnToken().equals("boolean")
				|| sigToken.getnToken().equals("string")) {
			Print(27);
			String tipoRetT = T();
			return tipoRetT;
		} else if (sigToken.getnToken().equals("parab")) {
			Print(28);
			return "vacio";
		} else {
			GE.newError("Token no esperado \""+sigToken.getnToken()+"\"", AL.linea, "Sintactico");
		}
		return "tipo_error";
	}

	private String A() {
		if (sigToken.getnToken().equals("int") || sigToken.getnToken().equals("boolean")
				|| sigToken.getnToken().equals("string")) {
			Print(29);
			String tipoT = T();
			int posTSid = (int) sigToken.getprop();
			Equipara("id");
			TS.insertarTipoTS(posTSid,tipoT);
			TS.insertarDesplazamientoTS(posTSid,DespL);
			DespL = DespL + getAncho(tipoT);
			String tipoK1 = K();
			String tipoA = "";
			if(tipoK1.equals("vacio")) {
				tipoA = tipoT;
			}else {
				tipoA = tipoT + " " + tipoK1;
			}
			return tipoA;
		} else if (sigToken.getnToken().equals("parcerr")) {
			Print(30);
			return "vacio";
		} else {
			GE.newError("Token no esperado \""+sigToken.getnToken()+"\"", AL.linea, "Sintactico");
		}
		return "tipo_error";
	}

	private String K() {
		if (sigToken.getnToken().equals("coma")) {
			Print(31);
			Equipara("coma");
			String tipoT = T();
			int posTSid = (int) sigToken.getprop();
			Equipara("id");
			String tipoK1 = K();
			String tipoK = "";
			if(tipoK1.equals("vacio")) {
				tipoK = tipoT;
			}else {
				tipoK = tipoK + " " + tipoK1;
			}
			TS.insertarTipoTS(posTSid,tipoT);
			TS.insertarDesplazamientoTS(posTSid,DespL);
			DespL = DespL + getAncho(tipoT);
			return tipoK;
		} else if (sigToken.getnToken().equals("parcerr")) {
			Print(32);
			return "vacio";
		} else {
			GE.newError("Token no esperado \""+sigToken.getnToken()+"\"", AL.linea, "Sintactico");
		}
		return "tipo_error";
	}

	private String E() {
		if (sigToken.getnToken().equals("neg") || sigToken.getnToken().equals("id")
				|| sigToken.getnToken().equals("parab") || sigToken.getnToken().equals("entero")
				|| sigToken.getnToken().equals("cadena") || sigToken.getnToken().equals("true")
				|| sigToken.getnToken().equals("false")) {
			Print(33);
			String tipoR= R();
			String tipoE2=E2(tipoR);
			return tipoE2;
		} else {
			GE.newError("Token no esperado \""+sigToken.getnToken()+"\"", AL.linea, "Sintactico");
		}
		return "tipo_error";
	}

	private String E2(String tipoE2) {
		if (sigToken.getnToken().equals("and")) {
			Print(34);
			int l=AL.linea;
			Equipara("and");
			String tipoR=R();
			if(tipoE2.equals("log") && tipoR.equals("log")) {
				tipoE2 = "log";
			}else {
				GE.newError("Uso incorrecto \"E && E \" han de ser expresiones logicas", l, "Semantico");
				tipoE2 = "tipo_error";
			}
			tipoE2 = E2(tipoE2);
			return tipoE2;
		} else if (sigToken.getnToken().equals("ptocoma") || sigToken.getnToken().equals("parcerr")
				|| sigToken.getnToken().equals("coma")) {
			Print(35);
			return tipoE2;
		} else {
			GE.newError("Token no esperado \""+sigToken.getnToken()+"\"", AL.linea, "Sintactico");
		}
		return "tipo_error";
	}

	private String R() {
		if (sigToken.getnToken().equals("neg") || sigToken.getnToken().equals("id")
				|| sigToken.getnToken().equals("parab") || sigToken.getnToken().equals("entero")
				|| sigToken.getnToken().equals("cadena") || sigToken.getnToken().equals("true")
				|| sigToken.getnToken().equals("false")) {
			Print(36);
			String tipoU = U();
			String tipoR2 =R2(tipoU);
			return tipoR2;
		} else {
			GE.newError("Token no esperado \""+sigToken.getnToken()+"\"", AL.linea, "Sintactico");
		}
		return "tipo_error";
	}

	private String R2(String tipoR2) {
		if (sigToken.getnToken().equals("mayor")) {
			Print(37);
			int l=AL.linea;
			Equipara("mayor");
			String tipoU=U();
			if(tipoR2.equals("ent")&& tipoU.equals("ent")) {
				tipoR2 = "log";
			}else {
				GE.newError("Uso incorrecto \"E > E \" han de ser expresiones enteras", l, "Semantico");
				tipoR2 = "tipo_error";
			}
			String tipoR22=R2(tipoR2);
			return tipoR22;
		} else if (sigToken.getnToken().equals("menor")) {
			Print(38);
			int l=AL.linea;
			Equipara("menor");
			String tipoU=U();
			if(tipoR2.equals("ent")&& tipoU.equals("ent")) {
				tipoR2 = "log";
			}else {
				GE.newError("Uso incorrecto \"E < E \" han de ser expresiones enteras", l, "Semantico");
				tipoR2 = "tipo_error";
			}
			String tipoR22=R2(tipoR2);
			return tipoR22;
		} else if (sigToken.getnToken().equals("ptocoma") || sigToken.getnToken().equals("parcerr")
				|| sigToken.getnToken().equals("coma") || sigToken.getnToken().equals("and")) {
			Print(39);
			return tipoR2;
		} else {
			GE.newError("Token no esperado \""+sigToken.getnToken()+"\"", AL.linea, "Sintactico");
		}
		return "tipo_error";
	}

	private String U() {
		if (sigToken.getnToken().equals("neg") || sigToken.getnToken().equals("id")
				|| sigToken.getnToken().equals("parab") || sigToken.getnToken().equals("entero")
				|| sigToken.getnToken().equals("cadena") || sigToken.getnToken().equals("true")
				|| sigToken.getnToken().equals("false")) {
			Print(40);
			String tipoU2=V();
			return U2(tipoU2);
		} else {
			GE.newError("Token no esperado \""+sigToken.getnToken()+"\"", AL.linea, "Sintactico");
		}
		return "tipo_error";
	}

	private String U2(String tipoU2) {
		if (sigToken.getnToken().equals("suma")) {
			Print(41);
			int l=AL.linea;
			Equipara("suma");
			String tipoV = V();
			String tipoU22 = null; 
			if(tipoU2.equals("ent") && tipoV.equals("ent")) {
				tipoU22 = "ent"; 
			}else {
				GE.newError("Uso incorrecto \"E + E \" han de ser expresiones enteras", l, "Semantico");
				tipoU2 = "tipo_error";
			}
			tipoU2 = U2(tipoU22);
			return tipoU2;
		} else if (sigToken.getnToken().equals("resta")) {
			Print(42);
			int l=AL.linea;
			Equipara("resta");
			String tipoV = V();
			String tipoU22 = null; 
			if(tipoU2.equals("ent") && tipoV.equals("ent")) {
				tipoU22 = "ent"; 
			}else {
				GE.newError("Uso incorrecto \"E - E \" han de ser expresiones enteras", l, "Semantico");
				tipoU2 = "tipo_error";
			}
			tipoU2 = U2(tipoU22);
			return tipoU2;
		} else if (sigToken.getnToken().equals("ptocoma") || sigToken.getnToken().equals("parcerr")
				|| sigToken.getnToken().equals("coma") || sigToken.getnToken().equals("and")
				|| sigToken.getnToken().equals("mayor") || sigToken.getnToken().equals("menor")) {
			Print(43);
			return tipoU2;
		} else {
			GE.newError("Token no esperado \""+sigToken.getnToken()+"\"", AL.linea, "Sintactico");
		}
		return "tipo_error";
	}

	private String V() {
		if (sigToken.getnToken().equals("neg")) {
			Print(44);
			Equipara("neg");
			int l=AL.linea;
			String tipoV2=V();
			String tipoV;
			if(!tipoV2.equals("log")) {
				GE.newError("Uso incorrecto \"!E\" ha de ser una expresion logica", l, "Semantico");
				tipoV = "tipo_error";
			}else {
				tipoV = "log";
			}
			return tipoV;
		} else if (sigToken.getnToken().equals("id") || sigToken.getnToken().equals("parab")
				|| sigToken.getnToken().equals("entero") || sigToken.getnToken().equals("cadena")
				|| sigToken.getnToken().equals("true") || sigToken.getnToken().equals("false")) {
			Print(45);
			return W();
		} else {
			GE.newError("Token no esperado \""+sigToken.getnToken()+"\"", AL.linea, "Sintactico");
		}
		return "tipo_error";
	}

	private String W() {
		if (sigToken.getnToken().equals("id")) {
			Print(46);
			int posTSid = (int) sigToken.getprop();
			String lexId = TS.buscarLexemaTS(posTSid);
			int l=AL.linea;
			Equipara("id");
			HashMap<String,String> W2params=W2();
			if(!W2params.get("tipo").equals("vacio")){
				if(W2params.get("params").equals(TS.buscarParametrosTS(posTSid))) {
					return TS.buscarTipoDevueltoTS(posTSid); 
				}else {
					GE.newError("Los parametros de la funcion \""+lexId+"\" son incorrectos", l, "Semantico");
					return "tipo_error";
				}
			}else {
				if(TS.buscarTipoTS(posTSid) == null) {
					TS.insertarTipoTS(posTSid, "ent");
					TS.insertarDesplazamientoTS(posTSid, DespG);
					DespG = DespG + getAncho("ent");
				}
				return TS.buscarTipoTS(posTSid); 
			}
		} else if (sigToken.getnToken().equals("parab")) {
			Print(47);
			Equipara("parab");
			String tipoE=E();
			Equipara("parcerr");
			return tipoE;
		} else if (sigToken.getnToken().equals("entero")) {
			Print(48);
			Equipara("entero");
			return "ent";
		} else if (sigToken.getnToken().equals("cadena")) {
			Print(49);
			Equipara("cadena");
			return "cad";
		} else if (sigToken.getnToken().equals("true")) {
			Print(50);
			Equipara("true");
			return "log";
		} else if (sigToken.getnToken().equals("false")) {
			Print(51);
			Equipara("false");
			return "log";
		} else {
			GE.newError("Token no esperado \""+sigToken.getnToken()+"\"", AL.linea, "Sintactico");
		}
		return "tipo_error";
	}

	private HashMap<String, String> W2() {
		if (sigToken.getnToken().equals("parab")) {
			Print(52);
			Equipara("parab");
			HashMap<String, String> W2params = new HashMap<>();
			W2params.put("params",L());
			W2params.put("tipo","tipo_ok");
			Equipara("parcerr");
			return W2params;
			
		} else if (sigToken.getnToken().equals("ptocoma") || sigToken.getnToken().equals("parcerr")
				|| sigToken.getnToken().equals("coma") || sigToken.getnToken().equals("and")
				|| sigToken.getnToken().equals("mayor") || sigToken.getnToken().equals("menor")
				|| sigToken.getnToken().equals("suma") || sigToken.getnToken().equals("resta")) {
			Print(53);
			HashMap<String, String> W2params = new HashMap<>();
			W2params.put("tipo","vacio");
			return W2params;
		} else {
			
			GE.newError("Token no esperado \""+sigToken.getnToken()+"\"", AL.linea, "Sintactico");
		}
		return null;
	}

	private String S3(String tipo) {
		if (sigToken.getnToken().equals("asig")) {
			Print(54);
			Equipara("asig");
			String tipoE=E();
			if(!tipo.equals(tipoE)) {
				return "tipo_error";
			}
			return tipoE;
		} else if (sigToken.getnToken().equals("asigmod")) {
			Print(55);
			Equipara("asigmod");
			String tipoE=E();
			if(!tipo.equals(tipoE)) {
				return "tipo_error";
			}
			return tipoE;
		} else if (sigToken.getnToken().equals("ptocoma")) {
			Print(56);
			return "vacio";
		} else {
			GE.newError("Token no esperado \""+sigToken.getnToken()+"\"", AL.linea, "Sintactico");
		}
		return "tipo_error";
	}

	
}
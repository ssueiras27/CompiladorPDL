

public class Procesador {

	private static AnalizadorLexico AL;
	private static TablaSimbolos TS;
	private static AnalizadorSintacticoSemantico AS;
	private static GestorErrores GE ;
	
	
	public static void main(String[] args) {
		GE = new GestorErrores();
		TS = new TablaSimbolos();
		AS = new AnalizadorSintacticoSemantico();
		AL = new AnalizadorLexico("C:\\Users\\santi\\eclipse-workspace\\PDL\\src\\prueba1.txt");
		AS.GE = GE;
		AL.TS = TS;
		AS.GE = GE;
		AS.TS = TS;
		AL.AS = AS;
		AS.AL = AL;
		
		AS.Analizar();
		
	}
}

import java.util.ArrayList;

public class GestorErrores {
	private class Error{
		public String error;
		public int linea;
		public String tipo;
		
		Error(String e, int l, String t){
			error = e;
			linea = l;
			tipo = t;
		}
		@Override
		public String toString() {
			return "Error ("+tipo+"): "+error +" en linea: "+ linea;
		}
	}
	ArrayList<Error> listado;
	
	GestorErrores(){
		listado = new ArrayList<>();
	}
	
	void newError(String e, int l, String t) { 
		listado.add(new Error(e, l,t));
		if(t.equals("Sintactico")) {
			System.out.println(this);
			System.exit(-1);
		}
	}
	
	@Override
	public String toString() {
		String result ="ERRORES: \n";
		for( Error e : listado) {
			result = result + e +"\n"; 
		}
		return result;
	}
}

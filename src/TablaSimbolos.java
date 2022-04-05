import java.util.HashMap;
import java.util.Map;

public class TablaSimbolos {

	private int idT = 1;
	private int index = 0;
	public Map<Integer,Entrada> TSG;
	public Map<Integer,Entrada> TSL;
	
	HashMap<String, Map<Integer,Entrada>> TSold = new HashMap<>();
	
	
	class Entrada {
		public String lexema;
		public String tipo = null;
		public int desplazamiento = -1;
		public String tipoParametros = null;
		public int numParametros = 0;
		public String modoPaso = null;
		public String tipoDev = null;
		
		Entrada(String l){
			this.lexema = l;
		}
	}

	TablaSimbolos() {
		TSG = new HashMap<>();
		TSL = null;
	}
	
	public void generarTablaLocal() {
		TSL = new HashMap<>();
	}
	
	public void destruirTablaLocal(int posTS) {
		Map<Integer,Entrada> TSaux = new HashMap<>();
		TSaux.putAll(TSL);
		TSold.put(TSG.get(posTS).lexema, TSaux);
		TSL = null;
	}

	public void newEntrada(String lexema, boolean local) {
		if(TSL != null && local) {
			TSL.put(index++, new Entrada(lexema));
		}else {
			TSG.put(index++, new Entrada(lexema));
		}
	}
	public void insertarTipoTS(int posTS, String tipo) {
		if(TSL != null && TSL.containsKey(posTS)) {
			TSL.get(posTS).tipo = tipo;
		}else {
			TSG.get(posTS).tipo = tipo;
		}
	}
	
	public void insertarDesplazamientoTS(int posTS, int desp) {
		if(TSL != null&& TSL.containsKey(posTS)) {
			TSL.get(posTS).desplazamiento = desp;
		}else {
			TSG.get(posTS).desplazamiento = desp;
		}
	}
	
	public void insertarTipoDevTS(int posTS, String tipo) {
		if(TSL != null&& TSL.containsKey(posTS)) {
			TSL.get(posTS).tipoDev = tipo;
		}else {
			TSG.get(posTS).tipoDev = tipo;
		}
	}
	
	public void insertarParametrosTS(int posTS, String tipos) {
		if(TSL != null&& TSL.containsKey(posTS)) {
			TSL.get(posTS).tipoParametros = tipos;
		}else {
			TSG.get(posTS).tipoParametros = tipos;
		}
	}
	
	public void insertarFunction(int posTSid ,String tipoRetH, String tipoA, int length) {
		TSG.get(posTSid).tipoDev = tipoRetH;
		TSG.get(posTSid).tipoParametros = tipoA;
		TSG.get(posTSid).numParametros = length;
		TSG.get(posTSid).tipo = "function";
	}
	
	public String buscarTipoTS(int posTS) {
		String tipo = null;
		if(TSL != null) {
			try {
				tipo =TSL.get(posTS).tipo;
			} catch (Exception e) {
				tipo = null;
			}
		}
		if(tipo == null ){
			try {
				tipo =TSG.get(posTS).tipo;
			} catch (Exception e) {
				tipo = null;
			}
		}
		return tipo;
	}
	
	public String buscarParametrosTS(int posTS) {
		String params = null;
		if(TSL != null) {
			try {
				params =TSL.get(posTS).tipoParametros;
			} catch (Exception e) {
				params = null;
			}
		}
		if(params == null ){
			params = TSG.get(posTS).tipoParametros;
		}
		return params;
	}
	
	public String buscarLexemaTS(int posTS) {
		String lex = null;
		if(TSL != null) {
			try {
				lex =TSL.get(posTS).lexema;
			} catch (Exception e) {
				lex = null;
			}
		}
		if(lex == null ){
			lex = TSG.get(posTS).lexema;
		}
		return lex;
	}
	
	public String buscarTipoDevueltoTS(int posTS) {
		String tipoDev = null;
		if(TSL != null) {
			try {
				tipoDev =TSL.get(posTS).tipoDev;
			} catch (Exception e) {
				tipoDev = null;
			}
		}
		if(tipoDev == null ){
			tipoDev = TSG.get(posTS).tipoDev;
		}
		return tipoDev;
	}
	
	public int getPosTS(String lexema) {
		if(TSL != null) {
			for(int i: TSL.keySet()) {
				if(TSL.get(i).lexema.equals(lexema)) {
					return i;
				}
			}
		}
		for(int i: TSG.keySet()) {
			if(TSG.get(i).lexema.equals(lexema)) {
				return i;
			}
		}
		return -1;
	}

	@Override
	public String toString() {
		String res = "TABLA #"+1 +":\n";
		for(int i: TSG.keySet()) {
			Entrada e = TSG.get(i);
			res = res + "*'"+ e.lexema+ "'\n";
			res = res + "+tipo: '"+ e.tipo + "'\n";
			if(e.tipo.equals("function")) {
				String[] tipoParams = e.tipoParametros.split(" ");
				res = res + "+numParam: "+ e.numParametros + "\n";
				for (int j = 0; j < tipoParams.length; j++) {
					res = res + "+TipoParam"+(j+1)+": '"+ tipoParams[j] + "'\n";
					res = res + "+ModoParam"+(j+1)+": 1\n";
				}
				res = res + "EtiqFunction: 'Et"+e.lexema+"01'\n";
			}else {
				res = res + "+despl: "+ e.desplazamiento + "\n";
			}
		}
		for(String tabla : TSold.keySet()) {
			idT++;
			res = res +"\nTABLA DE "+tabla+" #"+idT +":\n";
			Map<Integer,Entrada> TSaux = TSold.get(tabla);
			for(int i: TSaux.keySet()) {
				Entrada e = TSaux.get(i);
				res = res + "*'"+ e.lexema+ "'\n";
				res = res + "+tipo: '"+ e.tipo + "'\n";
				if(e.tipo.equals("function")) {
					String[] tipoParams = e.tipoParametros.split(" ");
					res = res + "+numParam: "+ e.numParametros + "\n";
					for (int j = 0; j < tipoParams.length; j++) {
						res = res + "+TipoParam"+(j+1)+": '"+ tipoParams[j] + "'\n";
						res = res + "+ModoParam"+(j+1)+": 1\n";
					}
					res = res + "EtiqFunction: 'Et"+e.lexema+"01'\n";
				}else {
					res = res + "+despl: "+ e.desplazamiento + "\n";
				}
			}
		}
		return res;
		
	}
}

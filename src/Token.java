
public class Token {
	private String ntoken;
	private Object prop;

	Token(String nombre, Object propiedad) {
		this.ntoken = nombre;
		this.prop = propiedad;
	}

	@Override
	public String toString() {
		if (prop != null)
			return "<" + ntoken + "," + prop + ">";
		return "<" + ntoken + ", >";
	}
	
	public String getnToken() {
		return this.ntoken;
	}
	
	public Object getprop() {
		return this.prop;
	}
}

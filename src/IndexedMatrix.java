import java.util.Map;

//just a matrix paired with an
//index
public class IndexedMatrix {
	
	private Map.Entry<Integer, MutableSparseMatrix> dat;
	
	public IndexedMatrix(Map.Entry<Integer, MutableSparseMatrix> dat) {
		this.dat = dat;
	}
	
	public int getK() {
		return dat.getKey();
	}
	
	public MutableSparseMatrix getMatrix() {
		return dat.getValue();
	}
	
}

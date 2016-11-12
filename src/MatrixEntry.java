import java.util.Map;

//element of a matrix, includes i, j
//indices and value at that index
public class MatrixEntry {

	private final Map.Entry<IndexPair, RationalNumber> dat;

	public MatrixEntry(Map.Entry<IndexPair, RationalNumber> dat) {
		this.dat = dat;
	}
	
	public int getI() { return dat.getKey().i; }
	public int getJ() { return dat.getKey().j; }
	public RationalNumber getVal() { return dat.getValue(); }
	
	/*
	public MatrixEntry(int i, int j, double val) {
		if (i < 0) throw new IllegalArgumentException("i: " + i + "< 0");
		if (j < 0) throw new IllegalArgumentException("j: " + j + "< 0");
		this.i = i;
		this.j = j;
		this.val = val;
	}
	*/
	
}

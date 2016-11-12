
public class ThreeDTensorEntry {

	private final MatrixEntry matrixEntry;
	private final int k;
	
	public ThreeDTensorEntry(int k, MatrixEntry matrixEntry) {
		this.k = k;
		this.matrixEntry = matrixEntry;
	}
	
	public int getI() { return matrixEntry.getI(); }
	public int getJ() { return matrixEntry.getJ(); }
	public int getK() { return k; }
	public RationalNumber getVal() { return matrixEntry.getVal(); }
	
}

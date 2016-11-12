
public interface Matrix {

	public int getM();
	public int getN();
	
	//get object that will allow us to iterate over
	//the nonzero entries in the matrix
	public Iterable<MatrixEntry> getNonzeroElements();
	
}

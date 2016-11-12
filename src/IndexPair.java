//we define an immutable pair
//type to be hash map keys
public class IndexPair implements Comparable<IndexPair> {

	public final int i;
	public final int j;
	public IndexPair(int i, int j) {
		this.i = i;
		this.j = j;
	}
	@Override
	public boolean equals(Object other) {
		if(other == null) return false;
		if(other == this) return true;
		if(!(other instanceof IndexPair)) return false;
		IndexPair otherIndexPair = (IndexPair) other;
		return this.i == otherIndexPair.i &&
				this.j == otherIndexPair.j;
	}
	@Override
	public int hashCode() {
		//hash code computation involves
		//multiplying by some 32 bit primes
		return i * (int)(2725272749L) ^
				j * 329558221;
	}
	@Override
	public String toString() {
		return "(" + i + ",  " + j + ")";
	}
	@Override
	public int compareTo(IndexPair other) {
		if (this.i > other.i) return 1; 
		if (this.i < other.i) return -1; 
		if (this.j > other.j) return 1; 
		if (this.j < other.j) return -1; 
		return 0;
	}
}

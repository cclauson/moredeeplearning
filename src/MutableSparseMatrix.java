import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

public class MutableSparseMatrix implements Matrix {

	private final int m;
	private final int n;
	
	private Map<IndexPair, RationalNumber> map =
				new TreeMap<IndexPair, RationalNumber>();
	
	private void checkDimensionBounds(int val, String dimension) {
		if(val <= 0) {
			throw new IllegalArgumentException("Dimension " + dimension +
					" is " + val + " <= 0, this is not allowed");
		}
	}
	
	//creates a new sparse tensor with the given
	//dimensions with all zero entries
	public MutableSparseMatrix(int m, int n) {
		checkDimensionBounds(m, "m");
		checkDimensionBounds(n, "n");
		this.m = m;
		this.n = n;
	}

	@Override public int getM() { return m; }
	@Override public int getN() { return n; }

	@Override
	public Iterable<MatrixEntry> getNonzeroElements() {
		return new Iterable<MatrixEntry>() {
			@Override
			public Iterator<MatrixEntry> iterator() {
				final Iterator<Map.Entry<IndexPair, RationalNumber>> mapIterator = map.entrySet().iterator();
				return new Iterator<MatrixEntry>() {
					@Override
					public boolean hasNext() {
						return mapIterator.hasNext();
					}
					@Override
					public MatrixEntry next() {
						Map.Entry<IndexPair, RationalNumber> nextEntry = mapIterator.next();
						return new MatrixEntry(nextEntry);
					}
				};
			}
		};
	}
	
	private void checkIndexBounds(int index, int max, String dim) {
		if (index < 0) {
			throw new IndexOutOfBoundsException("index for dimension " +
					dim + " is < 0");
		}
		if (index >= max) {
			throw new IndexOutOfBoundsException("index for dimension " +
					dim + " is >= " + max + ", which is the max for this dimension");
		}
	}
	
	//returns index triple if indices are in bounds,
	//otherwise throws an exception
	private IndexPair checkIndexBoundsForPair(int i, int j) {
		checkIndexBounds(i, m, "m");
		checkIndexBounds(j, n, "n");
		return new IndexPair(i, j);
	}
	
	public void setEntryAt(int i, int j, RationalNumber val) {
		IndexPair it = checkIndexBoundsForPair(i, j);
		if (val.isZero()) {
			map.remove(it);
		} else {
			map.put(it, val);
		}
	}
	
	public boolean isZero() {
		return this.map.isEmpty();
	}
	
	public void scaleByFactorInPlace(RationalNumber factor) {
		final Set<IndexPair> keys = new HashSet<IndexPair>(this.map.keySet());
		for (IndexPair ip : keys) {
			map.put(ip, map.get(ip).mul(factor));
		}
	}
	
	@Override
	public String toString() {
		return map.toString();
	}
	
}

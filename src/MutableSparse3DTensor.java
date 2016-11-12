import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

public class MutableSparse3DTensor {
	
	private final int m;
	private final int n;
	private final int p;
	
	public int getM() { return m; }
	public int getN() { return n; }
	public int getP() { return p; }
	
	//we let each matrix represent a horizontal slice,
	//the integer index is the vertical index
	private Map<Integer, MutableSparseMatrix> dat;
	
	public MutableSparse3DTensor(int m, int n, int p) {
		this.m = m;
		this.n = n;
		this.p = p;
		this.dat = new TreeMap<Integer, MutableSparseMatrix>();
	}
	
	public MutableSparseMatrix getMatrixAtIndex(int k) {
		if (dat.containsKey(k)) return dat.get(k);
		return new MutableSparseMatrix(m, n);
	}
	
	public void setEntryAt(int i, int j, int k, RationalNumber val) {
		if(!dat.containsKey(k)) {
			if (val.isZero()) return;
			dat.put(k, new MutableSparseMatrix(m, n));
		}
		dat.get(k).setEntryAt(i, j, val);
	}

	private class EntryIterator implements Iterator<ThreeDTensorEntry> {
		
		private final Iterator<Map.Entry<Integer, MutableSparseMatrix>> itOuter;
		private Iterator<MatrixEntry> itInner;
		private int kCurr;
		
		public EntryIterator() {
			itOuter = dat.entrySet().iterator();
		}

		@Override
		public boolean hasNext() {
			while (itInner == null || !itInner.hasNext()) {
				if (!itOuter.hasNext()) return false;
				Map.Entry<Integer, MutableSparseMatrix> entryNext =
						itOuter.next();
				kCurr = entryNext.getKey();
				itInner = entryNext.getValue().getNonzeroElements().iterator();
			}
			return true;
		}
		@Override
		public ThreeDTensorEntry next() {
			if (!hasNext()) {
				throw new IllegalStateException("next called, but no next to return");
			}
			return new ThreeDTensorEntry(kCurr, itInner.next());
		}
	}
	
	public Iterable<ThreeDTensorEntry> getNonzeroEntries() {
		return new Iterable<ThreeDTensorEntry>() {
			@Override
			public Iterator<ThreeDTensorEntry> iterator() {
				return new EntryIterator();
			}
		};
	}
	
	public Iterable<IndexedMatrix> getNonzeroMatrices() {
		return new Iterable<IndexedMatrix>() {
			@Override
			public Iterator<IndexedMatrix> iterator() {
				final Iterator<Map.Entry<Integer, MutableSparseMatrix>> it =
						dat.entrySet().iterator();
				return new Iterator<IndexedMatrix>() {
					@Override
					public boolean hasNext() {
						return it.hasNext();
					}
					@Override
					public IndexedMatrix next() {
						return new IndexedMatrix(it.next());
					}
				};
			}
		};
	}
	
	//get a number by which the entire tensor can be
	//divided so that it can be synthesized to
	//stochastic hardware
	public RationalNumber getNormalizingDivisor() {
		RationalNumber ret = new RationalNumber(0, 1);
		for (IndexedMatrix im : this.getNonzeroMatrices()) {
			RationalNumber sum = new RationalNumber(0, 1);
			for (MatrixEntry me : im.getMatrix().getNonzeroElements()) {
				RationalNumber val = me.getVal();
				if (val.isNegative()) {
					throw new IllegalArgumentException(
						"Matrix has negative value");
				}
				sum = sum.add(val);
			}
			if (sum.greaterThan(ret)) {
				ret = sum;
			}
		}
		if (ret.isZero()) {
			throw new IllegalArgumentException("called on zero matrix");
		}
		return ret;
	}
	
	public void scaleInPlaceByFactor(RationalNumber factor) {
		for (IndexedMatrix im : this.getNonzeroMatrices()) {
			im.getMatrix().scaleByFactorInPlace(factor);
		}
	}
	
	public int getDivisorLcm() {
		int ret = 1;
		for (ThreeDTensorEntry entry : this.getNonzeroEntries()) {
			ret = RationalNumber.LCM(ret, entry.getVal().getDenominator());
		}
		return ret;
	}
	
	@Override
	public String toString() {
		return this.dat.toString();
	}
	
}

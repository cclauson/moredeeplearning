import java.io.PrintStream;
import java.util.HashMap;
import java.util.Map;

public class Main {

	private Main() {}
	
	public static void main(String[] args) {
		printCodeForFullyConnectedLayer(System.out, "fullyconnected1", 8, 3);
		printCodeForFullyConnectedLayer(System.out, "fullyconnected2", 3, 8);
		//TODO: Could add code here to connect the layers together and get
		//full network...
	}
	
	private static void printCodeForFullyConnectedLayer(PrintStream out, String name, int m, int n) {
		final MutableSparse3DTensor tensor =
				constructTensorForFullyConnected(m, n);
		printForwardCodeForLayer(out, name, tensor, true);
	}
		
	private static void printForwardCodeForLayer(PrintStream out, String name, MutableSparse3DTensor tensor, boolean bias) {
		String tensorModSym = name + "_tensor";
		RationalNumber tensorScaling = printModuleCodeForTensor(out, tensorModSym , tensor);
		
		out.println();
		out.println();
		out.println();
		
		int inputDim = tensor.getM();
		int paramDim = tensor.getN();
		int outputDim = tensor.getP();

		String invecStr = "indat";
		String outvecStr = "outdat";
		String weightvecStr = "weights";
		String biasvecStr = "bias";
		
		RationalNumber sigmoidK = new RationalNumber(14);
		RationalNumber biasScaling = sigmoidK.mul(new RationalNumber(1, 4));
		RationalNumber weightScaling = biasScaling.mul(tensorScaling);
		
		out.println("module " + name + "(");
		out.println("  input [" + (inputDim - 1) + ":0] " + invecStr + ",");
		out.println("  //weights will be scaled by a factor of " + weightScaling);
		out.println("  input [" + (paramDim - 1) + ":0] " + weightvecStr + ",");
		if (bias) {
			out.println("  //bias will be scaled by a factor of " + biasScaling);
			out.println("  input [" + (outputDim - 1) + ":0] " + biasvecStr + ",");
		}
		out.println("  output [" + (outputDim - 1) + ":0] " + outvecStr);
		out.println(");");
		
		String tensoroutSym = "tensorout";
		out.println("  wire [" + (outputDim - 1) + ":0] " + tensoroutSym + ";");
		out.println("  " + tensorModSym + "(" + invecStr + ", " + weightvecStr +
				", tensorout);");
		
		String flatteningInSym;
		if (bias) {
			String biasoutSym = "biasout";
			out.println("  wire [" + (outputDim - 1) + ":0] " + biasoutSym + ";");
			for (int i = 0; i < outputDim; ++i) {
				out.println("  stochasticmux2(" + biasoutSym + "[" + i + "], " +
						tensoroutSym + "[" + i + "]" + ", " +
						biasvecStr + "[" + i + "]" + ");");
			}
			flatteningInSym = biasoutSym;
		} else
			flatteningInSym = tensoroutSym;
		
		for (int i = 0; i < outputDim; ++i) {
			out.println("  //let's assume that this is stanh with k=" + sigmoidK);
			out.println("  sigmoid(" + outvecStr + "[" + i + "], " +
					flatteningInSym + "[" + i + "]" + ");");
		}
		
		out.println("endmodule");
		
	}

	private static MutableSparse3DTensor constructTensorForFullyConnected(int m, int n) {
		final MutableSparse3DTensor tensor = new MutableSparse3DTensor(m, m * n, n);
		for (int i = 0; i < m; ++i) {
			for (int j = 0; j < n; ++j) {
				tensor.setEntryAt(i, i + m * j, j, new RationalNumber(1));
			}
		}
		return tensor;
	}
	
	//object to remember which product signals
	//we have already generated
	private static class ProductSymTable {
		
		private final Map<IndexPair, String> dat;
		
		public ProductSymTable() {
			this.dat = new HashMap<IndexPair, String>();
		}
		
		public String getSymForProductCreateIfNecessary(
				String invecStr, String paramvecStr, int i, int j, PrintStream out) {
			IndexPair ip = new IndexPair(i, j);
			if(!dat.containsKey(ip)) {
				String sym = "prod_" + i + "_" + j;
				out.println("  wire " + sym + ";");
				out.println("  assign " + sym +" = " + invecStr + "[" + i +
						"] ^ " + paramvecStr + "[" + j + "];");
				dat.put(ip, sym);
			}
			return dat.get(ip);
		}
		
	}
	
	//return inherent scaling factor of generated stochastic code
	private static RationalNumber printModuleCodeForTensor(PrintStream out, String name, MutableSparse3DTensor tensor) {
		
		String invecStr = "indat";
		String paramvecStr = "params";
		String outvecStr = "outdat";
		
		//get a divisor such that when the tensor is divided by it there is
		//no "horizontal slice" matrix for which the sum of the entries is
		//more than one.  To do this, for each such matrix we 
		RationalNumber normalizingDivisor = tensor.getNormalizingDivisor();
		//divide by the divisor
		tensor.scaleInPlaceByFactor(normalizingDivisor.inverse());
		
		int divisorLcm = tensor.getDivisorLcm();
		//scale by divisorLcm, this should result in an integer tensor
		tensor.scaleInPlaceByFactor(new RationalNumber(divisorLcm));
		
		out.println("//stochastic implementation inherently scales by factor of " + normalizingDivisor.inverse());
		out.println("module " + name + "(");
		out.println("  input [" + (tensor.getM() - 1) + ":0] " + invecStr + ",");
		out.println("  input [" + (tensor.getN() - 1) + ":0] " + paramvecStr + ",");
		out.println("  output [" + (tensor.getP() - 1) + ":0] " + outvecStr);
		out.println(");");
		
		ProductSymTable pst = new ProductSymTable();
		
		for (int k = 0; k < tensor.getP(); ++k) {
			MutableSparseMatrix matrix = tensor.getMatrixAtIndex(k);
			//go through all nonzero matrix entries once, to induce
			//code gen for all products
			for (MatrixEntry me : matrix.getNonzeroElements()) {
				if (me.getVal().isZero()) continue;
				if (!me.getVal().isWholeNumber()) throw new RuntimeException(
						"should be whole number, since we scaled by denominator lcm");
				//get the symbol representing the product
				pst.getSymForProductCreateIfNecessary(
						invecStr, paramvecStr, me.getI(), me.getJ(), out);
			}
			//we need to generate a number of terms equal to divisorLcm
			int numTermsGenerated = 0;
			out.println("  stochasticmux" + divisorLcm + "(");
			out.print  ("    " + outvecStr + "[" + k + "]");
			for (MatrixEntry me : matrix.getNonzeroElements()) {
				out.println(",");
				if (me.getVal().isZero()) continue;
				if (!me.getVal().isWholeNumber()) throw new RuntimeException(
						"should be whole number, since we scaled by denominator lcm");
				//get the symbol representing the product
				String prodSym = pst.getSymForProductCreateIfNecessary(
						invecStr, paramvecStr, me.getI(), me.getJ(), out);
				for (int i = 0; i < me.getVal().getAsInt(); ++i) {
					out.print("    " + prodSym);
				}
				++numTermsGenerated;
			}
			//check that we haven't generated more terms than we're supposed to
			if (numTermsGenerated > divisorLcm) {
				throw new RuntimeException("Too many routes created to mux, this should never happen");
			}
			//sometimes fewer will be created, in this case fill the remaining terms
			//with zeros
			for (; numTermsGenerated < divisorLcm; ++numTermsGenerated) {
				out.println(",");
				out.print("    1'b0");
			}
			out.println();
			out.println("  );");
		}
		
		out.println("endmodule");
		
		return normalizingDivisor.inverse();
		
	}
	
}

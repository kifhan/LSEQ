package alma.fr.strategiescomponents.boundary;

import java.math.BigInteger;

import com.google.inject.Inject;

public class ConstantBoundary implements IBoundary {

	private BigInteger value;

	@Inject
	public ConstantBoundary(@BoundaryValue BigInteger value) {
		this.value = value;
	}

	public BigInteger getBoundary(Integer depth) {
		return value;
	}

}

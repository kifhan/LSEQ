package alma.fr.strategiescomponents;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.Random;

import alma.fr.data.Positions;
import alma.fr.logootenginecomponents.LogootEngine;
import alma.fr.logootenginecomponents.Replica;
import alma.fr.strategiescomponents.boundary.IBoundary;

import com.google.inject.Inject;

/** boundary- **/
public class EndingBoundaryIdProvider implements IIdProviderStrategy {

	private Random rand = new Random();

	@Inject
	private IBoundary boundary;

	@Inject
	public EndingBoundaryIdProvider(IBoundary boundary) {
		this.boundary = boundary;
	}

	public Iterator<Positions> generateIdentifiers(Positions p, Positions q,
			Integer N, Replica rep, BigInteger interval, int index) {
		ArrayList<Positions> positions = new ArrayList<Positions>();

		// #0 process the interval for random
		BigInteger step = interval.divide(BigInteger.valueOf(N));

		step = (step.min(boundary.getBoundary(index))).max(BigInteger
				.valueOf(1));

		// #1a Truncate tail or add bits
		int nextBitCount = q.getD().bitLength() - 1;
		int bitBaseSum = LogootEngine.base.getSumBit(index);

		// #1b special case into account
		boolean comp = (nextBitCount < bitBaseSum)
				&& (nextBitCount <= p.getD().bitLength())
				&& (p.getD().shiftRight(
						p.getD().bitLength() - q.getD().bitLength()).equals(q
						.getD()));

		BigInteger r;
		if (!comp) { // add 0 or truncate
			r = q.getD().shiftRight(nextBitCount - bitBaseSum);
		} else { // add 1
			r = q.getD()
					.shiftRight(nextBitCount - bitBaseSum)
					.add(BigInteger.valueOf(2)
							.pow(LogootEngine.base.getBitBase(index))
							.subtract(BigInteger.ONE));
			;
		}

		// #2 create position by adding a random value; N times
		for (int j = 0; j < N; ++j) {
			BigInteger randomInt;

			// Random
			if (!(step.compareTo(BigInteger.valueOf(1)) == 1)) { // step <= 1
				randomInt = BigInteger.valueOf(1);
			} else {
				do {
					randomInt = new BigInteger(step.subtract(
							BigInteger.valueOf(1)).bitLength(), rand);
				} while (randomInt.compareTo(step.subtract(BigInteger
						.valueOf(1))) >= 0);
				randomInt = randomInt.add(BigInteger.valueOf(1));
			}
			// // Construct
			BigInteger newR = LogootEngine.base.sub(r, randomInt);
			rep.setClock(rep.getClock() + 1);
			ArrayList<Integer> s = getS(newR, index, p, q, rep);
			ArrayList<Integer> c = getC(newR, index, p, q, rep);
			Positions tempPositions = new Positions(newR,
					LogootEngine.base.getSumBit(index), s, c);
			positions.add(tempPositions);
			r = LogootEngine.base.sub(r, step);
		}
		Collections.reverse(positions);
		return positions.iterator();
	}

	public ArrayList<Integer> getS(BigInteger r, Integer index, Positions p,
			Positions q, Replica rep) {
		ArrayList<Integer> sources = new ArrayList<Integer>();
		BigInteger tempR = r.setBit(LogootEngine.base.getSumBit(index));
		int bitLength = tempR.bitLength() - 1;
		for (int i = 0; i < index; ++i) {
			int bitAtDepth = LogootEngine.base.getSumBit(i + 1);
			int lowerBitLength = bitLength - bitAtDepth;
			BigInteger lowerMask = BigInteger.valueOf(2).pow(lowerBitLength)
					.subtract(BigInteger.ONE);
			BigInteger mask = BigInteger.valueOf(2)
					.pow(LogootEngine.base.getBitBase(i + 1) + lowerBitLength)
					.subtract(BigInteger.ONE).subtract(lowerMask);
			if (p.getS().size() > i && r.and(mask).equals(p.getD().and(mask))) { // copy
																					// p
																					// site
				sources.add(p.getS().get(i));
			} else if (q.getS().size() > i
					&& r.and(mask).equals(q.getD().and(mask))) { // copy q
				// site){
				sources.add(q.getS().get(i));
			} else { // copy our own source
				sources.add(rep.getId());
			}
		}
		return sources;
	}

	public ArrayList<Integer> getC(BigInteger r, Integer index, Positions p,
			Positions q, Replica rep) {
		ArrayList<Integer> clocks = new ArrayList<Integer>();
		BigInteger tempR = r.setBit(LogootEngine.base.getSumBit(index));
		int bitLength = tempR.bitLength() - 1;
		for (int i = 0; i < index; ++i) {
			int bitAtDepth = LogootEngine.base.getSumBit(i + 1);
			int lowerBitLength = bitLength - bitAtDepth;
			BigInteger lowerMask = BigInteger.valueOf(2).pow(lowerBitLength)
					.subtract(BigInteger.ONE);
			BigInteger mask = BigInteger.valueOf(2)
					.pow(LogootEngine.base.getBitBase(i + 1) + lowerBitLength)
					.subtract(BigInteger.ONE).subtract(lowerMask);
			if (p.getC().size() > i && r.and(mask).equals(p.getD().and(mask))) { // copy
																					// p
																					// site
				clocks.add(p.getC().get(i));
			} else if (q.getC().size() > i
					&& r.and(mask).equals(q.getD().and(mask))) { // copy q
				// site){
				clocks.add(q.getC().get(i));
			} else { // copy our own source
				clocks.add(rep.getClock());
			}
		}
		return clocks;
	}
}

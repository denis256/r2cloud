package ru.r2cloud.satellite.decoder;

import java.io.IOException;

import ru.r2cloud.jradio.ByteInput;
import ru.r2cloud.jradio.Context;
import ru.r2cloud.jradio.FloatInput;
import ru.r2cloud.jradio.blocks.ClockRecoveryMM;
import ru.r2cloud.jradio.blocks.FLLBandEdge;
import ru.r2cloud.jradio.blocks.FloatToChar;
import ru.r2cloud.jradio.blocks.LowPassFilter;
import ru.r2cloud.jradio.blocks.LowPassFilterComplex;
import ru.r2cloud.jradio.blocks.QuadratureDemodulation;
import ru.r2cloud.jradio.blocks.Rail;
import ru.r2cloud.jradio.blocks.RmsAgcComplex;
import ru.r2cloud.jradio.blocks.Window;

public class GmskDemodulator implements ByteInput {

	private final ByteInput source;
	private final Context context;

	public GmskDemodulator(FloatInput source, int baudRate, float bandwidth, float gainMu) {
		this(source, baudRate, bandwidth, gainMu, 0.06f, 1, 2000);
	}

	public GmskDemodulator(FloatInput source, int baudRate, float bandwidth, float gainMu, Float fllBandwidth, int decimation, double transitionWidth) {
		FloatInput next = new RmsAgcComplex(source, 1e-2f, 0.5f);
		if (fllBandwidth != null) {
			next = new FLLBandEdge(next, next.getContext().getSampleRate() / baudRate, 0.35f, 100, fllBandwidth);
		}
		next = new LowPassFilterComplex(next, 1.0, bandwidth / 2, 600, Window.WIN_HAMMING, 6.76);
		next = new QuadratureDemodulation(next, 1.0f);
		next = new LowPassFilter(next, decimation, 1.0, (double) baudRate / 2, transitionWidth, Window.WIN_HAMMING, 6.76);
		next = new ClockRecoveryMM(next, next.getContext().getSampleRate() / baudRate, (float) (0.25 * gainMu * gainMu), 0.5f, gainMu, 0.005f);
		next = new Rail(next, -1.0f, 1.0f);
		this.source = new FloatToChar(next, 127.0f);
		this.context = new Context(this.source.getContext());
		this.context.setSoftBits(true);
	}

	@Override
	public byte readByte() throws IOException {
		return source.readByte();
	}

	@Override
	public Context getContext() {
		return context;
	}

	@Override
	public void close() throws IOException {
		source.close();
	}

}

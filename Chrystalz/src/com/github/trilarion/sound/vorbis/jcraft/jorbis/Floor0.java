/*
 * Copyright (C) 2000 ymnk<ymnk@jcraft.com>
 *               2015 Trilarion
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.github.trilarion.sound.vorbis.jcraft.jorbis;

import com.github.trilarion.sound.util.Util;
import com.github.trilarion.sound.vorbis.jcraft.jogg.Buffer;

class Floor0 extends FuncFloor {
    @Override
    void pack(final Object i, final Buffer opb) {
        final InfoFloor0 info = (InfoFloor0) i;
        opb.write(info.order, 8);
        opb.write(info.rate, 16);
        opb.write(info.barkmap, 16);
        opb.write(info.ampbits, 6);
        opb.write(info.ampdB, 8);
        opb.write(info.numbooks - 1, 4);
        for (int j = 0; j < info.numbooks; j++) {
            opb.write(info.books[j], 8);
        }
    }

    @Override
    Object unpack(final Info vi, final Buffer opb) {
        final InfoFloor0 info = new InfoFloor0();
        info.order = opb.read(8);
        info.rate = opb.read(16);
        info.barkmap = opb.read(16);
        info.ampbits = opb.read(6);
        info.ampdB = opb.read(8);
        info.numbooks = opb.read(4) + 1;
        if (info.order < 1 || info.rate < 1 || info.barkmap < 1
                || info.numbooks < 1) {
            return null;
        }
        for (int j = 0; j < info.numbooks; j++) {
            info.books[j] = opb.read(8);
            if (info.books[j] < 0 || info.books[j] >= vi.books) {
                return null;
            }
        }
        return info;
    }

    @Override
    Object look(final DspState vd, final InfoMode mi, final Object i) {
        float scale;
        final Info vi = vd.vi;
        final InfoFloor0 info = (InfoFloor0) i;
        final LookFloor0 look = new LookFloor0();
        look.m = info.order;
        look.n = vi.blocksizes[mi.blockflag] / 2;
        look.ln = info.barkmap;
        look.vi = info;
        look.lpclook.init(look.ln, look.m);
        // we choose a scaling constant so that:
        scale = look.ln / Floor0.toBARK((float) (info.rate / 2.));
        // the mapping from a linear scale to a smaller bark scale is
        // straightforward. We do *not* make sure that the linear mapping
        // does not skip bark-scale bins; the decoder simply skips them and
        // the encoder may do what it wishes in filling them. They're
        // necessary in some mapping combinations to keep the scale spacing
        // accurate
        look.linearmap = new int[look.n];
        for (int j = 0; j < look.n; j++) {
            int val = (int) Math
                    .floor(Floor0.toBARK((float) (info.rate / 2. / look.n * j))
                            * scale); // bark numbers represent band edges
            if (val >= look.ln) {
                val = look.ln; // guard against the approximation
            }
            look.linearmap[j] = val;
        }
        return look;
    }

    static float toBARK(final float f) {
        return (float) (13.1 * Math.atan(.00074 * f)
                + 2.24 * Math.atan(f * f * 1.85e-8) + 1e-4 * f);
    }

    Object state(final Object i) {
        final EchstateFloor0 state = new EchstateFloor0();
        final InfoFloor0 info = (InfoFloor0) i;
        // a safe size if usually too big (dim==1)
        state.codewords = new int[info.order];
        state.curve = new float[info.barkmap];
        state.frameno = -1;
        return state;
    }

    @Override
    void free_info(final Object i) {
    }

    @Override
    void free_look(final Object i) {
    }

    @Override
    void free_state(final Object vs) {
    }

    @Override
    int forward(final Block vb, final Object i, final float[] in,
            final float[] out, final Object vs) {
        return 0;
    }

    float[] lsp = null;

    int inverse(final Block vb, final Object i, final float[] out) {
        // System.err.println("Floor0.inverse "+i.getClass()+"]");
        final LookFloor0 look = (LookFloor0) i;
        final InfoFloor0 info = look.vi;
        final int ampraw = vb.opb.read(info.ampbits);
        if (ampraw > 0) { // also handles the -1 out of data case
            final int maxval = (1 << info.ampbits) - 1;
            final float amp = (float) ampraw / maxval * info.ampdB;
            final int booknum = vb.opb.read(Util.ilog(info.numbooks));
            if (booknum != -1 && booknum < info.numbooks) {
                synchronized (this) {
                    if (this.lsp == null || this.lsp.length < look.m) {
                        this.lsp = new float[look.m];
                    } else {
                        for (int j = 0; j < look.m; j++) {
                            this.lsp[j] = 0.f;
                        }
                    }
                    final CodeBook b = vb.vd.fullbooks[info.books[booknum]];
                    float last = 0.f;
                    for (int j = 0; j < look.m; j++) {
                        out[j] = 0.0f;
                    }
                    for (int j = 0; j < look.m; j += b.dim) {
                        if (b.decodevs(this.lsp, j, vb.opb, 1, -1) == -1) {
                            for (int k = 0; k < look.n; k++) {
                                out[k] = 0.0f;
                            }
                            return 0;
                        }
                    }
                    for (int j = 0; j < look.m;) {
                        for (int k = 0; k < b.dim; k++, j++) {
                            this.lsp[j] += last;
                        }
                        last = this.lsp[j - 1];
                    }
                    // take the coefficients back to a spectral envelope curve
                    Lsp.lsp_to_curve(out, look.linearmap, look.n, look.ln,
                            this.lsp, look.m, amp, info.ampdB);
                    return 1;
                }
            }
        }
        return 0;
    }

    @Override
    Object inverse1(final Block vb, final Object i, final Object memo) {
        final LookFloor0 look = (LookFloor0) i;
        final InfoFloor0 info = look.vi;
        float[] lsp1 = null;
        if (memo instanceof float[]) {
            lsp1 = (float[]) memo;
        }
        final int ampraw = vb.opb.read(info.ampbits);
        if (ampraw > 0) { // also handles the -1 out of data case
            final int maxval = (1 << info.ampbits) - 1;
            final float amp = (float) ampraw / maxval * info.ampdB;
            final int booknum = vb.opb.read(Util.ilog(info.numbooks));
            if (booknum != -1 && booknum < info.numbooks) {
                final CodeBook b = vb.vd.fullbooks[info.books[booknum]];
                float last = 0.f;
                if (lsp1 == null || lsp1.length < look.m + 1) {
                    lsp1 = new float[look.m + 1];
                } else {
                    for (int j = 0; j < lsp1.length; j++) {
                        lsp1[j] = 0.f;
                    }
                }
                for (int j = 0; j < look.m; j += b.dim) {
                    if (b.decodev_set(lsp1, j, vb.opb, b.dim) == -1) {
                        return null;
                    }
                }
                for (int j = 0; j < look.m;) {
                    for (int k = 0; k < b.dim; k++, j++) {
                        lsp1[j] += last;
                    }
                    last = lsp1[j - 1];
                }
                lsp1[look.m] = amp;
                return lsp1;
            }
        }
        return null;
    }

    @Override
    int inverse2(final Block vb, final Object i, final Object memo,
            final float[] out) {
        final LookFloor0 look = (LookFloor0) i;
        final InfoFloor0 info = look.vi;
        if (memo != null) {
            final float[] lsp1 = (float[]) memo;
            final float amp = lsp1[look.m];
            Lsp.lsp_to_curve(out, look.linearmap, look.n, look.ln, lsp1, look.m,
                    amp, info.ampdB);
            return 1;
        }
        for (int j = 0; j < look.n; j++) {
            out[j] = 0.f;
        }
        return 0;
    }

    static float fromdB(final float x) {
        return (float) Math.exp(x * .11512925);
    }

    static void lsp_to_lpc(final float[] lsp, final float[] lpc, final int m) {
        int i, j;
        final int m2 = m / 2;
        final float[] O = new float[m2];
        final float[] E = new float[m2];
        float A;
        final float[] Ae = new float[m2 + 1];
        final float[] Ao = new float[m2 + 1];
        float B;
        final float[] Be = new float[m2];
        final float[] Bo = new float[m2];
        float temp;
        // even/odd roots setup
        for (i = 0; i < m2; i++) {
            O[i] = (float) (-2. * Math.cos(lsp[i * 2]));
            E[i] = (float) (-2. * Math.cos(lsp[i * 2 + 1]));
        }
        // set up impulse response
        for (j = 0; j < m2; j++) {
            Ae[j] = 0.f;
            Ao[j] = 1.f;
            Be[j] = 0.f;
            Bo[j] = 1.f;
        }
        Ao[j] = 1.f;
        Ae[j] = 1.f;
        // run impulse response
        for (i = 1; i < m + 1; i++) {
            A = B = 0.f;
            for (j = 0; j < m2; j++) {
                temp = O[j] * Ao[j] + Ae[j];
                Ae[j] = Ao[j];
                Ao[j] = A;
                A += temp;
                temp = E[j] * Bo[j] + Be[j];
                Be[j] = Bo[j];
                Bo[j] = B;
                B += temp;
            }
            lpc[i - 1] = (A + Ao[j] + B - Ae[j]) / 2;
            Ao[j] = A;
            Ae[j] = B;
        }
    }

    static void lpc_to_curve(final float[] curve, final float[] lpc,
            final float amp, final LookFloor0 l) {
        // l->m+1 must be less than l->ln, but guard in case we get a bad stream
        final float[] lcurve = new float[Math.max(l.ln * 2, l.m * 2 + 2)];
        if (amp == 0) {
            for (int j = 0; j < l.n; j++) {
                curve[j] = 0.0f;
            }
            return;
        }
        l.lpclook.lpc_to_curve(lcurve, lpc, amp);
        for (int i = 0; i < l.n; i++) {
            curve[i] = lcurve[l.linearmap[i]];
        }
    }

    static class InfoFloor0 {
        int order;
        int rate;
        int barkmap;
        int ampbits;
        int ampdB;
        int numbooks; // <= 16
        int[] books = new int[16];
    }

    static class LookFloor0 {
        int n;
        int ln;
        int m;
        int[] linearmap;
        InfoFloor0 vi;
        Lpc lpclook = new Lpc();
    }

    static class EchstateFloor0 {
        int[] codewords;
        float[] curve;
        long frameno;
        long codes;
    }
}

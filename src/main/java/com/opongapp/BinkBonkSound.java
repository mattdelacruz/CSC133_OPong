package com.opongapp;

import javax.sound.midi.Instrument;
import javax.sound.midi.MidiChannel;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Synthesizer;

class BinkBonkSound {

    private static final int MAX_PITCH_BEND = 16383;
    private static final int MIN_PITCH_BEND = 0;
    private static final int REVERB_LEVEL_CONTROLLER = 91;
    private static final int MIN_REVERB_LEVEL = 0;
    private static final int DRUM_MIDI_CHANNEL = 9;
    private static final int CLAVES_NOTE = 76;
    private static final int NORMAL_VELOCITY = 100;

    Instrument[] instrument;
    MidiChannel[] midiChannels;
    boolean playSound;

    public BinkBonkSound() {
        playSound = true;
        try {
            Synthesizer gmSynthesizer = MidiSystem.getSynthesizer();
            gmSynthesizer.open();
            instrument = gmSynthesizer.getDefaultSoundbank().getInstruments();
            midiChannels = gmSynthesizer.getChannels();

        } catch (MidiUnavailableException e) {
            e.printStackTrace();
        }
    }

    void play(boolean hiPitch) {
        if (playSound) {
            midiChannels[DRUM_MIDI_CHANNEL]
                    .setPitchBend(hiPitch ? MAX_PITCH_BEND : MIN_PITCH_BEND);

            midiChannels[DRUM_MIDI_CHANNEL]
                    .controlChange(REVERB_LEVEL_CONTROLLER, MIN_REVERB_LEVEL);

            midiChannels[DRUM_MIDI_CHANNEL]
                    .noteOn(CLAVES_NOTE, NORMAL_VELOCITY);
        }
    }

    public void toggleSound() {
        playSound = !playSound;
    }
}
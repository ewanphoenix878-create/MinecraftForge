package com.jetsonmod.client;

import com.jetsonmod.JetsonEntity;
import com.jetsonmod.JetsonMod;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.sounds.SoundSource;

/** Looping helicopter "whop" that follows the toilet and swells as the rotor spins up. */
public class RotorSoundInstance extends AbstractTickableSoundInstance {
    private final JetsonEntity jetson;

    public RotorSoundInstance(JetsonEntity jetson) {
        super(JetsonMod.ROTOR_SOUND.get(), SoundSource.NEUTRAL, SoundInstance.createUnseededRandom());
        this.jetson = jetson;
        this.looping = true;
        this.delay = 0;
        this.volume = 0.0F;
        this.x = jetson.getX();
        this.y = jetson.getY();
        this.z = jetson.getZ();
    }

    @Override
    public boolean canPlaySound() {
        return !jetson.isSilent();
    }

    @Override
    public boolean canStartSilent() {
        return true;
    }

    @Override
    public void tick() {
        if (jetson.isRemoved()) {
            stop();
            return;
        }
        x = jetson.getX();
        y = jetson.getY();
        z = jetson.getZ();
        float speed = jetson.getPropSpeed();
        volume = speed < 0.02F ? 0.0F : speed;
        pitch = 0.5F + 0.6F * speed;
    }
}

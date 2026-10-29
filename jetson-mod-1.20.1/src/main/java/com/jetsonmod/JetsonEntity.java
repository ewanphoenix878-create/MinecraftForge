package com.jetsonmod;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.util.Mth;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.HasCustomInventoryScreen;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.ForgeHooks;
import org.jetbrains.annotations.Nullable;

/**
 * A rideable personal aircraft. Movement is simulated by whoever controls it (the rider's
 * client while ridden, the server otherwise), the same way vanilla boats work. Fuel is burned
 * on the server and synced to clients so they know whether the engines have power.
 */
public class JetsonEntity extends Entity implements HasCustomInventoryScreen {
    private static final EntityDataAccessor<Integer> DATA_BURN_TIME = SynchedEntityData.defineId(JetsonEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_MAX_BURN_TIME = SynchedEntityData.defineId(JetsonEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Float> DATA_DAMAGE = SynchedEntityData.defineId(JetsonEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<ItemStack> DATA_FUEL_ITEM = SynchedEntityData.defineId(JetsonEntity.class, EntityDataSerializers.ITEM_STACK);

    // Flight tuning (blocks per tick). Top speed is roughly accel * drag / (1 - drag) ~= 0.6 b/t ~= 12 m/s.
    private static final double HORIZONTAL_ACCEL = 0.06;
    private static final double AIR_DRAG = 0.91;
    private static final double CLIMB_SPEED = 0.32;
    private static final double VERTICAL_RESPONSE = 0.18;
    private static final double GRAVITY = 0.04;
    private static final double MAX_FALL_SPEED = 0.8;

    private final SimpleContainer fuelContainer = new SimpleContainer(1);
    private final ContainerData dataAccess = new ContainerData() {
        @Override
        public int get(int index) {
            return index == 0 ? getBurnTime() : getMaxBurnTime();
        }

        @Override
        public void set(int index, int value) {
            if (index == 0) setBurnTime(value);
            else setMaxBurnTime(value);
        }

        @Override
        public int getCount() {
            return 2;
        }
    };

    // Input from the controlling client
    private boolean inputForward, inputBack, inputLeft, inputRight, inputUp, inputDown;

    // Client-side interpolation for entities we do not control
    private int lerpSteps;
    private double lerpX, lerpY, lerpZ, lerpYRot;

    // Client-side propeller animation
    private float propSpeed;
    private float propAngle;
    private float prevPropAngle;

    public JetsonEntity(EntityType<? extends JetsonEntity> type, Level level) {
        super(type, level);
        this.blocksBuilding = true;
        // Gravity is applied manually in tick(). Being "no gravity" also stops dedicated servers from
        // kicking the rider for flying while hovering.
        this.setNoGravity(true);
        // Mirror the tank contents to clients for the HUD.
        this.fuelContainer.addListener(container -> {
            if (!level().isClientSide) {
                entityData.set(DATA_FUEL_ITEM, container.getItem(0).copy());
            }
        });
    }

    @Override
    protected void defineSynchedData() {
        entityData.define(DATA_BURN_TIME, 0);
        entityData.define(DATA_MAX_BURN_TIME, 0);
        entityData.define(DATA_DAMAGE, 0.0F);
        entityData.define(DATA_FUEL_ITEM, ItemStack.EMPTY);
    }

    public static boolean isFuel(ItemStack stack) {
        return ForgeHooks.getBurnTime(stack, RecipeType.SMELTING) > 0;
    }

    public int getBurnTime() {
        return entityData.get(DATA_BURN_TIME);
    }

    private void setBurnTime(int value) {
        entityData.set(DATA_BURN_TIME, value);
    }

    public int getMaxBurnTime() {
        return entityData.get(DATA_MAX_BURN_TIME);
    }

    private void setMaxBurnTime(int value) {
        entityData.set(DATA_MAX_BURN_TIME, value);
    }

    /** Fuel items waiting in the tank (not counting the one currently burning). */
    public ItemStack getFuelStack() {
        return entityData.get(DATA_FUEL_ITEM);
    }

    public boolean hasPower() {
        return getBurnTime() > 0;
    }

    public void setInput(boolean forward, boolean back, boolean left, boolean right, boolean up, boolean down) {
        this.inputForward = forward;
        this.inputBack = back;
        this.inputLeft = left;
        this.inputRight = right;
        this.inputUp = up;
        this.inputDown = down;
    }

    // ---------------------------------------------------------------- ticking

    @Override
    public void tick() {
        super.tick();

        if (!level().isClientSide) {
            tickFuel();
            float damage = entityData.get(DATA_DAMAGE);
            if (damage > 0.0F) {
                entityData.set(DATA_DAMAGE, Math.max(0.0F, damage - 0.5F));
            }
        }

        if (isControlledByLocalInstance()) {
            lerpSteps = 0;
            syncPacketPositionCodec(getX(), getY(), getZ());
            tickFlight();
        } else {
            tickLerp();
            setDeltaMovement(Vec3.ZERO);
        }

        if (level().isClientSide) {
            float target = isVehicle() && hasPower() ? 1.0F : 0.0F;
            propSpeed += (target - propSpeed) * 0.08F;
            prevPropAngle = propAngle;
            propAngle += propSpeed * 55.0F;
            if (propAngle > 360.0F) {
                propAngle -= 360.0F;
                prevPropAngle -= 360.0F;
            }
        }
    }

    private void tickFuel() {
        if (!isVehicle()) {
            return;
        }
        int burn = getBurnTime();
        if (burn > 0) {
            setBurnTime(burn - 1);
        }
        if (getBurnTime() <= 0) {
            ItemStack stack = fuelContainer.getItem(0);
            int value = stack.isEmpty() ? 0 : ForgeHooks.getBurnTime(stack, RecipeType.SMELTING);
            if (value > 0) {
                ItemStack remainder = stack.getCraftingRemainingItem();
                stack.shrink(1);
                if (stack.isEmpty() && !remainder.isEmpty()) {
                    fuelContainer.setItem(0, remainder);
                } else if (!remainder.isEmpty()) {
                    spawnAtLocation(remainder);
                }
                fuelContainer.setChanged();
                setBurnTime(value);
                setMaxBurnTime(value);
            } else {
                setMaxBurnTime(0);
            }
        }
    }

    private void tickFlight() {
        Vec3 motion = getDeltaMovement();
        double mx = motion.x;
        double my = motion.y;
        double mz = motion.z;

        LivingEntity pilot = getControllingPassenger();
        if (pilot != null) {
            setYRot(pilot.getYRot());
            yRotO = getYRot();
        }

        if (pilot != null && hasPower()) {
            float strafe = (inputLeft ? 1.0F : 0.0F) - (inputRight ? 1.0F : 0.0F);
            float forward = (inputForward ? 1.0F : 0.0F) - (inputBack ? 1.0F : 0.0F);
            if (strafe != 0.0F || forward != 0.0F) {
                double len = Math.sqrt(strafe * strafe + forward * forward);
                double s = strafe / len * HORIZONTAL_ACCEL;
                double f = forward / len * HORIZONTAL_ACCEL;
                float sin = Mth.sin(getYRot() * Mth.DEG_TO_RAD);
                float cos = Mth.cos(getYRot() * Mth.DEG_TO_RAD);
                mx += s * cos - f * sin;
                mz += f * cos + s * sin;
            }
            mx *= AIR_DRAG;
            mz *= AIR_DRAG;

            double targetY = (inputUp ? CLIMB_SPEED : 0.0) - (inputDown ? CLIMB_SPEED : 0.0);
            my += (targetY - my) * VERTICAL_RESPONSE;
        } else {
            double drag = onGround() ? 0.5 : 0.96;
            mx *= drag;
            mz *= drag;
            my = Math.max(my - GRAVITY, -MAX_FALL_SPEED);
        }

        setDeltaMovement(mx, my, mz);
        move(MoverType.SELF, getDeltaMovement());

        if (onGround() && getDeltaMovement().y < 0.0) {
            setDeltaMovement(getDeltaMovement().multiply(1.0, 0.0, 1.0));
        }
    }

    private void tickLerp() {
        if (lerpSteps > 0) {
            double x = getX() + (lerpX - getX()) / lerpSteps;
            double y = getY() + (lerpY - getY()) / lerpSteps;
            double z = getZ() + (lerpZ - getZ()) / lerpSteps;
            float yRot = (float) (getYRot() + Mth.wrapDegrees(lerpYRot - getYRot()) / lerpSteps);
            lerpSteps--;
            setPos(x, y, z);
            setYRot(yRot);
        }
    }

    @Override
    public void lerpTo(double x, double y, double z, float yRot, float xRot, int steps, boolean teleport) {
        this.lerpX = x;
        this.lerpY = y;
        this.lerpZ = z;
        this.lerpYRot = yRot;
        this.lerpSteps = 10;
    }

    public float getPropAngle(float partialTick) {
        return Mth.lerp(partialTick, prevPropAngle, propAngle);
    }

    // ---------------------------------------------------------------- riding

    @Override
    public double getPassengersRidingOffset() {
        return 0.5;
    }

    @Nullable
    @Override
    public LivingEntity getControllingPassenger() {
        return getFirstPassenger() instanceof Player player ? player : null;
    }

    @Override
    public InteractionResult interact(Player player, InteractionHand hand) {
        if (player.isSecondaryUseActive()) {
            if (!level().isClientSide) {
                openCustomInventoryScreen(player);
            }
            return InteractionResult.sidedSuccess(level().isClientSide);
        }
        if (isVehicle()) {
            return InteractionResult.PASS;
        }
        if (!level().isClientSide) {
            return player.startRiding(this) ? InteractionResult.CONSUME : InteractionResult.PASS;
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public void openCustomInventoryScreen(Player player) {
        player.openMenu(new SimpleMenuProvider(
                (id, inventory, p) -> new JetsonMenu(id, inventory, fuelContainer, dataAccess, this),
                Component.translatable("container.jetsonmod.jetson")));
    }

    /** True when the aircraft is resting on (or right above) something solid, so the pilot may climb out. */
    public boolean isLanded() {
        return onGround() || isInWater() || !level().noCollision(this, getBoundingBox().move(0.0, -0.5, 0.0));
    }

    @Override
    public boolean causeFallDamage(float fallDistance, float multiplier, DamageSource source) {
        return false;
    }

    @Override
    protected void checkFallDamage(double y, boolean onGround, BlockState state, BlockPos pos) {
        this.resetFallDistance();
    }

    // ---------------------------------------------------------------- damage / breaking

    @Override
    public boolean isPickable() {
        return !isRemoved();
    }

    @Override
    public boolean canBeCollidedWith() {
        return false;
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (isInvulnerableTo(source)) {
            return false;
        }
        if (level().isClientSide || isRemoved()) {
            return true;
        }
        Entity attacker = source.getEntity();
        if (attacker != null && hasPassenger(attacker)) {
            return false;
        }
        boolean creative = attacker instanceof Player player && player.getAbilities().instabuild;
        float damage = entityData.get(DATA_DAMAGE) + amount * 10.0F;
        entityData.set(DATA_DAMAGE, damage);
        markHurt();
        if (creative || damage > 40.0F) {
            if (!creative) {
                spawnAtLocation(new ItemStack(JetsonMod.JETSON_ITEM.get()));
            }
            Containers.dropContents(level(), blockPosition(), fuelContainer);
            discard();
        }
        return true;
    }

    // ---------------------------------------------------------------- save / load

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        setBurnTime(tag.getInt("BurnTime"));
        setMaxBurnTime(tag.getInt("MaxBurnTime"));
        fuelContainer.setItem(0, tag.contains("Fuel")
                ? ItemStack.of(tag.getCompound("Fuel"))
                : ItemStack.EMPTY);
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        tag.putInt("BurnTime", getBurnTime());
        tag.putInt("MaxBurnTime", getMaxBurnTime());
        ItemStack fuel = fuelContainer.getItem(0);
        if (!fuel.isEmpty()) {
            tag.put("Fuel", fuel.save(new CompoundTag()));
        }
    }

    @Override
    public ItemStack getPickResult() {
        return new ItemStack(JetsonMod.JETSON_ITEM.get());
    }
}

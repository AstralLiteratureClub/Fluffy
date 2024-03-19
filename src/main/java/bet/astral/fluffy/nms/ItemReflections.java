package bet.astral.fluffy.nms;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.TagParser;
import org.bukkit.inventory.ItemStack;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class ItemReflections extends Reflections {
	private static final Class<?> craftItemStackClass = getOBC("inventory.CraftItemStack");
	private static final Method asBukkitCopyMethod;
	private static final Method asNMSCopyMethod;

	static {
		try {
			asBukkitCopyMethod = craftItemStackClass.getMethod("asBukkitCopy", net.minecraft.world.item.ItemStack.class);
			asNMSCopyMethod = craftItemStackClass.getMethod("asNMSCopy", ItemStack.class);
		} catch (NoSuchMethodException e) {
			throw new RuntimeException(e);
		}
	}

	public static ItemStack fromNBT(String nbt){
		try {
			return fromNMS(net.minecraft.world.item.ItemStack.of(TagParser.parseTag(nbt)));
		} catch (CommandSyntaxException e) {
			throw new RuntimeException(e);
		}
	}
	public static String toNBT(ItemStack itemStack){
		return toNMS(itemStack).save(new CompoundTag()).toString();
	}
	private static ItemStack fromNMS(net.minecraft.world.item.ItemStack nms){
		try {
			return (ItemStack) asBukkitCopyMethod.invoke(null, nms);
		} catch (IllegalAccessException | InvocationTargetException e) {
			throw new RuntimeException(e);
		}
	}
	private static net.minecraft.world.item.ItemStack toNMS(ItemStack nms){
		try {
			return (net.minecraft.world.item.ItemStack) asNMSCopyMethod.invoke(null, nms);
		} catch (IllegalAccessException | InvocationTargetException e) {
			throw new RuntimeException(e);
		}
	}
}

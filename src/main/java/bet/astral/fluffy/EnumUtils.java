package bet.astral.fluffy;

public final class EnumUtils {
	private EnumUtils(){
		throw new RuntimeException("EnumUtils should not be initialized!");
	}

	public static <T extends Enum<T>> T valueOf(String name, T defaultValue){
		for (Enum<?> enumConstant : defaultValue.getClass().getEnumConstants()) {
			if (enumConstant.name().equalsIgnoreCase(name)){
				//noinspection unchecked
				return (T) enumConstant;
			}
		}
		return defaultValue;
	}
}

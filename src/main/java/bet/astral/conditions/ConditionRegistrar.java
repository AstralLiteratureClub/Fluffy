package bet.astral.conditions;

import bet.astral.conditions.automation.AutomatedCondition;
import bet.astral.conditions.conditions.Condition;
import bet.astral.messenger.Messenger;
import bet.astral.messenger.message.message.IMessage;
import io.github.classgraph.ClassGraph;
import io.github.classgraph.ClassInfo;
import io.github.classgraph.ScanResult;
import net.kyori.adventure.text.Component;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ConditionRegistrar {
	private final Messenger<?> messenger;
	private final Map<String, Condition<?, ?>> registeredConditions = new HashMap<>();

	public ConditionRegistrar(Messenger<?> messenger) {
		this.messenger = messenger;
		registerConditions(Condition.class.getPackage());
	}

	public List<IMessage<?, Component>> loadMessages(String key) {
		return null;
	}

	public boolean accept(Definition<?, ?>... definitions){
		for (Definition<?, ?> definition : definitions) {
			if (definition == null) {
				continue;
			}
			Condition<?, ?> condition = registeredConditions.get(definition.condition());
			if (condition == null) {
				continue;
			}
			if (!condition.getType().isSubtypeOf(definition.getObjectType())) {
				continue;
			}
			if (!condition.allowsNullValue() && definition.object() == null) {
				return false;
			}
			if (!condition.checkInternal(definition.object(), definition.required())){
				return false;
			}
		}
		return true;
	}

	public void registerCondition(String name, Condition<?, ?> condition){
		registeredConditions.put(name, condition);
	}


	public void registerConditions(Package conditionPackage){
		ClassGraph graph = null;
		ScanResult scanResult = null;
		try {
			graph = new ClassGraph().verbose()
					.enableAllInfo()
					.acceptPackages(conditionPackage.getName());
			scanResult = graph.scan();
			for (ClassInfo classInfo : scanResult.getClassesWithAnnotation(AutomatedCondition.class)){
				Class<?> clazz = classInfo.loadClass();
				AutomatedCondition automatedCondition = clazz.getAnnotation(AutomatedCondition.class);
				String name = automatedCondition.name();
				Condition<?, ?> conditionInstance = (Condition<?, ?>) clazz.getConstructor().newInstance();
				registerCondition(name, conditionInstance);
			}
		} catch (InvocationTargetException | InstantiationException | IllegalAccessException | NoSuchMethodException e) {
			throw new RuntimeException(e);
		} finally {
			if (scanResult != null) {
				scanResult.close();
			}
		}
	}
}

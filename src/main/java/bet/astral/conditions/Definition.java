package bet.astral.conditions;

public record Definition<Object, Value>(String condition, Object object, Value required) {
	Class<Object> getObjectType(){
		//noinspection unchecked
		return (Class<Object>) object.getClass();
	}
	Class<Value> getRequiredType() {
		//noinspection unchecked
		return (Class<Value>) required.getClass();
	}
}

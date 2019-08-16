package Context;

import com.alibaba.fastjson.JSONArray;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Context
{
    private final Map<ContextType, Object> contextTypeObjectMap = new ConcurrentHashMap<>();

    public enum ContextType
    {
        NewestPrice(Float.class),
        NewestTime(Long.class),
        PriceToday(JSONArray.class),
        ;

        private Class clazz;

        ContextType(Class clazz) {
            this.clazz = clazz;
        }
    }

    public <T> T add(final ContextType type, T value)
    {
        if (!type.clazz.isAssignableFrom(value.getClass())) {
            return null;
        }

        return (T) contextTypeObjectMap.put(type, value);
    }


    public <T> T get(ContextType type, T defaultValue)
    {
        if (!contextTypeObjectMap.containsKey(type)) {
            return defaultValue;
        }

        final Object attribute = contextTypeObjectMap.get(type);
        if (defaultValue.getClass().isAssignableFrom(type.clazz)) {
            return (T) attribute;
        }

        return defaultValue;
    }

    public boolean remove(final ContextType contextType)
    {
        return contextTypeObjectMap.remove(contextType) != null;
    }

}

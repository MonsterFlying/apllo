package com.gofobao.framework.marketing.serializer;

import com.gofobao.framework.marketing.enums.MarketingTypeEnum;
import com.google.gson.*;
import org.springframework.util.StringUtils;

import java.lang.reflect.Type;

public class MarketingEnumSerializer implements JsonSerializer<MarketingTypeEnum>,
        JsonDeserializer<MarketingTypeEnum> {
    @Override
    public MarketingTypeEnum deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        if (StringUtils.isEmpty(jsonElement.getAsString())) {
            return null;
        }

        for (MarketingTypeEnum key : MarketingTypeEnum.values()) {
            return key;
        }

        return null;
    }

    @Override
    public JsonElement serialize(MarketingTypeEnum marketingTypeEnum, Type type, JsonSerializationContext jsonSerializationContext) {
        return new JsonPrimitive(marketingTypeEnum.getValue());
    }
}

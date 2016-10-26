package com.stripe.model;

import com.google.gson.*;

import java.lang.reflect.Type;

public class ExpandableFieldDeserializer implements JsonDeserializer<ExpandableField> {
    public ExpandableField<Type> deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        Gson gson = new GsonBuilder()
                .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
                .create();

        if (json.isJsonNull()) {
            return null;
        }

        ExpandableField<Type> expandableField;

        // Check if json is a String ID:
        if (json.isJsonPrimitive()) {
            JsonPrimitive jsonPrimitive = json.getAsJsonPrimitive();
            if (jsonPrimitive.isString()) {
                expandableField = new ExpandableField<Type>(jsonPrimitive.getAsString(), null);
                return expandableField;
            }
            else {
                throw new JsonParseException("ExpandableField is a non-string primitive type.");
            }
        }
        // Check if json is an expanded Object:
        else if (json.isJsonObject()) {
            // Get the `id` out of the response
            JsonObject fieldAsJsonObject = json.getAsJsonObject();
            String id = fieldAsJsonObject.getAsJsonPrimitive("id").getAsString();
            // Create the expanded object
            Type parsedData = gson.fromJson(json, typeOfT);
            expandableField = new ExpandableField<Type>(id, parsedData);
            return expandableField;
        }

        // If json is neither a String nor an Object, error:
        throw new JsonParseException("ExpandableField is a non-object, non-primitive type.");
    }
}
package org.egov.edcr.web.adaptor;

import java.lang.reflect.Type;
import java.text.SimpleDateFormat;

import org.egov.edcr.entity.EdcrApplication;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
public class EdcrApplicationJsonAdaptor implements JsonSerializer<EdcrApplication>
{
    @Override
    public JsonElement serialize(final EdcrApplication edcrApplication, final Type type,final JsonSerializationContext jsc) 
    {
        final JsonObject jsonObject = new JsonObject();
        SimpleDateFormat ddmmyyyy=new SimpleDateFormat("dd/MM/yyyy");
        if (edcrApplication != null)
        {
            if(edcrApplication.getApplicationNumber()!=null)
                jsonObject.addProperty("applicationNumber", edcrApplication.getApplicationNumber());
            else
                jsonObject.addProperty("applicationNumber","");
            if(edcrApplication.getDcrNumber()!=null)
                jsonObject.addProperty("dcrNumber", edcrApplication.getDcrNumber());
            else
                jsonObject.addProperty("dcrNumber","");
            if(edcrApplication.getApplicationDate()!=null)
                jsonObject.addProperty("applicationDate", ddmmyyyy.format(edcrApplication.getApplicationDate()));
            else
                jsonObject.addProperty("applicationDate","");
            jsonObject.addProperty("id", edcrApplication.getId());
        } 
        return jsonObject;  }
}
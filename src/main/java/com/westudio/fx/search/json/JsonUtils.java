/**
 *
 *	Copyright (c) 2001 - 2011 fazhang
 *
 */
package com.westudio.fx.search.json;

import java.io.IOException;
import java.util.Date;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.FastDateFormat;

import com.westudio.fx.search.exception.ApplicationException;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;

/**
 * @author fayou_zhang
 *
 */
public class JsonUtils {
	private static final ObjectMapper DEFAULT_MAPPER = new ObjectMapper();
	static {
		DEFAULT_MAPPER.configure(JsonGenerator.Feature.QUOTE_FIELD_NAMES, true);
		DEFAULT_MAPPER.setSerializationInclusion(Include.NON_NULL);
	}

	static class JsonDateSerializer extends JsonSerializer<Date> {
		private static final FastDateFormat fdf = FastDateFormat
				.getInstance("YYYY-MM-dd HH:mm:ss");

		@Override
		public void serialize(Date date, JsonGenerator gen,
				SerializerProvider provider) throws IOException,
				JsonProcessingException {
			gen.writeString(fdf.format(date));
		}
	}

	public static final String toJson(Object obj) throws ApplicationException {
		try {
			return DEFAULT_MAPPER.writeValueAsString(obj);
		} catch (Exception e) {
			throw new ApplicationException("Write to json error:"
					+ e.getMessage() + "[" + obj + "].", e);
		}
		// return new Gson().toJson(obj);
	}

	public static final <T> T fromJson(String jsonString, Class<T> clazz) throws ApplicationException {
		if (StringUtils.isEmpty(jsonString)) {
			return null;
		}

		try {
			return DEFAULT_MAPPER.readValue(jsonString, clazz);
		} catch (IOException e) {
			throw new ApplicationException("Parse json string error:["
					+ jsonString + "].", e);
		}
	}
}

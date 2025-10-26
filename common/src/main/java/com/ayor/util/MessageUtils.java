package com.ayor.util;

import com.ayor.entity.wsMessage.ResultMessage;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MessageUtils {

    public static <T> String getMessage (boolean isSystemMessage,
                                         String fromName,
                                         T message) {
        try {
            ResultMessage<T> result = new ResultMessage<>();
            result.setIsSystem(isSystemMessage);
            result.setMessage(message);
            if (fromName != null) {
                result.setFromUser(fromName);
            }
            ObjectMapper objectMapper = new ObjectMapper();

            return objectMapper.writeValueAsString(result);
        } catch (JsonProcessingException e) {
            log.error("json转换异常", e);
        }
        return null;
    }

}
